/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seplatform.wizard;

import java.io.*;
import java.util.*;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;


import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.ErrorManager;
import org.openide.modules.SpecificationVersion;

import org.netbeans.api.java.platform.*;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.modules.java.j2seplatform.platformdefinition.PlatformConvertor;
import org.netbeans.modules.java.j2seplatform.platformdefinition.J2SEPlatformImpl;
import org.netbeans.modules.java.j2seplatform.platformdefinition.Util;
import org.openide.WizardDescriptor;
import org.openide.modules.InstalledFileLocator;

/**
 * Wizard Iterator for standard J2SE platforms. It assumes that there is a
 * 'bin{/}java[.exe]' underneath the platform's directory, which can be run to
 * produce the target platform's VM environment.
 *
 * @author Svata Dedic, Tomas Zezula
 */
public class J2SEWizardIterator implements WizardDescriptor.InstantiatingIterator, Runnable {


    private static final String CLASSIC = "classic";        //NOI18N
    private static final String MODERN = "modern";          //NOI18N
    private static final String JAVAC13 = "javac1.3";       //NOI18N


    DataFolder                  installFolder;
    DetectPanel.WizardPanel     detectPanel;
    SrcDocLocation.Panel        srcDocPanel;
    Collection                  listeners;
    JDKImpl                     platform;
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

    public Panel current() {
        switch (this.currentIndex) {
            case 0:
                return this.detectPanel;
            case 1:
                return this.srcDocPanel;
            default:
                throw new IllegalStateException();
        }
    }

    public boolean hasNext() {
        return this.currentIndex == 0;
    }

    public boolean hasPrevious() {
        return this.currentIndex == 1;
    }

    public void initialize(WizardDescriptor wiz) {
        this. detectPanel = new DetectPanel.WizardPanel(this);
        this.srcDocPanel = new SrcDocLocation.Panel (this);
        this.currentIndex = 0;
    }

    /**
     * This finally produces the java platform's XML that represents the basic
     * platform's properties. The XML is returned in the resulting Set.
     * @return singleton Set with java platform's instance DO inside.
     */
    public java.util.Set instantiate() throws IOException {
        final String systemName = ((J2SEPlatformImpl)getPlatform()).getAntName();
        FileObject platformsFolder = Repository.getDefault().getDefaultFileSystem().findResource(
                "Services/Platforms/org-netbeans-api-java-Platform"); //NOI18N
        if (platformsFolder.getFileObject(systemName,"xml")!=null) {   //NOI18N
            String msg = NbBundle.getMessage(J2SEWizardIterator.class,"ERROR_InvalidName");
            throw (IllegalStateException)ErrorManager.getDefault().annotate(
                new IllegalStateException(msg), ErrorManager.USER, null, msg,null, null);
        }
        try {
            ProjectManager.mutex().writeAccess(
                    new Mutex.ExceptionAction () {
                        public Object run () throws Exception{
                            String homePropName = createName(systemName,"home");      //NOI18N
                            String bootClassPathPropName = createName(systemName,"bootclasspath");    //NOI18N
                            String compilerType= createName (systemName,"compiler");  //NOI18N
                            EditableProperties props = PropertyUtils.getGlobalProperties();
                            if (props.getProperty(homePropName) != null || props.getProperty(bootClassPathPropName) != null
                               || props.getProperty(compilerType)!=null) {
                                //Already defined warn user
                                String msg = NbBundle.getMessage(J2SEWizardIterator.class,"ERROR_InvalidName"); //NOI18N
                                throw (IllegalStateException)ErrorManager.getDefault().annotate(
                                        new IllegalStateException(msg), ErrorManager.USER, null, msg,null, null);
                            }
                            JavaPlatform platform = getPlatform();
                            File jdkHome = FileUtil.toFile ((FileObject)platform.getInstallFolders().iterator().next());
                            props.setProperty(homePropName, jdkHome.getAbsolutePath());
                            ClassPath bootCP = getPlatform().getBootstrapLibraries();
                            StringBuffer sbootcp = new StringBuffer();
                            for (Iterator it = bootCP.entries().iterator(); it.hasNext();) {
                                ClassPath.Entry entry = (ClassPath.Entry) it.next();
                                URL url = entry.getURL();
                                if ("jar".equals(url.getProtocol())) {              //NOI18N
                                    url = FileUtil.getArchiveFile(url);
                                }
                                File root = new File (URI.create(url.toExternalForm()));
                                if (sbootcp.length()>0) {
                                    sbootcp.append(File.pathSeparator);
                                }
                                sbootcp.append(normalizePath(root, jdkHome, homePropName));
                            }
                            props.setProperty(bootClassPathPropName,sbootcp.toString());   //NOI18N
                            props.setProperty(compilerType,getCompilerType(getPlatform()));
                            FileObject tool = platform.findTool ("javac");                          //NOI18N
                            if (tool != null) {
                                if (!isDefaultLocation(tool,platform.getInstallFolders())) {
                                    String toolName = createName(systemName,"javac");               //NOI18N
                                    props.setProperty(toolName,normalizePath(getToolPath(tool),jdkHome, homePropName));
                                }
                            }
                            else {
                                //TODO: User should be asked about the exact path to javac
                                throw new IOException ("Can not locate javac command");

                            }
                            tool = platform.findTool ("java");                                      //NOI18N
                            if (tool != null) {
                                if (!isDefaultLocation(tool,platform.getInstallFolders())) {
                                    String toolName = createName(systemName,"java");
                                    props.setProperty(toolName,normalizePath(getToolPath(tool),jdkHome, homePropName));
                                }
                            }
                            else {
                                //TODO: User should be asked about the exact path to java
                                throw new IOException ("Can not locate java command");
                            }
                            PropertyUtils.putGlobalProperties (props);
                            return null;
                        }
                    }
            );
            DataObject dobj = PlatformConvertor.create(getPlatform(), DataFolder.findFolder(platformsFolder),systemName);
            return Collections.singleton(dobj);
        } catch (MutexException me) {
            Exception originalException = me.getException();
            if (originalException instanceof RuntimeException) {
                throw (RuntimeException) originalException;
            }
            else if (originalException instanceof IOException) {
                throw (IOException) originalException;
            }
            else
            {
                throw new IllegalStateException (); //Should never happen
            }
        }
    }

