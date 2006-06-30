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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor.options;

import org.openide.loaders.XMLDataObject;

/** XML Processor for Macros settings
 *
 *  @author  Martin Roskanin
 *  @since 08/2001
 */
public class MacrosMIMEProcessor extends MIMEProcessor{

    /** Public ID of catalog. */
    public static final String PUBLIC_ID = "-//NetBeans//DTD Editor Macros settings 1.0//EN"; // NOI18N
    public static final String SYSTEM_ID = "http://www.netbeans.org/dtds/EditorMacros-1_0.dtd"; // NOI18N

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
        return MacrosMIMEOptionFile.class;
    }
    
    /** Creates appropriate MIME Option file
     * @param o BaseOptions subClass
     * @param b object of MIMEProcessor */
    public MIMEOptionFile createMIMEOptionFile(BaseOptions o, Object b) {
        return new MacrosMIMEOptionFile(o, b);
    }
    
}
