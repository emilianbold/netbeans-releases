/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.phpunit;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.project.deprecated.PhpProgram;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.api.util.Pair;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.options.PhpOptions;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

/**
 * PHPUnit 3.4+ support.
 * @author Tomas Mysik
 */
public final class PhpUnit extends PhpProgram {

    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    private static final Logger LOGGER = Logger.getLogger(PhpUnit.class.getName());

    public static final String SCRIPT_NAME = "phpunit"; // NOI18N
    public static final String SCRIPT_NAME_LONG = SCRIPT_NAME + FileUtils.getScriptExtension(true);
    // for keeping log files to able to evaluate and fix issues
    public static final boolean KEEP_LOGS = Boolean.getBoolean("nb.php.phpunit.keeplogs"); // NOI18N
    // options
    public static final String OPTIONS_SUB_PATH = "PhpUnit"; // NOI18N
    public static final String OPTIONS_PATH = UiUtils.OPTIONS_PATH + "/" + PhpUnit.OPTIONS_SUB_PATH; // NOI18N
    // test files suffix
    public static final String TEST_CLASS_SUFFIX = "Test"; // NOI18N
    private static final String TEST_FILE_SUFFIX = TEST_CLASS_SUFFIX + ".php"; // NOI18N
    // suite files suffix
    private static final String SUITE_CLASS_SUFFIX = "Suite"; // NOI18N
    private static final String SUITE_FILE_SUFFIX = SUITE_CLASS_SUFFIX + ".php"; // NOI18N
    // create test
    private static final String REQUIRE_ONCE_TPL_START = "require_once '"; // NOI18N
    private static final String REQUIRE_ONCE_TPL_END = "%s';"; // NOI18N
    private static final String REQUIRE_ONCE_TPL = "require_once '%s';"; // NOI18N
    private static final String DIRNAME_FILE = ".dirname(__FILE__).'/"; // NOI18N
    static final String REQUIRE_ONCE_REL_PART = "'" + DIRNAME_FILE; // NOI18N
    // cli options
    public static final String PARAM_JUNIT_LOG = "--log-junit"; // NOI18N
    public static final String PARAM_FILTER = "--filter"; // NOI18N
    public static final String PARAM_COVERAGE_LOG = "--coverage-clover"; // NOI18N
    public static final String PARAM_SKELETON = "--skeleton-test"; // NOI18N
    public static final String PARAM_LIST_GROUPS = "--list-groups"; // NOI18N
    public static final String PARAM_GROUP = "--group"; // NOI18N

    // bootstrap & config
    public static final String PARAM_BOOTSTRAP = "--bootstrap"; // NOI18N
    private static final String BOOTSTRAP_FILENAME = "bootstrap%s.php"; // NOI18N
    public static final String PARAM_CONFIGURATION = "--configuration"; // NOI18N
    private static final String CONFIGURATION_FILENAME = "configuration%s.xml"; // NOI18N

    // output files
    public static final File XML_LOG;
    public static final File COVERAGE_LOG;

    // suite file
    public static final String SUITE_NAME = "NetBeansSuite"; // NOI18N
    public static final String SUITE_RUN = "--run=%s"; // NOI18N
    private static final String SUITE_REL_PATH = "phpunit/" + SUITE_NAME + ".php"; // NOI18N

    // php props
    private static final char DIRECTORY_SEPARATOR = '/'; // NOI18N

    public static final Pattern LINE_PATTERN = Pattern.compile("(?:.+\\(\\) )?(.+):(\\d+)"); // NOI18N

    // #200489
    private static volatile File suite; // ok if it is fetched more times


    static {
        // output files, see #200775
        String logDirName = System.getProperty("java.io.tmpdir"); // NOI18N
        String userLogDirName = System.getProperty("nb.php.phpunit.logdir"); // NOI18N
        if (userLogDirName != null) {
            LOGGER.log(Level.INFO, "Custom directory for PhpUnit logs provided: {0}", userLogDirName);
            File userLogDir = new File(userLogDirName);
            if (userLogDir.isDirectory() && FileUtils.isDirectoryWritable(userLogDir)) {
                logDirName = userLogDirName;
            } else {
                LOGGER.log(Level.WARNING, "Directory for PhpUnit logs {0} is not writable directory", userLogDirName);
            }
        }
        LOGGER.log(Level.FINE, "Directory for PhpUnit logs: {0}", logDirName);
        XML_LOG = new File(logDirName, "nb-phpunit-log.xml"); // NOI18N
        COVERAGE_LOG = new File(logDirName, "nb-phpunit-coverage.xml"); // NOI18N
     }

