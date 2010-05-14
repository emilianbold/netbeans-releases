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
 * Since the SchemaVistor interface does not allow any checked exception being
 * thrown, the following runtime exception is used to mark any failure
 * in applying/removing encoding.
 *
 * @author Jun Xu
 */
public class ApplyEncodingException extends RuntimeException {
    
    public ApplyEncodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplyEncodingException(String message) {
        super(message);
    }

    public ApplyEncodingException(Throwable cause) {
        super(cause);
    }
}
