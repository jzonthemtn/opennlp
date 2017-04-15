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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class HashListTest {

  @Test
  public void putGetTest() {
 
    final String key = "key";
    final String value = "value";
    
    HashList hashList = new HashList();
    hashList.put(key, value);
    
    @SuppressWarnings("unchecked")
    final List<Object> values = (List<Object>) hashList.get(key);
    
    Assert.assertEquals(1, values.size());

  }
  
  @Test
  public void getTest() {
 
    final String key = "key";
    final String value = "value";
    
    HashList hashList = new HashList();
    hashList.put(key, value);
    String result = hashList.get(key, 0).toString();
    Assert.assertEquals(value, result);
   
  }
  
  @Test
  public void getInvalidKeyTest() {
 
    HashList hashList = new HashList();
    Object result = hashList.get("invalidkey", 0);
    Assert.assertNull(result);
   
  }
  
  @Test
  public void putTest() {

    HashList hashList = new HashList();
    
    List<Object> list = hashList.put("key1", "value1");    
    Assert.assertNull(list);
    
    list = hashList.put("key1", "value2");    
    Assert.assertNotNull(list);

  }
  
  @Test
  public void putAllTest() {

    HashList hashList = new HashList();
    Collection<Object> values = Arrays.asList("val1", "val2", "val3");
    
    Object result = hashList.putAll("key", values);    
    Assert.assertNull(result);
    
  }
  
  @Test
  public void removeTest() {
 
    final String key = "key";
    final String value = "value";
    
    HashList hashList = new HashList();
    hashList.put(key, value);
    
    boolean result = hashList.remove(key, value);
    Assert.assertTrue(result);
    
    result = hashList.remove("nonexistentkey", value);
    Assert.assertFalse(result);
    
  }
  
}
