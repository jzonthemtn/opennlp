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

package opennlp.tools.cmdline.postag;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import opennlp.tools.AbstractModelLoaderTest;
import opennlp.tools.EnabledWhenCDNAvailable;
import opennlp.tools.postag.POSModel;
import opennlp.tools.util.DownloadUtil;

@EnabledWhenCDNAvailable(hostname = "dlcdn.apache.org")
public class POSModelLoaderIT extends AbstractModelLoaderTest {

  // SUT
  private POSModelLoader loader;

  @BeforeAll
  public static void initResources() {
    List<String> langs = List.of("en", "fr", "de", "it", "nl", "bg", "cs", "da",
            "es", "et", "fi", "hr", "lv", "no", "pl", "pt", "ro", "ru", "sk", "sl", "sr", "sv", "uk");
    langs.forEach(lang -> {
      try {
        DownloadUtil.downloadModel(lang,
                DownloadUtil.ModelType.POS, POSModel.class);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });
  }

  @BeforeEach
  public void setup() {
    loader = new POSModelLoader();
  }

  @ParameterizedTest(name = "Verify \"{0}\" POS model loading")
  @ValueSource(strings = {"en-ud-ewt", "fr-ud-gsd", "de-ud-gsd", "it-ud-vit", "nl-ud-alpino",
      "bg-ud-btb", "cs-ud-pdt", "da-ud-ddt", "es-ud-gsd", "et-ud-edt", "fi-ud-tdt", "hr-ud-set",
      "lv-ud-lvtb", "no-ud-bokmaal", "pl-ud-pdb", "pt-ud-gsd", "ro-ud-rrt", "ru-ud-gsd",
      "sr-ud-set", "sk-ud-snk", "sl-ud-ssj", "sv-ud-talbanken", "uk-ud-iu"})
  public void testLoadModelByLanguage(String langModel) throws IOException {
    String modelName = "opennlp-" + langModel + "-pos-1.1-2.4.0.bin";
    POSModel model = loader.loadModel(Files.newInputStream(OPENNLP_DIR.resolve(modelName)));
    Assertions.assertNotNull(model);
    Assertions.assertTrue(model.isLoadedFromSerialized());
  }
}