/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.core;

import org.openide.options.SystemOption;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;

/**
 *
 * @author Andrei Badea
 */
public class SQLOptions extends SystemOption {
    
    private static final long serialVersionUID = -3200264376265775809L;
    
    private static final String PROP_FETCH_STEP = "fetchStep"; // NOI18N
    
    private static final int DEFAULT_FETCH_STEP = 200;
    
    public static SQLOptions getDefault() {
        return (SQLOptions)SharedClassObject.findObject(SQLOptions.class, true);
    }
    
    public String displayName() {
        return NbBundle.getMessage(SQLOptions.class, "LBL_SQLOptions");
    }
    
    public int getFetchStep() {
        Object data = getProperty(PROP_FETCH_STEP);
        if (data != null) {
            return convertToInt((String)data, DEFAULT_FETCH_STEP);
        } else {
            return DEFAULT_FETCH_STEP;
        }
    }
    
    public void setFetchStep(int value) {
        putProperty(PROP_FETCH_STEP, String.valueOf(value), true);
    }
    
    private static int convertToInt(String str, int def) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
