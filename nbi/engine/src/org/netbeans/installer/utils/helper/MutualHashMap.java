/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 * 
 * $Id$
 */
package org.netbeans.installer.utils.helper;

import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.HashMap;

/**
 * @noinspection ALL
 */
//todo: not tested yet so use arefully
public class MutualHashMap<F, S> implements MutualMap<F, S> {

  private Map<F, S> map = new HashMap<F, S>();
  private Map<S, F> reversedMap = new HashMap<S, F>();

  public int size() {
    return map.size();
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  public S get(Object key) {
    return map.get(key);
  }

  public F reversedGet(S object) {
    return reversedMap.get(object);
  }

  public S put(F key, S value) {
    reversedMap.put(value, key);
    return map.put(key, value);
  }

  public S remove(Object key) {
    final S removed = map.remove(key);
    reversedMap.remove(removed);
    return removed;
  }

  public F reversedRemove(Object value) {
    final F removed = reversedMap.remove(value);
    map.remove(removed);
    return removed;
  }

  public void putAll(Map<? extends F, ? extends S> m) {
    map.putAll(m);
    for (Map.Entry<? extends F, ? extends S> entry : m.entrySet()) {
      reversedMap.put(entry.getValue(), entry.getKey());
    }
  }

  public void clear() {
    map.clear();
    reversedMap.clear();
  }

  public Set<F> keySet() {
    return map.keySet();
  }

  public Collection<S> values() {
    return map.values();
  }

  public Set<Entry<F, S>> entrySet() {
    return map.entrySet();
  }

  public Set<Entry<S, F>> reversedEntrySet() {
    return reversedMap.entrySet();
  }
}
