/*
 * This file is a part of Support Configuration.
 *
 * Copyright (c) 2019 - 2025
 * Tymko Oleg <olegtymko@yandex.ru>, Maximov Valery <maximovvalery@gmail.com> and contributors
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * Support Configuration is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * Support Configuration is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Support Configuration.
 */
package com.github._1c_syntax.bsl.supconf;

import com.github._1c_syntax.bsl.support.SupportVariant;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Служебный класс для чтения файла описания настроек поддержки
 */
@Slf4j
@UtilityClass
public class SupportDataReader {
  private static final Pattern PATTERN_SPLIT =
    Pattern.compile("(?:,[\\n\\r]*|\\n|^)(\"(?:(?:\"\")*[^\"]*)*\"|[^\",\\n]*|(?:\\n|$))");

  private static final int POINT_COUNT_CONFIGURATION = 2;
  private static final int SHIFT_CONFIGURATION_VERSION = 3;
  private static final int SHIFT_CONFIGURATION_PRODUCER = 4;
  private static final int SHIFT_CONFIGURATION_NAME = 5;
  private static final int SHIFT_CONFIGURATION_COUNT_OBJECT = 6;
  private static final int SHIFT_OBJECT_COUNT = 7;
  private static final int COUNT_ELEMENT_OBJECT = 4;
  private static final int CONFIGURATION_SUPPORT = 1;
  private static final int START_READ_POSITION = 3;
  private static final int SHIFT_SIZE = 2;

  public static Map<String, SupportVariant> read(Path pathParentConfigurationBin) throws FileNotFoundException {
    Map<String, SupportVariant> supportVariants = new HashMap<>();
    var dataStrings = readDataStrings(pathParentConfigurationBin);

    var countConfiguration = Integer.parseInt(dataStrings[POINT_COUNT_CONFIGURATION]);
    LOGGER.debug("Configurations count: {}", countConfiguration);

    var startPoint = START_READ_POSITION;
    for (var numberConfiguration = 1; numberConfiguration <= countConfiguration; numberConfiguration++) {
      var countObjectsConfiguration = Integer.parseInt(dataStrings[startPoint + SHIFT_CONFIGURATION_COUNT_OBJECT]);
      var configurationSupportVariant =
        GeneralSupportVariant.valueOf(Integer.parseInt(dataStrings[CONFIGURATION_SUPPORT]));
      printInfo(dataStrings, startPoint);

      var startObjectPoint = startPoint + SHIFT_OBJECT_COUNT;
      for (var numberObject = 0; numberObject < countObjectsConfiguration; numberObject++) {
        var currentObjectPoint = startObjectPoint + numberObject * COUNT_ELEMENT_OBJECT;
        var guidObject = dataStrings[currentObjectPoint + SHIFT_SIZE];
        // 0 - не редактируется, 1 - с сохранением поддержки, 2 - снято
        var supportVariant = computeSupportVariant(
          configurationSupportVariant,
          Integer.parseInt(dataStrings[currentObjectPoint]));

        supportVariants.compute(guidObject, (k, existingValue) -> {
          if (existingValue == null) {
            // Ключа нет - вставляем новое значение
            return supportVariant;
          } else {
            // Ключ есть - выбираем максимальное значение
            return SupportVariant.max(existingValue, supportVariant);
          }
        });
      }

      startPoint = startObjectPoint + SHIFT_SIZE + countObjectsConfiguration * COUNT_ELEMENT_OBJECT;
    }

    return Collections.unmodifiableMap(supportVariants);
  }

