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
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;

/**
 * utilities to convert between CsmUID and CsmObjects
 * @author Vladimir Voskresensky
 */
public class UIDCsmConverter {
   
    private UIDCsmConverter() {
    }
    
    public static CsmFile UIDtoFile(CsmUID uid) {
        return uid == null ? null : (CsmFile)uid.getObject();
    }
    
    public static CsmObject UIDtoCsmObject(CsmUID uid) {
        return uid == null ? null : (CsmObject)uid.getObject();
    }    

    public static CsmNamespace UIDtoNamespace(CsmUID uid) {
        return uid == null ? null : (CsmNamespace)uid.getObject();
    }    

    public static CsmProject UIDtoProject(CsmUID uid) {
        return uid == null ? null : (CsmProject)uid.getObject();
    }   
    
    public static CsmUID FileToUID(CsmFile file) {
        return file == null ? null : ((CsmFile)file).getUID();
    }
    
    public static CsmUID NamespaceToUID(CsmNamespace ns) {
        return ns == null ? null : ((CsmNamespace)ns).getUID();
    }    

    public static CsmUID ProjectToUID(CsmProject project) {
        return project == null ? null : ((CsmProject)project).getUID();
    }    
}
