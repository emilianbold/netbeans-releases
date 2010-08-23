/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.validation;

import java.util.ArrayList;
import java.util.List;
import nu.validator.htmlparser.common.CharacterHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author marekfukala
 */
public class LinesMapper implements CharacterHandler {

    private final List<Line> lines = new ArrayList<Line>();
    private Line currentLine = null;
    private boolean prevWasCr = false;
    private final int expectedLength = 2048;

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        int s = start;
        int end = start + length;
        for (int i = start; i < end; i++) {
            char c = ch[i];
            switch (c) {
                case '\r':
                    if (s < i) {
                        currentLine.characters(ch, s, i - s);
                    }
                    newLine();
                    s = i + 1;
                    prevWasCr = true;
                    break;
                case '\n':
                    if (!prevWasCr) {
                        if (s < i) {
                            currentLine.characters(ch, s, i - s);
                        }
                        newLine();
                    }
                    s = i + 1;
                    prevWasCr = false;
                    break;
                default:
                    prevWasCr = false;
                    break;
            }
        }
        if (s < end) {
            currentLine.characters(ch, s, end - s);
        }
    }

    private void newLine() {
        int offset;
        char[] buffer;
        if (currentLine == null) {
            offset = 0;
            buffer = new char[expectedLength];
        } else {
            offset = currentLine.getOffset() + currentLine.getBufferLength();
            buffer = currentLine.getBuffer();
        }
        currentLine = new Line(buffer, offset);
        lines.add(currentLine);
    }

    @Override
    public void end() throws SAXException {
        //no-op
    }

    @Override
    public void start() throws SAXException {
        lines.clear();
        currentLine = null;
        newLine();
        prevWasCr = false;
    }

    /** lines and columns starts at ONE! */
    public int getSourceOffsetForLocation(int line, int column) {
        if(line == -1 || column == -1) {
            throw new IllegalArgumentException();
        }
        Line lline = lines.get(line - 1);
        
        assert column <= lline.getBufferLength();

        return lline.getOffset() + column - 1;
    }

    private static class Line {

        private char[] buffer;
        private int offset = 0;
        private int bufferLength = 0;

        /**
         * @param buffer
         * @param offset
         */
        Line(char[] buffer, int offset) {
            this.buffer = buffer;
            this.offset = offset;
        }

        /**
         * Returns the buffer.
         *
         * @return the buffer
         */
        char[] getBuffer() {
            return buffer;
        }

        /**
         * Returns the bufferLength.
         *
         * @return the bufferLength
         */
        int getBufferLength() {
            return bufferLength;
        }

        /**
         * Returns the offset.
         *
         * @return the offset
         */
        int getOffset() {
            return offset;
        }

        void characters(char[] ch, int start, int length) {
            int newBufferLength = bufferLength + length;
            if (offset + newBufferLength > buffer.length) {
                char[] newBuf = new char[((newBufferLength >> 11) + 1) << 11];
                System.arraycopy(buffer, offset, newBuf, 0, bufferLength);
                buffer = newBuf;
                offset = 0;
            }
            System.arraycopy(ch, start, buffer, offset + bufferLength, length);
            bufferLength = newBufferLength;
        }
    }
}
