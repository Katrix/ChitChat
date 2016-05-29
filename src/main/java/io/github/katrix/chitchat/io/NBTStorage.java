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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

import io.github.katrix.chitchat.chat.ChannelChitChat;
import io.github.katrix.spongebt.nbt.NBTCompound;
import io.github.katrix.spongebt.nbt.NBTList;
import io.github.katrix.spongebt.nbt.NBTString;
import io.github.katrix.spongebt.nbt.NBTTag;
import io.github.katrix.spongebt.sponge.NBTTranslator;
import scala.Option;

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
		Option<NBTCompound> optRootTag = channelTag.get(ROOT).flatMap(NBTTag::asInstanceOfNBTCompound);

		if(optRootTag.isDefined()) {
			NBTCompound rootTag = optRootTag.get();
			Option<NBTString> optName = rootTag.get(NAME).flatMap(NBTTag::asInstanceOfNBTString);
			Option<NBTCompound> optPrefix = rootTag.get(PREFIX).flatMap(NBTTag::asInstanceOfNBTCompound);
			Option<NBTCompound> optDescription = rootTag.get(DESCRIPTION).flatMap(NBTTag::asInstanceOfNBTCompound);
			Option<NBTList> optChildren = rootTag.get(CHILDREN).flatMap(NBTTag::asInstanceOfNBTList);

			if(optName.isDefined() && optPrefix.isDefined() && optDescription.isDefined() && optChildren.isDefined()) {
				NBTString name = optName.get();
				NBTCompound prefix = optPrefix.get();
				NBTCompound description = optDescription.get();
				NBTList children = optChildren.get();

				Optional<Text> textPrefix = Sponge.getDataManager().deserialize(Text.class, NBTTranslator.translateFrom(prefix));
				Optional<Text> textDescription = Sponge.getDataManager().deserialize(Text.class, NBTTranslator.translateFrom(description));

				if(children.getListType() == TAG_COMPOUND && textPrefix.isPresent() && textDescription.isPresent()) {
					ChannelChitChat.ChannelRoot rootChannel = ChannelChitChat.ChannelRoot.createNewRoot(name.value(), textPrefix.get(),
							textDescription.get());
					List<NBTCompound> childrenCompound = children.getAllJava().stream().map(t -> (NBTCompound)t).collect(Collectors.toList());
					childrenCompound.forEach(c -> loadChannel(c, rootChannel));
					return Optional.of(rootChannel);
				}
			}
		}

		return Optional.empty();
	}

	private void loadChannel(NBTCompound channelCompound, ChannelChitChat parent) {
		Option<NBTString> optName = channelCompound.get(NAME).flatMap(NBTTag::asInstanceOfNBTString);
		Option<NBTCompound> optPrefix = channelCompound.get(PREFIX).flatMap(NBTTag::asInstanceOfNBTCompound);
		Option<NBTCompound> optDescription = channelCompound.get(DESCRIPTION).flatMap(NBTTag::asInstanceOfNBTCompound);
		Option<NBTList> optChildren = channelCompound.get(CHILDREN).flatMap(NBTTag::asInstanceOfNBTList);

		if(optName.isDefined() && optPrefix.isDefined() && optDescription.isDefined() && optChildren.isDefined()) {
			NBTString name = optName.get();
			NBTCompound prefix = optPrefix.get();
			NBTCompound description = optDescription.get();
			NBTList children = optChildren.get();

			Optional<Text> textPrefix = Sponge.getDataManager().deserialize(Text.class, NBTTranslator.translateFrom(prefix));
			Optional<Text> textDescription = Sponge.getDataManager().deserialize(Text.class, NBTTranslator.translateFrom(description));

			if(children.getListType() == TAG_COMPOUND && textPrefix.isPresent() && textDescription.isPresent()) {
				ChannelChitChat createdChannel = parent.createChannel(name.value(), textPrefix.get(), textDescription.get());
				List<NBTCompound> childrenCompound = children.getAllJava().stream().map(t -> (NBTCompound)t).collect(Collectors.toList());
				childrenCompound.forEach(c -> loadChannel(c, createdChannel));
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
