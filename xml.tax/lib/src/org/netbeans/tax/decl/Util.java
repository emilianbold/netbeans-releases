/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax.decl;

import java.util.*;
import java.text.MessageFormat;

import org.netbeans.tax.AbstractUtil;

/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
class Util extends AbstractUtil {

    static ResourceBundle bundle;  // cache
    
    static final String getString (String key) {
        
	return getBundle().getString(key);
    }

    static final String getString (String key, Object param) {

        return MessageFormat.format(getBundle().getString(key), new Object[] {param});
    }
    
    static final char getChar (String key) {

        return getString(key).charAt (0);
    }

    private static synchronized ResourceBundle getBundle() {
        if (bundle != null) return bundle;        
        bundle = ResourceBundle.getBundle("org.netbeans.tax.decl.Bundle"); //NOI18N
        return bundle;
    }
}

