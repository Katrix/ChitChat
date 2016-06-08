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
package io.github.katrix.chitchat.io;

import static io.github.katrix.spongebt.nbt.NBTType.TAG_COMPOUND;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiFunction;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Tuple;

import io.github.katrix.chitchat.chat.channels.Channel;
import io.github.katrix.chitchat.chat.channels.ChannelBuilder;
import io.github.katrix.chitchat.chat.channels.ChannelDefault;
import io.github.katrix.chitchat.chat.channels.ChannelNBTSerializer;
import io.github.katrix.chitchat.chat.channels.ChannelRoot;
import io.github.katrix.chitchat.chat.channels.ChannelTypeRegistry;
import io.github.katrix.spongebt.nbt.NBTCompound;
import io.github.katrix.spongebt.nbt.NBTList;
import io.github.katrix.spongebt.nbt.NBTString;
import io.github.katrix.spongebt.nbt.NBTTag;
import io.github.katrix.spongebt.sponge.NBTTranslator;
import scala.compat.java8.OptionConverters;

public class NBTStorage extends NBTBase implements IPersistentStorage {

	private static final String CHANNELS = "channels";

	private static final String NAME = "name";
	private static final String IDENTIFIER = "typeIdentifier";

	private static final String ROOT = ChannelRoot.ChannelRootType.INSTANCE.getTypeIdentifier();

	public NBTStorage(Path path, String name, boolean compressed) throws IOException {
		super(path, name, compressed);
	}

	@Override
	public Optional<ChannelRoot> loadRootChannel() {
		NBTCompound channelTag = getChannelTag();
		Optional<String> name = channelTag.getJava(NAME)
				.flatMap(t -> OptionConverters.toJava(t.asInstanceOfNBTString()))
				.map(NBTString::value);

		Optional<String> rootIdentifier = channelTag.getJava(IDENTIFIER)
				.flatMap(t -> OptionConverters.toJava(t.asInstanceOfNBTString()))
				.map(NBTString::value);

		if(name.isPresent() && rootIdentifier.isPresent() && rootIdentifier.get().equals(ROOT)) {
			Optional<ChannelBuilder<ChannelRoot>> rootBuilder =  ChannelRoot.ChannelRootType.INSTANCE.deserializeNBT(channelTag);

			if(rootBuilder.isPresent()) {
				return Optional.of(rootBuilder.get().createChannel(name.get(), null));
			}
		}

		return Optional.empty();
	}

	public Optional<Tuple<ChannelBuilder, String>> loadChannel(NBTCompound compound) {
		Optional<String> name = compound.getJava(NAME)
				.flatMap(t -> OptionConverters.toJava(t.asInstanceOfNBTString()))
				.map(NBTString::value);

		Optional<String> identifier = compound.getJava(IDENTIFIER)
				.flatMap(t -> OptionConverters.toJava(t.asInstanceOfNBTString()))
				.map(NBTString::value);

		if(name.isPresent() && identifier.isPresent()) {
			Optional<ChannelBuilder> builder = ChannelTypeRegistry.INSTANCE.getType(identifier.get())
					.flatMap(t -> t.getNBTSerializer().deserializeNBT(compound));

			if(builder.isPresent()) {
				return Optional.of(new Tuple<>(builder.get(), name.get()));
			}
		}

		return Optional.empty();
	}

	public static NBTCompound saveChannel(Channel channel) {
		NBTCompound compound = new NBTCompound();
		compound.setString(IDENTIFIER, channel.getChannelType().getTypeIdentifier());
		compound.setString(NAME, channel.getName());
		channel.getChannelType().getNBTSerializer().serializeNBT(channel, compound);

		return compound;
	}

	@Override
	public boolean saveRootChannel() {
		NBTCompound channelTag = getChannelTag();
		ChannelRoot root = ChannelRoot.getRoot();
		channelTag.setString(IDENTIFIER, ROOT);
		channelTag.setString(NAME, root.getName());
		ChannelRoot.ChannelRootType.INSTANCE.serializeNBT(root, channelTag);
		save();
		return true;
	}

	private NBTCompound getChannelTag() {
		NBTCompound defaultCompound = new NBTCompound();
		NBTTag tag = compound.getOrCreate(CHANNELS, defaultCompound);

		if(tag.getType() == TAG_COMPOUND) {
			return (NBTCompound)tag;
		}

		return defaultCompound;
	}
}
