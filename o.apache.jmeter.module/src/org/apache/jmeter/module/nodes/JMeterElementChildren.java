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

package org.apache.jmeter.module.nodes;

import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.jmeter.module.cookies.JMeterCookie;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Jaroslav Bachorik
 */
public class JMeterElementChildren extends Children.Keys implements Comparator<TestElement> {
  private static Collator SORTER = Collator.getInstance();
  
  private SortedSet<TestElement> allTestElements;  
  private HashTree nodeTree = null;
  
  public JMeterElementChildren(final HashTree tree) {
    nodeTree = tree;
  }
  
  public void rebind(final HashTree tree) {
    nodeTree = tree;
    allTestElements = null;
    refreshKeys(true);
  }
  
  @Override
  protected void addNotify() {
    super.addNotify();
    refreshKeys(true);
  }
  
  @Override
  protected void removeNotify() {
    super.removeNotify();
    setKeys(Collections.EMPTY_SET);
    synchronized (this) {
      allTestElements = null;
    }
  }
  
  @Override
  protected Node[] createNodes(Object key) {
    TestElement t = (TestElement) key;
    
//    return new Node[] {new JMeterElementNode(new JMeterCookie(t, cookie.getPath(), cookie.getScriptSource()))};
    return new Node[] {new JMeterElementNode(t, nodeTree.getTree(t))};
  }
  
  public int compare(TestElement t1, TestElement t2) {
    int x = SORTER.compare(t1.getProperty("TestElement.name").getStringValue(), t2.getProperty("TestElement.name").getStringValue());
    if (x != 0 || t1 == t2) {
      return x;
    } else {
      // #44491: was not displaying overridden targets.
      return System.identityHashCode(t1) - System.identityHashCode(t2);
    }
  }
  
  private void refreshKeys(boolean createKeys) {
    Collection keys = null;
    synchronized (this) {
      if (allTestElements == null && !createKeys) {
        // Aynch refresh after removeNotify; ignore. (#44428)
        return;
      }
      allTestElements = new TreeSet<TestElement>(this);
//      try {        
        for(Object elementObj : nodeTree.list()) {
          allTestElements.add((TestElement)elementObj);
        }
//        allTestElements.addAll(JMeterIntegrationEngine.getDefault().getChildren(cookie.getPath(), cookie.getScriptSource()));
        keys = allTestElements;
//      } catch (InitializationException e) {
//        e.printStackTrace();
//      }
    }
    if (keys != null) { // #65235
      setKeys(keys);
    }
  }
  
}
