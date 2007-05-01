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
package org.netbeans.modules.sql.framework.common.utils;

import java.util.Comparator;

import org.netbeans.modules.sql.framework.model.SQLDBColumn;


/**
 * NativeColumnOrderComparator
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public class NativeColumnOrderComparator implements Comparator {
    private static NativeColumnOrderComparator instance;

    /**
     * Gets an instance of NativeColumnOrderComparator.
     * 
     * @return instance of NativeColumnOrderComparator.
     */
    public static NativeColumnOrderComparator getInstance() {
        if (instance == null) {
            instance = new NativeColumnOrderComparator();
        }

        return instance;
    }

    /*
     * Private constructor - to get an instance, use static method getInstance().
     * 
     * @see #getInstance()
     */
    private NativeColumnOrderComparator() {
    }

    /**
     * Compare
     * 
     * @param o1 object1
     * @param o2 object2
     * @return int
     */
    public int compare(Object o1, Object o2) {
        SQLDBColumn col1 = (SQLDBColumn) o1;
        SQLDBColumn col2 = (SQLDBColumn) o2;

        return (col1.getOrdinalPosition() - col2.getOrdinalPosition());
    }
}
