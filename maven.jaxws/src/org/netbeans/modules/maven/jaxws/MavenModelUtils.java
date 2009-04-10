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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.websvc.wsstack.jaxws.JaxWs;
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
import org.netbeans.modules.websvc.wsstack.api.WSStack;

/**
 *
 * @author mkuchtiak
 */
public final class MavenModelUtils {

    private static final String WSIPMORT_GENERATE_PREFIF = "wsimport-generate-"; //NOI18N
    private static final String STALE_FILE_DIRECTORY = "${project.build.directory}/jaxws/stale/"; //NOI18N
    private static final String STALE_FILE_EXTENSION = ".stale"; //NOI18N
    private static final String JAXWS_GROUP_ID = "org.codehaus.mojo"; //NOI18N
    private static final String JAXWS_ARTIFACT_ID = "jaxws-maven-plugin"; //NOI18N
    private static final String JAXWS_PLUGIN_KEY = JAXWS_GROUP_ID+":"+JAXWS_ARTIFACT_ID; //NOI18N
    private static final String JAXWS_CATALOG = "jax-ws-catalog.xml"; //NOI18N

    /**
     * adds jaxws plugin, requires the model to have a transaction started,
     * eg. by calling as part of Utilities.performPOMModelOperations(ModelOperation<POMModel>)
     * @param model POMModel
     * @return JAX-WS Plugin instance
     */
    public static Plugin addJaxWSPlugin(POMModel model) {
        return MavenModelUtils.addJaxWSPlugin(model, null);
    }

    /**
     * adds jaxws plugin, requires the model to have a transaction started,
     * eg. by calling as part of Utilities.performPOMModelOperations(ModelOperation<POMModel>)
     * @param model POMModel
     * @param jaxWsVersion version of sources to generate. Value null means default version.
     * @return JAX-WS Plugin instance
     */
    public static Plugin addJaxWSPlugin(POMModel model, String jaxWsVersion) {
        assert model.isIntransaction() : "need to call model modifications under transaction."; //NOI18N
        Build bld = model.getProject().getBuild();
        if (bld == null) {
            bld = model.getFactory().createBuild();
            model.getProject().setBuild(bld);
        }
        Plugin plugin = bld.findPluginById(JAXWS_GROUP_ID, JAXWS_ARTIFACT_ID);
        if (plugin != null) {
            //TODO CHECK THE ACTUAL PARAMETER VALUES..
            return plugin;
        }
        plugin = model.getFactory().createPlugin();
        plugin.setGroupId(JAXWS_GROUP_ID);
        plugin.setArtifactId(JAXWS_ARTIFACT_ID);
        plugin.setVersion("1.10"); //NOI18N
        bld.addPlugin(plugin);

        // setup global configuration
        Configuration config = plugin.getConfiguration();
        if (config == null) {
            config = model.getFactory().createConfiguration();
            plugin.setConfiguration(config);
        }
        config.setSimpleParameter("sourceDestDir", "${project.build.directory}/generated-sources/jaxws-wsimport"); //NOI18N
        config.setSimpleParameter("xnocompile", "true"); //NOI18N
        config.setSimpleParameter("verbose", "true"); //NOI18N
        config.setSimpleParameter("extension", "true"); //NOI18N
        config.setSimpleParameter("catalog", "${basedir}/" + MavenJAXWSSupportImpl.CATALOG_PATH);
        if (jaxWsVersion != null) {
            config.setSimpleParameter("target", jaxWsVersion); //NOI18N
        }
        Dependency webservicesDep = model.getFactory().createDependency();
        webservicesDep.setGroupId("javax.xml"); //NOI18N
        webservicesDep.setArtifactId("webservices-api"); //NOI18N
        webservicesDep.setVersion("1.4"); //NOI18N
        plugin.addDependency(webservicesDep);
        
        return plugin; 
    }

    /** Adds WAR plugin.
     *
     * @param model
     * @return WAR plugin
     */
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
       //check for resource containing jax-ws-catalog.xml
        if (!hasResource(webResources, JAXWS_CATALOG)) {
            POMExtensibilityElement  res = model.getFactory().createPOMExtensibilityElement(
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
            include.setElementText(JAXWS_CATALOG);
            in.addExtensibilityElement(include);
            include = model.getFactory().createPOMExtensibilityElement(POMQName.createQName("include",
                    model.getPOMQNames().isNSAware()));
            include.setElementText("wsdl/**");
            in.addExtensibilityElement(include);
        }
        return plugin; 
    }

