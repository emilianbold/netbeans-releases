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
import java.awt.Component;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author  Jiri Rechtacek
 */
public class PluginManagerUI extends javax.swing.JPanel implements UpdateUnitListener {
    private List<UpdateUnit> units = Collections.emptyList ();
    private UnitTable installedTable;
    private UnitTable availableTable;
    private UnitTable updateTable;
    private UnitTable localTable;
    private JButton closeButton;
    private SplittedUnitTab settingTab;
    final  RequestProcessor.Task initTask;
    
    
    /** Creates new form PluginManagerUI */
    public PluginManagerUI (JButton closeButton) {
        this.closeButton = closeButton;
        initComponents();
        postInitComponents();
        //start initialize method as soon as possible
        initTask = Utilities.startAsWorkerThread(new Runnable() {
            public void run() {
                initialize();
            }
        });        
    }

    private Window findWindowParent() {
        Component c = this;
        while(c != null) {
            c = c.getParent();
            if (c instanceof Window) {
                return (Window)c;
            }
        }
        return null;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        //show progress for initialize method
        final Window w = findWindowParent();
        if (w != null) {
            w.addWindowListener(new WindowAdapter(){
                public void windowOpened(WindowEvent e) {
                    final WindowAdapter waa = this;
                    Utilities.startAsWorkerThread(PluginManagerUI.this,new Runnable() {
                        public void run() {
                            initTask.waitFinished();
                            w.removeWindowListener(waa);
                        }
                    }, NbBundle.getMessage(PluginManagerUI.class, "UnitTab_InitAndCheckingForUpdates"));
                }
            });
        }                
    }

    
    @Override
    public void removeNotify() {
        super.removeNotify();
        unitilialize();
    }

