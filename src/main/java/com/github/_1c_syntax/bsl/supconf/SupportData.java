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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

/**
 * Содержимое настроек поставки конфигурации поставщика.
 * Варианты поддержки обобщаются для всех конфигураций поставщика
 */
@Slf4j
public final class SupportData {
  /**
   * Путь к файлу описания поставки
   */
  @Getter
  private final Path pathParentConfigurationBin;

  /**
   * Прочитанная информация о настройка поставки
   */
  @Getter
  private final Map<String, SupportVariant> supportVariants;

  private SupportData(Path pathParentConfigurationBin) {
    this(pathParentConfigurationBin, Collections.emptyMap());
  }

  private SupportData(Path pathParentConfigurationBin, Map<String, SupportVariant> supportVariants) {
    this.supportVariants = supportVariants;
    this.pathParentConfigurationBin = pathParentConfigurationBin;
  }

  /**
   * Читает данные настроек поставки по переданному файлу
   *
   * @param pathParentConfigurationBin Путь к файлу описания поставки
   * @return Прочитанные данные
   */
  public static SupportData create(Path pathParentConfigurationBin) {
    Map<String, SupportVariant> supportVariants;
    try {
      supportVariants = SupportDataReader.read(pathParentConfigurationBin);
    } catch (NumberFormatException | FileNotFoundException exception) {
      LOGGER.error("Ошибка чтения файла {}", pathParentConfigurationBin);
      LOGGER.debug("TRACE", exception);
      supportVariants = Collections.emptyMap();
    }

    if (supportVariants.isEmpty()) {
      return new SupportData(pathParentConfigurationBin);
    } else {
      return new SupportData(pathParentConfigurationBin, supportVariants);
    }
  }

  /**
   * Возвращает значение о варианте поддержке для указанного идентификатора объекта.
   * Если значения нет, то вернет SupportVariant.NONE
   *
   * @param uid Идентификатор объекта
   * @return Значение варианта поддержки
   */
  public SupportVariant get(String uid) {
    if (uid.isBlank() || supportVariants.isEmpty()) {
      return SupportVariant.NONE;
    }
    return supportVariants.getOrDefault(uid, SupportVariant.NONE);
  }
}
