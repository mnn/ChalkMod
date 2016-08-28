package tk.monnef.chalk.item

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.init.Items
import net.minecraft.item.{EnumDyeColor, Item, ItemStack}
import net.minecraftforge.fml.common.registry.GameRegistry
import tk.monnef.chalk.ChalkMod

object ChalkItems {
  var chalk: ItemChalk = _

  private[this] def registerItemModel(item: Item): Unit = {
    val mesher = Minecraft.getMinecraft.getRenderItem.getItemModelMesher
    val resourceLoc = ChalkMod.ModId + ":" + item.getUnlocalizedName.drop(5)
    mesher.register(ChalkItems.chalk, 0, new ModelResourceLocation(resourceLoc, "inventory"))
  }

  def registerItemModels(): Unit = {
    registerItemModel(chalk)
  }

  private[this] def registerItem[T <: Item](item: T): T = {
    GameRegistry.register(item)
    item
  }

  def registerItems(): Unit = {
    ChalkItems.chalk = registerItem(new ItemChalk)
  }

  def registerRecipes(): Unit = {
    GameRegistry.addShapedRecipe(new ItemStack(chalk), "A", "A", Char.box('A'), new ItemStack(Items.DYE, 1, EnumDyeColor.WHITE.getDyeDamage))
  }
}
