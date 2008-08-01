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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.nbbuild;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Tries to validate a number of XML files
 * according to available XML schemas.
 * Ant's schemavalidate task is less flexible.
 * @author Jesse Glick
 */
public class ValidateAllBySchema extends Task {

    private List<FileSet> documents = new ArrayList<FileSet>();
    private List<FileSet> schemas = new ArrayList<FileSet>();
    private File report;

    public void addConfiguredDocuments(FileSet fs) {
        documents.add(fs);
    }

    public void addConfiguredSchemas(FileSet fs) {
        schemas.add(fs);
    }

    public void setReport(File r) {
        report = r;
    }

    public @Override void execute() throws BuildException {
        List<String> schemaUris = new ArrayList<String>();
        for (FileSet fs : schemas) {
            DirectoryScanner scanner = fs.getDirectoryScanner(getProject());
            File basedir = scanner.getBasedir();
            for (String file : scanner.getIncludedFiles()) {
                File f = new File(basedir, file);
                schemaUris.add(f.toURI().toString());
            }
        }
        log("Validating against " + schemaUris);
        SAXParser p;
        try {
            // XXX could also use javax.xml.validation.SchemaFactory, probably.
            // Using the JRE's version of Xerces does not seem to work with these parser properties,
            // for some reason, so use the Xerces original:
            SAXParserFactory factory = (SAXParserFactory) Class.forName("org.apache.xerces.jaxp.SAXParserFactoryImpl").newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(true);
            p = factory.newSAXParser();
            p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                          "http://www.w3.org/2001/XMLSchema");
            p.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource",
                          schemaUris.toArray(new String[0]));
        } catch (Exception x) {
            throw new BuildException(x, getLocation());
        }
        Map<String,String> tests = new TreeMap<String,String>();
        for (FileSet fs : documents) {
            DirectoryScanner scanner = fs.getDirectoryScanner(getProject());
            File basedir = scanner.getBasedir();
            for (String file : scanner.getIncludedFiles()) {
                File f = new File(basedir, file);
                log("Parsing: " + f);
                try {
                    p.parse(f.toURI().toString(), new Handler());
                    tests.put(file, null);
                } catch (SAXParseException x) {
                    String error = x.getSystemId() + ":" + x.getLineNumber() + ": " + x.getLocalizedMessage();
                    log(error, Project.MSG_ERR);
                    tests.put(file,error);
                } catch (Exception x) {
                    throw new BuildException("While parsing: " + f + " got: " + x, x, getLocation());
                }
            }
        }
        log("All files validated.");
        JUnitReportWriter.writeReport(this, report, tests);
    }

    private static final class Handler extends DefaultHandler {
        @Override
        public void warning(SAXParseException e) throws SAXException {
            throw e;
        }
        @Override
        public void error(SAXParseException e) throws SAXException {
            throw e;
        }
        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw e;
        }
    }

}
