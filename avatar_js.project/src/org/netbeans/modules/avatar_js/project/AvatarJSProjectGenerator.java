/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.avatar_js.project;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.avatar_js.project.ui.wizards.NewAvatarJSServerFileWizardIterator;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Martin
 */
public class AvatarJSProjectGenerator {
    
    private static final Logger LOG = Logger.getLogger(AvatarJSProjectGenerator.class.getName());

    private static final String loggerName = "org.netbeans.ui.metrics.avatar_js"; // NOI18N
    private static final String loggerKey = "USG_PROJECT_CREATE_AVATARJS"; // NOI18N
    
    private static final String DEFAULT_PLATFORM_ID = "default_platform";   //NOI18N
    private static final String PROP_PLATFORM_ANT_NAME = "platform.ant.name";    //NOI18N
    
    private static final String JAVA_LIBRARY_PATH = "-Djava.library.path="; // NOI18N
    
    private static final String MAIN_SERVER_FILE_TEMPLATE = "Templates/Avatar_js/ServerFile.js";  //NOI18N
    
    private static final String PACKAGE_JSON_NAME = "package.json";     // NOI18N
    private static final String PACKAGE_JSON_TEMPLATE = "Templates/Avatar_js/package.json";     // NOI18N
    
    private AvatarJSProjectGenerator() {}
    
