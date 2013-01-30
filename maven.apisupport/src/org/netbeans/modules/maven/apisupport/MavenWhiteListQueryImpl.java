/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.apisupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import javax.lang.model.element.ElementKind;
import javax.swing.event.ChangeListener;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.project.Project;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.whitelist.WhiteListQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service = WhiteListQueryImplementation.class, projectType="org-netbeans-modules-maven/" + NbMavenProject.TYPE_NBM)
public class MavenWhiteListQueryImpl implements WhiteListQueryImplementation {
    private final Project project;

    public MavenWhiteListQueryImpl(Project prj) {
        project = prj;
    }
    
    @Override
    public WhiteListImplementation getWhiteList(FileObject file) {
        NbMavenProject mvn = project.getLookup().lookup(NbMavenProject.class);
        assert mvn != null;
        MavenProject mp = mvn.getMavenProject();
        //TODO remove the check
        if (mp.getProperties() == null && mp.getProperties().getProperty("enable.whitelist") == null) {
            //a temporary thing to enable the whitelists for experimentation..
            return null;
        }
        String useOsgiString = PluginPropertyUtils.getPluginProperty(project, MavenNbModuleImpl.GROUPID_MOJO, MavenNbModuleImpl.NBM_PLUGIN, "useOSGiDependencies", null, null);
        boolean useOsgi = useOsgiString != null ? Boolean.parseBoolean(useOsgiString) : false;
        List<NBMWrapper> nbms = new ArrayList<NBMWrapper>();
        List<OSGIWrapper> osgis = new ArrayList<OSGIWrapper>();
        List<Wrapper> directCPs = new ArrayList<Wrapper>();
        List<Wrapper> unknown = new ArrayList<Wrapper>();
                
        for (Artifact a : mp.getCompileArtifacts()) {
            if (a.getFile() != null) {
                JarFile jf = null;
                try {
                    jf = new JarFile(a.getFile(), false);
                    Manifest mf = jf.getManifest();
                    if (mf != null && mf.getMainAttributes() != null) {
                        Attributes attrs = mf.getMainAttributes();
                        String osgiexport = attrs.getValue("Export-Package");
                        String osgiprivate = attrs.getValue("Private-Package");
                        String nbmexport = attrs.getValue("OpenIDE-Module-Public-Packages");
                        Set<String> allpackages = getAllPackages(jf);
                        if (nbmexport != null) {
                            String nbmMaven = attrs.getValue("Maven-Class-Path"); //modules built with maven with external libs
                            String friends = attrs.getValue("OpenIDE-Module-Friends");
                            nbms.add(new NBMWrapper(a, allpackages, nbmexport.equals("-") ? null : StringUtils.split(nbmexport, ","),
                                    friends != null ? StringUtils.split(friends, ",") : null, 
                                    nbmMaven != null ? StringUtils.split(nbmMaven, " ") : null));
                        }
                        else if (useOsgi && osgiexport != null) {
                            //TODO
                        } 
                        else {
                            if (a.getDependencyTrail() != null && a.getDependencyTrail().size() > 2) {
                                unknown.add(new Wrapper(a, allpackages));
                            } else {
                                //direct dependencies are part of the module's CP entirely..
                                directCPs.add(new Wrapper(a, allpackages));
                            }
                        }
                    }
                } catch (IOException ex) {
                } finally {
                    if (jf != null) {
                        try {
                            jf.close();
                        } catch (IOException ex) {
                        }
                    }
                }
                
            }
        }
        
        List<ExplicitDependency> explicits = PluginPropertyUtils.getPluginPropertyBuildable(project, MavenNbModuleImpl.GROUPID_MOJO, MavenNbModuleImpl.NBM_PLUGIN, null, new ExplicitBuilder());
        String codenamebase = PluginPropertyUtils.getPluginProperty(project, MavenNbModuleImpl.GROUPID_MOJO, MavenNbModuleImpl.NBM_PLUGIN, "codeNameBase", null, null);
        
        //compute the effective, known "private" packages that should not be accessible from the file.
        final Set<String> privatePackages = new HashSet<String>();
        final Set<String> transitivePackages = new HashSet<String>();
        //these two are here to remove duplicates, if a package is both private (in one module) and public (in another module)
        // consider the package public for our purposes. better a false negative than false positive here..
        Set<String> nonPrivatePackages = new HashSet<String>();
        Set<String> nonTransitivePackages = new HashSet<String>();
        //direct cp is always visible..
        for (Wrapper dir : directCPs) {
            nonTransitivePackages.addAll(dir.allPackages);
            nonPrivatePackages.addAll(dir.allPackages);
        }
        for (NBMWrapper nbm : nbms ) {
            Set<String> allPackages = new HashSet<String>(nbm.allPackages);
            //merge unknowns into their respective wrapper modules..
            if (nbm.hasMavenCPDefined()) {
                Iterator<Wrapper> it = unknown.iterator();
                while (it.hasNext()) {
                    Wrapper wrapper = it.next();
                    if (nbm.hasOnClassPath(wrapper.art)) {
                        nbm.wrappedLibs.add(wrapper.art); //TODO do we want to modify the nbm wrapper  at this point?
                        allPackages.addAll(wrapper.allPackages);
                        //it.remove(); cannot remove, sometimes multiple nbms reference the same jar, and some could make it's packages public and some could make them private..
                    }
                }
            }
            
            if (nbm.art.getDependencyTrail() != null && nbm.art.getDependencyTrail().size() > 2) {
                //transitive dependency - TODO 
                transitivePackages.addAll(allPackages);
            } else {
                nonTransitivePackages.addAll(allPackages);
            }
            
            //we need to check the explicit dependencies for implementation deps, in that case all packages are public
            if (explicits != null) {
                for (ExplicitDependency ex : explicits) {
                    if (ex.matches(nbm.art) && ex.isImplementationDependency()) {
                        //we got impl dep, none of the packages are private.
                        nonPrivatePackages.addAll(allPackages);
                        continue;
                    }
                }
            }
            
            for (String p : allPackages) {
                if (nbm.isPublicPackage(p)) {
                    nonPrivatePackages.add(p);
                } else {
                    privatePackages.add(p);
                }
            }
        }
        
        //remove all duplicates. only keep the privates we are 100% positive about..
        transitivePackages.removeAll(nonTransitivePackages);
        privatePackages.removeAll(nonPrivatePackages);
        
        return new WhiteListImplementation() {
            private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
            @Override
            public WhiteListQuery.Result check(org.netbeans.api.java.source.ElementHandle<?> arg0, WhiteListQuery.Operation arg1) {
                if (!arg1.equals(WhiteListQuery.Operation.USAGE)) {
                    return OK;
                }
                List<WhiteListQuery.RuleDescription> rds = new ArrayList<WhiteListQuery.RuleDescription>();
                if (arg0.getKind() == ElementKind.CLASS || arg0.getKind() == ElementKind.INTERFACE) {
                    String qn = arg0.getQualifiedName();
                    String pack = qn.substring(0, qn.lastIndexOf("."));
                    if (privatePackages.contains(pack)) {
                        rds.add(PRIVATE_RD);
                    }
                    if (transitivePackages.contains(pack)) {
                        rds.add(TRANSITIVE_RD);
                    }
                    if (!rds.isEmpty()) {
                        return new WhiteListQuery.Result(rds);
                    }
                }
                return OK;
            }

            @Override
            public void addChangeListener(ChangeListener listener) {
                synchronized (listeners) {
                    listeners.add(listener);
                }
            }

            @Override
            public void removeChangeListener(ChangeListener listener) {
                synchronized (listeners) {
                    listeners.remove(listener);
                }
            }
        };
    }
    
