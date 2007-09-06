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
import java.util.List;
import org.netbeans.api.project.*;

import  org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.classview.model.ProjectNode;
import org.openide.nodes.Node;

/**
 *
 * @author Vladimir Kvashin
 */
/*package-local*/ class ClassViewModel {
    
    private static final boolean showLibs = Boolean.getBoolean("cnd.classview.sys-includes"); // NOI18N
    
    private ClassViewUpdater updater;
    private ChildrenUpdater childrenUpdater;
    
    public ClassViewModel() {
        updater = new ClassViewUpdater(this);
        childrenUpdater = new ChildrenUpdater();
        updater.start();
    }
    
    public RootNode getRoot() {
        if( root == null ) {
            root = createRoot();
        }
        return root;
    }
    
    private RootNode createRoot() {
        return new RootNode(childrenUpdater);
    }
    
    public static boolean isShowLibs(){
        return showLibs;
    }
    
    /*package local*/ void openProject(CsmProject project){
        if( root == null ) { // paranoya
            root = createRoot();
            //return;
        }
        if (!isShowLibs() && project.isArtificial()){
            return;
        }
        ProjectsKeyArray children = (ProjectsKeyArray)root.getChildren();
        children.openProject(project);
    }
    
    /*package local*/ void closeProject(CsmProject project){
        if( root == null ) { // paranoya
            return;
        }
        childrenUpdater.unregister(project);
        ProjectsKeyArray children = (ProjectsKeyArray)root.getChildren();
        children.closeProject(project);
    }

    /*package local*/ void scheduleUpdate(CsmChangeEvent e) {
        updater.scheduleUpdate(e);
    }
    
    private volatile boolean userActivity = false;
    /*package local*/ void setUserActivity(boolean active){
        userActivity = active;
    }
    /*package local*/ boolean isUserActivity(){
        return userActivity;
    }
    
    /*package local*/ void dispose() {
        if( Diagnostic.DEBUG ) Diagnostic.trace("ClassesM: Dispose model"); // NOI18N
        updater.setStop();
        childrenUpdater.unregister();
        if (root !=null){
            root.destroy();
            root = null;
        }
        updater = null;
        childrenUpdater = null;
    }
    
    /*package local*/ void update(final SmartChangeEvent e) {
        if (childrenUpdater != null) {
            childrenUpdater.update(e);
        }
    }

    /*package local*/ Node findDeclaration(CsmOffsetableDeclaration decl) {
        if (root == null) {
            return null;
        }
        ProjectsKeyArray children = (ProjectsKeyArray)root.getChildren();
        CsmFile file = decl.getContainingFile();
        CsmProject project = file.getProject();
        children.ensureAddNotify();
        ProjectNode projectNode = (ProjectNode) children.findChild(project.getName());
        if (projectNode == null) {
            return null;
        }
        List<CsmObject> path = new ArrayList<CsmObject>();
        CsmObject scope = null;
        if (CsmKindUtilities.isFunctionDefinition(decl)){
            CsmFunction func = ((CsmFunctionDefinition)decl).getDeclaration();
            if (func != null){
                decl = func;
            }
            path.add(decl);
            scope = decl.getScope();
        } else if (CsmKindUtilities.isNamespaceDefinition(decl)){
            CsmNamespace ns = ((CsmNamespaceDefinition)decl).getNamespace();
            path.add(ns);
            scope = ns.getParent();
        } else {
            path.add(decl);
            scope = decl.getScope();
        }
        while(scope != null) {
            if (CsmKindUtilities.isFile(scope)) {
                path.add(project.getGlobalNamespace());
                break;
            }
            path.add(scope);
            if (CsmKindUtilities.isNamespace(scope)) {
                CsmNamespace ns = (CsmNamespace)scope;
                if (ns.isGlobal()){
                    break;
                }
                scope = ns.getParent();
            } else if (CsmKindUtilities.isClass(scope)) {
                CsmClass cls = (CsmClass)scope;
                scope = cls.getScope();
            } else {
                break;
            }
        }
        Node res = null;
        HostKeyArray child = (HostKeyArray) projectNode.getChildren();
        for (int i = path.size() - 2; i >= 0; i--){
            child.ensureInited();
            scope = path.get(i);
            res = child.findChild(scope);
            if (res != null && (res.getChildren() instanceof HostKeyArray)) {
                child = (HostKeyArray) res.getChildren();
            }
        }
        return res;
    }
    
    private void dump(Project[] projects) {
        if( Diagnostic.DEBUG ) {
            Diagnostic.trace("Dumping projects:"); // NOI18N
            for( int i = 0; i < projects.length; i++ ) {
                dump(projects[i]);
            }
        }
    }
    
    private void dump(Project p) {
        if( Diagnostic.DEBUG ) {
            ProjectInformation pi = ProjectUtils.getInformation(p);
            Diagnostic.trace("Project " + pi.getName() + " (" + pi.getDisplayName() + ')'); // NOI18N
            SourceGroup[] sg = ProjectUtils.getSources(p).getSourceGroups(Sources.TYPE_GENERIC);
            Diagnostic.trace("  Source groups are"); // NOI18N
            for( int i = 0; i < sg.length; i++ ) {
                Diagnostic.trace("    " + sg[i].getName() + " (" + sg[i].getDisplayName() + ") " + sg[i].getRootFolder().getName()); // NOI18N
            }
        }
    }
    
    private RootNode root;
    
}
