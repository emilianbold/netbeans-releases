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
package org.openide.util.io;

import java.io.IOException;
import java.io.InputStream;


/**
 * Input stream that is always empty.
 * @author Ales Novak
 */
public class NullInputStream extends InputStream {
    /**
     * True if attempting to read from the stream should throw an {@link IOException}.
     * False to simply return end of file.
     */
    public boolean throwException;

    /** Create an empty null input stream. */
    public NullInputStream() {
    }

    public int read() throws IOException {
        if (throwException) {
            throw new IOException();
        }

        return -1;
    }
}
