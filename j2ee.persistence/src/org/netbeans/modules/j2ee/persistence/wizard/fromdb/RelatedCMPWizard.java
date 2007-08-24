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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.awt.Component;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.persistence.api.PersistenceLocation;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
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
public class RelatedCMPWizard extends WizardDescriptor.ArrayIterator<WizardDescriptor> implements TemplateWizard.Iterator {
    
    private static final String WIZARD_PANEL_CONTENT_DATA = "WizardPanel_contentData"; // NOI18N
    private static final String WIZARD_PANEL_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex"; //NOI18N;
    
    private static final String PROP_HELPER = "wizard-helper"; //NOI18N
    private static final String PROP_CMP = "wizard-is-cmp"; //NOI18N
    
    private static final String TYPE_CMP = "cmp"; // NOI18N
    private static final String TYPE_JPA = "jpa"; // NOI18N
    
    private static final Lookup.Result<PersistenceGeneratorProvider> PERSISTENCE_PROVIDERS =
            Lookup.getDefault().lookupResult(PersistenceGeneratorProvider.class);
    
    private final String type;
    
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private String[] steps;
    private int stepsStartPos;
    
    private WizardDescriptor wizardDescriptor;
    
    private PersistenceGenerator generator;
    private RelatedCMPHelper helper;
    private ProgressPanel progressPanel;
    
    public static RelatedCMPWizard createForJPA() {
        return new RelatedCMPWizard(TYPE_JPA);
    }
    
    public static RelatedCMPWizard createForCMP() {
        return new RelatedCMPWizard(TYPE_CMP);
    }
    
    public RelatedCMPWizard(String type) {
        this.type = type;
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
    
    private boolean isCMP() {
        return TYPE_CMP.equals(type);
    }
    
    @SuppressWarnings("unchecked")
    protected WizardDescriptor.Panel<WizardDescriptor>[] initializePanels() {
        panels = (WizardDescriptor.Panel<WizardDescriptor>[])new WizardDescriptor.Panel<?>[] {
            new DatabaseTablesPanel.WizardPanel(),
            new EntityClassesPanel.WizardPanel()
        };
        return panels;
    }
    
    /**
     * Overriden to set the wizard content data and selected index for
     * each panel when it is needed, not in advance (which would cause
     * all the panels' components to be created prematurely).
     */
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        WizardDescriptor.Panel<WizardDescriptor> panel = super.current();
        
        if (steps == null) {
            mergeSteps(new String[] {
                NbBundle.getMessage(RelatedCMPWizard.class, "LBL_DatabaseTables"),
                NbBundle.getMessage(RelatedCMPWizard.class, isCMP() ? "LBL_EntityBeansLocation" : "LBL_EntityClasses"),
            });
        }
        
        JComponent component = (JComponent)panel.getComponent();
        if (component.getClientProperty(WIZARD_PANEL_CONTENT_DATA) == null) {
            component.putClientProperty(WIZARD_PANEL_CONTENT_DATA, steps);
        }
        if (component.getClientProperty(WIZARD_PANEL_CONTENT_SELECTED_INDEX) == null) {
            if (panel == panels[0]) {
                component.putClientProperty(WIZARD_PANEL_CONTENT_SELECTED_INDEX, new Integer(0));
                // don't use an absolute step value like 1,
                // since we dont' know how may steps there are before us
                component.setName(steps[steps.length - 2]);
            } else {
                component.putClientProperty(WIZARD_PANEL_CONTENT_SELECTED_INDEX, new Integer(1));
                // don't use an absolute step value like 2,
                // since we dont' know how may steps there are before us
                component.setName(steps[steps.length - 1]);
            }
        }
        
        return panel;
    }
    
    static RelatedCMPHelper getHelper(WizardDescriptor wizardDescriptor) {
        return (RelatedCMPHelper)wizardDescriptor.getProperty(PROP_HELPER);
    }
    
    static boolean isCMP(WizardDescriptor wizardDescriptor) {
        return ((Boolean)wizardDescriptor.getProperty(PROP_CMP)).booleanValue();
    }
    
    public final void initialize(TemplateWizard wiz) {
        wizardDescriptor = wiz;
        
        Project project = Templates.getProject(wiz);
        generator = createPersistenceGenerator(type);
        
        FileObject configFilesFolder = PersistenceLocation.getLocation(project);
        
        helper = new RelatedCMPHelper(project, configFilesFolder, generator);
        
        wiz.putProperty(PROP_HELPER, helper);
        wiz.putProperty(PROP_CMP, new Boolean(isCMP()));
        
        String wizardBundleKey = isCMP() ? "Templates/J2EE/RelatedCMP" : "Templates/Persistence/RelatedCMP"; // NOI18N
        wiz.putProperty("NewFileWizard_Title", NbBundle.getMessage(RelatedCMPWizard.class, wizardBundleKey)); // NOI18N
        
        generator.init(wiz);
    }
    
    public void mergeSteps(String[] thisSteps) {
        Object prop = wizardDescriptor.getProperty(WIZARD_PANEL_CONTENT_DATA);
        String[] beforeSteps;
        
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
            stepsStartPos = beforeSteps.length;
            if (stepsStartPos > 0 && ("...".equals(beforeSteps[stepsStartPos - 1]))) { // NOI18N
                stepsStartPos--;
            }
        } else {
            beforeSteps = null;
            stepsStartPos = 0;
        }
        
        steps = new String[stepsStartPos + thisSteps.length];
        System.arraycopy(beforeSteps, 0, steps, 0, stepsStartPos);
        System.arraycopy(thisSteps, 0, steps, stepsStartPos, thisSteps.length);
    }
    
    public final void uninitialize(TemplateWizard wiz) {
        generator.uninit();
    }
    
    public Set<DataObject> instantiate(final TemplateWizard wiz) throws IOException {
        Component c = WindowManager.getDefault().getMainWindow();
        
        // create the pu first if needed
        if (helper.getPersistenceUnit() != null){
            try {
                ProviderUtil.addPersistenceUnit(helper.getPersistenceUnit(), Templates.getProject(wiz));
            } catch (InvalidPersistenceXmlException ipx){
                // just log for debugging purposes, at this point the user has
                // already been warned about an invalid persistence.xml
                Logger.getLogger(RelatedCMPWizard.class.getName()).log(Level.FINE, "Invalid persistence.xml: " + ipx.getPath(), ipx); //NO18N
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
                SourceGroupSupport.getFolderForPackage(helper.getLocation(), helper.getPackageName())
                ));
    }
    
    private void createBeans(TemplateWizard wiz, ProgressContributor handle) throws IOException {
        try {
            handle.start(1); //TODO: need the correct number of work units here 
            Project project = Templates.getProject(wiz);
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
                assert configFilesFolder != null : "Should have set configFilesFolder, e.g. by retrieving it from a PersistenceLocationProvider or EjbJar or by asking the user"; // NOI18N
                
                String projectName = ProjectUtils.getInformation(project).getDisplayName();
                dbschemaFile = DBSchemaManager.updateDBSchemas(helper.getSchemaElement(), helper.getDBSchemaFileList(), configFilesFolder, projectName);
            }
            
            String extracting = NbBundle.getMessage(RelatedCMPWizard.class, isCMP() ?
                "TXT_ExtractingBeansAndRelationships" : "TXT_ExtractingEntityClassesAndRelationships");
            
            handle.progress(extracting);
            progressPanel.setText(extracting);
            
            helper.buildBeans();
            
            FileObject pkg = SourceGroupSupport.getFolderForPackage(helper.getLocation(), helper.getPackageName());
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
