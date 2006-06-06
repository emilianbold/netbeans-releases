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
package org.netbeans.modules.subversion.ui.search;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.tree.TreeSelectionModel;
import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.ListView;

/**
 *
 * @author Tomas Stupka
 */
public class SvnSearchPanel extends JPanel implements ExplorerManager.Provider{

    private final SvnSearchListView listView;
    private final JLabel label;
    private final ExplorerManager manager;

    private JPanel buttonPanel;
    
    /** Creates new form BrowserPanel */
    public SvnSearchPanel() {      
        setName("Browse Repository Folders");
        
        manager = new ExplorerManager();
        
        setLayout(new GridBagLayout());
        
        listView = new SvnSearchListView();
        listView.setPopupAllowed (false);
        listView.setBorder(BorderFactory.createEtchedBorder());
        //listView.getAccessibleContext().setAccessibleDescription(browserAcsd);
        //listView.getAccessibleContext().setAccessibleName(browserAcsn);   
        listView.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);     
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        add(listView, c);
        
        label = new JLabel();        
        label.setLabelFor(listView.getList());
        //label.setToolTipText(browserAcsd);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(4,0,4,4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        Mnemonics.setLocalizedText(label, "Select a Revision:");
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
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    private class SvnSearchListView extends ListView {
        public JList getList() {            
            return list;
        }         
    }     

}
