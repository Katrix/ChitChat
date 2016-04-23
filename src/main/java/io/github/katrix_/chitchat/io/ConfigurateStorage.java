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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.spongepowered.api.text.Text;

import com.google.common.reflect.TypeToken;

import io.github.katrix_.chitchat.ChitChat;
import io.github.katrix_.chitchat.chat.ChannelChitChat;
import io.github.katrix_.chitchat.chat.ChitChatChannels;
import io.github.katrix_.chitchat.chat.UserChitChat;
import io.github.katrix_.chitchat.helper.LogHelper;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class ConfigurateStorage implements IPersistentStorage {

	private static final String[] CHANNELS = {"data", "channel"};
	private static final String[] PLAYERS = {"data", "players"};

	private Path cfgFile = Paths.get(ChitChat.getPlugin().getConfigDir() + "/storage.conf");
	private ConfigurationLoader<CommentedConfigurationNode> cfgLoader = HoconConfigurationLoader.builder().setPath(cfgFile).build();
	private CommentedConfigurationNode cfgRoot;

	public ConfigurateStorage() {
		initFile();
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

	private void loadFile() {
		LogHelper.info("Loading plaintext storage");

		try {
			cfgRoot = cfgLoader.load();
		}
		catch(IOException e) {
			cfgRoot = cfgLoader.createEmptyNode(ConfigurationOptions.defaults());
		}
	}

	private void saveFile() {
		try {
			cfgLoader.save(cfgRoot);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	private void initFile() {
		if(cfgRoot == null) {
			loadFile();
		}

		File parent = cfgFile.getParent().toFile();
		if(!parent.exists()) {
			if(!parent.mkdirs()) {
				LogHelper.error("Something went wring when creating the parent directory for the plaintext storage");
				return;
			}
		}

		saveFile();
	}
}
