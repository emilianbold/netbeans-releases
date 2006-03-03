/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.ui.repository;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import org.netbeans.modules.subversion.client.*;
import org.netbeans.modules.subversion.settings.HistorySettings;
import org.netbeans.modules.subversion.settings.PasswordFile;
import org.netbeans.modules.subversion.settings.SvnRootSettings;
import org.netbeans.modules.subversion.ui.repository.ProxySelector;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 *
 *
 * @author Tomas Stupka
 */
public class Repository implements ActionListener, DocumentListener {
    
    private RepositoryPanel repositoryPanel;
    private ProxyDescriptor proxyDescriptor;   
    private RequestProcessor.Task updatePasswordTask;
    private volatile boolean internalDocumentChange;   
    private boolean passwordExpected;        
    private boolean valid = false;
    private boolean userVisitedProxySettings;
    private List listeners;

    public static final String PROP_VALID = "valid";

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
        ProxySelector selector = new ProxySelector();
        selector.setProxyDescriptor(proxyDescriptor);
        ProxyDescriptor pd = selector.selectProxy();
        if (pd != null) {
            proxyDescriptor = pd;            
            userVisitedProxySettings = true;            
        }
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
                    if (passwordFile !=null && passwordFile.getPassword() != null && passwordExpected) {                        
                        //scrambledPassword = password;
                        internalDocumentChange = true;
                        repositoryPanel.userPasswordField.setText(passwordFile.getPassword());
                        repositoryPanel.userTextField.setText(passwordFile.getUsername());
                        internalDocumentChange = false;
                        cancelPasswordUpdate();
                    }
                }
            });
        
        }
        
        Set recentRoots = new LinkedHashSet();
        recentRoots.addAll(HistorySettings.getRecent(HistorySettings.PROP_SVN_URLS));        
        // templates for supported connection methods        
        recentRoots.add("file:");       // NOI18N
        recentRoots.add("http:");       // NOI18N
        recentRoots.add("https:");      // NOI18N
        recentRoots.add("svn:");        // NOI18N
        recentRoots.add("svn+ssh:");    // NOI18N

        ComboBoxModel rootsModel = new DefaultComboBoxModel(new Vector(recentRoots));
        repositoryPanel.urlComboBox.setModel(rootsModel);
        repositoryPanel.urlComboBox.addActionListener(this);
        Component editor = repositoryPanel.urlComboBox.getEditor().getEditorComponent();
        JTextComponent textEditor = (JTextComponent) editor;
        if (recentRoots.size() == 0) {
            textEditor.setText("file:");    // NOI18N
        } else {
            SelectedRepository repository = null;
            try {
                repository = getSelectedRepository();
            } catch (Exception ex) {}
            SVNUrl url = null;
            if(repository!=null) {
                url = repository.getUrl();
            }
            proxyDescriptor = SvnRootSettings.getProxyFor(url);
            schedulePasswordUpdate();
        }
        textEditor.selectAll();
        textEditor.getDocument().addDocumentListener(this);
                        
        onSelectedRepositoryChange();
        return repositoryPanel;
    }

    public void setEditable(boolean editable) {
        repositoryPanel.urlComboBox.setEditable(editable);
        repositoryPanel.userPasswordField.setEditable(editable);
        repositoryPanel.userTextField.setEditable(editable);        
        repositoryPanel.proxySettingsButton.setEnabled(editable);        
    }
    
    public void store() {
        SelectedRepository repository = null;
        try {
            repository = getSelectedRepository();
        } catch (Exception ex) {}
        if(repository==null) {
            return; // uups 
        }
        
        boolean storeProxySettings = false;
        if (repository.getUrl().getProtocol().equals("http")  || // NOI18N          
            repository.getUrl().getProtocol().equals("https") || // NOI18N        
            repository.getUrl().getProtocol().equals("svn")   || // NOI18N        
            repository.getUrl().getProtocol().equals("svn+ssh") )  // NOI18N
        { 
            storeProxySettings = true;
            
            if(repository != null) {
                PasswordFile passwordFile = PasswordFile.findFileForUrl(repository.getUrl());                    
                if(passwordFile != null ) {
                    passwordFile.setPassword(new String(repositoryPanel.userPasswordField.getPassword()));
                    passwordFile.setUsername(repositoryPanel.userTextField.getText());
                    passwordFile.store();   
                } else {
                    // XXX should be stored in some way ...
                }               
            }                                
        }

        if (storeProxySettings) {
            SvnRootSettings.setProxyFor(repository.getUrl(), getProxyDescriptor());
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
        // synchronously access its content from selectedSvnRoot
        if (internalDocumentChange) return;
        Runnable awt = new Runnable() {
            public void run() {
                if (e.getDocument() == repositoryPanel.userPasswordField.getDocument()) {
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
        
    public SelectedRepository getSelectedRepository() throws Exception {    
        String urlString = selectedUrlString();        
        if(urlString == null ) {
            return null;
        }
        try {
            int idx = urlString.lastIndexOf('@');
            SVNRevision revision = null;
            if(idx < 0) {                
                revision = SVNRevision.HEAD;                    
            } else if (acceptRevision) {
                try {                    
                    revision = new SVNRevision.Number(Long.parseLong(urlString.substring(idx+1))); 
                } catch (NumberFormatException ex) {
                    setValid(false, ex.getLocalizedMessage());
                    throw ex;                    
                } 
                urlString = urlString.substring(0, idx);            
            } else {
                throw new MalformedURLException("The only revision allowed here is HEAD!"); 
            }            
            return new SelectedRepository(new SVNUrl (urlString), revision);

        } catch (MalformedURLException ex) {
            setValid(false, ex.getLocalizedMessage());
            throw ex;
        }        
    }
    
    /**
     * Fast url syntax check. It can invalidate the whole step     
     */
    private void validateSvnUrl() {
        try {
            setValid(getSelectedRepository() != null, message);
        } catch (Exception ex) {
            setValid(false, message);
        }
    }
    
    /**
     * On valid SVNUrl loads UI fields from SvnRootSettings.
     * Always updates UI fields visibility.
     */
    private void onSelectedRepositoryChange() {
        SelectedRepository repository = null;
        try {
            repository = getSelectedRepository();
        } catch (Exception ex) {}
        
        if(repository != null) {            
            if (repository.getUrl()!=null) {                   
                if (userVisitedProxySettings == false) {
                    // load  proxy from history
                    proxyDescriptor = SvnRootSettings.getProxyFor(repository.getUrl());
                }
                schedulePasswordUpdate();
            }
        }
        updateVisibility();
    }
    
    /** Shows proper fields depending on Svn connection method. */
    private void updateVisibility() {
        String selectedUrlString = selectedUrlString();
        boolean remoteServerFields = selectedUrlString.startsWith("http:")    ||  // NOI18N        
                                     selectedUrlString.startsWith("https:")   ||  // NOI18N        
                                     selectedUrlString.startsWith("svn:")     ||  // NOI18N        
                                     selectedUrlString.startsWith("svn+ssh:");    // NOI18N        
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
    private String selectedUrlString() {        
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
        } catch (InterruptedException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.notify(e);
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
        String selectedUrlString = selectedUrlString();
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

    private void onPasswordChange() {
        cancelPasswordUpdate();
    }

    public ProxyDescriptor getProxyDescriptor() {
        return proxyDescriptor;
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
        this.message = message;
        if(oldValue != valid) {
            this.valid = valid;
            fireValidPropertyChanged(oldValue, valid);
        };
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
            listeners = new ArrayList();
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
    
}