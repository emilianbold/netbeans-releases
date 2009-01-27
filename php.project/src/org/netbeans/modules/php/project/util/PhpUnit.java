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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.ExecutionService;
import org.netbeans.api.extexecution.ExternalProcessBuilder;
import org.netbeans.api.extexecution.input.InputProcessor;
import org.netbeans.api.extexecution.input.InputProcessors;
import org.netbeans.api.extexecution.input.LineProcessor;
import org.netbeans.modules.php.project.ui.actions.tests.PhpUnitConstants;
import org.openide.windows.InputOutput;

/**
 * @author Tomas Mysik
 */
public final class PhpUnit extends PhpProgram {
    static int[] version = null;

    /**
     * {@inheritDoc}
     */
    public PhpUnit(String command) {
        super(command);
    }

    @Override
    public boolean isValid() {
        return super.isValid();
    }

    /**
     * The minimum version of PHPUnit is <b>3.3.0</b> because:
     * - of XML log format changes (used for parsing of test results)
     * - running project action Test (older versions don't support directory as a parameter to run)
     * @return <code>true</code> if PHPUnit in minimum version was found
     */
    public boolean supportedVersionFound() {
        getVersion();
        if (version == null) {
            return false;
        }
        return version[0] >= PhpUnitConstants.MINIMAL_VERSION[0] && version[1] >= PhpUnitConstants.MINIMAL_VERSION[1];
    }

    /**
     * Get the version of PHPUnit in the form of [major][minor][revision].
     * @return
     */
    public int[] getVersion() {
        if (version != null) {
            return version;
        }

        ExternalProcessBuilder externalProcessBuilder = new ExternalProcessBuilder(getProgram())
                .addArgument(PhpUnitConstants.PARAM_VERSION);
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
        }
        return version;
    }

    public void resetVersion() {
        version = null;
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
            if (text.trim().length() == 0) {
                return null;
            }
            Matcher matcher = PHPUNIT_VERSION.matcher(text);
            if (matcher.find()) {
                int major = Integer.parseInt(matcher.group(1));
                int minor = Integer.parseInt(matcher.group(2));
                int release = Integer.parseInt(matcher.group(3));
                return new int[] {major, minor, release};
            }
            return null;
        }
    }
}
