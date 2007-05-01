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
package org.netbeans.modules.mashup.db.ui.resource;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.netbeans.modules.sql.framework.ui.editor.property.IResource;
import org.openide.util.NbBundle;


/**
 * Manages resource strings as referenced by PropertyEditor-derived classes.
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public class FlatfileDBResourceManager implements IResource {

    /* ResourceBundle containing resource strings. */
    private ResourceBundle bundle;

    /** Creates a new instance of ResourceManager */
    public FlatfileDBResourceManager() {
        try {
            bundle = NbBundle.getBundle(FlatfileDBResourceManager.class);
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Gets localized value associated with the given key from current resource bundle.
     * 
     * @param key Key of string resource to retrieve
     * @return String associated with key, or null if no such resource exists
     */
    public String getLocalizedValue(String key) {
        if (bundle != null) {
            try {
                return bundle.getString(key);
            } catch (MissingResourceException ex) {
                // ex.printStackTrace();
            }
        }

        return null;
    }
}

