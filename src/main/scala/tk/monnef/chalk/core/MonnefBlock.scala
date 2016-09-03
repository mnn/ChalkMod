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

  /**
    * Forces server to send a sync packet and marks state as dirty so it will get saved.
    * Probably works only from server side.
    */
  def forceResynchronization() {
    markDirty()
    val state = getWorld.getBlockState(getPos)
    getWorld.notifyBlockUpdate(getPos, state, state, 3)
  }
}