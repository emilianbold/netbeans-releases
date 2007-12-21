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
package org.netbeans.modules.xslt.tmap.model.impl;

/**
 *
 * @author ads
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class InvalidNamespaceException extends Exception {

    private static final long serialVersionUID = 2249869832631885258L;

    /**
     * Empty-argument Constructor for this exception class.
     */
    public InvalidNamespaceException() {
        super();
    }

    /**
     * Constructor for excpetion with message and cuase arguments.
     * @param message Information message.
     * @param cause Cause of exception.
     */
    public InvalidNamespaceException( String message, Throwable cause ) {
        super(message, cause);
    }

    /**
     * Constructor for excpetion with message argument. 
     * @param message Information message.
     */
    public InvalidNamespaceException( String message ) {
        super(message);
    }

    /**
     * Constructor for excpetion with  cuase argument.
     * @param cause Cause of exception.
     */
    public InvalidNamespaceException( Throwable cause ) {
        super(cause);
    }

}
