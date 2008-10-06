/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.websvc.rest.wizard.fromdb;

import java.awt.Component;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.progress.aggregate.AggregateProgressFactory;
import org.netbeans.api.progress.aggregate.AggregateProgressHandle;
import org.netbeans.api.progress.aggregate.ProgressContributor;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.persistence.api.PersistenceLocation;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.provider.InvalidPersistenceXmlException;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.DatabaseTablesPanel;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.EntityClassesPanel;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGenerator;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.PersistenceGeneratorProvider;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.ProgressPanel;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.RelatedCMPHelper;
import org.netbeans.modules.j2ee.persistence.wizard.fromdb.RelatedCMPWizard;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.codegen.EntityResourcesGenerator;
import org.netbeans.modules.websvc.rest.codegen.EntityResourcesGeneratorFactory;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBeanModel;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceModelBuilder;
import org.netbeans.modules.websvc.rest.codegen.model.RuntimeJpaEntity;
import org.netbeans.modules.websvc.rest.codegen.model.TypeUtil;
import org.netbeans.modules.websvc.rest.support.PersistenceHelper;
import org.netbeans.modules.websvc.rest.support.PersistenceHelper.PersistenceUnit;
import org.netbeans.modules.websvc.rest.support.SourceGroupSupport;
import org.netbeans.modules.websvc.rest.wizard.EntityResourcesIterator;
import org.netbeans.modules.websvc.rest.wizard.Util;
import org.netbeans.modules.websvc.rest.wizard.WizardProperties;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

public final class DatabaseResourceWizardIterator implements WizardDescriptor.InstantiatingIterator {

    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    private static final String PROP_CMP = "wizard-is-cmp"; //NOI18N

    private static final String PROP_HELPER = "wizard-helper"; //NOI18N

    private static final Lookup.Result<PersistenceGeneratorProvider> PERSISTENCE_PROVIDERS =
            Lookup.getDefault().lookupResult(PersistenceGeneratorProvider.class);
    private RelatedCMPHelper helper;
    private ProgressPanel progressPanel;
    private PersistenceGenerator generator;

    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        Project project = Templates.getProject(wizard);
        generator = createPersistenceGenerator("jpa");
        FileObject configFilesFolder = PersistenceLocation.getLocation(project);
        helper = new RelatedCMPHelper(project, configFilesFolder, generator);
        wizard.putProperty(PROP_HELPER, helper);
        wizard.putProperty(PROP_CMP, new Boolean(false));
        
        // Moved to getPanels()
        //String wizardBundleKey = "Templates/Persistence/RelatedCMP"; // NOI18N
        //wizard.putProperty("NewFileWizard_Title", NbBundle.getMessage(RelatedCMPWizard.class, wizardBundleKey)); // NOI18N

