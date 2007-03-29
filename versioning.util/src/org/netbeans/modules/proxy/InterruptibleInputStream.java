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

package org.netbeans.modules.proxy;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * Wrapper input stream that reacts to Thread.interrupt().
 *
 * @author Maros Sandor
 */
public class InterruptibleInputStream extends FilterInputStream {

    public InterruptibleInputStream(InputStream in) {
        super(in);
    }

    public int read() throws IOException {
        waitAvailable();
        return in.read();
    }

    public int read(byte b[], int off, int len) throws IOException {
        waitAvailable();
        return super.read(b, off, len);
    }

    private void waitAvailable() throws IOException {
        while (in.available() == 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new InterruptedIOException();
            }
        }
    }
}
