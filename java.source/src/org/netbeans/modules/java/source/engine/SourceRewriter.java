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

import java.io.IOException;
import javax.swing.text.BadLocationException;

/**
 * Defines methods used by Jackpot's source rewriting service.  These methods
 * treat the source file as a stream , but an interface is used rather than a
 * java.io.Writer subclass so that non-stream implementations are possible.
 * For example, if a source file is already represented by a javax.swing.Document,
 * it may be more efficient to map these methods to that interface.
 */
public interface SourceRewriter {

    /**
     * Writes the specified string to the source file at the current position.
     *
     * @param s the string to write to the source file.
     */
    void writeTo(String s) throws IOException, BadLocationException;
    
    /**
     * Skips the text from the current SourceReader position to the specified
     * offset.  Equivalent to java.io.Reader.skip().
     *
     * @param in the SourceReader to copy text from.
     * @param offset the ending offset of the text range to copy.
     * @throws IOException if the SourceReader's current position is greater
     *                     than the offset, or if the offset is outside the 
     *                     range of the SourceReader's character array.
     */
    void skipThrough(SourceReader in, int offset) throws IOException, BadLocationException;

    /**
     * Copies the contents of a SourceReader from its current position to the
     * specified source file offset.
     *
     * @param in the SourceReader to copy text from.
     * @param offset the ending offset of the text range to copy.
     * @throws IOException if the SourceReader's current position is greater
     *                     than the offset, or if the offset is outside the 
     *                     range of the SourceReader's character array.
     */
    void copyTo(SourceReader in, int offset) throws IOException;

    /**
     * Copies the remaining text from a SourceReader to the SourceWriter.
     *
     * @param in the SourceReader to copy text from.
     */
    void copyRest(SourceReader in) throws IOException;

    /**
     * Flush and close the SourceRewriter.
     * 
     * @param save flush the SourceRewriter before closing.
     */
    void close(boolean save);
}