    /** Adds wsdl Resource.
     *
     * @param handle ModelHandle object
     */
    
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

    private static POMExtensibilityElement findElementForValue(List<POMExtensibilityElement> elems, String value) {
        for (POMExtensibilityElement e : elems) {
            if (value.equals(e.getElementText())) {
                return e;
            }
        }
        return null;
    }

    public static void addWsimportExecution(Plugin plugin, String id, String wsdlPath) {
        POMModel model = plugin.getModel();
        assert model.isIntransaction();

        PluginExecution exec = model.getFactory().createExecution();
        exec.setId(WSIPMORT_GENERATE_PREFIF+id);
        exec.setPhase("generate-sources"); //NOI18N
        exec.addGoal("wsimport"); //NOI18N
        plugin.addExecution(exec);

        Configuration config = model.getFactory().createConfiguration();
        exec.setConfiguration(config);

        QName qname = POMQName.createQName("wsdlFiles", model.getPOMQNames().isNSAware()); //NOI18N
        POMExtensibilityElement wsdlFiles = model.getFactory().createPOMExtensibilityElement(qname);
        config.addExtensibilityElement(wsdlFiles);

        qname = POMQName.createQName("wsdlFile", model.getPOMQNames().isNSAware()); //NOI18N
        POMExtensibilityElement wsdlFile = model.getFactory().createPOMExtensibilityElement(qname);
        wsdlFile.setElementText(wsdlPath);
        wsdlFiles.addExtensibilityElement(wsdlFile);

        qname = POMQName.createQName("staleFile", model.getPOMQNames().isNSAware()); //NOI18N
        POMExtensibilityElement staleFile = model.getFactory().createPOMExtensibilityElement(qname);
        staleFile.setElementText(STALE_FILE_DIRECTORY+id+STALE_FILE_EXTENSION);
        config.addExtensibilityElement(staleFile);
    }

    public static void addBindingFile(POMModel model, String id, String bindingFilePath) {
        assert model.isIntransaction();
        Build bld = model.getProject().getBuild();
        if (bld == null) {
            return;
        }
        Plugin plugin = bld.findPluginById(JAXWS_GROUP_ID, JAXWS_ARTIFACT_ID);
        if (plugin != null) {
            List<PluginExecution> executions = plugin.getExecutions();
            String execId = WSIPMORT_GENERATE_PREFIF+id;
            for (PluginExecution exec : executions) {
                if (execId.equals(exec.getId())) {
                    Configuration config = exec.getConfiguration();
                    if (config != null) {
                        QName qname = POMQName.createQName("bindingDirectory", model.getPOMQNames().isNSAware()); //NOI18N
                        if (config.getChildElementText(qname) == null) {
                            POMExtensibilityElement bindingDir = model.getFactory().createPOMExtensibilityElement(qname);
                            bindingDir.setElementText("${basedir}/src/jaxws-bindings");
                            config.addExtensibilityElement(bindingDir);
                        }
                        POMExtensibilityElement bindingFiles =
                                findChild(config.getConfigurationElements(), "bindingFiles"); //NOI18N
                        if (bindingFiles == null) {
                            qname = POMQName.createQName("bindingFiles", model.getPOMQNames().isNSAware()); //NOI18N
                            bindingFiles = model.getFactory().createPOMExtensibilityElement(qname);
                            config.addExtensibilityElement(bindingFiles);
                        }

                        POMExtensibilityElement bindingFile =
                                findElementForValue(bindingFiles.getExtensibilityElements(), bindingFilePath);
                        if (bindingFile == null) {
                            qname = POMQName.createQName("bindingFile", model.getPOMQNames().isNSAware()); //NOI18N
                            bindingFile = model.getFactory().createPOMExtensibilityElement(qname);
                            bindingFile.setElementText(bindingFilePath);
                            bindingFiles.addExtensibilityElement(bindingFile);
                        }
                    }
                    break;
                }
            }
        }
    }

