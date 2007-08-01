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

package org.netbeans.modules.j2me.cdc.project.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.modules.j2me.cdc.project.CDCPropertiesDescriptor;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.netbeans.modules.mobility.project.ui.wizard.PlatformInstallPanel;
import org.netbeans.modules.j2me.cdc.project.ui.CDCFoldersListSettings;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.ui.wizard.PlatformSelectionPanel;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;


/**
 * Wizard to create a new CDC project.
 */
public class NewCDCProjectWizardIterator implements TemplateWizard.Iterator {

    public static final int TYPE_APP = 0;
    public static final int TYPE_LIB = 1;
    public static final int TYPE_EXT = 2;
    public static final int TYPE_SAMPLE = 3;
    
    static final String PROP_NAME_INDEX = "nameIndex";      //NOI18N
    static final String MANIFEST_FILE = "manifest.mf"; // NOI18N

    private static final long serialVersionUID = 1L;
    
    private int type;
    
    /** Create a new wizard iterator. */
    public NewCDCProjectWizardIterator() {
        this(TYPE_APP);
    }
    
    public NewCDCProjectWizardIterator(int type) {
        this.type = type;
    }
        
    public static NewCDCProjectWizardIterator library() {
        return new NewCDCProjectWizardIterator( TYPE_LIB );
    }
    
    public static NewCDCProjectWizardIterator existing () {
        return new NewCDCProjectWizardIterator( TYPE_EXT );
    }

    private WizardDescriptor.Panel[] createPanels () {
        int i = getNumberOfCdcPlatforms();
        return i != 0 ?
            new WizardDescriptor.Panel[] {
                new PanelConfigureProject( this.type ),
                new PanelConfigurePlatform(),
                } :
            new WizardDescriptor.Panel[] {
                new PlatformInstallPanel.WizardPanel(CDCPlatform.PLATFORM_CDC),
                new PanelConfigureProject( this.type ),
                new PanelConfigurePlatform(),
                
            };
    }
    
    private String[] createSteps() {
        int i = getNumberOfCdcPlatforms();
        return i != 0 ?
            new String[] {
                NbBundle.getMessage(NewCDCProjectWizardIterator.class,"LAB_ConfigureProject"),
                NbBundle.getMessage(NewCDCProjectWizardIterator.class,"LAB_SelectPlatform"),
                 }:
            new String[] {
                NbBundle.getMessage(NewCDCProjectWizardIterator.class,"LAB_Step_AddPlatform"), //NOI18N
                NbBundle.getMessage(NewCDCProjectWizardIterator.class,"LAB_ConfigureProject"),
                NbBundle.getMessage(NewCDCProjectWizardIterator.class,"LAB_SelectPlatform"),
            };
    }
    
    private static String createMainClass( String mainClassName, FileObject srcFolder, String platformType) throws IOException {
        int lastDotIdx = mainClassName.lastIndexOf( '.' );
        String mName, pName;
        if ( lastDotIdx == -1 ) {
            mName = mainClassName.trim();
            pName = null;
        } else {
            mName = mainClassName.substring( lastDotIdx + 1 ).trim();
            pName = mainClassName.substring( 0, lastDotIdx ).trim();
        }
        if ( mName.length() == 0 || platformType == null) return null;
        FileObject mainTemplate = Repository.getDefault().getDefaultFileSystem().findResource("MainTemplates/org.netbeans.modules.kjava.j2meproject/" + platformType); //NOI18N
        if ( mainTemplate == null ) return null;
        String templateName = (String)mainTemplate.getAttribute("templateName"); //NOI18N
        String templateType = (String)mainTemplate.getAttribute("templateType"); //NOI18N
        if (templateName == null || templateType == null) return null;
        mainTemplate = Repository.getDefault().getDefaultFileSystem().findResource(templateName);
        if ( mainTemplate == null ) return null;
        DataObject mt = DataObject.find( mainTemplate );
        FileObject pkgFolder = srcFolder;
        if ( pName != null ) {
            String fName = pName.replace( '.', '/' ); // NOI18N
            pkgFolder = FileUtil.createFolder( srcFolder, fName );        
        }
        DataFolder pDf = DataFolder.findFolder( pkgFolder );        
        mt.createFromTemplate( pDf, mName );
        return templateType;
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
        return loc;
    }


