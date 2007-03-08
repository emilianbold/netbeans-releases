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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import javax.imageio.stream.FileImageInputStream;
import org.netbeans.modules.cnd.api.model.*;
import java.util.*;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;

/**
 * @author Vladimir Kvasihn
 */
public class Resolver3 implements Resolver {

    private ProjectBase project;
    private CsmFile file;
    private int offset;

    private List usedNamespaces = new ArrayList();
    private Map namespaceAliases = new HashMap()/*<String, CsmNamespace>*/;
    private Map usingDeclarations = new HashMap()/*<String, CsmDeclaration>*/;
    
    private CsmTypedef currTypedef;

    private String[] names;
    private int currNamIdx;

    private String currName() {
        return (names != null && currNamIdx < names.length) ? names[currNamIdx] : "";
    }

    private CsmNamespace containingNamespace;
    private CsmClass containingClass;
    private boolean contextFound = false;

    private CsmNamespace getContainingNamespace() {
        if( ! contextFound ) {
            findContext();
        }
        return containingNamespace;
    }
    
    private CsmClass getContainingClass() {
        if( ! contextFound ) {
            findContext();
        }
        return containingClass;
    }
    
    private void findContext() {
        contextFound = true;
        findContext(file.getDeclarations());
    }
    
    private Set visitedFiles = new HashSet();
    {
	visitedFiles.add(this.file);
    }

    //private CsmNamespace currentNamespace;
    
    public Resolver3(CsmFile file, int offset) {
        this.file = file;
        this.offset = offset;
        this.project = (ProjectBase) file.getProject();
    }
    
    public Resolver3(CsmOffsetable context) {
        this.file = context.getContainingFile();
        this.offset = context.getStartOffset();
        this.project = (ProjectBase) context.getContainingFile().getProject();
    }

    private CsmClassifier findClassifier(CsmNamespace ns, String qulifiedNamePart) {
        CsmClassifier result = null;
        while ( ns != null  && result == null) {
            String fqn = ns.getQualifiedName() + "::" + qulifiedNamePart; // NOI18N
            result = findClassifier(fqn);
            ns = ns.getParent();
        }        
        return result;
    }
    private CsmClassifier findClassifier(String qualifiedName) {
        CsmClassifier result = project.findClassifier(qualifiedName);
        if( result == null ) {
            for (Iterator iter = project.getLibraries().iterator(); iter.hasNext() && result == null;) {
                CsmProject lib = (CsmProject) iter.next();
                result = lib.findClassifier(qualifiedName);
            }
        }
        return result;
    }
    
    public CsmNamespace findNamespace(String qualifiedName) {
        CsmNamespace result = project.findNamespace(qualifiedName);
        if( result == null ) {
            for (Iterator iter = project.getLibraries().iterator(); iter.hasNext() && result == null;) {
                CsmProject lib = (CsmProject) iter.next();
                result = lib.findNamespace(qualifiedName);
            }
        }
        return result;
    }

    private void findContext(Iterable declarations) {
        for (Iterator it = declarations.iterator(); it.hasNext();) {
            CsmDeclaration decl = (CsmDeclaration) it.next();
            if( decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
                CsmNamespaceDefinition nd = (CsmNamespaceDefinition) decl;
                if( nd.getStartOffset() < this.offset && this.offset < nd.getEndOffset()  ) {
                    containingNamespace = nd.getNamespace();
                    findContext(nd.getDeclarations());
                }
            }
            else if(   decl.getKind() == CsmDeclaration.Kind.CLASS 
                    || decl.getKind() == CsmDeclaration.Kind.STRUCT
                    || decl.getKind() == CsmDeclaration.Kind.UNION ) {
                
                CsmClass cls = (CsmClass) decl;
                if( cls.getStartOffset() < this.offset && this.offset < cls.getEndOffset()  ) {
                    containingClass = cls;
                }
            }
            else if( decl.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ) {
                CsmFunctionDefinition fd = (CsmFunctionDefinition) decl;
                if( fd.getStartOffset() < this.offset && this.offset < fd.getEndOffset()  ) {
                    CsmNamespace ns = getFunctionDefinitionNamespace(fd);
                    if( ns != null && ! ns.isGlobal() ) {
                        containingNamespace = ns;
                    }
                    CsmFunction fun = fd.getDeclaration();
                    if( fun != null && CsmKindUtilities.isMethod(fun) ) {
                        containingClass = ((CsmMethod) fun).getContainingClass();
                    }
                }
            }
        }
    }

