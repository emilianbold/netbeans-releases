/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFriendClass;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.repository.DeclarationContainerKey;
import org.netbeans.modules.cnd.modelimpl.repository.NamespaceDeclarationContainerKey;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;
import org.netbeans.modules.cnd.modelimpl.textcache.UniqueNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;

/**
 * Storage for project or namespace declarations.
 * @author Alexander Simon
 */
public class DeclarationContainer extends ProjectComponent implements Persistent, SelfPersistent {

    private final TreeMap<CharSequence, Object> declarations;
    private final ReadWriteLock declarationsLock = new ReentrantReadWriteLock();
    private final Map<CharSequence, Set<CsmUID<? extends CsmFriend>>> friends = new ConcurrentHashMap<CharSequence, Set<CsmUID<? extends CsmFriend>>>();
    // empty stub
    private static final DeclarationContainer EMPTY = new DeclarationContainer() {

        @Override
        public void put() {
        }

        @Override
        public void putDeclaration(CsmOffsetableDeclaration decl) {
        }
    };

    /** Creates a new instance of ProjectDeclarations */
    public DeclarationContainer(ProjectBase project) {
        super(new DeclarationContainerKey(project.getUniqueName().toString()), false);
        declarations = new TreeMap<CharSequence, Object>(CharSequenceKey.Comparator);
        put();
    }

    /** Creates a new instance of ProjectDeclarations */
    public DeclarationContainer(CsmNamespace ns) {
        super(new NamespaceDeclarationContainerKey(ns), false);
        declarations = new TreeMap<CharSequence, Object>(CharSequenceKey.Comparator);
        put();
    }

    public DeclarationContainer(DataInput input) throws IOException {
        super(input);
        declarations = UIDObjectFactory.getDefaultFactory().readStringToArrayUIDMap(input, UniqueNameCache.getManager());
    }

    // only for EMPTY static field
    private DeclarationContainer() {
        super((org.netbeans.modules.cnd.repository.spi.Key) null, false);
        declarations = new TreeMap<CharSequence, Object>(CharSequenceKey.Comparator);
    }

    public static DeclarationContainer empty() {
        return EMPTY;
    }

    public void removeDeclaration(CsmOffsetableDeclaration decl) {
        CharSequence uniqueName = CharSequenceKey.create(decl.getUniqueName());
        CsmUID<CsmOffsetableDeclaration> anUid = UIDCsmConverter.declarationToUID(decl);
        Object o = null;
        try {
            declarationsLock.writeLock().lock();
            o = declarations.get(uniqueName);

            if (o instanceof CsmUID[]) {
                @SuppressWarnings("unchecked")
                CsmUID<CsmOffsetableDeclaration>[] uids = (CsmUID<CsmOffsetableDeclaration>[]) o;
                int size = uids.length;
                CsmUID<CsmOffsetableDeclaration> res = null;
                int k = size;
                for (int i = 0; i < size; i++) {
                    CsmUID<CsmOffsetableDeclaration> uid = uids[i];
                    if (UIDUtilities.isSameFile(uid, anUid)) {
                        uids[i] = null;
                        k--;
                    } else {
                        res = uid;
                    }
                }
                if (k == 0) {
                    declarations.remove(uniqueName);
                } else if (k == 1) {
                    declarations.put(uniqueName, res);
                } else {
                    @SuppressWarnings("unchecked")
                    CsmUID<CsmOffsetableDeclaration>[] newUids = new CsmUID[k];
                    k = 0;
                    for (int i = 0; i < size; i++) {
                        CsmUID<CsmOffsetableDeclaration> uid = uids[i];
                        if (uid != null) {
                            newUids[k] = uid;
                            k++;
                        }
                    }
                    declarations.put(uniqueName, newUids);
                }
            } else if (o instanceof CsmUID) {
                declarations.remove(uniqueName);
            }
        } finally {
            declarationsLock.writeLock().unlock();
        }
        removeFriend(decl);
        put();
    }

    private void removeFriend(CsmOffsetableDeclaration decl) {
        if (CsmKindUtilities.isFriendClass(decl)) {
            CsmFriendClass cls = (CsmFriendClass) decl;
            CharSequence name = CharSequenceKey.create(cls.getName());
            Set<CsmUID<? extends CsmFriend>> set = friends.get(name);
            if (set != null) {
                set.remove(UIDs.get(cls));
                if (set.size() == 0) {
                    friends.remove(name);
                }
            }
        } else if (CsmKindUtilities.isFriendMethod(decl)) {
            CsmFriendFunction fun = (CsmFriendFunction) decl;
            CharSequence name = CharSequenceKey.create(fun.getSignature());
            Set<CsmUID<? extends CsmFriend>> set = friends.get(name);
            if (set != null) {
                set.remove(UIDs.get(fun));
                if (set.size() == 0) {
                    friends.remove(name);
                }
            }
        }
    }

