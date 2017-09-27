package net.katsstuff.chitchat.chat.data

import java.util.Optional

import org.spongepowered.api.Sponge
import org.spongepowered.api.data.manipulator.DataManipulatorBuilder
import org.spongepowered.api.data.manipulator.immutable.common.AbstractImmutableSingleData
import org.spongepowered.api.data.manipulator.mutable.common.AbstractSingleData
import org.spongepowered.api.data.merge.MergeFunction
import org.spongepowered.api.data.persistence.AbstractDataBuilder
import org.spongepowered.api.data.value.immutable.ImmutableValue
import org.spongepowered.api.data.value.mutable.Value
import org.spongepowered.api.data.{DataContainer, DataHolder, DataView}

import net.katsstuff.chitchat.ChitChatPlugin

class ChannelData(_value: String)(implicit plugin: ChitChatPlugin)
    extends AbstractSingleData[String, ChannelData, ImmutableChannelData](_value, plugin.versionHelper.ChannelKey)
      with Comparable[ChannelData] /*Adding in Comparable makes it easy to keep compat with API 4*/ {

  override def asImmutable(): ImmutableChannelData = new ImmutableChannelData(getValue)

  override def compareTo(o: ChannelData): Int = getValue.compareTo(o.getValue)

  override def getValueGetter: Value[_] =
    Sponge.getRegistry.getValueFactory.createValue(plugin.versionHelper.ChannelKey, getValue)

  override def from(container: DataContainer): Optional[ChannelData] = from(container)

  def from(view: DataView): Optional[ChannelData] = {
    view.getString(plugin.versionHelper.ChannelKey.getQuery).ifPresent(s => setValue(s))
    Optional.of(this)
  }

  override def copy(): ChannelData = new ChannelData(getValue)

  override def fill(dataHolder: DataHolder, overlap: MergeFunction): Optional[ChannelData] = {
    val merged = overlap.merge(this, dataHolder.get(classOf[ChannelData]).orElse(null))
    setValue(merged.getValue)

    Optional.of(this)
  }

  override def getContentVersion: Int = 1
}

class ImmutableChannelData(_value: String)(implicit plugin: ChitChatPlugin)
    extends AbstractImmutableSingleData[String, ImmutableChannelData, ChannelData](
      _value,
      plugin.versionHelper.ChannelKey
    ) with Comparable[ImmutableChannelData] {

  override def asMutable(): ChannelData = new ChannelData(value)

  override def getValueGetter: ImmutableValue[_] =
    Sponge.getRegistry.getValueFactory.createValue(plugin.versionHelper.ChannelKey, getValue).asImmutable()

  override def compareTo(o: ImmutableChannelData): Int = value.compareTo(o.value)

  override def getContentVersion: Int = 1
}

class ChannelDataBuilder(implicit plugin: ChitChatPlugin)
    extends AbstractDataBuilder[ChannelData](classOf[ChannelData], 1)
    with DataManipulatorBuilder[ChannelData, ImmutableChannelData] {

  override def create(): ChannelData = new ChannelData("Global")

  override def createFrom(dataHolder: DataHolder): Optional[ChannelData] = create().fill(dataHolder)

  override def buildContent(container: DataView): Optional[ChannelData] = create().from(container)
}
