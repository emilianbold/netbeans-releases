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
import java.util.Iterator;
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
import org.netbeans.modules.parsing.spi.ParserResultTask;
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
public class SchedulerTaskTest extends NbTestCase {
    
    public SchedulerTaskTest (String testName) {
        super (testName);
    }

    private static final MyScheduler SCHEDULER = new MyScheduler ();
    
    public void testEmbedding () throws Exception {
        
        // 1) register tasks and parsers
        MockServices.setServices (MockMimeLookup.class, MyScheduler.class);
        final Counter counter = new Counter (9);
        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"), 
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    return new Parser () {

                        public void parse (Snapshot snapshot, Task task, SchedulerEvent event) throws ParseException {
                            counter.check (2);
                        }

                        public Result getResult (Task task, SchedulerEvent event) throws ParseException {
                            counter.check (3);
                            return new Result () {
                                public void invalidate () {
                                    counter.check (5);
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
                        new EmbeddingProvider() {
                            public List<Embedding> getEmbeddings (Snapshot snapshot) {
                                counter.check (1);
                                return Arrays.asList (new Embedding[] {
                                    snapshot.create (10, 10, "text/boo")
                                });
                            }

                            public int getPriority () {
                                return 10;
                            }

                            public Class<? extends TaskScheduler> getSchedulerClass () {
                                return MyScheduler.class;
                            }

                            public void cancel () {
                            }
    
                            public String toString () {
                                return "FooEmbeddingProvider " + getPriority ();
                            }
                        },
                        new ParserResultTask () {

                            public void run (Result result, Snapshot snapshot) {
                                counter.check ("text/foo", snapshot.getMimeType ());
                                counter.check (4);
                            }

                            public int getPriority () {
                                return 100;
                            }

                            public Class<? extends TaskScheduler> getSchedulerClass () {
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
        MockMimeLookup.setInstances (
            MimePath.get ("text/boo"), 
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    return new Parser () {

                        public void parse (Snapshot snapshot, Task task, SchedulerEvent event) throws ParseException {
                            counter.check ("text/boo", snapshot.getMimeType ());
                            counter.check (6);
                        }

                        public Result getResult (Task task, SchedulerEvent event) throws ParseException {
                            counter.check (7);
                            return new Result () {
                                public void invalidate () {
                                    counter.check (9);
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
                            return "BooParser";
                        }
                    };
                }
            },
            new TaskFactory () {
                public Collection<SchedulerTask> create (Snapshot snapshot) {
                    return Arrays.asList (new SchedulerTask[] {
                        new ParserResultTask () {

                            public void run (Result result, Snapshot snapshot) {
                                counter.check (8);
                            }

                            public int getPriority () {
                                return 150;
                            }

                            public Class<? extends TaskScheduler> getSchedulerClass () {
                                return MyScheduler.class;
                            }

                            public void cancel () {
                            }
    
                            public String toString () {
                                return "BooParserResultTask " + getPriority ();
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
        MyScheduler.schedule (
            Collections.<Source>singleton (source), 
            new ASchedulerEvent ()
        
        );
        assertEquals (null, counter.errorMessage (true));
        System.out.println("");
    }

    public void testCaching () throws Exception {

        // 1) register tasks and parsers
        MockServices.setServices (MockMimeLookup.class, MyScheduler.class);
        final Counter counter = new Counter (24);
        final Object LOCK = new Object ();
        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"), 
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    return new Parser () {

                        public void parse (Snapshot snapshot, Task task, SchedulerEvent event) throws ParseException {
                            counter.check (2);
                        }

                        Stack<Integer> s1 = new Stack<Integer> ();
                        {s1.push (19);s1.push (14);s1.push (3);}

                        Stack<Integer> s2 = new Stack<Integer> ();
                        {s2.push (21);s2.push (16);s2.push (5);}

                        Stack<String> s3 = new Stack<String> ();
                        {s3.push ("foo3");s3.push ("foo2");s3.push ("foo1");}
                        
                        public Result getResult (Task task, SchedulerEvent event) throws ParseException {
                            counter.check (s1.pop ());
                            return new Result () {
                                public void invalidate () {
                                    counter.check (s2.pop ());
                                }
                                private String t = s3.pop ();
                                public String toString () {return t;}
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
                        new EmbeddingProvider() {
                            
                            Stack<Integer> s1 = new Stack<Integer> ();
                            {s1.push (18);s1.push (1);}
                            
                            public List<Embedding> getEmbeddings (Snapshot snapshot) {
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
    
                            public String toString () {
                                return "FooEmbeddingProvider " + getPriority ();
                            }
                        },
                        new ParserResultTask () {

                            Stack<Integer> s1 = new Stack<Integer> ();
                            {s1.push (20);s1.push (4);}
                            
                            Stack<String> results = new Stack<String> ();
                            {results.push ("foo3");results.push ("foo1");}
                            
                            public void run (Result result, Snapshot snapshot) {
                                counter.check ("text/foo", snapshot.getMimeType ());
                                counter.check (results.pop (), result.toString ());
                                counter.check (s1.pop ());
                            }

                            public int getPriority () {
                                return 100;
                            }

                            public Class<? extends TaskScheduler> getSchedulerClass () {
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
        MockMimeLookup.setInstances (
            MimePath.get ("text/boo"), 
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    return new Parser () {

                        public void parse (Snapshot snapshot, Task task, SchedulerEvent event) throws ParseException {
                            counter.check ("text/boo", snapshot.getMimeType ());
                            counter.check (6);
                        }

                        Stack<Integer> s1 = new Stack<Integer> ();
                        {s1.push (22);s1.push (12);s1.push (7);}

                        Stack<Integer> s2 = new Stack<Integer> ();
                        {s2.push (24);s2.push (17);s2.push (9);}

                        Stack<String> results = new Stack<String> ();
                        {results.push ("boo3");results.push ("boo2");results.push ("boo1");}
                        
                        public Result getResult (Task task, SchedulerEvent event) throws ParseException {
                            counter.check (s1.pop ());
                            return new Result () {
                                public void invalidate () {
                                    counter.check (s2.pop ());
                                    synchronized (LOCK) {
                                        LOCK.notify ();
                                    }
                                }
                                private String t = results.pop ();
                                public String toString () {return t;}
                            };
                        }

                        public void cancel () {
                        }

                        public void addChangeListener (ChangeListener changeListener) {
                        }

                        public void removeChangeListener (ChangeListener changeListener) {
                        }
    
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

                            Stack<Integer> s1 = new Stack<Integer> ();
                            {s1.push (23);s1.push (8);}

                            Stack<String> results = new Stack<String> ();
                            {results.push ("boo3");results.push ("boo1");}

                            public void run (Result result, Snapshot snapshot) {
                                counter.check (results.pop (), result.toString ());
                                counter.check ("text/boo", snapshot.getMimeType ());
                                counter.check (s1.pop ());
                            }

                            public int getPriority () {
                                return 150;
                            }

                            public Class<? extends TaskScheduler> getSchedulerClass () {
                                return MyScheduler.class;
                            }

                            public void cancel () {
                            }
    
                            public String toString () {
                                return "BooParserResultTask " + getPriority ();
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
        synchronized (LOCK) {
            LOCK.wait ();
        }
        counter.check (10);
        ParserManager.parse (
            Collections.<Source>singleton (source), 
            new MultiLanguageUserTask () {
                @Override
                public void run (ResultIterator resultIterator) throws Exception {
                    counter.check ("text/foo", resultIterator.getSnapshot ().getMimeType ());
                    Iterator<Embedding> it = resultIterator.getEmbeddings ().iterator ();
                    Embedding embedding = it.next ();
                    counter.check ("text/boo", embedding.getMimeType ());
                    counter.check ("false", Boolean.toString (it.hasNext ()));
                    ResultIterator resultIterator1 = resultIterator.getResultIterator (embedding);
                    counter.check ("text/boo", resultIterator1.getSnapshot ().getMimeType ());
                    counter.check ("false", Boolean.toString (resultIterator1.getEmbeddings ().iterator().hasNext()));
                    counter.check (11);
                    Result result = resultIterator1.getParserResult ();
                    assertEquals ("boo2", result.toString ());
                    counter.check (13);
                    result = resultIterator.getParserResult ();
                    assertEquals ("foo2", result.toString ());
                    counter.check (15);
                }
            }
        );
        assertEquals (null, counter.errorMessage (false));
        assertEquals (18, counter.count ());
        MyScheduler.schedule (Collections.singleton (source), new ASchedulerEvent ());
        assertEquals (null, counter.errorMessage (true));
        System.out.println("");
    }
}







