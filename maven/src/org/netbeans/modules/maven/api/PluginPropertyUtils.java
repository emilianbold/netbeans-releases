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

package org.netbeans.modules.maven.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.ReportSet;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.NBPluginParameterExpressionEvaluator;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Examines a POM for configuration of plugins.
 */
public class PluginPropertyUtils {

    private PluginPropertyUtils() {
    }

    private static final List<String> LIFECYCLE_PLUGINS = Arrays.asList(
                Constants.PLUGIN_COMPILER,
                Constants.PLUGIN_SUREFIRE,
                Constants.PLUGIN_EAR,
                Constants.PLUGIN_JAR,
                Constants.PLUGIN_WAR,
                Constants.PLUGIN_RESOURCES
            );
    
    /**
     * tries to figure out if the property of the given plugin is customized in the
     * current project and returns it's value if so, otherwise null
     * @deprecated use the variant with expressionProperty value
     */
    @Deprecated
    public static @CheckForNull String getPluginProperty(@NonNull Project prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String parameter, @NullAllowed String goal) {
       return getPluginProperty(prj, groupId, artifactId, parameter, goal, null);
    }
    
    /**
     * tries to figure out if the property of the given plugin is customized in the
     * current project and returns it's value if so, otherwise null
     * @param parameter the name of the plugin parameter to look for
     * @param expressionProperty expression property that once defined (and plugin configuration is omited) is used.
     */
    public static @CheckForNull String getPluginProperty(@NonNull Project prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String parameter, @NullAllowed String goal, @NullAllowed String expressionProperty) {
        NbMavenProjectImpl project = prj.getLookup().lookup(NbMavenProjectImpl.class);
        assert project != null : "Requires a maven project instance"; //NOI18N
        return getPluginPropertyImpl(project.getOriginalMavenProject(), createEvaluator(project), groupId, artifactId, parameter, goal, expressionProperty);
    }    

    /**
     * tries to figure out if the property of the given plugin is customized in the
     * current project and returns it's value if so, otherwise null
     * Please NOTE that if you have access to <code>Project</code> instance and your <code>MavenProject</code> is the project's own loaded one, then
     * the variant with <code>Project</code> as parameter is preferable. Faster and less prone to deadlock.     
     * @deprecated use the variant with expressionProperty value
     */
    @Deprecated
    public static @CheckForNull String getPluginProperty(@NonNull MavenProject prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String parameter, @NullAllowed String goal) {
        return getPluginProperty(prj, groupId, artifactId, parameter, goal, null);
    }
    
    public static @CheckForNull String getPluginProperty(@NonNull MavenProject prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String parameter, @NullAllowed String goal, @NullAllowed String expressionProperty) {
        return getPluginPropertyImpl(prj, createEvaluator(prj), groupId, artifactId, parameter, goal, expressionProperty);
    }

    private static @CheckForNull String getPluginPropertyImpl(@NonNull MavenProject prj, @NonNull ExpressionEvaluator eval, @NonNull String groupId, @NonNull String artifactId, @NonNull String parameter, @NullAllowed String goal, @NullAllowed String expressionProperty) {
        String toRet = null;
        if (prj.getBuildPlugins() == null) {
            return toRet;
        }
        for (Plugin plug : prj.getBuildPlugins()) {
            if (artifactId.equals(plug.getArtifactId()) &&
                   groupId.equals(plug.getGroupId())) {
                for (PluginExecution exe : getPluginExecutions(plug, goal)) {
                    toRet = checkConfiguration(eval, exe.getConfiguration(), parameter);
                    if (toRet != null) {
                        break;
                    }
                }
                if (toRet == null) {
                    toRet = checkConfiguration(eval, plug.getConfiguration(), parameter);
                }
            }
        }
        if (toRet == null && 
                //TODO - the plugin configuration probably applies to 
                //lifecycle plugins only. always checking is wrong, how to get a list of lifecycle plugins though?
                LIFECYCLE_PLUGINS.contains(artifactId)) {  //NOI18N
            if (prj.getPluginManagement() != null) {
                for (Plugin plug : prj.getPluginManagement().getPlugins()) {
                    if (artifactId.equals(plug.getArtifactId()) &&
                        groupId.equals(plug.getGroupId())) {
                        toRet = checkConfiguration(eval, plug.getConfiguration(), parameter);
                        break;
                    }
                }
            }
        }
        if (toRet == null && expressionProperty != null) {
            Properties prop = prj.getProperties();
            if (prop != null) {
                toRet = prop.getProperty(expressionProperty);                
            }
        }
        return toRet;
    }

