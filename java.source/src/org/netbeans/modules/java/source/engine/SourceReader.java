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

package org.netbeans.modules.java.source.engine;

import java.io.CharArrayReader;
import java.io.IOException;

/**
 * Utility class for reading source files.
 */
public class SourceReader extends CharArrayReader {

    /**
     * Create a new SourceReader instance.
     *
     * @param src the array of characters which are the source file's contents.
     */
    public SourceReader(char[] src) {
        super(src);
    }

    /**
     * Set position to specified array offset.
     */
    public long seek(int offset) throws IOException {
        return skip(offset - pos);
    }

    /**
     * Returns the chars between the current position and a
     * specified offset.
     *
     * @param offset the ending offset of the text range to return.
     * @throws IOException if the current position is greater
     *                     than the offset, or if the offset is outside the 
     *                     range of the SourceReader's character array.
     */
    public char[] getCharsTo(int offset) throws IOException {
        int len = offset - pos;
        if (len < 0 || offset > super.count)
            throw new IOException("invalid offset: " + offset);
        char[] buf = new char[len];
        read(buf);
        return buf;
    }
    
    /**
     * Returns current reader position.
     * 
     * @return reader position
     */
    public int getPos() {
        return pos;
    }
}

