/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.ruby.testrunner.ui;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.ruby.platform.execution.FileLocator;

/**
 * Represents a test session, i.e. a single run of a test suite.
 *
 * @author Erno Mononen
 */
public class TestSession {

    enum SessionType {

        TEST,
        DEBUG
    }
    private final List<Report.Testcase> testCases = new ArrayList<Report.Testcase>();
    private String suiteName;
    private final FileLocator fileLocator;

    public TestSession(FileLocator fileLocator) {
        this.fileLocator = fileLocator;
    }

    void setSuiteName(String suiteName) {
        this.testCases.clear();
        this.suiteName = suiteName;
    }

    void addTestCase(Report.Testcase testCase) {
        for (Report.Testcase each : testCases) {
            if (testCase.className.equals(each.className) 
                    && testCase.name.equals(each.name)) {
                return;
            }
        }
        testCases.add(testCase);
    }

    Report getReport() {
        Report report = new Report(suiteName, fileLocator);
        for (Report.Testcase testcase : testCases) {
            report.reportTest(testcase);
            report.totalTests += 1;
            if (testcase.trouble != null) {
                if (testcase.trouble.isError()) {
                    report.errors += 1;
                } else {
                    report.failures += 1;
                }
            } else {
                report.detectedPassedTests += 1;
            }
        }
        return report;
    }

    String getSuiteName() {
        return suiteName;
    }
    
}
