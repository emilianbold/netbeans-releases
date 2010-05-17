/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.lib.cvsclient.admin;

import junit.framework.*;
import java.io.*;
import java.util.*;
import org.netbeans.lib.cvsclient.TestKit;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.file.FileUtils;

/**
 *
 * @author Petr Kuzel
 */
public class StandardAdminHandlerTest extends TestCase {

    public StandardAdminHandlerTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(StandardAdminHandlerTest.class);
        
        return suite;
    }

    int test70625ThreadDone;
    
    /** 
     * Simulates 70625 issue. 
     * It can randomly pass for incorrect code but never fails for correct code.
     */
    public void test70625() throws Exception {
        try {
            System.err.println("test70625()");

            File tmpDir = TestKit.createTmpFolder("test_StandardAdminHandlerTest");

            final File file = new File(tmpDir, "test70625");                
            final Entry entry = new Entry();
            entry.setName("test70625");

            final StandardAdminHandler instance = new StandardAdminHandler();

            // create CVS structure that simulates rename in progress
            new File(tmpDir, "CVS").mkdirs();
            new File(tmpDir, "CVS/Entries.Backup").createNewFile();

            SyncTwo lock = new SyncTwo();
            instance.t9yBeforeRenameSync(lock);

            final Runnable r = new Runnable() {
                public void run() {
                    try {                    
                        instance.setEntry(file, entry);
                    } catch (IOException ex) {
                        System.err.println("Ignoring " + ex.getMessage() + " ...");
                    } finally {
                        test70625ThreadDone ++;
                        synchronized(StandardAdminHandlerTest.this) {
                            StandardAdminHandlerTest.this.notifyAll();
                        }
                    }
                }
            };

            final Runnable r2 = new Runnable() {
                public void run() {
                    try {                    
                        instance.getEntry(file);
                    } catch (IOException ex) {
                        System.err.println("Ignoring " + ex.getMessage() + " ...");
                    } finally {
                        test70625ThreadDone ++;
                        synchronized(StandardAdminHandlerTest.this) {
                            StandardAdminHandlerTest.this.notifyAll();
                        }
                    }
                }
            };

            new Thread(r, "Update").start();                    
            new Thread(r2, "Annotator").start();
            
            synchronized(this) {
                while (test70625ThreadDone < 2) {
                    wait();
                }
            }

            if (new File(tmpDir, "CVS/Entries").exists() == false) {
                fail("CVS/Entries gone.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    private class SyncTwo implements Runnable {
        
        int counter;
        
        public synchronized void run() {
            counter++;
            Thread.dumpStack();
            while (counter < 2) {
                try {
                    System.err.println(Thread.currentThread().getName() + " is waiting for sibling...");
                    wait(100);  // correct code will always wait
                    return;
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            notifyAll();
        }
    }
    
}
