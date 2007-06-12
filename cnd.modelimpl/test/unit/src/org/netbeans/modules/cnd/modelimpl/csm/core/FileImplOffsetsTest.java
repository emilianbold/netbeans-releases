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


package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;

/**
 * test for line-col/offset converting
 * @author Vladimir Voskresensky
 */
public class FileImplOffsetsTest extends TraceModelTestBase {

    public FileImplOffsetsTest(String testName) {
        super(testName);
    }

    public void testConverting() throws Exception {
        performOffsetsTest("dummy.cc");
    }
    
    private void performOffsetsTest(String source) throws Exception {
        File testFile = getDataFile(source);
        assertTrue("File not found "+testFile.getAbsolutePath(),testFile.exists());        
        super.performModelTest(testFile, System.out, System.err);
        FileImpl file = getProject().getFile(testFile);
        assertNotNull("csm file not found for " + testFile.getAbsolutePath(), file);
        checkFileOffsetsConverting(file);
    }
    
    private void checkFileOffsetsConverting(final FileImpl file) {
        List<CsmOffsetableDeclaration> decls = file.getDeclarations();
        for (CsmOffsetableDeclaration csmOffsetableDeclaration : decls) {
            checkOffsetConverting(file, csmOffsetableDeclaration.getStartPosition());
            checkOffsetConverting(file, csmOffsetableDeclaration.getEndPosition());
            checkLineColumnConverting(file, csmOffsetableDeclaration.getStartPosition());
            checkLineColumnConverting(file, csmOffsetableDeclaration.getEndPosition());
        }
    }
    
    private void checkOffsetConverting(FileImpl file, CsmOffsetable.Position pos) {
        int offset = pos.getOffset();
        int[] lineCol = file.getLineColumn(offset);
        assertEquals("different lines", pos.getLine(), lineCol[0]);
        assertEquals("different columns", pos.getColumn(), lineCol[1]);
    }

    private void checkLineColumnConverting(FileImpl file, CsmOffsetable.Position pos) {
        int offset = file.getOffset(pos.getLine(), pos.getColumn());
        assertEquals("different offset for " + pos, pos.getOffset(), offset);
    }
}
