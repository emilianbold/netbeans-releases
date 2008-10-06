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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import antlr.TokenStream;
import antlr.TokenStreamException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.deep.CsmGotoStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmLabel;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.openide.util.Exceptions;

/**
 * prototype implementation of service
 * @author Vladimir Voskresensky
 */
public class ReferenceRepositoryImpl extends CsmReferenceRepository {
    
    public ReferenceRepositoryImpl() {
    }
    
    public Collection<CsmReference> getReferences(CsmObject target, CsmProject project, Set<CsmReferenceKind> kinds, Interrupter interrupter) {
        if (!(project instanceof ProjectBase)) {
            return Collections.<CsmReference>emptyList();
        }
        ProjectBase basePrj = (ProjectBase)project;
        boolean unboxInstantiation = true;
        CsmObject[] decDef = CsmBaseUtilities.getDefinitionDeclaration(target, unboxInstantiation);
        CsmObject decl = decDef[0];
        CsmObject def = decDef[1];
        
        CsmScope scope = getDeclarationScope(decl);
        CsmFile scopeFile = CsmKindUtilities.isOffsetable(scope) ? ((CsmOffsetable)scope).getContainingFile() : null;
        List<CsmReference> out;
        Collection<FileImpl> files;
        if (scopeFile instanceof FileImpl) {
            out = new ArrayList<CsmReference>(10);
            CsmOffsetable offs = (CsmOffsetable)scope;
            out.addAll(getReferences(decl, def, (FileImpl)scopeFile, kinds, unboxInstantiation, offs.getStartOffset(), offs.getEndOffset(), interrupter));
        } else {
            files = basePrj.getAllFileImpls();
            out = new ArrayList<CsmReference>(files.size() * 10);
            for (FileImpl file : files) {
                if (interrupter != null && interrupter.cancelled()) {
                    break;
                }
                out.addAll(getReferences(decl, def, file, kinds,unboxInstantiation, 0, Integer.MAX_VALUE, interrupter));
            }
        }
        return out;
    }
    
    public Collection<CsmReference> getReferences(CsmObject target, CsmFile file, Set<CsmReferenceKind> kinds, Interrupter interrupter) {
        CsmScope scope = getDeclarationScope(target);
        CsmFile scopeFile = CsmKindUtilities.isOffsetable(scope) ? ((CsmOffsetable)scope).getContainingFile() : null;
        if (!(file instanceof FileImpl)) {
            return Collections.<CsmReference>emptyList();
        } else if (scopeFile != null && !scopeFile.equals(file)) {
            // asked file is not scope file for target object
            return Collections.<CsmReference>emptyList();
        } else {
            boolean unboxInstantiation = true;
            CsmObject[] decDef = CsmBaseUtilities.getDefinitionDeclaration(target, unboxInstantiation);
            CsmObject decl = decDef[0];
            CsmObject def = decDef[1];            
            int start = 0, end = Integer.MAX_VALUE;
            if (CsmKindUtilities.isOffsetable(scope)) {
                start = ((CsmOffsetable)scope).getStartOffset();
                end = ((CsmOffsetable)scope).getEndOffset();
            }
            return getReferences(decl, def, (FileImpl)file, kinds, unboxInstantiation, start,end, interrupter);
        }
    }
    
    public Map<CsmObject, Collection<CsmReference>> getReferences(CsmObject[] targets, CsmProject project, Set<CsmReferenceKind> kinds, Interrupter interrupter) {
        Map<CsmObject, Collection<CsmReference>> out = new HashMap<CsmObject, Collection<CsmReference>>(targets.length);
        for (CsmObject target : targets) {
            if (interrupter != null && interrupter.cancelled()) {
                break;
            }
            out.put(target, getReferences(target, project, kinds, interrupter));
        }
        return out;
    }
    
