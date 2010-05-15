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
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.model.DBMetaDataFactory;
import org.netbeans.modules.edm.model.SQLDBModel;
import org.netbeans.modules.edm.model.SQLDBTable;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.editor.utils.SQLObjectUtil;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author Nithya Radhakrishnan
 * @version $Revision$
 */
public class RemountCollaborationAction extends AbstractAction {

    private MashupDataObject mDataObj;
    private SQLObject sqlObj;
    private boolean isTableOnly = false;
    
    public RemountCollaborationAction(MashupDataObject dObj, boolean isTableOnly, SQLObject tableObj) {
        super(NbBundle.getMessage(RemountCollaborationAction.class, "LBL_Remount_Tables"));
        this.mDataObj = dObj;
        this.isTableOnly = isTableOnly;
        this.sqlObj = tableObj;

        //action tooltip
        this.putValue(Action.SHORT_DESCRIPTION,NbBundle.getMessage(RemountCollaborationAction.class, "TOOLTIP_Drops_and_re-creates_all_the_tables."));
    }

    /**
     * called when this action is performed in the ui
     *
     * @param ev event
     */
    public void actionPerformed(ActionEvent ev) {
        if(this.isTableOnly) {
            remountTable((SQLDBTable)this.sqlObj);
        } else {
            remountAllCollabTables();
        }
    }
    
    private void remountTable(SQLDBTable aTable) {
        try {
            SQLDBModel dbmodel = (SQLDBModel) aTable.getParent();
            if (dbmodel.getETLDBConnectionDefinition().getDBType().equals(DBMetaDataFactory.AXION) ||
                    dbmodel.getETLDBConnectionDefinition().getDBType().equalsIgnoreCase("Internal")) {
                SQLObjectUtil.dropTable(aTable, (SQLDBModel) aTable.getParent());
                SQLObjectUtil.createTable(aTable, (SQLDBModel) aTable.getParent());
                SQLObjectUtil.setOrgProperties(aTable);
            }
            mDataObj.setModified(true);
            mDataObj.getModel().setDirty(true);
            mDataObj.getMashupDataEditorSupport().synchDocument();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(RemountCollaborationAction.class, "MSG_Unable_to_remount") + ex.getMessage());
        }
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(RemountCollaborationAction.class, "MSG_Remount_Table") + aTable.getName() + NbBundle.getMessage(RemountCollaborationAction.class, "MSG_Success"));
    }
    
    private void remountAllCollabTables() {
        Iterator sourceTables = mDataObj.getModel().getSQLDefinition().getSourceTables().iterator();
        while (sourceTables.hasNext()) {
            SQLDBTable table = (SQLDBTable) sourceTables.next();
            remountTable(table);
        }
        StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(RemountCollaborationAction.class, "MSG_Successfully_remounted"));
    }
}