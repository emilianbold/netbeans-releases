/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.subversion.ui.browser;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;

/**
 *
 * @author Tomas Stupka
 */
public class BrowserPanel extends JPanel implements ExplorerManager.Provider {

    private final BrowserBeanTreeView treeView;
    private final JLabel label;
    private final ExplorerManager manager;

    private JPanel buttonPanel;
    
    /** Creates new form BrowserPanel */
    public BrowserPanel(String labelText, String browserAcsn, String browserAcsd, boolean singelSelectionOnly) {      
        setName(java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/browser/Bundle").getString("CTL_Browser_Prompt")); // NOI18N
        
        manager = new ExplorerManager();
        
        setLayout(new GridBagLayout());
        
        treeView = new BrowserBeanTreeView();
        treeView.setDragSource(true);
        treeView.setDropTarget(true);
      
        treeView.setPopupAllowed (false);
        treeView.setDefaultActionAllowed (false);
        treeView.setBorder(BorderFactory.createEtchedBorder());
        treeView.getAccessibleContext().setAccessibleDescription(browserAcsd);
        treeView.getAccessibleContext().setAccessibleName(browserAcsn);   
        if(singelSelectionOnly) {
            treeView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        }
        treeView.setPopupAllowed(true);        
        treeView.getTree().setShowsRootHandles(true);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        add(treeView, c);
        
        label = new JLabel();        
        label.setLabelFor(treeView.getTree());
        label.setToolTipText(browserAcsd);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(4,0,4,4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        Mnemonics.setLocalizedText(label, labelText);
        add(label, c);
        
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;                
        add(buttonPanel, c);
        
        setBorder(BorderFactory.createEmptyBorder(12,12,0,12));
    }

    public void setActions(AbstractAction[] actions) {
        if(actions != null) {
            buttonPanel.removeAll();
            for (int i = 0; i < actions.length; i++) {
                JButton button = new JButton(); 
                button.setAction(actions[i]);            
                buttonPanel.add(button);                
            }            
            revalidate();
        }                
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    private class BrowserBeanTreeView extends BeanTreeView {
        public JTree getTree() {            
            return tree;
        } 
        public void startEditingAtPath(TreePath path) {            
            tree.startEditingAtPath(path);
        }         
    }     
          
}
