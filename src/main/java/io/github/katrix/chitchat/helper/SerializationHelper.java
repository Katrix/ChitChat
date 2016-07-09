/**
 * This file is part of ChitChat, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2016 Katrix
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.katrix.chitchat.helper;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.translator.ConfigurateTranslator;
import org.spongepowered.api.text.Text;

import io.github.katrix.chitchat.chat.channels.Channel;
import io.github.katrix.chitchat.io.ConfigurateStorage;
import io.github.katrix.chitchat.io.NBTStorage;
import io.github.katrix.spongebt.nbt.NBTCompound;
import io.github.katrix.spongebt.nbt.NBTList;
import io.github.katrix.spongebt.nbt.NBTType;
import io.github.katrix.spongebt.sponge.NBTTranslator;
import ninja.leaping.configurate.ConfigurationNode;
import scala.compat.java8.OptionConverters;

public class SerializationHelper {

	private static final String PREFIX = "prefix";
	private static final String DESCRIPTION = "description";
	private static final String CHILDREN = "children";

	public static Optional<Text> getPrefixNbt(NBTCompound compound) {
		return compound.getJava(PREFIX)
				.flatMap(t -> OptionConverters.toJava(t.asInstanceOfNBTCompound()))
				.flatMap(c -> Sponge.getDataManager().deserialize(Text.class, NBTTranslator.translateFrom(c)));
	}

	public static void setPrefixNbt(NBTCompound compound, Text prefix) {
		compound.setTag(PREFIX, NBTTranslator.translateData(prefix.toContainer()));
	}

	public static Optional<Text> getDescriptionNbt(NBTCompound compound) {
		return compound.getJava(DESCRIPTION)
				.flatMap(t -> OptionConverters.toJava(t.asInstanceOfNBTCompound()))
				.flatMap(c -> Sponge.getDataManager().deserialize(Text.class, NBTTranslator.translateFrom(c)));
	}

	public static void setDescriptionNbt(NBTCompound compound, Text description) {
		compound.setTag(DESCRIPTION, NBTTranslator.translateData(description.toContainer()));
	}

	public static Optional<List<NBTCompound>> getChildrenNbt(NBTCompound compound) {
		return compound.getJava(CHILDREN)
				.flatMap(t -> OptionConverters.toJava(t.asInstanceOfNBTList()))
				.filter(l -> l.getListType() == NBTType.TAG_COMPOUND)
				.map(l -> l.getAllJava().stream()
						.map(t -> (NBTCompound)t)
						.collect(Collectors.toList()));
	}

	public static void setChildrenNbt(NBTCompound compound, Set<Channel> children) {
		NBTList childrenList = new NBTList(NBTType.TAG_COMPOUND);

		children.stream()
				.map(NBTStorage::saveChannel)
				.forEach(childrenList::add);

		compound.setTag(CHILDREN, childrenList);
	}

	public static void addChildrenToChannelNbt(List<NBTCompound> children, Channel parent) {
		children.stream()
				.map(NBTStorage::loadChannel)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.forEach(b -> parent.addChild(b.getFirst(), b.getSecond()));
	}

	public static Optional<Text> getPrefixConfigurate(ConfigurationNode node) {
		return Sponge.getDataManager().deserialize(Text.class, ConfigurateTranslator.instance().translateFrom(node.getNode(PREFIX)));
	}

	public static void setPrefixConfigurate(ConfigurationNode node, Text prefix) {
		node.getNode(PREFIX).setValue(ConfigurateTranslator.instance().translateData(prefix.toContainer()));
	}

	public static Optional<Text> getDescriptionConfigurate(ConfigurationNode node) {
		return Sponge.getDataManager().deserialize(Text.class, ConfigurateTranslator.instance().translateFrom(node.getNode(DESCRIPTION)));
	}

	public static void setDescriptionConfigurate(ConfigurationNode node, Text description) {
		node.getNode(DESCRIPTION).setValue(ConfigurateTranslator.instance().translateData(description.toContainer()));
	}

	public static List<? extends ConfigurationNode> getChildrenConfigurate(ConfigurationNode node) {
		ConfigurationNode childrenNode = node.getNode(CHILDREN);
		return childrenNode.getChildrenList();
	}

	public static void addChildrenToChannelConfigurate(List<? extends ConfigurationNode> children, Channel parent) {
		children.stream()
				.map(ConfigurateStorage::loadChannel)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.forEach(b -> parent.addChild(b.getFirst(), b.getSecond()));
	}

	public static void setChildrenConfigurate(ConfigurationNode node, Set<Channel> children) {
		ConfigurationNode childrenNode = node.getNode(CHILDREN);
		childrenNode.setValue(children.stream()
				.map(c -> ConfigurateStorage.saveChannel(c, childrenNode))
				.collect(Collectors.toList()));
	}
}
