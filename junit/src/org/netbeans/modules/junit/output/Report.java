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

package org.netbeans.modules.junit.output;

import java.awt.EventQueue;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import static java.util.logging.Level.FINER;

/**
 * Data structure (model) of results of JUnit task results.
 * The data are built by the {@link JUnitOutputReader}.
 *
 * @see  JUnitOutputReader
 * @author  Marian Petras
 */
final class Report {

    private final Logger LOG = Logger.getLogger(getClass().getName());

    static enum InfoSource {
        VERBOSE_MSG,
        TEST_REPORT,
        XML_FILE
    }

    File antScript;
    File resultsDir;
    String suiteClassName;
    String classpath;
    ClassPath platformSources;
    Collection<FileObject> classpathSourceRoots;
    String[] outputStd;
    String[] outputErr;
    int totalTests;
    int failures;
    int errors;
    int interruptedTests;
    int elapsedTimeMillis;
    /**
     * number of recognized (by the parser) passed test reports
     */
    int detectedPassedTests;
    private Collection<Testcase> tests;
    private boolean hasTestsFromVerboseMsgs = false;
    private boolean suiteFinished = false;
    
    /**
     */
    Report(String suiteClassName) {
        if (LOG.isLoggable(FINER)) {
            LOG.finer("<init>(" + suiteClassName + ')');                //NOI18N
        }
        this.suiteClassName = suiteClassName;
        this.antScript = antScript;
        this.tests = new ArrayList<Testcase>(10);
    }
    
    /**
     */
    void reportTest(Testcase test) {
        
        /* Called from the AntLogger thread */
        
        reportTest(test, InfoSource.TEST_REPORT);
    }

    void reportTest(Testcase test, final InfoSource source) {
        
        /* Called from the AntLogger thread */
        
        if (LOG.isLoggable(FINER)) {
            LOG.finer("reportTest("                                     //NOI18N
                      + (test.trouble == null ? "pass   "               //NOI18N
                                              : test.trouble.isError() ? "error  "  //NOI18N
                                                                       : "failure") //NOI18N
                      + ", name: " + test.name                          //NOI18N
                      + ", class: " + test.className                    //NOI18N
                      + ')');
        }

        boolean addToList = false;
        boolean updateAllStats = false;
        switch (source) {
            case VERBOSE_MSG:
                addToList = true;
                updateAllStats = true;
                hasTestsFromVerboseMsgs = true;
                break;
            case TEST_REPORT:
                addToList = !hasTestsFromVerboseMsgs
                            || (findTest(test.name, false) == null);
                break;
            case XML_FILE:
                addToList = true;
                break;
            default:
                assert false;
        }

        if (addToList) {
            //PENDING - should be synchronized
            tests.add(test);
        }
        if (test.trouble == null) {
            if (updateAllStats) {
                totalTests++;
            }
            if (test.timeMillis == Testcase.NOT_FINISHED_YET) {
                interruptedTests++;
            } else {
                detectedPassedTests++;
            }
        } else if (updateAllStats) {
            totalTests++;
            if (test.trouble.isError()) {
                errors++;
            } else {
                failures++;
            }
        }
    }

    /**
     * Finds a test having the given name in this {@code Report}.
     * If test of the given does not exist, a new {@code Testcase} is created
     * and is named after the given {@code name} parameter.
     *
     * @param  name  requested name of the test
     * @return  an existing test of the given name or a newly created
     *          {@code Testcase} if test of the given name did not exist
     */
    Testcase findTest(String name) {
        return findTest(name, true);
    }

