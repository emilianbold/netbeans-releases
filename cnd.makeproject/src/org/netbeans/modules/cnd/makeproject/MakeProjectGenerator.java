/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
//import org.netbeans.modules.cnd.makeproject.api.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.ui.support.ProjectChooser;

/**
 * Creates a MakeProject from scratch according to some initial configuration.
 */
public class MakeProjectGenerator {
    private MakeProjectGenerator() {}

    public static String getDefaultProjectFolder() {
	return ProjectChooser.getProjectsFolder().getPath();
    }

    public static String getValidProjectName(String projectFolder) {
	return getValidProjectName(projectFolder, "Project"); // NOI18N
    }

    public static String getValidProjectName(String projectFolder, String name) {
        int baseCount = 0;
	String projectName = null;
        while (true) {
            if (baseCount == 0)
                projectName = name;
            else
                projectName = name + baseCount;
	    File projectNameFile = new File(projectFolder + File.separator + projectName);
	    if (!projectNameFile.exists())
		break;
	    baseCount++;                
        }
	return projectName;
    }

    public static MakeProject createBlankProject(boolean open) throws IOException {
	String projectFolder = getDefaultProjectFolder();
	String projectName = getValidProjectName(projectFolder);
	String baseDir = projectFolder + File.separator + projectName;
	MakeConfiguration conf = new MakeConfiguration(baseDir, java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/Bundle").getString("DefaultProjectName"), MakeConfiguration.TYPE_MAKEFILE); // FIXUP
	return MakeProjectGenerator.createBlankProject(projectName, projectFolder, new MakeConfiguration[] {conf}, open);
    }

    public static MakeProject createBlankProject(String projectName, String projectFolder, boolean open) throws IOException {
	return createBlankProject(projectName, projectFolder, new MakeConfiguration[0], open);
    }
    
    public static MakeProject createBlankProject(String projectName, String makefileName, String projectFolder, boolean open) throws IOException {
	return createBlankProject(projectName, makefileName, projectFolder, new MakeConfiguration[0], open);
    }

    public static MakeProject createBlankProject(String projectName, String projectFolder, MakeConfiguration[] confs, boolean open) throws IOException {
        return createBlankProject(projectName, MakeConfigurationDescriptor. DEFAULT_PROJECT_MAKFILE_NAME, projectFolder, confs, open);
    }
    
    public static MakeProject createBlankProject(String projectName, String makefileName, String projectFolder, MakeConfiguration[] confs, boolean open) throws IOException {
	File projectNameFile = new File(projectFolder + File.separator + projectName);
	if (confs == null)
	    confs = new MakeConfiguration[0];
	for (int i = 0; i < confs.length; i++) {
	    confs[i].setBaseDir(projectNameFile.getPath());
	    RunProfile profile = (RunProfile) confs[i].getAuxObject(RunProfile.PROFILE_ID);
	    profile.setBuildFirst(false);
	}

        FileObject dirFO = createProjectDir (projectNameFile);
        AntProjectHelper h = createProject(dirFO, projectName, makefileName, confs, null, null);
        MakeProject p = (MakeProject)ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);

	if (open) {
	    OpenProjects.getDefault().open(new Project[] {p}, false);
            OpenProjects.getDefault().setMainProject(p);
        }
        
        return p;
    }
    
    /**
     * Create a new empty Make project.
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the name for the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(File dir, String name, String makefileName, Configuration[] confs, Iterator sourceFolders, Iterator importantItems) throws IOException {
        FileObject dirFO = createProjectDir (dir);
        AntProjectHelper h = createProject(dirFO, name, makefileName, confs, sourceFolders, importantItems); //NOI18N
        MakeProject p = (MakeProject)ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        //FileObject srcFolder = dirFO.createFolder("src"); // NOI18N
        return h;
    }

    /*
    public static AntProjectHelper createProject(final File dir, final String name, final File sourceFolder, final File testFolder) throws IOException {
	System.out.println("createProject2 ");
        assert sourceFolder != null : "Source folder must be given";   //NOI18N
        final FileObject dirFO = createProjectDir (dir);
        // this constructor creates only java application type
        final AntProjectHelper h = createProject(dirFO, name, null, null, null, null, false, 0, null, null);
        final MakeProject p = (MakeProject) ProjectManager.getDefault().findProject(dirFO);
        final ReferenceHelper refHelper = p.getReferenceHelper();
        try {
        ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction () {
            public Object run() throws Exception {
                String srcReference = refHelper.createForeignFileReference(sourceFolder, JavaProjectConstants.SOURCES_TYPE_JAVA);
                EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                props.put("src.dir",srcReference);          //NOI18N
                String testLoc;
                if (testFolder == null) {
                    testLoc = NbBundle.getMessage (MakeProjectGenerator.class,"TXT_DefaultTestFolderName");
                    File f = new File (dir,testLoc);    //NOI18N
                    f.mkdirs();
                }
                else {
                    if (!testFolder.exists()) {
                        testFolder.mkdirs();
                    }
                    h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
                    testLoc = refHelper.createForeignFileReference(testFolder, JavaProjectConstants.SOURCES_TYPE_JAVA);
                    props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH); // #47609
                }                
                props.put("test.src.dir",testLoc);    //NOI18N
                h.putProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                ProjectManager.getDefault().saveProject (p);
                return null;
            }
        });
        } catch (MutexException me ) {
            ErrorManager.getDefault().notify (me);
        }
        return h;
	return null;
    }
    */

