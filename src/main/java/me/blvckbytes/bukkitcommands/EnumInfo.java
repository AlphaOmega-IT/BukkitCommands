package me.blvckbytes.bukkitcommands;

import java.util.*;

/**
 * Represents a class that provides information about an Enum.
 */
public class EnumInfo implements IEnumInfo {
  
  private final Class<? extends Enum<?>> enumClass;
  private final List<String> enumConstantNames;
  private final List<Enum<?>> enumConstants;
  public final Map<String, Enum<?>> enumConstantByLowerCaseName;
  
  /**
   * Constructs an EnumInfo object with the specified Enum class.
   *
   * @param enumClass The Enum class to retrieve information from.
   */
  public EnumInfo(Class<? extends Enum<?>> enumClass) {
    this.enumClass = enumClass;
    this.enumConstants = Collections.unmodifiableList(Arrays.asList(enumClass.getEnumConstants()));
    
    List<String> names = new ArrayList<>();
    Map<String, Enum<?>> table = new HashMap<>();
    
    for (Enum<?> constant : this.enumConstants) {
      String name = constant.name();
      names.add(name);
      table.put(name.toLowerCase(), constant);
    }
    
    this.enumConstantNames = Collections.unmodifiableList(names);
    this.enumConstantByLowerCaseName = Collections.unmodifiableMap(table);
  }
  
  @Override
  public Class<? extends Enum<?>> getEnumClass() {
    return enumClass;
  }
  
  @Override
  public List<Enum<?>> getEnumConstants() {
    return enumConstants;
  }
  
  @Override
  public List<String> getEnumConstantNames() {
    return enumConstantNames;
  }
}