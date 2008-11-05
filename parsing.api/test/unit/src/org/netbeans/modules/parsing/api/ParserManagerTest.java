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

package org.netbeans.modules.parsing.api;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.TaskScheduler;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author tom
 */
public class ParserManagerTest extends NbTestCase {

    public ParserManagerTest (String name) {
        super (name);
    }

    @Override
    public void setUp () throws Exception {
        clearWorkDir ();
        // 1) register tasks and parsers
        MockServices.setServices (MockMimeLookup.class);
        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"), new FooParserFactory());
    }

    public void testParseCache () throws Exception {
        TimedWeakReference.TIMEOUT = 5000;
        final boolean[] called = new boolean[] {false};
        FooParser.getResultCount = 0;
        FooParser.parseCount = 0;
        FooParserFactory.createParserCount = 0;
        ParserManager.parse("text/foo", new UserTask() {
            @Override
            public void run(Result result) throws Exception {
                called[0]=true;
            }
        });
        assertTrue(called[0]);
        assertEquals(1, FooParserFactory.createParserCount);
        assertEquals(1, FooParser.parseCount);
        assertEquals(1, FooParser.getResultCount);        
        called[0] = false;
        ParserManager.parse("text/foo", new UserTask() {
            @Override
            public void run(Result result) throws Exception {
                called[0]=true;
            }
        });
        assertTrue(called[0]);
        assertEquals(1, FooParserFactory.createParserCount);
        assertEquals(2, FooParser.parseCount);
        assertEquals(2, FooParser.getResultCount);
        Thread.sleep(2 * TimedWeakReference.TIMEOUT);
        System.gc(); System.gc();
        called[0] = false;
        ParserManager.parse("text/foo", new UserTask() {
            @Override
            public void run(Result result) throws Exception {
                called[0]=true;
            }
        });
        assertTrue(called[0]);
        assertEquals(2, FooParserFactory.createParserCount);
        assertEquals(3, FooParser.parseCount);
        assertEquals(3, FooParser.getResultCount);


    }

    public void testParseWhenScanFinished () throws Exception {
        FileUtil.setMIMEType ("foo", "text/foo");
        final FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        final FileObject testFile = FileUtil.createData (workDir, "test.foo");
        final Source source = Source.create (testFile);
        final Collection<Source> sources = Collections.singleton(source);
        final TestTask tt = new TestTask();
        ParserManager.parse(sources, tt);
        assertEquals(1, tt.called);
        final Future<Void> future = ParserManager.parseWhenScanFinished(sources, tt);
        assertEquals(1, tt.called);
        assertFalse (future.isDone());
        future.cancel(false);
        assertFalse (future.isDone());
        assertTrue(future.isCancelled());

        final TestTask tt2 = new TestTask();
        final Future<Void> future2 = ParserManager.parseWhenScanFinished(sources, tt2);
        assertEquals(0, tt2.called);
        assertFalse (future2.isDone());

        RUEmulator emulator = new RUEmulator();
        Utilities.setIndexingStatus(emulator);

        final CountDownLatch countDown = new CountDownLatch(1);
        final TestTask tt3 = new TestTask(countDown);
        final Future<Void> future3 = ParserManager.parseWhenScanFinished(sources, tt3);
        assertEquals(0, tt3.called);
        assertFalse (future3.isDone());
        emulator.scan();
        assertTrue(countDown.await(10, TimeUnit.SECONDS));
        assertFalse (future.isDone());
        assertTrue (future2.isDone());
        assertTrue (future3.isDone());

        final TestTask tt4 = new TestTask();
        final Future<Void> future4 = ParserManager.parseWhenScanFinished(sources, tt4);
        assertEquals(1, tt4.called);
        assertTrue(future4.isDone());
    }

    private static class TestTask extends MultiLanguageUserTask {

        long called = 0;
        final CountDownLatch latch;

        public TestTask () {
            latch = null;
        }

        public TestTask (final CountDownLatch latch) {
            this.latch = latch;
        }

        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            called++;
            if (latch != null) {
                latch.countDown();
            }
        }

    }

    private static class RUEmulator extends ParserResultTask implements Utilities.IndexingStatus {

        private volatile boolean finished = true;

        public void scan () {
            Utilities.scheduleSpecialTask(this);
        }

        public void reset () {
            finished = true;
        }

        public boolean isScanInProgress() {
            return finished;
        }

        @Override
        public int getPriority() {
            return 0;
        }

        @Override
        public Class<? extends TaskScheduler> getSchedulerClass() {
            return null;
        }

        @Override
        public void cancel() {            
        }

        @Override
        public void run(Result result) {
            finished = false;
        }

    }


    private static class FooParser extends Parser {

        static int getResultCount = 0;
        
        static int parseCount = 0;

        private Snapshot last;

        public void parse (Snapshot snapshot, Task task, SchedulerEvent event) throws ParseException {
            parseCount++;
            last = snapshot;
        }

        public Result getResult (Task task, SchedulerEvent event) throws ParseException {
            getResultCount++;
            return new Result (last) {
                public void invalidate () {
                }
            };
        }

        public void cancel () {
        }

        public void addChangeListener (ChangeListener changeListener) {
        }

        public void removeChangeListener (ChangeListener changeListener) {
        }

        public String toString () {
            return "FooParser";
        }
    }

    private static class FooParserFactory extends ParserFactory {

        static int createParserCount = 0;

        public Parser createParser (Collection<Snapshot> snapshots2) {
            createParserCount++;
            return new FooParser();
        }
    }

}
