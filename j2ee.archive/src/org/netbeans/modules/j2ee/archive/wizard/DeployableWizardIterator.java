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

package org.netbeans.modules.j2ee.archive.wizard;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.archive.Util;
import org.netbeans.modules.j2ee.archive.project.ArchiveProject;
import org.netbeans.modules.j2ee.archive.project.ArchiveProjectProperties;
import org.netbeans.modules.j2ee.archive.project.ArchiveProjectType;
import org.netbeans.modules.j2ee.archive.ui.JavaEePlatformUiSupport;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectGenerator;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.dd.api.application.Web;
import org.netbeans.modules.j2ee.dd.api.client.AppClient;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.deployment.devmodules.api.AntDeploymentHelper;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.ejbjarproject.api.EjbJarProjectGenerator;
import org.netbeans.modules.web.project.api.WebProjectCreateData;
import org.netbeans.modules.web.project.api.WebProjectUtilities;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DeployableWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
    
    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;
    private static final String STEP_NAME_ONE = "LBL_CreateProjectStep";        // NOI18N
    public static final String PROJECT_DIR_PROP = "projdir";                           // NOI18N
    public static final String PROJECT_NAME_PROP = "name";                             // NOI18N
    private static final String ANT_VERSION_PROP = "minimum-ant-version";       // NOI18N
    private static final String MINIMUM_ANT_VERSION = "1.6";                    // NOI18N
    public static final String PROJECT_TARGET_PROP = "targetServer";                   // NOI18N
    public static final String PROJECT_ARCHIVE_PROP = "sourceArchive";                 // NOI18N
    public static final String PROJECT_TYPE_PROP = "type";                             // NOI18N
    public static final String SOURCE_JAR_CONST = "source-jar";                        // NOI18N
    
    static final String PROJECT_WAR_SUBARCHIVES_PROP = "warchives";     //NOI18N
    static final String PROJECT_JAR_SUBARCHIVES_PROP = "jarchives";     //NOI18N
    static final String PROJECT_RAR_SUBARCHIVES_PROP = "rarchives";     //NOI18N
    static final String PROJECT_CAR_SUBARCHIVES_PROP = "carchives";     //NOI18N
    
    public static final String PROJECT_HAS_DESCRIPTOR = "has.descriptor";              //NOI18N
    
    public DeployableWizardIterator() { 
        wiz = null;
        panels = new WizardDescriptor.Panel[0];
    }
    
    public static DeployableWizardIterator createIterator() {
        return new DeployableWizardIterator();
    }
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new DeployableWizardPanel(),
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(DeployableWizardIterator.class, STEP_NAME_ONE),
        };
    }
    
    
    public Set<FileObject> instantiate() throws IOException {
        return instantiate(null, null);
    }
    
    private Set<FileObject> instantiate(String distArchive, ProgressHandle ph) throws IOException {
        // test to see if we should really continue for a jar HERE...
        final File archiveFile = (File) wiz.getProperty(PROJECT_ARCHIVE_PROP);
        Object type = wiz.getProperty(PROJECT_TYPE_PROP);
            
        File dirF = FileUtil.normalizeFile((File) wiz.getProperty(PROJECT_DIR_PROP));
        final FileObject dir = FileUtil.createFolder(dirF);

        final AntProjectHelper h = ProjectGenerator.createProject(dir,
                ArchiveProjectType.TYPE);
        ReferenceHelper rH = new ReferenceHelper(h,
                h.createAuxiliaryConfiguration(),h.getStandardPropertyEvaluator());

        //
        // persist the project attributes
        //
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(ArchiveProjectType.PROJECT_CONFIGURATION_NS, PROJECT_NAME_PROP); // NOI18N
        nameEl.appendChild(doc.createTextNode(wiz.getProperty(PROJECT_NAME_PROP).toString()));
        data.appendChild(nameEl);
        Element minant = doc.createElementNS(ArchiveProjectType.PROJECT_CONFIGURATION_NS, ANT_VERSION_PROP); // NOI18N
        minant.appendChild(doc.createTextNode(MINIMUM_ANT_VERSION)); // NOI18N
        data.appendChild(minant);
        Element sourceRoots = doc.createElementNS(ArchiveProjectType.PROJECT_CONFIGURATION_NS, SOURCE_JAR_CONST);  //NOI18N
        data.appendChild(sourceRoots);
        String jarReference = rH.createForeignFileReference(FileUtil.normalizeFile(archiveFile), JavaProjectConstants.ARTIFACT_TYPE_JAR);
        sourceRoots.appendChild(doc.createTextNode(jarReference));
        h.putPrimaryConfigurationData(data, true);
        //
        // write the properties file
        //
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        Deployment deployment = Deployment.getDefault();
        final Object serverInstanceID = wiz.getProperty(PROJECT_TARGET_PROP);

        ep.setProperty(ArchiveProjectProperties.J2EE_SERVER_TYPE,
                deployment.getServerID(JavaEePlatformUiSupport.getServerInstanceID(serverInstanceID)));
        ep.setProperty(ArchiveProjectProperties.SOURCE_ARCHIVE,
                jarReference);
        ep.setProperty(ArchiveProjectProperties.DIST_DIR,ArchiveProjectProperties.DIST_DIR_VALUE);
        ep.setProperty(ArchiveProjectProperties.WAR_NAME,archiveFile.getName());
        ep.setProperty(ArchiveProjectProperties.ARCHIVE_TYPE, (String) type);
        ep.setProperty(ArchiveProjectProperties.PROXY_PROJECT_DIR, ArchiveProjectProperties.TMP_PROJ_DIR_VALUE);
        if (null == distArchive) {
            ep.setProperty(ArchiveProjectProperties.DIST_ARCHIVE, "${dist.dir}/${war.name}");//NOI18N
        } else {
            ep.setProperty(ArchiveProjectProperties.DIST_ARCHIVE, distArchive);
        }
        if (ArchiveProjectProperties.PROJECT_TYPE_VALUE_WAR.equals(ep.getProperty(ArchiveProjectProperties.ARCHIVE_TYPE))) {
            ep.setProperty(ArchiveProjectProperties.CONTENT_DIR,"${proxy.project.dir}/web");//NOI18N
            ep.setProperty(ArchiveProjectProperties.CONF_DIR, "${proxy.project.dir}/web/WEB-INF");//NOI18N
        } else {
            ep.setProperty(ArchiveProjectProperties.CONTENT_DIR,"${proxy.project.dir}/src/java");//NOI18N
            ep.setProperty(ArchiveProjectProperties.CONF_DIR, "${proxy.project.dir}/src/conf");     //NOI18N
        }

        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);

        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        // ant deployment support
        File projectFolder = dirF; // FileUtil.toFile(dirFO);
        try {
            AntDeploymentHelper.writeDeploymentScript(new File(projectFolder, ArchiveProjectProperties.ANT_DEPLOY_BUILD_SCRIPT),
                    ArchiveProjectProperties.mapType((String) wiz.getProperty(PROJECT_TYPE_PROP)),
                    JavaEePlatformUiSupport.getServerInstanceID(serverInstanceID));
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        }
        File deployAntPropsFile = AntDeploymentHelper.getDeploymentPropertiesFile(JavaEePlatformUiSupport.getServerInstanceID(serverInstanceID));
        if (deployAntPropsFile != null) {
            ep.setProperty(ArchiveProjectProperties.DEPLOY_ANT_PROPS_FILE, deployAntPropsFile.getAbsolutePath());
        }
        ep.setProperty(ArchiveProjectProperties.J2EE_SERVER_INSTANCE, JavaEePlatformUiSupport.getServerInstanceID(serverInstanceID));
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);

        final Project p = ProjectManager.getDefault().findProject(dir);
        ProjectManager.getDefault().saveProject(p);

        // explode the project here....
        final ArchiveProject ap = (ArchiveProject) p.getLookup().lookup(ArchiveProject.class);
        if (null != ap) {
            if (null == distArchive) {
                // start thread to unzip the project tree
                Runnable t = new  Runnable() {
                    public void run() {
                        final Thread t = Thread.currentThread();
                        ProgressHandle ph  = ProgressHandleFactory.createHandle(//"DISPLAY NAME",
                                NbBundle.getMessage(DeployableWizardIterator.class,
                                "TITLE_PROJECT_CREATE_STATUS"),     // NOI18N 
                                new Cancellable() {
                            public boolean cancel() {
                                t.interrupt();
                                return true;
                            }                                
                        });
                        ph.start();
                        boolean cleanup = false;
                        try {
                            explodeTheProject(dir, archiveFile, ap, (String) wiz.getProperty(PROJECT_TYPE_PROP),
                                    JavaEePlatformUiSupport.getServerInstanceID(serverInstanceID),ph);
                            ph.progress(
                                    NbBundle.getMessage(DeployableWizardIterator.class,
                                    "MESS_OPEN_PROJECT"));  //NOI18N
                            OpenProjects.getDefault().open(new Project[] {p},false);
                        } catch (java.nio.channels.ClosedByInterruptException cbie) {
                            // I see this when i shoot the thread...
                            // so I will just clean up...
                            cleanup = true;
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                    cbie);
                        } catch (IOException ioe) {
                            // I don't know exactly what is up here
                            cleanup = true;
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                    ioe);
                        } catch (SAXException saxe) {
                            // could not parse the application.xml file...
                            cleanup = true;
                            ErrorManager.getDefault().notify(ErrorManager.WARNING,
                                    saxe);
                        } catch (RuntimeException rte) {
                            cleanup = true;
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                    rte);
                        } finally {
                            if (cleanup) {
                                ph.progress(
                                        NbBundle.getMessage(DeployableWizardIterator.class,
                                        "MESS_CLEAN_UP"));      // NOI18N
                                try {
                                    Thread.sleep(500);
                                    ph.progress(
                                            NbBundle.getMessage(DeployableWizardIterator.class,
                                            "MESS_CLEAN_UP2"));     // NOI18N
                                    dir.delete();
                                    Thread.sleep(500);
                                    ap.getAntProjectHelper().notifyDeleted();
                                } catch (InterruptedException ie) {
                                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                            ie);
                                } catch (IOException ex) {
                                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                            ex);
                                }
                            }
                            ph.finish();
                            wiz.putProperty(PROJECT_DIR_PROP,null);
                            wiz.putProperty(PROJECT_NAME_PROP,null);
                            wiz = null;
                        }
                    }
                };
                RequestProcessor.getDefault().post(t);
            } else {
                try {
                    explodeTheProject(dir, archiveFile, ap, (String) wiz.getProperty(PROJECT_TYPE_PROP),
                            JavaEePlatformUiSupport.getServerInstanceID(serverInstanceID),
                            ph);
                } catch (SAXException saxe) {
                    IOException ioe = new IOException();
                    ioe.initCause(saxe);
                    throw ioe;
                }
            }
        }

        File parent = dirF.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }
        return Collections.EMPTY_SET;
    }
    
    
    private static FileObject saveZipEntryAsFileObject(final ZipInputStream str ,final FileObject projectRoot, String name) throws IOException{
        FileObject fo = FileUtil.createData(projectRoot, name);
        FileLock lock = fo.lock();
        try {
            OutputStream out = fo.getOutputStream(lock);
            try {
                FileUtil.copy(str, out);
            } finally {
                out.close();
            }
        } finally {
            lock.releaseLock();
        }
        return fo;
    }
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, (Integer) i);//NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);//NOI18N
            }
        }
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(NbBundle.getBundle(DeployableWizardIterator.class).getString("{0}_of_{1}"), //NOI18N
                new Object[] {(Integer)(index + 1), (Integer) panels.length});
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
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
    
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
    void explodeTheProject(final FileObject dir, final File sourceArchive,
            final ArchiveProject p, final String type, final String sid,
            ProgressHandle ph) throws IOException, SAXException {
        // explode the archive file
        FileObject subDir = dir.createFolder(ArchiveProjectProperties.TMP_PROJ_DIR_VALUE);
        FileObject srcArchive = FileUtil.toFileObject(sourceArchive);
        Unzipper t = new Unzipper(srcArchive, subDir, p, type,
                sid, ph);
        t.run();
    }
    
    static boolean isEjbJar(JarFile jf) throws IOException {
        boolean retVal = false;
        if (jf.getEntry("META-INF/ejb-jar.xml") != null) {  //NOI18N
            retVal = true;
        } else {
            retVal = EJBAnnotationDetector.containsSomeAnnotatedEJBs(jf);
        }
        return retVal;
    }
    
    static boolean hasMain(JarFile jf) {
        boolean retVal = false;
        // throws IOE
        try {
            Manifest mf = jf.getManifest();
            if (null != mf) {
                Attributes attrs = mf.getMainAttributes();
                retVal = attrs.getValue("Main-Class") != null;  //NOI18N
            }
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        }
        return retVal;
    }
    
    static private int count = 0;
    
    static private class Unzipper {
        FileObject srcArchive, subDir;
        
        ArchiveProject p = null;
        
        String typeProp, sid;
        ProgressHandle ph;
        
        Unzipper(FileObject srcArchive, FileObject subDir, ArchiveProject p,
                String typeProp, String sid, ProgressHandle ph) {
            this.srcArchive = srcArchive;
            this.subDir = subDir;
            this.p = p;
            this.typeProp = typeProp;
            this.sid = sid;
            this.ph = ph;
        }
        
        public void run() throws IOException, SAXException {
            ph.progress(NbBundle.getMessage(DeployableWizardIterator.class,
                    "MESS_EXPAND_ARCHIVE",srcArchive.getNameExt()));        // NOI18N
            if (ArchiveProjectProperties.PROJECT_TYPE_VALUE_EAR.equals(typeProp)) { // ep.getProperty(ArchiveProjectProperties.ARCHIVE_TYPE))) {
                // save real application.xml to nbproject directory
                ZipInputStream zstr = null;
                FileObject copiedAppXml = null;
                try {
                    
                    zstr = new ZipInputStream(new FileInputStream(FileUtil.toFile(srcArchive))); // archiveFile));
                    ZipEntry entry;
                    while ((entry = zstr.getNextEntry()) != null &&
                            copiedAppXml == null) {
                        String ename = entry.getName();
                        if (ename.endsWith("META-INF/application.xml")) {           // NOI18N
                            // found a real application.xml
                            copiedAppXml = saveZipEntryAsFileObject(zstr,p.getProjectDirectory(),"nbproject/application.xml"); // NOI18N
                        }
                    }
                } catch (java.nio.channels.ClosedByInterruptException cbie) {
                    // I see this when i shoot the thread...
                    // so I will just clean up...
                    throw cbie;
                } catch (java.io.FileNotFoundException fnfe) {
                    // the file got deleted while we were in the wizard
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, fnfe);
                } catch (java.io.IOException ioe) {
                    // the file couldn't be saved
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
                } finally {
                    if (null != zstr) {
                        try {
                            zstr.close();
                        } catch (IOException ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                    }
                }
            }
            boolean isWar = ArchiveProjectProperties.PROJECT_TYPE_VALUE_WAR.equals(typeProp);
            boolean isEar = ArchiveProjectProperties.PROJECT_TYPE_VALUE_EAR.equals(typeProp);
            boolean isCar = ArchiveProjectProperties.PROJECT_TYPE_VALUE_CAR.equals(typeProp);
            boolean isJar = ArchiveProjectProperties.PROJECT_TYPE_VALUE_JAR.equals(typeProp);
            boolean isRar = ArchiveProjectProperties.PROJECT_TYPE_VALUE_RAR.equals(typeProp);
            FileObject srcDir = subDir.createFolder("src");         // NOI18N
            FileObject javaDir = srcDir.createFolder("java");       // NOI18N
            if (isWar) {
                handleWarFile(javaDir);
            } else {
                handleJavaEeArchiveFile(srcDir, isCar, isEar, isJar, javaDir, isRar);
            }
        }
        
        private void handleJavaEeArchiveFile(final FileObject srcDir,
                boolean isCar, final boolean isEar, boolean isJar,
                final FileObject javaDir, boolean isRar) throws FileNotFoundException, IOException, SAXException {
            FileObject confDir;
            // this should work for ejb-jar/rar/car archives...
            confDir = srcDir.createFolder("conf");  //NOI18N
            // run the annotation detector here for jar/car descrimination
            if (!isEar && !isRar && !isCar && !isJar) {
                JarFile jf = new JarFile(FileUtil.toFile(srcArchive));
                isJar = isEjbJar(jf);
                isCar = hasMain(jf);
            }
            
            unZipFile(srcArchive.getInputStream(), javaDir);
            
            FileObject metaInf = javaDir.getFileObject("META-INF");     // NOI18N
            FileObject[] filesToMove = metaInf.getChildren();
            FileObject ddFile = metaInf.getFileObject("ejb-jar.xml");   // NOI18N
            if (null == ddFile){
                ddFile = metaInf.getFileObject("application-client.xml");   // NOI18N
            }
            if (null == ddFile){
                ddFile = metaInf.getFileObject("application.xml");      // NOI18N
            }
            if (null == ddFile){
                if (isEar) {
                    // build the nbproject/application.xml file here.
                    determineSubarchiveTypes(javaDir);
                }
                ddFile = metaInf.getFileObject("ra.xml");               // NOI18N
            }
            String versionVal = getJavaEEVersion(ddFile);
            int len = 0;
            if (null != filesToMove) {
                len = filesToMove.length;
            }
            for (int i = 0; i < len; i++) {
                if (filesToMove[i].isData()) {
                    FileUtil.moveFile(filesToMove[i],confDir,
                            filesToMove[i].getName());
                } // else -- this is a folder
            }
            EditableProperties ep = p.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            if (isJar) {
                ep.setProperty(ArchiveProjectProperties.ARCHIVE_TYPE,
                        ArchiveProjectProperties.PROJECT_TYPE_VALUE_JAR);
                p.getAntProjectHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(p);
                EjbJarProjectGenerator.importProject(FileUtil.toFile(subDir),
                        srcArchive.getName(), new File[] {FileUtil.toFile(srcDir.getFileObject("java"))},      // NOI18N
                        new File[0],FileUtil.toFile(confDir),null,versionVal,
                        sid,false);
//                EditableProperties subEp = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
//                subEp.setProperty(SourceTypeConstants.PROJECT_SOURCES_TYPE,SourceTypeConstants.SOURCES_VALUE_CLASS); // NOI18N
//                h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,subEp);
//                Project subP = ProjectManager.getDefault().findProject(h.getProjectDirectory());
//                ProjectManager.getDefault().saveProject(subP);
            } else if (isCar) {
                ep.setProperty(ArchiveProjectProperties.ARCHIVE_TYPE,
                        ArchiveProjectProperties.PROJECT_TYPE_VALUE_CAR);
                p.getAntProjectHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                ProjectManager.getDefault().saveProject(p);
                AppClientProjectGenerator.importProject(FileUtil.toFile(subDir),
                        "tmpProjName"+count, new File[] {FileUtil.toFile(srcDir)},      // NOI18N
                        new File[0],FileUtil.toFile(confDir), null, versionVal,
                        sid,false);
//                EditableProperties subEp = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
//                subEp.setProperty(SourceTypeConstants.PROJECT_SOURCES_TYPE,SourceTypeConstants.SOURCES_VALUE_CLASS); // NOI18N
//                h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,subEp);
//                Project subP = ProjectManager.getDefault().findProject(h.getProjectDirectory());
//                ProjectManager.getDefault().saveProject(subP);
            } else if (isEar) {
                createArchiveProjects();
            }
        }
        
        private void determineSubarchiveTypes(FileObject rootDir) throws IOException, SAXException {
            InputStream is = DeployableWizardIterator.class.getResourceAsStream("template-application.xml");  // NOI18N
            InputSource saxIs = new InputSource(is);
            Application app;
            FileLock outLock = null;
            OutputStream outStream = null;
            try {
                // throws IOE or SAXEx
                app = org.netbeans.modules.j2ee.dd.api.application.DDProvider.getDefault().getDDRoot(saxIs);
                if (null != app) {
                    Enumeration files = rootDir.getData(true);
                    Module m;
                    while (files.hasMoreElements()) {
                        FileObject fo = (FileObject) files.nextElement();
                        String pathInEar = PropertyUtils.relativizeFile(FileUtil.toFile(rootDir),
                                FileUtil.toFile(fo));
                        if (pathInEar.startsWith("lib/")) {     // NOI18N
                            continue;
                        }
                        if (pathInEar.endsWith("war")) {        // NOI18N
                            Web w;
                            m = app.newModule();
                            w = m.newWeb();
                            w.setWebUri(pathInEar);
                            // I don't need this
                            //w.setContextRoot(pathToCR(aPath));
                            m.setWeb(w);
                            app.addModule(m);
                            continue;
                        }
                        if (pathInEar.endsWith("rar")) {        // NOI18N
                            m = app.newModule();
                            m.setConnector(pathInEar);
                            app.addModule(m);
                            continue;
                        }
                        if (pathInEar.endsWith("jar")) {        // NOI18N
                            // throws IOE
                            JarFile jf = new JarFile(FileUtil.toFile(fo));
                            if (isEjbJar(jf)) {
                                m = app.newModule();
                                m.setEjb(pathInEar);
                                app.addModule(m);
                                continue;
                            }
                            if (hasMain(jf)) { // this may be a bit course
                                m = app.newModule();
                                m.setJava(pathInEar);
                                app.addModule(m);
                                continue;
                            }
                        }
                    }
                    // outstream needs to write to nbproject/application.xml
                    // throws IOE
                    FileObject out = FileUtil.createData(p.getProjectDirectory(),"nbproject/application.xml");  // NOI18N
                    outLock = out.lock();
                    outStream = out.getOutputStream(outLock);
                    app.write(outStream);
                }
            } finally {
                if (null != outStream) {
                    try {
                        outStream.close();
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                ioe);
                    }
                }
                if (null != outLock) {
                    outLock.releaseLock();
                }
                if (null != is) {
                    try {
                        is.close();
                    } catch (IOException ioe) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL,
                                ioe);
                    }
                }
                
            }
        }
        
        
        private AntProjectHelper handleWarFile(final FileObject javaDir) throws FileNotFoundException, IOException {
            FileObject webDir = subDir.createFolder("web");  //NOI18N
            //
            unZipFile(srcArchive.getInputStream(), webDir);
            FileObject webInf = webDir.getFileObject("WEB-INF");  //NOI18N
            // The web-inf is a MUST HAVE
            if (null == webInf) {
                throw new FileNotFoundException(NbBundle.getMessage(DeployableWizardIterator.class,
                        "ERROR_MISSING_WEB_INF",webDir, srcArchive));  //NOI18N
            }
            FileObject webDotXml = null;
            if (webInf.isFolder()) {
                webDotXml = webInf.getFileObject("web.xml");  //NOI18N
            }            
            WebProjectCreateData createData = new WebProjectCreateData();
            createData.setProjectDir(FileUtil.toFile(subDir));
            createData.setName("tmpProjName"+count++); //NOI18N
            createData.setWebModuleFO(webInf);
            createData.setSourceFolders(new File[] {FileUtil.toFile(javaDir)});
            createData.setTestFolders(null);
            createData.setDocBase(webDir); //NOI18N
            createData.setLibFolder(null);
            createData.setJavaEEVersion(getJavaEEVersion(webDotXml));
            createData.setServerInstanceID(sid); // (String) app.get(ArchiveProjectProperties.J2EE_SERVER_INSTANCE)
            createData.setBuildfile(GeneratedFilesHelper.BUILD_XML_PATH);
            createData.setJavaSourceBased(false);
            createData.setWebInfFolder(webInf);
            createData.setSourceLevel("1.5");  //NOI18N
            return WebProjectUtilities.importProject(createData);
        }
        
        
        private void createArchiveProjects() throws IOException {
            // get the application.xml file
            FileObject appXml = p.getProjectDirectory().getFileObject("nbproject").getFileObject("application.xml"); // NOI18N
            if (null != appXml) {
                Application app = org.netbeans.modules.j2ee.dd.api.application.DDProvider.getDefault().getDDRoot(appXml);
                Module ms[] = app.getModule();
                //int subprojIndex = 0;
                for (Module m : ms) {
                    String archivePath = m.getEjb();
                    if (null != archivePath) {
                        createArchiveProject(ArchiveProjectProperties.PROJECT_TYPE_VALUE_JAR,
                                archivePath);
                    } else {
                        archivePath = m.getJava();
                        if (null != archivePath) {
                            createArchiveProject(ArchiveProjectProperties.PROJECT_TYPE_VALUE_CAR,
                                    archivePath);
                        } else {
                            archivePath = m.getConnector();
                            if (null != archivePath) {
                                createArchiveProject(ArchiveProjectProperties.PROJECT_TYPE_VALUE_RAR,
                                        archivePath);
                            } else {
                                Web w = m.getWeb();
                                if (null != w) {
                                    archivePath = w.getWebUri();
                                    if (null != archivePath) {
                                        createArchiveProject(ArchiveProjectProperties.PROJECT_TYPE_VALUE_WAR,
                                                archivePath);
                                    }
                                }
                            }
                        }
                    }
                    
                }
            } else {
                ErrorManager.getDefault().log(ErrorManager.WARNING, NbBundle.getMessage(ArchiveProject.class,"WARN_EAR_ARCH_MISSING_APPLICATION_XML"));  //NOI18N
            }
        }
        
        private void createArchiveProject(String type, String pathInEar) throws IOException {
            FileObject root = p.getProjectDirectory();
            FileObject subprojRoot = FileUtil.createFolder(root,"subarchives");     // NOI18N
            String subprojkey = Util.getKey(pathInEar);
            FileObject projDest = subprojRoot.createFolder(subprojkey);
            //p.setEarPath(subprojkey, pathInEar);
            DeployableWizardIterator dwi = new DeployableWizardIterator();
            WizardDescriptor wizDesc = new WizardDescriptor(new WizardDescriptor.Panel[0]);
            File oldVal = ProjectChooser.getProjectsFolder();
            wizDesc.putProperty(PROJECT_DIR_PROP,
                    FileUtil.toFile(projDest));
            FileObject archive = root.getFileObject(ArchiveProjectProperties.TMP_PROJ_DIR_VALUE).
                    getFileObject("src").getFileObject("java").getFileObject(pathInEar);  //NOI18N
            wizDesc.putProperty(PROJECT_ARCHIVE_PROP,
                    FileUtil.toFile(archive));
            wizDesc.putProperty(PROJECT_NAME_PROP,
                    subprojkey);
            
            wizDesc.putProperty(PROJECT_TARGET_PROP,
                    JavaEePlatformUiSupport.getServerInstanceID(sid));
            wizDesc.putProperty(PROJECT_TYPE_PROP, type);
            String distArchive =
                    PropertyUtils.relativizeFile(FileUtil.toFile(archive.getParent().getParent()),FileUtil.toFile(root))+
                    "/tmpproj/src/java/"+pathInEar;     // NOI18N
            dwi.initialize(wizDesc);
            dwi.instantiate(distArchive, ph);
            if (null != oldVal) {
                ProjectChooser.setProjectsFolder(oldVal);
            }
        }
        
        private void unZipFile(InputStream source, final FileObject projectRoot) throws IOException {
            try {
                final ZipInputStream str = new ZipInputStream(source);
                ZipEntry entry;
                while ((entry = str.getNextEntry()) != null) {
                    if (entry.isDirectory()) {
                        FileUtil.createFolder(projectRoot, entry.getName());
                    } else {
                        final ZipEntry fentry = entry;
                        if (fentry.getName().endsWith(".xml")){   //NOI18N //need atomic action because we do not want half complete xml file later one in other threads
                            FileSystem fs = projectRoot.getFileSystem();
                            fs.runAtomicAction(new FileSystem.AtomicAction() {
                                public void run() throws IOException {
                                    saveZipEntryAsFileObject( str , projectRoot, fentry.getName());
                                }
                            });
                        } else {
                            saveZipEntryAsFileObject( str , projectRoot, fentry.getName());
                            
                        }
                    }
                }
            } finally {
                source.close();
            }
            
        }
        
        private String getJavaEEVersion(FileObject f) throws IOException {
            String versionVal = "1.5";                                              //NOI18N
            if (null != f) {
                if (f.getName().startsWith("web")) {                                //NOI18N
                    WebApp wa = org.netbeans.modules.j2ee.dd.api.web.DDProvider.getDefault().getDDRoot(f);
                    String t = wa.getVersion();
                    if (!"2.5".equals(t)) { // NOI18N
                        versionVal = "1."+t.charAt(t.length()-1);  //NOI18N
                    }
                }
                if (f.getName().startsWith("ejb")) {                                //NOI18N
                    // TODO -- this doesn't work right... I always get a null here..
                    EjbJar ej = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(f);
                    if (null!=ej){
                        versionVal = ej.getJ2eePlatformVersion();
                    }
                }
                if (f.getName().startsWith("application-")) {                       //NOI18N
                    AppClient ac = org.netbeans.modules.j2ee.dd.api.client.DDProvider.getDefault().getDDRoot(f);
                    String t = ac.getVersion().toString();
                    if (!"5".equals(t)) {  //NOI18N
                        versionVal = t;
                    }                    
                }
                if (f.getName().equals("application")) {                            //NOI18N
                    Application ap = org.netbeans.modules.j2ee.dd.api.application.DDProvider.getDefault().getDDRoot(f);
                    versionVal  = ap.getVersion().toString();
                }
//                if (f.getName().equals("ra")) {
//                    // let's just stick to 1.5...
//                }
            }
            return versionVal;
        }
        
    }
}
