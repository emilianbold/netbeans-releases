/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Translates XTest test results into the de facto standard XML report format used by JUnit.
 * The translated results can then be displayed by Hudson and other tools.
 */
public class XTest2JUnitResultFormatter extends Task {

    private List<FileSet> input = new ArrayList<FileSet>();
    /**
     * Add some XTest test result files.
     * Typically you will use a pattern such as:
     * test\results\testrun_*\testbag_*\xmlresults\suites\TEST-*.xml
     */
    public void addConfiguredInput(FileSet fs) {
        input.add(fs);
    }

    private File outputDir;
    /**
     * Directory in which to place JUnit-format XML result files.
     */
    public void setOutputDir(File d) {
        outputDir = d;
    }

    public @Override void execute() throws BuildException {
        for (FileSet fs : input) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File base = ds.getBasedir();
            for (String incl : ds.getIncludedFiles()) {
                File infile = new File(base, incl);
                try {
                    Document in = XMLUtil.parse(new InputSource(infile.toURI().toString()), false, false, null, null);
                    Document out = translate(in);
                    File outfile = new File(outputDir, infile.getName());
                    log("Writing: " + outfile);
                    OutputStream os = new FileOutputStream(outfile);
                    try {
                        XMLUtil.write(out, os);
                    } finally {
                        os.close();
                    }
                } catch (Exception x) {
                    throw new BuildException("Working on " + infile + ": " + x, x, getLocation());
                }
            }
        }
    }

    private static Document translate(Document in) {
        Document out = XMLUtil.createDocument("testsuite");
        Element testsuite = out.getDocumentElement();
        Element unitTestSuite = in.getDocumentElement();
        testsuite.setAttribute("name", unitTestSuite.getAttribute("name"));
        testsuite.setAttribute("tests", unitTestSuite.getAttribute("testsTotal"));
        testsuite.setAttribute("failures", unitTestSuite.getAttribute("testsFail"));
        testsuite.setAttribute("errors", unitTestSuite.getAttribute("testsError"));
        testsuite.setAttribute("time", Float.toString(Float.valueOf(unitTestSuite.getAttribute("time")) / 1000));
        /* Forget timestamp; anyway XTest seems to use localtime whereas JUnit uses UTC:
        testsuite.setAttribute("timestamp", unitTestSuite.getAttribute("timeStamp".replaceFirst("(\\d{4}-\\d{2}-\\d{2}) (\\d{2}:\\d{2}:\\d{2})\\.\\d{3}", "$1T$2")));
         */
        for (Element unitTestCase : XMLUtil.findSubElements(unitTestSuite)) {
            if (!unitTestCase.getTagName().equals("UnitTestCase")) {
                continue;
            }
            Element testcase = (Element) testsuite.appendChild(out.createElement("testcase"));
            testcase.setAttribute("classname", unitTestCase.getAttribute("class"));
            testcase.setAttribute("name", unitTestCase.getAttribute("name"));
            testcase.setAttribute("time", Float.toString(Float.valueOf(unitTestCase.getAttribute("time")) / 1000));
            String result = unitTestCase.getAttribute("result");
            if (result.equals("fail") || result.equals("error") || result.equals("unknown")) {
                Element failure = (Element) testcase.appendChild(out.createElement(result.equals("fail") ? "failure" : "error"));
                String message = unitTestCase.getAttribute("message");
                if (message.length() > 0) {
                    failure.setAttribute("message", message);
                }
                NodeList nl = unitTestCase.getChildNodes();
                boolean foundStack = false;
                for (int i = 0; i < nl.getLength(); i++) {
                    if (nl.item(i).getNodeType() == Node.CDATA_SECTION_NODE) {
                        String stack = ((CDATASection) nl.item(i)).getData();
                        Matcher m = Pattern.compile("^([a-zA-Z0-9_.]+)(:|$).*", Pattern.MULTILINE | Pattern.DOTALL).matcher(stack);
                        if (m.matches()) {
                            failure.setAttribute("type", m.group(1));
                        }
                        failure.appendChild(out.createTextNode(stack));
                        foundStack = true;
                    }
                }
                if (!foundStack) {
                    if (message.length() == 0) {
                        message = "(unknown error)";
                    }
                    failure.appendChild(out.createTextNode(message));
                }
            } else if (!result.equals("pass")) {
                throw new IllegalArgumentException("Unexpected result attribute: '" + result + "'");
            }
        }
        // skip <system-out>, <system-err>, <properties>
        return out;
    }

}
