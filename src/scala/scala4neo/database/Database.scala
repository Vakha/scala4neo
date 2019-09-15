package scala4neo.database

import monix.eval.Task
import org.neo4j.driver.v1.Record
import scala4neo.query.CypherQuery
import scala.reflect.runtime.universe.TypeTag

trait Database {

  def read[Q <: CypherQuery](query: Q)(implicit tx: Transaction = ReadAutoCommitTransaction, tt: TypeTag[Q]): Task[Seq[Record]]

  def write[Q <: CypherQuery](query: Q)(implicit tx: WriteTransaction = WriteAutoCommitTransaction, tt: TypeTag[Q]): Task[Boolean]

  def withWriteTransaction[T](block: WriteTransaction => Task[T]): Task[T]

  def stop(): Unit

}
