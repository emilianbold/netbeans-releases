/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.dlight.dtrace.collector.impl;

import java.util.List;
import org.netbeans.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.dlight.dtrace.collector.DTSTDCConfiguration;
import org.netbeans.dlight.dtrace.collector.MultipleDTDCConfiguration;

/**
 *
 * @author masha
 */
public abstract class MultipleDTDCConfigurationAccessor {
private static volatile MultipleDTDCConfigurationAccessor DEFAULT;

  public static MultipleDTDCConfigurationAccessor getDefault() {
    MultipleDTDCConfigurationAccessor a = DEFAULT;
    if (a != null) {
      return a;
    }

    try {
      Class.forName(MultipleDTDCConfiguration.class.getName(), true, MultipleDTDCConfiguration.class.getClassLoader());//
    } catch (Exception e) {
    }
    return DEFAULT;
  }

  public static void setDefault(MultipleDTDCConfigurationAccessor accessor) {
    if (DEFAULT != null) {
      throw new IllegalStateException();
    }
    DEFAULT = accessor;
  }

  public MultipleDTDCConfigurationAccessor() {
  }

  public abstract String getID();
  public abstract List<DTDCConfiguration> getDTDCConfigurations(MultipleDTDCConfiguration configuration);
  public abstract List<DTSTDCConfiguration> getDTSTDCConfigurations(MultipleDTDCConfiguration configuration);

}
