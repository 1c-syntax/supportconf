/*
 * This file is a part of Support Configuration.
 *
 * Copyright (c) 2019 - 2026
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
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Используется для чтения информации о поддержке из файла ParentConfigurations.bin конфигурации
 */
@Slf4j
@UtilityClass
public class ParseSupportData {
  private static final Map<Path, SupportData> CACHE = new ConcurrentHashMap<>();

  /**
   * Выполняет чтение сводной информации о поддержке и помещает значение в кеше
   *
   * @param pathParentConfigurationBin Путь к файлу конфигурации поставщика
   */
  public static void read(Path pathParentConfigurationBin) {
    var rootPath = getRootConfiguration(pathParentConfigurationBin);
    CACHE.computeIfAbsent(rootPath, key -> SupportData.create(pathParentConfigurationBin));
  }

  /**
   * Выполняет чтение сводной информации о поддержке без кеширования
   *
   * @param pathParentConfigurationBin Путь к файлу конфигурации поставщика
   */
  public static SupportData readNoCache(Path pathParentConfigurationBin) {
    return SupportData.create(pathParentConfigurationBin);
  }

  /**
   * Выполняет чтение полной информации о поддержке без кеширования
   *
   * @param pathParentConfigurationBin Путь к файлу конфигурации поставщика
   */
  public static FullSupportData readFull(Path pathParentConfigurationBin) {
    return FullSupportData.create(pathParentConfigurationBin);
  }

  /**
   * Возвращает вариант поддержки для объекта с явным указанием пути, на основании которого
   * находится нужный комплект поддержки в кеше
   *
   * @param uid  Строка-идентификатор объекта, для которого определяется вариант поддержки
   * @param path Путь к файлу MDO объекта / родительского объекта
   * @return Вариант поддержки
   */
  public static SupportVariant get(String uid, Path path) {
    var supportData = CACHE.get(path);
    if (supportData == null) {
      supportData = CACHE.entrySet().stream()
        .filter(entry -> path.startsWith(entry.getKey()))
        .max(Comparator.comparingInt(entry -> entry.getKey().getNameCount()))
        .map(Map.Entry::getValue)
        .orElse(null);
    }

    if (supportData == null) {
      return SupportVariant.NONE;
    } else {
      return supportData.get(uid);
    }
  }

  private static Path getRootConfiguration(Path mdoPath) {
    return Paths.get(
      FilenameUtils.getFullPathNoEndSeparator(
        FilenameUtils.getFullPathNoEndSeparator(mdoPath.toString())));
  }
}
