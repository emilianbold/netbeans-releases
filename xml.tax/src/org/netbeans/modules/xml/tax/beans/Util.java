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
package org.netbeans.modules.xml.tax.beans;

import org.openide.util.NbBundle;

import org.netbeans.modules.xml.tax.AbstractUtil;

/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
class Util extends AbstractUtil {

    /** Get localized string.
     * @param key key of localized value.
     * @return localized value.
     */
    static final String getString (String key) {
	return NbBundle.getMessage (Util.class, key);
    }
    
    /** Get localized string by passing parameter.
     * @param key key of localized value.
     * @param param argument to use when formating the message
     * @return localized value.
     */
    static final String getString (String key, Object param) {
	return NbBundle.getMessage (Util.class, key, param);
    }
    
    /** Get localized character. Usually used on mnemonic.
     * @param key key of localized value.
     * @return localized value.
     */
    static final char getChar (String key) {
	return NbBundle.getMessage (Util.class, key).charAt (0);
    }

}
