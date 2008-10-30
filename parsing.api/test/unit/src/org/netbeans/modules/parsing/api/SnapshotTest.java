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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.impl.TaskProcessor;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.ParserFactory;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskScheduler;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author hanz
 */
public class SnapshotTest extends NbTestCase {

    public SnapshotTest (String testName) {
        super (testName);
    }            

    public void testSnapshotEmbedding () throws IOException {
        clearWorkDir ();
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        FileObject testFile = FileUtil.createData (workDir, "bla");
        OutputStream outputStream = testFile.getOutputStream ();
        OutputStreamWriter writer = new OutputStreamWriter (outputStream);
        writer.append ("Toto je testovaci file, na kterem se budou delat hnusne pokusy!!!");
        writer.close ();
        Source source = Source.create (testFile);
        Snapshot originalSnapshot = source.createSnapshot ();
        assertEquals (0, originalSnapshot.getOriginalOffset (0));
        assertEquals (10, originalSnapshot.getOriginalOffset (10));
        assertEquals(originalSnapshot.getText ().length (),originalSnapshot.getOriginalOffset (originalSnapshot.getText ().length ()));
        try {
            originalSnapshot.getOriginalOffset (originalSnapshot.getText ().length ()+1);
            assert (false);
        } catch (ArrayIndexOutOfBoundsException ex) {
        }
        assertEquals (0, originalSnapshot.getEmbeddedOffset (0));
        assertEquals (10, originalSnapshot.getEmbeddedOffset (10));
//        try {
//            originalSnapshot.getEmbeddedOffset (originalSnapshot.getText ().length ());
//            assert (false);
//        } catch (ArrayIndexOutOfBoundsException ex) {
//        }
        assertEquals("stovaci fi", originalSnapshot.create (10, 10, "language/jedna").getSnapshot ().getText ());
        assertEquals("1234567890", originalSnapshot.create ("1234567890", "language/jedna").getSnapshot ().getText ());
        Embedding languageJednaEmbedding = Embedding.create (Arrays.asList (new Embedding[] {
            originalSnapshot.create (10, 10, "language/jedna"),
            originalSnapshot.create ("1234567890", "language/jedna"),
            originalSnapshot.create (30, 10, "language/jedna"),
        }));
        assertEquals ("language/jedna", languageJednaEmbedding.getMimeType ());
        Snapshot languageJednaSnapshot = languageJednaEmbedding.getSnapshot ();
        assertEquals ("language/jedna", languageJednaSnapshot.getMimeType ());
        assertEquals ("stovaci fi1234567890rem se bud", languageJednaSnapshot.getText ().toString ());
        assertEquals (10, languageJednaSnapshot.getOriginalOffset (0));
        assertEquals (12, languageJednaSnapshot.getOriginalOffset (2));
        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (10));
        assertEquals (-1, languageJednaSnapshot.getOriginalOffset (12));
        assertEquals (30, languageJednaSnapshot.getOriginalOffset (20));
        assertEquals (33, languageJednaSnapshot.getOriginalOffset (23));
        assertEquals (40, languageJednaSnapshot.getOriginalOffset (30));
        try {
            languageJednaSnapshot.getOriginalOffset (31);
            assert (false);
        } catch (ArrayIndexOutOfBoundsException ex) {
        }
        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (0));
        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (5));
        assertEquals (0, languageJednaSnapshot.getEmbeddedOffset (10));
        assertEquals (5, languageJednaSnapshot.getEmbeddedOffset (15));
        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (20));
        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (25));
        assertEquals (20, languageJednaSnapshot.getEmbeddedOffset (30));
        assertEquals (25, languageJednaSnapshot.getEmbeddedOffset (35));
        assertEquals (-1, languageJednaSnapshot.getEmbeddedOffset (40));
//        try {
//            languageJednaSnapshot.getEmbeddedOffset (50);
//            assert (false);
//        } catch (ArrayIndexOutOfBoundsException ex) {
//        }
        Embedding petaEmbedding = languageJednaSnapshot.create (5, 20, "peta");
        Snapshot petaSnapshot = petaEmbedding.getSnapshot ();
        assertEquals ("ci fi1234567890rem s", petaSnapshot.getText ().toString ());
        assertEquals (15, petaSnapshot.getOriginalOffset (0));
        assertEquals (18, petaSnapshot.getOriginalOffset (3));
        assertEquals (-1, petaSnapshot.getOriginalOffset (5));
        assertEquals (-1, petaSnapshot.getOriginalOffset (6));
        assertEquals (-1, petaSnapshot.getOriginalOffset (10));
        assertEquals (30, petaSnapshot.getOriginalOffset (15));
        assertEquals (34, petaSnapshot.getOriginalOffset (19));
        assertEquals (35, petaSnapshot.getOriginalOffset (20));
        try {
            petaSnapshot.getOriginalOffset (21);
            assert (false);
        } catch (ArrayIndexOutOfBoundsException ex) {
        }
        assertEquals (-1, petaSnapshot.getEmbeddedOffset (0));
        assertEquals (-1, petaSnapshot.getEmbeddedOffset (10));
        assertEquals (0, petaSnapshot.getEmbeddedOffset (15));
        assertEquals (4, petaSnapshot.getEmbeddedOffset (19));
        assertEquals (-1, petaSnapshot.getEmbeddedOffset (20));
        assertEquals (15, petaSnapshot.getEmbeddedOffset (30));
        assertEquals (-1, petaSnapshot.getEmbeddedOffset (35));
        
        Embedding fullSpanEmbedding = originalSnapshot.create (0, originalSnapshot.getText().length(), "peta");
        Snapshot fullSpanSnapshot = fullSpanEmbedding.getSnapshot ();
        assertEquals(originalSnapshot.getText().toString(), fullSpanSnapshot.getText().toString());
    }

    public void testSnapshotCreationDeadlock () throws Exception {  //Originally JavaSourceTest.testRTB_005
        MockMimeLookup.setInstances(MimePath.get("text/foo"), new ParserFactory(){
            public Parser createParser (Collection<Snapshot> snapshots) {
                return new Parser () {
                    public void parse (Snapshot snapshot, Task task, SchedulerEvent event) throws ParseException {

                    }

                    public Result getResult (Task task, SchedulerEvent event) throws ParseException {
                        return new Result () {
                            protected void invalidate (){}
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
        });
        FileObject workDir = FileUtil.toFileObject (getWorkDir ());
        FileObject testFile = FileUtil.createData (workDir, "bla.foo");
        FileUtil.setMIMEType ("foo", "text/foo");
        final Object lock = new Object ();
        Logger.getLogger(Source.class.getName()).setLevel(Level.FINEST);
        Logger.getLogger(Source.class.getName()).addHandler(new Handler() {
            public void publish(LogRecord record) {
                synchronized (lock) {
                    lock.getClass();
                }
            }
            public void flush() {}
            public void close() throws SecurityException {}
        });

        final Source src = Source.create(testFile);
        final ParserResultTask<Parser.Result> pr = new ParserResultTask<Parser.Result>() {
            public void run (Parser.Result r, Snapshot s) {

            }

            public Class<? extends TaskScheduler> getSchedulerClass () {
                return null;
            }

            public void cancel () {}

            public int getPriority () {
                return 1;
            }

        };
        Utilities.addParserResultTask(pr, src);
        synchronized (lock) {
            Utilities.revalidate(src);
            Thread.sleep(2000);
             TaskProcessor.runUserTask(new GenericUserTask() {
                    public void run () throws ParseException {

                    }
                }, Collections.singletonList(src));
        }
        Utilities.removeParserResultTask(pr, src);
    }
}





