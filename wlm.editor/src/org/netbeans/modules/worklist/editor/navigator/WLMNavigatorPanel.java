/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.worklist.editor.navigator;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.worklist.dataloader.WorklistDataObject;
import org.netbeans.modules.worklist.editor.nodes.TaskNode;
import org.netbeans.modules.worklist.editor.nodes.WLMNode;
import org.netbeans.modules.worklist.editor.utils.LookupUtils;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author anjeleevich
 */
public class WLMNavigatorPanel extends JPanel implements NavigatorPanel, 
        ExplorerManager.Provider, ComponentListener
{
    private Lookup lookup = null;
    private Lookup.Result<WorklistDataObject> dataObjectResult = null;

    private WorklistDataObject dataObject = null;
    private WLMModel model = null;
    private Node rootNode = null;
    
    private ExplorerManager explorerManager;

    private BeanTreeView beanTreeView;

    private final Object sync = new Object();

    private WLMNavigatorValidationSupport validationSupport;

    // private Lookup simpleLookup;

    private PropertyChangeListener activatedNodeListener
            = new PropertyChangeListener()
    {
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();

            if (!TopComponent.Registry.PROP_ACTIVATED_NODES.equals(propertyName)
                    && !TopComponent.Registry.PROP_ACTIVATED.equals(propertyName))
            {
                return;
            }

            TopComponent activatedTopComponent = TopComponent.getRegistry()
                    .getActivated();
            TopComponent navigatorTopComponent = (TopComponent) SwingUtilities
                    .getAncestorOfClass(TopComponent.class, WLMNavigatorPanel
                    .this);

            if (activatedTopComponent == navigatorTopComponent) {
                return;
            }

            Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
            if (nodes != null) {
                for (Node node : nodes) {
                    Node navigatorNode = findNode(node);
                    if (navigatorNode != null) {
                        try {
                            explorerManager.setSelectedNodes(
                                    new Node[] { navigatorNode });
                        } catch (PropertyVetoException ex) {
                            // do nothing
                        }
                        return;
                    }
                }
            }
        }
    };

    private PropertyChangeListener selectedNodeListener 
            = new PropertyChangeListener()
    {
        public void propertyChange(PropertyChangeEvent evt) {
//            if (!ExplorerManager.PROP_SELECTED_NODES.equals(evt
//                    .getPropertyName()))
//            {
//                return;
//            }
//
            LookupUtils.updateNodesInLookup(explorerManager.getSelectedNodes(),
                    navigatorLookup, navigatorLookupContent);
        }
    };

    private LookupListener lookupListener = new LookupListener() {
        public void resultChanged(LookupEvent arg0) {
            updateView();
        }
    };

    private InstanceContent navigatorLookupContent;
    private Lookup navigatorLookup;

    private PropertyChangeListener validDataObjectListener
            = new PropertyChangeListener()
    {
        public void propertyChange(PropertyChangeEvent evt) {
            if (DataObject.PROP_VALID.equals(evt.getPropertyName())) {
                updateView();
            }
        }
    };

    private PropertyChangeListener modelStatusListener
            = new PropertyChangeListener()
    {
        public void propertyChange(PropertyChangeEvent evt) {
            if (WLMModel.STATE_PROPERTY.equals(evt.getPropertyName())) {
                updateView();
            }
        }
    };

    private Runnable expandRunnable = new Runnable() {
        public void run() {
            if (rootNode instanceof TaskNode) {
                Children children = rootNode.getChildren();
                if (children != null && children != Children.LEAF) {
                    Node[] nodes = children.getNodes();
                    if (nodes != null) {
                        for (Node node : nodes) {
                            beanTreeView.expandNode(node);
                        }
                    }
                }

            }
        }
    };

    private Runnable updateViewRunnable = new Runnable() {
        public void run() {
            updateView();
        }
    };

    private Runnable updateNodesRunnable = new Runnable() {
        public void run() {
            updateNodes();
        }
    };

    public WLMNavigatorPanel() {
        setLayout(new BorderLayout());

        validationSupport = new WLMNavigatorValidationSupport(this);

        navigatorLookupContent = new InstanceContent();
        navigatorLookup = new AbstractLookup(navigatorLookupContent);
        
        explorerManager = new ExplorerManager();
//        simpleLookup = ExplorerUtils.createLookup(explorerManager, getActionMap());
        explorerManager.addPropertyChangeListener(selectedNodeListener);
        beanTreeView = new BeanTreeView();

        add(beanTreeView, BorderLayout.CENTER);
    }

    public String getDisplayName() {
        return "Worklist File";
    }

    public String getDisplayHint() {
        return"Worklist File Navigator";
    }

    public JComponent getComponent() {
        return this;
    }

    private Node findNode(Node target) {
        if (target instanceof WLMNode && rootNode instanceof WLMNode) {
            return findNode((WLMNode) rootNode, (WLMNode) target);
        }
        return null;
    }

    private void updateSelectionImpl() {
        TopComponent activatedTopComponent = TopComponent.getRegistry()
                .getActivated();
        TopComponent navigatorTopComponent = WindowManager.getDefault()
                .findTopComponent("navigatorTC"); // NOI18N

        if (activatedTopComponent == navigatorTopComponent) {
            return;
        }

        Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
        if (nodes != null) {
            for (Node node : nodes) {
                Node navigatorNode = findNode(node);
                if (navigatorNode != null) {
                    try {
                        explorerManager.setSelectedNodes(
                                new Node[] { navigatorNode });
                    } catch (PropertyVetoException ex) {
                        // do nothing
                    }
                    return;
                }
            }
        }
    }

    private WLMNode findNode(WLMNode navigatorNode, WLMNode target) {
        if (navigatorNode.equals(target)) {
            return navigatorNode;
        }

        if (!navigatorNode.isAcceptableDescedand(target)) {
            return null;
        }
        
        Children children = navigatorNode.getChildren();
        if (children == null) {
            return null;
        }

        Node[] childNodes = children.getNodes();
        if (childNodes == null) {
            return null;
        }

        for (Node childNode : childNodes) {
            if (childNode instanceof WLMNode) {
                WLMNode result = findNode((WLMNode) childNode, target);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    public void updateValidationItems() {
        if (rootNode instanceof TaskNode) {
            ((TaskNode) rootNode).reloadValidationItems();
        }
    }

    public void panelActivated(Lookup lookup) {
        if (lookup != null) {
            dataObjectResult = lookup.lookup(new Lookup
                    .Template<WorklistDataObject>(WorklistDataObject.class));
            dataObjectResult.addLookupListener(lookupListener);
        } else {
            dataObjectResult = null;
        }

        this.lookup = lookup;
        
        updateView();

        TopComponent.getRegistry()
                .addPropertyChangeListener(activatedNodeListener);
    }

    public void panelDeactivated() {
        this.lookup = null;

        if (dataObject != null) {
            dataObject.removePropertyChangeListener(validDataObjectListener);
            dataObject = null;
        }

        if (dataObjectResult != null) {
            dataObjectResult.removeLookupListener(lookupListener);
            dataObjectResult = null;
        }

        TopComponent.getRegistry()
                .removePropertyChangeListener(activatedNodeListener);

        updateView();
    }

    private void updateView() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(updateViewRunnable);
            return;
        }

        WorklistDataObject newDataObject = null;
        
        if (dataObjectResult != null) {
            Collection<? extends WorklistDataObject> collection 
                    = dataObjectResult.allInstances();
            if (collection != null) {
                for (WorklistDataObject dataObj : collection) {
                    if (dataObj != null) {
                        newDataObject = dataObj;
                        break;
                    }
                }
            }
        }

        if (newDataObject != null && !newDataObject.isValid()) {
            newDataObject = null;
        }

        if (this.dataObject != newDataObject) {
            if (this.dataObject != null) {
                this.dataObject.removePropertyChangeListener(
                        validDataObjectListener);
            }

            this.dataObject = newDataObject;

            if (this.dataObject != null) {
                this.dataObject.addPropertyChangeListener(
                        validDataObjectListener);
            }
        }

        WLMModel oldModel = this.model;
        WLMModel newModel = (dataObject == null) ? null
                : newDataObject.getModel();

        if (oldModel != newModel) {
            validationSupport.uninstall();
            
            if (this.model != null) {
                this.model.removeComponentListener(this);
                this.model.removePropertyChangeListener(modelStatusListener);
            }
            
            this.model = newModel;

            if (this.model != null) {
                this.model.addComponentListener(this);
                this.model.addPropertyChangeListener(modelStatusListener);
            }
        }

        TTask newTask = (newModel != null && newModel.getState()
                == WLMModel.State.VALID) ? newModel.getTask() : null;
        TTask oldTask = (rootNode instanceof TaskNode)
                ? ((TaskNode) rootNode).getWLMComponent()
                : null;

        if (newTask != oldTask) {
            if (newTask != null) {
                rootNode = new TaskNode(newTask, createNodeLookup());
            } else {
                rootNode = new AbstractNode(Children.LEAF);
            }
            
            explorerManager.setRootContext(rootNode);
            
            if (rootNode instanceof TaskNode) {
                beanTreeView.setVisible(true);
                SwingUtilities.invokeLater(expandRunnable);
            } else {
                beanTreeView.setVisible(false);
            }
        }

        if (oldModel != newModel && newModel != null) {
            validationSupport.install(lookup);
        }
    }

    private Lookup createNodeLookup() {
        return new ProxyLookup(dataObject.getLookup(),
                Lookups.singleton(validationSupport));
    }

    public Lookup getLookup() {
//        return simpleLookup;
        return navigatorLookup;
    }

    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    public void valueChanged(ComponentEvent arg0) {
        updateNodes();
    }

    public void childrenAdded(ComponentEvent arg0) {
        updateNodes();
    }

    public void childrenDeleted(ComponentEvent arg0) {
        updateNodes();
    }

    private void updateNodes() {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(updateNodesRunnable);
            return;
        }

        if (rootNode instanceof TaskNode) {
            ((TaskNode) rootNode).reload();
        }
    }
}
