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
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.impl.HudsonInstanceImpl;
import org.netbeans.modules.hudson.spi.ProjectHudsonJobCreatorFactory;
import org.netbeans.modules.hudson.spi.ProjectHudsonJobCreatorFactory.ProjectHudsonJobCreator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Submenu action to create a job on this server from one of the open projects.
 */
public class CreateJob extends AbstractAction {

    private final HudsonInstanceImpl instance;

    public CreateJob(HudsonInstanceImpl instance) {
        super("Create Job..."); // XXX I18N
        this.instance = instance;
    }

    public void actionPerformed(ActionEvent e) {
        Set<String> takenNames = new HashSet<String>();
        for (HudsonJob job : instance.getJobs()) {
            takenNames.add(job.getName());
        }
        final CreateJobPanel panel = new CreateJobPanel();
        DialogDescriptor dd = new DialogDescriptor(panel, "New Continuous Build"); // XXX I18N
        panel.init(takenNames, dd);
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result == NotifyDescriptor.OK_OPTION) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    finalizeJob(panel.creator, panel.name());
                }
            });
        }
    }

    private void finalizeJob(ProjectHudsonJobCreator creator, String name) {
        Document doc = XMLUtil.createDocument("project", null, null, null); // NOI18N
        try {
            creator.configure(doc);
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
            URLDisplayer.getDefault().showURL(new URL(instance.getUrl() + "job/" + URLEncoder.encode(name, "UTF-8") + "/")); // NOI18N
            instance.synchronize();
            // XXX remember that the new job corresponds to this project (see ProjectHudsonProvider)
        } catch (IOException x) {
            Exceptions.printStackTrace(x);
        }
    }

    @ServiceProvider(service=ProjectHudsonJobCreatorFactory.class)
    public static class DummyJobCreator implements ProjectHudsonJobCreatorFactory {
        public ProjectHudsonJobCreator forProject(final Project project) {
            return new ProjectHudsonJobCreator() {
                public String jobName() {
                    return ProjectUtils.getInformation(project).getName();
                }
                private boolean valid = false;
                private final ChangeSupport cs = new ChangeSupport(this);
                public JComponent customizer() {
                    final JCheckBox checkbox = new JCheckBox("<XXX just for testing>");
                    checkbox.setSelected(false);
                    checkbox.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            valid = checkbox.isSelected();
                            cs.fireChange();
                        }
                    });
                    return checkbox;
                }
                public String error() {
                    return valid ? null : "Check the checkbox!";
                }
                public void addChangeListener(ChangeListener listener) {
                    cs.addChangeListener(listener);
                }
                public void removeChangeListener(ChangeListener listener) {
                    cs.removeChangeListener(listener);
                }
                public void configure(Document configXml) throws IOException {
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
            };
        }
    }

}
