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

package org.netbeans.modules.maven.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Repository;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.NBPluginParameterExpressionEvaluator;
import org.netbeans.modules.maven.options.MavenVersionSettings;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.project.Project;
import org.openide.util.Exceptions;

/**
 *
 * @author mkleint
 */
public class PluginPropertyUtils {

    
    /** Creates a new instance of PluginPropertyUtils */
    private PluginPropertyUtils() {
    }
    
    
    /**
     * tries to figure out if the property of the given plugin is customized in the
     * current project and returns it's value if so, otherwise null
     */
    public static String getPluginProperty(Project prj, String groupId, String artifactId, String property, String goal) {
        NbMavenProjectImpl project = prj.getLookup().lookup(NbMavenProjectImpl.class);
        assert project != null : "Requires a maven project instance"; //NOI18N
        return getPluginProperty(project.getOriginalMavenProject(), groupId, artifactId, property, goal);
    }
    /**
     * tries to figure out if the property of the given plugin is customized in the
     * current project and returns it's value if so, otherwise null
     */
    public static String getPluginProperty(MavenProject prj, String groupId, String artifactId, String property, String goal) {
        String toRet = null;
        if (prj.getBuildPlugins() == null) {
            return toRet;
        }
        for (Object obj : prj.getBuildPlugins()) {
            Plugin plug = (Plugin)obj;
            if (artifactId.equals(plug.getArtifactId()) &&
                   groupId.equals(plug.getGroupId())) {
                if (plug.getExecutions() != null) {
                    for (Object obj2 : plug.getExecutions()) {
                        PluginExecution exe = (PluginExecution)obj2;
                        if (exe.getGoals().contains(goal)) {
                            toRet = checkConfiguration(prj, exe.getConfiguration(), property);
                            if (toRet != null) {
                                break;
                            }
                        }
                    }
                }
                if (toRet == null) {
                    toRet = checkConfiguration(prj, plug.getConfiguration(), property);
                }
            }
        }
        if (toRet == null && 
                //TODO - the plugin configuration probably applies to 
                //lifecycle plugins only. always checking is wrong, how to get a list of lifecycle plugins though?
                (Constants.PLUGIN_COMPILER.equals(artifactId) || //NOI18N
                 Constants.PLUGIN_SUREFIRE.equals(artifactId) || //NOI18N
                 Constants.PLUGIN_RESOURCES.equals(artifactId))) {  //NOI18N
            if (prj.getPluginManagement() != null) {
                for (Object obj : prj.getPluginManagement().getPlugins()) {
                    Plugin plug = (Plugin)obj;
                    if (artifactId.equals(plug.getArtifactId()) &&
                        groupId.equals(plug.getGroupId())) {
                        toRet = checkConfiguration(prj, plug.getConfiguration(), property);
                        break;
                    }
                }
            }
        }
        return toRet;
    }
    
