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

package org.netbeans.modules.java.j2seproject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.java.j2seproject.ui.customizer.J2SEProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Creates a J2SEProject from scratch according to some initial configuration.
 */
public class J2SEProjectGenerator {
    
    static final String MINIMUM_ANT_VERSION = "1.6.5";
    
    private J2SEProjectGenerator() {}
    
    /**
     * Create a new empty J2SE project.
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the name for the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(final File dir, final String name, final String mainClass, final String manifestFile) throws IOException {
        final FileObject dirFO = FileUtil.createFolder(dir);
        // if manifestFile is null => it's TYPE_LIB
        final AntProjectHelper[] h = new AntProjectHelper[1];
        dirFO.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                h[0] = createProject(dirFO, name, "src", "test", mainClass, manifestFile, manifestFile == null); //NOI18N
                Project p = ProjectManager.getDefault().findProject(dirFO);
                ProjectManager.getDefault().saveProject(p);
                FileObject srcFolder = dirFO.createFolder("src"); // NOI18N
                dirFO.createFolder("test"); // NOI18N
                if ( mainClass != null ) {
                    createMainClass( mainClass, srcFolder );
                }
            }
        });
        
        return h[0];
    }

    public static AntProjectHelper createProject(final File dir, final String name,
                                                  final File[] sourceFolders, final File[] testFolders, final String manifestFile) throws IOException {
        assert sourceFolders != null && testFolders != null: "Package roots can't be null";   //NOI18N
        final FileObject dirFO = FileUtil.createFolder(dir);
        final AntProjectHelper[] h = new AntProjectHelper[1];
        // this constructor creates only java application type
        dirFO.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            public void run () throws IOException {
                h[0] = createProject(dirFO, name, null, null, null, manifestFile, false);
                final J2SEProject p = (J2SEProject) ProjectManager.getDefault().findProject(dirFO);
                final ReferenceHelper refHelper = p.getReferenceHelper();
                try {
                    ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                    public Void run() throws Exception {
                        Element data = h[0].getPrimaryConfigurationData(true);
                        Document doc = data.getOwnerDocument();
                        NodeList nl = data.getElementsByTagNameNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");
                        assert nl.getLength() == 1;
                        Element sourceRoots = (Element) nl.item(0);
                        nl = data.getElementsByTagNameNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
                        assert nl.getLength() == 1;
                        Element testRoots = (Element) nl.item(0);
                        for (int i=0; i<sourceFolders.length; i++) {
                            String propName;
                            if (i == 0) {
                                //Name the first src root src.dir to be compatible with NB 4.0
                                propName = "src.dir";       //NOI18N
                            }
                            else {
                                String name = sourceFolders[i].getName();
                                propName = name + ".dir";    //NOI18N
                            }

                            int rootIndex = 1;
                            EditableProperties props = h[0].getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            while (props.containsKey(propName)) {
                                rootIndex++;
                                propName = name + rootIndex + ".dir";   //NOI18N
                            }
                            String srcReference = refHelper.createForeignFileReference(sourceFolders[i], JavaProjectConstants.SOURCES_TYPE_JAVA);
                            Element root = doc.createElementNS (J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                            root.setAttribute ("id",propName);   //NOI18N
                            sourceRoots.appendChild(root);
                            props = h[0].getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            props.put(propName,srcReference);
                            h[0].putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
                        }                 
                        for (int i = 0; i < testFolders.length; i++) {
                            if (!testFolders[i].exists()) {
                                testFolders[i].mkdirs();
                            }
                            String propName;
                            if (i == 0) {
                                //Name the first test root test.src.dir to be compatible with NB 4.0
                                propName = "test.src.dir";  //NOI18N
                            }
                            else {
                                String name = testFolders[i].getName();
                                propName = "test." + name + ".dir"; // NOI18N
                            }                    
                            int rootIndex = 1;
                            EditableProperties props = h[0].getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                            while (props.containsKey(propName)) {
                                rootIndex++;
                                propName = "test." + name + rootIndex + ".dir"; // NOI18N
                            }
                            String testReference = refHelper.createForeignFileReference(testFolders[i], JavaProjectConstants.SOURCES_TYPE_JAVA);
                            Element root = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root"); // NOI18N
                            root.setAttribute("id", propName); // NOI18N
                            testRoots.appendChild(root);
                            props = h[0].getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH); // #47609
                            props.put(propName, testReference);
                            h[0].putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                        }
                        h[0].putPrimaryConfigurationData(data,true);
                        ProjectManager.getDefault().saveProject (p);
                        return null;
                    }
                });
                } catch (MutexException me ) {
                    ErrorManager.getDefault().notify (me);
                }
            }
        });        
        return h[0];
    }

    private static AntProjectHelper createProject(FileObject dirFO, String name,
                                                  String srcRoot, String testRoot, String mainClass, String manifestFile, boolean isLibrary) throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, J2SEProjectType.TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        Element minant = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode(MINIMUM_ANT_VERSION)); // NOI18N
        data.appendChild(minant);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        Element sourceRoots = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
        if (srcRoot != null) {
            Element root = doc.createElementNS (J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute ("id","src.dir");   //NOI18N
            sourceRoots.appendChild(root);
            ep.setProperty("src.dir", srcRoot); // NOI18N
        }
        data.appendChild (sourceRoots);
        Element testRoots = doc.createElementNS(J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
        if (testRoot != null) {
            Element root = doc.createElementNS (J2SEProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute ("id","test.src.dir");   //NOI18N
            testRoots.appendChild (root);
            ep.setProperty("test.src.dir", testRoot); // NOI18N
        }
        data.appendChild (testRoots);
        h.putPrimaryConfigurationData(data, true);
        ep.setProperty("dist.dir", "dist"); // NOI18N
        ep.setComment("dist.dir", new String[] {"# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_dist.dir")}, false); // NOI18N
        ep.setProperty("dist.jar", "${dist.dir}/" + PropertyUtils.getUsablePropertyName(name) + ".jar"); // NOI18N
        ep.setProperty("javac.classpath", new String[0]); // NOI18N
        ep.setProperty("build.sysclasspath", "ignore"); // NOI18N
        ep.setComment("build.sysclasspath", new String[] {"# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_build.sysclasspath")}, false); // NOI18N
        ep.setProperty("run.classpath", new String[] { // NOI18N
            "${javac.classpath}:", // NOI18N
            "${build.classes.dir}", // NOI18N
        });
        ep.setProperty("debug.classpath", new String[] { // NOI18N
            "${run.classpath}", // NOI18N
        });        
        ep.setProperty("jar.compress", "false"); // NOI18N
        if (!isLibrary) {
            ep.setProperty("main.class", mainClass == null ? "" : mainClass); // NOI18N
        }
        
        ep.setProperty("javac.compilerargs", ""); // NOI18N
        ep.setComment("javac.compilerargs", new String[] {
            "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_javac.compilerargs"), // NOI18N
        }, false);
        SpecificationVersion sourceLevel = getDefaultSourceLevel();
        ep.setProperty("javac.source", sourceLevel.toString()); // NOI18N
        ep.setProperty("javac.target", sourceLevel.toString()); // NOI18N
        ep.setProperty("javac.deprecation", "false"); // NOI18N
        ep.setProperty("javac.test.classpath", new String[] { // NOI18N
            "${javac.classpath}:", // NOI18N
            "${build.classes.dir}:", // NOI18N
            "${libs.junit.classpath}:", // NOI18N
            "${libs.junit_4.classpath}",  //NOI18N
        });
        ep.setProperty("run.test.classpath", new String[] { // NOI18N
            "${javac.test.classpath}:", // NOI18N
            "${build.test.classes.dir}", // NOI18N
        });
        ep.setProperty("debug.test.classpath", new String[] { // NOI18N
            "${run.test.classpath}", // NOI18N
        });

        ep.setProperty("build.generated.dir", "${build.dir}/generated"); // NOI18N
        ep.setProperty("meta.inf.dir", "${src.dir}/META-INF"); // NOI18N
        
        ep.setProperty("build.dir", "build"); // NOI18N
        ep.setComment("build.dir", new String[] {"# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_build.dir")}, false); // NOI18N
        ep.setProperty("build.classes.dir", "${build.dir}/classes"); // NOI18N
        ep.setProperty("build.test.classes.dir", "${build.dir}/test/classes"); // NOI18N
        ep.setProperty("build.test.results.dir", "${build.dir}/test/results"); // NOI18N
        ep.setProperty("build.classes.excludes", "**/*.java,**/*.form"); // NOI18N
        ep.setProperty("dist.javadoc.dir", "${dist.dir}/javadoc"); // NOI18N
        ep.setProperty("platform.active", "default_platform"); // NOI18N

        ep.setProperty("run.jvmargs", ""); // NOI18N
        ep.setComment("run.jvmargs", new String[] {
            "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_run.jvmargs"), // NOI18N
            "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_run.jvmargs_2"), // NOI18N
            "# " + NbBundle.getMessage(J2SEProjectGenerator.class, "COMMENT_run.jvmargs_3"), // NOI18N
        }, false);

        ep.setProperty(J2SEProjectProperties.JAVADOC_PRIVATE, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_NO_TREE, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_USE, "true"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_NO_NAVBAR, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_NO_INDEX, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_SPLIT_INDEX, "true"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_AUTHOR, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_VERSION, "false"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_WINDOW_TITLE, ""); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_ENCODING, "${"+J2SEProjectProperties.SOURCE_ENCODING+"}"); // NOI18N
        ep.setProperty(J2SEProjectProperties.JAVADOC_ADDITIONALPARAM, ""); // NOI18N
        Charset enc = FileEncodingQuery.getDefaultEncoding();
        ep.setProperty(J2SEProjectProperties.SOURCE_ENCODING, enc.name());
        if (manifestFile != null) {
            ep.setProperty("manifest.file", manifestFile); // NOI18N
        }
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);        
        return h;
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
    
    //------------ Used by unit tests -------------------
    private static SpecificationVersion defaultSourceLevel;
    
    private static SpecificationVersion getDefaultSourceLevel () {
        if (defaultSourceLevel != null) {
            return defaultSourceLevel;
        }
        else {
            JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
            SpecificationVersion v = defaultPlatform.getSpecification().getVersion();
            if (v.equals(new SpecificationVersion("1.6")) || v.equals(new SpecificationVersion("1.7"))) {
                // #89131: these levels are not actually distinct from 1.5.
                return new SpecificationVersion("1.5");
            } else {
                return v;
            }
        }
    }
    
    /**
     * Unit test only method. Sets the default source level for tests
     * where the default platform is not available.
     * @param version the default source level set to project when it is created
     *
     */
    public static void setDefaultSourceLevel (SpecificationVersion version) {
        defaultSourceLevel = version;
    }
}


