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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.awt.Component;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.core.api.support.SourceGroups;
import org.netbeans.modules.j2ee.core.api.support.wizard.Wizards;
import org.netbeans.modules.j2ee.persistence.api.PersistenceLocation;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 *
 * @author Chris Webster, Martin Adamek, Andrei Badea
 */
public class RelatedCMPWizard implements TemplateWizard.Iterator {
    
    private static final String PROP_HELPER = "wizard-helper"; //NOI18N
    private static final String PROP_CMP = "wizard-is-cmp"; //NOI18N
    
    private static final String TYPE_CMP = "cmp"; // NOI18N
    private static final String TYPE_JPA = "jpa"; // NOI18N
    
    private static final Lookup.Result<PersistenceGeneratorProvider> PERSISTENCE_PROVIDERS =
            Lookup.getDefault().lookupResult(PersistenceGeneratorProvider.class);
    
    private final String type;
    
    private WizardDescriptor.Panel[] panels;
    private int currentPanel = 0;
    
    private WizardDescriptor wizardDescriptor;
    
    private PersistenceGenerator generator;
    private RelatedCMPHelper helper;
    private ProgressPanel progressPanel;

    private Project project;
    
    public static RelatedCMPWizard createForJPA() {
        return new RelatedCMPWizard(TYPE_JPA);
    }
    
    public static RelatedCMPWizard createForCMP() {
        return new RelatedCMPWizard(TYPE_CMP);
    }
    private static PersistenceGenerator createPersistenceGenerator(String type) {
        assert type != null;
        
        Collection<? extends PersistenceGeneratorProvider> providers = PERSISTENCE_PROVIDERS.allInstances();
        for (PersistenceGeneratorProvider provider : providers) {
            if (type.equals(provider.getGeneratorType())) {
                return provider.createGenerator();
            }
        }
        throw new AssertionError("Could not find a persistence generator of type " + type); // NOI18N
    }
    
    static RelatedCMPHelper getHelper(WizardDescriptor wizardDescriptor) {
        return (RelatedCMPHelper)wizardDescriptor.getProperty(PROP_HELPER);
    }
    
    static boolean isCMP(WizardDescriptor wizardDescriptor) {
        return ((Boolean)wizardDescriptor.getProperty(PROP_CMP)).booleanValue();
    }
    
    public RelatedCMPWizard(String type) {
        this.type = type;
    }
    
    public String name() {
        return null;
    }
    
    public boolean hasPrevious() {
        return currentPanel > 0;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        currentPanel--;
    }
    
