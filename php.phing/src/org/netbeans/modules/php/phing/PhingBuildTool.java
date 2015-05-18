/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.phing;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.phing.exec.PhingExecutable;
import org.netbeans.modules.php.phing.file.PhingTargets;
import org.netbeans.modules.php.phing.file.BuildXml;
import org.netbeans.modules.php.phing.preferences.PhingPreferences;
import org.netbeans.modules.php.phing.ui.customizer.PhingCustomizerProvider;
import org.netbeans.modules.php.phing.util.PhingUtils;
import org.netbeans.modules.web.clientproject.spi.build.BuildToolImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;


@ProjectServiceProvider(
        service = {
            BuildToolImplementation.class,
            PhingBuildTool.class,
        }, projectType = "org-netbeans-modules-php-project" // NOI18N
)
public final class PhingBuildTool implements BuildToolImplementation {

    private static final Logger LOGGER = Logger.getLogger(PhingBuildTool.class.getName());

    public static final String IDENTIFIER = "Phing"; // NOI18N

    private final Project project;
    private final BuildXml buildXml;
    private final PhingTargets phingTargets;
    private final PhingPreferences phingPreferences;


    public PhingBuildTool(Project project) {
        assert project != null;
        this.project = project;
        buildXml = new BuildXml(project.getProjectDirectory());
        phingTargets = PhingTargets.create(project, buildXml);
        phingPreferences = new PhingPreferences(project);
    }

    @NonNull
    public static PhingBuildTool forProject(Project project) {
        PhingBuildTool buildTool = inProject(project);
        assert buildTool != null : "PhingBuildTool should be found in project " + project.getClass().getName() + " (lookup: " + project.getLookup() + ")";
        return buildTool;
    }

    @CheckForNull
    public static PhingBuildTool inProject(Project project) {
        assert project != null;
        return project.getLookup().lookup(PhingBuildTool.class);
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @NbBundle.Messages("PhingBuildTool.name=Phing")
    @Override
    public String getDisplayName() {
        return Bundle.PhingBuildTool_name();
    }

    public BuildXml getBuildXml() {
        return buildXml;
    }

    public PhingTargets getPhingTargets() {
        return phingTargets;
    }

    public PhingPreferences getPhingPreferences() {
        return phingPreferences;
    }

    @Override
    public boolean isEnabled() {
        return buildXml.exists();
    }

    @NbBundle.Messages("PhingBuildTool.configure=Do you want to configure project actions to call Phing targets?")
    @Override
    public boolean run(String commandId, boolean waitFinished, boolean warnUser) {
        assert isEnabled() : project.getProjectDirectory().getNameExt();
        assert buildXml.exists() : project.getProjectDirectory().getNameExt();
        String phingBuild = phingPreferences.getTarget(commandId);
        if (phingBuild != null) {
            PhingExecutable phing = PhingExecutable.getDefault(project, warnUser);
            if (phing != null) {
                PhingUtils.logUsagePhingBuild();
                Future<Integer> result = phing.run(phingBuild.split(" ")); // NOI18N
                if (waitFinished) {
                    try {
                        result.get();
                    } catch (InterruptedException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                    } catch (CancellationException ex) {
                        // cancelled by user
                        LOGGER.log(Level.FINE, null, ex);
                    } catch (ExecutionException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                        if (warnUser) {
                            // XXX open customizer? show error dialog?
                        }
                    }
                }
            }
        } else if (warnUser) {
            Object option = DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Confirmation(Bundle.PhingBuildTool_configure(), NotifyDescriptor.YES_NO_OPTION));
            if (option == NotifyDescriptor.YES_OPTION) {
                project.getLookup().lookup(CustomizerProvider2.class).showCustomizer(PhingCustomizerProvider.CUSTOMIZER_IDENT, null);
            }
        }
        return true;
    }

}
