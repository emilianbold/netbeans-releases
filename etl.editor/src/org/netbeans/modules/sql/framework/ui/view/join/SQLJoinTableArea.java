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
package org.netbeans.modules.sql.framework.ui.view.join;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLSourceTableArea;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

import com.sun.sql.framework.exception.BaseException;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class SQLJoinTableArea extends SQLSourceTableArea {

    /** Creates a new instance of SQLJoinTableArea */
    public SQLJoinTableArea(SQLDBTable table) {
        super(table);
    }

    public void setShowHeader(boolean show) {
        super.setShowHeader(show);
    }

    protected void Remove_ActionPerformed(ActionEvent e) {
        SourceTable sTable = (SourceTable) this.getDataObject();
        JoinViewGraphNode joinViewNode = (JoinViewGraphNode) this.getParent();
        if (joinViewNode != null && sTable != null) {
            SQLJoinView joinView = (SQLJoinView) joinViewNode.getDataObject();

            if (joinView.getSourceTables().size() <= 2) {
                NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(SQLJoinTableArea.class, "ERROR_msg_join_remove_minimum_tables",
                    sTable.getName()), NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return;
            }

            try {
                if (joinViewNode.isTableColumnMapped(sTable)) {
                    NotifyDescriptor d = new NotifyDescriptor.Confirmation(NbBundle.getMessage(SQLJoinTableArea.class, "MSG_join_remove_is_mapped",
                        sTable.getName()), NotifyDescriptor.WARNING_MESSAGE);
                    Object response = DialogDisplayer.getDefault().notify(d);
                    if (response.equals(NotifyDescriptor.OK_OPTION)) {
                        joinViewNode.removeTable(sTable);
                    }
                } else {
                    joinViewNode.removeTable(sTable);
                }
            } catch (BaseException ex) {
                NotifyDescriptor d = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            }
        }
    }

    /**
     * Extends parent implementation to signal this table's enclosing join view that it
     * should also update itself.
     * 
     * @param e ActionEvent to be handled
     * @return true if column visibilities were updated; false otherwise
     */
    protected boolean selectVisibleColumnsActionPerformed(ActionEvent e) {
        boolean response = super.selectVisibleColumnsActionPerformed(e);
        if (response) {
            IGraphNode parent = this.getParentGraphNode();
            if (parent instanceof JoinViewGraphNode) {
                JoinViewGraphNode joinNode = (JoinViewGraphNode) parent;
                joinNode.setHeight(joinNode.getMaximumHeight());
                joinNode.layoutChildren();
            }
        }

        return response;
    }

    /**
     * Gets the parent node.
     * 
     * @return parent
     */
    public IGraphNode getParentGraphNode() {
        return (IGraphNode) this.getParent();
    }


    /**
     * is this node can be deleted
     * 
     * @return true if node can be deleted
     */
    public boolean isDeleteAllowed() {
        Remove_ActionPerformed(null);
        return false;
    }

    /**
     * get a list of all input and output links
     * 
     * @return list of input links
     */
    public List getAllLinks() {
        ArrayList links = new ArrayList();
        links.addAll(super.getAllLinks());

        JoinViewGraphNode joinNode = (JoinViewGraphNode) this.getParentGraphNode();
        if (joinNode != null) {
            Iterator it = joinNode.getAllTableAreas().iterator();
            while (it.hasNext()) {
                SQLJoinTableArea tableArea1 = (SQLJoinTableArea) it.next();
                if (tableArea1 != this) {
                    links.addAll(tableArea1.getTableLinks());
                }
            }
        }

        return links;
    }

    public List getTableLinks() {
        return super.getAllLinks();
    }
}

