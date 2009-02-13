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

package org.netbeans.modules.hudson.subversion;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem.HudsonJobChangeFile;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem.HudsonJobChangeFile.EditType;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import org.netbeans.modules.subversion.client.parser.LocalSubversionException;
import org.netbeans.modules.subversion.client.parser.SvnWcParser;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Lets Hudson understand things about Subversion.
 */
@ServiceProvider(service=HudsonSCM.class, position=100)
public class HudsonSubversionSCM implements HudsonSCM {

    private static final Logger LOG = Logger.getLogger(HudsonSubversionSCM.class.getName());

    public Configuration forFolder(File folder) {
        try {
            ISVNInfo info = new SvnWcParser().getInfoFromWorkingCopy(folder);
            SVNUrl url = info.getUrl();
            if (url == null) {
                return null;
            }
            final String urlS = url.toString();
            return new Configuration() {
                public void configure(Document doc) {
                    Element root = doc.getDocumentElement();
                    Element configXmlSCM = (Element) root.appendChild(doc.createElement("scm"));
                    configXmlSCM.setAttribute("class", "hudson.scm.SubversionSCM");
                    Element loc = (Element) configXmlSCM.appendChild(doc.createElement("locations")).
                            appendChild(doc.createElement("hudson.scm.SubversionSCM_-ModuleLocation"));
                    loc.appendChild(doc.createElement("remote")).appendChild(doc.createTextNode(urlS));
                    loc.appendChild(doc.createElement("local")).appendChild(doc.createTextNode("."));
                    configXmlSCM.appendChild(doc.createElement("useUpdate")).appendChild(doc.createTextNode("true"));
                    root.appendChild(doc.createElement("triggers")). // XXX reuse existing <triggers> if found
                            appendChild(doc.createElement("hudson.triggers.SCMTrigger")).
                            appendChild(doc.createElement("spec")).
                            // XXX pretty arbitrary but seems like a decent first guess
                            appendChild(doc.createTextNode("@hourly"));
                }
            };
        } catch (LocalSubversionException ex) {
            LOG.log(Level.WARNING, "inspecting configuration for " + folder, ex);
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public String translateWorkspacePath(HudsonJob job, String workspacePath, File localRoot) {
        try {
            ISVNInfo info = new SvnWcParser().getInfoFromWorkingCopy(localRoot);
            if (info.getUrl() == null) {
                return null;
            }
            int slash = workspacePath.lastIndexOf('/');
            String workspaceDir = workspacePath.substring(0, slash + 1);
            String workspaceFile = workspacePath.substring(slash + 1);
            // XXX using SvnWcParser is impossible on a remote URL, so need to hardcode format here
            URL svnEntries = new URL(job.getUrl() + "ws/" + workspaceDir + ".svn/entries");
            InputStream is = svnEntries.openStream();
            String checkout, repository;
            try {
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                r.readLine(); // "8" or similar
                r.readLine(); // blank
                r.readLine(); // "dir"
                r.readLine(); // rev #
                checkout = r.readLine();
                repository = r.readLine();
            } finally {
                is.close();
            }
            // Example:
            // workspacePath   = trunk/myprj/nbproject/build-impl.xml
            // workspaceDir    = trunk/myprj/nbproject/
            // workspaceFile   = build-impl.xml
            // svnEntries      = http://my.build.server/hudson/job/myprj/ws/trunk/myprj/nbproject/.svn/entries
            // repository      = https://myprj.dev.java.net/svnroot/myprj
            // checkout        = https://myprj.dev.java.net/svnroot/myprj/trunk/myprj/nbproject
            // info.repository = https://myprj.dev.java.net/svnroot/myprj
            // info.url        = https://myprj.dev.java.net/svnroot/myprj/trunk/myprj
            // checkoutPath    = /svnroot/myprj/trunk/myprj/nbproject/build-impl.xml
            // infoURLPath     = /svnroot/myprj/trunk/myprj/
            // translatedPath  = nbproject/build-impl.xml
            if (!new URL(repository).getPath().equals(new URL(info.getRepository().toString()).getPath())) {
                LOG.log(Level.FINE, "repository mismatch between {0} and {1}", new Object[] {repository, info.getRepository()});
                return null;
            }
            String checkoutPath = new URL(checkout + "/" + workspaceFile).getPath();
            String infoURLPath = new URL(info.getUrl() + "/").getPath();
            if (!checkoutPath.startsWith(infoURLPath)) {
                LOG.log(Level.FINE, "checkout mismatch between {0} and {1}", new Object[] {infoURLPath, checkoutPath});
                return null;
            }
            String translatedPath = checkoutPath.substring(infoURLPath.length());
            LOG.log(Level.FINE, "translated path as {0}", translatedPath);
            return translatedPath;
        } catch (Exception x) {
            LOG.log(Level.FINE, "cannot translate path", x);
            return null;
        }
    }

    private static final XPath xpath = XPathFactory.newInstance().newXPath();
    private static String xpath(String expr, Element xml) {
        try {
            return xpath.evaluate(expr, xml);
        } catch (XPathExpressionException x) {
            LOG.log(Level.FINE, "cannot evaluate '" + expr + "'", x);
            return null;
        }
    }

    public List<? extends HudsonJobChangeItem> parseChangeSet(Element changeSet) {
        if (!"svn".equals(xpath("kind", changeSet))) {
            // Either a different SCM, or old Hudson.
            if (changeSet.getElementsByTagName("revision").getLength() == 0) {
                // A different SCM. This clause could be deleted assuming 1.284.
                return null;
            }
        }
        class SubversionItem implements HudsonJobChangeItem {
            final Element xml;
            SubversionItem(Element xml) {
                this.xml = xml;
            }
            public String getUser() {
                return xpath("user", xml);
            }
            public String getMessage() {
                return xpath("msg", xml);
            }
            public Collection<? extends HudsonJobChangeFile> getFiles() {
                class SubversionFile implements HudsonJobChangeFile {
                    final Element xml;
                    SubversionFile(Element xml) {
                        this.xml = xml;
                    }
                    public String getName() {
                        return xpath("file", xml);
                    }
                    public EditType getEditType() {
                        return EditType.valueOf(xpath("editType", xml));
                    }
                }
                List<SubversionFile> files = new ArrayList<SubversionFile>();
                NodeList nl = xml.getElementsByTagName("path");
                for (int i = 0; i < nl.getLength(); i++) {
                    files.add(new SubversionFile((Element) nl.item(i)));
                }
                return files;
            }
        }
        List<SubversionItem> items = new ArrayList<SubversionItem>();
        NodeList nl = changeSet.getElementsByTagName("item");
        for (int i = 0; i < nl.getLength(); i++) {
            items.add(new SubversionItem((Element) nl.item(i)));
        }
        return items;
    }

}
