/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.dbschema.jdbcimpl;

import java.sql.*;
import java.util.LinkedList;

public class MetaDataUtil {

    public static boolean areViewsSupported(String productName) {
        LinkedList list = new LinkedList();
        
        list.add("PointBase"); // NOI18N
        list.add("MySQL"); // NOI18N
        list.add("HypersonicSQL"); // NOI18N
//        list.add("InstantDB"); // NOI18N - isn't necessary in the list - getTables() returns empty result set for views
        
        if (list.contains(productName.trim()))
            return false;
        else
            return true;
    }
}
