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

package org.netbeans.modules.swingapp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JComponent;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

@ProjectCustomizer.CompositeCategoryProvider.Registration(projectType="org-netbeans-modules-java-j2seproject", category="Application", position=100)
public class ProjectCustomizerProvider implements ProjectCustomizer.CompositeCategoryProvider {

    private static final String CAT_NAME = "AppFramework"; // NOI18N

    private static final String KEY_VENDOR_ID = "Application.vendorId"; // NOI18N
    private static final String KEY_APP_ID = "Application.id"; // NOI18N
    private static final String KEY_LOOK_AND_FEEL = "Application.lookAndFeel"; // NOI18N

    // mapping of app properties used by j2se project to swing app framework
    private static final String[][] APP_PROPERTIES = {
        { "application.title", "Application.title" }, // NOI18N
        { "application.vendor", "Application.vendor" }, // NOI18N
        { "application.desc", "Application.description" }, // NOI18N
        { "application.homepage", "Application.homepage" }, // NOI18N
    };

    @Override
    public Category createCategory(Lookup context) {
        Category cat;
        Project project = context.lookup(Project.class);
        if (AppFrameworkSupport.isApplicationProject(project)) {
            cat = ProjectCustomizer.Category.create(CAT_NAME,
                    NbBundle.getMessage(ProjectCustomizerProvider.class, "CTL_ProjectCustomizerCategoryTitle"), // NOI18N
                    null, (ProjectCustomizer.Category[])null);
            J2SEPropertyEvaluator propEval = project.getLookup().lookup(J2SEPropertyEvaluator.class);
            if (propEval != null) {
                propEval.evaluator().addPropertyChangeListener(new ApplicationPropertyChangeListener(project));
            }
        } else {
            cat = null;
        }
        return cat;
    }

    @Override
    public JComponent createComponent(Category category, Lookup context) {
        ProjectCustomizerPanel panel = new ProjectCustomizerPanel();
        Project project = context.lookup(Project.class);
        SaveListener listener = new SaveListener(project);
        listener.panel = panel;
        category.setOkButtonListener(listener);

        if (ProjectCustomizerPanel.fileChooserDir == null) {
            File projDir = FileUtil.toFile(project.getProjectDirectory());
            ProjectCustomizerPanel.fileChooserDir = projDir.getPath();
        }
        DesignResourceMap resMap = ResourceUtils.getAppDesignResourceMap(project);
        if (resMap != null) { // Issue 134831
            panel.setVendorId(resMap.getString(KEY_VENDOR_ID));
            panel.setApplicationId(resMap.getString(KEY_APP_ID));
            panel.setLookAndFeel(resMap.getString(KEY_LOOK_AND_FEEL));        
        } else {
            panel.setReadOnly();
        }
        return panel;
    }

    static void storeValue(String key, String value, DesignResourceMap resMap) {
        ResourceValueImpl resValue = resMap.getResourceValue(key, String.class);
        if (resValue != null) {
            if (value != null) {
                resValue.setValue(value);
                resValue.setStringValue(value);
                resMap.addResourceValue(resValue);
            } else {
                resMap.removeResourceValue(resValue);
            }
        } else if (value != null) {
            resValue = new ResourceValueImpl(key, String.class,
                    value, null, value,
                    false, DesignResourceMap.APP_LEVEL, null);
            resMap.addResourceValue(resValue);
        }
    }

    // -----

    // read properties from the common Application category panel
    // and store them to the application properties file
    private static class ApplicationPropertyChangeListener implements PropertyChangeListener {
        private Project project;

        ApplicationPropertyChangeListener(Project project) {
            this.project = project;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            for (String[] propNames : APP_PROPERTIES) {
                if (propNames[0].equals(propName)) {
                    J2SEPropertyEvaluator propEval = project.getLookup().lookup(J2SEPropertyEvaluator.class);
                    PropertyEvaluator props = propEval.evaluator();
                    if (props != null) {
                        DesignResourceMap resMap = ResourceUtils.getAppDesignResourceMap(project);
                        if (resMap == null) return;
                        String value = props.getProperty(propNames[0]);
                        storeValue(propNames[1], value, resMap);
                        resMap.save();
                    }
                }
            }
        }

    }

    private static class SaveListener implements ActionListener, Runnable {

        private Project project;
        private ProjectCustomizerPanel panel;

        SaveListener(Project project) {
            this.project = project;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RequestProcessor.getDefault().post(this);
        }

        @Override
        public void run() {
            DesignResourceMap resMap = ResourceUtils.getAppDesignResourceMap(project);
            if (resMap == null) {
                return; // Issue 134831
            }

            // store app framework specific properties
            if (panel != null) {
                storeValue(KEY_VENDOR_ID, panel.getVendorId(), resMap);
                storeValue(KEY_APP_ID, panel.getApplicationId(), resMap);
                storeValue(KEY_LOOK_AND_FEEL, panel.getLookAndFeel(), resMap);
                resMap.save();

                FileObject jarRoot = panel.getLookAndFeelJAR();
                if (jarRoot != null) { // update project classpath with JAR
                    try {
                        ProjectClassPathModifier.addRoots(new java.net.URL[]{jarRoot.getURL()},
                                                  resMap.getSourceFile(),
                                                  org.netbeans.api.java.classpath.ClassPath.EXECUTE);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                        // TODO should be reported?
                    }
                }
            }
        }

    }
}
