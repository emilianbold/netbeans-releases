/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.server.output;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class StreamLineReaderTest extends NbTestCase {

    private static final Charset TEST_CHARSET = Charset.forName("UTF-8"); // NOI18N

    private static final String[] TEST_LINES = new String[] {"line1", "line2", "line3", "line4"}; // NOI18N

    private static final int MAX_RETRIES = TEST_LINES.length * 2;

    public StreamLineReaderTest(String name) {
        super(name);
    }

    public void testReadLine() throws IOException {
        StreamLineReader lineReader = new StreamLineReader(TestLineReaderUtils.prepareInputStream(
                TEST_LINES, "\n", TEST_CHARSET, true), // NOI18N
                TEST_CHARSET);
        TestLineProcessor processor = new TestLineProcessor(false);

        int read = 0;
        int retries = 0;
        while (read < TEST_LINES.length && retries < MAX_RETRIES) {
            read += lineReader.readLines(processor, false);
            retries++;
        }

        assertEquals(read, TEST_LINES.length);
        assertEquals(0, processor.getResetCount());

        int i = 0;
        for (Iterator<String> it = processor.getLinesProcessed().iterator(); it.hasNext(); i++) {
            assertEquals(TEST_LINES[i], it.next());
        }
    }

    public void testReadLineUnterminated() throws IOException {
        StreamLineReader lineReader = new StreamLineReader(TestLineReaderUtils.prepareInputStream(
                TEST_LINES, "\n", TEST_CHARSET, false), // NOI18N
                TEST_CHARSET);
        TestLineProcessor processor = new TestLineProcessor(false);

        int read = 0;
        int retries = 0;
        while (read < TEST_LINES.length - 1 && retries < MAX_RETRIES) {
            read += lineReader.readLines(processor, false);
            retries++;
        }

        assertEquals(read, TEST_LINES.length - 1);
        assertEquals(0, processor.getResetCount());

        int i = 0;
        for (Iterator<String> it = processor.getLinesProcessed().iterator(); it.hasNext(); i++) {
            assertEquals(TEST_LINES[i], it.next());
        }

        read += lineReader.readLines(processor, true);
        assertEquals(read, TEST_LINES.length);
        assertEquals(0, processor.getResetCount());

        i = 0;
        for (Iterator<String> it = processor.getLinesProcessed().iterator(); it.hasNext(); i++) {
            assertEquals(TEST_LINES[i], it.next());
        }
    }

    public void testConstructor() {
        try {
            new StreamLineReader(null, TEST_CHARSET);
            fail("Accepts null stream"); // NOI18N
        } catch (NullPointerException ex) {
            // expected
        }

        try {
            new StreamLineReader(new ByteArrayInputStream(new byte[0]), null); // NOI18N
            fail("Accepts null charset"); // NOI18N
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testClose() throws IOException {
        StreamLineReader reader = new StreamLineReader(TestLineReaderUtils.prepareInputStream(
                new String[] {"test"}, "\n", TEST_CHARSET, true), // NOI18N
                TEST_CHARSET);
        reader.close();

        try {
            reader.readLines(null, false);
            fail("Reader not throw exception on read after closing it"); // NOI18N
        } catch (IllegalStateException ex) {
            // expected
        }
    }
}
