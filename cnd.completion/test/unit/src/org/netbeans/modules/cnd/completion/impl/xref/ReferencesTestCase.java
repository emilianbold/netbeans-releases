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

package org.netbeans.modules.cnd.completion.impl.xref;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.completion.test.ProjectBasedTestCase;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;

/**
 *
 * @author Vladimir Voskresensky
 */
public class ReferencesTestCase extends ProjectBasedTestCase {
    
    public ReferencesTestCase(String testName) {
        super(testName);
    }    

    public void testCpuH() throws Exception {
        performTest("cpu.h");
    }
    
    public void testCpuCC() throws Exception {
        performTest("cpu.cc");
    }

    public void testCustomerH() throws Exception {
        performTest("customer.h");
    }
    
    public void testCustomerCC() throws Exception {
        performTest("customer.cc");
    }
    
    public void testDiskH() throws Exception {
        performTest("disk.h");
    }
    
    public void testDiskCC() throws Exception {
        performTest("disk.cc");
    }
    
    public void testMemoryH() throws Exception {
        performTest("memory.h");
    }
    
    public void testMemoryCC() throws Exception {
        performTest("memory.cc");
    }
    
    public void testModuleH() throws Exception {
        performTest("module.h");
    }
    
    public void testModuleCC() throws Exception {
        performTest("module.cc");
    }
    
    public void testSystemH() throws Exception {
        performTest("system.h");
    }
    
    public void testSystemCC() throws Exception {
        performTest("system.cc");
    }
    
    public void testQuoteCC() throws Exception {
        performTest("quote.cc");
    }
    
    private void performTest(String source) throws Exception {
        File testSourceFile = getDataFile(source);
        CsmFile csmFile = getCsmFile(testSourceFile);
        BaseDocument doc = getBaseDocument(testSourceFile);
        ExtSyntaxSupport ssup = (ExtSyntaxSupport)doc.getSyntaxSupport();
        TokenItem token = ssup.getTokenChain(0, doc.getLength());
        log("creating list of references:");
        List<ReferenceImpl> refs = new ArrayList(1024);        
        while (token != null) {
            if (supportReference(token.getTokenID())) {
                ReferenceImpl ref = ReferencesSupport.createReferenceImpl(csmFile, doc, token);
                assertNotNull("reference must not be null for valid token " + token, ref);
                refs.add(ref);
                log(ref.toString());
            }
            token = token.getNext();
        }
        log("end of references list");
        log("start resolving referenced objects");
        for (ReferenceImpl ref : refs) {
            CsmObject owner = ref.getOwner();
            ref(ref.toString());
            ref("--OWNER:\n    " + CsmTracer.toString(owner));
            CsmObject out = ref.getReferencedObject();
            ref("--RESOLVED TO:\n    " + CsmTracer.toString(out));
            ref("==============================================================");
        }
        log("end of resolving referenced objects");
        compareReferenceFiles();
    }   

    private boolean supportReference(TokenID tokenID) {
        assert tokenID != null;
        switch (tokenID.getNumericID()) {
            case CCTokenContext.IDENTIFIER_ID:
            case CCTokenContext.SYS_INCLUDE_ID:
            case CCTokenContext.USR_INCLUDE_ID:
                return true;
        }
        return false;
    }
}
