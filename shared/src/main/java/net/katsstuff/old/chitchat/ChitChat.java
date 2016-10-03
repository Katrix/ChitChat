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
package net.katsstuff.old.chitchat;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;

import net.katsstuff.old.chitchat.chat.ChannelChitChat;
import net.katsstuff.old.chitchat.chat.ChannelChitChatSerializer;
import net.katsstuff.old.chitchat.chat.ChannelRootSerializer;
import net.katsstuff.old.chitchat.chat.ChatListener;
import net.katsstuff.old.chitchat.chat.data.ImmutableUserChitChatData;
import net.katsstuff.old.chitchat.chat.data.UserChitChatData;
import net.katsstuff.old.chitchat.chat.data.UserChitChatManipulatorBuilder;
import net.katsstuff.old.chitchat.command.CmdAnnounce;
import net.katsstuff.old.chitchat.command.CmdChannel;
import net.katsstuff.old.chitchat.command.CmdChitChat;
import net.katsstuff.old.chitchat.command.CmdForceChat;
import net.katsstuff.old.chitchat.command.CmdMe;
import net.katsstuff.old.chitchat.command.CmdPM;
import net.katsstuff.old.chitchat.command.CmdReply;
import net.katsstuff.old.chitchat.command.CmdShout;
import net.katsstuff.old.chitchat.command.CommandBase;
import net.katsstuff.old.chitchat.helper.LogHelper;
import net.katsstuff.old.chitchat.io.ConfigSettings;
import net.katsstuff.old.chitchat.io.ConfigurateStorage;
import net.katsstuff.old.chitchat.io.IPersistentStorage;
import net.katsstuff.old.chitchat.io.NBTStorage;
import net.katsstuff.old.chitchat.io.StorageType;
import net.katsstuff.old.chitchat.io.StorageTypeSerializer;
import net.katsstuff.old.chitchat.lib.LibPlugin;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;

@Plugin(id = LibPlugin.ID, name = LibPlugin.NAME, version = LibPlugin.VERSION, authors = LibPlugin.AUTHORS, description = LibPlugin.DESCRIPTION)
public class ChitChat {

	private static ChitChat plugin;
	private IPersistentStorage storage;
	private ConfigSettings cfg;

	@Inject
	private PluginContainer pluginContainer;

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
		DataManager dataManager = Sponge.getDataManager();

		dataManager.register(UserChitChatData.class, ImmutableUserChitChatData.class, new UserChitChatManipulatorBuilder());

		TypeSerializerCollection serializers = TypeSerializers.getDefaultSerializers();
		serializers.registerType(TypeToken.of(ChannelChitChat.ChannelRoot.class), new ChannelRootSerializer());
		serializers.registerType(TypeToken.of(ChannelChitChat.class), new ChannelChitChatSerializer());
		serializers.registerType(TypeToken.of(StorageType.class), new StorageTypeSerializer());

		cfg = new ConfigSettings(configDir);
		storage = createStorage(configDir);

		registerCommand(CmdChannel.INSTANCE);
		registerCommand(CmdShout.INSTANCE);
		registerCommand(CmdPM.INSTANCE);
		registerCommand(CmdReply.INSTANCE);
		registerCommand(CmdMe.INSTANCE);
		registerCommand(CmdChitChat.INSTANCE);
		registerCommand(CmdAnnounce.INSTANCE);
		registerCommand(CmdForceChat.INSTANCE);

		Sponge.getEventManager().registerListeners(this, new ChatListener());
		ChannelChitChat.ChannelRoot.init();
	}

	@Listener
	public void reload(GameReloadEvent event) {
		getConfig().reload();
		storage.loadRootChannel();
	}

	public Logger getLog() {
		return log;
	}

	public static ChitChat getPlugin() {
		return plugin;
	}

	public static IPersistentStorage getStorage() {
		return plugin.storage;
	}

	public static ConfigSettings getConfig() {
		return plugin.cfg;
	}

	public static PluginContainer getPluginContainer() {
		return plugin.pluginContainer;
	}

	/**
	 * Register a command both as a command, and to the help system.
	 */
	private void registerCommand(CommandBase command) {
		Sponge.getCommandManager().register(this, command.getCommand(), command.getAliases());
		command.registerHelp();
	}

	private IPersistentStorage createStorage(Path path) {
		String name = "storage";
		switch(cfg.getStorageType()) {
			case PLAINTEXT:
				return new ConfigurateStorage(path, name);
			case NBT:
				try {
					return new NBTStorage(path, name, cfg.getNbtCompressed());
				}
				catch(IOException e) {
					e.printStackTrace();
					LogHelper.error("Something went wrong when initiating the NBT file for ChitChat. Defaulting to plaintext");
					return new ConfigurateStorage(path, name);
				}
			default:
				LogHelper.error("Something went wrong when getting the storage type to use for ChitChat. Defaulting to plaintext");
				return new ConfigurateStorage(path, name);
		}
	}
}
