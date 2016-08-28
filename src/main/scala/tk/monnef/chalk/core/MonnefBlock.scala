package tk.monnef.chalk.core

import net.minecraft.block.Block
import net.minecraft.tileentity.TileEntity
import tk.monnef.chalk.ChalkMod

trait MonnefBlock {
  this: Block =>

  setCreativeTab(ChalkMod.creativeTab)

  def setupName(name: String): Unit = {
    setRegistryName(name)
    setUnlocalizedName(name)
  }
}

trait MonnefTileEntity {
  this: TileEntity =>
}