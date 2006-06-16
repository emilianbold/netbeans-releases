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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.ExceptionHandler;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.WizardStepProgressSupport;
import org.netbeans.modules.subversion.config.ProxyDescriptor;
import org.netbeans.modules.subversion.config.SvnConfigFiles;
import org.netbeans.modules.subversion.ui.repository.Repository;
import org.netbeans.modules.subversion.ui.wizards.AbstractStep;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
        
/**
 *
 *
 *
 * @author Tomas Stupka
 */
public class RepositoryStep
        extends AbstractStep
        implements WizardDescriptor.AsynchronousValidatingPanel, PropertyChangeListener
{
    
    private Repository repository;        
    private RepositoryStepPanel panel;    

    private RepositoryFile repositoryFile;    

    private boolean acceptRevision;

    private WizardStepProgressSupport support;

    public RepositoryStep(boolean acceptRevision) {
        this.acceptRevision = acceptRevision;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(RepositoryStep.class);
    }        

    protected JComponent createComponent() {
        if (repository == null) {
            repository = new Repository(true, acceptRevision, org.openide.util.NbBundle.getMessage(RepositoryStep.class, "CTL_Repository_Location")); // NOI18N
            repository.addPropertyChangeListener(this);
            panel = new RepositoryStepPanel();            
            panel.repositoryPanel.setLayout(new BorderLayout());
            panel.repositoryPanel.add(repository.getPanel());
            valid();
        }                        
        return panel;
    }

    protected void validateBeforeNext() {    
        try {
            if(support != null) {
                support.performInCurrentThread(NbBundle.getMessage(RepositoryStep.class, "BK2012")); // NOI18N
            }
        } finally {
            support = null;
        }
    }
    
    public void prepareValidation() {
        support = new RepositoryStepProgressSupport(panel.progressPanel);
        support.startProgress();
    }
    
        
    void storeConfigValues() {
        repository.storeConfigValues();        
    }

    private void storeHistory() {
        repository.storeHistory();
    }
    
    public RepositoryFile getRepositoryFile() {
        return repositoryFile;
    }
    
    private Repository.SelectedRepository getSelectedRepository() {      
        try {
            return repository.getSelectedRepository();
        } catch (Exception ex) {
            invalid(ex.getLocalizedMessage()); 
            return null;
        }
    }                       

    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Repository.PROP_VALID)) {
            if(repository.isValid()) {
                valid(repository.getMessage());
            } else {
                invalid(repository.getMessage());
            }
        }
    }

    public void stop() {
        if(support != null) {
            support.cancel();
        }
    }

    private class RepositoryStepProgressSupport extends WizardStepProgressSupport {

        public RepositoryStepProgressSupport(JPanel panel) {
            super(panel);
        }

        public void perform() {
            final Repository.SelectedRepository selectedRepository = getSelectedRepository();
            if (selectedRepository == null) {
                return;
            }
            String invalidMsg = null;
            try {
                invalid(null);                

                SvnClient client;
                try {
                    String hostString = SvnUtils.ripUserFromHost(selectedRepository.getUrl().getHost());
                    ProxyDescriptor pd = SvnConfigFiles.getInstance().getProxyDescriptor(hostString);
                    client = Subversion.getInstance().getClient(selectedRepository.getUrl(),
                                                                pd,
                                                                repository.getUserName(),
                                                                repository.getPassword());
                } catch (SVNClientException ex) {
                    ErrorManager.getDefault().notify(ex);
                    invalidMsg = ex.getLocalizedMessage();
                    return;
                }

                repositoryFile = null; // reset

                storeConfigValues();
                if(isCanceled()) {
                    return;
                }

                ISVNInfo info = null;
                try {
                    info = client.getInfo(selectedRepository.getUrl());
                } catch (SVNClientException ex) {
                    ExceptionHandler eh = new ExceptionHandler(ex);
                    eh.annotate();
                    invalidMsg = ExceptionHandler.parseExceptionMessage(ex);
                } 
                if(isCanceled()) {
                    return;
                }

                if(info != null) {
                    SVNUrl repositoryUrl = info.getRepository();
                    if(repositoryUrl==null) {
                        // XXX see issue #72810 and #72921. workaround!
                        repositoryUrl = selectedRepository.getUrl();
                    }
                    SVNRevision revision = selectedRepository.getRevision();
                    String[] repositorySegments = repositoryUrl.getPathSegments();
                    String[] selectedSegments = selectedRepository.getUrl().getPathSegments();
                    String[] repositoryFolder = new String[selectedSegments.length - repositorySegments.length];
                    System.arraycopy(selectedSegments, repositorySegments.length,
                                     repositoryFolder, 0,
                                     repositoryFolder.length);
                    try {
                        repositoryFile = new RepositoryFile(repositoryUrl, repositoryFolder, revision);
                    } catch (MalformedURLException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex); // should not happen
                    }
                } else {
                    invalidMsg = org.openide.util.NbBundle.getMessage(RepositoryStep.class, "CTL_Repository_Invalid", selectedRepository.getUrl()); // NOI18N
                    return;
                }
            } finally {
                if(isCanceled()) {
                    valid(org.openide.util.NbBundle.getMessage(RepositoryStep.class, "CTL_Repository_Canceled")); // NOI18N
                } else if(invalidMsg == null) {
                  valid();
                  storeHistory();
                } else {
                  valid(invalidMsg);
                }                
            }
        }

        public void setEditable(boolean editable) {
            repository.setEditable(editable);        
        }        
    };

}

