/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.git.ui.repository.remote;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.libs.git.utils.GitURI;
import org.netbeans.modules.git.Git;
import org.netbeans.modules.git.GitModuleConfig;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel.Message;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class RemoteRepository implements DocumentListener, ActionListener, ItemListener {
    private boolean valid;
    private Message msg;

    private ChangeSupport support = new ChangeSupport(this);
    private final boolean urlFixed;

    private enum Scheme {
        FILE("file", "file:///path/to/repo.git/  or  /path/to/repo.git/"),      // NOI18N
        HTTP("http", "http[s]://host.xz[:port]/path/to/repo.git/"),             // NOI18N
        HTTPS("https", "http[s]://host.xz[:port]/path/to/repo.git/"),           // NOI18N
//        FTP("ftp", "ftp[s]://host.xz[:port]/path/to/repo.git/"),                // NOI18N
//        FTPS("ftps", "ftp[s]://host.xz[:port]/path/to/repo.git/"),              // NOI18N
        SSH("ssh", "ssh://host.xz[:port]/path/to/repo.git/"),                   // NOI18N    
        SFTP("sftp", "sftp://host.xz[:port]/path/to/repo.git/"),              // NOI18N
//        RSYNC("rsync", "rsync://host.xz/path/to/repo.git/"),                    // NOI18N
        GIT("git", "git://host.xz[:port]/path/to/repo.git/");                   // NOI18N
        
        private final String name;
        private final String tip;

        private Scheme(String name, String tip) {
            this.name = name;
            this.tip = tip;
        };        
         
        private String getTip() {
            return tip;
        }
        
        @Override
        public String toString() {
            return name;
        }
    };
    
    private final RemoteRepositoryPanel panel;
    private final JComponent[] inputFields;
    
    public RemoteRepository(String forPath) {
        this(forPath, false);
    }
    
    private RemoteRepository(String forPath, boolean fixedUrl) {
        assert !fixedUrl || forPath != null && !forPath.trim().isEmpty();
        this.panel = new RemoteRepositoryPanel();
        this.urlFixed = fixedUrl;
        this.inputFields = new JComponent[] {
            panel.urlComboBox,
            panel.userTextField,
            panel.userPasswordField,
            panel.savePasswordCheckBox,
            panel.directoryBrowseButton,
            panel.proxySettingsButton,
            panel.repositoryLabel,
            panel.userLabel,
            panel.passwordLabel,
            panel.tipLabel,
            panel.leaveBlankLabel,
        };
        
        attachListeners();
        initUrlComboValues(forPath);
        setFieldsVisibility();
        validateFields();
    }
    
    public JPanel getPanel() {
        return panel;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public Message getMessage() {
        return msg;
    }
    
    public GitURI getURI() {
        String uriString = getURIString();        
        if(uriString != null && !uriString.isEmpty()) {
            try {
                return new GitURI(uriString);
            } catch (URISyntaxException ex) {
                Git.LOG.log(Level.INFO, uriString, ex);
            }
        }
        return null;
    }    
    
    public void setEnabled (boolean enabled) {
        for (JComponent inputField : inputFields) {
            inputField.setEnabled(enabled);
        }
    }
    
    public void store() {
        GitURI guri = getURI();
        assert guri != null;
        if(guri == null) {
            return;
        }
        
        final boolean isSelected = panel.savePasswordCheckBox.isSelected();
        guri = guri.setUser(panel.userTextField.getText().isEmpty() ? null : panel.userTextField.getText())
                .setPass(new String(panel.userPasswordField.getPassword()));
        final GitURI fguri = guri;
        Runnable outOfAWT = new Runnable() {
            @Override
            public void run() {
                GitModuleConfig.getDefault().insertRecentGitURI(fguri, isSelected);
                recentGuris.put(fguri.toString(), fguri);
            }
        };
        if (EventQueue.isDispatchThread()) {
            Git.getInstance().getRequestProcessor().post(outOfAWT);
        } else {
            outOfAWT.run();
        }
    }
    
    public void removeChangeListener(ChangeListener listener) {
        support.removeChangeListener(listener);
    }

    public void addChangeListener(ChangeListener listener) {
        support.addChangeListener(listener);
    }
    
    private String getURIString() {
        String uriString = (String) panel.urlComboBox.getEditor().getItem();
        return uriString;
    }
    
    private void attachListeners () {
        panel.proxySettingsButton.addActionListener(this);
        panel.directoryBrowseButton.addActionListener(this);
        panel.urlComboBox.addActionListener(this);
        ((JTextComponent) panel.urlComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);        
        panel.urlComboBox.addItemListener(this);
    }

    @Override
    public void insertUpdate(DocumentEvent de) {
        if(ignoreComboEvents) return;
        validateFields();
        setFieldsVisibility();
        findComboItem(false);
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
        if(ignoreComboEvents) return;
        validateFields();
        setFieldsVisibility();
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
        if(ignoreComboEvents) return;
        validateFields();
        setFieldsVisibility();
        findComboItem(false);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if(ae.getSource() == panel.directoryBrowseButton) {
            onBrowse();
        } else if(ae.getSource() == panel.proxySettingsButton) {
            onProxyConfiguration();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent ie) {
        GitURI guri = getURI();
        if(guri != null) {
            populateFields(recentGuris.get(guri.toString()));
        }
    }

    public static boolean updateFor (String url) {
        boolean retval = false;
        final RemoteRepository repository = new RemoteRepository(url, true);
        final DialogDescriptor dd = new DialogDescriptor(repository.getPanel(), NbBundle.getMessage(RemoteRepositoryPanel.class, "ACSD_RepositoryPanel_Title")); //NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        repository.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged (ChangeEvent e) {
                dd.setValid(repository.isValid());
            }
        });
        if (repository.isValid()) {
            dd.setValid(true);
        }
        dialog.setVisible(true);
        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            repository.store();
            retval = true;
        }
        return retval;
    }

    private void validateFields () {
        try {
            valid = true;
            msg = null;
            
            GitURI uri = getURI();
            if(uri == null) {
                valid = false;
                msg = new Message(NbBundle.getMessage(RemoteRepository.class, "MSG_EMPTY_URI_ERROR"), true); // NOI18N
            } else {
                // XXX check suported protocols
            }
        } finally {
            support.fireChange();
        }
    }    
    
    private void setFieldsVisibility() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                GitURI uri = getURI();
                if(uri == null) {
                    return;
                }
                
                boolean isFile = true;
                if(uri.getScheme() != null) {
                    for (Scheme s : Scheme.values()) {
                        if(s == Scheme.FILE) continue;
                        if(uri.getScheme().startsWith(s.toString())) {
                            panel.tipLabel.setText(s.getTip());
                            isFile = false;
                            break;
                        }
                    }
                }
                if(isFile) {
                    panel.tipLabel.setText(Scheme.FILE.getTip());
                }
                if (urlFixed) {
                    panel.tipLabel.setText(null);
                }
                
                panel.directoryBrowseButton.setVisible(isFile);
                
                panel.passwordLabel.setVisible(!isFile);
                panel.userPasswordField.setVisible(!isFile);
                panel.userLabel.setVisible(!isFile);
                panel.userTextField.setVisible(!isFile);
                panel.proxySettingsButton.setVisible(!isFile);
                panel.savePasswordCheckBox.setVisible(!isFile);
                panel.leaveBlankLabel.setVisible(!isFile);
            }
        });
    }

    private boolean ignoreComboEvents = false;
    private void findComboItem(boolean selectAll) {
        final String uriString = getURIString();        
        if(uriString == null || uriString.isEmpty()) {
            return;
        }
        DefaultComboBoxModel model = (DefaultComboBoxModel)panel.urlComboBox.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            final String item = (String) model.getElementAt(i);
            if(item.toLowerCase().startsWith(uriString.toLowerCase())) {
                final int start = selectAll ? 0 : uriString.length();
                final int end = item.length();
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        ignoreComboEvents = true;
                        try {
                            setComboText(item, start, end);
                            
                            setFieldsVisibility();
                            
                            GitURI guri = recentGuris.get(item);
                            populateFields(guri);
                            
                        } finally {
                            ignoreComboEvents = false;
                        }
                    }
                });
                return;
            } else {
                
            }
        }
    }

    private void setComboText (String item, int start, int end) {
        JTextComponent txt = (JTextComponent)panel.urlComboBox.getEditor().getEditorComponent();
        txt.setText(item);
        txt.setCaretPosition(end);
        txt.moveCaretPosition(start);
    }
    
    private void populateFields(GitURI guri) {
        if(guri == null) return;

        boolean hasUser = false;
        boolean hasPass = false;
        String user = guri.getUser();
        if(user != null && !user.isEmpty()) {
            panel.userTextField.setText(guri.getUser());
            hasUser = true;
        } else {
            panel.userTextField.setText("");
        }
        panel.userTextField.setText(guri.getUser());
        String pass = guri.getPass();
        if(pass != null && !pass.isEmpty()) {
            panel.userPasswordField.setText(guri.getPass());
            hasPass = true;
        } else {
            panel.userPasswordField.setText("");            // NOI18N
        }
        panel.savePasswordCheckBox.setSelected(hasUser || hasPass);
    }
    
    private Map<String, GitURI> recentGuris = new HashMap<String, GitURI>();
    private void initUrlComboValues(final String forPath) {
        Git.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                panel.urlComboBox.setEnabled(false);
                try {
                    final DefaultComboBoxModel model = new DefaultComboBoxModel();
                    
                    try {
                        List<GitURI> guris = GitModuleConfig.getDefault().getRecentUrls();
                        for (GitURI gitURI : guris) {

                            // strip user/psswd
                            GitURI g = new GitURI(gitURI.toString()).setPass(null).setUser(null);
                            model.addElement(g.toString());
                            
                            recentGuris.put(g.toString(), gitURI);
                        }
                    } catch (Throwable t) {
                        Git.LOG.log(Level.WARNING, null, t);
                    }
                    
                    for (Scheme s : Scheme.values()) {
                        model.addElement(s.toString() + (s == Scheme.FILE ? ":///" : "://"));   // NOI18N
                    }
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            ignoreComboEvents = true;
                            panel.urlComboBox.setModel(model);
                            if (forPath != null) {
                                setComboText(forPath, 0, forPath.length());
                            }
                            ignoreComboEvents = false;
                            findComboItem(true);
                            setFieldsVisibility();
                            validateFields();
                        }
                    });
                } finally {
                    if (!urlFixed) {
                        panel.urlComboBox.setEnabled(true);
                    }
                }
            }
        });
    }

    private void onBrowse() {
        JTextComponent comboEditor = ((JTextComponent) panel.urlComboBox.getEditor().getEditorComponent());
        String txt = comboEditor.getText();
        if(txt == null || txt.trim().isEmpty()) {
            return;
        }
        File file = null;
        try {
            URI uri = new URI(comboEditor.getText());
            if (uri.isAbsolute()) {
                file = new File(uri);
            } else {
                file = new File(comboEditor.getText());
            }
        } catch (URISyntaxException ex) {
            //
        }
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(RemoteRepositoryPanel.class, "RepositoryPanel.FileChooser.Descritpion"), //NOI18N
                file);
        fileChooser.setDialogTitle(NbBundle.getMessage(RemoteRepositoryPanel.class, "RepositoryPanel.FileChooser.Title")); //NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(panel, null);
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            comboEditor.setText(f.toURI().toString());
        }
    }    
    
    private void onProxyConfiguration() {
        OptionsDisplayer.getDefault().open("General");              // NOI18N
    }       
}
