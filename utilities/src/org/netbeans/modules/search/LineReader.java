/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.search;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Line reader of a {@code CharBuffer}.
 * It reads the input buffer, translates all commonly used line separators
 * (CR, LF, CRLF) to character {@code '\n'} and writes the result of
 * the translation fo a {@code StringBuilder}. It also stores information
 * about individual line separators used in the input buffer.
 *
 * @author  Marian Petras
 */
final class LineReader {

    enum LineSeparator {
        CR("\r"),                                                       //NOI18N
        LF("\n"),                                                       //NOI18N
        CRLF("\r\n");                                                   //NOI18N
        private final String sepString;
        private LineSeparator(String sepString) {
            this.sepString = sepString;
        }
        public String getString() {
            return sepString;
        }
    }

    private final CharBuffer input;
    private final List<LineSeparator> separators;
    LineReader(CharBuffer input) {
        this.input = input;
        this.separators = new ArrayList<LineSeparator>(100);
    }
    StringBuilder readText() {
        final int inputSize = input.length();
        StringBuilder buf = new StringBuilder(inputSize);
        boolean afterCR = false;
        for (int i = 0; i < inputSize; i++) {
            char c = input.get();
            if (!afterCR) {
                if (c == '\n') {
                    buf.append('\n');
                    recordSeparator(LineSeparator.LF);
                } else if (c == '\r') {
                    afterCR = true;
                } else {
                    buf.append(c);
                }
            } else {
                buf.append('\n');
                if (c == '\n') {
                    recordSeparator(LineSeparator.CRLF);
                    afterCR = false;
                } else if (c == '\r') {
                    recordSeparator(LineSeparator.CR);
                    //keep 'afterCR = true'
                } else {
                    buf.append(c);
                    recordSeparator(LineSeparator.CR);
                    afterCR = false;
                }
            }
        }
        if (afterCR) {
            buf.append('\n');
            recordSeparator(LineSeparator.CR);
        }
        assert !input.hasRemaining();
        return buf;
    }
    private void recordSeparator(LineSeparator sep) {
        separators.add(sep);
    }
    LineSeparator[] getLineSeparators() {
        return separators.toArray(new LineSeparator[separators.size()]);
    }
    void clear() {
        separators.clear();
    }
}
