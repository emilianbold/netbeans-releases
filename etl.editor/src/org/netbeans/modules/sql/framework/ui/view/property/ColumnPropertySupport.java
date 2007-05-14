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
package org.netbeans.modules.sql.framework.ui.view.property;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * @author Ritesh Adval
 */
public class ColumnPropertySupport {
    private Vector pkVec;

    ColumnPropertySupport(List list) {
        pkVec = new Vector(list);
    }

    public void add(String pk) {
        pkVec.add(pk);
    }

    public Vector getDisplayVector() {
        return pkVec;
    }

    public String getDisplayString() {
        if (pkVec.isEmpty()) {
            return "None";
        }

        StringBuilder strBuf = null;
        Iterator it = pkVec.iterator();
        while (it.hasNext()) {
            String str = (String) it.next();
            if (strBuf == null) {
                strBuf = new StringBuilder(str);
            } else {
                strBuf.append(", ").append(str);
            }
        }

        return strBuf.toString();
    }
}

