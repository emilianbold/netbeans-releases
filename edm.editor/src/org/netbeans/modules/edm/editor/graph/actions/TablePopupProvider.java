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
import org.netbeans.modules.edm.editor.graph.MashupGraphManager;
import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.model.DBMetaDataFactory;
import org.netbeans.modules.edm.model.SourceTable;
import org.netbeans.modules.edm.model.impl.SQLDBModelImpl;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * This class implements the popup provider for the table.
 * @author karthikeyan s
 */
public class TablePopupProvider implements PopupMenuProvider {

    private SQLObject obj;
    private MashupGraphManager manager;
    private MashupDataObject mObj;
    private static URL propertiesUrl = TablePopupProvider.class.getResource("/org/netbeans/modules/edm/editor/resources/properties.png");
    private static URL remountImgUrl = TablePopupProvider.class.getResource("/org/netbeans/modules/edm/editor/resources/redo.png");
    private static URL syncImgUrl = TablePopupProvider.class.getResource("/org/netbeans/modules/edm/editor/resources/refresh.png");

    public TablePopupProvider(SQLObject obj, MashupDataObject dObj) {
        this.obj = obj;
        this.manager = dObj.getGraphManager();
        this.mObj = dObj;
    }

    public JPopupMenu getPopupMenu(Widget widget, Point point) {
        JPopupMenu menu = new JPopupMenu();

        // add show sql action.
        JMenuItem showData = new JMenuItem(NbBundle.getMessage(TablePopupProvider.class, "LBL_Show_Data"));
        showData.setAction(new ShowDataAction(mObj, obj, NbBundle.getMessage(TablePopupProvider.class, "LBL_Show_Data")));
        menu.add(showData);

        // add show sql action.
        JMenuItem showSQL = new JMenuItem(NbBundle.getMessage(TablePopupProvider.class, "LBL_Show_SQL"));
        showSQL.setAction(new ShowSqlAction(obj, mObj, NbBundle.getMessage(TablePopupProvider.class, "LBL_Show_SQL")));
        menu.add(showSQL);

        menu.addSeparator();

        // add select columns action.
        JMenuItem selectColumns = new JMenuItem(NbBundle.getMessage(TablePopupProvider.class, "TITLE_Select_Columns"));
        selectColumns.setAction(new SelectColumnsAction(mObj, obj, NbBundle.getMessage(TablePopupProvider.class, "TITLE_Select_Columns")));
        menu.add(selectColumns);

        // add data extraction action
        JMenuItem dataExtraction = new JMenuItem(NbBundle.getMessage(TablePopupProvider.class, "LBL_Filter_Condition"));
        dataExtraction.setAction(new ExtractionConditionAction(mObj, obj, NbBundle.getMessage(TablePopupProvider.class, "LBL_Filter_Condition")));
        menu.add(dataExtraction);

        menu.addSeparator();

        // add remove table action
        JMenuItem remove = new JMenuItem(NbBundle.getMessage(TablePopupProvider.class, "LBL_Remove_Table"));
        remove.setAction(new RemoveObjectAction(mObj, obj, NbBundle.getMessage(TablePopupProvider.class, "LBL_Remove_Table")));
        if (obj instanceof SourceTable) {
            SourceTable srcTbl = (SourceTable) obj;
            if (!srcTbl.isUsedInJoin()) {
                remove.setEnabled(true);
            } else {
                remove.setEnabled(false);
            }
        }
        menu.add(remove);


        JMenuItem syncItem = new JMenuItem(NbBundle.getMessage(TablePopupProvider.class, "LBL_Refresh_Metadata"), new ImageIcon(syncImgUrl));
        syncItem.setMnemonic('R');
        syncItem.addActionListener(new RefreshMetadataAction(mObj, true, obj));
        menu.add(syncItem);

        SQLDBModelImpl impl = (SQLDBModelImpl) obj.getParentObject();
        try {
            if (impl.getETLDBConnectionDefinition().getDBType().equals(DBMetaDataFactory.AXION) || impl.getETLDBConnectionDefinition().getDBType().equalsIgnoreCase("Internal")) {
                JMenuItem remountItem = new JMenuItem(NbBundle.getMessage(TablePopupProvider.class, "LBL_Remount"), new ImageIcon(remountImgUrl));
                remountItem.setMnemonic('R');
                remountItem.addActionListener(new RemountCollaborationAction(mObj, true, obj));
                menu.add(remountItem);
            }
        } catch (EDMException ex) {
            Exceptions.printStackTrace(ex);
        }

        menu.addSeparator();

        JMenuItem properties = new JMenuItem(NbBundle.getMessage(TablePopupProvider.class, "LBL_Properties"), new ImageIcon(propertiesUrl));
        properties.setAction(new PropertiesAction(mObj, NbBundle.getMessage(TablePopupProvider.class, "LBL_Properties"), obj));
        menu.add(properties);

        return menu;
    }
}