    public static void generatePlatformProperties (CDCPlatform platform, String activeDevice, String activeProfile, EditableProperties props)  {
        Collection<FileObject> installFolders = platform.getInstallFolders();
        if (installFolders.size()>0) {            
            File jdkHome = FileUtil.toFile (installFolders.iterator().next());
            StringBuffer sbootcp = new StringBuffer();
            ClassPath bootCP = platform.getBootstrapLibrariesForProfile(activeDevice, activeProfile);
            for (ClassPath.Entry entry : (List<ClassPath.Entry>)bootCP.entries()) {
                URL url = entry.getURL();
                if ("jar".equals(url.getProtocol())) {              //NOI18N
                    url = FileUtil.getArchiveFile(url);
                }
                File root = new File (URI.create(url.toExternalForm()));
                if (sbootcp.length()>0) {
                    sbootcp.append(File.pathSeparator);
                }
                sbootcp.append(normalizePath(root, jdkHome, "platform.home"));
            }
            props.setProperty(CDCPropertiesDescriptor.PLATFORM_FAT_JAR, Boolean.toString(platform.isFatJar()));
            props.setProperty("platform.bootclasspath",sbootcp.toString());   //NOI18N
            props.setProperty("javac.source", platform.getClassVersion());
            props.setProperty("javac.target",  platform.getClassVersion());
        }
    }

    
    public Set<DataObject> instantiate (TemplateWizard wiz) throws IOException {
        Set<DataObject> resultSet = new HashSet<DataObject> ();
        File dirF = (File)wiz.getProperty("projdir");        //NOI18N
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }
        String name = (String)wiz.getProperty("name");        //NOI18N
        final String mainClass = (String)wiz.getProperty("mainClass");        //NOI18N
        String appName = (String)wiz.getProperty("appName"); //NOI18N

