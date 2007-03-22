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

package org.netbeans.modules.cnd.classview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.classview.model.CVUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Alexander Simon
 */
abstract public class HostKeyArray extends Children.Keys implements UpdatebleHost{
    private static final boolean traceEvents = Boolean.getBoolean("cnd.classview.key-events"); // NOI18N
    private static Comparator<java.util.Map.Entry<PersistentKey, SortedName>> COMARATOR = new MyComparator();
    
    private ChildrenUpdater childrenUpdater;
    private CsmProject myProject;
    private PersistentKey myID;
    private boolean update;
    private java.util.Map<PersistentKey,SortedName> myKeys ;
    private java.util.Map<PersistentKey,ChangeListener> myChanges;
    private boolean isInited = false;
    
    public HostKeyArray(ChildrenUpdater childrenUpdater, CsmProject project, PersistentKey id) {
        this.childrenUpdater = childrenUpdater;
        this.myProject = project;
        this.myID = id;
        childrenUpdater.register(project,id,this);
    }

    protected ChildrenUpdater getUpdater(){
        return childrenUpdater;
    }
    
    public void dispose(){
        if (isInited) {
            isInited = false;
            myKeys.clear();
            myChanges.clear();
            childrenUpdater.unregister(myProject, myID);
            setKeys(new Object[0]);
        }
    }

    private synchronized void resetKeys(){
        List<java.util.Map.Entry<PersistentKey,SortedName>> list =
                new ArrayList<java.util.Map.Entry<PersistentKey,SortedName>>();
        if (myKeys != null){
            list.addAll(myKeys.entrySet());
        }
        Collections.sort(list, COMARATOR);
        List<PersistentKey> res = new ArrayList<PersistentKey>();
        for(java.util.Map.Entry<PersistentKey,SortedName> entry :list){
            PersistentKey key = entry.getKey();
            res.add(key);
        }
        setKeys(res);
    }
    
    abstract protected java.util.Map<PersistentKey,SortedName> getMembers();
    abstract protected CsmDeclaration findDeclaration(PersistentKey key);
    abstract protected boolean canCreateNode(CsmDeclaration d);
    abstract protected Node createNode(PersistentKey key);
    
    protected SortedName getSortedName(CsmNamespace ns){
        return new SortedName(0,CVUtil.getNamesapceDisplayName(ns),0);
    }
    
    protected SortedName getSortedName(CsmDeclaration d){
        if( CsmKindUtilities.isClass(d) ) {
            return new SortedName(1,d.getName(),0);
        } else if( d.getKind() == CsmDeclaration.Kind.ENUM ) {
            return new SortedName(1,d.getName(),1);
        } else if( d.getKind() == CsmDeclaration.Kind.TYPEDEF ) {
            return new SortedName(1,d.getName(),2);
        } else if( d.getKind() == CsmDeclaration.Kind.VARIABLE ) {
            return new SortedName(2,d.getName(),0);
        } else if( d.getKind() == CsmDeclaration.Kind.FUNCTION ) {
            return new SortedName(3,CVUtil.getSignature((CsmFunction)d),0);
        } else if( d.getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ) {
            return new SortedName(3,CVUtil.getSignature((CsmFunction)d),1);
        }
        return new SortedName(9,d.getName(),0);
    }
    
    protected CsmProject getProject(){
        return myProject;
    }
    
    protected PersistentKey getHostId(){
        return myID;
    }
    
    public boolean newNamespsce(CsmNamespace ns) {
        if (!isInited){
            return false;
        }
        PersistentKey key = PersistentKey.createKey(ns);
        myKeys.put(key,getSortedName(ns));
        update = true;
        return true;
    }
    
    public boolean removeNamespsce(CsmNamespace ns) {
        if (!isInited){
            return false;
        }
        PersistentKey key = PersistentKey.createKey(ns);
        myKeys.remove(key);
        childrenUpdater.unregister(myProject,key);
        update = true;
        return true;
    }
    
    public boolean newDeclaration(CsmDeclaration decl) {
        if (!isInited){
            return false;
        }
        if (CsmKindUtilities.isFunctionDefinition(decl)) {
            CsmFunctionDefinition def = (CsmFunctionDefinition) decl;
            CsmFunction fun = def.getDeclaration();
            if (fun != null && fun != decl){
                PersistentKey funKey = PersistentKey.createKey(fun);
                if (myKeys.containsKey(funKey)) {
                    return false;
                }
                decl= fun;
            }
        }
        PersistentKey key = PersistentKey.createKey(decl);
        myKeys.put(key,getSortedName(decl));
        myChanges.remove(key);
        update = true;
        return true;
    }
    
    public boolean removeDeclaration(CsmDeclaration decl) {
        if (!isInited){
            return false;
        }
        PersistentKey key = PersistentKey.createKey(decl);
        myKeys.remove(key);
        myChanges.remove(key);
        childrenUpdater.unregister(myProject,key);
        update = true;
        return true;
    }
    
