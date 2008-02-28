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
package org.netbeans.modules.cnd.completion.impl.xref;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.services.CsmFileReferences;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.completion.cplusplus.utils.Token;
import org.netbeans.modules.cnd.completion.cplusplus.utils.TokenUtilities;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;

/**
 *
 * @author Sergey Grinev
 */
public class FileReferencesImpl extends CsmFileReferences  {

    public FileReferencesImpl() {
        /*System.err.println("FileReferencesImpl registered");
        CsmModelAccessor.getModel().addProgressListener(new CsmProgressAdapter() {

            @Override
            public void fileParsingStarted(CsmFile file) {
                System.err.println("remove cache for " + file);
                cache.remove(file);
            }
            
            public @Override void fileInvalidated(CsmFile file) {
                System.err.println("remove cache for " + file);
                cache.remove(file);
            }
        });*/
    }
    
//    private final Map<CsmFile, List<CsmReference>> cache = new HashMap<CsmFile, List<CsmReference>>();

    public void accept(CsmScope csmScope, Visitor visitor) {
        accept(csmScope, visitor, CsmReferenceKind.ALL);
    }
    
    public void accept(CsmScope csmScope, Visitor visitor, EnumSet<CsmReferenceKind> kinds) {
        if (!CsmKindUtilities.isOffsetable(csmScope) && !CsmKindUtilities.isFile(csmScope)){
            return;
        }
        CsmFile csmFile = null;
        int start=0;
        int end = Integer.MAX_VALUE;
        if (CsmKindUtilities.isFile(csmScope)){
            csmFile = (CsmFile) csmScope;
        } else {
            csmFile = ((CsmOffsetable)csmScope).getContainingFile();
            start = ((CsmOffsetable)csmScope).getStartOffset();
            end = ((CsmOffsetable)csmScope).getEndOffset();
        }
        for (CsmReference ref : getIdentifierReferences(csmFile,start,end, kinds)) {
            visitor.visit(ref);
        }        
    }
    
    private List<CsmReference> getIdentifierReferences(CsmFile csmFile, int start, int end,
                                                        EnumSet<CsmReferenceKind> kinds) {
        List<CsmReference> out = new ArrayList<CsmReference>();
        BaseDocument doc = ReferencesSupport.getDocument(csmFile);
        assert doc != null;
        boolean needAfterDereferenceUsages = kinds.contains(CsmReferenceKind.AFTER_DEREFERENCE_USAGE);
        List<Token> tokens = TokenUtilities.getTokens(doc, start, end);
        Token lastToken = null;
        for (Token token : tokens) {
            if (token.getEndOffset() > end) {
                break;
            }
            if (token.getStartOffset() >= start) {
                if (token.getTokenID() == CCTokenContext.IDENTIFIER) {
                    boolean skip = false;
                    
                    if (!needAfterDereferenceUsages && lastToken != null) {
                        switch (lastToken.getTokenID().getNumericID()) {
                            case CCTokenContext.DOT_ID:
                            case CCTokenContext.DOTMBR_ID:
                            case CCTokenContext.ARROW_ID:
                            case CCTokenContext.ARROWMBR_ID:
                            case CCTokenContext.SCOPE_ID:
                                skip = true;
                        }
                    }
                    if (!skip) {
                        ReferenceImpl ref = ReferencesSupport.createReferenceImpl(csmFile, doc, token.getStartOffset(), token);
                        out.add(ref);
                    }
                }
            }
            lastToken = token;
        }
        return out;
    }
}
