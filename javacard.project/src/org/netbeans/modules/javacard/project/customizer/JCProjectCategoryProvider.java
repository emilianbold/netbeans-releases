/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.javacard.project.customizer;

import javax.swing.JComponent;
import org.netbeans.modules.javacard.project.JCProjectProperties;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.netbeans.validation.api.Problem;
import org.openide.util.Lookup;

import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.modules.javacard.project.deps.ui.DependenciesPanel;
import org.netbeans.modules.javacard.spi.ProjectKind;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationGroupProvider;
import org.netbeans.validation.api.ui.ValidationUI;
import org.openide.filesystems.FileObject;

public class JCProjectCategoryProvider implements ProjectCustomizer.CompositeCategoryProvider {
    private final CustomizerIDs id;

    private JCProjectCategoryProvider(CustomizerIDs id) {
        this.id = id;
    }

    public Category createCategory(Lookup context) {
        return ProjectCustomizer.Category.create(id.name(), id.getDisplayName(),
                null);
    }

    public JComponent createComponent(Category category, Lookup context) {
        JCProjectProperties uiProps = context.lookup(JCProjectProperties.class);
        JCProject p = context.lookup(JCProject.class);
        ProjectKind kind = p.kind();
        JComponent result;
        switch (id) {
            case CUSTOMIZER_ID_DEPENDENCIES :
                result = new DependenciesPanel(uiProps);
                break;
            case CUSTOMIZER_ID_COMPILING :
                result = new CompilingPanel (uiProps);
                break;
            case CUSTOMIZER_ID_APPLET :
                result = new AppletCustomizer((AppletProjectProperties) uiProps, category);
                break;
            case CUSTOMIZER_ID_PACKAGING :
                result = new PackagingCustomizer((ClassicAppletProjectProperties) uiProps, category);
                break;
            case CUSTOMIZER_ID_RUN :
                switch (kind) {
                    case CLASSIC_APPLET :
                    case EXTENDED_APPLET :
                        result = new AppletProjectCustomizerRun((AppletProjectProperties) uiProps);
                        break;
                    case WEB :
                        result = new WebProjectCustomizerRun ((WebProjectProperties) uiProps);
                        break;
                    case EXTENSION_LIBRARY :
                    case CLASSIC_LIBRARY :
                        result = new RunCustomizer (uiProps);
                        break;
                    default :
                        throw new AssertionError();
                }
                break;
            case CUSTOMIZER_ID_SECURITY :
                result = new SecurityCustomizer (uiProps, category);
                break;
            case CUSTOMIZER_ID_SOURCES :
                result = new CustomizerSources (uiProps);
                break;
            case CUSTOMIZER_ID_WEB :
                result = new WebCustomizer ((WebProjectProperties) uiProps, category);
                break;
            default :
                throw new AssertionError();
        }
        if (result instanceof ValidationGroupProvider) {
            ValidationGroup vg = ((ValidationGroupProvider) result).getValidationGroup();
            vg.addUI(new CategoryValidationUI(category));
        }
        return result;
    }

    public static JCProjectCategoryProvider create(FileObject fo) {
        CustomizerIDs id = CustomizerIDs.forFileName(fo.getName());
        return new JCProjectCategoryProvider (id);
    }

    private static final class CategoryValidationUI implements ValidationUI {
        private final Category category;
        public CategoryValidationUI(Category category) {
            this.category = category;
        }

        @Override public void clearProblem() {
            category.setValid(true);
            category.setErrorMessage(null);
        }

        @Override public void showProblem(Problem prblm) {
            category.setValid(prblm.isFatal());
            category.setErrorMessage(prblm.getMessage());
        }
    }
}
