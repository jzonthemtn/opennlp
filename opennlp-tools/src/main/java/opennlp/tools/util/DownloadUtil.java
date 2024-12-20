/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opennlp.tools.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import opennlp.tools.commons.Internal;
import opennlp.tools.util.model.BaseModel;

/**
 * This class facilitates the downloading of pretrained OpenNLP models.
 */
public class DownloadUtil {

  private static final Logger logger = LoggerFactory.getLogger(DownloadUtil.class);

  /**
   * The type of model.
   */
  public enum ModelType {
    TOKENIZER("token"),
    SENTENCE_DETECTOR("sent"),
    POS("pos-perceptron"),
    NAME_FINDER("ner"),
    CHUNKER("chunker"),
    PARSER("parser-chunking");

    private final String name;

    ModelType(String name) {
      this.name = name;
    }
  }

  private static final String BASE_URL = "https://dlcdn.apache.org/opennlp/";
  private static final String MODELS_UD_MODELS_1_1 = "models/ud-models-1.1/";

  public static final Map<String, Map<ModelType, String>> available_models;

  static {
    try {
      available_models = new DownloadParser(new URL(BASE_URL + MODELS_UD_MODELS_1_1)).getAvailableModels();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Triggers a download for the specified {@link DownloadUtil.ModelType}.
   *
   * @param language  The ISO language code of the requested model.
   * @param modelType The {@link DownloadUtil.ModelType type} of model.
   * @param type      The class of the resulting model.
   * @param <T>       The generic type which is a subclass of {@link BaseModel}.
   * @return A model instance of type {@link T}.
   * @throws IOException Thrown if IO errors occurred or the model is invalid.
   */
  public static <T extends BaseModel> T downloadModel(String language, ModelType modelType,
                                                      Class<T> type) throws IOException {

    if (available_models.containsKey(language)) {
      final String url = (available_models.get(language).get(modelType));
      if (url != null) {
        return downloadModel(new URL(url), type);
      }
    }

    throw new IOException("Invalid model.");
  }

  /**
   * Downloads a model from a {@link URL}.
   * <p>
   * The model is saved to an {@code .opennlp/} directory
   * located in the user's home directory. This directory will be created
   * if it does not already exist. If a model to be downloaded already
   * exists in that directory, the model will not be re-downloaded.
   *
   * @param url  The model's {@link URL}.
   * @param type The class of the resulting model {@link T}.
   * @param <T>  The generic type which is a subclass of {@link BaseModel}.
   * @return A model instance of type {@link T}.
   * @throws IOException Thrown if the model cannot be downloaded.
   */
  public static <T extends BaseModel> T downloadModel(URL url, Class<T> type) throws IOException {

    final Path homeDirectory = Paths.get(System.getProperty("user.home") + "/.opennlp/");
    if (!Files.isDirectory(homeDirectory)) {
      homeDirectory.toFile().mkdir();
    }

    final String filename = url.toString().substring(url.toString().lastIndexOf("/") + 1);
    final Path localFile = Paths.get(homeDirectory.toString(), filename);

    if (!Files.exists(localFile)) {
      logger.debug("Downloading model from {} to {}.", url, localFile);

      try (final InputStream in = url.openStream()) {
        Files.copy(in, localFile, StandardCopyOption.REPLACE_EXISTING);
      }

      validateModel(new URL(url + ".sha512"), localFile);


      logger.debug("Download complete.");
    }

    try {
      return type.getConstructor(Path.class).newInstance(localFile);
    } catch (Exception e) {
      throw new IOException("Could not initialize Model of type " + type.getTypeName(), e);
    }
  }

  /**
   * Validates the downloaded model.
   *
   * @param sha512          the url to get the sha512 hash
   * @param downloadedModel the model file to check
   * @throws IOException thrown if the checksum could not be computed
   */
  private static void validateModel(URL sha512, Path downloadedModel) throws IOException {
    // Download SHA512 checksum file
    String expectedChecksum;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(sha512.openStream()))) {
      expectedChecksum = reader.readLine();

      if (expectedChecksum != null) {
        expectedChecksum = expectedChecksum.split("\\s")[0].trim();
      }
    }

    // Validate SHA512 checksum
    final String actualChecksum = calculateSHA512(downloadedModel);
    if (!actualChecksum.equalsIgnoreCase(expectedChecksum)) {
      throw new IOException("SHA512 checksum validation failed. Expected: "
          + expectedChecksum + ", but got: " + actualChecksum);
    }
  }

