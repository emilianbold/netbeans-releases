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

package org.netbeans.api.server.output;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.server.output.ReaderThread;
import org.netbeans.modules.server.output.StreamLineReader;
import org.netbeans.modules.server.output.TestLineProcessor;
import org.netbeans.modules.server.output.TestLineReaderUtils;

/**
 *
 * @author Petr Hejl
 */
public class ReaderManagerTest extends NbTestCase {

    private static final Charset TEST_CHARSET = Charset.forName("UTF-8"); // NOI18N

    private static final String[] TEST_LINES = new String[] {"line1", "line2", "line3", "line4"}; // NOI18N

    private static final long TEST_TIMEOUT = 5000;

    private static final long THREAD_EXIT_TIMEOUT = 1000;

    public ReaderManagerTest(String name) {
        super(name);
    }

    public void testSimpleRun() {
        StreamLineReader lineReader = new StreamLineReader(TestLineReaderUtils.prepareInputStream(
                TEST_LINES, "\n", TEST_CHARSET, true), // NOI18N
                TEST_CHARSET);
        TestLineProcessor processor = new TestLineProcessor(false);
        WaitingLineProcessor waitProcessor = new WaitingLineProcessor(Pattern.compile("line4")); // NOI18N

        // test depends on properly tested proxy and wait line processors
        ReaderManager manager = ReaderManager.newManager(lineReader, new ProxyLineProcessor(processor, waitProcessor));
        manager.start();
        try {
            waitProcessor.await(TEST_TIMEOUT);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        // TODO test thread running

        assertEquals(processor.getLinesProcessed().size(), TEST_LINES.length);
        assertEquals(0, processor.getResetCount());

        int i = 0;
        for (Iterator<String> it = processor.getLinesProcessed().iterator(); it.hasNext(); i++) {
            assertEquals(TEST_LINES[i], it.next());
        }
    }

    public void testStartStop() throws InterruptedException {
        // single test
        StreamLineReader lineReader = new StreamLineReader(
                new TestLineReaderUtils.InfiniteAsciiInputStream(), TEST_CHARSET);

        ReaderManager manager = ReaderManager.newManager(lineReader, null);
        assertManagerStateTransitions(manager, 1);

        // multi test
        StreamLineReader firstReader = new StreamLineReader(
                new TestLineReaderUtils.InfiniteAsciiInputStream(), TEST_CHARSET);
        StreamLineReader secondReader = new StreamLineReader(
                new TestLineReaderUtils.InfiniteAsciiInputStream(), TEST_CHARSET);

        manager = ReaderManager.newManager(new ReaderManager.Pair(firstReader, null),
                new ReaderManager.Pair(secondReader, null));
        assertManagerStateTransitions(manager, 2);
    }

    public void testPair() {
        StreamLineReader lineReader = new StreamLineReader(
                new TestLineReaderUtils.InfiniteAsciiInputStream(), TEST_CHARSET);

        LineProcessor processor = new LineProcessor() {

            public void processLine(String line) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void reset() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };

        // just test exception throwing
        new ReaderManager.Pair(lineReader, processor);
        new ReaderManager.Pair(lineReader, null);

        try {
            new ReaderManager.Pair(null, processor);
        } catch (NullPointerException ex) {
            // expected
        }
    }

    private static void assertManagerStateTransitions(ReaderManager manager, int size) throws InterruptedException {
        assertEquals(size, manager.getThreads().size());
        manager.start();

        assertTrue(manager.isRunning());
        for (ReaderThread thread : manager.getThreads()) {
            assertTrue(thread.isAlive());
        }

        manager.stop();

        for (ReaderThread thread : manager.getThreads()) {
            thread.join(THREAD_EXIT_TIMEOUT);
            assertFalse(thread.isAlive());
        }

        assertFalse(manager.isRunning());
    }
}
