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

package org.netbeans.modules.git.ui.clone;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.git.ui.wizards.AbstractWizardPanel.Message;
import org.netbeans.modules.versioning.util.AccessibleJFileChooser;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class Repository implements DocumentListener, ActionListener {
    private boolean valid;
    private Message msg;

    private ChangeSupport support = new ChangeSupport(this);
    private static final Pattern SCHEME_PATTERN = Pattern.compile("([a-z][a-z0-9+-]+)://"); // NOI18N

    private enum Scheme {
        FILE("file", "file:///path/to/repo.git/  or  /path/to/repo.git/"),
        HTTP("http", "http[s]://host.xz[:port]/path/to/repo.git/"),
        HTTPS("https", "http[s]://host.xz[:port]/path/to/repo.git/"),
        FTP("ftp", "ftp[s]://host.xz[:port]/path/to/repo.git/"),
        FTPS("ftps", "ftp[s]://host.xz[:port]/path/to/repo.git/"),
        SSH("ssh", "ssh://[user@]host.xz[:port]/path/to/repo.git/"),
        GIT("git", "git://host.xz[:port]/path/to/repo.git/"),
        RSYNC("rsync", "rsync://host.xz/path/to/repo.git/");     
        
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
    
    private final RepositoryPanel panel;
    private final JComponent[] inputFields;

    Repository() {
        this(null);
    }
    
    public Repository(String forPath) {
        this.panel = new RepositoryPanel();
        this.inputFields = new JComponent[] {
            panel.urlComboBox,
            panel.userTextField,
            panel.userPasswordField,
            panel.savePasswordCheckBox,
            panel.directoryBrowseButton,
            panel.proxySettingsButton
        };
        
        attachListeners();
        initUrlComboValues();
        setFieldsVisibility();
        
        if(forPath != null) {
            ((JTextComponent)panel.urlComboBox.getEditor().getEditorComponent()).setText(forPath);
        }
        
        validateFields();
    }
    
    JPanel getPanel() {
        return panel;
    }
    
    boolean isValid() {
        return valid;
    }
    
    Message getMessage() {
        return msg;
    }
    
    public void removeChangeListener(ChangeListener listener) {
        support.removeChangeListener(listener);
    }

    public void addChangeListener(ChangeListener listener) {
        support.addChangeListener(listener);
    }
    
    private void attachListeners () {
        panel.proxySettingsButton.addActionListener(this);
        panel.directoryBrowseButton.addActionListener(this);
        panel.urlComboBox.addActionListener(this);
        ((JTextComponent) panel.urlComboBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(this);        
        panel.userPasswordField.getDocument().addDocumentListener(this);
        panel.userTextField.getDocument().addDocumentListener(this);
    }

    @Override
    public void insertUpdate(DocumentEvent de) {
        validateFields();
        setFieldsVisibility();
    }

    @Override
    public void removeUpdate(DocumentEvent de) {
        validateFields();
        setFieldsVisibility();
    }

    @Override
    public void changedUpdate(DocumentEvent de) {
        validateFields();
        setFieldsVisibility();
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if(ae.getSource() == panel.directoryBrowseButton) {
            onBrowse();
        }
    }
    
    void enableFields (boolean enabled) {
        for (JComponent inputField : inputFields) {
            inputField.setEnabled(enabled);
        }
    }

    private void validateFields () {
        boolean oldValid = valid;
        try {
            valid = true;
            msg = null;
            
            String uri = getUrlString();
            if(uri == null || uri.trim().isEmpty()) {
                valid = false;
                msg = new Message(NbBundle.getMessage(Repository.class, "MSG_EMPTY_URI_ERROR"), true);
            } else {
                // XXX check suported protocols
            }
        } finally {
            if(valid != oldValid) {
                support.fireChange();
            }
        }
    }    
    
    private void setFieldsVisibility() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                String urlString = getUrlString();
                if(urlString == null) {
                    return;
                }
                
                boolean isFile = true;
                for (Scheme s : Scheme.values()) {
                    if(s == Scheme.FILE) continue;
                    if(urlString.startsWith(s.toString())) {
                        panel.tipLabel.setText(s.getTip());
                        isFile = false;
                        break;
                    }
                }
                if(isFile) {
                    panel.tipLabel.setText(Scheme.FILE.getTip());
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

    String getUrlString() {
        return (String) panel.urlComboBox.getEditor().getItem();        
    }
    
    private void initUrlComboValues() {
        String[] protocols = new String[Scheme.values().length];
        int i = 0;
        for (Scheme s : Scheme.values()) {
            protocols[i++] = s.toString() + (s == Scheme.FILE ?  ":///" : "://");
        }
        DefaultComboBoxModel model = new DefaultComboBoxModel(protocols);
        panel.urlComboBox.setModel(model);
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
            file = new File(uri);
        } catch (URISyntaxException ex) {
            //
        }
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(RepositoryPanel.class, "RepositoryPanel.FileChooser.Descritpion"), //NOI18N
                file);
        fileChooser.setDialogTitle(NbBundle.getMessage(RepositoryPanel.class, "RepositoryPanel.FileChooser.Title")); //NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.showDialog(panel, null);
        File f = fileChooser.getSelectedFile();
        if (f != null) {
            comboEditor.setText(f.toURI().toString());
        }
    }    
}
