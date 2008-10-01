/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.web.client.tools.api.WebClientToolsSessionStarterService;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;


/**
 *
 * @author schmidtm
 */
public class GrailsProjectCustomizerProvider implements CustomizerProvider {

    public static final String CUSTOMIZER_FOLDER_PATH = "Projects/org-netbeans-modules-groovy-grailsproject/Customizer"; //NO18N

    // Names of categories
    private static final String GENERAL_CATEGORY = "GeneralCategory";
    private static final String DEBUG_CATEGORY = "DebugCategory"; // NOI18N
    
    private final Project project;
    
    public GrailsProjectCustomizerProvider(Project project) {
        this.project = project;
    }
    
    public void showCustomizer() {
        showCustomizer(null);
    }

    public void showCustomizer(String preselectedCategory) {
        Lookup context = Lookups.fixed(project);
        OptionListener optionListener = new OptionListener(project);
        Dialog dialog = ProjectCustomizer.createCustomizerDialog(CUSTOMIZER_FOLDER_PATH, context, preselectedCategory,
                optionListener, null, null);
        dialog.addWindowListener(optionListener);
        dialog.setTitle(ProjectUtils.getInformation(project).getDisplayName());
        dialog.setVisible(true);
    }

    private class OptionListener extends WindowAdapter implements ActionListener {
        private Project project;

        OptionListener(Project project) {
            this.project = project;
        }

        public void actionPerformed(ActionEvent e) {
            // Close and dispose the dialog
            }

        @Override
        public void windowClosed(WindowEvent e) {

        }

        @Override
        public void windowClosing(WindowEvent e) {
            // Close and dispose the dialog
        }
    }
    
    // used from XML layer
    public static ProjectCustomizer.CompositeCategoryProvider createGeneral() {
        return new ProjectCustomizer.CompositeCategoryProvider() {
            public Category createCategory(Lookup context) {
                return ProjectCustomizer.Category.create(
                    GENERAL_CATEGORY, 
                    NbBundle.getMessage(GrailsProjectCustomizerProvider.class, "LBL_GeneralSettings"), //NOI18N
                    null);
            }

            public JComponent createComponent(Category category, Lookup context) {
                return new GeneralCustomizerPanel(context.lookup(Project.class));
            }
        };
    }

    // used from XML layer
    public static ProjectCustomizer.CompositeCategoryProvider createDebugging() {
        return new ProjectCustomizer.CompositeCategoryProvider() {
            public Category createCategory(Lookup context) {
                if (WebClientToolsSessionStarterService.isAvailable()) {
                    return ProjectCustomizer.Category.create(
                        DEBUG_CATEGORY,
                        NbBundle.getMessage(GrailsProjectCustomizerProvider.class, "DEBUG_CATEGORY"), // NOI18N
                        null);
                } else {
                    return null;
                }
            }

            public JComponent createComponent(Category category, Lookup context) {
                return new DebugCustomizerPanel(context.lookup(Project.class));
            }
        };
    }

}
