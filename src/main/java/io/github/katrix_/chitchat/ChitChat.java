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

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import com.google.inject.Inject;

import io.github.katrix_.chitchat.chat.ChitChatChannels;
import io.github.katrix_.chitchat.command.CmdChannel;
import io.github.katrix_.chitchat.command.CmdChitChat;
import io.github.katrix_.chitchat.command.CmdMe;
import io.github.katrix_.chitchat.command.CmdPM;
import io.github.katrix_.chitchat.command.CmdReply;
import io.github.katrix_.chitchat.command.CmdShout;
import io.github.katrix_.chitchat.command.CommandBase;
import io.github.katrix_.chitchat.io.ConfigSettings;
import io.github.katrix_.chitchat.lib.LibPlugin;

@Plugin(id = LibPlugin.ID, name = LibPlugin.NAME, version = LibPlugin.VERSION, authors = LibPlugin.AUTHORS, description = LibPlugin.DESCRIPTION)
public class ChitChat {

	private static ChitChat plugin;

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
		ConfigSettings.getConfig().initFile();

		registerCommand(CmdChannel.INSTACE);
		registerCommand(CmdShout.INSTACE);
		registerCommand(CmdPM.INSTACE);
		registerCommand(CmdReply.INSTACE);
		registerCommand(CmdMe.INSTACE);
		registerCommand(CmdChitChat.INSTACE);

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

	private void registerCommand(CommandBase command) {
		Sponge.getCommandManager().register(this, command.getCommand(), command.getAliases());
		command.registerHelp(null);
	}
}
