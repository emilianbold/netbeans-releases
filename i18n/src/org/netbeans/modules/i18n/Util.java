/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.i18n;

import org.openide.ErrorManager;
import org.openide.util.*;

/**
 * Bundle access, ...
 *
 * @author  Petr Kuzel
 */
final class Util {
    
    public static String getString(String key) {
        return NbBundle.getMessage(Util.class, key);
    }
    
    public static char getChar(String key) {
        return getString(key).charAt(0);
    }
    
    /**
     * Write the exception into log.
     */
    public static void debug(Throwable t) {
        ErrorManager err = ErrorManager.getDefault();
        err.notify(err.INFORMATIONAL, t);
    }

    /**
     * Write annotated exception into log.
     */
    public static void debug(String annotation, Throwable t) {
        ErrorManager err = ErrorManager.getDefault();
        err.annotate(t, err.INFORMATIONAL, annotation, null, null, null);
        err.notify(err.INFORMATIONAL, t);
    }
    
}
