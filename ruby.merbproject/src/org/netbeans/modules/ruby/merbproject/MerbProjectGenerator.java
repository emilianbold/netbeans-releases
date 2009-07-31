/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.ruby.merbproject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.ProjectGenerator;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.execution.DirectoryFileLocator;
import org.netbeans.modules.ruby.platform.execution.RubyExecutionDescriptor;
import org.netbeans.modules.ruby.platform.execution.RubyProcessCreator;
import org.netbeans.modules.ruby.rubyproject.Util;
import org.netbeans.modules.ruby.rubyproject.ui.customizer.RubyProjectProperties;
import org.netbeans.modules.ruby.spi.project.support.rake.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAlreadyLockedException;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Able to create NetBeans Ruby project either from scratch or from existing
 * sources.
 */
public final class MerbProjectGenerator {

    public static final String DEFAULT_SRC_NAME = "src.dir"; // NOI18N
    public static final String DEFAULT_TEST_SRC_NAME = "test.src.dir"; // NOI18N
    public static final String DEFAULT_SPEC_SRC_NAME = "spec.src.dir"; // NOI18N

    private MerbProjectGenerator() {
    }

    /**
     * Create a new empty NetBeans Ruby project.
     * 
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param prjName the name for the project
     * @param mainClass might be <tt>null</tt> to skip mainclass generation
     * @param platform project's platform
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static RakeProjectHelper createProject(File dir, 
            String prjName, String appType,
            final RubyPlatform platform) throws IOException {

        FileObject dirFO = null;
        try {
            dirFO = FileUtil.createFolder(dir);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        final String merbGen = platform.findExecutable("merb-gen");
        String displayName = NbBundle.getMessage(MerbProjectGenerator.class, "GenerateMerb");

        File pwd = dir.getParentFile();
        RubyExecutionDescriptor desc = new RubyExecutionDescriptor(platform, displayName, pwd, merbGen);
        desc.additionalArgs("app", prjName, appType);

        desc.fileLocator(new DirectoryFileLocator(dirFO));

//        LineConvertor convertor = LineConvertors.filePattern(locator, RAILS_GENERATOR_PATTERN, RubyLineConvertorFactory.EXT_RE, 2, -1);
        desc.addStandardRecognizers();
//        desc.addErrConvertor(convertor);
//        desc.addOutConvertor(convertor);

        RubyProcessCreator rpc = new RubyProcessCreator(desc);

        ExecutionService es = ExecutionService.newService(rpc, desc.toExecutionDescriptor(), displayName);
        try {
            es.run().get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }

        RakeProjectHelper helper = createBasicProjectMetadata(dirFO, prjName,
                "app", null, "spec", null, platform); // NOI18N
        Project project = ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(project);
        return helper;
    }

    /**
     * Creates a new empty Ruby project with initially set up source and test
     * folder. Used for creating NetBeans Ruby project from existing sources.
     * 
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param prjName the name for the project
     * @param sourceFolders initial source folders
     * @param testFolders initial test folders
     * @param platform project's platform
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static RakeProjectHelper createProject(final File dir, final String prjName,
            final File[] sourceFolders, final File[] testFolders, final RubyPlatform platform) throws IOException {
        assert sourceFolders != null && testFolders != null : "Package roots can't be null"; // NOI18N
        final FileObject dirFO = FileUtil.createFolder(dir);
        final RakeProjectHelper helper = createBasicProjectMetadata(dirFO, prjName, null, null, null, null, platform);
        final MerbProject project = (MerbProject) ProjectManager.getDefault().findProject(dirFO);
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {

                public Void run() throws IOException {
                    createProjectMetadata(project, helper, sourceFolders, testFolders, prjName);
                    return null;
                }
            });
        } catch (MutexException me) {
            ErrorManager.getDefault().notify(me);
        }
        return helper;
    }

    /**
     * Sets up nbproject folder appropriately according to the given data. That
     * is project.xml. That is project.properties, project.xml, etc.
     * 
     * @param project project for which to create metadata
     * @param helper {@link RakeProjectHelper}
     * @param sourceFolders initial source folders
     * @param testFolders initial test folders
     * @param prjName the name for the project
     * @throws IOException in case something went wrong
     */
    private static void createProjectMetadata(final MerbProject project,
            final RakeProjectHelper helper, final File[] sourceFolders,
            final File[] testFolders, final String prjName) throws IOException {
        ReferenceHelper refHelper = project.getReferenceHelper();
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        NodeList nl = data.getElementsByTagNameNS(MerbProjectType.PROJECT_CONFIGURATION_NAMESPACE, "source-roots"); // NOI18N

        assert nl.getLength() == 1;
        Element sourceRoots = (Element) nl.item(0);
        nl = data.getElementsByTagNameNS(MerbProjectType.PROJECT_CONFIGURATION_NAMESPACE, "test-roots"); // NOI18N

        assert nl.getLength() == 1;
        Element testRoots = (Element) nl.item(0);
        for (int i = 0; i < sourceFolders.length; i++) {
            String propName;
            if (i == 0) {
                propName = "src.dir"; // NOI18N
            } else {
                String name = sourceFolders[i].getName();
                propName = name + ".dir"; // NOI18N
            }

            int rootIndex = 1;
            EditableProperties props = helper.getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH);
            while (props.containsKey(propName)) {
                rootIndex++;
                propName = prjName + rootIndex + ".dir"; // NOI18N
            }
            String srcReference = refHelper.createForeignFileReference(sourceFolders[i], MerbProject.SOURCES_TYPE_RUBY);
            Element root = doc.createElementNS(MerbProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root"); // NOI18N

            root.setAttribute("id", propName); // NOI18N

            sourceRoots.appendChild(root);
            props = helper.getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH);
            props.put(propName, srcReference);
            helper.putProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609

        }
        for (int i = 0; i < testFolders.length; i++) {
            if (!testFolders[i].exists()) {
                testFolders[i].mkdirs();
            }
            String propName;
            if (i == 0) {
                propName = "test.src.dir"; // NOI18N

            } else {
                String name = testFolders[i].getName();
                propName = "test." + name + ".dir"; // NOI18N
            }
            int rootIndex = 1;
            EditableProperties props = helper.getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH);
            while (props.containsKey(propName)) {
                rootIndex++;
                propName = "test." + prjName + rootIndex + ".dir"; // NOI18N
            }
            String testReference = refHelper.createForeignFileReference(testFolders[i], MerbProject.SOURCES_TYPE_RUBY);
            Element root = doc.createElementNS(MerbProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root"); // NOI18N

            root.setAttribute("id", propName); // NOI18N

            testRoots.appendChild(root);
            props = helper.getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH); // #47609

