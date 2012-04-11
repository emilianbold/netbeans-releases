package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.jpa.dao;

import java.awt.Component;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.session.SessionEJBWizardDescriptor;
import org.netbeans.modules.j2ee.ejbcore.ejb.wizard.session.SessionEJBWizardPanel;
import org.netbeans.modules.j2ee.persistence.wizard.WizardProperties;
import org.netbeans.spi.project.support.ant.SourcesHelper;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class EjbFacadeWizardPanel2 implements WizardDescriptor.Panel, ChangeListener {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private EjbFacadeVisualPanel2 component;
    private WizardDescriptor wizardDescriptor;
    private Project project;
    private Project entityProject;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private List<String> entityClasses;
    
    public EjbFacadeWizardPanel2(Project project, WizardDescriptor wizardDescriptor) {
        this.project = project;
        this.wizardDescriptor = wizardDescriptor;
    }
    
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new EjbFacadeVisualPanel2(project, wizardDescriptor);
            component.addChangeListener(this);
        }
        return component;
    }
    
    @Override
    public HelpCtx getHelp() {
         return new HelpCtx(EjbFacadeWizardPanel2.class);
    }
    
    @Override
    public boolean isValid() {
        getComponent();
        if (!(component.isRemote() || component.isLocal())) {
            if(J2eeProjectCapabilities.forProject(project).isEjb31LiteSupported()) {
                //if it's jee6 project, ejb 3.1 allow to omit any interfaces
            } else {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, NbBundle.getMessage(EjbFacadeWizardPanel2.class, "ERR_ChooseInterface")); // NOI18N
                return false;
            }
        }
        if (component.isRemote()) {
            if (component.getRemoteInterfaceProject() == null) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                        SessionEJBWizardPanel.isMaven(project) ? NbBundle.getMessage(SessionEJBWizardDescriptor.class,"ERR_NoRemoteInterfaceProjectMaven") :
                            NbBundle.getMessage(SessionEJBWizardDescriptor.class,"ERR_NoRemoteInterfaceProject")); //NOI18N
                return false;
            } else {
                if (entityProject == null) {
                    wizardDescriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, 
                            NbBundle.getMessage(EjbFacadeWizardPanel2.class, "ERR_EntityLocationWarning", 
                            ProjectUtils.getInformation(component.getRemoteInterfaceProject()).getDisplayName())); // NOI18N
                    return true;
                } else if (project.getProjectDirectory().equals(entityProject.getProjectDirectory())) {
                    wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, 
                            NbBundle.getMessage(EjbFacadeWizardPanel2.class, "ERR_EntityLocation", 
                            ProjectUtils.getInformation(project).getDisplayName())); // NOI18N
                    return false;
                }
            }
        }
        if (!statelessIfaceOnProjectCP()) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,
                    NbBundle.getMessage(EjbFacadeWizardPanel2.class, "ERR_SessionIfaceNotOnProjectClasspath", //NOI18N
                    EjbFacadeWizardIterator.EJB_STATELESS));
            return false;
        }
        wizardDescriptor.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE, ""); // NOI18N
        wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); // NOI18N
        return true;
    }

    public static Project findProject(Project project, String clazz) {
        SourceGroup[] groups = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup group : groups) {
            if (group.getRootFolder().getFileObject(clazz) != null) {
                return project;
            }
            ClassPath cp = ClassPath.getClassPath(group.getRootFolder(), ClassPath.COMPILE);
            if (cp == null) {
                continue;
            }
            FileObject clazzFo = null;
            LOOP:
            for (ClassPath.Entry entry : cp.entries()) {
                FileObject fos[] = SourceForBinaryQuery.findSourceRoots2(entry.getURL()).getRoots();
                for (FileObject fo : fos) {
                    FileObject ff = fo.getFileObject(clazz);
                    if (ff != null) {
                        clazzFo = ff;
                        break LOOP;
                    }
                    
                }
            }
            if (clazzFo == null) {
                continue;
            }
            Project p = FileOwnerQuery.getOwner(clazzFo);
            if (p == null) {
                continue;
            }
            if (p.getProjectDirectory().equals(project.getProjectDirectory())) {
                return project;
            }
            return p;
        }
        return null;
    }
    
    public boolean isFinishPanel() {
        return true;
    }
    
    @Override
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    @Override
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public String getPackage() {
        return component.getPackage();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }

    @Override
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read(wizardDescriptor);
        entityClasses = (List<String>)wizardDescriptor.getProperty(WizardProperties.ENTITY_CLASS);
        assert entityClasses != null;
        assert entityClasses.size() > 0;
        // just check first entity for now; ideally all entities should be checked - TBD
        String clazz = entityClasses.iterator().next().replace('.', '/')+".java";
        entityProject = findProject(project, clazz);
    }
    
    @Override
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
    }
    
    boolean isRemote() {
        return component.isRemote();
    }
    
    boolean isLocal() {
        return component.isLocal();
    }
    
    Project getRemoteInterfaceProject() {
        return component.getRemoteInterfaceProject();
    }

    public Project getEntityProject() {
        return entityProject;
    }

    private boolean statelessIfaceOnProjectCP() {
        ClassPath cp = ClassPath.getClassPath(project.getProjectDirectory(), ClassPath.COMPILE);
        ClassLoader cl = cp.getClassLoader(true);
        try {
            Class.forName(EjbFacadeWizardIterator.EJB_STATELESS, false, cl);
        } catch (ClassNotFoundException cnfe) {
            return false;
        }
        return true;
    }

}

