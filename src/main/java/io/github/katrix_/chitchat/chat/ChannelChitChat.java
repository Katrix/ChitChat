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

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.AbstractMutableMessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import com.google.common.collect.ImmutableMap;

import io.github.katrix_.chitchat.ChitChat;
import io.github.katrix_.chitchat.helper.LogHelper;
import io.github.katrix_.chitchat.io.ConfigSettings;
import io.github.katrix_.chitchat.lib.LibKeys;

@SuppressWarnings("unused")
@NonnullByDefault
public class ChannelChitChat extends AbstractMutableMessageChannel {

	private String name;
	private Text description;
	private Text prefix;

	private ChannelChitChat parent;
	private final Set<ChannelChitChat> children = new HashSet<>();
	@SuppressWarnings("WeakerAccess")
	protected final Set<Player> players = Collections.newSetFromMap(new WeakHashMap<>()); //This works right?

	private ChannelChitChat(ChannelChitChat parent, String name, Text description, Text prefix) {
		this.parent = parent;
		this.description = description;
		this.prefix = prefix;
		this.name = name;
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
		return getParent().getQueryName().then(DataQuery.of(name));
	}

	public void moveChannel(ChannelChitChat channel) {
		if(channel == this) {
			throw new IllegalArgumentException("A channel can't have itself as a child");
		}

		LogHelper.info("The channel " + channel.getQueryName() + " is being moved to " + getQueryName());

		channel.getParent().removeChild(channel);
		channel.setParent(this);
		children.add(channel);
	}

	private void removeChild(ChannelChitChat channel) {
		children.remove(channel);
	}

	public ChannelChitChat createChannel(String name, Text prefix, Text description) {
		ChannelChitChat channel = new ChannelChitChat(this, name, prefix, description);

		if(!children.contains(channel)) {
			children.add(channel);
			CentralControl.INSTANCE.registerChannel(channel);
			LogHelper.info("Created channel " + name + " for " + getQueryName());
			return channel;
		}
		else {
			return children.stream().filter(c -> c.getName().equals(name)).findFirst().get(); //Get is safe as I know a channel with that name exists
		}
	}

	public Optional<ChannelChitChat> getChild(String name) {
		return children.stream().filter(c -> c.getName().equals(name)).findFirst();
	}

	public void removeChannel(ChannelChitChat channel) {
		removeChild(channel);
		channel.moveAllToParent(Text.of(TextColors.RED, "The channel you are in is being removed"));
	}

	public Set<ChannelChitChat> getChildren() {
		return children;
	}

	/*============================== Player stuff ==============================*/

	public void moveAllToParent(@Nullable Text message) {
		movePlayersToGlobal(message, player -> true);
	}

	public void moveChildToParent(ChannelChitChat channel, @Nullable Text message) {
		movePlayersToGlobal(message, player -> CentralControl.INSTANCE.getChannel(player
				.get(LibKeys.USER_CHANNEL)
				.orElse(ChannelRoot.ROOT_QUERY)).orElse(getRoot()).equals(channel));
	}

	public void movePlayersToGlobal(@Nullable Text message, Predicate<Player> test) {
		Text text = message != null ? message : Text.of(TextColors.RED, "You are being moved to the parent channel");
		players.stream().filter(test).forEach(player -> {
			player.sendMessage(text);
			player.offer(LibKeys.USER_CHANNEL, this.getQueryName());
		});
	}

	@SuppressWarnings({"deprecation", "OptionalGetWithoutIsPresent"})
	public void addUser(User user) {
		user.offer(LibKeys.USER_CHANNEL, getQueryName());
		if(user.isOnline()) {
			addMember(user.getPlayer().get());
		}
	}

	@SuppressWarnings({"deprecation", "OptionalGetWithoutIsPresent"})
	public void removeUser(User user) {
		user.remove(LibKeys.USER_CHANNEL);
		if(user.isOnline()) {
			removeMember(user.getPlayer().get());
		}
	}

	protected void addPlayer(Player player) {
		players.add(player);
		getParent().addPlayer(player);
	}

	protected void removePlayer(Player player) {
		players.remove(player);
		getParent().removePlayer(player);
	}

	/**
	 * Don't use this for adding members, use {@link #addUser(User)} instead
	 */
	//TODO: Better way to handle this
	@Deprecated
	@Override
	public boolean addMember(MessageReceiver member) {
		if(member instanceof Player) {
			addPlayer((Player)member);
		}
		return super.addMember(member);
	}

	/**
	 * Don't use this for removing members, use {@link #removeUser(User)} instead.
	 */
	//TODO: Better way to handle this
	@Deprecated
	@Override
	public boolean removeMember(MessageReceiver member) {
		if(member instanceof Player) {
			removePlayer((Player)member);
		}
		return super.removeMember(member);
	}

	public boolean nameUnused(String string) {
		return children.stream().anyMatch(c -> c.getName().equals(string));
	}

	public static ChannelRoot getRoot() {
		return ChannelRoot.INSTANCE;
	}

	public static class ChannelRoot extends ChannelChitChat {

		public static final ChannelRoot INSTANCE = loadOrCreateRoot();
		public static final DataQuery ROOT_QUERY = INSTANCE.getQueryName();

		@SuppressWarnings("ConstantConditions")
		private ChannelRoot() {
			super(null, "Root", Text.of(TextColors.GRAY, "R"), Text.of("The root channel"));
			CentralControl.INSTANCE.registerChannel(INSTANCE);
		}

		private static ChannelRoot loadOrCreateRoot() {
			return ChitChat.getStorage().loadRoot().orElse(new ChannelRoot());
		}

		public static void init() {
			LogHelper.debug("Loaded root channel");
		}

		@Override
		public ChannelChitChat getParent() {
			return this;
		}

		@Override
		public void setName(String name) {} //NO-OP

		@Override
		public void setParent(ChannelChitChat parent) {} //NO-OP

		@Override
		public DataQuery getQueryName() {
			return DataQuery.of("root");
		}

		@Override
		protected void addPlayer(Player player) {
			players.add(player);
		}

		@Override
		protected void removePlayer(Player player) {
			players.remove(player);
		}
	}

	//Because equality is decided by the parent and name, if two channels with the same parent and name exists, they are considered the same channel
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(getClass() != o.getClass()) return false;

		ChannelChitChat that = (ChannelChitChat)o;

		return name.equals(that.name) && parent.equals(that.parent);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + parent.hashCode();
		return result;
	}
}
