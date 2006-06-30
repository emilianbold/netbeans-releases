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
package org.netbeans.tax;

import java.util.ResourceBundle;
import java.text.MessageFormat;

/**
 *
 * @author Libor Kramolis
 * @version 0.2
 */
public abstract class AbstractUtil {
    /** Cached isLoggable value. */
    private boolean loggable;
    /** Has loggable already been initialized. */
    private boolean loggableInit = false;
    /** Package resource bundle. */
    private ResourceBundle bundle;


    /**
     * Kind of equals that treat null-ed object as equvivalent.
     * Suitable while testing for property equalence before firing.
     * return true if these are same
     */
    public static boolean equals (Object a, Object b) {
        if ( a != null ) {
            return (a.equals (b));
        } else {
            return (a == b);
        }
    }

    /**
     * Just for debugging purposes.
     */
    public final void debug (String message, Throwable ex) {
        if ( isLoggable() ) {
            System.err.println("[org.netbeans.tax] " + message);
            if ( ex != null ) {
                ex.printStackTrace (System.err);
            }
        }
    }
    
    
    /**
     * Just for debugging purposes.
     */
    public final void debug (String message) {
        debug (message, null);
    }


    /**
     * Just for debugging purposes.
     */
    public final void debug (Throwable ex) {
        debug (ex.getMessage(), ex);
    }


    /** Test if <code>debug (...)</code> will log something.
     */
    public final synchronized boolean isLoggable () {
        if ( loggableInit == false ) {
            loggable = Boolean.getBoolean("org.netbeans.tax"); // NOI18N
            loggableInit = true;
        }
        return loggable;
    }

    /**
     * @return bundle for this instance package
     */
    protected final synchronized ResourceBundle getBundle () {
        return ResourceBundle.getBundle(getClass().getName().replaceFirst("\\.[^.]+$", ".Bundle")); // NOI18N
    }
    
    /** Get localized string.
     * @param key key of localized value.
     * @return localized value.
     */
    public final String getString (String key) {
        return getBundle ().getString (key);
    }
    
    /** Get localized string by passing parameter.
     * @param key key of localized value.
     * @param param argument to use when formating the message
     * @return localized value.
     */
    public final String getString (String key, Object param) {
        return MessageFormat.format (getBundle().getString (key), new Object[] {param});
    }
    
    /** Get localized character. Usually used on mnemonic.
     * @param key key of localized value.
     * @return localized value.
     */
    public final char getChar (String key) {
        return getString (key).charAt (0);
    }

}
