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
import org.netbeans.modules.cnd.modelimpl.platform.*;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import java.io.File;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


/**
 * Container for all unresolved stuff in the project
 * @author Vladimir Kvasihn
 */
public class Unresolved {

    public class UnresolvedClass extends ClassEnumBase implements CsmClass {
        public UnresolvedClass(String name) {
            super(CsmClass.Kind.CLASS, name, unresolvedNamespace, unresolvedFile, null, null);
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
        
        public boolean isValid()
        {
            return true; // dummy code for dummy class
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
            return project;
        }
        public String getName() {
            return "<unresolved>";
        }
        public List getIncludes() {
            return Collections.EMPTY_LIST;
        }
        public List getDeclarations() {
            return Collections.EMPTY_LIST;
        }
        public String getAbsolutePath() {
            return "<unresolved>";
        }
        public boolean isValid() {
            return project.isValid();
        }
        public void scheduleParsing(boolean wait) {
        }
        public boolean isParsed() {
            return true;
        }
        public List getMacros() {
            return Collections.EMPTY_LIST;
        }
        
    };

    private ProjectBase project;
    private CsmFile unresolvedFile;
    private NamespaceImpl unresolvedNamespace;
    private Map dummiesForUnresolved = new HashMap();
    
    public Unresolved(ProjectBase project) {
        this.project = project;
        unresolvedFile = new UnresolvedFile();
        unresolvedNamespace = new NamespaceImpl(project, null, "unresolved") {
            protected void notifyCreation() {
                // do NOT register
            }
        };
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
                sb.append("::");
            }
            sb.append(nameTokens[i]);
        }
        return sb.toString();
    }
}
