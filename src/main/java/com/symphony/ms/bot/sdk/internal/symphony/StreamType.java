package com.symphony.ms.bot.sdk.internal.symphony;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Symphony stream types
 *
 * @author Gabriel Berberian
 */
public enum StreamType {
  ROOM, IM, MIM, UNKNOWN;

  public static StreamType value(String name) {
    switch (name.toUpperCase()) {
      case "ROOM":
        return ROOM;
      case "IM":
        return IM;
      case "MIM":
        return MIM;
      default:
        return UNKNOWN;
    }
  }

  public static List<StreamType> knownValues() {
    return Arrays.stream(values())
        .filter(streamType -> !streamType.equals(UNKNOWN))
        .collect(Collectors.toList());
  }

}
