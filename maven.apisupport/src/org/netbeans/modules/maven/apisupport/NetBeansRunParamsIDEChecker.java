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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.apisupport;

import java.io.File;
import java.util.Collections;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.execute.PrerequisitesChecker;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.embedder.NBPluginParameterExpressionEvaluator;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.POMComponentFactory;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.spi.project.LookupProvider.Registration.ProjectType;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.NotifyDescriptor.Confirmation;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.maven.apisupport.Bundle.*;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginManagement;

/**
 * Ensures that {@code netbeans.run.params.ide} will be interpolated into {@code netbeans.run.params}.
 */
@ProjectServiceProvider(service=PrerequisitesChecker.class, projectTypes={
    @ProjectType(id="org-netbeans-modules-maven/" + NbMavenProject.TYPE_NBM_APPLICATION), // cf. platformActionMappings.xml
    @ProjectType(id="org-netbeans-modules-maven/" + NbMavenProject.TYPE_NBM) // cf. ideActionMappings.xml
})
public class NetBeansRunParamsIDEChecker implements PrerequisitesChecker {

    private static final String MASTER_PROPERTY = "netbeans.run.params"; // NOI18N
    static final String PROPERTY = "netbeans.run.params.ide"; // NOI18N
    private static final String ADDITIONAL_ARGUMENTS = "additionalArguments"; // NOI18N

    public @Override boolean checkRunConfig(RunConfig config) {
        String val = config.getProperties().get(PROPERTY);
        if (val == null) {
            return true;
        }
        MavenProject prj = config.getMavenProject();
        String eval;
        try {
            eval = (String) new NBPluginParameterExpressionEvaluator(prj, new Settings(), config.getProperties()).evaluate(val);
        } catch (ExpressionEvaluationException ex) {
            Exceptions.printStackTrace(ex);
            return true;
        }
        String text = null;
        for (String goal : config.getGoals()) {
            text = PluginPropertyUtils.getPluginProperty(prj, MavenNbModuleImpl.GROUPID_MOJO, MavenNbModuleImpl.NBM_PLUGIN, ADDITIONAL_ARGUMENTS, goal, "netbeans.run.params");
            if (text != null) {
                break;
            }
        }
        if (text == null) {
            text = prj.getProperties().getProperty(MASTER_PROPERTY);
        }
        if (text == null || !text.contains(eval)) {
            missingInterpolation(prj.getFile());
            return false;
        }
        return true;
    }

    @Messages({
        "# {0} - property name", "# {1} - pom.xml file", "NetBeansRunParamsIDEChecker.msg_confirm=<html>The IDE needs to define <code>$'{'{0}}</code> in order to run this action.<br>Currently your project''s plugin configuration does not interpret this variable.<br>Adjust <code>{1}</code> to use it if defined?",
        "NetBeansRunParamsIDEChecker.title_confirm=Missing Variable in POM"
    })
    private static void missingInterpolation(File pom) {
        if (DialogDisplayer.getDefault().notify(new Confirmation(NetBeansRunParamsIDEChecker_msg_confirm(PROPERTY, pom), NetBeansRunParamsIDEChecker_title_confirm(), NotifyDescriptor.OK_CANCEL_OPTION)) != NotifyDescriptor.OK_OPTION) {
            return;
        }
        Utilities.performPOMModelOperations(FileUtil.toFileObject(pom), Collections.<ModelOperation<POMModel>>singletonList(new ModelOperation<POMModel>() {
            public @Override void performOperation(POMModel model) {
                POMComponentFactory factory = model.getFactory();
                Project project = model.getProject();
                //find and remove value from additionaParameters mojo paramerer
                String val = findAndRemoveAdditionalParameters(model);
                Properties properties = project.getProperties();
                if (properties == null) {
                    properties = factory.createProperties();
                    project.setProperties(properties);
                }
                if (properties.getProperty(PROPERTY) == null) {
                    properties.setProperty(PROPERTY, "");
                }
                String args = properties.getProperty(MASTER_PROPERTY);
                String ref = "${" + PROPERTY + "}"; // NOI18N
                if (args == null) {
                    args = ref;
                } else if (!args.contains(ref)) {
                    args += " " + ref;
                }
                if (val != null) {
                    args = args + " " + val;
                }
                properties.setProperty(MASTER_PROPERTY, args);
            }

            private String findAndRemoveAdditionalParameters(POMModel model) {
                Project project = model.getProject();
                Build bld = project.getBuild();
                if (bld != null) {
                    Plugin plg = bld.findPluginById(MavenNbModuleImpl.GROUPID_MOJO, MavenNbModuleImpl.NBM_PLUGIN);
                    if (plg != null) {
                        Configuration conf = plg.getConfiguration();
                        if (conf != null) {
                            for (POMExtensibilityElement ex : conf.getConfigurationElements()) {
                                if ("additionalArguments".equals(ex.getQName().getLocalPart())) {
                                    String s = ex.getElementText();
                                    conf.removeExtensibilityElement(ex);
                                    return s;
                                }
                            }
                        }
                    }
                    PluginManagement pm = bld.getPluginManagement();
                    if (pm != null) {
                        plg = pm.findPluginById(MavenNbModuleImpl.GROUPID_MOJO, MavenNbModuleImpl.NBM_PLUGIN);
                        if (plg != null) {
                            Configuration conf = plg.getConfiguration();
                            if (conf != null) {
                                for (POMExtensibilityElement ex : conf.getConfigurationElements()) {
                                    if ("additionalArguments".equals(ex.getQName().getLocalPart())) {
                                        String s = ex.getElementText();
                                        conf.removeExtensibilityElement(ex);
                                        return s;
                                    }
                                }
                            }
                        }
                    }
                }
                //we could also check profiles content but that would be a bit messy already..
                return null;
            }
        }));
    }

}
