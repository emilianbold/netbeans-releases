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

package org.netbeans.modules.apisupport.refactoring;

import org.netbeans.modules.refactoring.api.Problem;
import org.openide.ErrorManager;


public class Utility {
    
    private static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.modules.j2ee.refactoring");   // NOI18N
    
    /** Creates a new instance of Utility */
    private Utility() {
    }

    
    public static Problem addProblemsToEnd(Problem where, Problem what) {
        Problem whereCopy = where;
        err.log("Where: " + where);
        err.log("What: " + what);
        if (what != null) {
            if (where == null) {
                whereCopy = what;
            } else {
                while (where.getNext() != null) {
                    where = where.getNext();
                }
                err.log("Last where: " + where);
                while (what != null) {
                    Problem elem = what;
                    err.log("Elem: " + elem);
                    where.setNext(elem);
                    where = where.getNext();
                    what = what.getNext();
                }
            }
        }
        err.log("wherecopy return: " + whereCopy);
        return whereCopy;
    } 
    
    /**
     * Creates full class name from package name and simple class name
     * @param pkg package name
     * @param simpleName simple class name
     * @return full class name
     */
    public static String getClassName(String pkg, final String simpleName) {
        return (pkg == null || pkg.length() == 0 ? "" : pkg + ".") + simpleName;
    }

}
