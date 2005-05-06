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
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.UnresolvedClass;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeAppProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.src.ClassElement;

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
    
    public static void save(ClassElement ce) {
        if (ce == null) {
            return;
        }
        SaveCookie saveCookie = (SaveCookie) ce.getCookie(SaveCookie.class);
        assert saveCookie != null: ("SaveCookie not found for " + ce.getName().getName());
        if (saveCookie != null) {
            try {
                saveCookie.save();
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ioe);
            }
        }
    }
    
    public static boolean isModified(ClassElement ce) {
        DataObject dataObject = (DataObject) ce.getCookie(DataObject.class);
        assert dataObject != null: ("DataObject not found for " + ce.getName().getName());
        return dataObject.isModified();
    }

    public static boolean areInSameJ2EEApp(Project p1, Project p2) {
        Set globalPath = GlobalPathRegistry.getDefault().getSourceRoots();
        Iterator iter = globalPath.iterator();
        while (iter.hasNext()) {
            FileObject sourceRoot = (FileObject)iter.next();
            Project project = FileOwnerQuery.getOwner(sourceRoot);
            if (project != null) {
                Object j2eeAppProvider = project.getLookup().lookup(J2eeAppProvider.class);
                if (j2eeAppProvider != null) { // == it is j2ee app
                    J2eeAppProvider j2eeApp = (J2eeAppProvider)j2eeAppProvider;
                    J2eeModuleProvider[] j2eeModules = j2eeApp.getChildModuleProviders();
                    if ((j2eeModules != null) && (j2eeModules.length > 0)) { // == there are some modules in the j2ee app
                        J2eeModuleProvider affectedPrjProvider1 = 
                                (J2eeModuleProvider)p1.getLookup().lookup(J2eeModuleProvider.class);
                        J2eeModuleProvider affectedPrjProvider2 = 
                                (J2eeModuleProvider)p2.getLookup().lookup(J2eeModuleProvider.class);
                        if (affectedPrjProvider1 != null && affectedPrjProvider2 != null) {
                            List childModules = Arrays.asList(j2eeModules);
                            if (childModules.contains(affectedPrjProvider1) &&
                                childModules.contains(affectedPrjProvider2)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static JavaClass findClass(String className, FileObject[] roots) {
        return findClass(className, ClassPathSupport.createClassPath(roots));
    }
    
    // modified version of org.netbeans.modules.j2ee.refactoring.test.util.Helper.findClass()
    public static JavaClass findClass(String className, ClassPath cp) {
        JavaClass result;
        int i = 20;
        JavaModel.getJavaRepository().beginTrans(false);
        try {
            JavaModel.setClassPath(cp);
            do {
                result = (JavaClass) JavaModel.getDefaultExtent().getType().resolve(className);
                if (result instanceof UnresolvedClass) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                i--;
            } while ((result instanceof UnresolvedClass) && i > 0);
        } finally {
            JavaModel.getJavaRepository().endTrans();
        }
        if (result instanceof UnresolvedClass) {
            throw new IllegalStateException("Class " + className + " not found.");
        }
        return result;
    }

}
