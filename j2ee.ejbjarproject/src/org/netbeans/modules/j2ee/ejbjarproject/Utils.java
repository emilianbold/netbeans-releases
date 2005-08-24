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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.jmi.javamodel.JavaModelPackage;
import org.netbeans.jmi.javamodel.Type;
import org.netbeans.jmi.javamodel.UnresolvedClass;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.modules.javacore.internalapi.JavaMetamodel;
import org.netbeans.modules.javacore.jmiimpl.javamodel.MethodImpl;
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
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.ClassDefinition;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeAppProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;


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
     * JMI transaction must be started and JMI classpath must be set to use this method
     */
    public static void save(JavaClass jc) {
        if (jc == null) {
            return;
        }
        DataObject dataObject = null;
        try {
            DataObject.find(JavaModel.getFileObject(jc.getResource()));
        } catch (DataObjectNotFoundException dnfe) {
        }
        SaveCookie saveCookie = dataObject == null ? null : (SaveCookie) dataObject.getCookie(SaveCookie.class);
        // TODO: SaveCookie - is returned if file is not modified?
//        assert saveCookie != null: ("SaveCookie not found for " + jc.getName());
        if (saveCookie != null) {
            try {
                saveCookie.save();
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, ioe);
            }
        }
    }

    /**
     * JMI transaction must be started and JMI classpath must be set to use this method
     */
    public static boolean isModified(JavaClass ce) {        
        DataObject dataObject = JavaMetamodel.getManager().getDataObject(ce.getResource());
        assert dataObject != null: ("DataObject not found for " + ce.getName());
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

    // =========================================================================

    // utils for ejb code synchronization
    
    
    
    /** Returns list of all EJB projects that can be called from the caller project.
     *
     * @param enterpriseProject the caller enterprise project
     */
    public static Project [] getCallableEjbProjects (Project enterpriseProject) {
        Project[] allProjects = OpenProjects.getDefault().getOpenProjects();
        
        boolean isCallerEJBModule = false;
        J2eeModuleProvider callerJ2eeModuleProvider = (J2eeModuleProvider) enterpriseProject.getLookup().lookup(J2eeModuleProvider.class);
        if (callerJ2eeModuleProvider != null && callerJ2eeModuleProvider.getJ2eeModule().getModuleType().equals(J2eeModule.EJB)) {
            // TODO: HACK - this should be set by calling AntArtifactQuery.findArtifactsByType(p, EjbProjectConstants.ARTIFACT_TYPE_EJBJAR)
            // but now freeform doesn't implement this correctly
            isCallerEJBModule = true;
        }
        // TODO: HACK - this must be solved by freeform's own implementation of EnterpriseReferenceContainer, see issue 57003
        // call ejb should not make this check, all should be handled in EnterpriseReferenceContainer
        boolean isCallerFreeform = enterpriseProject.getClass().getName().equals("org.netbeans.modules.ant.freeform.FreeformProject");
        
        List /*<Project>*/ filteredResults = new ArrayList(allProjects.length);
        for (int i = 0; i < allProjects.length; i++) {
            boolean isEJBModule = false;
            J2eeModuleProvider j2eeModuleProvider = (J2eeModuleProvider) allProjects[i].getLookup().lookup(J2eeModuleProvider.class);
            if (j2eeModuleProvider != null && j2eeModuleProvider.getJ2eeModule().getModuleType().equals(J2eeModule.EJB)) {
                isEJBModule = true;
            }
            if ((isEJBModule && !isCallerFreeform) ||
                (isCallerFreeform && enterpriseProject.equals(allProjects[i]))) {
                filteredResults.add(allProjects[i]);
            }
        }
        return (Project []) filteredResults.toArray(new Project[filteredResults.size()]);
    }

    // Copied from j2ee/utilities JMIUtils
    public static JavaClass findClass(String className) {
        JavaClass result = (JavaClass) resolveType(className);
        return result instanceof UnresolvedClass ? null : result;
    }

    // Copied from j2ee/utilities JMIUtils
    public static Type resolveType(String typeName) {
        Type type = JavaModel.getDefaultExtent().getType().resolve(typeName);
        if (type instanceof UnresolvedClass) {
            Type basicType = JavaModel.getDefaultExtent().getType().resolve("java.lang." + typeName);  // NOI18N;
            if (!(basicType instanceof UnresolvedClass)) {
                return basicType;
            }
        }
        return type;
    }
}