  public static Map<String, Map<SupportConfiguration, SupportVariant>> readFull(Path pathParentConfigurationBin) throws FileNotFoundException {
    Map<String, Map<SupportConfiguration, SupportVariant>> supportVariants = new HashMap<>();
    var dataStrings = readDataStrings(pathParentConfigurationBin);

    var countConfiguration = Integer.parseInt(dataStrings[POINT_COUNT_CONFIGURATION]);
    LOGGER.debug("Configurations count: {}", countConfiguration);


    var startPoint = START_READ_POSITION;
    for (var numberConfiguration = 1; numberConfiguration <= countConfiguration; numberConfiguration++) {
      var configurationVersion = dataStrings[startPoint + SHIFT_CONFIGURATION_VERSION];
      var configurationProducer = dataStrings[startPoint + SHIFT_CONFIGURATION_PRODUCER];
      var configurationName = dataStrings[startPoint + SHIFT_CONFIGURATION_NAME];
      var countObjectsConfiguration = Integer.parseInt(dataStrings[startPoint + SHIFT_CONFIGURATION_COUNT_OBJECT]);
      var configurationSupportVariant =
        GeneralSupportVariant.valueOf(Integer.parseInt(dataStrings[CONFIGURATION_SUPPORT]));

      var supportConfiguration
        = new SupportConfiguration(configurationName, configurationProducer, configurationVersion);
      printInfo(dataStrings, startPoint);

      var startObjectPoint = startPoint + SHIFT_OBJECT_COUNT;
      for (var numberObject = 0; numberObject < countObjectsConfiguration; numberObject++) {
        var currentObjectPoint = startObjectPoint + numberObject * COUNT_ELEMENT_OBJECT;
        var guidObject = dataStrings[currentObjectPoint + SHIFT_SIZE];
        // 0 - не редактируется, 1 - с сохранением поддержки, 2 - снято
        var supportVariant = computeSupportVariant(configurationSupportVariant,
          Integer.parseInt(dataStrings[currentObjectPoint]));

        supportVariants.compute(guidObject, (k, map) -> {
          if (map == null) {
            // Ключа нет - вставляем новое значение
            map = new HashMap<>();
          }
          map.putIfAbsent(supportConfiguration, supportVariant);
          return map;
        });
      }

      startPoint = startObjectPoint + SHIFT_SIZE + countObjectsConfiguration * COUNT_ELEMENT_OBJECT;
    }

    return Collections.unmodifiableMap(supportVariants);
  }

  private static SupportVariant computeSupportVariant(GeneralSupportVariant configurationSupportVariant, int dataStrings) {
    SupportVariant supportVariant;
    if (configurationSupportVariant == GeneralSupportVariant.LOCKED) {
      supportVariant = SupportVariant.NOT_EDITABLE;
    } else {
      supportVariant = SupportVariant.valueOf(dataStrings);
    }
    return supportVariant;
  }

  private static String[] readDataStrings(Path pathParentConfigurationBin) throws FileNotFoundException {
    LOGGER.debug("Reading ParentConfigurations.bin from {}", pathParentConfigurationBin);

    String[] dataStrings;
    FileInputStream fileInputStream = new FileInputStream(pathParentConfigurationBin.toFile());
    try (var scanner = new Scanner(fileInputStream, StandardCharsets.UTF_8)) {
      dataStrings = scanner.findAll(PATTERN_SPLIT)
        .map(matchResult -> matchResult.group(1))
        .toArray(String[]::new);
    }

    try {
      fileInputStream.close();
    } catch (IOException exception) {
      LOGGER.error("Error close file {}", pathParentConfigurationBin);
      LOGGER.debug("TRACE", exception);
    }

    return dataStrings;
  }

  private static void printInfo(String[] dataStrings, int startPoint) {
    var configurationVersion = dataStrings[startPoint + SHIFT_CONFIGURATION_VERSION];
    var configurationProducer = dataStrings[startPoint + SHIFT_CONFIGURATION_PRODUCER];
    var configurationName = dataStrings[startPoint + SHIFT_CONFIGURATION_NAME];
    var countObjectsConfiguration = Integer.parseInt(dataStrings[startPoint + SHIFT_CONFIGURATION_COUNT_OBJECT]);

    LOGGER.debug(
      "Configuration name: {} Version: {} Vendor: {} Object count: {}",
      configurationName,
      configurationVersion,
      configurationProducer,
      countObjectsConfiguration);
  }
}
