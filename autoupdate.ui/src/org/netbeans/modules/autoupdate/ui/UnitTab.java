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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JLabel;
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
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
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
        
        int size = 0;
        
        switch (model.getType()) {
        case INSTALLED :
            break ;
        case UPDATE :
            for (Unit u : units) {
                size += u.getCompleteSize ();
            }
            break;
        case AVAILABLE :
            for (Unit u : units) {
                size += u.getCompleteSize ();
            }
            break;
        case LOCAL :
//            for (Unit u : units) {
//                size += u.getCompleteSize ();
//            }
            break;
        }
        
        
        if (units.size () == 0 || size == 0) {
            cleanHowMany();
        }
        
        bTabAction.setEnabled(units.size() > 0);
        setHowManyDownload(Unit.getSize(size));
        setHowManySelected(units.size());
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
        bTabAction.setEnabled(false);
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
        pProgress = new javax.swing.JPanel();

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

        pProgress.setLayout(new java.awt.BorderLayout());

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(lTabActionDescription, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 416, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(lSearch)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(tfSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 145, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(spUnitTable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 622, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(bRefresh, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 105, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(bAddLocallyDownloads))
                            .add(pProgress, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, lHowManyDownload, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, lHowManySelected, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(bTabAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 111, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(tfSearch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(lSearch)
                    .add(lTabActionDescription))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(spUnitTable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(lHowManySelected, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(bRefresh)
                        .add(bAddLocallyDownloads))
                    .add(bTabAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(lHowManyDownload, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(pProgress, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
private void bAddLocallyDownloadsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bAddLocallyDownloadsActionPerformed
    
    List<UpdateUnit> units = LocalDownloadSupport.getUpdateUnits ();
    List<UnitCategory> categories = new ArrayList<UnitCategory>();
    categories.addAll(Utilities.makeAvailableCategories(units, true));
    categories.addAll(Utilities.makeUpdateCategories(units, true));    
    model.setData (categories);
    fireUpdataUnitChange ();
    
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
            for (Iterator<UnitCategory> categoryIt = categories.iterator(); categoryIt.hasNext();) {
                UnitCategory unitCategory = categoryIt.next();
                List<Unit> units = unitCategory.getUnits();
                for (Iterator<Unit> it = units.iterator(); it.hasNext();) {
                    Unit unit = it.next();
                    if (unit.updateUnit.getInstalled() != null) {
                        it.remove();
                    }
                }
                if (units.size() == 0) {
                    categoryIt.remove();
                }                
            }            
            model.setData (categories);            
            refresh (false);
            //fireUpdataUnitChange();
        }
    }
}//GEN-LAST:event_bTabActionActionPerformed


private void bRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRefreshActionPerformed
    refresh (true);
}//GEN-LAST:event_bRefreshActionPerformed

private void refresh (final boolean force) {
    table.setEnabled(false);
    bRefresh.setEnabled(false);
    bTabAction.setEnabled(false);
    
    final Runnable checkUpdates = new Runnable(){
        public void run() {
            try {
                ProgressHandle handle = ProgressHandleFactory.createHandle ("refresh-providers-handle");
                JComponent progressComp = ProgressHandleFactory.createProgressComponent (handle);
                JLabel progressLabel = new JLabel (NbBundle.getMessage (UnitTab.class, "UnitTab_CheckingForUpdates"));
                manager.setProgressComponent (progressLabel, progressComp);
                UpdateUnitProviderFactory.getDefault ().refreshProviders (handle, force);
                manager.unsetProgressComponent (progressLabel, progressComp);
            } catch (IOException ioe) {
                log.log(Level.FINE, ioe.getMessage(), ioe);
                NetworkProblemPanel.showNetworkProblemDialog();
            } finally {
                SwingUtilities.invokeLater(new Runnable(){
                    public void run() {
                        fireUpdataUnitChange();
                        table.setEnabled(true);
                        bRefresh.setEnabled(true);
                        bTabAction.setEnabled(model.getMarkedUnits().size() > 0);
                    }
                });
            }
        }
    };
    NetworkProblemPanel.setPerformAgain(checkUpdates);
    
    RequestProcessor.Task t = RequestProcessor.getDefault().post(checkUpdates, 100);
}

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bAddLocallyDownloads;
    private javax.swing.JButton bRefresh;
    private javax.swing.JButton bTabAction;
    private javax.swing.JLabel lHowManyDownload;
    private javax.swing.JLabel lHowManySelected;
    private javax.swing.JLabel lSearch;
    private javax.swing.JLabel lTabActionDescription;
    private javax.swing.JPanel pProgress;
    private javax.swing.JScrollPane spUnitTable;
    private javax.swing.JTextField tfSearch;
    // End of variables declaration//GEN-END:variables
    
}
