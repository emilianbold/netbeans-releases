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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.DataOutput;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;


/**
 * Container for all unresolved stuff in the project
 * 
 * @author Vladimir Kvasihn
 */
public final class Unresolved implements Disposable {
    
    private static class IllegalCallException extends RuntimeException {
	IllegalCallException() {
	    super("This method should never be called for Unresolved"); // NOI18N
	}
    }
    
    public final class UnresolvedClass extends ClassEnumBase<CsmClass> implements CsmClass {
	
        public UnresolvedClass(String name) {
            super(name, unresolvedFile, null);
	    init(unresolvedNamespace, null);
        }
	
	public void register() {
	    // we don't need registering in project here.
	    // so we just register in namespace and in repository
            if (unresolvedNamespace != null) {
                unresolvedNamespace.addDeclaration(this);
            }
	}
	
        public boolean isTemplate() {
            return false;
        }
        public List<CsmScopeElement> getScopeElements() {
            return Collections.<CsmScopeElement>emptyList();
        }
        
        public List<CsmMember> getMembers() {
            return Collections.<CsmMember>emptyList();
        }

        public List<CsmFriend> getFriends() {
            return Collections.<CsmFriend>emptyList();
        }
        
        public int getLeftBracketOffset() {
            return 0;
        }
        
        public List<CsmInheritance> getBaseClasses() {
            return Collections.<CsmInheritance>emptyList();
        }
        
        public boolean isValid() {
            return false; // false for dummy class, to allow reconstruct in usage place
        }
        
        public CsmDeclaration.Kind getKind() {
            return CsmClass.Kind.CLASS;
        }

	@Override
	protected CsmUID createUID() {
	    return UIDUtilities.createUnresolvedClassUID(getName(), getProject());
	}
	
        ////////////////////////////////////////////////////////////////////////////
        // impl of SelfPersistent
        public void write(DataOutput output) throws IOException {
            throw new IllegalCallException();
        }        
    }
    
    private final static class UnresolvedNamespace extends NamespaceImpl {
	
        private UnresolvedNamespace(ProjectBase project) {
            super(project, null, "$unresolved$","$unresolved$"); // NOI18N
        }

        protected void notifyCreation() {
            // skip registration
        }

	@Override
	protected CsmUID<CsmNamespace> createUID() {
	    return UIDUtilities.createUnresolvedNamespaceUID(getProject());
	}

	
	@Override
	public void write(DataOutput output) throws IOException {
	    throw new IllegalCallException();
	}
    }
    
    public final static class UnresolvedFile implements CsmFile, Disposable  {
	
        // only one of projectRef/projectUID must be used (based on USE_REPOSITORY/USE_UID_TO_CONTAINER)
        private /*final*/ ProjectBase projectRef;// can be set in onDispose or contstructor only
        private final CsmUID<CsmProject> projectUID;
    
        private UnresolvedFile(ProjectBase project) {
            if (TraceFlags.USE_REPOSITORY && TraceFlags.UID_CONTAINER_MARKER) {
                this.projectUID = UIDCsmConverter.projectToUID(project);
                this.projectRef = null;
            } else {
                this.projectRef = project;
                this.projectUID = null;
            }            
        }
        
        public void dispose() {
            onDispose();
        }

        private void onDispose() {
            if (TraceFlags.RESTORE_CONTAINER_FROM_UID) {
                // restore container from it's UID
                this.projectRef = (ProjectBase)UIDCsmConverter.UIDtoProject(this.projectUID);
                assert (this.projectRef != null || this.projectUID == null) : "empty project for UID " + this.projectUID;
            }
        } 
        
