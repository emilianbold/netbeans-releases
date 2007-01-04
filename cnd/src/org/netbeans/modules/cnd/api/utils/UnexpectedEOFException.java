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

package org.netbeans.modules.cnd.api.utils;

import java.io.IOException;

/**
 *  Exception thrown by FortranReader when EOF is raised at an unexpected time. This
 *  usually means in the middle of a line of source.
 */
public class UnexpectedEOFException extends IOException {

    /**
     * Constructs an <code>UnexpectedEOFException</code> with <code>null</code>
     * as its error detail message.
     */
    public UnexpectedEOFException() {
	super();
    }

    /**
     * Constructs an <code>UnexpectedEOFException</code> with the specified detail
     * message. The error message string <code>s</code> can later be
     * retrieved by the <code>{@link java.lang.Throwable#getMessage}</code>
     * method of class <code>java.lang.Throwable</code>.
     *
     * @param   s   the detail message.
     */
    public UnexpectedEOFException(String s) {
	super(s);
    }
}
