/*
 * This file is part of PermissionBlock, licensed under the MIT License (MIT).
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
package io.github.katrix.chitchat.io;

import java.nio.file.Path;
import java.util.Optional;

import org.spongepowered.api.util.Tuple;

import io.github.katrix.chitchat.chat.channels.Channel;
import io.github.katrix.chitchat.chat.channels.ChannelBuilder;
import io.github.katrix.chitchat.chat.channels.impl.ChannelRoot;
import io.github.katrix.chitchat.chat.channels.ChannelTypeRegistry;
import ninja.leaping.configurate.ConfigurationNode;

public class ConfigurateStorage extends ConfigurateBase implements IPersistentStorage {

	private static final String CHANNELS = "channel";

	private static final String NAME = "name";
	private static final String IDENTIFIER = "typeIdentifier";

	private static final String ROOT = ChannelRoot.ChannelRootType.INSTANCE.getTypeIdentifier();

	public ConfigurateStorage(Path path, String name) {
		super(path, name, true);
	}

	@Override
	protected void saveData() {
		//NO-OP We save and load on demand
	}

	@Override
	public Optional<ChannelRoot> loadRootChannel() {
		ConfigurationNode channelNode = cfgRoot.getNode(CHANNELS);

		String name = channelNode.getNode(NAME).getString();
		String rootIdentifier = channelNode.getNode(IDENTIFIER).getString();

		if(name != null && rootIdentifier != null && rootIdentifier.equals(ROOT)) {
			Optional<ChannelBuilder<ChannelRoot>> rootBuilder = ChannelRoot.ChannelRootType.INSTANCE.deserializeConfigurate(channelNode);

			if(rootBuilder.isPresent()) {
				return Optional.of(rootBuilder.get().createChannel(name, null));
			}
		}

		return Optional.empty();
	}

	public static Optional<Tuple<ChannelBuilder, String>> loadChannel(ConfigurationNode node) {
		String name = node.getNode(NAME).getString();
		String identifier = node.getNode(IDENTIFIER).getString();

		if(name != null && identifier != null) {
			Optional<ChannelBuilder<Channel>> builder = ChannelTypeRegistry.INSTANCE.getType(identifier)
					.flatMap(t -> t.getConfigurateSerializer().deserializeConfigurate(node));

			return builder.map(b -> new Tuple<>(b, name));
		}

		return Optional.empty();
	}

	@Override
	public boolean saveRootChannel() {
		ConfigurationNode channelNode = cfgRoot.getNode(CHANNELS);
		ChannelRoot root = ChannelRoot.getRoot();

		channelNode.getNode(NAME).setValue(root.getName());
		channelNode.getNode(IDENTIFIER).setValue(ROOT);

		ChannelRoot.ChannelRootType.INSTANCE.serializeConfigurate(root, channelNode);
		saveFile();

		return true;
	}

	public static ConfigurationNode saveChannel(Channel channel, ConfigurationNode node) {
		ConfigurationNode childNode = node.getNode(channel.getName());
		childNode.getNode(NAME).setValue(channel.getName());
		childNode.getNode(IDENTIFIER).setValue(channel.getChannelType().getTypeIdentifier());
		channel.getChannelType().getConfigurateSerializer().serializeConfigurate(channel, childNode);

		return childNode;
	}
}
