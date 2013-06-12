/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.bugtracking.jira;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.DelegatingConnector;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.team.ide.spi.IDEServices;
import org.netbeans.modules.bugtracking.spi.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Notifies and eventually downloads a missing JIRA plugin from the Update Center
 * @author Tomas Stupka
 */
public class JiraUpdater {

    private static final String JIRA_CNB = "org.netbeans.modules.jira";         // NOI18N
    
    private static JiraUpdater instance;

    
    private DelegatingConnector connector;

    private JiraUpdater() {
    }

    public synchronized static JiraUpdater getInstance() {
        if(instance == null) {
            instance = new JiraUpdater();
        }
        return instance;
    }

    public static boolean supportsDownload() {
        IDEServices ideServices = BugtrackingManager.getInstance().getIDEServices();
        return ideServices != null && ideServices.providesPluginUpdate();
    }
    
    /**
     * Returns a fake {@link BugtrackingConnector} to be shown in the create
     * repository dialog. The repository controller panel notifies a the missing
     * JIRA plugin and comes with a button to download it from the Update Center.
     *
     * @return
     */
    public synchronized DelegatingConnector getConnector() {
        if(connector == null) {
            JiraProxyConector jpc = new JiraProxyConector();
            return new DelegatingConnector(
                    jpc, 
                    "fake.jira.connector",                                              // NOI18N
                    NbBundle.getMessage(JiraUpdater.class, "LBL_FakeJiraName"),         // NOI18N
                    NbBundle.getMessage(JiraUpdater.class, "LBL_FakeJiraNameTooltip"),  // NOI18N
                    null);
        }
        return connector;
    }

    /**
     * Download and install the JIRA plugin from the Update Center
     */
    @NbBundle.Messages({"MSG_JiraPluginName=JIRA"})
    public void downloadAndInstall() {
        IDEServices ideServices = BugtrackingManager.getInstance().getIDEServices();
        if(ideServices != null) {
            IDEServices.Plugin plugin = ideServices.getPluginUpdates(JIRA_CNB, Bundle.MSG_JiraPluginName());
            if(plugin != null) {
                plugin.installOrUpdate();
            }
        }
    }

