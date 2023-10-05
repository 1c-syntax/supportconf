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

import com.github._1c_syntax.bsl.support.SupportVariant;
import org.assertj.core.data.MapEntry;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ParseSupportDataTest {

  @Test
  void readSimpleEdt() {
    var path = Path.of("src/test/resources/edt/src/Configuration/ParentConfigurations.bin");
    var pathConfiguration = Path.of("src/test/resources/edt/src/Configuration/Configuration.mdo");
    var result = ParseSupportData.readSimple(path);

    assertThat(result)
      .hasSize(9)
      .contains(MapEntry.entry("3c907782-1b24-440c-b0de-1d62cebde27b", SupportVariant.NOT_EDITABLE))
      .contains(MapEntry.entry("df133230-33b2-42ac-8b8b-aae801d5007f", SupportVariant.NOT_EDITABLE))
      .contains(MapEntry.entry("50791551-3395-4b3f-94e4-c4dac0be017f", SupportVariant.EDITABLE_SUPPORT_ENABLED))
    ;

    var supportVariant = ParseSupportData.getSupportVariantByMDO(
      "50791551-3395-4b3f-94e4-c4dac0be017f", pathConfiguration);
    assertThat(supportVariant).isEqualTo(SupportVariant.EDITABLE_SUPPORT_ENABLED);
  }

  @Test
  void readSimpleDesignerFullSupport() {
    var path = Path.of("src/test/resources/designer-full-support/Ext/ParentConfigurations.bin");
    var pathConfiguration = Path.of("src/test/support/edt/src/Configuration/Configuration.mdo");
    var result = ParseSupportData.readSimple(path);

    assertThat(result)
      .hasSize(9)
      .contains(MapEntry.entry("28777e74-89cf-4993-8a0a-a5d2b9a758b9", SupportVariant.NOT_EDITABLE))
      .contains(MapEntry.entry("2b5d5d5d-3fa5-4448-a8e3-13011eb483cb", SupportVariant.NOT_EDITABLE))
      .contains(MapEntry.entry("50791551-3395-4b3f-94e4-c4dac0be017f", SupportVariant.NOT_EDITABLE))
    ;

    var supportVariant = ParseSupportData.getSupportVariantByMDO(
      "50791551-3395-4b3f-94e4-c4dac0be017f", pathConfiguration);
    assertThat(supportVariant).isEqualTo(SupportVariant.NONE);
  }

  @Test
  void readSimpleCorrectSupport() {
    var path = Path.of("src/test/resources/correct/Ext/ParentConfigurations.bin");
    var result = ParseSupportData.readSimple(path);

    assertThat(result)
      .hasSize(39784)
      .contains(MapEntry.entry("00035364-b591-4e6a-9219-e27dac18f687", SupportVariant.EDITABLE_SUPPORT_ENABLED))
    ;
  }

  @Test
  void readSimpleCorrectSupportClrf() {
    var path = Path.of("src/test/resources/correct_crlf/Ext/ParentConfigurations.bin");
    var result = ParseSupportData.readSimple(path);

    assertThat(result)
      .hasSize(39784)
      .contains(MapEntry.entry("00035364-b591-4e6a-9219-e27dac18f687", SupportVariant.EDITABLE_SUPPORT_ENABLED))
    ;

    path = Path.of("src/test/resources/correct_crlf2/Ext/ParentConfigurations.bin");
    result = ParseSupportData.readSimple(path);

    assertThat(result)
      .hasSize(109840)
      .contains(MapEntry.entry("00009f6c-9712-4a66-a48a-50b59fc617b6", SupportVariant.NOT_EDITABLE))
    ;
  }

  @Test
  void readSimpleIncorrectSupport() {
    var path = Path.of("src/test/resources/incorrect/Ext/ParentConfigurations.bin");
    var result = ParseSupportData.readSimple(path);

    assertThat(result).isEmpty();
  }

  @Test
  void readFull() {
    var path = Path.of("src/test/resources/designer-full-support/Ext/ParentConfigurations.bin");
    var result = ParseSupportData.readFull(path);

    var supportConf = new SupportConfiguration("Конфигурация", "Разработчик", "1.0.0.0");

    assertThat(result).hasSize(9);
    var supportConfList = result.values()
      .stream()
      .map(Map::keySet)
      .flatMap(Set::stream)
      .distinct()
      .collect(Collectors.toList());

    assertThat(supportConfList)
      .isNotEmpty()
      .allMatch(value -> value.compareTo(supportConf) == 0);
  }
}