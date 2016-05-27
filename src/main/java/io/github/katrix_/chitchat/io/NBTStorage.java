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
package io.github.katrix_.chitchat.io;

import static io.github.katrix.spongynbt.nbt.NBTType.TAG_COMPOUND;
import static io.github.katrix.spongynbt.nbt.NBTType.TAG_LIST;
import static io.github.katrix.spongynbt.nbt.NBTType.TAG_STRING;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

import io.github.katrix.spongynbt.nbt.NBTCompound;
import io.github.katrix.spongynbt.nbt.NBTList;
import io.github.katrix.spongynbt.nbt.NBTString;
import io.github.katrix.spongynbt.nbt.NBTTag;
import io.github.katrix.spongynbt.sponge.NBTTranslator;
import io.github.katrix_.chitchat.chat.ChannelChitChat;

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
		Optional<NBTTag> optRootTag = channelTag.getJava(ROOT);

		if(optRootTag.isPresent() && optRootTag.get().getType() == TAG_COMPOUND) {
			NBTCompound rootTag = (NBTCompound)optRootTag.get();
			Optional<NBTTag> optName = rootTag.getJava(NAME);
			Optional<NBTTag> optPrefix = rootTag.getJava(PREFIX);
			Optional<NBTTag> optDescription = rootTag.getJava(DESCRIPTION);
			Optional<NBTTag> optChildren = rootTag.getJava(CHILDREN);

			if(optName.isPresent() && optPrefix.isPresent() && optDescription.isPresent() && optChildren.isPresent()) {
				if(optName.get().getType() == TAG_STRING && optPrefix.get().getType() == TAG_COMPOUND && optDescription.get().getType() == TAG_COMPOUND
						&& optChildren.get().getType() == TAG_LIST) {
					NBTString name = (NBTString)optName.get();
					NBTCompound prefix = (NBTCompound)optPrefix.get();
					NBTCompound description = (NBTCompound)optDescription.get();
					NBTList children = (NBTList)optChildren.get();

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
		}

		return Optional.empty();
	}

	private void loadChannel(NBTCompound channelCompound, ChannelChitChat parent) {
		Optional<NBTTag> optName = channelCompound.getJava(NAME);
		Optional<NBTTag> optPrefix = channelCompound.getJava(PREFIX);
		Optional<NBTTag> optDescription = channelCompound.getJava(DESCRIPTION);
		Optional<NBTTag> optChildren = channelCompound.getJava(CHILDREN);

		if(optName.isPresent() && optPrefix.isPresent() && optDescription.isPresent() && optChildren.isPresent()) {
			if(optName.get().getType() == TAG_STRING && optPrefix.get().getType() == TAG_COMPOUND && optDescription.get().getType() == TAG_COMPOUND
					&& optChildren.get().getType() == TAG_LIST) {
				NBTString name = (NBTString)optName.get();
				NBTCompound prefix = (NBTCompound)optPrefix.get();
				NBTCompound description = (NBTCompound)optDescription.get();
				NBTList children = (NBTList)optChildren.get();

				Optional<Text> textPrefix = Sponge.getDataManager().deserialize(Text.class, NBTTranslator.translateFrom(prefix));
				Optional<Text> textDescription = Sponge.getDataManager().deserialize(Text.class, NBTTranslator.translateFrom(description));

				if(children.getListType() == TAG_COMPOUND && textPrefix.isPresent() && textDescription.isPresent()) {
					ChannelChitChat createdChannel = parent.createChannel(name.value(), textPrefix.get(), textDescription.get());
					List<NBTCompound> childrenCompound = children.getAllJava().stream().map(t -> (NBTCompound)t).collect(Collectors.toList());
					childrenCompound.forEach(c -> loadChannel(c, createdChannel));
				}
			}
		}
	}

	@Override
	public boolean saveRootChannel() {
		ChannelChitChat.ChannelRoot channel = ChannelChitChat.getRoot();
		NBTCompound channelTag = getChannelTag();
		channelTag.setTag(ROOT, saveChannel(channel));

		dirty = true;
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
