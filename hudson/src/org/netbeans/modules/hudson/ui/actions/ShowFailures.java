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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Action to display test failures.
 */
public class ShowFailures extends AbstractAction implements Runnable {

    private final HudsonJob job;
    private final int buildNumber;

    public ShowFailures(HudsonJob job, int buildNumber) {
        this.job = job;
        this.buildNumber = buildNumber;
        putValue(NAME, "Show Test Failures"); // XXX I18N
    }

    public void actionPerformed(ActionEvent e) {
        new RequestProcessor(job.toString() + buildNumber + "/testReport").post(this); // NOI18N
    }

    public void run() {
        String title = job.getDisplayName() + " #" + buildNumber + " Test Failures"; // XXX I18N
        InputOutput io = IOProvider.getDefault().getIO(title, new Action[0]);
        io.select();
        final OutputWriter w = io.getOut();
        try {
            XMLReader parser = XMLUtil.createXMLReader();
            parser.setContentHandler(new DefaultHandler() {
                StringBuilder buf;
                // XXX could collect and display other info, e.g. <className>.<name>
                public @Override void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if (qName.equals("errorStackTrace")) { // NOI18N
                        buf = new StringBuilder();
                    }
                }
                public @Override void characters(char[] ch, int start, int length) throws SAXException {
                    if (buf != null) {
                        buf.append(ch, start, length);
                    }
                }
                public @Override void endElement(String uri, String localName, String qName) throws SAXException {
                    if (qName.equals("errorStackTrace")) { // NOI18N
                        char[] cs = new char[buf.length()];
                        buf.getChars(0, cs.length, cs, 0);
                        // XXX would like to hyperlink the stack traces, but how? would need to know where source roots were
                        w.write(cs);
                        buf = null;
                    }
                }
            });
            parser.parse(job.getUrl() + buildNumber + "/testReport/api/xml"); // NOI18N
        } catch (Exception x) {
            Exceptions.printStackTrace(x);
        }
        w.close();
        io.getErr().close();
    }

}