    private static final WhiteListQuery.RuleDescription PRIVATE_RD = new WhiteListQuery.RuleDescription("private", "Module dependency's private package referenced", null);
    private static final WhiteListQuery.RuleDescription TRANSITIVE_RD = new WhiteListQuery.RuleDescription("transitive", "Package from transitive module dependency referenced, declare a direct dependency to fix.", null);
    private static final WhiteListQuery.Result OK = new WhiteListQuery.Result();
    

    private Set<String> getAllPackages(JarFile jf) {
        Set<String> toRet = new HashSet<String>();
        Enumeration<JarEntry> en = jf.entries();
        while (en.hasMoreElements()) {
            JarEntry je = en.nextElement();
            String name = je.getName();
            if (!je.isDirectory() && name.endsWith(".class") && name.lastIndexOf('/') > -1) {
                name = name.substring(0, name.lastIndexOf('/'));
                toRet.add(name.replace('/', '.'));
            }
        }
        return toRet;
    }


    private static class Wrapper {
        final Artifact art;
        final Set<String> allPackages;

        public Wrapper(Artifact art, Set<String> allPackages) {
            this.art = art;
            this.allPackages = allPackages;
        }
        
    }
    
    private static class OSGIWrapper extends Wrapper {
        final String[] exports;

        public OSGIWrapper( Artifact art, Set<String> allPackages, String[] exports) {
            super(art, allPackages);
            this.exports = exports;
        }
        
    }
    
