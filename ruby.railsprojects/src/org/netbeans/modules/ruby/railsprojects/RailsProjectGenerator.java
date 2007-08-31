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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby.railsprojects;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Stack;
import javax.swing.text.BadLocationException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.ruby.railsprojects.ui.customizer.RailsProjectProperties;
import org.netbeans.modules.ruby.rubyproject.api.RubyExecution;
import org.netbeans.modules.ruby.rubyproject.execution.ExecutionDescriptor;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.NbUtilities;
import org.netbeans.modules.ruby.rubyproject.RakeTargetsAction;
import org.netbeans.modules.ruby.rubyproject.execution.DirectoryFileLocator;
import org.netbeans.modules.ruby.rubyproject.execution.ExecutionService;
import org.netbeans.modules.ruby.rubyproject.execution.RegexpOutputRecognizer;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.EditableProperties;
import org.netbeans.modules.ruby.spi.project.support.rake.ProjectGenerator;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates a RailsProject from scratch according to some initial configuration.
 * @todo Take the "README" file in the Rails project and run it through rdoc and
 *   display in internal HTML viewer?
 */
public class RailsProjectGenerator {
    public static final RegexpOutputRecognizer RAILS_GENERATOR =
        new RegexpOutputRecognizer("^   (   create|    force|identical|     skip)\\s+([\\w|/]+\\.(rb|mab|rjs|rxml|rake|erb|builder|rhtml|yml|js|html|cgi|fcgi|txt|png|gif|css))\\s*$", // NOI18N
            2, -1, -1);

    private RailsProjectGenerator() {}
    
