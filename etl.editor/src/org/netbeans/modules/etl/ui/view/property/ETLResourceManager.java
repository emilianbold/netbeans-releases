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
package org.netbeans.modules.etl.ui.view.property;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.netbeans.modules.sql.framework.ui.editor.property.IResource;
import org.openide.util.NbBundle;

import com.sun.sql.framework.utils.Logger;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ETLResourceManager implements IResource {

    private static final boolean DEBUG = false;

    private static final String LOG_CATEGORY = ETLResourceManager.class.getName();

    private ResourceBundle bundle;

    /** Creates a new instance of ResourceManager */
    public ETLResourceManager() {
        try {
            bundle = NbBundle.getBundle(ETLResourceManager.class);
        } catch (MissingResourceException ex) {
            Logger.printThrowable(Logger.DEBUG, LOG_CATEGORY, this, "Could not locate resource bundle for ETLResourceManager.", ex);
        }
    }

    public String getLocalizedValue(String key) {
        if (bundle != null) {
            try {
                return bundle.getString(key);
            } catch (MissingResourceException ex) {
                // Ignore unless explicitly in debug mode for this class; GUI will use
                // default value.
                if (DEBUG) {
                    Logger.print(Logger.DEBUG, LOG_CATEGORY, this, "Could not locate resource string for key " + key
                        + " in Bundle.properties file associated with " + LOG_CATEGORY + "; using default value.");
                }
            }
        }

        return null;
    }
}

