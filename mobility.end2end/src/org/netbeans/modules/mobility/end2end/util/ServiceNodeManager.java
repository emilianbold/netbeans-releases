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
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.NbBundle;
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


    private static class ProjectChildren extends Children.Keys<String> implements ChangeListener {

        private final Sources s;
        private ClassDataRegistry activeProfileRegistry, allRegistry;
       
        public ProjectChildren(Project p) {
            s = ProjectUtils.getSources(p);
            stateChanged(null);
        }

        public void stateChanged(ChangeEvent e) {
            SourceGroup[] groups = s.getSourceGroups( JavaProjectConstants.SOURCES_TYPE_JAVA );
            // Add all paths to the ClasspathInfo structure
            List<ClasspathInfo> classpaths = new ArrayList();
            for (SourceGroup sg : s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                if (!sg.getName().equals("${test.src.dir}")) classpaths.add(ClasspathInfo.create(sg.getRootFolder())); //NOI18N
            }
            // Get the registry for all available classes
            allRegistry = ClassDataRegistry.getRegistry( ClassDataRegistry.ALL_JAVA_PROFILE, classpaths );
            activeProfileRegistry = ClassDataRegistry.getRegistry( getActiveProfile(), classpaths);
            setKeys(allRegistry.getBasePackages());
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

            public PackageChildren(String packageName) {
                setKeys(allRegistry.getBaseClassesForPackage(packageName));
            }
            
            protected Node[] createNodes(ClassData classData) {
                AbstractNode n = new AbstractNode(new ClassChildren(classData), Lookups.singleton(classData));
                n.setName(classData.getName());
                n.setDisplayName(classData.getName());
                n.setIconBaseWithExtension(CLASS_ICON);
                n.setValue(NODE_VALIDITY_ATTRIBUTE, Boolean.valueOf(activeProfileRegistry.getClassData(classData.getFullyQualifiedName()) != null));
                return new Node[] {n};
            }
        }
        
        private class ClassChildren extends Children.Keys<MethodData> {

            public ClassChildren(ClassData classData) {
                setKeys(classData.getMethods());
            }
            
            protected Node[] createNodes(MethodData methodData) {
                StringBuffer nodeText = new StringBuffer(methodData.getReturnType().getName());
                nodeText.append(' ').append(methodData.getName()).append('(');
                boolean first = true;
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
