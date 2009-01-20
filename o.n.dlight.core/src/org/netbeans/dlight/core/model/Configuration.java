package org.netbeans.dlight.core.model;

import org.netbeans.modules.dlight.indicator.api.ConfigurationData;
import java.awt.Color;
import java.util.HashMap;
import java.util.StringTokenizer;

 class Configuration {
  private final ConfigurationData data;

   Configuration(ConfigurationData data) {
    this.data = data == null ? new ConfigurationData(new HashMap<String, Object>()) : data;
  }

   Object get(String key) {
    return data.get(key);
  }

   Object getObject(String key, Object defaultValue) {
    Object result = defaultValue;
    try {
      result = get(key);
    } catch (IllegalArgumentException e) {
    }

    return result;
  }

//  public <T extends PropertyFactory> T get(String key, T defaultValue) {
//    T result = defaultValue;
//    Class clazz = defaultValue.getClass();
//
//    try {
//      Object o = get(key);
//      if (o == null) {
//        return result;
//      }
//
//      if (o.getClass().equals(clazz)) {
//        result = (T)o;
//      } else if (o instanceof String) {
//        result = (T) defaultValue.constructObject((String)o);
//      }
//    } catch (IllegalArgumentException e) {
//
//    }
//
//    return result;
//  }

  public boolean getBoolean(String key) {
    Object res = data.get(key);
    if (res instanceof Boolean) {
      return ((Boolean) res).booleanValue();
    } else if (res instanceof String) {
      return Boolean.valueOf((String) res);
    }
    throw new IllegalArgumentException(data.getFullKey(key));
  }

  public boolean getBoolean(String key, boolean defaultValue) {
    boolean result = defaultValue;
    try {
      result = getBoolean(key);
    } catch (IllegalArgumentException e) {
    }

    return result;
  }

  public int getInt(String key) {
    Object res = data.get(key);
    if (res instanceof Integer) {
      return ((Integer) res).intValue();
    } else if (res instanceof String) {
      return Integer.valueOf((String) res);
    }
    throw new IllegalArgumentException(data.getFullKey(key));
  }

  public int getInt(String key, int defaultValue) {
    int result = defaultValue;
    try {
      result = getInt(key);
    } catch (IllegalArgumentException e) {
    }

    return result;
  }

  public double getDouble(String key) {
    Object res = data.get(key);
    if (res instanceof Double) {
      return ((Double) res).doubleValue();
    } else if (res instanceof String) {
      return Double.valueOf((String) res);
    }
    throw new IllegalArgumentException(data.getFullKey(key));
  }

  public double getDouble(String key, double defaultValue) {
    double result = defaultValue;
    try {
      result = getDouble(key);
    } catch (IllegalArgumentException e) {
    }

    return result;
  }

  public double[] getDoubleArray(String key) {
    Object res = data.get(key);
    if (res instanceof double[]) {
      return (double[]) res;
    } else if (res instanceof String) {
      return parseDoubleArray((String) res);
    }
    throw new IllegalArgumentException(data.getFullKey(key));
  }

  public double[] getDoubleArray(String key, double[] defaultValue) {
    double[] result = defaultValue;
    try {
      result = getDoubleArray(key);
    } catch (IllegalArgumentException e) {
    }

    return result;
  }

  public String getString(String key, String defaultValue) {
    String result = defaultValue;
    try {
      result = getString(key);
    } catch (IllegalArgumentException e) {
    }

    return result;
  }

  public String getString(String key) {
    Object res = data.get(key);
    if (res instanceof String) {
      return (String) res;
    }
    throw new IllegalArgumentException(data.getFullKey(key));
  }

  private double[] parseDoubleArray(String value) {
    try {
      StringTokenizer tokenizer = new StringTokenizer(value);
      double[] result = new double[tokenizer.countTokens()];
      for (int i = 0; i < result.length; i++) {
        result[i] = new Double(tokenizer.nextToken()).doubleValue();
      }
      return result;
    } catch (NumberFormatException e) {
    }

    return null;
  }

  public Color getColor(String key) {
    Object res = data.get(key);
    if (res == null) {
      return Color.BLACK;
    }
    
    if (res instanceof Color) {
      return (Color) res;
    } else if (res instanceof String) {
      return Color.decode((String) res);
    }

    throw new IllegalArgumentException(data.getFullKey(key));
  }

  public Color getColor(String key, Color defaultValue) {
    Color result = defaultValue;
    try {
      result = getColor(key);
    } catch (IllegalArgumentException e) {
    }

    return result;
  }

//  public PlotVisualizerConfiguration getNode(String key) {
//    return new PlotVisualizerConfiguration(data.getNode(key));
//  }
}
