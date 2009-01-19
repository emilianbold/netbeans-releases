/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.indicator.api;

import java.util.HashMap;

/**
 *
 */
public class ConfigurationData {
  private final HashMap<String, Object> map;
  private final String path;
  
  public ConfigurationData(HashMap<String, Object> map, String path) {
    this.path = path;
    this.map = map == null ? new HashMap<String, Object>() : map;
  }
  
  public ConfigurationData(HashMap<String, Object> map) {
    this(map, null);
  }
  
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
    
    return get(prevPath == null ? null : prevPath.concat("/"), key);
  }

  public String getFullKey(String key) {
    return path == null ? key : path + key;    
  }
  
  public ConfigurationData getNode(String key) {
    String nodepath = getFullKey(key) + '/';
    return new ConfigurationData(map, nodepath);
  }
}
