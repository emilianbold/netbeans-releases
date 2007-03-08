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

package org.netbeans.modules.web.jsf.wizards;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.common.DelegatingWizardDescriptorPanel;
//TODO: RETOUCHE
//import org.netbeans.modules.j2ee.persistence.dd.orm.model_1_0.Entity;
//import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
//import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
//import org.netbeans.modules.j2ee.persistence.unit.PUDataObject;
//import org.netbeans.modules.j2ee.persistence.wizard.PersistenceClientEntitySelection;
//import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Pavel Buzek
 */
public class PersistenceClientIterator implements TemplateWizard.Iterator {
    
    private int index;
    private transient WizardDescriptor.Panel[] panels;
    
    public Set instantiate(TemplateWizard wizard) throws IOException {
//        List<Entity> entities = (List<Entity>) wizard.getProperty(WizardProperties.ENTITY_CLASS);
//        String jsfFolder = (String) wizard.getProperty(WizardProperties.JSF_FOLDER);
//        Project project = Templates.getProject(wizard);
//        FileObject targetFolder = Templates.getTargetFolder(wizard);
//        String controllerPkg = (String) wizard.getProperty(WizardProperties.JSF_CLASSES_PACKAGE);
//        
//        PersistenceUnit persistenceUnit = 
//                (PersistenceUnit) wizard.getProperty(org.netbeans.modules.j2ee.persistence.wizard.WizardProperties.PERSISTENCE_UNIT);
//
//        if (persistenceUnit != null){
//            ProviderUtil.addPersistenceUnit(persistenceUnit, Templates.getProject(wizard));
//        }
//        
//        for (Entity entity : entities) {
//            String entityClass = entity.getClass2();
//            String simpleClassName = JSFClientGenerator.simpleClassName(entityClass);
//            String firstLower = simpleClassName.substring(0, 1).toLowerCase() + simpleClassName.substring(1);
//            String folder = jsfFolder.endsWith("/") ? jsfFolder : jsfFolder + "/";
//            folder = folder + firstLower;
//            String controller = controllerPkg + "." + simpleClassName + "Controller";
//            JSFClientGenerator.generateJSFPages(project, entityClass, folder, controller, targetFolder);
//        }
//        
//        return Collections.singleton(DataFolder.findFolder(targetFolder));
        return null;
    }

    public void initialize(TemplateWizard wizard) {
//        index = 0;
//        // obtaining target folder
//        Project project = Templates.getProject( wizard );
//        DataFolder targetFolder=null;
//        try {
//            targetFolder = wizard.getTargetFolder();
//        } catch (IOException ex) {
//            targetFolder = DataFolder.findFolder(project.getProjectDirectory());
//        }
//        
//        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
//        
//        WizardDescriptor.Panel secondPanel = new ValidationPanel(
//                new PersistenceClientEntitySelection(NbBundle.getMessage(PersistenceClientIterator.class, "LBL_EntityClasses"),
//                        new HelpCtx(PersistenceClientIterator.class.getName() + "$PersistenceClientEntitySelection"), wizard)); // NOI18N
//        WizardDescriptor.Panel thirdPanel = new PersistenceClientSetupPanel(project, wizard);
////        WizardDescriptor.Panel javaPanel = JavaTemplates.createPackageChooser(project, sourceGroups, secondPanel);
////        panels = new WizardDescriptor.Panel[] { javaPanel };
//        panels = new WizardDescriptor.Panel[] { secondPanel, thirdPanel };
//        String names[] = new String[] {
//            NbBundle.getMessage(PersistenceClientIterator.class, "LBL_EntityClasses"),
//            NbBundle.getMessage(PersistenceClientIterator.class, "LBL_JSFPagesAndClasses")
//        };
//        wizard.putProperty("NewFileWizard_Title", 
//            NbBundle.getMessage(PersistenceClientIterator.class, "Templates/Persistence/JsfFromDB"));
//        Util.mergeSteps(wizard, panels, names);
    }

    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals (before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent ().getName ();
            }
        }
        return res;
    }
    
    public void uninitialize(TemplateWizard wiz) {
        panels = null;
    }

    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    public String name() {
        return NbBundle.getMessage (PersistenceClientIterator.class, "LBL_WizardTitle_FromEntity");
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (! hasNext ()) throw new NoSuchElementException ();
        index++;
    }

    public void previousPanel() {
        if (! hasPrevious ()) throw new NoSuchElementException ();
        index--;
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }
    
    /** 
     * A panel which checks that the target project has a valid server set
     * otherwise it delegates to the real panel.
     */
    private class ValidationPanel extends DelegatingWizardDescriptorPanel {

        private ValidationPanel(WizardDescriptor.Panel delegate) {
            super(delegate);
        }
        
        public boolean isValid() {
            Project project = getProject();
            WizardDescriptor wizardDescriptor = getWizardDescriptor();
            
            // check that this project has a valid target server
            if (!org.netbeans.modules.j2ee.common.Util.isValidServerInstance(project)) {
                wizardDescriptor.putProperty("WizardPanel_errorMessage",
                        NbBundle.getMessage(PersistenceClientIterator.class, "ERR_MissingServer")); // NOI18N
                return false;
            }

            return super.isValid();
        }
    }
}
