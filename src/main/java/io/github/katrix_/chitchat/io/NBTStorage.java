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

import static io.github.katrix.spongebt.nbt.NBTType.TAG_COMPOUND;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializer;
import org.spongepowered.api.text.serializer.TextSerializers;

import io.github.katrix.spongebt.nbt.NBTCompound;
import io.github.katrix.spongebt.nbt.NBTString;
import io.github.katrix.spongebt.nbt.NBTTag;
import io.github.katrix.spongebt.sponge.NBTTranslator;
import io.github.katrix_.chitchat.chat.ChannelChitChat;
import io.github.katrix_.chitchat.chat.ChitChatChannels;
import io.github.katrix_.chitchat.chat.UserChitChat;
import scala.Option;

public class NBTStorage extends NBTBase implements IPersistentStorage {

	private static final String CHANNELS = "channels";
	private static final String NAME = "name";
	private static final String PREFIX = "prefix";
	private static final String DESCRIPTION = "description";

	private static final String USERS = "users";
	private static final String USER_UUID = "userUUID";
	private static final String USER_CHANNEL = "userChannel";

	public NBTStorage(Path path, String name, boolean compressed) throws IOException {
		super(path, name, compressed);
	}

	@Override
	public boolean reloadChannels() {
		try {
			compound = load();
		}
		catch(IOException e) {
			e.printStackTrace();
		}

		NBTCompound channelTag = getChannelTag();
		Collection<NBTCompound> childTags = channelTag.valuesJava().values().stream()
				.filter(t -> t.getType() == TAG_COMPOUND)
				.map(t -> (NBTCompound)t)
				.collect(Collectors.toSet());

		for(NBTCompound tag : childTags) {
			Option<String> optName = tag.get(NAME)
					.flatMap(NBTTag::asInstanceOfNBTString)
					.map(NBTString::value);

			if(optName.isDefined()) {
				String channelName = optName.get();

				Text prefix = tag.get(PREFIX)
						.flatMap(NBTTag::asInstanceOfNBTCompound)
						.flatMap(t -> optionalToOption(Sponge.getDataManager().deserialize(Text.class, NBTTranslator.translateFrom(t))))
						.getOrElse(() -> Text.of(channelName));

				Text description = tag.get(DESCRIPTION)
						.flatMap(NBTTag::asInstanceOfNBTCompound)
						.flatMap(t -> optionalToOption(Sponge.getDataManager().deserialize(Text.class, NBTTranslator.translateFrom(t))))
						.getOrElse(Text::of);

				ChannelChitChat channel = new ChannelChitChat(channelName, description, prefix);
				ChitChatChannels.add(channel);
			}
		}

		return true;
	}

	//Stupid different types of Optional
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private static <T> Option<T> optionalToOption(Optional<T> optional) {
		if(optional.isPresent()) {
			return Option.apply(optional.get());
		}
		else {
			return Option.empty();
		}
	}

	@Override
	public boolean saveChannel(ChannelChitChat channel) {
		NBTCompound channelTag = getChannelTag();

		NBTCompound channelCompound = new NBTCompound();
		channelCompound.setString(NAME, channel.getName());
		channelCompound.setTag(PREFIX, NBTTranslator.translateData(channel.getPrefix().toContainer())); //Because we are fancy
		channelCompound.setTag(DESCRIPTION, NBTTranslator.translateData(channel.getDescription().toContainer()));

		channelTag.setTag(channel.getName(), channelCompound);

		save();
		return true;
	}

	@Override
	public boolean updateChannel(String channel, @Nullable Text prefix, @Nullable Text description) {
		if(prefix != null || description != null) {
			NBTCompound channelTag = getChannelTag();
			TextSerializer serializer = TextSerializers.FORMATTING_CODE;
			Option<NBTCompound> optTag = channelTag.get(channel).flatMap(NBTTag::asInstanceOfNBTCompound);

			if(optTag.isDefined()) {
				NBTCompound tag = optTag.get();

				if(prefix != null) {
					tag.setTag(PREFIX, new NBTString(serializer.serialize(prefix)));
				}

				if(description != null) {
					tag.setTag(DESCRIPTION, new NBTString(serializer.serialize(description)));
				}

				save();
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean deleteChannel(String channel) {
		getChannelTag().remove(channel);
		return true;
	}

	@Override
	public ChannelChitChat getChannelForUser(UUID uuid) {
		NBTCompound playerTag = getPlayerTag();
		Option<String> optChannelName = playerTag.get(uuid.toString())
				.flatMap(NBTTag::asInstanceOfNBTCompound)
				.flatMap(c -> c.get(USER_CHANNEL)
						.flatMap(NBTTag::asInstanceOfNBTString))
				.map(NBTString::value);

		if(optChannelName.isDefined()) {
			String channelName = optChannelName.get();

			if(ChitChatChannels.existName(channelName)) {
				return ChitChatChannels.get(channelName);
			}
		}

		return ChitChatChannels.getGlobal();
	}

	@Override
	public boolean updateUser(UserChitChat user) {
		NBTCompound playerTag = getPlayerTag();

		NBTCompound userTag = new NBTCompound();
		userTag.setUUID(USER_UUID, user.getUUID());
		userTag.setString(USER_CHANNEL, user.getChannel().getName());
		playerTag.setTag(user.getUUID().toString(), userTag);

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

	private NBTCompound getPlayerTag() {
		NBTCompound defaultCompound = new NBTCompound();
		NBTTag tag = compound.getOrCreate(USERS, defaultCompound);

		if(tag.getType() == TAG_COMPOUND) {
			return (NBTCompound)tag;
		}

		return defaultCompound;
	}
}
