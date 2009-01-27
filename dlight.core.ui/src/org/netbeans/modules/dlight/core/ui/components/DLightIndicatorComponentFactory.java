/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.core.ui.components;

import org.netbeans.modules.dlight.management.ui.spi.IndicatorComponent;
import org.netbeans.modules.dlight.management.ui.spi.IndicatorComponentFactory;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mt154047
 */
@ServiceProvider(service=IndicatorComponentFactory.class)
public final class DLightIndicatorComponentFactory implements IndicatorComponentFactory{

  public IndicatorComponent get() {
    return DLightIndicatorsTopComponent.getDefault();
  }

}
