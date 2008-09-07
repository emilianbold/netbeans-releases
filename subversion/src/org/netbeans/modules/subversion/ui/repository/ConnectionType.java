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

package org.netbeans.modules.subversion.ui.repository;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.diff.options.AccessibleJFileChooser;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author tomas
 */
public abstract class ConnectionType implements ActionListener, DocumentListener, FocusListener {
    
    private final static String LOCAL_URL_HELP          = "file:///repository_path[@REV]";              // NOI18N
    private final static String HTTP_URL_HELP           = "http://hostname/repository_path[@REV]";      // NOI18N
    private final static String HTTPS_URL_HELP          = "https://hostname/repository_path[@REV]";     // NOI18N
    private final static String SVN_URL_HELP            = "svn://hostname/repository_path[@REV]";       // NOI18N
    private final static String SVN_SSH_URL_HELP        = "svn+{0}://hostname/repository_path[@REV]";   // NOI18N

    protected final Repository repository;
    private List<JTextField> selectOnFocusFields = null;

    public ConnectionType(Repository repository) {
        this.repository = repository;
    }

    abstract String getTip(String url);
    abstract JPanel getPanel();
    abstract void setEditable(boolean editable);
    protected void refresh(RepositoryConnection rc) { }
    protected void setEnabled(boolean enabled) { }
    protected void textChanged(Document d) { }
    protected void storeConfigValues() { }
    protected void onSelectedRepositoryChange(String urlString) { }
    protected void showHints(boolean b) { }
    protected void fillRC(RepositoryConnection editedrc) { }
    protected void updateVisibility(String selectedUrlString) { }
    boolean savePassword() { return true; }
    boolean isValid(RepositoryConnection rc) { return true; }

    protected void addSelectOnFocusFields(JTextField... txts) {
        if(selectOnFocusFields == null) {
            selectOnFocusFields = new ArrayList<JTextField>();
        }
        for (JTextField txt : txts) {
            selectOnFocusFields.add(txt);
        }
    }

    public void insertUpdate(DocumentEvent e) {
        textChanged(e);
    }

    public void removeUpdate(DocumentEvent e) {
        textChanged(e);
    }

    public void changedUpdate(DocumentEvent e) {
        textChanged(e);
    }

    private void textChanged(final DocumentEvent e) {
        // repost later to AWT otherwise it can deadlock because
        // the document is locked while firing event and we try
        // synchronously access its content from selected repository
        Runnable awt = new Runnable() {
            public void run() {
                textChanged(e.getDocument());
                repository.validateSvnUrl();
            }
        };
        SwingUtilities.invokeLater(awt);
    }

    public void actionPerformed(ActionEvent e) {

    }

    public void focusGained(FocusEvent focusEvent) {
        if(selectOnFocusFields == null) return;
        for (JTextField txt : selectOnFocusFields) {
            if(focusEvent.getSource()==txt) {
                txt.selectAll();
            }
        }
    }

    public void focusLost(FocusEvent focusEvent) {
        // do nothing
    }