    public void putDeclaration(CsmOffsetableDeclaration decl) {
        CharSequence name = UniqueNameCache.getManager().getString(decl.getUniqueName());
        CsmUID<CsmOffsetableDeclaration> uid = RepositoryUtils.put(decl);
        assert uid != null;
        if (!(uid instanceof SelfPersistent)) {
            String line = " ["+decl.getStartPosition().getLine()+":"+decl.getStartPosition().getColumn()+"-"+ // NOI18N
                          decl.getEndPosition().getLine()+":"+decl.getEndPosition().getColumn()+"]"; // NOI18N
            new Exception("attempt to put local declaration " + decl + line).printStackTrace(); // NOI18N
        }
        try {
            declarationsLock.writeLock().lock();

            Object o = declarations.get(name);
            if (o instanceof CsmUID[]) {
                @SuppressWarnings("unchecked")
                CsmUID<CsmOffsetableDeclaration>[] uids = (CsmUID[]) o;
                boolean find = false;
                for (int i = 0; i < uids.length; i++) {
                    if (UIDUtilities.isSameFile(uids[i], uid)) {
                        uids[i] = uid;
                        find = true;
                        break;
                    }
                }
                if (!find) {
                    @SuppressWarnings("unchecked")
                    CsmUID<CsmOffsetableDeclaration>[] res = new CsmUID[uids.length + 1];
                    res[0] = uid;
                    for (int i = 0; i < uids.length; i++) {
                        res[i + 1] = uids[i];
                    }
                    declarations.put(name, res);
                }
            } else if (o instanceof CsmUID) {
                @SuppressWarnings("unchecked")
                CsmUID<CsmOffsetableDeclaration> oldUid = (CsmUID<CsmOffsetableDeclaration>) o;
                if (UIDUtilities.isSameFile(oldUid, uid)) {
                    declarations.put(name, uid);
                } else {
                    CsmUID[] uids = new CsmUID[]{uid, oldUid};
                    declarations.put(name, uids);
                }
            } else {
                declarations.put(name, uid);
            }
        } finally {
            declarationsLock.writeLock().unlock();
        }
        putFriend(decl);
        put();
    }

