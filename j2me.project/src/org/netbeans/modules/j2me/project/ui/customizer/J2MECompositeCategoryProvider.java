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
package org.netbeans.modules.j2me.project.ui.customizer;

import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.j2me.project.J2MEProject;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Theofanis Oikonomou
 */
public class J2MECompositeCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String SOURCES = "Sources";
    static final String LIBRARIES = "Libraries";
    public static final String BUILD = "Build";
    private static final String PLATFORM = "Platform";
    public static final String RUN = "Run";
    private static final String APPLICATION_DESCRIPTOR = "Application Descriptor";
    private static final String COMPILING = "Compiling";
    private static final String JAVADOC = "Documenting";
    private static final String OBFUSCATING = "Obfuscating";
    private static final String SIGNING = "Signing";
    private final String name;
    private static final Map<String, J2MEProjectProperties> projectProperties = new HashMap<>();

    private J2MECompositeCategoryProvider(String name) {
        this.name = name;
    }

    @Override
    @NbBundle.Messages({"LBL_Category_Sources=Sources",
        "LBL_Category_Libraries=Libraries",
        "LBL_Category_Platform=Platform",
        "LBL_Category_Run=Run",
        "LBL_Category_Application_Descriptor=Application Descriptor",
        "LBL_Category_Compiling=Compiling",
        "LBL_Category_JavaDoc=Documenting",
        "LBL_Category_Obfuscating=Obfuscating",
        "LBL_Category_Signing=Signing"})
    public ProjectCustomizer.Category createCategory(Lookup context) {
        ProjectCustomizer.Category toReturn = null;
        final J2MEProject project = context.lookup(J2MEProject.class);
        assert project != null;
        assert name != null;
        switch (name) {
            case SOURCES:
                toReturn = ProjectCustomizer.Category.create(
                        SOURCES,
                        Bundle.LBL_Category_Sources(),
                        null);
                break;
            case LIBRARIES:
                toReturn = ProjectCustomizer.Category.create(
                        LIBRARIES,
                        Bundle.LBL_Category_Libraries(),
                        null);
                break;
            case PLATFORM:
                toReturn = ProjectCustomizer.Category.create(
                        PLATFORM,
                        Bundle.LBL_Category_Platform(),
                        null);
                break;
            case RUN:
                toReturn = ProjectCustomizer.Category.create(
                        RUN,
                        Bundle.LBL_Category_Run(),
                        null);
                break;
            case APPLICATION_DESCRIPTOR:
                toReturn = ProjectCustomizer.Category.create(
                        APPLICATION_DESCRIPTOR,
                        Bundle.LBL_Category_Application_Descriptor(),
                        null);
                break;
            case COMPILING:
                toReturn = ProjectCustomizer.Category.create(
                        COMPILING,
                        Bundle.LBL_Category_Compiling(),
                        null);
                break;
            case JAVADOC:
                toReturn = ProjectCustomizer.Category.create(
                        JAVADOC,
                        Bundle.LBL_Category_JavaDoc(),
                        null);
                break;
            case OBFUSCATING:
                toReturn = ProjectCustomizer.Category.create(
                        OBFUSCATING,
                        Bundle.LBL_Category_Obfuscating(),
                        null);
                break;
            case SIGNING:
                toReturn = ProjectCustomizer.Category.create(
                        SIGNING,
                        Bundle.LBL_Category_Signing(),
                        null);
                break;
        }
        assert toReturn != null : "No category for name:" + name;
        return toReturn;
    }

    @Override
    public JComponent createComponent(ProjectCustomizer.Category category, final Lookup context) {
        String nm = category.getName();
        final J2MEProjectProperties uiProps = context.lookup(J2MEProjectProperties.class);
        assert uiProps != null;
        switch (nm) {
            case SOURCES:
                return new J2MESourcesPanel(uiProps);
            case LIBRARIES:
                CustomizerProviderImpl.SubCategoryProvider prov = context.lookup(CustomizerProviderImpl.SubCategoryProvider.class);
                assert prov != null : "Assuming CustomizerProviderImpl.SubCategoryProvider in customizer context";
                return new J2MELibrariesPanel(uiProps, prov, category);
            case PLATFORM:
                return new J2MEPlatformPanel(uiProps);
            case RUN:
                return new J2MERunPanel(uiProps);
            case APPLICATION_DESCRIPTOR:
                return new J2MEApplicationDescriptorPanel(uiProps);
            case OBFUSCATING:
                return new J2MEObfuscatingPanel(uiProps);
            case SIGNING:
                return new J2MESigningPanel(uiProps);
            case JAVADOC:
                return new J2MEJavadocPanel(uiProps);
            case COMPILING:
                return new J2MECompilingPanel(uiProps);
        }
        return new JPanel();
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-j2me-project",
            position = 100
    )
    public static J2MECompositeCategoryProvider createSources() {
        return new J2MECompositeCategoryProvider(SOURCES);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-j2me-project",
            position = 200
    )
    public static J2MECompositeCategoryProvider createPlatform() {
        return new J2MECompositeCategoryProvider(PLATFORM);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-j2me-project",
            position = 300
    )
    public static J2MECompositeCategoryProvider createLibraries() {
        return new J2MECompositeCategoryProvider(LIBRARIES);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-j2me-project",
            position = 400)
    public static J2MECompositeCategoryProvider createApplicationDescriptor() {
        return new J2MECompositeCategoryProvider(APPLICATION_DESCRIPTOR);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-j2me-project",
            category = BUILD,
            position = 510)
    public static J2MECompositeCategoryProvider createCompiling() {
        return new J2MECompositeCategoryProvider(COMPILING);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-j2me-project",
            category = BUILD,
            position = 520)
    public static J2MECompositeCategoryProvider createSigning() {
        return new J2MECompositeCategoryProvider(SIGNING);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-j2me-project",
            category = BUILD,
            position = 530)
    public static J2MECompositeCategoryProvider createObfuscating() {
        return new J2MECompositeCategoryProvider(OBFUSCATING);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-j2me-project",
            category = BUILD,
            position = 540)
    public static J2MECompositeCategoryProvider createJavadoc() {
        return new J2MECompositeCategoryProvider(JAVADOC);
    }

    @ProjectCustomizer.CompositeCategoryProvider.Registration(
            projectType = "org-netbeans-modules-j2me-project",
            position = 600)
    public static J2MECompositeCategoryProvider createRun() {
        return new J2MECompositeCategoryProvider(RUN);
    }
}
