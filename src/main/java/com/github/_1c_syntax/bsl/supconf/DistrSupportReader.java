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
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс для чтения файла описания настроек поддержки в формате EDT (XML).
 */
@Slf4j
@UtilityClass
public class DistrSupportReader {

  /**
   * Читает данные настроек поставки из XML-файла.
   */
  public static Map<String, SupportVariant> read(Path pathDistFile) throws FileNotFoundException {
    var handler = new DistrSupportHandler();
    executeParsing(pathDistFile, handler);
    return Collections.unmodifiableMap(handler.supportVariants);
  }

  /**
   * Читает полные данные настроек поставки из XML-файла.
   */
  public static Map<String, Map<SupportConfiguration, SupportVariant>> readFull(Path pathDistFile)
    throws FileNotFoundException {

    var handler = new DistrSupportHandler();
    executeParsing(pathDistFile, handler);
    return Collections.unmodifiableMap(handler.fullVariants);
  }

  private static void executeParsing(Path pathDistFile, DistrSupportHandler handler) {
    LOGGER.debug("Reading Configuration.distr from {}", pathDistFile);

    try (InputStream is = new FileInputStream(pathDistFile.toFile())) {
      var factory = SAXParserFactory.newInstance();
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setNamespaceAware(true);
      setFeatureSilently(factory, "http://apache.org/xml/features/disallow-doctype-decl", true);
      setFeatureSilently(factory, "http://xml.org/sax/features/external-general-entities", false);
      setFeatureSilently(factory, "http://xml.org/sax/features/external-parameter-entities", false);
      setFeatureSilently(factory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      var saxParser = factory.newSAXParser();
      saxParser.parse(new InputSource(is), handler);
    } catch (SAXException | IOException | IllegalArgumentException | ParserConfigurationException e) {
      LOGGER.error("Ошибка чтения XML-файла {}", pathDistFile);
      LOGGER.debug("TRACE", e);
      handler.supportVariants.clear();
      handler.fullVariants.clear();
    }
  }

  private static void setFeatureSilently(SAXParserFactory factory, String name, boolean value) {
    try {
      factory.setFeature(name, value);
    } catch (ParserConfigurationException | SAXException ignored) {
      // Some parsers don't support non-standard features;
      // FEATURE_SECURE_PROCESSING provides the core protection.
    }
  }

  private static class DistrSupportHandler extends DefaultHandler {

    private final Map<String, SupportVariant> supportVariants = new HashMap<>();
    private final Map<String, Map<SupportConfiguration, SupportVariant>> fullVariants = new HashMap<>();

    private String configName = "";
    private String configProvider = "";
    private String configVersion = "";

    private String userId = "";
    private boolean used;
    private boolean inItems;
    private boolean inParentConfigurationInfos;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
      if ("parentConfigurationInfos".equals(localName)) {
        inParentConfigurationInfos = true;
        configName = attributes.getValue("configName");
        configProvider = attributes.getValue("providerName");
        configVersion = attributes.getValue("configRelease");
      }

      if (inParentConfigurationInfos && "items".equals(localName)) {
        inItems = true;
        userId = attributes.getValue("userId");
        var usedAttr = attributes.getValue("used");
        used = "true".equalsIgnoreCase(usedAttr);
      }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
      if ("parentConfigurationInfos".equals(localName)) {
        inParentConfigurationInfos = false;
      }

      if ("items".equals(localName)) {
        if (inItems && !userId.isEmpty()) {
          var variant = computeVariant();
          supportVariants.compute(userId, (String key, SupportVariant existing) -> {
            if (existing == null) {
              return variant;
            }
            return SupportVariant.max(existing, variant);
          });

          fullVariants.compute(userId, (String key, Map<SupportConfiguration, SupportVariant> map) -> {
            if (map == null) {
              map = new HashMap<>();
            }
            SupportConfiguration config = new SupportConfiguration(configName, configProvider, configVersion);
            map.putIfAbsent(config, variant);
            return map;
          });
        }
        inItems = false;
      }
    }

    private SupportVariant computeVariant() {
      if (!used) {
        return SupportVariant.NOT_SUPPORTED;
      }
      return SupportVariant.NOT_EDITABLE;
    }
  }
}