    /**
     * Finds a test having the given name in this {@code Report}.
     * If parameter {@code create} is {@code true} and a test of the given
     * name did not exist, a new {@code Testcase} is created and is named
     * after the given {@code name} parameter.
     *
     * @param  name  requested name of the test
     * @param  create  whether a test should be created if it does not exist yet
     * @return  an existing test of the given name, or {@code null} if it does
     *          not exist and parameter {@code create} is {@code false},
     *          or a newly created {@code Testcase} if test of the given
     *          name did not exist and parameter {@code create} was {@code true}
     */
    private Testcase findTest(String name, boolean create) {
        if ((tests == null) || tests.isEmpty()) {
            return create ? new Testcase(name) : null;
        }

        for (Testcase test : tests) {
            if (name.equals(test.name)) {
                return test;
            }
        }
        return create ? new Testcase(name) : null;
    }

    /**
     */
    void markSuiteFinished() {
        suiteFinished = true;
    }

    /**
     */
    boolean isSuiteFinished() {
        return suiteFinished;
    }

    /**
     */
    boolean isSuiteInterrupted() {
        return !suiteFinished;
    }
    
    /**
     */
    void update(Report report) {
        
        /* Called from the AntLogger thread */
        
        //PENDING - should be synchronized
        
        //this.antScript = report.antScript;    - KEEP DISABLED!!!
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
        this.suiteFinished |= report.suiteFinished;
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
        static final int TIME_UNKNOWN = -1;
        static final int NOT_FINISHED_YET = -2;
        String className;
        String name;
        int timeMillis;
        Trouble trouble;

        Testcase() {}
        Testcase(String name) { this.name = name; }
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
    
    /**
     * Builds a source {@code ClassPath} for the given {@code Report}.
     *
     * @param  report  report to find the classpath for
     * @return  found classpath, or {@code null} if the classpath would be
     *          empty
     */
    ClassPath getSourceClassPath() {
        setClasspathSourceRoots();
        Collection<FileObject> srcRoots = classpathSourceRoots;
        if ((srcRoots == null) || srcRoots.isEmpty()) {
            return null;
        }

        FileObject[] srcRootsArr = new FileObject[srcRoots.size()];
        srcRoots.toArray(srcRootsArr);
        return ClassPathSupport.createClassPath(srcRootsArr);
    }

    /**
     * Finds source roots corresponding to the apparently active classpath
     * (as reported by logging from Ant when it runs the Java launcher
     * with -cp) and stores it in the current report.
     * <!-- copied from JavaAntLogger -->
     */
    void setClasspathSourceRoots() {

        /* Copied from JavaAntLogger */

        if (classpathSourceRoots != null) {      //already set
            return;
        }

        if (classpath == null) {
            return;
        }

        Collection<FileObject> sourceRoots = new LinkedHashSet<FileObject>();
        final StringTokenizer tok = new StringTokenizer(classpath,
                                                        File.pathSeparator);
        while (tok.hasMoreTokens()) {
            String binrootS = tok.nextToken();
            File f = FileUtil.normalizeFile(new File(binrootS));
            URL binroot;
            try {
                binroot = f.toURI().toURL();
            } catch (MalformedURLException e) {
                throw new AssertionError(e);
            }
            if (FileUtil.isArchiveFile(binroot)) {
                URL root = FileUtil.getArchiveRoot(binroot);
                if (root != null) {
                    binroot = root;
                }
            }
            FileObject[] someRoots = SourceForBinaryQuery
                                     .findSourceRoots(binroot).getRoots();
            if (someRoots.length == 0) {
                //do nothing
            } else if (someRoots.length == 1) {
                sourceRoots.add(someRoots[0]);
            } else {
                sourceRoots.addAll(Arrays.asList(someRoots));
            }
        }

        if (platformSources != null) {
            sourceRoots.addAll(Arrays.asList(platformSources.getRoots()));
        } else {
            // no platform found. use default one:
            JavaPlatform platform = JavaPlatform.getDefault();
            // in unit tests the default platform may be null:
            if (platform != null) {
                sourceRoots.addAll(
                        Arrays.asList(platform.getSourceFolders().getRoots()));
            }
        }
        classpathSourceRoots = sourceRoots;

        /*
         * The following fields are no longer necessary
         * once the source classpath is defined:
         */
        classpath = null;
        platformSources = null;
    }

}
