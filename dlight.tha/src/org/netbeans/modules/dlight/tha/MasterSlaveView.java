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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.Renderer;
import org.netbeans.module.dlight.threads.api.Datarace;
import org.netbeans.modules.dlight.core.stack.api.FunctionCall;
import org.netbeans.modules.dlight.core.stack.api.ThreadDump;
import org.netbeans.modules.dlight.core.stack.api.ThreadSnapshot;
import org.netbeans.modules.dlight.core.stack.ui.CallStackUISupport;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * @author Alexey Vladykin
 */
public final class MasterSlaveView<T, F extends THANodeFactory<T>> extends JSplitPane implements ExplorerManager.Provider {

    private final BeanTreeView master;
    private final JPanel rightPanel = new JPanel();
    private Component slave;
    private Renderer slaveRenderer;
    private final ExplorerManager manager = new ExplorerManager();
    private final RootNode rootNode = new RootNode();
    private final F nodeFactory;

    public MasterSlaveView(F factory) {
        this(factory, Collections.<T>emptyList(), null);
    }

    public MasterSlaveView(F factory, List<? extends T> data, Renderer slaveRenderer) {
        super(HORIZONTAL_SPLIT);
        this.master = new BeanTreeView();
        master.setRootVisible(false);
        this.slaveRenderer = slaveRenderer;
        this.nodeFactory = factory;
        setResizeWeight(0.5);
        setLeftComponent(master);
        setRightComponent(rightPanel);
        manager.setRootContext(rootNode);
        manager.addPropertyChangeListener(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                    valueChanged();
                }
            }
        });
        //showDetails(master.getSelectedValue(), false);
    }

    public void setMasterData(List<? extends T> data) {
        //master.setListData(data.toArray());
//        master.setRootVisible(true);
        if (data.isEmpty()) {
            master.setRootVisible(true);
        } else {
            master.setRootVisible(false);
            rootNode.setKeys(new ChildrenList(nodeFactory, data));
        }
        master.expandAll();
    }

    public void setSlaveRenderer(Renderer renderer) {
        slaveRenderer = renderer;
        showDetails(manager.getSelectedNodes().length > 0 ? ((THANode<T>) manager.getSelectedNodes()[0]).getObject() : null, true);
    }

    private void valueChanged() {
        showDetails(manager.getSelectedNodes().length > 0  && manager.getSelectedNodes()[0] instanceof THANode ? ((THANode<T>) manager.getSelectedNodes()[0]).getObject() : null, true);

    }

    private void showDetails(T masterItem, boolean keepDividerPos) {
        slave = null;
        if (masterItem != null && slaveRenderer != null) {
            slaveRenderer.setValue(masterItem, true);
            slave = slaveRenderer.getComponent();
        }
        if (slave == null) {
            slave = new JLabel("<No details>"); // NOI18N
        }
        int oldDividerPos = keepDividerPos ? getDividerLocation() : 0;
        rightPanel.removeAll();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(slave, BorderLayout.CENTER);
        rightPanel.repaint();
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
            setDisplayName("Loading...");//NOI18N
        }

        void setKeys(ChildrenList children) {
            setChildren(Children.LEAF);
            setChildren(children);
        }
    }
}
