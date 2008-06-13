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
import java.util.List;
import javax.swing.event.ChangeListener;

import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.impl.Installer;
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

    public void testEmbedding () throws Exception {
        MockServices.setServices (MockMimeLookup.class, MyScheduler.class);
        new Installer ().restored ();
        final Counter counter = new Counter (3);
        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"), 
            new ParserFactory () {
                public Parser createParser (Collection<Snapshot> snapshots2) {
                    return new Parser () {

                        public void parse (Snapshot snapshot, Task task, SchedulerEvent event) throws ParseException {
                            counter.check (2);
                        }

                        public Result getResult (Task task, SchedulerEvent event) throws ParseException {
                            return new Result () {
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
                    };
                }
            },
            new TaskFactory () {
                public Collection<SchedulerTask> create (Source source) {
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
                        },
                        new ParserResultTask () {

                            public void run (Result result, Snapshot snapshot) {
                                counter.check (3);
                            }

                            public int getPriority () {
                                return 100;
                            }

                            public Class<? extends TaskScheduler> getSchedulerClass () {
                                return MyScheduler.class;
                            }

                            public void cancel () {
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
                            counter.check (2);
                        }

                        public Result getResult (Task task, SchedulerEvent event) throws ParseException {
                            counter.check (1);
                            return new Result () {
                                public void invalidate () {
                                    counter.check (1);
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
            },
            new TaskFactory () {
                public Collection<SchedulerTask> create (Source source) {
                    return Arrays.asList (new SchedulerTask[] {
                        new ParserResultTask () {

                            public void run (Result result, Snapshot snapshot) {
                                counter.check (1);
                            }

                            public int getPriority () {
                                return 100;
                            }

                            public Class<? extends TaskScheduler> getSchedulerClass () {
                                return MyScheduler.class;
                            }

                            public void cancel () {
                            }
                        }
                    });
                }
            }
        );
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
            new MySchedulerEvent ()
        );
        assertEquals (3, counter.count ());
        System.out.println("end");
    }
    
    public static class MyScheduler extends TaskScheduler {
        
        private static MyScheduler myScheduler;
        
        public MyScheduler () {
            myScheduler = this;
        }
        
        public static void schedule (Collection<Source> sources, SchedulerEvent event) {
            myScheduler.scheduleTasks (sources, event);
        }
    }
    
    static class MySchedulerEvent extends SchedulerEvent {

        public MySchedulerEvent() {
            super (new Object ());
        }
    }
    
    static class Counter {
        
        private int count = 1;
        private int maxCount;

        public Counter (int maxCount) {
            this.maxCount = maxCount;
        }
        
        synchronized void check (int c) {
            if (c != count || c == maxCount) {
                notify ();
                return;
            }
            count ++;
        }
        
        synchronized int count () throws InterruptedException {
            wait ();
            return count;  
        }
    }
}







