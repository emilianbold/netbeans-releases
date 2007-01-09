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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.spi.ejbjar.support.J2eeProjectView;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Provides EJB tree of all open projects. This class is not used for displaying Enterprise Beans node in project view.
 */
public final class EJBListViewChildren extends Children.Keys<EJBListViewChildren.KEY> {

    public enum KEY { EJB }
    
    private final Sources sources;
    private final ClassPath classPath;
    private final Project project;

    public EJBListViewChildren(Project project) {
        assert project != null;
        this.project = project;
        sources = ProjectUtils.getSources(project);
        assert sources != null;
        classPath = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(getRoots());
    }

    private FileObject[] getRoots() {
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        FileObject[] roots = new FileObject[groups.length];
        for (int i = 0; i < groups.length; i++) {
            roots[i] = groups[i].getRootFolder();
        }
        return roots;
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        createNodes();
    }

    private void createNodes() {
        List<KEY> keys = new ArrayList<KEY>();
        keys.add(KEY.EJB);
        setKeys(keys);
    }

    @Override
    protected void removeNotify() {
        setKeys(Collections.<KEY>emptySet());
        super.removeNotify();
    }

    public Node[] createNodes(KEY key) {
        Node node = null;
        if (key == KEY.EJB) {
            EjbJar[] apiEjbJars = EjbJar.getEjbJars(project);
            org.netbeans.modules.j2ee.dd.api.ejb.EjbJar ejbJar = null;
            try {
                ejbJar = org.netbeans.modules.j2ee.dd.api.ejb.DDProvider.getDefault().getMergedDDRoot(apiEjbJars[0].getMetadataUnit());
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
            if (ejbJar != null) {
                node = J2eeProjectView.createEjbsView(ejbJar, classPath, apiEjbJars[0].getDeploymentDescriptor(), project);
            }
        }
        return node == null ? new Node[0] : new Node[] {node};
    }

}


