/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.execution.api.impl;

import org.netbeans.modules.dlight.execution.api.DLightSessionReference;
import org.netbeans.modules.dlight.execution.api.DLightTarget;

/**
 *
 * 
 */
public interface DLightToolkitManager {
  DLightSessionReference createSession(DLightTarget target, String configurationName);

  DLightSessionReference createSession(DLightTarget target);

  void startSession(DLightSessionReference reference);

  void stopSession(DLightSessionReference reference);
}
