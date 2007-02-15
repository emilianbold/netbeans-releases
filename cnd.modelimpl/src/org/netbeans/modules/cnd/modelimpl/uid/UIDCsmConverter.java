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
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmIdentifiable;
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
    
//    public static <T extends CsmClassifier> T UIDtoClassifier(CsmUID<T> uid) {
//        return uid == null ? null : uid.getObject();
//    }       

    public static <T extends CsmDeclaration> T UIDtoDeclaration(CsmUID<T> uid) {
        return uid == null ? null : uid.getObject();
    } 
    
    
    public static <T extends CsmDeclaration> List<T> UIDsToDeclarations(Collection<CsmUID<T>> uids) {
        List<T> out = new ArrayList<T>(uids.size());
        for (CsmUID<T> uid : uids) {
            assert uid != null;
            T decl = UIDCsmConverter.UIDtoDeclaration(uid);
            assert decl != null;
            out.add(decl);
        }
        return out;
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
    
//    public static <T extends CsmClassifier> CsmUID<T> classifierToUID(T classifier) {
//        return classifier == null ? null : classifier.getUID();
//    }

    public static <T extends CsmDeclaration> CsmUID<T> declarationToUID(T decl) {
        return decl == null ? null : decl.getUID();
    }
    
    private static <T extends CsmIdentifiable> CsmUID<T> identifiableToUID(CsmIdentifiable<T> obj) {
        return obj == null ? null : obj.getUID();
    }
        
    ////////////////////////////////////////////////////////////////////////////
    // accessor <--> Object

    public static CsmObjectAccessor objectToAccessor(CsmObject obj) {
        CsmObjectAccessor accessor = null;
        if (obj != null) {
            if (isCsmObjectSupportUID(obj)) {
                accessor = new UIDAccessorImpl((CsmIdentifiable)obj);
            } else {
                accessor = new ObjectAccessorImpl(obj);
            }                
        }
        return accessor;
    }    
    
    public static CsmObject accessorToObject(CsmObjectAccessor accessor) {
        return accessor == null ? null : accessor.getObject();
    }
    
    public static CsmObjectAccessor scopeToAccessor(CsmScope scope) {
        return objectToAccessor(scope);
    }
    
    public static CsmScope accessorToScope(CsmObjectAccessor accessor) {
        try {
            return accessor == null ? null : (CsmScope) accessor.getObject();
        } catch (ClassCastException ex) {
            return null;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl details
    
    private static final class UIDAccessorImpl implements CsmObjectAccessor {
        private CsmUID objUID;
        public UIDAccessorImpl(CsmIdentifiable obj) {
            _setObject(obj);
        }
        
        public void _setObject(CsmIdentifiable obj) {
            objUID = UIDCsmConverter.identifiableToUID(obj);
        }
        
        public CsmObject _getObject() {
            return UIDCsmConverter.UIDtoCsmObject(objUID);
        }
        
        public CsmObject getObject() {
            return _getObject();
        }
        
        public String toString() {
            String retValue;
            
            retValue = "UID-based Accessor for " + objUID; // NOI18N
            return retValue;
        }        
    }
    
    private static final class ObjectAccessorImpl implements CsmObjectAccessor {
        private final CsmObject obj;
        public ObjectAccessorImpl(CsmObject obj) {
            this.obj = obj;
        }
        
        public CsmObject getObject() {
            return obj;
        }

        public String toString() {
            String retValue;
            
            retValue = "Object-based Accessor for " + obj; // NOI18N
            return retValue;
        }
        
        
    }
    
    private static boolean isCsmObjectSupportUID(CsmObject obj) {
        if (obj != null && obj instanceof CsmIdentifiable) {
            // now we support only namespace and file
            // + project
            // + classifiers
            if (obj instanceof CsmNamespace || obj instanceof CsmFile || obj instanceof CsmProject || obj instanceof CsmClassifier) {
                return true;
            }
        }
        return false;
    }    
}
