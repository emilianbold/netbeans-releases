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

package org.netbeans.modules.php.project.ui.customizer;

import java.util.Collections;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.editor.indent.project.api.Customizers;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik, Radek Matous
 */
public class CompositePanelProviderImpl implements ProjectCustomizer.CompositeCategoryProvider {

    public static final String CUSTOMIZER_FOLDER = "org-netbeans-modules-php-project"; // NOI18N

    public static final String SOURCES = "Sources"; // NOI18N
    public static final String RUN = "Run"; // NOI18N
    public static final String PHP_INCLUDE_PATH = "PhpIncludePath"; // NOI18N
    public static final String IGNORE_PATH = "IgnorePath"; // NOI18N
    public static final String PHP_UNIT = "PhpUnit"; // NOI18N

    private final String name;

    public CompositePanelProviderImpl(String name) {
        this.name = name;
    }

    public ProjectCustomizer.Category createCategory(Lookup context) {
        ProjectCustomizer.Category toReturn = null;
        final ProjectCustomizer.Category[] categories = null;
        if (SOURCES.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    SOURCES,
                    NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_Config_Sources"),
                    null,
                    categories);
        } else if (RUN.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    RUN,
                    NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_Config_RunConfig"),
                    null,
                    categories);
        } else if (PHP_INCLUDE_PATH.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    PHP_INCLUDE_PATH,
                    NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_Config_PhpIncludePath"),
                    null,
                    categories);
        } else if (IGNORE_PATH.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    IGNORE_PATH,
                    NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_Config_IgnorePath"),
                    null,
                    categories);
        } else if (PHP_UNIT.equals(name)) {
            toReturn = ProjectCustomizer.Category.create(
                    PHP_UNIT,
                    NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_Config_PhpUnit"),
                    null,
                    categories);
        }
        assert toReturn != null : "No category for name: " + name;
        return toReturn;
    }

    public JComponent createComponent(ProjectCustomizer.Category category, Lookup context) {
        String nm = category.getName();
        PhpProjectProperties uiProps = context.lookup(PhpProjectProperties.class);
        if (SOURCES.equals(nm)) {
            return new CustomizerSources(category, uiProps);
        } else if (RUN.equals(nm)) {
            return new CustomizerRun(uiProps, category);
        } else if (PHP_INCLUDE_PATH.equals(nm)) {
            return new CustomizerPhpIncludePath(category, uiProps);
        } else if (IGNORE_PATH.equals(nm)) {
            return new CustomizerIgnorePath(category, uiProps);
        } else if (PHP_UNIT.equals(nm)) {
            return new CustomizerPhpUnit(category, uiProps);
        }
        return new JPanel();
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = CompositePanelProviderImpl.CUSTOMIZER_FOLDER,
        position = 100
    )
    public static CompositePanelProviderImpl createSources() {
        return new CompositePanelProviderImpl(SOURCES);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = CompositePanelProviderImpl.CUSTOMIZER_FOLDER,
        position = 150
    )
    public static CompositePanelProviderImpl createRunConfig() {
        return new CompositePanelProviderImpl(RUN);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = CompositePanelProviderImpl.CUSTOMIZER_FOLDER,
        position = 200
    )
    public static CompositePanelProviderImpl createPhpIncludePath() {
        return new CompositePanelProviderImpl(PHP_INCLUDE_PATH);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = CompositePanelProviderImpl.CUSTOMIZER_FOLDER,
        position = 250
    )
    public static CompositePanelProviderImpl createIgnorePath() {
        return new CompositePanelProviderImpl(IGNORE_PATH);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = CompositePanelProviderImpl.CUSTOMIZER_FOLDER,
        position = 300
    )
    public static CompositePanelProviderImpl createPhpUnit() {
        return new CompositePanelProviderImpl(PHP_UNIT);
    }

//o.n.m.javascript.libraries     Projects/o-n-m-php-project/Customizer/o-n-m-javascript-libraries-ui-customizer-JSLibraryCustomizerProvider.instance @375
//o.n.m.web.client.tools.impl    Projects/o-n-m-php-project/Customizer/o-n-m-web-client-tools-impl-projects-DebugCustomizerPanelProvider-createPhpProjectDebug.instance @400

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
        projectType = CompositePanelProviderImpl.CUSTOMIZER_FOLDER,
        position = 1000
    )
    public static ProjectCustomizer.CompositeCategoryProvider createFormatting() {
        return Customizers.createFormattingCategoryProvider(Collections.singletonMap("allowedMimeTypes", FileUtils.PHP_MIME_TYPE)); // NOI18N
    }
}
