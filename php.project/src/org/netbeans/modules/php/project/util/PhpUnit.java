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

import java.io.File;
import java.util.ArrayList;
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
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * PHP Unit 3.x support.
 * @author Tomas Mysik
 */
public final class PhpUnit extends PhpProgram {
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
    // output files
    public static final File XML_LOG = new File(System.getProperty("java.io.tmpdir"), "nb-phpunit-log.xml"); // NOI18N
    public static final File COVERAGE_LOG = new File(System.getProperty("java.io.tmpdir"), "nb-phpunit-coverage.xml"); // NOI18N

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

    /**
     * {@inheritDoc}
     */
    public PhpUnit(String command) {
        super(command);
    }

    /**
     * The minimum version of PHPUnit is <b>3.3.0</b> because:
     * - of XML log format changes (used for parsing of test results)
     * - running project action Test (older versions don't support directory as a parameter to run)
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

    static final class OutputProcessorFactory implements ExecutionDescriptor.InputProcessorFactory {
        //                                                              PHPUnit 3.3.1 by Sebastian Bergmann.
        private static final Pattern PHPUNIT_VERSION = Pattern.compile("PHPUnit\\s+(\\d+)\\.(\\d+)\\.(\\d+)\\s+"); // NOI18N

        public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
            return InputProcessors.bridge(new LineProcessor() {
                public void processLine(String line) {
                    int[] match = match(line);
                    if (match != null) {
                        version = match;
                    }
                }
                public void reset() {
                }
                public void close() {
                }
            });
        }

        static int[] match(String text) {
            assert text != null;
            if (PhpProjectUtils.hasText(text)) {
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
