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
package org.netbeans.modules.vmd.api.model;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.netbeans.modules.vmd.api.model.common.TypesSupport;

import org.netbeans.modules.vmd.api.model.descriptors.FirstCD;
import org.netbeans.modules.vmd.api.model.utils.ModelTestUtil;

import static org.netbeans.modules.vmd.api.model.utils.TestTypes.*;

/**
 *
 * @author Karol Harezlak
 */
public class DestructiveTest extends TestCase {

    private final DesignDocument document = ModelTestUtil.createTestDesignDocument();
    private final Thread read = new Thread(new Read());
    private final Thread writeContinuously = new Thread(new WriteContinuously());
    private final Thread writeShortly = new Thread(new WriteContinuously());
    
    public DestructiveTest(String testName){
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(DestructiveTest.class);
        //TestSuite suite = new TestSuite();
        //suite.addTest(new DestructiveTest("testForceReadAccesWhileWriteing"));
        
        return suite;
    }
    
    public void testLotComponents(){
        System.out.println("This test tries to create 1000 document components so please be patience" + // NOI18N
                " and check output for more results"); // NOI18N
        System.out.println("It shouldn't take longer then 10s to 1 min"); // NOI18N
        
        document.getTransactionManager().writeAccess(new Runnable() {
            int i=0;
            public void run() {
                do {
                    document.createComponent(FirstCD.TYPEID_CLASS);
                    i++;
                } while(i<1000);
            }
        });
        document.getTransactionManager().writeAccess(new Runnable() {
            int i=0;
            public void run() {
                do {
                    document.getComponentByUID(i).writeProperty(FirstCD.PROPERTY_TEST,TypesSupport.createStringValue( String.valueOf(i)));
                    i++;
                } while(i<1000);
            }
        });
    }
    
    public void testForceReadAccesWhileWriteContinuously(){
        System.out.println("This test force to obtain read access while component is lock because of writing "); // NOI18N
        writeContinuously.start();
        try {
            writeContinuously.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    public void testForceReadAccesWhileWriteShortly(){
        System.out.println("This test force to obtain read access while component is lock because of writing "); // NOI18N
        writeShortly.start();
        try {
            writeShortly.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    private class Read implements Runnable {
        public void run() {
            int i=0;
            System.out.println("Start trying obtain read access to the document"); // NOI18N
            do{
                i++;
                System.out.println("Trying obtaian read access to the document "+ i); // NOI18N
                DestructiveTest.this.document.getTransactionManager().readAccess(new Runnable() {
                    public void run() {
                        if (DestructiveTest.this.document.getTransactionManager().isWriteAccess()){
                            System.out.println("!!!Read access obtained while isWriteAccess() == TRUE!!!"); // NOI18N
                            fail(); // If test fail in this line it means that it was able obtain read access while writing lock was active!
                        } else{
                            System.out.println("Read access obtain when document isWriteAccess()= FALSE"); // NOI18N
                        }
                    }
                });
            }while(writeContinuously.isAlive() || writeShortly.isAlive());
            System.out.println("Stop trying obtaian read access to the document"); // NOI18N
        }
    }
    
    private class WriteContinuously implements Runnable{
        public void run() {
            read.start();
            DestructiveTest.this.document.getTransactionManager().writeAccess(new Runnable() {
                int i=0;
                public void run() {
                    System.out.println("Start writing to the document"); // NOI18N
                    do {
                        i++;
                        DestructiveTest.this.document.createComponent(FirstCD.TYPEID_CLASS);
                    } while(i<1000);
                    System.out.println("Stop writing to the document"); // NOI18N
                }
            });
        }
    }
    
    private class WriteShort implements Runnable{
        public void run() {
            int i=0;
            read.start();
            do {
                DestructiveTest.this.document.getTransactionManager().writeAccess(new Runnable() {
                    public void run() {
                        System.out.println("Start writing to the document"); // NOI18N
                        DestructiveTest.this.document.createComponent(FirstCD.TYPEID_CLASS);
                        System.out.println("Stop writing to the document"); // NOI18N
                    }
                });
                i++;
            } while(i<1000);
        }
    }
}
