/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.text.api;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.netbeans.editor.TokenContext;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenID;
import org.netbeans.modules.xml.text.syntax.*;

/**
 * XML token-context defines token-ids and token-categories
 * used in XML language.
 *
 * @author Miloslav Metelka
 * @version 1.00
 * @contributor(s) XML Modifications Sandeep Singh Randhawa
 * @integrator Petr Kuzel
 */

public class XMLDefaultTokenContext extends TokenContext implements XMLTokenIDs {

    
    // Context instance declaration
    public static final XMLDefaultTokenContext context = new XMLDefaultTokenContext();  //??? lazy init

    public static final TokenContextPath contextPath = context.getContextPath();


    private XMLDefaultTokenContext() {
        super("xml-");

        try {
            //!!! uses introspection to init us
            Field[] fields = XMLTokenIDs.class.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                int flags = Modifier.STATIC | Modifier.FINAL;
                if ((fields[i].getModifiers() & flags) == flags
                        && TokenID.class.isAssignableFrom(fields[i].getType())
                   ) {
                    addTokenID((TokenID)fields[i].get(null));
                }
            }            
        } catch (Exception e) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                e.printStackTrace();
            }
        }
    }

}