  private static String calculateSHA512(Path file) throws IOException {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-512");
      try (InputStream fis = Files.newInputStream(file);
           DigestInputStream dis = new DigestInputStream(fis, digest)) {
        byte[] buffer = new byte[4096];
        while (dis.read(buffer) != -1) {
          // Reading the file to update the digest
        }
      }
      return byteArrayToHexString(digest.digest());
    } catch (NoSuchAlgorithmException e) {
      throw new IOException("SHA-512 algorithm not found", e);
    }
  }

  private static String byteArrayToHexString(byte[] bytes) {
    try (Formatter formatter = new Formatter()) {
      for (byte b : bytes) {
        formatter.format("%02x", b);
      }
      return formatter.toString();
    }
  }

  @Internal
  static class DownloadParser {

    private static final Pattern LINK_PATTERN = Pattern.compile("<a href=\\\"(.*?)\\\">(.*?)</a>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    private final URL indexUrl;

    DownloadParser(URL indexUrl) {
      Objects.requireNonNull(indexUrl);
      this.indexUrl = indexUrl;
    }

    Map<String, Map<ModelType, String>> getAvailableModels() {
      final Matcher matcher = LINK_PATTERN.matcher(fetchPageIndex());

      final List<String> links = new ArrayList<>();
      while (matcher.find()) {
        links.add(matcher.group(1));
      }

      return toMap(links);
    }

    private Map<String, Map<ModelType, String>> toMap(List<String> links) {
      final Map<String, Map<ModelType, String>> result = new HashMap<>();
      for (String link : links) {
        if (link.endsWith(".bin")) {
          if (link.contains("de-ud")) { // German
            addModel("de", link, result);
          } else if (link.contains("en-ud")) { // English
            addModel("en", link, result);
          } else if (link.contains("it-ud")) { // Italian
            addModel("it", link, result);
          } else if (link.contains("nl-ud")) { // Dutch
            addModel("nl", link, result);
          } else if (link.contains("fr-ud")) { // French
            addModel("fr", link, result);
          } else if (link.contains("bg-ud")) { // Bulgarian
            addModel("bg", link, result);
          } else if (link.contains("cs-ud")) { // Czech
            addModel("cs", link, result);
          } else if (link.contains("hr-ud")) { // Croatian
            addModel("hr", link, result);
          } else if (link.contains("da-ud")) { // Danish
            addModel("da", link, result);
          } else if (link.contains("es-ud")) { // Spanish
            addModel("es", link, result);
          } else if (link.contains("et-ud")) { // Estonian
            addModel("et", link, result);
          } else if (link.contains("fi-ud")) { // Finnish
            addModel("fi", link, result);
          } else if (link.contains("lv-ud")) { // Latvian
            addModel("lv", link, result);
          } else if (link.contains("no-ud")) { // Norwegian
            addModel("no", link, result);
          } else if (link.contains("pl-ud")) { // Polish
            addModel("pl", link, result);
          } else if (link.contains("pt-ud")) { // Portuguese
            addModel("pt", link, result);
          } else if (link.contains("ro-ud")) { // Romanian
            addModel("ro", link, result);
          } else if (link.contains("ru-ud")) { // Russian
            addModel("ru", link, result);
          } else if (link.contains("sr-ud")) { // Serbian
            addModel("sr", link, result);
          } else if (link.contains("sk-ud")) { // Slovak
            addModel("sk", link, result);
          } else if (link.contains("sl-ud")) { // Slovenian
            addModel("sl", link, result);
          } else if (link.contains("sv-ud")) { // Swedish
            addModel("sv", link, result);
          } else if (link.contains("uk-ud")) { // Ukrainian
            addModel("uk", link, result);
          }
        }
      }
      return result;
    }

    private void addModel(String locale, String link, Map<String, Map<ModelType, String>> result) {
      final Map<ModelType, String> models = result.getOrDefault(locale, new HashMap<>());
      final String url = (indexUrl.toString().endsWith("/") ? indexUrl : indexUrl + "/") + link;

      if (link.contains("sentence")) {
        models.put(ModelType.SENTENCE_DETECTOR, url);
      } else if (link.contains("tokens")) {
        models.put(ModelType.TOKENIZER, url);
      } else if (link.contains("pos")) {
        models.put(ModelType.POS, url);
      }

      result.putIfAbsent(locale, models);
    }

    private String fetchPageIndex() {
      final StringBuilder html = new StringBuilder();
      try (BufferedReader br = new BufferedReader(
          new InputStreamReader(indexUrl.openStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = br.readLine()) != null) {
          html.append(line);
        }
      } catch (IOException e) {
        logger.error("Could not read page index from {}", indexUrl, e);
      }

      return html.toString();
    }
  }
}
