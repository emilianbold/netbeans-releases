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

package org.netbeans;

/**
 * Thrown during exit of the task, not reported as exception
 *
 * @author Ales Novak
 */
class ExitSecurityException extends SecurityException {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8973677308554045785L;

    /** Creates new exception ExitSecurityException
    */
    public ExitSecurityException () {
        super ();
    }

    /** Creates new exception ExitSecurityException with text specified
    * string s.
    * @param s the text describing the exception
    */
    public ExitSecurityException (String s) {
        super (s);
    }
}
