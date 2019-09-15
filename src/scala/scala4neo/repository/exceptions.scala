package scala4neo.repository

import scala4neo.model.Scala4NeoException

case class Neo4jException(exception: Throwable) extends Scala4NeoException(
  message = exception.getMessage,
  cause = Some(exception)
)

case class NodeNotSavedException(nodeJson: String) extends Scala4NeoException(
  message = s"Performed operation to store node ($nodeJson), but it hasn't produced any changes in DB",
  parameters = Map(
    "nodeJson" -> nodeJson
  )
)

case class RelationNotSavedException(relationJson: String, sourceJson: String, targetJson: String) extends Scala4NeoException(
  message = s"Performed operation to store relation ($relationJson) between ($sourceJson) and ($targetJson), " +
    s"but it hasn't produced any changes in DB. Probably nodes you try to connect don't exist",
  parameters = Map(
    "relationJson" -> relationJson,
    "sourceJson" -> sourceJson,
    "targetJson" -> targetJson
  )
)

case class RelationNotUpdatedException(relationJson: String) extends Scala4NeoException(
  message = s"Performed operation to update relation ($relationJson), but it hasn't produced any changes in DB",
  parameters = Map(
    "relationJson" -> relationJson,
  )
)

case class TooManyRowsException(expected: Int, actual: Int) extends Scala4NeoException(
  message = s"Expected to get $expected, but got $actual",
  parameters = Map(
    "expected" -> expected.toString,
    "actual" -> actual.toString
  )
)