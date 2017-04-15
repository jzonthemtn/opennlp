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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Class which creates mapping between keys and a list of values.
 */
@SuppressWarnings("unchecked")
public class HashList extends HashMap<Object, Object> {

  private static final long serialVersionUID = 1;

  /**
   * Creates a new hash list.
   */
  public HashList() {
  }

  /**
   * Gets an object (list) from the hash list.
   * @param key The key of the object to retrieve.
   * @param index The index of the object to retrieve.
   * @return The corresponding object from the hash list
   * or <code>null</code> if no object for the given key
   * exists.
   */
  public Object get(Object key, int index) {
    if (get(key) != null) {
      return ((List<Object>) get(key)).get(index);
    }
    else {
      return null;
    }
  }

  /**
   * Puts a collection of objects into a hash list.
   * @param key The key of the list.
   * @param values The collection of objects to add to the list.
   * @return <code>null</code> if the size of the contained list matches
   * the size of the values to add to the list, or the list corresponding
   * to the given key.
   */
  public Object putAll(Object key, Collection<Object> values) {
    List<Object> o = (List<Object>) get(key);

    if (o == null) {
      o = new ArrayList<Object>();
      super.put(key, o);
    }

    o.addAll(values);

    if (o.size() == values.size())
      return null;
    else
      return o;
  }

  @Override
  public List<Object> put(Object key, Object value) {
    List<Object> o = (List<Object>) get(key);

    if (o == null) {
      o = new ArrayList<Object>();
      super.put(key, o);
    }

    o.add(value);

    if (o.size() == 1)
      return null;
    else
      return o;
  }

  /**
   * Removes a value from a hash list. If after removing
   * the value if the list contains no items the list's key
   * will also be removed.
   * @param key The key of the list.
   * @param value The value to remove from the list.
   * @return <code>false</code> if the list identified by
   * the key does not exist; <code>true</code> if the item
   * was removed from the list; or <code>false</code> if the
   * list did not contain the value to remove.
   */
  public boolean remove(Object key, Object value) {
    List<Object> l = (List<Object>) get(key);
    if (l == null) {
      return false;
    }
    else {
      boolean r = l.remove(value);
      if (l.size() == 0) {
        remove(key);
      }
      return r;
    }
  }
}
