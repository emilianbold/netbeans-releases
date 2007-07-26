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
package org.netbeans.modules.subversion.ui.browser;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.TreeTableView;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class BrowserPanel extends JPanel implements ExplorerManager.Provider {

    private final BrowserTreeTableView treeView;    
    private final ExplorerManager manager;

    private ControlPanel controlPanel;
    
    /** Creates new form BrowserPanel */
    public BrowserPanel(String labelText, String browserAcsn, String browserAcsd, boolean singleSelection) {      
        setName(java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/browser/Bundle").getString("CTL_Browser_Prompt"));                   // NOI18N
        
        manager = new ExplorerManager();
        
        setLayout(new GridBagLayout());
        
        treeView = new BrowserTreeTableView();
        treeView.setDragSource(true);
        treeView.setDropTarget(true);
              
        treeView.setDefaultActionAllowed (false);
        treeView.setBorder(UIManager.getBorder("Nb.ScrollPane.border")); // NOI18N
        treeView.getAccessibleContext().setAccessibleDescription(browserAcsd);
        treeView.getAccessibleContext().setAccessibleName(browserAcsn);   
        if(singleSelection) {
            treeView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        }
        treeView.setPopupAllowed(true);        
        treeView.getTree().setShowsRootHandles(true);                        
        
        GridBagConstraints c = new GridBagConstraints();
        int gridY = 0;
                
        // title label        
        JLabel label = new JLabel();        
        label.setLabelFor(treeView.getTable());
        label.setToolTipText(browserAcsd);
        if(labelText != null && !labelText.trim().equals("")) {
            org.openide.awt.Mnemonics.setLocalizedText(label, labelText);
        } else {
            org.openide.awt.Mnemonics.setLocalizedText(label, org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BK2003"));                          // NOI18N
        }        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = gridY++;
        c.insets = new Insets(4,0,4,4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;        
        
        add(label, c);        
        
        // treetable               
        c.gridx = 0;
        c.gridy = gridY++;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        add(treeView, c);
                
        // buttons
        controlPanel = new ControlPanel();        
        controlPanel.warningLabel.setVisible(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = gridY++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;                
        add(controlPanel, c);                
        
        setBorder(BorderFactory.createEmptyBorder(12,12,0,12));
        
        setPreferredSize(new Dimension(800, 400));
    }   
    
    void warning(String warningText) {
        if(warningText != null) {
            controlPanel.warningLabel.setText(warningText);
            controlPanel.warningLabel.setVisible(true);      
        } else {
            controlPanel.warningLabel.setText("");   
            controlPanel.warningLabel.setVisible(false);                  
        }     
    }
    
    public void setActions(AbstractAction[] actions) {
        if(actions != null) {
            controlPanel.buttonPanel.removeAll();
            for (int i = 0; i < actions.length; i++) {
                JButton button = new JButton(); 
                button.setAction(actions[i]);      
                button.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "CTL_Action_MakeDir"));     // NOI18N
                org.openide.awt.Mnemonics.setLocalizedText(button, org.openide.util.NbBundle.getMessage(BrowserPanel.class, "CTL_Action_MakeDir"));         // NOI18N
                controlPanel.buttonPanel.add(button);                    
            }            
            revalidate();
        }                
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    void addTreeExpansionListener(TreeExpansionListener l) {
        treeView.getTree().addTreeExpansionListener(l);
    }
    
    void removeTreeExpansionListener(TreeExpansionListener l) {
        treeView.getTree().removeTreeExpansionListener(l);
    }
    
    private class BrowserTreeTableView extends TreeTableView {        
        BrowserTreeTableView() {
            setupColumns();
        }
        
        public JTree getTree() {            
            return tree;
        } 
        
        JTable getTable() {
            return treeTable;
        }
        
        public void startEditingAtPath(TreePath path) {            
            tree.startEditingAtPath(path);
        }         
        
        public void addNotify() {
            super.addNotify();
            setDefaultColumnSizes();
        }
        
        private void setupColumns() {
            ResourceBundle loc = NbBundle.getBundle(BrowserPanel.class);            
            Node.Property [] columns = new Node.Property[4];
            columns[0] = new ColumnDescriptor<String>(RepositoryPathNode.PROPERTY_NAME_REVISION, String.class, loc.getString("LBL_BrowserTree_Column_Revision"), loc.getString("LBL_BrowserTree_Column_Revision_Desc"));        // NOI18N
            columns[1] = new ColumnDescriptor<String>(RepositoryPathNode.PROPERTY_NAME_DATE,     String.class, loc.getString("LBL_BrowserTree_Column_Date"),     loc.getString("LBL_BrowserTree_Column_Date_Desc"));            // NOI18N
            columns[2] = new ColumnDescriptor<String>(RepositoryPathNode.PROPERTY_NAME_AUTHOR,   String.class, loc.getString("LBL_BrowserTree_Column_Author"),   loc.getString("LBL_BrowserTree_Column_Author_Desc"));          // NOI18N
            columns[3] = new ColumnDescriptor<String>(RepositoryPathNode.PROPERTY_NAME_HISTORY,  String.class, loc.getString("LBL_BrowserTree_Column_History"),  loc.getString("LBL_BrowserTree_Column_History_Desc"));         // NOI18N    
            
            setProperties(columns);
        }    
    
        private void setDefaultColumnSizes() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    int width = getWidth();                    
                    treeTable.getColumnModel().getColumn(0).setPreferredWidth(width * 50 / 100);
                    treeTable.getColumnModel().getColumn(1).setPreferredWidth(width * 10 / 100);
                    treeTable.getColumnModel().getColumn(2).setPreferredWidth(width * 20 / 100);                                    
                    treeTable.getColumnModel().getColumn(3).setPreferredWidth(width * 10 / 100);                                    
                    treeTable.getColumnModel().getColumn(4).setPreferredWidth(width * 10 / 100);                    
                }
            });
        }            
    }     

    private static class ColumnDescriptor<T> extends PropertySupport.ReadOnly<T> {        
        public ColumnDescriptor(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        public T getValue() throws IllegalAccessException, InvocationTargetException {
            return null;
        }
    }    
}
