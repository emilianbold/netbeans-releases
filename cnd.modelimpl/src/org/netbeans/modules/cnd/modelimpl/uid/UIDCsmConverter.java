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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmIdentifiable;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmUID;

/**
 * utilities to convert between CsmUID and CsmObjects
 * @author Vladimir Voskresensky
 */
public class UIDCsmConverter {
   
    private UIDCsmConverter() {
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // UID -> Object
    
    public static CsmFile UIDtoFile(CsmUID<CsmFile> uid) {
        return uid == null ? null : uid.getObject();
    }
    
    public static CsmObject UIDtoCsmObject(CsmUID uid) {
        return uid == null ? null : (CsmObject)uid.getObject();
    }    

    public static CsmNamespace UIDtoNamespace(CsmUID<CsmNamespace> uid) {
        return uid == null ? null : uid.getObject();
    }    

    public static CsmProject UIDtoProject(CsmUID<CsmProject> uid) {
        return uid == null ? null : uid.getObject();
    }

    public static <T extends CsmDeclaration> T UIDtoDeclaration(CsmUID<T> uid) {
        return uid == null ? null : uid.getObject();
    } 
    
    public static CsmScope UIDToScope(CsmUID<CsmScope> uid) {
        return uid == null ? null : uid.getObject();
    }
    
    public static <T extends CsmNamespace> List<T> UIDsToNamespaces(Collection<CsmUID<T>> uids) {
        List<T> out = UIDsToList(uids);
        return out;
    }
    
    public static <T extends CsmDeclaration> List<T> UIDsToDeclarations(Collection<CsmUID<T>> uids) {
        List<T> out = UIDsToList(uids);
        return out;
    }
    
    public static <T extends CsmMacro> List<T> UIDsToMacros(Collection<CsmUID<T>> uids) {
        List<T> out = UIDsToList(uids);
        return out;
    }
    
    public static <T extends CsmInclude> List<T> UIDsToIncludes(Collection<CsmUID<T>> uids) {
        List<T> out = UIDsToList(uids);
        return out;
    }
    
    private static <T extends CsmIdentifiable> List<T> UIDsToList(Collection<CsmUID<T>> uids) {
        List<T> out = new ArrayList<T>(uids.size());
        for (CsmUID<T> uid : uids) {
            assert uid != null;
            T decl = UIDCsmConverter.UIDToIdentifiable(uid);
            assert decl != null;
            out.add(decl);
        }
        return out;
    }
    
    public static <T extends CsmIdentifiable> T UIDToIdentifiable(CsmUID<T> uid) {
        return uid == null ? null : uid.getObject();
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Object -> UID
    
    public static CsmUID<CsmFile> fileToUID(CsmFile file) {
        return file == null ? null : file.getUID();
    }
    
    public static CsmUID<CsmNamespace> namespaceToUID(CsmNamespace ns) {
        return ns == null ? null : ns.getUID();
    }    

    public static CsmUID<CsmProject> projectToUID(CsmProject project) {
        return project == null ? null : project.getUID();
    }  

    public static <T extends CsmDeclaration> CsmUID<T> declarationToUID(T decl) {
        return decl == null ? null : decl.getUID();
    }

    public static CsmUID<CsmScope> scopeToUID(CsmScope scope) {
        return scope == null ? null : ((CsmIdentifiable)scope).getUID();
    }
    
    public static <T extends CsmIdentifiable> CsmUID<T> identifiableToUID(CsmIdentifiable<T> obj) {
        return obj == null ? null : obj.getUID();
    }
}
