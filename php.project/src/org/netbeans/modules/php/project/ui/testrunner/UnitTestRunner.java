/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.testrunner;

import java.io.IOException;
import java.io.Reader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.testrunner.api.Manager;
import org.netbeans.modules.gsf.testrunner.api.Status;
import org.netbeans.modules.gsf.testrunner.api.TestSession;
import org.netbeans.modules.gsf.testrunner.api.TestSuite;
import org.netbeans.modules.gsf.testrunner.api.Testcase;
import org.netbeans.modules.gsf.testrunner.api.Trouble;
import org.netbeans.modules.php.project.ui.testrunner.TestSessionVO.TestSuiteVO;
import org.netbeans.modules.php.project.ui.testrunner.TestSessionVO.TestCaseVO;
import org.openide.util.Exceptions;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Test runner UI for PHP unit tests.
 * <p>
 * Currently, only PHPUnit is supported.
 * All the times are in milliseconds.
 * @author Tomas Mysik
 */
public final class UnitTestRunner {

    private UnitTestRunner() {
    }

    public static void run(Project project) {
        Manager manager = Manager.getInstance();
        final String suiteName = "my suite";

        TestSession testSession = new TestSession("my test session", project, TestSession.SessionType.TEST);
        manager.testStarted(testSession);
        manager.displaySuiteRunning(testSession, suiteName);

        TestSuite testSuite = new TestSuite(suiteName);
        testSession.addSuite(testSuite);

        Testcase testcase = new Testcase("PHPUnit", testSession);
        testcase.setName("test 1");
        testcase.setTimeMillis(1000);
        testcase.setStatus(Status.PASSED);
        testSession.addTestCase(testcase);
        testcase = new Testcase("PHPUnit", testSession);
        testcase.setName("test 2");
        testcase.setTimeMillis(1500);
        testcase.setStatus(Status.ERROR);
        Trouble trouble = new Trouble(true);
        trouble.setMessage("description of a trouble");
        trouble.setStackTrace(new String[] {"string 1", "string 2"});
        Trouble.ComparisonFailure failure = new Trouble.ComparisonFailure("abc\na", "abcd\na");
        trouble.setComparisonFailure(failure);
        testcase.setTrouble(trouble);
        testSession.addTestCase(testcase);

        manager.displayReport(testSession, testSession.getReport(2500));
        manager.displayOutput(testSession, "my error", true);

        manager.sessionFinished(testSession);
    }

    static final class PhpUnitLogParser extends DefaultHandler {
        enum Content { NONE, ERROR, FAILURE };
        private final XMLReader xmlReader;
        private final TestSessionVO testSession;
        private boolean firstTestSuite = true;
        private Content content = Content.NONE;
        private boolean firstContent = true; // for error/failure: the 1st line is ignored
        private boolean stacktraceStarted = false; // for error/failure: flag for description/stacktrace
        private StringBuilder buffer = new StringBuilder(200); // for failure: buffer for message

        private PhpUnitLogParser(TestSessionVO testSession) throws SAXException {
            this.testSession = testSession;
            xmlReader = createXmlReader();
            xmlReader.setContentHandler(this);
        }

        static PhpUnitLogParser parse(Reader reader, TestSessionVO testSession) {
            PhpUnitLogParser parser = null;
            try {
                parser = new PhpUnitLogParser(testSession);
                parser.xmlReader.parse(new InputSource(reader));
            } catch (SAXException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                assert false;
            } finally {
                try {
                    reader.close();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return parser;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if ("testsuite".equals(qName)) {
                if (firstTestSuite) {
                    processTestSession(attributes);
                    firstTestSuite = false;
                } else {
                    processTestSuite(attributes);
                }
            } else if ("testcase".equals(qName)) {
                processTestCase(attributes);
            } else if ("failure".equals(qName)) {
                startTestFailure(attributes);
            } else if ("error".equals(qName)) {
                startTestError(attributes);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if ("failure".equals(qName)
                    || "error".equals(qName)) {
                endTestContent();
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            TestCaseVO activeTestCase;
            String string;
            switch (content) {
                case FAILURE:
                    string = getString(ch, start, length);
                    if (string == null) {
                        break;
                    }
                    if (!stacktraceStarted) {
                        buffer.append(string);
                    } else {
                        activeTestCase = testSession.getActiveTestSuite().getActiveTestCase();
                        activeTestCase.addStacktrace(string.trim());
                    }
                    break;
                case ERROR:
                    string = getString(ch, start, length);
                    if (string == null) {
                        break;
                    }
                    activeTestCase = testSession.getActiveTestSuite().getActiveTestCase();
                    if (!stacktraceStarted) {
                        activeTestCase.setError(string.trim());
                    } else {
                        activeTestCase.addStacktrace(string.trim());
                    }
                    break;
            }
        }

        private String getString(char[] ch, int start, int length) {
            if (firstContent) {
                firstContent = false;
                return null;
            }
            String string = new String(ch, start, length);
            if (string.trim().length() == 0) {
                stacktraceStarted = true;
                return null;
            } else if (string.startsWith("\n\n")) { // NOI18N
                // at least one empty line in the begining but do not return
                stacktraceStarted = true;
            }
            return string;
        }

        private XMLReader createXmlReader() throws SAXException {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(false);

            try {
                return factory.newSAXParser().getXMLReader();
            } catch (ParserConfigurationException ex) {
                throw new SAXException("Cannot create parser satisfying configuration parameters", ex);
            }
        }

        private void processTestSession(Attributes attributes) {
            testSession.setTests(getTests(attributes));
            testSession.setTime(getTime(attributes));
        }

        private void processTestSuite(Attributes attributes) {
            TestSuiteVO testSuite = new TestSuiteVO(
                    getName(attributes),
                    getFile(attributes),
                    getTime(attributes));
            testSession.addTestSuite(testSuite);
        }

        private void processTestCase(Attributes attributes) {
            TestCaseVO testCase = new TestCaseVO(
                    getName(attributes),
                    getFile(attributes),
                    getLine(attributes),
                    getTime(attributes));
            testSession.getActiveTestSuite().addTestCase(testCase);
        }

        private void startTestError(Attributes attributes) {
            content = Content.ERROR;
        }

        private void startTestFailure(Attributes attributes) {
            content = Content.FAILURE;
        }

        private void endTestContent() {
            switch (content) {
                case FAILURE:
                    TestCaseVO activeTestCase = testSession.getActiveTestSuite().getActiveTestCase();
                    activeTestCase.setFailure(buffer.toString().trim());
                    buffer = new StringBuilder(200);
                    break;
            }
            firstContent = true;
            stacktraceStarted = false;
            content = Content.NONE;
        }

        private int getTests(Attributes attributes) {
            return getInt(attributes, "tests"); // NOI18N
        }

        private long getTime(Attributes attributes) {
            long l = -1;
            try {
                l = Math.round(Double.parseDouble(attributes.getValue("time")) * 1000); // NOI18N
            } catch (NumberFormatException exc) {
                // ignored
            }
            return l;
        }

        private String getName(Attributes attributes) {
            return attributes.getValue("name"); // NOI18N
        }

        private String getFile(Attributes attributes) {
            return attributes.getValue("file"); // NOI18N
        }

        private int getLine(Attributes attributes) {
            return getInt(attributes, "line"); // NOI18N
        }

        private int getInt(Attributes attributes, String name) {
            int i = -1;
            try {
                i = Integer.parseInt(attributes.getValue(name));
            } catch (NumberFormatException exc) {
                // ignored
            }
            return i;
        }
    }
}
