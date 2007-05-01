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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mashup.db.ui.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.MissingResourceException;

import javax.swing.JPanel;

import org.netbeans.modules.mashup.db.model.FlatfileDatabaseModel;
import org.netbeans.modules.mashup.db.ui.FlatfileTreeTableView;
import org.netbeans.modules.mashup.db.ui.model.FlatfileTreeTableModel;
import org.netbeans.modules.mashup.db.wizard.FlatfileViewerTreePanel;
import org.openide.util.NbBundle;


/**
 * Descriptor for single panel to select tables and columns to be included in an Flatfile
 * Database definition instance.
 * 
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class PreviewDatabaseVisualPanel extends JPanel {

    /* Container to hold configuration components */
    private JPanel contentPanel;

    /* Model used to supply content to FlatfileTreeTableView instance */
    private FlatfileTreeTableModel mTreeModel;

    /* Component which displays content to be configured. */
    private FlatfileTreeTableView mTreeView;

    /**
     * Constructs a new instance of PreviewDatabaseVisualPanel with the given
     * PreviewDatabasePanel instance as its owner.
     * 
     * @param panelHost owner of this new instance
     */
    public PreviewDatabaseVisualPanel(PreviewDatabasePanel panelHost) {
        mTreeModel = new FlatfileTreeTableModel();
        mTreeView = new FlatfileTreeTableView();

        setLayout(new BorderLayout());

        try {
            setName(NbBundle.getMessage(PreviewDatabaseVisualPanel.class, "TITLE_configureotd"));
        } catch (MissingResourceException e) {
            setName("*** Preview Flatfile Database ***");
        }
    }
    
     /**
     * Constructs a new instance of PreviewDatabaseVisualPanel
     * 
     * @param panelHost owner of this new instance
     */
    public PreviewDatabaseVisualPanel() {
        mTreeModel = new FlatfileTreeTableModel();
        mTreeView = new FlatfileTreeTableView();

        setLayout(new BorderLayout());

        try {
            setName(NbBundle.getMessage(PreviewDatabaseVisualPanel.class, "TITLE_configureotd"));
        } catch (MissingResourceException e) {
            setName("*** Preview Flatfile Database ***");
        }
    }

    /**
     * Gets FlatfileDatabaseModel representing current contents of this visual component.
     * 
     * @return FlatfileDatabaseModel representing the contents of this visual component
     */
    public FlatfileDatabaseModel getModel() {
        if (mTreeModel != null) {
            FlatfileDatabaseModel modFolder = mTreeModel.getModel();
            return modFolder;
        }
        return null;
    }

    /**
     * Indicates whether the controls in this panel all have sufficient valid data to
     * advance the wizard to the next panel.
     * 
     * @return true if data are valid, false otherwise
     */
    public boolean hasValidData() {
        return true;
    }

    /**
     * Sets data model of this visual component to the contents of the given
     * FlatfileDatabaseModel.
     * 
     * @param newModel FlatfileDatabaseModel whose contents will be rendered by this
     *        component
     */
    public void setModel(FlatfileDatabaseModel newModel) {
        mTreeModel.configureModel(newModel);
        mTreeView.setModel(mTreeModel);

        if (contentPanel == null) {
            contentPanel = createContentPanel(newModel, mTreeView);
            add(contentPanel, BorderLayout.CENTER);
        }

        mTreeView.revalidate();
        mTreeView.repaint();
    }

    private JPanel createContentPanel(FlatfileDatabaseModel folder, FlatfileTreeTableView view) {
        JPanel outermost = new JPanel(new BorderLayout());

        mTreeView.setModel(mTreeModel);
        mTreeView.setDividerLocation(190);
        outermost.add(mTreeView, BorderLayout.CENTER);
        outermost.setSize(300, 200);
        return outermost;
    }
}