    /**
     * tries to figure out if the property of the given report plugin is customized in the
     * current project and returns it's value if so, otherwise null
     */
    public static @CheckForNull String getReportPluginProperty(@NonNull Project prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String property, @NullAllowed String report) {
        NbMavenProjectImpl project = prj.getLookup().lookup(NbMavenProjectImpl.class);
        assert project != null : "Requires a maven project instance"; //NOI18N
        return getReportPluginPropertyImpl(project.getOriginalMavenProject(), createEvaluator(project), groupId, artifactId, property, report);
    }

    /**
     * tries to figure out if the property of the given report plugin is customized in the
     * current project and returns it's value if so, otherwise null
     * Please NOTE that if you have access to <code>Project</code> instance and your <code>MavenProject</code> is the project's own loaded one, then
     * the variant with <code>Project</code> as parameter is preferable. Faster and less prone to deadlock.     
     */
    public static @CheckForNull String getReportPluginProperty(@NonNull MavenProject prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String property, @NullAllowed String report) {
        return getReportPluginPropertyImpl(prj, createEvaluator(prj), groupId, artifactId, property, report);
    }

    private static @CheckForNull String getReportPluginPropertyImpl(@NonNull MavenProject prj, @NonNull ExpressionEvaluator eval, @NonNull String groupId, @NonNull String artifactId, @NonNull String property, @NullAllowed String report) {
        String toRet = null;
        for (ReportPlugin plug : getEffectiveReportPlugins(prj)) {
            if (artifactId.equals(plug.getArtifactId()) &&
                   groupId.equals(plug.getGroupId())) {
                if (plug.getReportSets() != null) {
                    for (ReportSet exe : plug.getReportSets()) {
                        if (exe.getReports().contains(report)) {
                            toRet = checkConfiguration(eval, exe.getConfiguration(), property);
                            if (toRet != null) {
                                break;
                            }
                        }
                    }
                }
                if (toRet == null) {
                    toRet = checkConfiguration(eval, plug.getConfiguration(), property);
                }
            }
        }
        return toRet;
    }


    /**
     * tries to figure out if the a plugin is defined in the project
     * and return the version declared.
     * @return version string or null
     */
    public static @CheckForNull String getPluginVersion(@NonNull MavenProject prj, @NonNull String groupId, @NonNull String artifactId) {
        String toRet = null;
        if (prj.getBuildPlugins() == null) {
            return toRet;
        }
        for (Plugin plug : prj.getBuildPlugins()) {
            if (artifactId.equals(plug.getArtifactId()) &&
                   groupId.equals(plug.getGroupId())) {
                toRet = plug.getVersion();
            }
        }
        if (toRet == null &&
                //TODO - the plugin configuration probably applies to
                //lifecycle plugins only. always checking is wrong, how to get a list of lifecycle plugins though?
                LIFECYCLE_PLUGINS.contains(artifactId)) {  //NOI18N
            if (prj.getPluginManagement() != null) {
                for (Plugin plug : prj.getPluginManagement().getPlugins()) {
                    if (artifactId.equals(plug.getArtifactId()) &&
                        groupId.equals(plug.getGroupId())) {
                        toRet = plug.getVersion();
                        break;
                    }
                }
            }
        }
        return toRet;
    }

    /**
     * Like {@link #getPluginVersion} but for report plugins.
     * @since 2.32
     */
    public static @CheckForNull String getReportPluginVersion(@NonNull MavenProject prj, @NonNull String groupId, @NonNull String artifactId) {
        for (ReportPlugin plug : getEffectiveReportPlugins(prj)) {
            if (groupId.equals(plug.getGroupId()) && artifactId.equals(plug.getArtifactId())) {
                return plug.getVersion();
            }
        }
        return null;
    }
    
