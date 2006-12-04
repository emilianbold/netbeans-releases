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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.modules.loadgenerator.api.EngineManager;
import org.netbeans.modules.loadgenerator.spi.Engine;
import org.netbeans.modules.loadgenerator.spi.ProcessInstance;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ManagerNode extends AbstractNode implements LookupListener {
  private static final Image icon = Utilities.loadImage("org/netbeans/modules/loadgenerator/images/hammer.png");
  
  private Lookup.Result lookup;
  
  private static ManagerNode instance = null;
  
  /**
   * Creates a new instance of ManagerNode
   */
  private ManagerNode(final Children children) {
    super(children);
  }
  
  synchronized public static final ManagerNode getInstance() {
    //    EngineManager manager = Lookup.getDefault().lookup(EngineManager.class);
    //    Children children = new Children.Array();
    //    Collection<Node> nodeList = new ArrayList<Node>();
    //
    //    for(Engine provider : manager.findEngines()) {
    //      EngineNode node = EngineNode.getInstance();
    //      node.setProvider((Engine)provider);
    //      nodeList.add(node);
    ////      final String providerName = provider.getDisplayName();
    ////      nodeList.add(new AbstractNode(Children.LEAF) {
    ////        public String getName() {
    ////          return providerName;
    ////        }
    ////
    ////      });
    //    }
    //    children.add(nodeList.toArray(new Node[nodeList.size()]));
    if (instance == null) {
      ManagerChildren children = new ManagerChildren();
      children.setEngines(new ArrayList<Engine>());
      instance = new ManagerNode(children);
    }
    return instance;
  }
  
  @Override
  public String getName() {
    return NbBundle.getMessage(ManagerNode.class, "ManagerNode_ID"); // NOI18N
  }
  
  @Override
  public String getDisplayName() {
    return NbBundle.getMessage(ManagerNode.class, "ManagerNode_Title"); // NOI18N
  }
  
  @Override
  public Image getOpenedIcon(int i) {
    return icon;
  }
  
  @Override
  public Image getIcon(int i) {
    return icon;
  }
  
  public void setEngineLookup(Lookup.Result<Engine> lookup) {
    if (this.lookup != null) {
      this.lookup.removeLookupListener(this);
    }
    this.lookup = lookup;
    if (this.lookup != null) {
      this.lookup.addLookupListener(this);
    }
    refreshChildren(this.lookup);
  }
  
  public void resultChanged(LookupEvent ev) {
    Result<Engine> result = (Result<Engine>)ev.getSource();
    refreshChildren(result);
  }
  
  private synchronized void refreshChildren(Lookup.Result<Engine> result) {
    if (result == null) {
      setChildren(Children.LEAF);
    } else {
      ManagerChildren children = null;
      if (getChildren() instanceof ManagerChildren) {
        children = (ManagerChildren)getChildren();
      } else {
        children = new ManagerChildren();
        setChildren(children);
      }
      Set<Engine> removedEngines = new HashSet<Engine>();
      Set<Engine> addedEngines = new HashSet<Engine>();
      
      Collection<Node> currentNodes = new ArrayList<Node>(Arrays.asList(getChildren().getNodes()));
      Collection<Engine> currentEngines = new ArrayList<Engine>(currentNodes.size());
      for(Node node : currentNodes) {
        currentEngines.add(((EngineNode)node).getProvider());
      }
      
      Collection<? extends Lookup.Item<Engine>> newItems = result.allItems();
      Collection<Engine> newEngines = new ArrayList<Engine>(newItems.size());
      for(Lookup.Item<Engine> item : newItems) {
        newEngines.add(item.getInstance());
      }
      
      for(Engine engine : newEngines) {
        if (!currentEngines.contains(engine)) {
          currentEngines.add(engine);
        }
      }
      for (Iterator<Engine> iter = currentEngines.iterator(); iter.hasNext();) {
          Engine engine = iter.next();

          if (!newEngines.contains(engine)) {
              for (ProcessInstance process : engine.getProcesses()) {
                process.stop(true);
              }
              engine.cleanup();
              iter.remove();
          }
      }
      
      children.setEngines(currentEngines);
    }
  }
}
