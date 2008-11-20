/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.jaxws;

import java.io.IOException;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.openide.filesystems.FileObject;
import javax.xml.namespace.QName;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMQName;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginExecution;
import org.netbeans.modules.maven.model.pom.Resource;

/**
 *
 * @author mkuchtiak
 */
public class MavenModelUtils {

    /**
     * returns a RESOLVED project instance of the
     * plugin if defined in the pom.xml or any of the parents.
     * @param handle
     * @return
     */
    public static org.apache.maven.model.Plugin getJaxWSPlugin(MavenProject project) {
        @SuppressWarnings("unchecked")
        List<org.apache.maven.model.Plugin> plugins = project.getBuildPlugins();
        for (org.apache.maven.model.Plugin plg : plugins) {
            if ("org.codehaus.mojo:jaxws-maven-plugin".equalsIgnoreCase(plg.getKey())) {
                //TODO CHECK THE ACTUAL PARAMETER VALUES..
                return plg;
            }
        }
        return null;
    }
    
    public static org.apache.maven.model.Plugin getWarPlugin(MavenProject project) {
        @SuppressWarnings("unchecked")
        List<org.apache.maven.model.Plugin> plugins = project.getBuildPlugins();
        for (org.apache.maven.model.Plugin plg : plugins) {
            if ("org.apache.maven.plugins:maven-war-plugin".equalsIgnoreCase(plg.getKey())) { //NOI18N
                //TODO CHECK THE ACTUAL PARAMETER VALUES..
                return plg;
            }
        }
        return null;
    }

    /**
     * adds jaxws plugin, requires the model to have a transaction started,
     * eg. by calling as part of Utilities.performPOMModelOperations(ModelOperation<POMModel>)
     * @param model
     * @return
     */
    public static Plugin addJaxWSPlugin(POMModel model) {
        assert model.isIntransaction() : "need to call model modifications under transaction."; //NOI18N
        Build bld = model.getProject().getBuild();
        if (bld == null) {
            bld = model.getFactory().createBuild();
            model.getProject().setBuild(bld);
        }
        Plugin plugin = bld.findPluginById("org.codehaus.mojo", "jaxws-maven-plugin"); //NOI18N
        if (plugin != null) {
            //TODO CHECK THE ACTUAL PARAMETER VALUES..
            return plugin;
        }
        plugin = model.getFactory().createPlugin();
        plugin.setGroupId("org.codehaus.mojo"); //NOI18N
        plugin.setArtifactId("jaxws-maven-plugin"); //NOI18N
        plugin.setVersion("1.10"); //NOI18N
        bld.addPlugin(plugin);
        PluginExecution exec = plugin.findExecutionById("wsimport-generate"); //NOI18N
        if (exec == null) {
            exec = model.getFactory().createExecution();
            exec.setId("wsimport-generate"); //NOI18N
            exec.setPhase("generate-sources"); //NOI18N
            exec.addGoal("wsimport"); //NOI18N
            plugin.addExecution(exec);
        } else {
            //shall we do something here?
        }
        Configuration config = plugin.getConfiguration();
        if (config == null) {
            config = model.getFactory().createConfiguration();
            plugin.setConfiguration(config);
        }

        config.setSimpleParameter("sourceDestDir", "${project.build.directory}/generated-sources/jaxws-wsimport"); //NOI18N
        config.setSimpleParameter("xnocompile", "true"); //NOI18N
        config.setSimpleParameter("verbose", "true"); //NOI18N
        config.setSimpleParameter("catalog", "${basedir}/" + MavenJAXWSSupportIml.CATALOG_PATH);
        return plugin; 
    }
        
