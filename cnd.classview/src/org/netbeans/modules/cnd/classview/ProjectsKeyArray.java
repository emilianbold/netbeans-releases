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

package org.netbeans.modules.cnd.classview;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.classview.model.CVUtil;
import org.netbeans.modules.cnd.classview.model.ProjectNode;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class ProjectsKeyArray extends Children.Keys<CsmProject> {

    private java.util.Map<CsmProject,SortedName> myProjects;
    private ChildrenUpdater childrenUpdater;
    private static Comparator<java.util.Map.Entry<CsmProject, SortedName>> COMARATOR = new ProjectComparator();    
    
    /** guards myProjects */
    private final Object myProjectsLock = new Object();
    
    private final CsmProject libOwnerProject;
    private static final RequestProcessor RP = new RequestProcessor(ProjectsKeyArray.class.getName(), 1);
    
    public ProjectsKeyArray(CsmProject libOwnerProject, ChildrenUpdater childrenUpdater){
        this.childrenUpdater = childrenUpdater;
        this.libOwnerProject= libOwnerProject;
    }

    private void resetKeys(){
        CndUtils.assertFalse(Thread.holdsLock(myProjectsLock), "resetKeys should never be caled under the lock"); //NOI18N
        final List<CsmProject> res = new ArrayList<CsmProject>();
        synchronized(myProjectsLock) {
            if (myProjects != null) {
                List<java.util.Map.Entry<CsmProject, SortedName>> list = new ArrayList<java.util.Map.Entry<CsmProject, SortedName>>(myProjects.entrySet());
                Collections.sort(list, COMARATOR);                
                for (java.util.Map.Entry<CsmProject, SortedName> entry : list) {
                    CsmProject key = entry.getKey();
                    res.add(key);
                }                
            }
        }
        setKeys(res);
    }

    public void projectLibsChanged(CsmProject owner) {
        if (owner == libOwnerProject) {
            synchronized(myProjectsLock) {
                if (myProjects == null) {
                    myProjects = createProjectsMap();
                } else {
                    myProjects.clear();
                }
                for (CsmProject p : owner.getLibraries()) {
                    myProjects.put(p, getSortedName(p, true));
                }
            }
            resetKeys();
        }
    }

    public void dispose(){
        synchronized(myProjectsLock) {
            if (myProjects != null) {
                myProjects.clear();
            }
        }
        childrenUpdater =null;
        setKeys(new CsmProject[0]);
    }
    
    private Set<CsmProject> getProjects(){
        Set<CsmProject> projects = new HashSet<CsmProject>();
        if (libOwnerProject == null) {
            for (CsmProject p : CsmModelAccessor.getModel().projects()) {
                projects.add(p);
            }
        } else {
            for(CsmProject lib : libOwnerProject.getLibraries()) {
                projects.add(lib);
            }
        }
        return projects;
    }
    
    private SortedName getSortedName(CsmProject project, boolean isLibrary){
        if (isLibrary){
            return new IgnoreCaseSortedName(1,project.getName(), 0);
        }
        return new IgnoreCaseSortedName(0,project.getName(), 0);
    }
    
    public boolean isEmpty(){
        synchronized(myProjectsLock) {
            return (myProjects == null) || myProjects.isEmpty();
        }
    }
    
    public void openProject(CsmProject project) {
        if (project.isArtificial()) {
            if (libOwnerProject == null) {
                return;
            } else if (!libOwnerProject.getLibraries().contains(project)) {
                return;
            }
        }
        synchronized(myProjectsLock) {
            if (myProjects == null) {
                return;
            }
            for(java.util.Map.Entry<CsmProject,SortedName> entry : myProjects.entrySet()) {
                if (entry.getKey() instanceof DummyProject) {
                    myProjects.clear();
                    break;
                }
            }
            if (myProjects.containsKey(project)) {
                return;
            }
            myProjects.put(project, getSortedName(project, false));
        }
        resetKeys();
    }
    
    public void closeProject(CsmProject project){
        synchronized(myProjectsLock) {
            if (myProjects == null || myProjects.isEmpty()){
                return;
            }
            if (!myProjects.containsKey(project)){
                return;
            }
            myProjects.remove(project);
            childrenUpdater.unregister(project);
            boolean removeAll = true;
            for (CsmProject p : myProjects.keySet()) {
                SortedName name = myProjects.get(p);
                if (name != null && name.getPrefix() == 0) {
                    removeAll = false;
                    break;
                }
            }
            if (removeAll) {
                for (CsmProject p : myProjects.keySet()) {
                    childrenUpdater.unregister(p);
                }
                myProjects.clear();
            }
        }
        resetKeys();
    }
    
    private void resetProjects(){
        synchronized(myProjectsLock) {
            if (myProjects != null) {
                for (CsmProject p : myProjects.keySet()) {
                    childrenUpdater.unregister(p);
                }
            }
            myProjects = createProjectsMap();
            myProjects.put(new DummyProject(), new SortedName(0, "", 0));
        }
        resetKeys();
        RP.post(new Runnable(){
            @Override
            public void run() {
                Set<CsmProject> newProjects = getProjects();
                synchronized(myProjectsLock) {
                    if (myProjects != null) {
                        for (CsmProject p : myProjects.keySet()) {
                            if (!newProjects.contains(p)) {
                                childrenUpdater.unregister(p);
                            }
                        }
                    }
                    myProjects = createProjectsMap();
                    for (CsmProject p : newProjects) {
                        if (p.isValid()) {
                            myProjects.put(p, getSortedName(p, false));
                        }
                    }
                }
                resetKeys();
            }
        });
    }
    
    private java.util.Map<CsmProject, SortedName> createProjectsMap() {
	return new ConcurrentHashMap<CsmProject,SortedName>();
    }
    
    @Override
    protected Node[] createNodes(CsmProject project) {
        //System.out.println("Create project"); // NOI18N
        Node node = null;
        try {
            if (project instanceof DummyProject) {
                node = CVUtil.createLoadingNode();
            } else {
                node = new ProjectNode(project, new NamespaceKeyArray(childrenUpdater,project));
            }
        } catch (AssertionError ex){
            ex.printStackTrace(System.err);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
        if (node != null) {
            return new Node[] {node};
        }
        return new Node[0];
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
        boolean nullProjects;
        synchronized (myProjectsLock) {
            nullProjects = (myProjects == null);
        }
        if (nullProjects){
            addNotify();
        }
    }
    
    @Override
    protected void addNotify() {
        if( Diagnostic.DEBUG ) {Diagnostic.trace("ClassesP: addNotify()");} // NOI18N
        resetProjects();
        super.addNotify();
    }
    
    @Override
    protected void removeNotify() {
        super.removeNotify();
        synchronized(myProjectsLock) {
            if (myProjects != null) {
                myProjects.clear();                
            }
            myProjects = null;
        }
        resetKeys();
    }
    
    private static class IgnoreCaseSortedName extends SortedName {
        
        public IgnoreCaseSortedName(int prefix, CharSequence name, int suffix) {
            super(prefix, name, suffix);
        }

        @Override
        protected Comparator<CharSequence> getCharSequenceComparator() {
            return CharSequenceUtils.ComparatorIgnoreCase;
        }
    }
    
    private static final class ProjectComparator implements Comparator<java.util.Map.Entry<CsmProject,SortedName>>, Serializable {
        @Override
        public int compare(java.util.Map.Entry<CsmProject, SortedName> o1, java.util.Map.Entry<CsmProject, SortedName> o2) {
            if (o1.getKey().isArtificial() != o2.getKey().isArtificial()){
                return o1.getKey().isArtificial()?1:-1;
            }
            return o1.getValue().compareTo(o2.getValue());
        }
    }
    
    private static final class DummyProject implements CsmProject {

        @Override
        public CsmNamespace getGlobalNamespace() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void waitParse() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getPlatformProject() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getDisplayName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public FileSystem getFileSystem() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getHtmlDisplayName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CsmNamespace findNamespace(CharSequence qualifiedName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CsmClassifier findClassifier(CharSequence qualifiedName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<CsmClassifier> findClassifiers(CharSequence qualifiedName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<CsmInheritance> findInheritances(CharSequence name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CsmDeclaration findDeclaration(CharSequence uniqueName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<CsmOffsetableDeclaration> findDeclarations(CharSequence uniqueName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CsmFile findFile(Object absolutePathOrNativeFileItem, boolean createIfPossible, boolean snapShot) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<CsmFile> getSourceFiles() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<CsmFile> getHeaderFiles() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<CsmFile> getAllFiles() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<CsmProject> getLibraries() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isStable(CsmFile skipFile) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isArtificial() {
            return false;
        }

        @Override
        public CharSequence getName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isValid() {
            throw new UnsupportedOperationException();
        }
    }
}
