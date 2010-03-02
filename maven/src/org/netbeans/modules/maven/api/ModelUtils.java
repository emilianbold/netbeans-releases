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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.api;

import java.util.Collections;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.DependencyManagement;
import org.netbeans.modules.maven.model.pom.POMComponent;
import org.netbeans.modules.maven.model.pom.POMComponentFactory;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.model.pom.Repository;
import org.netbeans.modules.maven.options.MavenVersionSettings;
import org.openide.filesystems.FileObject;

/**
 * Various maven model related utilities.
 * @author mkleint
 * @author Anuradha G
 */
public final class ModelUtils {

    /**
     * 
     * @param pom       FileObject that represents POM
     * @param group     
     * @param artifact
     * @param version
     * @param type
     * @param scope
     * @param classifier
     * @param acceptNull accept null values to scope,type and classifier.
     *                   If true null values will remove corresponding tag.
     */
    public static void addDependency(FileObject pom,
            final String group,
            final String artifact,
            final String version,
            final String type,
            final String scope,
            final String classifier, final boolean acceptNull)
    {
        ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
            public void performOperation(POMModel model) {
                Dependency dep = checkModelDependency(model, group, artifact, true);
                dep.setVersion(version);
                if (acceptNull || scope != null) {
                    dep.setScope(scope);
                }
                if (acceptNull || type != null) {
                    dep.setType(type);
                }
                if (acceptNull || classifier != null) {
                    dep.setClassifier(classifier);
                }
            }
        };
        Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
    }

    public static Dependency checkModelDependency(POMModel pom, String groupId, String artifactId, boolean add) {
        Project mdl = pom.getProject();
        Dependency ret = mdl.findDependencyById(groupId, artifactId, null);
        Dependency managed = null;
        if (ret == null || ret.getVersion() == null) {
            //check dependency management section as well..
            DependencyManagement mng = mdl.getDependencyManagement();
            if (mng != null) {
                managed = mng.findDependencyById(groupId, artifactId, null);
            }
        }
        if (add && ret == null) {
            ret = mdl.getModel().getFactory().createDependency();
            ret.setGroupId(groupId);
            ret.setArtifactId(artifactId);
            mdl.addDependency(ret);
        }
        // if managed dependency section is present, return that one for editing..
        return managed == null ? ret : managed;
    }


    public static boolean hasModelDependency(POMModel mdl, String groupid, String artifactid) {
        return checkModelDependency(mdl, groupid, artifactid, false) != null;
    }

    /**
     *
     * @param mdl
     * @param url of the repository
     * @param add true == add to model, will not add if the repo is in project but not in model (eg. central repo)
     * @return null if repository with given url exists, otherwise a returned newly created item.
     */
    public static Repository addModelRepository(MavenProject project, POMModel mdl, String url) {
        if (url.contains("http://repo1.maven.org/maven2")) { //NOI18N
            return null;
        }
        List<Repository> repos = mdl.getProject().getRepositories();
        if (repos != null) {
            for (Repository r : repos) {
                if (url.equals(r.getUrl())) {
                    //already in model..either in pom.xml or added in this session.
                    return null;
                }
            }
        }
        
        @SuppressWarnings("unchecked")
        List<org.apache.maven.model.Repository> reps = project.getRepositories();
        org.apache.maven.model.Repository prjret = null;
        Repository ret = null;
        if (reps != null) {
            for (org.apache.maven.model.Repository re : reps) {
                if (url.equals(re.getUrl())) {
                    prjret = re;
                    break;
                }
            }
        }
        if (prjret == null) {
            ret = mdl.getFactory().createRepository();
            ret.setUrl(url);
            ret.setId(url);
            mdl.getProject().addRepository(ret);
        }
        return ret;
    }

    /**
     * update the source level of project to given value.
     *
     * @param handle handle which models are to be updated
     * @param sourceLevel the sourcelevel to set
     */
    public static void checkSourceLevel(ModelHandle handle, String sourceLevel) {
        String source = PluginPropertyUtils.getPluginProperty(handle.getProject(),
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, Constants.SOURCE_PARAM,
                "compile"); //NOI18N
        if (source != null && source.contains(sourceLevel)) {
            return;
        }
        POMModel mdl = handle.getPOMModel();
        Plugin old = null;
        Plugin plugin;
        Build bld = mdl.getProject().getBuild();
        if (bld != null) {
            old = bld.findPluginById(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
        } else {
            mdl.getProject().setBuild(mdl.getFactory().createBuild());
        }
        if (old != null) {
            plugin = old;
        } else {
            plugin = mdl.getFactory().createPlugin();
            plugin.setGroupId(Constants.GROUP_APACHE_PLUGINS);
            plugin.setArtifactId(Constants.PLUGIN_COMPILER);
            plugin.setVersion(MavenVersionSettings.getDefault().getVersion(MavenVersionSettings.VERSION_COMPILER));
            mdl.getProject().getBuild().addPlugin(plugin);
        }
        Configuration conf = plugin.getConfiguration();
        if (conf == null) {
            conf = mdl.getFactory().createConfiguration();
            plugin.setConfiguration(conf);
        }
        conf.setSimpleParameter(Constants.SOURCE_PARAM, sourceLevel);
        conf.setSimpleParameter(Constants.TARGET_PARAM, sourceLevel);
        handle.markAsModified(handle.getPOMModel());
    }

    /**
     * update the encoding of project to given value.
     *
     * @param handle handle which models are to be updated
     * @param enc encoding to use
     */
    public static void checkEncoding(ModelHandle handle, String enc) {
        String source = handle.getProject().getProperties().getProperty(Constants.ENCODING_PROP);
        if (source != null && source.contains(enc)) {
            return;
        }
        //new approach, assume all plugins conform to the new setting.
        Properties props = handle.getPOMModel().getProject().getProperties();
        if (props == null) {
            props = handle.getPOMModel().getFactory().createProperties();
            handle.getPOMModel().getProject().setProperties(props);
            handle.markAsModified(handle.getPOMModel());
        }
        props.setProperty(Constants.ENCODING_PROP, enc);
        boolean createPlugins = source == null;

        //check if compiler/resources plugins are configured and update them to ${project.source.encoding expression
        POMModel model = handle.getPOMModel();
        POMComponentFactory fact = model.getFactory();
        Plugin plugin;
        Plugin plugin2;
        Build bld = handle.getPOMModel().getProject().getBuild();
        if (bld == null) {
            if (createPlugins) {
                bld = fact.createBuild();
                model.getProject().setBuild(bld);
            } else {
                return;
            }
        }

        plugin = bld.findPluginById(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
        plugin2 = bld.findPluginById(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_RESOURCES);

        String compilesource = PluginPropertyUtils.getPluginProperty(handle.getProject(),
                    Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER,
                    Constants.ENCODING_PARAM, null);
        String resourcesource = PluginPropertyUtils.getPluginProperty(handle.getProject(),
                    Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_RESOURCES,
                    Constants.ENCODING_PARAM, null);

        boolean updateCompiler = createPlugins || compilesource != null; /** configured in parent somehow */
        if (plugin == null && updateCompiler) {
            plugin = fact.createPlugin();
            plugin.setGroupId(Constants.GROUP_APACHE_PLUGINS);
            plugin.setArtifactId(Constants.PLUGIN_COMPILER);
            plugin.setVersion(MavenVersionSettings.getDefault().getVersion(MavenVersionSettings.VERSION_COMPILER));
            bld.addPlugin(plugin);
        }
        if (plugin != null) {
            Configuration conf = plugin.getConfiguration();
            if (conf == null && updateCompiler) {
                conf = fact.createConfiguration();
                plugin.setConfiguration(conf);
            }
            if (conf != null && updateCompiler) {
                conf.setSimpleParameter(Constants.ENCODING_PARAM, "${" + Constants.ENCODING_PROP + "}");
            }
        }

        boolean updateResources = createPlugins || resourcesource != null; /** configured in parent somehow */
        if (plugin2 == null && updateResources) {
            plugin2 = fact.createPlugin();
            plugin2.setGroupId(Constants.GROUP_APACHE_PLUGINS);
            plugin2.setArtifactId(Constants.PLUGIN_RESOURCES);
            plugin2.setVersion(MavenVersionSettings.getDefault().getVersion(MavenVersionSettings.VERSION_RESOURCES));
            bld.addPlugin(plugin2);
        }
        if (plugin2 != null) {
            Configuration conf = plugin2.getConfiguration();
            if (conf == null && updateResources) {
                conf = fact.createConfiguration();
                plugin2.setConfiguration(conf);
            }
            if (conf != null && updateResources) {
                conf.setSimpleParameter(Constants.ENCODING_PARAM, "${" + Constants.ENCODING_PROP + "}");
            }
        }
    }

    /**
     * Returns child element of given parent, specified by its local name.
     * Creates such child in case it doesn't exist.
     *
     * @param parent parent element
     * @param localQName local name of the child
     * @param pomModel whole pom model
     * @return existing or newly created child
     */
    public static POMExtensibilityElement getOrCreateChild (POMComponent parent, String localQName, POMModel pomModel) {
        POMExtensibilityElement result = null;
        for (POMExtensibilityElement el : parent.getExtensibilityElements()) {
            if (localQName.equals(el.getQName().getLocalPart())) {
                result = el;
                break;
            }
        }

        if (result == null) {
            result = pomModel.getFactory().
                    createPOMExtensibilityElement(new QName(localQName));
            parent.addExtensibilityElement(result);
        }

        return result;
    }

}