    private CsmNamespace getFunctionDefinitionNamespace(CsmFunctionDefinition def) {
        CsmFunction fun = def.getDeclaration();
        if( fun != null ) {
            CsmScope scope = fun.getScope();
            if( CsmKindUtilities.isNamespace(scope)) {
                CsmNamespace ns = (CsmNamespace) scope;
                return ns;
            }
            else if( CsmKindUtilities.isClass(scope) ) {
                CsmNamespace ns = ((CsmClass) scope).getContainingNamespace();
                return ns;
            }
        }
        return null;
    }

    protected void gatherMaps(CsmFile file) {
        if( visitedFiles.contains(file) ) {
            return;
        }
        visitedFiles.add(file);
        
        for (Iterator iter = file.getIncludes().iterator(); iter.hasNext();) {
            CsmInclude inc = (CsmInclude) iter.next();
            CsmFile incFile = inc.getIncludeFile();
            if( incFile != null ) {
                int oldOffset = offset;
                offset = Integer.MAX_VALUE;
                gatherMaps(incFile);
                offset = oldOffset;
            }
        }
        gatherMaps(file.getDeclarations());
    }
    
    protected void gatherMaps(Iterable declarations) {
        for( Iterator it = declarations.iterator(); it.hasNext(); ) {
            Object o = it.next();
            assert o instanceof CsmOffsetable;
            try {
                int start = ((CsmOffsetable) o).getStartOffset();
                int end = ((CsmOffsetable) o).getEndOffset();
                if( start >= this.offset ) {
                    break;
                }
                //assert o instanceof CsmScopeElement;
                if( o instanceof CsmScopeElement ) {
                    gatherMaps((CsmScopeElement) o, end);
                }
                else {
                    if( FileImpl.reportErrors ) {
                        System.err.println("Expected CsmScopeElement, got " + o);
                    }
                }
            } catch (NullPointerException ex) {
                if( FileImpl.reportErrors ) {
                    // FIXUP: do not crush on NPE
                    System.err.println("Unexpected NULL element in declarations collection");
                    ex.printStackTrace(System.err);
                }                
            }
        }
    }
    
    private void doProcessTypedefsInUpperNamespaces(CsmNamespaceDefinition nsd) {
        for (Iterator iter = nsd.getDeclarations().iterator(); iter.hasNext();) {
            CsmDeclaration decl = (CsmDeclaration) iter.next();
            if( decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
                processTypedefsInUpperNamespaces((CsmNamespaceDefinition) decl);
            }
            else if( decl.getKind() == CsmDeclaration.Kind.TYPEDEF ) {
                CsmTypedef typedef = (CsmTypedef) decl; 
                if( currName().equals(typedef.getName()) ) {
                    currTypedef = typedef;
                }
            }
        }
    }
    
    private void processTypedefsInUpperNamespaces(CsmNamespaceDefinition nsd) {
        if( nsd.getName().equals(currName()) ) {
            currNamIdx++;
            doProcessTypedefsInUpperNamespaces(nsd);
        }
        else {
            CsmNamespace cns = getContainingNamespace();
            if( cns != null ) {
                if( cns.equals(nsd.getNamespace())) {
                    doProcessTypedefsInUpperNamespaces(nsd);
                }
            }
        }
    }
    
