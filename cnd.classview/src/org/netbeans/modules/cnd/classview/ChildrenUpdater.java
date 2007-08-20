/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.modules.cnd.classview;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmCompoundClassifier;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;

/**
 *
 * @author Alexander Simon
 */
public class ChildrenUpdater {
    private static final boolean traceEvents = Boolean.getBoolean("cnd.classview.key-events"); // NOI18N
    private Map<CsmProject, Map<PersistentKey, UpdatebleHost>> map =
            new HashMap<CsmProject, Map<PersistentKey, UpdatebleHost>>();
    
    public ChildrenUpdater() {
    }
    
    public Object getLock(CsmProject project){
        Object lock = map.get(project);
        if (lock == null) {
            return this;
        }
        return lock;
    }
    
    public void register(CsmProject project, PersistentKey host, UpdatebleHost children){
        Map<PersistentKey, UpdatebleHost> p = map.get(project);
        if (p == null){
            p = new HashMap<PersistentKey, UpdatebleHost>();
            map.put(project,p);
        }
        p.put(host,children);
        if (traceEvents) {
            System.out.println("Register Children Updater on key "+host.toString()); // NOI18N
        }
    }
    
    public void unregister(){
        if (traceEvents) {
            System.out.println("Clean Children Updater"); // NOI18N
        }
        map.clear();
        NameCache.dispose();
    }
    
    public void unregister(CsmProject project){
        if (traceEvents) {
            System.out.println("Clean Children Updater on project "+project.getName()); // NOI18N
        }
        map.remove(project);
    }
    
    public void unregister(CsmProject project, PersistentKey host){
        Map<PersistentKey, UpdatebleHost> p = map.get(project);
        if (p != null){
            if (traceEvents) {
                System.out.println("Clean Children Updater on key "+host.toString()); // NOI18N
            }
            p.remove(host);
        }
    }
    public void update(SmartChangeEvent e){
        if (map.size() == 0) {
            return;
        }
        for (Map.Entry<CsmProject,SmartChangeEvent.Storage> entry : e.getChangedProjects().entrySet()){
            CsmProject project = entry.getKey();
            if (map.containsKey(project) && project.isValid()) {
                synchronized (getLock(project)) {
                    SmartChangeEvent.Storage storage = entry.getValue();
                    update(project, storage);
                }
            }
        }
    }
    
    public void update(CsmProject project, SmartChangeEvent.Storage e){
        Set<UpdatebleHost> toFlush = new HashSet<UpdatebleHost>();
        for(CsmNamespace ns : e.getNewNamespaces()){
            UpdatebleHost keys = findHost(project, ns);
            if (keys != null){
                if (keys.newNamespsce(ns)){
                    toFlush.add(keys);
                }
            }
        }
        for(CsmNamespace ns : e.getRemovedNamespaces()){
            UpdatebleHost keys = findHost(project, ns);
            if (keys != null){
                if (keys.removeNamespsce(ns)){
                    toFlush.add(keys);
                }
            }
        }
        for(CsmOffsetableDeclaration decl : e.getNewDeclarations()){
            for (UpdatebleHost keys : findHost(project, decl)){
                if (keys.newDeclaration(decl)){
                    toFlush.add(keys);
                }
            }
        }
        for(CsmOffsetableDeclaration decl : e.getRemovedDeclarations()){
            for (UpdatebleHost keys : findHost(project, decl)){
                if (keys.removeDeclaration(decl)){
                    toFlush.add(keys);
                }
            }
        }
        List<CsmOffsetableDeclaration> recursive = new ArrayList<CsmOffsetableDeclaration>();
        List<Map.Entry<CsmOffsetableDeclaration,CsmOffsetableDeclaration>> change =
                new ArrayList<Map.Entry<CsmOffsetableDeclaration,CsmOffsetableDeclaration>>(packChangedDeclarations(e.getChangedDeclarations()));
        for(Map.Entry<CsmOffsetableDeclaration,CsmOffsetableDeclaration>  decl : change){
            for (UpdatebleHost keys : findHost(project, decl.getKey())){
                if (keys.changeDeclaration(decl.getKey(),decl.getValue())){
                    toFlush.add(keys);
                }
            }
            UpdatebleHost keys = findNode(project, decl.getValue());
            if (keys != null){
                if (keys.reset(decl.getValue(),recursive)){
                    toFlush.add(keys);
                }
            }
        }
        while(recursive.size()>0){
            List<CsmOffsetableDeclaration> list = new ArrayList<CsmOffsetableDeclaration>(recursive);
            recursive.clear();
            for(CsmOffsetableDeclaration decl : list){
                UpdatebleHost keys = findNode(project, decl);
                if (keys != null){
                    if (keys.reset(decl,recursive)){
                        toFlush.add(keys);
                    }
                }
            }
        }
        if (toFlush.size() > 0) {
            for (UpdatebleHost keys : toFlush){
                keys.flush();
            }
        }
    }
    