    private static @CheckForNull String checkConfiguration(@NonNull ExpressionEvaluator eval, @NullAllowed Object conf, @NonNull String property) {
        if (conf != null) {
            Xpp3Dom dom = (Xpp3Dom) conf; // MNG-4862
            Xpp3Dom source = dom.getChild(property);
            if (source != null) {
                String value = source.getValue();
                if (value == null) {
                    return null;
                }
                String valueT = value.trim();
                try {
                    Object evaluated = eval.evaluate(valueT);
                    return evaluated != null ? evaluated.toString() : valueT;
                } catch (ExpressionEvaluationException ex) {
                    Exceptions.printStackTrace(ex);
                    return valueT;
                }
            }
        }
        return null;
    }
    

    /**
     * gets the list of values for the given property, if configured in the current project.
     * @param multiproperty list's root element (eg. "sourceRoots")
     * @param singleproperty - list's single value element (eg. "sourceRoot")
     */
    public static @CheckForNull String[] getPluginPropertyList(@NonNull Project prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String multiproperty, @NonNull String singleproperty, @NullAllowed String goal) {
        NbMavenProjectImpl project = prj.getLookup().lookup(NbMavenProjectImpl.class);
        assert project != null : "Requires a maven project instance"; //NOI18N
        return getPluginPropertyListImpl(project.getOriginalMavenProject(), createEvaluator(project), groupId, artifactId, multiproperty, singleproperty, goal);
    }

    /**
     * gets the list of values for the given property, if configured in the current project.
     * Please NOTE that if you have access to <code>Project</code> instance and your <code>MavenProject</code> is the project's own loaded one, then
     * the variant with <code>Project</code> as parameter is preferable. Faster and less prone to deadlock.     
     * @param multiproperty list's root element (eg. "sourceRoots")
     * @param singleproperty - list's single value element (eg. "sourceRoot")
     */
    public static @CheckForNull String[] getPluginPropertyList(@NonNull MavenProject prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String multiproperty, @NonNull String singleproperty, @NullAllowed String goal) {
        return getPluginPropertyListImpl(prj, createEvaluator(prj), groupId, artifactId, multiproperty, singleproperty, goal);
    }

    private static @CheckForNull String[] getPluginPropertyListImpl(@NonNull MavenProject prj, @NonNull ExpressionEvaluator eval, @NonNull String groupId, @NonNull String artifactId, @NonNull String multiproperty, @NonNull String singleproperty, @NullAllowed String goal) {
        String[] toRet = null;
        if (prj.getBuildPlugins() == null) {
            return toRet;
        }
        for (Plugin plug : prj.getBuildPlugins()) {
            if (artifactId.equals(plug.getArtifactId()) &&
                   groupId.equals(plug.getGroupId())) {
                for (PluginExecution exe : getPluginExecutions(plug, goal)) {
                    toRet = checkListConfiguration(eval, exe.getConfiguration(), multiproperty, singleproperty);
                    if (toRet != null) {
                        break;
                    }
                }
                if (toRet == null) {
                    toRet = checkListConfiguration(eval, plug.getConfiguration(), multiproperty, singleproperty);
                }
            }
        }
        if (toRet == null &&
                //TODO - the plugin configuration probably applies to
                //lifecycle plugins only. always checking is wrong, how to get a list of lifecycle plugins though?
                LIFECYCLE_PLUGINS.contains(artifactId)) {  //NOI18N
            if (prj.getPluginManagement() != null) {
                for (Plugin plug : prj.getPluginManagement().getPlugins()) {
                    if (artifactId.equals(plug.getArtifactId()) &&
                        groupId.equals(plug.getGroupId())) {
                        toRet = checkListConfiguration(eval, plug.getConfiguration(), multiproperty, singleproperty);
                        break;
                    }
                }
            }
        }
        return toRet;
    }

    /**
     * gets the list of values for the given property, if configured in the current project.
     * @param multiproperty list's root element (eg. "sourceRoots")
     * @param singleproperty - list's single value element (eg. "sourceRoot")
     */
    public static @CheckForNull String[] getReportPluginPropertyList(@NonNull Project prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String multiproperty, @NonNull String singleproperty, @NullAllowed String goal) {
        NbMavenProjectImpl project = prj.getLookup().lookup(NbMavenProjectImpl.class);
        assert project != null : "Requires a maven project instance"; //NOI18N
        return getReportPluginPropertyListImpl(project.getOriginalMavenProject(), createEvaluator(project), groupId, artifactId, multiproperty, singleproperty, goal);
    }

