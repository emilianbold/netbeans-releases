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
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.wsdlextensions.mq.editor;

/**
 * Exception to indicate that a model change failed.
 *
 * @author Noel.Ang@sun.com
 */
public class ModelModificationException extends Exception {
    /**
     * Constructs a new exception with <code>null</code> as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public ModelModificationException() {
    }

    /**
     * Constructs a new exception with the specified detail message.  The cause
     * is not initialized, and may subsequently be initialized by a call to
     * {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for later
     * retrieval by the {@link #getMessage()} method.
     */
    public ModelModificationException(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * <p>Note that the detail message associated with <code>cause</code> is
     * <i>not</i> automatically incorporated in this exception's detail
     * message.
     *
     * @param message the detail message (which is saved for later retrieval by
     * the {@link #getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the {@link
     * #getCause()} method).  (A <tt>null</tt> value is permitted, and indicates
     * that the cause is nonexistent or unknown.)
     *
     * @since 1.4
     */
    public ModelModificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
