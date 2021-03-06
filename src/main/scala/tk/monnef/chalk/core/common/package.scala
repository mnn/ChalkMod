package tk.monnef.chalk.core

import net.minecraft.world.World

import scala.util.Random

package object common extends WorldPimps
  with SeqPimps
  with ArrayPimps {

}

trait WorldPimps {

  implicit class WorldPimps(w: World) {
    def isLogicalClient = w.isRemote

    def isLogicalServer = !isLogicalClient
  }

}

trait SeqPimps {

  implicit class SeqPimps[T](s: Seq[T]) {
    def random: T = s(Random.nextInt(s.size))
  }

}

trait ArrayPimps {

  implicit class ArrayPimps[T](a: Array[Array[T]]) {
    def zipWith2dIndex: Array[Array[(T, Int, Int)]] =
      (for {
        x <- a.indices
        col = a(x)
      } yield (for {
        y <- col.indices
        value = col(y)
      } yield (value, x, y)).toArray).toArray
  }

}
