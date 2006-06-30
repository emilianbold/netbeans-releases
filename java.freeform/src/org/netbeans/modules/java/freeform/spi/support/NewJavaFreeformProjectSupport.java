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

package org.netbeans.modules.java.freeform.spi.support;

import java.io.IOException;
import org.netbeans.modules.java.freeform.ui.ClasspathWizardPanel;
import org.netbeans.modules.java.freeform.ui.NewJ2SEFreeformProjectWizardIterator;
import org.netbeans.modules.java.freeform.ui.ProjectModel;
import org.netbeans.modules.java.freeform.ui.SourceFoldersPanel;
import org.netbeans.modules.java.freeform.ui.SourceFoldersWizardPanel;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.WizardDescriptor;

/**
 * Support for Java New Project Wizard. These methods are typically used by the
 * freeform project extension which want to instantiate also Java development
 * support in project.
 * <div class="nonnormative">
 * <p>
 * Typical usage of these methods is:
 * </p>
 * <ol>
 * <li>create implementation of {@link org.openide.WizardDescriptor.InstantiatingIterator}
 *   with your wizard panels and add panels created by {@link #createJavaPanels}
 *   method</li>
 * <li>in implementation of {@link org.openide.WizardDescriptor.Iterator.hasNext}
 *   method call also {@link #enableNextButton}</li>
 * <li>in implementation of {@link org.openide.WizardDescriptor.InstantiatingIterator.instantiate}
 *   method call in addition also {@link #instantiateJavaPanels}</li>
 * <li>do not forget to call {@link #uninitializeJavaPanels} in your 
 *   {@link org.openide.WizardDescriptor.InstantiatingIterator.uninitialize}
 *    to clean up Java panels</li>
 * </ol>
 * </div>
 *
 * @author  David Konecny
 */
public class NewJavaFreeformProjectSupport {

    /** List of initial source folders. Type: List of String pair: [source path, its display name]*/
    public static final String PROP_EXTRA_JAVA_SOURCE_FOLDERS = "sourceFolders"; // <List<String,String>> NOI18N
    
    private NewJavaFreeformProjectSupport() {
    }
    
    /**
     * Returns array of standard Java panels suitable for new project wizard.
     * Panel gathers info about Java source folders and their classpath.
     */
    public static WizardDescriptor.Panel[] createJavaPanels() {
        return new WizardDescriptor.Panel[]{new SourceFoldersWizardPanel(), new ClasspathWizardPanel()};
    }

    /**
     * There is special logic in Java panels that Sources panel should enable 
     * Next button only when at least one source folder was specified. Wizard
     * iterator which is using panels created by createJavaPanels() method 
     * should always call this method in hasNext() method.
     */
    public static boolean enableNextButton(WizardDescriptor.Panel panel) {
        if (panel instanceof SourceFoldersWizardPanel) {
            SourceFoldersPanel sfp = (SourceFoldersPanel)panel.getComponent();
            if (!sfp.hasSomeSourceFolder()) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Update project with information gathered in Java panels, that is
     * add Java support to project. The method must to be called under 
     * ProjectManager.writeMutex.
     */
    public static void instantiateJavaPanels(AntProjectHelper helper, WizardDescriptor wiz) throws IOException {
        ProjectModel pm = (ProjectModel)wiz.getProperty(NewJ2SEFreeformProjectWizardIterator.PROP_PROJECT_MODEL);
        ProjectModel.instantiateJavaProject(helper, pm);
    }
    
    /**
     * Uninitialize Java panels after wizard was instantiated.
     */
    public static void uninitializeJavaPanels(WizardDescriptor wiz) {
        wiz.putProperty(NewJavaFreeformProjectSupport.PROP_EXTRA_JAVA_SOURCE_FOLDERS, null);
        wiz.putProperty(NewJ2SEFreeformProjectWizardIterator.PROP_PROJECT_MODEL, null);
    }
    
}
