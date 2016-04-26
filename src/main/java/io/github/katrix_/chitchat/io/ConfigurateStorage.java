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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.spongepowered.api.text.Text;

import com.google.common.reflect.TypeToken;

import io.github.katrix_.chitchat.chat.ChannelChitChat;
import io.github.katrix_.chitchat.chat.ChitChatChannels;
import io.github.katrix_.chitchat.chat.UserChitChat;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class ConfigurateStorage extends ConfigurateBase implements IPersistentStorage {

	private static final String CHANNELS = "channel";
	private static final String PLAYERS = "players";

	public ConfigurateStorage(Path path, String name) {
		super(path, name, true);
	}

	@Override
	public boolean reloadChannels() {
		List<ChannelChitChat> channels = new ArrayList<>();
		Collection<? extends CommentedConfigurationNode> children = cfgRoot.getNode(CHANNELS).getChildrenMap().values();

		for(ConfigurationNode node : children) {
			try {
				channels.add(deserialize(node));
			}
			catch(ObjectMappingException e) {
				e.printStackTrace();
			}
		}

		ChitChatChannels.clearMap();
		channels.forEach(ChitChatChannels::add);
		return true;
	}

	private ChannelChitChat deserialize(ConfigurationNode node) throws ObjectMappingException {
		return node.getValue(TypeToken.of(ChannelChitChat.class));
	}

	@Override
	public boolean saveChannel(ChannelChitChat channel) {
		try {
			cfgRoot.getNode(CHANNELS, channel.getName()).setValue(TypeToken.of(ChannelChitChat.class), channel);
			saveFile();
		}
		catch(ObjectMappingException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean updateChannel(String channel, @Nullable Text prefix, @Nullable Text description) {
		try {
			if(prefix != null) {
				cfgRoot.getNode(CHANNELS, channel, "prefix").setValue(TypeToken.of(Text.class), prefix);
			}
			if(description != null) {
				cfgRoot.getNode(CHANNELS, channel, "prefix").setValue(TypeToken.of(Text.class), description);
			}
			saveFile();
			return !(prefix == null || description == null);
		}
		catch(ObjectMappingException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean deleteChannel(String channel) {
		cfgRoot.getNode(CHANNELS).removeChild(channel);
		saveFile();
		return true;
	}

	@Override
	public ChannelChitChat getChannelForUser(UUID uuid) {
		String channelName = cfgRoot.getNode(PLAYERS, uuid.toString(), "channel").getString();

		if(ChitChatChannels.existName(channelName)) {
			return ChitChatChannels.get(channelName);
		}

		return ChitChatChannels.getGlobal();
	}

	@Override
	public boolean updateUser(UserChitChat user) {
		try {
			cfgRoot.getNode(PLAYERS, user.getUUID().toString()).setValue(TypeToken.of(UserChitChat.class),user);
			saveFile();
		}
		catch(ObjectMappingException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	protected void saveData() {
		//NO-OP We save and load on demand
	}
}
