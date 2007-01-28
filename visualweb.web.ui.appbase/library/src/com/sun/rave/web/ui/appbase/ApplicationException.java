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


package com.sun.rave.web.ui.appbase;

import java.util.List;

/**
 * <p>Application exception class that wraps the one or more runtime
 * exceptions that were intercepted and cached during the execution of
 * a particular request's lifecycle.  Call the <code>getExceptions()</code>
 * method to retrieve the cached exception instances.</p>
 */
public class ApplicationException extends RuntimeException {


    // ------------------------------------------------------------ Constructors


    /**
     * <p>Construct a new exception with no additional information.</p>
     */
    public ApplicationException() {
        this(null, null, null);
    }


    /**
     * <p>Construct a new exception with the specified detail message.</p>
     *
     * @param message Detail message for this exception
     */
    public ApplicationException(String message) {
        this(message, null, null);
    }


    /**
     * <p>Construct a new exception with the specified detail message and
     * root cause.</p>
     *
     * @param message Detail message for this exception
     * @param cause Root cause for this exception
     */
    public ApplicationException(String message, Throwable cause) {
        this(message, cause, null);
    }


    /**
     * <p>Construct a new exception with the specified root cause.</p>
     *
     * @param cause Root cause for this exception
     */
    public ApplicationException(Throwable cause) {
        this(cause.getMessage(), cause, null);
    }


    /**
     * <p>Construct a new exception with the specified root cause and
     * list of cached exceptions.</p>
     *
     * @param cause Root cause for this exception
     * @param list <code>List</code> of cached exceptions
     */
    public ApplicationException(Throwable cause, List list) {
        this(cause.getMessage(), cause, list);
    }


    /**
     * <p>Construct a new exception with the specified detail message,
     * root cause, and list of cached exceptions.</p>
     *
     * @param message Detail message for this exception
     * @param cause Root cause for this exception
     * @param list <code>List</code> of cached exceptions
     */
    public ApplicationException(String message, Throwable cause, List list) {
        super(message, cause);
        this.list = list;
    }


    // ------------------------------------------------------------- Properties


    /**
     * <p><code>List</code> of cached exceptions associated with this
     * exception.</p>
     */
    private List list = null;


    /**
     * <p>Return a <code>List</code> of the cached exceptions associated with
     * this exception.  If no such exceptions were associated, return
     * <code>null</code> instead.</p>
     */
    public List getExceptions() {
        return this.list;
    }


}
