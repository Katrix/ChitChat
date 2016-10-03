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
package net.katsstuff.old.chitchat.io;

import static io.github.katrix.spongebt.nbt.NBTType.TAG_COMPOUND;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

import net.katsstuff.old.chitchat.chat.ChannelChitChat;
import io.github.katrix.spongebt.nbt.NBTCompound;
import io.github.katrix.spongebt.nbt.NBTList;
import io.github.katrix.spongebt.nbt.NBTString;
import io.github.katrix.spongebt.nbt.NBTTag;
import io.github.katrix.spongebt.sponge.NBTTranslator;
import scala.compat.java8.OptionConverters;

public class NBTStorage extends NBTBase implements IPersistentStorage {

	private static final String CHANNELS = "channels";
	private static final String NAME = "name";
	private static final String PREFIX = "prefix";
	private static final String DESCRIPTION = "description";
	private static final String CHILDREN = "children";
	private static final String ROOT = "root";

	public NBTStorage(Path path, String name, boolean compressed) throws IOException {
		super(path, name, compressed);
	}

	@Override
	public Optional<ChannelChitChat.ChannelRoot> loadRootChannel() {
		NBTCompound channelTag = getChannelTag();
		Optional<NBTCompound> optRootTag = channelTag.getJava(ROOT).flatMap(t -> OptionConverters.toJava(t.asInstanceOfNBTCompound()));

		if(optRootTag.isPresent()) {
			NBTCompound rootTag = optRootTag.get();
			Optional<String> optName = rootTag.getJava(NAME)
					.flatMap(t -> OptionConverters.toJava(t.asInstanceOfNBTString()))
					.map(NBTString::value);
			Optional<Text> optPrefix = rootTag.getJava(PREFIX)
					.flatMap(t -> OptionConverters.toJava(t.asInstanceOfNBTCompound()))
					.flatMap(t -> Sponge.getDataManager().deserialize(Text.class, NBTTranslator.translateFrom(t)));
			Optional<Text> optDescription = rootTag.getJava(DESCRIPTION)
					.flatMap(t -> OptionConverters.toJava(t.asInstanceOfNBTCompound()))
					.flatMap(t -> Sponge.getDataManager().deserialize(Text.class, NBTTranslator.translateFrom(t)));
			Optional<NBTList> optChildren = rootTag.getJava(CHILDREN)
					.flatMap(t -> OptionConverters.toJava(t.asInstanceOfNBTList()));

			if(optName.isPresent() && optPrefix.isPresent() && optDescription.isPresent() && optChildren.isPresent()) {
				String name = optName.get();
				NBTList children = optChildren.get();

				if(children.getListType() == TAG_COMPOUND) {
					ChannelChitChat.ChannelRoot rootChannel = ChannelChitChat.ChannelRoot.createNewRoot(name, optPrefix.get(), optDescription.get());
					children.getAllJava().stream()
							.forEach(c -> loadChannel((NBTCompound)c, rootChannel));
					return Optional.of(rootChannel);
				}
			}
		}

		return Optional.empty();
	}

	private void loadChannel(NBTCompound channelCompound, ChannelChitChat parent) {
		Optional<String> optName = channelCompound.getJava(NAME)
				.flatMap(t -> OptionConverters.toJava(t.asInstanceOfNBTString()))
				.map(NBTString::value);
		Optional<Text> optPrefix = channelCompound.getJava(PREFIX)
				.flatMap(t -> OptionConverters.toJava(t.asInstanceOfNBTCompound()))
				.flatMap(t -> Sponge.getDataManager().deserialize(Text.class, NBTTranslator.translateFrom(t)));
		Optional<Text> optDescription = channelCompound.getJava(DESCRIPTION)
				.flatMap(t -> OptionConverters.toJava(t.asInstanceOfNBTCompound()))
				.flatMap(t -> Sponge.getDataManager().deserialize(Text.class, NBTTranslator.translateFrom(t)));
		Optional<NBTList> optChildren = channelCompound.getJava(CHILDREN)
				.flatMap(t -> OptionConverters.toJava(t.asInstanceOfNBTList()));

		if(optName.isPresent() && optPrefix.isPresent() && optDescription.isPresent() && optChildren.isPresent()) {
			String name = optName.get();
			NBTList children = optChildren.get();

			if(children.getListType() == TAG_COMPOUND) {
				ChannelChitChat createdChannel = parent.createChannel(name, optPrefix.get(), optDescription.get());
				children.getAllJava().stream()
						.forEach(c -> loadChannel((NBTCompound)c, createdChannel));
			}
		}
	}

	@Override
	public boolean saveRootChannel() {
		ChannelChitChat.ChannelRoot channel = ChannelChitChat.getRoot();
		NBTCompound channelTag = getChannelTag();
		channelTag.setTag(ROOT, saveChannel(channel));

		save();
		return true;
	}

	private NBTCompound saveChannel(ChannelChitChat channel) {
		NBTCompound channelCompound = new NBTCompound();

		channelCompound.setString(NAME, channel.getName());
		channelCompound.setTag(PREFIX, NBTTranslator.translateData(channel.getPrefix().toContainer()));
		channelCompound.setTag(DESCRIPTION, NBTTranslator.translateData(channel.getPrefix().toContainer()));

		NBTList list = new NBTList(TAG_COMPOUND);
		channel.getChildren().forEach(c -> list.add(saveChannel(c)));
		channelCompound.setTag(CHILDREN, list);

		return channelCompound;
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
