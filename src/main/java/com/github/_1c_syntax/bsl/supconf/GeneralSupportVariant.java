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

public enum GeneralSupportVariant {
  LOCKED, UNLOCKED;

  /**
   * Находит элемент по приоритету
   *
   * @param priority номер приоритета
   * @return Найденное значение
   */
  public static GeneralSupportVariant valueOf(int priority) {
    if (priority == 0) {
      return UNLOCKED;
    }
    return LOCKED;
  }
}