    private PhpUnit(String command) {
        super(command);
    }

    public static File getNbSuite() {
        if (suite == null) {
            suite = InstalledFileLocator.getDefault().locate(SUITE_REL_PATH, "org.netbeans.modules.php.project", false);  // NOI18N
        }
        return suite;
    }

    public static PhpUnit getDefault() throws InvalidPhpProgramException {
        return getCustom(PhpOptions.getInstance().getPhpUnit());
    }

    public static PhpUnit forProject(PhpProject project) throws InvalidPhpProgramException {
        File script = ProjectPropertiesSupport.getPhpUnitScript(project);
        if (script == null) {
            return null;
        }
        return getCustom(script.getAbsolutePath());
    }

    public static PhpUnit getCustom(String command) throws InvalidPhpProgramException {
        String error = validate(command);
        if (error != null) {
            throw new InvalidPhpProgramException(error);
        }
        return new PhpUnit(command);
    }

    private ExecutionDescriptor getDescriptor() {
        return getExecutionDescriptor()
                .controllable(false)
                .frontWindow(false);
    }

    public File generateTest(PhpProject phpProject, ConfigFiles configFiles, PhpClass phpClass, FileObject sourceFo, File workingDirectory) {
        String className = phpClass.getName();
        final File testFile = getTestFile(phpProject, sourceFo, className);
        if (testFile.isFile()) {
            // already exists
            return testFile;
        }
        final File sourceFile = FileUtil.toFile(sourceFo);
        final File parent = FileUtil.toFile(sourceFo.getParent());

        // # 205135
        File generatedFile = getGeneratedFile(className, parent);
        if (generatedFile.isFile()) {
            // test already exists, next to source file
            if (!useExistingTestInSources(generatedFile)) {
                return null;
            }
        } else {
            // test does not exist yet
            if (!generateTestInternal(configFiles, phpClass.getFullyQualifiedName(), sourceFo, workingDirectory)) {
                // test not generated
                return null;
            }
        }
        if (!generatedFile.isFile()) {
            LOGGER.log(Level.WARNING, "Generated PHPUnit test file {0} was not found.", generatedFile.getName());
            return null;
        }
        return moveAndAdjustGeneratedFile(generatedFile, testFile, sourceFile);
    }

    private boolean generateTestInternal(ConfigFiles configFiles, String className, FileObject sourceFo, File workingDirectory) {
        ExternalProcessBuilder externalProcessBuilder = getProcessBuilder()
                .workingDirectory(workingDirectory);
        // #179960
        if (configFiles.bootstrap != null
                && configFiles.useBootstrapForCreateTests) {
            externalProcessBuilder = externalProcessBuilder
                    .addArgument(PhpUnit.PARAM_BOOTSTRAP)
                    .addArgument(configFiles.bootstrap.getAbsolutePath());
        }
        if (configFiles.configuration != null) {
            externalProcessBuilder = externalProcessBuilder
                    .addArgument(PhpUnit.PARAM_CONFIGURATION)
                    .addArgument(configFiles.configuration.getAbsolutePath());
        }
        // http://www.phpunit.de/ticket/904
        if (className.startsWith("\\")) { // NOI18N
            className = className.substring(1);
        }
        externalProcessBuilder = externalProcessBuilder
                .addArgument(PARAM_SKELETON)
                .addArgument(className)
                .addArgument(FileUtil.toFile(sourceFo).getAbsolutePath());

        try {
            int status = executeAndWait(
                    externalProcessBuilder,
                    getDescriptor(),
                    String.format("%s %s %s %s", getProgram(), PARAM_SKELETON, className, sourceFo.getNameExt())); // NOI18N
            return status == 0;
        } catch (CancellationException ex) {
            // canceled
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, PhpUnit.OPTIONS_SUB_PATH);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return false;
    }

