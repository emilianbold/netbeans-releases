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

package org.netbeans.modules.form;

/**
 * Exception class used for handling errors in persistence operations (loading
 * and saving forms).
 *
 * @author Tomas Pavek
 */

public class PersistenceException extends Exception {

    private Throwable originalException;

    public PersistenceException() {
    }

    public PersistenceException(String s) {
        super(s);
    }

    public PersistenceException(Throwable t) {
        originalException = t;
    }

    public PersistenceException(Throwable t, String s) {
        super(s);
        originalException = t;
    }

    public Throwable getOriginalException() {
        return originalException;
    }
}