    public Collection<CsmReference> getReferences(CsmObject[] targets, CsmFile file, Set<CsmReferenceKind> kinds, Interrupter interrupter) {
        Collection<CsmReference> refs = new LinkedHashSet<CsmReference>(1024);
        // TODO: optimize performance
        for (CsmObject target : targets) {            
            refs.addAll(getReferences(target, file, kinds, interrupter));
        }
        if (!refs.isEmpty() && targets.length > 1) {
            // if only one target, then collection is already sorted
            List<CsmReference> sortedRefs = new ArrayList<CsmReference>(refs);
            Collections.sort(sortedRefs, new Comparator<CsmReference>() {
                public int compare(CsmReference o1, CsmReference o2) {
                    return o1.getStartOffset() - o2.getStartOffset();
                }
            });    
            refs = sortedRefs;
        }
        return refs;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // prototype of impl
    
    private Collection<CsmReference> getReferences(CsmObject targetDecl, CsmObject targetDef, FileImpl file, 
            Set<CsmReferenceKind> kinds, boolean unboxInstantiation, int startOffset, int endOffset, Interrupter interrupter) {
        assert targetDecl != null;
        assert file != null;
        CharSequence name = "";
        if (CsmKindUtilities.isNamedElement(targetDecl)) {
            name = ((CsmNamedElement)targetDecl).getName();
        } else if (CsmKindUtilities.isStatement(targetDecl)) {
            if (targetDecl instanceof CsmLabel) {
                name = ((CsmLabel)targetDecl).getLabel();
            } else if (targetDecl instanceof CsmGotoStatement){
                name = ((CsmGotoStatement)targetDecl).getLabel();
            }
        }
        if (name.length() == 0) {
            if (TraceFlags.TRACE_XREF_REPOSITORY) {
                System.err.println("resolving unnamed element is not yet supported " + targetDecl);
            }
            return Collections.<CsmReference>emptyList();
        }
        if (TraceFlags.TRACE_XREF_REPOSITORY) {
            System.err.println("resolving " + name + " in file " + file.getAbsolutePath());
        }
        Collection<CsmReference> out = new ArrayList<CsmReference>(20);
        long time = 0;
        if (TraceFlags.TRACE_XREF_REPOSITORY) {
            time = System.currentTimeMillis();
        }
        Collection<APTToken> tokens = getTokensToResolve(file, name.toString(), startOffset, endOffset);
        if (TraceFlags.TRACE_XREF_REPOSITORY) {
            time = System.currentTimeMillis() - time;
            System.err.println("collecting tokens");
        }
        for (APTToken token : tokens) {
            if (interrupter != null && interrupter.cancelled()){
                break;
            }
            // this is candidate to resolve
            int offset = token.getOffset();
            CsmReference ref = CsmReferenceResolver.getDefault().findReference(file, offset);
            if (acceptReference(ref, targetDecl, targetDef, kinds, unboxInstantiation)) {
                out.add(ref);
            }
        }
        return out;
    }
    
    // Fast check of name.
    private boolean hasName(FileImpl file, String name){
        try {
            String text = file.getBuffer().getText();
            if (text.indexOf(name) < 0) {
                return false;
            }
            // TODO use grep by line and detect whole word
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return true;
    }
    
    private Collection<APTToken> getTokensToResolve(FileImpl file, String name, int startOffset, int endOffset) {
        // in prototype use just unexpanded identifier tokens in file
        if (!hasName(file, name)){
            return Collections.<APTToken>emptyList();
        }
        TokenStream ts = getTokenStream(file);
        Collection<APTToken> tokens = new ArrayList<APTToken>(100);
        boolean destructor = false;
        if (name.startsWith("~")) { // NOI18N
            destructor = true;
            name = name.substring(1);
        }
        if (ts != null) {
            try {
                APTToken token = (APTToken) ts.nextToken();
                APTToken prev = null;
                while (!APTUtils.isEOF(token)) {
                    if (token.getOffset() >= startOffset) {
                        int id = token.getType();
                        if ((id == APTTokenTypes.ID || id == APTTokenTypes.ID_DEFINED) &&
                                name.equals(token.getText())) {
                            // this is candidate to resolve
                            if (!destructor || (prev != null && prev.getType() == APTTokenTypes.TILDE)) {
                                tokens.add(token);
                            }
                        }
                    }
                    if (token.getEndOffset() > endOffset) {
                        break;
                    }
                    prev = token;
                    token = (APTToken) ts.nextToken();
                }
            } catch (TokenStreamException ex) {
                DiagnosticExceptoins.register(ex);
            }
        }
        return tokens;
    }
    
    private TokenStream getTokenStream(FileImpl file) {
        // build token stream for file
        Reader reader = null;
        TokenStream ts = null;
        try {
            reader = file.getBuffer().getReader();
            ts = APTTokenStreamBuilder.buildTokenStream(file.getAbsolutePath(), reader);
        } catch (IOException ex) {
            DiagnosticExceptoins.register(ex);
            ts = null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    DiagnosticExceptoins.register(ex);
                }
            }
        }
        APTPreprocHandler preprocHandler = file.getPreprocHandler();
        APTPreprocHandler.State ppState = preprocHandler == null ? null : preprocHandler.getState();        
        return ts == null ? null : file.getLanguageFilter(ppState).getFilteredStream( new APTCommentsFilter(ts));
    }

    private boolean acceptReference(CsmReference ref, CsmObject targetDecl, CsmObject targetDef, 
            Set<CsmReferenceKind> kinds, boolean unboxInstantiation) {
        assert targetDecl != null;
        boolean accept = false;
        CsmObject referencedObj = ref == null ? null : ref.getReferencedObject();
        if (unboxInstantiation && CsmKindUtilities.isTemplateInstantiation(referencedObj)) {
            referencedObj = ((CsmInstantiation)referencedObj).getTemplateDeclaration();
        }
        if (targetDecl.equals(referencedObj) || (targetDef != null && targetDef.equals(referencedObj))) {
            if (kinds == CsmReferenceKind.ALL) {
                accept = true;
            } else { 
                CsmReferenceKind kind = ref.getKind();
                accept = kinds.contains(kind);
            }
        }
        return accept;
    }   

    private CsmScope getDeclarationScope(CsmObject decl) {
        assert decl != null;
        CsmObject scopeElem = decl;
        while (CsmKindUtilities.isScopeElement(scopeElem)) {
            CsmScope scope = ((CsmScopeElement)scopeElem).getScope();
            if (CsmKindUtilities.isFunction(scope)) {
                return ((CsmFunction)scope);
            } else if (CsmKindUtilities.isScopeElement(scope)) {
                scopeElem = ((CsmScopeElement)scope);
            } else {
                break;
            }
        }
        return null;        
    }
}
