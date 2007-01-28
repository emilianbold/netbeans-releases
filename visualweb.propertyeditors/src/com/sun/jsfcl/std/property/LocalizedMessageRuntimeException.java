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
package com.sun.jsfcl.std.property;

/**
 * This class exists SOLELY to allow the property editors to display a nice message
 * on the status pane, with an informational icon rather than having a stack trace
 * being put out to the log and a red icon being displayed there.
 *
 * @author eric
 *
 */
public class LocalizedMessageRuntimeException extends RuntimeException {
    protected String localizedMessage;

    /**
     * @param message
     */
    public LocalizedMessageRuntimeException(String message) {

        this(message, null, null);
    }

    /**
     * @param cause
     */
    public LocalizedMessageRuntimeException(Throwable cause) {

        this(null, null, cause);
    }

    /**
     * @param message
     * @param cause
     */
    public LocalizedMessageRuntimeException(String message, Throwable cause) {

        this(message, null, cause);
    }

    public LocalizedMessageRuntimeException(String message, String localizedMessage,
        Throwable cause) {

        super(message, cause);
        if (localizedMessage == null) {
            this.localizedMessage = "" + getMessage();
        } else {
            this.localizedMessage = localizedMessage;
        }
    }

    public String getLocalizedMessage() {

        return localizedMessage;
    }

}
