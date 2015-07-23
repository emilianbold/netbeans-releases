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

package org.netbeans.modules.jira.autoupdate;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import org.eclipse.core.runtime.CoreException;
import org.netbeans.modules.bugtracking.commons.AutoupdateSupport;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.jira.JiraConfig;
import org.netbeans.modules.jira.client.spi.JiraConnectorProvider;
import org.netbeans.modules.jira.client.spi.JiraConnectorSupport;
import org.netbeans.modules.jira.client.spi.JiraVersion;
import org.netbeans.modules.jira.repository.JiraConfiguration;
import org.netbeans.modules.jira.repository.JiraRepository;
import org.netbeans.modules.mylyn.util.BugtrackingCommand;
import org.netbeans.modules.team.ide.spi.SettingsServices;
import org.openide.awt.HtmlRenderer;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.WeakSet;

/**
 *
 * @author Tomas Stupka
 */
public class JiraAutoupdate {

    public static final JiraVersion SUPPORTED_JIRA_VERSION;
    private static JiraAutoupdate instance;
    static {
        String version = System.getProperty("netbeans.t9y.jira.supported.version"); // NOI18N
        SUPPORTED_JIRA_VERSION = JiraConnectorSupport.getInstance().getConnector().createJiraVersion(version != null ? version : "6.0"); // NOI18N
    }
    static final String JIRA_MODULE_CODE_NAME = "org.netbeans.modules.jira"; // NOI18N
    private static final Pattern VERSION_PATTERN = Pattern.compile("^.*version ((\\d+?\\.\\d+?\\.\\d+?)|(\\d+?\\.\\d+?)).*$"); // NOI18N
    
    private final Set<JiraRepository> repos = new WeakSet<>();
    
    private final AutoupdateSupport support = new AutoupdateSupport(new AutoupdateCallback(), JIRA_MODULE_CODE_NAME, NbBundle.getMessage(Jira.class, "LBL_ConnectorName"));
    private boolean connectorNotified;

    private JiraAutoupdate() { }

    public static JiraAutoupdate getInstance() {
        if(instance == null) {
            instance = new JiraAutoupdate();
        }
        return instance;
    }
    
    /**
     * Checks if the remote JIRA has a version higher then actually supported and if
     * an update is available on the UC.
     *
     * @param repository the repository to check the version for
     */
    public void checkAndNotify(JiraRepository repository) {
        repos.add(repository);
        support.checkAndNotify(repository.getUrl());

        if(JiraConnectorSupport.getActiveConnector() != JiraConnectorProvider.Type.XMLRPC) {
            return;
        }
        JiraVersion serverVersion = getSupportedServerVersion(repository);
        if(serverVersion == null) {
            return;
        }
        JiraVersion version50 = JiraConnectorSupport.getInstance().getConnector().createJiraVersion("5.0.0");
        if(serverVersion.compareTo(version50) >= 0) {
            askToChangeConnector();
        }         
    }
    
    public JiraVersion getSupportedServerVersion(final JiraRepository repository) {
        final String[] v = new String[1];
        BugtrackingCommand cmd = new BugtrackingCommand() {
            @Override
            public void execute() throws CoreException, IOException, MalformedURLException {
                JiraConfiguration conf = repository.getConfiguration();
                v[0] = conf.getServerVersion();
            }
        };
        repository.getExecutor().execute(cmd, false, false, false);
        if(cmd.hasFailed()) {
            return null; // be optimistic at this point
        }
        return JiraConnectorSupport.getInstance().getConnector().createJiraVersion(v[0]);
    }

    public AutoupdateSupport getAutoupdateSupport() {
        return support;
    }
    
    public boolean isSupportedVersion(JiraVersion version) {
        return version.compareTo(SUPPORTED_JIRA_VERSION) <= 0;
    }

    public JiraVersion getVersion(String desc) {
        Matcher m = VERSION_PATTERN.matcher(desc);
        if(m.matches()) {
            return JiraConnectorSupport.getInstance().getConnector().createJiraVersion(m.group(1)) ;
        }
        return null;
    }
    
    class AutoupdateCallback implements AutoupdateSupport.Callback {
        @Override
        public String getServerVersion(String url) {
            JiraRepository repository = null;
            for (JiraRepository r : repos) {
                if(r.getUrl().equals(url)) {
                    repository = r;
                }
            }
            assert repository != null;
            JiraVersion version = JiraAutoupdate.this.getSupportedServerVersion(repository);
            return version != null ? version.toString() : null;
        }

        @Override
        public boolean checkIfShouldDownload(String desc) {
            JiraVersion version = getVersion(desc);
            return version != null && SUPPORTED_JIRA_VERSION.compareTo(version) < 0;
        }

        @Override
        public boolean isSupportedVersion(String version) {
            return JiraAutoupdate.this.isSupportedVersion(JiraConnectorSupport.getInstance().getConnector().createJiraVersion(version));
        }
    };    
    
    @NbBundle.Messages({"CTL_Restart=Change Connector",
                        "CTL_OldVersion=You are accessing a JIRA server with a version higher than 5.0.",
                        "CTL_ClickHere=Click here to change your connector settings.",
                        "CTL_NeverAgain=Do not show this warning again."})
    private void askToChangeConnector() {
        if( connectorNotified || !JiraConfig.getInstance().showChangeConnectorWarning()) {
            return;
        }
        connectorNotified = true;
        
        final JButton[] btns = new JButton[2];
        btns[0] = createLink(Bundle.CTL_NeverAgain(), createDoNotShowListener(btns));   
        btns[1] = createLink(Bundle.CTL_NeverAgain(), createDoNotShowListener(btns));   
        
        NotificationDisplayer.getDefault().notify(Bundle.CTL_Restart(),
            ImageUtilities.loadImageIcon( "org/netbeans/modules/jira/resources/warning.gif", true ), 
            createPanel(btns[0]), 
            createPanel(btns[1]),
            NotificationDisplayer.Priority.HIGH, NotificationDisplayer.Category.INFO);
    }  

    protected ActionListener createDoNotShowListener(final JButton[] btns) {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JiraConfig.getInstance().stopShowingChangeConnectorWarning();
                for (JButton btn : btns) {
                    Container p = btn.getParent();
                    p.remove(btn);
                    p.repaint();
                }
            }
        };
    }  
    
    private JComponent createPanel(JButton doNotShowButton) {
        final SettingsServices settings = Lookup.getDefault().lookup(SettingsServices.class);
        ActionListener showSettings = null;
        if(settings != null && settings.providesOpenSection(SettingsServices.Section.TASKS)) {
            showSettings = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    settings.openSection(SettingsServices.Section.TASKS);
                }
            };
        }
        final JPanel result = new JPanel();
        result.setOpaque(false);
        result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
        result.add(new JLabel(Bundle.CTL_OldVersion()));
        result.add(createLink(Bundle.CTL_ClickHere(), showSettings));
        result.add(new JSeparator());
        result.add(doNotShowButton);
        return result;
    }

    private JButton createLink(String text, ActionListener a) {
        JButton btn = new HtmlButton(text);
        btn.setFocusable(false);
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.addActionListener(a);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private static class HtmlButton extends JButton {
        public HtmlButton(String text) {
            super(text);
        }
        @Override
        protected void paintComponent(Graphics g) {
            HtmlRenderer.renderString("<html><a>" + getText() + "</a></html>",  // NOI18N
                    g, 0, getBaseline(Integer.MAX_VALUE, getFont().getSize()),
                    Integer.MAX_VALUE, getFont().getSize(),
                    getFont(), getForeground(), HtmlRenderer.STYLE_CLIP, true);
        }
    }
}
