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

package org.netbeans.modules.subversion.ui.wizards.repositorystep;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.ProxyDescriptor;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.settings.HistorySettings;
import org.netbeans.modules.subversion.settings.PasswordFile;
import org.netbeans.modules.subversion.settings.SvnRootSettings;
import org.netbeans.modules.subversion.ui.wizards.AbstractStep;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

// XXX check against CVS RepositoryStep
// XXX error output isn't very userfriendly
/**
 *
 *
 *
 * @author Tomas Stupka
 */
public class RepositoryStep
        extends AbstractStep
        implements WizardDescriptor.AsynchronousValidatingPanel, 
                   ActionListener, 
                   DocumentListener {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private RepositoryPanel repositoryPanel;
    private ProxyDescriptor proxyDescriptor;
    private RepositoryFile repositoryFile = null;
    private RequestProcessor.Task updatePasswordTask;
    private volatile boolean internalDocumentChange;
    
    private boolean passwordExpected;

    private JLabel progressLabel;
    private ProgressHandle progress;
    private JComponent progressComponent;        
    
    private Thread backgroundValidationThread;

    private boolean userVisitedProxySettings;
    
    private class SelectedRepository {       
        final SVNUrl url;
        final SVNRevision revision;
        SelectedRepository (SVNUrl url, SVNRevision revision) {
            this.url = url;
            this.revision = revision;
        }
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(RepositoryStep.class);
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
            valid();
        }
    }    

    protected JComponent createComponent() {
        if (repositoryPanel == null) {
            repositoryPanel = new RepositoryPanel();
            repositoryPanel.proxySettingsButton.addActionListener(this);
                                    
            RequestProcessor requestProcessor = new RequestProcessor();
            updatePasswordTask = requestProcessor.create(new Runnable() {
                public void run() {
                    SelectedRepository repository = getSelectedRepository();
                    if(repository == null) {
                        return;
                    }                    
                    PasswordFile passwordFile = PasswordFile.findFileForUrl(repository.url);
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
//        if (preferedCvsRoot != null) { // XXX is there somthing like a preffered root???
//            recentRoots.add(preferedCvsRoot);
//        }
        recentRoots.addAll(HistorySettings.getRecent(HistorySettings.PROP_SVN_URLS));
//        if (initialCvsRoot != null) { // XXX is there somthing like a initial root???
//            // it's first => initially selected
//            recentRoots.add(initialCvsRoot);
//        }
        
          // XXX is there somethig like this ???
//        Iterator cvsPassRoots = PasswordsFile.listRoots(":pserver:").iterator();  // NOI18N
//        while (cvsPassRoots.hasNext()) {
//            String next = (String) cvsPassRoots.next();
//            if (recentRoots.contains(next) == false) {
//                recentRoots.add(next);
//            }
//        }
        
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
            textEditor.setText(":file");    // NOI18N
        } else {
            SelectedRepository repository = getSelectedRepository();
            SVNUrl url = null;
            if(repository!=null) {
                url = repository.url;
            }
            proxyDescriptor = SvnRootSettings.getProxyFor(url);
            schedulePasswordUpdate();
        }
        textEditor.selectAll();
        textEditor.getDocument().addDocumentListener(this);
                
        valid();
        onSelectedRepositoryChange();
        return repositoryPanel;
    }

    protected void validateBeforeNext() {    
        final SelectedRepository selectedRepository = getSelectedRepository();
        if (selectedRepository==null) {
            return;
        }
        
        backgroundValidationThread = Thread.currentThread();
        final SvnClient client;
        try {
            client = Subversion.getInstance().getClient(selectedRepository.url, 
                                                        getProxyDescriptor(), 
                                                        repositoryPanel.userTextField.getText(),
                                                        new String(repositoryPanel.userPasswordField.getPassword()));
        } catch (SVNClientException ex) {
            ex.printStackTrace(); // XXX
            invalid(null);                
            return; 
        }
         
        repositoryFile = null; // reset
        
        final String invalidMsg[] = new String[1]; // ret value
        Runnable worker = new Runnable() {
            private void fail(String msg) {
                invalidMsg[0] = msg;
            }
            public void run() {
                invalid(null);                

                ISVNInfo info = null;
                
                try {                                    
                    info = client.getInfo(selectedRepository.url);                                                                
                    
                    // XXX set the plain text selectedRepository name as some kind of title in the browser panel .....                            
                } catch (SVNClientException ex) {                                    
                    invalidMsg[0] = ex.getLocalizedMessage();
                    // XXX the logic for authentication problems can't be implemented here 
                    //     -> some server configurations don't authenticate readonly commands
                }                                
                
                if(info != null) {
                    SVNUrl repositoryUrl = info.getRepository();
                    SVNRevision revision = selectedRepository.revision; 
                    // XXX
//                    if(Long.getLong(revision.toString()).longValue() > 
//                       Long.getLong(info.getRevision().toString()).longValue()) 
//                    {
//                        invalidMsg[0] = "Revision does not exist"; // XXX
//                        return;
//                    }                    
                    
                    String[] repositorySegments = repositoryUrl.getPathSegments();
                    String[] selectedSegments = selectedRepository.url.getPathSegments();
                    String[] repositoryFolder = new String[selectedSegments.length - repositorySegments.length];
                    System.arraycopy(selectedSegments, repositorySegments.length, 
                                     repositoryFolder, 0, 
                                     repositoryFolder.length);
                    try {                        
                        repositoryFile = new RepositoryFile(repositoryUrl, repositoryFolder, revision);                                                                                                        
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace(); // should not happen
                    }
                }                
            }
        };

        Thread workerThread = new Thread(worker, "SVN I/O Probe ");  // NOI18N
        workerThread.start();
        try {
            workerThread.join();
            if (invalidMsg[0] == null) {
                valid();
                storeValidValues();
            } else {
                valid(invalidMsg[0]);
            }
        } catch (InterruptedException e) {
            invalid(org.openide.util.NbBundle.getMessage(RepositoryStep.class, "BK2023"));  // NOI18N
            ErrorManager err = ErrorManager.getDefault();
            err.annotate(e, "Passing interrupt to possibly uninterruptible nested thread: " + workerThread);  // NOI18N
            workerThread.interrupt();
            err.notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            backgroundValidationThread = null;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    validationDone();
                }
            });
        }    
    }

    public void prepareValidation() {
        progress = ProgressHandleFactory.createHandle(NbBundle.getMessage(RepositoryStep.class, "BK2012")); // NOI18N
        JComponent bar = ProgressHandleFactory.createProgressComponent(progress);
        JButton stopButton = new JButton(org.openide.util.NbBundle.getMessage(RepositoryStep.class, "BK2022")); // NOI18N
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stop();
            }
        });
        progressComponent = new JPanel();
        progressComponent.setLayout(new BorderLayout(6, 0));
        progressLabel = new JLabel();
        progressComponent.add(progressLabel, BorderLayout.NORTH);
        progressComponent.add(bar, BorderLayout.CENTER);
        progressComponent.add(stopButton, BorderLayout.LINE_END);
        progress.start(/*2, 5*/);
        repositoryPanel.progressPanel.setVisible(true);
        repositoryPanel.progressPanel.add(progressComponent, BorderLayout.SOUTH);
        repositoryPanel.progressPanel.revalidate();

        editable(false);
    }

    public void stop() {
        if (backgroundValidationThread != null) {
            backgroundValidationThread.interrupt();
        }
    }
    
    private void progress(String message) {
        if (progressLabel != null) {
            progressLabel.setText(message);
        }
    }

    private void validationDone() {
        progress.finish();
        repositoryPanel.progressPanel.remove(progressComponent);
        repositoryPanel.progressPanel.revalidate();
        repositoryPanel.progressPanel.repaint();
        repositoryPanel.progressPanel.setVisible(false);
        editable(true);
    }
    
    private void editable(boolean editable) {
        repositoryPanel.urlComboBox.setEditable(editable);
        repositoryPanel.userPasswordField.setEditable(editable);
        repositoryPanel.userTextField.setEditable(editable);        
        repositoryPanel.proxySettingsButton.setEnabled(editable);        
    }
    
    void storeValidValues() {
        SelectedRepository repository = getSelectedRepository();
        if(repository==null) {
            return; // uups 
        }
        
        boolean storeProxySettings = false;
        if (repository.url.getProtocol().equals("http")  || // NOI18N          
            repository.url.getProtocol().equals("https") || // NOI18N        
            repository.url.getProtocol().equals("svn")   || // NOI18N        
            repository.url.getProtocol().equals("svn+ssh") )  // NOI18N
        { 
            storeProxySettings = true;
            
            if(repository != null) {
                PasswordFile passwordFile = PasswordFile.findFileForUrl(repository.url);                    
                if(passwordFile != null ) {
                    passwordFile.setPassword(new String(repositoryPanel.userPasswordField.getPassword()));
                    passwordFile.setUsername(repositoryPanel.userTextField.getText());
                    passwordFile.store();   
                } else {
                    // XXX should be stored in some way ...
                }               
            }                    
            
            
//        if (svnUrl. .startsWith(":pserver:")) { // NOI18N
//            storeProxySettings = true;
//            try {
//                // CVSclient library reads password directly from .cvspass file
//                // store it here into the file. It's potentionally necessary for
//                // next step branch and module browsers
//
//                PasswordsFile.storePassword(root, getScrambledPassword());
//            } catch (IOException e) {
//                ErrorManager err = ErrorManager.getDefault();
//                err.annotate(e, org.openide.util.NbBundle.getMessage(RepositoryStep.class, "BK2020"));
//                err.notify(e);
//            }
//        } 
        }

        if (storeProxySettings) {
            SvnRootSettings.setProxyFor(repository.url, getProxyDescriptor());
        }

        HistorySettings.addRecent(HistorySettings.PROP_SVN_URLS, repository.url.toString());
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
        // synchronously access its content from selectedCvsRoot
        if (internalDocumentChange) return;
        Runnable awt = new Runnable() {
            public void run() {
                if (e.getDocument() == repositoryPanel.userPasswordField.getDocument()) {
                    onPasswordChange();
                } else if (e.getDocument() == ((JTextComponent) repositoryPanel.urlComboBox.getEditor().getEditorComponent()).getDocument()) {
                    onSelectedRepositoryChange();
                } else if (e.getDocument() == repositoryPanel.userTextField.getDocument()) {
                    valid();
                    validateSvnUrl();
                }
            }
        };
        SwingUtilities.invokeLater(awt);
    }
    
    public RepositoryFile getRepositoryFile() {
        return repositoryFile;
    }
    
    private SelectedRepository getSelectedRepository() {     // XXX rename   
        String urlString = selectedUrlString();        
        if(urlString == null ) {
            return null;
        }
        try {
            int idx = urlString.lastIndexOf('@');
            SVNRevision revision = null;
            if(idx < 0) {                
                revision = SVNRevision.HEAD;                    
            } else {                
                try {                    
                    revision = new SVNRevision.Number(Long.parseLong(urlString.substring(idx+1))); 
                } catch (NumberFormatException ex) {
                    invalid(ex.getLocalizedMessage()); 
                    return null; // could be a typo 
                } 
                urlString = urlString.substring(0, idx);            
            }               
            // XXX what if this is used for the import wizard? the only revision which make sense is HEAD!
            return new SelectedRepository(new SVNUrl (urlString), revision);    
        } catch (MalformedURLException ex) {
            invalid(ex.getLocalizedMessage()); 
        }        
        return null;
    }
    
    /**
     * Fast url syntax check. It can invalidate the whole step     
     */
    private boolean validateSvnUrl() {        
        return getSelectedRepository() != null;
    }
    
    /**
     * On valid SVNUrl loads UI fields from SvnRootSettings.
     * Always updates UI fields visibility.
     */
    private void onSelectedRepositoryChange() {
        repositoryFile = null;
        SelectedRepository repository = getSelectedRepository();
        if(repository != null) {            
            if (repository.url!=null) {                   
                 valid();
                if (userVisitedProxySettings == false) {
                    // load  proxy from history
                    proxyDescriptor = SvnRootSettings.getProxyFor(repository.url);
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
//        if (initialCvsRoot != null) { 
//            return initialCvsRoot;
//        }
        
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
        // scrambledPassword = null; 
        valid();
    }

    public ProxyDescriptor getProxyDescriptor() {
        return proxyDescriptor;
    }

}

