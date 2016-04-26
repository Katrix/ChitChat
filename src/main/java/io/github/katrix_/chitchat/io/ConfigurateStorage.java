/*
 * This file is part of PermissionBlock, licensed under the MIT License (MIT).
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

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.spongepowered.api.text.Text;

import com.google.common.reflect.TypeToken;

import io.github.katrix_.chitchat.chat.ChannelChitChat;
import io.github.katrix_.chitchat.chat.ChitChatChannels;
import io.github.katrix_.chitchat.chat.UserChitChat;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class ConfigurateStorage extends ConfigurateBase implements IPersistentStorage {

	private static final String[] CHANNELS = {"data", "channel"};
	private static final String[] PLAYERS = {"data", "players"};

	public ConfigurateStorage(Path path, String name) {
		super(path, name);
	}

	@SuppressWarnings("ConfusingArgumentToVarargsMethod")
	@Override
	public boolean reloadChannels() {
		try {
			List<ChannelChitChat> channels = cfgRoot.getNode(CHANNELS).getList(TypeToken.of(ChannelChitChat.class));
			ChitChatChannels.clearChannelMap();
			channels.forEach(ChitChatChannels::addChannel);
			return true;
		}
		catch(ObjectMappingException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean saveChannel(ChannelChitChat channel) {
		cfgRoot.getNode(CHANNELS, channel.getName()).setValue(channel);
		return true;
	}

	@Override
	public boolean updateChannel(String channel, @Nullable Text prefix, @Nullable Text description) {
		if(prefix != null) {
			cfgRoot.getNode(CHANNELS, channel, "prefix").setValue(prefix);
		}
		if(description != null) {
			cfgRoot.getNode(CHANNELS, channel, "prefix").setValue(description);
		}
		return !(prefix == null || description == null);
	}

	@SuppressWarnings("ConfusingArgumentToVarargsMethod")
	@Override
	public boolean deleteChannel(String channel) {
		cfgRoot.getNode(CHANNELS).removeChild(channel);
		return true;
	}

	@Override
	public ChannelChitChat getChannelForUser(UUID uuid) {
		String channelName = cfgRoot.getNode(PLAYERS, uuid.toString(), "channel").getString();

		if(ChitChatChannels.doesChannelExist(channelName)) {
			return ChitChatChannels.getChannel(channelName);
		}

		return ChitChatChannels.getGlobalChannel();
	}

	@Override
	public boolean updateUserChannel(UserChitChat user) {
		cfgRoot.getNode(PLAYERS, user.getUUID().toString()).setValue(user);
		return true;
	}

	@Override
	protected void saveData() {
		//NO-OP We save and load on demand
	}
}
