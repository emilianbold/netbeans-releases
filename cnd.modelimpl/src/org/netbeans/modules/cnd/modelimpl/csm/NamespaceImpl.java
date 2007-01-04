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

package org.netbeans.modules.cnd.modelimpl.csm;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import java.util.*;

import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 * CsmNamespace implementation
 * @author Vladimir Kvashin
 */
public class NamespaceImpl implements CsmNamespace, MutableDeclarationsContainer {

    private ProjectBase project;
    private CsmNamespace parent;
    private String name;
    private String qualifiedName;

    /** maps namespaces FQN to namespaces */
    private Map/*<String, CsmNamespaceImpl>*/ nestedMap = Collections.synchronizedMap(new HashMap/*<String, CsmNamespaceImpl>*/());
    
    private Map/*<String,CsmDeclaration>*/ declarations = Collections.synchronizedMap(new HashMap/*<String,CsmDeclaration>*/());
    //private Collection/*<CsmNamespace>*/ nestedNamespaces = Collections.synchronizedList(new ArrayList/*<CsmNamespace>*/());
    
//    private Collection/*<CsmNamespaceDefinition>*/ definitions = new ArrayList/*<CsmNamespaceDefinition>*/();
    private Map/*<String,CsmNamespaceDefinition>*/ definitions = Collections.synchronizedSortedMap(new TreeMap/*<String,CsmNamespaceDefinition>*/());

    private boolean global;
    
    /** Constructor used for global namespace and unnamed top-level namespaces */
    public NamespaceImpl(ProjectBase project, boolean global) {
        this.name = "";
        this.qualifiedName = "";
        this.parent = null;
        this.global = global;
        this.project = project;
        project.registerNamespace(this);
        notifyCreation();
    }
    
    public NamespaceImpl(ProjectBase project, NamespaceImpl parent, String name) {
        this.name = name;
        this.global = false;
        this.project = project;
        this.qualifiedName = getQualifiedName(parent,  name);
        // TODO: rethink once more
        // now all classes do have namespaces
//        // TODO: this makes parent-child relationships assymetric, that's bad;
//        // on the other hand I dont like an idea of top-level namespaces' getParent() returning non-null 
//        // Probably the CsmProject should have 2 methods: 
//        // getGlobalNamespace() and getTopLevelNamespaces()
//        this.parent = (parent == null || parent.isGlobal()) ? null : parent;
        this.parent = parent;
        if( parent != null ) {
            // nb: this.parent should be set first, since getQualidfiedName request parent's fqn
            parent.addNestedNamespace(this);
        }
        project.registerNamespace(this);
        notifyCreation();
    }
    
    protected void notifyCreation() {
        if( ! isGlobal() ) {
            Notificator.instance().registerNewNamespace(this);
        }
    }
    
    private static int unnamedNr = 0;
    public static String getQualifiedName(NamespaceImpl parent, String name) {
        return (parent == null || parent.isGlobal()) ? name : parent.getQualifiedName() + "::" + 
                (name.length()==0 ? ("<unnamed>"+unnamedNr++):name);
    }
    
    public CsmNamespace getParent() {
        return parent;
    }
    
    public Collection/*<CsmNamespace>*/ getNestedNamespaces() {
        //return new ArrayList(nestedNamespaces);
        return new ArrayList(nestedMap.values());
    }
    
//    public Collection/*<CsmDeclaration>*/ getDeclarations() {
//        return getDeclarations(true);
//    }
//    
//    public Collection/*<CsmDeclaration>*/ getDeclarations(boolean wait) {
//        if( wait ) {
//            long l = Diagnostic.start();
//            project.waitParse();
//            Diagnostic.stop(l, "%% getDeclarations(): ensureAllParsed took ");
//        }
//        return new ArrayList(declarations.values());
//    }
    
    public Collection/*<CsmDeclaration>*/ getDeclarations() {
        return new ArrayList(declarations.values());
    }
    
    public boolean isGlobal() {
        return global;        
    }
    
