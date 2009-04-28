/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.dlight.api.impl;

import java.util.HashMap;

/**
 * Represents Map to be used by {@link org.netbeans.modules.dlight.api.indicator.IndicatorConfiguration}
 */
final class ConfigurationData {
  private final HashMap<String, Object> map;
  private final String path;
  
  private ConfigurationData(HashMap<String, Object> map, String path) {
    this.path = path;
    this.map = map == null ? new HashMap<String, Object>() : map;
  }
  
  /**
   * Creates new configuration data 
   * @param map pair name-value
   */
  public ConfigurationData(HashMap<String, Object> map) {
    this(map, null);
  }

 
  
  /**
   *Returns value for the key
   * @param key key to get value for
   * @return value  if record with <code>key</code> exists, <code>null</code> otherwise
   */
  public Object get(String key) {
    return get(path, key);
  }
  
  private Object get(final String path, final String key) {
    String k = path == null ? key : path + key;
    if (map.containsKey(k)) {
      return map.get(k);
    }
    
    if (path == null || path.length() == 0) {
      return null;
    }
    
    String prevPath = path.substring(0, path.length() - 1);
    int idx = prevPath.lastIndexOf('/');
    prevPath = (idx >= 0) ? prevPath.substring(0, idx) : null;
    
    return get(prevPath == null ? null : prevPath.concat("/"), key); //NOI18N
  }

  /**
   * Returns full keey value
   * @param key
   * @return
   */
  private String getFullKey(String key) {
    return path == null ? key : path + key;    
  }
  
  /**
   * Return node
   * @param key
   * @return ConfigurationData for the <code>key</code>
   */
  public ConfigurationData getNode(String key) {
    String nodepath = getFullKey(key) + '/';
    return new ConfigurationData(map, nodepath);
  }
}
