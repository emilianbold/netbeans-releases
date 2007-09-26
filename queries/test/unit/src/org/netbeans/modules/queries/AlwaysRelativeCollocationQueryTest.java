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

package org.netbeans.modules.queries;

import java.io.IOException;
import junit.framework.*;
import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.queries.CollocationQueryImplementation;

/**
 *
 * @author Tomas Zezula
 */
public class AlwaysRelativeCollocationQueryTest extends NbTestCase {

    public AlwaysRelativeCollocationQueryTest(String testName) {
        super(testName);
    }


    public void testFindRoot() throws IOException {
        AlwaysRelativeCollocationQuery cq = new AlwaysRelativeCollocationQuery ();
        File testRoot = this.getWorkDir();
        File root1 = new File (testRoot,"root1");
        root1.mkdirs();
        File root2 = new File (testRoot, "root2");
        root2.mkdirs();
        File folder1 = new File (new File (root1,"folder1_1"), "folder1_2");
        folder1.mkdirs();
        File folder2 = new File (new File (root1,"folder2_1"), "folder2_2");
        folder2.mkdirs();
        File folderExt = new File (new File (root2,"folderExt_1"), "folderExt_2");
        folderExt.mkdirs();        
        
        File[] roots = new File[] {
            root1
        };        
        cq.setFileSystemRoots (roots);
        assertEquals("Wrong root of the folder1", root1, cq.findRoot(folder1));
        assertEquals("Wrong root of the folder2", root1, cq.findRoot(folder2));
        
        roots = new File[] {
            root1,
            root2
        };
        cq.setFileSystemRoots (roots);
        assertEquals("Wrong root of the folder1", root1, cq.findRoot(folder1));
        assertEquals("Wrong root of the folder2", root1, cq.findRoot(folder2));
        assertEquals("Wrong root of the folderExt", root2, cq.findRoot(folderExt));
    }

    public void testAreCollocated() throws IOException {
        AlwaysRelativeCollocationQuery cq = new AlwaysRelativeCollocationQuery ();
        File testRoot = this.getWorkDir();
        File root1 = new File (testRoot,"root1");
        root1.mkdirs();
        File root2 = new File (testRoot, "root2");
        root2.mkdirs();
        File folder1 = new File (new File (root1,"folder1_1"), "folder1_2");
        folder1.mkdirs();
        File folder2 = new File (new File (root1,"folder2_1"), "folder2_2");
        folder2.mkdirs();
        File folderExt = new File (new File (root2,"folderExt_1"), "folderExt_2");
        folderExt.mkdirs();        
        
        File[] roots = new File[] {
            root1
        };       
        cq.setFileSystemRoots (roots);
        assertTrue ("The folder1 should be collocated with the folder2", cq.areCollocated(folder1,folder2));
                
        roots = new File[] {
            root1,
            root2
        };
        cq.setFileSystemRoots (roots);
        assertTrue ("The folder1 should be collocated with the folder2", cq.areCollocated(folder1,folder2));
        assertFalse ("The folder1 should not be collocated with the folderExt", cq.areCollocated(folder1,folderExt));
        assertFalse ("The folder2 should not be collocated with the folderExt", cq.areCollocated(folder2,folderExt));
    }
    
}
