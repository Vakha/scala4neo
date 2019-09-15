package scala4neo.codec

import org.neo4j.driver.v1.Record

/**
  * Typeclass that provide functionality to convert [[Record]]
  * with objects we selected in query.
  */
trait RecordCodec[T] {
  def convert(record: Record): T
}
