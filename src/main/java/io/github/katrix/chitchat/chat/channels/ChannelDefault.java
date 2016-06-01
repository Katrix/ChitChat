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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.AbstractMutableMessageChannel;

import com.google.common.collect.ImmutableSet;

public class ChannelDefault extends AbstractMutableMessageChannel implements Channel {

	private String name;
	private Text prefix;
	private Text description;

	private Channel parent;
	private final Set<Channel> children = new HashSet<>();

	public ChannelDefault(String name, Text prefix, Text description, Channel parent) {
		this.name = name;
		this.prefix = prefix;
		this.description = description;
		this.parent = parent;
	}

	/*============================== Channel stuff ==============================*/

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
		members.stream()
				.filter(r -> r instanceof User)
				.map(r -> (User)r)
				.forEach(this::setUser);
	}

	@Override
	public Text getPrefix() {
		return prefix;
	}

	@Override
	public boolean setPrefix(Text prefix) {
		this.prefix = prefix;
		return save();
	}

	@Override
	public Text getDescription() {
		return description;
	}

	@Override
	public boolean setDescription(Text description) {
		this.description = description;
		return save();
	}

	/*============================== Nested channels stuff ==============================*/

	@Override
	public Optional<Channel> getParent() {
		return Optional.of(parent);
	}

	@Override
	public Set<Channel> getChildren() {
		return ImmutableSet.copyOf(children);
	}

	@Override
	public boolean addChild(ChannelBuilder builder, String name) {
		if(isNameUnused(name)) {
			Channel createdChannel = builder.createChannel(name, this);
			Optional<Channel> parent = createdChannel.getParent();
			if(parent.isPresent() && parent.get().equals(this)) {
				children.add(createdChannel);
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean removeChild(Channel channel) {
		return children.remove(channel);
	}

	@Override
	public Optional<Channel> getChild(String name) {
		return getByName(name).findFirst();
	}

	private Stream<Channel> getByName(String name) {
		return children.stream().filter(c -> c.getName().equals(name));
	}
}
