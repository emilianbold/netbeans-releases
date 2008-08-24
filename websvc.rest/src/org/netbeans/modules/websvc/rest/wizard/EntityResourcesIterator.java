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

package org.netbeans.modules.websvc.rest.wizard;

import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.codegen.EntityResourcesGenerator;
import org.netbeans.modules.websvc.rest.codegen.EntityResourcesGeneratorFactory;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBeanModel;
import org.netbeans.modules.websvc.rest.support.PersistenceHelper.PersistenceUnit;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;
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
        RestUtils.ensureRestDevelopmentReady(project);
        FileObject targetFolder = Templates.getTargetFolder(wizard);
        String targetPackage = SourceGroupSupport.packageForFolder(targetFolder);
        String resourcePackage = (String) wizard.getProperty(WizardProperties.RESOURCE_PACKAGE);
        String converterPackage = (String) wizard.getProperty(WizardProperties.CONVERTER_PACKAGE);
        EntityResourceBeanModel model = (EntityResourceBeanModel) wizard.getProperty(WizardProperties.ENTITY_RESOURCE_MODEL);
        final PersistenceUnit pu = (PersistenceUnit) wizard.getProperty(WizardProperties.PERSISTENCE_UNIT);
    
        final EntityResourcesGenerator generator = EntityResourcesGeneratorFactory.newInstance(project);
        generator.initialize(model, project, targetFolder, targetPackage, resourcePackage, converterPackage, pu);
        final ProgressDialog progressDialog = new ProgressDialog(NbBundle.getMessage(
                EntityResourcesIterator.class,
                "LBL_RestSevicicesFromEntitiesProgress"));
        
        transformTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                try {
                    RestUtils.disableRestServicesChangeListner(project);
                    generator.generate(progressDialog.getProgressHandle());
                } catch(Exception iox) {
                    Exceptions.printStackTrace(iox);
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
        WizardDescriptor.Panel secondPanel = new EntitySelectionPanel(
                NbBundle.getMessage(EntityResourcesIterator.class, "LBL_EntityClasses"), wizard);
        WizardDescriptor.Panel thirdPanel = new EntityResourcesSetupPanel(
                NbBundle.getMessage(EntityResourcesIterator.class, "LBL_RestResourcesAndClasses"), wizard);
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
