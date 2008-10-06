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
package org.netbeans.modules.maven;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.netbeans.modules.maven.indexer.api.RepositoryIndexer;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInfo;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Module install that checks if the local repo index shall be refreshed.
 * @author mkleint
 */
public class ModInstall extends ModuleInstall implements ErrorHandler, EntityResolver {

    private static int MILIS_IN_SEC = 1000;
    private static int MILIS_IN_MIN = MILIS_IN_SEC * 60;
    private transient PropertyChangeListener projectsListener;

    /*logger*/
    private static final Logger LOGGER =
            Logger.getLogger("org.netbeans.modules.maven.ModuleInstall");//NOI18N

    /** Creates a new instance of ModInstall */
    public ModInstall() {
    }

    @Override
    public void restored() {
        super.restored();
        try {
            Preferences prfs = NbPreferences.root().node("/org/netbeans/modules/maven/touchme");
            prfs.put("touch", "me");
            prfs.flush();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
        disableOldModules();

        projectsListener = new OpenProjectsListener();
        OpenProjects.getDefault().addPropertyChangeListener(projectsListener);
        final int freq = RepositoryPreferences.getInstance().getIndexUpdateFrequency();
        //#138102
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                List<RepositoryInfo> ris = RepositoryPreferences.getInstance().getRepositoryInfos();
                for (final RepositoryInfo ri : ris) {
                    //check this repo can be indexed
                    if (!ri.isRemoteDownloadable() && !ri.isLocal()) {
                        continue;
                    }
                    if (freq != RepositoryPreferences.FREQ_NEVER) {
                        boolean run = false;
                        if (freq == RepositoryPreferences.FREQ_STARTUP) {
                            LOGGER.finer("Index At Startup :" + ri.getId());//NOI18N
                            run = true;
                        } else if (freq == RepositoryPreferences.FREQ_ONCE_DAY && checkDiff(ri.getId(), 86400000L)) {
                            LOGGER.finer("Index Once a Day :" + ri.getId());//NOI18N
                            run = true;
                        } else if (freq == RepositoryPreferences.FREQ_ONCE_WEEK && checkDiff(ri.getId(), 604800000L)) {
                            LOGGER.finer("Index once a Week :" + ri.getId());//NOI18N
                            run = true;
                        }
                        if (run && ri.isRemoteDownloadable()) {
                            RepositoryIndexer.indexRepo(ri);
                        }
                    }
                }
            }
        }, MILIS_IN_MIN * 2);
    }

    private boolean checkDiff(String repoid, long amount) {
        Date date = RepositoryPreferences.getInstance().getLastIndexUpdate(repoid);
        Date now = new Date();
        LOGGER.finer("Check Date Diff :" + repoid);//NOI18N
        LOGGER.finer("Last Indexed Date :" + SimpleDateFormat.getInstance().format(date));//NOI18N
        LOGGER.finer("Now :" + SimpleDateFormat.getInstance().format(now));//NOI18N
        long diff = now.getTime() - date.getTime();
        LOGGER.finer("Diff :" + diff);//NOI18N
        return (diff < 0 || diff > amount);
    }

    @Override
    public void uninstalled() {
        super.uninstalled();
        if (projectsListener != null) {
            OpenProjects.getDefault().removePropertyChangeListener(projectsListener);
        }
    }

    private static class OpenProjectsListener implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            Project[] prjs = OpenProjects.getDefault().getOpenProjects();
            for (int i = 0; i < prjs.length; i++) {
                NbMavenProjectImpl mavProj = prjs[i].getLookup().lookup(NbMavenProjectImpl.class);
                if (mavProj != null) {
                    List repos = mavProj.getOriginalMavenProject().getRemoteArtifactRepositories();
                    if (repos != null) {
                        Iterator it = repos.iterator();
                        while (it.hasNext()) {
                            ArtifactRepository rep = (ArtifactRepository) it.next();
                            if (RepositoryPreferences.getInstance().
                                    getRepositoryInfoById(rep.getId()) == null) {
                                RepositoryInfo ri = new RepositoryInfo(rep.getId(),
                                        RepositoryPreferences.TYPE_NEXUS,
                                        rep.getId() + " " + NbBundle.getMessage(ModInstall.class, "LBL_REPOSITORY"),//NOI18N
                                        null, rep.getUrl(), null);
                                RepositoryPreferences.getInstance().addOrModifyRepositoryInfo(ri);
                            }
                        }
                    }
                }
            }

        }
    }

    private void disableOldModules() {
        Runnable runnable = new Runnable() {

            public void run() {
                final List<String> moduleNames = new ArrayList<String>();
                Collection<? extends ModuleInfo> all = Lookup.getDefault().lookupAll(ModuleInfo.class);
                for (ModuleInfo info : all) {
                    if (info.getCodeNameBase().startsWith("org.codehaus.mevenide")) { //NOI18N
                        moduleNames.add(info.getCodeNameBase());
                    }
                }
                try {
                    Repository.getDefault().getDefaultFileSystem().runAtomicAction(new FileSystem.AtomicAction() {

                        public void run() throws IOException {
                            boolean notified = false;
                            outter:
                            for (String newModule : moduleNames) {
                                FileLock lock = null;
                                OutputStream os = null;
                                try {
                                    String newModuleXML = "Modules/" + newModule.replace('.', '-') + ".xml"; // NOI18N
                                    FileObject fo = Repository.getDefault().getDefaultFileSystem().findResource(newModuleXML);
                                    if (fo == null) {
                                        continue;
                                    }
                                    Document document = readModuleDocument(fo);
                                    NodeList list = document.getDocumentElement().getElementsByTagName("param"); // NOI18N
                                    int n = list.getLength();
                                    boolean doNotify = false;
                                    for (int j = 0; j < n; j++) {
                                        Element node = (Element) list.item(j);
                                        if ("enabled".equals(node.getAttribute("name"))) {
                                            // NOI18N
                                            Text text = (Text) node.getChildNodes().item(0);
                                            String value = text.getNodeValue();
                                            if ("true".equals(value)) {
                                                // NOI18N
                                                doNotify = true;
                                                text.setNodeValue("false"); // NOI18N
                                                break;
                                            } else {
                                                continue outter;
                                            }
                                        }
                                    }
                                    if (doNotify && !notified) {
                                        NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(ModInstall.class, "MSG_Install_Warning"));
                                        nd.setTitle(NbBundle.getMessage(ModInstall.class, "MSG_Install_Warning_Title"));
                                        DialogDisplayer.getDefault().notify(nd);
                                        notified = true;
                                    }
                                    lock = fo.lock();
                                    os = fo.getOutputStream(lock);
                                    XMLUtil.write(document, os, "UTF-8"); // NOI18N
                                } catch (Exception e) {
                                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                                } finally {
                                    if (os != null) {
                                        try {
                                            os.close();
                                        } catch (IOException ex) {
                                        }
                                    }
                                    if (lock != null) {
                                        lock.releaseLock();
                                    }
                                }
                            }
                        }
                    });
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        RequestProcessor.getDefault().post(runnable);
    }

    private Document readModuleDocument(FileObject fo) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder parser = dbf.newDocumentBuilder();
        parser.setEntityResolver(this);
        parser.setErrorHandler(this);
        InputStream is = fo.getInputStream();
        Document document = parser.parse(is);
        is.close();
        return document;
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        return new InputSource(new ByteArrayInputStream(new byte[0]));
    }

    public void error(SAXParseException exception) {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exception);
    }

    public void fatalError(SAXParseException exception) {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exception);
    }

    public void warning(SAXParseException exception) {
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, exception);
    }
}
