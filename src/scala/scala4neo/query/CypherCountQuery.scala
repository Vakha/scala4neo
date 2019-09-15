package scala4neo.query

import scala4neo.model.CountRecord

trait CypherCountQuery extends CypherQuery {
  type R = CountRecord
}