    public static Plugin addWarPlugin(POMModel model) {
        assert model.isIntransaction() : "need to call model modifications under transaction."; //NOI18N
        Build bld = model.getProject().getBuild();
        if (bld == null) {
            bld = model.getFactory().createBuild();
            model.getProject().setBuild(bld);
        }
        Plugin plugin = bld.findPluginById("org.apache.maven.plugins", "maven-war-plugin"); //NOI18N
        if (plugin == null) {
            plugin = model.getFactory().createPlugin();
            plugin.setGroupId("org.apache.maven.plugins"); //NOI18N
            plugin.setArtifactId("maven-war-plugin"); //NOI18N
            bld.addPlugin(plugin);
        }
        plugin.setVersion("2.0.2"); //NOI18N

        Configuration config = plugin.getConfiguration();
        if (config == null) {
            config = model.getFactory().createConfiguration();
            plugin.setConfiguration(config);
        }
        List<POMExtensibilityElement> ex = config.getConfigurationElements();
        POMExtensibilityElement webResources = findChild(config.getConfigurationElements(), "webResources");
        if (webResources == null) {
            webResources = model.getFactory().createPOMExtensibilityElement(
                    POMQName.createQName("webResources", model.getPOMQNames().isNSAware()));
            config.addExtensibilityElement(webResources);
        }
        //TODO how to recognize if the correct resource element is present???

        POMExtensibilityElement res = findChild(webResources.getExtensibilityElements(), "resource");
        //we check for presense only, we should iterate all and check the internals.
        if (res == null) {
            res = model.getFactory().createPOMExtensibilityElement(
                    POMQName.createQName("resource", model.getPOMQNames().isNSAware()));
            webResources.addExtensibilityElement(res);
            POMExtensibilityElement dir = model.getFactory().createPOMExtensibilityElement(
                    POMQName.createQName("directory", model.getPOMQNames().isNSAware()));
            dir.setElementText("src");
            res.addExtensibilityElement(dir);

            POMExtensibilityElement tp = model.getFactory().createPOMExtensibilityElement(POMQName.createQName("targetPath",
                    model.getPOMQNames().isNSAware()));
            tp.setElementText("WEB-INF");
            res.addExtensibilityElement(tp);

            POMExtensibilityElement in = model.getFactory().createPOMExtensibilityElement(POMQName.createQName("includes",
                    model.getPOMQNames().isNSAware()));
            res.addExtensibilityElement(in);

            POMExtensibilityElement include = model.getFactory().createPOMExtensibilityElement(POMQName.createQName("include",
                    model.getPOMQNames().isNSAware()));
            include.setElementText("jax-ws-catalog.xml");
            in.addExtensibilityElement(include);

            include = model.getFactory().createPOMExtensibilityElement(POMQName.createQName("include",
                    model.getPOMQNames().isNSAware()));
            include.setElementText("wsdl/**");
            in.addExtensibilityElement(include);
        }
    
        return plugin; 
    }
    
    public static void addWsdlResources(POMModel model) {
        assert model.isIntransaction();
        Build bld = model.getProject().getBuild();
        if (bld == null) {
            return;
        }
        boolean foundResourceForMetaInf = false;
        List<Resource> resources = bld.getResources();
        if (resources != null) {
            for (Resource resource : resources) {
                if ("META-INF".equals(resource.getTargetPath())
                     && ("src".equals(resource.getDirectory()) || "${basedir}/src".equals(resource.getDirectory()))) { //NOI18N
                    foundResourceForMetaInf = true;
                    //TODO shall we chckf or jax-ws-catalog.xml + wsdl includes?
                }
            }
        }
        if (!foundResourceForMetaInf) {
            Resource res = model.getFactory().createResource();
            res.setTargetPath("META-INF"); //NOI18N
            res.setDirectory("src"); //NOI18N
            res.addInclude("jax-ws-catalog.xml"); //NOI18N
            res.addInclude("wsdl/**"); //NOI18N
            bld.addResource(res);
        }

    }
    

    private static POMExtensibilityElement findChild(List<POMExtensibilityElement> elems, String name) {
        for (POMExtensibilityElement e : elems) {
            if (name.equals(e.getQName().getLocalPart())) {
                return e;
            }
        }
        return null;
    }
    