    public String name() {
        return NbBundle.getMessage(J2SEWizardIterator.class, "TITLE_J2SEWizardIterator_Configure");
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
        this.detectPanel = null;
        this.srcDocPanel = null;
    }

    public boolean isValid() {
        return valid;
    }

    public JavaPlatform getPlatform() {
        return platform;
    }


    private static boolean isDefaultLocation (FileObject tool, Collection installFolders) {
        assert tool != null && installFolders != null;
        if (installFolders.size()!=1)
            return false;
        FileObject root = (FileObject)installFolders.iterator().next();
        String relativePath = FileUtil.getRelativePath(root,tool);
        if (relativePath == null) {
            return false;
        }
        StringTokenizer tk = new StringTokenizer(relativePath, "/");
        return (tk.countTokens()== 2 && "bin".equals(tk.nextToken()));
    }


    private static File getToolPath (FileObject tool) throws IOException {
        assert tool != null;
        return new File (URI.create(tool.getURL().toExternalForm()));
    }

    private static String normalizePath (File path,  File jdkHome, String propName) {
        String jdkLoc = jdkHome.getAbsolutePath();
        if (!jdkLoc.endsWith(File.separator)) {
            jdkLoc = jdkLoc + File.separator;
        }
        String loc = path.getAbsolutePath();
        if (loc.startsWith(jdkLoc)) {
            return "${"+propName+"}"+File.separator+loc.substring(jdkLoc.length());           //NOI18N
        }
        else {
            return loc;
        }
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
        URL codeBase = getClass().getProtectionDomain().getCodeSource().getLocation();
        try {
            String[] command = new String[5];
            command[0] = javaPath;
            command[1] = "-classpath";    //NOI18N
            command[2] = InstalledFileLocator.getDefault().locate("modules/ext/org-netbeans-modules-java-j2seplatform-probe.jar", "org.netbeans.modules.java.j2seplatform", false).getAbsolutePath(); // NOI18N
            command[3] = "org.netbeans.modules.java.j2seplatform.wizard.SDKProbe";
            command[4] = path;
            System.out.println("Probe command: "+Arrays.asList(command));
            final Process process = runtime.exec(command);
            // PENDING -- this may be better done by using ExecEngine, since
            // it produces a cancellable task.
            process.waitFor();
            int exitValue = process.exitValue();
            System.out.println("Probe exit status: "+exitValue);
            if (exitValue != 0)
                throw new IOException();
        } catch (InterruptedException ex) {
            IOException e = new IOException();
            ErrorManager.getDefault().annotate(e,ex);
            throw e;
        }
    }


    private static String createName (String propName, String propType) {
        return "platforms." + propName + "." + propType;        //NOI18N
    }

    private static String getCompilerType (JavaPlatform platform) {
        assert platform != null;
        String prop = (String) platform.getSystemProperties().get("java.specification.version"); //NOI18N
        assert prop != null;
        SpecificationVersion specificationVersion = new SpecificationVersion (prop);
        SpecificationVersion jdk13 = new SpecificationVersion("1.3");   //NOI18N
        int c = specificationVersion.compareTo (jdk13);
        if (c<0) {
            return CLASSIC;
        }
        else if (c == 0) {
            return JAVAC13;
        }
        else {
            return MODERN;
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
