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
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

// XXX use from VerifyUpdateCenter when in trunk

/**
 * Utility permitting Ant tasks to write out JUnit-format reports rather than aborting the build.
 */
public class JUnitReportWriter {

    private JUnitReportWriter() {}

    /**
     * Possibly write out a report.
     * @param task the Ant task doing the work (will be used as a class name for the "test", among other purposes)
     * @param reportFile an XML file to create with the report; if null, and there were some failures,
     *                   throw a {@link BuildException} instead
     * @param pseudoTests the results of the "tests", as a map from test name (e.g. <samp>testSomething</samp>)
     *        to either null (success) or a (possibly multiline) failure message;
     *        use of a {@link java.util.LinkedHashMap} to preserve order is recommended
     * @throws BuildException in case <code>reportFile</code> was null
     *                        and <code>pseudoTests</code> contained some non-null values
     */
    public static void writeReport(Task task, File reportFile, Map<String,String> pseudoTests) throws BuildException {
        if (reportFile == null) {
            StringBuilder errors = new StringBuilder();
            for (Map.Entry<String,String> entry : pseudoTests.entrySet()) {
                String msg = entry.getValue();
                if (msg != null) {
                    errors.append("\n" + entry.getKey() + ": " + msg);
                }
            }
            if (errors.length() > 0) {
                throw new BuildException("Some tests failed:" + errors, task.getLocation());
            }
        } else {
            Document reportDoc = XMLUtil.createDocument("testsuite");
            Element testsuite = reportDoc.getDocumentElement();
            int failures = 0;
            testsuite.setAttribute("errors", "0");
            testsuite.setAttribute("time", "0.0");
            for (Map.Entry<String,String> entry : pseudoTests.entrySet()) {
                Element testcase = reportDoc.createElement("testcase");
                testsuite.appendChild(testcase);
                testcase.setAttribute("classname", task.getClass().getName());
                testcase.setAttribute("name", entry.getKey());
                testcase.setAttribute("time", "0.0");
                String msg = entry.getValue();
                if (msg != null) {
                    failures++;
                    Element failure = reportDoc.createElement("failure");
                    testcase.appendChild(failure);
                    failure.setAttribute("type", "junit.framework.AssertionFailedError");
                    failure.setAttribute("message", msg.replaceFirst("(?s)\n.*", ""));
                    failure.appendChild(reportDoc.createTextNode(msg));
                }
            }
            testsuite.setAttribute("failures", Integer.toString(failures));
            testsuite.setAttribute("tests", Integer.toString(pseudoTests.size()));
            try {
                OutputStream os = new FileOutputStream(reportFile);
                try {
                    XMLUtil.write(reportDoc, os);
                } finally {
                    os.close();
                }
            } catch (IOException x) {
                throw new BuildException("Could not write " + reportFile + ": " + x, x, task.getLocation());
            }
            task.log(reportFile + ": " + failures + " failures out of " + pseudoTests.size() + " tests");
        }
    }

}
