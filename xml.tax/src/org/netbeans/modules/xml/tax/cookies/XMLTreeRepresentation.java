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
