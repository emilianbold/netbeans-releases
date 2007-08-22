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

package org.netbeans.modules.websvc.rest.wizard;

import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.codegen.EntityResourcesGenerator;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBeanModel;
import org.netbeans.modules.websvc.rest.support.PersistenceHelper;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Peter Liu
 */
public class EntityResourcesIterator implements TemplateWizard.Iterator {
    
    private int index;
    private transient WizardDescriptor.Panel[] panels;
    private RequestProcessor.Task transformTask;
    
    public Set instantiate(TemplateWizard wizard) throws IOException {
        final Project project = Templates.getProject(wizard);
        FileObject targetFolder = Templates.getTargetFolder(wizard);
        String targetPackage = SourceGroupSupport.packageForFolder(targetFolder);
        String resourcePackage = (String) wizard.getProperty(WizardProperties.RESOURCE_PACKAGE);
        String converterPackage = (String) wizard.getProperty(WizardProperties.CONVERTER_PACKAGE);
        EntityResourceBeanModel model = (EntityResourceBeanModel) wizard.getProperty(WizardProperties.ENTITY_RESOURCE_MODEL);
        String puName = (String) wizard.getProperty(WizardProperties.PERSISTENCE_UNIT_NAME);
        
        // Add the entity classes to persistence.xml,
        // Note: this is a work-around for TopLink PM implementation not compliant to persistence.xml schema.
        PersistenceHelper.addEntityClasses(project, model.getBuilder().getAllEntityNames());
        
        final EntityResourcesGenerator generator = new EntityResourcesGenerator(
                model, targetFolder, targetPackage, resourcePackage, converterPackage, puName);
        final ProgressDialog progressDialog = new ProgressDialog(NbBundle.getMessage(
                EntityResourcesIterator.class,
                "LBL_RestSevicicesFromEntitiesProgress"));
        
        transformTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                try {
                    RestUtils.disableRestServicesChangeListner(project);
                    generator.generate(progressDialog.getProgressHandle());
                    
                } catch(Exception iox) {
                    ErrorManager.getDefault().notify(iox);
                } finally {
                    RestUtils.enableRestServicesChangeListner(project);
                    progressDialog.close();
                }
            }
        });
        transformTask.schedule(50);
        progressDialog.open();
        
        return Collections.singleton(DataFolder.findFolder(targetFolder));
    }
    
 
    public void initialize(TemplateWizard wizard) {
        index = 0;
        // obtaining target folder
        Project project = Templates.getProject( wizard );
        SourceGroup[] sourceGroups = SourceGroupSupport.getJavaSourceGroups(project);
        
        WizardDescriptor.Panel secondPanel = new EntitySelectionPanel(
                NbBundle.getMessage(EntityResourcesIterator.class, "LBL_EntityClasses"), wizard);
        WizardDescriptor.Panel thirdPanel = new EntityResourcesSetupPanel(
                NbBundle.getMessage(EntityResourcesIterator.class, "LBL_RestResourcesAndClasses"), wizard);
        WizardDescriptor.Panel javaPanel = JavaTemplates.createPackageChooser(project, sourceGroups, secondPanel);
        panels = new WizardDescriptor.Panel[] { javaPanel };
        panels = new WizardDescriptor.Panel[] { secondPanel, thirdPanel };
        String names[] = new String[] {
            NbBundle.getMessage(EntityResourcesIterator.class, "LBL_EntityClasses"),
            NbBundle.getMessage(EntityResourcesIterator.class, "LBL_RestResourcesAndClasses")
        
        
        
        };
        wizard.putProperty("NewFileWizard_Title",
                NbBundle.getMessage(EntityResourcesIterator.class, "Templates/WebServices/RestServicesFromEntities"));
        Util.mergeSteps(wizard, panels, names);
    }
    
    public void uninitialize(TemplateWizard wiz) {
        panels = null;
    }
    
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    public String name() {
        return NbBundle.getMessage(EntityResourcesIterator.class, "LBL_WizardTitle_FromEntity");
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    
    public void nextPanel() {
        if (! hasNext()) throw new NoSuchElementException();
        index++;
    }
    
    public void previousPanel() {
        if (! hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    
    public void addChangeListener(ChangeListener l) {
    }
    
    public void removeChangeListener(ChangeListener l) {
    }
}
