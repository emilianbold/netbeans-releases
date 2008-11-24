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

import java.util.Collections;
import java.util.Iterator;
import org.netbeans.modules.parsing.api.*;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import javax.swing.event.ChangeListener;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.spi.CursorMovedSchedulerEvent;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author hanz
 */
public class SchedulerTaskTest extends NbTestCase {
    
    public SchedulerTaskTest (String testName) {
        super (testName);
    }

    /**
     * Complex tests checking embedding, custom schedulers, sharing of events, 
     * snapshots and parser results.
     * 
     * @throws java.lang.Exception
     */
    public void testEmbedding () throws Exception {

        // 1) register tasks and parsers
        MockServices.setServices (MockMimeLookup.class, MyScheduler.class);
        final CountDownLatch        latch1 = new CountDownLatch (2);
        final CountDownLatch        latch2 = new CountDownLatch (4);
        final StringBuilder         sb = new StringBuilder ();

        final int[]                 fooParser = {1};
        final int[]                 fooParserResult = {1};
        final int[]                 fooEmbeddingProvider = {1};
        final int[]                 fooTask = {1};
        final int[]                 booParser = {1};
        final int[]                 booParserResult = {1};
        final int[]                 booTask = {1};
        final List<Snapshot>        snapshots = new ArrayList<Snapshot> ();

        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"),
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    return new Parser () {

                        private Snapshot last;
                        private int i = fooParser [0]++;

                        public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                            sb.append ("foo parse " + i + " (" + snapshot + ", " + task + ", " + event + "), \n");
                            last = snapshot;
                            snapshots.add (snapshot);
                        }

                        public Result getResult (Task task) throws ParseException {
                            sb.append ("foo get result " + i + " (" + task + "), \n");
                            return new Result (last) {

                                private int i = fooParserResult [0]++;

                                public void invalidate () {
                                    sb.append ("foo invalidate " + i + ", \n");
                                }

                                @Override
                                public String toString () {return "FooResult " + i + " " + getSnapshot ();}
                            };
                        }

                        public void cancel () {
                        }

                        public void addChangeListener (ChangeListener changeListener) {
                        }

                        public void removeChangeListener (ChangeListener changeListener) {
                        }

                    @Override
                        public String toString () {
                            return "FooParser";
                        }
                    };
                }
            },
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new EmbeddingProvider() {

                            private int i = fooEmbeddingProvider [0]++;

                            public List<Embedding> getEmbeddings (Snapshot snapshot) {
                                sb.append ("foo get embeddings " + i + " (" + snapshot + "), \n");
                                return Arrays.asList (new Embedding[] {
                                    snapshot.create (10, 10, "text/boo")
                                });
                            }

                            public int getPriority () {
                                return 10;
                            }

                            public void cancel () {
                            }

                            @Override
                            public String toString () {
                                return "FooEmbeddingProvider " + getPriority ();
                            }
                        },
                        new ParserResultTask () {

                            private int i = fooTask [0]++;

                            public void run (Result result, SchedulerEvent event) {
                                sb.append ("foo task " + i + " (" + result + ", " + event + "), \n");
                            }

                            public int getPriority () {
                                return 100;
                            }

                            public Class<? extends Scheduler> getSchedulerClass () {
                                return MyScheduler.class;
                            }

                            public void cancel () {
                            }

                            @Override
                            public String toString () {
                                return "FooParserResultTask " + i;
                            }
                        },
                        new ParserResultTask () {

                            private int i = fooTask [0]++;

                            public void run (Result result, SchedulerEvent event) {
                                sb.append ("foo task " + i + " (" + result + ", " + event + "), \n");
                            }

                            public int getPriority () {
                                return 101;
                            }

                            public Class<? extends Scheduler> getSchedulerClass () {
                                return MyScheduler.class;
                            }

                            public void cancel () {
                            }

                            @Override
                            public String toString () {
                                return "FooParserResultTask " + i;
                            }
                        }
                    });
                }
            }

        );
        MockMimeLookup.setInstances (
            MimePath.get ("text/boo"),
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    return new Parser () {

                        private Snapshot last;
                        private int i = booParser [0]++;

                        public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                            sb.append ("boo parse " + i + " (" + snapshot + ", " + task + ", " + event + "), \n");
                            last = snapshot;
                            snapshots.add (snapshot);
                        }

                        public Result getResult (Task task) throws ParseException {
                            sb.append ("boo get result " + i + " (" + task + "), \n");
                            return new Result (last) {

                                private int i = booParserResult [0]++;

                                public void invalidate () {
                                    sb.append ("boo invalidate " + i + ", \n");
                                    latch1.countDown ();
                                    latch2.countDown ();
                                }

                                @Override
                                public String toString () {return "BooResult " + i + " " + getSnapshot ();}
                            };
                        }

                        public void cancel () {
                        }

                        public void addChangeListener (ChangeListener changeListener) {
                        }

                        public void removeChangeListener (ChangeListener changeListener) {
                        }

                        @Override
                        public String toString () {
                            return "BooParser";
                        }
                    };
                }
            },
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new ParserResultTask () {

                            private int i = booTask [0]++;

                            public void run (Result result, SchedulerEvent event) {
                                sb.append ("boo task " + i + " (" + result + ", " + event + "), \n");
                            }

                            public int getPriority () {
                                return 150;
                            }

                            public Class<? extends Scheduler> getSchedulerClass () {
                                return MyScheduler.class;
                            }

                            public void cancel () {
                            }

                            @Override
                            public String toString () {
                                return "BooParserResultTask " + i;
                            }
                        },
                        new ParserResultTask () {

                            private int i = booTask [0]++;

                            public void run (Result result, SchedulerEvent event) {
                                sb.append ("boo task " + i + " (" + result + ", " + event + "), \n");
                            }

                            public int getPriority () {
                                return 151;
                            }

                            public Class<? extends Scheduler> getSchedulerClass () {
                                return MyScheduler.class;
                            }

                            public void cancel () {
                            }

                            @Override
                            public String toString () {
                                return "BooParserResultTask " + i;
                            }
                        }
                    });
                }
            }

        );

        // 2) create source file
        clearWorkDir ();
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        FileObject testFile = FileUtil.createData (workDir, "bla.foo");
        FileUtil.setMIMEType ("foo", "text/foo");
        OutputStreamWriter writer = new OutputStreamWriter (testFile.getOutputStream ());
        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!");
        writer.close ();
        Source source = Source.create (testFile);
        sb.append ("1\n");

        SchedulerEvent event1 = new ASchedulerEvent ();
        MyScheduler.schedule2 (source, event1);
        latch1.await ();
        sb.append ("2\n");

        SchedulerEvent event2 = new ASchedulerEvent ();
        MyScheduler.schedule2 (source, event2);
        latch2.await ();
        sb.append ("3\n");

        assertEquals (
            "1\n" +
            "foo get embeddings 1 (" + snapshots.get (0) + "), \n" +
            "foo parse 1 (" + snapshots.get (0) + ", FooParserResultTask 1, ASourceModificationEvent -1:-1), \n" +// WRONG!!!
            "foo get result 1 (FooParserResultTask 1), \n" +
            "foo task 1 (FooResult 1 " + snapshots.get (0) + ", " + event1 + "), \n" +
            "foo invalidate 1, \n" +
            "foo get result 1 (FooParserResultTask 2), \n" +
            "foo task 2 (FooResult 2 " + snapshots.get (0) + ", " + event1 + "), \n" +
            "foo invalidate 2, \n" +
            "boo parse 1 (" + snapshots.get (1) + ", BooParserResultTask 1, ASourceModificationEvent -1:-1), \n" +// WRONG!!!
            "boo get result 1 (BooParserResultTask 1), \n" +
            "boo task 1 (BooResult 1 " + snapshots.get (1) + ", " + event1 + "), \n" +
            "boo invalidate 1, \n" +
            "boo get result 1 (BooParserResultTask 2), \n" +
            "boo task 2 (BooResult 2 " + snapshots.get (1) + ", " + event1 + "), \n" +
            "boo invalidate 2, \n" +
            "2\n" +
            "foo get embeddings 1 (" + snapshots.get (0) + "), \n" + // WRONG!!!
            "foo get result 1 (FooParserResultTask 1), \n" +
            "foo task 1 (FooResult 3 " + snapshots.get (0) + ", " + event2 + "), \n" +
            "foo invalidate 3, \n" +
            "foo get result 1 (FooParserResultTask 2), \n" +
            "foo task 2 (FooResult 4 " + snapshots.get (0) + ", " + event2 + "), \n" +
            "foo invalidate 4, \n" +
            "boo get result 1 (BooParserResultTask 1), \n" +
            "boo task 1 (BooResult 3 " + snapshots.get (1) + ", " + event2 + "), \n" +
            "boo invalidate 3, \n" +
            "boo get result 1 (BooParserResultTask 2), \n" +
            "boo task 2 (BooResult 4 " + snapshots.get (1) + ", " + event2 + "), \n" +
            "boo invalidate 4, \n" +
            "3\n"
            ,sb.toString ());
    }

    public void testCaching () throws Exception {

        // 1) register tasks and parsers
        MockServices.setServices (MockMimeLookup.class, MyScheduler.class);
        final CountDownLatch        latch1 = new CountDownLatch (1);
        final CountDownLatch        latch2 = new CountDownLatch (3);
        final StringBuilder         sb = new StringBuilder ();

        final int[]                 fooParser = {1};
        final int[]                 fooParserResult = {1};
        final int[]                 fooEmbeddingProvider = {1};
        final int[]                 fooTask = {1};
        final int[]                 booParser = {1};
        final int[]                 booParserResult = {1};
        final int[]                 booTask = {1};
        final List<Snapshot>        snapshots = new ArrayList<Snapshot> ();
        final List<SourceModificationEvent>  events = new ArrayList<SourceModificationEvent> ();

        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"),
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    return new Parser () {

                        private Snapshot last;
                        private int i = fooParser [0]++;

                        public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                            sb.append ("foo parse " + i + " (" + snapshot + ", " + task + ", " + event + "), \n");
                            last = snapshot;
                            snapshots.add (snapshot);
                            events.add (event);
                        }

                        public Result getResult (Task task) throws ParseException {
                            sb.append ("foo get result " + i + " (" + task + "), \n");
                            return new Result (last) {

                                private int i = fooParserResult [0]++;

                                public void invalidate () {
                                    sb.append ("foo invalidate " + i + ", \n");
                                }

                                @Override
                                public String toString () {return "FooResult " + i + " " + getSnapshot ();}
                            };
                        }

                        public void cancel () {}

                        public void addChangeListener (ChangeListener changeListener) {}

                        public void removeChangeListener (ChangeListener changeListener) {}

                        @Override public String toString () {return "FooParser";}
                    };
                }
            },
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new EmbeddingProvider() {

                            private int i = fooEmbeddingProvider [0]++;

                            public List<Embedding> getEmbeddings (Snapshot snapshot) {
                                sb.append ("foo get embeddings " + i + " (" + snapshot + "), \n");
                                return Arrays.asList (new Embedding[] {
                                    snapshot.create (10, 10, "text/boo")
                                });
                            }

                            public int getPriority () {return 10;}

                            public void cancel () {}

                            @Override public String toString () {return "FooEmbeddingProvider " + getPriority ();}
                        },
                        new ParserResultTask () {

                            private int i = fooTask [0]++;

                            public void run (Result result, SchedulerEvent event) {
                                sb.append ("foo task " + i + " (" + result + ", " + event + "), \n");
                            }

                            public int getPriority () {
                                return 100;
                            }

                            public Class<? extends Scheduler> getSchedulerClass () {
                                return MyScheduler.class;
                            }

                            public void cancel () {
                            }

                            @Override
                            public String toString () {
                                return "FooParserResultTask " + i;
                            }
                        }

                    });
                }
            }

        );
        MockMimeLookup.setInstances (
            MimePath.get ("text/boo"),
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    return new Parser () {

                        private Snapshot last;
                        private int i = booParser [0]++;

                        public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                            sb.append ("boo parse " + i + " (" + snapshot + ", " + task + ", " + event + "), \n");
                            last = snapshot;
                            snapshots.add (snapshot);
                            events.add (event);
                        }

                        public Result getResult (Task task) throws ParseException {
                            sb.append ("boo get result " + i + " (" + task + "), \n");
                            return new Result (last) {

                                private int i = booParserResult [0]++;

                                public void invalidate () {
                                    sb.append ("boo invalidate " + i + ", \n");
                                    latch1.countDown ();
                                    latch2.countDown ();
                                }

                                @Override public String toString () {return "BooResult " + i + " " + getSnapshot ();}
                            };
                        }

                        public void cancel () {}

                        public void addChangeListener (ChangeListener changeListener) {}

                        public void removeChangeListener (ChangeListener changeListener) {}

                        @Override public String toString () {return "BooParser";}
                    };
                }
            },
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new ParserResultTask () {

                            private int i = booTask [0]++;

                            public void run (Result result, SchedulerEvent event) {
                                sb.append ("boo task " + i + " (" + result + ", " + event + "), \n");
                            }

                            public int getPriority () {return 150;}

                            public Class<? extends Scheduler> getSchedulerClass () {return MyScheduler.class;}

                            public void cancel () {}

                            @Override public String toString () {return "BooParserResultTask " + i;}
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
        sb.append ("1\n");

        SchedulerEvent event1 = new ASchedulerEvent ();
        MyScheduler.schedule2 (source, event1);
        latch1.await ();
        sb.append ("2\n");

        ParserManager.parse (
            Collections.<Source>singleton (source),
            new UserTask () {
                @Override
                public void run (ResultIterator resultIterator) throws Exception {
                    sb.append ("user task (" + resultIterator.getSnapshot () + ", " + resultIterator.getParserResult () + "), \n");
                    Iterator<Embedding> it = resultIterator.getEmbeddings ().iterator ();
                    Embedding embedding = it.next ();
                    ResultIterator resultIterator1 = resultIterator.getResultIterator (embedding);
                    sb.append ("user task - embedding " + embedding.getMimeType () + " (" + resultIterator1.getSnapshot () + ", " + resultIterator1.getParserResult () + "), \n");
                    sb.append ("" + it.hasNext () + ", \n");
                }
                @Override public String toString () {return "MyUserTask";}
            }
        );
        sb.append ("3\n");

        SchedulerEvent event2 = new ASchedulerEvent ();
        MyScheduler.schedule2 (source, event2);
        latch2.await ();
        sb.append ("4\n");

        assertEquals (
            "1\n" +
            "foo get embeddings 1 (" + snapshots.get (0) + "), \n" +
            "foo parse 1 (" + snapshots.get (0) + ", FooParserResultTask 1, ASourceModificationEvent -1:-1), \n" +
            "foo get result 1 (FooParserResultTask 1), \n" +
            "foo task 1 (FooResult 1 " + snapshots.get (0) + ", " + event1 + "), \n" +
            "foo invalidate 1, \n" +
            "boo parse 1 (" + snapshots.get (1) + ", BooParserResultTask 1, ASourceModificationEvent -1:-1), \n" +
            "boo get result 1 (BooParserResultTask 1), \n" +
            "boo task 1 (BooResult 1 " + snapshots.get (1) + ", " + event1 + "), \n" +
            "boo invalidate 1, \n" +
            "2\n" +
            "foo get result 1 (MyUserTask), \n" +
            "user task (" + snapshots.get (0) + ", FooResult 2 " + snapshots.get (0) + "), \n" +
            "boo get result 1 (MyUserTask), \n" +
            "user task - embedding text/boo (" + snapshots.get (1) + ", BooResult 2 " + snapshots.get (1) + "), \n" +
            "false, \n" +
            "foo invalidate 2, \n" +
            "boo invalidate 2, \n" +
            "3\n" +
            "foo get embeddings 1 (" + snapshots.get (0) + "), \n" + //XXX:DELETE!
            "foo get result 1 (FooParserResultTask 1), \n" +
            "foo task 1 (FooResult 3 " + snapshots.get (0) + ", " + event2 + "), \n" +
            "foo invalidate 3, \n" +
            "boo get result 1 (BooParserResultTask 1), \n" +
            "boo task 1 (BooResult 3 " + snapshots.get (1) + ", " + event2 + "), \n" +
            "boo invalidate 3, \n" +
            "4\n"
            ,sb.toString ());
    }

    public void testCaretScheduler () throws Exception {

        // 1) register tasks and parsers
        MockServices.setServices (MockMimeLookup.class, MyScheduler.class);
        final CountDownLatch        latch1 = new CountDownLatch (1);
        final StringBuilder         sb = new StringBuilder ();

        final int[]                 fooParser = {1};
        final int[]                 fooParserResult = {1};
        final int[]                 fooEmbeddingProvider = {1};
        final int[]                 fooTask = {1};
        final int[]                 booParser = {1};
        final int[]                 booParserResult = {1};
        final int[]                 booTask = {1};
        final List<Snapshot>        snapshots = new ArrayList<Snapshot> ();
        final List<SourceModificationEvent> sourceEvents = new ArrayList<SourceModificationEvent> ();

        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"),
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    return new Parser () {

                        private Snapshot last;
                        private int i = fooParser [0]++;

                        public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                            sb.append ("foo parse " + i + " (" + snapshot + ", " + task + ", " + event + "), \n");
                            last = snapshot;
                            snapshots.add (snapshot);
                            sourceEvents.add (event);
                        }

                        public Result getResult (Task task) throws ParseException {
//                            CursorMovedSchedulerEvent cevent = (CursorMovedSchedulerEvent) event;
//                            assertEquals (666, cevent.getCaretOffset ());
                            sb.append ("foo get result " + i + " (" + task + "), \n");
                            return new Result (last) {

                                private int i = fooParserResult [0]++;

                                public void invalidate () {
                                    sb.append ("foo invalidate " + i + ", \n");
                                }

                                @Override
                                public String toString () {return "FooResult " + i + " " + getSnapshot ();}
                            };
                        }

                        public void cancel () {
                        }

                        public void addChangeListener (ChangeListener changeListener) {
                        }

                        public void removeChangeListener (ChangeListener changeListener) {
                        }

                    @Override
                        public String toString () {
                            return "FooParser";
                        }
                    };
                }
            },
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new EmbeddingProvider () {

                            private int i = fooEmbeddingProvider [0]++;

                            public List<Embedding> getEmbeddings (Snapshot snapshot) {
                                sb.append ("foo get embeddings " + i + " (" + snapshot + "), \n");
                                return Arrays.asList (new Embedding[] {
                                    snapshot.create (10, 10, "text/boo")
                                });
                            }

                            public int getPriority () {
                                return 10;
                            }

                            public void cancel () {
                            }

                            @Override
                            public String toString () {
                                return "FooEmbeddingProvider " + getPriority ();
                            }
                        },
                        new ParserResultTask () {

                            private int i = fooTask [0]++;

                            public void run (Result result, SchedulerEvent event) {
                                sb.append ("foo task " + i + " (" + result + ", " + event + "), \n");
                            }

                            public int getPriority () {
                                return 100;
                            }

                            public Class<? extends Scheduler> getSchedulerClass () {
                                return MyScheduler.class;
                            }

                            public void cancel () {
                            }

                            @Override
                            public String toString () {
                                return "FooParserResultTask " + i;
                            }
                        }

                    });
                }
            }

        );
        MockMimeLookup.setInstances (
            MimePath.get ("text/boo"),
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    return new Parser () {

                        private Snapshot last;
                        private int i = booParser [0]++;

                        public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                            sb.append ("boo parse " + i + " (" + snapshot + ", " + task + ", " + event + "), \n");
                            last = snapshot;
                            snapshots.add (snapshot);
                            sourceEvents.add (event);
                        }

                        public Result getResult (Task task) throws ParseException {
//                            CursorMovedSchedulerEvent cevent = (CursorMovedSchedulerEvent) event;
//                            assertEquals (666, cevent.getCaretOffset ());
                            sb.append ("boo get result " + i + " (" + task + "), \n");
                            return new Result (last) {

                                private int i = booParserResult [0]++;

                                public void invalidate () {
                                    sb.append ("boo invalidate " + i + ", \n");
                                    latch1.countDown ();
                                }

                                @Override
                                public String toString () {return "BooResult " + i + " " + getSnapshot ();}
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
            },
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new ParserResultTask () {

                            private int i = booTask [0]++;

                            public void run (Result result, SchedulerEvent event) {
                                sb.append ("boo task " + i + " (" + result + ", " + event + "), \n");
                            }

                            public int getPriority () {
                                return 150;
                            }

                            public Class<? extends Scheduler> getSchedulerClass () {
                                return MyScheduler.class;
                            }

                            public void cancel () {
                            }

                            @Override
                            public String toString () {
                                return "BooParserResultTask " + i;
                            }
                        }

                    });
                }
            }

        );

        // 2) create source file
        clearWorkDir ();
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        FileObject testFile = FileUtil.createData (workDir, "bla.foo");
        FileUtil.setMIMEType ("foo", "text/foo");
        OutputStream outputStream = testFile.getOutputStream ();
        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!");
        writer.close ();
        Source source = Source.create (testFile);
        sb.append ("1\n");
        CursorMovedSchedulerEvent event1 = new CursorMovedSchedulerEvent (MyScheduler.class, 666) {};
        MyScheduler.schedule2 (source, event1);
        latch1.await ();
        sb.append ("2\n");

        assertEquals (
            "1\n" +
            "foo get embeddings 1 (" + snapshots.get (0) + "), \n" +
            "foo parse 1 (" + snapshots.get (0) + ", FooParserResultTask 1, ASourceModificationEvent -1:-1), \n" +
            "foo get result 1 (FooParserResultTask 1), \n" +
            "foo task 1 (FooResult 1 " + snapshots.get (0) + ", " + event1 + "), \n" +
            "foo invalidate 1, \n" +
            "boo parse 1 (" + snapshots.get (1) + ", BooParserResultTask 1, ASourceModificationEvent -1:-1), \n" +
            "boo get result 1 (BooParserResultTask 1), \n" +
            "boo task 1 (BooResult 1 " + snapshots.get (1) + ", " + event1 + "), \n" +
            "boo invalidate 1, \n" +
            "2\n"
            ,sb.toString ());

    }

    /**
     * @throws java.lang.Exception
     */
    public void testDocumentModification () throws Exception {

        // 1) register tasks and parsers
        MockServices.setServices (MockMimeLookup.class, MyScheduler.class);
        final CountDownLatch        latch1 = new CountDownLatch (1);
        final CountDownLatch        latch2 = new CountDownLatch (2);
        final StringBuilder         sb = new StringBuilder ();

        final int[]                 fooParser = {1};
        final int[]                 fooParserResult = {1};
        final int[]                 fooEmbeddingProvider = {1};
        final int[]                 fooTask = {1};
        final int[]                 booParser = {1};
        final int[]                 booParserResult = {1};
        final int[]                 booTask = {1};
        final List<Snapshot>        snapshots = new ArrayList<Snapshot> ();
        final List<SourceModificationEvent> sourceEvents = new ArrayList<SourceModificationEvent> ();
        final List<SchedulerEvent>  schedulerEvents = new ArrayList<SchedulerEvent> ();

        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"),
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    return new Parser () {

                        private Snapshot        last;
                        private int             i = fooParser [0]++;

                        public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                            sb.append ("foo parse " + i + " (" + snapshot + ", " + task + ", " + event + "), \n");
                            last = snapshot;
                            snapshots.add (snapshot);
                            sourceEvents.add (event);
                        }

                        public Result getResult (Task task) throws ParseException {
                            sb.append ("foo get result " + i + " (" + task + "), \n");
                            return new Result (last) {

                                public void invalidate () {
                                    sb.append ("foo invalidate " + i + ", \n");
                                }

                                private int i = fooParserResult [0]++;

                                @Override
                                public String toString () {return "FooResult " + i + " " + getSnapshot ();}
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
            },
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new EmbeddingProvider () {

                            private int i = fooEmbeddingProvider [0]++;

                            public List<Embedding> getEmbeddings (Snapshot snapshot) {
                                sb.append ("foo get embeddings " + i + " (" + snapshot + "), \n");
                                return Arrays.asList (new Embedding[] {
                                    snapshot.create (10, 10, "text/boo")
                                });
                            }

                            public int getPriority () {
                                return 10;
                            }

                            public void cancel () {
                            }
                        },
                        new ParserResultTask () {


                            public void run (Result result, SchedulerEvent event) {
                                sb.append ("foo task " + i + " (" + result + ", " + event + "), \n");
                                schedulerEvents.add (event);
                            }

                            public int getPriority () {
                                return 100;
                            }

                            public Class<? extends Scheduler> getSchedulerClass () {
                                return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
                            }

                            public void cancel () {
                            }

                            private int i = fooTask [0]++;

                            @Override
                            public String toString () {
                                return "FooParserResultTask " + i;
                            }
                        }

                    });
                }
            }

        );
        MockMimeLookup.setInstances (
            MimePath.get ("text/boo"),
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    return new Parser () {

                        private Snapshot last;
                        private int i = booParser [0]++;

                        public void parse (Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException {
                            sb.append ("boo parse " + i + " (" + snapshot + ", " + task + ", " + event + "), \n");
                            last = snapshot;
                            snapshots.add (snapshot);
                            sourceEvents.add (event);
                        }

                        public Result getResult (Task task) throws ParseException {
                            sb.append ("boo get result " + i + " (" + task + "), \n");
                            return new Result (last) {
                                public void invalidate () {
                                    sb.append ("boo invalidate " + i + ", \n");
                                    latch1.countDown ();
                                    latch2.countDown ();
                                }

                                private int i = booParserResult [0]++;

                                @Override
                                public String toString () {return "BooResult " + i + " " + getSnapshot ();}
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
            },
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new ParserResultTask () {

                            private int i = booTask [0]++;

                            public void run (Result result, SchedulerEvent event) {
                                sb.append ("boo task " + i + " (" + result + ", " + event + "), \n");
                            }

                            public int getPriority () {
                                return 150;
                            }

                            public Class<? extends Scheduler> getSchedulerClass () {
                                return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
                            }

                            public void cancel () {
                            }

                            @Override
                            public String toString () {
                                return "BooParserResultTask " + i;
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

        sb.append ("1\n");

        for (Scheduler scheduler : Schedulers.getSchedulers ())
            if (scheduler instanceof CurrentDocumentScheduler)
                ((CurrentDocumentScheduler) scheduler).schedule (source);

        latch1.await ();//10, TimeUnit.SECONDS);
        sb.append ("2\n");

        outputStream = testFile.getOutputStream ();
        writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci file (druha verze), na kterem se budou delat hnusne pokusy!!!");
        writer.close ();
        latch2.await ();//10, TimeUnit.SECONDS);
        sb.append ("3\n");

        assertEquals (
            "1\n" +
            "foo get embeddings 1 (" + snapshots.get (0) + "), \n" +
            "foo parse 1 (" + snapshots.get (0) + ", FooParserResultTask 1, " + sourceEvents.get (0) + "), \n" +
            "foo get result 1 (FooParserResultTask 1), \n" +
            "foo task 1 (FooResult 1 " + snapshots.get (0) + ", " + schedulerEvents.get (0) + "), \n" +
            "foo invalidate 1, \n" +
            "boo parse 1 (" + snapshots.get (1) + ", BooParserResultTask 1, " + sourceEvents.get (0) + "), \n" +
            "boo get result 1 (BooParserResultTask 1), \n" +
            "boo task 1 (BooResult 1 " + snapshots.get (1) + ", " + schedulerEvents.get (0) + "), \n" +
            "boo invalidate 1, \n" +
            "2\n" +
            "foo get embeddings 1 (" + snapshots.get (2) + "), \n" +
            "foo get embeddings 1 (" + snapshots.get (2) + "), \n" + // WRONG!!!
            "foo get embeddings 1 (" + snapshots.get (2) + "), \n" + // WRONG!!!
            "foo get embeddings 1 (" + snapshots.get (2) + "), \n" + // WRONG!!!
            "foo parse 1 (" + snapshots.get (2) + ", FooParserResultTask 1, " + sourceEvents.get (1) + "), \n" +
            "foo get result 1 (FooParserResultTask 1), \n" +
            "foo task 1 (FooResult 2 " + snapshots.get (2) + ", " + schedulerEvents.get (1) + "), \n" +
            "foo invalidate 2, \n" +
            "boo parse 1 (" + snapshots.get (3) + ", BooParserResultTask 1, " + sourceEvents.get (1) + "), \n" +
            "boo get result 1 (BooParserResultTask 1), \n" +
            "boo task 1 (BooResult 2 " + snapshots.get (3) + ", " + schedulerEvents.get (1) + "), \n" +
            "boo invalidate 2, \n" +
            "3\n", sb.toString ());
    }
}







