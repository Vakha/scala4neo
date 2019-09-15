package scala4neo.codec

import io.circe.{Encoder, Printer}
import org.neo4j.driver.v1.Value
import scala.reflect.runtime.universe.TypeTag

/**
  * Typeclass that provide functionality to convert case classes to json-like format to store in Neo4j
  * and convert [[Value]] we getting from Neo4j driver to case class
  */
abstract class ValueCodec[T : TypeTag : Encoder] {

  private val printer = Printer.noSpaces.copy(
    // add space before " in array to replacing it by regexp in toJson
    arrayCommaRight = " "
  )

  def label: String

  def convert(record: Value): T

  def toJsonWithLabel(t: T): String = s"$label ${toJson(t)}"

  def toJson(t: T): String = {
    // Neo4j expects keys to be not in quotes
    printer.pretty(Encoder[T].apply(t))
      .replace("{\"", "{")
      .replace(",\"", ",")
      .replace("\":", ":")
  }

}

object ValueCodec {
  def apply[T](implicit instance: ValueCodec[T]): ValueCodec[T] = instance
}
