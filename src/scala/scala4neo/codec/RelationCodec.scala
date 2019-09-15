package scala4neo.codec

import io.circe.Encoder
import scala4neo.util.TypeName
import scala.reflect.runtime.universe.TypeTag

abstract class RelationCodec[T : TypeTag : Encoder] extends ValueCodec[T]{
  override def label: String = camelToUpperSnakeCase(TypeName[T])

  private def camelToUpperSnakeCase(name: String): String =
    "[A-Z\\d]".r.replaceAllIn(name,  "_" + _.group(0) ).drop(1).toUpperCase
}
