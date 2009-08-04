/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.dlight.spi.impl;

import org.netbeans.modules.dlight.api.visualizer.VisualizerConfiguration;
import org.netbeans.modules.dlight.spi.indicator.Indicator;


/**
 *
 * @author masha
 */
public interface IndicatorActionListener {

  void mouseClickedOnIndicator(Indicator source);
  void openVisualizerForIndicator(Indicator source, VisualizerConfiguration vc);
}
