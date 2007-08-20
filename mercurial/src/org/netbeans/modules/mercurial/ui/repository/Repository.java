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

package org.netbeans.modules.mercurial.ui.repository;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.mercurial.HgModuleConfig;
//import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
//import org.tigris.subversion.svnclientadapter.SVNRevision;
//import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * @author Tomas Stupka
 */
public class Repository implements ActionListener, DocumentListener, FocusListener, ItemListener {
    
    public final static int FLAG_URL_EDITABLE           = 2;
    public final static int FLAG_URL_ENABLED            = 4;
    public final static int FLAG_ACCEPT_REVISION        = 8;
    public final static int FLAG_SHOW_REMOVE            = 16;
    public final static int FLAG_SHOW_HINTS             = 32;    
    public final static int FLAG_SHOW_PROXY             = 64;    
    
    private final static String LOCAL_URL_HELP          = "file:///repository_path";              // NOI18N
    private final static String HTTP_URL_HELP           = "http://hostname/repository_path";      // NOI18N
    private final static String HTTPS_URL_HELP          = "https://hostname/repository_path";     // NOI18N
    private final static String STATIC_HTTP_URL_HELP    = "static-http://hostname/repository_path";       // NOI18N
    private final static String SSH_URL_HELP        = "ssh://hostname/repository_path";   // NOI18N   
               
    private RepositoryPanel repositoryPanel;
    private boolean valid = true;
    private List<PropertyChangeListener> listeners;
    
    private RepositoryConnection editedRC;
    
    public static final String PROP_VALID = "valid";                                                    // NOI18N

    private String message;            
    private int modeMask;
    private Dimension maxNeededSize;
    
    public Repository(String titleLabel) {
        this(0, titleLabel);
    }
            
    public Repository(int modeMask, String titleLabel) {
        
        this.modeMask = modeMask;
        
        initPanel();
        
        repositoryPanel.titleLabel.setText(titleLabel);
                                        
        repositoryPanel.urlComboBox.setEditable(isSet(FLAG_URL_EDITABLE));
        repositoryPanel.urlComboBox.setEnabled(isSet(FLAG_URL_ENABLED));        
        repositoryPanel.tunnelHelpLabel.setVisible(isSet(FLAG_SHOW_HINTS));
        repositoryPanel.tipLabel.setVisible(isSet(FLAG_SHOW_HINTS));
        repositoryPanel.removeButton.setVisible(isSet(FLAG_SHOW_REMOVE));        
        
        // retrieve the dialog size for the largest configuration
        updateVisibility("svn+");                                                                       // NOI18N
        maxNeededSize = repositoryPanel.getPreferredSize();
        
        refreshUrlHistory();
    }
    
