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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.mobility.end2end.util;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.mobility.e2e.classdata.ClassData;
import org.netbeans.modules.mobility.e2e.classdata.ClassDataRegistry;
import org.netbeans.modules.mobility.e2e.classdata.MethodData;
import org.netbeans.modules.mobility.end2end.classdata.TypeData;
import org.netbeans.modules.mobility.end2end.client.config.Configuration;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.WeakHashMap;
import org.netbeans.modules.mobility.e2e.classdata.MethodParameter;
import org.netbeans.modules.mobility.end2end.classdata.OperationData;
import org.netbeans.modules.mobility.end2end.ui.treeview.MethodCheckedTreeBeanView;
import org.netbeans.modules.mobility.end2end.ui.treeview.MultiStateCheckBox;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Adam
 */

public class ServiceNodeManager {

    static final String PACKAGE_ICON = "org/netbeans/spi/java/project/support/ui/packageBadge.gif"; //NOI18N
    static final String CLASS_ICON = "org/netbeans/spi/java/project/support/ui/packageBadge.gif"; //NOI18N
    static final String METHOD_ICON = "org/netbeans/spi/java/project/support/ui/packageBadge.gif"; //NIOI18N
    public final static String NODE_VALIDITY_ATTRIBUTE = "isValid"; //NOI18N
    public final static String NODE_SELECTION_ATTRIBUTE = "isSelected"; //NOI18N
    private static final WeakHashMap<MethodCheckedTreeBeanView, ProjectChildren> oldNodes = 
        new WeakHashMap<MethodCheckedTreeBeanView, ProjectChildren>();

    public static Node getRootNode(Configuration cfg, MethodCheckedTreeBeanView tree) {
        synchronized( oldNodes ) {
            ProjectChildren ch = oldNodes.get( tree );
            if( ch != null ) {
                ch.removeNotify();
            }            
        }    
        
        ProjectChildren ch = new ProjectChildren(cfg, tree);
        synchronized( oldNodes ) {
            oldNodes.put( tree, ch );
        }
        return new AbstractNode( ch );
    }

    private static String getActiveProfile() {
        return ClassDataRegistry.DEFAULT_PROFILE;
    }


    private static class ProjectChildren extends Children.Keys<String> implements ChangeListener, PropertyChangeListener, FileChangeListener, Runnable {

        private final Configuration cfg;
        private final MethodCheckedTreeBeanView tree;
        private ClassDataRegistry activeProfileRegistry, allRegistry;
        private ChangeListener ref1;
        private final HashMap<Object, Object> hookedListeners = new HashMap(); // FileObject or SourceGroup -> listener
        private final Task refreshTask = RequestProcessor.getDefault().create(this);
        private final HashSet<String> selectionSource = new HashSet(); 
       
        public ProjectChildren(Configuration cfg, MethodCheckedTreeBeanView tree) {
            this.cfg = cfg;
            this.tree = tree;
        }

        private Sources getSources() {
            return ProjectUtils.getSources(Util.getServerProject(cfg));
        }

        private volatile boolean running = false;
        @Override
        protected void addNotify() {
            if (!running) {
                run();
            }
            Sources sources = getSources();
            ref1 = WeakListeners.change(this, sources);
            sources.addChangeListener(ref1);
        }

        @Override
        protected synchronized void removeNotify() {
            getSources().removeChangeListener(ref1);
            synchronized (hookedListeners) {
                removeListeners();
            }
            refreshTask.cancel();
        }

        private void removeListeners() {
            for (java.util.Map.Entry en : hookedListeners.entrySet()) {
                Object o = en.getKey();
                if (o instanceof SourceGroup) ((SourceGroup)o).removePropertyChangeListener((PropertyChangeListener)en.getValue());
                else ((FileObject)o).removeFileChangeListener((FileChangeListener)en.getValue());
            }
            hookedListeners.clear();
        }

        private void enqueue() {
            if (!running) {
                refreshTask.schedule(200);
            }
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            enqueue();
        }

        public void stateChanged(ChangeEvent e) {
            enqueue();
        }
        public void fileFolderCreated(FileEvent fe) {
            enqueue();
        }

