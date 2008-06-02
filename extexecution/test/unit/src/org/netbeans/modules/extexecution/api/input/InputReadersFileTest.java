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

package org.netbeans.modules.extexecution.api.input;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.Callable;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class InputReadersFileTest extends NbTestCase {

    private static final char[] TEST_CHARS = "abcdefghij".toCharArray();

    private static final char[] TEST_CHARS_ROTATE = "jihgfedcba".toCharArray();
    
    private static final Charset TEST_CHARSET = Charset.forName("UTF-8");

    private static final int MAX_RETRIES = TEST_CHARS.length * 2;

    private File byteFile;

    private File byteFileRotate;

    public InputReadersFileTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        byteFile = TestInputUtils.prepareFile(
                "testFile.txt", getWorkDir(), TEST_CHARS, TEST_CHARSET);
        byteFileRotate = TestInputUtils.prepareFile(
                "testFileRotate.txt", getWorkDir(), TEST_CHARS_ROTATE, TEST_CHARSET);
    }

    public void testReadOutput() throws IOException {
        InputReader outputReader = InputReaders.forFileGenerator(new Callable<File>() {

            public File call() throws Exception {
                return byteFile;
            }
        }, TEST_CHARSET);
        TestInputProcessor processor = new TestInputProcessor(false);

        int read = 0;
        int retries = 0;
        while (read < TEST_CHARS.length && retries < MAX_RETRIES) {
            read += outputReader.readOutput(processor);
            retries++;
        }

        assertEquals(read, TEST_CHARS.length);
        assertEquals(0, processor.getResetCount());

        assertTrue(Arrays.equals(TEST_CHARS, processor.getCharsProcessed()));
    }

    public void testRotation() throws IOException {
        TestCallable callable = new TestCallable();
        callable.setFile(byteFile);

        InputReader outputReader = InputReaders.forFileGenerator(callable, TEST_CHARSET);
        TestInputProcessor processor = new TestInputProcessor(true);

        int read = 0;
        int retries = 0;
        while (read < TEST_CHARS.length && retries < MAX_RETRIES) {
            read += outputReader.readOutput(processor);
            retries++;
        }

        assertEquals(read, TEST_CHARS.length);
        assertEquals(0, processor.getResetCount());

        assertTrue(Arrays.equals(TEST_CHARS, processor.getCharsProcessed()));

        // file rotation
        callable.setFile(byteFileRotate);

        read = 0;
        retries = 0;
        while (read < TEST_CHARS_ROTATE.length && retries < MAX_RETRIES) {
            read += outputReader.readOutput(processor);
            retries++;
        }

        assertEquals(read, TEST_CHARS_ROTATE.length);
        assertEquals(1, processor.getResetCount());

        assertTrue(Arrays.equals(TEST_CHARS_ROTATE, processor.getCharsProcessed()));
    }

    public void testFactory() {
        try {
            InputReaders.forFile(null, null);
            fail("Accepts null file generator"); // NOI18N
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testClose() throws IOException {
        InputReader reader = InputReaders.forFileGenerator(new Callable<File>() {

            public File call() throws Exception {
                return byteFile;
            }
        }, TEST_CHARSET);
        reader.close();

        try {
            reader.readOutput(null);
            fail("Reader not throw exception on read after closing it"); // NOI18N
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    private static class TestCallable implements Callable<File> {

        private File file;

        public synchronized File call() throws Exception {
            return file;
        }

        public synchronized void setFile(File file) {
            this.file = file;
        }

    }
}
