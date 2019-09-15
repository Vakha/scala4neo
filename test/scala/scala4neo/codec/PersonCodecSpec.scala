package scala4neo.codec

import scala4neo.data.codec._
import scala4neo.data.model.Person
import scala4neo.modeltestkit.person._
import scala4neo.testkit.NodeCodecSpec

class PersonCodecSpec extends
  NodeCodecSpec[Person] with
  CodecSpecSettings  {

  override def randomEntity: Person = personArbitrary.arbitrary.sample.get

  override def updateEntity(entityToUpdate: Person, update: Person): Person =
    update.copy(id = entityToUpdate.id)

  behave like correctNodeCodec()

}