        generator.init(wizard);
        SourceGroup[] sourceGroups = SourceGroupSupport.getJavaSourceGroups(project);
        if (Templates.getTargetFolder(wizard) == null) {
            Templates.setTargetFolder(wizard, sourceGroups[0].getRootFolder());
        }

    }

    public Set instantiate() throws IOException {
        Component c = WindowManager.getDefault().getMainWindow();

        // create the pu first if needed
        if (helper.getPersistenceUnit() != null) {
            try {
                ProviderUtil.addPersistenceUnit(helper.getPersistenceUnit(), Templates.getProject(wizard));
            } catch (InvalidPersistenceXmlException ipx) {
                // just log for debugging purposes, at this point the user has
                // already been warned about an invalid persistence.xml
                Logger.getLogger(RelatedCMPWizard.class.getName()).log(Level.FINE, "Invalid persistence.xml: " + ipx.getPath(), ipx); //NOI18N

            }
        }

        final String title = NbBundle.getMessage(RelatedCMPWizard.class, "TXT_EntityClassesGeneration");
        final ProgressContributor progressContributor = AggregateProgressFactory.createProgressContributor(title);
        final AggregateProgressHandle aggregateHandle =
                AggregateProgressFactory.createHandle(title, new ProgressContributor[]{progressContributor}, null, null);
        progressPanel = new ProgressPanel();
        final JComponent progressComponent = AggregateProgressFactory.createProgressComponent(aggregateHandle);

        final Runnable r = new Runnable() {

            public void run() {
                try {
                    aggregateHandle.start();
                    generate(progressContributor);
                } catch (IOException ioe) {
                    Logger.getLogger("global").log(Level.INFO, null, ioe);
                    NotifyDescriptor nd = new NotifyDescriptor.Message(ioe.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                } finally {
                    generator.uninit();
                    aggregateHandle.finish();
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

        // remember to wait for generate() to actually return!
        // Set created = generator.createdObjects();
        // if (created.size() == 0) {
        //     created = Collections.singleton(SourceGroupSupport.getFolderForPackage(helper.getLocation(), helper.getPackageName()));
        // }

        return Collections.<DataObject>singleton(DataFolder.findFolder(
                getFolderForPackage(helper.getLocation(), helper.getPackageName())));
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

    private void generate(ProgressContributor handle) throws IOException {
        final Project project = Templates.getProject(wizard);
        try {
            handle.start(1); //TODO: need the correct number of work units here 


            handle.progress(NbBundle.getMessage(RelatedCMPWizard.class, "TXT_SavingSchema"));
            progressPanel.setText(NbBundle.getMessage(RelatedCMPWizard.class, "TXT_SavingSchema"));

            FileObject dbschemaFile = helper.getDBSchemaFile();

            String extracting = NbBundle.getMessage(RelatedCMPWizard.class, "TXT_ExtractingEntityClassesAndRelationships");

            handle.progress(extracting);
            progressPanel.setText(extracting);

            helper.buildBeans();

            FileObject pkg = getFolderForPackage(helper.getLocation(), helper.getPackageName());
            generator.generateBeans(progressPanel, helper, dbschemaFile, handle);

            Set<FileObject> files = generator.createdObjects();
            Set<Entity> entities = getEntities(project, files);

            EntityResourceModelBuilder builder = new EntityResourceModelBuilder(project, entities);
            EntityResourceBeanModel model = builder.build(entities);

            PersistenceUnit pu = new PersistenceHelper(project).getPersistenceUnit();

            RestUtils.ensureRestDevelopmentReady(project);
            FileObject targetFolder = Templates.getTargetFolder(wizard);
            String targetPackage = SourceGroupSupport.packageForFolder(targetFolder);
            String resourcePackage = (String) wizard.getProperty(WizardProperties.RESOURCE_PACKAGE);
            String converterPackage = (String) wizard.getProperty(WizardProperties.CONVERTER_PACKAGE);

            final EntityResourcesGenerator gen = EntityResourcesGeneratorFactory.newInstance(project);
            gen.initialize(model, project, targetFolder, targetPackage, resourcePackage, converterPackage, pu);

            RequestProcessor.Task transformTask = RequestProcessor.getDefault().create(new Runnable() {

                public void run() {
                    try {
                        RestUtils.disableRestServicesChangeListner(project);
                        gen.generate(null);

                    } catch (Exception iox) {
                        Exceptions.printStackTrace(iox);
                    } finally {
                        RestUtils.enableRestServicesChangeListner(project);

                    }
                }
            });
            transformTask.schedule(50);

        } finally {
            handle.finish();
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    progressPanel.close();
                }
            });
        }

    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            
            String wizardBundleKey = "Templates/Persistence/RelatedCMP"; // NOI18N
            String wizardTitle = NbBundle.getMessage(RelatedCMPWizard.class, wizardBundleKey); // NOI18N
            panels = new WizardDescriptor.Panel[]{
                        //new DatabaseResourceWizardPanel1()
                        new DatabaseTablesPanel.WizardPanel(wizardTitle),
                        new EntityClassesPanel.WizardPanel(),
                        new EntityResourcesSetupPanel(NbBundle.getMessage(EntityResourcesIterator.class,
                        "LBL_RestResourcesAndClasses"), wizard)
                    };

            String[] steps = createSteps();
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName();
                }
                if (c instanceof JComponent) { // assume Swing components

                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);
                }
            }
        }
        return panels;
    }

    private TypeElement getPublicTopLevelElement(CompilationController controller) {
        Parameters.notNull("controller", controller); // NOI18N

        FileObject mainFileObject = controller.getFileObject();
        if (mainFileObject == null) {
            throw new IllegalStateException();
        }
        String mainElementName = mainFileObject.getName();
        List<? extends TypeElement> elements = controller.getTopLevelElements();
        if (elements != null) {
            for (TypeElement element : elements) {
                if (element.getModifiers().contains(Modifier.PUBLIC) && element.getSimpleName().contentEquals(mainElementName)) {
                    return element;
                }
            }
        }
        return null;
    }

    private Set<Entity> getEntities(Project project, Set<FileObject> files) throws IOException {
        final List<TypeElement> typeElements = new ArrayList<TypeElement>();
        Set<Entity> entities = new HashSet<Entity>();
        for (FileObject file : files) {
            JavaSource source = JavaSource.forFileObject(file);
            source.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController controller) throws Exception {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    TypeElement typeElement = getPublicTopLevelElement(controller);
                    typeElements.add(typeElement);
                }
            }, true);
        }
        for (TypeElement te : typeElements) {
            String entityName = null;
            Class entityClass = Util.getType(project, te.getQualifiedName().toString());
            if (entityClass != null) {
                Annotation annotation = TypeUtil.getJpaTableAnnotation(entityClass);
                if (annotation != null) {
                    entityName = TypeUtil.getAnnotationValueName(annotation);
                }
                if (entityName == null) {
                    annotation = TypeUtil.getJpaEntityAnnotation(entityClass);
                    entityName = TypeUtil.getAnnotationValueName(annotation);
                }
            }
            if (entityName == null) {
                entityName = te.getSimpleName().toString();
            }
            entities.add(new RuntimeJpaEntity(te, entityName));
        }

        return entities;
    }

    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }

    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }

    public String name() {
        return index + 1 + ". from " + getPanels().length;
    }

    public boolean hasNext() {
        return index < getPanels().length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }


    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty(WizardDescriptor.PROP_CONTENT_DATA);
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }

        if (beforeSteps == null) {
            beforeSteps = new String[0];
        }

        String[] res = new String[(beforeSteps.length - 1) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (beforeSteps.length - 1)) {
                res[i] = beforeSteps[i];
            } else {
                res[i] = panels[i - beforeSteps.length + 1].getComponent().getName();
            }
        }
        return res;
    }

    /**
     * Gets the folder representing the given <code>packageName</code>.
     * 
     * @param sourceGroup the source group of the package; must not be null.
     * @param packageName the name of the package; must not be null.
     * @param create specifies whether the folder should be created if it does not exist.
     * @return the folder representing the given package or null if it was not found.
     */
    public static FileObject getFolderForPackage(SourceGroup sourceGroup, String packageName) throws IOException {
        Parameters.notNull("sourceGroup", sourceGroup); //NOI18N

        Parameters.notNull("packageName", packageName); //NOI18N

        String relativePkgName = packageName.replace('.', '/');
        FileObject folder = sourceGroup.getRootFolder().getFileObject(relativePkgName);
        if (folder != null) {
            return folder;
        } else {
            return FileUtil.createFolder(sourceGroup.getRootFolder(), relativePkgName);
        }
    }
}
