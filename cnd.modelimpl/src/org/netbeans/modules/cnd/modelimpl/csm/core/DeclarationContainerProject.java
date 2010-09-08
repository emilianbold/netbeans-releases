/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFriendClass;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.repository.DeclarationContainerKey;
import org.netbeans.modules.cnd.modelimpl.textcache.UniqueNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.openide.util.CharSequences;

/**
 *
 * @author Alexander Simon
 */
public class DeclarationContainerProject extends DeclarationContainer {
    private final Map<CharSequence, Set<CsmUID<CsmFriend>>> friends;

    private static final DeclarationContainerProject EMPTY = new DeclarationContainerProject() {

        @Override
        public void put() {
        }

        @Override
        public void putDeclaration(CsmOffsetableDeclaration decl) {
        }
    };

    public DeclarationContainerProject(ProjectBase project) {
        super(new DeclarationContainerKey(project.getUniqueName()), false);
        friends = new ConcurrentHashMap<CharSequence, Set<CsmUID<CsmFriend>>>();
        put();
    }

    public DeclarationContainerProject(DataInput input) throws IOException {
        super(input);
        int colSize = input.readInt();
        friends = new ConcurrentHashMap<CharSequence, Set<CsmUID<CsmFriend>>>(colSize);
        UIDObjectFactory.getDefaultFactory().readStringToUIDMapSet(friends, input, UniqueNameCache.getManager(), colSize);
    }

    // only for EMPTY static field
    private DeclarationContainerProject() {
        super((Key) null, false);
        friends = new ConcurrentHashMap<CharSequence, Set<CsmUID<CsmFriend>>>();
    }

    public static DeclarationContainerProject empty() {
        return EMPTY;
    }

    @Override
    protected void onRemoveDeclaration(CsmOffsetableDeclaration decl) {
        if (CsmKindUtilities.isFriendClass(decl)) {
            CsmFriend cls = (CsmFriend) decl;
            CharSequence name = CharSequences.create(cls.getName());
            Set<CsmUID<CsmFriend>> set = friends.get(name);
            if (set != null) {
                set.remove(UIDs.get(cls));
                if (set.isEmpty()) {
                    friends.remove(name);
                }
            }
        } else if (CsmKindUtilities.isFriendMethod(decl)) {
            CsmFriend fun = (CsmFriend) decl;
            CharSequence name = CharSequences.create(((CsmFriendFunction)fun).getSignature());
            Set<CsmUID<CsmFriend>> set = friends.get(name);
            if (set != null) {
                set.remove(UIDs.get(fun));
                if (set.isEmpty()) {
                    friends.remove(name);
                }
            }
        }
    }

    @Override
    protected void onPutDeclaration(CsmOffsetableDeclaration decl) {
        if (CsmKindUtilities.isFriendClass(decl)) {
            CsmFriend cls = (CsmFriend) decl;
            CharSequence name = CharSequences.create(cls.getName());
            Set<CsmUID<CsmFriend>> set = friends.get(name);
            if (set == null) {
                set = new HashSet<CsmUID<CsmFriend>>();
                friends.put(name, set);
            }
            set.add(UIDs.get(cls));
        } else if (CsmKindUtilities.isFriendMethod(decl)) {
            CsmFriend fun = (CsmFriend) decl;
            CharSequence name = CharSequences.create(((CsmFriendFunction)fun).getSignature());
            Set<CsmUID<CsmFriend>> set = friends.get(name);
            if (set == null) {
                set = new HashSet<CsmUID<CsmFriend>>();
                friends.put(name, set);
            }
            set.add(UIDs.get(fun));
        }
    }

    SortedMap<CharSequence, Set<CsmUID<CsmFriend>>> testFriends(){
        return new TreeMap<CharSequence, Set<CsmUID<CsmFriend>>>(friends);
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
            name = CharSequences.create(name);
            List<CsmUID<? extends CsmFriend>> list = new ArrayList<CsmUID<? extends CsmFriend>>();
            try {
                getLock().readLock().lock();
                Set<CsmUID<CsmFriend>> set = friends.get(name);
                if (set != null) {
                    list.addAll(set);
                }
            } finally {
                getLock().readLock().unlock();
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

    @Override
    public void write(DataOutput aStream) throws IOException {
        super.write(aStream);
        try {
            getLock().readLock().lock();
            UIDObjectFactory.getDefaultFactory().writeStringToUIDMapSet(friends, aStream);
        } finally {
            getLock().readLock().unlock();
        }
    }
}
