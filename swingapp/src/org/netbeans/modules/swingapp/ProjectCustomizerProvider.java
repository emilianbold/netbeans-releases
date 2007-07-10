/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.swingapp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JComponent;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

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

    private ProjectCustomizerProvider() {
    }

    public static ProjectCustomizerProvider create() {
        return new ProjectCustomizerProvider();
    }

    public Category createCategory(Lookup context) {
        Category cat;
        Project project = context.lookup(Project.class);
        if (AppFrameworkSupport.isApplicationProject(project)) {
            cat = ProjectCustomizer.Category.create(CAT_NAME,
                    NbBundle.getMessage(ProjectCustomizerProvider.class, "CTL_ProjectCustomizerCategoryTitle"), // NOI18N
                    null, null);
            cat.setOkButtonListener(new SaveListener(project));
            // we need the save listener even if the panel is not created
            // (to get the possibly changed common application properties)
        } else {
            cat = null;
        }
        return cat;
    }

    public JComponent createComponent(Category category, Lookup context) {
        ProjectCustomizerPanel panel = new ProjectCustomizerPanel();
        ((SaveListener)category.getOkButtonListener()).panel = panel;

        Project project = context.lookup(Project.class);
        if (ProjectCustomizerPanel.fileChooserDir == null) {
            ProjectCustomizerPanel.fileChooserDir = project.getProjectDirectory().getPath();
        }
        DesignResourceMap resMap = ResourceUtils.getAppDesignResourceMap(project);
        panel.setVendorId(resMap.getString(KEY_VENDOR_ID));
        panel.setApplicationId(resMap.getString(KEY_APP_ID));
        panel.setLookAndFeel(resMap.getString(KEY_LOOK_AND_FEEL));
        return panel;
    }

    // -----

    private static class SaveListener implements ActionListener {

        private Project project;
        private ProjectCustomizerPanel panel;

        SaveListener(Project project) {
            this.project = project;
        }

        public void actionPerformed(ActionEvent e) {
            DesignResourceMap resMap = ResourceUtils.getAppDesignResourceMap(project);
            // read properties from the common Application category panel
            // and store them to the application properties file
            J2SEPropertyEvaluator propEval = project.getLookup().lookup(J2SEPropertyEvaluator.class);
            if (propEval != null) {
                PropertyEvaluator props = propEval.evaluator();
                for (String[] propNames : APP_PROPERTIES) {
                    String value = props.getProperty(propNames[0]);
                    storeValue(propNames[1], value, resMap);
                }
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

        private static void storeValue(String key, String value, DesignResourceMap resMap) {
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
    }
}