    protected void onBrowse(JTextField txt) {
        File oldFile = FileUtil.normalizeFile(new File(txt.getText()));
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(Repository.class, "ACSD_BrowseCertFile"), oldFile); // NOI18N
        fileChooser.setDialogTitle(NbBundle.getMessage(Repository.class, "Browse_title"));                                            // NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.showDialog(this.getPanel(), NbBundle.getMessage(Repository.class, "OK_Button"));                                            // NOI18N
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            txt.setText(f.getAbsolutePath());
        }
    }

    static class FileUrl extends ConnectionType {
        private JPanel panel = new JPanel();
        public FileUrl(Repository repository) {
            super(repository);
        }
        @Override
        JPanel getPanel() {
            return panel;
        }
        @Override
        String getTip(String url) {
            return LOCAL_URL_HELP;
        }
        @Override
        void setEditable(boolean editable) {
        }
    }

    static class InvalidUrl extends FileUrl {
        public InvalidUrl(Repository repository) {
            super(repository);
        }
        @Override
        String getTip(String url) {
            return NbBundle.getMessage(Repository.class, "MSG_Repository_Url_Help", new Object [] { // NOI18N
                    LOCAL_URL_HELP, HTTP_URL_HELP, HTTPS_URL_HELP, SVN_URL_HELP, SVN_SSH_URL_HELP
                   });
        }
    }

    static class Http extends ConnectionType {

        private HttpPanel panel = new HttpPanel();

        public Http(Repository repository) {
            super(repository);
            panel.proxySettingsButton.addActionListener(this);
            panel.savePasswordCheckBox.addActionListener(this);

            addSelectOnFocusFields(panel.userPasswordField);
            panel.browseButton.addActionListener(this);

            panel.userPasswordField.getDocument().addDocumentListener(this);
            panel.certPasswordField.getDocument().addDocumentListener(this);
            panel.userPasswordField.addFocusListener(this);
            panel.certPasswordField.addFocusListener(this);

            panel.userTextField.getDocument().addDocumentListener(this);
            panel.certFileTextField.getDocument().addDocumentListener(this);
        }

        @Override
        JPanel getPanel() {
            return panel;
        }

        @Override
        protected void refresh(RepositoryConnection rc) {
            panel.userTextField.setText(rc.getUsername());
            panel.userPasswordField.setText(rc.getPassword());
            panel.savePasswordCheckBox.setSelected(rc.getSavePassword());
            panel.certFileTextField.setText(rc.getCertFile());
            panel.certPasswordField.setText(rc.getCertPassword());
        }

        @Override
        public void setEnabled(boolean enabled) {
            panel.proxySettingsButton.setEnabled(enabled);
            panel.userPasswordField.setEnabled(enabled);
            panel.userTextField.setEnabled(enabled);
            panel.savePasswordCheckBox.setEnabled(enabled);
            panel.certFileTextField.setEnabled(enabled);
            panel.certPasswordField.setEnabled(enabled);
            panel.browseButton.setEnabled(enabled);
        }

        @Override
        public void setEditable(boolean editable) {
            panel.userPasswordField.setEditable(editable);
            panel.userTextField.setEditable(editable);
            panel.proxySettingsButton.setEnabled(editable);
            panel.savePasswordCheckBox.setEnabled(editable);
            panel.certFileTextField.setEnabled(editable);
            panel.certPasswordField.setEnabled(editable);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == panel.proxySettingsButton) {
                onProxyConfiguration();
            } else if(e.getSource() == panel.savePasswordCheckBox) {
                onSavePasswordChange();
            } else if(e.getSource() == panel.browseButton) {
                onBrowse(panel.certFileTextField);
            } else {
                super.actionPerformed(e);
            }
        }

        private void onProxyConfiguration() {
            OptionsDisplayer.getDefault().open("General");              // NOI18N
        }

        private void onSavePasswordChange() {
            Runnable awt = new Runnable() {
                public void run() {
                    RepositoryConnection rc = repository.getSelectedRCIntern();
                    if (rc != null) {
                        rc.setSavePassword(panel.savePasswordCheckBox.isSelected());
                    }
                    repository.validateSvnUrl();
                }
            };
            SwingUtilities.invokeLater(awt);
        }

        @Override
        protected void storeConfigValues() {

        }

        @Override
        protected boolean savePassword() {
            return panel.savePasswordCheckBox.isSelected();
        }

        @Override
        public void onSelectedRepositoryChange(String urlString) {

        }

        @Override
        protected void textChanged(Document d) {
            if (d == panel.userTextField.getDocument()) {
                onUsernameChange(repository.getSelectedRCIntern());
            } else if (d == panel.userPasswordField.getDocument()) {
                onPasswordChange(repository.getSelectedRCIntern());
            } else if (d == panel.certFileTextField.getDocument()) {
                onCertFileChange(repository.getSelectedRCIntern());
            } else if (d == panel.certPasswordField.getDocument()) {
                onCertPasswordChange(repository.getSelectedRCIntern());
            }
        }
        
        private void onUsernameChange(RepositoryConnection rc) {
            if (rc != null) {
                rc.setUsername(panel.userTextField.getText());
            }
            repository.setValid(true, "");
        }

        private void onPasswordChange(RepositoryConnection rc) {
            if (rc != null) {
                rc.setPassword(new String(panel.userPasswordField.getPassword()));
            }
            repository.setValid(true, "");
        }

        private void onCertPasswordChange(RepositoryConnection rc) {
            if (rc != null) {
                rc.setCertPassword(new String(panel.certPasswordField.getPassword()));
            }
        }

        private void onCertFileChange(RepositoryConnection rc) {
            if (rc != null) {
                rc.setCertFile(panel.certFileTextField.getText());
            }
        }

        @Override
        protected void fillRC(RepositoryConnection editedrc) {
            editedrc.setUsername(panel.userTextField.getText());
            editedrc.setPassword(new String(panel.userPasswordField.getPassword()));
            editedrc.setSavePassword(panel.savePasswordCheckBox.isSelected());
            editedrc.setCertFile(panel.certFileTextField.getText());
            editedrc.setCertPassword(new String(panel.certPasswordField.getPassword()));
        }

        @Override
        String getTip(String url) {
            if(url.startsWith("http:")) {                             // NOI18N
                return HTTP_URL_HELP;
            } else if(url.startsWith("https:")) {                     // NOI18N
                return HTTPS_URL_HELP;
            } else if(url.startsWith("svn:")) {                       // NOI18N
                return SVN_URL_HELP;
            }
            return null;
        }

        @Override
        protected void updateVisibility(String url) {
            panel.sslPanel.setVisible(url.startsWith("https:"));
        }
    }

    static class SvnSSHCli extends ConnectionType {
        private org.netbeans.modules.subversion.ui.repository.SvnSSHCliPanel panel = new org.netbeans.modules.subversion.ui.repository.SvnSSHCliPanel();
        public SvnSSHCli(Repository repository) {
            super(repository);
            panel.tunnelCommandTextField.getDocument().addDocumentListener(this);
        }

        @Override
        JPanel getPanel() {
            return panel;
        }

        @Override
        protected void refresh(RepositoryConnection rc) {
            panel.tunnelCommandTextField.setText(rc.getExternalCommand());
        }

        @Override
        void setEditable(boolean editable) {
            panel.tunnelCommandTextField.setEditable(editable);
        }

        @Override
        protected void showHints(boolean b) {
            panel.tunnelHelpLabel.setVisible(b);
        }

        @Override
        protected void textChanged(Document d) {
            if (d == panel.tunnelCommandTextField.getDocument()) {
                onTunnelCommandChange(repository.getSelectedRCIntern());
            }
        }

        private void onTunnelCommandChange(RepositoryConnection rc) {
            if (rc != null) {
                rc.setExternalCommand(panel.tunnelCommandTextField.getText());
            }
        }

        @Override
        protected void storeConfigValues() {
            RepositoryConnection rc = repository.getSelectedRCIntern();
            if(rc == null) {
                return; // uups
            }

            try {
                SVNUrl repositoryUrl = rc.getSvnUrl();
                if(repositoryUrl.getProtocol().startsWith("svn+")) {
                    SvnConfigFiles.getInstance().setExternalCommand(getTunnelName(repositoryUrl.getProtocol()), panel.tunnelCommandTextField.getText());
                }
            } catch (MalformedURLException mue) {
                // should not happen
                Subversion.LOG.log(Level.INFO, null, mue);
            }
        }

        @Override
        protected boolean savePassword() {
            return true;
        }

        @Override
        protected boolean isValid(RepositoryConnection rc) {
            return !(rc.getUrl().startsWith("svn+") && panel.tunnelCommandTextField.getText().trim().equals(""));
        }

        @Override
        public void onSelectedRepositoryChange(String urlString) {
            if(urlString.startsWith("svn+")) {
                String tunnelName = getTunnelName(urlString).trim();
                if( panel.tunnelCommandTextField.getText().trim().equals("") &&
                    tunnelName != null &&
                    !tunnelName.equals("") )
                {
                    panel.tunnelCommandTextField.setText(SvnConfigFiles.getInstance().getExternalCommand(tunnelName));
                }
            }
        }

        @Override
        protected void fillRC(RepositoryConnection editedrc) {
            editedrc.setExternalCommand(panel.tunnelCommandTextField.getText());
        }

        @Override
        String getTip(String url) {
            String tunnelName = getTunnelName(url);
            return MessageFormat.format(SVN_SSH_URL_HELP, tunnelName).trim();
        }

        private String getTunnelName(String urlString) {
            int idx = urlString.indexOf(":", 4);
            if(idx < 0) {
                idx = urlString.length();
            }
            return urlString.substring(4, idx);
        }
    }

    static class SvnSSHJhl extends ConnectionType {
        private SvnSSHJhlPanel panel = new SvnSSHJhlPanel();
        public SvnSSHJhl(Repository repository) {
            super(repository);
            panel.passwordField.getDocument().addDocumentListener(this);
            panel.certPasswordField.getDocument().addDocumentListener(this);
            panel.certFileTextField.getDocument().addDocumentListener(this);
            panel.passwordField.addFocusListener(this);
            panel.certPasswordField.addFocusListener(this);
            panel.passwordRadioButton.addActionListener(this);
            panel.privateKeyRadioButton.addActionListener(this);
            panel.browseButton.addActionListener(this);
            addSelectOnFocusFields(panel.certPasswordField, panel.passwordField);
        }

        @Override
        JPanel getPanel() {
            return panel;
        }

        @Override
        protected void refresh(RepositoryConnection rc) {
            panel.passwordField.setText(rc.getPassword());
            panel.certPasswordField.setText(rc.getCertPassword());
            panel.certFileTextField.setText(rc.getCertFile());
        }

        @Override
        void setEditable(boolean editable) {
            panel.passwordField.setEditable(editable);
            panel.certPasswordField.setEditable(editable);
            panel.certFileTextField.setEditable(editable);
        }

        @Override
        protected void storeConfigValues() {
//            RepositoryConnection rc = repository.getSelectedRCIntern();
//            if(rc == null) {
//                return; // uups
//            }
//
//            try {
//                SVNUrl repositoryUrl = rc.getSvnUrl();
//                if(repositoryUrl.getProtocol().startsWith("svn+")) {
//                    SvnConfigFiles.getInstance().setExternalCommand(getTunnelName(repositoryUrl.getProtocol()), panel.tunnelCommandTextField.getText());
//                }
//            } catch (MalformedURLException mue) {
//                // should not happen
//                Subversion.LOG.log(Level.INFO, null, mue);
//            }
        }

        @Override
        protected boolean savePassword() {
            return true;
        }

        @Override
        protected void setEnabled(boolean b) {
            if(panel.passwordRadioButton.isSelected()) {
                panel.passwordField.setEnabled(b);
                panel.certPasswordField.setEnabled(false);
                panel.certFileTextField.setEnabled(false);
            } else if (panel.privateKeyRadioButton.isSelected()) {
                panel.passwordField.setEnabled(false);
                panel.certPasswordField.setEnabled(b);
                panel.certFileTextField.setEnabled(b);
            }
            panel.browseButton.setEnabled(b);
            panel.passwordRadioButton.setEnabled(b);
            panel.privateKeyRadioButton.setEnabled(b);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == panel.passwordRadioButton || 
               e.getSource() == panel.privateKeyRadioButton)
            {
                setEnabled(true);
            } else if (e.getSource() == panel.browseButton) {
                onBrowse(panel.certFileTextField);
            } else {
                super.actionPerformed(e);
            }
        }

        @Override
        public void onSelectedRepositoryChange(String urlString) {
//            if(urlString.startsWith("svn+")) {
//                String tunnelName = getTunnelName(urlString).trim();
//                if( panel.tunnelCommandTextField.getText().trim().equals("") &&
//                    tunnelName != null &&
//                    !tunnelName.equals("") )
//                {
//                    panel.tunnelCommandTextField.setText(SvnConfigFiles.getInstance().getExternalCommand(tunnelName));
//                }
//            }
        }

        @Override
        protected void fillRC(RepositoryConnection rc) {
            rc.setPassword(new String(panel.passwordField.getPassword()));
            rc.setCertPassword(new String(panel.certPasswordField.getPassword()));
            rc.setCertFile(panel.certFileTextField.getText());
        }

        @Override
        String getTip(String url) {
            String tunnelName = getTunnelName(url);
            return MessageFormat.format(SVN_SSH_URL_HELP, tunnelName).trim();
        }

        private String getTunnelName(String urlString) {
            int idx = urlString.indexOf(":", 4);
            if(idx < 0) {
                idx = urlString.length();
            }
            return urlString.substring(4, idx);
        }
    }
}
