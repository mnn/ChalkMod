package tk.monnef.chalk.core

import net.minecraft.world.World

import scala.util.Random

package object common extends WorldPimps with SeqPimps {

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