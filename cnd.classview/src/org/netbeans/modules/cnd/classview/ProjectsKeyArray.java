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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.classview.model.ProjectNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Alexander Simon
 */
public class ProjectsKeyArray extends Children.Keys<CsmProject> {
    private java.util.Map<CsmProject,SortedName> myProjects;
    private ChildrenUpdater childrenUpdater;
    private static Comparator<java.util.Map.Entry<CsmProject, SortedName>> COMARATOR = new ProjectComparator();
    
    public ProjectsKeyArray(ChildrenUpdater childrenUpdater){
        this.childrenUpdater = childrenUpdater;
    }

    private synchronized void resetKeys(){
        List<java.util.Map.Entry<CsmProject,SortedName>> list =
                new ArrayList<java.util.Map.Entry<CsmProject,SortedName>>(myProjects.entrySet());
        Collections.sort(list, COMARATOR);
        final List<CsmProject> res = new ArrayList<CsmProject>();
        for(java.util.Map.Entry<CsmProject,SortedName> entry :list){
            CsmProject key = entry.getKey();
            res.add(key);
        }
        setKeys(res);
    }
    
    public void dispose(){
        if (myProjects != null) {
            myProjects.clear();
        }
        childrenUpdater =null;
        setKeys(new CsmProject[0]);
    }
    
    private Set<CsmProject> getProjects(){
        Set<CsmProject> projects = new HashSet<CsmProject>();
        for (CsmProject p : CsmModelAccessor.getModel().projects()) {
            if (ClassViewModel.isShowLibs()) {
                for(CsmProject lib : p.getLibraries()) {
                    projects.add(lib);
                }
            }
            projects.add(p);
        }
        return projects;
    }
    
    private SortedName getSortedName(CsmProject project, boolean isLibrary){
        if (isLibrary){
            return new SortedName(1,project.getName(), 0);
        }
        return new SortedName(0,project.getName(), 0);
    }
    
    public boolean isEmpty(){
        if (myProjects != null) {
            return myProjects.size()==0;
        }
        return true;
    }
    
    public void openProject(CsmProject project){
        if (myProjects == null){
            return;
        }
        if (myProjects.containsKey(project)){
            return;
        }
        myProjects.put(project,getSortedName(project,false));
        resetKeys();
    }
    
    public void closeProject(CsmProject project){
        if (myProjects == null || myProjects.size() == 0){
            return;
        }
        if (!myProjects.containsKey(project)){
            return;
        }
        myProjects.remove(project);
        childrenUpdater.unregister(project);
        boolean removeAll = true;
        for(CsmProject p : myProjects.keySet()){
            SortedName name = myProjects.get(p);
            if (name.getPrefix()==0){
                removeAll = false;
            }
        }
        if (removeAll) {
            for(CsmProject p : myProjects.keySet()){
                childrenUpdater.unregister(p);
            }
            myProjects.clear();
        }
        resetKeys();
    }
    
    public void resetProjects(){
        Set<CsmProject> newProjects = getProjects();
        if (myProjects != null){
            for(CsmProject p : myProjects.keySet()){
                if (!newProjects.contains(p)){
                    childrenUpdater.unregister(p);
                }
            }
        }
        myProjects = createProjectsMap();
        for(CsmProject p : newProjects){
            myProjects.put(p,getSortedName(p,false));
        }
        resetKeys();
    }
    
    private java.util.Map<CsmProject, SortedName> createProjectsMap() {
	return new java.util.concurrent.ConcurrentHashMap<CsmProject,SortedName>();
    }
    
    protected Node[] createNodes(CsmProject project) {
        //System.out.println("Create project"); // NOI18N
        return new Node[] {new ProjectNode(project,
                new NamespaceKeyArray(childrenUpdater,project.getGlobalNamespace()))};
    }
    
    @Override
    protected void destroyNodes(Node[] node) {
        for (Node n : node){
            Children children = n.getChildren();
            if (children instanceof HostKeyArray){
                //System.out.println("Destroy project node "+n); // NOI18N
                ((HostKeyArray)children).dispose();
            }
        }
        super.destroyNodes(node);
    }
    
    void ensureAddNotify() {
        if (myProjects == null){
            addNotify();
        }
    }
    
    @Override
    protected void addNotify() {
        if( Diagnostic.DEBUG ) Diagnostic.trace("ClassesP: addNotify()"); // NOI18N
        resetProjects();
        super.addNotify();
    }
    
    @Override
    protected void removeNotify() {
        super.removeNotify();
        if (myProjects != null) {
            myProjects.clear();
            resetKeys();
        }
        myProjects = null;
    }
    
    private static final class ProjectComparator implements Comparator<java.util.Map.Entry<CsmProject,SortedName>> {
        public int compare(java.util.Map.Entry<CsmProject, SortedName> o1, java.util.Map.Entry<CsmProject, SortedName> o2) {
            return o1.getValue().compareTo(o2.getValue());
        }
    }
}