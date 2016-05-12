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
package io.github.katrix_.chitchat.chat;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.AbstractMutableMessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.format.TextColors;

import com.google.common.collect.ImmutableMap;

import io.github.katrix_.chitchat.ChitChat;
import io.github.katrix_.chitchat.helper.LogHelper;
import io.github.katrix_.chitchat.io.ConfigSettings;

@SuppressWarnings("unused")
public class ChannelChitChat extends AbstractMutableMessageChannel {

	private String name;
	private Text description;
	private Text prefix;

	private ChannelChitChat parent;
	private final Map<String, ChannelChitChat> children = new HashMap<>();
	@SuppressWarnings("WeakerAccess")
	protected final Set<UserChitChat> players = new HashSet<>();

	private ChannelChitChat(ChannelChitChat parent, String name, Text description, Text prefix) {
		super();
		this.parent = parent;
		this.name = name;
		this.description = description;
		this.prefix = prefix;
	}

	/*============================== Channel stuff ==============================*/

	@Override
	public Optional<Text> transformMessage(@Nullable Object sender, MessageReceiver recipient, Text original, ChatType type) {
		return Optional.of(original.toBuilder()
				.insert(0, ChitChat.getConfig().getChannelTemplate().apply(ImmutableMap.of(ConfigSettings.TEMPLATE_PREFIX, prefix)).build()).build());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Text getDescription() {
		return description;
	}

	/**
	 * Returns false if it somehow failed to save to disk.
	 */
	public boolean setDescription(Text description) {
		this.description = description;
		return ChitChat.getStorage().updateChannel(this, null, description);
	}

	public Text getPrefix() {
		return prefix;
	}

	/**
	 * Returns false if it somehow failed to save to disk.
	 */
	public boolean setPrefix(Text prefix) {
		this.prefix = prefix;
		return ChitChat.getStorage().updateChannel(this, prefix, null);
	}

	public ChannelChitChat getParent() {
		return parent;
	}

	public void setParent(ChannelChitChat parent) {
		this.parent = parent;
	}

	/*============================== Nested channels stuff ==============================*/

	public DataQuery getQueryName() {
		return getParent().getQueryName().then(DataQuery.of(name)); //TODO
	}

	public void moveChannel(ChannelChitChat channel) {
		if(channel == this) {
			throw new IllegalArgumentException("A channel can't have itself as a child");
		}

		LogHelper.info("The channel " + channel.getQueryName() + " is being moved to " + getQueryName());

		channel.getParent().removeChild(channel);
		channel.setParent(this);
		children.put(channel.getName(), channel);
	}

	private void removeChild(ChannelChitChat channel) {
		children.remove(channel.getName());
	}

	public ChannelChitChat createChannel(String name, Text prefix, Text description) {
		ChannelChitChat channel = new ChannelChitChat(this, name, prefix, description);
		children.put(name, channel);
		CentralControl.INSTANCE.registerChannel(channel);
		LogHelper.info("Created channel " + name + " for " + getQueryName());
		return channel;
	}

	public Optional<ChannelChitChat> getChild(String name) {
		return Optional.ofNullable(children.get(name));
	}

	public void removeChannel(ChannelChitChat channel) {
		removeChild(channel);
		channel.moveAllToParent(Text.of(TextColors.RED, "The channel you are in is being removed"));
	}

	public Collection<ChannelChitChat> getChildren() {
		return children.values();
	}

	/*============================== Player stuff ==============================*/

	public void moveAllToParent(@Nullable Text message) {
		movePlayersToGlobal(message, user -> true);
	}

	public void moveChildToParent(ChannelChitChat channel, @Nullable Text message) {
		movePlayersToGlobal(message, user -> user.getChannel().equals(channel));
	}

	public void movePlayersToGlobal(@Nullable Text message, Predicate<UserChitChat> test) {
		Text text = message != null ? message : Text.of(TextColors.RED, "You are being moved to the parent channel");
		players.stream().filter(test).forEach(user -> {
			user.getPlayer().ifPresent(p -> p.sendMessage(text));
			user.setChannel(this);
		});
	}

	protected void addUser(UserChitChat userChitChat) {
		userChitChat.setChannel(this);
		userChitChat.getPlayer().ifPresent(player -> players.add(userChitChat));
		getParent().addUser(userChitChat);
	}

	protected void removeUser(UserChitChat player) {
		players.remove(player);
		getParent().removeUser(player);
	}

	@Override
	public boolean addMember(MessageReceiver member) {
		if(member instanceof UserChitChat) {
			addUser((UserChitChat)member);
		}
		return super.addMember(member);
	}

	@Override
	public boolean removeMember(MessageReceiver member) {
		if(member instanceof UserChitChat) {
			removeUser((UserChitChat)member);
		}
		return super.removeMember(member);
	}

	public static ChannelRoot getRoot() {
		return ChannelRoot.INSTANCE;
	}

	private static class ChannelRoot extends ChannelChitChat {

		public static final ChannelRoot INSTANCE = new ChannelRoot();

		private ChannelRoot() {
			super(null, "Root", Text.of(TextColors.GRAY, "R"), Text.of("The root channel"));
			CentralControl.INSTANCE.registerChannel(INSTANCE);
		}

		@Override
		public ChannelChitChat getParent() {
			return this;
		}

		@Override
		public void setParent(ChannelChitChat parent) {} //NO-OP

		@Override
		protected void addUser(UserChitChat userChitChat) {
			userChitChat.getPlayer().ifPresent(p -> players.add(userChitChat));
		}

		@Override
		protected void removeUser(UserChitChat player) {
			players.remove(player);
		}
	}
}
