/*
 *
 *          Copyright (c) 2003-2005, SeeBeyond Technology Corporation,
 *          All Rights Reserved
 *
 *          This program, and all the routines referenced herein,
 *          are the proprietary properties and trade secrets of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 *          Except as provided for by license agreement, this
 *          program shall not be duplicated, used, or disclosed
 *          without  written consent signed by an officer of
 *          SEEBEYOND TECHNOLOGY CORPORATION.
 *
 */

package org.netbeans.modules.edm.editor.graph.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.model.SQLDBTable;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SourceTable;
import org.netbeans.modules.edm.model.visitors.SQLDBSynchronizationVisitor;
import org.netbeans.modules.edm.editor.graph.jgo.TableConstants;
import org.netbeans.modules.edm.editor.graph.MetaTableModel;
import org.netbeans.modules.edm.model.EDMException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 * @author Nithya Radhakrishnan
 * @version $Revision$
 */
public class RefreshMetadataAction extends AbstractAction {

    private MashupDataObject mDataObj;
    private SQLObject sqlObj;
    private boolean isTableOnly = false;
    
    public RefreshMetadataAction(MashupDataObject dObj, boolean isTableOnly, SQLObject tableObj) {
        super(NbBundle.getMessage(RefreshMetadataAction.class, "LBL_Refresh_Metadata"));
        this.mDataObj = dObj;
        this.isTableOnly = isTableOnly;
        this.sqlObj = tableObj;
    }

    /**
     * called when this action is performed in the ui
     *
     * @param ev event
     */
    public void actionPerformed(ActionEvent ev) {
        if(this.isTableOnly) {
            refreshMetadataForTable();
        } else {
            refreshMetadataForCollaboration();
        }
    }
    
    private void refreshMetadataForTable() {
        //mDataObj.getModel().getSQLDefinition(); mDataObj.getEditorView();
        List infoList = new ArrayList();
        String dlgMsg = NbBundle.getMessage(RefreshMetadataAction.class, "MSG_sync_the_collaboration_table");
        String dlgTitle = NbBundle.getMessage(RefreshMetadataAction.class, "LBL_Refresh_Metadata");
        
        int response = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), dlgMsg, dlgTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (JOptionPane.OK_OPTION == response) {
            try {
                SourceTable st = (SourceTable) this.sqlObj;
                SQLDBSynchronizationVisitor visitView = new SQLDBSynchronizationVisitor();
                MetaTableModel metaModel = new MetaTableModel(st, TableConstants.INPUT_TABLE);
                visitView.mergeCollabTableWithDatabaseTable((SQLDBTable) st, metaModel);
                if (!visitView.infoList.isEmpty()) {
                    // Mark collab as needing to be persisted.
                    mDataObj.setModified(true);
                    mDataObj.getModel().setDirty(true);
                    mDataObj.getMashupDataEditorSupport().synchDocument();
                    infoList.addAll(visitView.infoList);
                }
            } catch (EDMException ex) {
                Exceptions.printStackTrace(ex);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    private void refreshMetadataForCollaboration() {
        mDataObj.getModel().getSQLDefinition();
        mDataObj.getEditorView();
        List infoList = new ArrayList();
        String dlgMsg = NbBundle.getMessage(RefreshMetadataAction.class, "MSG_sync_the_collaboration_table");
        String dlgTitle = NbBundle.getMessage(RefreshMetadataAction.class, "LBL_Refresh_Metadata");
        
        int response = JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), dlgMsg, dlgTitle, JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
        if (JOptionPane.OK_OPTION == response) {
            Iterator sourceTables = mDataObj.getModel().getSQLDefinition().getSourceTables().iterator();
            while (sourceTables.hasNext()) {
                try {
                    SourceTable st = (SourceTable) sourceTables.next();
                    SQLDBSynchronizationVisitor visitView = new SQLDBSynchronizationVisitor();
                    MetaTableModel metaModel = new MetaTableModel(st, TableConstants.INPUT_TABLE);
                    visitView.mergeCollabTableWithDatabaseTable((SQLDBTable) st, metaModel);
                    if (!visitView.infoList.isEmpty()) {
                        // Mark collab as needing to be persisted.
                        mDataObj.setModified(true);
                        mDataObj.getModel().setDirty(true);
                        mDataObj.getMashupDataEditorSupport().synchDocument();
                        infoList.addAll(visitView.infoList);
                    }
                } catch (EDMException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}