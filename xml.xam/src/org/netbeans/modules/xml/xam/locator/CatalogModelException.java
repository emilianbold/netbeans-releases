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

package org.netbeans.modules.xml.xam.locator;

/**
 *
 * @author girix
 */
public class CatalogModelException extends Exception {
    static final long serialVersionUID = 19800701l;
    /**
     * Creates a new instance of <code>CatalogModelException</code> without detail message.
     */
    public CatalogModelException() {
    }
    
    
    /**
     * Constructs an instance of <code>CatalogModelException</code> with the specified detail message.
     * 
     * @param msg the detail message.
     */
    public CatalogModelException(String msg) {
        super(msg);
    }
    
    public CatalogModelException(Throwable cause){
        super();
        this.initCause(cause);
    }
}
