package net.katsstuff.chitchat.helper

import scala.annotation.tailrec
import scala.collection.JavaConverters._

import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.{TextColor, TextColors, TextFormat, TextStyle, TextStyles}

object TextHelper {

  /**
    * Gets the format at the end of a [[Text]].
    */
  def getFormatAtEnd(text: Text): Option[TextFormat] =
    getTextAtEnd(text, _.getFormat != TextFormat.NONE).map(_.getFormat)

  /**
    * Gets the color at the end of a [[Text]].
    */
  def getColorAtEnd(text: Text): Option[TextColor] = getTextAtEnd(text, _.getColor != TextColors.NONE).map(_.getColor)

  /**
    * Gets the style at the end of a [[Text]].
    */
  def getStyleAtEnd(text: Text): Option[TextStyle] = getTextAtEnd(text, _.getStyle != TextStyles.NONE).map(_.getStyle)

  /**
    * Gets some [[Text]] at the end of another [[Text]], according to a
    * predicate.
    * @return The text if it was found.
    */
  def getTextAtEnd(text: Text, predicate: Text => Boolean): Option[Text] = {
    @tailrec
    def allEnds(acc: List[Text], end: Text): List[Text] = {
      val children = end.getChildren.asScala
      if (children.isEmpty) end :: acc
      else allEnds(end :: acc, children.last)
    }

    allEnds(Nil, text).find(predicate)
  }
}
