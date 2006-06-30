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
