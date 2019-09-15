package scala4neo.modeltestkit

import org.scalacheck.{Arbitrary, Gen}
import scala4neo.data.model.Person

object person extends PersonTestKit

trait PersonTestKit {

  implicit val personArbitrary: Arbitrary[Person] = Arbitrary {
    for {
      id <- Gen.uuid
      name <- Gen.alphaNumStr
    } yield Person(id, name)
  }

}