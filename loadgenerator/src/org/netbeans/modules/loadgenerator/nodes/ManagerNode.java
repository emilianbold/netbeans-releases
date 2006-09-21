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
import java.util.Collection;
import org.netbeans.modules.loadgenerator.api.EngineManager;
import org.netbeans.modules.loadgenerator.spi.Engine;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ManagerNode extends AbstractNode {
  private static final Image icon = Utilities.loadImage("org/netbeans/modules/loadgenerator/images/hammer.png");
  
  /**
   * Creates a new instance of ManagerNode
   */
  public ManagerNode(final Children children) {
    super(children);
  }

  public static final ManagerNode getInstance() {
    EngineManager manager = Lookup.getDefault().lookup(EngineManager.class);
    Children children = new Children.Array();
    Collection<Node> nodeList = new ArrayList<Node>();
    
    for(Engine provider : manager.findEngines()) {
      EngineNode node = EngineNode.getInstance();
      node.setProvider((Engine)provider);
      nodeList.add(node);
//      final String providerName = provider.getDisplayName();
//      nodeList.add(new AbstractNode(Children.LEAF) {
//        public String getName() {
//          return providerName;
//        }
//        
//      });
    }
    children.add(nodeList.toArray(new Node[]{}));
    return new ManagerNode(children);
  }
  
  @Override
  public String getName() {
    return "LoadGenerators";
  }
  
  @Override
  public String getDisplayName() {
    return "Load Generators";
  }
  
  @Override
  public Image getOpenedIcon(int i) {
    return icon;
  }

  @Override
  public Image getIcon(int i) {
    return icon;
  }
  
}
