package scala4neo.database

import com.typesafe.config.ConfigFactory
import monix.eval.Task
import org.scalatest.FlatSpec
import scala4neo.data.model.Person
import scala4neo.databaseimpl.NeoDatabase
import scala4neo.modeltestkit.all._
import scala4neo.query.GenericQueries
import scala4neo.util.future.AwaitHelper

class DatabaseSpec extends
  FlatSpec with
  AwaitHelper {

  import monix.execution.Scheduler.Implicits.global
  import scala4neo.data.codec._
  import scala4neo.util.rnd.rnd

  private val config = ConfigFactory.load()
  private val database: Database = new NeoDatabase(config)

  it should "write and read without transaction" in {
    val person = rnd[Person]
    val label = personValueCodec.label
    val json = personValueCodec.toJson(person)
    val saveQuery = GenericQueries.SaveOrUpdateNode[Person](label, person.id, json)
    val isSaved = database.write(saveQuery).runToFuture.await
    assert(isSaved)

    val findQuery = GenericQueries.NodeById[Person](label, person.id)
    val res = database.read(findQuery).runToFuture.await
    assert(res.size == 1)

    val record = res.head.get("n")

    assert(record.get("id").asString == person.id.toString)
    assert(record.get("name").asString == person.name)
  }

  it should "write and read within transaction" in {
    val person = rnd[Person]
    val label = personValueCodec.label
    val json = personValueCodec.toJson(person)

    val saveQuery = GenericQueries.SaveOrUpdateNode[Person](label, person.id, json)
    val findQuery = GenericQueries.NodeById[Person](label, person.id)

    database.withWriteTransaction { implicit tx: WriteTransaction =>
      val isSaved = database.write(saveQuery).runToFuture.await
      assert(isSaved)
      val res = database.read(findQuery).runToFuture.await
      assert(res.size == 1)

      val record = res.head.get("n")

      assert(record.get("id").asString == person.id.toString)
      assert(record.get("name").asString == person.name)
      Task.unit
    }.runToFuture.await

    withClue("writen data is committed") {
      val res = database.read(findQuery).runToFuture.await
      assert(res.size == 1)

      val record = res.head.get("n")

      assert(record.get("id").asString == person.id.toString)
      assert(record.get("name").asString == person.name)
    }
  }

  it should "NOT commit if transaction has failed" in {
    val person = rnd[Person]
    val label = personValueCodec.label
    val json = personValueCodec.toJson(person)

    val saveQuery = GenericQueries.SaveOrUpdateNode[Person](label, person.id, json)
    val findQuery = GenericQueries.NodeById[Person](label, person.id)

    database.withWriteTransaction { implicit tx: WriteTransaction =>
      val isSaved = database.write(saveQuery).runToFuture.await
      assert(isSaved)
      val res = database.read(findQuery).runToFuture.await
      assert(res.size == 1)

      val record = res.head.get("n")

      assert(record.get("id").asString == person.id.toString)
      assert(record.get("name").asString == person.name)
      Task.raiseError(new RuntimeException("ERROR!!"))
    }.runToFuture.failed.await

    withClue("writen data is NOT committed") {
      val res = database.read(findQuery).runToFuture.await
      assert(res.isEmpty)
    }
  }

}