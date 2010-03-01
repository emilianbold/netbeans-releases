/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.cnd.repository.sfs;

import org.netbeans.modules.cnd.repository.test.TestObject;
import org.netbeans.modules.cnd.repository.test.TestObjectCreator;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.junit.NbTestCase;

/**
 * A test for DoubleFileStorage defragmentation
 * @author Vladimir Kvashin
 */
public class DefragmentationTest extends NbTestCase {

    private static final boolean TRACE = false;

    public DefragmentationTest(String testName) {
        super(testName);
    }

//    @Override
//    protected void setUp() throws Exception {
//	super.setUp();
//    }
//
//    @Override
//    protected void tearDown() throws Exception {
//	super.tearDown();
//    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    private DoubleFileStorage createStorage() throws IOException {
        File file = new File(getWorkDir(), "double_file_storage.dat");
        DoubleFileStorage dfs = new DoubleFileStorage(file, true);
        return dfs;
    }

    private void fillData(DoubleFileStorage dfs) throws IOException {
        String dataPath = getDataDir().getAbsolutePath().replaceAll("repository", "modelimpl"); //NOI18N
        Collection<TestObject> objects = new TestObjectCreator().createTestObjects(dataPath);
        for (int i = 0; i < 3; i++) {
            for (TestObject obj : objects) {
                dfs.write(obj.getKey(), obj);
            }
        }
    }

    private DoubleFileStorage createAndFillStorage() throws IOException {
        DoubleFileStorage dfs = createStorage();
        fillData(dfs);
        return dfs;
    }

    public void testFullDeframentation() throws IOException {
        DoubleFileStorage dfs = createAndFillStorage();
        assertTrue(dfs.getFragmentationPercentage() > 50);
        if (TRACE) {
            System.out.printf("--- Before defragmentation\n");
            dfs.dumpSummary(System.out);
        }
        dfs.defragment(0);
        if (TRACE) {
            System.out.printf("--- After defragmentation\n");
            dfs.dumpSummary(System.out);
        }
        assertTrue(dfs.getFragmentationPercentage() == 0);
    }

    public void testPartialDeframentation() throws IOException {
        DoubleFileStorage dfs = createAndFillStorage();
        assertTrue(dfs.getFragmentationPercentage() > 50);
        long timeToDefragment = System.currentTimeMillis();
        dfs.defragment(0);
        timeToDefragment = System.currentTimeMillis() - timeToDefragment;
        if (TRACE) {
            System.err.printf("Full defragmentation took %d ms\n", timeToDefragment);
        }

        dfs = createAndFillStorage();
        long slice = Math.max(timeToDefragment / 100, 1);
        long count = 1000;

        for (int i = 0; i < count; i++) {
            int oldFragmentation = dfs.getFragmentationPercentage();
            dfs.defragment(slice);
            int newFragmentation = dfs.getFragmentationPercentage();
            if (TRACE) {
                System.err.printf("Partial defragmentation %4d: %d -> %d\n", i, oldFragmentation, newFragmentation);
            }
            if (newFragmentation == 0) {
                break;
            }
        }
        assertTrue(dfs.getFragmentationPercentage() == 0);
    }
}
