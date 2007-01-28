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

package com.sun.data.provider;

/**
 * The TableCursorVetoException is thrown when a listener to a cursorChanging
 * event does not want the cursor to move.
 *
 * @see TableCursorListener#cursorChanging
 *
 * @author Joe Nuxoll
 */
public class TableCursorVetoException extends DataProviderException {

    public TableCursorVetoException() {}

    public TableCursorVetoException(String message) {
        super(message);
    }

    public TableCursorVetoException(String message, Throwable cause) {
        super(message, cause);
    }

    public TableCursorVetoException(Throwable cause) {
        super(cause);
    }
}
