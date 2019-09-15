package scala4neo.modeltestkit

import org.scalacheck.{Arbitrary, Gen}
import scala4neo.data.model.ActedIn

object actedIn extends ActedInTestKit

trait ActedInTestKit {

  implicit val actedInArbitrary: Arbitrary[ActedIn] = Arbitrary {
    for {
      id <- Gen.uuid
      roles <- Gen.listOf(Gen.alphaNumStr)
    } yield ActedIn(id, roles)
  }

}