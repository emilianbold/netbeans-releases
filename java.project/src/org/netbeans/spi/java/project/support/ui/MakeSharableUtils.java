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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.spi.java.project.support.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;


public final class MakeSharableUtils {

    static final String PROP_LOCATION = "location"; //NOI18N
    static final String PROP_ACTIONS = "actions"; //NOI18N
    static final String PROP_HELPER = "helper"; //NOI18N
    static final String PROP_REFERENCE_HELPER = "refhelper"; //NOI18N
    static final String PROP_LIBRARIES = "libraries"; //NOI18N
    static final String PROP_JAR_REFS = "jars"; //NOI18N

    public static boolean showMakeSharableWizard(final AntProjectHelper helper, ReferenceHelper ref, List<String> libraryNames, List<String> jarReferences) {

        final WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels());
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle("Make project sharable and self-contained.");
        wizardDescriptor.putProperty(PROP_HELPER, helper);
        wizardDescriptor.putProperty(PROP_REFERENCE_HELPER, ref);
        wizardDescriptor.putProperty(PROP_LIBRARIES, libraryNames);
        wizardDescriptor.putProperty(PROP_JAR_REFS, jarReferences);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            final String loc = (String) wizardDescriptor.getProperty(PROP_LOCATION);
            assert loc != null;
            try {
                // create libraries property file if it does not exist:
                File f = new File(loc);
                if (!f.isAbsolute()) {
                    f = new File(FileUtil.toFile(helper.getProjectDirectory()), loc);
                }
                f = FileUtil.normalizeFile(f);
                if (!f.exists()) {
                    FileUtil.createData(f);
                }

                try {
                    ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {

                        public Object run() throws IOException {
                            try {
                                helper.setLibrariesLocation(loc);
                                ProjectManager.getDefault().saveProject(FileOwnerQuery.getOwner(helper.getProjectDirectory()));

                                // TODO or make just runnables?
                                List<Action> actions = (List<Action>) wizardDescriptor.getProperty(PROP_ACTIONS);
                                for (Action act : actions) {
                                    act.actionPerformed(null);
                                }
                            } catch (IllegalArgumentException ex) {
                                Exceptions.printStackTrace(ex);
                            }

                            return null;
                        }
                    });
                } catch (MutexException ex) {
                    throw (IOException) ex.getException();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }


        }
        return !cancelled;
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private static WizardDescriptor.Panel[] getPanels() {
        WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[]{
            new MakeSharableWizardPanel1(),
            new MakeSharableWizardPanel2()
        };
        String[] steps = new String[panels.length];
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            // Default step name to component name of panel. Mainly useful
            // for getting the name of the target chooser to appear in the
            // list of steps.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Sets step number of a component
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                // Sets steps names for a panel
                jc.putClientProperty("WizardPanel_contentData", steps);
                // Turn on subtitle creation on each step
                jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                // Show steps on the left side with the image on the background
                jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                // Turn on numbering of all steps
                jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
            }
        }
        return panels;
    }
}
