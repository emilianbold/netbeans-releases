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

package org.netbeans.xtest.usertasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
                    Document in = parse(new InputSource(infile.toURI().toString()), false, false, null, null);
                    Document out = translate(in);
                    File outfile = new File(outputDir, infile.getName());
                    log("Writing: " + outfile);
                    OutputStream os = new FileOutputStream(outfile);
                    try {
                        write(out, os);
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
        Document out = createDocument("testsuite");
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
        for (Element unitTestCase : findSubElements(unitTestSuite)) {
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

    // The following copied from org.netbeans.nbbuild.XMLUtil:

    @SuppressWarnings("unchecked")
    private static final ThreadLocal<DocumentBuilder>[] builderTL = new ThreadLocal[4];
    static {
        for (int i = 0; i < 4; i++) {
            builderTL[i] = new ThreadLocal<DocumentBuilder>();
        }
    }
    private static Document parse (
            InputSource input,
            boolean validate,
            boolean namespaceAware,
            ErrorHandler errorHandler,
            EntityResolver entityResolver
        ) throws IOException, SAXException {

        int index = (validate ? 0 : 1) + (namespaceAware ? 0 : 2);
        DocumentBuilder builder = builderTL[index].get();
        if (builder == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(validate);
            factory.setNamespaceAware(namespaceAware);

            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                throw new SAXException(ex);
            }
            builderTL[index].set(builder);
        }

        if (errorHandler != null) {
            builder.setErrorHandler(errorHandler);
        }

        if (entityResolver != null) {
            builder.setEntityResolver(entityResolver);
        }

        return builder.parse(input);
    }

    private static Document createDocument(String rootQName) throws DOMException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            return factory.newDocumentBuilder().getDOMImplementation().createDocument(null, rootQName, null);
        } catch (ParserConfigurationException ex) {
            throw (DOMException)new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot create parser").initCause(ex); // NOI18N
        }
    }

    private static DOMImplementation getDOMImplementation() throws DOMException { //can be made public

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            return factory.newDocumentBuilder().getDOMImplementation();
        } catch (ParserConfigurationException ex) {
            throw (DOMException)new DOMException(DOMException.NOT_SUPPORTED_ERR, "Cannot create parser").initCause(ex); // NOI18N
        }
    }

    // Cf. org.openide.xml.XMLUtil.
    private static final String IDENTITY_XSLT_WITH_INDENT =
            "<xsl:stylesheet version='1.0' " + // NOI18N
            "xmlns:xsl='http://www.w3.org/1999/XSL/Transform' " + // NOI18N
            "xmlns:xalan='http://xml.apache.org/xslt' " + // NOI18N
            "exclude-result-prefixes='xalan'>" + // NOI18N
            "<xsl:output method='xml' indent='yes' xalan:indent-amount='4'/>" + // NOI18N
            "<xsl:template match='@*|node()'>" + // NOI18N
            "<xsl:copy>" + // NOI18N
            "<xsl:apply-templates select='@*|node()'/>" + // NOI18N
            "</xsl:copy>" + // NOI18N
            "</xsl:template>" + // NOI18N
            "</xsl:stylesheet>"; // NOI18N

    private static void write(Document doc, OutputStream out) throws IOException {
        // XXX note that this may fail to write out namespaces correctly if the document
        // is created with namespaces and no explicit prefixes; however no code in
        // this package is likely to be doing so
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer(
                    new StreamSource(new StringReader(IDENTITY_XSLT_WITH_INDENT)));
            DocumentType dt = doc.getDoctype();
            if (dt != null) {
                String pub = dt.getPublicId();
                if (pub != null) {
                    t.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, pub);
                }
                t.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dt.getSystemId());
            }
            t.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); // NOI18N
            Source source = new DOMSource(doc);
            Result result = new StreamResult(out);
            t.transform(source, result);
        } catch (Exception e) {
            throw (IOException)new IOException(e.toString()).initCause(e);
        } catch (TransformerFactoryConfigurationError e) {
            throw (IOException)new IOException(e.toString()).initCause(e);
        }
    }

    private static List<Element> findSubElements(Element parent) throws IllegalArgumentException {
        NodeList l = parent.getChildNodes();
        List<Element> elements = new ArrayList<Element>(l.getLength());
        for (int i = 0; i < l.getLength(); i++) {
            Node n = l.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                elements.add((Element)n);
            } else if (n.getNodeType() == Node.TEXT_NODE) {
                String text = ((Text)n).getNodeValue();
                if (text.trim().length() > 0) {
                    throw new IllegalArgumentException("non-ws text encountered in " + parent + ": " + text); // NOI18N
                }
            } else if (n.getNodeType() == Node.COMMENT_NODE) {
                // OK, ignore
            } else {
                throw new IllegalArgumentException("unexpected non-element child of " + parent + ": " + n); // NOI18N
            }
        }
        return elements;
    }

}
