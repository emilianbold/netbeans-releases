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
package org.netbeans.modules.mashup.db.common;

/**
 * Base class for exceptions thrown within the Flatfile Database module.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class FlatfileDBException extends java.lang.Exception {

    private Throwable rootCause;

    /**
     * Creates a new instance of <code>FlatfileDBException</code> without detail
     * message.
     */
    public FlatfileDBException() {
    }

    /**
     * Constructs an instance of <code>FlatfileDBException</code> with the specified
     * detail message.
     * 
     * @param msg the detail message.
     */
    public FlatfileDBException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>FlatfileDBException</code> with the specified
     * category and detail message.
     * 
     * @param category error category
     * @param msg the detail message.
     */
    public FlatfileDBException(String category, String msg) {
        super(category + " - " + msg);
    }

    /**
     * Constructs an instance of <code>FlatfileDBException</code> with the specified
     * category and detail message.
     * 
     * @param category string categorizing this exception.
     * @param e cause identifying this exception.
     */
    public FlatfileDBException(String category, Throwable e) {
        super(category);
        rootCause = e;
    }

    /**
     * Returns a description of this exception.
     * 
     * @return exception description.
     */
    public String toString() {
        StringBuilder buf = new StringBuilder();

        buf.append(this.getMessage());
        if (null != rootCause) {
            buf.append(" - Root Cause: ").append(rootCause.toString());
        }

        return buf.toString();
    }
}
