/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.j2ee.ear.EarModuleProviderImpl;
import org.netbeans.modules.maven.j2ee.ejb.EjbModuleProviderImpl;
import org.netbeans.modules.maven.j2ee.web.WebModuleProviderImpl;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarFactory;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarProvider;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarsInProject;
import org.netbeans.modules.j2ee.spi.ejbjar.support.EjbJarSupport;
import org.netbeans.modules.maven.j2ee.appclient.AppClientModuleProviderImpl;
import org.netbeans.modules.maven.j2ee.ejb.EjbEntRefContainerImpl;
import org.netbeans.modules.maven.j2ee.web.EntRefContainerImpl;
import org.netbeans.modules.maven.j2ee.web.MavenWebProjectWebRootProvider;
import org.netbeans.modules.maven.j2ee.web.WebEjbJarImpl;
import org.netbeans.modules.maven.j2ee.web.WebReplaceTokenProvider;
import org.netbeans.modules.web.jsfapi.spi.JsfSupportHandle;
import org.netbeans.spi.project.LookupProvider;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * extending the default maven project lookup.
 * @author  Milos Kleint
 */
@LookupProvider.Registration(projectType="org-netbeans-modules-maven")
public class J2eeLookupProvider implements LookupProvider {
    
    /** Creates a new instance of J2eeLookupProvider */
    public J2eeLookupProvider() {
    }
    
    @Override
    public Lookup createAdditionalLookup(Lookup baseLookup) {
        Project project = baseLookup.lookup(Project.class);
        assert project != null;
//        // if there's more items later, just do a proxy..
        InstanceContent ic = new InstanceContent();
        // XXX replace everything with @ProjectServiceProvider(projectType="org-netbeans-modules-maven/" + NbMavenProject.TYPE_WAR) etc.
        ic.add(new J2EEPrerequisitesChecker());
        ic.add(new J2eeRecoPrivTemplates(project));
        ic.add(new J2eeMavenSourcesImpl(project));
        ic.add(new ExecutionChecker(project));
        ic.add(new SessionContent());
        Provider prov = new Provider(project, ic);
        POHImpl p = new POHImpl(project, prov);
        ic.add(p);
        ic.add(new POHImpl.Hook(p));
        ic.add(new ContainerCPModifierImpl(project));
        return prov;
    }
    
    public static class Provider extends AbstractLookup implements  PropertyChangeListener {
        private final Project project;
        private final InstanceContent content;
        private String lastType = NbMavenProject.TYPE_JAR;
        private Object lastInstance = null;
        private CopyOnSave copyOnSave;
        private final WebReplaceTokenProvider replacer;
        private final EntRefContainerImpl webEnt;
        private final EjbEntRefContainerImpl ejbEnt;
        private final JPAStuffImpl jpa;
        private final EMGSResolverImpl resolver;
        private final MavenPersistenceProviderSupplier supplier;
        private EjbJarProvider webEjbJarProvider;
        private EjbJarsInProject ejbJarsInProject;
        private MavenWebProjectWebRootProvider webRootProvider;
        private JsfSupportHandle jsfSupportHandle;

        public Provider(Project proj, InstanceContent cont) {
            super(cont);
            project = proj;
            content = cont;
            replacer = new WebReplaceTokenProvider(proj);
            webEnt = new EntRefContainerImpl(proj);
            ejbEnt = new EjbEntRefContainerImpl(proj);
            jpa = new JPAStuffImpl(proj);
            resolver = new EMGSResolverImpl();
            supplier = new MavenPersistenceProviderSupplier(proj);
            webRootProvider = new MavenWebProjectWebRootProvider(project);
            jsfSupportHandle = new JsfSupportHandle();

            checkJ2ee();
            NbMavenProject.addPropertyChangeListener(project, this);
        }
        
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            if (NbMavenProject.PROP_PROJECT.equals(propertyChangeEvent.getPropertyName())) {
                checkJ2ee();
            }
        }
        
        private void checkJ2ee() {
            NbMavenProject watcher = project.getLookup().lookup(NbMavenProject.class);
            String packaging = watcher.getPackagingType();
            doCheckJ2ee(packaging);
        }
        
        public void hackModuleServerChange() {
            //#109507 use reflection on J2eeModuleProvider.resetConfigSupport()
            doCheckJ2ee(null);
            checkJ2ee();
        }

        private boolean isWebSupported(String packaging) {
            if ("war".equals(packaging)) { // NOI18N
                return true;
            }
            // #179584
            // if it is bundle packaging type but a valid "src/main/webapp" exists
            // then provide lookup content as for web application so that code
            // completion etc. works
            if ("bundle".equals(packaging)) { // NOI18N
                NbMavenProject proj = project.getLookup().lookup(NbMavenProject.class);
                if (new File(proj.getWebAppDirectory()).exists()) {
                    return true;
                }
            }
            return false;
        }
        
