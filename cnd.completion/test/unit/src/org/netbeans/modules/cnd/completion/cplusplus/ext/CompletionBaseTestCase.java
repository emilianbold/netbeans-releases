/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.completion.cplusplus.ext;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import org.netbeans.modules.cnd.completion.test.ProjectBasedTestCase;
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
        
        CompletionItem[] array = new CompletionTestPerformer().test(logWriter, textToInsert, offsetAfterInsertion, false, testFile, lineIndex, colIndex); // NOI18N        

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
            fail("OUTPUT Difference between " + output + " and " + goldenCopyFile); // NOI18N
        }
    }
}
