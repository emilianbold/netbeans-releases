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

package org.netbeans.modules.autoupdate.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Jiri Rechtacek
 */
public class UnitTab extends javax.swing.JPanel {
    private UnitTable table = null;
    private UnitDetails details = null;
    private UnitCategoryTableModel model = null;
    private DocumentListener dlForSearch;
    private String filter = "";
    private PluginManagerUI manager = null;
    private static final RequestProcessor RP = new RequestProcessor();
    private final RequestProcessor.Task searchTask = RP.create(new Runnable(){
        public void run() {
            if (filter != null) {
                model.setFilter(filter);
            }
        }
    });
    private final Logger log = Logger.getLogger ("org.netbeans.modules.autoupdate.ui.UnitTab");
    
    /** Creates new form UnitTab */
    public UnitTab(UnitTable table, UnitDetails details, PluginManagerUI manager) {
        this.table = table;
        this.details = details;
        this.manager = manager;
        TableModel m = table.getModel();
        assert m instanceof UnitCategoryTableModel : m + " instanceof UnitCategoryTableModel.";
        this.model = (UnitCategoryTableModel) m;
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        initComponents();
        spUnitTable.setViewportView(table);        
        initTab();
        listenOnSelection();
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        if (dlForSearch == null) {
            tfSearch.getDocument().addDocumentListener(getDocumentListener());
        }
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        if (dlForSearch != null) {
            tfSearch.getDocument().removeDocumentListener(getDocumentListener());
        }
        dlForSearch = null;
    }
    
    
    public void refreshState() {
        Collection<Unit> units = model.getMarkedUnits();
        int downloadSize = model.getDownloadSize ();
        
        if (units.size () == 0 || downloadSize == 0) {
            cleanHowMany();
        } else {
            setHowManyDownload(Utilities.getDownloadSizeAsString (downloadSize));
            setHowManySelected(units.size());
        }
        
        bTabAction.setEnabled(units.size() > 0);
    }
    