    private static AntProjectHelper createProject(FileObject dirFO, String name, String makefileName, Configuration[] confs, Iterator sourceFolders, Iterator importantItems) throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, MakeProjectType.TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(MakeProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        //Element minant = doc.createElementNS(MakeProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        //minant.appendChild(doc.createTextNode("1.6")); // NOI18N
        //data.appendChild(minant);
        Element nativeProjectType = doc.createElementNS(MakeProjectType.PROJECT_CONFIGURATION_NAMESPACE, "make-project-type"); // NOI18N
        nativeProjectType.appendChild(doc.createTextNode("" + 0)); // NOI18N
        data.appendChild(nativeProjectType);
        h.putPrimaryConfigurationData(data, true);

        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
	//ep.setProperty("make.configurations", "");
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        //ep.setProperty("application.args", ""); // NOI18N
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);

	// Create new project descriptor with default configurations and save it to disk.
	MakeConfigurationDescriptor projectDescriptor = new MakeConfigurationDescriptor(FileUtil.toFile(dirFO).getPath());
        projectDescriptor.setProjectMakefileName(makefileName);
	projectDescriptor.init(confs);
	projectDescriptor.initLogicalFolders(sourceFolders, sourceFolders == null, importantItems); // FIXUP: need a better check whether logical folder should be ccreated or not.
	projectDescriptor.save();
	// create Makefile
	copyURLFile("nbresloc:/org/netbeans/modules/cnd/makeproject/resources/MasterMakefile",  // NOI18N
	    projectDescriptor.getBaseDir() + File.separator + projectDescriptor.getProjectMakefileName());
        return h;
    }

    private static void copyURLFile(String fromURL, String toFile) throws IOException {
	InputStream is = null;
	try {
	    URL url = new URL(fromURL);
	    is = url.openStream();
	}
	catch (Exception e) {
	    ; // FIXUP
	}
	if (is != null) {
	    FileOutputStream os = new FileOutputStream(toFile);
	    FileUtil.copy(is, os);
	}
    }

    private static FileObject createProjectDir (File dir) throws IOException {
        FileObject dirFO;
        if(!dir.exists()) {
            //Refresh before mkdir not to depend on window focus
            refreshFileSystem (dir);
            if (!dir.mkdirs()) {
                throw new IOException ("Can not create project folder."); // NOI18N
            }
            refreshFileSystem (dir);
        }        
        dirFO = FileUtil.toFileObject(dir);
        assert dirFO != null : "No such dir on disk: " + dir; // NOI18N
        assert dirFO.isFolder() : "Not really a dir: " + dir; // NOI18N
        return dirFO;
    }

    private static void createMainClass( String mainClassName, FileObject srcFolder ) throws IOException {
        
        int lastDotIdx = mainClassName.lastIndexOf( '.' );
        String mName, pName;
        if ( lastDotIdx == -1 ) {
            mName = mainClassName.trim();
            pName = null;
        }
        else {
            mName = mainClassName.substring( lastDotIdx + 1 ).trim();
            pName = mainClassName.substring( 0, lastDotIdx ).trim();
        }
        
        if ( mName.length() == 0 ) {
            return;
        }
        
        FileObject mainTemplate = Repository.getDefault().getDefaultFileSystem().findResource( "Templates/Classes/Main.java" ); // NOI18N

        if ( mainTemplate == null ) {
            return; // Don't know the template
        }
                
        DataObject mt = DataObject.find( mainTemplate );
        
        FileObject pkgFolder = srcFolder;
        if ( pName != null ) {
            String fName = pName.replace( '.', '/' ); // NOI18N
            pkgFolder = FileUtil.createFolder( srcFolder, fName );        
        }
        DataFolder pDf = DataFolder.findFolder( pkgFolder );        
        mt.createFromTemplate( pDf, mName );
        
    }


    private static void refreshFileSystem (final File dir) throws FileStateInvalidException {
        File rootF = dir;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject dirFO = FileUtil.toFileObject(rootF);
        assert dirFO != null : "At least disk roots must be mounted! " + rootF; // NOI18N
        dirFO.getFileSystem().refresh(false);
    }
}


