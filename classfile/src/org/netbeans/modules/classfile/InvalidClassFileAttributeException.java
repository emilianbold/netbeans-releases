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
 * Software is Sun Microsystems, Inc. Portions Copyright 2000-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.classfile;

/**
 * Thrown when a classfile attribute does not follow the specified format.
 * This is an RuntimeException subclass rather than an Exception because it 
 * is very unlikely that a classfile can be read without exception (such as 
 * when the classfile is truncated or overwritten) yet have an invalid attribute.  
 * Making this an Exception would force existing client code to be updated with
 * almost no benefit in robustness to those clients.
 * 
 * @author Thomas Ball
 */
public final class InvalidClassFileAttributeException extends RuntimeException {
    
    InvalidClassFileAttributeException(String message, Throwable cause) {
        super(message, cause);
    }
    
    private static final long serialVersionUID = -2988920220798200016L;
}
