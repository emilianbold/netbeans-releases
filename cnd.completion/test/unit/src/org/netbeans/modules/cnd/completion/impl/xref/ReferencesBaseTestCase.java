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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;
import org.netbeans.modules.cnd.modelimpl.test.ProjectBasedTestCase;

/**
 *
 * @author vv159170
 */
public class ReferencesBaseTestCase extends ProjectBasedTestCase {

    public ReferencesBaseTestCase(String testName) {
        super(testName);
    }  
    
    protected final void performTest(String source) throws Exception {
        File testSourceFile = getDataFile(source);
        CsmFile csmFile = getCsmFile(testSourceFile);
        BaseDocument doc = getBaseDocument(testSourceFile);
        ExtSyntaxSupport ssup = (ExtSyntaxSupport) doc.getSyntaxSupport();
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

    protected boolean supportReference(TokenID tokenID) {
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
