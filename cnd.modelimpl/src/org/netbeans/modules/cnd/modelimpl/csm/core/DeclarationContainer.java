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
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFriendClass;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.repository.DeclarationContainerKey;
import org.netbeans.modules.cnd.modelimpl.repository.NamespaceDeclararationContainerKey;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.support.SelfPersistent;
import org.netbeans.modules.cnd.utils.cache.CharSequenceKey;
import org.netbeans.modules.cnd.modelimpl.textcache.UniqueNameCache;

/**
 * Storage for project or namespace declarations.
 * @author Alexander Simon
 */
public class DeclarationContainer extends ProjectComponent implements Persistent, SelfPersistent {
    
    private SortedMap<CharSequence, Object> declarations = new TreeMap<CharSequence, Object>(CharSequenceKey.Comparator);
    private ReadWriteLock declarationsLock = new ReentrantReadWriteLock();
    
    private Map<CharSequence, Set<CsmUID<? extends CsmFriend>>> friends = new ConcurrentHashMap<CharSequence, Set<CsmUID<? extends CsmFriend>>>();

    /** Creates a new instance of ProjectDeclarations */
    public DeclarationContainer(ProjectBase project) {
	super(new DeclarationContainerKey(project.getUniqueName().toString()));
	put();
    }

    /** Creates a new instance of ProjectDeclarations */
    public DeclarationContainer(CsmNamespace ns) {
        super(new NamespaceDeclararationContainerKey(ns));
        put();
    }
    
    public DeclarationContainer(DataInput input) throws IOException {
	super(input);
	read(input);
    }

    public void removeDeclaration(CsmDeclaration decl) {
	CharSequence uniqueName = CharSequenceKey.create(decl.getUniqueName());
	Object o = null;
	try {
	    declarationsLock.writeLock().lock();
	    o = declarations.get(uniqueName);

	    if (o instanceof CsmUID[]) {
                @SuppressWarnings("unchecked")
		CsmUID<CsmOffsetableDeclaration>[] uids = (CsmUID<CsmOffsetableDeclaration>[])o;
		int size = uids.length;
		CsmUID<CsmOffsetableDeclaration> res = null;
		int k = size;
		for(int i = 0; i < size; i++){
		    CsmUID<CsmOffsetableDeclaration> uid = uids[i];
		    if (isSameFile(uid,(CsmOffsetableDeclaration)decl)){
			uids[i] = null;
			k--;
		    } else {
			res = uid;
		    }
		}
		if (k == 0) {
		    declarations.remove(uniqueName);
		} else if (k == 1){
		    declarations.put(uniqueName,res);
		} else {
                    @SuppressWarnings("unchecked")
		    CsmUID<CsmOffsetableDeclaration>[] newUids = new CsmUID[k];
		    k = 0;
		    for(int i = 0; i < size; i++){
			CsmUID<CsmOffsetableDeclaration> uid = uids[i];
			if (uid != null){
			    newUids[k]=uid;
			    k++;
			}
		    }
		    declarations.put(uniqueName,newUids);
		}
	    } else if (o instanceof CsmUID){
		declarations.remove(uniqueName);
	    }
	} finally {
	    declarationsLock.writeLock().unlock();
	}
	removeFriend(decl);
	put();
    }
    
    private void removeFriend(CsmDeclaration decl){
        if (CsmKindUtilities.isFriendClass(decl)) {
            CsmFriendClass cls = (CsmFriendClass) decl;
            CharSequence name = CharSequenceKey.create(cls.getName());
            Set<CsmUID<? extends CsmFriend>> set = friends.get(name);
            if (set != null) {
                set.remove(cls.getUID());
                if (set.size()==0){
                    friends.remove(name);
                }
            }
        } else if (CsmKindUtilities.isFriendMethod(decl)) {
            CsmFriendFunction fun = (CsmFriendFunction) decl;
            CharSequence name = CharSequenceKey.create(fun.getSignature());
            Set<CsmUID<? extends CsmFriend>> set = friends.get(name);
            if (set != null) {
                set.remove(fun.getUID());
                if (set.size()==0){
                    friends.remove(name);
                }
            }
        }
    }

    public void putDeclaration(CsmOffsetableDeclaration decl) {
	CharSequence name = UniqueNameCache.getManager().getString(decl.getUniqueName());
        @SuppressWarnings("unchecked")
	CsmUID<CsmOffsetableDeclaration> uid = RepositoryUtils.put(decl);
	assert uid != null;
	try {
	    declarationsLock.writeLock().lock();

	    Object o = declarations.get(name);
	    if (o instanceof CsmUID[]) {
                @SuppressWarnings("unchecked")
		CsmUID<CsmOffsetableDeclaration>[] uids = (CsmUID[])o;
		boolean find = false;
		for(int i = 0; i < uids.length; i++){
		    if (isSameFile(uids[i],uid)){
			uids[i] = uid;
			find = true;
			break;
		    }
		}
		if (!find){
                    @SuppressWarnings("unchecked")
		    CsmUID<CsmOffsetableDeclaration>[] res = new CsmUID[uids.length+1];
		    res[0]=uid;
		    for(int i = 0; i < uids.length; i++){
			res[i+1] = uids[i];
		    }
		    declarations.put(name, res);
		}
	    } else if (o instanceof CsmUID) {
                @SuppressWarnings("unchecked")
		CsmUID<CsmOffsetableDeclaration> oldUid = (CsmUID<CsmOffsetableDeclaration>)o;
		if (isSameFile(oldUid,uid)) {
		    declarations.put(name, uid);
		} else {
		    CsmUID[] uids = new CsmUID[]{uid,oldUid};
		    declarations.put(name, uids);
		}
	    } else {
		declarations.put(name, uid);
	    }
	} finally {
	    declarationsLock.writeLock().unlock();
	}
	putFriend(decl);
    }
    