    public static AntProjectHelper createProject(final File prjDir,
                                                 final String name,
                                                 final String mainFile,
                                                 final String port,
                                                 @NonNull final JavaPlatform platform,
                                                 final String avatarLibsFolder,
                                                 final File avatar_jsJAR) throws IOException {
        final String platformAntName = platform.getProperties().get(PROP_PLATFORM_ANT_NAME);
        if (platformAntName == null) {
            throw new IllegalArgumentException("Invalid platform, the platform has no platform.ant.name");  //NOI18N
        }
        final SpecificationVersion sourceLevel = platform.getSpecification().getVersion();
        final FileObject dirFO = FileUtil.createFolder(prjDir);
        final AntProjectHelper[] h = new AntProjectHelper[1];
        final String jvmArgs = (avatarLibsFolder != null) ? JAVA_LIBRARY_PATH+avatarLibsFolder : "";
        dirFO.getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                h[0] = createProject(
                        dirFO,
                        name,
                        sourceLevel,
                        null,
                        null,
                        mainFile,
                        null,
                        jvmArgs,
                        new String[]{},
                        new String[] {},
                        avatar_jsJAR,
                        platformAntName
                       );
                AvatarJSProject ap = (AvatarJSProject) ProjectManager.getDefault().findProject(dirFO);
                ProjectManager.getDefault().saveProject(ap);
                //final ReferenceHelper refHelper = ap.getReferenceHelper();
                FileObject srcDir = dirFO.getFileObject(AvatarJSProject.CONFIG_JS_SOURCE_PATH);
                if (srcDir == null) {
                    srcDir = FileUtil.createFolder(dirFO, AvatarJSProject.CONFIG_JS_SOURCE_PATH);
                }
                DataObject mainFileDO = null;
                if (mainFile != null) {
                    mainFileDO = createMainFile(mainFile, port, srcDir, null);
                }
                createPackageJSON(srcDir, h[0], name, mainFileDO);
            }

        });
        return h[0];
    }
    
    private static AntProjectHelper createProject(FileObject dirFO,
                                                  String name,
                                                  SpecificationVersion sourceLevel,
                                                  String srcRoot,
                                                  String distFolder,
                                                  String mainFile,
                                                  String librariesDefinition,
                                                  String jvmArgs,
                                                  String[] compileClassPath,
                                                  String[] runtimeClassPath,
                                                  File avatar_jsJAR,
                                                  @NonNull final String platformId) throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, AvatarJSProject.TYPE, librariesDefinition);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(AvatarJSProject.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        if (!DEFAULT_PLATFORM_ID.equals(platformId)) {
            final Element platformEl = doc.createElementNS(AvatarJSProject.PROJECT_CONFIGURATION_NAMESPACE, "explicit-platform");   //NOI18N
            platformEl.setAttribute("explicit-source-supported", "true"); //NOI18N
            data.appendChild(platformEl);
        }
        Element sourceRoots = doc.createElementNS(AvatarJSProject.PROJECT_CONFIGURATION_NAMESPACE, "source-roots");  //NOI18N
        if (srcRoot != null) {
            Element root = doc.createElementNS (AvatarJSProject.PROJECT_CONFIGURATION_NAMESPACE, "root");   //NOI18N
            root.setAttribute ("id","src.dir");   //NOI18N
            sourceRoots.appendChild(root);
            ep.setProperty("src.dir", srcRoot); // NOI18N
        }
        data.appendChild (sourceRoots);
        File jsSrc = new File(FileUtil.toFile(dirFO), AvatarJSProject.CONFIG_JS_SOURCE_PATH);
        ep.setProperty("src.js.dir", jsSrc.getAbsolutePath());
        File javaSrc = new File(FileUtil.toFile(dirFO), AvatarJSProject.CONFIG_JAVA_SOURCE_PATH);
        ep.setProperty("src.java.dir", javaSrc.getAbsolutePath());
        //data.appendChild (testRoots);
        h.putPrimaryConfigurationData(data, true);
        
        {
            String avatarJSJARRef = "${file.reference."+avatar_jsJAR.getName()+"}";     // NOI18N
            String[] runtimeClassPath2 = new String[runtimeClassPath.length + 1];
            System.arraycopy(runtimeClassPath, 0, runtimeClassPath2, 0, runtimeClassPath.length);
            runtimeClassPath2[runtimeClassPath.length] = avatarJSJARRef;
            runtimeClassPath = runtimeClassPath2;
        }
        
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_ENABLED, "true"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR, "false"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, "true"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ""); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_SOURCE_OUTPUT, "${build.generated.sources.dir}/ap-source-output"); // NOI18N
        ep.setProperty(ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS, ""); // NOI18N
        ep.setProperty("dist.dir", distFolder != null ? distFolder : "dist"); // NOI18N
        ep.setComment("dist.dir", new String[] {"# " + NbBundle.getMessage(AvatarJSProjectGenerator.class, "COMMENT_dist.dir")}, false); // NOI18N
        ep.setProperty("dist.jar", "${dist.dir}/" + PropertyUtils.getUsablePropertyName(name) + ".jar"); // NOI18N
        ep.setProperty("javac.classpath", compileClassPath); // NOI18N
        ep.setProperty(ProjectProperties.JAVAC_PROCESSORPATH, new String[] {"${javac.classpath}"}); // NOI18N
        ep.setProperty("javac.test.processorpath", new String[] {"${javac.test.classpath}"}); // NOI18N
        ep.setProperty("build.sysclasspath", "ignore"); // NOI18N
        ep.setComment("build.sysclasspath", new String[] {"# " + NbBundle.getMessage(AvatarJSProjectGenerator.class, "COMMENT_build.sysclasspath")}, false); // NOI18N
        ep.setProperty("run.classpath", runtimeClassPath);
        ep.setProperty("debug.classpath", new String[] { // NOI18N
            "${run.classpath}", // NOI18N
        });
        ep.setComment("debug.classpath", new String[] { // NOI18N
            "# " + NbBundle.getMessage(AvatarJSProjectGenerator.class, "COMMENT_debug.transport"),
            "#debug.transport=dt_socket"
        }, false);
        ep.setProperty("avatar.js.jar", avatar_jsJAR.getAbsolutePath());
        ep.setProperty("file.reference."+avatar_jsJAR.getName(), avatar_jsJAR.getAbsolutePath());
        if (mainFile != null) {
            int l = mainFile.length();
            if (l < 3 || !mainFile.substring(l - 3, l).equalsIgnoreCase(AvatarJSProject.JS_FILE_EXT)) {
                mainFile += AvatarJSProject.JS_FILE_EXT;
            }
            ep.setProperty("main.file", mainFile); // NOI18N
        } else /*if (!isLibrary)*/ {
            ep.setProperty("main.file", ""); // NOI18N
        }
        ep.setProperty("javac.compilerargs", ""); // NOI18N
        ep.setComment("javac.compilerargs", new String[] {
            "# " + NbBundle.getMessage(AvatarJSProjectGenerator.class, "COMMENT_javac.compilerargs"), // NOI18N
        }, false);
        ep.setProperty("javac.source", sourceLevel.toString()); // NOI18N
        ep.setProperty("javac.target", sourceLevel.toString()); // NOI18N
        ep.setProperty("javac.deprecation", "false"); // NOI18N
        
        ep.setProperty("build.dir", "build"); // NOI18N
        ep.setComment("build.dir", new String[] {"# " + NbBundle.getMessage(AvatarJSProjectGenerator.class, "COMMENT_build.dir")}, false); // NOI18N
        ep.setProperty("build.classes.dir", "${build.dir}/classes"); // NOI18N
        ep.setProperty("build.test.classes.dir", "${build.dir}/test/classes"); // NOI18N
        ep.setProperty("build.test.results.dir", "${build.dir}/test/results"); // NOI18N
        ep.setProperty("build.classes.excludes", "**/*.java,**/*.form"); // NOI18N
        ep.setProperty("dist.javadoc.dir", "${dist.dir}/javadoc"); // NOI18N
        ep.setProperty("platform.active", platformId); // NOI18N

        ep.setProperty(ProjectProperties.RUN_JVM_ARGS, jvmArgs); // NOI18N
        ep.setComment(ProjectProperties.RUN_JVM_ARGS, new String[] {
            "# " + NbBundle.getMessage(AvatarJSProjectGenerator.class, "COMMENT_run.jvmargs"), // NOI18N
            "# " + NbBundle.getMessage(AvatarJSProjectGenerator.class, "COMMENT_run.jvmargs_2"), // NOI18N
            "# " + NbBundle.getMessage(AvatarJSProjectGenerator.class, "COMMENT_run.jvmargs_3"), // NOI18N
        }, false);

        ep.setProperty(ProjectProperties.JAVADOC_PRIVATE, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_NO_TREE, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_USE, "true"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_NO_NAVBAR, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_NO_INDEX, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_SPLIT_INDEX, "true"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_AUTHOR, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_VERSION, "false"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_WINDOW_TITLE, ""); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_ENCODING, "${"+ProjectProperties.SOURCE_ENCODING+"}"); // NOI18N
        ep.setProperty(ProjectProperties.JAVADOC_ADDITIONALPARAM, ""); // NOI18N
        Charset enc = FileEncodingQuery.getDefaultEncoding();
        ep.setProperty(ProjectProperties.SOURCE_ENCODING, enc.name());
        
        ep.setProperty(ProjectProperties.DIST_ARCHIVE_EXCLUDES,""); //NOI18N
        ep.setComment(ProjectProperties.DIST_ARCHIVE_EXCLUDES,
                new String[] {
                    "# " + NbBundle.getMessage(AvatarJSProjectGenerator.class, "COMMENT_dist.archive.excludes") //NOI18N
                },
                false);
        
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty(ProjectProperties.COMPILE_ON_SAVE, "true"); // NOI18N
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        
        logUsage();
        return h;
    }
    
    private static DataObject createMainFile( 
            final @NonNull String mainFileName,
            final @NonNull String port,
            final @NonNull FileObject srcFolder,
            @NullAllowed String mainFileTemplate) throws IOException {
        
        /*int lastDotIdx = mainClassName.lastIndexOf( '.' );
        String mName, pName;
        if ( lastDotIdx == -1 ) {
            mName = mainClassName.trim();
            pName = null;
        }
        else {
            mName = mainClassName.substring( lastDotIdx + 1 ).trim();
            pName = mainClassName.substring( 0, lastDotIdx ).trim();
        }*/
        
        if ( mainFileName.length() == 0 ) {
            return null;
        }
        
        if (mainFileTemplate == null) {
            mainFileTemplate = MAIN_SERVER_FILE_TEMPLATE;
        }
        final FileObject mainTemplate = FileUtil.getConfigFile(mainFileTemplate);

        if ( mainTemplate == null ) {
            LOG.log(
                Level.WARNING,
                "Template {0} not found!",  //NOI18N
                mainFileTemplate);
            return null; // Don't know the template
        }
                
        DataObject mt = DataObject.find( mainTemplate );
        
        /*FileObject pkgFolder = srcFolder;
        if ( pName != null ) {
            String fName = pName.replace( '.', '/' ); // NOI18N
            pkgFolder = FileUtil.createFolder( srcFolder, fName );        
        }*/
        DataFolder pDf = DataFolder.findFolder( srcFolder );
        Map<String, Object> params = new HashMap<>();
        params.put(NewAvatarJSServerFileWizardIterator.PARAM_PORT, port);
        DataObject res = mt.createFromTemplate( pDf, mainFileName, params );
        if (res == null || !res.isValid()) {
            LOG.log(
                Level.WARNING,
                "Template {0} created an invalid DataObject in folder {1}!",  //NOI18N
                new Object[] {
                    mainFileTemplate,
                    FileUtil.getFileDisplayName(srcFolder)
                });
        }
        return res;
    }
    
    private static void createPackageJSON(FileObject srcDir, AntProjectHelper h,
                                          String projName, DataObject mainFile) throws IOException {
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        final FileObject pjTemplate = FileUtil.getConfigFile(PACKAGE_JSON_TEMPLATE);
        if ( pjTemplate == null ) {
            LOG.log(
                Level.WARNING,
                "Template {0} not found!",  //NOI18N
                PACKAGE_JSON_TEMPLATE);
            return; // Don't know the template
        }
        DataObject pt = DataObject.find(pjTemplate);
        DataFolder pDf = DataFolder.findFolder(srcDir);
        Map<String, Object> params = new HashMap<>();
        params.putAll(ep);
        params.put("projectName", projName);        // NOI18N
        String mainFileName;
        if (mainFile != null) {
            mainFileName = FileUtil.getRelativePath(srcDir, mainFile.getPrimaryFile());
        } else {
            mainFileName = "";
        }
        params.put("mainFile", mainFileName);           // NOI18N
        DataObject res = pt.createFromTemplate(pDf, PACKAGE_JSON_NAME, params);
        if (res == null || !res.isValid()) {
            LOG.log(
                Level.WARNING,
                "Template {0} created an invalid DataObject in folder {1}!",  //NOI18N
                new Object[] {
                    PACKAGE_JSON_TEMPLATE,
                    FileUtil.getFileDisplayName(srcDir)
                });
        }
    }
    
    // http://wiki.netbeans.org/UsageLoggingSpecification
    private static void logUsage() {
        LogRecord logRecord = new LogRecord(Level.INFO, loggerKey);
        logRecord.setLoggerName(loggerName);
        //logRecord.setParameters(new Object[] {""}); // NOI18N
        Logger.getLogger(loggerName).log(logRecord);
    }

}
