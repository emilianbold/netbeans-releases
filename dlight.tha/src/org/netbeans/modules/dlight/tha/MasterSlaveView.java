/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.dlight.tha;

import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author Alexey Vladykin
 */
public final class MasterSlaveView<T, F extends THANodeFactory<T>> extends JSplitPane implements ExplorerManager.Provider {

    private final BeanTreeView master;
    private Component slave;
    private SlaveRenderer slaveRenderer;
    private final ExplorerManager manager = new ExplorerManager();
    private final RootNode rootNode = new RootNode();
    private final F nodeFactory;
    private static final String SWITCH_TO_LEFT = "switchToLeftComponent"; // NOI18N
    private static final String SWITCH_TO_RIGHT = "switchToRightComponent"; // NOI18N

    public MasterSlaveView(F factory) {
        this(factory, Collections.<T>emptyList(), null);
    }

    public MasterSlaveView(F factory, List<? extends T> data, SlaveRenderer slaveRenderer) {
        super(HORIZONTAL_SPLIT);
        this.master = new BeanTreeView();
        master.setRootVisible(false);
        this.slaveRenderer = slaveRenderer;
        this.nodeFactory = factory;
        setResizeWeight(0.5);
        setLeftComponent(master);
        manager.setRootContext(rootNode);
        manager.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                    valueChanged();
                }
            }
        });
        Set<AWTKeyStroke> keys = new HashSet<AWTKeyStroke>(master.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        keys.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0));
        master.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, keys);

        keys = new HashSet<AWTKeyStroke>(master.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        keys.add(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK));
        master.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, keys);
        InputMap inputMap = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_MASK), SWITCH_TO_LEFT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_MASK), SWITCH_TO_RIGHT);

        ActionMap actionMap = getActionMap();
        actionMap.put(SWITCH_TO_LEFT, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                focus(getLeftComponent());
            }
        });
        actionMap.put(SWITCH_TO_RIGHT, new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                focus(getRightComponent());
            }
        });
        //showDetails(master.getSelectedValue(), false);
    }

    @Override
    public void requestFocus() {
        if (master != null) {
            master.requestFocus();
        }
    }

    @Override
    public boolean requestFocusInWindow() {
        if (master != null) {
            return master.requestFocusInWindow();
        }
        return super.requestFocusInWindow();
    }

    private void focus(Component component) {
        while (component instanceof JScrollPane) {
            component = ((JScrollPane) component).getViewport().getView();
        }
        if (component != null) {
            component.requestFocusInWindow();
        }
    }

    public void setMasterData(List<? extends T> data) {
        //master.setListData(data.toArray());
//        master.setRootVisible(true);
        if (data == null || data.isEmpty()) {
            master.setRootVisible(true);
            rootNode.setLeaf();
            return;
        }
        
        master.setRootVisible(false);
        rootNode.setKeys(new ChildrenList(nodeFactory, data));

        master.expandAll();

        try {
            manager.setSelectedNodes(new Node[]{rootNode.getChildren().getNodes()[0]});
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void setSlaveRenderer(SlaveRenderer renderer) {
        slaveRenderer = renderer;
        valueChanged();
    }

    private void valueChanged() {
        Node[] selectedNodes = manager.getSelectedNodes();
        Node node = 0 < selectedNodes.length ? selectedNodes[0] : null;
        if (node instanceof THANode<?>) {
            @SuppressWarnings("unchecked")
            THANode<T> thaNode = (THANode<T>) node;
            showDetails(thaNode.getObject(), true);
        } else {
            showDetails(null, true);
        }
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(
                MasterSlaveView.class, key, params);
    }

    private void showDetails(T masterItem, boolean keepDividerPos) {
        slave = null;
        if (masterItem != null && slaveRenderer != null) {
            slaveRenderer.setValue(masterItem, true);
            slave = slaveRenderer.getComponent();
        }
        if (slave == null) {
            slave = new JLabel(loc("MasterSlaveView.NoDetails")); // NOI18N
        }
        int oldDividerPos = keepDividerPos ? getDividerLocation() : 0;
        setRightComponent(slave);
        slaveRenderer.expandAll();
        if (keepDividerPos) {
            setDividerLocation(oldDividerPos);
        }
        revalidate();
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    private class ChildrenList extends Children.Keys<T> {

        private final List<? extends T> children;
        private final F factory;

        ChildrenList(F factory, List<? extends T> children) {
            this.children = children;
            this.factory = factory;
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            setKeys(children);

        }

        @Override
        protected Node[] createNodes(T key) {
            return new Node[]{factory.create(key)};
        }
    }

    private final class RootNode extends AbstractNode {

        RootNode() {
            super(Children.LEAF);
            setDisplayName(loc("MasterSlaveView.RootNode.Loading"));//NOI18N
        }

        void setKeys(ChildrenList children) {
            setChildren(Children.LEAF);
            setChildren(children);
        }

        void setLeaf(){
            setChildren(Children.LEAF);
        }
    }
}
