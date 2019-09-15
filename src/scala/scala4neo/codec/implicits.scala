package scala4neo.codec

import org.neo4j.driver.v1.{Record, Value}
import scala4neo.model.{CountRecord, NRecord}

package object implicits {

  implicit val nRecordLongCodec: RecordCodec[NRecord[Long]] =
    (record: Record) => NRecord(
      n = record.get("n").asLong
    )

  implicit def nRecordCodec[N : ValueCodec]: RecordCodec[NRecord[N]] =
    (record: Record) => NRecord(
      n = record.get("n").convertTo[N]
    )

  implicit val countRecordCodec: RecordCodec[CountRecord] =
    (record: Record) => CountRecord(
      count = record.get("count").asLong
    )

  implicit class ConvertibleRecord(val record: Record) {
    def convertTo[T](implicit codec: RecordCodec[T]): T = codec.convert(record)
  }

  implicit class ConvertibleValue(val value: Value) {
    def convertTo[T](implicit codec: ValueCodec[T]): T = codec.convert(value)
  }

}
