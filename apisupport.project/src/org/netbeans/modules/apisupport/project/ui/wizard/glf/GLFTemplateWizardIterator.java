package org.netbeans.modules.apisupport.project.ui.wizard.glf;

import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Manifest;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;


public final class GLFTemplateWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private int index;
    
    private WizardDescriptor wizard;
    private WizardDescriptor.Panel[] panels;
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels () {
        if (panels == null) {
            panels = new WizardDescriptor.Panel[] {
                new GLFTemplateWizardPanel1 (this),
                new GLFTemplateWizardPanel2 (this)
            };
            String[] steps = createSteps ();
            for (int i = 0; i < panels.length; i++) {
                Component c = panels[i].getComponent ();
                if (steps[i] == null) {
                    // Default step name to component name of panel. Mainly
                    // useful for getting the name of the target chooser to
                    // appear in the list of steps.
                    steps[i] = c.getName ();
                }
                if (c instanceof JComponent) { // assume Swing components
                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty ("WizardPanel_contentSelectedIndex", new Integer (i));
                    // Sets steps names for a panel
                    jc.putClientProperty ("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty ("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty ("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty ("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }
        return panels;
    }
    
    public Set instantiate () throws IOException {
        String fileName = ((GLFTemplateWizardPanel1) panels [0]).getFileName ();
        String packg = ((GLFTemplateWizardPanel1) panels [0]).getPackage ();
        String mimeType = ((GLFTemplateWizardPanel2) panels [1]).getMimeType ();
        String extensions = ((GLFTemplateWizardPanel2) panels [1]).getExtensions ();
        
        NbModuleProvider module = (NbModuleProvider) getProject ().getLookup ().
            lookup (NbModuleProvider.class);
        FileObject srcRoot = module.getSourceDirectory ();
        FileObject manifestFO = FileUtil.createData (srcRoot, getManifest ());
        File manifest = new File (getManifest ());
        manifest = manifest.getParentFile ();
        String path = manifest.getPath ().replace (File.separatorChar, '/');
        FileObject mimeResolverFO = FileUtil.createData (
            srcRoot, 
            path + "/MIMEResolver.xml" //NOI18N
        );
        FileObject nbsFO = FileUtil.createData (
            srcRoot, 
            packg.replace ('.', '/') + '/' + fileName + ".nbs" //NOI18N
        );
        InputStream is = getClass ().getResourceAsStream ("/org/netbeans/modules/apisupport/project/ui/resources/NBSTemplate.nbs");
        try {
            OutputStream os = nbsFO.getOutputStream ();
            try {
                FileUtil.copy (is, os);
            } finally {
                os.close ();
            }
        } finally {
            is.close ();
        }
        BufferedWriter writer = new BufferedWriter (new OutputStreamWriter (mimeResolverFO.getOutputStream ()));
        try {
            writer.append ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.append ("<!DOCTYPE MIME-resolver PUBLIC \"-//NetBeans//DTD MIME Resolver 1.0//EN\" \"http://www.netbeans.org/dtds/mime-resolver-1_0.dtd\">\n");
            writer.append ("<MIME-resolver>\n");
            writer.append ("    <file>\n");
            StringTokenizer st = new StringTokenizer (extensions, " ");
            while (st.hasMoreElements ()) {
                writer.append ("        <ext name=\"").append (st.nextToken ()).append ("\"/>\n");
            }
            writer.append ("        <resolver mime=\"").append (mimeType).append ("\"/>\n");
            writer.append ("    </file>\n");
            writer.append ("</MIME-resolver>\n");
        } finally {
            writer.close ();
        }
        writer = new BufferedWriter (new OutputStreamWriter (manifestFO.getOutputStream ()));
        int i = mimeType.indexOf ('/');
        String mimeType1 = mimeType.substring (0, i);
        String mimeType2 = mimeType.substring (i + 1);
        try {
            writer.append ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.append ("<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">\n");
            writer.append ("<filesystem>\n");
            writer.append ("    <folder name=\"Services\">\n");
            writer.append ("        <folder name=\"MIMEResolver\">\n");
            writer.append ("            <file name=\"MIMEResolver.xml\" url=\"MIMEResolver.xml\"/>\n");
            writer.append ("        </folder>\n");
            writer.append ("    </folder>\n");
            writer.append ("    <folder name=\"Navigator\">\n");
            writer.append ("        <folder name=\"Panels\">\n");
            writer.append ("            <folder name=\"").append (mimeType1).append ("\">\n");
            writer.append ("                <folder name=\"").append (mimeType2).append ("\">\n");
            writer.append ("                    <file name=\"org-netbeans-modules-languages-features-LanguagesNavigator.instance\"/>\n");
            writer.append ("                </folder>\n");
            writer.append ("            </folder>\n");
            writer.append ("        </folder>\n");
            writer.append ("    </folder>\n");
            writer.append ("    <folder name=\"Editors\">\n");
            writer.append ("        <folder name=\"").append (mimeType1).append ("\">\n");
            writer.append ("            <folder name=\"").append (mimeType2).append ("\">\n");
            writer.append ("                <file name=\"language.nbs\" url=\"").append (fileName).append (".nbs\"/>\n");
            writer.append ("            </folder>\n");
            writer.append ("        </folder>\n");
            writer.append ("    </folder>\n");
            writer.append ("</filesystem>\n");
        } finally {
            writer.close ();
        }
        DataObject nbsDO = DataObject.find (nbsFO);
        OpenCookie openCookie = nbsDO.getLookup().lookup (OpenCookie.class);
        openCookie.open ();
        return Collections.EMPTY_SET;
    }
    
    public void initialize (WizardDescriptor wizard) {
        this.wizard = wizard;
        System.out.println (wizard);
    }
    
    public void uninitialize (WizardDescriptor wizard) {
        panels = null;
    }
    
    public WizardDescriptor.Panel current () {
        return getPanels ()[index];
    }
    
    public String name () {
        return index + 1 + ". from " + getPanels ().length;
    }
    
    public boolean hasNext () {
        return index < getPanels ().length - 1;
    }
    
    public boolean hasPrevious () {
        return index > 0;
    }
    
    public void nextPanel () {
        if (!hasNext ()) {
            throw new NoSuchElementException ();
        }
        index++;
    }
    
    public void previousPanel () {
        if (!hasPrevious ()) {
            throw new NoSuchElementException ();
        }
        index--;
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener (ChangeListener l) {}
    public void removeChangeListener (ChangeListener l) {}
    
    // If something changes dynamically (besides moving between panels), e.g.
    // the number of panels changes in response to user input, then uncomment
    // the following and call when needed: fireChangeEvent();
    /*
    private Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0
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
    private String[] createSteps () {
        String[] beforeSteps = null;
        Object prop = wizard.getProperty ("WizardPanel_contentData");
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
                res[i] = panels[i - beforeSteps.length + 1].getComponent ().getName ();
            }
        }
        return res;
    }
    
    
    Project getProject () {
        return (Project) wizard.getProperty ("project");
    }
    
    String getManifest () {
        NbModuleProvider module = (NbModuleProvider) getProject ().getLookup ().
            lookup (NbModuleProvider.class);
        try {
            InputStream is = module.getManifestFile().getInputStream();
            try {
                Manifest manifest =  new Manifest (is);
                return manifest.getMainAttributes ().getValue ("OpenIDE-Module-Layer");
            } finally {
                is.close();
            }
        } catch (IOException e) {
            ErrorManager.getDefault ().notify (e);
        }
        return null;
    }
    
    List<String> getPackages () {
        List<String> result = new ArrayList<String> ();
        NbModuleProvider module = (NbModuleProvider) getProject ().getLookup ().
            lookup (NbModuleProvider.class);
        addPackages (module.getSourceDirectory (), "", result);
        return result;
    }
    
    private void addPackages (FileObject fo, String prefix, List<String> result) {
        FileObject[] children = fo.getChildren ();
        int i, k = children.length;
        for (i = 0; i < k; i++) {
            if (!children [i].isFolder ()) continue;
            result.add (prefix + children [i].getName ());
            addPackages (
                children [i], 
                prefix + children [i].getName () + '.', 
                result
            );
        }
    }
    
    WizardDescriptor getWizardDescriptor () {
        return wizard;
    }
}
