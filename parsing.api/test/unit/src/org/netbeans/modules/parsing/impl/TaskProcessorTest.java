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

package org.netbeans.modules.parsing.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.impl.indexing.Util;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public class TaskProcessorTest extends NbTestCase {
    
    public TaskProcessorTest(String testName) {
        super(testName);
    }            
    
    public void testWarningWhenRunUserTaskCalledFromAWT() throws Exception {
        this.clearWorkDir();
        final File _wd = this.getWorkDir();
        final FileObject wd = FileUtil.toFileObject(_wd);

        FileUtil.setMIMEType("foo", "text/foo");
        final FileObject foo = wd.createData("file.foo");
        final LogRecord[] warning = new LogRecord[1];
        final String msgPrefix = "ParserManager.parse called in AWT event thread by: ";

        MockMimeLookup.setInstances(MimePath.parse("text/foo"), new FooParserFactory());
        Logger.getLogger(TaskProcessor.class.getName()).addHandler(new Handler() {
            public @Override void publish(LogRecord record) {
                if (record.getMessage().startsWith(msgPrefix)) {
                    warning[0] = record;
                }
            }

            public @Override void flush() {
            }

            public @Override void close() throws SecurityException {
            }
        });

        final StackTraceUserTask stackTraceUserTask = new StackTraceUserTask();
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    ParserManager.parse(Collections.singleton(Source.create(foo)), stackTraceUserTask);
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });

        assertNotNull("No warning when calling ParserManager.parse from AWT", warning[0]);
        assertEquals("Suspiciosly wrong warning message (is the caller identified correctly?)",
                msgPrefix + stackTraceUserTask.caller, warning[0].getMessage());
    }

    private static final class FooParserFactory extends ParserFactory {
        @Override
        public Parser createParser(Collection<Snapshot> snapshots) {
            return new FooParser();
        }
    }

    private static final class FooParser extends Parser {
        private FooParserResult result;

        public @Override void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
            result = new FooParserResult((snapshot));
        }

        public @Override Result getResult(Task task) throws ParseException {
            return result;
        }

        public @Override void cancel() {
        }

        public @Override void addChangeListener(ChangeListener changeListener) {
        }

        public @Override void removeChangeListener(ChangeListener changeListener) {
        }
    }

    private static final class FooParserResult extends Parser.Result {
        public FooParserResult(Snapshot snapshot) {
            super(snapshot);
        }

        protected @Override void invalidate() {
        }
    }

    private static final class StackTraceUserTask extends UserTask {
        public StackTraceElement caller;
        @Override
        public void run(ResultIterator resultIterator) throws Exception {
            ArrayList<StackTraceElement> filteredStackTrace = new ArrayList<StackTraceElement>();
            StackTraceElement [] stackTrace = Thread.currentThread().getStackTrace();
            for(StackTraceElement e : stackTrace) {
                if (!getClass().getName().equals(e.getClassName())) {
                    filteredStackTrace.add(e);
                }
            }
            caller = Util.findCaller(filteredStackTrace.toArray(new StackTraceElement[filteredStackTrace.size()]));
        }
    }
}
