package scala4neo.codec

import scala4neo.data.codec._
import scala4neo.data.model.Movie
import scala4neo.modeltestkit.movie._
import scala4neo.testkit.NodeCodecSpec

class MovieCodecSpec extends
  NodeCodecSpec[Movie] with
  CodecSpecSettings {

  override def randomEntity: Movie = movieArbitrary.arbitrary.sample.get

  override def updateEntity(entityToUpdate: Movie, update: Movie): Movie =
    update.copy(id = entityToUpdate.id)

  behave like correctNodeCodec()

}