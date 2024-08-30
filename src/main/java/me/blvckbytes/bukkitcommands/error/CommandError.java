package me.blvckbytes.bukkitcommands.error;

import org.jetbrains.annotations.Nullable;

/**
 * Represents an error that can occur during command execution.
 */
public class CommandError extends RuntimeException {
  
  public final @Nullable Integer argumentIndex;
  public final EErrorType errorType;
  public final Object parameter;
  
  /**
   * Constructs a CommandError with the given argument index and error type.
   * @param argumentIndex The index of the argument that caused the error.
   * @param errorType The type of error that occurred.
   */
  public CommandError(@Nullable Integer argumentIndex, EErrorType errorType) {
    this(argumentIndex, errorType, null);
  }
  
  /**
   * Constructs a CommandError with the given argument index, error type, and parameter.
   * @param argumentIndex The index of the argument that caused the error.
   * @param errorType The type of error that occurred.
   * @param parameter The parameter associated with the error.
   */
  public CommandError(@Nullable Integer argumentIndex, EErrorType errorType, Object parameter) {
    this.argumentIndex = argumentIndex;
    this.errorType = errorType;
    this.parameter = parameter;
  }
}