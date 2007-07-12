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

package org.netbeans.modules.cnd.completion.cplusplus.hyperlink;

import java.io.File;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.completion.cplusplus.utils.Token;
import org.netbeans.modules.cnd.completion.test.ProjectBasedTestCase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;

/**
 * base class for hyperlink tests
 *
 * entry point:
 * - performTest (@see performTest)
 *
 * What should be configured:
 * - the dir with the same name as test class (harness init the project from the dir)
 * i.e for CsmHyperlinkProviderTestCase create
 * ${completion}/test/unit/data/org/netbeans/modules/cnd/completion/cplusplus/hyperlink/CsmHyperlinkProviderTestCase
 * and put there any C/C++ files
 *
 * @author Vladimir Voskresensky
 */
public abstract class HyperlinkBaseTestCase extends ProjectBasedTestCase {
        
    private CsmHyperlinkProvider declarationsProvider;
    private CsmIncludeHyperlinkProvider includeProvider;

    private static boolean GENERATE_GOLDEN_DATA = false;
    public HyperlinkBaseTestCase(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();    
        log("CndHyperlinkBaseTestCase.setUp started.");
        
        declarationsProvider = new CsmHyperlinkProvider();
        includeProvider = new CsmIncludeHyperlinkProvider();

        log("CndHyperlinkBaseTestCase.setUp finished.");
        log("Test "+getName()+  " started");
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    /**
     * @param source relative path of source file
     * @param lineIndex line where hyperlink is perfomed (1-based)
     * @param colIndex column where hyperlink is perfomed (1-based)
     * @param destGoldenFile relative path of expected destination file
     * @param destGoldenLine start line of expected destination object (1-based)
     * @param destGoldenColumn start column of expected destination object (1-based)
     */
    protected void performTest(String source, int lineIndex, int colIndex, String destGoldenFile, int destGoldenLine, int destGoldenColumn) throws Exception {
        String goldenFileAbsPath = getDataFile(destGoldenFile).getAbsolutePath();
        File testSourceFile = getDataFile(source);
        BaseDocument doc = getBaseDocument(testSourceFile);
        int offset = CndCoreTestUtils.getDocumentOffset(doc, lineIndex, colIndex);
        Token jumpToken = getJumpToken(doc, offset);
        assertNotNull("Hyperlink not found token in file " + testSourceFile + " on position (" + lineIndex + ", " + colIndex + ")", // NOI18N
                        jumpToken);
        CsmOffsetable targetObject = findTargetObject(doc, offset, jumpToken);
        assertNotNull("Hyperlink target is not found for " + jumpToken.getText() + //NOI18N
                " in file " + testSourceFile + " on position (" + lineIndex + ", " + colIndex + ")", targetObject);//NOI18N
        String destResultFileAbsPath = targetObject.getContainingFile().getAbsolutePath();
        Position resultPos = targetObject.getStartPosition();
        int resultOffset = resultPos.getOffset();
        int resultLine = resultPos.getLine();
        int resultColumn = resultPos.getColumn();
        if (GENERATE_GOLDEN_DATA) {
            System.err.println("result file " + goldenFileAbsPath);
            System.err.println("result position " + resultPos);
            System.err.println("result line " + resultLine);
            System.err.println("result column " + resultColumn);
        } else {
            String positions = toString(source, lineIndex, colIndex) + " -> " + toString(destGoldenFile, destGoldenLine, destGoldenColumn);
            assertEquals("Different target *FILE* " + positions, goldenFileAbsPath, destResultFileAbsPath);
            assertEquals("Different target *LINE* positions " + positions, destGoldenLine, resultLine);
            assertEquals("Different target *COLUMN* positions " + positions, destGoldenColumn, resultColumn);
        }
    }
    
    private String toString(String file, int line, int column) {
        return "[" + file + ":" + "Line-" + line +"; Col-" + column + "]";
    }
    
    private CsmOffsetable findTargetObject(BaseDocument doc, int offset, Token jumpToken) {
        CsmOffsetable csmItem = null;
        // emulate hyperlinks order
        // first ask includes handler
        if (includeProvider.isValidToken(jumpToken)) {
            csmItem = includeProvider.findTargetObject(doc, offset);
        }
        // if failed => ask declarations handler
        if (csmItem == null && declarationsProvider.isValidToken(jumpToken)) {
            csmItem = declarationsProvider.findTargetObject(null, doc, jumpToken, offset);
        }
        return csmItem;
    }
    
    private Token getJumpToken(BaseDocument doc, int offset) {
        return CsmAbstractHyperlinkProvider.getToken(doc, offset);
    }  
    
}