        private void doCheckJ2ee(String packaging) {
            if (packaging == null) {
                packaging = NbMavenProject.TYPE_JAR;
            }
            if (copyOnSave != null && !isWebSupported(packaging)) {
                try {
                    copyOnSave.cleanup();
                } catch (FileStateInvalidException ex) {
                    ex.printStackTrace();
                }
                content.remove(copyOnSave);
                copyOnSave = null;
            }
            if (isWebSupported(packaging) && !lastType.equals(packaging)) {
                removeInstances();
                WebModuleProviderImpl prov = new WebModuleProviderImpl(project);
                lastInstance = prov;
                content.add(lastInstance);
                content.add(replacer);
                content.add(webEnt);
                content.add(jpa);
                content.add(resolver);
                content.add(supplier);
                content.add(webRootProvider);
                content.add(jsfSupportHandle);
                //j2ee 6 stuff..
                Profile prf = prov.getWebModuleImplementation().getJ2eeProfile();
                if (Profile.JAVA_EE_6_WEB.equals(prf) || Profile.JAVA_EE_6_FULL.equals(prf)) {
                    WebEjbJarImpl webEjbJarImpl = new WebEjbJarImpl(prov.getWebModuleImplementation(), project);
                    EjbJar apiEjbJar = EjbJarFactory.createEjbJar(webEjbJarImpl);
                    webEjbJarProvider = EjbJarSupport.createEjbJarProvider(project, apiEjbJar);
                    ejbJarsInProject = EjbJarSupport.createEjbJarsInProject(apiEjbJar);
                    content.add(webEjbJarProvider);
                    content.add(ejbJarsInProject);
                }
                copyOnSave = prov.getCopyOnSaveSupport();
                try {
                    copyOnSave.initialize();
                } catch (FileStateInvalidException ex) {
                    ex.printStackTrace();
                }
                content.add(copyOnSave);
            } else if (NbMavenProject.TYPE_EAR.equals(packaging) && !lastType.equals(packaging)) {
                removeInstances();
                lastInstance = new EarModuleProviderImpl(project);
                content.add(lastInstance);
                content.add(((EarModuleProviderImpl)lastInstance).getEarImplementation());
            } else if (NbMavenProject.TYPE_EJB.equals(packaging) && !lastType.equals(packaging)) {
                removeInstances();
                EjbModuleProviderImpl prov = new EjbModuleProviderImpl(project);
                lastInstance = prov;
                content.add(lastInstance);
                content.add(jpa);
                content.add(ejbEnt);
                content.add(resolver);
                content.add(supplier);
                copyOnSave = prov.getCopyOnSaveSupport();
                try {
                    copyOnSave.initialize();
                } catch (FileStateInvalidException ex) {
                    ex.printStackTrace();
                }
                content.add(copyOnSave);
            } else if (NbMavenProject.TYPE_APPCLIENT.equals(packaging) && !lastType.equals(packaging)) {
                removeInstances();
                
                AppClientModuleProviderImpl prov = new AppClientModuleProviderImpl(project);
                lastInstance = prov;
                content.add(lastInstance);
                
                content.add(jpa);
                //content.add(ejbEnt);
                //content.add(resolver);
                content.add(supplier);
                
                copyOnSave = prov.getCopyOnSaveSupport();
                try {
                    copyOnSave.initialize();
                } catch (FileStateInvalidException ex) {
                    ex.printStackTrace();
                }
                content.add(copyOnSave);
            } else if (lastInstance != null && !(
                    isWebSupported(packaging) || 
                    NbMavenProject.TYPE_EJB.equals(packaging) || 
                    NbMavenProject.TYPE_APPCLIENT.equals(packaging) || 
                    NbMavenProject.TYPE_EAR.equals(packaging)))
            {
                removeInstances();

                lastInstance = null;
            }
            lastType = packaging;
        }
        
        private void removeInstances() {
            if (lastInstance != null) {
                if (lastInstance instanceof EarModuleProviderImpl) {
                    content.remove(((EarModuleProviderImpl)lastInstance).getEarImplementation());
                }
                content.remove(lastInstance);
            }
            content.remove(replacer);
            content.remove(webEnt);
            content.remove(ejbEnt);
            content.remove(jpa);
            content.remove(resolver);
            content.remove(supplier);
            content.remove(webRootProvider);
            content.remove(jsfSupportHandle);
            if (webEjbJarProvider != null) {
                content.remove(webEjbJarProvider);
                webEjbJarProvider = null;
            }
            if (ejbJarsInProject != null) {
                content.remove(ejbJarsInProject);
                ejbJarsInProject = null;
            }
        }
    }
}
