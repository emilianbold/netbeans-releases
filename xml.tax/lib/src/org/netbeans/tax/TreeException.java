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

/**
 * All exceptions in tree are by default unchecked so they must be
 * declared in method signature if they should behave as checked exceptions.
 * <p>
 * At many places it is accurate just mention in JavaDoc that the method
 * may throw it (if passing the right value is at callee reponsibility).
 * It must be declared just at places where callee can not explicitly
 * guarantee that the exception will not occure so it must check for it.
 * <p>
 * It is a folding exception.
 *
 * @author Libor Kramolis
 */
public class TreeException extends Exception {
    
    /** Serial Version UID */
    private static final long serialVersionUID =1949769568282926780L;
    
    //
    // init
    //
    
    /** Create new TreeException. */
    public TreeException (String msg, Exception exception) {
        super (msg);
        if (exception != null) {
            initCause(exception);
        }
    }
    
    
    /** Creates new TreeException with specified detail message.
     * @param msg detail message
     */
    public TreeException (String msg) {
        this (msg, null);
    }
    
    
    /** Creates new TreeException with specified encapsulated exception.
     * @param exc encapsulated exception
     */
    public TreeException (Exception exc) {
        this(exc.toString(), exc);
    }
    
    
    //
    // itself
    //
    
    /** Get the encapsulated exception.
     * @return encapsulated encapsulated
     */
    public Exception getException () {
        return (Exception) getCause();
    }

}
