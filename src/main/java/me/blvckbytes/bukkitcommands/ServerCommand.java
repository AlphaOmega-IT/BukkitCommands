package me.blvckbytes.bukkitcommands;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.logging.Logger;

public abstract class ServerCommand extends BukkitCommand {

    protected ServerCommand(ICommandConfigProvider configProvider, Logger logger) {
        super(configProvider, logger);
    }

    protected abstract void onConsoleInvocation(ConsoleCommandSender sender, String alias, String[] args);

    @Override
    protected void onInvocation(CommandSender sender, String alias, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            this.onConsoleInvocation((ConsoleCommandSender) sender, alias, args);
        }
    }
}