    /**
     * Create a new empty Rails project.
     * 
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the name for the project
     * @param create whether to generate base directory structure or not (use
     *        false for existing application)
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static RakeProjectHelper createProject(File dir, String name, boolean create, 
            String database, boolean jdbc, boolean deploy) throws IOException {
        FileObject dirFO = createProjectDir (dir);
        
        // Run Rails to generate the appliation skeleton
        if (create) {
            boolean runThroughRuby = RubyInstallation.getInstance().getVersion("rails") != null; // NOI18N
            ExecutionDescriptor desc = null;
            String displayName = NbBundle.getMessage(RailsProjectGenerator.class, "GenerateRails");

            File pwd = dir.getParentFile();
            if (runThroughRuby) {
                desc = new ExecutionDescriptor(displayName, pwd,
                    RubyInstallation.getInstance().getRails());
                if (database != null) {
                    desc.additionalArgs(name, "--database=" + database);
                } else {
                    desc.additionalArgs(name);
                }
            } else {
                desc = new ExecutionDescriptor(displayName, pwd, name);
                if (database != null) {
                    desc.additionalArgs("--database=" + database);
                }
                desc.cmd(new File(RubyInstallation.getInstance().getRails()));
            }
            desc.fileLocator(new DirectoryFileLocator(dirFO));
            desc.addOutputRecognizer(RAILS_GENERATOR);
            ExecutionService service = null;
            if (runThroughRuby) {
                service = new RubyExecution(desc);
            } else {
                // Try invoking the Rails script directly (probably a Linux distribution
                // with railties installed and rails is a Unix shell script rather 
                // than a Ruby program)
                service = new ExecutionService(desc);
            }
            Task task = service.run();
            task.waitFinished();
            
            // Precreate a spec directory if it doesn't exist such that my source root will work
            if (RubyInstallation.getInstance().getVersion("rspec") != null) { // NOI18N
                File spec = new File(dir, "spec"); // NOI18N
                if (!spec.exists()) {
                    spec.mkdirs();
                }
            }
            
            dirFO.getFileSystem().refresh(true);

            // TODO - only do this if not creating from existing app?
            if (jdbc) {
                insertActiveJdbcHook(dirFO);
            }
        }

        RakeProjectHelper h = createProject(dirFO, name/*, "app", "test", mainClass, manifestFile, false*/); //NOI18N
        Project p = ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        
        // Start the server? No, disabled for now; this clobbers the generate-project
        // window with useful info, and besides, users may want to configure the port number
        // first, etc.
        // 
        //RailsServer server = p.getLookup().lookup(RailsServer.class);
        //if (server != null) {
        //    server.ensureRunning();
        //}
        
        // Install goldspike if the user wants Rails deployment
        if (deploy) {
            InstalledFileLocator locator = InstalledFileLocator.getDefault();
            File goldspikeFile = locator.locate("goldspike.zip", "org.netbeans.modules.ruby.railsprojects", false);
            if (goldspikeFile != null) {
                FileObject fo = FileUtil.toFileObject(goldspikeFile);
                if (fo != null) {
                    NbUtilities.extractZip(fo, p.getProjectDirectory());
                }
            }
        }

        // Run Rake -T silently to determine the available targets and write into private area
        RakeTargetsAction.refreshTargets(p);
        
        return h;
    }
    
    private static void insertActiveJdbcHook(FileObject dir) {
        FileObject fo = dir.getFileObject("config/environment.rb"); // NOI18N
        if (fo != null) {
            try {
                DataObject dobj = DataObject.find(fo);
                EditorCookie ec = dobj.getCookie(EditorCookie.class);
                if (ec != null) {
                    javax.swing.text.Document doc = ec.openDocument();
                    String text = doc.getText(0, doc.getLength());
                    int offset = text.indexOf("jdbc"); // NOI18N
                    if (offset != -1) {
                        // This rails version already handles JDBC somehow
                        return;
                    }
                    offset = text.indexOf("Rails::Initializer.run do |config|"); // NOI18N
                    if (offset != -1) {
                        String insert =
                            "# Inserted by NetBeans Ruby support to support JRuby\n" +
                            "if RUBY_PLATFORM =~ /java/\n" + // NOI18N
                            "  require 'rubygems'\n" + // NOI18N
                            "  gem 'ActiveRecord-JDBC'\n" + // NOI18N
                            "  require 'jdbc_adapter'\n" + // NOI18N
                            "end\n\n"; // NOI18N
                        doc.insertString(offset, insert, null);
                        SaveCookie sc = dobj.getCookie(SaveCookie.class);
                        if (sc != null) {
                            sc.save();
                        }
                    }
                }
            } catch (BadLocationException ble) {
                Exceptions.printStackTrace(ble);
            } catch (DataObjectNotFoundException dnfe) {
                Exceptions.printStackTrace(dnfe);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }
    

//    public static RakeProjectHelper createProject(final File dir, final String name,
//                                                  final File[] sourceFolders, final File[] testFolders, final String manifestFile) throws IOException {
//        assert sourceFolders != null && testFolders != null: "Package roots can't be null";   //NOI18N
//        final FileObject dirFO = createProjectDir (dir);
//        // this constructor creates only java application type
//        final RakeProjectHelper h = createProject(dirFO, name, null, null, null, manifestFile, false);
//        final RailsProject p = (RailsProject) ProjectManager.getDefault().findProject(dirFO);
//        final ReferenceHelper refHelper = p.getReferenceHelper();
//        try {
//        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
//            public Void run() throws Exception {
//                Element data = h.getPrimaryConfigurationData(true);
//                Document doc = data.getOwnerDocument();
//                NodeList nl = data.getElementsByTagNameNS(RailsProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");
//                assert nl.getLength() == 1;
//                Element sourceRoots = (Element) nl.item(0);
//                nl = data.getElementsByTagNameNS(RailsProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
//                assert nl.getLength() == 1;
//                Element testRoots = (Element) nl.item(0);
//                for (int i=0; i<sourceFolders.length; i++) {
//                    String propName;
//                    if (i == 0) {
//                        //Name the first src root src.dir to be compatible with NB 4.0
//                        propName = "src.dir";       //NOI18N
//                    }
//                    else {
//                        String name = sourceFolders[i].getName();
//                        propName = name + ".dir";    //NOI18N
//                    }
//                    
//                    int rootIndex = 1;
//                    EditableProperties props = h.getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH);
//                    while (props.containsKey(propName)) {
//                        rootIndex++;
//                        propName = name + rootIndex + ".dir";   //NOI18N
//                    }
//                    String srcReference = refHelper.createForeignFileReference(sourceFolders[i], RailsProject.SOURCES_TYPE_RUBY);
//                    Element root = doc.createElementNS (RailsProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
//                    root.setAttribute ("id",propName);   //NOI18N
//                    sourceRoots.appendChild(root);
//                    props = h.getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH);
//                    props.put(propName,srcReference);
//                    h.putProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
//                }                 
//                for (int i = 0; i < testFolders.length; i++) {
//                    if (!testFolders[i].exists()) {
//                        testFolders[i].mkdirs();
//                    }
//                    String propName;
//                    if (i == 0) {
//                        //Name the first test root test.src.dir to be compatible with NB 4.0
//                        propName = "test.src.dir";  //NOI18N
//                    }
//                    else {
//                        String name = testFolders[i].getName();
//                        propName = "test." + name + ".dir"; // NOI18N
//                    }                    
//                    int rootIndex = 1;
//                    EditableProperties props = h.getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH);
//                    while (props.containsKey(propName)) {
//                        rootIndex++;
//                        propName = "test." + name + rootIndex + ".dir"; // NOI18N
//                    }
//                    String testReference = refHelper.createForeignFileReference(testFolders[i], RailsProject.SOURCES_TYPE_RUBY);
//                    Element root = doc.createElementNS(RailsProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root"); // NOI18N
//                    root.setAttribute("id", propName); // NOI18N
//                    testRoots.appendChild(root);
//                    props = h.getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH); // #47609
//                    props.put(propName, testReference);
//                    h.putProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH, props);
//                }
//                h.putPrimaryConfigurationData(data,true);
//                ProjectManager.getDefault().saveProject (p);
//                return null;
//            }
//        });
//        } catch (MutexException me ) {
//            ErrorManager.getDefault().notify (me);
//        }
//        return h;
//    }

    private static RakeProjectHelper createProject(FileObject dirFO, String name/*,
                                                  String srcRoot, String testRoot, String mainClass, String manifestFile, boolean isLibrary*/) throws IOException {
        RakeProjectHelper h = ProjectGenerator.createProject(dirFO, RailsProjectType.TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(RailsProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
//        Element minant = doc.createElementNS(RailsProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
//        minant.appendChild(doc.createTextNode(MINIMUM_ANT_VERSION)); // NOI18N
//        data.appendChild(minant);
        EditableProperties ep = h.getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH);
        
        
        ep.setProperty(RailsProjectProperties.RAILS_PORT, "3000"); // NOI18N

        Charset enc = FileEncodingQuery.getDefaultEncoding();
        ep.setProperty(RailsProjectProperties.SOURCE_ENCODING, enc.name());
        
//        Element sourceRoots = doc.createElementNS(RailsProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
//        if (srcRoot != null) {
//            Element root = doc.createElementNS (RailsProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
//            root.setAttribute ("id","src.dir");   //NOI18N
//            sourceRoots.appendChild(root);
//            ep.setProperty("src.dir", srcRoot); // NOI18N
//        }
//        data.appendChild (sourceRoots);
//        Element testRoots = doc.createElementNS(RailsProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
//        if (testRoot != null) {
//            Element root = doc.createElementNS (RailsProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
//            root.setAttribute ("id","test.src.dir");   //NOI18N
//            testRoots.appendChild (root);
//            ep.setProperty("test.src.dir", testRoot); // NOI18N
//        }
//        data.appendChild (testRoots);
        h.putPrimaryConfigurationData(data, true);
//        ep.setProperty("dist.dir", "dist"); // NOI18N
//        ep.setComment("dist.dir", new String[] {"# " + NbBundle.getMessage(RailsProjectGenerator.class, "COMMENT_dist.dir")}, false); // NOI18N
//        ep.setProperty("dist.jar", "${dist.dir}/" + PropertyUtils.getUsablePropertyName(name) + ".jar"); // NOI18N
//        ep.setProperty("javac.classpath", new String[0]); // NOI18N
//        ep.setProperty("build.sysclasspath", "ignore"); // NOI18N
//        ep.setComment("build.sysclasspath", new String[] {"# " + NbBundle.getMessage(RailsProjectGenerator.class, "COMMENT_build.sysclasspath")}, false); // NOI18N
//        ep.setProperty("run.classpath", new String[] { // NOI18N
//            "${javac.classpath}:", // NOI18N
//            "${build.classes.dir}", // NOI18N
//        });
//        ep.setProperty("debug.classpath", new String[] { // NOI18N
//            "${run.classpath}", // NOI18N
//        });        
//        ep.setProperty("jar.compress", "false"); // NOI18N
//        if (!isLibrary) {
//            ep.setProperty(RailsProjectProperties.MAIN_CLASS, mainClass == null ? "" : mainClass); // NOI18N
//        }
        
//        ep.setProperty("javac.compilerargs", ""); // NOI18N
//        ep.setComment("javac.compilerargs", new String[] {
//            "# " + NbBundle.getMessage(RailsProjectGenerator.class, "COMMENT_javac.compilerargs"), // NOI18N
//        }, false);
//        SpecificationVersion sourceLevel = getDefaultSourceLevel();
//        ep.setProperty("javac.source", sourceLevel.toString()); // NOI18N
//        ep.setProperty("javac.target", sourceLevel.toString()); // NOI18N
//        ep.setProperty("javac.deprecation", "false"); // NOI18N
//        ep.setProperty("javac.test.classpath", new String[] { // NOI18N
//            "${javac.classpath}:", // NOI18N
//            "${build.classes.dir}:", // NOI18N
//            "${libs.junit.classpath}", // NOI18N
//        });
//        ep.setProperty("run.test.classpath", new String[] { // NOI18N
//            "${javac.test.classpath}:", // NOI18N
//            "${build.test.classes.dir}", // NOI18N
//        });
//        ep.setProperty("debug.test.classpath", new String[] { // NOI18N
//            "${run.test.classpath}", // NOI18N
//        });
//
//        ep.setProperty("build.generated.dir", "${build.dir}/generated"); // NOI18N
//        ep.setProperty("meta.inf.dir", "${src.dir}/META-INF"); // NOI18N
//        
//        ep.setProperty("build.dir", "build"); // NOI18N
//        ep.setComment("build.dir", new String[] {"# " + NbBundle.getMessage(RailsProjectGenerator.class, "COMMENT_build.dir")}, false); // NOI18N
//        ep.setProperty("build.classes.dir", "${build.dir}/classes"); // NOI18N
//        ep.setProperty("build.test.classes.dir", "${build.dir}/test/classes"); // NOI18N
//        ep.setProperty("build.test.results.dir", "${build.dir}/test/results"); // NOI18N
//        ep.setProperty("build.classes.excludes", "**/*.java,**/*.form"); // NOI18N
//        ep.setProperty("dist.javadoc.dir", "${dist.dir}/javadoc"); // NOI18N
//        ep.setProperty("platform.active", "default_platform"); // NOI18N
//
//        ep.setProperty("run.jvmargs", ""); // NOI18N
//        ep.setComment("run.jvmargs", new String[] {
//            "# " + NbBundle.getMessage(RailsProjectGenerator.class, "COMMENT_run.jvmargs"), // NOI18N
//            "# " + NbBundle.getMessage(RailsProjectGenerator.class, "COMMENT_run.jvmargs_2"), // NOI18N
//            "# " + NbBundle.getMessage(RailsProjectGenerator.class, "COMMENT_run.jvmargs_3"), // NOI18N
//        }, false);

//        ep.setProperty(RailsProjectProperties.JAVADOC_PRIVATE, "false"); // NOI18N
//        ep.setProperty(RailsProjectProperties.JAVADOC_NO_TREE, "false"); // NOI18N
//        ep.setProperty(RailsProjectProperties.JAVADOC_USE, "true"); // NOI18N
//        ep.setProperty(RailsProjectProperties.JAVADOC_NO_NAVBAR, "false"); // NOI18N
//        ep.setProperty(RailsProjectProperties.JAVADOC_NO_INDEX, "false"); // NOI18N
//        ep.setProperty(RailsProjectProperties.JAVADOC_SPLIT_INDEX, "true"); // NOI18N
//        ep.setProperty(RailsProjectProperties.JAVADOC_AUTHOR, "false"); // NOI18N
//        ep.setProperty(RailsProjectProperties.JAVADOC_VERSION, "false"); // NOI18N
//        ep.setProperty(RailsProjectProperties.JAVADOC_WINDOW_TITLE, ""); // NOI18N
//        ep.setProperty(RailsProjectProperties.JAVADOC_ENCODING, ""); // NOI18N
//        ep.setProperty(RailsProjectProperties.JAVADOC_ADDITIONALPARAM, ""); // NOI18N
        
//        if (manifestFile != null) {
//            ep.setProperty("manifest.file", manifestFile); // NOI18N
//        }
        h.putProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH, ep);        
        return h;
    }

    private static FileObject createProjectDir (File dir) throws IOException {
        Stack<String> stack = new Stack<String>();
        while (!dir.exists()) {
            stack.push (dir.getName());
            dir = dir.getParentFile();
        }        
        FileObject dirFO = FileUtil.toFileObject (dir);
        if (dirFO == null) {
            refreshFileSystem(dir);
            dirFO = FileUtil.toFileObject (dir);
        }
        assert dirFO != null;
        while (!stack.isEmpty()) {
            dirFO = dirFO.createFolder(stack.pop());
        }        
        return dirFO;
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
    

    private static void createMainClass( String mainClassName, FileObject srcFolder, String templateName ) throws IOException {
        
        int lastDotIdx = mainClassName.lastIndexOf( '/' );
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
        
        FileObject mainTemplate = Repository.getDefault().getDefaultFileSystem().findResource( templateName );

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
        // BEGIN SEMPLICE MODIFICATIONS
        int extension = mName.lastIndexOf('.');
        if (extension != -1) {
            mName = mName.substring(0, extension);
        }
        // END SEMPLICE MODIFICATIONS
        mt.createFromTemplate( pDf, mName );
        
    }
}


