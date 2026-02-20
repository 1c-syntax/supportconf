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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SupportConfigurationTest {

  @Test
  void compareTo() {
    var supportConf = new SupportConfiguration("Конфигурация", "Разработчик", "1.0.0.0");
    var supportConfWithQuotes = new SupportConfiguration("Конфигурация\"", "\"Разработчик", "\"1.0.0.0\"");
    var supportConfWithMoreQuotes = new SupportConfiguration("Конфигурация\"\"", "\"Разработчик", "\"1.0.0.0\"\"");

    var supportConfV2 = new SupportConfiguration("Конфигурация", "Разработчик", "2.0.0.0");
    var supportConfDev2 = new SupportConfiguration("Конфигурация", "1", "1.0.0.0");

    assertThat(supportConf)
      .isEqualTo(supportConfWithQuotes)
      .isEqualByComparingTo(supportConfWithQuotes)
      .isNotEqualByComparingTo(supportConfWithMoreQuotes)
      .isNotEqualByComparingTo(supportConfV2)
      .isNotEqualByComparingTo(supportConfDev2);
  }
}