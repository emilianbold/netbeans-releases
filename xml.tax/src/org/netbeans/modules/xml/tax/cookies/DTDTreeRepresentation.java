/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tax.cookies;

import org.xml.sax.*;

import org.netbeans.modules.xml.core.sync.*;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookieImpl;

/**
 *
 * @author  Petr Kuzel
 * @version 
 */
public class DTDTreeRepresentation extends TreeRepresentation {

    /** Creates new DTDTreeRepresentation */
    public DTDTreeRepresentation(TreeEditorCookieImpl editor, Synchronizator sync) {
        super(editor, sync);
    }

    /**
     * Update the representation without marking it as modified.
     */
    public void update(Object change) {
        if (change instanceof InputSource) {
            InputSource update = (InputSource) change;
            editor.updateTree(change);
        }
    }

    /**
     * Is this representation modified since last sync?
     */
    public boolean isModified() {
        return false;
    }
    
    /**
     * Return modification passed as update parameter to all slave representations.
     */
    public Object getChange(Class type) {
        return null;  // read only
    }
    
}
