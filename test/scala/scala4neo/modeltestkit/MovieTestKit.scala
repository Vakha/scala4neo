package scala4neo.modeltestkit

import org.scalacheck.{Arbitrary, Gen}
import scala4neo.data.model.Movie

object movie extends MovieTestKit

trait MovieTestKit {

  implicit val movieArbitrary: Arbitrary[Movie] = Arbitrary {
    for {
      id <- Gen.uuid
      name <- Gen.alphaNumStr
    } yield Movie(id, name)
  }

}