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
package org.netbeans.modules.web.clientproject.ant;

import java.io.File;
import javax.swing.JComponent;
import org.netbeans.modules.web.clientproject.indirect.AntProjectHelper;
import org.netbeans.modules.web.clientproject.indirect.IndirectServices;
import org.netbeans.modules.web.clientproject.indirect.PropertyEvaluator;
import org.netbeans.modules.web.clientproject.ui.customizer.LicensePanelSupport;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** Bridges calls into Ant.
 */
public final class AntServices extends IndirectServices {
    public AntProjectHelper createProject(FileObject dirFO, String type) {
        return ProjectGenerator.createProject(dirFO, type);
    }

    @Override
    public String getUsablePropertyName(String displayName) {
        return PropertyUtils.getUsablePropertyName(displayName);
    }

    @Override
    public File resolveFile(File dir, String relative) {
        return PropertyUtils.resolveFile(dir, relative);
    }

    @Override
    public PropertyEvaluator createEvaluator(AntProjectHelper h, FileObject dir) {
        PropertyEvaluator baseEval2 = PropertyUtils.sequentialPropertyEvaluator(
                projectHelper.getStockPropertyPreprovider(),
                projectHelper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        return PropertyUtils.sequentialPropertyEvaluator(
                projectHelper.getStockPropertyPreprovider(),
                projectHelper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH),
                PropertyUtils.userPropertiesProvider(baseEval2,
                        "user.properties.file", FileUtil.toFile(dir)), // NOI18N
                projectHelper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
    }

    @Override
    public JComponent createLicenseHeaderCustomizerPanel(ProjectCustomizer.Category category, LicensePanelSupport licenseSupport) {
        return CustomizerUtilities.createLicenseHeaderCustomizerPanel(category, licenseSupport);
    }

    @Override
    public String relativizeFile(File base, File relative) {
        return PropertyUtils.relativizeFile(base, relative);
    }
}
