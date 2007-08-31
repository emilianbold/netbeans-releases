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

package org.netbeans.modules.mobility.end2end.ui.treeview;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.event.FocusListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import javax.swing.UIManager;
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
public class MethodCheckedTreeBeanView extends BeanTreeView {
    
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
        validateTree();
        fireChange();
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
