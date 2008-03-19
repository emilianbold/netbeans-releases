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

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.customizer.PhpProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * PHP project customizer main class.
 * @author Tomas Mysik
 */
public class CustomizerProviderImpl implements CustomizerProvider {

    public static final String CUSTOMIZER_FOLDER_PATH = "Projects/org-netbeans-modules-php-project/Customizer"; //NO18N

    private static final Map<Project, Dialog> project2Dialog = new HashMap<Project, Dialog>();
    private final Project project;
    private final AntProjectHelper helper;

    public CustomizerProviderImpl(Project project, AntProjectHelper helper) {
        this.project = project;
        this.helper = helper;
    }

    public void showCustomizer() {
        showCustomizer(null);
    }

    public void showCustomizer(String preselectedCategory) {
        Dialog dialog = project2Dialog.get(project);
        if (dialog != null) {
            dialog.setVisible(true);
            return;
        }
        PhpProjectProperties uiProperties = new PhpProjectProperties((PhpProject) project);
        Lookup context = Lookups.fixed(new Object[] {
            project,
            uiProperties,
        });

        OptionListener listener = new OptionListener(project, uiProperties);
        dialog = ProjectCustomizer.createCustomizerDialog(CUSTOMIZER_FOLDER_PATH, context, preselectedCategory,
                listener, null);
        dialog.addWindowListener(listener);
        dialog.setTitle(MessageFormat.format(
                NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_Customizer_Title"),
                ProjectUtils.getInformation(project).getDisplayName()));

        project2Dialog.put(project, dialog);
        dialog.setVisible(true);
    }

    /** Listens to the actions on the Customizer's option buttons */
    private static class OptionListener extends WindowAdapter implements ActionListener {

        private final Project project;
        private final PhpProjectProperties uiProperties;

        OptionListener(Project project, PhpProjectProperties uiProperties) {
            this.project = project;
            this.uiProperties = uiProperties;
        }

        // Listening to OK button ----------------------------------------------
        public void actionPerformed( ActionEvent e ) {
            // Store the properties into project

//#95952 some users experience this assertion on a fairly random set of changes in
// the customizer, that leads me to assume that a project can be already marked
// as modified before the project customizer is shown.
//            assert !ProjectManager.getDefault().isModified(project) :
//                "Some of the customizer panels has written the changed data before OK Button was pressed. Please file it as bug."; //NOI18N
            uiProperties.save();

            // Close & dispose the the dialog
            Dialog dialog = project2Dialog.get(project);
            if (dialog != null) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }

        // Listening to window events ------------------------------------------
        @Override
        public void windowClosed(WindowEvent e) {
            project2Dialog.remove(project);
        }

        @Override
        public void windowClosing(WindowEvent e) {
            //Dispose the dialog otherwsie the {@link WindowAdapter#windowClosed}
            //may not be called
            Dialog dialog = project2Dialog.get(project);
            if (dialog != null) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }
}
