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

import com.typesafe.config.ConfigRenderOptions;

import io.github.katrix_.chitchat.helper.LogHelper;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public abstract class ConfigurateBase {

	protected final Path path;
	protected final ConfigurationLoader<CommentedConfigurationNode> cfgLoader;
	protected CommentedConfigurationNode cfgRoot;

	public ConfigurateBase(Path path, String name, boolean data) {
		String ending = data ? ".json" : ".conf";
		this.path = Paths.get(path.toString(), "/" + name + ending);

		File parent = this.path.getParent().toFile();
		if(!parent.exists()) {
			if(!parent.mkdirs()) {
				LogHelper.error("Something went wrong when creating the directory for the file used by" + getClass().getName());
			}
		}

		HoconConfigurationLoader.Builder builder = HoconConfigurationLoader.builder().setPath(this.path);
		if(data) {
			builder.setRenderOptions(ConfigRenderOptions.concise());
		}
		cfgLoader = builder.build();
		cfgRoot = loadRoot();
		loadData();
		saveFile();
	}

	protected void loadData() {
		saveData();
	}

	protected abstract void saveData();

	protected CommentedConfigurationNode loadRoot() {
		LogHelper.info("Loading config");
		CommentedConfigurationNode root;
		try {
			root = cfgLoader.load();
		}
		catch(IOException e) {
			root = cfgLoader.createEmptyNode(ConfigurationOptions.defaults());
		}

		return root;
	}

	protected void saveFile() {
		try {
			cfgLoader.save(cfgRoot);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
}
