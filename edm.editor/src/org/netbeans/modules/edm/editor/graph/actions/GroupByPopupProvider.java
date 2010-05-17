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
import java.util.HashMap;
import java.util.List;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.model.impl.SQLGroupByImpl;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.Anchor.Entry;
import org.netbeans.modules.edm.editor.widgets.EDMConnectionWidget;
import org.netbeans.modules.edm.editor.widgets.EDMNodeAnchor;
import org.netbeans.modules.edm.editor.widgets.EDMNodeWidget;
import org.openide.util.NbBundle;

/**
 * This class implements the popup provider for the group by operator.
 * @author karthikeyan s
 */
public class GroupByPopupProvider implements PopupMenuProvider {

    private SQLGroupByImpl grpby;
    private MashupDataObject mObj;

    /*
     *  Creates an instance of groupby popup provider
     */
    public GroupByPopupProvider(SQLGroupByImpl op, MashupDataObject dObj) {
        grpby = op;
        this.mObj = dObj;
    }

    /*
     * return the popup menu for this widget type.
     */
    public JPopupMenu getPopupMenu(Widget widget, Point point) {
        JPopupMenu menu = new JPopupMenu();

        // add show sql action.
        JMenuItem showSQL = new JMenuItem(NbBundle.getMessage(GroupByPopupProvider.class, "LBL_Show_SQL"));
        showSQL.setAction(new ShowSqlAction(grpby, mObj, NbBundle.getMessage(GroupByPopupProvider.class, "LBL_Show_SQL")));
        menu.add(showSQL);

        // add edit having condition action.
        JMenuItem editHavingCondition = new JMenuItem(NbBundle.getMessage(GroupByPopupProvider.class, "LBL_Having_Condition"));
        editHavingCondition.setAction(new EditHavingConditionAction(mObj, grpby, NbBundle.getMessage(GroupByPopupProvider.class, "LBL_Having_Condition")));
        menu.add(editHavingCondition);

        menu.addSeparator();
        
        JMenuItem removeGrpBy = new JMenuItem(NbBundle.getMessage(GroupByPopupProvider.class, "LBL_Remove_GroupBy"));
        removeGrpBy.setAction(new RemoveGroupbyAction(mObj, grpby, widget, NbBundle.getMessage(GroupByPopupProvider.class, "LBL_Remove_GroupBy")));
        menu.add(removeGrpBy);

        EDMNodeWidget nd = (EDMNodeWidget) widget;
        HashMap<EDMNodeWidget, Anchor> edgesMap = mObj.getGraphManager().getScene().getEdgesMap();
        if (edgesMap != null) {
            EDMNodeAnchor anchor = (EDMNodeAnchor) edgesMap.get(nd);
            if (nd.getNodeName().trim().equals("Group By")) {
                List<Anchor.Entry> entries = anchor.getEntries();
                for (Entry entry : entries) {
                    EDMNodeWidget edmWidget2 = (EDMNodeWidget) entry.getOppositeAnchor().getRelatedWidget();
                    if (edmWidget2.getNodeName().trim().equalsIgnoreCase("ROOT JOIN")) {
                        removeGrpBy.setEnabled(false);
                    } else {
                        removeGrpBy.setEnabled(true);
                    }
                }
            }
        }
        return menu;
    }
}