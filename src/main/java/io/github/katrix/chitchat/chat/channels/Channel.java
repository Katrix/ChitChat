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
package io.github.katrix.chitchat.chat.channels;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.text.format.TextColors;

import io.github.katrix.chitchat.ChitChat;
import io.github.katrix.chitchat.lib.LibKeys;

public interface Channel extends MutableMessageChannel {

	/*============================== Channel stuff ==============================*/

	String getName();

	/**
	 * Sets the name of this channel. The online users
	 * of this channel must also have their data updated.
	 */
	void setName(String name);

	/**
	 * A special name that includes all the names up the tree.
	 */
	default DataQuery getQueryName() {
		DataQuery thisName = DataQuery.of(getName());
		return getParent().map(p -> p.getQueryName().then(thisName)).orElse(thisName);
	}

	Text getPrefix();

	/**
	 * Sets the prefix
	 * @return If saving was successful
	 */
	boolean setPrefix(Text prefix);

	Text getDescription();

	/**
	 * Sets the description
	 * @return If saving was successful
	 */
	boolean setDescription(Text description);

	/**
	 * Saves the root.
	 * @return If the save was successful
	 */
	default boolean save() {
		return ChitChat.getStorage().saveRootChannel();
	}

	/*============================== Nested channels stuff ==============================*/

	/**
	 * Gets the parent for this channel.
	 * Might not be present if this is a root channel.
	 */
	Optional<Channel> getParent();

	/**
	 * Gets all the children for this channel.
	 * The can only be one channel in the children for each name.
	 */
	Set<Channel> getChildren();

	/**
	 * Adds a new child to this channel.
	 * The child can be any subtype of {@link Channel},
	 * but to get serialized correctly it needs to registered in the {@link ChannelTypeRegistry}
	 * @param builder The builder for the new channel.
	 * @param name The name for this channel.
	 * will be passed into the builder after passing it's own test.
	 * @return If the channel was created successfully
	 */
	boolean addChild(ChannelBuilder builder, String name);

	/**
	 * Removes a child.
	 * @return If a child was removed.
	 */
	boolean removeChild(Channel name);

	/**
	 * Gets a child with the passed in name.
	 */
	Optional<Channel> getChild(String name);

	/**
	 * Searches recursively for the passed in query name.
	 * Normally used on the root channel.
	 */
	default Optional<Channel> getChannel(DataQuery thatQueryName) {
		List<String> thatNameParts = thatQueryName.getParts();
		List<String> thisNameParts = getQueryName().getParts();

		//If the depth of this channel is bigger than the depth of the other channel, then the other channel can't be a parent for this
		if(thisNameParts.size() > thatNameParts.size()) {
			return Optional.empty();
		}

		if(thisNameParts.size() == thatNameParts.size()) {
			if(thisNameParts.equals(thatNameParts)) {
				return Optional.of(this);
			}
		}

		for(int i = 0; i <= thatNameParts.size(); i++) {
			String thisPart;
			String thatPart = thatNameParts.get(i);

			try {
				thisPart = thisNameParts.get(i);
			}
			catch(IndexOutOfBoundsException e) {
				thisPart = null;
			}

			if(thisPart == null || !thisPart.equals(thatPart)) {
				if(i == 0) {
					//Should not ever happen. Just for safety.
					return Optional.empty();
				}
				else {
					return getChildren().stream()
							.filter(c -> c.getName().equals(thatPart))
							.findFirst()
							.flatMap(c -> c.getChannel(thatQueryName));
				}
			}
		}

		return Optional.empty();
	}

	/**
	 * Checks if s specific name is used for any of the children.
	 */
	default boolean isNameUnused(String name) {
		return getChildren().stream().noneMatch(c -> c.getName().equals(name));
	}

	/*============================== Player stuff ==============================*/

	/**
	 * Moves all members, and subMembers that pass the predicate into the specified channel
	 * @param message The message to send to the members.
	 * If null, a default message will be used.
	 * @param filter A filter to who will be moved
	 * @param destination The channel to move to
	 */
	default void moveAllToChannel(@Nullable Text message, Predicate<MessageReceiver> filter, Channel destination) {
		Text text = message != null ? message : Text.of(TextColors.RED, "You are being moved to the channel" + destination.getQueryName());
		getMembersNested().stream()
				.filter(filter)
				.forEach(r -> {
					r.sendMessage(text);

					if(r instanceof User) {
						destination.setUser((User)r);
					}
					else {
						destination.addMember(r);
					}
				});
	}

	/**
	 * Gets all the members of this channel, and all the child channels recursively
	 */
	default List<MessageReceiver> getMembersNested() {
		List<MessageReceiver> ret = getChildren().stream()
				.flatMap(c -> c.getMembersNested().stream())
				.collect(Collectors.toList());
		ret.addAll(getMembers());

		return ret;
	}

	/**
	 * Sets the channel data of a {@link User}
	 * @return The {@link DataTransactionResult}
	 */
	static DataTransactionResult setUserData(User user, Channel channel) {
		return user.offer(LibKeys.USER_CHANNEL, channel.getQueryName());
	}

	/**
	 * Add an user to this channel and adds the data to the user.
	 * Prefer this over {@link #addMember(MessageReceiver)}when adding an user.
	 */
	default void setUser(User user) {
		setUserData(user, this);

		if(user instanceof MessageReceiver) {
			MessageReceiver receiver = (MessageReceiver)user;
			MessageChannel oldChannel = receiver.getMessageChannel();

			if(oldChannel instanceof MutableMessageChannel) {
				((MutableMessageChannel)oldChannel).removeMember(receiver);
			}

			addMember(receiver);
		}
	}
}