    public static void addWsdlFile(Plugin plugin, String wsdlPath) {
        POMModel model = plugin.getModel();
        assert model.isIntransaction();
        Configuration config = plugin.getConfiguration();
        if (config == null) {
            config = model.getFactory().createConfiguration();
            plugin.setConfiguration(config);
            //TODO shall we add the other config elements
        }
        POMExtensibilityElement wsdlFiles = findChild(config.getConfigurationElements(), "wsdlFiles");
        if (wsdlFiles == null) {
            QName qname = POMQName.createQName("wsdlFiles", model.getPOMQNames().isNSAware()); //NOI18N
            wsdlFiles = model.getFactory().createPOMExtensibilityElement(qname);
            config.addExtensibilityElement(wsdlFiles);
        }
        QName qname = POMQName.createQName("wsdlFile", model.getPOMQNames().isNSAware()); //NOI18N
        List<POMExtensibilityElement> elems = wsdlFiles.getExtensibilityElements();
        for (POMExtensibilityElement el : elems) {
            if (qname.equals(el.getQName())) {
                if (wsdlPath.equals(el.getElementText())) {
                    //already there..
                    return;
                }
            }
        }
        POMExtensibilityElement el = model.getFactory().createPOMExtensibilityElement(qname);
        el.setElementText(wsdlPath);
        wsdlFiles.addExtensibilityElement(el);
    }
    
    public static void removeWsdlFile(POMModel model, String wsdlPath) {
        assert model.isIntransaction();
        Build bld = model.getProject().getBuild();
        if (bld == null) {
            return;
        }
        Plugin plugin = bld.findPluginById("org.codehaus.mojo", "jaxws-maven-plugin");
        if (plugin != null) {
            Configuration config = plugin.getConfiguration();
            if (config != null) {
                POMExtensibilityElement wsdlFiles = findChild(config.getConfigurationElements(), "wsdlFiles");
                if (wsdlFiles != null) {
                    List<POMExtensibilityElement> files = wsdlFiles.getExtensibilityElements();
                    for (POMExtensibilityElement el : files) {
                        if ("wsdlFile".equals(el.getQName().getLocalPart()) &&
                            wsdlPath.equals(el.getElementText())) {
                            wsdlFiles.removeExtensibilityElement(el);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    public static void renameWsdlFile(POMModel model, String oldWsdlPath, String newWsdlPath) {
        assert model.isIntransaction();
        Build bld = model.getProject().getBuild();
        if (bld == null) {
            return;
        }
        Plugin plugin = bld.findPluginById("org.codehaus.mojo", "jaxws-maven-plugin"); //NOI18N
        if (plugin != null) {
            Configuration config = plugin.getConfiguration();
            if (config != null) {
                POMExtensibilityElement wsdlFiles = findChild(config.getConfigurationElements(), "wsdlFiles");
                if (wsdlFiles != null) {
                    List<POMExtensibilityElement> files = wsdlFiles.getExtensibilityElements();
                    for (POMExtensibilityElement el : files) {
                        if ("wsdlFile".equals(el.getQName().getLocalPart()) && //NOI18N
                            oldWsdlPath.equals(el.getElementText())) {
                            el.setElementText(newWsdlPath);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    public static void addJaxws21Library(Project project) throws IOException {
        SourceGroup[] srcGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (srcGroups.length > 0) {
            ClassPath classPath = ClassPath.getClassPath(srcGroups[0].getRootFolder(), ClassPath.COMPILE);
            FileObject wsimportFO = classPath.findResource("javax/xml/ws/WebServiceFeature.class"); // NOI18N
            if (wsimportFO == null) {
                //add the JAXWS 2.1 library
                Library jaxws21_ext = LibraryManager.getDefault().getLibrary("jaxws21"); //NOI18N
                if (jaxws21_ext == null) {
                    throw new IOException("Unable to find JAXWS 21 Library."); //NOI18N
                }
                try {
                    ProjectClassPathModifier.addLibraries(new Library[] {jaxws21_ext}, srcGroups[0].getRootFolder(), ClassPath.COMPILE);
                } catch (IOException e) {
                    throw new IOException("Unable to add JAXWS 21 Library. " + e.getMessage()); //NOI18N
                }
            }
        }
    }
}
