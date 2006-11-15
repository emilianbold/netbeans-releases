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
import java.text.MessageFormat;
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

    private final static String LOCAL_URL_HELP      = "file:///repository_path[@REV]";              // NOI18N
    private final static String HTTP_URL_HELP       = "http://hostname/repository_path[@REV]";      // NOI18N
    private final static String HTTPS_URL_HELP      = "https://hostname/repository_path[@REV]";     // NOI18N
    private final static String SVN_URL_HELP        = "svn://hostname/repository_path[@REV]";       // NOI18N
    private final static String SVN_SSH_URL_HELP    = "svn+{0}://hostname/repository_path[@REV]";   // NOI18N   
            
    private RepositoryPanel repositoryPanel;
    private ProxyDescriptor proxyDescriptor;
    private RequestProcessor.Task updatePasswordAndProxyTask;
    private volatile boolean internalDocumentChange;   
    private boolean passwordExpected;        
    private boolean valid = true;
    private boolean userVisitedProxySettings;    
    private boolean userEditedPasswordOrName;        
    private List<PropertyChangeListener> listeners;


    public static final String PROP_VALID = "valid";                                                // NOI18N

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

    public Repository(List<String> recentUrls, SVNUrl selectedUrl, boolean urlEditable, boolean acceptRevision, String titleLabel) {
        this(recentUrls, urlEditable, acceptRevision, null, titleLabel);        
        repositoryPanel.urlComboBox.setSelectedItem(selectedUrl.toString());
    }        
    
    public Repository(List<String> recentUrls, boolean urlEditable, boolean acceptRevision, ActionListener removeActionListener, String titleLabel) {
        initPanel(removeActionListener);
        refreshUrlHistory(recentUrls);
        
        repositoryPanel.urlComboBox.setEnabled(urlEditable);
        repositoryPanel.titleLabel.setText(titleLabel);
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
    
    private void initPanel(ActionListener removeActionListener) {        
        repositoryPanel = new RepositoryPanel();

        repositoryPanel.proxySettingsButton.addActionListener(this);
        repositoryPanel.removeButton.addActionListener(this);

        if(removeActionListener!=null) {
            repositoryPanel.removeButton.setVisible(true);    
            repositoryPanel.removeButton.addActionListener(removeActionListener);
        } else {
            repositoryPanel.removeButton.setVisible(false);    
        }            

        RequestProcessor requestProcessor = new RequestProcessor();
        updatePasswordAndProxyTask = requestProcessor.create(new Runnable() {
            public void run() {
                SelectedRepository repository = null;
                try {
                    repository = getSelectedRepository();
                } catch (Exception ex) {}
                if(repository == null) {
                    return;
                }                    
                if (userVisitedProxySettings == false) {
                    proxyDescriptor = getProxyFromConfigFile(repository.getUrl());
                }
                PasswordFile passwordFile = PasswordFile.findFileForUrl(repository.getUrl());
                if (passwordFile != null && 
                    passwordFile.getPassword() != null && 
                    passwordExpected &&                         
                    (repositoryPanel.userTextField.getText().trim().equals("") || !userEditedPasswordOrName) &&           // NOI18N
                    (repositoryPanel.userPasswordField.getPassword().length == 0 || !userEditedPasswordOrName) )  
                {
                    internalDocumentChange = true;
                    repositoryPanel.userPasswordField.setText(passwordFile.getPassword());
                    repositoryPanel.userTextField.setText(passwordFile.getUsername());
                    internalDocumentChange = false;
                    cancelPasswordUpdate();
                }
            }
        });        
        
        repositoryPanel.urlComboBox.addActionListener(this);
        getUrlComboEditor().getDocument().addDocumentListener(this);
        
        repositoryPanel.userPasswordField.getDocument().addDocumentListener(this);
        repositoryPanel.userPasswordField.addFocusListener(this);
        
        repositoryPanel.userTextField.getDocument().addDocumentListener(this);
        repositoryPanel.tunnelCommandTextField.getDocument().addDocumentListener(this);
        
        onSelectedRepositoryChange();
    }
    
    public void refreshUrlHistory(List<String> recentUrls) {
        Set<String> recentRoots = new LinkedHashSet<String>();
        recentRoots.addAll(recentUrls);                               
        // templates for supported connection methods        
        recentRoots.add("file:///");      // NOI18N
        recentRoots.add("http://");       // NOI18N
        recentRoots.add("https://");      // NOI18N
        recentRoots.add("svn://");        // NOI18N
        recentRoots.add("svn+ssh://");    // NOI18N

        ComboBoxModel rootsModel = new DefaultComboBoxModel(new Vector<String>(recentRoots));
        repositoryPanel.urlComboBox.setModel(rootsModel);
        JTextComponent textEditor = getUrlComboEditor();
        if (recentRoots.size() == 0) {
            textEditor.setText("file:///");    // NOI18N
        } 
        textEditor.selectAll();        
    }

    private JTextComponent getUrlComboEditor() {
        Component editor = repositoryPanel.urlComboBox.getEditor().getEditorComponent();
        JTextComponent textEditor = (JTextComponent) editor;
        return textEditor;
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
            repositoryUrl.getProtocol().startsWith("svn+") )  // NOI18N
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
            SvnConfigFiles.getInstance().setExternalCommand(getTunnelName(repositoryUrl.getProtocol()), repositoryPanel.tunnelCommandTextField.getText());
        }    
        
    }