    public String getQualifiedName() {
        return qualifiedName;
    }

    /** creates or gets (if already exists) namespace with the given name and current parent */
    public NamespaceImpl getNamespace(String name) {
        String fqn = Utils.getQualifiedName(name,  this);
        NamespaceImpl impl = (NamespaceImpl) nestedMap.get(fqn);
        if( impl == null ) {
                impl = new NamespaceImpl(project, this, name);
                // it would register automatically
                //nestedMap.put(fqn, impl);
                //nestedNamespaces.add(impl);
        }
        return impl;
    }

    public String getName() {
        return name;
    }


    private void addNestedNamespace(NamespaceImpl nsp) {
        //nestedNamespaces.add(nsp);
        nestedMap.put(nsp.getQualifiedName(), nsp);
    }
     
    public void addDeclaration(CsmOffsetableDeclaration declaration) {
        
        // don't put unnamed declarations into namespace
        if( declaration.getName().length() == 0 ) {
            return;
        }
        
        // TODO: remove this dirty hack!
        if( (declaration instanceof VariableImpl) ) {
            VariableImpl v = (VariableImpl) declaration;
	    if( isMine(v) ) {
                v.setScope(this);
            }
	    else {
		return;
	    }
        }
//	else if( declaration instanceof FunctionImpl ) {
//	}
	String uniqueName = declaration.getUniqueName();
	CsmDeclaration oldDecl = (CsmDeclaration) declarations.get(uniqueName);
        
//	// replace declaration with new one unless
//	// 1) it's a function 2) old one contains body 3) new one does not
//	if( oldDecl instanceof CsmFunctionDefinition ) {
//	    if( ! (declaration instanceof CsmFunctionDefinition) ) {
//		return;
//	    }
//	}
        // TODO: replace this hack with proper processing
        if( oldDecl != null && oldDecl.getKind() == CsmDeclaration.Kind.FUNCTION &&  declaration.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ) {
            //CsmFunction func = (CsmFunction) oldDecl;
            //CsmFunctionDefinition fdef = (CsmFunctionDefinition) declaration;
            //if( ! func.getContainingFile().getName().equals(declaration.getContainingFile().getName()) ) {
                return;
            //} 
        }
        
        declarations.put(uniqueName, declaration);

//        if( "Cursor".equals(declaration.getName()) ) {
//            System.err.println("Cursor");
//        }
        if( oldDecl != null ) { //&& oldDecl.getKind() == declaration.getKind() ) {
            Notificator.instance().registerChangedDeclaration(declaration);
        }
        else {
            Notificator.instance().registerNewDeclaration(declaration);
        }
    }
	
    private boolean isMine(VariableImpl v) {
	if( FileImpl.isOfFileScope(v) ) {
	    return false;
	}
	if( v.isExtern() ) {
	    if( v.getInitialValue() == null ) {
		return false;
	    }
	}
	return true;
    }
    
    public void removeDeclaration(CsmOffsetableDeclaration declaration) {
        declarations.remove(declaration.getUniqueName());
	Notificator.instance().registerRemovedDeclaration(declaration);
    }
    
    public Collection/*<CsmNamespaceDefinition>*/ getDefinitions()  {
//        return definitions;
        return new ArrayList(definitions.values());
    }
    
    public void addNamespaceDefinition(NamespaceDefinitionImpl def) {
//        definitions.add(def);
        definitions.put(getSortKey(def), def);
    }
    
    public static String getSortKey(NamespaceDefinitionImpl def) {
        StringBuffer sb = new StringBuffer(def.getContainingFile().getAbsolutePath());
        int start = ((CsmOffsetable) def).getStartOffset();
        String s = Integer.toString(start);
        int gap = 8 - s.length();
        while( gap-- > 0 ) {
            sb.append('0');
        }
        sb.append(s);
        sb.append(def.getName());
        return sb.toString();
    }    

    public List getScopeElements() {
        return (List) getDeclarations();
    }
    
    
}
