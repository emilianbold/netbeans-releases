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

package org.netbeans.modules.masterfs.filebasedfs.naming;

import java.lang.ref.WeakReference;
import org.netbeans.junit.NbTestCase;
import java.io.File;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Radek Matous
 */
public class FileNameTest extends NbTestCase {
    private File f1;
    private File f2;
    private File f3;
    private FileNaming n1;
    private FileNaming n2;
    private FileNaming n3;

    public FileNameTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        clearWorkDir();
        
        f1 = getTestFile();
        f2 = new File (f1.getAbsolutePath());
        f3 = f1.getParentFile();
        n1 = NamingFactory.fromFile(f1);
        n2 = NamingFactory.fromFile(f2);
        n3 = NamingFactory.fromFile(f3);        
    }

    protected File getTestFile() throws Exception {
        File retVal = new File (getWorkDir(), "namingTest");
        if (!retVal.exists()) {
            retVal.createNewFile();
        }
        return retVal;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        n1 = null;
        n2 = null;
        n3 = null;
    }

    public void test69450() throws Exception {
        File fA = new File(getWorkDir(),"A");
        if (fA.exists()) {
            assertTrue(fA.delete());
        }        
        File fa = new File(getWorkDir(),"a");
        if (!fa.exists()) {
            assertTrue(fa.createNewFile());
        }        
        boolean isCaseSensitive = !fa.equals(fA);                
        FileNaming na = NamingFactory.fromFile(fa);        
        assertEquals(fa.getName(),NamingFactory.fromFile(fa).getName());        
        if (isCaseSensitive) {
            assertFalse(fa.getName().equals(NamingFactory.fromFile(fA).getName()));
            assertFalse(NamingFactory.fromFile(fa).equals(NamingFactory.fromFile(fA)));
            assertNotSame(NamingFactory.fromFile(fa),NamingFactory.fromFile(fA));            
            assertTrue(fa.delete());
            assertTrue(fA.createNewFile());
            assertFalse(fA.getName().equals(na.getName()));
            assertFalse(na.equals(NamingFactory.fromFile(fA)));
            assertFalse(fA.getName().equals(na.getName()));            
        } else {
            assertSame(na,NamingFactory.fromFile(fA));            
            assertEquals(fa.getName(),na.getName());            
            assertEquals(fa.getName(),NamingFactory.fromFile(fA).getName());
            assertEquals(NamingFactory.fromFile(fa),NamingFactory.fromFile(fA));
            assertSame(NamingFactory.fromFile(fa),NamingFactory.fromFile(fA));            
            //#69450            
            assertTrue(fa.delete());
            assertTrue(fA.createNewFile());
            assertFalse(fA.getName().equals(na.getName()));
            assertEquals(na,NamingFactory.fromFile(fA));
            assertSame(na, NamingFactory.fromFile(fA));
            assertFalse(fA.getName() + " / " + na.getName(),fA.getName().equals(na.getName()));
            FileNaming nna = NamingFactory.checkCaseSensitivity(na,fA);
            assertTrue(fA.getName() + " / " + nna.getName(),fA.getName().equals(nna.getName()));
        }
    }
    
    /**
     * Test of equals method, of class org.netbeans.modules.masterfs.pathtree.PathItem.
     */
   public void testEquals () throws Exception {
        assertEquals(n1, n2);
        assertSame(n1, n2);        
        assertNotSame(n3, n1);
        assertNotSame(n3, n2);
        assertEquals(n3, n1.getParent());
        assertEquals(n3, n2.getParent());
        assertSame(n3, n1.getParent());
        assertSame(n3, n2.getParent());                
    }    

    public void testHashcode () throws Exception {
        assertEquals(n3.hashCode(), n1.getParent().hashCode());                
        assertEquals(n3.hashCode(), n2.getParent().hashCode());                                
    }
    
    public void testWeakReferenced () throws Exception {
        List l = new ArrayList ();
        FileNaming current = n1;
        while (current != null) {
            l.add(new WeakReference (current));
            current = current.getParent();
        }
        
        current = null;        
        n1 = null;
        n2 = null;
        n3 = null;
        
        for (int i = 0; i < l.size(); i++) {
            WeakReference weakReference = (WeakReference) l.get(i);
            assertGC("Shoul be GCed: "+((FileNaming)weakReference.get()),  weakReference);
        }        
    }
    
    public void testFileConversion () throws Exception {
        FileNaming[] all = new FileNaming [] {n1, n2, n3};
        File[] files = new File [] {f1, f2, f3};
        for (int i = 0; i < all.length; i++) {
            FileNaming current = all[i];
            File currentFile = files[i];            
            
            while (current != null) {
                assertEquals (current.getFile(), currentFile);
                current = current.getParent();
                currentFile = currentFile.getParentFile();
            }            
        }        
    }

    public void testFileExist () throws Exception {
        FileNaming[] all = new FileNaming [] {n1, n2, n3};
        for (int i = 0; i < all.length; i++) {
            FileNaming current = all[i];
            while (current != null) {
                File file = current.getFile();
                assertTrue(file.getAbsolutePath(), file.exists());
                current = current.getParent();
            }            
        }        
    }


    /**
     * Test of rename method, of class org.netbeans.modules.masterfs.naming.PathItem.
     */
    public void testRename() throws Exception {
        File f = f1;
        assertTrue(f.exists());
        FileNaming pi = NamingFactory.fromFile(f);
        FileNaming ni = pi.rename("renamed3", null);
        assertTrue(pi != ni);
        File f2 = ni.getFile();
        assertFalse(f.exists());
        assertTrue(f2.exists());
        assertFalse(f2.equals(f));
        assertTrue (f2.getName().equals("renamed3"));        
    }

    public void testTwoNamingsAreOnlyEqualIfTheyRepresentTheSamePath() {
        File hc1 = new HashCodeFile("/space/root/myfile", 444);
        File hc2 = new HashCodeFile("/space/myfile", 444);

        FileNaming nf1 = NamingFactory.fromFile(hc1);
        FileNaming nf2 = NamingFactory.fromFile(hc2);
        
        assertFalse("namings are different", nf1.equals(nf2));
    }


    private static final class HashCodeFile extends File {
        private final int hash;
        public HashCodeFile(String path, int hash) {
            super(path);
            this.hash = hash;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj) && (obj instanceof HashCodeFile) && ((HashCodeFile)obj).hash == hash;
        }

    }
}
