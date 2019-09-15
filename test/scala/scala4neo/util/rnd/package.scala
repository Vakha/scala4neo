package scala4neo.util

import org.scalacheck.Arbitrary

package object rnd {
  def rnd[A](implicit arbitraryA: Arbitrary[A]): A =
    arbitraryA.arbitrary.sample.get
}
