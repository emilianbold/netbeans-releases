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
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmUsingResolver;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;

/**
 *
 * @author Vladimir Voskresensky
 */
public class UsingResolverImplTestCase extends TraceModelTestBase {
    
    public UsingResolverImplTestCase(String testName) {
        super(testName);
    }

    public void testOnlyGlobalIsVisible() throws Exception {
        performTest("fileUsing.cc", 3, 5);
    }

    public void testNSOneIsVisible() throws Exception {
        performTest("fileUsing.cc", 10, 5);
    }

    public void testNSOneAndNsTwoAreVisible() throws Exception {
        performTest("fileUsing.cc", 23, 5);
    }
    
    public void testNSOneIsVisibleNsTwoNotYetInFun() throws Exception {
        performTest("fileUsing.cc", 15, 5);
    }    
    
    public void testNSOneIsVisibleNsTwoIsUsedInFun() throws Exception {
        performTest("fileUsing.cc", 17, 5);
    }    

    public void testNSOneAndNsTwoAreVisibleInMain() throws Exception {
        performTest("main.cc", 5, 5);
    }      

    public void testUnnamedIsVisble() throws Exception {
        performTest("unnamedNs.cc", 12, 5);
    }      
    
    public void testOuterIsVisble() throws Exception {
        performTest("main.cc", 10, 10);
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
        boolean onlyInProject = (Boolean) params[2];
        int offset = fileImpl.getOffset(line, column);
        CsmUsingResolver impl = new UsingResolverImpl();
        CsmProject inPrj = onlyInProject ? fileImpl.getProject() : null;
        Collection<CsmNamespace> visNSs = impl.findVisibleNamespaces(fileImpl, offset, inPrj);
        for (CsmNamespace nsp : visNSs) {
            streamOut.println("NAMESPACE " + nsp.getName() + " (" + nsp.getQualifiedName() + ") ");
        }
        
        Collection<CsmDeclaration> visDecls = impl.findUsedDeclarations(fileImpl, offset, inPrj);
        for (CsmDeclaration decl : visDecls) {
            streamErr.println("DECLARATION " + decl);
        }
    }
    
    private void performTest(String source, int line, int column) throws Exception {
        boolean onlyInProject = false;
        super.performTest(source, getName() + ".nsp", getName() + ".decl", // NOI18N
                            line, column, onlyInProject);
    }    

}
