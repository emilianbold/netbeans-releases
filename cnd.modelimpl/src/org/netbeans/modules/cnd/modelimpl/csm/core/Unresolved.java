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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;


/**
 * Container for all unresolved stuff in the project
 * @author Vladimir Kvasihn
 */
public class Unresolved {
    
    public static class UnresolvedClass extends ClassEnumBase<CsmClass> implements CsmClass {
        public UnresolvedClass(String name, NamespaceImpl namespace, CsmFile file) {
            super(name, namespace, file, null, null);
            register();
        }
        public boolean isTemplate() {
            return false;
        }
        public List getScopeElements() {
            return Collections.EMPTY_LIST;
        }
        
        public List getMembers() {
            return Collections.EMPTY_LIST;
        }
        
        public int getLeftBracketOffset() {
            return 0;
        }
        
        public CsmClass getContainingClass() {
            return null;
        }
        
        public List getBaseClasses() {
            return Collections.EMPTY_LIST;
        }
        
        public boolean isValid() {
            return false; // false for dummy class, to allow reconstruct in usage place
        }
        
        public CsmDeclaration.Kind getKind() {
            return CsmClass.Kind.CLASS;
        }
        
        ////////////////////////////////////////////////////////////////////////////
        // impl of SelfPersistent
        
        public void write(DataOutput output) throws IOException {
            super.write(output);
        }
        
        public UnresolvedClass(DataInput input) throws IOException {
            super(input);
        }
    }
    
    private static class UnresolvedNamespace extends NamespaceImpl {
        private UnresolvedNamespace(ProjectBase project) {
            super(project, null, "$unresolved$","$unresolved$");
        }

        protected void notifyCreation() {
            // skip registration
        }
    }
    
    public static class UnresolvedFile implements CsmFile, Persistent, SelfPersistent  {
        // only one of project/projectUID must be used 
        private final ProjectBase project;
        private final CsmUID<CsmProject> projectUID;
    
        private UnresolvedFile(ProjectBase project) {
            if (TraceFlags.USE_REPOSITORY) {
                this.projectUID = UIDCsmConverter.projectToUID(project);
                this.project = null;
            } else {
                this.project = project;
                this.projectUID = null;
            }            
        }
        public String getText(int start, int end) {
            return "";
        }
        public String getText() {
            return "";
        }
        public List getScopeElements() {
            return Collections.EMPTY_LIST;
        }
        public CsmProject getProject() {
            return _getProject(projectUID, project);
        }
        public String getName() {
            return "$unresolved file$"; // NOI18N
        }
        public List getIncludes() {
            return Collections.EMPTY_LIST;
        }
        public List getDeclarations() {
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
        public List getMacros() {
            return Collections.EMPTY_LIST;
        }
        
        public CsmUID getUID() {
            if (uid == null) {
                uid = UIDUtilities.createFileUID(this);
            }
            return uid;
        }
        private CsmUID<CsmFile> uid = null;
        
        ////////////////////////////////////////////////////////////////////////////
        // impl of SelfPersistent

        public void write(DataOutput output) throws IOException {
            UIDObjectFactory.getDefaultFactory().writeUID(this.projectUID, output);
        }  

        public UnresolvedFile(DataInput input) throws IOException {
            this.projectUID = UIDObjectFactory.getDefaultFactory().readUID(input);
            assert this.projectUID != null;
            
            assert TraceFlags.USE_REPOSITORY;
            this.project = null;
        }          
    };
    
    // only one of project/projectUID must be used 
    private final ProjectBase project;
    private final CsmUID<CsmProject> projectUID;
    
    // doesn't need Repository Keys
    private CsmFile unresolvedFile;
    // doesn't need Repository Keys
    private NamespaceImpl unresolvedNamespace;
    // doesn't need Repository Keys
    private Map dummiesForUnresolved = new HashMap();
    
    public Unresolved(ProjectBase project) {
        if (TraceFlags.USE_REPOSITORY) {
            this.projectUID = UIDCsmConverter.projectToUID(project);
            this.project = null;
        } else {
            this.project = project;
            this.projectUID = null;
        }
        unresolvedFile = new UnresolvedFile(project);
        unresolvedNamespace = new UnresolvedNamespace(project);
        
        if (TraceFlags.USE_REPOSITORY) {
            // TODO: hang or put unresolvedFile?
            RepositoryUtils.put(unresolvedFile);
            assert (UIDCsmConverter.fileToUID(unresolvedFile) != null);
            assert (UIDCsmConverter.UIDtoFile(UIDCsmConverter.fileToUID(unresolvedFile)) != null);
            assert (UIDCsmConverter.namespaceToUID(unresolvedNamespace) != null);
            assert (UIDCsmConverter.UIDtoNamespace(UIDCsmConverter.namespaceToUID(unresolvedNamespace)) != null);
        }
    }
    
    private ProjectBase _getProject() {       
        return _getProject(this.projectUID, this.project);
    }
    
    private static ProjectBase _getProject(CsmUID<CsmProject> projectUID, ProjectBase project) {
        ProjectBase prj = project;
        if (TraceFlags.USE_REPOSITORY) {
            assert projectUID != null;
            prj = (ProjectBase)UIDCsmConverter.UIDtoProject(projectUID);
        }        
        return prj;
    }
    
    public CsmClass getDummyForUnresolved(String[] nameTokens) {
        String name = getName(nameTokens);
        CsmClass cls = (CsmClass) dummiesForUnresolved.get(name);
        if( cls == null ) {
            cls = new UnresolvedClass(name, unresolvedNamespace, unresolvedFile);
            dummiesForUnresolved.put(name, cls);
        }
        return cls;
    }
    
    private String getName(String[] nameTokens) {
        StringBuffer sb = new StringBuffer();
        for( int i = 0; i < nameTokens.length; i++ ) {
            if( i > 0 ) {
                sb.append("::"); // NOI18N
            }
            sb.append(nameTokens[i]);
        }
        return sb.toString();
    }
}
