/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import org.openide.loaders.XMLDataObject;

/** XML Processor for Fonts and Colors settings
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */
public class FontsColorsMIMEProcessor extends MIMEProcessor{
    
    /** Public ID of catalog. */
    public static final String PUBLIC_ID = "-//NetBeans//DTD Editor Fonts and Colors settings 1.0//EN"; // NOI18N
    public static final String SYSTEM_ID = "http://www.netbeans.org/dtds/EditorFontsColors-1_0.dtd"; // NOI18N
    
    /** Gets DTD's PUBLIC_ID */
    public String getPublicID(){
        return PUBLIC_ID;
    }
    
    /** Gets DTD's SYSTEM_ID */
    public String getSystemID(){
        return SYSTEM_ID;
    }
    
    /** Gets the class of MIMEOption file that handle this XML file type */
    public Class getAsociatedMIMEOptionFile() {
        return FontsColorsMIMEOptionFile.class;
    }
    
    /** Creates appropriate MIME Option file
     * @param o BaseOptions subClass
     * @param b object of MIMEProcessor */
    public MIMEOptionFile createMIMEOptionFile(BaseOptions o, Object b) {
        return new FontsColorsMIMEOptionFile(o, b);
    }
    
}
