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

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeSelectionModel;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.UserCancelException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class BrowserPanel extends JPanel implements ExplorerManager.Provider{

    private BrowserBeanTreeView treeView;
    private JLabel label;
    private final ExplorerManager manager;

    /** Creates new form BrowserPanel */
    public BrowserPanel(String title, String browserAcsn, String browserAcsd, boolean singelSelectionOnly) {      
        manager = new ExplorerManager();
        
        setLayout(new BorderLayout(6, 6));
        
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
        add(java.awt.BorderLayout.CENTER, treeView);
        label = new JLabel();        
        label.setLabelFor(treeView.getTree());
        label.setToolTipText(browserAcsd);
        Mnemonics.setLocalizedText(label, title);
        add(label, BorderLayout.NORTH);
                 
        setBorder(BorderFactory.createEmptyBorder(12,12,0,12));
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    // XXX ist there some another way to get the tree?
    private class BrowserBeanTreeView extends BeanTreeView {
        public JTree getTree() {
            return tree;
        } 
    } 


}