    /**
     * gets the list of values for the given property, if configured in the current project.
     * Please NOTE that if you have access to <code>Project</code> instance and your <code>MavenProject</code> is the project's own loaded one, then
     * the variant with <code>Project</code> as parameter is preferable. Faster and less prone to deadlock.     
     * @param multiproperty list's root element (eg. "sourceRoots")
     * @param singleproperty - list's single value element (eg. "sourceRoot")
     */
    public static @CheckForNull String[] getReportPluginPropertyList(@NonNull MavenProject prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String multiproperty, @NonNull String singleproperty, @NullAllowed String goal) {
        return getReportPluginPropertyListImpl(prj, createEvaluator(prj), groupId, artifactId, multiproperty, singleproperty, goal);
    }

    private static @CheckForNull String[] getReportPluginPropertyListImpl(@NonNull MavenProject prj, @NonNull ExpressionEvaluator eval, @NonNull String groupId, @NonNull String artifactId, @NonNull String multiproperty, @NonNull String singleproperty, @NullAllowed String goal) {
        String[] toRet = null;
        for (ReportPlugin plug : getEffectiveReportPlugins(prj)) {
            if (artifactId.equals(plug.getArtifactId()) &&
                   groupId.equals(plug.getGroupId())) {
                if (plug.getReportSets() != null) {
                    for (ReportSet exe : plug.getReportSets()) {
                        if (exe.getReports().contains(goal)) {
                            toRet = checkListConfiguration(eval, exe.getConfiguration(), multiproperty, singleproperty);
                            if (toRet != null) {
                                break;
                            }
                        }
                    }
                }
                if (toRet == null) {
                    toRet = checkListConfiguration(eval, plug.getConfiguration(), multiproperty, singleproperty);
                }
            }
        }
        if (toRet == null) {  //NOI18N
            if (prj.getPluginManagement() != null) {
                for (Plugin plug : prj.getPluginManagement().getPlugins()) {
                    if (artifactId.equals(plug.getArtifactId()) &&
                        groupId.equals(plug.getGroupId())) {
                        toRet = checkListConfiguration(eval, plug.getConfiguration(), multiproperty, singleproperty);
                        break;
                    }
                }
            }
        }
        return toRet;
    }


