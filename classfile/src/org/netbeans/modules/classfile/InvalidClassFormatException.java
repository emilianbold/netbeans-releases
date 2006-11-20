/*
 * InvalidClassFormatException.java
 *
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
 * Software is Sun Microsystems, Inc. Portions Copyright 2000-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * Contributor(s): Thomas Ball
 *
 * Version: $Revision$
 */

package org.netbeans.modules.classfile;

import java.io.IOException;

/**
 * Exception thrown when a classfile with an invalid format is detected.
 *
 * @author Thomas Ball
 */
public final class InvalidClassFormatException extends IOException {
    /**
     * Constructs an <code>InvalidClassFormatException</code> with
     * <code>null</code> as its error detail message.
     */
    InvalidClassFormatException() {
	super();
    }

    /**
     * Constructs an <code>InvalidClassFormatException</code> with the 
     * specified detail message. The error message string <code>s</code> 
     * can later be retrieved by the 
     * <code>{@link java.lang.Throwable#getMessage}</code>
     * method of class <code>java.lang.Throwable</code>.
     *
     * @param   s   the detail message.
     */
    InvalidClassFormatException(String s) {
	super(s);
    }


    /**
     * Constructs an <code>InvalidClassFormatException</code> with the 
     * specified cause, which is used to define the error message.
     *
     * @param cause   the exception which is used to define the error message.
     */
    InvalidClassFormatException(Throwable cause) {
        super(cause.getLocalizedMessage());
        initCause(cause);
    }
    
    private static final long serialVersionUID = -7043855006167696889L;
}

