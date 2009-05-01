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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem.HudsonJobChangeFile;
import org.netbeans.modules.hudson.spi.HudsonJobChangeItem.HudsonJobChangeFile.EditType;
import org.netbeans.modules.hudson.spi.HudsonSCM;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.OutputListener;
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
            SvnUtils.Info info = SvnUtils.parseCheckout(folder.toURI().toURL());
            if (info == null) {
                return null;
            }
            final String urlS = info.module.toString();
            return new Configuration() {
                public void configure(Document doc) {
                    Element root = doc.getDocumentElement();
                    Element configXmlSCM = (Element) root.appendChild(doc.createElement("scm")); // NOI18N
                    configXmlSCM.setAttribute("class", "hudson.scm.SubversionSCM"); // NOI18N
                    Element loc = (Element) configXmlSCM.appendChild(doc.createElement("locations")). // NOI18N
                            appendChild(doc.createElement("hudson.scm.SubversionSCM_-ModuleLocation")); // NOI18N
                    loc.appendChild(doc.createElement("remote")).appendChild(doc.createTextNode(urlS)); // NOI18N
                    loc.appendChild(doc.createElement("local")).appendChild(doc.createTextNode(".")); // NOI18N
                    // HUDSON-3390 would be a more attractive alternative:
                    configXmlSCM.appendChild(doc.createElement("useUpdate")).appendChild(doc.createTextNode("false")); // NOI18N
                    Helper.addTrigger(doc);
                }
            };
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "inspecting configuration for " + folder, ex);
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public String translateWorkspacePath(HudsonJob job, String workspacePath, File localRoot) {
        try {
            SvnUtils.Info local = SvnUtils.parseCheckout(localRoot.toURI().toURL());
            if (local == null) {
                return null;
            }
            int slash = workspacePath.lastIndexOf('/');
            String workspaceDir = workspacePath.substring(0, slash + 1);
            String workspaceFile = workspacePath.substring(slash + 1);
            URL remoteCheckout = new URL(job.getUrl() + "ws/" + workspaceDir); // NOI18N
            SvnUtils.Info remote = SvnUtils.parseCheckout(remoteCheckout, job);
            if (remote == null) {
                LOG.log(Level.FINE, "no remote checkout found at {0}", remoteCheckout);
                return null;
            }
            // Example:
            // workspacePath     = trunk/myprj/nbproject/build-impl.xml
            // workspaceDir      = trunk/myprj/nbproject/
            // workspaceFile     = build-impl.xml
            // remoteCheckout    = http://my.build.server/hudson/job/myprj/ws/trunk/myprj/nbproject/
            // remote.repository = https://myprj.dev.java.net/svnroot/myprj
            // remote.module     = https://myprj.dev.java.net/svnroot/myprj/trunk/myprj/nbproject
            // local.repository  = https://myprj.dev.java.net/svnroot/myprj
            // local.module      = https://myprj.dev.java.net/svnroot/myprj/trunk/myprj
            // checkoutPath      = /svnroot/myprj/trunk/myprj/nbproject/build-impl.xml
            // infoURLPath       = /svnroot/myprj/trunk/myprj/
            // translatedPath    = nbproject/build-impl.xml
            if (!remote.repository.getPath().equals(local.repository.getPath())) {
                LOG.log(Level.FINE, "repository mismatch between {0} and {1}", new Object[] {remote.repository, local.repository});
                return null;
            }
            String remoteModule = new URL(remote.module + "/" + workspaceFile).getPath(); // NOI18N
            String localModuleBase = new URL(local.module + "/").getPath(); // NOI18N
            if (!remoteModule.startsWith(localModuleBase)) {
                LOG.log(Level.FINE, "checkout mismatch between {0} and {1}", new Object[] {localModuleBase, remoteModule});
                return null;
            }
            String translatedPath = remoteModule.substring(localModuleBase.length());
            LOG.log(Level.FINE, "translated path as {0}", translatedPath);
            return translatedPath;
        } catch (Exception x) {
            LOG.log(Level.FINE, "cannot translate path", x);
            return null;
        }
    }

    public List<? extends HudsonJobChangeItem> parseChangeSet(final HudsonJob job, final Element changeSet) {
        if (!"svn".equals(Helper.xpath("kind", changeSet))) { // NOI18N
            return null;
        }
        class SubversionItem implements HudsonJobChangeItem {
            final Element itemXML;
            SubversionItem(Element xml) {
                this.itemXML = xml;
            }
            public String getUser() {
                return Helper.xpath("user", itemXML); // NOI18N
            }
            public String getMessage() {
                return Helper.xpath("msg", itemXML); // NOI18N
            }
            public Collection<? extends HudsonJobChangeFile> getFiles() {
                class SubversionFile implements HudsonJobChangeFile {
                    final Element fileXML;
                    SubversionFile(Element xml) {
                        this.fileXML = xml;
                    }
                    public String getName() {
                        return Helper.xpath("file", fileXML); // NOI18N
                    }
                    public EditType getEditType() {
                        return EditType.valueOf(Helper.xpath("editType", fileXML)); // NOI18N
                    }
                    public OutputListener hyperlink() {
                        String module = Helper.xpath("revision/module", changeSet); // NOI18N
                        String rev = Helper.xpath("revision", itemXML); // NOI18N
                        if (module == null || !module.startsWith("http") || rev == null) { // NOI18N
                            return null;
                        }
                        int r = Integer.parseInt(rev);
                        String path = getName();
                        int startRev, endRev;
                        switch (getEditType()) {
                        case edit:
                            startRev = r - 1;
                            endRev = r;
                            break;
                        case add:
                            startRev = 0;
                            endRev = r;
                            break;
                        case delete:
                            startRev = r - 1;
                            endRev = 0;
                            break;
                        default:
                            throw new AssertionError();
                        }
                        return new SubversionHyperlink(module, path, startRev, endRev, job);
                    }
                }
                List<SubversionFile> files = new ArrayList<SubversionFile>();
                NodeList nl = itemXML.getElementsByTagName("path"); // NOI18N
                for (int i = 0; i < nl.getLength(); i++) {
                    files.add(new SubversionFile((Element) nl.item(i)));
                }
                return files;
            }
        }
        List<SubversionItem> items = new ArrayList<SubversionItem>();
        NodeList nl = changeSet.getElementsByTagName("item"); // NOI18N
        for (int i = 0; i < nl.getLength(); i++) {
            items.add(new SubversionItem((Element) nl.item(i)));
        }
        return items;
    }

}
