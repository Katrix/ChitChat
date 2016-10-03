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
package net.katsstuff.old.chitchat.helper;

import org.slf4j.Logger;

import net.katsstuff.old.chitchat.ChitChat;

@SuppressWarnings("unused")
public class LogHelper {

	private static Logger getRawLogger() {
		return ChitChat.getPlugin().getLog();
	}

	public static void error(Object object) {
		getRawLogger().error(String.valueOf(object));
	}

	public static void warn(Object object) {
		getRawLogger().warn(String.valueOf(object));
	}

	public static void info(Object object) {
		getRawLogger().info(String.valueOf(object));
	}

	public static void debug(Object object) {
		if(ChitChat.getConfig().getDebug()) {
			getRawLogger().debug(String.valueOf(object));
		}
	}

	public static void trace(Object object) {
		getRawLogger().trace(String.valueOf(object));
	}
}
