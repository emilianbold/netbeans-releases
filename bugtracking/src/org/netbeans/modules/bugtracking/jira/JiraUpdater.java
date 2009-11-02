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

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import org.jdesktop.layout.GroupLayout;
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
     * Determines if the jira plugin is instaled or not
     *
     * @return true if jira plugin is installed, otherwise false
     */
    public static boolean isJiraInstalled() {
        BugtrackingConnector[] connectors = BugtrackingManager.getInstance().getConnectors();
        for (BugtrackingConnector c : connectors) {
            // XXX hack
            if(c.getClass().getName().startsWith("org.netbeans.modules.jira")) {    // NOI18N
                return true;
            }
        }
        return false;
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
        JButton download = new JButton(NbBundle.getMessage(DownloadPlugin.class, "CTL_Action_Download"));     // NOI18N
        JButton cancel = new JButton(NbBundle.getMessage(DownloadPlugin.class, "CTL_Action_Cancel"));   // NOI18N

        String msg = NbBundle.getMessage(FakeJiraSupport.class, "MSG_PROJECT_NEEDS_JIRA");              // NOI18N
        if(url != null) {
            msg += "<div><br>" + NbBundle.getMessage(FakeJiraSupport.class, "MSG_PROJECT_NEEDS_JIRA_URL", url);// NOI18N
        }
        msg = "<html>" + msg + "</body>";                                       // NOI18N                                                             // NOI18N
        
        JPanel panel = createNotificationPanel(msg, new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if(e.getEventType() != HyperlinkEvent.EventType.ACTIVATED) {
                    return;
                }
                HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
                if (displayer != null) {
                    displayer.showURL(e.getURL());
                } else {
                    // XXX nice error message?
                    BugtrackingManager.LOG.warning("No URLDisplayer found.");             // NOI18N
                }
            }
        });

        final DialogDescriptor dd =
            new DialogDescriptor(
                panel,
                NbBundle.getMessage(FakeJiraSupport.class, "CTL_MissingJiraPlugin"),                    // NOI18N
                true,
                new Object[] {download, cancel},
                download,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(JiraUpdater.class),
                null);
        return DialogDisplayer.getDefault().notify(dd) == download;
    }

    private static JPanel createNotificationPanel(String msg, HyperlinkListener hl) {
        JPanel panel = new JPanel();

        JTextPane pane = new JTextPane();
        pane.setBackground(panel.getBackground());
        pane.setContentType("text/html"); // NOI18N
        pane.setText(msg);
        pane.setEditable(false);
        pane.addHyperlinkListener(hl);
        
        panel.setPreferredSize(new Dimension(650, 100));
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup().addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(pane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup().addContainerGap()
                .add(pane)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE))
                .addContainerGap(60, Short.MAX_VALUE))
        );

        return panel;
    }
    
    private class JiraProxyConector extends BugtrackingConnector {
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(FakeJiraSupport.class, "LBL_FakeJiraName");              // NOI18N
        }
        @Override
        public String getTooltip() {
            return NbBundle.getMessage(FakeJiraSupport.class, "LBL_FakeJiraNameTooltip");       // NOI18N
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
            pane.setText(NbBundle.getMessage(FakeJiraSupport.class, "MSG_NOT_YET_INSTALLED"));

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
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(pane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 100, Short.MAX_VALUE))
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(downloadButton))
            );
            layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(pane)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(downloadButton))
                .addContainerGap())
            );

            return panel;
        }

    }
}