    public boolean changeDeclaration(CsmDeclaration oldDecl,CsmDeclaration newDecl) {
        if (!isInited){
            return false;
        }
        PersistentKey oldKey = PersistentKey.createKey(oldDecl);
        if (newDecl == null) {
            // remove non-existent element
            myKeys.remove(oldKey);
            myChanges.remove(oldKey);
            childrenUpdater.unregister(myProject,oldKey);
            update = true;
            return true;
        }
        PersistentKey newKey = PersistentKey.createKey(newDecl);
        if (oldKey.equals(newKey)) {
            myKeys.put(newKey,getSortedName(newDecl));
            ChangeListener l = myChanges.get(newKey);
            if (l != null) {
                l.stateChanged(new ChangeEvent(newDecl));
            }
            return false;
        } else {
            myKeys.remove(oldKey);
            myChanges.remove(oldKey);
            childrenUpdater.unregister(myProject,oldKey);
            myKeys.put(newKey,getSortedName(newDecl));
            myChanges.remove(newKey);
            update = true;
            return true;
        }
    }
    
    public boolean reset(CsmDeclaration decl, List<CsmDeclaration> recursive){
        myID = PersistentKey.createKey(decl);
        if (!isInited){
            return false;
        }
        boolean needUpdate = false;
        java.util.Map<PersistentKey,SortedName> members = getMembers();
        List<PersistentKey> toDelete = null;
        for(PersistentKey key : myKeys.keySet()){
            if (!members.containsKey(key)){
                // delete
                if (toDelete == null) {
                    toDelete = new ArrayList<PersistentKey>();
                }
                toDelete.add(key);
            }
        }
        if (toDelete != null){
            for(PersistentKey key : toDelete){
                myKeys.remove(key);
                myChanges.remove(key);
                needUpdate = true;
            }
        }
        for(PersistentKey key : members.keySet()){
            if (myKeys.containsKey(key)){
                // update
                myKeys.put(key,members.get(key));
                CsmDeclaration what = findDeclaration(key);
                if (what == null) {
                    // remove non-existent element
                    myKeys.remove(key);
                    myChanges.remove(key);
                    needUpdate = true;
                } else {
                    ChangeListener l = myChanges.get(key);
                    if (l != null) {
                        l.stateChanged(new ChangeEvent(what));
                    }
                    if (CsmKindUtilities.isClassifier(what)||
                            CsmKindUtilities.isEnum(what)){
                        recursive.add(what);
                    }
                }
            } else {
                // new
                myKeys.put(key,members.get(key));
                myChanges.remove(key);
                needUpdate = true;
            }
        }
        if (needUpdate) {
            update = true;
            return true;
        }
        return false;
    }
    
    public void flush() {
        if (update &&  isInited){
            resetKeys();
        }
        update = false;
    }
    
    protected Node[] createNodes(Object object) {
        Node node = null;
        if (object instanceof PersistentKey){
            node = createNode((PersistentKey)object);
        }
        if (node != null) {
            if (node instanceof ChangeListener){
                myChanges.put((PersistentKey)object,(ChangeListener)node);
            }
            return new Node[]{node};
        }
        return new Node[0];
    }
    
    protected void addNotify() {
        isInited = true;
        myKeys = getMembers();
        myChanges = new HashMap<PersistentKey,ChangeListener>();
        isInited = true;
        resetKeys();
        super.addNotify();
    }
    
    protected void removeNotify() {
        super.removeNotify();
        isInited = false;
        myKeys.clear();
        myChanges.clear();
        childrenUpdater.unregister(myProject, myID);
        if (traceEvents) {
            System.out.println("Remove key "+myID.toString()); // NOI18N
        }
    }

    protected void destroyNodes(Node[] node) {
        for (Node n : node){
            Children children = n.getChildren();
            if (children instanceof HostKeyArray){
                ((HostKeyArray)children).dispose();
            }
        }
        super.destroyNodes(node);
        if (traceEvents) {
            System.out.println("Destroy nodes "+node.length+" in "+myID.toString()); // NOI18N
        }
    }
    
    protected void onPprojectParsingFinished(CsmProject project) {
        if (!isInited || project != getProject()){
            return;
        }
        PersistentKey key = PersistentKey.createKey(project);
        if (myKeys.containsKey(key)){
            myKeys.remove(key);
            resetKeys();
        }
    }
    
    private static class MyComparator implements Comparator<java.util.Map.Entry<PersistentKey, SortedName>> {
        public int compare(java.util.Map.Entry<PersistentKey, SortedName> o1, java.util.Map.Entry<PersistentKey, SortedName> o2) {
            SortedName n1 = o1.getValue();
            SortedName n2 = o2.getValue();
            int res = n1.compareTo(n2);
            if (res != 0) {
                return res;
            }
            String s1 = o1.getKey().toString();
            String s2 = o2.getKey().toString();
            return s1.compareTo(s2);
        }
    }
}
