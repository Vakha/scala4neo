package scala4neo.query.pagination

import enumeratum.EnumEntry.Uppercase
import enumeratum.{Enum, EnumEntry}
import scala.collection.immutable.IndexedSeq

case class SortOrder(field: String, direction: Direction)

sealed trait Direction extends EnumEntry with Uppercase

object Direction extends Enum[Direction] {

  case object ASC extends Direction
  case object DESC extends Direction

  val values: IndexedSeq[Direction] = findValues
}

