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
package org.netbeans.modules.visualweb.propertyeditors;

import org.openide.ErrorManager;

/**
 * An exception thrown by property editors when the user enters invalid text.
 * This exception should be thrown by <code>PropertyEditor.setAsText(String)</code>
 * when the text value is invalid. Unlike <code>IllegalArgumentException</code>,
 * which NetBeans will pass directly to the console, instances of this class
 * will annotate themselves using the NetBeans error manager so that a pop-window
 * alerts the user to his or her mistake.
 *
 * @author gjmurphy
 */
public class IllegalTextArgumentException extends IllegalArgumentException {

    public IllegalTextArgumentException(String message) {
        super(message);
        ErrorManager.getDefault().annotate(this, ErrorManager.USER, message,
            message, null, null);
    }

    public IllegalTextArgumentException(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
        ErrorManager.getDefault().annotate(this, ErrorManager.USER, message,
            message, cause, null);
    }

}
