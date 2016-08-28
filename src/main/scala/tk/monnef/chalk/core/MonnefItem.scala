package tk.monnef.chalk.core

import net.minecraft.item.Item
import tk.monnef.chalk.ChalkMod

trait MonnefItem {
  this: Item =>

  setCreativeTab(ChalkMod.creativeTab)

  def setupName(name: String): Unit = {
    setRegistryName(name)
    setUnlocalizedName(name)
  }
}
