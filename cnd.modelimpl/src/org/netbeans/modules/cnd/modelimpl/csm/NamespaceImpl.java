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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.*;
import java.util.*;
import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.utils.cache.FilePathCache;

/**
 * CsmNamespace implementation
 * @author Vladimir Kvashin
 */
public class NamespaceImpl implements CsmNamespace, MutableDeclarationsContainer,
        Persistent, SelfPersistent, Disposable, CsmIdentifiable {
    
    private static final CharSequence GLOBAL = CharSequenceKey.create("$Global$"); // NOI18N
    // only one of project/projectUID must be used (based on USE_UID_TO_CONTAINER)
    private Object projectRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmProject> projectUID;
    
    // only one of parent/parentUID must be used (based on USE_UID_TO_CONTAINER)
    private /*final*/ CsmNamespace parentRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmNamespace> parentUID;
    
    private final CharSequence name;
    private final CharSequence qualifiedName;
    
    /** maps namespaces FQN to namespaces */
    private final Map<CharSequence, CsmUID<CsmNamespace>> nestedNamespaces;
    
    private final Key declarationsSorageKey;

    private final Set<CsmUID<CsmOffsetableDeclaration>> unnamedDeclarations;
    
    private final TreeMap<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>> nsDefinitions;
    private final ReadWriteLock nsDefinitionsLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock projectLock = new ReentrantReadWriteLock();
    
    private final boolean global;
    
    /** Constructor used for global namespace */
    public NamespaceImpl(ProjectBase project, boolean fake) {
        this.name = GLOBAL;
        this.qualifiedName = CharSequenceKey.empty(); // NOI18N
        this.parentUID = null;
        this.parentRef = null;
        this.global = true;
        assert project != null;
        
        this.projectUID = UIDCsmConverter.projectToUID(project);
        assert this.projectUID != null;
        unnamedDeclarations = Collections.synchronizedSet(new HashSet<CsmUID<CsmOffsetableDeclaration>>());
        nestedNamespaces = new ConcurrentHashMap<CharSequence, CsmUID<CsmNamespace>>();
        nsDefinitions = new TreeMap<FileNameSortedKey, CsmUID<CsmNamespaceDefinition>>(defenitionComparator);

        this.projectRef = new WeakReference<ProjectBase>(project);
        this.declarationsSorageKey = fake ? null : new DeclarationContainer(this).getKey();
        if (!fake) {
            project.registerNamespace(this);
        }
    }
    
    private static final boolean CHECK_PARENT = false;
    
    public NamespaceImpl(ProjectBase project, NamespaceImpl parent, String name, String qualifiedName) {
        this.name = NameCache.getManager().getString(name);
        this.global = false;
        assert project != null;
        
        this.projectUID = UIDCsmConverter.projectToUID(project);
        assert this.projectUID != null;
        unnamedDeclarations = Collections.synchronizedSet(new HashSet<CsmUID<CsmOffsetableDeclaration>>());
        nestedNamespaces = new ConcurrentHashMap<CharSequence, CsmUID<CsmNamespace>>();
        nsDefinitions = new TreeMap<FileNameSortedKey,CsmUID<CsmNamespaceDefinition>>(defenitionComparator);

        this.projectRef = new WeakReference<ProjectBase>(project);
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
        declarationsSorageKey = new DeclarationContainer(this).getKey();
        
        project.registerNamespace(this);
        if( parent != null ) {
            // nb: this.parent should be set first, since getQualidfiedName request parent's fqn
            parent.addNestedNamespace(this);
        }
        notify(this, NotifyEvent.NAMESPACE_ADDED);
    }

    protected enum NotifyEvent {
        DECLARATION_ADDED,
        DECLARATION_REMOVED,
        NAMESPACE_ADDED,
        NAMESPACE_REMOVED,
    }

    protected void notify(CsmObject obj, NotifyEvent kind) {
        switch (kind) {
            case DECLARATION_ADDED:
                assert obj instanceof CsmOffsetableDeclaration;
                if (!ForwardClass.isForwardClass((CsmOffsetableDeclaration)obj)) {
                    // no need to notify about fake classes
                    Notificator.instance().registerNewDeclaration((CsmOffsetableDeclaration)obj);
                }
                break;
            case DECLARATION_REMOVED:
                assert obj instanceof CsmOffsetableDeclaration;
                if (!ForwardClass.isForwardClass((CsmOffsetableDeclaration)obj)) {
                    // no need to notify about fake classes
                    Notificator.instance().registerRemovedDeclaration((CsmOffsetableDeclaration)obj);
                }
                break;
            case NAMESPACE_ADDED:
                assert obj instanceof CsmNamespace;
                assert !((CsmNamespace)obj).isGlobal();
                Notificator.instance().registerNewNamespace((CsmNamespace)obj);
                break;
            case NAMESPACE_REMOVED:
                assert obj instanceof CsmNamespace;
                assert !((CsmNamespace)obj).isGlobal();
                Notificator.instance().registerRemovedNamespace((CsmNamespace)obj);
                break;
            default:
                throw new IllegalArgumentException("unexpected kind " + kind); // NOI18N
        }
    }
    
    public void dispose() {
        onDispose();
        notify(this, NotifyEvent.NAMESPACE_REMOVED);
    }    
    
    private void onDispose() {
        projectLock.writeLock().lock();
        try {
            if (projectRef == null) {
                // restore container from it's UID
                this.projectRef = (ProjectBase) UIDCsmConverter.UIDtoProject(this.projectUID);
                assert this.projectRef != null || this.projectUID == null : "no object for UID " + this.projectUID;
            }
            if (parentRef == null) {
                // restore container from it's UID
                this.parentRef = UIDCsmConverter.UIDtoNamespace(this.parentUID);
                assert this.parentRef != null || this.parentUID == null : "no object for UID " + this.parentUID;
            }
        } finally {
            projectLock.writeLock().unlock();
        }
        weakDeclarationContainer = null;
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
        Collection<CsmNamespace> out = UIDCsmConverter.UIDsToNamespaces(new ArrayList<CsmUID<CsmNamespace>>(nestedNamespaces.values()));
        return out;
    }

    private WeakReference<DeclarationContainer> weakDeclarationContainer = TraceFlags.USE_WEAK_MEMORY_CACHE ?  new WeakReference<DeclarationContainer>(null) : null;
    private DeclarationContainer getDeclarationsSorage() {
        if (declarationsSorageKey == null) {
            return DeclarationContainer.empty();
        }
        DeclarationContainer dc = null;
        WeakReference<DeclarationContainer> weak = null;
        if (TraceFlags.USE_WEAK_MEMORY_CACHE) {
            weak = weakDeclarationContainer;
            if (weak != null) {
                dc = weak.get();
                if (dc != null) {
                    return dc;
                }
            }
        }
        dc = (DeclarationContainer) RepositoryUtils.get(declarationsSorageKey);
        if (dc == null) {
            DiagnosticExceptoins.register(new IllegalStateException("Failed to get DeclarationsSorage by key " + declarationsSorageKey)); // NOI18N
        }
        if (TraceFlags.USE_WEAK_MEMORY_CACHE && dc != null && weakDeclarationContainer != null) {
            weakDeclarationContainer = new WeakReference<DeclarationContainer>(dc);
        }
        return dc != null ? dc : DeclarationContainer.empty();
    }
    
    public Collection<CsmOffsetableDeclaration> getDeclarations() {
        DeclarationContainer declStorage = getDeclarationsSorage();
        // add all declarations
        Collection<CsmUID<CsmOffsetableDeclaration>> uids = declStorage.getDeclarationsUIDs();
        // add all unnamed declarations
        synchronized (unnamedDeclarations) {
            uids.addAll(unnamedDeclarations);
        }
        // convert to objects
        Collection<CsmOffsetableDeclaration> decls = UIDCsmConverter.UIDsToDeclarations(uids);
        return decls;
    }

    public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmFilter filter) {
        DeclarationContainer declStorage = getDeclarationsSorage();
        // add all declarations
        Collection<CsmUID<CsmOffsetableDeclaration>> uids = declStorage.getDeclarationsUIDs();
        // add all unnamed declarations
        synchronized (unnamedDeclarations) {
            uids.addAll(unnamedDeclarations);
        }
        return UIDCsmConverter.UIDsToDeclarations(uids, filter);
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> findUidsByPrefix(String prefix) {
        // To improve performance use char(255) instead real Character.MAX_VALUE
        char maxChar = 255; //Character.MAX_VALUE;
        return findUidsRange(prefix, prefix+maxChar);
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> findUidsRange(String from, String to) {
        DeclarationContainer declStorage = getDeclarationsSorage();
        return declStorage.getUIDsRange(from, to);
    }

    public Collection<CsmOffsetableDeclaration> getDeclarationsRange(CharSequence fqn, Kind[] kinds) {
        DeclarationContainer declStorage = getDeclarationsSorage();
        return declStorage.getDeclarationsRange(fqn, kinds);
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> getUnnamedUids() {
        // add all declarations
        Collection<CsmUID<CsmOffsetableDeclaration>> uids;
        // add all unnamed declarations
        synchronized (unnamedDeclarations) {
            uids = new ArrayList<CsmUID<CsmOffsetableDeclaration>>(unnamedDeclarations);
        }
        return uids;
    }

    public boolean isGlobal() {
        return global;
    }
    
    public CharSequence getQualifiedName() {
        return qualifiedName;
    }
    
    public CharSequence getName() {
        return name;
    }
    
    private void addNestedNamespace(NamespaceImpl nsp) {
        assert nsp != null;
        CsmUID<CsmNamespace> nestedNsUid = RepositoryUtils.put((CsmNamespace)nsp);
        assert nestedNsUid != null;
        nestedNamespaces.put(nsp.getQualifiedName(), nestedNsUid);
        RepositoryUtils.put(this);
    }
    
    private void removeNestedNamespace(NamespaceImpl nsp) {
        assert nsp != null;
        CsmUID<CsmNamespace> nestedNsUid = nestedNamespaces.remove(nsp.getQualifiedName());
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
        RepositoryUtils.put(this);
    }

    /**
     * Determines whether a variable has namespace or global scope
     *
     * @param v variable to check.
     * NB: should be file- or namespace- level,
     * don't pass a field, a parameter or a local var!
     *
     * @param isFileLevel true if it's defined on file level,
     * otherwise (if it's defined in namespace definition) false
     *
     * @return true if the variable has namesapce scope or global scope,
     * or false if it is file-local scope (i.e. no external linkage)
     */
    public static boolean isNamespaceScope(VariableImpl var, boolean isFileLevel) {
        if( ((FileImpl) var.getContainingFile()).isHeaderFile() && ! CsmKindUtilities.isVariableDefinition(var)) {
            return true;
        } else if( var.isStatic() ) {
	    return false;
	}
	else if( var.isConst() && isFileLevel ) {
	    if( ! var.isExtern() ) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Determines whether a function has namesace scope
     *
     * @param func function to check.
     *
     * @return true if the function has namesapce scope or global scope,
     * or false if it is file-local scope (i.e. no external linkage)
     */
    public static boolean isNamespaceScope(FunctionImpl func) {
        if( ((FileImpl) func.getContainingFile()).isHeaderFile() && ! func.isPureDefinition() ) {
            return true;
        } else if (func.isStatic()) {
            return false;
        }
        return true;
    }

    public CsmOffsetableDeclaration findExistingDeclaration(int start, int end, CharSequence name) {
        throw new UnsupportedOperationException();
    }

    public void addDeclaration(CsmOffsetableDeclaration declaration) {
        boolean unnamed = !ProjectBase.canRegisterDeclaration(declaration);
        // allow to register any enum
        if(unnamed && !CsmKindUtilities.isEnum(declaration) ) {
            return;
        }
        
        // TODO: remove this dirty hack!
        if( (declaration instanceof VariableImpl) ) {
            VariableImpl v = (VariableImpl) declaration;
            if( isNamespaceScope(v, isGlobal()) ) {
                v.setScope(this, true);
            } else {
                return;
            }
        }


        if (unnamed) {
            unnamedDeclarations.add(UIDCsmConverter.declarationToUID(declaration));
        } else {
            getDeclarationsSorage().putDeclaration(declaration);
        }
        
        // update repository
        RepositoryUtils.put(this);

        notify(declaration, NotifyEvent.DECLARATION_ADDED);
    }
    
    @SuppressWarnings("unchecked")
    public void removeDeclaration(CsmOffsetableDeclaration declaration) {
        CsmUID<CsmOffsetableDeclaration> declarationUid;
        if (declaration.getName().length() == 0) {
            declarationUid = UIDs.get(declaration);
            unnamedDeclarations.remove(declarationUid);
        } else {
            getDeclarationsSorage().removeDeclaration(declaration);
        }
        // do not clean repository, it must be done from physical container of declaration
        if (false) { RepositoryUtils.remove(declarationUid, declaration); }
        // update repository
        RepositoryUtils.put(this);
        notify(declaration, NotifyEvent.DECLARATION_REMOVED);
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
        boolean add = false;
        try {
            nsDefinitionsLock.writeLock().lock();
            add = nsDefinitions.isEmpty();
            nsDefinitions.put(getSortKey(def), definitionUid);
        } finally {
            nsDefinitionsLock.writeLock().unlock();
        }
        // update repository
        RepositoryUtils.put(this);
        if (add){
            addRemoveInParentNamespace(true);
        }
    }
    
    private synchronized void addRemoveInParentNamespace(boolean add){
        if (add){
            // add this namespace in the parent namespace
            NamespaceImpl parent = (NamespaceImpl) _getParentNamespace();
            if (parent != null) {
                parent.addNestedNamespace(this);
            }
            _getProject().registerNamespace(this);
        } else {
            // remove this namespace from the parent namespace
            try {
                nsDefinitionsLock.readLock().lock();
                if (!nsDefinitions.isEmpty()) {
                    // someone already registered in definitions
                    // do not unregister
                    return;
                }
            } finally {
                nsDefinitionsLock.readLock().unlock();
            }
            NamespaceImpl parent = (NamespaceImpl) _getParentNamespace();
            if (parent != null) {
                parent.removeNestedNamespace(this);
            }
            projectRef = _getProject();
            ((ProjectBase)projectRef).unregisterNamesace(this);
            dispose();            
        }
    }

    public void removeNamespaceDefinition(CsmNamespaceDefinition def) {
        assert !this.isGlobal();
        boolean remove = false;
        CsmUID<CsmNamespaceDefinition> definitionUid = null;
        try {
            nsDefinitionsLock.writeLock().lock();
            definitionUid = nsDefinitions.remove(getSortKey(def));
            remove =  nsDefinitions.isEmpty();
        } finally {
            nsDefinitionsLock.writeLock().unlock();
        }
        // does not remove unregistered declaration from repository, it's responsibility of physical container
        if (false) { RepositoryUtils.remove(definitionUid, def); }
        // update repository about itself
        RepositoryUtils.put(this);
        if (remove) {
            addRemoveInParentNamespace(false);
        }
    }
    
    @SuppressWarnings("unchecked")
    public Collection<CsmScopeElement> getScopeElements() {
        return (List) getDeclarations();
    }
    
    public CsmProject getProject() {
        return _getProject();
    }
    
    private CsmUID<CsmNamespace> uid = null;
    public final CsmUID<CsmNamespace> getUID() {
        CsmUID<CsmNamespace> out = uid;
        if (out == null) {
            synchronized (this) {
                if (uid == null) {
                    uid = out = createUID();
                }
            }
        }
        return uid;
    }   
    
    protected CsmUID<CsmNamespace> createUID() {
	return UIDUtilities.createNamespaceUID(this);
    }

    private ProjectBase _getProject() {
        Object o = projectRef;
        if (o instanceof ProjectBase) {
            return (ProjectBase) o;
        } else if (o instanceof Reference) {
            ProjectBase prj = (ProjectBase)((Reference) o).get();
            if (prj != null) {
                return prj;
            }
        }
        projectLock.readLock().lock();
        try {
            ProjectBase prj = null;
            if (projectRef instanceof ProjectBase) {
                prj = (ProjectBase)projectRef;
            } else if (projectRef instanceof Reference) {
                @SuppressWarnings("unchecked")
                Reference<ProjectBase> ref = (Reference<ProjectBase>) projectRef;
                prj = ref.get();
            }
            if (prj == null) {
                prj = (ProjectBase) UIDCsmConverter.UIDtoProject(this.projectUID);
                assert (prj != null || this.projectUID == null) : "empty project for UID " + this.projectUID;
                projectRef = new WeakReference<ProjectBase>(prj);
            }
            return prj;
        } finally {
            projectLock.readLock().unlock();
        }
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
        PersistentUtils.writeUTF(name, output);
        assert this.qualifiedName != null;
        PersistentUtils.writeUTF(qualifiedName, output);

        theFactory.writeStringToUIDMap(this.nestedNamespaces, output, true);
        ProjectComponent.writeKey(this.declarationsSorageKey, output);
        try {
            nsDefinitionsLock.readLock().lock();
            theFactory.writeNameSortedToUIDMap2(this.nsDefinitions, output, false);
        } finally {
            nsDefinitionsLock.readLock().unlock();
        }
        theFactory.writeUIDCollection(this.unnamedDeclarations, output, true);
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
       

        this.name = PersistentUtils.readUTF(input, NameCache.getManager());
        assert this.name != null;
        this.qualifiedName = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        assert this.qualifiedName != null;

        int collSize = input.readInt();
        if (collSize <= 0) {
            nestedNamespaces = new ConcurrentHashMap<CharSequence, CsmUID<CsmNamespace>>(0);
        } else {
            nestedNamespaces = new ConcurrentHashMap<CharSequence, CsmUID<CsmNamespace>>(collSize);
        }
        theFactory.readStringToUIDMap(this.nestedNamespaces, input, QualifiedNameCache.getManager(), collSize);
        declarationsSorageKey = ProjectComponent.readKey(input);
        assert declarationsSorageKey != null : "declarationsSorageKey can not be null";

        this.nsDefinitions = theFactory.readNameSortedToUIDMap2(input, null);

        collSize = input.readInt();
        if (collSize < 0) {
            unnamedDeclarations = Collections.synchronizedSet(new HashSet<CsmUID<CsmOffsetableDeclaration>>(0));
        } else {
            unnamedDeclarations = Collections.synchronizedSet(new HashSet<CsmUID<CsmOffsetableDeclaration>>(collSize));
        }
        theFactory.readUIDCollection(this.unnamedDeclarations, input, collSize);
    }

    private static FileNameSortedKey getSortKey(CsmNamespaceDefinition def) {
        return new FileNameSortedKey(def);
    }

    public static final Comparator<FileNameSortedKey> defenitionComparator = new Comparator<FileNameSortedKey>() {
        public int compare(FileNameSortedKey o1, FileNameSortedKey o2) {
            return o1.compareTo(o2);
        }
    };

    public static class FileNameSortedKey implements Comparable<FileNameSortedKey>, Persistent, SelfPersistent {
        private int start = 0;
        private CharSequence name;
        private FileNameSortedKey(CsmNamespaceDefinition def) {
            this(def.getContainingFile().getAbsolutePath(), def.getStartOffset());
        }
        private FileNameSortedKey(CharSequence name, int start) {
            this.start = start;
            this.name = NameCache.getManager().getString(name);
        }
        public int compareTo(FileNameSortedKey o) {
            int res = CharSequenceKey.Comparator.compare(name, o.name);
            if (res == 0) {
                res = start - o.start;
            }
            return res;
        }
        @Override public boolean equals(Object obj) {
            if (obj instanceof FileNameSortedKey) {
                FileNameSortedKey key = (FileNameSortedKey) obj;
                return compareTo(key)==0;
            }
            return false;
        }
        @Override public int hashCode() {
            int hash = 7;
            hash = 37 * hash + this.start;
            hash = 37 * hash + (this.name != null ? this.name.hashCode() : 0);
            return hash;
        }
        @Override public String toString() {
            return "FileNameSortedKey: " + this.name + "[" + this.start; // NOI18N
        }
        public static FileNameSortedKey getStartKey(CharSequence name) {
            return new FileNameSortedKey(name, 0);
        }
        public static FileNameSortedKey getEndKey(CharSequence name) {
            return new FileNameSortedKey(name, Integer.MAX_VALUE);
        }
        public void write(DataOutput output) throws IOException {
            output.writeInt(start);
            PersistentUtils.writeUTF(name, output);
        }
        public FileNameSortedKey(DataInput input) throws IOException {
            start = input.readInt();
            name = PersistentUtils.readUTF(input, FilePathCache.getManager());
        }
    }
}
