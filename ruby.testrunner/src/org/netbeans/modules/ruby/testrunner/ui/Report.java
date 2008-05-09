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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.ruby.testrunner.ui;

import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.ruby.platform.execution.FileLocator;
import org.openide.filesystems.FileObject;

/**
 * Data structure (model) of results of test results.
 * The data are built by the {@link TestRecognizer}.
 * 
 * <i>This is a modified version for <code>o.n.m.junit.output.Report</code>.</i>
 *
 * @author  Marian Petras, Erno Mononen
 */
final class Report {

    File resultsDir;
    String suiteClassName;
    String classpath;
    Collection<FileObject> classpathSourceRoots;
    String[] outputStd;
    String[] outputErr;
    int totalTests;
    int failures;
    int errors;
    int elapsedTimeMillis;
    /**
     * number of recognized (by the parser) passed test reports
     */
    int detectedPassedTests;
    private Collection<Testcase> tests;
    private final FileLocator fileLocator;
    
    /**
     */
    Report(String suiteClassName, FileLocator fileLocator) {
        this.suiteClassName = suiteClassName;
        this.fileLocator = fileLocator;
        this.tests = new ArrayList<Testcase>(10);
    }

    public FileLocator getFileLocator() {
        return fileLocator;
    }
    
    /**
     */
    void reportTest(Testcase test) {
        
        //PENDING - should be synchronized
        tests.add(test);
        
        if (test.trouble == null) {
            detectedPassedTests++;
        }
    }
    
    /**
     */
    void update(Report report) {
        
        //PENDING - should be synchronized
        
        this.resultsDir = report.resultsDir;
        this.suiteClassName = report.suiteClassName;
        this.outputStd = report.outputStd;
        this.outputErr = report.outputErr;
        this.totalTests = report.totalTests;
        this.failures = report.failures;
        this.errors = report.errors;
        this.elapsedTimeMillis = report.elapsedTimeMillis;
        this.detectedPassedTests = report.detectedPassedTests;
        this.tests = report.tests;
    }
    
    /**
     */
    Collection<Testcase> getTests() {
        
        /*
         * May be called both from the EventDispatch thread and
         * from other threads!
         *
         * TestSuiteNodeChildren.setFiltered() ... EventDispatch thread
         * TestSuiteNodeChildren.addNotify() ... EventDispatch thread or else
         */
        
        //PENDING - should be synchronized
        if (tests.isEmpty()) {
            final Collection<Testcase> emptyList = Collections.emptyList();
            return emptyList;
        } else {
            return new ArrayList<Testcase>(tests);
        }
    }
    
    /**
     */
    boolean containsFailed() {
        assert EventQueue.isDispatchThread();
        
        /* Called from the EventDispatch thread */
        
        return (failures + errors) != 0;
    }
    
    /**
     */
    static final class Testcase {
        String className;
        String name;
        int timeMillis;
        Trouble trouble;
    }
    
    /**
     */
    static final class Trouble {
        
        static final String COMPARISON_FAILURE_JUNIT3
                = "junit.framework.ComparisonFailure";                  //NOI18N
        static final String COMPARISON_FAILURE_JUNIT4
                = "org.junit.ComparisonFailure";                        //NOI18N

        boolean error;
        String message;
        String exceptionClsName;
        String[] stackTrace;
        Trouble nestedTrouble;
        
        /**
         */
        Trouble(boolean error) {
            this.error = error;
        }
        
        /** */
        boolean isError() {
            return error;
        }

        /** */
        boolean isComparisonFailure() {
            return (exceptionClsName != null)
                   && (exceptionClsName.equals(COMPARISON_FAILURE_JUNIT3)
                       || exceptionClsName.equals(COMPARISON_FAILURE_JUNIT4));
        }

        /** */
        boolean isFakeError() {
            return error && isComparisonFailure();
        }
        
    }
    
}