        public String getText(int start, int end) {
            return "";
        }
        public String getText() {
            return "";
        }
        public List<CsmScopeElement> getScopeElements() {
            return Collections.<CsmScopeElement>emptyList();
        }
        public CsmProject getProject() {
            return _getProject(projectUID, projectRef);
        }
        public String getName() {
            return "$unresolved file$"; // NOI18N
        }
        public List<CsmInclude> getIncludes() {
            return Collections.EMPTY_LIST;
        }
        public List<CsmOffsetableDeclaration> getDeclarations() {
            return Collections.EMPTY_LIST;
        }
        public String getAbsolutePath() {
            return "$unresolved file$"; // NOI18N
        }
        public boolean isValid() {
            return getProject().isValid();
        }
        public void scheduleParsing(boolean wait) {
        }
        public boolean isParsed() {
            return true;
        }
        public List<CsmMacro> getMacros() {
            return Collections.EMPTY_LIST;
        }
        
        public CsmUID getUID() {
            if (uid == null) {
                uid = UIDUtilities.createUnresolvedFileUID(this.getProject());
            }
            return uid;
        }
	
        private CsmUID<CsmFile> uid = null;
    };
    
    // only one of projectRef/projectUID must be used (based on USE_REPOSITORY/USE_UID_TO_CONTAINER)
    private /*final*/ ProjectBase projectRef;// can be set in onDispose or contstructor only
    private final CsmUID<CsmProject> projectUID;
    
    // doesn't need Repository Keys
    private final CsmFile unresolvedFile;
    // doesn't need Repository Keys
    private final NamespaceImpl unresolvedNamespace;
    // doesn't need Repository Keys
    private Map<String, Reference<UnresolvedClass>> dummiesForUnresolved = new HashMap<String, Reference<UnresolvedClass>>();
    
    public Unresolved(ProjectBase project) {
        if (TraceFlags.USE_REPOSITORY && TraceFlags.UID_CONTAINER_MARKER) {
            this.projectUID = UIDCsmConverter.projectToUID(project);
            this.projectRef = null;
        } else {
            this.projectRef = project;
            this.projectUID = null;
        }
        unresolvedFile = new UnresolvedFile(project);
        unresolvedNamespace = new UnresolvedNamespace(project);
    }
    
    public void dispose() {
        onDispose();
    }
    
    private void onDispose() {
        if (TraceFlags.RESTORE_CONTAINER_FROM_UID) {
            // restore container from it's UID
            this.projectRef = (ProjectBase)UIDCsmConverter.UIDtoProject(this.projectUID);
            assert (this.projectRef != null || this.projectUID == null) : "empty project for UID " + this.projectUID;
        }
    }    
    
    private ProjectBase _getProject() {       
        return _getProject(this.projectUID, this.projectRef);
    }
    
    private static ProjectBase _getProject(CsmUID<CsmProject> projectUID, ProjectBase project) {
        ProjectBase prj = project;
        if (prj == null) {
            if (TraceFlags.USE_REPOSITORY) {
                assert projectUID != null;
                prj = (ProjectBase)UIDCsmConverter.UIDtoProject(projectUID);
            }        
        }
        return prj;
    }
    
    public CsmClass getDummyForUnresolved(String[] nameTokens) {
	return getDummyForUnresolved(getName(nameTokens));
    }
    
    public CsmClass getDummyForUnresolved(String name) {
        Reference<UnresolvedClass> ref = dummiesForUnresolved.get(name);
        UnresolvedClass cls = ref == null ? null : ref.get();
        if( cls == null ) {
            cls = new UnresolvedClass(name);
            dummiesForUnresolved.put(name, new SoftReference(cls));
	    cls.register();
        }
        return cls;
    }
    
    public CsmNamespace getUnresolvedNamespace() {
	return unresolvedNamespace;
    }
    
    public CsmFile getUnresolvedFile() {
	return unresolvedFile;
    }
    
    private String getName(String[] nameTokens) {
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < nameTokens.length; i++ ) {
            if( i > 0 ) {
                sb.append("::"); // NOI18N
            }
            sb.append(nameTokens[i]);
        }
        return sb.toString();
    }
}