    public static void removeWsimportExecution(POMModel model, String id) {
        assert model.isIntransaction();
        Build bld = model.getProject().getBuild();
        if (bld == null) {
            return;
        }
        Plugin plugin = bld.findPluginById(JAXWS_GROUP_ID, JAXWS_ARTIFACT_ID);
        if (plugin != null) {
            List<PluginExecution> executions = plugin.getExecutions();
            for (PluginExecution exec : executions) {
                String execId = WSIPMORT_GENERATE_PREFIF+id;
                if (execId.equals(exec.getId())) {
                    plugin.removeExecution(exec);
                    break;
                }
            }
        }
    }

    public static void renameWsdlFile(POMModel model, String oldId, String newId, String oldWsdlPath, String newWsdlPath) {
        assert model.isIntransaction();
        Build bld = model.getProject().getBuild();
        if (bld == null) {
            return;
        }
        Plugin plugin = bld.findPluginById(JAXWS_GROUP_ID, JAXWS_ARTIFACT_ID);
        if (plugin != null) {
            List<PluginExecution> executions = plugin.getExecutions();
            String execId = WSIPMORT_GENERATE_PREFIF+oldId;
            for (PluginExecution exec : executions) {
                Configuration config = exec.getConfiguration();
                if (config != null && execId.equals(exec.getId())) {
                    // replace wsdlFile element
                    POMExtensibilityElement wsdlFiles = findChild(config.getConfigurationElements(), "wsdlFiles"); //NOI18N
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
                    // replace staleFile element
                    POMExtensibilityElement staleFile = findChild(config.getConfigurationElements(), "staleFile"); //NOI18N
                    if (staleFile != null) {
                        staleFile.setElementText(STALE_FILE_DIRECTORY+newId+STALE_FILE_EXTENSION);
                    }
                    // replace exec id
                    exec.setId(WSIPMORT_GENERATE_PREFIF+newId);
                    break;
                }
            }
        }
    }

    /** Adds JAX-WS 2.1 Library.
     *
     * @param project Project
     * @throws java.io.IOException throws when Library cannot be found
     */
    public static boolean addJaxws21Library(Project project) throws IOException {
        SourceGroup[] srcGroups = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (srcGroups.length > 0) {
            ClassPath classPath = ClassPath.getClassPath(srcGroups[0].getRootFolder(), ClassPath.COMPILE);
            FileObject wsimportFO = classPath.findResource("javax/xml/ws/WebServiceFeature.class"); // NOI18N
            if (wsimportFO == null) {
                //add the Metro library
                Library metroLib = LibraryManager.getDefault().getLibrary("metro"); //NOI18N
                if (metroLib == null) {
                    throw new IOException("Unable to find Metro Library."); //NOI18N
                }
                try {
                    ProjectClassPathModifier.addLibraries(new Library[] {metroLib},
                            srcGroups[0].getRootFolder(),
                            ClassPath.COMPILE);
                    return true;
                } catch (IOException e) {
                    throw new IOException("Unable to add Metro Library. " + e.getMessage()); //NOI18N
                }
            }
        }
        return false;
    }

    /** get list of wsdl files in Maven project
     *
     * @param project Maven project instance
     * @return list of wsdl files
     */
    static List<WsimportPomInfo> getWsdlFiles(Project project) {
        MavenProject mavenProject = project.getLookup().lookup(NbMavenProject.class).getMavenProject();
        assert mavenProject != null;
        @SuppressWarnings("unchecked")
        List<org.apache.maven.model.Plugin> plugins = mavenProject.getBuildPlugins();
        List<WsimportPomInfo> wsdlList = new ArrayList<WsimportPomInfo>();
        for (org.apache.maven.model.Plugin plg : plugins) {
            if (JAXWS_PLUGIN_KEY.equalsIgnoreCase(plg.getKey())) {
                @SuppressWarnings("unchecked")
                List<org.apache.maven.model.PluginExecution> executions = plg.getExecutions();
                for (org.apache.maven.model.PluginExecution exec : executions) {
                    Xpp3Dom conf =  (Xpp3Dom)exec.getConfiguration();
                    if (conf != null) {
                        Xpp3Dom wsdlFiles = conf.getChild("wsdlFiles"); //NOI18N
                        if (wsdlFiles != null) {
                            Xpp3Dom wsdlFile = wsdlFiles.getChild("wsdlFile"); //NOI18N
                            if (wsdlFile != null) {
                                WsimportPomInfo pomInfo = new WsimportPomInfo(wsdlFile.getValue());
                                // detect handler binding file
                                Xpp3Dom bindingFiles = conf.getChild("bindingFiles"); //NOI18N
                                if (bindingFiles != null) {
                                    String bindingPath = findHandler(bindingFiles);
                                    if (bindingPath != null) {
                                        pomInfo.setHandlerFile(bindingPath);
                                    }
                                }
                                wsdlList.add(pomInfo);
                            }
                        }
                    }
                }
            }
        }
        return wsdlList;
    }

