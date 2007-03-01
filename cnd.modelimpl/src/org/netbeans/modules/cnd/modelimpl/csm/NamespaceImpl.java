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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.modules.cnd.api.model.*;
import java.util.*;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * CsmNamespace implementation
 * @author Vladimir Kvashin
 */
public class NamespaceImpl implements CsmNamespace, MutableDeclarationsContainer,
                                        Persistent, SelfPersistent {

    // only one of project/projectUID must be used (based on USE_REPOSITORY)
    private ProjectBase projectOLD;
    private CsmUID<CsmProject> projectUID;
    
    // only one of parent/parentUID must be used (based on USE_REPOSITORY)
    private CsmNamespace parentOLD;
    private CsmUID<CsmNamespace> parentUID;
    
    private String name;
    private String qualifiedName;

    /** maps namespaces FQN to namespaces */
    private Map/*<String, CsmNamespaceImpl>*/ nestedMapOLD = Collections.synchronizedMap(new HashMap/*<String, CsmNamespaceImpl>*/());
    private Map<String, CsmUID<CsmNamespace>> nestedMap = Collections.synchronizedMap(new HashMap<String, CsmUID<CsmNamespace>>());
    
    private Map/*<String,CsmDeclaration>*/ declarationsOLD = Collections.synchronizedMap(new HashMap/*<String,CsmDeclaration>*/());
    private Map<String,CsmUID<CsmOffsetableDeclaration>> declarations = Collections.synchronizedMap(new HashMap<String,CsmUID<CsmOffsetableDeclaration>>());
    //private Collection/*<CsmNamespace>*/ nestedNamespaces = Collections.synchronizedList(new ArrayList/*<CsmNamespace>*/());
    
//    private Collection/*<CsmNamespaceDefinition>*/ definitions = new ArrayList/*<CsmNamespaceDefinition>*/();
    private Map/*<String,CsmNamespaceDefinition>*/ definitionsOLD = Collections.synchronizedSortedMap(new TreeMap/*<String,CsmNamespaceDefinition>*/());
    private Map<String,CsmUID<CsmNamespaceDefinition>> nsDefinitions = Collections.synchronizedSortedMap(new TreeMap<String,CsmUID<CsmNamespaceDefinition>>());

    private boolean global;
    
    /** Constructor used for global namespace */
    public NamespaceImpl(ProjectBase project) {
        this.name = "$Global$"; // NOI18N
        this.qualifiedName = ""; // NOI18N
        _setParentNamespace(null);
        this.global = true;
        this._setProject(project);
        project.registerNamespace(this);
        notifyCreation();
    }
    
    public NamespaceImpl(ProjectBase project, NamespaceImpl parent, String name) {
        this.name = name;
        this.global = false;
        this._setProject(project);
        this.qualifiedName = getQualifiedName(parent,  name);
        // TODO: rethink once more
        // now all classes do have namespaces
//        // TODO: this makes parent-child relationships assymetric, that's bad;
//        // on the other hand I dont like an idea of top-level namespaces' getParent() returning non-null 
//        // Probably the CsmProject should have 2 methods: 
//        // getGlobalNamespace() and getTopLevelNamespaces()
//        this.parent = (parent == null || parent.isGlobal()) ? null : parent;
        _setParentNamespace(parent);
        project.registerNamespace(this);
        if( parent != null ) {
            // nb: this.parent should be set first, since getQualidfiedName request parent's fqn
            parent.addNestedNamespace(this);
        }
        notifyCreation();
    }
    
    protected void notifyCreation() {
        if( ! isGlobal() ) {
            Notificator.instance().registerNewNamespace(this);
        }
    }
    
    private static int unnamedNr = 0;
    public static String getQualifiedName(NamespaceImpl parent, String name) {
        return (parent == null || parent.isGlobal()) ? name : parent.getQualifiedName() + "::" + // NOI18N
                (name.length()==0 ? ("<unnamed>"+unnamedNr++):name); // NOI18N
    }
    
    public CsmNamespace getParent() {
        return _getParentNamespace();
    }
    
    public Collection/*<CsmNamespace>*/ getNestedNamespaces() {
        if (TraceFlags.USE_REPOSITORY) {
            List<CsmNamespace> out = UIDCsmConverter.UIDsToNamespaces(new ArrayList(nestedMap.values()));
            return out;
        } else {
            return new ArrayList(nestedMapOLD.values());
        }
    }
    
    public Collection/*<CsmDeclaration>*/ getDeclarations() {
        if (TraceFlags.USE_REPOSITORY) {
            List<CsmOffsetableDeclaration> decls = UIDCsmConverter.UIDsToDeclarations(new ArrayList<CsmUID<CsmOffsetableDeclaration>>(declarations.values()));
            return decls;
        } else {
            return new ArrayList(declarationsOLD.values());
        }
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
        NamespaceImpl impl = _getNestedNamespace(fqn);
        if( impl == null ) {
                impl = new NamespaceImpl(_getProject(), this, name);
                // it would register automatically
                //nestedMap.put(fqn, impl);
                //nestedNamespaces.add(impl);
        }
        return impl;
    }

    public String getName() {
        return name;
    }

    private NamespaceImpl _getNestedNamespace(String fqn) {
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmNamespace> uid = nestedMap.get(fqn);
            NamespaceImpl out = (NamespaceImpl)UIDCsmConverter.UIDtoNamespace(uid);
            assert out != null || uid == null;
            return out;
        } else {
            return (NamespaceImpl) nestedMapOLD.get(fqn);
        }
    }

    private void addNestedNamespace(NamespaceImpl nsp) {
        if (TraceFlags.USE_REPOSITORY) {
            assert nsp != null;
            CsmUID<CsmNamespace> uid = RepositoryUtils.put(nsp);
            assert uid != null;
            nestedMap.put(nsp.getQualifiedName(), uid);
        } else {
            nestedMapOLD.put(nsp.getQualifiedName(), nsp);
        }
    }
     
    public void addDeclaration(CsmOffsetableDeclaration declaration) {
        
        if( !ProjectBase.canRegisterDeclaration(declaration) ) {
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
	CsmDeclaration oldDecl; 
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmOffsetableDeclaration> uid = declarations.get(uniqueName);
            oldDecl = UIDCsmConverter.UIDtoDeclaration(uid);
            assert oldDecl != null || uid == null;
        } else {
            oldDecl = (CsmDeclaration) declarationsOLD.get(uniqueName);
        }
        
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
        
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmOffsetableDeclaration> uid = UIDCsmConverter.declarationToUID(declaration);
            declarations.put(uniqueName, uid);
        } else {
            declarationsOLD.put(uniqueName, declaration);
        }

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
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmOffsetableDeclaration> uid = declarations.remove(declaration.getUniqueName());
            // clean repository
            if (true) RepositoryUtils.remove(uid);
        } else {
            declarationsOLD.remove(declaration.getUniqueName());
        }
	Notificator.instance().registerRemovedDeclaration(declaration);
    }
    
    public Collection/*<CsmNamespaceDefinition>*/ getDefinitions()  {
//        return definitions;
        if (TraceFlags.USE_REPOSITORY) {
//            if (false) {
//                List<Key> keys = new ArrayList<Key>(nsDefinitions.values());
//                List defs = RepositoryUtils.getDeclarations(keys);
//            }
            List<CsmUID<CsmNamespaceDefinition>> uids = new ArrayList<CsmUID<CsmNamespaceDefinition>>(nsDefinitions.values());
            List<CsmNamespaceDefinition> defs = UIDCsmConverter.UIDsToDeclarations(uids);
            return defs;
        } else {
            return new ArrayList(definitionsOLD.values());
        }
    }
    
    public void addNamespaceDefinition(NamespaceDefinitionImpl def) {
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmNamespaceDefinition> uid = RepositoryUtils.put(def);
            nsDefinitions.put(getSortKey(def), uid);
        } else {
            definitionsOLD.put(getSortKey(def), def);
        }
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

    public CsmProject getProject() {
        return _getProject();
    }

    private CsmUID<CsmNamespace> uid = null;
    public CsmUID<CsmNamespace> getUID() {
        if (uid == null) {
            uid = UIDUtilities.createNamespaceUID(this);
        }
        return uid;
    }   
    
    private void _setProject(ProjectBase project) {
        if (TraceFlags.USE_REPOSITORY) {
            this.projectUID = UIDCsmConverter.projectToUID(project);
        } else {
            this.projectOLD = project;
        }
    }
    
    private ProjectBase _getProject() {
        ProjectBase prj = this.projectOLD;
        if (TraceFlags.USE_REPOSITORY) {
            prj = (ProjectBase)UIDCsmConverter.UIDtoProject(projectUID);
        }        
        return prj;
    }

    private void _setParentNamespace(CsmNamespace ns) {
        if (TraceFlags.USE_REPOSITORY) {
            parentUID = UIDCsmConverter.namespaceToUID(ns);
        } else {
            this.parentOLD = ns;
        }
    }
    
    private CsmNamespace _getParentNamespace() {
        if (TraceFlags.USE_REPOSITORY) {
            CsmNamespace ns = UIDCsmConverter.UIDtoNamespace(parentUID);
            return ns;
        } else {
            return parentOLD;
        }        
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    
    public void write(DataOutput output) throws IOException {
        UIDObjectFactory theFactory = UIDObjectFactory.getDefaultFactory();
        theFactory.writeUID(this.projectUID, output);
        theFactory.writeUID(this.parentUID, output);
        assert this.name != null;
        output.writeUTF(this.name);
        assert this.qualifiedName != null;
        output.writeUTF(this.qualifiedName);
        theFactory.writeStringToUIDMap(this.nestedMap, output);
        theFactory.writeStringToUIDMap(this.declarations, output);
        theFactory.writeStringToUIDMap(this.nsDefinitions, output);
        output.writeBoolean(this.global);
    }

    public NamespaceImpl (DataInput input) throws IOException {
        UIDObjectFactory theFactory = UIDObjectFactory.getDefaultFactory();
        this.projectUID = theFactory.readUID(input);
        this.parentUID = theFactory.readUID(input);
        this.name = TextCache.getString(input.readUTF());
        assert this.name != null;
        this.qualifiedName = QualifiedNameCache.getString(input.readUTF());
        assert this.qualifiedName != null;
        theFactory.readStringToUIDMap(this.nestedMap, input, QualifiedNameCache.getManager());
        theFactory.readStringToUIDMap(this.declarations, input, TextCache.getManager());
        theFactory.readStringToUIDMap(this.nsDefinitions, input, QualifiedNameCache.getManager());
        this.global = input.readBoolean();
    }
    
    
}
