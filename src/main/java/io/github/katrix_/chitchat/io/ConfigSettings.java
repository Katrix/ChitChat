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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import com.google.common.reflect.TypeToken;

import io.github.katrix_.chitchat.ChitChat;
import io.github.katrix_.chitchat.helper.LogHelper;
import io.github.katrix_.chitchat.lib.LibPlugin;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class ConfigSettings {

	public static final String TEMPLATE_PLAYER = "playerName";
	public static final String TEMPLATE_HEADER = "header";
	public static final String TEMPLATE_SUFFIX = "suffix";
	public static final String TEMPLATE_PREFIX = "prefix";
	public static final String TEMPLATE_MESSAGE = "message";
	public static final String TEMPLATE_CHANNEL = "channel";

	private static ConfigSettings cfg = new ConfigSettings();

	private Path cfgFile = Paths.get(ChitChat.getPlugin().getConfigDir() + "/" + LibPlugin.ID + ".conf");
	private ConfigurationLoader<CommentedConfigurationNode> cfgLoader = HoconConfigurationLoader.builder().setPath(cfgFile).build();
	private CommentedConfigurationNode cfgRoot;

	private TextTemplate defaultHeader = TextTemplate.of(TextTemplate.arg(TEMPLATE_PLAYER).color(TextColors.GRAY));
	private Text defaultSuffix = Text.EMPTY;
	private Text globalPrefix = Text.of(TextColors.GOLD, "G");
	private TextTemplate headerTemplate = TextTemplate.of(TextTemplate.arg(TEMPLATE_HEADER), Text.of(": "));
	private TextTemplate suffixTemplate = TextTemplate.of(TextTemplate.arg(TEMPLATE_SUFFIX));

	private TextTemplate meTemplate = TextTemplate.of(Text.of(TextColors.GOLD, "* "), TextTemplate.arg(TEMPLATE_PLAYER), Text.of(" "),
			TextTemplate.arg(TEMPLATE_MESSAGE));
	private TextTemplate pmReciever = TextTemplate.of(Text.of(TextColors.LIGHT_PURPLE, "From "), TextTemplate.arg(TEMPLATE_PLAYER), Text.of(": "),
			TextTemplate.arg(TEMPLATE_MESSAGE));
	private TextTemplate pmSender = TextTemplate.of(Text.of(TextColors.LIGHT_PURPLE, "To "), TextTemplate.arg(TEMPLATE_PLAYER), Text.of(": "),
			TextTemplate.arg(TEMPLATE_MESSAGE));
	private TextTemplate shoutTemplate = TextTemplate.of(Text.of(TextColors.YELLOW, TextStyles.BOLD), TextTemplate.arg(TEMPLATE_PLAYER),
			Text.of(" shouts: "), TextTemplate.arg(TEMPLATE_MESSAGE));
	private TextTemplate channelTemplate = TextTemplate.of(Text.of(TextColors.WHITE, "["), TextTemplate.arg(TEMPLATE_PREFIX),
			Text.of(TextColors.WHITE, "] "));
	private TextTemplate announceTemplate = TextTemplate.of(Text.of(TextColors.YELLOW, TextStyles.UNDERLINE), TextTemplate.arg(TEMPLATE_PLAYER),
			Text.of(": "), TextTemplate.arg(TEMPLATE_MESSAGE));
	private TextTemplate chattingJoinTemplate = TextTemplate.of(Text.of("You are currently chatting in the channel "), TextTemplate.arg(TEMPLATE_CHANNEL));

	private boolean chatPling = true;
	private boolean debug = false;

	public void loadSettings() {
		CommentedConfigurationNode node;
		try {
			node = cfgRoot.getNode("chat", "defaultHeader");
			defaultHeader = !node.isVirtual() ? node.getValue(TypeToken.of(TextTemplate.class)) : defaultHeader;

			node = cfgRoot.getNode("chat", "defaultSuffix");
			defaultSuffix = !node.isVirtual() ? node.getValue(TypeToken.of(Text.class)) : defaultSuffix;

			node = cfgRoot.getNode("chat", "globalPrefix");
			globalPrefix = !node.isVirtual() ? node.getValue(TypeToken.of(Text.class)) : globalPrefix;

			node = cfgRoot.getNode("chat", "headerTemplate");
			headerTemplate = !node.isVirtual() ? node.getValue(TypeToken.of(TextTemplate.class)) : headerTemplate;

			node = cfgRoot.getNode("chat", "suffixTemplate");
			suffixTemplate = !node.isVirtual() ? node.getValue(TypeToken.of(TextTemplate.class)) : suffixTemplate;

			node = cfgRoot.getNode("chat", "channelTemplate");
			channelTemplate = !node.isVirtual() ? node.getValue(TypeToken.of(TextTemplate.class)) : channelTemplate;

			node = cfgRoot.getNode("command", "meTemplate");
			meTemplate = !node.isVirtual() ? node.getValue(TypeToken.of(TextTemplate.class)) : meTemplate;

			node = cfgRoot.getNode("command", "pmReciever");
			pmReciever = !node.isVirtual() ? node.getValue(TypeToken.of(TextTemplate.class)) : pmReciever;

			node = cfgRoot.getNode("command", "pmSender");
			pmSender = !node.isVirtual() ? node.getValue(TypeToken.of(TextTemplate.class)) : pmSender;

			node = cfgRoot.getNode("command", "shoutTemplate");
			shoutTemplate = !node.isVirtual() ? node.getValue(TypeToken.of(TextTemplate.class)) : shoutTemplate;

			node = cfgRoot.getNode("command", "announceTemplate");
			announceTemplate = !node.isVirtual() ? node.getValue(TypeToken.of(TextTemplate.class)) : announceTemplate;

			node = cfgRoot.getNode("command", "chattingJoinTemplate");
			chattingJoinTemplate = !node.isVirtual() ? node.getValue(TypeToken.of(TextTemplate.class)) : chattingJoinTemplate;
		}
		catch(ObjectMappingException e) {
			e.printStackTrace();
		}

		node = cfgRoot.getNode("chat", "chatPling");
		chatPling = !node.isVirtual() ? node.getBoolean() : chatPling;

		node = cfgRoot.getNode("misc", "debug");
		debug = !node.isVirtual() ? node.getBoolean() : debug;

		saveSettings();
	}

	public void saveSettings() {
		try {
			cfgRoot.getNode("chat", "defaultHeader")
					.setComment(
							"Type = TextTemplate\nThe prefix that should be used if no prefix option for permissions is found. If a prefix option is found, it will override this")
					.setValue(TypeToken.of(TextTemplate.class), defaultHeader);
			cfgRoot.getNode("chat", "defaultSuffix")
					.setComment(
							"Type = Text\nThe suffix that should be used if no suffix option for permissions is found. If a suffix option is found, it will override this")
					.setValue(TypeToken.of(Text.class), defaultSuffix);
			cfgRoot.getNode("chat", "globalPrefix").setComment("Type = Text\nThe prefix that will be used for the global channel.")
					.setValue(TypeToken.of(Text.class), globalPrefix);
			cfgRoot.getNode("chat", "headerTemplate")
					.setComment(
							"Type = TextTemplate\nThe format that will be used for the prefix and the name. Do note that the prefix and the name always sits side by side")
					.setValue(TypeToken.of(TextTemplate.class), headerTemplate);
			cfgRoot.getNode("chat", "suffixTemplate").setComment("Type = TextTemplate\nThe format that will be used for the suffix.")
					.setValue(TypeToken.of(TextTemplate.class), suffixTemplate);
			cfgRoot.getNode("chat", "channelTemplate").setComment("Type = TextTemplate\nThe format that will be used for the channel.")
					.setValue(TypeToken.of(TextTemplate.class), channelTemplate);

			cfgRoot.getNode("command", "meTemplate").setComment("Type = TextTemplate\nThe format that will be used for the me command.")
					.setValue(TypeToken.of(TextTemplate.class), meTemplate);
			cfgRoot.getNode("command", "pmReciever").setComment("Type = TextTemplate\nThe format that will be used for the reciever of a PM.")
					.setValue(TypeToken.of(TextTemplate.class), pmReciever);
			cfgRoot.getNode("command", "pmSender").setComment("Type = TextTemplate\nThe format that will be used for the sender of a PM.")
					.setValue(TypeToken.of(TextTemplate.class), pmSender);
			cfgRoot.getNode("command", "shoutTemplate").setComment("Type = TextTemplate\nThe format that will be used for the shout command.")
					.setValue(TypeToken.of(TextTemplate.class), shoutTemplate);
			cfgRoot.getNode("command", "announceTemplate").setComment("Type = TextTemplate\nThe format that will be used for the announce command.")
					.setValue(TypeToken.of(TextTemplate.class), announceTemplate);
			cfgRoot.getNode("command", "chattingJoinTemplate").setComment("Type = TextTemplate\nThe format that will be used for telling players what channel they are in when they join the server.")
					.setValue(TypeToken.of(TextTemplate.class), announceTemplate);
		}
		catch(ObjectMappingException e) {
			e.printStackTrace();
		}

		cfgRoot.getNode("chat", "chatPling").setComment("Type = Boolean\nPlay a sound for the player when his name is mentioned, or he recieves a PM")
				.setValue(chatPling);
		cfgRoot.getNode("misc", "debug").setComment("Type = Boolean\nOutput debug stuff in console").setValue(debug);
	}

	public void loadFile() {
		LogHelper.info("Loading config");
		try {
			cfgRoot = cfgLoader.load();
		}
		catch(IOException e) {
			cfgRoot = cfgLoader.createEmptyNode(ConfigurationOptions.defaults());
		}

		loadSettings();
	}

	public void saveFile() {
		try {
			cfgLoader.save(cfgRoot);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void initFile() {
		if(cfgRoot == null) {
			loadFile();
		}

		File parent = cfgFile.getParent().toFile();
		if(!parent.exists()) {
			if(!parent.mkdirs()) {
				LogHelper.error("Something went wring when creating the parent directory for the config");
				return;
			}
		}

		saveFile();
	}

	public static ConfigSettings getConfig() {
		return cfg;
	}

	public static TextTemplate getDefaultHeader() {
		return cfg.defaultHeader;
	}

	public static Text getDefaultSuffix() {
		return cfg.defaultSuffix;
	}

	public static Text getGlobalChannelPrefix() {
		return cfg.globalPrefix;
	}

	public static TextTemplate getHeaderTemplate() {
		return cfg.headerTemplate;
	}

	public static TextTemplate getSuffixTemplate() {
		return cfg.suffixTemplate;
	}

	public static TextTemplate getMeTemplate() {
		return cfg.meTemplate;
	}

	public static TextTemplate getPmRecieverTemplate() {
		return cfg.pmReciever;
	}

	public static TextTemplate getPmSenderTemplate() {
		return cfg.pmSender;
	}

	public static TextTemplate getShoutTemplate() {
		return cfg.shoutTemplate;
	}

	public static TextTemplate getChannelTemplate() {
		return cfg.channelTemplate;
	}

	public static TextTemplate getAnnounceTemplate() {
		return cfg.announceTemplate;
	}

	public static TextTemplate getChattingJoinTemplate() {
		return cfg.chattingJoinTemplate;
	}

	public static boolean getChatPling() {
		return cfg.chatPling;
	}

	public static boolean getDebug() {
		return cfg.debug;
	}
}
