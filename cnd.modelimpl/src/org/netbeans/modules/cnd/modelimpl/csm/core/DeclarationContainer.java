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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFriendClass;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.apt.utils.TextCache;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 * Storage for project declarations. Class was extracted from ProjectBase.
 * @author Alexander Simon
 */
/*package-local*/ class DeclarationContainer {
    private SortedMap<String, Object> declarationsOLD = Collections.synchronizedSortedMap(new TreeMap<String, Object>());
    //private Map<String, CsmUID<CsmDeclaration>> declarations = Collections.synchronizedMap(new HashMap<String, CsmUID<CsmDeclaration>>());
    private SortedMap<String, Object> declarations = Collections.synchronizedSortedMap(new TreeMap<String, Object>());
    
    private Map<String, Set<CsmUID<? extends CsmFriend>>> friends = Collections.synchronizedMap(new HashMap<String, Set<CsmUID<? extends CsmFriend>>>());
    
    /** Creates a new instance of ProjectDeclarations */
    public DeclarationContainer() {
    }
    
    public void removeDeclaration(CsmDeclaration decl) {
        if (TraceFlags.USE_REPOSITORY) {
            String uniqueName = decl.getUniqueName();
            synchronized(declarations){
                Object o = declarations.get(uniqueName);
                if (o instanceof CsmUID[]) {
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
                removeFriend(decl);
            }
        } else {
            String uniqueName = decl.getUniqueName();
            synchronized(declarationsOLD){
                Object o = declarationsOLD.get(uniqueName);
                if (o instanceof CsmOffsetableDeclaration[]) {
                    CsmOffsetableDeclaration[] decls = (CsmOffsetableDeclaration[])o;
                    int size = decls.length;
                    CsmOffsetableDeclaration res = null;
                    int k = size;
                    for(int i = 0; i < size; i++){
                        CsmOffsetableDeclaration d = decls[i];
                        if (isSameFile(d,(CsmOffsetableDeclaration)decl)){
                            decls[i] = null;
                            k--;
                        } else {
                            res = d;
                        }
                    }
                    if (k == 0) {
                        declarationsOLD.remove(uniqueName);
                    } else if (k == 1){
                        declarationsOLD.put(uniqueName,res);
                    } else {
                        CsmOffsetableDeclaration[] newDecls = new CsmOffsetableDeclaration[k];
                        k = 0;
                        for(int i = 0; i < size; i++){
                            CsmOffsetableDeclaration d = decls[i];
                            if (d != null){
                                newDecls[k]=d;
                                k++;
                            }
                        }
                        declarationsOLD.put(uniqueName,newDecls);
                    }
                } else if (o instanceof CsmOffsetableDeclaration){
                    declarationsOLD.remove(uniqueName);
                }
            }
        }
    }
    
    private void removeFriend(CsmDeclaration decl){
        if (CsmKindUtilities.isFriendClass(decl)) {
            CsmFriendClass cls = (CsmFriendClass) decl;
            String name = cls.getName();
            Set<CsmUID<? extends CsmFriend>> set = friends.get(name);
            if (set != null) {
                set.remove(cls.getUID());
                if (set.size()==0){
                    friends.remove(name);
                }
            }
        } else if (CsmKindUtilities.isFriendMethod(decl)) {
            CsmFriendFunction fun = (CsmFriendFunction) decl;
            String name = fun.getSignature();
            Set<CsmUID<? extends CsmFriend>> set = friends.get(name);
            if (set != null) {
                set.remove(fun.getUID());
                if (set.size()==0){
                    friends.remove(name);
                }
            }
        }
    }
    
    public void putDeclaration(CsmDeclaration decl) {
        String name = decl.getUniqueName();
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmOffsetableDeclaration> uid = RepositoryUtils.put(decl);
            assert uid != null;
            synchronized(declarations) {
                Object o = declarations.get(name);
                if (o instanceof CsmUID[]) {
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
                        CsmUID<CsmOffsetableDeclaration>[] res = new CsmUID[uids.length+1];
                        res[0]=uid;
                        for(int i = 0; i < uids.length; i++){
                            res[i+1] = uids[i];
                        }
                        declarations.put(name, res);
                    }
                } else if (o instanceof CsmUID) {
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
                putFriend(decl);
            }
        } else {
            CsmOffsetableDeclaration newDecl = (CsmOffsetableDeclaration)decl;
            synchronized(declarationsOLD){
                Object o = declarationsOLD.get(name);
                if (o instanceof CsmOffsetableDeclaration[]) {
                    CsmOffsetableDeclaration[] decls = (CsmOffsetableDeclaration[])o;
                    boolean find = false;
                    for(int i = 0; i < decls.length; i++){
                        if (isSameFile(decls[i], newDecl)){
                            decls[i] = newDecl;
                            find = true;
                            break;
                        }
                    }
                    if (!find){
                        CsmOffsetableDeclaration[] res = new CsmOffsetableDeclaration[decls.length+1];
                        res[0]=newDecl;
                        for(int i = 0; i < decls.length; i++){
                            res[i+1] = decls[i];
                        }
                        declarationsOLD.put(name, res);
                    }
                } else if (o instanceof CsmOffsetableDeclaration) {
                    CsmOffsetableDeclaration oldDecl = (CsmOffsetableDeclaration)o;
                    if (isSameFile(oldDecl, newDecl)) {
                        declarationsOLD.put(name, decl);
                    } else {
                        CsmOffsetableDeclaration[] decls = new CsmOffsetableDeclaration[]{newDecl,oldDecl};
                        declarationsOLD.put(name, decls);
                    }
                } else {
                    declarationsOLD.put(name, newDecl);
                }
            }
        }
    }
    
    private void putFriend(CsmDeclaration decl){
        if (CsmKindUtilities.isFriendClass(decl)) {
            CsmFriendClass cls = (CsmFriendClass) decl;
            String name = cls.getName();
            Set<CsmUID<? extends CsmFriend>> set = friends.get(name);
            if (set == null) {
                set = new HashSet<CsmUID<? extends CsmFriend>>();
                friends.put(name,set);
            }
            set.add(cls.getUID());
        } else if (CsmKindUtilities.isFriendMethod(decl)) {
            CsmFriendFunction fun = (CsmFriendFunction) decl;
            String name = fun.getSignature();
            Set<CsmUID<? extends CsmFriend>> set = friends.get(name);
            if (set == null) {
                set = new HashSet<CsmUID<? extends CsmFriend>>();
                friends.put(name,set);
            }
            set.add(fun.getUID());
        }
    }
    
    public Collection<CsmOffsetableDeclaration> getDeclarationsRange(String from, String to) {
        if (TraceFlags.USE_REPOSITORY) {
            List<CsmUID<CsmOffsetableDeclaration>> list = new ArrayList<CsmUID<CsmOffsetableDeclaration>>();
            synchronized(declarations) {
                for (Map.Entry<String, Object> entry : declarations.subMap(from, to).entrySet()){
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
            }
            return UIDCsmConverter.UIDsToDeclarations(list);
        } else {
            List<CsmOffsetableDeclaration> list = new ArrayList<CsmOffsetableDeclaration>();
            synchronized(declarationsOLD){
                for (Map.Entry<String, Object> entry : declarationsOLD.subMap(from, to).entrySet()){
                    Object o = entry.getValue();
                    if (o instanceof CsmOffsetableDeclaration[]) {
                        CsmOffsetableDeclaration[] decls = (CsmOffsetableDeclaration[])o;
                        for(CsmOffsetableDeclaration decl : decls){
                            list.add(decl);
                        }
                    } else if (o instanceof CsmOffsetableDeclaration){
                        list.add((CsmOffsetableDeclaration)o);
                    }
                }
            }
            return list;
        }
    }
    
    public Collection<CsmFriend> findFriends(CsmOffsetableDeclaration decl){
        if (TraceFlags.USE_REPOSITORY) {
            String name = null;
            if (CsmKindUtilities.isClass(decl)) {
                CsmClass cls = (CsmClass) decl;
                name = cls.getName();
            } else if (CsmKindUtilities.isFunction(decl)) {
                CsmFunction fun = (CsmFunction) decl;
                name = fun.getSignature();
            }
            if (name != null) {
                List<CsmUID<? extends CsmFriend>> list = new ArrayList<CsmUID<? extends CsmFriend>>();
                synchronized(declarations) {
                    Set<CsmUID<? extends CsmFriend>> set = friends.get(name);
                    if (set != null) {
                        list.addAll(set);
                    }
                }
                if (list.size()>0){
                    Collection<CsmFriend> res = new ArrayList<CsmFriend>();
                    for(CsmUID<? extends CsmFriend> friendUID : list){
                        CsmFriend friend = friendUID.getObject();
                        if (CsmKindUtilities.isFriendClass(friend)) {
                            CsmFriendClass cls = (CsmFriendClass) friend;
                            if (cls.getReferencedClass() == decl){
                                res.add(cls);
                            }
                        } else if (CsmKindUtilities.isFriendMethod(friend)) {
                            CsmFriendFunction fun = (CsmFriendFunction) friend;
                            if (fun.getReferencedFunction() == decl){
                                res.add(fun);
                            }
                        }
                    }
                    return res;
                }
            }
        }
        return Collections.<CsmFriend>emptyList();
    }
    
    public Collection<CsmOffsetableDeclaration> findDeclarations(String uniqueName) {
        if (TraceFlags.USE_REPOSITORY) {
            List<CsmUID<CsmOffsetableDeclaration>> list = new ArrayList<CsmUID<CsmOffsetableDeclaration>>();
            synchronized(declarations) {
                Object o = declarations.get(uniqueName);
                if (o instanceof CsmUID[]) {
                    CsmUID[] uids = (CsmUID[])o;
                    for(CsmUID uid:uids){
                        list.add(uid);
                    }
                } else if (o instanceof CsmUID){
                    list.add((CsmUID)o);
                }
            }
            return UIDCsmConverter.UIDsToDeclarations(list);
        } else {
            List<CsmOffsetableDeclaration> list = new ArrayList<CsmOffsetableDeclaration>();
            synchronized(declarationsOLD){
                Object o = declarationsOLD.get(uniqueName);
                if (o instanceof CsmOffsetableDeclaration[]) {
                    CsmOffsetableDeclaration[] decls = (CsmOffsetableDeclaration[])o;
                    for(CsmOffsetableDeclaration decl:decls){
                        list.add(decl);
                    }
                } else if (o instanceof CsmOffsetableDeclaration){
                    list.add((CsmOffsetableDeclaration)o);
                }
            }
            return list;
        }
    }
    
    public CsmDeclaration getDeclaration(String uniqueName) {
        CsmDeclaration result;
        if (TraceFlags.USE_REPOSITORY) {
            CsmUID<CsmDeclaration> uid = null;
            synchronized(declarations) {
                Object o = declarations.get(uniqueName);
                if (o instanceof CsmUID[]) {
                    uid = ((CsmUID[])o)[0];
                } else if (o instanceof CsmUID){
                    uid = (CsmUID)o;
                }
            }
            result = UIDCsmConverter.UIDtoDeclaration(uid);
            assert result != null || uid == null : "no declaration for UID " + uid;
        } else {
            synchronized(declarationsOLD){
                Object o = declarationsOLD.get(uniqueName);
                if (o instanceof CsmDeclaration[]) {
                    result = ((CsmDeclaration[])o)[0];
                } else {
                    result = (CsmDeclaration)o;
                }
            }
        }
        return result;
    }
    
    public void clearDeclarations() {
        if (TraceFlags.USE_REPOSITORY) {
            declarations.clear();
        } else {
            declarationsOLD.clear();
        }
    }
    
    
    public void write(DataOutput aStream) throws IOException {
        UIDObjectFactory.getDefaultFactory().writeStringToArrayUIDMap(declarations, aStream, true);
    }
    
    public void read(DataInput aStream) throws IOException {
        UIDObjectFactory.getDefaultFactory().readStringToArrayUIDMap(this.declarations, aStream, TextCache.getManager());
    }
    
    private boolean isSameFile(CsmUID<CsmOffsetableDeclaration> uid1, CsmUID<CsmOffsetableDeclaration> uid2){
        CsmOffsetableDeclaration decl1 = uid1.getObject();
        CsmOffsetableDeclaration decl2 = uid2.getObject();
        if (decl1 != null && decl2 != null){
            return decl1.getContainingFile() == decl2.getContainingFile();
        }
        // assert?
        return false;
    }
    
    private boolean isSameFile(CsmUID<CsmOffsetableDeclaration> uid1, CsmOffsetableDeclaration decl2){
        CsmOffsetableDeclaration decl1 = uid1.getObject();
        if (decl1 != null && decl2 != null){
            return decl1.getContainingFile() == decl2.getContainingFile();
        }
        // assert?
        return false;
    }
    
    private boolean isSameFile(CsmOffsetableDeclaration decl1, CsmOffsetableDeclaration decl2){
        if (decl1 != null && decl2 != null){
            return decl1.getContainingFile() == decl2.getContainingFile();
        }
        // assert?
        return false;
    }
}
