/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.dlight.dtrace.collector;

import org.netbeans.modules.dlight.collector.api.DataCollectorConfiguration;

/**
 * This class is to configure
 */
public final class MultipleDTDCConfiguration implements DataCollectorConfiguration{
  private static final String ID = "MultipleDtraceDataCollectorConfiguration";

  public MultipleDTDCConfiguration(){
    
  }

  public void addDTDCConfiguration(DTDCConfiguration dataCollectorConfiguration){

  }

  public void addDTSTDCConfiguration(DTSTDCConfiguration stackDataCollectorConfiguration){
    
  }

  public String getID() {
    return ID;
  }

}
