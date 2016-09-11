package tk.monnef.chalk.sigil

object Sigils {
  type Sigil = Byte
  type Canvas = Array[Array[Sigil]]

  object SigilType {
    val Blank: Sigil = 0.toByte
    val WhiteChalk: Sigil = 1.toByte
  }

}

