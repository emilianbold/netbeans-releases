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
import org.netbeans.modules.cnd.api.model.*;
import java.util.*;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
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
    
    private final String name;
    private final String qualifiedName;

    /** maps namespaces FQN to namespaces */
    private Map/*<String, CsmNamespaceImpl>*/ nestedMapOLD = Collections.synchronizedMap(new HashMap/*<String, CsmNamespaceImpl>*/());
    private Map<String, CsmUID<CsmNamespace>> nestedMap = Collections.synchronizedMap(new HashMap<String, CsmUID<CsmNamespace>>());
    
    private Map/*<String,CsmDeclaration>*/ declarationsOLD = Collections.synchronizedMap(new HashMap/*<String,CsmDeclaration>*/());
    private Map<String,CsmUID<CsmOffsetableDeclaration>> declarations = Collections.synchronizedMap(new HashMap<String,CsmUID<CsmOffsetableDeclaration>>());
    //private Collection/*<CsmNamespace>*/ nestedNamespaces = Collections.synchronizedList(new ArrayList/*<CsmNamespace>*/());
    
//    private Collection/*<CsmNamespaceDefinition>*/ definitions = new ArrayList/*<CsmNamespaceDefinition>*/();
    private Map/*<String,CsmNamespaceDefinition>*/ definitionsOLD = Collections.synchronizedSortedMap(new TreeMap/*<String,CsmNamespaceDefinition>*/());
    private Map<String,CsmUID<CsmNamespaceDefinition>> nsDefinitions = Collections.synchronizedSortedMap(new TreeMap<String,CsmUID<CsmNamespaceDefinition>>());

    private final boolean global;
    
    /** Constructor used for global namespace */
    public NamespaceImpl(ProjectBase project) {
        this.name = "$Global$"; // NOI18N
        this.qualifiedName = ""; // NOI18N
        _setParentNamespace(null);
        this.global = true;
        this._setProject(project);
        project.registerNamespace(this);
    }
    
    public NamespaceImpl(ProjectBase project, NamespaceImpl parent, String name, String qualifiedName) {
        this.name = name;
        this.global = false;
        this._setProject(project);
        this.qualifiedName = qualifiedName;
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
        assert !isGlobal();
        Notificator.instance().registerNewNamespace(this);
    }
    
    private static final String UNNAMED_PREFIX = "<unnamed>";  // NOI18N
    private Set<Integer> unnamedNrs = new HashSet<Integer>();
    public String getNameForUnnamedElement() {
        String out = UNNAMED_PREFIX;
        int minVal = getMinUnnamedValue();
        if (minVal != 0) {
            out = out + minVal;
        }
        unnamedNrs.add(new Integer(minVal));
        return out;
    }
    
    private int getMinUnnamedValue() {
        for (int i = 0; i < unnamedNrs.size(); i++) {
            if (!unnamedNrs.contains(new Integer(i))) {
                return i;
            }
        }
        return unnamedNrs.size();
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
        assert name != null && name.length() != 0 : "non empty namespace should be asked";
        String fqn = Utils.getNestedNamespaceQualifiedName(name,  this, true);
        NamespaceImpl impl = _getNestedNamespace(fqn);
        if( impl == null ) {
                impl = new NamespaceImpl(_getProject(), this, name, fqn);
                // it would register automatically
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
        if (TraceFlags.USE_REPOSITORY) {
            RepositoryUtils.put(this);
        }    
    }
     
    private void removeNestedNamespace(NamespaceImpl nsp) {
        if (TraceFlags.USE_REPOSITORY) {
            assert nsp != null;
            CsmUID<CsmNamespace> uid = nestedMap.remove(nsp.getQualifiedName());
            assert uid != null;
        } else {
            nestedMapOLD.remove(nsp.getQualifiedName());
        }
        // handle unnamed namespace index
        if (nsp.getName().length() == 0) {
            String fqn = nsp.getQualifiedName();
            int greaterInd = fqn.lastIndexOf('>');
            assert greaterInd >= 0;
            if (greaterInd + 1 < fqn.length()) {
                try {
                    Integer index = Integer.parseInt(fqn.substring(greaterInd+1));
                    unnamedNrs.remove(index);
                } catch (NumberFormatException ex) {
                    ex.printStackTrace(System.err);
                }
            } else {
                unnamedNrs.remove(new Integer(0));
            }
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
        // update repository
        if (TraceFlags.USE_REPOSITORY) {
            RepositoryUtils.put(this);
        }
        
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
            RepositoryUtils.remove(uid);
            // update repository
            RepositoryUtils.put(this);
        } else {
            declarationsOLD.remove(declaration.getUniqueName());
        }
	Notificator.instance().registerRemovedDeclaration(declaration);
    }
    
    public Collection/*<CsmNamespaceDefinition>*/ getDefinitions()  {
//        return definitions;
        if (TraceFlags.USE_REPOSITORY) {
            List<CsmUID<CsmNamespaceDefinition>> uids = new ArrayList<CsmUID<CsmNamespaceDefinition>>(nsDefinitions.values());
            List<CsmNamespaceDefinition> defs = UIDCsmConverter.UIDsToDeclarations(uids);
            return defs;
        } else {
            return new ArrayList(definitionsOLD.values());
        }
    }
    
    public void addNamespaceDefinition(CsmNamespaceDefinition def) {
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmNamespaceDefinition> uid = RepositoryUtils.put(def);
            nsDefinitions.put(getSortKey(def), uid);
            // update repository
            RepositoryUtils.put(this);            
        } else {
            definitionsOLD.put(getSortKey(def), def);
        }
    }
    
    public void removeNamespaceDefinition(CsmNamespaceDefinition def) {
        assert !this.isGlobal();
        boolean remove = false;
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmNamespaceDefinition> uid = nsDefinitions.remove(getSortKey(def));
            // update repository
            RepositoryUtils.remove(uid);
            RepositoryUtils.put(this);  
            remove =  (nsDefinitions.size() == 0);
        } else {
            definitionsOLD.remove(getSortKey(def));
            remove = (definitionsOLD.size() == 0);
        }
        if (remove) {
            NamespaceImpl parent = (NamespaceImpl) _getParentNamespace();
            if (parent != null) {
                parent.removeNestedNamespace(this);
            }
            _getProject().unregisterNamesace(this);
            Notificator.instance().registerRemoveNamespace(this);
        }
    }
    
    public static String getSortKey(CsmNamespaceDefinition def) {
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
    
    public String toString() {
	StringBuilder sb = new StringBuilder(getName());
	sb.append(' ');
	sb.append(getQualifiedName());
	sb.append(" NamespaceImpl @");
	sb.append(hashCode());
	return sb.toString();
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
        theFactory.writeStringToUIDMap(this.nestedMap, output, true);
        theFactory.writeStringToUIDMap(this.declarations, output, true);
        theFactory.writeStringToUIDMap(this.nsDefinitions, output, true);
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
