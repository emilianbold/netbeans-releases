/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.php.project.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.php.api.util.Pair;
import org.netbeans.modules.php.api.util.PhpProgram;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * PHP Unit 3.x support.
 * @author Tomas Mysik
 */
public final class PhpUnit extends PhpProgram {
    // for keeping log files to able to evaluate and fix issues
    public static final boolean KEEP_LOGS = Boolean.getBoolean("org.netbeans.modules.php.project.util.PhpUnit.keepLogs");
    // test files suffix
    public static final String TEST_CLASS_SUFFIX = "Test"; // NOI18N
    public static final String TEST_FILE_SUFFIX = TEST_CLASS_SUFFIX + ".php"; // NOI18N
    // cli options
    public static final String PARAM_VERSION = "--version"; // NOI18N
    public static final String PARAM_XML_LOG = "--log-xml"; // NOI18N
    public static final String PARAM_COVERAGE_LOG = "--coverage-clover"; // NOI18N
    public static final String PARAM_SKELETON = "--skeleton-test"; // NOI18N
    // for older PHP Unit versions
    public static final String PARAM_SKELETON_OLD = "--skeleton"; // NOI18N
    // bootstrap & config
    public static final String PARAM_BOOTSTRAP = "--bootstrap"; // NOI18N
    private static final String BOOTSTRAP_FILENAME = "bootstrap%s.php"; // NOI18N
    public static final String PARAM_CONFIGURATION = "--configuration"; // NOI18N
    private static final String CONFIGURATION_FILENAME = "configuration%s.xml"; // NOI18N

    // output files
    public static final File XML_LOG = new File(System.getProperty("java.io.tmpdir"), "nb-phpunit-log.xml"); // NOI18N
    public static final File COVERAGE_LOG = new File(System.getProperty("java.io.tmpdir"), "nb-phpunit-coverage.xml"); // NOI18N

    // suite file
    public static final String SUITE_NAME = "NetBeansSuite.php"; // NOI18N
    public static final String SUITE_RUN = "run=%s"; // NOI18N
    public static final File SUITE;

    // php props
    public static final char DIRECTORY_SEPARATOR = '/'; // NOI18N
    public static final String DIRNAME_FILE = ".dirname(__FILE__).'/"; // NOI18N
    public static final String REQUIRE_ONCE_REL_PART = "'" + DIRNAME_FILE; // NOI18N

    public static final Pattern LINE_PATTERN = Pattern.compile("(?:.+\\(\\) )?(.+):(\\d+)"); // NOI18N

    // unknown version
    static final int[] UNKNOWN_VERSION = new int[0];
    // minimum supported version
    static final int[] MINIMAL_VERSION = new int[] {3, 3, 0};

    /**
     * volatile is enough because:
     *  - never mind if the version is detected 2x
     *  - we don't change array values but only the array itself (local variable created and then assigned to 'version')
     */
    static volatile int[] version = null;

    static {
        SUITE = InstalledFileLocator.getDefault().locate(SUITE_NAME, "org.netbeans.modules.php.project", false);  // NOI18N
        if (SUITE == null || !SUITE.isFile()) {
            throw new IllegalStateException("Could not locate file " + SUITE_NAME);
        }
    }

    /**
     * {@inheritDoc}
     */
    public PhpUnit(String command) {
        super(command);
    }

    // XXX see 2nd paragraph
    /**
     * The minimum version of PHPUnit is <b>3.3.0</b> because:
     * - of XML log format changes (used for parsing of test results)
     * - running project action Test (older versions don't support directory as a parameter to run)
     * <p>
     * Since issue #167519 is fixed, this is not necessary true any more:
     * - test listener could be used instead of XML file (this would be more reliable and XML file independent)
     * - all tests are run using suite file, so no need to support directory as a parameter
     * @return <code>true</code> if PHPUnit in minimum version was found
     */
    public boolean supportedVersionFound() {
        if (!isValid()) {
            return false;
        }
        getVersion();
        return version != null
                && version != UNKNOWN_VERSION
                && version[0] >= MINIMAL_VERSION[0]
                && version[1] >= MINIMAL_VERSION[1];
    }

    public static void resetVersion() {
        version = null;
    }

