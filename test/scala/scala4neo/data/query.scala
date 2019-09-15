package scala4neo.data

import java.util.UUID
import scala4neo.data.model.{Person, QueryRes}
import scala4neo.query.{CypherCountQuery, CypherNQuery, CypherQuery}
import scala4neo.query.pagination.PageRequest
import scala4neo.data.codec._
import scala.collection.JavaConverters._

package object query {

  private val Person = personValueCodec.label
  private val ACTED_IN = actedInValueCodec.label
  private val Movie = movieValueCodec.label

  case class PersonByIds(ids: Seq[UUID], page: PageRequest) extends CypherNQuery.Aux[Person] {
    override val query = s"MATCH (n: $Person) WHERE n.id IN {personIds} RETURN n"
    override val pageRequest = Some(page)
    override val paramMap: Map[String, Any] = Map(
      "personIds" -> ids.map(_.toString).asJava
    )
  }

  case class FindAllPersonsActedInMovieByIds(
    ids: Seq[UUID],
    page: PageRequest
  ) extends CypherQuery.Aux[QueryRes] {
    override val query: String =
      s"MATCH (person: $Person)-[actedIn: $ACTED_IN]->(movie: $Movie) " +
      s"WHERE person.id IN {personIds} " +
      s"RETURN person, actedIn, movie"
    override val pageRequest = Some(page)
    override val paramMap: Map[String, Any] = Map(
      "personIds" -> ids.map(_.toString).asJava
    )
  }

  case class FindPersonById(id: UUID) extends CypherQuery.Aux[QueryRes] {
    override val query: String =
      s"MATCH (person: $Person)-[actedIn: $ACTED_IN]->(movie: $Movie) " +
      s"WHERE person.id = {id} " +
      s"RETURN person, actedIn, movie"
    override val paramMap: Map[String, Any] = Map(
      "id" -> id.toString
    )
  }

  case class FindPersonByName(name: String) extends CypherNQuery.Aux[Person] {
    override val query: String = s"""MATCH (n:$Person { name: {name} }) RETURN n"""
    override val paramMap: Map[String, Any] = Map(
      "name" -> name
    )
  }

  case class CountPersons(name: String) extends CypherCountQuery {
    override val query: String = s"""MATCH (n:$Person {name: {name} }) RETURN COUNT(n) AS count"""
    override val paramMap: Map[String, Any] = Map(
      "name" -> name
    )
  }

}
