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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.loadgenerator.api.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.Action;
import org.netbeans.modules.loadgenerator.spi.*;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ManagerOutputWindow {
  private String script;
  
  private Map<ProcessInstance, InputOutput> iomap = new WeakHashMap<ProcessInstance, InputOutput>();
  private Action[] sharedActions;
  
  /**
   * Creates a new instance of ManagerOutputWindow
   */
  public ManagerOutputWindow(final Action[] actions) {
    sharedActions = actions;
  }
  
  public void attach(final ProcessInstance provider) {
    InputOutput io = iomap.get(provider);
    if (io == null) {
      io = IOProvider.getDefault().getIO("JMeter: " + provider.getCurrentScript(), sharedActions);
      iomap.put(provider, io);
    }
    provider.addPropertyChangeListener(ProcessInstance.FACTORY, new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getNewValue() == null) {
          InputOutput oldIo = iomap.get(provider);
          if (oldIo != null) {
            oldIo.closeInputOutput();
            iomap.remove(provider);
            provider.removePropertyChangeListener(ProcessInstance.FACTORY, this);
          }
        }
      }
    });
    
    provider.attachWriter(io.getOut());
    io.select();
  }

  public void detach(final ProcessInstance provider) {
    if (provider.isDeleted()) {
      close(provider);
    }
    provider.detachWriter();
  }
  
  public void close(final ProcessInstance instance) {
    InputOutput oldIo = iomap.get(instance);
    oldIo.closeInputOutput();
  }
}
