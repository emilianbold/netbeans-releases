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

package org.netbeans.modules.groovy.support.wizard.maven;

import java.util.List;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.POMComponentFactory;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.Project;

/**
 *
 * @author Martin Janicek
 */
public class AddEclipseCompilerPluginOperation implements ModelOperation<POMModel> {

    private static final String MAVEN_COMPILER_ARTIFACT_ID = "maven-compiler-plugin"; // NOI18N
    private static final String MAVEN_COMPILER_VERSION = "2.3.2";                     // NOI18N

    @Override
    public void performOperation(final POMModel model) {
        POMComponentFactory factory = model.getFactory();
        Project project = model.getProject();
        Build build = project.getBuild();

        if (build == null) {
            build = factory.createBuild();
        }
        if (pluginAlreadyExists(build)) {
            return;
        }

        // maven-eclipse-compiler doesn't exists --> let's create it
        build.addPlugin(createMavenEclipseCompilerPlugin(factory));
        project.setBuild(build);
    }

    private boolean pluginAlreadyExists(final Build build) {
        List<Plugin> plugins = build.getPlugins();
        if (plugins != null) {
            for (Plugin plugin : plugins) {
                if (MAVEN_COMPILER_ARTIFACT_ID.equals(plugin.getArtifactId()) &&
                    MAVEN_COMPILER_VERSION.equals(plugin.getVersion())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Plugin createMavenEclipseCompilerPlugin(final POMComponentFactory factory) {
        Plugin plugin = factory.createPlugin();
        plugin.setArtifactId(MAVEN_COMPILER_ARTIFACT_ID);
        plugin.setVersion(MAVEN_COMPILER_VERSION);
        plugin.setConfiguration(createConfiguration(factory));
        plugin.addDependency(createDependency(factory));

        return plugin;
    }

    private Configuration createConfiguration(final POMComponentFactory factory) {
        Configuration configuration = factory.createConfiguration();
        configuration.setSimpleParameter("compilerId", "groovy-eclipse-compiler");  //NOI18N
        configuration.setSimpleParameter("verbose", "true");                        //NOI18N

        return configuration;
    }

    private Dependency createDependency(final POMComponentFactory factory) {
        Dependency dependency = factory.createDependency();
        dependency.setGroupId("org.codehaus.groovy");        // NOI18N
        dependency.setArtifactId("groovy-eclipse-compiler"); // NOI18N
        dependency.setVersion("2.6.0-01");                   // NOI18N

        return dependency;
    }
}
