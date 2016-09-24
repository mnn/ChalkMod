package tk.monnef.chalk

import net.minecraft.util.EnumFacing
import org.scalatest._
import org.scalatest.Assertions._
import tk.monnef.chalk.sigil.{CenteringRotatingSlightlyShiftingSigilComparator, MeanSquareDifference, Sigils}
import tk.monnef.chalk.sigil.Sigils._

import scalaz._
import Scalaz._

class SigilsTest extends FreeSpec with Matchers with AppendedClues {

  // 0 1 2
  // 3 4 5
  def arr3x2 = canvasFromIntArray(Array(Array(0, 3), Array(1, 4), Array(2, 5)))

  def arrL = parseCanvas(
    """1 0 0
      |1 0 0
      |1 1 0
    """.stripMargin)

  def arrSquare2In4 = parseCanvas(
    """1 1 0 0
      |1 1 0 0
      |0 0 0 0
      |0 0 0 0
    """.stripMargin)

  def arrSquare2In3 = parseCanvas(
    """1 1 0
      |1 1 0
      |0 0 0
    """.stripMargin)

  def arrBigL = parseCanvas(
    """0 0 0 0 0
      |0 1 0 0 0
      |0 1 0 0 0
      |0 1 1 0 0
      |0 0 0 0 0
    """.stripMargin)