    /**
     * Get the version of PHPUnit in the form of [major][minor][revision].
     * @return
     */
    private int[] getVersion() {
        if (!isValid()) {
            return UNKNOWN_VERSION;
        }
        if (version != null) {
            return version;
        }

        version = UNKNOWN_VERSION;
        ExternalProcessBuilder externalProcessBuilder = new ExternalProcessBuilder(getProgram())
                .addArgument(PARAM_VERSION);
        ExecutionDescriptor executionDescriptor = new ExecutionDescriptor()
                .inputOutput(InputOutput.NULL)
                .outProcessorFactory(new OutputProcessorFactory());
        ExecutionService service = ExecutionService.newService(externalProcessBuilder, executionDescriptor, null);
        Future<Integer> result = service.run();
        try {
            result.get();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            // ignored
            LOGGER.log(Level.INFO, null, ex);
        }
        return version;
    }

    /**
     * Get an array with actual and minimal PHPUnit versions.
     * <p>
     * Return three times "?" if the actual version is not known or <code>null</code>.
     */
    public static String[] getVersions(PhpUnit phpUnit) {
        List<String> params = new ArrayList<String>(6);
        if (phpUnit == null || phpUnit.getVersion() == UNKNOWN_VERSION) {
            String questionMark = NbBundle.getMessage(PhpUnit.class, "LBL_QuestionMark");
            params.add(questionMark); params.add(questionMark); params.add(questionMark);
        } else {
            for (Integer i : phpUnit.getVersion()) {
                params.add(String.valueOf(i));
            }
        }
        for (Integer i : MINIMAL_VERSION) {
            params.add(String.valueOf(i));
        }
        return params.toArray(new String[params.size()]);
    }

    public Files getFiles(PhpProject project, boolean withSuite) {
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
            missingFiles.add(Pair.of(NbBundle.getMessage(PhpUnit.class, "LBL_Configuration"), configuration));
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
        return new Files(bootstrap, configuration, suite);
    }

    public File getCustomSuite(PhpProject project) {
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
            LOGGER.info("Cannot delete temporary file " + tmpBootstrap);
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
        return getRelPath(testFile, sourceFile, DIRNAME_FILE, "'"); // NOI18N
    }

    public static String getRequireOnce(File testFile, File sourceFile) {
        return getRelPath(testFile, sourceFile, REQUIRE_ONCE_REL_PART, ""); // NOI18N
    }

    // XXX improve this and related method
    public static String getRelPath(File testFile, File sourceFile, String prefix, String suffix) {
        File parentFile = testFile.getParentFile();
        String relPath = PropertyUtils.relativizeFile(parentFile, sourceFile);
        if (relPath == null) {
            // cannot be versioned...
            relPath = sourceFile.getAbsolutePath();
        } else {
            relPath = prefix + relPath + suffix;
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

    public static final class Files {
        public final File bootstrap;
        public final File configuration;
        public final File suite;

        public Files(File bootstrap, File configuration, File suite) {
            this.bootstrap = bootstrap;
            this.configuration = configuration;
            this.suite = suite;
        }
    }

    static final class OutputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory {
        //                                                              PHPUnit 3.3.1 by Sebastian Bergmann.
        private static final Pattern PHPUNIT_VERSION = Pattern.compile("PHPUnit\\s+(\\d+)\\.(\\d+)\\.(\\d+)\\s+"); // NOI18N

        public InputProcessor newInputProcessor(final InputProcessor defaultProcessor) {
            return InputProcessors.bridge(new LineProcessor() {
                public void processLine(String line) {
                    int[] match = match(line);
                    if (match != null) {
                        version = match;
                    }
                }
                public void reset() {
                    try {
                        defaultProcessor.reset();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                public void close() {
                    try {
                        defaultProcessor.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
        }

        static int[] match(String text) {
            assert text != null;
            if (StringUtils.hasText(text)) {
                Matcher matcher = PHPUNIT_VERSION.matcher(text);
                if (matcher.find()) {
                    int major = Integer.parseInt(matcher.group(1));
                    int minor = Integer.parseInt(matcher.group(2));
                    int release = Integer.parseInt(matcher.group(3));
                    return new int[] {major, minor, release};
                }
            }
            return null;
        }
    }
}
