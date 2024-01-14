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

package me.blvckbytes.bukkitcommands.config;

import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bbconfigmapper.sections.CSAlways;
import me.blvckbytes.bbconfigmapper.sections.CSIgnore;
import me.blvckbytes.bbconfigmapper.sections.IConfigSection;
import me.blvckbytes.bukkitcommands.ICommandConfigProvider;
import me.blvckbytes.bukkitcommands.IEnumInfo;
import me.blvckbytes.bukkitcommands.error.ErrorContext;
import me.blvckbytes.bukkitevaluable.BukkitEvaluable;
import me.blvckbytes.gpeee.interpreter.EvaluationEnvironmentBuilder;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public abstract class ACommandSection implements IConfigSection, ICommandConfigProvider {

  @CSIgnore
  private final String defaultCommandName;

  private String name, description, usage;

  @CSAlways
  private List<String> aliases;

  @CSAlways
  private Map<String, BukkitEvaluable> argumentUsages;

  @CSAlways
  private CommandErrorMessagesSection errorMessages;

  public ACommandSection(String defaultCommandName) {
    this.defaultCommandName = defaultCommandName;
  }

  @Override
  public @Nullable Object defaultFor(Field field) throws Exception {
    if (field.getName().equals("name"))
      return this.defaultCommandName;

    if (field.getType() == String.class)
      return "";

    return null;
  }

  @Override
  public @NotNull String getName() {
    return this.name;
  }

  @Override
  public @NotNull List<String> getAliases() {
    return this.aliases;
  }

  @Override
  public @NotNull String getDescription() {
    return this.description;
  }

  @Override
  public @NotNull String getUsage() {
    return this.usage;
  }

  @Override
  public String getMalformedDoubleMessage(ErrorContext errorContext) {
    return errorMessages.getMalformedDouble().asScalar(ScalarType.STRING, getErrorContextEnvironment(errorContext));
  }

  @Override
  public String getMalformedFloatMessage(ErrorContext errorContext) {
    return errorMessages.getMalformedFloat().asScalar(ScalarType.STRING, getErrorContextEnvironment(errorContext));
  }

  @Override
  public String getMalformedLongMessage(ErrorContext errorContext) {
    return errorMessages.getMalformedLong().asScalar(ScalarType.STRING, getErrorContextEnvironment(errorContext));
  }

  @Override
  public String getMalformedIntegerMessage(ErrorContext errorContext) {
    return errorMessages.getMalformedInteger().asScalar(ScalarType.STRING, getErrorContextEnvironment(errorContext));
  }

  @Override
  public String getMalformedUuidMessage(ErrorContext errorContext) {
    return errorMessages.getMalformedUuid().asScalar(ScalarType.STRING, getErrorContextEnvironment(errorContext));
  }

  @Override
  public String getMalformedEnumMessage(ErrorContext errorContext, IEnumInfo enumInfo) {
    return errorMessages.getMalformedEnum().asScalar(
      ScalarType.STRING,
      new EvaluationEnvironmentBuilder()
        .withStaticVariable("constant_names", enumInfo.getEnumConstantNames())
        .build(getErrorContextEnvironment(errorContext))
    );
  }

  @Override
  public String getMissingArgumentMessage(ErrorContext errorContext) {
    if (errorContext.argumentIndex == null)
      throw new IllegalStateException("Argument index cannot be null if a usage string is requested");

    int index = errorContext.argumentIndex + 1;
    BukkitEvaluable usageEvaluable = argumentUsages.get(String.valueOf(index));

    if (usageEvaluable == null)
      return "§cThere's no usage string configured for index " + index;

    return String.join("\n", usageEvaluable.asList(ScalarType.STRING, getErrorContextEnvironment(errorContext)));
  }

  @Override
  public String getNotAPlayerMessage(ErrorContext errorContext) {
    return errorMessages.getNotAPlayer().asScalar(ScalarType.STRING, getErrorContextEnvironment(errorContext));
  }

  @Override
  public String getPlayerUnknownMessage(ErrorContext errorContext) {
    return errorMessages.getPlayerUnknown().asScalar(ScalarType.STRING, getErrorContextEnvironment(errorContext));
  }

  @Override
  public String getPlayerNotOnlineMessage(ErrorContext errorContext) {
    return errorMessages.getPlayerNotOnline().asScalar(ScalarType.STRING, getErrorContextEnvironment(errorContext));
  }

  @Override
  public String getInternalErrorMessage(ErrorContext errorContext) {
    return errorMessages.getInternalError().asScalar(ScalarType.STRING, getErrorContextEnvironment(errorContext));
  }

  private IEvaluationEnvironment getErrorContextEnvironment(ErrorContext context) {
    String value = null;

    if (context.argumentIndex != null && context.argumentIndex < context.arguments.length)
      value = context.arguments[context.argumentIndex];

    return new EvaluationEnvironmentBuilder()
      .withStaticVariable("value", value)
      .withStaticVariable("alias", context.alias)
      .withStaticVariable("sender_name", context.sender.getName())
      .build();
  }
}