    private static class NBMWrapper extends Wrapper {
        final String[] publicPackages;
        final List<Artifact> wrappedLibs = new ArrayList<Artifact>();
        boolean isImplementationDependency;
        final List<String> friends;
        final List<String> mavenCP;
        private final Set<String> eqPublic = new HashSet<String>();
        private final Set<String> subPublic = new HashSet<String>();

        public NBMWrapper(Artifact art, Set<String> allPackages, String[] publicPackages, String[] friends, String[] mavenCP) {
            super(art, allPackages);
            this.publicPackages = publicPackages;
            this.friends = friends != null ? Arrays.asList(friends) : Collections.<String>emptyList();
            this.mavenCP = mavenCP != null ? Arrays.asList(mavenCP) : Collections.<String>emptyList();
            if (publicPackages == null) {
                //no public packages.
            } else {
                for (String pub : publicPackages) {
                    if (pub.endsWith(".*")) {
                        subPublic.add(pub.substring(0, pub.length() - ".*".length()));
                    } else {
                        eqPublic.add(pub);
                    }
                }
            }
        }

        boolean isFriend(String codenamebase) {
            return friends.contains(codenamebase);
        }
        
        boolean hasFriendAPI() {
            return !friends.isEmpty();
        }
        
        boolean hasOnClassPath(Artifact art) {
            //construct ID as we do in NetbeansManifestUpdateMojo
            String id = art.getGroupId() + ":" + art.getArtifactId() + ":" + art.getBaseVersion() + (art.getClassifier() != null ? ":" + art.getClassifier() : "");
             return mavenCP.contains(id);
        }
        
        boolean hasMavenCPDefined() {
            return !mavenCP.isEmpty();
        }
        
        boolean isPublicPackage(String pack) {
            if (eqPublic.contains(pack)) {
                return true;
            }
            for (String suString : subPublic) {
                if (pack.startsWith(suString)) {
                    return true;
                }
            }
            return false;
        }
        
    }
    
    //model for http://mojo.codehaus.org/nbm-maven/nbm-maven-plugin/manifest-mojo.html#moduleDependencies
    private static class ExplicitDependency {
        String id;
        String explicit;
        String type;
        
        boolean matches(Artifact art) {
            return id != null && id.equals(art.getGroupId() + ":" +  art.getArtifactId());
        }
        
        boolean isImplementationDependency() {
            return (explicit != null && explicit.contains("=")) || ("impl".equals(type));
        }
    }
    
    private static class ExplicitBuilder implements PluginPropertyUtils.ConfigurationBuilder<List<ExplicitDependency>> {

        @Override
        public List<ExplicitDependency> build(Xpp3Dom configRoot, ExpressionEvaluator eval) {
            if (configRoot != null) {
                Xpp3Dom list = configRoot.getChild("moduleDependencies");
                if (list != null) {
                    List<ExplicitDependency> toRet = new ArrayList<ExplicitDependency>();
                    Xpp3Dom[] childs = list.getChildren("moduleDependency");
                    for (Xpp3Dom ch : childs) {
                        Xpp3Dom idDom = ch.getChild("id"); //NOI18N
                        Xpp3Dom typeDom = ch.getChild("type"); //NOI18N
                        Xpp3Dom explicitDom = ch.getChild("explicitValue"); //NOI18N
                        if (idDom != null && (typeDom != null || explicitDom != null)) {
                            String id = idDom.getValue();
                            String type = typeDom.getValue();
                            String explicit = explicitDom.getValue();
                            if (id != null && (type != null || explicit != null)) {
                                ExplicitDependency ed = new ExplicitDependency();
                                ed.id = id;
                                ed.type = type;
                                ed.explicit = explicit;
                                //TODO any value evaluation necessary?
                                toRet.add(ed);
                            }
                        }
                    }
                    return toRet;
                }
            }
            return null;
        }
        
    }
}
