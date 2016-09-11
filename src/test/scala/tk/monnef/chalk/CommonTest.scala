package tk.monnef.chalk

import org.scalatest._
import tk.monnef.chalk.core.common._

class CommonTest extends FlatSpec with Matchers {
  "Array pimps" should "index 2d" in {
    val arr = Array(Array(0, 3), Array(1, 4), Array(2, 5))
    arr.zipWith2dIndex shouldBe
      Array(Array((0, 0, 0), (3, 0, 1)), Array((1, 1, 0), (4, 1, 1)), Array((2, 2, 0), (5, 2, 1)))
  }
}
