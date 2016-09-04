package tk.monnef.chalk.block

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.{GlStateManager, OpenGlHelper, Tessellator}
import net.minecraft.util.{EnumFacing, ResourceLocation}
import org.lwjgl.opengl.GL11
import tk.monnef.chalk.ChalkMod

object TilePaintedChalkRenderer {

  import TilePaintedChalk._

  private final val Texture = new ResourceLocation(ChalkMod.ModId, "textures/tiles/chalk.png")
  final val DotSizeInPix = 16 / CanvasSize
  final val DotSize = DotSizeInPix * 1f / 16
  val TexMapping: List[List[(Double, Double)]] = List(
    List((0d, 1d), (1d, 1d), (1d, 0d), (0d, 0d)),
    List((0d, 0d), (0d, 1d), (1d, 1d), (1d, 0d)),
    List((0d, 1d), (1d, 1d), (1d, 0d), (0d, 0d)),
    List((1d, 1d), (1d, 0d), (0d, 0d), (0d, 1d))
  )

}

class TilePaintedChalkRenderer extends TileEntitySpecialRenderer[TilePaintedChalk] {

  import TilePaintedChalkRenderer._

  override def renderTileEntityAt(te: TilePaintedChalk, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(Texture)
    GlStateManager.pushMatrix()
    import EnumFacing._
    val (rotX: Float, rotY: Float, rotZ: Float) = te.side match {
      case DOWN => (0f, 0f, 0f) // already done
      case UP => (180f, 180f, 0f)
      case _ => (90f, 0f, te.side.getHorizontalAngle + 180f)
    }
    GlStateManager.translate(x, y, z)
    GlStateManager.translate(.5f, .5f, .5f)
    GlStateManager.rotate(rotX, 1, 0, 0)
    GlStateManager.rotate(rotY, 0, 1, 0)
    GlStateManager.rotate(rotZ, 0, 0, 1)
    GlStateManager.translate(-.5f, -.5f, -.5f)
    GlStateManager.translate(0, 0.001f, 0)
    val tessellator = Tessellator.getInstance
    val buff = tessellator.getBuffer

    GL11.glDisable(GL11.GL_LIGHTING)
    GlStateManager.color(1, 1, 1, 1)
    buff.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)

    // TODO: figure out why lighting is strange (dark)
    /*
        val combinedLight = te.getWorld.getCombinedLight(te.getPos, 15728640)
        val lightingMapX = combinedLight % 65536
        val lightingMapY = combinedLight / 65536
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightingMapX, lightingMapY)
    */

    val U = DotSize
    val canvas = te.canvas
    val blockPos = te.getPos
    var texMappingRandom = (blockPos.getX * 73 + blockPos.getY * 773 + blockPos.getZ * 59).toInt
    for {
      x <- canvas.indices
      y <- canvas.head.indices
    } {
      texMappingRandom += 181 * x + y * -331 + 53
      if (canvas(x)(y)) {
        val sx = x * U
        val sy = y * U
        val texMapping = TexMapping(texMappingRandom.abs % TexMapping.size)
        buff.pos(0 + sx, 0, U + sy).tex(texMapping(0)._1, texMapping(0)._2).endVertex()
        buff.pos(U + sx, 0, U + sy).tex(texMapping(1)._1, texMapping(1)._2).endVertex()
        buff.pos(U + sx, 0, 0 + sy).tex(texMapping(2)._1, texMapping(2)._2).endVertex()
        buff.pos(0 + sx, 0, 0 + sy).tex(texMapping(3)._1, texMapping(3)._2).endVertex()
      }
    }
    tessellator.draw()
    GL11.glEnable(GL11.GL_LIGHTING)
    GlStateManager.popMatrix()
  }
}