//    public void storeUrlHistory() {        
//        SelectedRepository repository;
//        try {
//            repository = getSelectedRepository();
//        } catch (InterruptedException ex) {
//            return; // should not happen
//        }
//        Utils.insert(SvnModuleConfig.getDefault().getPreferences(), RECENT_URL, repository.getUrl().toString(), -1);
//    }
    
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
                } 
                validateSvnUrl();
            }
        };
        SwingUtilities.invokeLater(awt);
    }
        
    public SelectedRepository getSelectedRepository() throws InterruptedException {
        String urlString = getSelection();        
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
        int hostIdx = urlString.indexOf("://");                         // NOI18N
        int firstSlashIdx = urlString.indexOf("/", hostIdx + 3);        // NOI18N
        SVNRevision revision = null;
        if(idx < 0 || firstSlashIdx < 0 || idx < firstSlashIdx) {
            revision = SVNRevision.HEAD;
        } else if (acceptRevision) {
            if( idx + 1 < urlString.length()) {
                String revisionString = "";                             // NOI18N
                try {
                    revisionString = urlString.substring(idx+1);
                    revision = SvnUtils.getSVNRevision(revisionString);
                } catch (NumberFormatException ex) {
                    setValid(false, NbBundle.getMessage(Repository.class, "MSG_Repository_WrongRevision", revisionString));     // NOI18N
                    return null;
                }
            } else {
                revision = SVNRevision.HEAD;
            }
            urlString = urlString.substring(0, idx);
        } else {
            throw new MalformedURLException(NbBundle.getMessage(Repository.class, "MSG_Repository_OnlyHEADRevision"));          // NOI18N
        }
        SVNUrl url = removeEmptyPathSegments(new SVNUrl(urlString));
        return new SelectedRepository(url, revision);
    }

    private SVNUrl removeEmptyPathSegments(SVNUrl url) throws MalformedURLException {
        String[] pathSegments = url.getPathSegments();
        StringBuffer urlString = new StringBuffer();
        urlString.append(url.getProtocol());
        urlString.append("://");                                                // NOI18N
        urlString.append(SvnUtils.ripUserFromHost(url.getHost()));
        if(url.getPort() > 0) {
            urlString.append(":");                                              // NOI18N
            urlString.append(url.getPort());
        }
        boolean gotSegments = false;
        for (int i = 0; i < pathSegments.length; i++) {
            if(!pathSegments[i].trim().equals("")) {                            // NOI18N
                gotSegments = true;
                urlString.append("/");                                          // NOI18N
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
        }
        catch (InterruptedException ex) {
            valid = false;
        }
        
        if(isTunneled() && repositoryPanel.tunnelCommandTextField.getText().trim().equals("")) {
            valid = false;
        }
        
        setValid(valid, message);
        repositoryPanel.proxySettingsButton.setEnabled(valid);
        repositoryPanel.userPasswordField.setEnabled(valid);
        repositoryPanel.userTextField.setEnabled(valid);

        try {            
            String str = getSelection();
            repositoryPanel.removeButton.setEnabled(str.trim().length() > 0);
        }
        catch (InterruptedException ex) {
            // ignore
        };
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
            if (url != null && !url.getProtocol().equals("file") && !userEditedPasswordOrName) { // NOI18N
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
            selectedUrlString = getSelection();
        } catch (InterruptedException ex) {
            return;
        }
        boolean authFields = false;
        boolean proxyFields = false;
        boolean sshFields = false;
        if(selectedUrlString.startsWith("http:")) {                             // NOI18N
            repositoryPanel.tipLabel.setText(HTTP_URL_HELP);
            authFields = true;
            proxyFields = true;
        } else if(selectedUrlString.startsWith("https:")) {                     // NOI18N
            repositoryPanel.tipLabel.setText(HTTPS_URL_HELP);
            authFields = true;
            proxyFields = true;
        } else if(selectedUrlString.startsWith("svn:")) {                       // NOI18N
            repositoryPanel.tipLabel.setText(SVN_URL_HELP);
            authFields = true;
            proxyFields = true;
        } else if(selectedUrlString.startsWith("svn+")) {                       // NOI18N
            repositoryPanel.tipLabel.setText(getSVNTunnelTip(selectedUrlString));
            sshFields = true;
        } else if(selectedUrlString.startsWith("file:")) {                      // NOI18N
            repositoryPanel.tipLabel.setText(LOCAL_URL_HELP);
        } else {
            repositoryPanel.tipLabel.setText(NbBundle.getMessage(Repository.class, "MSG_Repository_Url_Help", new Object [] { // NOI18N
                LOCAL_URL_HELP, HTTP_URL_HELP, HTTPS_URL_HELP, SVN_URL_HELP, SVN_SSH_URL_HELP
            }));
        }

        repositoryPanel.userPasswordField.setVisible(authFields);
        repositoryPanel.passwordLabel.setVisible(authFields);          
        repositoryPanel.userTextField.setVisible(authFields);          
        repositoryPanel.leaveBlankLabel.setVisible(authFields);        
        repositoryPanel.userLabel.setVisible(authFields);             
        repositoryPanel.proxySettingsButton.setVisible(proxyFields);        
        repositoryPanel.tunnelCommandTextField.setVisible(sshFields);        
        repositoryPanel.tunnelCommandLabel.setVisible(sshFields);        
        repositoryPanel.tunnelLabel.setVisible(sshFields);        
        repositoryPanel.tunnelHelpLabel.setVisible(sshFields);        
        
    }

    private String getSVNTunnelTip(String urlString) {
        String tunnelName = getTunnelName(urlString);
        return MessageFormat.format(SVN_SSH_URL_HELP, tunnelName).trim();
    }
    
    private String getTunnelName(String urlString) {
        int idx = urlString.indexOf(":", 4);
        if(idx < 0) {
            idx = urlString.length();
        }
        return urlString.substring(4, idx);
    }
    
    /**
     * Load selected root from Swing structures (from arbitrary thread).
     * @return null on failure
     */
    public String getSelection() throws InterruptedException {        
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
        if(userEditedPasswordOrName) {
            return;
        }
        String selectedUrlString;
        try {
            selectedUrlString = getSelection();
        } catch (InterruptedException ex) {
            return;
        }
        if ( selectedUrlString.startsWith("http:")     || // NOI18N
             selectedUrlString.startsWith("https:")    || // NOI18N
             selectedUrlString.startsWith("svn:"))        // NOI18N
        {    
            passwordExpected = true;
            updatePasswordAndProxyTask.schedule(10);
        }
    }

    private void cancelPasswordUpdate() {
        passwordExpected = false;
    }

    private void onUsernameChange() {
        if(!internalDocumentChange) {
            userEditedPasswordOrName = true;
        }
        setValid(true, "");
    }
    
    private void onPasswordChange() {
        if(!internalDocumentChange) {
            userEditedPasswordOrName = true;
        }
        setValid(true, "");
        cancelPasswordUpdate();
    }

    public RepositoryPanel getPanel() {
        return repositoryPanel;
    }

    public String getUserName() {
        return repositoryPanel.userTextField.getText();
    }

    public String getPassword() {
        return new String(repositoryPanel.userPasswordField.getPassword());
    }
    
    public boolean isTunneled() {        
        try {
            String selectedUrlString = getSelection();
            return selectedUrlString.startsWith("svn+");
        } catch (InterruptedException ex) {
            return false;
        }
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

    public ProxyDescriptor getProxyDescriptor() {
        if(proxyDescriptor == null) {
                SVNUrl url = null;
                try {
                    url = getSelectedRepository().getUrl();
                } catch (InterruptedException ex) {
                    return null; // should not happen
                }
                proxyDescriptor = getProxyFromConfigFile(url);
        }
        return proxyDescriptor;
    }
    
    public void removefromModel(String toRemove) {
        ComboBoxModel model = repositoryPanel.urlComboBox.getModel();        
        int idxToRemove = -1;        
        for (int i = 0; i < model.getSize(); i++) {
            String elementAt = (String) model.getElementAt(i);
            if(elementAt.equals(toRemove)) {
                idxToRemove = i;            
                break;
            }            
        }
        if(idxToRemove > -1) {            
            repositoryPanel.urlComboBox.removeItemAt(idxToRemove);    
        }                
    }
    
    private ProxyDescriptor getProxyFromConfigFile(SVNUrl url) {
        return SvnConfigFiles.getInstance().getProxyDescriptor(SvnUtils.ripUserFromHost(url.getHost()));
    }
    
    
}
