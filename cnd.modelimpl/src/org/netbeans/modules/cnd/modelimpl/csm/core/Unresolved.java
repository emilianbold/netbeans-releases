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

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;


/**
 * Container for all unresolved stuff in the project
 * @author Vladimir Kvasihn
 */
public class Unresolved {
    
    public class UnresolvedClass extends ClassEnumBase implements CsmClass {
        public UnresolvedClass(String name) {
            super(name, unresolvedNamespace, unresolvedFile, null, null);
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
            return true; // dummy code for dummy class
        }
        
        public CsmDeclaration.Kind getKind() {
            return CsmClass.Kind.CLASS;
        }
    }
    
    private class UnresolvedFile implements CsmFile {
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
            return _getProject();
        }
        public String getName() {
            return "<unresolved>"; // NOI18N
        }
        public List getIncludes() {
            return Collections.EMPTY_LIST;
        }
        public List getDeclarations() {
            return Collections.EMPTY_LIST;
        }
        public String getAbsolutePath() {
            return "<unresolved>"; // NOI18N
        }
        public boolean isValid() {
            return _getProject().isValid();
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
            if (unresolvedFileUID == null) {
                unresolvedFileUID = UIDUtilities.createFileUID(this);
            }
            return unresolvedFileUID;
        }
    };
    
    private static CsmUID unresolvedFileUID = null;
    private static CsmUID unresolvedClassUID = null;
    
    // only one of project/projectUID must be used 
    private ProjectBase project;
    private CsmUID projectUID;
    
    // doesn't need Repository Keys
    private CsmFile unresolvedFile;
    // doesn't need Repository Keys
    private NamespaceImpl unresolvedNamespace;
    // doesn't need Repository Keys
    private Map dummiesForUnresolved = new HashMap();
    
    public Unresolved(ProjectBase project) {
        _setProject(project);
        unresolvedFile = new UnresolvedFile();
        unresolvedNamespace = new NamespaceImpl(project, null, "unresolved") { // NOI18N
            protected void notifyCreation() {
                // do NOT register
            }
        };
    }
    
    private void _setProject(ProjectBase project) {
        if (TraceFlags.USE_REPOSITORY) {
            this.projectUID = UIDCsmConverter.ProjectToUID(project);
        } else {
            this.project = project;
        }
    }
    
    private ProjectBase _getProject() {
        ProjectBase prj = this.project;
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
            cls = new UnresolvedClass(name);
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
