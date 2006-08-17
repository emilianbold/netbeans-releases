/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.ui.repository;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.subversion.config.ProxyDescriptor;
import org.netbeans.modules.subversion.settings.HistorySettings;
import org.netbeans.modules.subversion.config.PasswordFile;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 *
 *
 * @author Tomas Stupka
 */
public class Repository implements ActionListener, DocumentListener, FocusListener {

    private final static String LOCAL_URL_HELP      = "file:///repository_path[@REV]"; // NOI18N
    private final static String HTTP_URL_HELP       = "http://hostname/repository_path[@REV]"; // NOI18N
    private final static String HTTPS_URL_HELP      = "https://hostname/repository_path[@REV]"; // NOI18N
    private final static String SVN_URL_HELP        = "svn://hostname/repository_path[@REV]"; // NOI18N
    private final static String SVN_SSH_URL_HELP    = "svn+ssh://hostname/repository_path[@REV]"; // NOI18N

    private RepositoryPanel repositoryPanel;
    private ProxyDescriptor proxyDescriptor;
    private RequestProcessor.Task updatePasswordTask;
    private volatile boolean internalDocumentChange;   
    private boolean passwordExpected;        
    private boolean valid = true;
    private boolean userVisitedProxySettings;    
    private List<PropertyChangeListener> listeners;


    public static final String PROP_VALID = "valid"; // NOI18N

    private String message;

    private boolean acceptRevision;

    public class SelectedRepository {
        private final SVNUrl url;
        private final SVNRevision revision;
        SelectedRepository (SVNUrl url, SVNRevision revision) {
            this.url = url;
            this.revision = revision;
        }
        public SVNUrl getUrl() {
            return url;
        }
        public SVNRevision getRevision() {
            return revision;
        }
    }    

    public Repository(SVNUrl url, boolean urlEditable, boolean acceptRevision, String titleLabel) {
        this(urlEditable, acceptRevision, titleLabel);
        repositoryPanel.urlComboBox.setSelectedItem(url.toString());
    }
    
