/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.debugger.common2.ui.processlist;

import java.awt.dnd.DnDConstants;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessInfo;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessInfoDescriptor;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessList;
import org.netbeans.modules.cnd.debugger.common2.utils.ProcessListSupport;
import org.netbeans.swing.etable.ETableColumn;
import org.netbeans.swing.etable.ETableColumnModel;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.TreePathSupport;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author ak119685
 */
public final class ProcessListPanel extends javax.swing.JPanel
        implements ExplorerManager.Provider, ChangeListener, Lookup.Provider {

    private static final boolean IS_TREE_VIEW_ENABLED = Boolean.valueOf(System.getProperty("cnd.debugger.common2.attach.treeview", "false"));//NOI18N
    private final static int FILTER_DELAY = 200;
    private final static int REFRESH_DELAY = 1000;
    private final ProviderLock providerLock = new ProviderLock();
    private final ExplorerManager manager = new ExplorerManager();
    private final Task filterTask;
    private final Task refreshTask;
    private final Lookup lookup;
    private ProcessView currentView = null;
    private ProcessListSupport.Provider listProvider = null;
    //private ProcessActionsSupport.Provider actionsProvider;
    private boolean autorefresh = false;
    private boolean showHierarchy = false;
    private ProcessesRootNode rootNode;
    private final InstanceContent content = new InstanceContent();
    private final PropertyChangeListener propertyChangeListener = new PropertyChangeListenerImpl();
    private ProcessPanelCustomizer customizer = new DefaultCustomizer();
    private final ProcessFilter filter = new ProcessFilter();
    private static final RequestProcessor RP  = new RequestProcessor(ProcessListPanel.class.getName(), 1);
    private static final Preferences prefs =
            NbPreferences.forModule(ProcessListPanel.class);
    private final ChangeSupport changeSupport;
    
    private Lookup.Result<ProcessInfo> lookupResult = null;
    
    /** Creates new form AttachToProcessPanel */
    public ProcessListPanel() {
        changeSupport = new ChangeSupport(this);
        lookup = new AbstractLookup(content);
        content.add(filter);

        initComponents();

        filterTask = RequestProcessor.getDefault().create(new Runnable() {

            @Override
            public void run() {
                filter.set(filterFld.getText());
                updateChildren(getSelectedInfo());
            }
        });

        filterFld.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTask.schedule(FILTER_DELAY);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTask.schedule(FILTER_DELAY);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTask.schedule(FILTER_DELAY);
            }
        });

        refreshTask = RequestProcessor.getDefault().create(new Runnable() {

            @Override
            public void run() {
                synchronized (providerLock) {
                    if (listProvider != null) {
                        listProvider.refresh(true, !userProcessesOnlyCheckBox.isSelected());
                    }
                }

                if (autorefresh) {
                    refreshTask.schedule(REFRESH_DELAY);
                }
            }
        });

        manager.addPropertyChangeListener(propertyChangeListener);        
    }
    
    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }    

    public void setCustomizer(ProcessPanelCustomizer customizer) {
        this.customizer = customizer;
    }

    class PropertyChangeListenerImpl implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
                Node[] selectedNodes = manager.getSelectedNodes();
                if (selectedNodes.length == 0) {
                    content.set(Collections.emptyList(), null);
                    changeSupport.fireChange();
                } else {
                    ProcessInfo info = ((ProcessNode) selectedNodes[0]).getInfo();
                    content.set(Arrays.asList(info), null);
                    changeSupport.fireChange();
                }
            }
        }
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    /*package*/ void setLoading() {
        if (rootNode == null) {
            return;
        }
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    setLoading();
                }
            });
            return;
        }
        this.rootNode.setLoading();
        currentView.expandNodes();
             
    }

    public void setListProvider(final ProcessListSupport.Provider newProvider) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    setListProvider(newProvider);
                }
            });

            return;
        }

        synchronized (providerLock) {
            if (newProvider == listProvider) {
                return;
            }

            if (listProvider != null) {
                listProvider.removeChangeListener(this);
            }

            listProvider = newProvider;

            processListPanel.removeAll();

            if (newProvider != null) {
                newProvider.addChangeListener(this);
                currentView = new ProcessView(customizer.getHeaders(newProvider), customizer);
                processListPanel.add(currentView);
                refreshTask.schedule(0);
            } else {
                processListPanel.add(new JLabel("<No process list provider>")); // NOI18N
            }

            updateRootNode();

            revalidate();
            repaint();
        }
    }

