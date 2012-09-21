/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.groovy.extender;

import java.util.List;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.POMComponentFactory;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginExecution;
import org.netbeans.modules.maven.model.pom.Project;

/**
 * Add maven-compiler-plugin into the pom model.
 * 
 * This is necessary for compiling both Java and Groovy files together and also
 * for running mixed Java/Groovy JUnit tests.
 *
 * @author Martin Janicek
 */
public class AddMavenCompilerPlugin implements ModelOperation<POMModel> {

    private static final String MAVEN_COMPILER_ARTIFACT_ID = "maven-compiler-plugin"; // NOI18N
    private static final String MAVEN_COMPILER_VERSION = "2.3.2";                     // NOI18N

    private static final String GROOVY_ECLIPSE_COMPILER_ARTIFACT_ID = "groovy-eclipse-compiler";    // NOI18N
    private static final String GROOVY_ECLIPSE_COMPILER_GROUP_ID = "org.codehaus.groovy";           // NOI18N
    private static final String GROOVY_ECLIPSE_COMPILER_VERSION = "2.6.0-01";                       // NOI18N

    private POMComponentFactory factory;
    private Project project;


    @Override
    public void performOperation(final POMModel model) {
        factory = model.getFactory();
        project = model.getProject();
        Build build = project.getBuild();
        if (build == null) {
            build = factory.createBuild();
            project.setBuild(build);
        }
        
        Plugin plugin = mavenCompilerPluginExists(build);
        if (plugin == null) {
            build.addPlugin(createMavenEclipseCompilerPlugin());
        } else {
            Plugin newPlugin = createMavenEclipseCompilerPlugin(plugin);
            
            build.removePlugin(plugin);
            build.addPlugin(newPlugin);
        }
    }

    private Plugin mavenCompilerPluginExists(final Build build) {
        List<Plugin> plugins = build.getPlugins();
        if (plugins != null) {
            for (Plugin plugin : plugins) {
                if (MAVEN_COMPILER_ARTIFACT_ID.equals(plugin.getArtifactId()) &&
                    MAVEN_COMPILER_VERSION.equals(plugin.getVersion())) {
                    return plugin;
                }
            }
        }
        return null;
    }

    private Plugin createMavenEclipseCompilerPlugin() {
        Plugin plugin = factory.createPlugin();
        plugin.setArtifactId(MAVEN_COMPILER_ARTIFACT_ID);
        plugin.setVersion(MAVEN_COMPILER_VERSION);
        plugin.setConfiguration(createConfiguration());
        plugin.addDependency(createDependency());

        return plugin;
    }

    private Configuration createConfiguration() {
        Configuration configuration = factory.createConfiguration();
        configuration.setSimpleParameter("compilerId", GROOVY_ECLIPSE_COMPILER_ARTIFACT_ID);  //NOI18N

        return configuration;
    }

    private Dependency createDependency() {
        Dependency dependency = factory.createDependency();
        dependency.setGroupId(GROOVY_ECLIPSE_COMPILER_GROUP_ID);        // NOI18N
        dependency.setArtifactId(GROOVY_ECLIPSE_COMPILER_ARTIFACT_ID); // NOI18N
        dependency.setVersion(GROOVY_ECLIPSE_COMPILER_VERSION);                   // NOI18N

        return dependency;
    }

    private Plugin createMavenEclipseCompilerPlugin(final Plugin plugin) {
        Plugin newPlugin = factory.createPlugin();
        newPlugin.setArtifactId(plugin.getArtifactId());
        newPlugin.setGroupId(plugin.getGroupId());
        newPlugin.setVersion(plugin.getVersion());

        updateDependency(plugin, newPlugin);
        updateConfiguration(plugin, newPlugin);

        return newPlugin;
    }

    /**
     * Just find out if the groovy-eclipse-compiler dependency is already present
     * in maven-compiler-plugin and if not, create on and put it into pom.xml.
     *
     * @param plugin where we want to update groovy-eclipse-compiler dependency
     */
    private void updateDependency(
        final Plugin oldPlugin,
        final Plugin newPlugin) {
        
        List<Dependency> dependencies = oldPlugin.getDependencies();
        if (dependencies != null) {
            for (Dependency dependency : dependencies) {
                if (GROOVY_ECLIPSE_COMPILER_ARTIFACT_ID.equals(dependency.getArtifactId()) &&
                    GROOVY_ECLIPSE_COMPILER_GROUP_ID.equals(dependency.getGroupId())) {

                    // Reuse already existing dependency
                    Dependency newDependency = factory.createDependency();
                    newDependency.setArtifactId(dependency.getArtifactId());
                    newDependency.setGroupId(dependency.getGroupId());
                    newDependency.setVersion(dependency.getVersion());
                    newPlugin.addDependency(newDependency);
                    
                    return;
                }
            }
        }

        // groovy-eclipse-compiler dependency doesn't exist at the moment, let's create it
        newPlugin.addDependency(createDependency());
    }

    private void updateConfiguration(
        final Plugin oldPlugin,
        final Plugin newPlugin) {
        
        PluginExecution compileExecution = factory.createExecution();
        Configuration compileConfiguration = factory.createConfiguration();

        Configuration currentConfiguration = oldPlugin.getConfiguration();
        if (currentConfiguration != null) {
            for (POMExtensibilityElement element : currentConfiguration.getConfigurationElements()) {
                POMExtensibilityElement newElement = factory.createPOMExtensibilityElement(element.getQName());
                newElement.setElementText(element.getElementText());

                int position = 0;
                for (POMExtensibilityElement childElement : element.getAnyElements()) {
                    POMExtensibilityElement newChildElement = factory.createPOMExtensibilityElement(childElement.getQName());
                    newChildElement.setElementText(childElement.getElementText());
                    newElement.addAnyElement(newChildElement, position++);
                }

                compileConfiguration.addExtensibilityElement(newElement);
            }
        }

        compileExecution.addGoal("compile");                                // NOI18N
        compileExecution.setId("compileId");                                // NOI18N
        compileExecution.setConfiguration(compileConfiguration);
        newPlugin.addExecution(compileExecution);

        PluginExecution testCompileExecution = factory.createExecution();
        testCompileExecution.addGoal("compile");                            // NOI18N
        testCompileExecution.addGoal("testCompile");                        // NOI18N
        testCompileExecution.setId("testCompileId");                        // NOI18N
        testCompileExecution.setConfiguration(createConfiguration());
        newPlugin.addExecution(testCompileExecution);
    }
}
