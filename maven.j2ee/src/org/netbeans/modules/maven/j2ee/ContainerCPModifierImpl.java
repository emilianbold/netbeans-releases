/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.core.api.support.classpath.ContainerClassPathModifier;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkleint
 */
public class ContainerCPModifierImpl implements ContainerClassPathModifier {
    private Project project;

    public ContainerCPModifierImpl(Project prj) {
        project = prj;
    }

    public void extendClasspath(final FileObject file, final String[] symbolicNames) {
        if (symbolicNames == null) {
            return;
        }
        final Boolean[] added = new Boolean[1];
        added[0] = Boolean.FALSE;

        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            public void performOperation(POMModel model) {
                Map<String, Item> items = createItemList();
                String version = WebModule.JAVA_EE_5_LEVEL; //sort of fallback
                WebModule wm = WebModule.getWebModule(file);
                if (wm != null) {
                    version = wm.getJ2eePlatformVersion();
                } else {
                    EjbJar ejb = EjbJar.getEjbJar(file);
                    if (ejb != null) {
                        version = ejb.getJ2eePlatformVersion();
                    }
                }
                for (String name : symbolicNames) {
                    Item item = items.get(name + ":" + version);//NOI18N
                    if (item != null) {
                        if (item.classToCheck != null) {
                            ClassPath cp = ClassPath.getClassPath(file, ClassPath.COMPILE);
                            FileObject fo = cp.findResource(item.classToCheck);
                            if (fo != null) {
                                //skip, already on CP somehow..
                                continue;
                            }
                            Dependency dep = ModelUtils.checkModelDependency(model, item.groupId, item.artifactId, true);
                            dep.setVersion(item.version); 
                            dep.setScope("provided"); //NOI18N
                            added[0] = Boolean.TRUE;
                            //TODO repository insertion once we define a non-central repo content
                        }
                    }
                }
            }
        };
        FileObject pom = project.getProjectDirectory().getFileObject("pom.xml");//NOI18N
        org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
        //TODO is the manual reload necessary if pom.xml file is being saved?
//                NbMavenProject.fireMavenProjectReload(project);
        if (added[0]) {
            project.getLookup().lookup(NbMavenProject.class).triggerDependencyDownload();
        }

    }

    // String is conbination of symbolic name + ":" + j2ee level
    private Map<String, Item> createItemList() {
        HashMap<String, Item> toRet = new HashMap<String, Item>();
        String key = ContainerClassPathModifier.API_SERVLET + ":" + J2eeModule.J2EE_13;//NOI18N
        Item item = new Item();
        item.groupId = "javax.servlet";//NOI18N
        item.artifactId = "servlet-api";//NOI18N
        item.version = "2.3";//NOI18N
        item.classToCheck = "javax/servlet/http/HttpServlet.class";//NOI18N
        toRet.put(key, item);
        key = ContainerClassPathModifier.API_SERVLET + ":" + J2eeModule.J2EE_14;//NOI18N
        item = new Item();
        item.groupId = "javax.servlet";//NOI18N
        item.artifactId = "servlet-api";//NOI18N
        item.version = "2.4";//NOI18N
        item.classToCheck = "javax/servlet/http/HttpServlet.class";//NOI18N
        toRet.put(key, item);
        key = ContainerClassPathModifier.API_SERVLET + ":" + J2eeModule.JAVA_EE_5;//NOI18N
        item = new Item();
        item.groupId = "javax.servlet";//NOI18N
        item.artifactId = "servlet-api";//NOI18N
        item.version = "2.5";//NOI18N
        item.classToCheck = "javax/servlet/http/HttpServlet.class";//NOI18N
        toRet.put(key, item);

        key = ContainerClassPathModifier.API_JSP + ":" + J2eeModule.J2EE_13;//NOI18N
        item = new Item();
        item.groupId = "javax.servlet.jsp";
        item.artifactId = "jsp-api";
        item.version = "2.0";
        item.classToCheck = "javax/servlet/jsp/tagext/BodyContent.class";
        toRet.put(key, item);
        key = ContainerClassPathModifier.API_JSP + ":" + J2eeModule.J2EE_14;
        item = new Item();
        item.groupId = "javax.servlet.jsp";//NOI18N
        item.artifactId = "jsp-api";//NOI18N
        item.version = "2.1";//NOI18N
        item.classToCheck = "javax/servlet/jsp/tagext/BodyContent.class";//NOI18N
        toRet.put(key, item);
        key = ContainerClassPathModifier.API_JSP + ":" + J2eeModule.JAVA_EE_5;//NOI18N
        item = new Item();
        item.groupId = "javax.servlet.jsp";//NOI18N
        item.artifactId = "jsp-api";//NOI18N
        item.version = "2.1";//NOI18N
        item.classToCheck = "javax/servlet/jsp/tagext/BodyContent.class";//NOI18N
        toRet.put(key, item);

        return toRet;
    }


    private static class Item {
        String classToCheck;
        String groupId;
        String artifactId;
        String version;
        String repositoryurl;
    }

}
