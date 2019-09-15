package scala4neo.query

import scala4neo.model.NRecord

trait CypherNQuery extends CypherQuery {
  type V
  type R = NRecord[V]
}

object CypherNQuery {
  trait Aux[V1] extends CypherNQuery { type V = V1 }
}