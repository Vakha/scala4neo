package scala4neo.repository

import com.typesafe.config.ConfigFactory
import monix.eval.Task
import monix.execution.Scheduler
import org.scalatest.{FlatSpec, OptionValues}
import scala4neo.data.model._
import scala4neo.data.query._
import scala4neo.database.{Database, WriteTransaction}
import scala4neo.databaseimpl.NeoDatabase
import scala4neo.query.pagination._
import scala4neo.testkit.NeoEntitiesSaver
import scala4neo.util.rnd.rnd

class GenericRepositorySpec extends
  FlatSpec with
  OptionValues with
  NeoEntitiesSaver {

  import scala4neo.codec.implicits._
  import scala4neo.data.codec._
  import scala4neo.modeltestkit.all._

  override implicit protected def scheduler: Scheduler = Scheduler.global

  private val config = ConfigFactory.load()

  private val neo4jDB: Database = new NeoDatabase(config)
  override val genericRepo = new GenericRepository(neo4jDB)

  it should "find, save, update and delete node by id" in {
    val person = rnd[Person]

    genericRepo.saveOrUpdateNode(person).runToFuture.await
    val personFromDB = genericRepo.findNodeById[Person](person.id).runToFuture.await.value
    assert(personFromDB == person)

    val personToUpdate = rnd[Person].copy(id = person.id)

    genericRepo.saveOrUpdateNode(personToUpdate).runToFuture.await
    val updatedPersonFromDB = genericRepo.findNodeById[Person](person.id).runToFuture.await.value
    assert(updatedPersonFromDB != personFromDB)
    assert(updatedPersonFromDB == personToUpdate)

    genericRepo.deleteNode(person).runToFuture.await
    assert(genericRepo.findNodeById[Person](person.id).runToFuture.await.isEmpty)
  }

  it should "find, save, update and delete relation by id" in {
    val person = rnd[Person].save
    val movie = rnd[Movie].save
    val actedIn = rnd[ActedIn]

    genericRepo.saveRelation(person, actedIn, movie).runToFuture.await
    val actedIdFromDB = genericRepo.findRelationById(actedIn.id).runToFuture.await.value
    assert(actedIdFromDB == actedIn)

    val actedInToUpdate = rnd[ActedIn].copy(id = actedIn.id)
    genericRepo.updateRelation(actedInToUpdate).runToFuture.await

    val updatedActedIdFromDB = genericRepo.findRelationById(actedIn.id).runToFuture.await.value
    assert(updatedActedIdFromDB != actedIn)
    assert(updatedActedIdFromDB == actedInToUpdate)

    genericRepo.deleteRelation(actedIn).runToFuture.await

    assert(genericRepo.findRelationById(actedIn.id).runToFuture.await.isEmpty)
  }

  it should "delete relations" in {
    val person = rnd[Person].save
    val movie1 = rnd[Movie].save
    val movie2 = rnd[Movie].save
    val actedIn1 = rnd[ActedIn]
    val actedIn2 = rnd[ActedIn]

    genericRepo.saveRelation(person, actedIn1, movie1).runToFuture.await
    genericRepo.saveRelation(person, actedIn2, movie2).runToFuture.await

    genericRepo.deleteRelations(Seq(actedIn1, actedIn2)).runToFuture.await

    assert(genericRepo.findRelationById(actedIn1.id).runToFuture.await.isEmpty)
    assert(genericRepo.findRelationById(actedIn2.id).runToFuture.await.isEmpty)
  }

  it should "fail if relation is not saved" in {
    val person = rnd[Person].save
    val movie = rnd[Movie] // no save
    val actedIn = rnd[ActedIn]

    val error = genericRepo.saveRelation(person, actedIn, movie).runToFuture.failed.await
    assert(error.isInstanceOf[RelationNotSavedException])
  }

  it should "fail if relation does not exist" in {
    val actedIn = rnd[ActedIn]

    val error = genericRepo.updateRelation(actedIn).runToFuture.failed.await
    assert(error.isInstanceOf[RelationNotUpdatedException])
  }

  behavior of "PersonByIds"

  it should "find all by query with paging" in {
    val persons = (1 to 10).map(_ => rnd[Person].save).sortBy(_.name)

    val ids = persons.map(_.id)

    val firstPage = persons.take(6)
    val secondPage = persons.drop(5)

    val firstPageRequest = PageRequest(offset = 0, size = 5, sort = Seq("n.name".asc))
    val queryFirstPage = PersonByIds(ids, firstPageRequest)

    val secondPageRequest = firstPageRequest.copy(offset = 5)
    val querySecondPage = PersonByIds(ids, secondPageRequest)

    val firstPageFromDB = genericRepo.findAllBy(queryFirstPage).runToFuture.await
    assert(firstPageFromDB == firstPage)

    val secondPageFromDB = genericRepo.findAllBy(querySecondPage).runToFuture.await
    assert(secondPageFromDB == secondPage)
  }

  behavior of "FindAllPersonsActedInMovieByIds"

  it should "find custom all by query with paging" in {
    val persons = (1 to 4).map(_ => rnd[Person].save)
    val movies = (1 to 4).map(_ => rnd[Movie].save)

    val rels = for {
      person <- persons
      movie <- movies
    } yield {
      val actedIn = (person, movie).connectBy(rnd[ActedIn])
      QueryRes(person, actedIn, movie)
    }

    val sortedRels = rels.sortWith { (a, b) =>
      val aPerson = a.person.name
      val bPerson = b.person.name
      val aMovie = a.movie.name
      val bMovie = b.movie.name
      val res = aPerson.compareTo(bPerson) match {
        case 0 => aMovie.compareTo(bMovie)
        case other => other
      }
      res < 0
    }

    val ids = persons.map(_.id)

    val firstPage = sortedRels.take(9)
    val secondPage = sortedRels.drop(8)

    val firstPageRequest = PageRequest(offset = 0, size = 8, sort = Seq("person.name".asc, "movie.name".asc))
    val firstPageQuery = FindAllPersonsActedInMovieByIds(ids, firstPageRequest)

    val firstPageFromDB = genericRepo.findCustomAllBy(firstPageQuery).runToFuture.await
    assert(firstPageFromDB == firstPage)

    val secondPageRequest = firstPageRequest.copy(offset = 8)
    val secondPageQuery = FindAllPersonsActedInMovieByIds(ids, secondPageRequest)

    val secondPageFromDB = genericRepo.findCustomAllBy(secondPageQuery).runToFuture.await
    assert(secondPageFromDB == secondPage)
  }

  behavior of "FindPersonById"

  it should "find custom one by query" in {
    val person = rnd[Person].save
    val query = FindPersonById(person.id)

    withClue("none if nothing is found") {
      assert(genericRepo.findCustomOneBy(query).runToFuture.await.isEmpty)
    }

    val movie1 = rnd[Movie].save
    val actedIn1 = (person, movie1).connectBy(rnd[ActedIn])

    withClue("successfully return value") {
      val res = genericRepo.findCustomOneBy(query).runToFuture.await
      assert(res.contains(QueryRes(person, actedIn1, movie1)))
    }

    val movie2 = rnd[Movie].save
    (person, movie2).connectBy(rnd[ActedIn])

    withClue("fail if got more than one result") {
      val error = genericRepo.findCustomOneBy(query).runToFuture.failed.await
      assert(error == TooManyRowsException(1, 2))
    }
  }

  behavior of "FindPersonByName"

  it should "find one by" in {
    val person = rnd[Person].save
    val query = FindPersonByName(person.name)
    val personFromDB = genericRepo.findOneBy(query).runToFuture.await
    assert(personFromDB.contains(person))
  }

  it should "handle transaction" in {
    val person = rnd[Person]
    neo4jDB.withWriteTransaction { implicit tx: WriteTransaction =>
      genericRepo.saveOrUpdateNode(person).runToFuture.await
      val personFromDB = genericRepo.findNodeById[Person](person.id).runToFuture.await
      assert(personFromDB.contains(person))
      Task.raiseError(new RuntimeException(""))
    }.runToFuture.failed.await
    val personFromDB = genericRepo.findNodeById[Person](person.id).runToFuture.await
    assert(personFromDB.isEmpty)
  }

  it should "exists by" in {
    val person = rnd[Person].save

    val existsQuery = FindPersonByName(person.name)
    val personExists = genericRepo.existsBy(existsQuery).runToFuture.await
    assert(personExists)

    val notExistsQuery = FindPersonByName(rnd[String])
    val personNotExists = genericRepo.existsBy(notExistsQuery).runToFuture.await
    assert(!personNotExists)
  }

  behavior of "CountPersons"

  it should "count by" in {
    val name = "Boris"
    val boris1 = rnd[Person].copy(name = name).save
    val boris2 = rnd[Person].copy(name = name).save
    val notBoris = rnd[Person].save
    val query = CountPersons(name)
    val countFromDb = genericRepo.countBy(query).runToFuture.await
    assert(countFromDb == 2)
  }

}
