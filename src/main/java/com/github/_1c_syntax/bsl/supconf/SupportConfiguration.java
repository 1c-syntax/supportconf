/*
 * This file is a part of Support Configuration.
 *
 * Copyright (c) 2019 - 2023
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

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Описание конфигурации поставщика
 */
@Value
@AllArgsConstructor
public class SupportConfiguration implements Comparable<SupportConfiguration> {
  /**
   * Название
   */
  String name;

  /**
   * Поставщик
   */
  String provider;

  /**
   * Версия
   */
  String version;

  @Override
  public int compareTo(SupportConfiguration o) {
    if(this.name.compareTo(o.name) != 0) {
      return this.name.compareTo(o.name);
    } else if (this.provider.compareTo(o.provider) != 0) {
      return this.provider.compareTo(o.provider);
    } else {
      return this.version.compareTo(o.version);
    }
  }
}
