package tk.monnef.chalk

import org.scalatest._
import tk.monnef.chalk.sigil.{MeanSquareDifference, Sigils}
import tk.monnef.chalk.sigil.Sigils._

import scalaz._
import Scalaz._

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
    parseCanvas("0") shouldBe Array(Array(0))
    parseCanvas("0 1 2\n3 4 5") shouldBe arr3x2
  }

  it should "compute center" in {
    val nanRes = computeCanvasCenter(parseCanvas("0"))
    nanRes._1.isNaN shouldBe true
    nanRes._2.isNaN shouldBe true

    computeCanvasCenter(parseCanvas("1")) shouldBe(.5f, .5f)
    computeCanvasCenter(parseCanvas("1 1\n1 1")) shouldBe(1f, 1f)
    computeCanvasCenter(parseCanvas("1 0\n0 1")) shouldBe(1f, 1f)
    computeCanvasCenter(parseCanvas("1 0\n0 0")) shouldBe(.5f, .5f)
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

  it should "calculate mean square difference" in {
    MeanSquareDifference.calculate(Array(Array(1.toByte)), Array(Array(3.toByte))) shouldBe 4
    MeanSquareDifference.calculate(
      Array(Array(0, 10), Array(0, 0)) |> canvasFromIntArray,
      Array(Array(0, 0), Array(1, 0)) |> canvasFromIntArray
    ) shouldBe ((10 * 10 + 1) / 4f)

    val size = 4
    val Mod = 128
    val lastIndex = size * size - 1
    val oneDim = (0 to lastIndex).toArray.map(_ % Mod)
    val a = oneDim.grouped(size).toArray.transpose |> canvasFromIntArray
    val b = oneDim.map((_: Int) => 1).grouped(size).toArray.transpose |> canvasFromIntArray

    def f(a: Array[Array[Byte]]): Unit = a.foreach { line =>
      println("( " + line.map(_.toString.reverse.padTo(3, ' ').reverse).mkString(" ") + " )")
    }
    //    println("a:")
    //    f(a)
    //    println("b:")
    //    f(b)

    MeanSquareDifference.calculate(a, a) shouldBe 0
    MeanSquareDifference.calculate(b, b) shouldBe 0
    val testInput: Array[Int] = (-1 to 14).toArray.grouped(size).toArray.transpose.flatten
    val testDiffs: Array[Double] = testInput.map(Math.pow(_, 2))
    val testSum = testDiffs.sum / (size * size)
    MeanSquareDifference.calculate(a, b) shouldBe testSum
  }

  it should "rotate canvas CW" in {
    rotateCanvasCw(arr3x2) shouldBe parseCanvas("3 0\n4 1\n5 2")
  }

  it should "get all rotations" in {
    val res = computeAllCwRations(arr3x2)
    val expected = List(
      arr3x2,
      parseCanvas("3 0\n4 1\n5 2"),
      parseCanvas("5 4 3\n2 1 0"),
      parseCanvas("2 5\n1 4\n0 3")
    )
    res.length shouldBe 4
    res(0) shouldBe expected(0)
    res(1) shouldBe expected(1)
    res(2) shouldBe expected(2)
    res(3) shouldBe expected(3)
  }
}
