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

package org.netbeans.modules.java.j2seproject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Create a fresh J2SEProject from scratch.
 * Currently does not permit much to be specified - feel free to add more parameters
 * as needed.
 * @author Jesse Glick
 */
public class J2SEProjectGenerator {
    
    private J2SEProjectGenerator() {}

    /**
     * Create a new empty J2SE project.
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param codename the code name for the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(File dir, String codename, String displayName, String mainClass ) throws IOException {
        dir.mkdirs();
        // XXX clumsy way to refresh, but otherwise it doesn't work for new folders
        File rootF = dir;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject[] fo = FileUtil.fromFile(rootF);
        assert fo.length > 0 : "At least disk roots must be mounted! " + rootF;
        fo[0].getFileSystem().refresh(false);
        fo = FileUtil.fromFile(dir);
        assert fo.length > 0 : "No such dir on disk: " + dir;
        FileObject dirFO = fo[0];
        assert dirFO.isFolder() : "Not really a dir: " + dir;
        assert dirFO.getChildren().length == 0 : "Dir must have been empty: " + dir;
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, J2SEProjectType.TYPE, codename);
        h.setDisplayName( displayName == null ? codename : displayName ); // for now
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element minant = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode("1.6")); // NOI18N
        data.appendChild(minant);
        h.putPrimaryConfigurationData(data, true);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        // XXX the following just for testing, TBD:
        ep.setProperty("dist.dir", "dist");
        ep.setProperty("dist.jar", "${dist.dir}/" + codename + ".jar");
        ep.setProperty("javac.classpath", Arrays.asList(new String[]{""}));
        ep.setProperty("run.classpath", Arrays.asList(new String[]{"${javac.classpath}","${build.classes.dir}"}));
        ep.setProperty("debug.classpath", Arrays.asList(new String[]{"${run.classpath}"}));
        ep.setProperty("application.args", "");
        ep.setProperty("jar.compress", "false");
        ep.setProperty("main.class", mainClass == null ? "" : mainClass );
        ep.setProperty("javac.source", "1.4");
        ep.setProperty("javac.debug", "true");
        ep.setProperty("javac.deprecation", "false");
        ep.setProperty("javac.test.classpath", Arrays.asList(new String[]{"${javac.classpath}","${build.classes.dir}","${libs.junit.classpath}"}));
        ep.setProperty("run.test.classpath", Arrays.asList(new String[]{"${javac.test.classpath}","${build.test.classes.dir}"}));
        ep.setProperty("debug.test.classpath", Arrays.asList(new String[]{"${run.test.classpath}"}));
        ep.setProperty("src.dir", "src");
        ep.setProperty("test.src.dir", "test");
        ep.setProperty("build.dir", "build");
        ep.setProperty("build.classes.dir", "${build.dir}/classes");
        ep.setProperty("build.test.classes.dir", "${build.dir}/test/classes");
        ep.setProperty("build.test.results.dir", "${build.dir}/test/results");
        ep.setProperty("build.classes.excludes", "**/*.java,**/*.form");
        ep.setProperty("dist.javadoc.dir", "${dist.dir}/javadoc");
        ep.setProperty("no.dependencies", "false");
        ep.setProperty("platform.active", "default_platform");
        
        ep.setProperty(J2SEProjectProperties.JAVADOC_PRIVATE, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_NO_TREE, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_USE, "true"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_NO_NAVBAR, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_NO_INDEX, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_SPLIT_INDEX, "true"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_AUTHOR, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_VERSION, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_WINDOW_TITLE, ""); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_ENCODING, ""); // NOI18N
                
        
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty("application.args", "");
        ep.setProperty(J2SEProjectProperties.JAVADOC_PREVIEW, "true"); // NOI18N
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        Project p = ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        FileObject srcFolder = dirFO.createFolder("src"); // NOI18N
        dirFO.createFolder("test"); // NOI18N
        if ( mainClass != null ) {
            createMainClass( mainClass, srcFolder );
        }
        return h;
    }
    
    private static void createMainClass( String mainClassName, FileObject srcFolder ) throws IOException {
        
        int lastDotIdx = mainClassName.lastIndexOf( '.' );
        String mName = mainClassName.substring( lastDotIdx + 1 ).trim();
        String pName = mainClassName.substring( 0, lastDotIdx ).trim();
        
        if ( mName.length() == 0 || pName.length() == 0 ) {            
            return;
        }
        
        FileObject mainTemplate = Repository.getDefault().getDefaultFileSystem().findResource( "Templates/Classes/Main.java" );

        if ( mainTemplate == null ) {
            return; // Don't know the template
        }
                
        DataObject mt = DataObject.find( mainTemplate );
        
        String fName = pName.replace( '.', '/' ); // NOI18N
        FileObject pkgFolder = FileUtil.createFolder( srcFolder, fName );        
        DataFolder pDf = DataFolder.findFolder( pkgFolder );        
        mt.createFromTemplate( pDf, mName );
        
    }
    
}