    public boolean hasNext() {
        return currentPanel < panels.length - 1;
    }
    
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        currentPanel++;
    }
    
    @SuppressWarnings("unchecked")
    public Panel<WizardDescriptor> current() {
        return panels[currentPanel];
    }
    
    public void addChangeListener(ChangeListener l) {
    }
    
    public void removeChangeListener(ChangeListener l) {
    }
    
    private WizardDescriptor.Panel[] createPanels() {
        
        String wizardBundleKey = isCMP() ? "Templates/J2EE/RelatedCMP" : "Templates/Persistence/RelatedCMP"; // NOI18N
        String wizardTitle = NbBundle.getMessage(RelatedCMPWizard.class, wizardBundleKey);
        
        if (isCMP()) {
            return new WizardDescriptor.Panel[] {
                    new DatabaseTablesPanel.WizardPanel(wizardTitle),
                    new EntityClassesPanel.WizardPanel(),
            };
        } else {
            return new WizardDescriptor.Panel[] {
                    new DatabaseTablesPanel.WizardPanel(wizardTitle),
                    new EntityClassesPanel.WizardPanel(),
                    new MappingOptionsPanel.WizardPanel(),
            };
        }
    }
    
    private String[] createSteps() {
        if (isCMP()) {
            return new String[] {
                    NbBundle.getMessage(RelatedCMPWizard.class, "LBL_DatabaseTables"),
                    NbBundle.getMessage(RelatedCMPWizard.class, isCMP() ? "LBL_EntityBeansLocation" : "LBL_EntityClasses"),
            };
        } else {
            return new String[] {
                    NbBundle.getMessage(RelatedCMPWizard.class, "LBL_DatabaseTables"),
                    NbBundle.getMessage(RelatedCMPWizard.class, isCMP() ? "LBL_EntityBeansLocation" : "LBL_EntityClasses"),
                    NbBundle.getMessage(RelatedCMPWizard.class, "LBL_MappingOptions")
            };
        }
    }
    
    private boolean isCMP() {
        return TYPE_CMP.equals(type);
    }
    
    public final void initialize(TemplateWizard wiz) {
        wizardDescriptor = wiz;
        
        panels = createPanels();
        Wizards.mergeSteps(wizardDescriptor, panels, createSteps());
        
        project = Templates.getProject(wiz);
        generator = createPersistenceGenerator(type);
        
        FileObject configFilesFolder = PersistenceLocation.getLocation(project);
        
        helper = new RelatedCMPHelper(project, configFilesFolder, generator);
        
        wiz.putProperty(PROP_HELPER, helper);
        wiz.putProperty(PROP_CMP, new Boolean(isCMP()));
        
        generator.init(wiz);
    }
    
    public final void uninitialize(TemplateWizard wiz) {
        generator.uninit();
    }
    
    public Set<DataObject> instantiate(final TemplateWizard wiz) throws IOException {
        Component c = WindowManager.getDefault().getMainWindow();
        
        // create the pu first if needed
        if (helper.getPersistenceUnit() != null) {
            
            //Only add library for Hibernate in NB 6.5
            String providerClass = helper.getPersistenceUnit().getProvider();
            if(providerClass != null){
                if (providerClass.equals("org.hibernate.ejb.HibernatePersistence")) {
                    Library lib = LibraryManager.getDefault().getLibrary("hibernate-support"); //NOI18N
                    if (lib != null) {
                        Util.addLibraryToProject(project, lib);
                    }
                }
                else if(providerClass.equals("org.eclipse.persistence.jpa.PersistenceProvider"))//NOI18N
                {
                    //fix #170046
                    //TODO: find some common approach what libraries to add and what do not need to be added
                    Library lib =  LibraryManager.getDefault().getLibrary("eclipselink"); //NOI18N
                    if (lib != null) {
                        Util.addLibraryToProject(project, lib);
                    }
                }
            }


            try {
                ProviderUtil.addPersistenceUnit(helper.getPersistenceUnit(), Templates.getProject(wiz));
            } catch (InvalidPersistenceXmlException ipx) {
                // just log for debugging purposes, at this point the user has
                // already been warned about an invalid persistence.xml
                Logger.getLogger(RelatedCMPWizard.class.getName()).log(Level.FINE, "Invalid persistence.xml: " + ipx.getPath(), ipx); //NOI18N
            }
        }
        
        final String title = NbBundle.getMessage(RelatedCMPWizard.class, isCMP() ? "TXT_EjbGeneration" : "TXT_EntityClassesGeneration");
        final ProgressContributor progressContributor = AggregateProgressFactory.createProgressContributor(title);
        final AggregateProgressHandle handle = 
                AggregateProgressFactory.createHandle(title, new ProgressContributor[]{progressContributor}, null, null);
        progressPanel = new ProgressPanel();
        final JComponent progressComponent = AggregateProgressFactory.createProgressComponent(handle);
        
        final Runnable r = new Runnable() {

            public void run() {
                try {
                    handle.start();
                    createBeans(wiz, progressContributor);
                } catch (IOException ioe) {
                    Logger.getLogger("global").log(Level.INFO, null, ioe);
                    NotifyDescriptor nd = new NotifyDescriptor.Message(ioe.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                } finally {
                    generator.uninit();
                    handle.finish();
                }
            }
        };
        
        // Ugly hack ensuring the progress dialog opens after the wizard closes. Needed because:
        // 1) the wizard is not closed in the AWT event in which instantiate() is called.
        //    Instead it is closed in an event scheduled by SwingUtilities.invokeLater().
        // 2) when a modal dialog is created its owner is set to the foremost modal
        //    dialog already displayed (if any). Because of #1 the wizard will be
        //    closed when the progress dialog is already open, and since the wizard
        //    is the owner of the progress dialog, the progress dialog is closed too.
        // The order of the events in the event queue:
        // -  this event
        // -  the first invocation event of our runnable
        // -  the invocation event which closes the wizard
        // -  the second invocation event of our runnable
        
        SwingUtilities.invokeLater(new Runnable() {
            private boolean first = true;
            public void run() {
                if (!first) {
                    RequestProcessor.getDefault().post(r);
                    progressPanel.open(progressComponent, title);
                } else {
                    first = false;
                    SwingUtilities.invokeLater(this);
                }
            }
        });
        
        // The commented code below is the ideal state, but since there is not way to request
        // TemplateWizard.Iterator.instantiate() be called asynchronously it
        // would cause the wizard to stay visible until the bean generation process
        // finishes. So for now just returning the package -- not a problem,
        // JavaPersistenceGenerator.createdObjects() returns an empty set anyway.
        
        // remember to wait for createBeans() to actually return!
        // Set created = generator.createdObjects();
        // if (created.size() == 0) {
        //     created = Collections.singleton(SourceGroupSupport.getFolderForPackage(helper.getLocation(), helper.getPackageName()));
        // }
        
        return Collections.<DataObject>singleton(DataFolder.findFolder(
                SourceGroups.getFolderForPackage(helper.getLocation(), helper.getPackageName())
                ));
    }
    
    private void createBeans(TemplateWizard wiz, ProgressContributor handle) throws IOException {
        try {
            handle.start(1); //TODO: need the correct number of work units here 
            handle.progress(NbBundle.getMessage(RelatedCMPWizard.class, "TXT_SavingSchema"));
            progressPanel.setText(NbBundle.getMessage(RelatedCMPWizard.class, "TXT_SavingSchema"));
            
            FileObject dbschemaFile = helper.getDBSchemaFile();
            if (dbschemaFile == null) {
                FileObject configFilesFolder = getHelper(wiz).getConfigFilesFolder();
                if (configFilesFolder == null && !isCMP()) {
                    // if we got here, this must be an entity class library project or just a
                    // project without persistence.xml
                    configFilesFolder = PersistenceLocation.createLocation(project);
                }
                if (configFilesFolder == null) {
                    String message = NbBundle.getMessage(RelatedCMPWizard.class, "TXT_NoConfigFiles");
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                    return;
                }
                
                String projectName = ProjectUtils.getInformation(project).getDisplayName();
                dbschemaFile = DBSchemaManager.updateDBSchemas(helper.getSchemaElement(), helper.getDBSchemaFileList(), configFilesFolder, projectName);
            }
            
            String extracting = NbBundle.getMessage(RelatedCMPWizard.class, isCMP() ?
                "TXT_ExtractingBeansAndRelationships" : "TXT_ExtractingEntityClassesAndRelationships");
            
            handle.progress(extracting);
            progressPanel.setText(extracting);
            
            helper.buildBeans();
            
            FileObject pkg = SourceGroups.getFolderForPackage(helper.getLocation(), helper.getPackageName());
            generator.generateBeans(progressPanel, helper, dbschemaFile, handle);
            
            //            if (EjbJar.VERSION_3_0.equals(dd.getVersion().toString())) {
            //                JavaPersistenceGenerator jpg = new JavaPersistenceGenerator();
            //                jpg.generateBeans(
            //            } else {
            //                CmpGenerator gen = new CmpGenerator();
            //                gen.generateBeans(progressPanel,helper, pkg, dbschemaFile, genHelper, handle, module.getDeploymentDescriptor(), pwm, dd, false);
            //            }
        } finally {
            handle.finish();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    progressPanel.close();
                }
            });
        }
    }
}
