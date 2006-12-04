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
import org.netbeans.modules.loadgenerator.actions.LoadAction;
import org.netbeans.modules.loadgenerator.actions.RemoveStoppedAction;
import org.netbeans.modules.loadgenerator.spi.Engine;
import org.netbeans.modules.loadgenerator.spi.ProcessInstance;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;

/**
 *
 * @author Jaroslav Bachorik
 */
public class EngineNode extends AbstractNode {
  private final static Action[] ACTIONARRAY = new Action[]{};
  
  private Engine provider;
  private List<Action> actions;
  private String lastDisplayName = null;
  
  private PropertyChangeListener listener = new PropertyChangeListener() {
    public void propertyChange(PropertyChangeEvent evt) {
      if (evt.getPropertyName().equals(Engine.INSTANCE)) {
//        System.out.println("LoadGeneratorNode: Received notification about a new instance being created");
        refreshChildren();
      } else if (evt.getPropertyName().equals(Engine.STATE)) {
        fireDisplayNameChange(lastDisplayName, getDisplayName());
        lastDisplayName = getDisplayName();
      }
    }
  };
  
  /**
   * Creates a new instance of EngineNode
   */
  private EngineNode(final Children children) {
    super(children);
    actions = new ArrayList<Action>();
  }
  
  public static final EngineNode getInstance() {
    return new EngineNode(new Children.Array());
  }
  
  public void setProvider(final Engine provider) {
    this.provider = provider;
    this.provider.addPropertyChangeListener(Engine.INSTANCE, WeakListeners.propertyChange(listener, provider));
    actions.add(new LoadAction(this.provider));
    actions.add(new RemoveStoppedAction(this.provider));
    refreshChildren();
  }
  
  public Engine getProvider() {
    return this.provider;
  }
  
  @Override
  public String getDisplayName() {
    return provider.getDisplayName();
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
  
  private void refreshChildren() {
    Node[] currentNodes = getChildren().getNodes();
    getChildren().remove(currentNodes);
    
    List<Node> nodes = new ArrayList<Node>();
    for(ProcessInstance instance : provider.getProcesses()) {
      ProcessNode node = ProcessNode.getInstance();
      node.setProvider(instance);
      nodes.add(node);
    }
    getChildren().add(nodes.toArray(new Node[]{}));
  }
  
  public boolean equals(Object anotherObject) {
    if (anotherObject instanceof EngineNode) return provider.equals(((EngineNode)anotherObject).provider);
    if (anotherObject instanceof Engine) return provider.equals(anotherObject);
    return false;
  }
  
  public int hashCode() {
    return provider.hashCode();
  }
}
