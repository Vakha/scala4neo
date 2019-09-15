package scala4neo.data

import java.util.UUID

import io.circe.Encoder
import org.neo4j.driver.v1.{Record, Value}
import scala4neo.codec.{NodeCodec, RecordCodec, RelationCodec, ValueCodec}
import scala4neo.data.model._

import scala.collection.JavaConverters._

package object codec {

  import scala4neo.codec.implicits._
  import io.circe.generic.semiauto._

  private implicit val movieEncoder: Encoder[Movie] = deriveEncoder[Movie]
  private implicit val personEncoder: Encoder[Person] = deriveEncoder[Person]
  private implicit val actedInEncoder: Encoder[ActedIn] = deriveEncoder[ActedIn]

  implicit val movieValueCodec: ValueCodec[Movie] =
    new NodeCodec[Movie] {
      override def convert(record: Value): Movie =
        Movie(
          id = UUID.fromString(record.get("id").asString),
          name = record.get("name").asString
        )
    }

  implicit val personValueCodec: ValueCodec[Person] =
    new NodeCodec[Person] {
      override def convert(record: Value): Person =
        Person(
          id = UUID.fromString(record.get("id").asString),
          name = record.get("name").asString
        )
    }

  implicit val actedInValueCodec: ValueCodec[ActedIn] =
    new RelationCodec[ActedIn] {
      override def convert(record: Value): ActedIn =
        ActedIn(
          id = UUID.fromString(record.get("id").asString),
          roles = record.get("roles").asList(_.asString).asScala
        )
    }

  implicit val queryResCodec: RecordCodec[QueryRes] = (record: Record) => QueryRes(
    person = record.get("person").convertTo[Person],
    actedIn = record.get("actedIn").convertTo[ActedIn],
    movie = record.get("movie").convertTo[Movie]
  )
}
