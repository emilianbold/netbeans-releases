/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.mobility.end2end.ui.treeview;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.mobility.end2end.util.ServiceNodeManager;
import org.openide.nodes.Children;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;

/**
 * User: suchys
 * Date: Dec 12, 2003
 * Time: 3:57:41 PM
 */
public class MethodCheckedTreeBeanView extends BeanTreeView implements Runnable {
    
    private Node root;
    private MethodCheckedNodeEditor editor;
    
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    
    public static final String rootPath = "#root"; // NOI18N
    
    private final Node waitNode = new AbstractNode(Children.LEAF);
    
    public MethodCheckedTreeBeanView() {
        super();
        FocusListener[] fl = tree.getFocusListeners();
        for (int i = 0; i < fl.length; i++) {
            if (fl[i].getClass().getName().startsWith("org.openide")){  //NOI18N
                tree.removeFocusListener(fl[i]);
            }
        }
        MouseListener[] ml = tree.getMouseListeners();
        for (int i = 0; i < ml.length; i++) {
            if (ml[i].getClass().getName().startsWith("org.openide")){  //NOI18N
                tree.removeMouseListener(ml[i]);
            }
        }
    }
    
    public Node getWaitNode() {
        return waitNode;
    }
    
    public void setEditable(final boolean editable) {
        tree.setEditable(editable);
        tree.setBackground(UIManager.getDefaults().getColor(editable ?  "Tree.background" : "TextField.inactiveBackground")); //NOI18N
    }
    
    public void setRoot(final Node root) {
        this.root = root;        
        tree.setCellRenderer(new MethodCheckedNodeRenderer());
        tree.setCellEditor(editor = new MethodCheckedNodeEditor(tree));
        tree.setEditable(true);
        editor.setContentStorage(this);
        updateMixedStates(root);
    }
    
    public synchronized void updateTreeNodeStates(Node n) {
        if (root == null) return;
        if (n != null) forceState(n.getChildren(), (MultiStateCheckBox.State)n.getValue(ServiceNodeManager.NODE_SELECTION_ATTRIBUTE));
        updateMixedStates(root);
        fireChange();        
        SwingUtilities.invokeLater(this);
    }

    public void run() {
        TreePath tp = tree.getSelectionPath();
        if (tp != null) tree.setSelectionPath(null);
        //((DefaultTreeModel)tree.getModel()).reload();
        //if (tp != null) tree.scrollPathToVisible(tp);
    }
    
    private void forceState(Children ch, MultiStateCheckBox.State state) {
        for (Node n : ch.getNodes()) {
            n.setValue(ServiceNodeManager.NODE_SELECTION_ATTRIBUTE, state);
            forceState(n.getChildren(), state);
        }
    }
    
    private MultiStateCheckBox.State updateMixedStates(Node pn) {
        Children ch = pn.getChildren();
        MultiStateCheckBox.State ret = null;
        for (Node n : pn.getChildren().getNodes()) {
            if (n.getValue(ServiceNodeManager.NODE_VALIDITY_ATTRIBUTE) != Boolean.FALSE) {
                MultiStateCheckBox.State state = updateMixedStates(n);
                if (ret == null) ret = state;
                else if (state != ret) {
                    ret = MultiStateCheckBox.State.MIXED;
                }
            }
        }
        if (ret == null) ret = (MultiStateCheckBox.State)pn.getValue(ServiceNodeManager.NODE_SELECTION_ATTRIBUTE);
        if (ret == null) ret = MultiStateCheckBox.State.UNSELECTED;
        pn.setValue(ServiceNodeManager.NODE_SELECTION_ATTRIBUTE, ret);
        return ret;
    }
    
    private void fireChange() {
        final ChangeEvent e = new ChangeEvent(this);
        for ( ChangeListener cl : listeners ) {
            cl.stateChanged(e);
        }
    }
    public void addChangeListener(final ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(final ChangeListener l) {
        listeners.remove(l);
    }
}
