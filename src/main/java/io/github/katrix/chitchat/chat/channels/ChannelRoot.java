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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.AbstractMutableMessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;

import com.google.common.collect.ImmutableSet;

import io.github.katrix.chitchat.ChitChat;
import io.github.katrix.chitchat.helper.LogHelper;

public class ChannelRoot extends AbstractMutableMessageChannel implements Channel {

	private String name;
	private Text prefix;
	private Text description;

	private final Set<Channel> children = new HashSet<>();

	private static ChannelRoot INSTANCE = loadOrCreateRoot();

	public ChannelRoot(String name, Text prefix, Text description) {
		this.name = name;
		this.prefix = prefix;
		this.description = description;
	}

	public ChannelRoot() {
		this("Root", Text.of("R"), Text.of("The root channel"));
	}

	public static ChannelRoot getRoot() {
		return INSTANCE;
	}

	private static ChannelRoot loadOrCreateRoot() {
		return ChitChat.getStorage().loadRootChannel().orElse(new ChannelRoot());
	}

	/**
	 * Creates a new root channel from the passed in arguments and sets it as the root channel.
	 * All nested members are moved into the new root channel.
	 * @return The newly created {@link ChannelRoot}
	 */
	public static ChannelRoot createNewRoot(String name, Text prefix, Text description) {
		List<MessageReceiver> members = INSTANCE.getMembersNested();
		ChannelRoot newRoot = new ChannelRoot(name, prefix, description);
		INSTANCE = newRoot;

		for(MessageReceiver receiver : members) {
			if(receiver instanceof User) {
				newRoot.setUser((User)receiver);
			}
			else {
				newRoot.addMember(receiver);
			}
		}

		return newRoot;
	}

	public static void init() {
		LogHelper.debug("Loaded root channel");
	}

	/*============================== Channel stuff ==============================*/

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {} //NO-OP

	@Override
	public Text getPrefix() {
		return prefix;
	}

	@Override
	public boolean setPrefix(Text prefix) {
		return false; //NO-OP
	}

	@Override
	public Text getDescription() {
		return description;
	}

	@Override
	public boolean setDescription(Text description) {
		return false; //NO-OP
	}

	/*============================== Nested channels stuff ==============================*/

	@Override
	public Optional<Channel> getParent() {
		return Optional.empty();
	}

	@Override
	public Set<Channel> getChildren() {
		return ImmutableSet.copyOf(children);
	}

	@Override
	public boolean addChild(ChannelBuilder builder, String name) {
		if(isNameUnused(name)) {
			children.add(builder.createChannel(name, this));
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean removeChild(Channel channel) {
		return children.remove(channel);
	}

	@Override
	public Optional<Channel> getChild(String name) {
		return children.stream()
				.filter(c -> c.getName().equals(name))
				.findFirst();
	}

}
