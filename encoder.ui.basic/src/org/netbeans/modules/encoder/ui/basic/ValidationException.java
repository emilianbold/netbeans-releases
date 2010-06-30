/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.encoder.ui.basic;

/**
 * Exception for indicating abnormal situation encountered during validation.
 * Normal validation errors (e.g., invalidity found in the document) should not
 * be fired using this exception.  This exception is meant for reporting
 * severe problems occurred during validation and the whole validation process
 * should thus be stopped.
 * 
 * @author Jun Xu
 */
public class ValidationException extends Exception {
    
    /**
     * Constructs from an error message.
     * 
     * @param message the error message
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructs from a throwable.
     * @param e the throwable
     */
    public ValidationException(Throwable e) {
        super(e);
    }

    /**
     * Constructs from a message and a throwable.
     * 
     * @param message the error message
     * @param e the throwable
     */
    public ValidationException(String message, Throwable e) {
        super(message, e);
    }
}
