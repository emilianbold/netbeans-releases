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
import org.netbeans.modules.php.project.ui.testrunner.TestSessionVO.TestCaseVO;
import org.netbeans.modules.php.project.ui.testrunner.TestSessionVO.TestSuiteVO;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parser of PHPUnit XML log files (version 3.x).
 * @author Tomas Mysik
 */
public final class PhpUnitLogParser extends DefaultHandler {
    enum Content { NONE, ERROR, FAILURE };
    private final XMLReader xmlReader;
    private final TestSessionVO testSession;
    private TestSuiteVO testSuite; // actual test suite
    private TestCaseVO testCase; // actual test case
    private Content content = Content.NONE;
    private boolean firstContent = true; // for error/failure: the 1st line is ignored
    private boolean stacktraceStarted = false; // for error/failure: flag for description/stacktrace
    private StringBuilder buffer = new StringBuilder(200); // for error/failure: buffer for message

    private PhpUnitLogParser(TestSessionVO testSession) throws SAXException {
        this.testSession = testSession;
        xmlReader = createXmlReader();
        xmlReader.setContentHandler(this);
    }

    static void parse(Reader reader, TestSessionVO testSession) {
        try {
            PhpUnitLogParser parser = new PhpUnitLogParser(testSession);
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
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("testsuite".equals(qName)) {
            processTestSuite(attributes);
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
        if ("testsuite".equals(qName)) {
            testSuite = null;
        } else if ("testcase".equals(qName)) {
            testCase = null;
        } else if ("failure".equals(qName)
                || "error".equals(qName)) {
            endTestContent();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        switch (content) {
            case FAILURE:
            case ERROR:
                String string = getString(ch, start, length);
                if (string == null) {
                    break;
                }
                if (!stacktraceStarted) {
                    buffer.append(string);
                } else {
                    assert testCase != null;
                    testCase.addStacktrace(NbBundle.getMessage(PhpUnitLogParser.class, "LBL_At", string.trim()));
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

    private void processTestSuite(Attributes attributes) {
        if (testSession.getTime() == -1
                && testSession.getTests() == -1) {
            // no active suite yet => set total/session info
            testSession.setTests(getTests(attributes));
            testSession.setTime(getTime(attributes));
        }

        String file = getFile(attributes);
        if (file != null) {
            // 'real' suite found
            assert testSuite == null;
            testSuite = new TestSuiteVO(
                    getName(attributes),
                    file,
                    getTime(attributes));
            testSession.addTestSuite(testSuite);
        }
    }

    private void processTestCase(Attributes attributes) {
        assert testSuite != null;
        assert testCase == null;
        testCase = new TestCaseVO(
                getName(attributes),
                getFile(attributes),
                getLine(attributes),
                getTime(attributes));
        testSuite.addTestCase(testCase);
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
            case ERROR:
                assert testCase != null;
                String message = buffer.toString().trim();
                if (content.equals(Content.FAILURE)) {
                    testCase.setFailure(message);
                } else if (content.equals(Content.ERROR)) {
                    testCase.setError(message);
                } else {
                    assert false : "Should not get here";
                }

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
