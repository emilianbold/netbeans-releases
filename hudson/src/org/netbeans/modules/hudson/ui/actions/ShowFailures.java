/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.hudson.ui.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputEvent;
import org.openide.windows.OutputListener;
import org.openide.windows.OutputWriter;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Action to display test failures.
 */
public class ShowFailures extends AbstractAction implements Runnable {

    private static final Logger LOG = Logger.getLogger(ShowFailures.class.getName());
    
    private final HudsonJob job;
    private final String url;
    private final String displayName;

    public ShowFailures(HudsonJobBuild build) {
        this(build.getJob(), build.getUrl(), build.getDisplayName());
    }

    public ShowFailures(HudsonMavenModuleBuild module) {
        this(module.getBuild().getJob(), module.getUrl(), module.getBuildDisplayName());
    }

    private ShowFailures(HudsonJob job, String url, String displayName) {
        this.job = job;
        this.url = url;
        this.displayName = displayName;
        putValue(NAME, NbBundle.getMessage(ShowFailures.class, "ShowFailures.label"));
    }

    public void actionPerformed(ActionEvent e) {
        new RequestProcessor(url + "testReport").post(this); // NOI18N
    }

    public void run() {
        try {
            XMLReader parser = XMLUtil.createXMLReader();
            parser.setContentHandler(new DefaultHandler() {
                InputOutput io;
                StringBuilder buf;
                Hyperlinker hyperlinker = new Hyperlinker(job);
                private void prepareOutput() {
                    if (io == null) {
                        String title = NbBundle.getMessage(ShowFailures.class, "ShowFailures.title", displayName);
                        io = IOProvider.getDefault().getIO(title, new Action[0]);
                        io.select();
                    }
                }
                class Suite {
                    String name;
                    String stdout;
                    String stderr;
                    Stack<Case> cases = new Stack<Case>();
                    List<Case> casesDone = new ArrayList<Case>();
                }
                class Case {
                    String className;
                    String name;
                    String errorStackTrace;
                }
                Stack<Suite> suites = new Stack<Suite>();
                public @Override void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if (qName.matches("errorStackTrace|stdout|stderr|name|className")) { // NOI18N
                        buf = new StringBuilder();
                    } else if (qName.equals("suite")) { // NOI18N
                        suites.push(new Suite());
                    } else if (qName.equals("case") && !suites.empty()) { // NOI18N
                        suites.peek().cases.push(new Case());
                    }
                }
                public @Override void characters(char[] ch, int start, int length) throws SAXException {
                    if (buf != null) {
                        buf.append(ch, start, length);
                    }
                }
                public @Override void endElement(String uri, String localName, String qName) throws SAXException {
                    if (suites.empty()) {
                        return;
                    }
                    Suite s = suites.peek();
                    String text = buf != null && buf.length() > 0 ? buf.toString() : null;
                    buf = null;
                    if (s.cases.empty()) { // suite level
                        if (qName.equals("stdout")) { // NOI18N
                            s.stdout = text;
                        } else if (qName.equals("stderr")) { // NOI18N
                            s.stderr = text;
                        } else if (qName.equals("name")) { // NOI18N
                            s.name = text;
                        }
                    } else { // case level
                        Case c = s.cases.peek();
                        if (qName.equals("errorStackTrace")) { // NOI18N
                            c.errorStackTrace = text;
                        } else if (qName.equals("name")) { // NOI18N
                            c.name = text;
                        } else if (qName.equals("className")) { // NOI18N
                            c.className = text;
                        }
                    }
                    if (qName.equals("suite")) { // NOI18N
                        try {
                            show(s);
                        } catch (IOException x) {
                            LOG.log(Level.FINE, null, x);
                        }
                        suites.pop();
                    } else if (qName.equals("case")) { // NOI18N
                        s.casesDone.add(s.cases.pop());
                    }
                }
                void show(Suite s) throws IOException {
                    prepareOutput();
                    OutputWriter out = io.getOut();
                    OutputWriter err = io.getErr();
                    for (final Case c : s.casesDone) {
                        if (c.errorStackTrace == null) {
                            continue;
                        }
                        String name = c.className + "." + c.name;
                        if (s.name != null && !s.name.equals(c.className)) {
                            name = NbBundle.getMessage(ShowFailures.class, "ShowFailures.from_suite", name, s.name);
                        }
                        println();
                        out.println(name, new OutputListener() {
                            public void outputLineAction(OutputEvent ev) {
                                try {
                                    URLDisplayer.getDefault().showURL(new URL(url +
                                            "testReport/" + c.className.replaceFirst("[.][^.]+$", "") + "/" + // NOI18N
                                            c.className.replaceFirst(".+[.]", "") + "/" + c.name + "/")); // NOI18Nb
                                } catch (MalformedURLException x) {
                                    LOG.log(Level.FINE, null, x);
                                }
                            }
                            public void outputLineSelected(OutputEvent ev) {}
                            public void outputLineCleared(OutputEvent ev) {}
                        });
                        show(c.errorStackTrace, /* err is too hard to read */ out);
                    }
                    if (s.stderr != null || s.stdout != null) {
                        println();
                        show(s.stderr, err);
                        show(s.stdout, out);
                    }
                }
                boolean firstLine = true;
                void println() {
                    if (firstLine) {
                        firstLine = false;
                    } else {
                        io.getOut().println();
                    }
                }
                void show(String lines, OutputWriter w) {
                    if (lines == null) {
                        return;
                    }
                    for (String line : lines.split("\r\n?|\n")) { // NOI18N
                        hyperlinker.handleLine(line, w);
                    }
                }
                public @Override void endDocument() throws SAXException {
                    if (io != null) {
                        io.getOut().close();
                        io.getErr().close();
                    }
                }
            });
            String u = url + "testReport/api/xml?xpath=//suite[case/errorStackTrace]&wrapper=failures"; // NOI18N
            InputSource source = new InputSource(new ConnectionBuilder().job(job).url(u).connection().getInputStream());
            source.setSystemId(u);
            parser.parse(source);
        } catch (FileNotFoundException x) {
            Toolkit.getDefaultToolkit().beep();
        } catch (Exception x) {
            Toolkit.getDefaultToolkit().beep();
            LOG.log(Level.INFO, null, x);
        }
    }

}
