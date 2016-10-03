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
package net.katsstuff.old.chitchat.io;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.Predicate;

import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;

import net.katsstuff.old.chitchat.lib.LibPlugin;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class ConfigSettings extends ConfigurateBase {

	public static final String TEMPLATE_PLAYER = LibPlugin.ID + ":playerName";
	@SuppressWarnings("WeakerAccess")
	public static final String TEMPLATE_HEADER = MessageChannelEvent.PARAM_MESSAGE_HEADER; //No Plugin id as this refears to the already existing template that we replace
	public static final String TEMPLATE_SUFFIX = LibPlugin.ID + ":suffix";
	public static final String TEMPLATE_PREFIX = LibPlugin.ID + ":prefix";
	public static final String TEMPLATE_MESSAGE = LibPlugin.ID + ":message";
	public static final String TEMPLATE_CHANNEL = LibPlugin.ID + ":channel";

	private Text defaultPrefix = Text.EMPTY;
	private Text defaultSuffix = Text.EMPTY;
	private Text globalPrefix = Text.of(TextColors.GOLD, "G");
	private TextTemplate headerTemplate = TextTemplate.of(TextTemplate.arg(TEMPLATE_PREFIX).optional(), TextTemplate.arg(TEMPLATE_HEADER), Text.of(": "));
	private TextTemplate suffixTemplate = TextTemplate.of(TextTemplate.arg(TEMPLATE_SUFFIX));

	private TextTemplate meTemplate = TextTemplate.of(Text.of(TextColors.GOLD, "* "), TextTemplate.arg(TEMPLATE_HEADER), Text.of(" "));
	private TextTemplate pmReciever = TextTemplate.of(Text.of(TextColors.LIGHT_PURPLE, "From "), TextTemplate.arg(TEMPLATE_HEADER), Text.of(": "));
	private TextTemplate pmSender = TextTemplate.of(Text.of(TextColors.LIGHT_PURPLE, "To "), TextTemplate.arg(TEMPLATE_HEADER), Text.of(": "));
	private TextTemplate shoutTemplate = TextTemplate.of(Text.of(TextColors.YELLOW, TextStyles.BOLD), TextTemplate.arg(TEMPLATE_HEADER),
			Text.of(" shouts: "));
	private TextTemplate channelTemplate = TextTemplate.of(Text.of(TextColors.WHITE, "["), TextTemplate.arg(TEMPLATE_PREFIX),
			Text.of(TextColors.WHITE, "] "));
	private TextTemplate announceTemplate = TextTemplate.of(Text.of(TextColors.YELLOW, TextStyles.UNDERLINE), TextTemplate.arg(TEMPLATE_HEADER),
			Text.of(": "));
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

	public ConfigSettings(Path path) {
		super(path, "settings", false);
		loadData();
		saveFile();
	}

	private void updateOldData() {
		try {
			TypeToken<TextTemplate> textTemplateToken = TypeToken.of(TextTemplate.class);

			ImmutableSet<String> oldArgs = ImmutableSet.of(TEMPLATE_MESSAGE, TEMPLATE_MESSAGE, "playerName");
			Predicate<TextTemplate> messagePredicate = t -> {
				Set<String> argSet = t.getArguments().keySet();
				return oldArgs.stream().anyMatch(argSet::contains);
			};

			//playerName and message
			updateOldObject(cfgRoot.getNode("command", "meTemplate"), textTemplateToken, messagePredicate, meTemplate);
			updateOldObject(cfgRoot.getNode("command", "pmReciever"), textTemplateToken, messagePredicate, pmReciever);
			updateOldObject(cfgRoot.getNode("command", "pmSender"), textTemplateToken, messagePredicate, pmSender);
			updateOldObject(cfgRoot.getNode("command", "shoutTemplate"), textTemplateToken, messagePredicate, shoutTemplate);
			updateOldObject(cfgRoot.getNode("command", "announceTemplate"), textTemplateToken, messagePredicate, announceTemplate);

			//suffix
			updateOldObject(cfgRoot.getNode("chat", "suffixTemplate"), textTemplateToken,
					t -> t.getArguments().containsKey("suffix"), suffixTemplate);

			//prefix
			updateOldObject(cfgRoot.getNode("chat", "headerTemplate"), textTemplateToken,
					t -> t.getArguments().containsKey("prefix"), headerTemplate);
			updateOldObject(cfgRoot.getNode("chat", "channelTemplate"), textTemplateToken,
					t -> t.getArguments().containsKey("prefix"), channelTemplate);

			//channel
			updateOldObject(cfgRoot.getNode("chat", "chattingJoinTemplate"), textTemplateToken,
					t -> t.getArguments().containsKey("channel"), chattingJoinTemplate);

			//Remove old defaultHeader, there is now defaultPrefix instead
			cfgRoot.getNode("chat", "defaultHeader").setValue(null);
		}
		catch(ObjectMappingException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void loadData() {
		updateOldData();
		CommentedConfigurationNode node;
		try {
			TypeToken<TextTemplate> textTemplateToken = TypeToken.of(TextTemplate.class);
			TypeToken<Text> textToken = TypeToken.of(Text.class);

			defaultPrefix = getOrDefaultObject(cfgRoot.getNode("chat", "defaultPrefix"), textToken, defaultPrefix);
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

		super.loadData();
	}

	@Override
	public void saveData() {
		try {
			TypeToken<TextTemplate> textTemplateToken = TypeToken.of(TextTemplate.class);
			TypeToken<Text> textToken = TypeToken.of(Text.class);

			saveNodeObject(cfgRoot.getNode("chat", "defaultPrefix"), textToken, defaultPrefix,
					"Type = Text\nThe prefix that should be used if no prefix option from permissions is found.\nIf a prefix option is found, it will override this");
			saveNodeObject(cfgRoot.getNode("chat", "defaultSuffix"), textToken, defaultSuffix,
					"Type = Text\nThe suffix that should be used if no suffix option from permissions is found.\nIf a suffix option is found, it will override this");
			saveNodeObject(cfgRoot.getNode("chat", "globalPrefix"), textToken, globalPrefix,
					"Type = Text\nThe prefix that will be used for the global channel");
			saveNodeObject(cfgRoot.getNode("chat", "headerTemplate"), textTemplateToken, headerTemplate,
					"Type = TextTemplate\nThe format that will be used for the prefix and the name");
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

	private <T> void updateOldObject(ConfigurationNode node, TypeToken<T> typeToken, Predicate<T> predicate, T newValue) throws ObjectMappingException {
		T value = node.getValue(typeToken);

		if(value == null || predicate.test(value)) {
			node.setValue(typeToken, newValue);
		}
	}

	public Text getDefaultPrefix() {
		return defaultPrefix;
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
}