//    public void setActionsProvider(ProcessActionsSupport.Provider newProvider) {
//        this.actionsProvider = newProvider;
//    }

    @Override
    public void addNotify() {
        super.addNotify();

        if (listProvider != null) {
            listProvider.addChangeListener(this);
        }
    }

    @Override
    public void removeNotify() {

        if (refreshTask != null) {
            refreshTask.cancel();
        }

        synchronized (providerLock) {
            if (listProvider != null) {
                listProvider.removeChangeListener(this);
            }
        }
        super.removeNotify();
    }

    private void updateRootNode() {
        assert SwingUtilities.isEventDispatchThread();

//        if (actionsProvider == null) {
//            actionsProvider = ProcessActionsSupport.getDefault();
//        }

        final ProcessInfo selectedInfo = getSelectedInfo();

        rootNode = new ProcessesRootNode(showHierarchy, customizer, filter);
        manager.setRootContext(rootNode);

        updateChildren(selectedInfo);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        filterLabel = new javax.swing.JLabel();
        filterFld = new javax.swing.JTextField();
        refreshBtn = new javax.swing.JButton();
        splitPane = new javax.swing.JSplitPane();
        processListPanel = new javax.swing.JPanel();
        processInfoPanel = new org.netbeans.modules.cnd.debugger.common2.ui.processlist.ProcessInfoPanel();
        treeTogleButton = new javax.swing.JToggleButton();
        listTogleButton = new javax.swing.JToggleButton();
        userProcessesOnlyCheckBox = new javax.swing.JCheckBox();

        filterLabel.setLabelFor(filterFld);
        org.openide.awt.Mnemonics.setLocalizedText(filterLabel, org.openide.util.NbBundle.getMessage(ProcessListPanel.class, "ProcessListPanel.filterLabel.text")); // NOI18N

        filterFld.setText(org.openide.util.NbBundle.getMessage(ProcessListPanel.class, "ProcessListPanel.filterFld.text")); // NOI18N
        filterFld.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                filterFldKeyPressed(evt);
            }
        });

        refreshBtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/debugger/common2/icons/refresh.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(refreshBtn, org.openide.util.NbBundle.getMessage(ProcessListPanel.class, "ProcessListPanel.refreshBtn.text")); // NOI18N
        refreshBtn.setToolTipText(org.openide.util.NbBundle.getMessage(ProcessListPanel.class, "ProcessListPanel.refreshBtn.toolTipText")); // NOI18N
        refreshBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshBtnActionPerformed(evt);
            }
        });

        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.8);

        processListPanel.setMinimumSize(new java.awt.Dimension(200, 50));
        processListPanel.setPreferredSize(new java.awt.Dimension(400, 200));
        processListPanel.setLayout(new java.awt.BorderLayout());
        splitPane.setTopComponent(processListPanel);

        processInfoPanel.setMinimumSize(new java.awt.Dimension(100, 50));
        splitPane.setBottomComponent(processInfoPanel);

        buttonGroup1.add(treeTogleButton);
        treeTogleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/debugger/common2/icons/tree-toggle16.png"))); // NOI18N
        treeTogleButton.setText(org.openide.util.NbBundle.getMessage(ProcessListPanel.class, "ProcessListPanel.treeTogleButton.text")); // NOI18N
        treeTogleButton.setToolTipText(org.openide.util.NbBundle.getMessage(ProcessListPanel.class, "ProcessListPanel.toolTipText")); // NOI18N
        treeTogleButton.setName(""); // NOI18N
        treeTogleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                treeTogleButtonActionPerformed(evt);
            }
        });
        treeTogleButton.setVisible(IS_TREE_VIEW_ENABLED);

        buttonGroup1.add(listTogleButton);
        listTogleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/cnd/debugger/common2/icons/list-toggle16.png"))); // NOI18N
        listTogleButton.setSelected(true);
        listTogleButton.setText(org.openide.util.NbBundle.getMessage(ProcessListPanel.class, "ProcessListPanel.listTogleButton.text_1")); // NOI18N
        listTogleButton.setToolTipText(org.openide.util.NbBundle.getMessage(ProcessListPanel.class, "ProcessListPanel.listTogleButton.toolTipText")); // NOI18N
        listTogleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listTogleButtonActionPerformed(evt);
            }
        });
        listTogleButton.setVisible(IS_TREE_VIEW_ENABLED);

        userProcessesOnlyCheckBox.setSelected(prefs.getBoolean("showAllUserProcesses", false));//NOI18N
        userProcessesOnlyCheckBox.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/debugger/common2/ui/processlist/Bundle").getString("ProcessListPanel.userProcessesOnlyCheckBox.text.mn").charAt(0));
        userProcessesOnlyCheckBox.setText(org.openide.util.NbBundle.getMessage(ProcessListPanel.class, "ProcessListPanel.userProcessesOnlyCheckBox.text")); // NOI18N
        userProcessesOnlyCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userProcessesOnlyCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(splitPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(filterLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(filterFld, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(userProcessesOnlyCheckBox)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(refreshBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(treeTogleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(listTogleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(filterLabel)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(filterFld, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(refreshBtn)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(userProcessesOnlyCheckBox))
                    .addComponent(treeTogleButton)
                    .addComponent(listTogleButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void filterFldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filterFldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            filterTask.schedule(0);
            evt.consume();
        }
    }//GEN-LAST:event_filterFldKeyPressed

    private void refreshBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshBtnActionPerformed
        if (listProvider != null) {
            setLoading();
            listProvider.refresh(true, !userProcessesOnlyCheckBox.isSelected());
        }
    }//GEN-LAST:event_refreshBtnActionPerformed

    private void treeTogleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_treeTogleButtonActionPerformed
        showHierarchy = true;
        updateRootNode();
        if (listProvider != null) {
            listProvider.refresh(true, !userProcessesOnlyCheckBox.isSelected());
        }
    }//GEN-LAST:event_treeTogleButtonActionPerformed

    private void listTogleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listTogleButtonActionPerformed
        showHierarchy = false;
        updateRootNode();
        if (listProvider != null) {
            listProvider.refresh(true, !userProcessesOnlyCheckBox.isSelected());
        }
    }//GEN-LAST:event_listTogleButtonActionPerformed

    private void userProcessesOnlyCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_userProcessesOnlyCheckBoxActionPerformed
        // TODO add your handling code here:
        if (listProvider != null) {
            setLoading();
            listProvider.refresh(true, !userProcessesOnlyCheckBox.isSelected());
        }
        prefs.putBoolean("showAllUserProcesses", userProcessesOnlyCheckBox.isSelected());//NOI18N
    }//GEN-LAST:event_userProcessesOnlyCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JTextField filterFld;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JToggleButton listTogleButton;
    private org.netbeans.modules.cnd.debugger.common2.ui.processlist.ProcessInfoPanel processInfoPanel;
    private javax.swing.JPanel processListPanel;
    private javax.swing.JButton refreshBtn;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JToggleButton treeTogleButton;
    private javax.swing.JCheckBox userProcessesOnlyCheckBox;
    // End of variables declaration//GEN-END:variables

    @Override
    public void stateChanged(ChangeEvent e) {
        Object src = e.getSource();
        if (src instanceof ProcessListSupport.Provider) {
            updateChildren(getSelectedInfo());
        }
    }

    public void setFilter(String filter) {
        this.filterFld.setText(filter);
    }

    public String getFilter() {
        return filterFld.getText();
    }

    /**
     * Returns currently selected ProcessInfo. 
     * Should be called from AWT.
     * 
     * @return currently selected ProcessInfo
     */
    public ProcessInfo getSelectedInfo() {
        return currentView == null ? null : currentView.getSelectedInfo();
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    private void updateChildren(final ProcessInfo selectedInfo) {
        if (rootNode == null || listProvider == null) {
            return;
        }

        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    updateChildren(selectedInfo);
                }
            });
            return;
        }

        updateChildrenInAWT(selectedInfo);
    }

    private void updateChildrenInAWT(final ProcessInfo selectedInfo) {
        setLoading();
        RP.post(new Runnable() {
            @Override
            public void run() {
                final ProcessList processList = listProvider.getProcessList();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        rootNode.refresh(processList);
                        try {
                            currentView.expandNodes();
                            if (selectedInfo != null) {
                                Node node = rootNode.getNode(selectedInfo.getPID());
                                if (node != null) {
                                    manager.setSelectedNodes(new Node[]{node});
                                }
                            }
                        } catch (PropertyVetoException ex) {
                        }                       
                    }
                });
            }
        });
        

        
    }

    private static class ProcessView extends OutlineView {

        private final Set<Integer> collapsedPIDs = new HashSet<Integer>();
        private final TreeExpansionListener expansionListener;

        private ProcessView(final List<ProcessInfoDescriptor> descriptors, final ProcessPanelCustomizer customizer) {
            super(customizer.getOutlineHeaderName());

            setDragSource(false);
            setDropTarget(false);

            expansionListener = new TreeExpansionListener() {

                @Override
                public void treeExpanded(TreeExpansionEvent event) {
                    assert SwingUtilities.isEventDispatchThread();
                    Node node = Visualizer.findNode(event.getPath().getLastPathComponent());

                    if (node == null) {
                        return;
                    }

                    ProcessInfo info = node.getLookup().lookup(ProcessInfo.class);

                    if (info == null) {
                        return;
                    }

                    collapsedPIDs.remove(info.getPID());
                    // ??? 
                    // if collapse a child node and then collapse a parent node
                    // then do refresh and expand the parent, the child will be 
                    // also expanded! 
                    // so do own expansion that will collapse collapsed nodes
                    expandNodes();
                }

                @Override
                public void treeCollapsed(TreeExpansionEvent event) {
                    assert SwingUtilities.isEventDispatchThread();
                    Node node = Visualizer.findNode(event.getPath().getLastPathComponent());

                    if (node == null) {
                        return;
                    }

                    ProcessInfo info = node.getLookup().lookup(ProcessInfo.class);

                    if (info == null) {
                        return;
                    }

                    collapsedPIDs.add(info.getPID());
                }
            };

            this.addTreeExpansionListener(expansionListener);
            setAllowedDropActions(DnDConstants.ACTION_NONE);

            Outline outline = getOutline();
            outline.setRootVisible(false);
            setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            
            outline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            outline.setCellEditor(null);
            Property[] props = new Property[descriptors.size()];

            int idx = 0;

            for (ProcessInfoDescriptor d : descriptors) {
                props[idx++] = new PrototypeProperty(d.id, d.header, d.shortDescription);
            }

            setProperties(props);

            ETableColumnModel colModel = (ETableColumnModel) outline.getColumnModel();
            TableColumn firstColumn = colModel.getColumn(0);
            ETableColumn col = (ETableColumn) firstColumn;
            col.setNestedComparator(customizer);
        }

        private ProcessInfo getSelectedInfo() {
            ExplorerManager manager = ExplorerManager.find(ProcessView.this);

            if (manager == null) {
                return null;
            }

            Node[] selectedNodes = manager.getSelectedNodes();
            return selectedNodes.length == 0 ? null
                    : selectedNodes[0].getLookup().lookup(ProcessInfo.class);
        }

        private void expandNodes() {
            assert SwingUtilities.isEventDispatchThread();

            ExplorerManager manager = ExplorerManager.find(ProcessView.this);

            if (manager == null || !(manager.getRootContext() instanceof ProcessesRootNode)) {
                return;
            }

            removeTreeExpansionListener(expansionListener);

            try {
                ProcessesRootNode rootNode = (ProcessesRootNode) manager.getRootContext();

                expandAll(new TreePath(getOutline().getOutlineModel().getRoot()),
                        (TreeNode) getOutline().getOutlineModel().getRoot());

                for (Integer pid : collapsedPIDs) {
                    final Node node = rootNode.getNode(pid);
                    if (node != null) {
                        collapseNode(node);
                    }
                }
            } finally {
                addTreeExpansionListener(expansionListener);
            }
        }

        private void expandAll(TreePath path, TreeNode node) {
            if (node == null) {
                return;
            }

            TreePathSupport tps = getOutline().getOutlineModel().getTreePathSupport();
            tps.expandPath(path);

            Enumeration children = node.children();
            while (children.hasMoreElements()) {
                Object nextElement = children.nextElement();
                expandAll(path.pathByAddingChild(nextElement), (TreeNode) nextElement);
            }
        }
    }

    static final class PrototypeProperty extends PropertySupport.ReadOnly<Object> {

        PrototypeProperty(String name, String displayName, String description) {
            super(name, Object.class, displayName, description);
        }

        @Override
        public Object getValue() throws IllegalAccessException,
                InvocationTargetException {
            throw new AssertionError();
        }

        @Override
        public boolean equals(Object o) {
            return o != null && o instanceof Property
                    && getName().equals(((Property) o).getName());
        }

        @Override
        public int hashCode() {
            return getName().hashCode();
        }
    }

    private static class DefaultCustomizer implements ProcessPanelCustomizer {

        @Override
        public String getDisplayName(ProcessInfo info) {
            return Integer.toString(info.getPID());
        }

        @Override
        public List<ProcessInfoDescriptor> getValues(ProcessInfo info) {
            return info.getDescriptors();
        }

        @Override
        public List<ProcessInfoDescriptor> getHeaders(ProcessListSupport.Provider provider) {
            return provider.getDescriptors();
        }

        @Override
        public int compare(AbstractNode o1, AbstractNode o2) {
            if (o1 instanceof ProcessNode && o2 instanceof ProcessNode) {
                Integer pid1 = ((ProcessNode) o1).getInfo().getPID();
                Integer pid2 = ((ProcessNode) o2).getInfo().getPID();
                return pid1.compareTo(pid2);
            }

            return 1;
        }

        @Override
        public String getOutlineHeaderName() {
            return NbBundle.getMessage(ProcessListPanel.class, "ProcessListPanel.outlineHeaderName"); // NOI18N
        }
    }

    private final static class ProviderLock {
    };
}
