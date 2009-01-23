/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.execution.api.impl;

import org.netbeans.modules.dlight.execution.api.DLightTarget;
import org.netbeans.modules.dlight.execution.api.DLightToolkitManagement.DLightSessionHandler;

/**
 *
 * 
 */
public interface DLightToolkitManager {
  DLightSessionHandler createSession(DLightTarget target, String configurationName);

  DLightSessionHandler createSession(DLightTarget target);

  void startSession(DLightSessionHandler reference);

  void stopSession(DLightSessionHandler reference);
}
