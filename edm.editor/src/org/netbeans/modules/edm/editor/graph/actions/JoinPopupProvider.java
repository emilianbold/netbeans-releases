/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */
package org.netbeans.modules.edm.editor.graph.actions;

import java.awt.Point;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.edm.model.SQLJoinOperator;
import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import java.util.logging.Logger;
import org.netbeans.modules.edm.editor.widgets.EDMNodeWidget;
import org.openide.util.NbBundle;

/**
 * This class implements the popup provider for the join.
 * @author karthikeyan s
 */
public class JoinPopupProvider implements PopupMenuProvider {

    private SQLJoinOperator joinOp;
    private MashupDataObject mObj;
    private static final Logger mLogger = Logger.getLogger(JoinPopupProvider.class.getName());
    private static URL unJoin = JoinPopupProvider.class.getResource("/org/netbeans/modules/edm/editor/resources/ungroup_table.png");
    /*
     * Creates an instance of join popup provider.
     */

    public JoinPopupProvider(SQLJoinOperator op, MashupDataObject dObj) {
        joinOp = op;
        this.mObj = dObj;
    }

    /*
     * return the popup menu for this widget type.
     */
    public JPopupMenu getPopupMenu(Widget widget, Point point) {
        JPopupMenu menu = new JPopupMenu();

        // add show sql action.
        JMenuItem showData = new JMenuItem(NbBundle.getMessage(JoinPopupProvider.class, "LBL_Show_Data"));
        showData.setAction(new ShowDataAction(mObj, joinOp, NbBundle.getMessage(JoinPopupProvider.class, "LBL_Show_Data")));
        menu.add(showData);

        // add show sql action.
        JMenuItem showSQL = new JMenuItem(NbBundle.getMessage(JoinPopupProvider.class, "LBL_Show_SQL"));
        showSQL.setAction(new ShowSqlAction(joinOp, mObj, NbBundle.getMessage(JoinPopupProvider.class, "LBL_Show_SQL")));
        menu.add(showSQL);

        menu.addSeparator();

        // add edit join condition action.
        JMenuItem editJoinCondition = new JMenuItem(NbBundle.getMessage(JoinPopupProvider.class, "LBL_Edit_Join_Condition"));
        editJoinCondition.setAction(new EditJoinConditionAction(mObj, joinOp, NbBundle.getMessage(JoinPopupProvider.class, "LBL_Edit_Join_Condition")));
        menu.add(editJoinCondition);
        
        JMenuItem unjoinTables = new JMenuItem(NbBundle.getMessage(JoinPopupProvider.class, "LBL_Unjoin_Tables"), new ImageIcon(unJoin));
        unjoinTables.addActionListener(new UnJoinAction(mObj, NbBundle.getMessage(JoinPopupProvider.class, "LBL_Unjoin_Tables")));
        if (!((EDMNodeWidget) widget).getNodeName().trim().equals("JOIN")) {
            menu.add(unjoinTables);
        }

        return menu;
    }
}