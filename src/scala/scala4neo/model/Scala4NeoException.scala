package scala4neo.model

class Scala4NeoException(
  message: String,
  cause: Option[Throwable] = None,
  parameters: Map[String, String] = Map.empty
) extends Exception(message, cause.orNull)

object Scala4NeoException {
  def apply(exception: Throwable): Scala4NeoException =
    new Scala4NeoException(
      message = exception.getMessage,
      cause = Some(exception)
    )
}