    /**
     * It is quaranteed that element.getStartOffset < this.offset
     */
    protected void gatherMaps(CsmScopeElement element, int end) {

        CsmDeclaration.Kind kind = (element instanceof CsmDeclaration) ? ((CsmDeclaration) element).getKind() : null;
        if( kind == CsmDeclaration.Kind.NAMESPACE_DEFINITION ) {
            CsmNamespaceDefinition nsd = (CsmNamespaceDefinition) element;
            if( this.offset < end ) {
                //currentNamespace = nsd.getNamespace();
                gatherMaps(nsd.getDeclarations());
            }
            else {
                processTypedefsInUpperNamespaces(nsd);
            }
        }
        else if( kind == CsmDeclaration.Kind.NAMESPACE_ALIAS ) {
            CsmNamespaceAlias alias = (CsmNamespaceAlias) element;
            namespaceAliases.put(alias.getAlias(), alias.getReferencedNamespace());
        }
        else if( kind == CsmDeclaration.Kind.USING_DECLARATION ) {
            CsmUsingDeclaration udecl = (CsmUsingDeclaration) element;
            CsmDeclaration decl = udecl.getReferencedDeclaration();
            if( decl != null ) {
                String id;
                if( decl.getKind() == CsmDeclaration.Kind.FUNCTION || decl.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ) {
                    // TODO: decide how to resolve functions
                    id = ((CsmFunction) decl).getSignature();
                }
                else {
                    id = decl.getName();
                }
                usingDeclarations.put(id, decl);
            }
        }
        else if( kind == CsmDeclaration.Kind.USING_DIRECTIVE ) {
            CsmUsingDirective udir = (CsmUsingDirective) element;
            usedNamespaces.add(udir.getName()); // getReferencedNamespace()
        }
        else if( element instanceof CsmDeclarationStatement ) {
            CsmDeclarationStatement ds = (CsmDeclarationStatement) element;
            if( ds.getStartOffset() < this.offset ) {
                gatherMaps( ((CsmDeclarationStatement) element).getDeclarators());
            }
        }
        else if( element instanceof CsmScope ) {
            if( this.offset < end ) {
                gatherMaps( ((CsmScope) element).getScopeElements());
            }
        }
        else if( kind == CsmDeclaration.Kind.TYPEDEF ) {
            CsmTypedef typedef = (CsmTypedef) element; 
            if( currName().equals(typedef.getName()) ) {
                currTypedef = typedef;
            }
        }
    }
    
    
    public CsmObject resolve(String qualified) {
        return resolve(Utils.splitQualifiedName(qualified));
    }
    
