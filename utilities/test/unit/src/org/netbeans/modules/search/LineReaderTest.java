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
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.search.LineReader.LineSeparator;
import static org.netbeans.modules.search.LineReader.LineSeparator.CR;
import static org.netbeans.modules.search.LineReader.LineSeparator.LF;
import static org.netbeans.modules.search.LineReader.LineSeparator.CRLF;

/**
 *
 * @author  Marian Petras
 */
public class LineReaderTest extends NbTestCase {

    private static final LineSeparator[] NO_SEPARATOR = new LineSeparator[0];

    public LineReaderTest() {
        super("LineReaderTest");
    }

    public void testReader() {
        checkResult(createReader(""), "", NO_SEPARATOR);

        checkResult(createReader("a"), "a", NO_SEPARATOR);
        checkResult(createReader("ab"), "ab", NO_SEPARATOR);
        checkResult(createReader("abc"), "abc", NO_SEPARATOR);

        checkResult(createReader("\r"), "\n", list(CR));
        checkResult(createReader("\n"), "\n", list(LF));

        checkResult(createReader("\r\n"), "\n", list(CRLF));
        checkResult(createReader("\r\r"), "\n\n", list(CR, CR));
        checkResult(createReader("\n\n"), "\n\n", list(LF, LF));
        checkResult(createReader("\n\r"), "\n\n", list(LF, CR));

        checkResult(createReader("\n\n\n"), "\n\n\n", list(LF, LF, LF));
        checkResult(createReader("\n\n\r"), "\n\n\n", list(LF, LF, CR));
        checkResult(createReader("\n\r\n"), "\n\n", list(LF, CRLF));
        checkResult(createReader("\n\r\r"), "\n\n\n", list(LF, CR, CR));
        checkResult(createReader("\r\n\n"), "\n\n", list(CRLF, LF));
        checkResult(createReader("\r\n\r"), "\n\n", list(CRLF, CR));
        checkResult(createReader("\r\r\n"), "\n\n", list(CR, CRLF));
        checkResult(createReader("\r\r\r"), "\n\n\n", list(CR, CR, CR));

        checkResult(createReader("a\r"), "a\n", list(CR));
        checkResult(createReader("a\n"), "a\n", list(LF));
        checkResult(createReader("a\r\n"), "a\n", list(CRLF));
        checkResult(createReader("a\n\r"), "a\n\n", list(LF, CR));

        checkResult(createReader("\ra"), "\na", list(CR));
        checkResult(createReader("\na"), "\na", list(LF));
        checkResult(createReader("\r\na"), "\na", list(CRLF));
        checkResult(createReader("\n\ra"), "\n\na", list(LF, CR));

        checkResult(createReader("a\rb"), "a\nb", list(CR));
        checkResult(createReader("a\nb"), "a\nb", list(LF));
        checkResult(createReader("a\r\nb"), "a\nb", list(CRLF));
        checkResult(createReader("a\n\rb"), "a\n\nb", list(LF, CR));

        checkResult(createReader("\ra\r"), "\na\n", list(CR, CR));
        checkResult(createReader("\ra\n"), "\na\n", list(CR, LF));
        checkResult(createReader("\na\r"), "\na\n", list(LF, CR));
        checkResult(createReader("\na\n"), "\na\n", list(LF, LF));

        checkResult(createReader("alpha\nbeta\ngamma"), "alpha\nbeta\ngamma", list(LF, LF));
        checkResult(createReader("alpha\nbeta\ngamma\n"), "alpha\nbeta\ngamma\n", list(LF, LF, LF));
        checkResult(createReader("alpha\nbeta\ngamma\n\n"), "alpha\nbeta\ngamma\n\n", list(LF, LF, LF, LF));

        checkResult(createReader("alpha\nbeta\ngamma"), "alpha\nbeta\ngamma", list(LF, LF));
        checkResult(createReader("\nalpha\nbeta\ngamma"), "\nalpha\nbeta\ngamma", list(LF, LF, LF));
        checkResult(createReader("\n\nalpha\nbeta\ngamma"), "\n\nalpha\nbeta\ngamma", list(LF, LF, LF, LF));

        checkResult(createReader("alpha\nbeta\rgamma\n\r"), "alpha\nbeta\ngamma\n\n", list(LF, CR, LF, CR));
        checkResult(createReader("alpha\nbeta\rgamma\r\n"), "alpha\nbeta\ngamma\n", list(LF, CR, CRLF));

        checkResult(createReader("\n\ralpha\nbeta\rgamma"), "\n\nalpha\nbeta\ngamma", list(LF, CR, LF, CR));
        checkResult(createReader("\r\nalpha\nbeta\rgamma"), "\nalpha\nbeta\ngamma", list(CRLF, LF, CR));
    }

    private static LineSeparator[] list(LineSeparator... separators) {
        return separators;
    }

    private void checkResult(LineReader reader,
                             String expText, LineSeparator[] expSeparators) {
        assertEquals("text does not match", expText, reader.readText().toString());
        assertSeparatorsEqual(expSeparators, reader.getLineSeparators());
    }

    private static void assertSeparatorsEqual(LineSeparator[] expected,
                                              LineSeparator[] actual) {
        boolean failed = false;
        if (expected.length != actual.length) {
            failed = true;
        } else {
            for (int i = 0; i < expected.length; i++) {
                if (actual[i] != expected[i]) {
                    failed = true;
                    break;
                }
            }
        }
        if (failed) {
            fail("line separators differ - expected: " + expected
                 + ", actual: " + actual);
        }
    }

    private static LineReader createReader(String s) {
        return new LineReader(createBuffer(s));
    }

    private static CharBuffer createBuffer(String s) {
        CharBuffer result = CharBuffer.allocate(s.length());
        result.append(s);
        result.rewind();
        return result;
    }

}