  "Sigils" - {
    "should create canvas from int array" in {
      canvasFromIntArray(Array(Array(1, 2))) shouldBe Array(Array(1.toByte, 2.toByte))
    }

    "should format canvas" in {
      formatCanvas(arr3x2) shouldBe "0 1 2\n3 4 5"
    }

    "should parse canvas" in {
      parseCanvas("0") shouldBe Array(Array(0))
      parseCanvas("\n0") shouldBe Array(Array(0))
      parseCanvas("\n0\n  ") shouldBe Array(Array(0))
      parseCanvas("0 1 2\n3 4 5") shouldBe arr3x2
      parseCanvas(
        """1 0 0
          |1 0 0
          |1 1 0
        """.stripMargin) shouldBe canvasFromIntArray(Array(Array(1, 1, 1), Array(0, 0, 1), Array(0, 0, 0)))
    }

    "compute center" - {
      "should compute center" - {
        "of 1x1" in {
          val
          nanRes = computeSigilCenter(parseCanvas("0"))
          nanRes._1.isNaN shouldBe true
          nanRes._2.isNaN shouldBe true
          computeSigilCenter(parseCanvas("1")) shouldBe(.5f, .5f)
        }
        "of 2x2" in {
          computeSigilCenter(parseCanvas("1 1\n1 1")) shouldBe(1f, 1f)
          computeSigilCenter(parseCanvas("1 0\n0 1")) shouldBe(1f, 1f)
          computeSigilCenter(parseCanvas("1 0\n0 0")) shouldBe(.5f, .5f)
        }
        "of 3x2" in {
          computeSigilCenter(arr3x2) shouldBe(1.7f, 1.1f)
        }
      }
    }

    "should shift array" in {
      val
      arr = Array(10, 20, 30, 40, 50)
      shiftArray(arr, 0, 0) shouldBe Array(10, 20, 30, 40, 50)
      shiftArray(arr, 2, 0) shouldBe Array(0, 0, 10, 20, 30)
      shiftArray(arr, -2, 0) shouldBe Array(30, 40, 50, 0, 0)
    }

    "should shift canvas" in {
      shiftCanvas(arr3x2, (2, 0)) shouldBe parseCanvas("0 0 0\n0 0 3")
      shiftCanvas(arr3x2, (0, 1)) shouldBe parseCanvas("0 0 0\n0 1 2")
      shiftCanvas(arr3x2, (0, -1)) shouldBe parseCanvas("3 4 5\n0 0 0")
      shiftCanvas(arr3x2, (-2, 0)) shouldBe parseCanvas("2 0 0\n5 0 0")

      shiftCanvas(arr3x2, (2, 1)) shouldBe parseCanvas("0 0 0\n0 0 0")
      shiftCanvas(arr3x2, (-2, -1)) shouldBe parseCanvas("5 0 0\n0 0 0")
      shiftCanvas(arr3x2, (2, -1)) shouldBe parseCanvas("0 0 3\n0 0 0")
    }

    "should calculate mean square difference" in {
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

    "should rotate canvas CW" in {
      rotateCanvasCw(arr3x2) shouldBe parseCanvas("3 0\n4 1\n5 2")
    }

    "should get all rotations" in {
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

    "should create all possible shifts" - {
      "for trivial case" in {
        val res = computeAllPossibleShifts(arrL, 0)
        res.length shouldBe 1
        res.head._1 shouldEqual arrL
        res.head._2 shouldEqual(0, 0)
      }
      "for L array" in {
        val one = computeAllPossibleShifts(arrL, 1)
        def test(idx: Int, x: Int, y: Int, data: String) {
          val t = one(idx)
          val exp = parseCanvas(data)
          val expShift = (x, y)
          t._1 shouldBe exp
          t._2 shouldBe expShift
        }
        //for {(c, i) <- one.zipWithIndex} println(s"$i:\n ${formatCanvas(c)}\n")
        def p(x: Int, y: Int, data: String) = (parseCanvas(data), (x, y))
        one.length shouldBe 5
        test(0, 0, 0, formatCanvas(arrL))
        test(1, 0, 1,
          """0 0 0
            |1 0 0
            |1 0 0
          """.stripMargin)
        test(2, -1, 0,
          """0 0 0
            |0 0 0
            |1 0 0
          """
          .stripMargin)
        test(3, 0, -1,
          """1 0 0
            |1 1 0
            |0 0 0
          """.stripMargin)
        test(4, 1, 0,
          """0 1 0
            |0 1 0
            |0 1 1
          """.
          stripMargin)
      }
    }

    "should center" - {
      "square of 2 in 4x4" in {
        val res = centerCanvas(arrSquare2In4)
        res._2 shouldBe(1, 1)
        res._1 shouldBe parseCanvas(
          """0 0 0 0
            |0 1 1 0
            |0 1 1 0
            |0 0 0 0
          """.stripMargin)
      }
      "square of 2 in 3x3" in {
        val res = centerCanvas(arrSquare2In3)
        Some(res._2) should contain oneOf((0, 0), (1, 1))
        Some(res._1) should contain oneOf(
          parseCanvas(
            """1 1 0
              |1 1 0
              |0 0 0
            """.stripMargin),
          parseCanvas(
            """0 0 0
              |0 1 1
              |0 1 1
            """.stripMargin)
          )
      }
    }
  }

  "Comparator" - {
    "should return 1 and north for identity" in {
      val arr1 = parseCanvas("1")
      val res = CenteringRotatingSlightlyShiftingSigilComparator.calculateSimilarity(arr1, arr1)
      res.score shouldBe 1f
      res.facing shouldBe EnumFacing.NORTH
      res.shift shouldBe(0, 0)
    }

    "should return 1 and west for template rotated to the right" in {
      val input = rotateCanvasCw(arrL)
      val res = CenteringRotatingSlightlyShiftingSigilComparator.calculateSimilarity(input, arrL);
      {
        res.score shouldBe 1f
        res.facing shouldBe EnumFacing.WEST
        res.shift shouldBe(0, 0)
      } withClue res
    }

    "should return 1, north and shift -1, 0 for template moved to the right" in {
      val input = shiftCanvas(arrL, (1, 0))
      val res = CenteringRotatingSlightlyShiftingSigilComparator.calculateSimilarity(input, arrL);
      {
        res.score shouldBe 1f
        res.facing shouldBe EnumFacing.NORTH
        res.shift shouldBe(-1, 0)
      } withClue res
    }
  }
}
