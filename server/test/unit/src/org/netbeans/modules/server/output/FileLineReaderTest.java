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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import org.netbeans.api.server.output.FileInputProvider;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class FileLineReaderTest extends NbTestCase {

    private static final Charset TEST_CHARSET = Charset.forName("UTF-8"); // NOI18N

    private static final String[] TEST_LINES = new String[] {"line1", "line2", "line3", "line4"}; // NOI18N

    private static final String[] TEST_LINES_ROTATE = new String[] {"line5", "line6", "line7", "line8"}; // NOI18N

    private static final int MAX_RETRIES = TEST_LINES.length * 2;

    private File terminatedFile;

    private File terminatedFileRotate;

    private File notTerminatedFile;

    public FileLineReaderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        terminatedFile = TestLineReaderUtils.prepareFile(
                "testFile.txt", TEST_LINES, "\n", TEST_CHARSET, getWorkDir(), true); // NOI18N
        terminatedFileRotate = TestLineReaderUtils.prepareFile(
                "testFileRotate.txt", TEST_LINES_ROTATE, "\n", TEST_CHARSET, getWorkDir(), true); // NOI18N
        notTerminatedFile = TestLineReaderUtils.prepareFile(
                "notTerminatedFile.txt", TEST_LINES, "\n", TEST_CHARSET, getWorkDir(), false); // NOI18N
    }

    public void testReadLine() throws IOException {
        FileInputProvider provider = new FileInputProvider() {
            public FileInput getFileInput() {
                return new FileInput(terminatedFile, TEST_CHARSET);
            }
        };

        FileLineReader lineReader = new FileLineReader(provider);
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

    public void testReadLineUnterminated() {
        final  FileInputProvider.FileInput input =
                new  FileInputProvider.FileInput(notTerminatedFile, TEST_CHARSET);

        FileInputProvider provider = new FileInputProvider() {
            public FileInput getFileInput() {
                return input;
            }
        };

        FileLineReader lineReader = new FileLineReader(provider);
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

    public void testRotation() {
        TestLineReaderUtils.TestFileInputProvider provider = new TestLineReaderUtils.TestFileInputProvider();
        provider.setFileInput(new FileInputProvider.FileInput(terminatedFile, TEST_CHARSET));

        FileLineReader lineReader = new FileLineReader(provider);
        TestLineProcessor processor = new TestLineProcessor(true);

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

        // file rotation
        provider.setFileInput(new FileInputProvider.FileInput(terminatedFileRotate, TEST_CHARSET));

        read = 0;
        retries = 0;
        while (read < TEST_LINES_ROTATE.length && retries < MAX_RETRIES) {
            read += lineReader.readLines(processor, false);
            retries++;
        }

        assertEquals(read, TEST_LINES_ROTATE.length);
        assertEquals(1, processor.getResetCount());

        i = 0;
        for (Iterator<String> it = processor.getLinesProcessed().iterator(); it.hasNext(); i++) {
            assertEquals(TEST_LINES_ROTATE[i], it.next());
        }
    }

    public void testConstructor() {
        try {
            new FileLineReader(null);
            fail("Accepts null file provider"); // NOI18N
        } catch (NullPointerException ex) {
            // expected
        }
    }

}
