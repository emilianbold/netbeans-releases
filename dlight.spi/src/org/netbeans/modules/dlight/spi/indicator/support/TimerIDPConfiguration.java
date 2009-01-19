/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.spi.indicator.support;

import org.netbeans.modules.dlight.indicator.api.IndicatorDataProviderConfiguration;
import org.netbeans.modules.dlight.storage.api.DataTableMetadata.Column;

/**
 *
 * @author masha
 */
public final class TimerIDPConfiguration implements IndicatorDataProviderConfiguration {

  public static final String TIME_ID = "time";
  public static final Column TIME_INFO = new Column(TIME_ID, Long.class);
  public static final String ID = "TimerIndicatorDataProviderConfiguration_ID";

  public String getID() {
    return ID;
  }
}
