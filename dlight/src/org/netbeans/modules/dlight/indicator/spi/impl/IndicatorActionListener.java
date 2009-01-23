/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.indicator.spi.impl;

import org.netbeans.modules.dlight.indicator.spi.Indicator;
import org.netbeans.modules.dlight.indicator.api.*;

/**
 *
 * @author masha
 */
public interface IndicatorActionListener {

  void mouseClickedOnIndicator(Indicator source);
}
