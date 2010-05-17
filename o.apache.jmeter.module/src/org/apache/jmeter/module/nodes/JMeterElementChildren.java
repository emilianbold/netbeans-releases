/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
