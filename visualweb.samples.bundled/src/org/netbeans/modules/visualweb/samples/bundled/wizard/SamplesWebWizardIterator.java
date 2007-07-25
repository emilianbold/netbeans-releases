package org.netbeans.modules.visualweb.samples.bundled.wizard;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

public final class SamplesWebWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    public SamplesWebWizardIterator() {}
    
    public static SamplesWebWizardIterator createIterator() {
        return new SamplesWebWizardIterator();
    }
    
    private int index;
    private WizardDescriptor.Panel[] panels;
    protected WizardDescriptor wizard;
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels() {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {
                new SamplesWebWizardPanel()
            };
            String[] steps = new String[panels.length];
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent();
                // Default step name to component name of panel.
                steps[i] = c.getName();
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
        return panels;
    }
    
    public Set instantiate() throws IOException {
        Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
        File projectDirectoryFile = FileUtil.normalizeFile((File) this.wizard.getProperty(WizardProperties.PROJ_DIR));
        FileUtil.createFolder(projectDirectoryFile);
        
        FileObject template = Templates.getTemplate( this.wizard );
        FileObject projectDirectoryFileObject = FileUtil.toFileObject(projectDirectoryFile);
        unZipFile(template.getInputStream(), projectDirectoryFileObject);
        ProjectManager.getDefault().clearNonProjectCache();

        resultSet.add(projectDirectoryFileObject);
        DataObject projectDirectoryDataObject = DataObject.find( projectDirectoryFileObject );
        Enumeration e = projectDirectoryFileObject.getFolders(true);
        while (e.hasMoreElements()) {
            FileObject subfolder = (FileObject) e.nextElement();
            if (ProjectManager.getDefault().isProject(subfolder)) {
                resultSet.add(subfolder);
            }
        }
        Boolean isSetAsMainProject = (Boolean) wizard.getProperty( WizardProperties.SET_MAIN_PROJ );
        File parent = projectDirectoryFile.getParentFile();
        if ( isSetAsMainProject.booleanValue() && parent != null && parent.exists() ) {
            ProjectChooser.setProjectsFolder(parent);
        }
        // Open the new project
        OpenCookie openCookie = (OpenCookie) projectDirectoryDataObject.getCookie( OpenCookie.class );
        if ( openCookie != null ) {
            openCookie.open();
        }
        return resultSet;
    }
    
     private static void unZipFile(InputStream source, FileObject projectRoot) throws IOException {
        try {
            ZipInputStream zipInputStream = new ZipInputStream(source);
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(projectRoot, entry.getName());
                } else {
                    FileObject fileObject = FileUtil.createData(projectRoot, entry.getName());
                    FileLock lock = fileObject.lock();
                    try {
                        OutputStream outputStream = fileObject.getOutputStream(lock);
                        try {
                            FileUtil.copy(zipInputStream, outputStream);
                        } finally {
                            outputStream.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                }
            }
        } finally {
            source.close();
        }
    }

     public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;
        FileObject template = Templates.getTemplate(wizard);
        wizard.putProperty( WizardProperties.NAME, template.getName() );
        wizard.putProperty( WizardProperties.SET_MAIN_PROJ, Boolean.valueOf(true) );
    }
    
    public void uninitialize(WizardDescriptor wizard) {
        this.wizard.putProperty(WizardProperties.NAME, null);
        this.wizard.putProperty(WizardProperties.PROJ_DIR, null);
        this.wizard.putProperty(WizardProperties.SET_MAIN_PROJ, null );
        this.wizard = null;
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
    public void addChangeListener(ChangeListener l) {}
    public void removeChangeListener(ChangeListener l) {}
    
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then uncomment
    // the following and call when needed: fireChangeEvent();
    /*
    private Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
     */
    
    // You could safely ignore this method. Is is here to keep steps which were
    // there before this wizard was instantiated. It should be better handled
    // by NetBeans Wizard API itself rather than needed to be implemented by a
    // client code.
    private String[] createSteps() {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty("WizardPanel_contentData");
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
    
}
