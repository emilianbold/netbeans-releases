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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;

/**
 * Storage for project classifiers. Class was extracted from ProjectBase.
 * @author Alexander Simon
 */
/*package-local*/ class ClassifierContainer implements Persistent, SelfPersistent {

    private Map/*<String, ClassImpl>*/ classifiersOLD = new ConcurrentHashMap(/*<String, ClassImpl>*/);
    private Map<String, CsmUID<CsmClassifier>> classifiers = new ConcurrentHashMap<String, CsmUID<CsmClassifier>>();
    private Map<String, CsmUID<CsmClassifier>> typedefs = new ConcurrentHashMap<String, CsmUID<CsmClassifier>>();
    
    /** Creates a new instance of ClassifierContainer */
    public ClassifierContainer() {
    }

    public ClassifierContainer(DataInput input) throws IOException {
	read(input);
    }
    
    public CsmClassifier getClassifier(String qualifiedName) {
        CsmClassifier result;
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmClassifier> uid = classifiers.get(qualifiedName);
            if (uid == null) {
                uid = typedefs.get(qualifiedName);
            }
            result = UIDCsmConverter.UIDtoDeclaration(uid);
        } else {
            result = (CsmClassifier) classifiersOLD.get(qualifiedName);
        }
        return result;
    }
    
    public boolean putClassifier(CsmClassifier decl) {
        String qn = decl.getQualifiedName();
        if (TraceFlags.USE_REPOSITORY) {
            Map<String, CsmUID<CsmClassifier>> map;
            if (isTypedef(decl)) {
                map = typedefs;
            } else {
                map = classifiers;
            }
            if (!map.containsKey(qn)) {
                CsmUID<CsmClassifier> uid = UIDCsmConverter.declarationToUID(decl);
                assert uid != null;
                map.put(qn, uid);
                assert (UIDCsmConverter.UIDtoDeclaration(uid) != null);
                return true;
            }
        } else {
            if (!classifiersOLD.containsKey(qn)){
                classifiersOLD.put(qn, decl);
                return true;
            }
        }
        return false;
    }

    public void removeClassifier(CsmDeclaration decl) {
        if (TraceFlags.USE_REPOSITORY) {
            Map<String, CsmUID<CsmClassifier>> map;
            if (isTypedef(decl)) {
                map = typedefs;
            } else {
                map = classifiers;
            }
            CsmUID<CsmClassifier> uid = map.remove(decl.getQualifiedName());
            assert (uid == null) || (UIDCsmConverter.UIDtoCsmObject(uid) != null) : " no object for UID " + uid;
        } else {
            classifiersOLD.remove(decl.getQualifiedName());
        }
    }

    public void clearClassifiers() {
        if (TraceFlags.USE_REPOSITORY) {
            classifiers.clear();
            typedefs.clear();
        } else {
            classifiersOLD.clear();
        }
    }

    private boolean isTypedef(CsmDeclaration decl){
        return CsmKindUtilities.isTypedef(decl);
    }
    
    public void write(DataOutput output) throws IOException {
        UIDObjectFactory.getDefaultFactory().writeStringToUIDMap(this.classifiers, output, false);
        UIDObjectFactory.getDefaultFactory().writeStringToUIDMap(this.typedefs, output, false);
    }
    
    private void read(DataInput input) throws IOException {
        UIDObjectFactory.getDefaultFactory().readStringToUIDMap(this.classifiers, input, QualifiedNameCache.getManager());
        UIDObjectFactory.getDefaultFactory().readStringToUIDMap(this.typedefs, input, QualifiedNameCache.getManager());
    }
}
