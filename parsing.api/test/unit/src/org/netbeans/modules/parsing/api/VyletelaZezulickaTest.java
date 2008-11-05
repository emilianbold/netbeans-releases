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

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Stack;
import javax.swing.event.ChangeListener;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author hanz
 */
public class VyletelaZezulickaTest extends NbTestCase {
    
    public VyletelaZezulickaTest (String testName) {
        super (testName);
    }

    private static final MyScheduler SCHEDULER = new MyScheduler ();

    public void testEmbedding () throws Exception {
        
        // 1) register tasks and parsers
        MockServices.setServices (MockMimeLookup.class, MyScheduler.class);
        final Counter counter = new Counter (8);
        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"), 
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {                                                            
                    return new Parser () {
                        
                        private Snapshot last;

                        public void parse (Snapshot snapshot, Task task, SchedulerEvent event) throws ParseException {
                            last = snapshot;
                        }

                        public Result getResult (Task task, SchedulerEvent event) throws ParseException {
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
                    };
                }
            },
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new ParserResultTask () {

                            boolean done = false;
                            
                            public void run (Result result) {
                                if (!done) {
                                    counter.check ("text/foo", result.getSnapshot().getMimeType ());
                                    counter.check (1);
                                    counter.wait (4);
                                    try {
                                        final String original = result.getSnapshot().getText ().toString ();
                                        ParserManager.parse (
                                            Collections.<Source>singleton (result.getSnapshot().getSource ()), 
                                            new UserTask() {
                                                public void run (ResultIterator resultIterator) throws Exception {
                                                    Result result = resultIterator.getParserResult (1);
                                                    counter.check (original, result.getSnapshot().getText ().toString ());
                                                    counter.check (5);
                                                }
                                            }
                                        );
                                        counter.check (6);
                                    } catch (ParseException ex) {
                                    }
                                }
                                done = true;
                            }

                            public int getPriority () {
                                return 100;
                            }

                            public Class<? extends Scheduler> getSchedulerClass () {
                                return MyScheduler.class;
                            }

                            public void cancel () {
                            }
    
                            public String toString () {
                                return "FooParserResultTask " + getPriority ();
                            }
                        }
                    });
                }
            }
        );
        
        // 2) create source file
        clearWorkDir ();
        //Collection c = MimeLookup.getLookup("text/boo").lookupAll (ParserFactory.class);
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        FileObject testFile = FileUtil.createData (workDir, "bla.foo");
        FileUtil.setMIMEType ("foo", "text/foo");
        OutputStream outputStream = testFile.getOutputStream ();
        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!");
        writer.close ();
        Source source = Source.create (testFile);

        MyScheduler.schedule (
            Collections.<Source>singleton (source), 
            new ASchedulerEvent ()
        );

        counter.wait (2);

        outputStream = testFile.getOutputStream ();
        writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci2 file, na kterem se budou delat hnusne pokusy!!!");
        writer.close ();

        counter.check (3);
        
        ParserManager.parse (
            Collections.<Source> singleton (source),
            new UserTask () {
                public void run (ResultIterator resultIterator) throws Exception {
                    Result result = resultIterator.getParserResult (1);
                    counter.check ("Toto je testovaci2 file, na kterem se budou delat hnusne pokusy!!!", result.getSnapshot().getText ().toString ());
                    counter.wait (7);
                }
            }
        );
        
        counter.wait (8);
        assertEquals (null, counter.errorMessage (true));
    }
}







