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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.mobility.end2end.util;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.mobility.e2e.classdata.ClassData;
import org.netbeans.modules.mobility.e2e.classdata.ClassDataRegistry;
import org.netbeans.modules.mobility.e2e.classdata.MethodData;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.mobility.e2e.classdata.MethodParameter;
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

    public static Node getRootNode( final Project project ) {
        return new AbstractNode(new ProjectChildren(project));
    }

    private static String getActiveProfile() {
        return ClassDataRegistry.DEFAULT_PROFILE;
    }


    private static class ProjectChildren extends Children.Keys<String> implements ChangeListener, PropertyChangeListener, FileChangeListener, Runnable {

        private final Sources s;
        private ClassDataRegistry activeProfileRegistry, allRegistry;
        private ChangeListener ref1;
        private final HashMap<Object, Object> hookedListeners = new HashMap(); // FileObject or SourceGroup -> listener
        private final Task refreshTask = RequestProcessor.getDefault().create(this);
       
        public ProjectChildren(Project p) {
            s = ProjectUtils.getSources(p);
            run();
        }

        protected void addNotify() {
            ref1 = WeakListeners.change(this, s);
            s.addChangeListener(ref1);
        }

        protected synchronized void removeNotify() {
            s.removeChangeListener(ref1);
            synchronized (hookedListeners) {
                removeListeners();
            }
        }

        private void removeListeners() {
            for (java.util.Map.Entry en : hookedListeners.entrySet()) {
                Object o = en.getKey();
                if (o instanceof SourceGroup) ((SourceGroup)o).removePropertyChangeListener((PropertyChangeListener)en.getValue());
                else ((FileObject)o).removeFileChangeListener((FileChangeListener)en.getValue());
            }
            hookedListeners.clear();
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            refreshTask.schedule(200);
        }

        public void stateChanged(ChangeEvent e) {
            refreshTask.schedule(200);
        }
        public void fileFolderCreated(FileEvent fe) {
            refreshTask.schedule(200);
        }

        public void fileDataCreated(FileEvent fe) {
            refreshTask.schedule(200);
        }

        public void fileChanged(FileEvent fe) {
            refreshTask.schedule(200);
        }

        public void fileDeleted(FileEvent fe) {
            refreshTask.schedule(200);
        }

        public void fileRenamed(FileRenameEvent fe) {
            refreshTask.schedule(200);
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
            
        public void run() {
            SourceGroup[] groups = s.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA );
            // Add all paths to the ClasspathInfo structure
            List<ClasspathInfo> classpaths = new ArrayList();
            HashMap<Object, Object> newHooks = new HashMap();
            for (SourceGroup sg : s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
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
            String packages[] = allRegistry.getBasePackages().toArray(new String[0]);
            Arrays.sort(packages);
            setKeys(packages);
            for (Node n : getNodes()) ((PackageChildren)n.getChildren()).notifyChange();
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
            n.setValue(NODE_VALIDITY_ATTRIBUTE, Boolean.valueOf(activeProfileRegistry.getBasePackages().contains(packageName)));
            return new Node[] {n};
        }
  
        private class PackageChildren extends Children.Keys<ClassData> {

            private final String packageName;
            
            public PackageChildren(String packageName) {
                this.packageName = packageName;
                notifyChange();
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
                n.setValue(NODE_VALIDITY_ATTRIBUTE, Boolean.valueOf(activeProfileRegistry.getClassData(classData.getFullyQualifiedName()) != null));
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
            
            public ClassChildren(ClassData classData) {
                this.classData = classData;
                notifyChange();
            }
            
            public void notifyChange() {
                classData = allRegistry.getClassData(classData.getFullyQualifiedName());
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
                n.setValue(NODE_VALIDITY_ATTRIBUTE, Boolean.valueOf(cd != null && cd.getMethods().contains(methodData)));
                return new Node[] {n};
            }
        }

    }
}