    //public void selectUrl(SVNUrl url, boolean force) {
    //    DefaultComboBoxModel dcbm = (DefaultComboBoxModel) repositoryPanel.urlComboBox.getModel();
    //    int idx = dcbm.getIndexOf(url.toString());
    //    if(idx > -1) {
    //        dcbm.setSelectedItem(url.toString());    
    //    } else if(force) {
    //        RepositoryConnection rc = new RepositoryConnection(url.toString());
    //        dcbm.addElement(rc);
    //        dcbm.setSelectedItem(rc);    
    //    }                        
    //}
    
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == repositoryPanel.proxySettingsButton) {
            onProxyConfiguration();
        } else if(e.getSource() == repositoryPanel.removeButton) {
            onRemoveClick();
        }  
    }
    
    private void onProxyConfiguration() {
        OptionsDisplayer.getDefault().open("General");              // NOI18N
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
    
    public void refreshUrlHistory() {
        
        List<RepositoryConnection> recentUrls = HgModuleConfig.getDefault().getRecentUrls();
                
        Set<RepositoryConnection> recentRoots = new LinkedHashSet<RepositoryConnection>();
        recentRoots.addAll(recentUrls);                               
        
        if(repositoryPanel.urlComboBox.isEditable()) {
            // templates for supported connection methods        
            recentRoots.add(new RepositoryConnection("file:///"));      // NOI18N
            recentRoots.add(new RepositoryConnection("http://"));       // NOI18N
            recentRoots.add(new RepositoryConnection("https://"));      // NOI18N
            recentRoots.add(new RepositoryConnection("static-http://"));        // NOI18N
            recentRoots.add(new RepositoryConnection("ssh://"));        // NOI18N
        };
        
        ComboBoxModel rootsModel = new RepositoryModel(new Vector<RepositoryConnection>(recentRoots));                        
        repositoryPanel.urlComboBox.setModel(rootsModel);
        
        if (recentRoots.size() > 0 ) {         
            repositoryPanel.urlComboBox.setSelectedIndex(0);
            refresh(getSelectedRC());         
        }         
        
        if(repositoryPanel.urlComboBox.isEditable()) {
            JTextComponent textEditor = getUrlComboEditor();
            textEditor.selectAll();            
        }         
        updateVisibility();
    }

    public void storeRecentUrls() {
        HgModuleConfig.getDefault().setRecentUrls(getRecentUrls());
    }
    
    private List<RepositoryConnection> getRecentUrls() {
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
    
    public void storeConfigValues() {
        RepositoryConnection rc = getSelectedRC();        
        if(rc==null) {
            return; // uups 
        }
        
        //try {
            //SVNUrl repositoryUrl = rc.getSvnUrl();
            //if(repositoryUrl.getProtocol().startsWith("svn+")) {
            //    SvnConfigFiles.getInstance().setExternalCommand(getTunnelName(repositoryUrl.getProtocol()), repositoryPanel.tunnelCommandTextField.getText());
            //}    
        //} catch (MalformedURLException mue) {
            // should not happen
        //    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, mue); 
        //}
        
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
                if (e.getDocument() == repositoryPanel.userTextField.getDocument()) {
                    onUsernameChange();
                } else if (e.getDocument() == repositoryPanel.userPasswordField.getDocument()) {
                    onPasswordChange();
                } else if (e.getDocument() == ((JTextComponent) repositoryPanel.urlComboBox.getEditor().getEditorComponent()).getDocument()) {
                    onSelectedRepositoryChange();
                } else if (e.getDocument() == (repositoryPanel.tunnelCommandTextField.getDocument())) {
                    onTunnelCommandChange();
                }
                validateHgUrl();
            }
        };
        SwingUtilities.invokeLater(awt);
    }
            
    /**
     * Fast url syntax check. It can invalidate the whole step
     */
    private void validateHgUrl() {
        boolean valid = true;

        RepositoryConnection rc = null; 
        try {
            rc = getSelectedRC();            
            // check for a valid svnurl
            rc.getURL();                             
            //if(!isSet(FLAG_ACCEPT_REVISION) && !rc.getSvnRevision().equals(SVNRevision.HEAD)) 
            //{
            //    message = NbBundle.getMessage(Repository.class, "MSG_Repository_OnlyHEADRevision");
            //    valid = false;
            //} else {
            //      // check for a valid svnrevision
            //    rc.getSvnRevision();
            //}
        } catch (Exception ex) {             
            message = ex.getLocalizedMessage();
            valid = false;
        }        
        
        if(valid) {            
            valid = rc != null && !rc.getUrl().equals("");
            //if(rc.getUrl().startsWith("svn+") && repositoryPanel.tunnelCommandTextField.getText().trim().equals("")) {
            //    valid = false;
            //}
        }
        
        setValid(valid, message);
        repositoryPanel.proxySettingsButton.setEnabled(valid);
        repositoryPanel.userPasswordField.setEnabled(valid);
        repositoryPanel.userTextField.setEnabled(valid);

        repositoryPanel.removeButton.setEnabled(rc != null && rc.getUrl().length() > 0);
    }
    
    /**    
     * Always updates UI fields visibility.
     */
    private void onSelectedRepositoryChange() {
        setValid(true, "");                                                                            // NOI18N     
        String urlString = "";                                                                         // NOI18N         
        try {
            urlString = getUrlString();
        } catch (InterruptedException ex) {
            return; // should not happen
        }
                
        if(urlString != null) {
                       
            RepositoryConnection editedrc = getEditedRC();
            editedrc.setUrl(urlString);
            
            DefaultComboBoxModel dcbm = (DefaultComboBoxModel) repositoryPanel.urlComboBox.getModel();                
            int idx = dcbm.getIndexOf(editedrc);       
            if(idx > -1) {
                //dcbm.setSelectedItem(urlString);                                                
                refresh((RepositoryConnection)dcbm.getElementAt(idx));                
            } 
            if(urlString.startsWith("svn+")) {
                String tunnelName = getTunnelName(urlString).trim();
                if( repositoryPanel.tunnelCommandTextField.getText().trim().equals("") && 
                    tunnelName != null && 
                    !tunnelName.equals("") ) 
                {
                    //repositoryPanel.tunnelCommandTextField.setText(SvnConfigFiles.getInstance().getExternalCommand(tunnelName));
                } 
            }     
            
            editedrc.setUsername(repositoryPanel.userTextField.getText());
            editedrc.setPassword(new String(repositoryPanel.userPasswordField.getPassword()));
            editedrc.setExternalCommand(repositoryPanel.tunnelCommandTextField.getText());                                               
        }
        message = "";                                                                                   // NOI18N
        updateVisibility();
    }            

    private RepositoryConnection getEditedRC() {
        if(editedRC == null) {
            editedRC = new RepositoryConnection("");
        }
        return editedRC;
    }

    private void updateVisibility() {
        try {
            updateVisibility(getUrlString());
        } catch (InterruptedException ex) {
            return;
        }        
    }   
    
    /** Shows proper fields depending on Mercurial connection method. */
    private void updateVisibility(String selectedUrlString) {

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
        } else if(selectedUrlString.startsWith("static-http:")) {                       // NOI18N
            repositoryPanel.tipLabel.setText(STATIC_HTTP_URL_HELP);
            authFields = true;
            proxyFields = true;
        } else if(selectedUrlString.startsWith("ssh")) {                        // NOI18N
            repositoryPanel.tipLabel.setText(getSVNTunnelTip(selectedUrlString));
            sshFields = true;
        } else if(selectedUrlString.startsWith("file:")) {                      // NOI18N
            repositoryPanel.tipLabel.setText(LOCAL_URL_HELP);
        } else {
            repositoryPanel.tipLabel.setText(NbBundle.getMessage(Repository.class, "MSG_Repository_Url_Help", new Object [] { // NOI18N
                LOCAL_URL_HELP, HTTP_URL_HELP, HTTPS_URL_HELP, STATIC_HTTP_URL_HELP, SSH_URL_HELP
            }));
        }

        /*
        repositoryPanel.userPasswordField.setVisible(authFields);
        repositoryPanel.passwordLabel.setVisible(authFields);          
        repositoryPanel.userTextField.setVisible(authFields);          
        repositoryPanel.leaveBlankLabel.setVisible(authFields);        
        repositoryPanel.userLabel.setVisible(authFields);             
        repositoryPanel.proxySettingsButton.setVisible(proxyFields && ((modeMask & FLAG_SHOW_PROXY) != 0));        
        repositoryPanel.tunnelCommandTextField.setVisible(sshFields);        
        repositoryPanel.tunnelCommandLabel.setVisible(sshFields);        
        repositoryPanel.tunnelLabel.setVisible(sshFields);        
        repositoryPanel.tunnelHelpLabel.setVisible(sshFields);       
        */
        repositoryPanel.userPasswordField.setVisible(false);
        repositoryPanel.passwordLabel.setVisible(false);          
        repositoryPanel.userTextField.setVisible(false);          
        repositoryPanel.leaveBlankLabel.setVisible(false);        
        repositoryPanel.userLabel.setVisible(false);             
        repositoryPanel.proxySettingsButton.setVisible(false);        
        repositoryPanel.tunnelCommandTextField.setVisible(false);        
        repositoryPanel.tunnelCommandLabel.setVisible(false);        
        repositoryPanel.tunnelLabel.setVisible(false);        
        repositoryPanel.tunnelHelpLabel.setVisible(false);       
    }

    private String getSVNTunnelTip(String urlString) {
        String tunnelName = getTunnelName(urlString);
        return MessageFormat.format(SSH_URL_HELP, tunnelName).trim();
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
    private String getUrlString() throws InterruptedException {        
        if(!repositoryPanel.urlComboBox.isEditable()) {
            Object selection = repositoryPanel.urlComboBox.getSelectedItem();
            if(selection != null) {
                return selection.toString().trim();    
            }
            return "";    
        } else {
            final String[] hgUrl = new String[1];
            try {
                Runnable awt = new Runnable() {
                    public void run() {
                        hgUrl[0] = (String) repositoryPanel.urlComboBox.getEditor().getItem().toString().trim();
                    }
                };
                if (SwingUtilities.isEventDispatchThread()) {
                    awt.run();
                } else {
                    SwingUtilities.invokeAndWait(awt);
                }
                return hgUrl[0].trim();
            } catch (InvocationTargetException e) {
                ErrorManager err = ErrorManager.getDefault();
                err.notify(e);
            }
            return null;            
        }
    }

    public RepositoryConnection getSelectedRC() {
        String urlString;
        try {
            urlString = getUrlString();            
        }
        catch (InterruptedException ex) {
            // should not happen
            ErrorManager.getDefault().notify(ex);
            return null;
        };
        
        DefaultComboBoxModel dcbm = (DefaultComboBoxModel) repositoryPanel.urlComboBox.getModel();                
        int idx = dcbm.getIndexOf(urlString);        
        
        if(idx > -1) {
            return (RepositoryConnection) dcbm.getElementAt(idx);
        }        
        return getEditedRC();        
    }
    
    private void onUsernameChange() {
        RepositoryConnection rc = getSelectedRC();
        if (rc != null) {
            rc.setUsername(repositoryPanel.userTextField.getText());
        }
        setValid(true, "");        
    }
    
    private void onPasswordChange() {        
        RepositoryConnection rc = getSelectedRC();
        if (rc != null) {
            rc.setPassword(new String(repositoryPanel.userPasswordField.getPassword()));
        }        
        setValid(true, "");
    }

    private void onTunnelCommandChange() {
        RepositoryConnection rc = getSelectedRC();
        if (rc != null) {
            rc.setExternalCommand(repositoryPanel.tunnelCommandTextField.getText());
        }        
    }

    private void onRemoveClick() {
        RepositoryConnection rc = getSelectedRC();
        if (rc != null) {                      
            remove(rc);                                                                
        }        
    }    

    public RepositoryPanel getPanel() {
        return repositoryPanel;
    }
    
    public boolean isValid() {
        return valid;
    }

    private void setValid(boolean valid, String message) {
        boolean oldValue = this.valid;
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

    public void itemStateChanged(ItemEvent evt) {
        if(evt.getStateChange() == ItemEvent.SELECTED) {
            RepositoryConnection rc = (RepositoryConnection) evt.getItem();
            refresh(rc);
            updateVisibility();  
            editedRC = new RepositoryConnection(rc);           
        } else if(evt.getStateChange() == ItemEvent.DESELECTED) {
            updateVisibility();  
        }       
    }
    
    private void refresh(RepositoryConnection rc) {        
        repositoryPanel.userTextField.setText(rc.getUsername());
        repositoryPanel.userPasswordField.setText(rc.getPassword());        
        repositoryPanel.tunnelCommandTextField.setText(rc.getExternalCommand());           
    } 

    public boolean show(String title, HelpCtx helpCtx, boolean setMaxNeddedSize) {
        RepositoryDialogPanel corectPanel = new RepositoryDialogPanel();
        corectPanel.panel.setLayout(new BorderLayout());
        JPanel p = getPanel();
        if(setMaxNeddedSize) {
            p.setPreferredSize(maxNeededSize);
        }        
        corectPanel.panel.add(p, BorderLayout.NORTH);
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

    private boolean isSet(int flag) {
        return (modeMask & flag) != 0;
    }
    
    public class RepositoryModel  extends DefaultComboBoxModel {

        public RepositoryModel(Vector v) {
            super(v);
        }

        public void setSelectedItem(Object obj) {
            if(obj instanceof String) {
                int idx = getIndexOf(obj);
                if(idx > -1) {
                    obj = getElementAt(idx);
                } else {
                    obj = createNewRepositoryConnection((String) obj);                   
                }                
            }            
            super.setSelectedItem(obj);
        }

        public int getIndexOf(Object obj) {
            if(obj instanceof String) {
                obj = createNewRepositoryConnection((String)obj);                
            }
            return super.getIndexOf(obj);
        }

        public void addElement(Object obj) {
            if(obj instanceof String) {
                obj = createNewRepositoryConnection((String)obj);                
            }
            super.addElement(obj);
        }

        public void insertElementAt(Object obj,int index) {
            if(obj instanceof String) {
                String str = (String) obj;
                RepositoryConnection rc = null;
                try {
                    rc = (RepositoryConnection) getElementAt(index);                    
                } catch (ArrayIndexOutOfBoundsException e) {
                }
                if(rc != null) {
                    rc.setUrl(str);
                    obj = rc;
                }                
                obj = createNewRepositoryConnection(str);
            } 
            super.insertElementAt(obj, index);
        }         

        public void removeElement(Object obj) {
            int index = getIndexOf(obj);
            if ( index != -1 ) {
                removeElementAt(index);
            }
        }
        
        private RepositoryConnection createNewRepositoryConnection(String url) {
            editedRC.setUrl(url);
            return new RepositoryConnection(editedRC);
        }
    }    
}