    /**
     * Notifies about the missing jira plugin and provides an option to choose
     * if it should be downloaded
     *
     * @param url if not null a hyperlink is shown in the dialog
     * 
     * @return true if the user pushes the Download button, otherwise false
     */
    public static boolean notifyJiraDownload(String url) {
        final JButton download = new JButton(NbBundle.getMessage(JiraUpdater.class, "CTL_Action_Download"));     // NOI18N
        JButton cancel = new JButton(NbBundle.getMessage(JiraUpdater.class, "CTL_Action_Cancel"));   // NOI18N

        URL openURL = null;
        if (url != null) {
            try {
                openURL = new URL(url);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        JPanel panel = createNotificationPanel(openURL);

        DialogDescriptor dd =
            new DialogDescriptor(
                panel,
                NbBundle.getMessage(JiraUpdater.class, "CTL_MissingJiraPlugin"),                    // NOI18N
                true,
                new Object[] {download, cancel},
                download,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(JiraUpdater.class),
                null);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                download.requestFocusInWindow();
            }
        });
        return DialogDisplayer.getDefault().notify(dd) == download;
    }

    private static JPanel createNotificationPanel(final URL url) {
        JPanel panel = new JPanel();

        JLabel msgLabel = new JLabel("<html>" + NbBundle.getMessage(JiraUpdater.class, "MSG_PROJECT_NEEDS_JIRA")); // NOI18N
        JButton linkButton = new org.netbeans.modules.bugtracking.util.LinkButton();
        org.openide.awt.Mnemonics.setLocalizedText(linkButton, NbBundle.getMessage(JiraUpdater.class, "MSG_PROJECT_NEEDS_JIRA_LINK")); // NOI18N
        if (url != null) {
            linkButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
                    if (displayer != null) {
                        displayer.showURL(url);
                    } else {
                        // XXX nice error message?
                        BugtrackingManager.LOG.warning("No URLDisplayer found.");             // NOI18N
                    }
                }
            });
        } else {
            linkButton.setVisible(false);
        }

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(msgLabel, GroupLayout.PREFERRED_SIZE, 470, Short.MAX_VALUE)
                    .addComponent(linkButton))
                .addContainerGap()
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(msgLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(linkButton)
                .addContainerGap(25, Short.MAX_VALUE)
        );

        return panel;
    }
    
    private class JiraProxyConector extends BugtrackingConnector {
        private BugtrackingFactory<Object, Object, Object> f = new BugtrackingFactory<Object, Object, Object>();
        @Override
        public Repository createRepository() {
            return f.createRepository(f, new JiraProxyRepositoryProvider(), null, null);
        }
        @Override
        public Repository createRepository(RepositoryInfo info) {
            throw new UnsupportedOperationException("Not supported yet.");      // NOI18N
        }
    }
    private class JiraProxyRepositoryProvider extends RepositoryProvider<Object,Object,Object> {
        @Override
        public Image getIcon(Object r) {
            return null;
        }
        @Override
        public RepositoryInfo getInfo(Object r) {
            return null;
        }
        @Override
        public Object[] getIssues(Object r, String... id) {
            throw new UnsupportedOperationException("Not supported yet.");      // NOI18N
        }
        @Override
        public void remove(Object r) { }
        @Override
        public RepositoryController getController(Object r) {
            return new JiraProxyController();
        }
        @Override
        public Object createIssue(Object r) {
            throw new UnsupportedOperationException("Not supported yet.");      // NOI18N
        }
        @Override
        public Object createQuery(Object r) {
            throw new UnsupportedOperationException("Not supported yet.");      // NOI18N
        }
        @Override
        public Collection<Object> getQueries(Object r) {
            return Collections.emptyList();
        }
        @Override
        public Collection<Object> simpleSearch(Object r, String criteria) {
            return Collections.emptyList();
        }
        @Override
        public void removePropertyChangeListener(Object r, PropertyChangeListener listener) {
            // do nothing
        }
        @Override
        public void addPropertyChangeListener(Object r, PropertyChangeListener listener) {
            // do nothing
        }
    }

    private class JiraProxyController implements RepositoryController {
        private JPanel panel;
        @Override
        public JComponent getComponent() {
            if(panel == null) {
                panel = createControllerPanel();
            }
            return panel;
        }
        @Override
        public HelpCtx getHelpCtx() {
            return new HelpCtx(this.getClass());
        }
        @Override
        public boolean isValid() {
            return false;
        }
        @Override
        public void applyChanges() throws IOException {

        }

        private JPanel createControllerPanel() {
            JPanel controllerPanel = new JPanel();

            JLabel pane = new JLabel();
            pane.setText(NbBundle.getMessage(JiraUpdater.class, "MSG_NOT_YET_INSTALLED")); // NOI18N

            JButton downloadButton = new JButton();
            downloadButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    downloadAndInstall();
                }
            });
            
            org.openide.awt.Mnemonics.setLocalizedText(downloadButton, org.openide.util.NbBundle.getMessage(JiraUpdater.class, "MissingJiraSupportPanel.downloadButton.text")); // NOI18N

            GroupLayout layout = new GroupLayout(controllerPanel);
            controllerPanel.setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(pane, GroupLayout.PREFERRED_SIZE, 100, Short.MAX_VALUE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(downloadButton))
            );
            layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(pane)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(downloadButton))
                .addContainerGap())
            );

            return controllerPanel;
        }

        @Override
        public void populate() {}

        @Override
        public String getErrorMessage() {
            return null;
        }

        @Override
        public void addChangeListener(ChangeListener l) {}

        @Override
        public void removeChangeListener(ChangeListener l) {}

    }
}
