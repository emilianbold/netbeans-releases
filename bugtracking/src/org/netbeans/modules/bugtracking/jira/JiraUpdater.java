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

package org.netbeans.modules.bugtracking.jira;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.BugtrackingController;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.spi.RepositoryUser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Notifies and eventually downloads a missing JIRA plugin from the Update Center
 * @author Tomas Stupka
 */
public class JiraUpdater {

    private static JiraUpdater instance;
    private JiraProxyConector connector;

    private JiraUpdater() {
    }

    public synchronized static JiraUpdater getInstance() {
        if(instance == null) {
            instance = new JiraUpdater();
        }
        return instance;
    }

    /**
     * Returns a fake {@link BugtrackingConnector} to be shown in the create
     * repository dialog. The repository controler panel notifies a the missing
     * JIRA plugin and comes with a button to donload it from the Update Center.
     *
     * @return
     */
    public BugtrackingConnector getConnector() {
        if(connector == null) {
            connector = new JiraProxyConector();
        }
        return connector;
    }

    /**
     * Download and install the JIRA plugin from the Update Center
     */
    public void downloadAndInstall() {
        final DownloadPlugin dp = new DownloadPlugin();
        dp.startDownload();
        
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
        final JButton download = new JButton(NbBundle.getMessage(DownloadPlugin.class, "CTL_Action_Download"));     // NOI18N
        JButton cancel = new JButton(NbBundle.getMessage(DownloadPlugin.class, "CTL_Action_Cancel"));   // NOI18N

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
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(JiraUpdater.class, "LBL_FakeJiraName");              // NOI18N
        }
        @Override
        public String getTooltip() {
            return NbBundle.getMessage(JiraUpdater.class, "LBL_FakeJiraNameTooltip");       // NOI18N
        }
        @Override
        public Repository createRepository() {
            return new JiraProxyRepository();
        }
        @Override
        public Repository[] getRepositories() {
            return new Repository[0];
        }
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public String getID() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Image getIcon() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private class JiraProxyRepository extends Repository {
        @Override
        public Image getIcon() {
            return null;
        }
        @Override
        public String getDisplayName() {
            throw new UnsupportedOperationException("Not supported yet.");      // NOI18N
        }
        @Override
        public String getTooltip() {
            throw new UnsupportedOperationException("Not supported yet.");      // NOI18N
        }
        @Override
        public String getID() {
            throw new UnsupportedOperationException("Not supported yet.");      // NOI18N
        }
        @Override
        public String getUrl() {
            throw new UnsupportedOperationException("Not supported yet.");      // NOI18N
        }
        @Override
        public Issue getIssue(String id) {
            throw new UnsupportedOperationException("Not supported yet.");      // NOI18N
        }
        @Override
        public void remove() { }
        @Override
        public BugtrackingController getController() {
            return new JiraProxyController();
        }
        @Override
        public Query createQuery() {
            throw new UnsupportedOperationException("Not supported yet.");      // NOI18N
        }
        @Override
        public Issue createIssue() {
            throw new UnsupportedOperationException("Not supported yet.");      // NOI18N
        }

        @Override
        public Query[] getQueries() {
            return new Query[0];
        }
        @Override
        public Collection<RepositoryUser> getUsers() {
            return Collections.EMPTY_LIST;
        }
        @Override
        public Issue[] simpleSearch(String criteria) {
            return new Issue[0];
        }
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
    }

    private class JiraProxyController extends BugtrackingController {
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
            JPanel panel = new JPanel();

            JLabel pane = new JLabel();
            pane.setText(NbBundle.getMessage(JiraUpdater.class, "MSG_NOT_YET_INSTALLED"));

            JButton downloadButton = new JButton();
            downloadButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    downloadAndInstall();
                }
            });
            
            org.openide.awt.Mnemonics.setLocalizedText(downloadButton, org.openide.util.NbBundle.getMessage(MissingJiraSupportPanel.class, "MissingJiraSupportPanel.downloadButton.text")); // NOI18N

            GroupLayout layout = new GroupLayout(panel);
            panel.setLayout(layout);
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

            return panel;
        }

    }
}