    private void putFriend(CsmDeclaration decl) {
        if (CsmKindUtilities.isFriendClass(decl)) {
            CsmFriendClass cls = (CsmFriendClass) decl;
            CharSequence name = CharSequenceKey.create(cls.getName());
            Set<CsmUID<? extends CsmFriend>> set = friends.get(name);
            if (set == null) {
                set = new HashSet<CsmUID<? extends CsmFriend>>();
                friends.put(name, set);
            }
            set.add(UIDs.get(cls));
        } else if (CsmKindUtilities.isFriendMethod(decl)) {
            CsmFriendFunction fun = (CsmFriendFunction) decl;
            CharSequence name = CharSequenceKey.create(fun.getSignature());
            Set<CsmUID<? extends CsmFriend>> set = friends.get(name);
            if (set == null) {
                set = new HashSet<CsmUID<? extends CsmFriend>>();
                friends.put(name, set);
            }
            set.add(UIDs.get(fun));
        }
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> getUIDsRange(CharSequence from, CharSequence to) {
        Collection<CsmUID<CsmOffsetableDeclaration>> list = new ArrayList<CsmUID<CsmOffsetableDeclaration>>();
        from = CharSequenceKey.create(from);
        to = CharSequenceKey.create(to);
        try {
            declarationsLock.readLock().lock();
            for (Map.Entry<CharSequence, Object> entry : declarations.subMap(from, to).entrySet()) {
                addAll(list, entry.getValue());
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        return list;
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> getUIDsFQN(CharSequence fqn, Kind[] kinds) {
        Collection<CsmUID<CsmOffsetableDeclaration>> list = new ArrayList<CsmUID<CsmOffsetableDeclaration>>();
        char maxChar = 255; //Character.MAX_VALUE;
        for(Kind kind : kinds) {
            String prefix = Utils.getCsmDeclarationKindkey(kind) + OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR + fqn;
            CharSequence from  = CharSequenceKey.create(prefix);
            CharSequence to  = CharSequenceKey.create(prefix+maxChar);
            try {
                declarationsLock.readLock().lock();
                for (Map.Entry<CharSequence, Object> entry : declarations.subMap(from, to).entrySet()) {
                    addAll(list, entry.getValue());
                }
            } finally {
                declarationsLock.readLock().unlock();
            }
        }
        return list;
    }

    // for unit test
    SortedMap<CharSequence, Object> testDeclarations() {
        try {
            declarationsLock.readLock().lock();
            return new TreeMap<CharSequence, Object>(declarations);
        } finally {
            declarationsLock.readLock().unlock();
        }
    }

    SortedMap<CharSequence, Set<CsmUID<? extends CsmFriend>>> testFriends(){
        return new TreeMap<CharSequence, Set<CsmUID<? extends CsmFriend>>>(friends);
    }

    /**
     * Adds ether object to the collection or array of objects
     * @param list
     * @param o - can be CsmUID or CsmUID[]
     */
    private static void addAll(Collection<CsmUID<CsmOffsetableDeclaration>> list, Object o) {
        if (o instanceof CsmUID<?>[]) {
            // we know the template type to be CsmOffsetableDeclaration
            @SuppressWarnings("unchecked") // checked
            final CsmUID<CsmOffsetableDeclaration>[] uids = (CsmUID<CsmOffsetableDeclaration>[]) o;
            for (CsmUID<CsmOffsetableDeclaration> uid : uids) {
                list.add(uid);
            }
        } else if (o instanceof CsmUID<?>) {
            // we know the template type to be CsmOffsetableDeclaration
            @SuppressWarnings("unchecked") // checked
            final CsmUID<CsmOffsetableDeclaration> uid = (CsmUID<CsmOffsetableDeclaration>) o;
            list.add(uid);
        }
    }

    public Collection<CsmOffsetableDeclaration> getDeclarationsRange(CharSequence from, CharSequence to) {
        return UIDCsmConverter.UIDsToDeclarations(getUIDsRange(from, to));
    }

    public Collection<CsmOffsetableDeclaration> getDeclarationsRange(CharSequence fqn, Kind[] kinds) {
        return UIDCsmConverter.UIDsToDeclarations(getUIDsFQN(fqn, kinds));
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> getDeclarationsUIDs() {
        // add all declarations
        Collection<CsmUID<CsmOffsetableDeclaration>> list = new ArrayList<CsmUID<CsmOffsetableDeclaration>>();
        try {
            declarationsLock.readLock().lock();
            for (Object o : declarations.values()) {
                addAll(list, o);
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        return list;
    }

    public Collection<CsmFriend> findFriends(CsmOffsetableDeclaration decl) {
        CharSequence name = null;
        if (CsmKindUtilities.isClass(decl)) {
            CsmClass cls = (CsmClass) decl;
            name = cls.getName();
        } else if (CsmKindUtilities.isFunction(decl)) {
            CsmFunction fun = (CsmFunction) decl;
            name = fun.getSignature();
        }
        if (name != null) {
            name = CharSequenceKey.create(name);
            List<CsmUID<? extends CsmFriend>> list = new ArrayList<CsmUID<? extends CsmFriend>>();
            try {
                declarationsLock.readLock().lock();
                Set<CsmUID<? extends CsmFriend>> set = friends.get(name);
                if (set != null) {
                    list.addAll(set);
                }
            } finally {
                declarationsLock.readLock().unlock();
            }
            if (list.size() > 0) {
                Collection<CsmFriend> res = new ArrayList<CsmFriend>();
                for (CsmUID<? extends CsmFriend> friendUID : list) {
                    CsmFriend friend = friendUID.getObject();
                    if (CsmKindUtilities.isFriendClass(friend)) {
                        CsmFriendClass cls = (CsmFriendClass) friend;
                        if (decl.equals(cls.getReferencedClass())) {
                            res.add(cls);
                        }
                    } else if (CsmKindUtilities.isFriendMethod(friend)) {
                        CsmFriendFunction fun = (CsmFriendFunction) friend;
                        if (decl.equals(fun.getReferencedFunction())) {
                            res.add(fun);
                        }
                    }
                }
                return res;
            }
        }
        return Collections.<CsmFriend>emptyList();
    }

    public Collection<CsmOffsetableDeclaration> findDeclarations(CharSequence uniqueName) {
        Collection<CsmUID<CsmOffsetableDeclaration>> list = new ArrayList<CsmUID<CsmOffsetableDeclaration>>();
        uniqueName = CharSequenceKey.create(uniqueName);
        try {
            declarationsLock.readLock().lock();
            addAll(list, declarations.get(uniqueName));
        } finally {
            declarationsLock.readLock().unlock();
        }
        return UIDCsmConverter.UIDsToDeclarations(list);
    }

    public CsmDeclaration getDeclaration(CharSequence uniqueName) {
        CsmDeclaration result;
        CsmUID<CsmDeclaration> uid = null;
        uniqueName = CharSequenceKey.create(uniqueName);
        try {
            declarationsLock.readLock().lock();
            Object o = declarations.get(uniqueName);
            if (o instanceof CsmUID<?>[]) {
                // we know the template type to be CsmDeclaration
                @SuppressWarnings("unchecked") // checked
                final CsmUID<CsmDeclaration>[] uids = (CsmUID<CsmDeclaration>[]) o;
                uid = uids[0];
            } else if (o instanceof CsmUID<?>) {
                // we know the template type to be CsmDeclaration
                @SuppressWarnings("unchecked") // checked
                final CsmUID<CsmDeclaration> uidt = (CsmUID<CsmDeclaration>) o;
                uid = uidt;
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        result = UIDCsmConverter.UIDtoDeclaration(uid);
        if (uid != null && result == null) {
            DiagnosticExceptoins.register(new IllegalStateException("no declaration for UID " + uid)); // NOI18N
        }
        return result;
    }

    public void clearDeclarations() {
        try {
            declarationsLock.writeLock().lock();
            declarations.clear();
            put();
        } finally {
            declarationsLock.writeLock().unlock();
        }
    }

    @Override
    public void write(DataOutput aStream) throws IOException {
        super.write(aStream);
        try {
            declarationsLock.readLock().lock();
            UIDObjectFactory.getDefaultFactory().writeStringToArrayUIDMap(declarations, aStream, false);
        } finally {
            declarationsLock.readLock().unlock();
        }
    }
}
