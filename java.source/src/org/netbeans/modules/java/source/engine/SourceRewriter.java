/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
    void close(boolean save) throws IOException;
}