            props.put(propName, testReference);
            helper.putProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH, props);
        }
        helper.putPrimaryConfigurationData(data, true);
        ProjectManager.getDefault().saveProject(project);
    }

    /**
     * Creates very basic project skeleton.
     */
    private static RakeProjectHelper createBasicProjectMetadata(FileObject dirFO, String name,
            String srcRoot, String testRoot, String specRoot, String mainClass,
            final RubyPlatform platform) throws IOException {
        RakeProjectHelper helper = ProjectGenerator.createProject(dirFO, MerbProjectType.TYPE);
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(MerbProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        EditableProperties ep = helper.getProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH);
        Element sourceRoots = doc.createElementNS(MerbProjectType.PROJECT_CONFIGURATION_NAMESPACE, "source-roots"); // NOI18N
        if (srcRoot != null) {
            Element root = doc.createElementNS(MerbProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root"); // NOI18N
            root.setAttribute("id", "src.dir"); // NOI18N
            sourceRoots.appendChild(root);
            ep.setProperty("src.dir", srcRoot); // NOI18N
        }
        data.appendChild(sourceRoots);
        Element testRoots = doc.createElementNS(MerbProjectType.PROJECT_CONFIGURATION_NAMESPACE, "test-roots"); // NOI18N
        if (testRoot != null) {
            Element root = doc.createElementNS(MerbProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root"); // NOI18N
            root.setAttribute("id", "test.src.dir"); // NOI18N
            testRoots.appendChild(root);
            ep.setProperty("test.src.dir", testRoot); // NOI18N
        }
        if (specRoot != null) {
            Element root = doc.createElementNS(MerbProjectType.PROJECT_CONFIGURATION_NAMESPACE, "root"); // NOI18N
            root.setAttribute("id", "spec.src.dir"); // NOI18N
            testRoots.appendChild(root);
            ep.setProperty("spec.src.dir", specRoot); // NOI18N
        }
        data.appendChild(testRoots);
        helper.putPrimaryConfigurationData(data, true);

        Charset enc = FileEncodingQuery.getDefaultEncoding();
        ep.setProperty(RubyProjectProperties.SOURCE_ENCODING, enc.name());
        ep.setProperty(RubyProjectProperties.MAIN_CLASS, mainClass == null ? "" : mainClass); // NOI18N
        RubyProjectProperties.storePlatform(ep, platform);
        helper.putProperties(RakeProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        Util.logUsage(MerbProjectGenerator.class, "USG_PROJECT_CREATE_MERB", // NOI18N
                platform.getInfo().getKind(),
                platform.getInfo().getPlatformVersion(),
                platform.getInfo().getGemVersion());

        return helper;
    }

    private static DataObject createFromTemplate(String mainClassName, FileObject srcFolder, String templateName) throws IOException {
        return createFromTemplate(mainClassName, srcFolder, templateName, null);
    }

    private static DataObject createFromTemplate(String mainClassName,
            FileObject srcFolder, String templateName,
            final Map<String, ? extends Object> props) throws IOException {
        int lastDotIdx = mainClassName.lastIndexOf('/');
        String mName, pName;
        if (lastDotIdx == -1) {
            mName = mainClassName.trim();
            pName = null;
        } else {
            mName = mainClassName.substring(lastDotIdx + 1).trim();
            pName = mainClassName.substring(0, lastDotIdx).trim();
        }

        if (mName.length() == 0) {
            return null;
        }

        FileObject mainTemplate = FileUtil.getConfigFile(templateName);

        if (mainTemplate == null) {
            return null; // Don't know the template
        }

        DataObject mt = DataObject.find(mainTemplate);

        FileObject pkgFolder = srcFolder;
        if (pName != null) {
            String fName = pName.replace('.', '/');
            pkgFolder = FileUtil.createFolder(srcFolder, fName);
        }
        DataFolder pDf = DataFolder.findFolder(pkgFolder);

        mName = Util.stripExtension(mName, ".rb"); // NOI18N

        if (props != null) {
            return mt.createFromTemplate(pDf, mName, props);
        } else {
            return mt.createFromTemplate(pDf, mName);
        }
    }

    private static void createFileWithContent(final FileObject dirFO, final String fileName,
            final String contentKey, final String prjName) throws IOException {
        FileObject newFile = dirFO.createData(fileName); // NOI18N
        writeLines(newFile, NbBundle.getMessage(MerbProjectGenerator.class, contentKey, prjName));
    }

    // TODO: use FileUtils when #118087 is fixed
    private static void writeLines(final FileObject readme, final String... lines) throws FileAlreadyLockedException, IOException {
        PrintWriter readmeW = new PrintWriter(new OutputStreamWriter(readme.getOutputStream(), "UTF-8")); // NOI18N
        for (String line : lines) {
            readmeW.println(line);
        }
        readmeW.close();
    }
}
