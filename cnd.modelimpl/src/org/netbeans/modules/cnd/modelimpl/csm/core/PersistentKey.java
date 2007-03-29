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

import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmIdentifiable;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.*;

/**
 *
 * @author Alexander Simon
 */
public final class PersistentKey {
    private static final boolean USE_REPOSITORY = Boolean.getBoolean("cnd.modelimpl.use.repository"); // NOI18N
    private static final byte PROXY = 0;
    private static final byte UID = 1;
    private static final byte DECLARATION = 2;
    
    private Object key;
    private CsmProject project;
    private byte kind;
    
    private PersistentKey(CsmIdentifiable id) {
        key = id;
        kind = PROXY;
    }
    
    private PersistentKey(CsmUID id) {
        key = id;
        kind = UID;
    }
    
    private PersistentKey(String id, CsmProject host,  byte type) {
        key = id;
        project = host;
        kind = type;
    }
    
    public static PersistentKey createKey(CsmOffsetableDeclaration decl){
        String name = decl.getName();
        String uniq = decl.getUniqueName();
        CsmProject project = decl.getContainingFile().getProject();
        if (name.length() > 0 && uniq.indexOf("::::") < 0 && project != null){ // NOI18N
            return new PersistentKey(uniq, project, DECLARATION);
        } else {
            //System.out.println("Skip "+uniq);
        }
        if (USE_REPOSITORY){
            return new PersistentKey(decl.getUID());
        } else {
            return new PersistentKey(decl);
        }
    }
    
    public CsmIdentifiable getObject(){
        switch(kind){
            case UID:
                return (CsmIdentifiable) ((CsmUID)key).getObject();
            case PROXY:
                return (CsmIdentifiable) key;
            case DECLARATION:
                return project.findDeclaration((String)key);
        }
        return null;
    }
    
    public boolean equals(Object object) {
        if (object instanceof PersistentKey){
            PersistentKey what = (PersistentKey) object;
            if (kind != what.kind) {
                return false;
            }
            switch(kind){
                case PROXY:
                case UID:
                    return key.equals(what.key);
                case DECLARATION:
                    return project == what.project && key.equals(what.key);
            }
        }
        return super.equals(object);
    }
    
    public int hashCode() {
        switch(kind){
            case PROXY:
            case UID:
                return key.hashCode();
            case DECLARATION:
                return project.hashCode() ^ key.hashCode();
        }
        return super.hashCode();
    }
}
