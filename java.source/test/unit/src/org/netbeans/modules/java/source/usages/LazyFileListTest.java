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

package org.netbeans.modules.java.source.usages;

import java.util.Arrays;
import java.util.Set;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashSet;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Tomas Zezula
 */
public class LazyFileListTest extends NbTestCase {

    private static final String[] EXPECTED_NAMES = {
        "a1.java",
        "a2.java",
        "d1.java",
        "d2.java",
        "b1.java",
        "b2.java",
        "e1.java",
        "e2.java",
        "c1.java",
        "c2.java"
    };
    
    private File root;
    
    public LazyFileListTest(String testName) {
        super(testName);
    }
    
    private void createNewFile(File f) throws Exception {
        f.createNewFile();
        
        if (f.length() == 0) {
            //the RepositoryUpdater.LazyFileList needs some content:
            OutputStream out = new FileOutputStream(f);
            
            try {
                out.write('\n');
            } finally {
                out.close();
            }
        }
    }

    protected void setUp() throws Exception {
        this.clearWorkDir();
        this.root = this.getWorkDir();
        File f = new File (new File(new File (root,"a"),"b"),"c");
        f.mkdirs();
        File t = new File (f,"c1.java");
        createNewFile(t);
        t = new File (f,"c2.java");
        createNewFile(t);
        t = new File (f,"c3.txt");
        createNewFile(t);
        f = f.getParentFile();
        t = new File (f,"b1.java");
        createNewFile(t);
        t = new File (f,"b2.java");
        createNewFile(t);
        t = new File (f,"b3.java");
        t.createNewFile();
        t = new File (f,"b3.txt");
        createNewFile(t);
        f = f.getParentFile();
        t = new File (f,"a1.java");
        createNewFile(t);
        t = new File (f,"a2.java");
        createNewFile(t);
        t = new File (f,"a3.txt");
        createNewFile(t);
        
        f = new File(new File (root,"d"),"e");
        f.mkdirs();
        t = new File (f,"e1.java");
        createNewFile(t);
        t = new File (f,"e2.java");
        createNewFile(t);
        f = f.getParentFile();
        t = new File (f,"d1.java");
        createNewFile(t);
        t = new File (f,"d2.java");
        createNewFile(t);
        
    }

    protected void tearDown() throws Exception {
    }

    public void testIterator() {
        RepositoryUpdater.FileList lfl = new RepositoryUpdater.FileList (this.root);
        final Set<String> fileNames = new HashSet<String>();
        for (File f :lfl.getJavaFiles()) {
            fileNames.add(f.getName());
        }
        assertEquals(new HashSet(Arrays.asList(EXPECTED_NAMES)),fileNames);
    }
    
    
    
}