    private void initTab() {
        
        switch (model.getType()) {
        case INSTALLED :
            setTabActionDescription("UnitTab_lTabActionDescription_Text_INSTALLED");
            setTabActionName("UnitTab_bTabAction_Name_INSTALLED");
            bRefresh.setVisible (true);
            bAddLocallyDownloads.setVisible(false);
            break;
        case UPDATE :
            setTabActionDescription("UnitTab_lTabActionDescription_Text_UPDATE");
            setTabActionName("UnitTab_bTabAction_Name_UPDATE");
            bRefresh.setVisible (true);
            bAddLocallyDownloads.setVisible(false);
            break;
        case AVAILABLE :
            setTabActionDescription("UnitTab_lTabActionDescription_Text_AVAILABLE");
            setTabActionName("UnitTab_bTabAction_Name_AVAILABLE");
            bRefresh.setVisible (true);
            bAddLocallyDownloads.setVisible(false);
            break;
        case LOCAL :
            setTabActionDescription("UnitTab_lTabActionDescription_Text_LOCAL");
            setTabActionName("UnitTab_bTabAction_Name_LOCAL");
            bRefresh.setVisible (false);
            bAddLocallyDownloads.setVisible (true);
            break;
        }
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                refreshState();
            }
        });
        bTabAction.setEnabled(model.getMarkedUnits().size() > 0);
    }
    
    private void setTabActionDescription(String key) {
        lTabActionDescription.setText(NbBundle.getMessage(UnitTab.class, key));
    }
    
    private void setTabActionName(String key) {
        Mnemonics.setLocalizedText(bTabAction, NbBundle.getMessage(UnitTab.class, key));
    }
    
    private void cleanHowMany() {
        lHowManyDownload.setText("");
        lHowManySelected.setText("");
    }
    
    private void setHowManyDownload(String size) {
        // don't show how many downloaded in INSTALLED tab
        if (UnitCategoryTableModel.Type.INSTALLED == model.getType ()) {
            return ;
        }
        // don't show how many downloaded in LOCAL tab
        if (UnitCategoryTableModel.Type.LOCAL == model.getType ()) {
            return ;
        }
        lHowManyDownload.setText(NbBundle.getMessage(UnitTab.class, "UnitTab_lHowManyDownloaded_Text", size));
    }
    
    private void setHowManySelected(int count) {
        String key = null;
        switch (model.getType()) {
        case INSTALLED :
            key = "UnitTab_lHowManySelected_Text_INSTALLED";
            break;
        case UPDATE :
            key = "UnitTab_lHowManySelected_Text_UPDATE";
            break;
        case AVAILABLE :
            key = "UnitTab_lHowManySelected_Text_AVAILABLE";
            break;
        case LOCAL :
            key = "UnitTab_lHowManySelected_Text_LOCAL";
            break;
        }
        lHowManySelected.setText( (NbBundle.getMessage(UnitTab.class, key, count)));
    }
    
    private void showDetailsAtRow(int row) {
        if (row == -1) {
            details.setUnit(null);
        } else {
            Unit u = model.isCategoryAtRow(row) ? null : model.getUnitAtRow(row);
            if (u == null) {
                UnitCategory category = model.getCategoryAtRow(row);
                details.setUnitCategory(category);
            } else {
                details.setUnit(u);
            }
        }
    }
    
    private void listenOnSelection() {
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) return;
                
                ListSelectionModel lsm =
                        (ListSelectionModel)e.getSource();
                if (lsm.isSelectionEmpty()) {
                    //no rows are selected
                    showDetailsAtRow(-1);
                } else {
                    int selectedRow = lsm.getMinSelectionIndex();
                    //selectedRow is selected
                    showDetailsAtRow(selectedRow);
                }
            }
        });
    }
    
    public void addUpdateUnitListener(UpdateUnitListener l) {
        model.addUpdateUnitListener(l);
    }
    
    public void removeUpdateUnitListener(UpdateUnitListener l) {
        model.removeUpdateUnitListener(l);
    }
    
    void fireUpdataUnitChange() {
        model.fireUpdataUnitChange();
    }
    
    DocumentListener getDocumentListener() {
        if (dlForSearch == null) {
            dlForSearch = new DocumentListener() {
                public void insertUpdate(DocumentEvent arg0) {
                    filter = tfSearch.getText().trim();
                    searchTask.schedule(350);
                }
                
                public void removeUpdate(DocumentEvent arg0) {
                    insertUpdate(arg0);
                }
                
                public void changedUpdate(DocumentEvent arg0) {
                    insertUpdate(arg0);
                }
                
            };
        }
        return dlForSearch;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        lTabActionDescription = new javax.swing.JLabel();
        lSearch = new javax.swing.JLabel();
        tfSearch = new javax.swing.JTextField();
        spUnitTable = new javax.swing.JScrollPane();
        bRefresh = new javax.swing.JButton();
        lHowManySelected = new javax.swing.JLabel();
        lHowManyDownload = new javax.swing.JLabel();
        bTabAction = new javax.swing.JButton();
        bAddLocallyDownloads = new javax.swing.JButton();

        lSearch.setLabelFor(tfSearch);
        org.openide.awt.Mnemonics.setLocalizedText(lSearch, org.openide.util.NbBundle.getMessage(UnitTab.class, "UnitTab_lSearch_Text")); // NOI18N

        tfSearch.setText(org.openide.util.NbBundle.getMessage(UnitTab.class, "UnitTab.tfSearch.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(bRefresh, org.openide.util.NbBundle.getMessage(UnitTab.class, "UnitTab_bRefresh_Name")); // NOI18N
        bRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bRefreshActionPerformed(evt);
            }
        });

        bTabAction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bTabActionActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(bAddLocallyDownloads, org.openide.util.NbBundle.getMessage(UnitTab.class, "UnitTab_bAddLocallyDownloads_Name")); // NOI18N
        bAddLocallyDownloads.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bAddLocallyDownloadsActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(lTabActionDescription, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(lSearch)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(bAddLocallyDownloads)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(bRefresh)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, lHowManyDownload, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, lHowManySelected, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .add(32, 32, 32)))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(bTabAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(tfSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 145, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(spUnitTable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                        .add(12, 12, 12))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(lSearch)
                    .add(tfSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lTabActionDescription, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(spUnitTable, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(lHowManySelected, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(bAddLocallyDownloads)
                        .add(bRefresh))
                    .add(bTabAction))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(lHowManyDownload, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {lHowManyDownload, lHowManySelected}, org.jdesktop.layout.GroupLayout.VERTICAL);

    }// </editor-fold>//GEN-END:initComponents
    
private void bAddLocallyDownloadsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bAddLocallyDownloadsActionPerformed
       try {
        table.setEnabled(false);
        bAddLocallyDownloads.setEnabled(false);
        bTabAction.setEnabled(false);   
        final List<UnitCategory> categories = new ArrayList<UnitCategory>();
        final Runnable addUpdates = new Runnable(){
            public void run() {
                LocalDownloadSupport lDSupport = getLocalDownloadSupport();
                lDSupport.selectNbmFiles();
                List<UpdateUnit> units = lDSupport.getUpdateUnits();
                categories.addAll(Utilities.makeAvailableCategories(units, true));
                categories.addAll(Utilities.makeUpdateCategories(units, true));
                for (UnitCategory c : categories) {
                    for (Unit u : c.getUnits()) {
                        if (! u.isMarked()) {
                            u.setMarked(true);
                        }
                    }
                }
                
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        model.setData(categories);
                        model.fireUpdataUnitChange();
                    }
                });
            }
        };
    RequestProcessor.getDefault().post(addUpdates);
    } finally {
        table.setEnabled(true);
        bAddLocallyDownloads.setEnabled(true);
        bTabAction.setEnabled(model.getMarkedUnits().size() > 0);
        fireUpdataUnitChange();
    }
    
}//GEN-LAST:event_bAddLocallyDownloadsActionPerformed