    private File moveAndAdjustGeneratedFile(File generatedFile, File testFile, File sourceFile) {
        assert generatedFile.isFile() : "Generated files must exist: " + generatedFile;
        assert !testFile.exists() : "Test file cannot exist: " + testFile;

        // create all the parents
        try {
            FileUtil.createFolder(testFile.getParentFile());
        } catch (IOException exc) {
            // what to do now??
            LOGGER.log(Level.WARNING, null, exc);
            return generatedFile;
        }

        testFile = adjustFileContent(generatedFile, testFile, sourceFile, PhpUnit.getRequireOnce(testFile, sourceFile));
        if (testFile == null) {
            return null;
        }
        assert testFile.isFile() : "Test file must exist: " + testFile;

        // reformat the file
        try {
            PhpProjectUtils.reformatFile(testFile);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, "Cannot reformat file " + testFile, ex);
        }

        return testFile;
    }

    private File adjustFileContent(File generatedFile, File testFile, File sourceFile, String requireOnce) {
        try {
            // input
            BufferedReader in = new BufferedReader(new FileReader(generatedFile));

            try {
                // output
                BufferedWriter out = new BufferedWriter(new FileWriter(testFile));

                try {
                    String line;
                    boolean requireWritten = false;
                    String filename = sourceFile.getName();
                    while ((line = in.readLine()) != null) {
                        if (!requireWritten && PhpUnit.isRequireOnceSourceFile(line.trim(), filename)) {
                            // original require generated by phpunit
                            out.write(String.format(REQUIRE_ONCE_TPL, requireOnce).replace("''.", "")); // NOI18N
                            requireWritten = true;
                        } else {
                            out.write(line);
                        }
                        out.newLine();
                    }
                } finally {
                    out.flush();
                    out.close();
                }
            } finally {
                in.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return null;
        }

        if (!generatedFile.delete()) {
            LOGGER.log(Level.INFO, "Cannot delete generated file {0}", generatedFile);
        }
        return testFile;
    }

    public static File getTestFile(PhpProject project, FileObject source, String className) {
        assert project != null;
        assert source != null;

        FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(project);
        String relativeSourcePath = FileUtil.getRelativePath(sourcesDirectory, source.getParent());
        assert relativeSourcePath != null : String.format("Relative path must be found for sources %s and folder %s", sourcesDirectory, source.getParent());

        File relativeTestDirectory = PhpProjectUtils.resolveFile(getTestDirectory(project), relativeSourcePath);

        return new File(relativeTestDirectory, PhpUnit.makeTestFile(className));
    }

    private static File getTestDirectory(PhpProject phpProject) {
        FileObject testDirectory = ProjectPropertiesSupport.getTestDirectory(phpProject, false);
        assert testDirectory != null && testDirectory.isValid() : "Valid folder for tests must be found for " + phpProject;
        return FileUtil.toFile(testDirectory);
    }

    private File getGeneratedFile(String className, File parent) {
        return new File(parent, PhpUnit.makeTestFile(className));
    }

    @NbBundle.Messages({
        "# {0} - file name",
        "PhpUnit.useTestFileInSources=Use the existing test file {0}? If not, no test will be generated for this file."
    })
    private boolean useExistingTestInSources(File testFile) {
        NotifyDescriptor.Confirmation confirmation = new NotifyDescriptor.Confirmation(
                Bundle.PhpUnit_useTestFileInSources(testFile.getName()),
                NotifyDescriptor.YES_NO_OPTION);
        return DialogDisplayer.getDefault().notify(confirmation) == NotifyDescriptor.YES_OPTION;
    }

    public static boolean isRequireOnceSourceFile(String line, String filename) {
        return line.startsWith(REQUIRE_ONCE_TPL_START)
                && line.endsWith(String.format(REQUIRE_ONCE_TPL_END, filename));
    }

    public static boolean isTestFile(String fileName) {
        return !fileName.equals(PhpUnit.TEST_FILE_SUFFIX) && fileName.endsWith(PhpUnit.TEST_FILE_SUFFIX);
    }

    public static boolean isTestClass(String className) {
        return !className.equals(PhpUnit.TEST_CLASS_SUFFIX) && className.endsWith(PhpUnit.TEST_CLASS_SUFFIX);
    }

    public static boolean isSuiteFile(String fileName) {
        return !fileName.equals(PhpUnit.SUITE_FILE_SUFFIX) && fileName.endsWith(PhpUnit.SUITE_FILE_SUFFIX);
    }

    public static boolean isSuiteClass(String className) {
        return !className.equals(PhpUnit.SUITE_CLASS_SUFFIX) && className.endsWith(PhpUnit.SUITE_CLASS_SUFFIX);
    }

    public static boolean isTestOrSuiteFile(String fileName) {
        return isTestFile(fileName) || isSuiteFile(fileName);
    }

    public static boolean isTestOrSuiteClass(String className) {
        return isTestClass(className) || isSuiteClass(className);
    }

    public static String getTestedClass(String testOrSuiteClass) {
        assert isTestOrSuiteClass(testOrSuiteClass) : "Not Test or Suite class: " + testOrSuiteClass;
        int lastIndexOf = -1;
        if (isTestClass(testOrSuiteClass)) {
            lastIndexOf = testOrSuiteClass.lastIndexOf(PhpUnit.TEST_CLASS_SUFFIX);
        } else if (isSuiteClass(testOrSuiteClass)) {
            lastIndexOf = testOrSuiteClass.lastIndexOf(PhpUnit.SUITE_CLASS_SUFFIX);
        }
        assert lastIndexOf != -1;
        return testOrSuiteClass.substring(0, lastIndexOf);
    }

    public static String makeTestFile(String testedFileName) {
        return testedFileName + PhpUnit.TEST_FILE_SUFFIX;
    }

    public static String makeTestClass(String testedClass) {
        return testedClass + PhpUnit.TEST_CLASS_SUFFIX;
    }

    public static String makeSuiteFile(String testedFileName) {
        return testedFileName + PhpUnit.SUITE_FILE_SUFFIX;
    }

    public static String makeSuiteClass(String testedClass) {
        return testedClass + PhpUnit.SUITE_CLASS_SUFFIX;
    }

    @Override
    public ExternalProcessBuilder getProcessBuilder() {
        return super.getProcessBuilder()
                .workingDirectory(new File(getProgram()).getParentFile());
    }

    // #170120
    public File getWorkingDirectory(ConfigFiles configFiles, File defaultWorkingDirectory) {
        if (configFiles.configuration != null) {
            return configFiles.configuration.getParentFile();
        }
        return defaultWorkingDirectory;
    }

    public static ConfigFiles getConfigFiles(PhpProject project, boolean withSuite) {
        List<Pair<String, File>> missingFiles = new LinkedList<Pair<String, File>>();
        File bootstrap = ProjectPropertiesSupport.getPhpUnitBootstrap(project);
        if (bootstrap != null
                && !bootstrap.isFile()) {
            missingFiles.add(Pair.of(NbBundle.getMessage(PhpUnit.class, "LBL_Bootstrap"), bootstrap));
            bootstrap = null;
        }

        File configuration = ProjectPropertiesSupport.getPhpUnitConfiguration(project);
        if (configuration != null
                && !configuration.isFile()) {
            missingFiles.add(Pair.of(NbBundle.getMessage(PhpUnit.class, "LBL_XmlConfiguration"), configuration));
            configuration = null;
        }

        File suite = null;
        if (withSuite) {
            suite = ProjectPropertiesSupport.getPhpUnitSuite(project);
            if (suite != null
                    && !suite.isFile()) {
                missingFiles.add(Pair.of(NbBundle.getMessage(PhpUnit.class, "LBL_TestSuite"), suite));
                suite = null;
            }
        }
        warnAboutMissingFiles(missingFiles);
        return new ConfigFiles(bootstrap, ProjectPropertiesSupport.usePhpUnitBootstrapForCreateTests(project), configuration, suite);
    }

    public static File getCustomSuite(PhpProject project) {
        File suite = ProjectPropertiesSupport.getPhpUnitSuite(project);
        if (suite != null
                && suite.isFile()) {
            return suite;
        }
        return null;
    }

    public static File createBootstrapFile(final PhpProject project) {
        FileObject testDirectory = ProjectPropertiesSupport.getTestDirectory(project, false);
        assert testDirectory != null : "Test directory must already be set";

        final FileObject configFile = FileUtil.getConfigFile("Templates/PHPUnit/PHPUnitBootstrap"); // NOI18N
        final DataFolder dataFolder = DataFolder.findFolder(testDirectory);
        final File bootstrapFile = new File(getBootstrapFilepath(project));
        final File[] files = new File[1];
        FileUtil.runAtomicAction(new Runnable() {
            @Override
            public void run() {
                try {
                    DataObject dataTemplate = DataObject.find(configFile);
                    DataObject bootstrap = dataTemplate.createFromTemplate(dataFolder, bootstrapFile.getName() + "~"); // NOI18N
                    assert bootstrap != null;
                    moveAndAdjustBootstrap(project, FileUtil.toFile(bootstrap.getPrimaryFile()), bootstrapFile);
                    assert bootstrapFile.isFile();
                    files[0] = bootstrapFile;
                    informAboutGeneratedFile(bootstrapFile.getName());
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Cannot create PHPUnit bootstrap file", ex);
                }
            }
        });
        if (files[0] == null) {
            // no file generated
            warnAboutNotGeneratedFile(bootstrapFile.getName());
        }
        return files[0];
    }

    private static void moveAndAdjustBootstrap(PhpProject project, File tmpBootstrap, File finalBootstrap) {
        try {
            // input
            BufferedReader in = new BufferedReader(new FileReader(tmpBootstrap));
            try {
                // output
                BufferedWriter out = new BufferedWriter(new FileWriter(finalBootstrap));
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        if (line.contains("%INCLUDE_PATH%")) { // NOI18N
                            if (line.startsWith("//")) { // NOI18N
                                // comment about %INCLUDE_PATH%, let's skip it
                                continue;
                            }
                            line = processIncludePath(
                                    finalBootstrap,
                                    line,
                                    ProjectPropertiesSupport.getPropertyEvaluator(project).getProperty(PhpProjectProperties.INCLUDE_PATH),
                                    FileUtil.toFile(project.getProjectDirectory()));
                        }
                        out.write(line);
                        out.newLine();
                    }
                } finally {
                    out.flush();
                    out.close();
                }
            } finally {
                in.close();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }

        if (!tmpBootstrap.delete()) {
            LOGGER.log(Level.INFO, "Cannot delete temporary file {0}", tmpBootstrap);
            tmpBootstrap.deleteOnExit();
        }

        FileUtil.refreshFor(finalBootstrap.getParentFile());
    }

    static String processIncludePath(File bootstrap, String line, String includePath, File projectDir) {
        if (StringUtils.hasText(includePath)) {
            if (includePath.startsWith(":")) { // NOI18N
                includePath = includePath.substring(1);
            }
            StringBuilder buffer = new StringBuilder(200);
            for (String path : PropertyUtils.tokenizePath(includePath)) {
                File reference = PropertyUtils.resolveFile(projectDir, path);
                buffer.append(".PATH_SEPARATOR"); // NOI18N
                buffer.append(getDirnameFile(bootstrap, reference));
            }
            includePath = buffer.toString();
        } else {
            // comment out the line
            line = "//" + line; // NOI18N
        }
        line = line.replace("%INCLUDE_PATH%", includePath); // NOI18N
        return line;
    }

    public static File createConfigurationFile(PhpProject project) {
        FileObject testDirectory = ProjectPropertiesSupport.getTestDirectory(project, false);
        assert testDirectory != null : "Test directory must already be set";

        final FileObject configFile = FileUtil.getConfigFile("Templates/PHPUnit/PHPUnitConfiguration.xml"); // NOI18N
        final DataFolder dataFolder = DataFolder.findFolder(testDirectory);
        final File configurationFile = new File(getConfigurationFilepath(project));
        File file = null;
        try {
            DataObject dataTemplate = DataObject.find(configFile);
            DataObject configuration = dataTemplate.createFromTemplate(dataFolder, configurationFile.getName().replace(".xml", "")); // NOI18N
            assert configuration != null;
            file = configurationFile;
            informAboutGeneratedFile(configurationFile.getName());
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Cannot create PHPUnit configuration file", ex);
        }
        if (file == null) {
            // no file generated
            warnAboutNotGeneratedFile(configurationFile.getName());
        }
        return file;
    }

    public static void informAboutGeneratedFile(String generatedFile) {
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(NbBundle.getMessage(PhpUnit.class, "MSG_FileGenerated", generatedFile)));
    }

    private static void warnAboutNotGeneratedFile(String file) {
        NotifyDescriptor warning = new NotifyDescriptor.Message(
                NbBundle.getMessage(PhpUnit.class, "MSG_NotGenerated", file),
                NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(warning);
    }

    private static void warnAboutMissingFiles(List<Pair<String, File>> missingFiles) {
        if (missingFiles.isEmpty()) {
            return;
        }
        StringBuilder buffer = new StringBuilder(100);
        for (Pair<String, File> pair : missingFiles) {
            buffer.append(NbBundle.getMessage(PhpUnit.class, "LBL_MissingFile", pair.first, pair.second.getAbsolutePath()));
            buffer.append("\n"); // NOI18N
        }
        NotifyDescriptor warning = new NotifyDescriptor.Message(
                NbBundle.getMessage(PhpUnit.class, "MSG_MissingFiles", buffer.toString()),
                NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notifyLater(warning);
    }

    private static String getDirnameFile(File testFile, File sourceFile) {
        return getRelPath(testFile, sourceFile, ".'", DIRNAME_FILE, "'"); // NOI18N
    }

    public static String getRequireOnce(File testFile, File sourceFile) {
        return getRelPath(testFile, sourceFile, "", REQUIRE_ONCE_REL_PART, ""); // NOI18N
    }

    // XXX improve this and related method
    private static String getRelPath(File testFile, File sourceFile, String absolutePrefix, String relativePrefix, String suffix) {
        return getRelPath(testFile, sourceFile, absolutePrefix, relativePrefix, suffix, false);
    }

    // forceAbsolute only for unit tests
    static String getRelPath(File testFile, File sourceFile, String absolutePrefix, String relativePrefix, String suffix, boolean forceAbsolute) {
        File parentFile = testFile.getParentFile();
        String relPath = PropertyUtils.relativizeFile(parentFile, sourceFile);
        if (relPath == null || forceAbsolute) {
            // cannot be versioned...
            relPath = absolutePrefix + sourceFile.getAbsolutePath() + suffix;
        } else {
            relPath = relativePrefix + relPath + suffix;
        }
        return relPath.replace(File.separatorChar, DIRECTORY_SEPARATOR);
    }

    private static String getBootstrapFilepath(PhpProject project) {
        return getFilepath(project, BOOTSTRAP_FILENAME);
    }

    private static String getConfigurationFilepath(PhpProject project) {
        return getFilepath(project, CONFIGURATION_FILENAME);
    }

    private static String getFilepath(PhpProject project, String filename) {
        FileObject testDirectory = ProjectPropertiesSupport.getTestDirectory(project, false);
        assert testDirectory != null : "Test directory must already be set";

        File tests = FileUtil.toFile(testDirectory);
        File file = null;
        int i = 0;
        do {
            file = new File(tests, getFilename(filename, i++));
        } while (file.isFile());
        assert !file.isFile();
        return file.getAbsolutePath();
    }

    private static String getFilename(String filename, int i) {
        return String.format(filename, i == 0 ? "" : i); // NOI18N
    }

    @NbBundle.Messages("PhpUnit.script.label=PHPUnit script")
    @Override
    public String validate() {
        return FileUtils.validateFile(Bundle.PhpUnit_script_label(), getProgram(), false);
    }

    public static String validate(String command) {
        return new PhpUnit(command).validate();
    }

    public static final class ConfigFiles {
        public final File bootstrap;
        public final boolean useBootstrapForCreateTests;
        public final File configuration;
        public final File suite;

        public ConfigFiles(File bootstrap, boolean useBootstrapForCreateTests, File configuration, File suite) {
            this.bootstrap = bootstrap;
            this.useBootstrapForCreateTests = useBootstrapForCreateTests;
            this.configuration = configuration;
            this.suite = suite;
        }
    }

}