        final String activePlatform = (String)wiz.getProperty("activePlatform");       //NOI18N
        final String activeDevice   = (String)wiz.getProperty("activeDevice");         //NOI18N
        final String activeProfile  = (String)wiz.getProperty("activeProfile");        //NOI18N
        Properties props = (Properties) wiz.getProperty("additionalProperties"); //NOI18N               

            
        PlatformSelectionPanel.PlatformDescription pd=(PlatformSelectionPanel.PlatformDescription) wiz.getProperty(PlatformSelectionPanel.PLATFORM_DESCRIPTION);
        AntProjectHelper h =J2MEProjectGenerator.createProject(dirF, name, pd,new J2MEProjectGenerator.ProjectGeneratorCallback() {
        public void doPostGeneration(Project project, AntProjectHelper helper, FileObject projectLocation, File projectLocationFile, ArrayList<String> configurations) throws IOException {
            final FileObject src = projectLocation.createFolder("src");
            JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms (activePlatform, new Specification(CDCPlatform.PLATFORM_CDC,null));    //NOI18N
            if (platforms.length != 0){
                CDCPlatform cdcplatform = (CDCPlatform)platforms[0];
                final EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                if (mainClass != null)
                {
                    String templateType = createMainClass(mainClass, src, cdcplatform.getType());
                    ep.setProperty(CDCPropertiesDescriptor.MAIN_CLASS, mainClass);
                    if (templateType != null) {
                        ep.setProperty(CDCPropertiesDescriptor.MAIN_CLASS_CLASS, templateType);
                    }
                }
                ep.setProperty(CDCPropertiesDescriptor.APPLICATION_NAME, project.getProjectDirectory().getNameExt());
                ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_ACTIVE, cdcplatform.getAntName()); // NOI18N        
                ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_ACTIVE_DESCRIPTION, cdcplatform.getDisplayName()); // NOI18N        
                ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_TRIGGER, "CDC"); // NOI18N        
                ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_TYPE, cdcplatform.getType()); // NOI18N        
                String classVersion = cdcplatform.getClassVersion();
                ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_DEVICE, activeDevice); // NOI18N
                ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_PROFILE, activeProfile); // NOI18N
                //add bootclasspath
                generatePlatformProperties(cdcplatform, activeDevice, activeProfile, ep); // NOI18N
                ep.setProperty(DefaultPropertiesDescriptor.JAVAC_SOURCE, classVersion != null ? classVersion : "1.2"); // NOI18N
                ep.setProperty(DefaultPropertiesDescriptor.JAVAC_TARGET, classVersion != null ? classVersion : "1.2"); // NOI18N
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
            } else {
                throw new IllegalArgumentException("No CDC platform installed");// NOI18N
            } 
        }
        });
        if (mainClass != null && mainClass.length () > 0) {
            try {
                FileObject sourcesRoot = h.getProjectDirectory ().getFileObject ("src");        //NOI18N
                FileObject mainClassFo = getMainClassFO (sourcesRoot, mainClass);
                assert mainClassFo != null : "sourcesRoot: " + sourcesRoot + ", mainClass: " + mainClass;        //NOI18N
                // Returning FileObject of main class, will be called its preferred action
                resultSet.add (DataObject.find(mainClassFo));
            } catch (Exception x) {
                ErrorManager.getDefault().notify(x);
            }
        }

        FileObject dir = FileUtil.toFileObject(dirF);
        if (type == TYPE_APP || type == TYPE_EXT) {
            createManifest(dir, MANIFEST_FILE);
        }

        // Returning FileObject of project diretory. 
        // Project will be open and set as main
        Integer index = (Integer) wiz.getProperty(PROP_NAME_INDEX);
        switch (this.type) {
            case TYPE_APP:
                CDCFoldersListSettings.setNewApplicationCount(index);
                break;
            case TYPE_LIB:
                CDCFoldersListSettings.setNewLibraryCount(index.intValue());
                break;
            case TYPE_EXT:
                CDCFoldersListSettings.setNewProjectCount(index.intValue());
                break;
        }        
        resultSet.add (DataObject.find(h.getProjectDirectory()));

        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder (dirF);    
        }
                        
        return resultSet;
    }
    
        
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    
    public void initialize(TemplateWizard wiz) {
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
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
        wiz.putProperty("additionalProperties", new Properties());
    }

    public void uninitialize(TemplateWizard wiz) {
        
        if (wiz != null)
        {
            wiz.putProperty("additionalProperties", null);
            wiz.putProperty("projdir",null);           //NOI18N
            wiz.putProperty("name",null);          //NOI18N
            wiz.putProperty("mainClass",null);         //NOI18N
            if (this.type == TYPE_EXT) {
                wiz.putProperty("sourceRoot",null);    //NOI18N
                wiz.putProperty("testRoot",null);      //NOI18N
            }
        }
        wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format (NbBundle.getMessage(NewCDCProjectWizardIterator.class,"LAB_IteratorName"),
            new Object[] {new Integer (index + 1), new Integer (panels.length) });                                
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    public boolean hasPrevious() {
        return index > 0;
    }
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    public WizardDescriptor.Panel current () {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
    // helper methods, finds mainclass's FileObject
    private FileObject getMainClassFO (FileObject sourcesRoot, String mainClass) {
        // replace '.' with '/'
        mainClass = mainClass.replace ('.', '/'); // NOI18N
        
        // ignore unvalid mainClass ???
        
        return sourcesRoot.getFileObject (mainClass+ ".java"); // NOI18N
    }

    static String getPackageName (String displayName) {
        StringBuffer builder = new StringBuffer ();
        boolean firstLetter = true;
        for (int i=0; i< displayName.length(); i++) {
            char c = displayName.charAt(i);            
            if ((!firstLetter && Character.isJavaIdentifierPart (c)) || (firstLetter && Character.isJavaIdentifierStart(c))) {
                firstLetter = false;
                if (Character.isUpperCase(c)) {
                    c = Character.toLowerCase(c);
                }                    
                builder.append(c);
            }            
        }
        return builder.length() == 0 ? NbBundle.getMessage(NewCDCProjectWizardIterator.class,"TXT_DefaultPackageName") : builder.toString();
    }
    
    /**
     * Create a new application manifest file with minimal initial contents.
     * @param dir the directory to create it in
     * @param path the relative path of the file
     * @throws IOException in case of problems
     */
    static void createManifest(FileObject dir, String path) throws IOException {
        FileObject manifest = dir.createData(MANIFEST_FILE);
        FileLock lock = manifest.lock();
        try {
            OutputStream os = manifest.getOutputStream(lock);
            try {
                PrintWriter pw = new PrintWriter(os);
                pw.println("Manifest-Version: 1.0"); // NOI18N
                pw.println("X-COMMENT: Main-Class will be added automatically by build"); // NOI18N
                pw.println(); // safest to end in \n\n due to JRE parsing bug
                pw.flush();
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }

    static int getNumberOfCdcPlatforms(){
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms (null, new Specification(CDCPlatform.PLATFORM_CDC,null));    //NOI18N
        return platforms.length;
    }
}
