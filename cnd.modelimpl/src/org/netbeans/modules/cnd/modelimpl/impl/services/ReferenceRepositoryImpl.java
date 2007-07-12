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

import antlr.TokenStream;
import antlr.TokenStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVariableDefinition;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 * prototype implementation of service
 * @author Vladimir Voskresensky
 */
public class ReferenceRepositoryImpl extends CsmReferenceRepository {
    
    public enum ReferenceKind {
        DEFINITION,
        DECLARATION,
        USAGE,
    }
    
    public ReferenceRepositoryImpl() {
    }
    
    public Collection<CsmReference> getReferences(CsmObject target, CsmProject project, boolean includeSelfDeclarations) {
        if (!(project instanceof ProjectBase)) {
            return Collections.<CsmReference>emptyList();
        }
        ProjectBase basePrj = (ProjectBase)project;
        
        CsmObject[] decDef = getDefinitionDeclaration(target);
        CsmObject decl = decDef[0];
        CsmObject def = decDef[1];
        
        Collection<FileImpl> files = getFiles(decl, basePrj);
        
        List<CsmReference> out = new ArrayList<CsmReference>(files.size() * 10);
        for (FileImpl file : files) {
            out.addAll(getReferences(decl, def, file, includeSelfDeclarations));
        }
        return out;
    }
    
    public Collection<CsmReference> getReferences(CsmObject target, CsmFile file, boolean includeSelfDeclarations) {
        if (!(file instanceof FileImpl)) {
            return Collections.<CsmReference>emptyList();
        } else {
            CsmObject[] decDef = getDefinitionDeclaration(target);
            CsmObject decl = decDef[0];
            CsmObject def = decDef[1];            
            return getReferences(decl, def, (FileImpl)file, includeSelfDeclarations);
        }
    }
    
    public Map<CsmObject, Collection<CsmReference>> getReferences(CsmObject[] targets, CsmProject project, boolean includeSelfDeclarations) {
        Map<CsmObject, Collection<CsmReference>> out = new HashMap<CsmObject, Collection<CsmReference>>(targets.length);
        for (CsmObject target : targets) {
            out.put(target, getReferences(target, project, includeSelfDeclarations));
        }
        return out;
    }
    
    public Map<CsmObject, Collection<CsmReference>> getReferences(CsmObject[] targets, CsmFile file, boolean includeSelfDeclarations) {
        Map<CsmObject, Collection<CsmReference>> out = new HashMap<CsmObject, Collection<CsmReference>>(targets.length);
        for (CsmObject target : targets) {
            out.put(target, getReferences(target, file, includeSelfDeclarations));
        }
        return out;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // prototype of impl
    
    private Collection<CsmReference> getReferences(CsmObject targetDecl, CsmObject targetDef, FileImpl file, boolean includeSelfDeclarations) {
        assert targetDecl != null;
        assert file != null;
        String name = "";
        if (CsmKindUtilities.isNamedElement(targetDecl)) {
            name = ((CsmNamedElement)targetDecl).getName();
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
        Collection<APTToken> tokens = getTokensToResolve(file, name);
        if (TraceFlags.TRACE_XREF_REPOSITORY) {
            time = System.currentTimeMillis() - time;
            System.err.println("collecting tokens");
        }
        for (APTToken token : tokens) {
            // this is candidate to resolve
            int offset = token.getOffset();
            CsmReference ref = CsmReferenceResolver.getDefault().findReference(file, offset);
            if (acceptReference(ref, targetDecl, targetDef, includeSelfDeclarations)) {
                out.add(ref);
            }
        }
        return out;
    }
    
    private Collection<APTToken> getTokensToResolve(FileImpl file, String name) {
        // in prototype use just unexpanded identifier tokens in file
        TokenStream ts = getTokenStream(file);
        Collection<APTToken> tokens = new ArrayList<APTToken>(100);
        boolean destructor = false;
        if (name.startsWith("~")) {
            destructor = true;
            name = name.substring(1);
        }
        if (ts != null) {
            try {
                APTToken token = (APTToken) ts.nextToken();
                APTToken prev = null;
                while (!APTUtils.isEOF(token)) {
                    if (APTUtils.isID(token) && name.equals(token.getText())) {
                        // this is candidate to resolve
                        if (!destructor || (prev != null && prev.getType() == APTTokenTypes.TILDE)) {
                            tokens.add(token);
                        }
                    }
                    prev = token;
                    token = (APTToken) ts.nextToken();
                }
            } catch (TokenStreamException ex) {
                ex.printStackTrace(System.err);
            }
        }
        return tokens;
    }
    
    private TokenStream getTokenStream(FileImpl file) {
        // build token stream for file
        InputStream stream = null;
        TokenStream ts = null;
        try {
            stream = file.getBuffer().getInputStream();
            ts = APTTokenStreamBuilder.buildTokenStream(file.getAbsolutePath(), stream);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
            ts = null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ex) {
                    ex.printStackTrace(System.err);
                }
            }
        }
        return ts == null ? null : file.getLanguageFilter().getFilteredStream( new APTCommentsFilter(ts));
    }
    
