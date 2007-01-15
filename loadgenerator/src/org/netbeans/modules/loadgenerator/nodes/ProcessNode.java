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

package org.netbeans.modules.loadgenerator.nodes;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.loadgenerator.actions.StartAction;
import org.netbeans.modules.loadgenerator.actions.StopAction;
import org.netbeans.modules.loadgenerator.spi.ProcessInstance;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ProcessNode extends AbstractNode {
  private ProcessInstance provider;
  private final static Action[] ACTIONARRAY = new Action[]{};
  private List<Action> actions;
  private String lastDisplayName = null;
  
  private PropertyChangeListener listener = new PropertyChangeListener() {
    public void propertyChange(PropertyChangeEvent evt) {
      fireDisplayNameChange(lastDisplayName, getDisplayName());
      lastDisplayName = getDisplayName();
    }
  };
  
  /**
   * Creates a new instance of ProcessNode
   */
  private ProcessNode() {
    super(Children.LEAF);
    actions = new ArrayList<Action>();
  }
  
  public static final ProcessNode getInstance() {
    return new ProcessNode();
  }
  
  public void setProvider(final ProcessInstance provider) {
    this.provider = provider;
    actions.add(new StartAction(this.provider));
    actions.add(new StopAction(this.provider));
    
    for(Action action : actions) {
      action.addPropertyChangeListener(WeakListeners.propertyChange(listener, this.provider));
    }
  }
  
  @Override
  public String getDisplayName() {
    return (provider.isRunning() ? NbBundle.getMessage(ProcessNode.class, "ProcessNode_Running") : NbBundle.getMessage(ProcessNode.class, "ProcessNode_Stopped")) + " : " + provider.getDisplayName(); // NOI18N
  }
  
  @Override
  public Image getIcon(int i) {
    Image retValue;
    
    retValue = provider.getIcon();
    return retValue != null ? retValue : super.getIcon(i);
  }
  
  @Override
  public Image getOpenedIcon(int i) {
    return getIcon(i);
  }
  
  @Override
  public Action[] getActions(boolean b) {
    return actions.toArray(ACTIONARRAY);
  }
  
}
