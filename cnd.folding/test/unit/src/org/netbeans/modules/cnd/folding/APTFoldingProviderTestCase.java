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

package org.netbeans.modules.cnd.folding;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.editor.Analyzer;
import org.netbeans.modules.cnd.editor.parser.CppFoldRecord;
import org.netbeans.modules.cnd.test.BaseTestCase;

/**
 *
 * @author Vladimir Voskresensky
 */
public class APTFoldingProviderTestCase extends BaseTestCase {
    
    /**
     * Creates a new instance of ModelImplBaseTestCase
     */
    public APTFoldingProviderTestCase(String testName) {
        super(testName);
    }
    
    public void testSimpleFolding() throws Exception {
        performTest("simpleFolding.cc");
    }
    
    public void testErrorDirective() throws Exception {
        performTest("error_directive.cc");
    }
    
    public void testLastIncludes() throws Exception {
        performTest("lastIncludes.cc");
    }
    
    public void testMixedPrepocDirectives() throws Exception {
        performTest("mixedPreprocDirectives.cc");
    }
    
    
    private void performTest(String source) throws Exception {
        System.out.println(getWorkDir());
        File testSourceFile = getDataFile(source);
        char[] text = Analyzer.loadFile(testSourceFile.getAbsolutePath());
        Reader reader = new StringReader(new String(text));
        APTFoldingProvider provider = new APTFoldingProvider();
        List<CppFoldRecord> folds = provider.parse(source, reader);
        Collections.sort(folds, FOLD_COMPARATOR);
        for (CppFoldRecord fold : folds) {
            ref(fold.toString());
        }
        compareReferenceFiles();
    }
    
    private static Comparator<CppFoldRecord> FOLD_COMPARATOR = new Comparator<CppFoldRecord>() {
        public int compare(CppFoldRecord o1, CppFoldRecord o2) {
            int start1 = o1.getStartLine();
            int start2 = o2.getStartLine();
            if (start1 == start2) {
                return o1.getStartOffset() - o2.getStartOffset();
            } else {
                return start1 - start2;
            }
        }
    };
}
