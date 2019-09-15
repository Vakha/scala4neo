package scala4neo.codec

import scala4neo.data.codec._
import scala4neo.data.model.ActedIn
import scala4neo.modeltestkit.actedIn._
import scala4neo.testkit.RelationCodecSpec

class ActedInCodecSpec extends
  RelationCodecSpec[ActedIn] with
  CodecSpecSettings  {

  override def randomEntity: ActedIn =
    actedInArbitrary.arbitrary.sample.get

  override def updateEntity(entityToUpdate: ActedIn, update: ActedIn): ActedIn =
    update.copy(id = entityToUpdate.id)

  behave like correctRelationCodec()

}
