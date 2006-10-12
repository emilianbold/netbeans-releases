package org.netbeans.modules.j2ee.persistence.wizard.dao;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.jmi.javamodel.Annotation;
import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.jmi.javamodel.Method;
import org.netbeans.jmi.javamodel.Parameter;
import org.netbeans.modules.j2ee.common.JMIUtils;
import org.netbeans.modules.j2ee.common.JMIGenerationUtil;
import org.netbeans.modules.j2ee.persistence.action.EntityManagerGenerator;
import org.netbeans.modules.j2ee.persistence.dd.orm.model_1_0.Entity;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
import org.netbeans.modules.j2ee.persistence.provider.ProviderUtil;
import org.netbeans.modules.j2ee.persistence.wizard.PersistenceClientEntitySelection;
import org.netbeans.modules.j2ee.persistence.wizard.Util;
import org.netbeans.modules.j2ee.persistence.wizard.WizardProperties;
import org.netbeans.modules.javacore.api.JavaModel;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public final class EjbFacadeWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private static final String WIZARD_PANEL_CONTENT_DATA = "WizardPanel_contentData"; // NOI18N
    
    private int index;
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    private String[] steps;
    private int stepsStartPos;
    
    private WizardDescriptor.Panel[] getPanels() {
        return panels;
    }
    
    public Set instantiate() throws IOException {
        List<Entity> entities = (List<Entity>) wizard.getProperty(WizardProperties.ENTITY_CLASS);
        Project project = Templates.getProject(wizard);
        FileObject targetFolder = Templates.getTargetFolder(wizard);
        Set createdFiles = new HashSet();
        EjbFacadeWizardPanel2 panel = (EjbFacadeWizardPanel2) panels[1];
        String pkg = panel.getPackage();

        PersistenceUnit persistenceUnit = (PersistenceUnit) wizard.getProperty(WizardProperties.PERSISTENCE_UNIT);
        if (persistenceUnit != null){
            ProviderUtil.addPersistenceUnit(persistenceUnit, Templates.getProject(wizard));
        }
        
        for (Entity entity : entities) {
            String entityClass = entity.getClass2();
            String simpleClassName = Util.simpleClassName(entityClass);
            String variableName = simpleClassName.toLowerCase().charAt(0) + simpleClassName.substring(1);
            
            String facadeNameBase = pkg + "." + simpleClassName;
            String classBean = JMIUtils.uniqueClassName(facadeNameBase + "Facade", targetFolder);
            classBean = classBean.substring(classBean.lastIndexOf(".") + 1);
            JavaClass jc = JMIGenerationUtil.createClass(targetFolder, classBean);
            
            boolean rollback = true;
            JMIUtils.beginJmiTransaction(true);
            try {
                Annotation stateless = JMIGenerationUtil.createAnnotation(jc, "javax.ejb.Stateless", Collections.EMPTY_LIST);
                jc.getAnnotations().add(stateless);
                
                JavaClass localIF = null;
                JavaClass remoteIF = null;
                boolean hasLocal = panel.isLocal();
                boolean hasRemote = panel.isRemote();
                if (hasLocal) {
                    String classLocal = JMIUtils.uniqueClassName(facadeNameBase + "FacadeLocal", targetFolder);
                    classLocal = classLocal.substring(classLocal.lastIndexOf(".") + 1);
                    localIF = JMIGenerationUtil.createInterface(targetFolder, classLocal);
                    Annotation localAnn = JMIGenerationUtil.createAnnotation(localIF, "javax.ejb.Local", Collections.EMPTY_LIST);
                    localIF.getAnnotations().add(localAnn);
                    JMIUtils.addInterface(jc, localIF.getName());
                }
                if (hasRemote) {
                    String classRemote = JMIUtils.uniqueClassName(facadeNameBase + "FacadeRemote", targetFolder);
                    classRemote = classRemote.substring(classRemote.lastIndexOf(".") + 1);
                    remoteIF = JMIGenerationUtil.createInterface(targetFolder, classRemote);
                    Annotation remoteAnn = JMIGenerationUtil.createAnnotation(remoteIF, "javax.ejb.Remote", Collections.EMPTY_LIST);
                    remoteIF.getAnnotations().add(remoteAnn);
                    JMIUtils.addInterface(jc, remoteIF.getName());
                }
                
                Parameter p = JMIGenerationUtil.createParameter(jc, variableName, entityClass);
                EntityManagerGenerator.generate(jc, EntityManagerGenerator.OPERATION_PERSIST, "create", "void", p, null, false, false);
                addMethodToInterface("create", "void", p, localIF, remoteIF);
                
                p = JMIGenerationUtil.createParameter(jc, variableName, entityClass);
                EntityManagerGenerator.generate(jc, EntityManagerGenerator.OPERATION_MERGE, "edit", "void", p, null, false, false);
                addMethodToInterface("edit", "void", p, localIF, remoteIF);
                
                p = JMIGenerationUtil.createParameter(jc, variableName, entityClass);
                EntityManagerGenerator.generate(jc, EntityManagerGenerator.OPERATION_REMOVE, "destroy", "void", p, null, false, false);
                addMethodToInterface("destroy", "void", p, localIF, remoteIF);
                
                p = JMIGenerationUtil.createParameter(jc, "pk", Object.class.getName());
                EntityManagerGenerator.generate(jc, EntityManagerGenerator.OPERATION_FIND, "find", entityClass, p, entityClass, false, false);
                addMethodToInterface("find", entityClass, p, localIF, remoteIF);
                
                EntityManagerGenerator.generate(jc, EntityManagerGenerator.OPERATION_FIND_ALL, "findAll", List.class.getName(), null, entity.getName(), false, false);
                addMethodToInterface("findAll", List.class.getName(), null, localIF, remoteIF);
                
                createdFiles.add(JavaModel.getFileObject(jc.getResource()));
                rollback = false;
            } finally {
                JMIUtils.endJmiTransaction(rollback);
            }
            
            // replace fully qualified names by simple names and add needed imports
            // note: cannot be performed in the jmi transaction above
            EntityManagerGenerator.fixImports(jc);
            
        }
        return createdFiles;
    }
    
    private static void addMethodToInterface(String name, String type, Parameter p, JavaClass localIF, JavaClass remoteIF) {
        if (localIF != null) {
            Method m = JMIGenerationUtil.createMethod(localIF, name, 0, type);
            if (p != null) {
                m.getParameters().add(p.duplicate());
            }
            localIF.getFeatures().add(m);
        }
        if (remoteIF != null) {
            Method m = JMIGenerationUtil.createMethod(remoteIF, name, 0, type);
            if (p != null) {
                m.getParameters().add(p.duplicate());
            }
            remoteIF.getFeatures().add(m);
        }
    }
    
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        wizard.putProperty("NewFileWizard_Title",
                NbBundle.getMessage(EjbFacadeWizardIterator.class, "Templates/Persistence/ejbFacade"));
        Project project = Templates.getProject(wizard);
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {
                new PersistenceClientEntitySelection(
                        NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_EntityClasses"),
                        new HelpCtx(EjbFacadeWizardIterator.class.getName() + "$PersistenceClientEntitySelection"), wizard), // NOI18N
                new EjbFacadeWizardPanel2(project, wizard)
            };
            if (steps == null) {
                mergeSteps(new String[] {
                    NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_EntityClasses"),
                    NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_GeneratedSessionBeans"),
                });
            }
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }
    }
    
    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
    }
    
    public WizardDescriptor.Panel current() {
        return getPanels()[index];
    }
    
    public String name() {
        return NbBundle.getMessage(EjbFacadeWizardIterator.class, "LBL_FacadeWizardTitle");
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
    public void addChangeListener(ChangeListener l) {}
    public void removeChangeListener(ChangeListener l) {}
    
    private void mergeSteps(String[] thisSteps) {
        Object prop = wizard.getProperty(WIZARD_PANEL_CONTENT_DATA);
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
    
}
