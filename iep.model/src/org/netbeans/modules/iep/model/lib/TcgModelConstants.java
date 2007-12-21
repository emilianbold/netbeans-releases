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

package org.netbeans.modules.iep.model.lib;

import javax.swing.ImageIcon;


/**
 *  This class holds constants for the tool's use.
 *
 * @author    Bing Lu
 */
public interface TcgModelConstants {
    public final static String UNKNOWN_ICON_NAME = "stcIco.gif";

    public final static ImageIcon UNKNOWN_ICON = ImageUtil.getImageIcon(UNKNOWN_ICON_NAME);

    public static final String NAME_KEY = "name";
    public static final String CODE_TYPE_KEY = "codeType";
 
    // TcgComponentType
    public static final String PCT_ALLOWS_CHILDREN_KEY = "allowsChildren";
    public static final String PCT_ICON_KEY            = "icon";
    public static final String PCT_NAME_KEY            = "name";
    public static final String PCT_PATH_KEY            = "path";
    public static final String PCT_TITLE_KEY           = "title";
    public static final String PCT_VISIBLE_KEY         = "visible";
     
    // TcgComponent
    public static final String PC_IS_VALID_KEY = "isValid";
    public static final String PC_NAME_KEY     = "value";
    public static final String PC_TYPE_KEY     = "type";
    public static final String PC_TITLE_KEY    = "title";
 
    // TcgPropertyType 
    public static final String PPT_DEFAULT_KEY     = "default";
    public static final String PPT_DESCRIPTION_KEY = "description";
    public static final String PPT_EDITOR_KEY      = "editor";
    public static final String PPT_MULTIPLE_KEY    = "multiple";
    public static final String PPT_NAME_KEY        = "name";
    public static final String PPT_TITLE_KEY       = "title";
    public static final String PPT_TYPE_KEY        = "type";
    public static final String PPT_RENDERER_KEY    = "renderer";
    public static final String PPT_READABLE_KEY    = "readable";
    public static final String PPT_WRITABLE_KEY    = "writable";
    
    // TcgProperty
    public static final String PP_TYPE_KEY  = "type";
    public static final String PP_VALUE_KEY = "value";
     
    // TcgCodeType
    public static final String CT_NAME_KEY     = "name";
    public static final String CT_TEMPLATE_KEY = "template";
    
    // Validation
    public static final String VALIDATION_OK_KEY = "validationOk";
    public static final String VALIDATION_ERROR_KEY = "validationError";
    public static final String VALIDATION_WARNING_KEY = "validationWarning";
}


