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

package org.netbeans.modules.loadgenerator.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import org.netbeans.modules.loadgenerator.spi.Engine;
import org.netbeans.modules.loadgenerator.spi.ProcessInstance;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jaroslav Bachorik
 */
public class RemoveStoppedAction extends AbstractAction {
  private static final String ICON = "org/netbeans/modules/j2ee/deployment/impl/ui/resources/start.png"; // NOI18N
  private static final String PROP_ENABLED = "enabled"; // NOI18N
  
  private Engine engine = null;
  
  private PropertyChangeListener listener = new PropertyChangeListener() {
    public void propertyChange(PropertyChangeEvent evt) {
      // revert the boolean - true = disabled, false = enabled
      firePropertyChange(PROP_ENABLED, !((Boolean)evt.getOldValue()).booleanValue(), !((Boolean)evt.getNewValue()).booleanValue());
    }
  };
  
  /** Creates a new instance of RemoveStoppedAction */
  public RemoveStoppedAction(final Engine engine) {
    super("Remove All Stopped", new ImageIcon(Utilities.loadImage(ICON)));
    this.engine = engine;
    this.engine.addPropertyChangeListener(ProcessInstance.STATE, WeakListeners.propertyChange(listener, engine));
  }

  public void actionPerformed(ActionEvent e) {
    engine.cleanup();
  }
  
  public boolean isEnabled() {
    return engine.canCleanup();
  }
}
