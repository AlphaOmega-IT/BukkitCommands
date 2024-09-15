/*
 * MIT License
 *
 * Copyright (c) 2023 BlvckBytes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.blvckbytes.bukkitcommands;

import me.blvckbytes.bukkitcommands.error.CommandError;
import me.blvckbytes.bukkitcommands.error.EErrorType;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public abstract class ServerCommand extends BukkitCommand {

  protected ServerCommand(
		final ICommandConfigProvider configProvider,
		final Logger logger
	) {
    super(configProvider, logger);
  }

  protected abstract void onConsoleInvocation(
		final ConsoleCommandSender sender,
		final String alias,
		final String[] args
	);

  @Override
  protected void onInvocation(
		final CommandSender sender,
		final String alias,
		final String[] args
	) {
		if (sender instanceof ConsoleCommandSender consoleCommandSender)
			this.onConsoleInvocation(consoleCommandSender, alias, args);
		else
			throw new CommandError(null, EErrorType.NOT_A_CONSOLE);
  }
}