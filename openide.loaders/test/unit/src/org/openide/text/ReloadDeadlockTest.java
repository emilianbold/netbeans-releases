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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */


package org.openide.text;


import java.io.File;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position;
import javax.swing.text.StyledDocument;


import org.netbeans.junit.NbTestCase;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;


/**
 * Test of deadlock described by issue #12557. Do not include between
 * stable regression test bag until the issue is fixed
 * (see <Netbeans>/openide/test/cfg-unit).
 *
 * @author  Peter Zavadsky
 */
public class ReloadDeadlockTest extends NbTestCase {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");
    }

    /** Creates new TextTest */
    public ReloadDeadlockTest(String s) {
        super(s);
    }
    
    /** Tests the deadlock described in #12557. */
    public void testDeadlock() throws Exception {
        System.out.println("START of deadlock test: see issue #12557.");

        LocalFileSystem lfs = new LocalFileSystem();

        File rootDir;
        
        int i = 1;
        do {
            rootDir = new File(getWorkDir(), "TestRootDir"+(i++));
        } while(rootDir.exists() && !rootDir.isDirectory());
        
        System.err.println("root exists="+rootDir.exists()); 
        
        if(!rootDir.exists()) {
            System.err.println("Created rootDir="+rootDir.mkdir());
        }
        
        lfs.setRootDirectory(rootDir); 
        
        FileObject root = lfs.getRoot();

        FileObject fo = root.getFileObject("test", "txt");
        
        if(fo == null) {
            fo = root.createData("test", "txt");
        }

        final SimpleCESHidden.Env env = new SimpleCESHidden.Env(fo);
        final CloneableEditorSupport ces = new SimpleCESHidden(env);
        env.setSupport(ces);

        final StyledDocument doc = ces.openDocument();
        
        System.err.println("Creating PositionRef at 0 offset.");
        final PositionRef posRef = ces.createPositionRef(0, Position.Bias.Forward);
        
        System.err.println("\nT1="+Thread.currentThread()); // TEMP
        System.err.println("T1: Acquiring doc write lock.");

        final Thread th2 = new Thread(new Runnable() {
            public void run() {

                System.err.println("\nT2=" + Thread.currentThread()
                + "T2: Starting to reload doc.\n"
                + "T2: Acquiring locks in order: 1st CES lock, 2nd doc write lock.\n"
                + "T2: Thus will acquire CES lock and wait on doc write lock.");
                ces.reloadDocument().waitFinished();

                System.err.println("T2: document reloaded");
            }
        });

        
        NbDocument.runAtomic(doc, new Runnable() {
            public void run() {
                System.err.println("T1: doc write lock acquired.");

                // Start the reloading thread.
                th2.start();
                
                try {
                    Thread.currentThread().sleep(3000);
                } catch(InterruptedException ie) {
                    System.err.println("T1: interrupted");
                    ie.printStackTrace();
                }
                
                System.err.println("\nT1: trying to get Position from PositionRef.\n"
                + "T1: Acquring CES lock. It simulates the issue condition,"
                + " this thread already holds doc write lock.\n"
                + "T1: After this the deadlock should occure!!");
                
                try {
                    Position pos = posRef.getPosition();

                    doc.insertString(pos.getOffset(), "New String Added", null);
                    
                    System.err.println("T1: Position="+pos);
                    
                    System.err.println("T1: Document after insert="+doc.getText(pos.getOffset(), doc.getLength()));
                    
                } catch(IOException ioe) {
                    ioe.printStackTrace();
                } catch(BadLocationException ble) {
                    ble.printStackTrace();
                }
                
                System.err.println("T1: new string inserted, releasing doc write lock");
            }
        });

        System.err.println("T1 waits for reloading thread (T2).");
        th2.join();

        System.out.println("END of deadlock test, see issue #12557.");
        
        System.err.println("Document after reload=\n" + doc.getText(0, doc.getLength()));
        
        rootDir.delete();
    }
    
}