        public void fileDataCreated(FileEvent fe) {
            enqueue();
        }

        public void fileChanged(FileEvent fe) {
            enqueue();
        }

        public void fileDeleted(FileEvent fe) {
            enqueue();
        }

        public void fileRenamed(FileRenameEvent fe) {
            enqueue();
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
            
        public void run() {
            running = true;
            try {
            Sources sources = getSources();
            SourceGroup[] groups = sources.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA );
            // Add all paths to the ClasspathInfo structure
            List<ClasspathInfo> classpaths = new ArrayList();
            HashMap<Object, Object> newHooks = new HashMap();
            for (SourceGroup sg : sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                if (!sg.getName().equals("${test.src.dir}")) {
                    classpaths.add(ClasspathInfo.create(sg.getRootFolder())); //NOI18N
                    synchronized (hookedListeners) {
                        PropertyChangeListener l = (PropertyChangeListener)hookedListeners.get(sg);
                        if (l == null) {
                            l = WeakListeners.propertyChange(this, sg);
                            sg.addPropertyChangeListener(l);
                            hookedListeners.put(sg, l);
                        }
                        newHooks.put(sg, l);
                    }
                    FileObject root = sg.getRootFolder();
                    addFCListener(root, newHooks);
                    Enumeration<? extends FileObject> en = root.getChildren(true);
                    while(en.hasMoreElements()) {
                        FileObject fo = en.nextElement();
                        if (fo.isFolder() || fo.getExt().equals("java")) addFCListener(fo, newHooks); //NOI18N
                    }
                }
            }
            synchronized (hookedListeners) {
                hookedListeners.keySet().removeAll(newHooks.keySet());
                removeListeners();
                hookedListeners.putAll(newHooks);
            }
            // Get the registry for all available classes
            allRegistry = ClassDataRegistry.getRegistry( ClassDataRegistry.ALL_JAVA_PROFILE, classpaths );
            activeProfileRegistry = ClassDataRegistry.getRegistry( getActiveProfile(), classpaths);
            synchronized (selectionSource) {
                selectionSource.clear();
                List<org.netbeans.modules.mobility.end2end.classdata.ClassData> data = cfg.getServices().get(0).getData();
                if (data != null) for (org.netbeans.modules.mobility.end2end.classdata.ClassData cd : data) {
                    String fqn = cd.getPackageName();
                    if (fqn.length() > 0) fqn = fqn + '.';
                    fqn = fqn + cd.getClassName();
                    for (OperationData od : cd.getOperations()) {
                        StringBuffer sb = new StringBuffer(fqn);
                        sb.append('.').append(od.getName());
                        for(TypeData td : od.getParameterTypes()) {
                            sb.append(',').append(td.getType());
                        }
                        selectionSource.add(sb.toString());
                    }
                }
            }
            String packages[] = allRegistry.getBasePackages().toArray(new String[0]);
            Arrays.sort(packages);
            setKeys(packages);
            for (Node n : getNodes()) ((PackageChildren)n.getChildren()).notifyChange();
            tree.updateTreeNodeStates(null);
            } finally {
                running = false;
            }
        }
        
        private void addFCListener(FileObject fo, HashMap<Object, Object> newHooks) {
            synchronized (hookedListeners) {
                FileChangeListener l = (FileChangeListener)hookedListeners.get(fo); 
                if (l == null) {
                    l = FileUtil.weakFileChangeListener(this, fo);
                    fo.addFileChangeListener(l);
                    hookedListeners.put(fo, l);
                }
                newHooks.put(fo, l);
            }
        }
        
        protected Node[] createNodes(String packageName) {
            AbstractNode n = new AbstractNode(new PackageChildren(packageName));
            n.setName(packageName);
            n.setDisplayName(packageName.length() == 0 ? NbBundle.getMessage(ServiceNodeManager.class, "LBL_DefaultPackage") : packageName); //NOI18N
            n.setIconBaseWithExtension(PACKAGE_ICON);
            n.setValue(NODE_VALIDITY_ATTRIBUTE, activeProfileRegistry.getBasePackages().contains(packageName));
            return new Node[] {n};
        }
  
