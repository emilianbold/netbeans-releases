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
import java.util.List;
import java.util.Stack;
import javax.swing.event.ChangeListener;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.TaskScheduler;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author hanz
 */
public class RunUserActionTaskTest extends NbTestCase {
    
    public RunUserActionTaskTest (String testName) {
        super (testName);
    }

    /**
     * Creates one embedding and calls user task on it. Tests ordering of calls:
     * 1) EmbeddingProvider.getEmbeddings
     * 2) ParserFactory.createParser
     * 3) Parser.parse
     * 4) Parser.getResult
     * 5) run user action task
     * 6) continue...
     */
    public void testEmbedding () throws Exception {
        MockServices.setServices (MockMimeLookup.class);
        final Counter counter = new Counter ();
        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"), 
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    counter.check (2);
                    return Arrays.asList (new SchedulerTask[] {
                        new EmbeddingProvider() {
                            public List<Embedding> getEmbeddings (Snapshot snapshot) {
                                assertEquals ("text/foo", snapshot.getMimeType ());
                                counter.check (3);
                                return Arrays.asList (new Embedding[] {
                                    snapshot.create (10, 10, "text/boo")
                                });
                            }

                            public int getPriority () {
                                return 10;
                            }

                            public Class<? extends TaskScheduler> getSchedulerClass () {
                                return null;
                            }

                            public void cancel () {
                            }
                        }
                    });
                }
            }
        );
        final Snapshot[] snapshots = new Snapshot [1];
        MockMimeLookup.setInstances (
            MimePath.get ("text/boo"), 
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    assertEquals (1, snapshots2.size ());
                    snapshots[0] = snapshots2.iterator ().next ();
                    assertEquals ("text/boo", snapshots[0].getMimeType ());
                    assertEquals ("stovaci fi", snapshots[0].getText ().toString ());
                    counter.check (4);
                    return new Parser () {

                        public void parse (Snapshot snapshot, Task task, SchedulerEvent event) throws ParseException {
                            assertEquals (snapshot, snapshots [0]);
                            counter.check (5);
                        }

                        public Result getResult (Task task, SchedulerEvent event) throws ParseException {
                            counter.check (6);
                            return new Result () {
                                public void invalidate () {
                                    counter.check (8);
                                }
                            };
                        }

                        public void cancel () {
                        }

                        public void addChangeListener (ChangeListener changeListener) {
                        }

                        public void removeChangeListener (ChangeListener changeListener) {
                        }
                    };
                }
            }
        );
        //Collection c = MimeLookup.getLookup("text/boo").lookupAll (ParserFactory.class);
        clearWorkDir ();
        FileUtil.setMIMEType ("foo", "text/foo");
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        FileObject testFile = FileUtil.createData (workDir, "bla.foo");
        OutputStream outputStream = testFile.getOutputStream ();
        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!");
        writer.close ();
        Source source = Source.create (testFile);
        counter.check (1);
        ParserManager.parse (source, new UserTask() {
            public void run (Result result, Snapshot snapshot) throws Exception {
                counter.check (7);
            }
        }, 15);
        counter.check (9);
    }

    /**
     */
    public void testCachingOfTopLevelParser () throws Exception {
        MockServices.setServices (MockMimeLookup.class);
        final Counter counter = new Counter ();
        final Snapshot[] snapshots = new Snapshot [1];
        final Parser[] parser = new Parser [1];
        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"), 
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    assertEquals (1, snapshots2.size ());
                    snapshots[0] = snapshots2.iterator ().next ();
                    assertEquals ("text/foo", snapshots[0].getMimeType ());
                    counter.check (2);
                    Parser p = new Parser () {

                        public void parse (Snapshot snapshot, Task task, SchedulerEvent event) throws ParseException {
                            assertEquals (snapshot, snapshots [0]);
                            assertEquals (parser [0], this);
                            counter.check (3);
                        }

                        Stack<Integer> s1 = new Stack<Integer> ();
                        {s1.push (8);s1.push (4);}
                        
                        Stack<Integer> s2 = new Stack<Integer> ();
                        {s2.push (10);s2.push (6);}

                        public Result getResult (Task task, SchedulerEvent event) throws ParseException {
                            assertEquals (parser [0], this);
                            counter.check (s1.pop ());
                            return new Result () {
                                public void invalidate () {
                                    counter.check (s2.pop ());
                                }
                            };
                        }

                        public void cancel () {
                        }

                        public void addChangeListener (ChangeListener changeListener) {
                        }

                        public void removeChangeListener (ChangeListener changeListener) {
                        }
                    };
                    parser[0] = p;
                    return p;
                }
            }
        );
        //Collection c = MimeLookup.getLookup("text/boo").lookupAll (ParserFactory.class);
        clearWorkDir ();
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        FileObject testFile = FileUtil.createData (workDir, "bla.foo");
        FileUtil.setMIMEType ("foo", "text/foo");
        OutputStream outputStream = testFile.getOutputStream ();
        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!");
        writer.close ();
        Source source = Source.create (testFile);
        counter.check (1);
        ParserManager.parse (source, new UserTask() {
            public void run (Result result, Snapshot snapshot) throws Exception {
                counter.check (5);
            }
        }, 15);
        counter.check (7); 
        ParserManager.parse (source, new UserTask() {
            public void run (Result result, Snapshot snapshot) throws Exception {
                counter.check (9); 
            }
        }, 15);
        counter.check (11); 
    }
    
    public void testCachingOfSecondLevelParser () throws Exception {
        MockServices.setServices (MockMimeLookup.class);
        final Counter counter = new Counter ();
        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"), 
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new EmbeddingProvider() {
                            public List<Embedding> getEmbeddings (Snapshot snapshot) {
                                assertEquals ("text/foo", snapshot.getMimeType ());
                                counter.check (2);
                                return Arrays.asList (new Embedding[] {
                                    snapshot.create (10, 10, "text/boo")
                                });
                            }

                            public int getPriority () {
                                return 10;
                            }

                            public Class<? extends TaskScheduler> getSchedulerClass () {
                                return null;
                            }

                            public void cancel () {
                            }
                        }
                    });
                }
            }
        );
        final Snapshot[] snapshots = new Snapshot [1];
        MockMimeLookup.setInstances (
            MimePath.get ("text/boo"), 
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    assertEquals (1, snapshots2.size ());
                    snapshots[0] = snapshots2.iterator ().next ();
                    assertEquals ("text/boo", snapshots[0].getMimeType ());
                    assertEquals ("stovaci fi", snapshots[0].getText ().toString ());
                    counter.check (3);
                    return new Parser () {

                        public void parse (Snapshot snapshot, Task task, SchedulerEvent event) throws ParseException {
                            assertEquals (snapshot, snapshots [0]);
                            counter.check (4);
                        }

                        Stack<Integer> s1 = new Stack<Integer> ();
                        {s1.push (9);s1.push (5);}

                        Stack<Integer> s2 = new Stack<Integer> ();
                        {s2.push (11);s2.push (7);}
                        
                        public Result getResult (Task task, SchedulerEvent event) throws ParseException {
                            counter.check (s1.pop ());
                            return new Result () {
                                public void invalidate () {
                                    counter.check (s2.pop ());
                                }
                            };
                        }

                        public void cancel () {
                        }

                        public void addChangeListener (ChangeListener changeListener) {
                        }

                        public void removeChangeListener (ChangeListener changeListener) {
                        }
                    };
                }
            }
        );
        //Collection c = MimeLookup.getLookup("text/boo").lookupAll (ParserFactory.class);
        clearWorkDir ();
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        FileObject testFile = FileUtil.createData (workDir, "bla.foo");
        FileUtil.setMIMEType ("foo", "text/foo");
        OutputStream outputStream = testFile.getOutputStream ();
        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!");
        writer.close ();
        Source source = Source.create (testFile);
        counter.check (1);
        ParserManager.parse (source, new UserTask() {
            public void run (Result result, Snapshot snapshot) throws Exception {
                counter.check (6);
            }
        }, 15);
        counter.check (8);
        ParserManager.parse (source, new UserTask() {
            public void run (Result result, Snapshot snapshot) throws Exception {
                counter.check (10);
            }
        }, 15);
        counter.check (12);
    }
    
    public void testCachingOfSecondLevelParserAfterChange () throws Exception {
        MockServices.setServices (MockMimeLookup.class);
        final Counter counter = new Counter ();
        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"), 
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new EmbeddingProvider() {
                            Stack<Integer> s1 = new Stack<Integer> ();
                            {s1.push (13);s1.push (2);}
                            
                            public List<Embedding> getEmbeddings (Snapshot snapshot) {
                                assertEquals ("text/foo", snapshot.getMimeType ());
                                counter.check (s1.pop ());
                                return Arrays.asList (new Embedding[] {
                                    snapshot.create (10, 10, "text/boo")
                                });
                            }

                            public int getPriority () {
                                return 10;
                            }

                            public Class<? extends TaskScheduler> getSchedulerClass () {
                                return null;
                            }

                            public void cancel () {
                            }
                        }
                    });
                }
            }
        );
        final Snapshot[] snapshots = new Snapshot [1];
        MockMimeLookup.setInstances (
            MimePath.get ("text/boo"), 
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    assertEquals (1, snapshots2.size ());
                    snapshots[0] = snapshots2.iterator ().next ();
                    assertEquals ("text/boo", snapshots[0].getMimeType ());
                    assertEquals ("stovaci fi", snapshots[0].getText ().toString ());
                    counter.check (3);
                    return new Parser () {

                        Stack<Integer> s3 = new Stack<Integer> ();
                        {s3.push (14);s3.push (4);}
                        Stack<String> s4 = new Stack<String> ();
                        {s4.push ("stovaci2 f");s4.push ("stovaci fi");}
                        
                        public void parse (Snapshot snapshot, Task task, SchedulerEvent event) throws ParseException {
                            counter.check (s3.pop ());
                        }

                        Stack<Integer> s1 = new Stack<Integer> ();
                        {s1.push (15);s1.push (9);s1.push (5);}

                        Stack<Integer> s2 = new Stack<Integer> ();
                        {s2.push (17);s2.push (11);s2.push (7);}
                        
                        public Result getResult (Task task, SchedulerEvent event) throws ParseException {
                            counter.check (s1.pop ());
                            return new Result () {
                                public void invalidate () {
                                    counter.check (s2.pop ());
                                }
                            };
                        }

                        public void cancel () {
                        }

                        public void addChangeListener (ChangeListener changeListener) {
                        }

                        public void removeChangeListener (ChangeListener changeListener) {
                        }
                    };
                }
            }
        );
        //Collection c = MimeLookup.getLookup("text/boo").lookupAll (ParserFactory.class);
        clearWorkDir ();
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        FileObject testFile = FileUtil.createData (workDir, "bla.foo");
        FileUtil.setMIMEType ("foo", "text/foo");
        OutputStream outputStream = testFile.getOutputStream ();
        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!");
//        outputStream.close ();
        writer.close ();
        Source source = Source.create (testFile);
        counter.check (1);
        ParserManager.parse (source, new UserTask() {
            public void run (Result result, Snapshot snapshot) throws Exception {
                counter.check (6);
            }
        }, 15);
        counter.check (8);
        ParserManager.parse (source, new UserTask() {
            public void run (Result result, Snapshot snapshot) throws Exception {
                counter.check (10);
            }
        }, 15);
        counter.check (12);
        outputStream = testFile.getOutputStream ();
        writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci2 file, na kterem se budou delat hnusne pokusy!!!");
        writer.close ();
        ParserManager.parse (source, new UserTask() {
            public void run (Result result, Snapshot snapshot) throws Exception {
                counter.check (16);
            }
        }, 15);
        counter.check (18);
    }
    
    private static class Counter {
        private int counter = 1;
        
        void check (int count) {
            assertEquals (count, counter++);
        }
    }
}







