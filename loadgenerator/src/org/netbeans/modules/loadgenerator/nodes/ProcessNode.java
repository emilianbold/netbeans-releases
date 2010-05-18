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