    private static String checkConfiguration(MavenProject prj, Object conf, String property) {
        if (conf != null && conf instanceof Xpp3Dom) {
            Xpp3Dom dom = (Xpp3Dom)conf;
            Xpp3Dom source = dom.getChild(property);
            if (source != null) {
                NBPluginParameterExpressionEvaluator eval = new NBPluginParameterExpressionEvaluator(prj, EmbedderFactory.getProjectEmbedder().getSettings(), new Properties());
                try {
                    Object evaluated = eval.evaluate(source.getValue().trim());
                    return evaluated != null ? ("" + evaluated) : source.getValue().trim(); //NOI18N
                } catch (ExpressionEvaluationException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return source.getValue().trim();
            }
        }
        return null;
    }
    

    /**
     * gets the list of values for the given property, if configured in the current project.
     * @param multiproperty list's root element (eg. "sourceRoots")
     * @param singleproperty - list's single value element (eg. "sourceRoot")
     */
    public static String[] getPluginPropertyList(Project prj, String groupId, String artifactId, String multiproperty, String singleproperty, String goal) {
        NbMavenProjectImpl project = prj.getLookup().lookup(NbMavenProjectImpl.class);
        assert project != null : "Requires a maven project instance"; //NOI18N
        return getPluginPropertyList(project.getOriginalMavenProject(), groupId, artifactId, multiproperty, singleproperty, goal);
    }
    
    /**
     * gets the list of values for the given property, if configured in the current project.
     * @param multiproperty list's root element (eg. "sourceRoots")
     * @param singleproperty - list's single value element (eg. "sourceRoot")
     */
    public static String[] getPluginPropertyList(MavenProject prj, String groupId, String artifactId, String multiproperty, String singleproperty, String goal) {
        String[] toRet = null;
        if (prj.getBuildPlugins() == null) {
            return toRet;
        }
        for (Object obj : prj.getBuildPlugins()) {
            Plugin plug = (Plugin)obj;
            if (artifactId.equals(plug.getArtifactId()) &&
                   groupId.equals(plug.getGroupId())) {
                if (plug.getExecutions() != null) {
                    for (Object obj2 : plug.getExecutions()) {
                        PluginExecution exe = (PluginExecution)obj2;
                        if (exe.getGoals().contains(goal)) {
                            toRet = checkListConfiguration(prj, exe.getConfiguration(), multiproperty, singleproperty);
                            if (toRet != null) {
                                break;
                            }
                        }
                    }
                }
                if (toRet == null) {
                    toRet = checkListConfiguration(prj, plug.getConfiguration(), multiproperty, singleproperty);
                }
            }
        }
        if (toRet == null && 
                //TODO - the plugin configuration probably applies to 
                //lifecycle plugins only. always checking is wrong, how to get a list of lifecycle plugins though?
                (Constants.PLUGIN_COMPILER.equals(artifactId) || //NOI18N
                 Constants.PLUGIN_SUREFIRE.equals(artifactId) || //NOI18N
                 Constants.PLUGIN_RESOURCES.equals(artifactId))) {  //NOI18N
            if (prj.getPluginManagement() != null) {
                for (Object obj : prj.getPluginManagement().getPlugins()) {
                    Plugin plug = (Plugin)obj;
                    if (artifactId.equals(plug.getArtifactId()) &&
                        groupId.equals(plug.getGroupId())) {
                        toRet = checkListConfiguration(prj, plug.getConfiguration(), multiproperty, singleproperty);
                        break;
                    }
                }
            }
        }
        return toRet;
    }
    
    private static String[] checkListConfiguration(MavenProject prj, Object conf, String multiproperty, String singleproperty) {
        if (conf != null && conf instanceof Xpp3Dom) {
            Xpp3Dom dom = (Xpp3Dom)conf;
            Xpp3Dom source = dom.getChild(multiproperty);
            if (source != null) {
                List<String> toRet = new ArrayList<String>();
                Xpp3Dom[] childs = source.getChildren(singleproperty);
                NBPluginParameterExpressionEvaluator eval = new NBPluginParameterExpressionEvaluator(prj, EmbedderFactory.getProjectEmbedder().getSettings(), new Properties());
                for (Xpp3Dom ch : childs) {
                    try {
                        Object evaluated = eval.evaluate(ch.getValue().trim());
                        toRet.add(evaluated != null ? ("" + evaluated) : ch.getValue().trim());  //NOI18N
                    } catch (ExpressionEvaluationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                return toRet.toArray(new String[toRet.size()]);
            }
        }
        return null;
    }    
    
    
    /**
     * 
     * @param mdl 
     * @param groupId 
     * @param artifactId 
     * @param add true == add to model, always returns a non-null value then.
     * @return 
     */
    public static Dependency checkModelDependency(Model mdl, String groupId, String artifactId, boolean add) {
        List deps = mdl.getDependencies();
        Dependency ret = null;
        Dependency managed = null;
        if (deps != null) {
            Iterator it = deps.iterator();
            while (it.hasNext()) {
                Dependency d = (Dependency)it.next();
                if (groupId.equalsIgnoreCase(d.getGroupId()) && artifactId.equalsIgnoreCase(d.getArtifactId())) {
                    ret = d;
                    break;
                }
            }
        }
        if (ret == null || ret.getVersion() == null) {
            //check dependency management section as well..
            DependencyManagement mng = mdl.getDependencyManagement();
            if (mng != null) {
                deps = mng.getDependencies();
                if (deps != null) {
                    Iterator it = deps.iterator();
                    while (it.hasNext()) {
                        Dependency d = (Dependency)it.next();
                        if (groupId.equalsIgnoreCase(d.getGroupId()) && artifactId.equalsIgnoreCase(d.getArtifactId())) {
                            managed = d;
                            break;
                        }
                    }
                }
            }
        }
        if (add && ret == null) {
            ret = new Dependency();
            ret.setGroupId(groupId);
            ret.setArtifactId(artifactId);
            mdl.addDependency(ret);
        }
        // if managed dependency section is present, return that one for editing..
        return managed == null ? ret : managed;
    }
    
    public static boolean hasModelDependency(Model mdl, String groupid, String artifactid) {
        return checkModelDependency(mdl, groupid, artifactid, false) != null;
    }

    /**
     * 
     * @param mdl 
     * @param url of the repository 
     * @param add true == add to model, will not add if the repo is in project but not in model (eg. central repo)
     * @return 
     */
    public static Repository checkModelRepository(MavenProject project, Model mdl, String url, boolean add) {
        if (url.contains("http://repo1.maven.org/maven2")) { //NOI18N
            return null;
        }
        for (Object rr : mdl.getRepositories()) {
            Repository r = (Repository)rr;
            if (url.equals(r.getUrl())) {
                //already in model..either in pom.xml or added in this session.
                return null;
            }
        }
        List reps = project.getRepositories();
        Repository prjret = null;
        Repository ret = null;
        if (reps != null) {
            Iterator it = reps.iterator();
            while (it.hasNext()) {
                Repository re = (Repository)it.next();
                if (url.equals(re.getUrl())) {
                    prjret = re;
                    break;
                }
            }
        }
        //now find the correct instance in model
        if (prjret != null) {
            reps = mdl.getRepositories();
            if (reps != null) {
                Iterator it = reps.iterator();
                while (it.hasNext()) {
                    Repository re = (Repository)it.next();
                    if (re.getId().equals(prjret.getId())) {
                        ret = re;
                        break;
                    }
                }
            }
        }
        if (add && ret == null && prjret == null) {
            ret = new Repository();
            ret.setUrl(url);
            ret.setId(url);
            mdl.addRepository(ret);
        }
        return ret;
    }

    public static boolean hasModelRepository(MavenProject project, Model mdl, String url) {
        return checkModelRepository(project, mdl, url, false) != null;
    }

    
    private static final String CONFIGURATION_EL = "configuration";//NOI18N
    
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
        Plugin plugin = new Plugin();
        plugin.setGroupId(Constants.GROUP_APACHE_PLUGINS);
        plugin.setArtifactId(Constants.PLUGIN_COMPILER);
        plugin.setVersion(MavenVersionSettings.getDefault().getVersion(MavenVersionSettings.VERSION_COMPILER));
        Plugin old = null;
        Build bld = handle.getPOMModel().getBuild();
        if (bld != null) {
            old = (Plugin) bld.getPluginsAsMap().get(plugin.getKey());
        } else {
            handle.getPOMModel().setBuild(new Build());
        }
        if (old != null) {
            plugin = old;
        } else {
            handle.getPOMModel().getBuild().addPlugin(plugin);
        }
        Xpp3Dom dom = (Xpp3Dom) plugin.getConfiguration();
        if (dom == null) {
            dom = new Xpp3Dom(CONFIGURATION_EL);
            plugin.setConfiguration(dom);
        }
        Xpp3Dom dom2 = dom.getChild(Constants.SOURCE_PARAM);
        if (dom2 == null) {
            dom2 = new Xpp3Dom(Constants.SOURCE_PARAM);
            dom.addChild(dom2);
        }
        dom2.setValue(sourceLevel);
        
        dom2 = dom.getChild(Constants.TARGET_PARAM);
        if (dom2 == null) {
            dom2 = new Xpp3Dom(Constants.TARGET_PARAM);
            dom.addChild(dom2);
        }
        dom2.setValue(sourceLevel);
        handle.markAsModified(handle.getPOMModel());
    }
    
    /**
     * update the encoding of project to given value.
     * 
     * @param handle handle which models are to be updated
     * @param enc encoding to use
     */
    public static void checkEncoding(ModelHandle handle, String enc) {
        boolean wasProperty = false;
        String source = handle.getProject().getProperties().getProperty(Constants.ENCODING_PROP);
        if (source == null) {
            source = PluginPropertyUtils.getPluginProperty(handle.getProject(), 
                    Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, 
                    Constants.ENCODING_PARAM, null);
        } else {
            wasProperty = true;
        }
        if (source != null && source.contains(enc)) {
            return;
        }
        if (wasProperty) {
            //new approach, assume all plugins conform to the new setting.
            handle.getPOMModel().getProperties().setProperty(Constants.ENCODING_PROP, enc);
            //do not bother configuring the plugins in this case.
        } else {
            Plugin plugin = new Plugin();
            plugin.setGroupId(Constants.GROUP_APACHE_PLUGINS);
            plugin.setArtifactId(Constants.PLUGIN_COMPILER);
            plugin.setVersion(MavenVersionSettings.getDefault().getVersion(MavenVersionSettings.VERSION_COMPILER));
            Plugin plugin2 = new Plugin();
            plugin2.setGroupId(Constants.GROUP_APACHE_PLUGINS);
            plugin2.setArtifactId(Constants.PLUGIN_RESOURCES);
            plugin2.setVersion(MavenVersionSettings.getDefault().getVersion(MavenVersionSettings.VERSION_RESOURCES));
            Plugin old = null;
            Plugin old2 = null;
            Build bld = handle.getPOMModel().getBuild();
            if (bld != null) {
                old = (Plugin) bld.getPluginsAsMap().get(plugin.getKey());
                old2 = (Plugin) bld.getPluginsAsMap().get(plugin2.getKey());
            } else {
                handle.getPOMModel().setBuild(new Build());
            }
            if (old != null) {
                plugin = old;
            } else {
                handle.getPOMModel().getBuild().addPlugin(plugin);
            }
            if (old2 != null) {
                plugin2 = old2;
            } else {
                handle.getPOMModel().getBuild().addPlugin(plugin2);
            }
            Xpp3Dom dom = (Xpp3Dom) plugin.getConfiguration();
            if (dom == null) {
                dom = new Xpp3Dom(CONFIGURATION_EL);
                plugin.setConfiguration(dom);
            }
            Xpp3Dom dom2 = dom.getChild(Constants.ENCODING_PARAM);
            if (dom2 == null) {
                dom2 = new Xpp3Dom(Constants.ENCODING_PARAM);
                dom.addChild(dom2);
            }
            dom2.setValue(enc);

            dom = (Xpp3Dom) plugin2.getConfiguration();
            if (dom == null) {
                dom = new Xpp3Dom(CONFIGURATION_EL);
                plugin2.setConfiguration(dom);
            }
            dom2 = dom.getChild(Constants.ENCODING_PARAM);
            if (dom2 == null) {
                dom2 = new Xpp3Dom(Constants.ENCODING_PARAM);
                dom.addChild(dom2);
            }
            dom2.setValue(enc);
        }
        handle.markAsModified(handle.getPOMModel());
    }
    
    
//    /**
//     * 
//     * @param mdl 
//     * @param groupId 
//     * @param artifactId 
//     * @param profileId 
//     * @param add true == add to model, always returns a non-null value then.
//     * @return 
//     */
//    public static Plugin checkModelPlugin(Model mdl, String groupId, String artifactId, String profileId, boolean add) {
//        Plugin ret = null;
//        Profile prof = null;
//        if (profileId != null) {
//            List lst = mdl.getProfiles();
//            if (lst != null) {
//                Iterator it = lst.iterator();
//                while (it.hasNext()) {
//                    Profile p = (Profile)it.next();
//                    if (profileId.equalsIgnoreCase(p.getId())) {
//                        prof = p;
//                        break;
//                    }
//                }
//            }
//            //TODO
//        }
//        
//        //TODO
//        
//        if (add && ret == null) {
//            ret = new Plugin();
//            ret.setGroupId(groupId);
//            ret.setArtifactId(artifactId);
//            if (profileId == null) {
//                Build bld = mdl.getBuild();
//                if (bld == null) {
//                    bld = new Build();
//                    mdl.setBuild(bld);
//                }
//                bld.addPlugin(ret);
//            } else {
//                if (prof == null) {
//                    prof = new Profile();
//                    prof.setId(profileId);
//                    mdl.addProfile(prof);
//                }
//                BuildBase bld = prof.getBuild();
//                if (bld == null) {
//                    bld = new BuildBase();
//                    prof.setBuild(bld);
//                }
//                bld.addPlugin(ret);
//            }
//        }
//        return ret;
//    }
    
}
