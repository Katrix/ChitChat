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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.spongepowered.api.Sponge;

import io.github.katrix.spongebt.NBTStreamTools;
import io.github.katrix.spongebt.nbt.NBTCompound;
import io.github.katrix_.chitchat.ChitChat;
import io.github.katrix_.chitchat.helper.LogHelper;

@SuppressWarnings("WeakerAccess")
public abstract class NBTBase {

	protected final Path path;
	protected NBTCompound compound = new NBTCompound();
	protected final boolean compressed;

	public NBTBase(Path path, String name, boolean compressed) throws IOException {
		this.compressed = compressed;
		this.path = Paths.get(path.toString(), "/" + name + ".nbt");
		LogHelper.debug("Created NBT file");

		File parent = this.path.getParent().toFile();
		if(!parent.exists()) {
			if(!parent.mkdirs()) {
				LogHelper.error("Something went wrong when creating the directory for the file used by" + getClass().getName());
				throw new IOException();
			}
		}

		loadOrCreateFile();
	}

	protected void save() {

		Runnable save = () -> {
			try(OutputStream stream = getOutputStream()) {
				NBTStreamTools.writeTo(stream, compound, "storage", compressed);
			}
			catch(IOException e) {
				e.printStackTrace();
			}
			LogHelper.debug("Saved NBT File: " + path.toAbsolutePath() + " for: " + getClass().getName());
		};

		Sponge.getScheduler().createTaskBuilder().async().execute(save).submit(ChitChat.getPlugin());
	}

	protected NBTCompound load() throws IOException {
		try(InputStream stream = getInputStream()) {
			return NBTStreamTools.readFrom(stream, compressed)._2;
		}
	}

	protected OutputStream getOutputStream() throws FileNotFoundException {
		return new FileOutputStream(path.toFile());
	}

	protected InputStream getInputStream() throws FileNotFoundException {
		return new FileInputStream(path.toFile());
	}

	private void loadOrCreateFile() throws IOException {
		File file = this.path.toFile();
		if(file.exists()) {
			compound = load();
		}
		else {
			save();
		}
	}
}
