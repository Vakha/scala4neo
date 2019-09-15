package scala4neo.data

import java.util.UUID
import scala4neo.model.{Node, Relation}

package object model {

  case class Person(
    id: UUID,
    name: String,
  ) extends Node

  case class Movie(
    id: UUID,
    name: String,
  ) extends Node

  case class ActedIn(
    id: UUID,
    roles: Seq[String]
  ) extends Relation

  case class QueryRes(
    person: Person,
    actedIn: ActedIn,
    movie: Movie
  )

}