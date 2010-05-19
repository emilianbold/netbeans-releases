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

import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.editor.graph.MashupGraphManager;
import java.util.logging.Logger;
import org.openide.util.NbBundle;

/**
 * This class implements the popup provider for the scene.
 * @author karthikeyan s
 */
public class ScenePopupProvider implements PopupMenuProvider {

    private MashupDataObject mObj;
    private MashupGraphManager manager;
    private static final Logger mLogger = Logger.getLogger(ScenePopupProvider.class.getName());

    private static URL remountImgUrl = ScenePopupProvider.class.getResource("/org/netbeans/modules/edm/editor/resources/redo.png");
    private static URL syncImgUrl = ScenePopupProvider.class.getResource("/org/netbeans/modules/edm/editor/resources/refresh.png");

    public ScenePopupProvider(MashupDataObject dObj, MashupGraphManager manager) {
        mObj = dObj;
        this.manager = manager;
    }

    public JPopupMenu getPopupMenu(Widget widget, Point point) {
        JPopupMenu menu = new JPopupMenu();

        // add auto Expand all action.
        JMenuItem expandall = new JMenuItem(NbBundle.getMessage(ScenePopupProvider.class, "LBL_Expand_All_Widgets"));
        expandall.setAction(new ExpandAllAction(mObj, NbBundle.getMessage(ScenePopupProvider.class, "LBL_Expand_All_Widgets")));
        menu.add(expandall);

        // add auto Collapse all action.
        JMenuItem collapseall = new JMenuItem(NbBundle.getMessage(ScenePopupProvider.class, "LBL_Collapse_All_Widgets"));
        collapseall.setAction(new CollapseAllAction(mObj, NbBundle.getMessage(ScenePopupProvider.class, "LBL_Collapse_All_Widgets")));
        menu.add(collapseall);

        // add auto Toggle output action.
        JMenuItem output = new JMenuItem(NbBundle.getMessage(ScenePopupProvider.class, "LBL_Toggle_Output"));
        output.setAction(new ShowOutputAction(NbBundle.getMessage(ScenePopupProvider.class, "LBL_Toggle_Output"), mObj));
        menu.add(output); 
        
        // add Refresh action.
        JMenuItem syncItem = new JMenuItem(NbBundle.getMessage(ScenePopupProvider.class, "LBL_Refresh_Metadata"), new ImageIcon(syncImgUrl));
        syncItem.setMnemonic('R');
        syncItem.setToolTipText(NbBundle.getMessage(ScenePopupProvider.class, "LBL_Refresh_Metadata"));
        syncItem.addActionListener(new RefreshMetadataAction(mObj, false, manager.mapWidgetToObject(widget)));
        menu.add(syncItem);
        
        // add remount action.
        JMenuItem remountItem = new JMenuItem(NbBundle.getMessage(ScenePopupProvider.class, "LBL_RemountTables"), new ImageIcon(remountImgUrl));
        remountItem.setMnemonic('m');
        remountItem.setToolTipText(NbBundle.getMessage(ScenePopupProvider.class, "TOOLTIP_Drops_and_re-creates_all_the_tables."));
        remountItem.addActionListener(new RemountCollaborationAction(mObj, false, manager.mapWidgetToObject(widget)));
        menu.add(remountItem);

        // add auto Add Table action.
        JMenuItem addtable = new JMenuItem(NbBundle.getMessage(ScenePopupProvider.class, "LBL_Add_Table"));
        addtable.setAction(new AddTableAction(mObj, NbBundle.getMessage(ScenePopupProvider.class, "LBL_Add_Table")));
        menu.add(addtable);  
        
        menu.addSeparator();

        // add edit join view action.
        JMenuItem edit = new JMenuItem(NbBundle.getMessage(ScenePopupProvider.class, "LBL_Edit_Join"));
        edit.setAction(new EditJoinAction(mObj, NbBundle.getMessage(ScenePopupProvider.class, "LBL_Edit_Join")));
        menu.add(edit);

        // Edit connection action.
        JMenuItem editDB = new JMenuItem(NbBundle.getMessage(ScenePopupProvider.class, "LBL_Database_Properties"));
        editDB.setAction(new EditConnectionAction(mObj,NbBundle.getMessage(ScenePopupProvider.class, "LBL_Database_Properties")));
        menu.add(editDB);   

        // Edit Runtime input action.
        JMenuItem editRuntime = new JMenuItem(NbBundle.getMessage(ScenePopupProvider.class, "LBL_Runtime_Input_Arguments"));
        editRuntime.setAction(new RuntimeInputAction(mObj, NbBundle.getMessage(ScenePopupProvider.class, "LBL_Runtime_Input_Arguments")));
        menu.add(editRuntime);

        menu.addSeparator();

        //add fit to page action
        JMenuItem fitpage = new JMenuItem(NbBundle.getMessage(ScenePopupProvider.class, "LBL_Fit_to_Page"));
        fitpage.setAction(new FitToPageAction(mObj, NbBundle.getMessage(ScenePopupProvider.class, "LBL_Fit_to_Page")));
        menu.add(fitpage);

        //add fit to Width action
        JMenuItem fitWidth = new JMenuItem(NbBundle.getMessage(ScenePopupProvider.class, "LBL_Fit_to_Width"));
        fitWidth.setAction(new FitToWidthAction(mObj, NbBundle.getMessage(ScenePopupProvider.class, "LBL_Fit_to_Width")));
        menu.add(fitWidth);

        //add fit to Height action
        JMenuItem fitHeight = new JMenuItem(NbBundle.getMessage(ScenePopupProvider.class, "LBL_Fit_to_Height"));
        fitHeight.setAction(new FitToHeightAction(mObj, NbBundle.getMessage(ScenePopupProvider.class, "LBL_Fit_to_Height")));
        menu.add(fitHeight);
        
        menu.addSeparator();

        //add Zoom In action
        JMenuItem zoomIn = new JMenuItem(NbBundle.getMessage(ScenePopupProvider.class, "LBL_Zoom_In"));
        zoomIn.setAction(new ZoomInAction(mObj, NbBundle.getMessage(ScenePopupProvider.class, "LBL_Zoom_In")));
        menu.add(zoomIn);


        //add Zoom Out action
        JMenuItem zoomOut = new JMenuItem(NbBundle.getMessage(ScenePopupProvider.class, "LBL_Zoom_Out"));
        zoomOut.setAction(new ZoomOutAction(mObj, NbBundle.getMessage(ScenePopupProvider.class, "LBL_Zoom_Out")));
        menu.add(zoomOut);
        
        menu.addSeparator();

        // add auto layout action.
        JMenuItem layout = new JMenuItem(NbBundle.getMessage(ScenePopupProvider.class, "LBL_Auto_Layout"));
        layout.setAction(new AutoLayoutAction(mObj, NbBundle.getMessage(ScenePopupProvider.class, "LBL_Auto_Layout")));
        menu.add(layout);
        

        // add Validate action.
        JMenuItem validate = new JMenuItem(NbBundle.getMessage(ScenePopupProvider.class, "LBL_Validate"));
        validate.setAction(new ValidationAction(mObj, NbBundle.getMessage(ScenePopupProvider.class, "LBL_Validate")));
        menu.add(validate);
        
        // add run action.
        JMenuItem run = new JMenuItem(NbBundle.getMessage(ScenePopupProvider.class, "LBL_Run"));
        run.setAction(new TestRunAction(mObj, NbBundle.getMessage(ScenePopupProvider.class, "LBL_Run")));
        menu.add(run);
        
        menu.addSeparator();
        
        //add Properties action
        JMenuItem properties = new JMenuItem(NbBundle.getMessage(ScenePopupProvider.class, "LBL_Properties"));
        properties.setAction(new PropertiesAction(mObj, NbBundle.getMessage(ScenePopupProvider.class, "LBL_Properties"), manager.mapWidgetToObject(widget)));
        menu.add(properties);

        return menu;
    }
}