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

package org.netbeans.modules.websvc.design.view.panels;

import javax.swing.JPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import java.awt.BorderLayout;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.NbBundle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JLabel;
import org.netbeans.api.project.Project;
import org.openide.nodes.Children;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.FilterNode;

/**
 * @author  rico
 */
public class SelectSchemaPanel extends JPanel implements ExplorerManager.Provider {
    
    private ExplorerManager manager;
    private Node[] selectedNodes;
    private JPanel panel;
    private Project project;
    
    /**
     * Creates a new instance of SelectSchemaPanel
     */
    public SelectSchemaPanel(Project project) {
        this.project = project;
        initComponents();
        manager = new ExplorerManager();
        manager.addPropertyChangeListener(
                new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                selectedNodes = manager.getSelectedNodes();
            }
        });
        populateTree();
    }
    
    public Node[] getSelectedNodes(){
        return selectedNodes;
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    private void populateTree(){
        LogicalViewProvider lvp = (LogicalViewProvider)project.getLookup().lookup(LogicalViewProvider.class);
        Node projectView = lvp.createLogicalView();
        Children.Array children = new Children.Array();
        FilterNode filter = new FilterNode(projectView, new SourceListViewChildren());
        children.add(new FilterNode[] {filter});
        Node root = new AbstractNode(children);
        manager.setRootContext(filter);
        
    }
    
    private void initComponents() {
        panel = new JPanel();
        setLayout(new BorderLayout());
        BorderLayout bl = new BorderLayout();
        panel.setLayout(bl);
        bl.setVgap(10);
        add(panel, BorderLayout.CENTER);
        
        BeanTreeView btv = new BeanTreeView();
        btv.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        btv.getAccessibleContext().
                setAccessibleName(NbBundle.getMessage(SelectSchemaPanel.class,"LBL_Schemas"));
        btv.getAccessibleContext().setAccessibleDescription
                (NbBundle.getMessage(SelectSchemaPanel.class,"TTL_SelectSchema"));
        String projectName = project.getProjectDirectory().getName();
        String classesLabel = projectName + " " +
                NbBundle.getMessage(SelectSchemaPanel.class, "LBL_Schemas") + ":";
        JLabel label = new JLabel(classesLabel);
        panel.add(label, BorderLayout.NORTH);
        panel.add(btv, BorderLayout.CENTER);   //NOI18N
        panel.validate();
        validate();
    }
    
    
    class SourceListViewChildren extends Children.Keys<String> {
        public static final String KEY_SOURCES = "sourcesKey"; //NOI18N
        
        protected Node[] createNodes(String key) {
            Node n = null;
            List<Node> sourceNodes = new LinkedList<Node>();
            if (KEY_SOURCES.equals(key)) {
                Sources sources = ProjectUtils.getSources(project);
                SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                for(int i = 0; i < groups.length; i++){
                    sourceNodes.add(PackageView.createPackageView(groups[i]));
                }
            }
            return sourceNodes.toArray(new Node[sourceNodes.size()]);
        }
        
        @Override
        protected void addNotify() {
            super.addNotify();
            createNodes();
        }
        
        private void createNodes() {
            List<String> l = new ArrayList<String>();
            l.add(KEY_SOURCES);
            setKeys(l);
        }
        
        @Override
        protected void removeNotify() {
            setKeys(Collections.EMPTY_SET);
            super.removeNotify();
        }
        
    }
}
