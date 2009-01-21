/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.dlight.dtrace.collector.impl;

import com.sun.org.apache.xerces.internal.parsers.DTDConfiguration;
import java.util.List;
import org.netbeans.dlight.dtrace.collector.DTDCConfiguration;
import org.netbeans.dlight.dtrace.collector.support.DtraceParser;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata;

/**
 *
 * @author masha
 */
public abstract class DTDCConfigurationAccessor {

  private static volatile DTDCConfigurationAccessor DEFAULT;

  public static DTDCConfigurationAccessor getDefault() {
    DTDCConfigurationAccessor a = DEFAULT;
    if (a != null) {
      return a;
    }

    try {
      Class.forName(DTDCConfiguration.class.getName(), true, DTDConfiguration.class.getClassLoader());//
    } catch (Exception e) {
    }
    return DEFAULT;
  }

  public static void setDefault(DTDCConfigurationAccessor accessor) {
    if (DEFAULT != null) {
      throw new IllegalStateException();
    }
    DEFAULT = accessor;
  }

  public DTDCConfigurationAccessor() {
  }

  public abstract String getArgs(DTDCConfiguration conf);

  public abstract List<DataTableMetadata> getDatatableMetadata(DTDCConfiguration conf);

  public abstract DtraceParser getParser(DTDCConfiguration conf);

  public abstract List<String> getRequiredPrivileges(DTDCConfiguration conf);

  public abstract String getScriptPath(DTDCConfiguration conf);

  public abstract String getID();

  public abstract boolean isStackSupportEnabled(DTDCConfiguration conf);

  public abstract int getIndicatorFiringFactor(DTDCConfiguration conf);
}