    /**
     * Resolver class or namespace name.
     * Why class or namespace? Because in usage of kind org::vk::test
     * you don't know which is class and which is namespace name 
     *
     * @param nameTokens tokenized name to resolve 
     * (for example, for std::vector it is new String[] { "std", "vector" })
     *
     * @param context declaration within which the name found
     *
     * @return object of the following class:
     *  CsmClass 
     *  CsmEnum
     *  CsmNamespace
     */
    public CsmObject resolve(String[] nameTokens) {
        CsmObject result = null;
        
        names = nameTokens;
        currNamIdx = 0;
        CsmNamespace containingNS = null;
        
        if( nameTokens.length == 1 ) {
            result = findClassifier(nameTokens[0]);
            if( result == null ) {
                result = findNamespace(nameTokens[0]);
            }
	    if( result == null ) {
                containingNS = getContainingNamespace();
                result = findClassifier(containingNS, nameTokens[0]);
	    }
            if( result == null ) {
                CsmClass cls = getContainingClass();
		result = resolveInClass(cls, nameTokens[0]);
		if( result == null ) {
		    result = resolveInBaseClasses(cls, nameTokens[0]);
		}
            }
            if( result == null ) {
                currTypedef = null;
                visitedFiles.clear();
		visitedFiles.add(this);
                gatherMaps(file);
                if( currTypedef != null ) {
                    CsmType type = currTypedef.getType();
                    if( type != null ) {
                        result = type.getClassifier();
                    }
                }

                if( result == null ) {
                    CsmDeclaration decl = (CsmDeclaration) usingDeclarations.get(nameTokens[0]);
                    if( decl != null ) {
                        result = decl;
                    }
                }
                
                if( result == null ) {    
                    for (Iterator iter = usedNamespaces.iterator(); iter.hasNext();) {
                        String nsp = (String) iter.next();
			String fqn = nsp + "::" + nameTokens[0]; // NOI18N
                        result = findClassifier(fqn);
                        if (result == null) {
                            result = findClassifier(containingNS, fqn);
                        }
                        if( result != null ) {
                            break;
                        }
                    }
                }
                
                if( result == null ) {
                    Object o = namespaceAliases.get(nameTokens[0]);
                    if( o instanceof CsmNamespace ) {
                        result = (CsmNamespace) o;
                    }
                }
            }
        }
        else if( nameTokens.length > 1 ) {
            StringBuffer sb = new StringBuffer(nameTokens[0]);
            for (int i = 1; i < nameTokens.length; i++) {
                sb.append("::"); // NOI18N
                sb.append(nameTokens[i]);
            }
            result = findClassifier(sb.toString());
            if( result == null ) {
//                containingNS = getContainingNamespace();
//                result = findClassifier(containingNS, sb.toString());
//            }
//            if( result == null ) {
                result = findNamespace(sb.toString());
            }
            if( result == null ) {
                gatherMaps(file);
                if( currTypedef != null ) {
                    CsmType type = currTypedef.getType();
                    if( type != null ) {
			boolean overflow = Thread.currentThread().getStackTrace().length > 200;
			if( overflow ) {
                                traceOverflow(type, sb.toString());
			}
			if( ! overflow ) {
			    result = type.getClassifier();
			}
                    }
                }
                if( result == null ) {
                    CsmObject first = this.resolve(nameTokens[0]);
                    if( first != null ) {
                        if( first instanceof CsmNamespace ) {
                            NamespaceImpl ns = (NamespaceImpl) first;
                            sb = new StringBuffer(ns.getQualifiedName());
                            for (int i = 1; i < nameTokens.length; i++) {
                                sb.append("::"); // NOI18N
                                sb.append(nameTokens[i]);
                            }
                            result = findClassifier(sb.toString());
                        }
                        else if( first instanceof CsmClass ) {

                        }
                    }
                }
                else {
                    gatherMaps(file);
                    if( currTypedef != null ) {
                        CsmType type = currTypedef.getType();
                        if( type != null ) {
                            result = type.getClassifier();
                        }
                    }
                }
            }
        }
        if( result == null ) {
            result = project.getDummyForUnresolved(nameTokens, file, offset);
        }
                
//        CsmObject curr = null;
//        for( int i = 0; i < nameTokens.length; i++ ) {
//            String name = nameTokens[i];
//        }
        return result;
    }

    private void traceOverflow(final CsmOffsetable type, final String sb) {
        CsmOffsetable.Position pos = type.getStartPosition();
        System.err.println("\n\n'nINFINITE LOOP. FILE: " + type.getContainingFile().getAbsolutePath() + " POS " + new CsmTracer().getOffsetString(type));
        int ln = 1;
        String text = file.getText(0, offset);
        for( int i = 0; i < text.length(); i++ ) {
				if( text.charAt(i) == '\n') {
				    ln++;
				}
        }
        System.err.println("while resolving " + sb + " in " + file.getAbsolutePath() + " at " + ln + (type instanceof CsmOffsetableDeclaration ? " using declaration " + ((CsmOffsetableDeclaration)type).getUniqueName() : ""));
        
        Thread.currentThread().dumpStack();
    }
    

    private CsmObject resolveInBaseClasses(CsmClass cls, String name) {
	if( cls != null && cls.isValid()) {
	    for( CsmInheritance inh : (List<CsmInheritance>) cls.getBaseClasses() ) {
                if (inh.getContainingFile() != this.file || inh.getStartOffset() < this.offset) {
                    boolean overflow = Thread.currentThread().getStackTrace().length > 200;
                    if( overflow ) {
                        traceOverflow(cls, name);
                        return null;
                    } else {
                        CsmClass base = inh.getCsmClass();
                        CsmObject result = resolveInClass(base, name);
                        if( result != null ) {
                            return result;
                        }
                        result = resolveInBaseClasses(base, name);
                        if( result != null ) {
                            return result;
                        }
                    }
                } else {
                    break;
                }
	    }
	}
	return null;
    }
    
    private CsmObject resolveInClass(CsmClass cls, String name) {
	if( cls != null && cls.isValid()) {
	    String fqn = cls.getQualifiedName() + "::" + name; // NOI18N
	    return findClassifier(fqn);
	}
	return null;
    }
    
}