    public void initialize() {
        try {   
            units = UpdateManager.getDefault().getUpdateUnits();
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    refreshUnits();
                }
            });
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    //workaround of #96282 - Memory leak in org.netbeans.core.windows.services.NbPresenter     
    public void unitilialize() {
        Utilities.startAsWorkerThread(new Runnable() {
            public void run() {
                //ensures that uninitialization runs after initialization
                initTask.waitFinished();
                //ensure exclusivity between this uninitialization code and refreshUnits (which can run even after this dialog is disposed)
                synchronized(initTask) {
                    units = null;
                    installedTable = null;
                    availableTable = null;
                    updateTable = null;
                    localTable = null;
                    settingTab = null;                    
                }
            }
        }, 10000);
    }
    
    void setProgressComponent (final JLabel detail, final JComponent progressComponent) {
        if (SwingUtilities.isEventDispatchThread ()) {
            setProgressComponentInAwt (detail, progressComponent);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    setProgressComponentInAwt (detail, progressComponent);
                    }
                });
            }
        }
    
    private void setProgressComponentInAwt (JLabel detail, JComponent progressComponent) {
        assert pProgress != null;
        assert SwingUtilities.isEventDispatchThread () : "Must be called in EQ.";
        pProgress.setVisible (true);
        pProgress.add (detail, BorderLayout.CENTER);
        pProgress.add (progressComponent, BorderLayout.EAST);
        revalidate ();
    }
    
    void unsetProgressComponent (final JLabel detail, final JComponent progressComponent) {
        if (SwingUtilities.isEventDispatchThread ()) {
            unsetProgressComponentInAwt (detail, progressComponent);
        } else {
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    unsetProgressComponentInAwt (detail, progressComponent);
                }
            });
        }
    }
    
    private void unsetProgressComponentInAwt (JLabel detail, JComponent progressComponent) {
        assert pProgress != null;
        assert SwingUtilities.isEventDispatchThread () : "Must be called in EQ.";
        pProgress.remove (detail);
        pProgress.remove (progressComponent);
        pProgress.setVisible (false);
        revalidate ();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        tpTabs = new javax.swing.JTabbedPane();
        pProgress = new javax.swing.JPanel();
        bClose = closeButton;

        tpTabs.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tpTabsStateChanged(evt);
            }
        });

        pProgress.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(bClose, org.openide.util.NbBundle.getMessage(PluginManagerUI.class, "UnitTab_bClose_Text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(pProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 562, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 121, Short.MAX_VALUE)
                        .add(bClose))
                    .add(tpTabs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(tpTabs, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 471, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(bClose)
                    .add(pProgress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

private void tpTabsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tpTabsStateChanged
    if (((JTabbedPane) evt.getSource ()).getSelectedComponent ().equals (settingTab)) {
        settingTab.refreshState ();
    }
}//GEN-LAST:event_tpTabsStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bClose;
    private javax.swing.JPanel pProgress;
    private javax.swing.JTabbedPane tpTabs;
    // End of variables declaration//GEN-END:variables
    
    private void postInitComponents () {
        Containers.initNotify();        
        installedTable = new UnitTable (new InstalledTableModel (units));
        updateTable = new UnitTable (new UpdateTableModel (units));
        availableTable = new UnitTable (new AvailableTableModel (units));
        localTable = new UnitTable (new LocallyDownloadedTableModel (units));
        selectFirstRow(installedTable);
        selectFirstRow(updateTable);
        selectFirstRow(availableTable);

        SplittedUnitTab updateTab = new SplittedUnitTab(updateTable, new UnitDetails (), this);
        updateTab.addUpdateUnitListener (this);
        tpTabs.add (NbBundle.getMessage(PluginManagerUI.class, "PluginManagerUI_UnitTab_Update_Title"), updateTab);
        
        SplittedUnitTab availableTab = new SplittedUnitTab(availableTable, new UnitDetails (), this);
        availableTab.addUpdateUnitListener (this);
        tpTabs.add (NbBundle.getMessage(PluginManagerUI.class, "PluginManagerUI_UnitTab_Available_Title"), availableTab);
                
        SplittedUnitTab localTab = new SplittedUnitTab(localTable, new UnitDetails (), this);
        localTab.addUpdateUnitListener (this);
        tpTabs.add (NbBundle.getMessage(PluginManagerUI.class, "PluginManagerUI_UnitTab_Local_Title"), localTab);
        
        SplittedUnitTab installedTab = new SplittedUnitTab(installedTable, new UnitDetails (), this);
        installedTab.addUpdateUnitListener (this);
        tpTabs.add (NbBundle.getMessage(PluginManagerUI.class, "PluginManagerUI_UnitTab_Installed_Title"), installedTab);
        
        SettingsTab tab = new SettingsTab(this, new DetailsPanel());
        settingTab = new SplittedUnitTab (tab, tab.details);
        tpTabs.add (tab.getDisplayName(), settingTab);
        
        decorateTitle (0, updateTable, NbBundle.getMessage (PluginManagerUI.class, "PluginManagerUI_UnitTab_Update_Title"));
        decorateTitle (1, availableTable, NbBundle.getMessage (PluginManagerUI.class, "PluginManagerUI_UnitTab_Available_Title"));
        decorateTitle (2, localTable, NbBundle.getMessage (PluginManagerUI.class, "PluginManagerUI_UnitTab_Local_Title"));
        decorateTitle (3, installedTable, NbBundle.getMessage (PluginManagerUI.class, "PluginManagerUI_UnitTab_Installed_Title"));
    }
    
    private void decorateTitle (int index, JTable table, String originalName) {
        TableModel model = table.getModel ();
        assert model instanceof UnitCategoryTableModel : model + " is instanceof UnitCategoryTableModel.";
        UnitCategoryTableModel catModel = (UnitCategoryTableModel) model;
        int count = catModel.getItemCount ();
        int rawCount = catModel.getRawItemCount ();        
        String countInfo = (count == rawCount) ? String.valueOf(rawCount) :
            NbBundle.getMessage (PluginManagerUI.class, "PluginManagerUI_Tabs_CountFormat", count, rawCount);
        String newName = NbBundle.getMessage (PluginManagerUI.class, "PluginManagerUI_Tabs_NameFormat", originalName, countInfo);
        tpTabs.setTitleAt (index, count == 0 ? originalName : newName);
    }

    private int findRowWithFirstUnit(UnitCategoryTableModel model) {
        for (int row = 0; row <= model.getRowCount(); row++) {
            if (model.getUnitAtRow(row) != null) {
                return row;
            }
        }
        return -1;
    }
    
    private void selectFirstRow(UnitTable table) {
        if (table.getSelectedRow() == -1) {
            UnitCategoryTableModel model = (UnitCategoryTableModel)table.getModel();
            int row = findRowWithFirstUnit(model);
            if (row != -1) {
                table.getSelectionModel().setSelectionInterval(row, row);
            }
        }        
    }

    private void refreshUnits () {        
        //ensure exclusivity between this refreshUnits code(which can run even after this dialog is disposed) and uninitialization code
        synchronized(initTask) {
            //return immediatelly if uninialization(after removeNotify) was alredy called
            if (units == null) return;
            //TODO: REVIEW THIS CODE - problem is that is called from called from AWT thread
            //UpdateManager.getDefault().getUpdateUnits() should never be called fromn AWT because it may cause 
            //long terming starvation because in fact impl. of this method calls AutoUpdateCatalogCache.getCatalogURL
            //which is synchronized and may wait until cache is created
            //even more AutoUpdateCatalog.getUpdateItems () can at first start call refresh and thus writeToCache again
            units = UpdateManager.getDefault().getUpdateUnits();
            UnitCategoryTableModel installTableModel = ((UnitCategoryTableModel)installedTable.getModel());
            UnitCategoryTableModel updateTableModel = ((UnitCategoryTableModel)updateTable.getModel());
            UnitCategoryTableModel availableTableModel = ((UnitCategoryTableModel)availableTable.getModel());
            UnitCategoryTableModel localTableModel = ((UnitCategoryTableModel)localTable.getModel());
            
            updateTableModel.setUnits(units);
            installTableModel.setUnits(units);
            availableTableModel.setUnits(units);            
            selectFirstRow(installedTable);
            selectFirstRow(updateTable);
            selectFirstRow(availableTable);
            decorateTitle(0, updateTable, NbBundle.getMessage(PluginManagerUI.class, "PluginManagerUI_UnitTab_Update_Title"));
            decorateTitle(1, availableTable, NbBundle.getMessage(PluginManagerUI.class, "PluginManagerUI_UnitTab_Available_Title"));
            decorateTitle(2, localTable, NbBundle.getMessage(PluginManagerUI.class, "PluginManagerUI_UnitTab_Local_Title"));
            decorateTitle(3, installedTable, NbBundle.getMessage(PluginManagerUI.class, "PluginManagerUI_UnitTab_Installed_Title"));            
        }                
    }
        
    
    static boolean canContinue (String message) {
        return NotifyDescriptor.YES_OPTION.equals (DialogDisplayer.getDefault ().notify (new NotifyDescriptor.Confirmation (message)));
    }
    //TODO: all the request for refresh should be cancelled if there is already one such running refresh task    
    public void updateUnitsChanged() {
        refreshUnits ();
    }
    
    public void buttonsChanged () {
        Component c = tpTabs.getSelectedComponent ();
        if (c instanceof SplittedUnitTab) {
            ((SplittedUnitTab) c).refreshState ();
        }
    }
}
