package net.katsstuff.chitchat

import java.util.{Locale, ResourceBundle}

import org.jetbrains.annotations.PropertyKey
import org.spongepowered.api.text.Text

import net.katsstuff.katlib.i18n.Resource

object CCResource extends Resource {

  final val ResourceLocation = "assets.chitchat.lang"

  override def getBundle(implicit locale: Locale): ResourceBundle = ResourceBundle.getBundle(ResourceLocation, locale)

  override def get(@PropertyKey(resourceBundle = ResourceLocation) key: String)(implicit locale: Locale): String =
    super.get(key)

  override def get(@PropertyKey(resourceBundle = ResourceLocation) key: String, params: Map[String, String])(
      implicit locale: Locale
  ): String = super.get(key, params)
  override def get(@PropertyKey(resourceBundle = ResourceLocation) key: String, params: (String, String)*)(
      implicit locale: Locale
  ): String = super.get(key, params: _*)

  override def getText(@PropertyKey(resourceBundle = ResourceLocation) key: String)(implicit locale: Locale): Text =
    super.getText(key)

  override def getText(@PropertyKey(resourceBundle = ResourceLocation) key: String, params: Map[String, AnyRef])(
      implicit locale: Locale
  ): Text = super.getText(key, params)
  override def getText(@PropertyKey(resourceBundle = ResourceLocation) key: String, params: (String, AnyRef)*)(
      implicit locale: Locale
  ): Text = super.getText(key, params: _*)

}
