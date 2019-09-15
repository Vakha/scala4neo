package scala4neo.databaseimpl

import cats.effect.ExitCase
import com.typesafe.config.{Config => TypesafeConfig}
import monix.eval.Task
import org.neo4j.driver.v1.{Transaction => DriverTransaction, _}
import scala4neo.database._
import scala4neo.query.CypherQuery
import scala4neo.util.config._
import scala4neo.util.logging._
import scala4neo.util.task._

import scala.collection.JavaConverters._
import scala.reflect.runtime.universe.TypeTag

class NeoDatabase(val config: TypesafeConfig) extends
  Database with
  Logs {

  private val host = config.getString("neo4j.host")
  private val port = config.getInt("neo4j.port")
  private val uri = s"bolt://$host:$port"
  private val usernameOpt = config.getStringOpt("neo4j.user")
  private val passwordOpt = config.getStringOpt("neo4j.password")

  private val creds: Option[(String, String)] = for {
    u <- usernameOpt
    p <- passwordOpt
  } yield (u, p)

  private val authToken = creds match {
    case None => AuthTokens.none()
    case Some((username, password)) => AuthTokens.basic(username, password)
  }

  private val driver: Driver = GraphDatabase.driver(uri, authToken)

  def read[Q <: CypherQuery]
          (query: Q)
          (implicit tx: Transaction = ReadAutoCommitTransaction,
           tt: TypeTag[Q]): Task[Seq[Record]] = {
    logger.debug(s"read: ${tt.tpe.toString} statement: ${query.statement}; parameters: ${query.parameters}")
    tx match {
      case tx if tx.isAutoCommit =>
        readAutoCommit(query)
      case WriteNonAutoCommitTransaction(value) =>
        readInTx(query, value)
    }
  }

  def write[Q <: CypherQuery]
           (query: Q)
           (implicit tx: WriteTransaction = WriteAutoCommitTransaction,
            tt: TypeTag[Q]): Task[Boolean] = {
    logger.debug(s"write: ${tt.tpe.toString} statement: ${query.statement}; parameters: ${query.parameters}")
    tx match {
      case WriteAutoCommitTransaction =>
        writeAutoCommit(query)
      case WriteNonAutoCommitTransaction(tx) =>
        writeInTx(query, tx)
    }
  }

    def withWriteTransaction[T](block: WriteTransaction => Task[T]): Task[T] = {
      val acquireSession = Task.eval(driver.session(AccessMode.WRITE))
      acquireSession.bracket { session =>
        val beginTransaction = session.beginTransactionAsync().toTask
        beginTransaction.bracketCase { tx =>
          logger.debug(s"Started transaction: ${tx.hashCode}")
          val txWrapped = WriteNonAutoCommitTransaction(tx)
          block(txWrapped)
        } { // end transaction part
          case (tx, ExitCase.Completed) =>
            logger.debug(s"Committing transaction: ${tx.hashCode}")
            tx.commitAsync().toTask.void
          case (tx, ExitCase.Error(e)) =>
            logger.debug(s"Rollback transaction: ${tx.hashCode} due to error: $e")
            tx.rollbackAsync().toTask.void
          case (tx, ExitCase.Canceled) =>
            logger.debug(s"Rollback transaction: ${tx.hashCode} due to cancel")
            tx.rollbackAsync().toTask.void
        }
      } { session => // release session part
        session.closeAsync().toTask.void
      }
    }

  def stop(): Unit =
    driver.close()

  private def readAutoCommit(query: CypherQuery): Task[Seq[Record]] = {
    val acquireSession = Task.eval(driver.session(AccessMode.READ))
    acquireSession.bracket { session =>
      for {
        resultCursor <- session.runAsync(query.statement, query.parameters).toTask
        records <- resultCursor.listAsync().toTask
        _ <- session.closeAsync().toTask
      } yield records.asScala
    } { session => // release part
      session.closeAsync().toTask.void
    }
  }

  private def readInTx(query: CypherQuery, tx: DriverTransaction): Task[Seq[Record]] = {
    for {
      resultCursor <- tx.runAsync(query.statement, query.parameters).toTask
      records <- resultCursor.listAsync().toTask
    } yield records.asScala
  }

  private def writeInTx(query: CypherQuery, tx: DriverTransaction): Task[Boolean] = {
    for {
      res <- tx.runAsync(query.statement, query.parameters).toTask
      summary <- res.summaryAsync.toTask
    } yield summary.counters.containsUpdates
  }

  private def writeAutoCommit(query: CypherQuery): Task[Boolean] = {
    val acquireSession = Task.eval(driver.session(AccessMode.READ))
    acquireSession.bracket { session =>
      for {
        res <- session.runAsync(query.statement, query.parameters).toTask
        summary <- res.summaryAsync().toTask
        _ <- session.closeAsync().toTask
      } yield summary.counters.containsUpdates
    } { session => // release part
      session.closeAsync().toTask.void
    }
  }

}
