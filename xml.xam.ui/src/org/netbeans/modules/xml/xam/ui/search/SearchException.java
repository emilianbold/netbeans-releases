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

package org.netbeans.modules.xml.xam.ui.search;

/**
 * Thrown to indicate that a search failed to perform successfully.
 *
 * @author  Nathan Fiedler
 */
public class SearchException extends Exception {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of SearchException.
     */
    public SearchException() {
    }

    /**
     * Creates a new instance of SearchException.
     *
     * @param  message  detail message.
     */
    public SearchException(String message) {
        super(message);
    }

    /**
     * Creates a new instance of SearchException.
     *
     * @param  message  the detail message.
     * @param  cause    the cause.
     */
    public SearchException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new instance of SearchException.
     *
     * @param  cause  the cause.
     */
    public SearchException(Throwable cause) {
        super(cause);
    }
}
