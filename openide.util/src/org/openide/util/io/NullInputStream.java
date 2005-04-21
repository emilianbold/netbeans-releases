/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
