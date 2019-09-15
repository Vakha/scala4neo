package scala4neo.model

import java.util.UUID

trait Entity {
  val id: UUID
}

trait Node extends Entity
trait Relation extends Entity
