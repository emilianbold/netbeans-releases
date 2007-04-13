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

package org.netbeans.modules.j2ee.metadata.model.api;

import java.io.IOException;

/**
 * Signals that an exception has occured while working with
 * the metadata model.
 *
 * @author Andrei Badea
 */
public final class MetadataModelException extends IOException {

    /**
     * Constructs an {@code MetadataModelException} with {@code null}
     * as its error detail message.
     */
    public MetadataModelException() {
        super();
    }

    /**
     * Constructs an {@code MetadataModelException} with the specified detail message.
     *
     * @param  message the detail message (which is saved for later retrieval
     *         by the {@link #getMessage()} method)
     */
    public MetadataModelException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code MetadataModelException} with the specified cause and a
     * detail message equal to the localized message (if present) or message of <code>cause</code>.
     * This constructor is useful for IO exceptions that are little more
     * than wrappers for other throwables.
     *
     * @param  cause the cause (which is saved for later retrieval by the
     *         {@link #getCause()} method).  (A null value is permitted,
     *         and indicates that the cause is nonexistent or unknown.)
     */
    public MetadataModelException(Throwable cause) {
        super(cause == null ? null : getMessage(cause));
        initCause(cause);
    }

    private static String getMessage(Throwable t) {
        String message = t.getLocalizedMessage();
        if (message == null) {
            message = t.getMessage();
        }
        return message;
    }
}
