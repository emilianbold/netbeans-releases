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

import hidden.org.codehaus.plexus.util.StringUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.core.api.support.classpath.ContainerClassPathModifier;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Repository;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkleint
 */
public class ContainerCPModifierImpl implements ContainerClassPathModifier {
    private final Project project;

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
                ProjectSourcesClassPathProvider prv = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
                ClassPath[] cps = prv.getProjectClassPaths(ClassPath.COMPILE);
                ClassPath cp = ClassPathSupport.createProxyClassPath(cps);
                Profile version = Profile.JAVA_EE_5; //sort of fallback
                WebModule wm = WebModule.getWebModule(file);
                if (wm != null) {
                    version = wm.getJ2eeProfile();
                } else {
                    EjbJar ejb = EjbJar.getEjbJar(file);
                    if (ejb != null) {
                        version = ejb.getJ2eeProfile();
                    }
                }

                for (String name : symbolicNames) {
                    Item item = items.get(name + ":" + version.toPropertiesString());//NOI18N
                    if (item != null) {
                        if (item.classToCheck != null) {
                            FileObject fo = cp.findResource(item.classToCheck);
                            if (fo != null) {
                                //skip, already on CP somehow..
                                continue;
                            }
                        }
                        Dependency dep = ModelUtils.checkModelDependency(model, item.groupId, item.artifactId, true);
                        dep.setVersion(item.version);
                        dep.setScope(Artifact.SCOPE_PROVIDED); 
                        added[0] = Boolean.TRUE;
                        //TODO repository insertion once we define a non-central repo content
                        if (item.repositoryurl != null) {
                            String[] repo = StringUtils.split(item.repositoryurl, "|"); //NOI18N
                            assert repo.length == 3;
                            NbMavenProject prj = project.getLookup().lookup(NbMavenProject.class);
                            Repository repository = ModelUtils.addModelRepository(prj.getMavenProject(), model, repo[2]);
                            if (repository != null) {
                                repository.setId(repo[0]);
                                repository.setLayout(repo[1]);
                            }
                        }
                    } else {
                        Logger.getLogger(ContainerCPModifierImpl.class.getName()).warning("Cannot process api with symbolic name: " + name + ". Nothing will be added to project's classpath.");
                    }
                }
            }
        };
        FileObject pom = project.getProjectDirectory().getFileObject("pom.xml");//NOI18N
        org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
        if (added[0]) {
            project.getLookup().lookup(NbMavenProject.class).downloadDependencyAndJavadocSource();
        }

    }

    // String is conbination of symbolic name + ":" + j2ee level
    private Map<String, Item> createItemList() {
        HashMap<String, Item> toRet = new HashMap<String, Item>();
        String key = ContainerClassPathModifier.API_SERVLET + ":" + Profile.J2EE_13.toPropertiesString();//NOI18N
        Item item = new Item();
        item.groupId = "javax.servlet";//NOI18N
        item.artifactId = "servlet-api";//NOI18N
        item.version = "2.3";//NOI18N
        item.classToCheck = "javax/servlet/http/HttpServlet.class";//NOI18N
        toRet.put(key, item);
        key = ContainerClassPathModifier.API_SERVLET + ":" + Profile.J2EE_14.toPropertiesString();//NOI18N
        item = new Item();
        item.groupId = "javax.servlet";//NOI18N
        item.artifactId = "servlet-api";//NOI18N
        item.version = "2.4";//NOI18N
        item.classToCheck = "javax/servlet/http/HttpServlet.class";//NOI18N
        toRet.put(key, item);
        key = ContainerClassPathModifier.API_SERVLET + ":" + Profile.JAVA_EE_5.toPropertiesString();//NOI18N
        item = new Item();
        item.groupId = "javax.servlet";//NOI18N
        item.artifactId = "servlet-api";//NOI18N
        item.version = "2.5";//NOI18N
        item.classToCheck = "javax/servlet/http/HttpServlet.class";//NOI18N
        toRet.put(key, item);

        key = ContainerClassPathModifier.API_JSP + ":" + Profile.J2EE_13.toPropertiesString();//NOI18N
        item = new Item();
        item.groupId = "javax.servlet.jsp";//NOI18N
        item.artifactId = "jsp-api";//NOI18N
        item.version = "2.0";//NOI18N
        item.classToCheck = "javax/servlet/jsp/tagext/BodyContent.class";//NOI18N
        toRet.put(key, item);
        key = ContainerClassPathModifier.API_JSP + ":" + Profile.J2EE_14.toPropertiesString();
        item = new Item();
        item.groupId = "javax.servlet.jsp";//NOI18N
        item.artifactId = "jsp-api";//NOI18N
        item.version = "2.1";//NOI18N
        item.classToCheck = "javax/servlet/jsp/tagext/BodyContent.class";//NOI18N
        toRet.put(key, item);
        key = ContainerClassPathModifier.API_JSP + ":" + Profile.JAVA_EE_5.toPropertiesString();//NOI18N
        item = new Item();
        item.groupId = "javax.servlet.jsp";//NOI18N
        item.artifactId = "jsp-api";//NOI18N
        item.version = "2.1";//NOI18N
        item.classToCheck = "javax/servlet/jsp/tagext/BodyContent.class";//NOI18N
        toRet.put(key, item);


        key = ContainerClassPathModifier.API_J2EE + ":" + Profile.J2EE_13.toPropertiesString();//NOI18N
        item = new Item();
        item.groupId = "org.apache.geronimo.specs";//NOI18N
        item.artifactId = "geronimo-j2ee_1.4_spec";//NOI18N
        item.version = "1.0";//NOI18N
        toRet.put(key, item);
        key = ContainerClassPathModifier.API_J2EE + ":" + Profile.J2EE_14.toPropertiesString();
        item = new Item();
        item.groupId = "org.apache.geronimo.specs";//NOI18N
        item.artifactId = "geronimo-j2ee_1.4_spec";//NOI18N
        item.version = "1.0";//NOI18N
        toRet.put(key, item);
        key = ContainerClassPathModifier.API_J2EE + ":" + Profile.JAVA_EE_5.toPropertiesString();//NOI18N
        item = new Item();
        item.groupId = "javaee";//NOI18N
        item.artifactId = "javaee-api";//NOI18N
        item.version = "5";//NOI18N
        item.repositoryurl = "java.net1|legacy|http://download.java.net/maven/1"; //NOI18N
        toRet.put(key, item);

        key = ContainerClassPathModifier.API_TRANSACTION + ":" + Profile.J2EE_13.toPropertiesString();//NOI18N
        item = new Item();
        item.groupId = "javax.transaction";//NOI18N
        item.artifactId = "jta";//NOI18N
        item.version = "1.0.1B";//NOI18N
        item.repositoryurl = "java.net2|default|http://download.java.net/maven/2"; //NOI18N
        item.classToCheck = "javax/transaction/UserTransaction.class"; //NOI18N
        toRet.put(key, item);
        key = ContainerClassPathModifier.API_TRANSACTION + ":" + Profile.J2EE_14.toPropertiesString();//NOI18N
        item = new Item();
        item.groupId = "javax.transaction";//NOI18N
        item.artifactId = "jta";//NOI18N
        item.version = "1.0.1B";//NOI18N
        item.classToCheck = "javax/transaction/UserTransaction.class"; //NOI18N
        item.repositoryurl = "java.net2|default|http://download.java.net/maven/2"; //NOI18N
        toRet.put(key, item);
        key = ContainerClassPathModifier.API_TRANSACTION + ":" + Profile.JAVA_EE_5.toPropertiesString();//NOI18N
        item = new Item();
        item.groupId = "javax.transaction";//NOI18N
        item.artifactId = "jta";//NOI18N
        item.version = "1.1";//NOI18N
        item.classToCheck = "javax/transaction/UserTransaction.class"; //NOI18N
        toRet.put(key, item);

        key = ContainerClassPathModifier.API_PERSISTENCE + ":" + Profile.J2EE_13.toPropertiesString();//NOI18N
        item = new Item();
        item.groupId = "javax.persistence";//NOI18N
        item.artifactId = "persistence-api";//NOI18N
        item.version = "1.0";//NOI18N
        item.classToCheck = "javax/persistence/PersistenceContext.class"; //NOI18N
        toRet.put(key, item);
        key = ContainerClassPathModifier.API_PERSISTENCE + ":" + Profile.J2EE_14.toPropertiesString();//NOI18N
        item = new Item();
        item.groupId = "javax.persistence";//NOI18N
        item.artifactId = "persistence-api";//NOI18N
        item.version = "1.0";//NOI18N
        item.classToCheck = "javax/persistence/PersistenceContext.class"; //NOI18N
        toRet.put(key, item);
        key = ContainerClassPathModifier.API_PERSISTENCE + ":" + Profile.JAVA_EE_5.toPropertiesString();//NOI18N
        item = new Item();
        item.groupId = "javax.persistence";//NOI18N
        item.artifactId = "persistence-api";//NOI18N
        item.version = "1.0";//NOI18N
        item.classToCheck = "javax/persistence/PersistenceContext.class"; //NOI18N
        toRet.put(key, item);

        key = ContainerClassPathModifier.API_ANNOTATION + ":" + Profile.J2EE_13.toPropertiesString();//NOI18N
        item = new Item();
        item.groupId = "javax.annotation";//NOI18N
        item.artifactId = "jsr250-api";//NOI18N
        item.version = "1.0";//NOI18N
        item.classToCheck = "javax/annotation/Resource.class"; //NOI18N
        toRet.put(key, item);
        key = ContainerClassPathModifier.API_ANNOTATION + ":" + Profile.J2EE_14.toPropertiesString();//NOI18N
        item = new Item();
        item.groupId = "javax.annotation";//NOI18N
        item.artifactId = "jsr250-api";//NOI18N
        item.version = "1.0";//NOI18N
        item.classToCheck = "javax/annotation/Resource.class"; //NOI18N
        toRet.put(key, item);
        key = ContainerClassPathModifier.API_ANNOTATION + ":" + Profile.JAVA_EE_5.toPropertiesString();//NOI18N
        item = new Item();
        item.groupId = "javax.annotation";//NOI18N
        item.artifactId = "jsr250-api";//NOI18N
        item.version = "1.0";//NOI18N
        item.classToCheck = "javax/annotation/Resource.class"; //NOI18N
        toRet.put(key, item);


        key = ContainerClassPathModifier.API_EJB + ":" + Profile.J2EE_13.toPropertiesString();//NOI18N
        item = new Item();
        // oh well does it matter? where is 2.0 stuff to be found?
        item.groupId = "org.apache.geronimo.specs";//NOI18N
        item.artifactId = "geronimo-ejb_2.1_spec";//NOI18N
        item.version = "1.0";//NOI18N
        item.classToCheck = "javax/ejb/EJB.class"; //NOI18N
        toRet.put(key, item);
        key = ContainerClassPathModifier.API_EJB + ":" + Profile.J2EE_14.toPropertiesString();//NOI18N
        item = new Item();
        item.groupId = "org.apache.geronimo.specs";//NOI18N
        item.artifactId = "geronimo-ejb_2.1_spec";//NOI18N
        item.version = "1.1";//NOI18N
        item.classToCheck = "javax/ejb/EJB.class"; //NOI18N
        toRet.put(key, item);
        key = ContainerClassPathModifier.API_EJB + ":" + Profile.JAVA_EE_5.toPropertiesString();//NOI18N
        item = new Item();
        item.groupId = "org.apache.geronimo.specs";//NOI18N
        item.artifactId = "geronimo-ejb_3.0_spec";//NOI18N
        item.version = "1.0.1";//NOI18N
        item.classToCheck = "javax/ejb/EJB.class"; //NOI18N
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
