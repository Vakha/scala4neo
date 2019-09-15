package scala4neo.util

import scala.reflect.runtime.universe.TypeTag

object TypeName {
  def apply[A](implicit typeTag: TypeTag[A]): String =
    typeTag.tpe.typeSymbol.name.toString
}