    private Collection<Map.Entry<CsmOffsetableDeclaration,CsmOffsetableDeclaration>> packChangedDeclarations(Map<CsmOffsetableDeclaration,CsmOffsetableDeclaration> changed){
        Map<PersistentKey,Map.Entry<CsmOffsetableDeclaration,CsmOffsetableDeclaration>> packed =
                new HashMap<PersistentKey,Map.Entry<CsmOffsetableDeclaration,CsmOffsetableDeclaration>>();
        for(Map.Entry<CsmOffsetableDeclaration,CsmOffsetableDeclaration>  decl : changed.entrySet()){
            packed.put(PersistentKey.createKey(decl.getKey()),decl);
        }
        return packed.values();
    }
    
    private UpdatebleHost findHost(CsmProject project, CsmNamespace ns){
        if (!project.isValid()){
            return null;
        }
        Map<PersistentKey, UpdatebleHost> hosts = map.get(project);
        if (hosts == null){
            return null;
        }
        CsmNamespace parent = ns.getParent();
        if (parent != null){
            return hosts.get(PersistentKey.createKey(parent));
        }
        return null;
    }
    
    private UpdatebleHost findNode(CsmProject project, CsmOffsetableDeclaration decl){
        if (!project.isValid()){
            return null;
        }
        Map<PersistentKey, UpdatebleHost> hosts = map.get(project);
        if (hosts == null){
            return null;
        }
        if (CsmKindUtilities.isClass(decl)){
            CsmClass cls = (CsmClass)decl;
            if (cls.isValid()) {
                CsmFile file = cls.getContainingFile();
                if (file != null && file.isValid()) {
                    return hosts.get(PersistentKey.createKey(cls));
                }
            }
        } else if(CsmKindUtilities.isEnum(decl)){
            CsmEnum cls = (CsmEnum)decl;
            if (cls.isValid()) {
                CsmFile file = cls.getContainingFile();
                if (file != null && file.isValid()) {
                    return hosts.get(PersistentKey.createKey(cls));
                }
            }
        } else if(CsmKindUtilities.isTypedef(decl)){
            CsmTypedef def = (CsmTypedef)decl;
            if (def.isTypeUnnamed()) {
                CsmClassifier classifier = def.getType().getClassifier();
                if (classifier instanceof CsmCompoundClassifier) {
                    CsmCompoundClassifier cls = (CsmCompoundClassifier)classifier;
                    if (cls.isValid() && cls.getName().length()==0) {
                        CsmFile file = cls.getContainingFile();
                        if (file != null && file.isValid()) {
                            return hosts.get(PersistentKey.createKey(def));
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private List<UpdatebleHost> findHost(CsmProject project, CsmOffsetableDeclaration decl){
        List<UpdatebleHost> res = new ArrayList<UpdatebleHost>();
        if (!project.isValid()){
            return res;
        }
        Map<PersistentKey, UpdatebleHost> hosts = map.get(project);
        if (hosts == null){
            return res;
        }
        if (CsmKindUtilities.isFriendMethod(decl)){
            CsmClass cls = ((CsmFriendFunction)decl).getContainingClass();
            if (cls != null && cls.isValid()) {
                CsmFile file = cls.getContainingFile();
                if (file != null && file.isValid()) {
                    UpdatebleHost host = hosts.get(PersistentKey.createKey(cls));
                    if (host != null) {
                        res.add(host);
                    }
                }
            }
        }
        CsmScope scope = decl.getScope();
        if (CsmKindUtilities.isClass(scope)){
            CsmClass cls = (CsmClass)scope;
            if (cls.isValid()) {
                CsmFile file = cls.getContainingFile();
                if (file != null && file.isValid()) {
                    UpdatebleHost host = hosts.get(PersistentKey.createKey(cls));
                    if (host != null) {
                        res.add(host);
                    }
                }
            }
        } else if(CsmKindUtilities.isEnum(scope)){
            CsmEnum cls = (CsmEnum)scope;
            if (cls.isValid()) {
                CsmFile file = cls.getContainingFile();
                if (file != null && file.isValid()) {
                    UpdatebleHost host = hosts.get(PersistentKey.createKey(cls));
                    if (host != null) {
                        res.add(host);
                    }
                }
            }
        } else if (CsmKindUtilities.isNamespace(scope)){
            CsmNamespace cls = (CsmNamespace)scope;
            UpdatebleHost host = hosts.get(PersistentKey.createKey(cls));
            if (host != null) {
                res.add(host);
            }
        } else if (CsmKindUtilities.isNamespaceDefinition(scope)){
            CsmNamespaceDefinition cls = (CsmNamespaceDefinition)scope;
            CsmFile file = cls.getContainingFile();
            if (file != null && file.isValid()) {
                UpdatebleHost host = hosts.get(PersistentKey.createKey(cls.getNamespace()));
                if (host != null) {
                    res.add(host);
                }
            }
        } else if (CsmKindUtilities.isFile(scope)){
            CsmFile cls = (CsmFile)scope;
            if (cls.isValid()) {
                UpdatebleHost host = hosts.get(PersistentKey.createKey(project.getGlobalNamespace()));
                if (host != null) {
                    res.add(host);
                }
            }
        }
        return res;
    }
}
