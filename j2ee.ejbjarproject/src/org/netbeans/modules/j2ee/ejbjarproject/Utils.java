/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbjarproject;

import java.util.ArrayList;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;

public class Utils {

    private static final String WIZARD_PANEL_CONTENT_DATA = "WizardPanel_contentData"; // NOI18N
    private static final String WIZARD_PANEL_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex"; //NOI18N;

    public static String toClasspathString(File[] classpathEntries) {
        if (classpathEntries == null) {
            return "";
        }
        StringBuffer classpath = new StringBuffer();
        for (int i = 0; i < classpathEntries.length; i++) {
            classpath.append(classpathEntries[i].getAbsolutePath());
            if (i + 1 < classpathEntries.length) {
                classpath.append(":"); // NOI18N
            }
        }
        return classpath.toString();
    }

    public static void notifyError(Exception ex) {
        NotifyDescriptor ndd = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(ndd);
    }

    public static void mergeSteps(WizardDescriptor wizard, WizardDescriptor.Panel[] panels, String[] steps) {
        Object prop = wizard.getProperty (WIZARD_PANEL_CONTENT_DATA);
        String[] beforeSteps;
        int offset;
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
            offset = beforeSteps.length;
            if (offset > 0 && ("...".equals(beforeSteps[offset - 1]))) {// NOI18N
                offset--;
            }
        } else {
            beforeSteps = null;
            offset = 0;
        }
        String[] resultSteps = new String[ (offset) + panels.length];
        for (int i = 0; i < offset; i++) {
            resultSteps[i] = beforeSteps[i];
        }
        setSteps(panels, steps, resultSteps, offset);
    }

    private static void setSteps(WizardDescriptor.Panel[] panels, String[] steps, String[] resultSteps, int offset) {
        int n = steps == null ? 0 : steps.length;
        for (int i = 0; i < panels.length; i++) {
            final JComponent component = (JComponent) panels[i].getComponent();
            String step = i < n ? steps[i] : null;
            if (step == null) {
                step = component.getName();
            }
            component.putClientProperty (WIZARD_PANEL_CONTENT_DATA, resultSteps);
            component.putClientProperty(WIZARD_PANEL_CONTENT_SELECTED_INDEX, new Integer(i));
            component.getAccessibleContext().setAccessibleDescription (step);
            resultSteps[i + offset] = step;
        }
    }

    public static void setSteps(WizardDescriptor.Panel[] panels, String[] steps) {
        setSteps(panels, steps, steps, 0);
    }
    
    /**
     * Returns Java source groups for all source packages in given project.<br>
     * Doesn't include test packages.
     *
     * @param project Project to search
     * @return Array of SourceGroup. It is empty if any probelm occurs.
     */
    public static SourceGroup[] getJavaSourceGroups(Project project) {
        Sources sources = (Sources) project.getLookup().lookup(Sources.class);
        EjbJarImplementation ejbJarImpl = (EjbJarImplementation) project.getLookup().lookup(EjbJarImplementation.class);
        if (sources == null || ejbJarImpl == null) {
            return new SourceGroup[0];
        }
        FileObject[] sourceRoots = ejbJarImpl.getJavaSources();
        Set sourceRootsSet = new HashSet(Arrays.asList(sourceRoots));
        SourceGroup[] sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        List result = new ArrayList();
        for (int i = 0; i < sourceGroups.length; i++) {
            FileObject rootFolder = sourceGroups[i].getRootFolder();
            if (sourceRootsSet.contains(rootFolder)) {
                result.add(sourceGroups[i]);
            }
        }
        return (SourceGroup[]) result.toArray(new SourceGroup[result.size()]);
    }
    
}
