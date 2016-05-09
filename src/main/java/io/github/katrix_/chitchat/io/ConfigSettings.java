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

import java.nio.file.Path;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class ConfigSettings extends ConfigurateBase {

	public static final String TEMPLATE_PLAYER = "playerName";
	public static final String TEMPLATE_HEADER = "header";
	public static final String TEMPLATE_SUFFIX = "suffix";
	public static final String TEMPLATE_PREFIX = "prefix";
	public static final String TEMPLATE_MESSAGE = "message";
	public static final String TEMPLATE_CHANNEL = "channel";

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
	private TextTemplate chattingJoinTemplate = TextTemplate.of(TextColors.YELLOW, Text.of("You are currently chatting in the channel "),
			TextTemplate.arg(TEMPLATE_CHANNEL));
	private TextTemplate joinTemplate = TextTemplate.of(Text.of(TextColors.GREEN, "The player "), TextTemplate.arg("body").color(TextColors.AQUA),
			Text.of(TextColors.GREEN, " has joined the server"));
	private TextTemplate disconnectTemplate = TextTemplate.of(Text.of(TextColors.RED, "The player "), TextTemplate.arg("body").color(TextColors.AQUA),
			Text.of(TextColors.RED, " has left the server"));

	private boolean chatPling = true;
	private boolean debug = false;
	private StorageType storage = StorageType.PLAINTEXT;
	private boolean nbtCompressed = true;
	private long saveInterval = 10;

	public ConfigSettings(Path path, String name) {
		super(path, name, false);
		loadData();
		saveFile();
	}

	@Override
	public void loadData() {
		CommentedConfigurationNode node;
		try {
			TypeToken<TextTemplate> textTemplateToken = TypeToken.of(TextTemplate.class);
			TypeToken<Text> textToken = TypeToken.of(Text.class);

			defaultHeader = getOrDefaultObject(cfgRoot.getNode("chat", "defaultHeader"), textTemplateToken, defaultHeader);
			defaultSuffix = getOrDefaultObject(cfgRoot.getNode("chat", "defaultSuffix"), textToken, defaultSuffix);
			globalPrefix = getOrDefaultObject(cfgRoot.getNode("chat", "globalPrefix"), textToken, globalPrefix);
			headerTemplate = getOrDefaultObject(cfgRoot.getNode("chat", "headerTemplate"), textTemplateToken, headerTemplate);
			suffixTemplate = getOrDefaultObject(cfgRoot.getNode("chat", "suffixTemplate"), textTemplateToken, suffixTemplate);
			channelTemplate = getOrDefaultObject(cfgRoot.getNode("chat", "channelTemplate"), textTemplateToken, channelTemplate);
			chattingJoinTemplate = getOrDefaultObject(cfgRoot.getNode("chat", "chattingJoinTemplate"), textTemplateToken, chattingJoinTemplate);
			joinTemplate = getOrDefaultObject(cfgRoot.getNode("chat", "joinTemplate"), textTemplateToken, joinTemplate);
			disconnectTemplate = getOrDefaultObject(cfgRoot.getNode("chat", "disconnectTemplate"), textTemplateToken, disconnectTemplate);

			meTemplate = getOrDefaultObject(cfgRoot.getNode("command", "meTemplate"), textTemplateToken, meTemplate);
			pmReciever = getOrDefaultObject(cfgRoot.getNode("command", "pmReciever"), textTemplateToken, pmReciever);
			pmSender = getOrDefaultObject(cfgRoot.getNode("command", "pmSender"), textTemplateToken, pmSender);
			shoutTemplate = getOrDefaultObject(cfgRoot.getNode("command", "shoutTemplate"), textTemplateToken, shoutTemplate);
			announceTemplate = getOrDefaultObject(cfgRoot.getNode("command", "announceTemplate"), textTemplateToken, announceTemplate);

			storage = getOrDefaultObject(cfgRoot.getNode("misc", "storage"), TypeToken.of(StorageType.class), storage);
		}
		catch(ObjectMappingException e) {
			e.printStackTrace();
		}

		node = cfgRoot.getNode("chat", "chatPling");
		chatPling = !node.isVirtual() ? node.getBoolean() : chatPling;

		node = cfgRoot.getNode("misc", "debug");
		debug = !node.isVirtual() ? node.getBoolean() : debug;

		node = cfgRoot.getNode("misc", "nbtCompressed");
		nbtCompressed = !node.isVirtual() ? node.getBoolean() : nbtCompressed;

		node = cfgRoot.getNode("misc", "saveInterval");
		saveInterval = !node.isVirtual() ? node.getLong() : saveInterval;

		super.loadData();
	}

	@Override
	public void saveData() {
		try {
			TypeToken<TextTemplate> textTemplateToken = TypeToken.of(TextTemplate.class);
			TypeToken<Text> textToken = TypeToken.of(Text.class);

			saveNodeObject(cfgRoot.getNode("chat", "defaultHeader"), textTemplateToken, defaultHeader,
					"Type = TextTemplate\nThe prefix that should be used if no prefix option from permissions is found.\nIf a prefix option is found, it will override this");
			saveNodeObject(cfgRoot.getNode("chat", "defaultSuffix"), textToken, defaultSuffix,
					"Type = Text\nThe suffix that should be used if no suffix option from permissions is found.\nIf a suffix option is found, it will override this");
			saveNodeObject(cfgRoot.getNode("chat", "globalPrefix"), textToken, globalPrefix,
					"Type = Text\nThe prefix that will be used for the global channel");
			saveNodeObject(cfgRoot.getNode("chat", "headerTemplate"), textTemplateToken, headerTemplate,
					"Type = TextTemplate\nThe format that will be used for the prefix and the name. Do note that the prefix and the name always sits side by side");
			saveNodeObject(cfgRoot.getNode("chat", "suffixTemplate"), textTemplateToken, suffixTemplate,
					"Type = TextTemplate\nThe format that will be used for the suffix");
			saveNodeObject(cfgRoot.getNode("chat", "channelTemplate"), textTemplateToken, channelTemplate,
					"Type = TextTemplate\nThe format that will be used for the channel");
			saveNodeObject(cfgRoot.getNode("chat", "joinTemplate"), textTemplateToken, joinTemplate,
					"Type = TextTemplate\nThe format that will be used when a player joins the server");
			saveNodeObject(cfgRoot.getNode("chat", "disconnectTemplate"), textTemplateToken, disconnectTemplate,
					"Type = TextTemplate\nThe format that will be used when a player leaves the server");

			saveNodeObject(cfgRoot.getNode("command", "meTemplate"), textTemplateToken, meTemplate,
					"Type = TextTemplate\nThe format that will be used for the me command");
			saveNodeObject(cfgRoot.getNode("command", "pmReciever"), textTemplateToken, pmReciever,
					"Type = TextTemplate\nThe format that will be used for the receiver of a PM");
			saveNodeObject(cfgRoot.getNode("command", "pmSender"), textTemplateToken, pmSender,
					"Type = TextTemplate\nThe format that will be used for the sender of a PM");
			saveNodeObject(cfgRoot.getNode("command", "shoutTemplate"), textTemplateToken, shoutTemplate,
					"Type = TextTemplate\nThe format that will be used for the shout command");
			saveNodeObject(cfgRoot.getNode("command", "announceTemplate"), textTemplateToken, announceTemplate,
					"Type = TextTemplate\nThe format that will be used for the announce command");

			saveNodeObject(cfgRoot.getNode("misc", "storage"), TypeToken.of(StorageType.class), storage,
					"Type = Enum\nWhat type of storage to use. Valid options are:\nPLAINTEXT\nH2\nNBT !!EXPERIMENTAL!!");
		}
		catch(ObjectMappingException e) {
			e.printStackTrace();
		}

		cfgRoot.getNode("chat", "chatPling").setComment("Type = Boolean\nPlay a sound for the player when his name is mentioned, or he receives a PM")
				.setValue(chatPling);
		cfgRoot.getNode("misc", "debug").setComment("Type = Boolean\nOutput debug stuff in console").setValue(debug);
		cfgRoot.getNode("misc", "nbtCompressed").setComment("Type = Boolean\nIf the NBT should be compressed.\nOnly used if NBT is used as a storage type").setValue(nbtCompressed);
		cfgRoot.getNode("misc", "saveInterval").setComment("Type = Long\nHow many minutes between saves for NBT.\nOnly used if NBT is used as a storage type").setValue(saveInterval);
	}

	public void reload() {
		cfgRoot = loadRoot();
		loadData();
		saveFile();
	}

	private <T> T getOrDefaultObject(ConfigurationNode node, TypeToken<T> typeToken, T defaultValue) throws ObjectMappingException {
		return !node.isVirtual() ? node.getValue(typeToken) : defaultValue;
	}

	private <T> void saveNodeObject(CommentedConfigurationNode node, TypeToken<T> typeToken, T value, String comment) throws ObjectMappingException {
		node.setComment(comment);
		node.setValue(typeToken, value);
	}
	public TextTemplate getDefaultHeader() {
		return defaultHeader;
	}

	public Text getDefaultSuffix() {
		return defaultSuffix;
	}

	public Text getGlobalChannelPrefix() {
		return globalPrefix;
	}

	public TextTemplate getHeaderTemplate() {
		return headerTemplate;
	}

	public TextTemplate getSuffixTemplate() {
		return suffixTemplate;
	}

	public TextTemplate getMeTemplate() {
		return meTemplate;
	}

	public TextTemplate getPmReceiverTemplate() {
		return pmReciever;
	}

	public TextTemplate getPmSenderTemplate() {
		return pmSender;
	}

	public TextTemplate getShoutTemplate() {
		return shoutTemplate;
	}

	public TextTemplate getChannelTemplate() {
		return channelTemplate;
	}

	public TextTemplate getAnnounceTemplate() {
		return announceTemplate;
	}

	public TextTemplate getChattingJoinTemplate() {
		return chattingJoinTemplate;
	}

	public TextTemplate getJoinTemplate() {
		return joinTemplate;
	}

	public TextTemplate getDisconnectTemplate() {
		return disconnectTemplate;
	}

	public boolean getChatPling() {
		return chatPling;
	}

	public boolean getDebug() {
		return debug;
	}

	public StorageType getStorageType() {
		return storage;
	}

	public boolean getNbtCompressed() {
		return nbtCompressed;
	}

	public long getSaveInterval() {
		return saveInterval;
	}
}
