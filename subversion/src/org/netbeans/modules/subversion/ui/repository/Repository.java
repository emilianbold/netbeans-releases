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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.subversion.config.ProxyDescriptor;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * @author Tomas Stupka
 */
public class Repository implements ActionListener, DocumentListener, FocusListener, ItemListener {

    private final static String LOCAL_URL_HELP      = "file:///repository_path[@REV]";              // NOI18N
    private final static String HTTP_URL_HELP       = "http://hostname/repository_path[@REV]";      // NOI18N
    private final static String HTTPS_URL_HELP      = "https://hostname/repository_path[@REV]";     // NOI18N
    private final static String SVN_URL_HELP        = "svn://hostname/repository_path[@REV]";       // NOI18N
    private final static String SVN_SSH_URL_HELP    = "svn+{0}://hostname/repository_path[@REV]";   // NOI18N   
            
    private RepositoryPanel repositoryPanel;
    private boolean valid = true;
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

    public Repository(List<RepositoryConnection> recentUrls, SVNUrl selectedUrl, boolean urlEditable, boolean urlEnabled, boolean acceptRevision, boolean showHints, String titleLabel) {
        this(recentUrls, urlEditable, urlEnabled, acceptRevision, false, showHints, titleLabel);        
        repositoryPanel.urlComboBox.setSelectedItem(selectedUrl.toString());
    }        
    
