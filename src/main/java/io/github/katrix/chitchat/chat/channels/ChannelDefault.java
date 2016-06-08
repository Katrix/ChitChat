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
package io.github.katrix.chitchat.chat.channels;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.AbstractMutableMessageChannel;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

import io.github.katrix.chitchat.ChitChat;
import io.github.katrix.chitchat.io.IPersistentStorage;
import io.github.katrix.chitchat.io.NBTStorage;
import io.github.katrix.spongebt.nbt.NBTCompound;
import io.github.katrix.spongebt.nbt.NBTList;
import io.github.katrix.spongebt.nbt.NBTTag;
import io.github.katrix.spongebt.nbt.NBTType;
import io.github.katrix.spongebt.sponge.NBTTranslator;
import scala.compat.java8.OptionConverters;

public class ChannelDefault extends AbstractMutableMessageChannel implements Channel {

	private String name;
	private Text prefix;
	private Text description;

	private Channel parent;
	private final Set<Channel> children = new HashSet<>();

	public ChannelDefault(String name, Text prefix, Text description, Channel parent) {
		this.name = name;
		this.prefix = prefix;
		this.description = description;
		this.parent = parent;
	}

	/*============================== Channel stuff ==============================*/

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
		members.stream()
				.filter(r -> r instanceof User)
				.map(r -> (User)r)
				.forEach(this::setUser);
	}

	@Override
	public Text getPrefix() {
		return prefix;
	}

	@Override
	public boolean setPrefix(Text prefix) {
		this.prefix = prefix;
		return save();
	}

	@Override
	public Text getDescription() {
		return description;
	}

	@Override
	public boolean setDescription(Text description) {
		this.description = description;
		return save();
	}

	@Override
	public ChannelType getChannelType() {
		return ChannelDefaultType.INSTANCE;
	}

	/*============================== Nested channels stuff ==============================*/

	@Override
	public Optional<Channel> getParent() {
		return Optional.of(parent);
	}

	@Override
	public Set<Channel> getChildren() {
		return ImmutableSet.copyOf(children);
	}

	@Override
	public boolean addChild(ChannelBuilder builder, String name) {
		if(isNameUnused(name)) {
			Channel createdChannel = builder.createChannel(name, this);
			Optional<Channel> parent = createdChannel.getParent();
			if(parent.isPresent() && parent.get().equals(this)) {
				children.add(createdChannel);
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean removeChild(Channel channel) {
		return children.remove(channel);
	}

	@Override
	public Optional<Channel> getChild(String name) {
		return getByName(name).findFirst();
	}

	private Stream<Channel> getByName(String name) {
		return children.stream().filter(c -> c.getName().equals(name));
	}

	public static class ChannelDefaultType implements ChannelType<ChannelDefault>, ChannelNBTSerializer<ChannelDefault> {

		private static final String NBT_PREFIX = "prefix";
		private static final String NBT_DESCRIPTION = "description";
		private static final String NBT_CHILDREN = "children";

		public static final ChannelDefaultType INSTANCE = new ChannelDefaultType();

		private ChannelDefaultType() {}

		@Override
		public String getTypeIdentifier() {
			return "default";
		}

		@Override
		public ChannelNBTSerializer<ChannelDefault> getNBTSerializer() {
			return this;
		}

		@Override
		public TypeToken<ChannelDefault> getConfigurateSerializer() {
			return TypeToken.of(ChannelDefault.class);
		}

		@Override
		public Optional<ChannelBuilder<ChannelDefault>> deserializeNBT(NBTCompound compound) {
			Optional<Text> prefix = compound.getJava(NBT_PREFIX)
					.flatMap(t -> OptionConverters.toJava(t.asInstanceOfNBTCompound()))
					.flatMap(c -> Sponge.getDataManager().deserialize(Text.class, NBTTranslator.translateFrom(c)));

			Optional<Text> description = compound.getJava(NBT_DESCRIPTION)
					.flatMap(t -> OptionConverters.toJava(t.asInstanceOfNBTCompound()))
					.flatMap(c -> Sponge.getDataManager().deserialize(Text.class, NBTTranslator.translateFrom(c)));

			Optional<List<NBTCompound>> children = compound.getJava(NBT_CHILDREN)
					.flatMap(t -> OptionConverters.toJava(t.asInstanceOfNBTList()))
					.filter(l -> l.getListType() == NBTType.TAG_COMPOUND)
					.map(l -> l.getAllJava().stream()
							.map(t -> (NBTCompound)t)
							.collect(Collectors.toList()));

			if(prefix.isPresent() && description.isPresent() && children.isPresent()) {
				ChannelBuilder<ChannelDefault> ret = ((name, parent) -> {
					ChannelDefault channel = new ChannelDefault(name, prefix.get(), description.get(), parent);

					children.get().stream()
							.map(c -> ((NBTStorage)ChitChat.getStorage()).loadChannel(c))
							.filter(Optional::isPresent)
							.map(Optional::get)
							.forEach(b -> channel.addChild(b.getFirst(), b.toString()));

					return channel;
				});

				return Optional.of(ret);
			}
			else {
				return Optional.empty();
			}
		}

		@Override
		public NBTCompound serializeNBT(ChannelDefault channel, NBTCompound compound) {
			compound.setTag(NBT_PREFIX, NBTTranslator.translateData(channel.getPrefix().toContainer()));
			compound.setTag(NBT_DESCRIPTION, NBTTranslator.translateData(channel.getDescription().toContainer()));

			NBTList children = new NBTList(NBTType.TAG_COMPOUND);

			channel.getChildren().stream()
					.map(NBTStorage::saveChannel)
					.forEach(children::add);

			compound.setTag(NBT_CHILDREN, children);

			return compound;
		}
	}
}