        private class PackageChildren extends Children.Keys<ClassData> {

            private final String packageName;
            
            public PackageChildren(String packageName) {
                this.packageName = packageName;
                notifyChange();
            }

            @Override
            protected void addNotify() {
                super.addNotify();
                notifyChange();
            }

            @Override
            protected void removeNotify() {
                setKeys (Collections.EMPTY_LIST);
            }

            public void notifyChange() {
                ClassData cd[] = allRegistry.getBaseClassesForPackage(packageName).toArray(new ClassData[0]);
                Arrays.sort(cd, new Comparator<ClassData>() {
                    public int compare(ClassData o1, ClassData o2) {
                        return o1.getClassName().compareTo(o2.getClassName());
                    }
                });
                setKeys(cd);
                for (Node n : getNodes()) ((ClassChildren)n.getChildren()).notifyChange();
            }
            
            protected Node[] createNodes(ClassData classData) {
                AbstractNode n = new AbstractNode(new ClassChildren(classData), Lookups.singleton(classData));
                n.setName(classData.getName());
                StringBuffer nodeText = new StringBuffer();
                createDisplayName(nodeText, classData);
                n.setDisplayName(nodeText.toString());
                n.setIconBaseWithExtension(CLASS_ICON);
                n.setValue(NODE_VALIDITY_ATTRIBUTE, activeProfileRegistry.getClassData(classData.getFullyQualifiedName()) != null);
                return new Node[] {n};
            }
        }
        
        private void createDisplayName(StringBuffer sb, ClassData cl) {
            sb.append(cl.getName());
            List<ClassData> gTypes = cl.getParameterTypes();
            if (gTypes.size() > 0) {
                sb.append('<');
                boolean first = true;
                for (ClassData param : gTypes) {
                    if (first) first = false;
                    else sb.append(',');
                    createDisplayName(sb, param);
                }
                sb.append('>');
            }
        }
        
        private class ClassChildren extends Children.Keys<MethodData> {

            private ClassData classData;
            private final String fqn;
            
            public ClassChildren(ClassData classData) {
                this.classData = classData;
                this.fqn = classData.getFullyQualifiedName();
            }

            @Override
            protected void addNotify() {
                notifyChange();
            }

            @Override
            protected void removeNotify() {
                setKeys (Collections.EMPTY_SET);
            }
            
            public void notifyChange() {
                classData = allRegistry.getClassData(fqn);
                setKeys(classData == null ? Collections.EMPTY_LIST : classData.getMethods());
            }
            
            protected Node[] createNodes(MethodData methodData) {
                StringBuffer nodeText = new StringBuffer();
                createDisplayName(nodeText, methodData.getReturnType());
                nodeText.append(' ').append(methodData.getName()).append('(');
                boolean first = true;
                for (MethodParameter param : methodData.getParameters()) {
                    if (first) first = false;
                    else nodeText.append(',');
                    createDisplayName(nodeText, param.getType());
                    nodeText.append(' ').append(param.getName());
                }
                nodeText.append(')');
                AbstractNode n = new AbstractNode(Children.LEAF, Lookups.singleton(methodData));
                n.setName(methodData.getName());
                n.setDisplayName(nodeText.toString());
                n.setIconBaseWithExtension(METHOD_ICON);
                ClassData cd = activeProfileRegistry.getClassData(methodData.getParentClassName());
                n.setValue(NODE_VALIDITY_ATTRIBUTE, cd != null && cd.getMethods().contains(methodData));
                StringBuffer sb = new StringBuffer(methodData.getParentClassName());
                sb.append('.').append(methodData.getName());
                for (MethodParameter mp : methodData.getParameters()) {
                    sb.append(',').append(mp.getType().getFullyQualifiedName());
                }
                n.setValue(NODE_SELECTION_ATTRIBUTE, selectionSource.contains(sb.toString()) ? MultiStateCheckBox.State.SELECTED : MultiStateCheckBox.State.UNSELECTED);
                return new Node[] {n};
            }
        }

    }
}
