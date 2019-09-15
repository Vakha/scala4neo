package scala4neo.codec

import io.circe.Encoder
import scala4neo.util.TypeName
import scala.reflect.runtime.universe.TypeTag

abstract class NodeCodec[T : TypeTag : Encoder] extends ValueCodec[T]{
  override def label: String = TypeName[T]
}