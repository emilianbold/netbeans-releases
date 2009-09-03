/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import org.netbeans.modules.javacard.project.ui.*;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;

import org.netbeans.modules.javacard.api.ProjectKind;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.modules.javacard.project.deps.ui.DependenciesPanel;
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

        switch (id) {
            case CUSTOMIZER_ID_DEPENDENCIES :
                return new DependenciesPanel(uiProps);
            case CUSTOMIZER_ID_COMPILING :
                return new CompilingPanel (uiProps);
            case CUSTOMIZER_ID_APPLET :
                return new AppletCustomizer((AppletProjectProperties) uiProps, category);
            case CUSTOMIZER_ID_PACKAGING :
                return new PackagingCustomizer((ClassicAppletProjectProperties) uiProps, category);
            case CUSTOMIZER_ID_RUN :
                switch (kind) {
                    case CLASSIC_APPLET :
                    case EXTENDED_APPLET :
                        return new AppletProjectCustomizerRun((AppletProjectProperties) uiProps);
                    case WEB :
                        return new WebProjectCustomizerRun ((WebProjectProperties) uiProps);
                    case EXTENSION_LIBRARY :
                    case CLASSIC_LIBRARY :
                        return new RunCustomizer (uiProps);
                    default :
                        throw new AssertionError();
                }
            case CUSTOMIZER_ID_SECURITY :
                return new SecurityCustomizer (uiProps, category);
            case CUSTOMIZER_ID_SOURCES :
                return new CustomizerSources (uiProps);
            case CUSTOMIZER_ID_WEB :
                return new WebCustomizer ((WebProjectProperties) uiProps, category);
            default :
                throw new AssertionError();
        }
    }

    public static JCProjectCategoryProvider create(FileObject fo) {
        CustomizerIDs id = CustomizerIDs.forFileName(fo.getName());
        return new JCProjectCategoryProvider (id);
    }
}
