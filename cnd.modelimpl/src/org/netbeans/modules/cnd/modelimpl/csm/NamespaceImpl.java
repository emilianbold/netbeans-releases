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

package org.netbeans.modules.cnd.modelimpl.csm;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.*;
import java.util.*;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;

/**
 * CsmNamespace implementation
 * @author Vladimir Kvashin
 */
public class NamespaceImpl implements CsmNamespace, MutableDeclarationsContainer,
        Persistent, SelfPersistent, Disposable {
    
    private static final CharSequence GLOBAL = CharSequenceKey.create("$Global$"); // NOI18N)
    // only one of project/projectUID must be used (based on USE_UID_TO_CONTAINER)
    private /*final*/ ProjectBase projectRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmProject> projectUID;
    
    // only one of parent/parentUID must be used (based on USE_UID_TO_CONTAINER)
    private /*final*/ CsmNamespace parentRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmNamespace> parentUID;
    
    private final CharSequence name;
    private final CharSequence qualifiedName;
    
    /** maps namespaces FQN to namespaces */
    private Map<CharSequence, CsmUID<CsmNamespace>> nestedMap = new ConcurrentHashMap<CharSequence, CsmUID<CsmNamespace>>();
    
    private Map<CharSequence,CsmUID<CsmOffsetableDeclaration>> declarations = new ConcurrentHashMap<CharSequence,CsmUID<CsmOffsetableDeclaration>>();
    //private Collection/*<CsmNamespace>*/ nestedNamespaces = Collections.synchronizedList(new ArrayList/*<CsmNamespace>*/());
    
//    private Collection/*<CsmNamespaceDefinition>*/ definitions = new ArrayList/*<CsmNamespaceDefinition>*/();
    private Map<CharSequence,CsmUID<CsmNamespaceDefinition>> nsDefinitions = new TreeMap<CharSequence,CsmUID<CsmNamespaceDefinition>>(CharSequenceKey.Comparator);
    private ReadWriteLock nsDefinitionsLock = new ReentrantReadWriteLock();
    
    private final boolean global;
    
    /** Constructor used for global namespace */
    public NamespaceImpl(ProjectBase project) {
        this.name = GLOBAL;
        this.qualifiedName = CharSequenceKey.empty(); // NOI18N
        this.parentUID = null;
        this.parentRef = null;
        this.global = true;
        assert project != null;
        
        this.projectUID = UIDCsmConverter.projectToUID(project);
        assert this.projectUID != null;
            
        this.projectRef = null;
        project.registerNamespace(this);
    }
    
    private static final boolean CHECK_PARENT = false;
    
    public NamespaceImpl(ProjectBase project, NamespaceImpl parent, String name, String qualifiedName) {
        this.name = NameCache.getManager().getString(name);
        this.global = false;
        assert project != null;
        
        this.projectUID = UIDCsmConverter.projectToUID(project);
        assert this.projectUID != null;

        this.projectRef = null;
        this.qualifiedName = QualifiedNameCache.getManager().getString(qualifiedName);
        // TODO: rethink once more
        // now all classes do have namespaces
//        // TODO: this makes parent-child relationships assymetric, that's bad;
//        // on the other hand I dont like an idea of top-level namespaces' getParent() returning non-null
//        // Probably the CsmProject should have 2 methods:
//        // getGlobalNamespace() and getTopLevelNamespaces()
//        this.parent = (parent == null || parent.isGlobal()) ? null : parent;
        assert !CHECK_PARENT || parent != null;

        this.parentUID = UIDCsmConverter.namespaceToUID(parent);
        assert parentUID != null || parent == null;

        this.parentRef = null;
        
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
    
    public void dispose() {
        onDispose();
        notifyRemove();
    }    
    
    private void onDispose() {
        if (TraceFlags.RESTORE_CONTAINER_FROM_UID) {
            // restore container from it's UID
            this.projectRef = (ProjectBase) UIDCsmConverter.UIDtoProject(this.projectUID);
            assert this.projectRef != null || this.projectUID == null : "no object for UID " + this.projectUID;
            // restore container from it's UID
            this.parentRef = UIDCsmConverter.UIDtoNamespace(this.parentUID);
            assert this.parentRef != null || this.parentUID == null : "no object for UID " + this.parentUID;
        }
    }
    
    protected void notifyRemove() {
        assert !isGlobal();
        Notificator.instance().registerRemovedNamespace(this);
    }
    
    private static final String UNNAMED_PREFIX = "<unnamed>";  // NOI18N
    private Set<Integer> unnamedNrs = new HashSet<Integer>();
    public String getNameForUnnamedElement() {
        String out = UNNAMED_PREFIX;
        int minVal = getMinUnnamedValue();
        if (minVal != 0) {
            out = out + minVal;
        }
        unnamedNrs.add(Integer.valueOf(minVal));
        return out;
    }
    
    private int getMinUnnamedValue() {
        for (int i = 0; i < unnamedNrs.size(); i++) {
            if (!unnamedNrs.contains(Integer.valueOf(i))) {
                return i;
            }
        }
        return unnamedNrs.size();
    }
    
    public CsmNamespace getParent() {
        return _getParentNamespace();
    }
    
    public Collection<CsmNamespace> getNestedNamespaces() {
        Collection<CsmNamespace> out = UIDCsmConverter.UIDsToNamespaces(new ArrayList(nestedMap.values()));
        return out;
    }
    
    public Collection<CsmOffsetableDeclaration> getDeclarations() {
        Collection<CsmOffsetableDeclaration> decls = UIDCsmConverter.UIDsToDeclarations(new ArrayList<CsmUID<CsmOffsetableDeclaration>>(declarations.values()));
        return decls;
    }
    
    public boolean isGlobal() {
        return global;
    }
    
    public CharSequence getQualifiedName() {
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
    
    public CharSequence getName() {
        return name;
    }
    
    private NamespaceImpl _getNestedNamespace(CharSequence fqn) {
        fqn = CharSequenceKey.create(fqn);
        CsmUID<CsmNamespace> nestedNsUid = nestedMap.get(fqn);
        NamespaceImpl out = (NamespaceImpl)UIDCsmConverter.UIDtoNamespace(nestedNsUid);
        assert out != null || nestedNsUid == null;
        return out;
    }
    
    private void addNestedNamespace(NamespaceImpl nsp) {
        assert nsp != null;
        CsmUID<CsmNamespace> nestedNsUid = RepositoryUtils.put(nsp);
        assert nestedNsUid != null;
        nestedMap.put(nsp.getQualifiedName(), nestedNsUid);
        RepositoryUtils.put(this);
    }
    
    private void removeNestedNamespace(NamespaceImpl nsp) {
        assert nsp != null;
        CsmUID<CsmNamespace> nestedNsUid = nestedMap.remove(nsp.getQualifiedName());
        assert nestedNsUid != null;
        // handle unnamed namespace index
        if (nsp.getName().length() == 0) {
            String fqn = nsp.getQualifiedName().toString();
            int greaterInd = fqn.lastIndexOf('>');
            assert greaterInd >= 0;
            if (greaterInd + 1 < fqn.length()) {
                try {
                    Integer index = Integer.parseInt(fqn.substring(greaterInd+1));
                    unnamedNrs.remove(index);
                } catch (NumberFormatException ex) {
                    DiagnosticExceptoins.register(ex);
                }
            } else {
                unnamedNrs.remove(Integer.valueOf(0));
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
            } else {
                return;
            }
        }

        CharSequence uniqueName = declaration.getUniqueName();
        CsmOffsetableDeclaration oldDecl;
        
        CsmUID<CsmOffsetableDeclaration> oldDeclarationUid = declarations.get(uniqueName);
        oldDecl = UIDCsmConverter.UIDtoDeclaration(oldDeclarationUid);
        // use TraceFlags.SAFE_UID_ACCESS as workaround
        // see IZ#101952
        if (!TraceFlags.SAFE_UID_ACCESS ) {
            assert (oldDecl != null || oldDeclarationUid == null): "no object for UID " + oldDeclarationUid;
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
        
        CsmUID<CsmOffsetableDeclaration> newDeclarationUID = UIDCsmConverter.declarationToUID(declaration);
        declarations.put(uniqueName, newDeclarationUID);
        
//        if( "Cursor".equals(declaration.getName()) ) {
//            System.err.println("Cursor");
//        }
        // update repository
        RepositoryUtils.put(this);
        
        if( oldDecl != null ) { //&& oldDecl.getKind() == declaration.getKind() ) {
            //Notificator.instance().registerChangedDeclaration(oldDecl,declaration);
            // It's notificator responsibility of detecting change event.
            Notificator.instance().registerRemovedDeclaration(oldDecl);
            Notificator.instance().registerNewDeclaration(declaration);
        } else {
            Notificator.instance().registerNewDeclaration(declaration);
        }
    }
    
    private boolean isMine(VariableImpl v) {
        if( FileImpl.isOfFileScope(v) ) {
            return false;
        }
        return true;
    }
    
    public void removeDeclaration(CsmOffsetableDeclaration declaration) {
        CsmUID<CsmOffsetableDeclaration> declarationUid = declarations.remove(declaration.getUniqueName());
        // do not clean repository, it must be done from physical container of declaration
        if (false) RepositoryUtils.remove(declarationUid);
        // update repository
        RepositoryUtils.put(this);
        Notificator.instance().registerRemovedDeclaration(declaration);
    }
    
    public Collection<CsmNamespaceDefinition> getDefinitions()  {
        List<CsmUID<CsmNamespaceDefinition>> uids = new ArrayList<CsmUID<CsmNamespaceDefinition>>();
        try {
            nsDefinitionsLock.readLock().lock();
            uids.addAll(nsDefinitions.values());
        } finally {
            nsDefinitionsLock.readLock().unlock();
        }
        Collection<CsmNamespaceDefinition> defs = UIDCsmConverter.UIDsToDeclarations(uids);
        return defs;
    }
    
    public void addNamespaceDefinition(CsmNamespaceDefinition def) {
        CsmUID<CsmNamespaceDefinition> definitionUid = RepositoryUtils.put(def);
        try {
            nsDefinitionsLock.writeLock().lock();
            nsDefinitions.put(getSortKey(def), definitionUid);
        } finally {
            nsDefinitionsLock.writeLock().unlock();
        }
        // update repository
        RepositoryUtils.put(this);
    }
    
    public void removeNamespaceDefinition(CsmNamespaceDefinition def) {
        assert !this.isGlobal();
        boolean remove = false;
        CsmUID<CsmNamespaceDefinition> definitionUid = null;
        try {
            nsDefinitionsLock.writeLock().lock();
            definitionUid = nsDefinitions.remove(getSortKey(def));
        } finally {
            nsDefinitionsLock.writeLock().unlock();
        }
        // does not remove unregistered declaration from repository, it's responsibility of physical container
        if (false) RepositoryUtils.remove(definitionUid);
        // update repository about itself
        RepositoryUtils.put(this);
        try {
            nsDefinitionsLock.readLock().lock();
            remove =  (nsDefinitions.size() == 0);
        } finally {
            nsDefinitionsLock.readLock().unlock();
        }
        if (remove) {
            NamespaceImpl parent = (NamespaceImpl) _getParentNamespace();
            if (parent != null) {
                parent.removeNestedNamespace(this);
            }
            _getProject().unregisterNamesace(this);
            dispose();            
        }
    }
    
    public static String getSortKey(CsmNamespaceDefinition def) {
        StringBuilder sb = new StringBuilder(def.getContainingFile().getAbsolutePath());
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
    
    public Collection<CsmScopeElement> getScopeElements() {
        return (List) getDeclarations();
    }
    
    public CsmProject getProject() {
        return _getProject();
    }
    
    private CsmUID<CsmNamespace> uid = null;
    public CsmUID<CsmNamespace> getUID() {
        if (uid == null) {
            uid = createUID();
        }
        return uid;
    }
    
    protected CsmUID<CsmNamespace> createUID() {
	return UIDUtilities.createNamespaceUID(this);
    }
    
    private ProjectBase _getProject() {
        ProjectBase prj = this.projectRef;
        if (prj == null) {
            prj = (ProjectBase)UIDCsmConverter.UIDtoProject(this.projectUID);
            assert (prj != null || this.projectUID == null) : "empty project for UID " + this.projectUID;
        }
        return prj;
    }
    
    private CsmNamespace _getParentNamespace() {
        CsmNamespace ns = this.parentRef;
        if (ns == null) {
            ns = UIDCsmConverter.UIDtoNamespace(this.parentUID);
            assert (ns != null || this.parentUID == null) : "null object for UID " + this.parentUID;   
        }
        return ns;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getName());
        sb.append(' ');
        sb.append(getQualifiedName());
        sb.append(" NamespaceImpl @"); // NOI18N
        sb.append(hashCode());
        return sb.toString();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    
    public void write(DataOutput output) throws IOException {
        output.writeBoolean(this.global);
        
        UIDObjectFactory theFactory = UIDObjectFactory.getDefaultFactory();      
        // not null UID
        assert this.projectUID != null;
        theFactory.writeUID(this.projectUID, output);
        // can be null for global ns
        assert !CHECK_PARENT || this.parentUID != null || isGlobal();
        theFactory.writeUID(this.parentUID, output);
        
        assert this.name != null;
        output.writeUTF(this.name.toString());
        assert this.qualifiedName != null;
        output.writeUTF(this.qualifiedName.toString());
        theFactory.writeStringToUIDMap(this.nestedMap, output, true);
        theFactory.writeStringToUIDMap(this.declarations, output, true);
        try {
            nsDefinitionsLock.readLock().lock();
            theFactory.writeStringToUIDMap(this.nsDefinitions, output, false);
        } finally {
            nsDefinitionsLock.readLock().unlock();
        }
    }
    
    public NamespaceImpl(DataInput input) throws IOException {
        this.global = input.readBoolean();
        
        UIDObjectFactory theFactory = UIDObjectFactory.getDefaultFactory();
        
        this.projectUID = theFactory.readUID(input);
        this.parentUID = theFactory.readUID(input);
        // not null UID
        assert this.projectUID != null;
        assert !CHECK_PARENT || this.parentUID != null || this.global;
        this.projectRef = null;
        this.parentRef = null;
       

        this.name = NameCache.getManager().getString(input.readUTF());
        assert this.name != null;
        this.qualifiedName = QualifiedNameCache.getManager().getString(input.readUTF());
        assert this.qualifiedName != null;
        theFactory.readStringToUIDMap(this.nestedMap, input, QualifiedNameCache.getManager());
        theFactory.readStringToUIDMap(this.declarations, input, QualifiedNameCache.getManager());
        theFactory.readStringToUIDMap(this.nsDefinitions, input, QualifiedNameCache.getManager());
    }
    
    
}
