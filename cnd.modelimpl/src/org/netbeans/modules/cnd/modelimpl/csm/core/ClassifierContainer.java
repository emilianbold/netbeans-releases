/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.TypeImpl;
import org.netbeans.modules.cnd.modelimpl.repository.ClassifierContainerKey;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.CharSequences;

/**
 * Storage for project classifiers. Class was extracted from ProjectBase.
 * @author Alexander Simon
 */
/*package-local*/ class ClassifierContainer extends ProjectComponent implements Persistent, SelfPersistent {

    private final Map<CharSequence, CsmUID<CsmClassifier>> classifiers;
    private final Map<CharSequence, CsmUID<CsmClassifier>> typedefs;
    private final Map<CharSequence, Set<CsmUID<CsmInheritance>>> inheritances;
    private final ReadWriteLock declarationsLock = new ReentrantReadWriteLock();

    // empty stub
    private static final ClassifierContainer EMPTY = new ClassifierContainer() {
        @Override
        public boolean putClassifier(CsmClassifier decl) {
            return false;
        }

        @Override
        public void put() {
        }
    };

    public static ClassifierContainer empty() {
        return EMPTY;
    }
    
    /** Creates a new instance of ClassifierContainer */
    public ClassifierContainer(ProjectBase project) {
        super(new ClassifierContainerKey(project.getUniqueName().toString()), false);
        classifiers = new HashMap<CharSequence, CsmUID<CsmClassifier>>();
        typedefs = new HashMap<CharSequence, CsmUID<CsmClassifier>>();
        inheritances = new HashMap<CharSequence, Set<CsmUID<CsmInheritance>>>();
        put();
    }

    public ClassifierContainer(DataInput input) throws IOException {
        super(input);
        int collSize = input.readInt();
        classifiers = new HashMap<CharSequence, CsmUID<CsmClassifier>>(collSize);
        UIDObjectFactory.getDefaultFactory().readStringToUIDMap(this.classifiers, input, QualifiedNameCache.getManager(), collSize);
        collSize = input.readInt();
        typedefs = new HashMap<CharSequence, CsmUID<CsmClassifier>>(collSize);
        UIDObjectFactory.getDefaultFactory().readStringToUIDMap(this.typedefs, input, QualifiedNameCache.getManager(), collSize);
        collSize = input.readInt();
        inheritances = new HashMap<CharSequence, Set<CsmUID<CsmInheritance>>>();
        UIDObjectFactory.getDefaultFactory().readStringToUIDMapSet(this.inheritances, input, NameCache.getManager(), collSize);
    }

    // only for EMPTY static field
    private ClassifierContainer() {
        super((org.netbeans.modules.cnd.repository.spi.Key) null, false);
        classifiers = new HashMap<CharSequence, CsmUID<CsmClassifier>>();
        typedefs = new HashMap<CharSequence, CsmUID<CsmClassifier>>();
        inheritances = new HashMap<CharSequence, Set<CsmUID<CsmInheritance>>>();
    }
    
    public CsmClassifier getClassifier(CharSequence qualifiedName) {
        CsmClassifier result;
        CsmUID<CsmClassifier> uid;
        qualifiedName = CharSequences.create(qualifiedName);
        try {
            declarationsLock.readLock().lock();
            uid = classifiers.get(qualifiedName);
            if (uid == null) {
                uid = typedefs.get(qualifiedName);
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        result = UIDCsmConverter.UIDtoDeclaration(uid);
        return result;
    }

    public Collection<CsmInheritance> getInheritances(CharSequence name){
        Collection<CsmUID<CsmInheritance>> inh;
        name = CharSequences.create(name);
        try {
            declarationsLock.readLock().lock();
            inh = inheritances.get(name);
        } finally {
            declarationsLock.readLock().unlock();
        }
        if (inh != null) {
            return UIDCsmConverter.<CsmInheritance>UIDsToInheritances(inh);
        }
        return Collections.<CsmInheritance>emptyList();
    }


    // for unit teast
    Map<CharSequence, CsmClassifier> getClassifiers(){
        Map<CharSequence, CsmClassifier> res = new TreeMap<CharSequence, CsmClassifier>();
        try {
            declarationsLock.readLock().lock();
            for(Map.Entry<CharSequence, CsmUID<CsmClassifier>> entry : classifiers.entrySet()) {
                res.put(entry.getKey(), UIDCsmConverter.UIDtoDeclaration(entry.getValue()));
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        return res;
    }

    // for unit teast
    Map<CharSequence, CsmClassifier> getTypedefs(){
        Map<CharSequence, CsmClassifier> res = new TreeMap<CharSequence, CsmClassifier>();
        try {
            declarationsLock.readLock().lock();
            for(Map.Entry<CharSequence, CsmUID<CsmClassifier>> entry : typedefs.entrySet()) {
                res.put(entry.getKey(), UIDCsmConverter.UIDtoDeclaration(entry.getValue()));
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        return res;
    }

    public boolean putClassifier(CsmClassifier decl) {
        boolean put = false;
        CsmUID<CsmClassifier> uid = UIDCsmConverter.declarationToUID(decl);
        Map<CharSequence, CsmUID<CsmClassifier>> map;
        if (isTypedef(decl)) {
            map = typedefs;
        } else {
            map = classifiers;
            if (CsmKindUtilities.isClass(decl)) {
                CsmClass cls = (CsmClass) decl;
                Collection<CsmInheritance> base = cls.getBaseClasses();
                if (!base.isEmpty()) {
                    try {
                        declarationsLock.writeLock().lock();
                        for(CsmInheritance inh : base) {
                            CharSequence id = inheritanceName(inh);
                            Set<CsmUID<CsmInheritance>> set = inheritances.get(id);
                            if (set == null) {
                                set = new HashSet<CsmUID<CsmInheritance>>();
                                inheritances.put(id, set);
                            }
                            set.add(UIDCsmConverter.inheritanceToUID(inh));
                        }
                    } finally {
                        declarationsLock.writeLock().unlock();
                    }
                }
            }
        }
        CharSequence qn = decl.getQualifiedName();
        put = putClassifier(map, qn, uid);
        if (CsmKindUtilities.isClass(decl) && !CsmKindUtilities.isTemplate(decl)) {
            // Special case for nested structs in C
            // See Bug 144535 - wrong error highlighting for inner structure
            CharSequence qn2 = getQualifiedNameWithoutScopeClasses(decl);
            if (qn.length() != qn2.length()) {
                putClassifier(map, qn2, uid);
            }
        }
        return put;
    }

    private CharSequence inheritanceName(CsmInheritance inh) {
        CharSequence id;
        if (inh instanceof TypeImpl) {
            id = ((TypeImpl) inh.getAncestorType()).getOwnText();
        } else {
            id = inh.getAncestorType().getClassifierText();
        }
        int i = CharSequenceUtils.lastIndexOf(id, "::"); //NOI18N
        if (i >= 0) {
            id = id.subSequence(i+2, id.length());
        }
        return NameCache.getManager().getString(id);
    }

    private boolean putClassifier(Map<CharSequence, CsmUID<CsmClassifier>> map, CharSequence qn, CsmUID<CsmClassifier> uid) {
        boolean put = false;
        try {
            declarationsLock.writeLock().lock();
            if (!map.containsKey(qn)) {
                assert uid != null;
                map.put(qn, uid);
                assert (UIDCsmConverter.UIDtoDeclaration(uid) != null);
                put = true;
            }
        } finally {
            declarationsLock.writeLock().unlock();
        }
        if (put) {
            put();
        }
        return put;
    }

    public void removeClassifier(CsmDeclaration decl) {
        Map<CharSequence, CsmUID<CsmClassifier>> map;
        if (isTypedef(decl)) {
            map = typedefs;
        } else {
            map = classifiers;
            if (CsmKindUtilities.isClass(decl)) {
                CsmClass cls = (CsmClass) decl;
                Collection<CsmInheritance> base = cls.getBaseClasses();
                if (!base.isEmpty()) {
                    try {
                        declarationsLock.writeLock().lock();
                        for(CsmInheritance inh : base) {
                            CharSequence id = inheritanceName(inh);
                            Set<CsmUID<CsmInheritance>> set = inheritances.get(id);
                            if (set != null) {
                                set.remove(UIDCsmConverter.inheritanceToUID(inh));
                            }
                        }
                    } finally {
                        declarationsLock.writeLock().unlock();
                    }
                }
            }
        }
        CharSequence qn = decl.getQualifiedName();
        removeClassifier(map, qn);
        if (CsmKindUtilities.isClass(decl) && !CsmKindUtilities.isTemplate(decl)) {
            // Special case for nested structs in C
            // See Bug 144535 - wrong error highlighting for inner structure
            CharSequence qn2 = getQualifiedNameWithoutScopeClasses((CsmClass) decl);
            if (qn.length() != qn2.length()) {
                removeClassifier(map, qn2);
            }
        }
    }

    private void removeClassifier(Map<CharSequence, CsmUID<CsmClassifier>> map, CharSequence qn) {
        CsmUID<CsmClassifier> uid;
        try {
            declarationsLock.writeLock().lock();
            uid = map.remove(qn);
        } finally {
            declarationsLock.writeLock().unlock();
        }
        assert (uid == null) || (UIDCsmConverter.UIDtoCsmObject(uid) != null) : " no object for UID " + uid;
        if (uid != null) {
            put();
        }
    }

    //public void clearClassifiers() {
    //    classifiers.clear();
    //    typedefs.clear();
    //}

    private CharSequence getQualifiedNameWithoutScopeClasses(CsmClassifier decl) {
        CharSequence qualifiedNamePostfix;
        if(decl instanceof OffsetableDeclarationBase) {
            qualifiedNamePostfix = ((OffsetableDeclarationBase)decl).getQualifiedNamePostfix();
        } else {
            qualifiedNamePostfix = decl.getName();
        }
        CsmScope scope = decl.getScope();
        while (CsmKindUtilities.isClass(scope)) {
            scope = ((CsmClass) scope).getScope();
        }
        CharSequence qualifiedName;
        if (CsmKindUtilities.isNamespace(scope)) {
            qualifiedName = Utils.getQualifiedName(qualifiedNamePostfix.toString(), (CsmNamespace) scope);
        } else {
            qualifiedName = qualifiedNamePostfix;
        }
        qualifiedName = QualifiedNameCache.getManager().getString(qualifiedName);
        return qualifiedName;
    }

    private boolean isTypedef(CsmDeclaration decl){
        return CsmKindUtilities.isTypedef(decl);
    }
    
    @Override
    public void write(DataOutput output) throws IOException {
        super.write(output);
        try {
            declarationsLock.readLock().lock();
            UIDObjectFactory.getDefaultFactory().writeStringToUIDMap(this.classifiers, output, false);
            UIDObjectFactory.getDefaultFactory().writeStringToUIDMap(this.typedefs, output, false);
            UIDObjectFactory.getDefaultFactory().writeStringToUIDMapSet(this.inheritances, output);
        } finally {
            declarationsLock.readLock().unlock();
        }
    }
}
