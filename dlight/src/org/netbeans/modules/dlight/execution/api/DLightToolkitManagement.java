/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.dlight.execution.api;

import java.util.Collection;
import org.netbeans.modules.dlight.execution.api.impl.DLightToolkitManager;
import org.openide.util.Lookup;

/**
 *
 * @author masha
 */
public final class DLightToolkitManagement {

  private static DLightToolkitManagement instance = null;
  private static DLightToolkitManager toolkitManager;

  private DLightToolkitManagement() {
    Collection<? extends DLightToolkitManager> result = Lookup.getDefault().lookupAll(DLightToolkitManager.class);
    toolkitManager = Lookup.getDefault().lookup(DLightToolkitManager.class);
  }

  public static final DLightToolkitManagement getInstance() {
    if (instance == null) {
      instance = new DLightToolkitManagement();
    }
    return instance;
  }

  public DLightSessionReference createSession(DLightTarget target, String configurationName) {
    return toolkitManager.createSession(target, configurationName);
  }

  public DLightSessionReference createSession(DLightTarget target) {
    return toolkitManager.createSession(target);
  }

  public void startSession(DLightSessionReference reference) {
    toolkitManager.startSession(reference);
  }

  public void stopSession(DLightSessionReference reference) {
    toolkitManager.stopSession(reference);
  }
}
