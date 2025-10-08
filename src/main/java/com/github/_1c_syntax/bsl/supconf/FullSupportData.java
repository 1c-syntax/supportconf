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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

/**
 * Содержимое настроек поставки конфигурации поставщика.
 * Предоставляет возможность получить варианты поддержки для каждой конфигурации поставщика
 */
@Slf4j
public final class FullSupportData {
  /**
   * Путь к файлу описания поставки
   */
  @Getter
  private final Path pathParentConfigurationBin;

  /**
   * Прочитанная информация о настройка поставки
   */
  @Getter
  private final Map<String, Map<SupportConfiguration, SupportVariant>> supportVariants;

  private FullSupportData(Path pathParentConfigurationBin) {
    this(pathParentConfigurationBin, Collections.emptyMap());
  }

  private FullSupportData(Path pathParentConfigurationBin, Map<String, Map<SupportConfiguration, SupportVariant>> supportVariants) {
    this.supportVariants = supportVariants;
    this.pathParentConfigurationBin = pathParentConfigurationBin;
  }

  /**
   * Читает данные настроек поставки по переданному файлу
   *
   * @param pathParentConfigurationBin Путь к файлу описания поставки
   * @return Прочитанные данные
   */
  public static FullSupportData create(Path pathParentConfigurationBin) {
    Map<String, Map<SupportConfiguration, SupportVariant>> supportVariants;
    try {
      supportVariants = SupportDataReader.readFull(pathParentConfigurationBin);
    } catch (NumberFormatException | FileNotFoundException exception) {
      LOGGER.error("Ошибка чтения файла {}", pathParentConfigurationBin);
      LOGGER.debug("TRACE", exception);
      supportVariants = Collections.emptyMap();
    }

    if (supportVariants.isEmpty()) {
      return new FullSupportData(pathParentConfigurationBin);
    } else {
      return new FullSupportData(pathParentConfigurationBin, supportVariants);
    }
  }

  /**
   * Возвращает варианты поддержки для указанного идентификатора объекта для всех конфигураций поставщика.
   * Если значения нет, то вернет пустую коллекцию
   *
   * @param uid Идентификатор объекта
   * @return Значение варианта поддержки
   */
  public Map<SupportConfiguration, SupportVariant> get(String uid) {
    if (uid.isBlank() || supportVariants.isEmpty()) {
      return Collections.emptyMap();
    }
    var configurations = supportVariants.get(uid);
    if (configurations == null || configurations.isEmpty()) {
      return Collections.emptyMap();
    }
    return Map.copyOf(configurations);
  }

  /**
   * Возвращает значение о варианте поддержке для указанного идентификатора объекта и конфигурации поставщика
   * Если значения нет, то вернет SupportVariant.NONE
   *
   * @param uid           Идентификатор объекта
   * @param configuration Конфигурация поставщика
   * @return Значение варианта поддержки
   */
  public SupportVariant get(String uid, SupportConfiguration configuration) {
    if (uid.isBlank() || supportVariants.isEmpty()) {
      return SupportVariant.NONE;
    }
    return supportVariants.getOrDefault(uid, Collections.emptyMap()).getOrDefault(configuration, SupportVariant.NONE);
  }
}
