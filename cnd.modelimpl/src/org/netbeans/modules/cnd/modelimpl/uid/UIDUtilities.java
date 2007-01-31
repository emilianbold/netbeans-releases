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

package org.netbeans.modules.cnd.modelimpl.uid;

import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities;

/**
 * utilities to create CsmUID for CsmObjects
 * @author Vladimir Voskresensky
 */
public class UIDUtilities {
    
    /** Creates a new instance of UIDUtilities */
    private UIDUtilities() {
    }
 
    public static CsmUID createProjectUID(ProjectBase prj) {
        return new ProjectUID(prj);
    } 
    
    public static CsmUID createFileUID(CsmFile file) {
        return new FileUID(file);
    } 

    public static CsmUID createNamespaceUID(CsmNamespace ns) {
        return new NamespaceUID(ns);
    }
    
    //////////////////////////////////////////////////////////////////////////
    // impl details
    
    /**
     * UID for CsmProject
     */
    private static final class ProjectUID extends KeyBasedUID {
        public ProjectUID(ProjectBase project) {
            super(KeyUtilities.createProjectKey(project));
        }
    }    
  
    /**
     * UID for CsmNamespace
     */
    private static final class NamespaceUID extends KeyBasedUID {
        public NamespaceUID(CsmNamespace ns) {
            super(KeyUtilities.createNamespaceKey(ns));
        }
    }    
    
    /**
     * UID for CsmFile
     */
    private static final class FileUID extends KeyBasedUID {
        public FileUID(CsmFile file) {
            super(KeyUtilities.createFileKey(file));
        }
    }
}
