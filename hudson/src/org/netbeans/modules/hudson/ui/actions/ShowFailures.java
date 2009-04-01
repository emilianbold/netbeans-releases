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
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.api.HudsonJobBuild;
import org.netbeans.modules.hudson.api.HudsonMavenModuleBuild;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
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

    private final HudsonJob job;
    private final String url;
    private final String displayName;

    public ShowFailures(HudsonJobBuild build) {
        this(build.getJob(), build.getUrl(), build.getJob().getDisplayName() + " #" + build.getNumber()); // XXX I18N
    }

    public ShowFailures(HudsonMavenModuleBuild module) {
        this(module.getBuild().getJob(), module.getUrl(), module.getDisplayName() + " #" + module.getBuild().getNumber()); // XXX I18N
    }

    private ShowFailures(HudsonJob job, String url, String displayName) {
        this.job = job;
        this.url = url;
        this.displayName = displayName;
        putValue(NAME, "Show Test Failures"); // XXX I18N
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
                int insideCase;
                private void prepareOutput() {
                    if (io == null) {
                        String title = displayName + " Test Failures"; // XXX I18N
                        io = IOProvider.getDefault().getIO(title, new Action[0]);
                        io.select();
                    }
                }
                // XXX could collect and display other info, e.g. case/className or suite/name
                public @Override void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if (qName.matches("errorStackTrace|stdout|stderr")) { // NOI18N
                        buf = new StringBuilder();
                    } else if (qName.equals("case")) { // NOI18N
                        insideCase++;
                    }
                }
                public @Override void characters(char[] ch, int start, int length) throws SAXException {
                    if (buf != null) {
                        buf.append(ch, start, length);
                    }
                }
                public @Override void endElement(String uri, String localName, String qName) throws SAXException {
                    if (qName.equals("errorStackTrace") || (insideCase == 0 && qName.matches("stdout|stderr"))) { // NOI18N
                        prepareOutput();
                        OutputWriter w = qName.equals("stdout") ? io.getOut() : io.getErr();
                        for (String line : buf.toString().split("\r\n?|\n")) {
                            hyperlinker.handleLine(line, w);
                        }
                        buf = null;
                    } else if (qName.equals("case")) { // NOI18N
                        insideCase--;
                    }
                }
                public @Override void endDocument() throws SAXException {
                    if (io != null) {
                        io.getOut().close();
                        io.getErr().close();
                    }
                }
            });
            // Requires Hudson 1.281 or later:
            String u = url + "testReport/api/xml?xpath=//suite[case/errorStackTrace]&wrapper=failures"; // NOI18N
            InputSource source = new InputSource(new ConnectionBuilder().job(job).url(u).connection().getInputStream());
            source.setSystemId(u);
            parser.parse(source);
        } catch (FileNotFoundException x) {
            Toolkit.getDefaultToolkit().beep();
        } catch (Exception x) {
            Exceptions.printStackTrace(x);
        }
    }

}
