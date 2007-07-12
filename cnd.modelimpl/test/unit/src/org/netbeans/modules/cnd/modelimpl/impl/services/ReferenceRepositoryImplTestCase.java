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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;
import org.netbeans.modules.cnd.modelimpl.trace.TraceXRef;

/**
 *
 * @author Vladimir Voskresensky
 */
public class ReferenceRepositoryImplTestCase extends TraceModelTestBase {
    
    public ReferenceRepositoryImplTestCase(String testName) {
        super(testName);
    }
    
    public void testCpuClassRefs() throws Exception {
        performTest("cpu.h", 25, 9);
    }
    ////////////////////////////////////////////////////////////////////////////
    // general staff
    
    protected void postSetUp() throws Exception {
        super.postSetUp();
        log("postSetUp preparing project.");
        initParsedProject();
        log("postSetUp finished preparing project.");
        log("Test "+getName()+  "started");         
    }    
    
    protected void doTest(File testFile, PrintStream streamOut, PrintStream streamErr, Object ... params) throws Exception {
        FileImpl fileImpl = getFileImpl(testFile);
        assertNotNull("csm file not found for " + testFile.getAbsolutePath(), fileImpl);
        int line = (Integer) params[0];
        int column = (Integer) params[1];
        boolean inProject = (Boolean)params[2];
        boolean includeSelfDeclarations = (Boolean) params[3];
        int offset = fileImpl.getOffset(line, column);
        CsmReference tgtRef = CsmReferenceResolver.getDefault().findReference(fileImpl, offset);
        assertNotNull("reference is not found for " + testFile.getAbsolutePath() + "; line="+line+";column="+column, tgtRef);
        CsmObject target = tgtRef.getReferencedObject();
        assertNotNull("referenced object is not found for " + testFile.getAbsolutePath() + "; line="+line+";column="+column, target);
        streamOut.println("TARGET OBJECT IS\n  " + CsmTracer.toString(target));
        ReferenceRepositoryImpl xRefRepository = new ReferenceRepositoryImpl();
        Collection<CsmReference> out;
        if (inProject) {
            out = xRefRepository.getReferences(target, fileImpl.getProject(), includeSelfDeclarations);
        } else {
            out = xRefRepository.getReferences(target, fileImpl, includeSelfDeclarations);
        }
        TraceXRef.traceRefs(out, target, streamOut);
    }
    
    private void performTest(String source, int line, int column) throws Exception {
        boolean includeSelfDeclarations = true;
        boolean inProject = true;
        super.performTest(source, getName() + ".res", null, // NOI18N
                            line, column, inProject, includeSelfDeclarations);
    }        
}
