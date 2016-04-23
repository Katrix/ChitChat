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
package io.github.katrix_.chitchat;

import java.nio.file.Path;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import io.github.katrix_.chitchat.chat.ChannelChitChat;
import io.github.katrix_.chitchat.chat.ChannelChitChatSerializer;
import io.github.katrix_.chitchat.chat.ChitChatChannels;
import io.github.katrix_.chitchat.chat.UserChitChat;
import io.github.katrix_.chitchat.chat.UserChitChatSerializer;
import io.github.katrix_.chitchat.command.CmdAnnounce;
import io.github.katrix_.chitchat.command.CmdChannel;
import io.github.katrix_.chitchat.command.CmdChitChat;
import io.github.katrix_.chitchat.command.CmdMe;
import io.github.katrix_.chitchat.command.CmdPM;
import io.github.katrix_.chitchat.command.CmdReply;
import io.github.katrix_.chitchat.command.CmdShout;
import io.github.katrix_.chitchat.command.CommandBase;
import io.github.katrix_.chitchat.helper.LogHelper;
import io.github.katrix_.chitchat.io.ConfigSettings;
import io.github.katrix_.chitchat.io.ConfigurateStorage;
import io.github.katrix_.chitchat.io.IPersistentStorage;
import io.github.katrix_.chitchat.io.SQLStorage;
import io.github.katrix_.chitchat.lib.LibPlugin;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;

@Plugin(id = LibPlugin.ID, name = LibPlugin.NAME, version = LibPlugin.VERSION, authors = LibPlugin.AUTHORS, description = LibPlugin.DESCRIPTION)
public class ChitChat {

	private static ChitChat plugin;
	private IPersistentStorage storage;

	@Inject
	private Logger log;
	@Inject
	@ConfigDir(sharedRoot = false)
	private Path configDir;

	@Listener
	public void gameConstruct(GameConstructionEvent event) {
		plugin = this;
	}

	@Listener
	public void init(GameInitializationEvent event) {
		TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(UserChitChat.class), new UserChitChatSerializer());
		TypeSerializers.getDefaultSerializers().registerType(TypeToken.of(ChannelChitChat.class), new ChannelChitChatSerializer());
		ConfigSettings.getConfig().initFile();

		storage = createStorage();

		registerCommand(CmdChannel.INSTANCE);
		registerCommand(CmdShout.INSTANCE);
		registerCommand(CmdPM.INSTANCE);
		registerCommand(CmdReply.INSTANCE);
		registerCommand(CmdMe.INSTANCE);
		registerCommand(CmdChitChat.INSTANCE);
		registerCommand(CmdAnnounce.INSTANCE);

		ChitChatChannels.initChannels();
	}

	public Logger getLog() {
		return log;
	}

	public static ChitChat getPlugin() {
		return plugin;
	}

	public Path getConfigDir() {
		return configDir;
	}

	public static IPersistentStorage getStorage() {
		return plugin.storage;
	}

	/**
	 * Register a command both as a command, and to the help system.
	 */
	private void registerCommand(CommandBase command) {
		Sponge.getCommandManager().register(this, command.getCommand(), command.getAliases());
		command.registerHelp(null);
	}

	private IPersistentStorage createStorage() {
		switch(ConfigSettings.getStorageType()) {
			case PLAINTEXT:
				return new ConfigurateStorage();
			case H2:
				try {
					storage = new SQLStorage();
				}
				catch(SQLException e) {
					e.printStackTrace();
					LogHelper.error("Something went really wrong when initiating the persistent storage for ChitChat. Defaulting back to plaintext");
					return new ConfigurateStorage();
				}
				break;
			default:
				LogHelper.error("Something went wrong when getting the storage type to use for ChitChat. Defaulting to plaintext");
				return new ConfigurateStorage();
		}

		return new ConfigurateStorage();
	}
}
