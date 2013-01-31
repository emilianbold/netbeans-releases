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
package org.netbeans.modules.php.phpunit.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.phpunit.commands.PhpUnit;
import org.netbeans.modules.php.phpunit.preferences.PhpUnitPreferences;
import org.netbeans.modules.php.spi.testing.run.TestRunException;
import org.netbeans.modules.php.spi.testing.run.TestRunInfo;
import org.netbeans.modules.php.spi.testing.run.TestSession;

public final class TestRunner {

    private static final Logger LOGGER = Logger.getLogger(TestRunner.class.getName());

    private final PhpModule phpModule;


    public TestRunner(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
    }

    public TestSession runTests(TestRunInfo runInfo) throws TestRunException {
        PhpUnit phpUnit = PhpUnit.getForPhpModule(phpModule, true);
        if (phpUnit == null) {
            return null;
        }
        Integer result = phpUnit.runTests(phpModule, runInfo);
        if (result == null) { // do NOT check 0 since phpunit returns 1 if any test fails
            // some error
            return null;
        }
        return createTestSession(PhpUnit.XML_LOG);
    }

    private TestSession createTestSession(File xmlLog) throws TestRunException {
        Reader reader;
        try {
            // #163633 - php unit always uses utf-8 for its xml logs
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(xmlLog), "UTF-8")); // NOI18N
        } catch (UnsupportedEncodingException ex) {
            LOGGER.log(Level.WARNING, null, ex);
            return null;
        } catch (FileNotFoundException ex) {
            processPhpUnitError(ex);
            return null;
        }
        TestSessionImpl session = new TestSessionImpl(getCustomTestSuite());
        boolean parsed = PhpUnitLogParser.parse(reader, session);
        if (!parsed) {
            processPhpUnitError(null);
            return null;
        }
        return session;
    }

    private String getCustomTestSuite() {
        if (PhpUnitPreferences.isSuiteEnabled(phpModule)) {
            return PhpUnitPreferences.getSuitePath(phpModule);
        }
        return null;
    }


    private void processPhpUnitError(Exception cause) throws TestRunException {
        LOGGER.info(String.format("File %s not found or cannot be parsed. If there are no errors in PHPUnit output (verify in Output window), "
                + "please report an issue (http://www.netbeans.org/issues/).", PhpUnit.XML_LOG));
        throw new TestRunException(cause);
    }

//
//    private void handleCodeCoverage() {
//        if (!isCoverageEnabled()) {
//            return;
//        }
//
//        CoverageVO coverage = new CoverageVO();
//        try {
//            PhpUnitCoverageLogParser.parse(new BufferedReader(new InputStreamReader(new FileInputStream(PhpUnit.COVERAGE_LOG), "UTF-8")), coverage);
//        } catch (FileNotFoundException ex) {
//            LOGGER.info(String.format("File %s not found. If there are no errors in PHPUnit output (verify in Output window), "
//                    + "please report an issue (http://www.netbeans.org/issues/).", PhpUnit.COVERAGE_LOG));
//            return;
//        } catch (IOException ex) {
//            LOGGER.log(Level.WARNING, null, ex);
//            return;
//        }
//        if (!PhpUnit.KEEP_LOGS) {
//            if (!PhpUnit.COVERAGE_LOG.delete()) {
//                LOGGER.log(Level.INFO, "Cannot delete code coverage log {0}", PhpUnit.COVERAGE_LOG);
//            }
//        }
//        if (info.allTests()) {
//            coverageProvider.setCoverage(coverage);
//        } else {
//            coverageProvider.updateCoverage(coverage);
//        }
//    }

}