    private boolean acceptReference(CsmReference ref, CsmObject targetDecl, CsmObject targetDef, boolean includeSelfDeclarations) {
        assert targetDecl != null;
        boolean accept = false;
        if (ref != null && ref.getReferencedObject() == targetDecl) {
            if (includeSelfDeclarations) {
                accept = true;
            } else {
                accept = (getReferenceKind(ref, targetDecl, targetDef) == ReferenceKind.USAGE);
            }
        }
        return accept;
    }
    
    public static ReferenceKind getReferenceKind(CsmReference ref, CsmObject targetDecl, CsmObject targetDef) {
        assert targetDecl != null;
        CsmObject owner = ref.getOwner();
        assert owner != null;        
        ReferenceKind kind;
        if (owner == targetDecl) {
            kind = ReferenceKind.DECLARATION;
        } else if (owner == targetDef) {
            kind = ReferenceKind.DEFINITION;
        } else {
            kind = ReferenceKind.USAGE;
        }
        return kind;
    }
    
    public static CsmObject[] getDefinitionDeclaration(CsmObject target) {
        CsmObject decl;
        CsmObject def; 
        if (CsmKindUtilities.isVariableDefinition(target)) {
            decl = ((CsmVariableDefinition)target).getDeclaration();
            if (decl == null) {
                decl = target;
                if (TraceFlags.TRACE_XREF_REPOSITORY) {
                    System.err.println("not found declaration for variable definition " + target);
                }
            }
            def = target;
        } else if (CsmKindUtilities.isVariableDeclaration(target)) {
            decl = target;
            def = ((CsmVariable)target).getDefinition();
        } else if (CsmKindUtilities.isFunctionDefinition(target)) {
            decl = ((CsmFunctionDefinition)target).getDeclaration();
            if (decl == null) {
                decl = target;
                if (TraceFlags.TRACE_XREF_REPOSITORY) {
                    System.err.println("not found declaration for function definition " + target);
                }
            }
            def = target;
        } else if (CsmKindUtilities.isFunctionDeclaration(target)) {
            decl = target;
            def = ((CsmFunction)target).getDefinition();
        } else {
            decl = target;
            def = null;
        }
        assert decl != null;
        return new CsmObject[] { decl, def };
    }

    private Collection<FileImpl> getFiles(CsmObject decl, ProjectBase basePrj) {
        assert decl != null;
        boolean retFile = false; // fake flag
        if (retFile && CsmKindUtilities.isOffsetable(decl)) {
            return Collections.<FileImpl>singleton((FileImpl)(((CsmOffsetable)decl).getContainingFile()));
        }
        CsmObject scopeElem = decl;
        while (CsmKindUtilities.isScopeElement(scopeElem)) {
            CsmScope scope = ((CsmScopeElement)scopeElem).getScope();
            if (CsmKindUtilities.isFunction(scope)) {
                CsmFile file = ((CsmFunction)scope).getContainingFile();
                return Collections.<FileImpl>singleton((FileImpl)file);
            } else if (CsmKindUtilities.isScopeElement(scope)) {
                scopeElem = ((CsmScopeElement)scope);
            } else {
                break;
            }
        }
        return basePrj.getAllFiles();
    }
}
