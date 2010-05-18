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
package org.netbeans.modules.edm.editor.utils;

import java.awt.Image;
import org.openide.util.Utilities;

/**
 *
 * @author karthikeyan s
 */
public interface ImageConstants {
    
    /* CONSTANTS USED FOR GETTING IMAGE ICONS */
    public static final String CONDITION = "CONDITION";
    
    public static final String COLUMN = "COLUMN";
    
    public static final String PROPERTIES = "PROPERTIES";
    
    public static final String FILTER = "FILTER";
    
    public static final String RUNTIMEATTR = "RUNTIMEATTR";
    
    public static final String PRIMARYKEYCOL = "PRIMARYKEYCOL";
    
    public static final String FOREIGNKEYCOL = "FOREIGNKEYCOL";
    
    public static final String FOREIGNKEY = "FOREIGNKEY";
    
    public static final String JOIN = "JOIN";
    
    public static final String TABLE = "TABLE";
    
    public static final String RUNTIMEINPUT = "RUNTIMEINPUT";
    
    public static final String LAYOUT = "LAYOUT";
    
    public static final String COLLAPSEALL = "COLLAPSEALL";
    
    public static final String EDITCONNECTION = "EDITCONNECTION";
    
    public static final String EDITJOIN = "EDITJOIN";
    
    public static final String ADDTABLE = "ADDTABLE";
    
    public static final String JOINCONDITION = "JOINCONDITION";
    
    public static final String EXPANDALL = "EXPANDALL";
    
    public static final String FITTOHEIGHT = "FITTOHEIGHT";
    
    public static final String FITTOPAGE = "FITTOPAGE";
    
    public static final String FITTOWIDTH = "FITTOWIDTH";
    
    public static final String REMOVE = "REMOVE";
    
    public static final String OUTPUT = "OUTPUT";
    
    public static final String SHOW_SQL = "SHOW_SQL";
    
    public static final String RUN = "RUN";
    
    public static final String ZOOMIN = "ZOOMIN";
    
    public static final String ZOOMOUT = "ZOOMOUT";
    
    public static final String VALIDATE = "VALIDATE";
    
    public static final String GROUPBY = "GROUPBY";
    
    public static final String UNJOIN = "UNJOIN";
    
    public static final String REFRESHMETADATA = "REFRESHMETADATA";
    
    public static final String REMOUNT = "REMOUNT";
    
    /* Image objects */
    public static final Image JOIN_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/join_view.png"); // NOI18N
    
    public static final Image TABLE_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/SourceTable.png"); // NOI18N
    
    public static final Image COLUMN_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/column.gif"); // NOI18N
    
    public static final Image PRIMARY_COLUMN_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/columnPrimary.gif"); // NOI18N
    
    public static final Image FOREIGN_COLUMN_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/columnForeign.gif"); // NOI18N
    
    public static final Image CONDITION_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/condition.png"); // NOI18N
    
    public static final Image PROPERTIES_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/properties.png"); // NOI18N
    
    public static final Image FILTER_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/filter16.gif"); // NOI18N
    
    public static final Image RUNTIME_INPUT_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/RuntimeInput.png"); // NOI18N
    
    public static final Image RUNTIME_ATTR_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/columnselection.png"); // NOI18N
    
    public static final Image FOREIGN_KEY_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/foreignKey.gif"); // NOI18N
    
    public static final Image LAYOUT_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/layout.png"); // NOI18N
    
    public static final Image COLLAPSE_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/collapse_all.png"); // NOI18N
    
    public static final Image EDIT_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/DatabaseProperties.png"); // NOI18N
    
    public static final Image EDITJOIN_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/edit_join.png"); // NOI18N
    
    public static final Image ADDTABLE_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/join_view.png");// NOI18N
    
    public static final Image JOINCONDITION_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/system_condition.png"); // NOI18N
    
    public static final Image EXPAND_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/expand_all.png"); // NOI18N
    
    public static final Image FITTOHEIGHT_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/fit_height.png"); // NOI18N
    
    public static final Image FITTOPAGE_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/fit_diagram.png"); // NOI18N
    
    public static final Image FITTOWIDTH_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/fit_width.png"); // NOI18N
    
    public static final Image REMOVE_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/remove.png"); // NOI18N
    
    public static final Image OUTPUT_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/showOutput.png"); // NOI18N
    
    public static final Image SQL_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/Show_Sql.png"); // NOI18N
    
    public static final Image RUN_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/run.png"); // NOI18N
    
    public static final Image ZOOM_IN_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/zoom_in.png"); // NOI18N
    
    public static final Image ZOOM_OUT_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/zoom_out.png"); // NOI18N
    
    public static final Image VALIDATE_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/validation.png"); // NOI18N        
    
    public static final Image GROUPBY_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/groupby.gif"); // NOI18N   
    
    public static final Image UNJOIN_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/ungroup_table.png"); // NOI18N       
    
    public static final Image REFRESHMETADATA_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/refresh.png"); // NOI18N       
    
    public static final Image REMOUNT_IMAGE = Utilities.loadImage(
            "org/netbeans/modules/edm/editor/resources/redo.png"); // NOI18N             
}