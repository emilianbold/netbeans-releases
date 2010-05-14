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

package org.netbeans.modules.edm.editor.utils;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import org.netbeans.modules.edm.model.SQLConstants;

/**
 *
 * @author karthikeyan s
 */
public class MashupGraphUtil {
    
    private static Map<String, Image> imageMap = new HashMap<String, Image>();
    
    static {
        imageMap.put(ImageConstants.COLUMN, ImageConstants.COLUMN_IMAGE);
        imageMap.put(ImageConstants.CONDITION, ImageConstants.CONDITION_IMAGE);
        imageMap.put(ImageConstants.PROPERTIES, ImageConstants.PROPERTIES_IMAGE);
        imageMap.put(ImageConstants.FILTER, ImageConstants.FILTER_IMAGE);
        imageMap.put(ImageConstants.RUNTIMEATTR, ImageConstants.RUNTIME_ATTR_IMAGE);
        imageMap.put(ImageConstants.PRIMARYKEYCOL, ImageConstants.PRIMARY_COLUMN_IMAGE);
        imageMap.put(ImageConstants.FOREIGNKEYCOL, ImageConstants.FOREIGN_COLUMN_IMAGE);
        imageMap.put(ImageConstants.FOREIGNKEY, ImageConstants.FOREIGN_KEY_IMAGE);
        imageMap.put(ImageConstants.JOIN, ImageConstants.JOIN_IMAGE);
        imageMap.put(ImageConstants.RUNTIMEINPUT, ImageConstants.RUNTIME_INPUT_IMAGE);
        imageMap.put(ImageConstants.TABLE, ImageConstants.TABLE_IMAGE);
        imageMap.put(ImageConstants.LAYOUT, ImageConstants.LAYOUT_IMAGE);
        imageMap.put(ImageConstants.COLLAPSEALL, ImageConstants.COLLAPSE_IMAGE);
        imageMap.put(ImageConstants.EDITCONNECTION, ImageConstants.EDIT_IMAGE);
        imageMap.put(ImageConstants.EDITJOIN, ImageConstants.EDITJOIN_IMAGE);
        imageMap.put(ImageConstants.ADDTABLE,ImageConstants.ADDTABLE_IMAGE);
        imageMap.put(ImageConstants.JOINCONDITION, ImageConstants.JOINCONDITION_IMAGE);
        imageMap.put(ImageConstants.EXPANDALL, ImageConstants.EXPAND_IMAGE);
        imageMap.put(ImageConstants.FITTOHEIGHT, ImageConstants.FITTOHEIGHT_IMAGE);
        imageMap.put(ImageConstants.FITTOPAGE, ImageConstants.FITTOPAGE_IMAGE);
        imageMap.put(ImageConstants.FITTOWIDTH, ImageConstants.FITTOWIDTH_IMAGE);
        imageMap.put(ImageConstants.REMOVE, ImageConstants.REMOVE_IMAGE);
        imageMap.put(ImageConstants.OUTPUT, ImageConstants.OUTPUT_IMAGE);
        imageMap.put(ImageConstants.SHOW_SQL, ImageConstants.SQL_IMAGE);
        imageMap.put(ImageConstants.RUN, ImageConstants.RUN_IMAGE);
        imageMap.put(ImageConstants.ZOOMIN, ImageConstants.ZOOM_IN_IMAGE);
        imageMap.put(ImageConstants.ZOOMOUT, ImageConstants.ZOOM_OUT_IMAGE);
        imageMap.put(ImageConstants.VALIDATE, ImageConstants.VALIDATE_IMAGE);
        imageMap.put(ImageConstants.GROUPBY, ImageConstants.GROUPBY_IMAGE);
        imageMap.put(ImageConstants.UNJOIN, ImageConstants.UNJOIN_IMAGE);
        imageMap.put(ImageConstants.REFRESHMETADATA, ImageConstants.REFRESHMETADATA_IMAGE);
        imageMap.put(ImageConstants.REMOUNT, ImageConstants.REMOUNT_IMAGE);
    }
    
    /** Creates a new instance of MashupGraphUtil */
    private MashupGraphUtil() {
    }
    
    public static Image getImageForObject(int type) {
        switch(type) {
        case SQLConstants.JOIN:
            return imageMap.get(ImageConstants.JOIN);
        case SQLConstants.RUNTIME_INPUT:
            return imageMap.get(ImageConstants.RUNTIMEINPUT);
        case SQLConstants.SOURCE_TABLE:
        case SQLConstants.JOIN_TABLE:
            return imageMap.get(ImageConstants.TABLE);
        }
        return null;
    }
    
    public static Image getImage(String imageName) {        
        return imageMap.get(imageName);
    }
}