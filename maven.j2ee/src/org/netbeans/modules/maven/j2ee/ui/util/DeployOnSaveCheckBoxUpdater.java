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

package org.netbeans.modules.maven.j2ee.ui.util;

import java.util.prefs.Preferences;
import javax.swing.JCheckBox;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.maven.api.customizer.support.CheckBoxUpdater;
import org.netbeans.modules.maven.j2ee.MavenJavaEEConstants;
import org.netbeans.modules.maven.j2ee.utils.MavenProjectSupport;

/**
 *
 * @author Martin Janicek
 */
public final class DeployOnSaveCheckBoxUpdater extends CheckBoxUpdater {

    private final Project project;
    private final boolean defaultValue;

    private DeployOnSaveCheckBoxUpdater(Project project, JCheckBox deployOnSaveCheckBox) {
        super(deployOnSaveCheckBox);
        this.project = project;
        this.defaultValue = MavenProjectSupport.isDeployOnSave(project);
    }


    /**
     * Factory method encapsulating CheckBoxUpdater creation. Typically client don't
     * want to do anything with a new instance so this makes more sense than creating
     * it using "new" keyword.
     *
     * @param project project for which we want to change DoS
     * @param deployOnSaveCheckBox Deploy on Save check box for which we want to create updater
     */
    public static void create(Project project, JCheckBox deployOnSaveCheckBox) {
        new DeployOnSaveCheckBoxUpdater(project, deployOnSaveCheckBox);
    }

    @Override
    public Boolean getValue() {
        Preferences preferences = ProjectUtils.getPreferences(project, DeployOnSaveCheckBoxUpdater.class, true);
        String value = preferences.get(MavenJavaEEConstants.HINT_DEPLOY_ON_SAVE, null);
        
        if (value != null) {
            return Boolean.parseBoolean(value);
        } else {
            return null;
        }
    }

    @Override
    public void setValue(Boolean value) {
        MavenProjectSupport.setDeployOnSave(project, value);
    }

    @Override
    public boolean getDefaultValue() {
        return defaultValue;
    }
}
