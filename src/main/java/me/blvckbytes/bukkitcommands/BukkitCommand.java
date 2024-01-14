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

import me.blvckbytes.bukkitcommands.error.*;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BukkitCommand extends Command {

  protected static final List<String> EMPTY_STRING_LIST;
  private static final Map<Class<? extends Enum<?>>, EnumInfo> enumConstantsCache;

  static {
    EMPTY_STRING_LIST = Collections.unmodifiableList(new ArrayList<>());
    enumConstantsCache = new HashMap<>();
  }

  protected final ICommandConfigProvider configProvider;
  protected final Logger logger;

  protected BukkitCommand(
		final ICommandConfigProvider configProvider,
		final Logger logger
	) {
    super(
      configProvider.getName(),
      configProvider.getDescription(),
      configProvider.getUsage(),
      configProvider.getAliases()
    );

    this.configProvider = configProvider;
    this.logger = logger;
  }

  //=========================================================================//
  //                            Abstract Handlers                            //
  //=========================================================================//

  protected abstract void onInvocation(
		final CommandSender sender,
		final String alias,
		final String[] args
	);

  protected abstract List<String> onTabComplete(
		final CommandSender sender,
		final String alias,
		final String[] args
	);

  //=========================================================================//
  //                             Bukkit Handlers                             //
  //=========================================================================//

  @Override
  public boolean execute(
		final @NotNull CommandSender sender,
		final @NotNull String alias,
		final @NotNull String[] args
	) {
    return this.executeAndHandleCommandErrors(() -> {
      onInvocation(sender, alias, args);
      return true;
    }, false, sender, alias, args);
  }

  @NotNull
  @Override
  public List<String> tabComplete(
		final @NotNull CommandSender sender,
		final @NotNull String alias,
		final @NotNull String[] args
	) throws IllegalArgumentException {
    return this.executeAndHandleCommandErrors(() -> this.onTabComplete(sender, alias, args), EMPTY_STRING_LIST, sender, alias, args);
  }

  //=========================================================================//
  //                                Utilities                                //
  //=========================================================================//

  @SuppressWarnings("unchecked")
  protected <T extends Enum<?>> T enumParameter(String[] args, int argumentIndex, Class<T> enumClass) {
    EnumInfo enumInfo = enumConstantsCache.computeIfAbsent(enumClass, EnumInfo::new);
    Object constant = enumInfo.enumConstantByLowerCaseName.get(resolveArgument(args, argumentIndex).toLowerCase());

    if (constant == null)
      throw new CommandError(argumentIndex, EErrorType.MALFORMED_ENUM, enumInfo);

    return (T) constant;
  }

  protected <T extends Enum<?>> T enumParameterOrElse(String[] args, int argumentIndex, Class<T> enumClass, T fallback) {
    return invokeIfArgPresentOrElse(() -> enumParameter(args, argumentIndex, enumClass), fallback);
  }

  protected Player playerParameter(String[] args, int argumentIndex) {
    Player player = Bukkit.getPlayer(resolveArgument(args, argumentIndex));

    if (player == null)
      throw new CommandError(argumentIndex, EErrorType.PLAYER_NOT_ONLINE);

    return player;
  }

  protected Player playerParameterOrElse(String[] args, int argumentIndex, Player fallback) {
    return invokeIfArgPresentOrElse(() -> playerParameter(args, argumentIndex), fallback);
  }

  protected OfflinePlayer offlinePlayerParameter(String[] args, int argumentIndex, boolean hasToHavePlayed) {
    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(resolveArgument(args, argumentIndex));

    if (hasToHavePlayed && !offlinePlayer.hasPlayedBefore())
      throw new CommandError(argumentIndex, EErrorType.PLAYER_UNKNOWN);

    return offlinePlayer;
  }

  protected OfflinePlayer offlinePlayerParameterOrElse(String[] args, int argumentIndex, boolean hasToHavePlayed, OfflinePlayer fallback) {
    return invokeIfArgPresentOrElse(() -> offlinePlayerParameter(args, argumentIndex, hasToHavePlayed), fallback);
  }

	protected String stringParameter(String[] args, int argumentIndex) {
		return this.resolveArgument(args, argumentIndex);
	}

  protected UUID uuidParameter(String[] args, int argumentIndex) {
    try {
      return UUID.fromString(resolveArgument(args, argumentIndex));
    } catch (IllegalArgumentException exception) {
      throw new CommandError(argumentIndex, EErrorType.MALFORMED_UUID);
    }
  }

  protected UUID uuidParameterOrElse(String[] args, int argumentIndex, UUID fallback) {
    return invokeIfArgPresentOrElse(() -> uuidParameter(args, argumentIndex), fallback);
  }

  protected Integer integerParameter(String[] args, int argumentIndex) {
    try {
      return Integer.parseInt(resolveArgument(args, argumentIndex));
    } catch (NumberFormatException exception) {
      throw new CommandError(argumentIndex, EErrorType.MALFORMED_INTEGER);
    }
  }

  protected Integer integerParameterOrElse(String[] args, int argumentIndex, Integer fallback) {
    return invokeIfArgPresentOrElse(() -> integerParameter(args, argumentIndex), fallback);
  }

  protected Long longParameter(String[] args, int argumentIndex) {
    try {
      return Long.parseLong(resolveArgument(args, argumentIndex));
    } catch (NumberFormatException exception) {
      throw new CommandError(argumentIndex, EErrorType.MALFORMED_LONG);
    }
  }

  protected Long longParameterOrElse(String[] args, int argumentIndex, Long fallback) {
    return invokeIfArgPresentOrElse(() -> longParameter(args, argumentIndex), fallback);
  }

  protected Double doubleParameter(String[] args, int argumentIndex) {
    try {
      return Double.parseDouble(resolveArgument(args, argumentIndex));
    } catch (NumberFormatException exception) {
      throw new CommandError(argumentIndex, EErrorType.MALFORMED_DOUBLE);
    }
  }

  protected Double doubleParameterOrElse(String[] args, int argumentIndex, Double fallback) {
    return invokeIfArgPresentOrElse(() -> doubleParameter(args, argumentIndex), fallback);
  }

  protected Float floatParameter(String[] args, int argumentIndex) {
    try {
      return Float.parseFloat(resolveArgument(args, argumentIndex));
    } catch (NumberFormatException exception) {
      throw new CommandError(argumentIndex, EErrorType.MALFORMED_FLOAT);
    }
  }

  protected Float floatParameterOrElse(String[] args, int argumentIndex, Float fallback) {
    return invokeIfArgPresentOrElse(() -> floatParameter(args, argumentIndex), fallback);
  }

  //=========================================================================//
  //                                Internals                                //
  //=========================================================================//

  private <T> T invokeIfArgPresentOrElse(Supplier<T> executable, T fallback) {
    try {
      return executable.get();
    } catch (CommandError error) {
      if (error.errorType == EErrorType.MISSING_ARGUMENT)
        return fallback;
      throw error;
    }
  }

  private String resolveArgument(String[] args, int argumentIndex) {
    if (argumentIndex < 0)
      throw new IllegalArgumentException("Argument indices start at zero");

    if (argumentIndex >= args.length)
      throw new CommandError(argumentIndex, EErrorType.MISSING_ARGUMENT);

    return args[argumentIndex];
  }

  private <T> T executeAndHandleCommandErrors(Supplier<T> executable, T returnValueOnError, CommandSender sender, String alias, String[] args) {
    try {
      return executable.get();
    } catch (CommandError commandError) {
      handleError(commandError, sender, alias, args);
      return returnValueOnError;
    } catch (Exception exception) {
      this.logger.log(Level.SEVERE, exception, () -> "An error occurred while executing a command");
      ErrorContext context = new ErrorContext(sender, alias, args, null);
      sender.sendMessage(configProvider.getInternalErrorMessage(context));
      return returnValueOnError;
    }
  }

  private void handleError(CommandError error, CommandSender sender, String alias, String[] args) {
    ErrorContext context = new ErrorContext(sender, alias, args, error.argumentIndex);

    String message = switch (error.errorType) {
			case MALFORMED_DOUBLE -> configProvider.getMalformedDoubleMessage(context);
			case MALFORMED_FLOAT -> configProvider.getMalformedFloatMessage(context);
			case MALFORMED_LONG -> configProvider.getMalformedLongMessage(context);
			case MALFORMED_INTEGER -> configProvider.getMalformedIntegerMessage(context);
			case MALFORMED_UUID -> configProvider.getMalformedUuidMessage(context);
			case MALFORMED_ENUM -> configProvider.getMalformedEnumMessage(
				context,
				(EnumInfo) error.parameter
			);
			case MISSING_ARGUMENT -> configProvider.getMissingArgumentMessage(context);
			case NOT_A_PLAYER -> configProvider.getNotAPlayerMessage(context);
			case PLAYER_UNKNOWN -> configProvider.getPlayerUnknownMessage(context);
			case PLAYER_NOT_ONLINE -> configProvider.getPlayerNotOnlineMessage(context);
		};

		sender.sendMessage(
			MiniMessage.miniMessage().deserialize(message).decoration(TextDecoration.ITALIC, false)
		);
  }
}