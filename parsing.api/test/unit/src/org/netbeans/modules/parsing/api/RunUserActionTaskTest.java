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

    public void testEmbedding () throws Exception {
        MockServices.setServices (MockMimeLookup.class);
        final int[] n = new int[] {0};
        MockMimeLookup.setInstances (
            MimePath.get ("text/foo"), 
            new TaskFactory () {
                public Collection<SchedulerTask> create (Source source) {
                    return Arrays.asList (new SchedulerTask[] {
                        new EmbeddingProvider() {
                            public List<Embedding> getEmbeddings (Snapshot snapshot) {
                                assertEquals ("text/foo", snapshot.getMimeType ());
                                assertEquals (1, n[0]++);
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
                    assertEquals (2, n[0]++);
                    return new Parser () {

                        public void parse (Snapshot snapshot, Task task, SchedulerEvent event) throws ParseException {
                            assertEquals (snapshot, snapshots [0]);
                            assertEquals (3, n[0]++);
                        }

                        public Result getResult (Task task, SchedulerEvent event) throws ParseException {
                            assertEquals (4, n[0]++);
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
        assertEquals (0, n[0]++);
        ParserManager.parse (source, new UserTask() {
            public void run (Result result, Snapshot snapshot) throws Exception {
                assertEquals (5, n[0]++);
            }
        }, 15);
        assertEquals (6, n[0]++);
//        ParserManager.parse (source, new UserTask() {
//            public void run (Result result, Snapshot snapshot) throws Exception {
//                assertEquals (5, n[0]++);
//            }
//        }, 15);
//        assertEquals (6, n[0]++);
    }
}







