/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.dlight.dtrace.collector.impl;

import org.netbeans.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.dlight.dtrace.collector.DTSTDCConfiguration;

/**
 *
 * @author masha
 */
public abstract class DTSTDCConfigurationAccessor {
 private static volatile DTSTDCConfigurationAccessor DEFAULT;

  public static DTSTDCConfigurationAccessor getDefault() {
    DTSTDCConfigurationAccessor a = DEFAULT;
    if (a != null) {
      return a;
    }

    try {
      Class.forName(DTSTDCConfiguration.class.getName(), true, DTSTDCConfiguration.class.getClassLoader());//
    } catch (Exception e) {
    }
    return DEFAULT;
  }

  public static void setDefault(DTSTDCConfigurationAccessor accessor) {
    if (DEFAULT != null) {
      throw new IllegalStateException();
    }
    DEFAULT = accessor;
  }

  public DTSTDCConfigurationAccessor() {
  }

  public abstract DTDCConfiguration getDTDCConfiguration(DTSTDCConfiguration conf);
  public abstract String getID();
}
