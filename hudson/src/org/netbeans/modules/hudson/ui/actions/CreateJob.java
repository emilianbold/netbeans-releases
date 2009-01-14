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

package org.netbeans.modules.hudson.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.impl.HudsonInstanceImpl;
import org.netbeans.modules.hudson.spi.ProjectHudsonJobCreator;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Submenu action to create a job on this server from one of the open projects.
 */
public class CreateJob extends AbstractAction implements Presenter.Popup {

    private final HudsonInstanceImpl instance;

    public CreateJob(HudsonInstanceImpl instance) {
        this.instance = instance;
    }

    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    public JMenuItem getPopupPresenter() {
        return new PopupMenu();
    }

    private class PopupMenu extends JMenu implements DynamicMenuContent {

        public JComponent[] getMenuPresenters() {
            removeAll();
            boolean found = false;
            PROJECT: for (final Project p : OpenProjects.getDefault().getOpenProjects()) {
                // XXX skip projects which already have corresponding projects
                JMenuItem item = new JMenuItem(ProjectUtils.getInformation(p).getDisplayName());
                add(item);
                for (final ProjectHudsonJobCreator creator : Lookup.getDefault().lookupAll(ProjectHudsonJobCreator.class)) {
                    if (creator.canHandle(p)) {
                        item.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                RequestProcessor.getDefault().post(new Runnable() {
                                    public void run() {
                                        createJob(creator, p);
                                    }
                                });
                            }
                        });
                        found = true;
                        continue PROJECT;
                    }
                }
                item.setEnabled(false);
            }
            setEnabled(found);
            setText(found ? "Create Job from" : "Create Job");
            return new JComponent[] {this};
        }

        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return getMenuPresenters();
        }

    }

    private void createJob(ProjectHudsonJobCreator creator, Project p) {
        String name = creator.jobName(p);
        Set<String> takenNames = new HashSet<String>();
        for (HudsonJob job : instance.getJobs()) {
            takenNames.add(job.getName());
        }
        if (takenNames.contains(name)) {
            for (int i = 2; ; i++) {
                String _name = name + "_" + i; // NOI18N
                if (!takenNames.contains(_name)) {
                    name = _name;
                    break;
                }
            }
        }
        Document doc = XMLUtil.createDocument("project", null, null, null); // NOI18N
        try {
            creator.configure(p, doc);
            String createItemURL = instance.getUrl() + "createItem?name=" + URLEncoder.encode(name, "UTF-8"); // NOI18N
            HttpURLConnection conn = (HttpURLConnection) new URL(createItemURL).openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "text/xml");
            conn.connect();
            // XXX how do we pass authentication info? Where do we get it from?
            OutputStream os = conn.getOutputStream();
            try {
                XMLUtil.write(doc, os, "UTF-8"); // NOI18N
            } finally {
                os.close();
            }
            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("Server rejected creation of job " + name + " with response code " + responseCode); // NOI18N
            }
            URLDisplayer.getDefault().showURL(new URL(instance.getUrl() + "job/" +
                    URLEncoder.encode(name, "UTF-8") + "/configure")); // NOI18N
            instance.synchronize();
            // XXX remember that the new job corresponds to this project (see ProjectHudsonProvider)
        } catch (IOException x) {
            Exceptions.printStackTrace(x);
        }
    }

    @ServiceProvider(service=ProjectHudsonJobCreator.class)
    public static class DummyJobCreator implements ProjectHudsonJobCreator {
        public boolean canHandle(Project project) {
            return true;
        }
        public String jobName(Project project) {
            return ProjectUtils.getInformation(project).getName();
        }
        public void configure(Project project, Document configXml) throws IOException {
            configXml.getDocumentElement().
                    appendChild(configXml.createElement("builders")).
                    appendChild(configXml.createElement("hudson.tasks.Shell")).
                    appendChild(configXml.createElement("command")).
                    appendChild(configXml.createTextNode("echo 'XXX not yet implemented! Just for testing.'"));
            for (String dummy : new String[] {"actions", "publishers", "buildWrappers"}) {
                configXml.getDocumentElement().
                        appendChild(configXml.createElement(dummy));
            }
            ((Element) configXml.getDocumentElement().
                    appendChild(configXml.createElement("scm"))).
                    setAttribute("class", "hudson.scm.NullSCM");
        }
    }

}
