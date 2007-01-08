/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.loadgenerator.api.impl;

import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Action;
import org.netbeans.modules.loadgenerator.actions.LoadAction;
import org.netbeans.modules.loadgenerator.actions.StartAction;
import org.netbeans.modules.loadgenerator.actions.StopAction;
import org.netbeans.modules.loadgenerator.spi.Engine;
import org.netbeans.modules.loadgenerator.spi.ProcessInstance;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ManagerOutputWindowRegistry {
  final private static ManagerOutputWindowRegistry instance = new ManagerOutputWindowRegistry();
  
  final private Map<ProcessInstance, ManagerOutputWindow> windows = new WeakHashMap<ProcessInstance, ManagerOutputWindow>();
  
  /** Creates a new instance of ManagerOutputWindowRegistry */
  private ManagerOutputWindowRegistry() {
  }
  
  final public static ManagerOutputWindowRegistry getDefault() {
    return instance;
  }
  
  public ManagerOutputWindow open(final ProcessInstance instance) {
    // open the main management window
    ManagerOutputWindow mngrWin = windows.get(instance);
    if (mngrWin == null) {
      mngrWin = new ManagerOutputWindow(new Action[]{
        new LoadAction((Engine)instance.getFactory()),
        new StartAction((ProcessInstance)instance),
        new StopAction((ProcessInstance)instance)});
      windows.put(instance, mngrWin);
    }
    // ***
    mngrWin.attach(instance);
    return mngrWin;
  }
  
  public void close(final ProcessInstance instance) {
    ManagerOutputWindow mngrWin = windows.get(instance);
    if (mngrWin != null) {
      mngrWin.close(instance);
    }
  }
  
  public ManagerOutputWindow find(final ProcessInstance instance) {
    return windows.get(instance);
  }
}