    private void putFriend(CsmDeclaration decl){
        if (CsmKindUtilities.isFriendClass(decl)) {
            CsmFriendClass cls = (CsmFriendClass) decl;
            CharSequence name = CharSequenceKey.create(cls.getName());
            Set<CsmUID<? extends CsmFriend>> set = friends.get(name);
            if (set == null) {
                set = new HashSet<CsmUID<? extends CsmFriend>>();
                friends.put(name,set);
            }
            set.add(cls.getUID());
        } else if (CsmKindUtilities.isFriendMethod(decl)) {
            CsmFriendFunction fun = (CsmFriendFunction) decl;
            CharSequence name = CharSequenceKey.create(fun.getSignature());
            Set<CsmUID<? extends CsmFriend>> set = friends.get(name);
            if (set == null) {
                set = new HashSet<CsmUID<? extends CsmFriend>>();
                friends.put(name,set);
            }
            set.add(fun.getUID());
        }
	put();
    }
    
    public Collection<CsmUID<CsmOffsetableDeclaration>> getUIDsRange(CharSequence from, CharSequence to) {
        List<CsmUID<CsmOffsetableDeclaration>> list = new ArrayList<CsmUID<CsmOffsetableDeclaration>>();
        from = CharSequenceKey.create(from);
        to = CharSequenceKey.create(to);
        try {
            declarationsLock.readLock().lock();
            for (Map.Entry<CharSequence, Object> entry : declarations.subMap(from, to).entrySet()){
                Object o = entry.getValue();
                if (o instanceof CsmUID[]) {
                    CsmUID[] uids = (CsmUID[])o;
                    for(CsmUID uid:uids){
                        list.add(uid);
                    }
                } else if (o instanceof CsmUID){
                    list.add((CsmUID)o);
                }
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        return list;
    }
    
    public Collection<CsmOffsetableDeclaration> getDeclarationsRange(CharSequence from, CharSequence to) {
        return UIDCsmConverter.UIDsToDeclarations(getUIDsRange(from, to));
    }

    public Collection<CsmUID<CsmOffsetableDeclaration>> getDeclarationsUIDs() {
        // add all declarations
        Collection<CsmUID<CsmOffsetableDeclaration>> list = new ArrayList<CsmUID<CsmOffsetableDeclaration>>();
        try {
            declarationsLock.readLock().lock();
            for (Object o : declarations.values()) {
                if (o instanceof CsmUID[]) {
                    CsmUID[] uids = (CsmUID[]) o;
                    for (CsmUID<CsmOffsetableDeclaration> uid : uids) {
                        list.add(uid);
                    }
                } else if (o instanceof CsmUID) {
                    list.add((CsmUID) o);
                }
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        return list;
    }

    public Collection<CsmFriend> findFriends(CsmOffsetableDeclaration decl){
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
            if (list.size()>0){
                Collection<CsmFriend> res = new ArrayList<CsmFriend>();
                for(CsmUID<? extends CsmFriend> friendUID : list){
                    CsmFriend friend = friendUID.getObject();
                    if (CsmKindUtilities.isFriendClass(friend)) {
                        CsmFriendClass cls = (CsmFriendClass) friend;
                        if (decl.equals(cls.getReferencedClass())){
                            res.add(cls);
                        }
                    } else if (CsmKindUtilities.isFriendMethod(friend)) {
                        CsmFriendFunction fun = (CsmFriendFunction) friend;
                        if (decl.equals(fun.getReferencedFunction())){
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
        List<CsmUID<CsmOffsetableDeclaration>> list = new ArrayList<CsmUID<CsmOffsetableDeclaration>>();
        uniqueName = CharSequenceKey.create(uniqueName);
        try {
            declarationsLock.readLock().lock();
            Object o = declarations.get(uniqueName);
            if (o instanceof CsmUID[]) {
                CsmUID[] uids = (CsmUID[])o;
                for(CsmUID uid:uids){
                    list.add(uid);
                }
            } else if (o instanceof CsmUID){
                list.add((CsmUID)o);
            }
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
            if (o instanceof CsmUID[]) {
                uid = ((CsmUID[])o)[0];
            } else if (o instanceof CsmUID){
                uid = (CsmUID)o;
            }
        } finally {
            declarationsLock.readLock().unlock();
        }
        result = UIDCsmConverter.UIDtoDeclaration(uid);
        assert result != null || uid == null : "no declaration for UID " + uid;
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
    
    private void read(DataInput aStream) throws IOException {
        UIDObjectFactory.getDefaultFactory().readStringToArrayUIDMap(declarations, aStream, UniqueNameCache.getManager());
    }
    
    private boolean isSameFile(CsmUID<CsmOffsetableDeclaration> uid1, CsmUID<CsmOffsetableDeclaration> uid2){
        return isSameFile(uid1.getObject(), uid2.getObject());
    }
    
    private boolean isSameFile(CsmUID<CsmOffsetableDeclaration> uid1, CsmOffsetableDeclaration decl2){
        return isSameFile(uid1.getObject(), decl2);
    }
    
    private boolean isSameFile(CsmOffsetableDeclaration decl1, CsmOffsetableDeclaration decl2){
        if (decl1 != null && decl2 != null){
            CsmFile file1 = decl1.getContainingFile();
            CsmFile file2 = decl2.getContainingFile();
            if (file1 != null && file2 != null) {
                return file1.equals(file2);
            }
        }
        // assert?
        return false;
    }
}
