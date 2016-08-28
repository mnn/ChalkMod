package tk.monnef.chalk.block

import net.minecraft.block.Block
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.fml.common.registry.GameRegistry
import tk.monnef.chalk.ChalkMod
import tk.monnef.chalk.core.{MonnefBlock, MonnefTileEntity}

object ChalkBlocks {
  var paintedChalk: BlockPaintedChalk = _

  private[this] def registerTileEntity(clazz: Class[_ <: TileEntity with MonnefTileEntity], name: String) {
    GameRegistry.registerTileEntity(clazz, name)
  }

  private[this] def registerBlock[T <: Block with MonnefBlock](block: T, tileEntityDescription: Option[(Class[_ <: TileEntity with MonnefTileEntity], String)] = None): T = {
    GameRegistry.register(block)
    tileEntityDescription.foreach { case (clazz, name) => registerTileEntity(clazz, ChalkMod.ModId + "_" + name) }
    block
  }

  def registerBlocks(): Unit = {
    paintedChalk = registerBlock(new BlockPaintedChalk, Some(classOf[TilePaintedChalk], "painted_chalk"))
  }
}