private void bTabActionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bTabActionActionPerformed
    if (UnitCategoryTableModel.Type.UPDATE == model.getType()) {
        OperationContainer<InstallSupport> cont = Containers.forUpdate();
        try {
            table.setEnabled(false);
            bRefresh.setEnabled(false);
            bTabAction.setEnabled(false);            
            new InstallUnitWizard ().invokeWizard (cont);
        } finally {
            table.setEnabled(true);
            bRefresh.setEnabled(true);
            bTabAction.setEnabled(true);                                    
            cont.removeAll();
            fireUpdataUnitChange();
        }
    } else if (UnitCategoryTableModel.Type.AVAILABLE == model.getType()) {
        OperationContainer<InstallSupport> cont = Containers.forAvailable();
        try {
            table.setEnabled(false);
            bRefresh.setEnabled(false);
            bTabAction.setEnabled(false);                        
            new InstallUnitWizard ().invokeWizard (cont);
        } finally {
            table.setEnabled(true);
            bRefresh.setEnabled(true);
            bTabAction.setEnabled(true);                                                
            cont.removeAll();
            fireUpdataUnitChange();
        }
    } else if (UnitCategoryTableModel.Type.INSTALLED == model.getType()) {
        UninstallUnitWizard wizard = new UninstallUnitWizard ();
        try {
            table.setEnabled(false);
            bRefresh.setEnabled(false);
            bTabAction.setEnabled(false);                        
            wizard.invokeWizard ();            
        } finally {
            table.setEnabled(true);
            bRefresh.setEnabled(true);
            bTabAction.setEnabled(true);                                                
            Containers.forUninstall().removeAll();
            fireUpdataUnitChange();
        }        
    } else if (UnitCategoryTableModel.Type.LOCAL == model.getType()) {
        int available = Containers.forAvailableNbms().listAll().size();
        int updates = Containers.forUpdateNbms().listAll().size();        
        OperationContainer<InstallSupport> cont = (updates > available) ? Containers.forUpdateNbms() : Containers.forAvailableNbms();
        
        try {
            //nonsense condition - but wizard can't do both operations at once NOW
            new InstallUnitWizard ().invokeWizard (cont);
        } finally {
            cont.removeAll();
            List<UnitCategory> categories = model.data;
            LocalDownloadSupport lDSupport = getLocalDownloadSupport();
            for (Iterator<UnitCategory> categoryIt = categories.iterator(); categoryIt.hasNext();) {
                UnitCategory unitCategory = categoryIt.next();
                List<Unit> units = unitCategory.getUnits();
                for (Iterator<Unit> it = units.iterator(); it.hasNext();) {
                    Unit unit = it.next();
                    if (unit.updateUnit.getInstalled() != null) {
                        lDSupport.remove(unit.updateUnit);
                    }
                }
            }
            model.setData(categories);
            refresh(false);
            //fireUpdataUnitChange();
        }
    }
}//GEN-LAST:event_bTabActionActionPerformed


private void bRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRefreshActionPerformed
    refresh (true);
}//GEN-LAST:event_bRefreshActionPerformed

private LocalDownloadSupport getLocalDownloadSupport () {     
    LocalDownloadSupport localDownloadSupport = null; 
    if (model instanceof LocallyDownloadedTableModel) {         
        localDownloadSupport = ((LocallyDownloadedTableModel)model).getLocalDownloadSupport();
    }
    return localDownloadSupport;
}

private void refresh (final boolean force) {
    table.setEnabled(false);
    bRefresh.setEnabled(false);
    bTabAction.setEnabled(false);
    
    final Runnable checkUpdates = new Runnable(){
        public void run() {
            manager.initTask.waitFinished();
            Utilities.presentRefreshProviders (manager, force);
            SwingUtilities.invokeLater(new Runnable () {
                public void run() {
                    fireUpdataUnitChange();
                    table.setEnabled(true);
                    bRefresh.setEnabled(true);
                    bTabAction.setEnabled(model.getMarkedUnits().size() > 0);
                }
            });
        }
    };
    NetworkProblemPanel.setPerformAgain(checkUpdates);
    
    Utilities.startAsWorkerThread(checkUpdates);
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAddLocallyDownloads;
    private javax.swing.JButton bRefresh;
    private javax.swing.JButton bTabAction;
    private javax.swing.JLabel lHowManyDownload;
    private javax.swing.JLabel lHowManySelected;
    private javax.swing.JLabel lSearch;
    private javax.swing.JLabel lTabActionDescription;
    private javax.swing.JScrollPane spUnitTable;
    private javax.swing.JTextField tfSearch;
    // End of variables declaration//GEN-END:variables
    
}