    public Repository(List<RepositoryConnection> recentUrls, boolean urlEditable, boolean urlEnabled, boolean acceptRevision, boolean showRemove, boolean showHints, String titleLabel) {
        initPanel();
        refreshUrlHistory(recentUrls);
        
        repositoryPanel.urlComboBox.setEditable(urlEditable);
        repositoryPanel.urlComboBox.setEnabled(urlEnabled);
        repositoryPanel.titleLabel.setText(titleLabel);
        
        repositoryPanel.tunnelHelpLabel.setVisible(showHints);
        repositoryPanel.tipLabel.setVisible(showHints);
        repositoryPanel.removeButton.setVisible(showRemove);

        this.acceptRevision = acceptRevision;        
        
    }
    
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==repositoryPanel.proxySettingsButton) {
            onProxyConfiguration();
        } else if(e.getSource()==repositoryPanel.removeButton) {
            onRemoveClick();
        }  
    }
    
    private void onProxyConfiguration() {
        // don't call SvnConfigFiles.getInstance().getProxyDescriptor(url.getHost());
        // in awt
        RequestProcessor requestProcessor = new RequestProcessor().getDefault();
        requestProcessor.post(new Runnable() {
            public void run() {
                RepositoryConnection rc;
                try {
                    rc = getSelectedRepositoryConnection();                                        
                } catch (InterruptedException e) {
                    return; // should not happen
                }

                if(rc.getProxyDescriptor() == null) {
                    rc.setProxyDescriptor(SvnConfigFiles.getInstance().getProxyDescriptor(SvnUtils.ripUserFromHost(rc.getUrl())));
                }                
                ProxySelector selector = new ProxySelector();
                selector.setProxyDescriptor(rc.getProxyDescriptor());
                ProxyDescriptor pd = selector.selectProxy();
                if (pd != null) {
                    rc.setProxyDescriptor(pd);
                }
            }
        });
        setValid(true, "");
    }    
    
    private void initPanel() {        
        repositoryPanel = new RepositoryPanel();

        repositoryPanel.proxySettingsButton.addActionListener(this);
        repositoryPanel.removeButton.addActionListener(this);        
       
        repositoryPanel.urlComboBox.addActionListener(this);
        getUrlComboEditor().getDocument().addDocumentListener(this);
        
        repositoryPanel.userPasswordField.getDocument().addDocumentListener(this);
        repositoryPanel.userPasswordField.addFocusListener(this);
        
        repositoryPanel.userTextField.getDocument().addDocumentListener(this);
        repositoryPanel.tunnelCommandTextField.getDocument().addDocumentListener(this);
        
        repositoryPanel.urlComboBox.addItemListener(this);
        
        onSelectedRepositoryChange();
    }
    
    public void refreshUrlHistory(List<RepositoryConnection> recentUrls) {
        Set<RepositoryConnection> recentRoots = new LinkedHashSet<RepositoryConnection>();
        recentRoots.addAll(recentUrls);                               
        
        if(repositoryPanel.urlComboBox.isEditable()) {
            // templates for supported connection methods        
            recentRoots.add(new RepositoryConnection("file:///"));      // NOI18N
            recentRoots.add(new RepositoryConnection("http://"));       // NOI18N
            recentRoots.add(new RepositoryConnection("https://"));      // NOI18N
            recentRoots.add(new RepositoryConnection("svn://"));        // NOI18N
            recentRoots.add(new RepositoryConnection("svn+ssh://"));    // NOI18N
        };
        
        ComboBoxModel rootsModel = new RepositoryModel(new Vector<RepositoryConnection>(recentRoots));                        
        repositoryPanel.urlComboBox.setModel(rootsModel);
        
        if (recentRoots.size() > 0 ) {
            try {
                repositoryPanel.urlComboBox.setSelectedIndex(0);
                refresh(getSelectedRepositoryConnection());
            }
            catch (InterruptedException ex) { }                
        }         
        
        if(repositoryPanel.urlComboBox.isEditable()) {
            JTextComponent textEditor = getUrlComboEditor();
            textEditor.selectAll();            
        }         
    }

    public List<RepositoryConnection> getRecentUrls() {
        ComboBoxModel model = repositoryPanel.urlComboBox.getModel();
        List<RepositoryConnection> ret = new ArrayList<RepositoryConnection>(model.getSize());
        for (int i = 0; i < model.getSize(); i++) {
            ret.add((RepositoryConnection)model.getElementAt(i));
        }
        return ret;
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
        RepositoryConnection rc = null;        
        rc = getSelectedRepositoryConnection();        
        if(rc==null) {
            return; // uups 
        }
        
        SVNUrl repositoryUrl = rc.getSvnUrl();
        if (repositoryUrl.getProtocol().equals("http")  ||    // NOI18N
            repositoryUrl.getProtocol().equals("https") ||    // NOI18N
            repositoryUrl.getProtocol().equals("svn")   ||    // NOI18N
            repositoryUrl.getProtocol().startsWith("svn+") )  // NOI18N
        {                                
            // XXX the way the usr, password and proxy settings are stored is not symetric and consistent...
            SvnConfigFiles.getInstance().setProxy(rc.getProxyDescriptor(), SvnUtils.ripUserFromHost(repositoryUrl.getHost()));
            if(repositoryUrl.getProtocol().startsWith("svn+")) {
                SvnConfigFiles.getInstance().setExternalCommand(getTunnelName(repositoryUrl.getProtocol()), repositoryPanel.tunnelCommandTextField.getText());
            }
        }    
        
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
        Runnable awt = new Runnable() {
            public void run() {
                if (e.getDocument() == repositoryPanel.userTextField.getDocument()) {
                    onUsernameChange();
                } else if (e.getDocument() == repositoryPanel.userPasswordField.getDocument()) {
                    onPasswordChange();
                } else if (e.getDocument() == ((JTextComponent) repositoryPanel.urlComboBox.getEditor().getEditorComponent()).getDocument()) {
                    onSelectedRepositoryChange();
                } else if (e.getDocument() == (repositoryPanel.tunnelCommandTextField.getDocument())) {
                    onTunnelCommandChange();
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

        SelectedRepository sr = null;
        try {
            sr = getSelectedRepository();
        } catch (InterruptedException ex) {
            valid = false;
        }
        valid = sr != null;
        
        if(valid) {            
            if(sr.getUrl().toString().startsWith("svn+") && repositoryPanel.tunnelCommandTextField.getText().trim().equals("")) {
                valid = false;
            }
        }
        
        setValid(valid, message);
        repositoryPanel.proxySettingsButton.setEnabled(valid);
        repositoryPanel.userPasswordField.setEnabled(valid);
        repositoryPanel.userTextField.setEnabled(valid);

        repositoryPanel.removeButton.setEnabled(sr != null && sr.getUrl().toString().length() > 0);
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
                
        if(repository != null) {
            RepositoryModel model = (RepositoryModel) repositoryPanel.urlComboBox.getModel();
            int idx = model.getIndexOf(repository.getUrl().toString());
            if(idx > -1) {
                repositoryPanel.urlComboBox.setSelectedIndex(idx);
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
        if(!repositoryPanel.urlComboBox.isEditable()) {
            Object selection = repositoryPanel.urlComboBox.getSelectedItem();
            if(selection != null) {
                return selection.toString();    
            }
            return "";    
        } else {
            final String[] svnUrl = new String[1];
            try {
                Runnable awt = new Runnable() {
                    public void run() {
                        svnUrl[0] = (String) repositoryPanel.urlComboBox.getEditor().getItem().toString();
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
    }

    public RepositoryConnection getSelectedRepositoryConnection() throws InterruptedException {
        return (RepositoryConnection) repositoryPanel.urlComboBox.getEditor().getItem();
    }
    
    private void onUsernameChange() {
        try {
            RepositoryConnection rc = getSelectedRepositoryConnection();
            if (rc != null) {
                rc.setUsername(repositoryPanel.userTextField.getText());
            }
        } catch (InterruptedException ex) { };        
        setValid(true, "");        
    }
    
    private void onPasswordChange() {
        try {
            RepositoryConnection rc = getSelectedRepositoryConnection();
            if (rc != null) {
                rc.setPassword(new String(repositoryPanel.userPasswordField.getPassword()));
            }
        } catch (InterruptedException ex) { };                
        setValid(true, "");
    }

    private void onTunnelCommandChange() {
        try {
            RepositoryConnection rc = getSelectedRepositoryConnection();
            if (rc != null) {
                rc.setExternalCommand(new String(repositoryPanel.tunnelCommandTextField.getText()));
            }
        } catch (InterruptedException ex) { };                
    }

    private void onRemoveClick() {
        try {
            RepositoryConnection rc = getSelectedRepositoryConnection();
            if (rc != null) {                      
                remove(rc);                                                                
            }
        } catch (InterruptedException ex) {
            // ignore
        };                    
    }    

    public RepositoryPanel getPanel() {
        return repositoryPanel;
    }
    
    public boolean isValid() {
        return valid;
    }

    private void setValid(boolean valid, String message) {
        boolean oldValue = this.valid;
        String oldMessage = this.message;
        this.message = message;
        this.valid = valid;
        fireValidPropertyChanged(oldValue, valid);
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
    
    public void remove(RepositoryConnection toRemove) {
        RepositoryModel model = (RepositoryModel) repositoryPanel.urlComboBox.getModel();        
        model.removeElement(toRemove);        
    }
    
    private ProxyDescriptor getProxyFromConfigFile(SVNUrl url) {
        return SvnConfigFiles.getInstance().getProxyDescriptor(SvnUtils.ripUserFromHost(url.getHost()));
    }    
    
    public void itemStateChanged(ItemEvent evt) {
        if(evt.getStateChange() == ItemEvent.SELECTED) {
            RepositoryConnection rc = (RepositoryConnection) evt.getItem();
            refresh(rc);
            updateVisibility();  
            repositoryPanel.urlComboBox.getEditor().setItem(new RepositoryConnection(rc));
        }        
    }
    
    private void refresh(RepositoryConnection rc) {        
        repositoryPanel.userTextField.setText(rc.getUsername());
        repositoryPanel.userPasswordField.setText(rc.getPassword());
        repositoryPanel.tunnelCommandTextField.setText(rc.getExternalCommand());        
    }

    public boolean show(String title, HelpCtx helpCtx) {
        RepositoryDialogPanel corectPanel = new RepositoryDialogPanel();
        corectPanel.panel.setLayout(new BorderLayout());
        corectPanel.panel.add(getPanel(), BorderLayout.NORTH);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(corectPanel, title); // NOI18N        
        showDialog(dialogDescriptor, helpCtx);
        return dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION;
    }
    
    public Object show(String title, HelpCtx helpCtx, Object[] options) {
        RepositoryDialogPanel corectPanel = new RepositoryDialogPanel();
        corectPanel.panel.setLayout(new BorderLayout());
        corectPanel.panel.add(getPanel(), BorderLayout.NORTH);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(corectPanel, title); // NOI18N        
        if(options!= null) {
            dialogDescriptor.setOptions(options); // NOI18N
        }        
        showDialog(dialogDescriptor, helpCtx);
        return dialogDescriptor.getValue();
    }
    
    private void showDialog(DialogDescriptor dialogDescriptor, HelpCtx helpCtx) {
        dialogDescriptor.setModal(true);
        dialogDescriptor.setHelpCtx(helpCtx);        

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);        
        dialog.setVisible(true);
    }
    
}
