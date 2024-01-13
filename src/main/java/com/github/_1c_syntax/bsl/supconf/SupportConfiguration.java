/*
 * This file is a part of Support Configuration.
 *
 * Copyright (c) 2019 - 2024
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

/**
 * Описание конфигурации поставщика
 *
 * @param name     Название
 * @param provider Поставщик
 * @param version  Версия
 */
public record SupportConfiguration(String name, String provider,
                                   String version) implements Comparable<SupportConfiguration> {
  public SupportConfiguration(String name, String provider, String version) {
    this.name = stripQuotes(name);
    this.provider = stripQuotes(provider);
    this.version = stripQuotes(version);
  }

  @Override
  public int compareTo(SupportConfiguration o) {
    if (this.name.compareTo(o.name) != 0) {
      return this.name.compareTo(o.name);
    } else if (this.provider.compareTo(o.provider) != 0) {
      return this.provider.compareTo(o.provider);
    } else {
      return this.version.compareTo(o.version);
    }
  }

  private static String stripQuotes(String value) {
    if (value == null || value.length() < 2) {
      return value;
    }

    var result = value;
    if (value.charAt(0) == '\"') {
      result = value.substring(1);
    }

    if (result.charAt(result.length() - 1) == '\"') {
      result = result.substring(0, result.length() - 1);
    }
    return result;
  }
}
