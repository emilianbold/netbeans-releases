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
package org.netbeans.modules.xml.tax.cookies;

import java.io.IOException;
import org.xml.sax.*;

import org.openide.*;

import org.netbeans.tax.*;
import org.netbeans.modules.xml.core.sync.*;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookieImpl;
import org.netbeans.modules.xml.tax.cookies.TreeRepresentation;

/**
 * Manages relations between tree model ant tree editor.
 *
 * @author  Petr Kuzel
 * @version 
 */
public class XMLTreeRepresentation extends TreeRepresentation {

    /** Creates new XMLTreeRepresentation */
    public XMLTreeRepresentation(TreeEditorCookieImpl editor, Synchronizator sync) {
        super(editor, sync);
    }    

    /**
     * Update the representation without marking it as modified.
     */
    public void update(Object change) {

        if (change instanceof InputSource) {
            InputSource update = (InputSource) change;
            editor.updateTree(update);
        } else {
            throw new RuntimeException("TreeRepresentation does not support: " + change.getClass()); // NOI18N
        }
    }


    /**
     * Is this representation modified since last sync?
     */
    public boolean isModified() {
        return false;
    }
    
}
