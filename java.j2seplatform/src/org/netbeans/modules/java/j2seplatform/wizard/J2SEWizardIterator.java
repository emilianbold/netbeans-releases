/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seplatform.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.java.j2seplatform.platformdefinition.J2SEPlatformImpl;
import org.netbeans.modules.java.j2seplatform.platformdefinition.PlatformConvertor;
import org.netbeans.modules.java.j2seplatform.platformdefinition.Util;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

/**
 * Wizard Iterator for standard J2SE platforms. It assumes that there is a
 * 'bin{/}java[.exe]' underneath the platform's directory, which can be run to
 * produce the target platform's VM environment.
 *
 * @author Svata Dedic, Tomas Zezula
 */
public class J2SEWizardIterator implements WizardDescriptor.InstantiatingIterator, Runnable {

    DataFolder                  installFolder;
    DetectPanel.WizardPanel     detectPanel;
    Collection                  listeners;
    JDKImpl                     platform;
    WizardDescriptor            wizard;
    boolean                     valid;
    int                         currentIndex;

    public J2SEWizardIterator(FileObject installFolder) throws IOException {
        this.installFolder = DataFolder.findFolder(installFolder);
        this.platform  = JDKImpl.create (installFolder);
    }

    FileObject getInstallFolder() {
        return installFolder.getPrimaryFile();
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public WizardDescriptor.Panel current() {
        switch (this.currentIndex) {
            case 0:
                return this.detectPanel;
            default:
                throw new IllegalStateException();
        }
    }

    public boolean hasNext() {
        return false;
    }

    public boolean hasPrevious() {
        return false;
    }

    public void initialize(WizardDescriptor wiz) {
        this.wizard = wiz;
        this. detectPanel = new DetectPanel.WizardPanel(this);
        this.currentIndex = 0;
    }

    /**
     * This finally produces the java platform's XML that represents the basic
     * platform's properties. The XML is returned in the resulting Set.
     * @return singleton Set with java platform's instance DO inside.
     */
    public java.util.Set instantiate() throws IOException {
        //Workaround #44444
        this.detectPanel.storeSettings (this.wizard);
        final String systemName = ((J2SEPlatformImpl)getPlatform()).getAntName();
        FileObject platformsFolder = Repository.getDefault().getDefaultFileSystem().findResource(
                "Services/Platforms/org-netbeans-api-java-Platform"); //NOI18N
        if (platformsFolder.getFileObject(systemName,"xml")!=null) {   //NOI18N
            String msg = NbBundle.getMessage(J2SEWizardIterator.class,"ERROR_InvalidName");
            throw (IllegalStateException)ErrorManager.getDefault().annotate(
                new IllegalStateException(msg), ErrorManager.USER, null, msg,null, null);
        }
        DataObject dobj = PlatformConvertor.create(getPlatform(), DataFolder.findFolder(platformsFolder),systemName);
        JavaPlatform platform = (JavaPlatform) dobj.getNodeDelegate().getLookup().lookup(JavaPlatform.class);
        return Collections.singleton(platform);
    }

    public String name() {
        return NbBundle.getMessage(J2SEWizardIterator.class, "TITLE_PlatformName");
    }

    public void nextPanel() {
        this.currentIndex++;
    }

    public void previousPanel() {
        this.currentIndex--;
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void uninitialize(WizardDescriptor wiz) {
        this.wizard = null;        
        this.detectPanel = null;
    }

    public boolean isValid() {
        return valid;
    }

    public JavaPlatform getPlatform() {
        return platform;
    }




    /**
     * Actually performs the detection and stores relevant information
     * in this Iterator
     */
    public void run() {
        try {
            FileObject java = Util.findTool("java", Collections.singleton(installFolder.getPrimaryFile()));
            if (java == null)
                return;
            File javaFile = FileUtil.toFile (java);
            if (javaFile == null)
                return;
            String javapath = javaFile.getAbsolutePath();
            String filePath = File.createTempFile("nb-platformdetect", "properties").getAbsolutePath();
            getSDKProperties(javapath, filePath);
            File f = new File(filePath);
            Properties p = new Properties();
            InputStream is = new FileInputStream(f);
            p.load(is);
            Map m = new HashMap(p.size());
            for (Enumeration en = p.keys(); en.hasMoreElements(); ) {
                String k = (String)en.nextElement();
                m.put(k, p.getProperty(k));
            }
            this.platform.loadProperties(m);
            this.valid = true;
            is.close();
            f.delete();
        } catch (IOException ex) {
            this.valid = false;
        }
    }

    private void getSDKProperties(String javaPath, String path) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        try {
            String[] command = new String[5];
            command[0] = javaPath;
            command[1] = "-classpath";    //NOI18N
            command[2] = InstalledFileLocator.getDefault().locate("modules/ext/org-netbeans-modules-java-j2seplatform-probe.jar", "org.netbeans.modules.java.j2seplatform", false).getAbsolutePath(); // NOI18N
            command[3] = "org.netbeans.modules.java.j2seplatform.wizard.SDKProbe";
            command[4] = path;
            final Process process = runtime.exec(command);
            // PENDING -- this may be better done by using ExecEngine, since
            // it produces a cancellable task.
            process.waitFor();
            int exitValue = process.exitValue();
            if (exitValue != 0)
                throw new IOException();
        } catch (InterruptedException ex) {
            IOException e = new IOException();
            ErrorManager.getDefault().annotate(e,ex);
            throw e;
        }
    }

    /**
     * Rather dummy implementation of the Java Platform, but sufficient for communication
       inside the Wizard.
     */
    static class JDKImpl extends J2SEPlatformImpl {

        static JDKImpl create (FileObject installFolder) throws IOException {
            assert installFolder != null;
            Map platformProperties = new HashMap ();
            return new JDKImpl(null,Collections.singletonList(installFolder.getURL()),platformProperties,Collections.EMPTY_MAP);
        }

        private JDKImpl(String name, List installFolders, Map platformProperties, Map systemProperties) {
            super(name, name, installFolders, platformProperties, systemProperties,null,null);
        }

        void loadProperties(Map m) {
            super.setSystemProperties(m);
        }
    }
}
