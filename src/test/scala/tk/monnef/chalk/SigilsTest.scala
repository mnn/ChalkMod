package tk.monnef.chalk

import org.scalatest._
import tk.monnef.chalk.sigil.Sigils
import tk.monnef.chalk.sigil.Sigils._

class SigilsTest extends FlatSpec with Matchers {
  // 0 1 2
  // 3 4 5
  def arr3x2 = canvasFromIntArray(Array(Array(0, 3), Array(1, 4), Array(2, 5)))

  "Sigils" should "create canvas from int array" in {
    canvasFromIntArray(Array(Array(1, 2))) shouldBe Array(Array(1.toByte, 2.toByte))
  }

  it should "format canvas" in {
    formatCanvas(arr3x2) shouldBe "0 1 2\n3 4 5"
  }

  it should "parse canvas" in {
    parseCanvas("0 1 2\n3 4 5") shouldBe arr3x2
  }

  it should "compute center" in {
    computeCanvasCenter(arr3x2) shouldBe(1.7f, 1.1f)
  }

  it should "shift array" in {
    val arr = Array(10, 20, 30, 40, 50)
    shiftArray(arr, 0, 0) shouldBe Array(10, 20, 30, 40, 50)
    shiftArray(arr, 2, 0) shouldBe Array(0, 0, 10, 20, 30)
    shiftArray(arr, -2, 0) shouldBe Array(30, 40, 50, 0, 0)
  }

  it should "shift canvas" in {
    shiftCanvas(arr3x2, (2, 0)) shouldBe parseCanvas("0 0 0\n0 0 3")
    shiftCanvas(arr3x2, (0, 1)) shouldBe parseCanvas("0 0 0\n0 1 2")
    shiftCanvas(arr3x2, (0, -1)) shouldBe parseCanvas("3 4 5\n0 0 0")
    shiftCanvas(arr3x2, (-2, 0)) shouldBe parseCanvas("2 0 0\n5 0 0")

    shiftCanvas(arr3x2, (2, 1)) shouldBe parseCanvas("0 0 0\n0 0 0")
    shiftCanvas(arr3x2, (-2, -1)) shouldBe parseCanvas("5 0 0\n0 0 0")
    shiftCanvas(arr3x2, (2, -1)) shouldBe parseCanvas("0 0 3\n0 0 0")
  }
}
