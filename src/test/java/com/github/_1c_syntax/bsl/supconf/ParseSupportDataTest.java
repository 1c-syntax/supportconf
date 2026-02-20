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
    var result = ParseSupportData.readNoCache(path);

    assertThat(result.getSupportVariants()).hasSize(9);
    assertThat(result.get("3c907782-1b24-440c-b0de-1d62cebde27b")).isEqualTo(SupportVariant.NOT_EDITABLE);
    assertThat(result.get("df133230-33b2-42ac-8b8b-aae801d5007f")).isEqualTo(SupportVariant.NOT_EDITABLE);
    assertThat(result.get("50791551-3395-4b3f-94e4-c4dac0be017f")).isEqualTo(SupportVariant.EDITABLE_SUPPORT_ENABLED);

    // в кеше нет ничего
    var supportVariant = ParseSupportData.get(
      "50791551-3395-4b3f-94e4-c4dac0be017f", pathConfiguration);
    assertThat(supportVariant).isEqualTo(SupportVariant.NONE);

    // в кеше есть
    ParseSupportData.read(path);
    supportVariant = ParseSupportData.get(
      "50791551-3395-4b3f-94e4-c4dac0be017f", pathConfiguration);
    assertThat(supportVariant).isEqualTo(SupportVariant.EDITABLE_SUPPORT_ENABLED);
  }

  @Test
  void readSimpleDesignerFullSupport() {
    var path = Path.of("src/test/resources/designer-full-support/Ext/ParentConfigurations.bin");
    var pathConfiguration = Path.of("src/test/resources/designer-full-support/Ext/Configuration/Configuration.xml");
    var result = ParseSupportData.readNoCache(path);

    assertThat(result.getSupportVariants()).hasSize(9);
    assertThat(result.get("28777e74-89cf-4993-8a0a-a5d2b9a758b9")).isEqualTo(SupportVariant.NOT_EDITABLE);
    assertThat(result.get("2b5d5d5d-3fa5-4448-a8e3-13011eb483cb")).isEqualTo(SupportVariant.NOT_SUPPORTED);
    assertThat(result.get("50791551-3395-4b3f-94e4-c4dac0be017f")).isEqualTo(SupportVariant.EDITABLE_SUPPORT_ENABLED);

    // в кеше нет ничего
    var supportVariant = ParseSupportData.get(
      "2b5d5d5d-3fa5-4448-a8e3-13011eb483cb", pathConfiguration);
    assertThat(supportVariant).isEqualTo(SupportVariant.NONE);

    // в кеше есть
    ParseSupportData.read(path);
    supportVariant = ParseSupportData.get(
      "2b5d5d5d-3fa5-4448-a8e3-13011eb483cb", pathConfiguration);
    assertThat(supportVariant).isEqualTo(SupportVariant.NOT_SUPPORTED);
  }

  @Test
  void readSimpleCorrectSupport() {
    var path = Path.of("src/test/resources/correct/Ext/ParentConfigurations.bin");
    var result = ParseSupportData.readNoCache(path);

    assertThat(result.getSupportVariants()).hasSize(39784);
    assertThat(result.get("00035364-b591-4e6a-9219-e27dac18f687")).isEqualTo(SupportVariant.EDITABLE_SUPPORT_ENABLED);
  }

  @Test
  void readSimpleCorrectSupportClrf() {
    var path = Path.of("src/test/resources/correct_crlf/Ext/ParentConfigurations.bin");
    var result = ParseSupportData.readNoCache(path);
    assertThat(result.getSupportVariants()).hasSize(39784);
    assertThat(result.get("00035364-b591-4e6a-9219-e27dac18f687")).isEqualTo(SupportVariant.EDITABLE_SUPPORT_ENABLED);

    path = Path.of("src/test/resources/correct_crlf2/Ext/ParentConfigurations.bin");
    result = ParseSupportData.readNoCache(path);

    assertThat(result.getSupportVariants()).hasSize(109840);
    assertThat(result.get("00009f6c-9712-4a66-a48a-50b59fc617b6")).isEqualTo(SupportVariant.NOT_EDITABLE);
  }

  @Test
  void readSimpleIncorrectSupport() {
    var path = Path.of("src/test/resources/incorrect/Ext/ParentConfigurations.bin");
    var result = ParseSupportData.readNoCache(path);

    assertThat(result.getSupportVariants()).isEmpty();
  }

  @Test
  void readNotFoundFile() {
    var path = Path.of("fake/ParentConfigurations.bin");

    var result = ParseSupportData.readNoCache(path);
    assertThat(result.getSupportVariants()).isEmpty();

    var resultFull = ParseSupportData.readFull(path);
    assertThat(resultFull.getSupportVariants()).isEmpty();
  }

  @Test
  void readFull() {
    var path = Path.of("src/test/resources/designer-full-support/Ext/ParentConfigurations.bin");
    var result = ParseSupportData.readFull(path);

    var supportConf = new SupportConfiguration("Конфигурация", "Разработчик", "1.0.0.0");

    assertThat(result.getSupportVariants()).hasSize(9);
    var supportConfList = result.getSupportVariants().values()
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