    private static String findHandler(Xpp3Dom parent) {
        for (Xpp3Dom child : parent.getChildren("bindingFile")) { //NOI18N
            String bindingPath = child.getValue();
            if (bindingPath != null && bindingPath.endsWith("_handler.xml")) { //NOI18N
                return bindingPath;
            }
        }
        return null;
    }

    private static void updateLibraryScope(POMModel model, String targetScope) {
        assert model.isIntransaction() : "need to call model modifications under transaction."; //NOI18N
        Dependency wsDep = model.getProject().findDependencyById("com.sun.xml.ws", "webservices-rt", null); //NOI18N
        if (wsDep != null) {
            wsDep.setScope(targetScope);
        }
    }

    /** Update dependency scope for webservices-rt.
     *
     * @param prj Project
     */
    static void reactOnServerChanges(final Project prj) {
        NbMavenProject nb = prj.getLookup().lookup(NbMavenProject.class);
        @SuppressWarnings("unchecked")
        List<org.apache.maven.model.Dependency> deps = nb.getMavenProject().getDependencies();
        String metroScope = null;
        boolean foundMetroDep = false;
        for (org.apache.maven.model.Dependency dep:deps) {
            if ("com.sun.xml.ws".equals(dep.getGroupId()) && "webservices-rt".equals(dep.getArtifactId())) { //NOI18N
                String scope = dep.getScope();
                metroScope = scope == null ? "compile" : scope; //NOI18N
                foundMetroDep = true;
                break;
            }
        }
        String updateScopeTo = null;
        if (foundMetroDep) {
            WSStack<JaxWs> wsStack = new WSStackUtils(prj).getWsStack(JaxWs.class);
            if (wsStack != null) {
                if (wsStack.isFeatureSupported(JaxWs.Feature.WSIT)) {
                    if ("compile".equals(metroScope)) { //NOI18N
                        updateScopeTo = "provided"; //NOI18N
                    }
                } else {
                    if ("provided".equals(metroScope)) {
                        updateScopeTo = "compile"; //NOI18N
                    }
                }
            } else {
                if ("compile".equals(metroScope)) { //NOI18N
                    updateScopeTo = "provided"; //NOI18N
                }
            }
            if (updateScopeTo != null) {
                final String targetScope = updateScopeTo;
                ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                    public void performOperation(POMModel model) {
                        // update webservices-rt library dependency scope (provided or compile)
                        // depending whether J2EE Server contains metro jars or not
                        updateLibraryScope(model, targetScope);
                    }
                };
                Utilities.performPOMModelOperations(prj.getProjectDirectory().getFileObject("pom.xml"), //NOI18N
                        Collections.singletonList(operation));
            }
        }
    }

    private static boolean hasResource(POMExtensibilityElement webResources, String resourceName) {
        List<POMExtensibilityElement> resources = webResources.getChildren(POMExtensibilityElement.class);
        for (POMExtensibilityElement res : resources) {
           POMExtensibilityElement includesEl = findChild(res.getExtensibilityElements(), "includes"); //NOI18N
           if (includesEl != null) {
               List<POMExtensibilityElement> includes = includesEl.getChildren(POMExtensibilityElement.class);
               for (POMExtensibilityElement include : includes) {
                   if (resourceName.equals(include.getElementText())) {
                       return true;
                   }
               }
           }
        }
        return false;
    }

}