    public Repository(boolean urlEditable, boolean acceptRevision, String titleLabel) {
        getPanel().urlComboBox.setEnabled(urlEditable);
        getPanel().titleLabel.setText(titleLabel);
        this.acceptRevision = acceptRevision;
    }
    
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==repositoryPanel.proxySettingsButton) {
            onProxyConfiguration();
        }
    }
    
    private void onProxyConfiguration() {
        // don't call SvnConfigFiles.getInstance().getProxyDescriptor(url.getHost());
        // in awt
        RequestProcessor requestProcessor = new RequestProcessor().getDefault();
        requestProcessor.post(new Runnable() {
            public void run() {
                SVNUrl url = null;
                try {
                    url = getSelectedRepository().getUrl();
                } catch (InterruptedException ex) {
                    return; // should not happen
                }

                if(!userVisitedProxySettings) {
                    proxyDescriptor = SvnConfigFiles.getInstance().getProxyDescriptor(SvnUtils.ripUserFromHost(url.getHost()));
                }                
                ProxySelector selector = new ProxySelector();
                selector.setProxyDescriptor(proxyDescriptor);
                ProxyDescriptor pd = selector.selectProxy();
                if (pd != null) {
                    proxyDescriptor = pd;
                    userVisitedProxySettings = true;
                }
            }
        });
        setValid(true, "");
    }    

    private RepositoryPanel createPanel() {
        if (repositoryPanel == null) {
            repositoryPanel = new RepositoryPanel();
            repositoryPanel.proxySettingsButton.addActionListener(this);

            RequestProcessor requestProcessor = new RequestProcessor();
            updatePasswordTask = requestProcessor.create(new Runnable() {
                public void run() {
                    SelectedRepository repository = null;
                    try {
                        repository = getSelectedRepository();
                    } catch (Exception ex) {}
                    if(repository == null) {
                        return;
                    }                    
                    PasswordFile passwordFile = PasswordFile.findFileForUrl(repository.getUrl());
                    if (passwordFile != null && passwordFile.getPassword() != null && passwordExpected) {
                        internalDocumentChange = true;
                        repositoryPanel.userPasswordField.setText(passwordFile.getPassword());
                        repositoryPanel.userTextField.setText(passwordFile.getUsername());
                        internalDocumentChange = false;
                        cancelPasswordUpdate();
                    }
                }
            });        
        }
        
        Set<String> recentRoots = new LinkedHashSet<String>();
        recentRoots.addAll(HistorySettings.getRecent(HistorySettings.PROP_SVN_URLS));        
        // templates for supported connection methods        
        recentRoots.add("file:///");      // NOI18N
        recentRoots.add("http://");       // NOI18N
        recentRoots.add("https://");      // NOI18N
        recentRoots.add("svn://");        // NOI18N
        recentRoots.add("svn+ssh://");    // NOI18N

        ComboBoxModel rootsModel = new DefaultComboBoxModel(new Vector<String>(recentRoots));
        repositoryPanel.urlComboBox.setModel(rootsModel);
        repositoryPanel.urlComboBox.addActionListener(this);
        Component editor = repositoryPanel.urlComboBox.getEditor().getEditorComponent();
        JTextComponent textEditor = (JTextComponent) editor;
        if (recentRoots.size() == 0) {
            textEditor.setText("file:///");    // NOI18N
        } 
        textEditor.selectAll();
        textEditor.getDocument().addDocumentListener(this);
        
        repositoryPanel.userPasswordField.getDocument().addDocumentListener(this);
        repositoryPanel.userPasswordField.addFocusListener(this);
        
        repositoryPanel.userTextField.getDocument().addDocumentListener(this);
        
        onSelectedRepositoryChange();
        return repositoryPanel;
    }

    public void setEditable(boolean editable) {
        repositoryPanel.urlComboBox.setEditable(editable);
        repositoryPanel.userPasswordField.setEditable(editable);
        repositoryPanel.userTextField.setEditable(editable);        
        repositoryPanel.proxySettingsButton.setEnabled(editable);        
    }
    
    public void storeConfigValues() throws InterruptedException {
        SelectedRepository repository = null;        
        repository = getSelectedRepository();        
        if(repository==null) {
            return; // uups 
        }
        
        SVNUrl repositoryUrl = repository.getUrl();
        if (repositoryUrl.getProtocol().equals("http")  ||    // NOI18N
            repositoryUrl.getProtocol().equals("https") ||    // NOI18N
            repositoryUrl.getProtocol().equals("svn")   ||    // NOI18N
            repositoryUrl.getProtocol().equals("svn+ssh") )   // NOI18N
        {                                
            PasswordFile passwordFile = PasswordFile.findFileForUrl(repositoryUrl);                    
            if(passwordFile != null ) {
                passwordFile.setPassword(getPassword());
                passwordFile.setUsername(getUserName());
                try {
                    passwordFile.store();   
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                }   
            } else {
                // XXX should be stored in some way ...
            }               
            
            // XXX the way the usr, password and proxy settings are stored is not symetric and consistent...
            if(userVisitedProxySettings) {
                SvnConfigFiles.getInstance().setProxy(proxyDescriptor, SvnUtils.ripUserFromHost(repositoryUrl.getHost()));
            }            
        }    
        
    }

    public void storeHistory() {        
        SelectedRepository repository;
        try {
            repository = getSelectedRepository();
        } catch (InterruptedException ex) {
            return; // should not happen
        }
        HistorySettings.addRecent(HistorySettings.PROP_SVN_URLS, repository.getUrl().toString());        
    }

    public void insertUpdate(DocumentEvent e) {
        textChanged(e);
    }

    public void removeUpdate(DocumentEvent e) {
        textChanged(e);
    }

    public void changedUpdate(DocumentEvent e) { }

    private void textChanged(final DocumentEvent e) {
        // repost later to AWT otherwise it can deadlock because
        // the document is locked while firing event and we try
        // synchronously access its content from selected repository
        if (internalDocumentChange) return;
        Runnable awt = new Runnable() {
            public void run() {
                if (e.getDocument() == repositoryPanel.userTextField.getDocument()) {
                    onUsernameChange();
                } else if (e.getDocument() == repositoryPanel.userPasswordField.getDocument()) {
                    onPasswordChange();
                } else if (e.getDocument() == ((JTextComponent) repositoryPanel.urlComboBox.getEditor().getEditorComponent()).getDocument()) {
                    onSelectedRepositoryChange();
                } else if (e.getDocument() == repositoryPanel.userTextField.getDocument()) {

                }
                validateSvnUrl();
            }
        };
        SwingUtilities.invokeLater(awt);
    }
        
    public SelectedRepository getSelectedRepository() throws InterruptedException {
        String urlString = selectedUrlString();        
        if(urlString == null ) {
            return null;
        }
        try {            
            return getSelectedRepository(urlString);
        } catch (MalformedURLException ex) {
            setValid(false, ex.getLocalizedMessage());
            return null;
        }        
    }

    private SelectedRepository getSelectedRepository(String urlString) throws MalformedURLException {
        int idx = urlString.lastIndexOf('@');
        int hostIdx = urlString.indexOf("://"); // NOI18N
        int firstSlashIdx = urlString.indexOf("/", hostIdx + 3); // NOI18N
        SVNRevision revision = null;
        if(idx < 0 || firstSlashIdx < 0 || idx < firstSlashIdx) {
            revision = SVNRevision.HEAD;
        } else if (acceptRevision) {
            if( idx + 1 < urlString.length()) {
                String number = ""; // NOI18N
                try {
                    number = urlString.substring(idx+1);
                    revision = new SVNRevision.Number(Long.parseLong(number));
                } catch (NumberFormatException ex) {
                    setValid(false, NbBundle.getMessage(Repository.class, "MSG_Repository_WrongRevision", number)); // NOI18N
                    return null;
                }
            }
            urlString = urlString.substring(0, idx);
        } else {
            throw new MalformedURLException(NbBundle.getMessage(Repository.class, "MSG_Repository_OnlyHEADRevision")); // NOI18N
        }
        SVNUrl url = removeEmptyPathSegments(new SVNUrl(urlString));
        return new SelectedRepository(url, revision);
    }

    private SVNUrl removeEmptyPathSegments(SVNUrl url) throws MalformedURLException {
        String[] pathSegments = url.getPathSegments();
        StringBuffer urlString = new StringBuffer();
        urlString.append(url.getProtocol());
        urlString.append("://"); // NOI18N
        urlString.append(SvnUtils.ripUserFromHost(url.getHost()));
        if(url.getPort() > 0) {
            urlString.append(":"); // NOI18N
            urlString.append(url.getPort());
        }
        boolean gotSegments = false;
        for (int i = 0; i < pathSegments.length; i++) {
            if(!pathSegments[i].trim().equals("")) { // NOI18N
                gotSegments = true;
                urlString.append("/"); // NOI18N
                urlString.append(pathSegments[i]);                
            }
        }
        try {
            if(gotSegments) {
                return new SVNUrl(urlString.toString());
            } else {
                return url;
            }
        } catch (MalformedURLException ex) {
            throw ex;
        }
    }
    
    /**
     * Fast url syntax check. It can invalidate the whole step
     */
    private void validateSvnUrl() {
        boolean valid;
        try {
            valid = getSelectedRepository() != null;
        } catch (InterruptedException ex) {
            valid = false; // should not happen
        }
        setValid(valid, message);
        repositoryPanel.proxySettingsButton.setEnabled(valid);
        repositoryPanel.userPasswordField.setEnabled(valid);    
        repositoryPanel.userTextField.setEnabled(valid);  
    }
    
    /**
     * On valid SVNUrl loads UI fields from Svn Config Files
     * Always updates UI fields visibility.
     */
    private void onSelectedRepositoryChange() {
        setValid(true, "");
        SelectedRepository repository = null;
        try {
            repository = getSelectedRepository();
        } catch (InterruptedException ex) {
            return; // should not happen
        }

        SVNUrl url = null;
        if(repository != null) {
            url = repository.getUrl();
            if (url != null && !url.getProtocol().equals("file")) { // NOI18N
                if (userVisitedProxySettings == false) {
                    proxyDescriptor = SvnConfigFiles.getInstance().getProxyDescriptor(SvnUtils.ripUserFromHost(url.getHost()));
                }
                schedulePasswordUpdate();
            }
        }
        message = ""; // NOI18N
        updateVisibility();
    }            

    /** Shows proper fields depending on Svn connection method. */
    private void updateVisibility() {
        String selectedUrlString;
        try {
            selectedUrlString = selectedUrlString();
        } catch (InterruptedException ex) {
            return;
        }
        boolean remoteServerFields = false;
        if(selectedUrlString.startsWith("http:")) { // NOI18N
            repositoryPanel.tipLabel.setText(HTTP_URL_HELP);
            remoteServerFields = true;
        } else if(selectedUrlString.startsWith("https:")) { // NOI18N
            repositoryPanel.tipLabel.setText(HTTPS_URL_HELP);
            remoteServerFields = true;
        } else if(selectedUrlString.startsWith("svn:")) { // NOI18N
            repositoryPanel.tipLabel.setText(SVN_URL_HELP);
            remoteServerFields = true;
        } else if(selectedUrlString.startsWith("svn+ssh:")) { // NOI18N
            repositoryPanel.tipLabel.setText(SVN_SSH_URL_HELP);
            remoteServerFields = true;
        } else if(selectedUrlString.startsWith("file:")) { // NOI18N
            repositoryPanel.tipLabel.setText(LOCAL_URL_HELP);
        } else {
            repositoryPanel.tipLabel.setText(NbBundle.getMessage(Repository.class, "MSG_Repository_Url_Help", new Object [] { // NOI18N
                LOCAL_URL_HELP, HTTP_URL_HELP, HTTPS_URL_HELP, SVN_URL_HELP, SVN_SSH_URL_HELP
            }));
        }

        repositoryPanel.userPasswordField.setVisible(remoteServerFields);
        repositoryPanel.passwordLabel.setVisible(remoteServerFields);          
        repositoryPanel.userTextField.setVisible(remoteServerFields);          
        repositoryPanel.leaveBlankLabel.setVisible(remoteServerFields);        
        repositoryPanel.userLabel.setVisible(remoteServerFields);             
        repositoryPanel.proxySettingsButton.setVisible(remoteServerFields);        
    }

    /**
     * Load selected root from Swing structures (from arbitrary thread).
     * @return null on failure
     */
    private String selectedUrlString() throws InterruptedException {        
        final String[] svnUrl = new String[1];
        try {
            Runnable awt = new Runnable() {
                public void run() {
                    svnUrl[0] = (String) repositoryPanel.urlComboBox.getEditor().getItem();
                }
            };
            if (SwingUtilities.isEventDispatchThread()) {
                awt.run();
            } else {
                SwingUtilities.invokeAndWait(awt);
            }
            return svnUrl[0].trim();
        } catch (InvocationTargetException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.notify(e);
        }
        return null;
    }

    /**
     * Visually notifies user about password length
     */
    private void schedulePasswordUpdate() {
        String selectedUrlString;
        try {
            selectedUrlString = selectedUrlString();
        } catch (InterruptedException ex) {
            return;
        }
        if ( selectedUrlString.startsWith("http:")     || // NOI18N
             selectedUrlString.startsWith("https:")    || // NOI18N
             selectedUrlString.startsWith("svn:")      || // NOI18N
             selectedUrlString.startsWith("svn+ssh:") )   // NOI18N
        {    
            passwordExpected = true;
            updatePasswordTask.schedule(10);
        }
    }

    private void cancelPasswordUpdate() {
        passwordExpected = false;
    }

    private void onUsernameChange() {
        setValid(true, "");
    }
    
    private void onPasswordChange() {
        setValid(true, "");
        cancelPasswordUpdate();
    }

    public RepositoryPanel getPanel() {
        if(repositoryPanel == null) {
            repositoryPanel = createPanel();
        };
        return repositoryPanel;
    }

    public String getUserName() {
        return repositoryPanel.userTextField.getText();
    }

    public String getPassword() {
        return new String(repositoryPanel.userPasswordField.getPassword());
    }

    public boolean isValid() {
        return valid;
    }

    private void setValid(boolean valid, String message) {
        boolean oldValue = this.valid;
        String oldMessage = this.message;
        this.message = message;
        //if(oldValue != valid || oldMessage != message) {
            this.valid = valid;
            fireValidPropertyChanged(oldValue, valid);
        //};
    }

    private void fireValidPropertyChanged(boolean oldValue, boolean valid) {
        if(listeners==null) {
            return;
        }
        for (Iterator it = listeners.iterator();  it.hasNext();) {
            PropertyChangeListener l = (PropertyChangeListener) it.next();
            l.propertyChange(new PropertyChangeEvent(this, PROP_VALID, new Boolean(oldValue), new Boolean(valid)));
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        if(listeners==null) {
            listeners = new ArrayList<PropertyChangeListener>();
        }
        listeners.add(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        if(listeners==null) {
            return;
        }
        listeners.remove(l);
    }

    public String getMessage() {
        return message;
    }

    public void focusGained(FocusEvent focusEvent) {
        if(focusEvent.getSource()==repositoryPanel.userPasswordField) {
            repositoryPanel.userPasswordField.selectAll();
        }
    }

    public void focusLost(FocusEvent focusEvent) {
        // do nothing
    }
    
}
