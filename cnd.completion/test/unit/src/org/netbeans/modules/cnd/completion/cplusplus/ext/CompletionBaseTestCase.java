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

package org.netbeans.modules.cnd.completion.cplusplus.ext;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.netbeans.modules.editor.completion.CompletionItemComparator;
import org.netbeans.spi.editor.completion.CompletionItem;


/**
 *
 * @author Vladimir Voskresensky
 */
public abstract class CompletionBaseTestCase extends ProjectBasedTestCase {
    
    /**
     * if test performs any modifications in data files or create new files
     * => pass performInWorkDir as true to create local copy of project in work dir
     */
    public CompletionBaseTestCase(String testName, boolean performInWorkDir) {
        super(testName, performInWorkDir);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("cnd.completion.trace", "true");
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        System.setProperty("cnd.completion.trace", "false");
    }
    
    protected void performTest(String source, int lineIndex, int colIndex) throws Exception {
        performTest(source, lineIndex, colIndex, "");// NOI18N
    }
    
    protected void performTest(String source, int lineIndex, int colIndex, String textToInsert) throws Exception {
        performTest(source, lineIndex, colIndex, textToInsert, 0);// NOI18N
    }
    
    protected void performTest(String source, int lineIndex, int colIndex, String textToInsert, int offsetAfterInsertion) throws Exception {
        performTest(source, lineIndex, colIndex, textToInsert, offsetAfterInsertion, getName()+".ref");// NOI18N
    }
    
    protected void performTest(String source, int lineIndex, int colIndex, String textToInsert, int offsetAfterInsertion, String goldenFileName) throws Exception {
        performTest(source, lineIndex, colIndex, textToInsert, offsetAfterInsertion, goldenFileName, null, null);
    }
    
    protected void performTest(String source, int lineIndex, int colIndex, String textToInsert, int offsetAfterInsertion, String goldenFileName, String toPerformItemRE, String goldenFileName2) throws Exception {
        File workDir = getWorkDir();
        File testFile = getDataFile(source);
        
        File output = new File(workDir, goldenFileName);
        PrintStream streamOut = new PrintStream(output);
        
        CompletionItem[] array = createTestPerformer().test(logWriter, textToInsert, offsetAfterInsertion, false, testFile, lineIndex, colIndex); // NOI18N        

	assertNotNull("Result should not be null", array);
        Arrays.sort(array, CompletionItemComparator.BY_PRIORITY);
        for (int i = 0; i < array.length; i++) {
            CompletionItem completionItem = array[i];
            streamOut.println(completionItem.toString());
        }
        streamOut.close();
        
        File goldenDataFile = getGoldenFile(goldenFileName);
        if (!goldenDataFile.exists()) {
            fail("No golden file " + goldenDataFile.getAbsolutePath() + "\n to check with output file " + output.getAbsolutePath());
        }
        if (CndCoreTestUtils.diff(output, goldenDataFile, null)) {
            // copy golden
            File goldenCopyFile = new File(workDir, goldenFileName + ".golden");
            CndCoreTestUtils.copyToWorkDir(goldenDataFile, goldenCopyFile); // NOI18N
            fail("OUTPUT Difference between diff " + output + " " + goldenCopyFile); // NOI18N
        }
    }
    
    protected CompletionTestPerformer createTestPerformer() {
        return new CompletionTestPerformer();
    }
}