    private static @CheckForNull String[] checkListConfiguration(ExpressionEvaluator eval, @NullAllowed Object conf, @NonNull String multiproperty, @NonNull String singleproperty) {
        if (conf != null) {
            Xpp3Dom dom = (Xpp3Dom) conf; // MNG-4862
            Xpp3Dom source = dom.getChild(multiproperty);
            if (source != null) {
                List<String> toRet = new ArrayList<String>();
                Xpp3Dom[] childs = source.getChildren(singleproperty);
                for (Xpp3Dom ch : childs) {
                    try {
                        String chvalue = ch.getValue()==null ? "" : ch.getValue().trim();  //NOI18N
                        Object evaluated = eval.evaluate(chvalue);
                        toRet.add(evaluated != null ? ("" + evaluated) : chvalue);  //NOI18N
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
     * Loads name/value pairs from plugin configuration.
     * Two syntaxes are supported:
     * {@code <params><key>value</key><flag/></params>} produces {@code {key=value, flag=}}
     * (last value is {@code ""} not {@code null} due to {@link Properties} limitation);
     * {@code <params><param><name>key</name><value>value</value></param></params>} produces {@code {key=value}}.
     * @return properties
     */
    public static @CheckForNull Properties getPluginPropertyParameter(@NonNull Project prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String propertyParameter, @NullAllowed String goal) {
        NbMavenProjectImpl project = prj.getLookup().lookup(NbMavenProjectImpl.class);
        assert project != null : "Requires a maven project instance"; //NOI18N
        return getPluginPropertyParameterImpl(project.getOriginalMavenProject(), createEvaluator(project), groupId, artifactId, propertyParameter, goal);
    }
    /**
     * Loads name/value pairs from plugin configuration. 
     * Please NOTE that if you have access to <code>Project</code> instance and your <code>MavenProject</code> is the project's own loaded one, then
     * the variant with <code>Project</code> as parameter is preferable. Faster and less prone to deadlock.
     * Two syntaxes are supported:
     * {@code <params><key>value</key><flag/></params>} produces {@code {key=value, flag=}}
     * (last value is {@code ""} not {@code null} due to {@link Properties} limitation);
     * {@code <params><param><name>key</name><value>value</value></param></params>} produces {@code {key=value}}.
     * @return properties
     */
    public static @CheckForNull Properties getPluginPropertyParameter(@NonNull MavenProject prj, @NonNull String groupId, @NonNull String artifactId, @NonNull String propertyParameter, @NullAllowed String goal) {
        return getPluginPropertyParameterImpl(prj, createEvaluator(prj), groupId, artifactId, propertyParameter, goal);
    }

    private static @CheckForNull Properties getPluginPropertyParameterImpl(@NonNull MavenProject prj, @NonNull ExpressionEvaluator eval, @NonNull String groupId, @NonNull String artifactId, @NonNull String propertyParameter, @NullAllowed String goal) {
        //TODO we might need to merge the props from various locations..
        Properties toRet = null;
        if (prj.getBuildPlugins() == null) {
            return toRet;
        }
        for (Plugin plug : prj.getBuildPlugins()) {
            if (artifactId.equals(plug.getArtifactId()) &&
                   groupId.equals(plug.getGroupId())) {
                for (PluginExecution exe : getPluginExecutions(plug, goal)) {
                    toRet = checkPropertiesConfiguration(eval, exe.getConfiguration(), propertyParameter);
                    if (toRet != null) {
                        break;
                    }
                }
                if (toRet == null) {
                    toRet = checkPropertiesConfiguration(eval, plug.getConfiguration(), propertyParameter);
                }
            }
        }
        if (toRet == null && 
                //TODO - the plugin configuration probably applies to 
                //lifecycle plugins only. always checking is wrong, how to get a list of lifecycle plugins though?
                LIFECYCLE_PLUGINS.contains(artifactId)) {  //NOI18N
            if (prj.getPluginManagement() != null) {
                for (Plugin plug : prj.getPluginManagement().getPlugins()) {
                    if (artifactId.equals(plug.getArtifactId()) &&
                        groupId.equals(plug.getGroupId())) {
                        toRet = checkPropertiesConfiguration(eval, plug.getConfiguration(), propertyParameter);
                        break;
                    }
                }
            }
        }
        return toRet;
    }
    
    private static @CheckForNull Properties checkPropertiesConfiguration(@NonNull ExpressionEvaluator eval, @NullAllowed Object conf, @NonNull String propertyParameter) {
        if (conf != null) {
            Xpp3Dom dom = (Xpp3Dom) conf; // MNG-4862
            Xpp3Dom source = dom.getChild(propertyParameter);
            if (source != null) {
                Properties toRet = new Properties();
                Xpp3Dom[] childs = source.getChildren();
                for (Xpp3Dom ch : childs) {
                    try {
                        String val = ch.getValue();
                        if (val == null) {
                            //#168036
                            //we have the "property" named element now.
                            if (ch.getChildCount() == 2) {
                                Xpp3Dom nameDom = ch.getChild("name"); //NOI18N
                                Xpp3Dom valueDom = ch.getChild("value"); //NOI18N
                                if (nameDom != null && valueDom != null) {
                                    String name = nameDom.getValue();
                                    String value = valueDom.getValue();
                                    Object evaluated = eval.evaluate(value);
                                    if (name != null && value != null) {
                                        toRet.put(name, evaluated != null ? ("" + evaluated) : value);  //NOI18N
                                    }
                                }
                            }
                            // #153063, #187648
                            toRet.put(ch.getName(), "");
                            continue;
                        }
                        Object evaluated = eval.evaluate(val.trim());
                        toRet.put(ch.getName(), evaluated != null ? ("" + evaluated) : ch.getValue().trim());  //NOI18N
                    } catch (ExpressionEvaluationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                return toRet;
            }
        }
        return null;
    }

    /**
     * Should handle both deprecated 2.x-style report section, and 3.x-style Site Plugin config.
     * https://jira.codehaus.org/browse/MSITE-484 and https://jira.codehaus.org/browse/MSITE-443 if and when implemented may require updates.
     */
    private static @NonNull Iterable<ReportPlugin> getEffectiveReportPlugins(@NonNull MavenProject prj) {
        List<ReportPlugin> plugins = new ArrayList<ReportPlugin>();
        for (Plugin plug : prj.getBuildPlugins()) {
            if (Constants.GROUP_APACHE_PLUGINS.equals(plug.getGroupId()) && Constants.PLUGIN_SITE.equals(plug.getArtifactId())) {
                Xpp3Dom cfg = (Xpp3Dom) plug.getConfiguration(); // MNG-4862
                if (cfg == null) {
                    continue;
                }
                Xpp3Dom reportPlugins = cfg.getChild("reportPlugins");
                if (reportPlugins == null) {
                    continue;
                }
                for (Xpp3Dom plugin : reportPlugins.getChildren("plugin")) {
                    ReportPlugin p = new ReportPlugin();
                    Xpp3Dom groupId = plugin.getChild("groupId");
                    if (groupId != null) {
                        p.setGroupId(groupId.getValue());
                    }
                    Xpp3Dom artifactId = plugin.getChild("artifactId");
                    if (artifactId != null) {
                        p.setArtifactId(artifactId.getValue());
                    }
                    Xpp3Dom version = plugin.getChild("version");
                    if (version != null) {
                        p.setVersion(version.getValue());
                    }
                    p.setConfiguration(plugin.getChild("configuration"));
                    // XXX reportSets
                    // maven-site-plugin does not appear to apply defaults from plugin.xml (unlike 2.x?)
                    plugins.add(p);
                }
            }
        }
        @SuppressWarnings("deprecation") List<ReportPlugin> m2Plugins = prj.getReportPlugins();
        plugins.addAll(m2Plugins);
        return plugins;
    }
/**
     * Evaluator usable for interpolating variables in plugin properties.
     * @since 2.57
     */
    public static @NonNull ExpressionEvaluator createEvaluator(@NonNull Project project) {
        //ugly
        Settings ss = EmbedderFactory.getProjectEmbedder().getSettings();
        ss.setLocalRepository(EmbedderFactory.getProjectEmbedder().getLocalRepository().getBasedir());

        NbMavenProjectImpl prj = project.getLookup().lookup(NbMavenProjectImpl.class);
        assert prj != null;
        return new NBPluginParameterExpressionEvaluator(
                prj.getOriginalMavenProject(),
                ss,
                prj.createSystemPropsForPropertyExpressions());
    }
    
    /**
     * Evaluator usable for interpolating variables in plugin properties.
     * Please NOTE that if you have access to <code>Project</code> instance, then
     * the variant with <code>Project</code> as parameter is preferable. Faster and less prone to deadlock.     
     * @since 2.32
     */
    public static @NonNull ExpressionEvaluator createEvaluator(@NonNull MavenProject prj) {
        Map<? extends String,? extends String> props = Collections.emptyMap();
        File basedir = prj.getBasedir();
        if (basedir != null) {
        FileObject bsd = FileUtil.toFileObject(FileUtil.normalizeFile(basedir));
        if (bsd != null) {
            Project p = FileOwnerQuery.getOwner(bsd);
            if (p != null) {
                NbMavenProjectImpl project = p.getLookup().lookup(NbMavenProjectImpl.class);
                if (project != null) {
                    props = project.createSystemPropsForPropertyExpressions();
                }
            }
        }
        }
        //ugly
        Settings ss = EmbedderFactory.getProjectEmbedder().getSettings();
        ss.setLocalRepository(EmbedderFactory.getProjectEmbedder().getLocalRepository().getBasedir());

        return new NBPluginParameterExpressionEvaluator(
                prj,
                ss,
                props);
    }

    /** @see org.apache.maven.lifecycle.internal.DefaultLifecycleExecutionPlanCalculator */
    private static @NonNull List<PluginExecution> getPluginExecutions(@NonNull Plugin plug, @NullAllowed String goal) {
        if (goal == null) {
            return Collections.emptyList();
        }
        List<PluginExecution> exes = new ArrayList<PluginExecution>();
        for (PluginExecution exe : plug.getExecutions()) {
            if (exe.getGoals().contains(goal) || /* #179328: Maven 2.2.0+ */ ("default-" + goal).equals(exe.getId())) {
                exes.add(exe);
            }
        }
        Collections.sort(exes, new Comparator<PluginExecution>() {
            @Override public int compare(PluginExecution e1, PluginExecution e2) {
                return e2.getPriority() - e1.getPriority();
            }
        });
        return exes;
    }

}
