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

package org.netbeans.modules.subversion.ui.wizards.repositorystep;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.ExceptionHandler;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.WizardStepProgressSupport;
import org.netbeans.modules.subversion.config.ProxyDescriptor;
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
            repository = new Repository(SvnModuleConfig.getDefault().getRecentUrls(), true, acceptRevision, 
                                        null, org.openide.util.NbBundle.getMessage(RepositoryStep.class, "CTL_Repository_Location")); // NOI18N
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
        SVNUrl url = null;
        try {
            url = repository.getSelectedRepository().getUrl();
        } catch (InterruptedException e) {
            // ignore
        }
        support.setRepositoryRoot(url);
        support.startProgress();
    }

    private void storeHistory() {        
        Repository.SelectedRepository selection = getSelectedRepository();
        if(selection != null) {  
            SvnModuleConfig.getDefault().insertRecentUrl(getSelectedRepository().getUrl().toString());           
        }        
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
                    ProxyDescriptor pd = repository.getProxyDescriptor();
                    client = Subversion.getInstance().getClient(selectedRepository.getUrl(),
                                                                pd,
                                                                repository.getUserName(),
                                                                repository.getPassword(),
                                                                ExceptionHandler.EX_DEFAULT_HANDLED_EXCEPTIONS ^ // the default without
                                                                (ExceptionHandler.EX_NO_HOST_CONNECTION |        // host connection errors (misspeled host or proxy urls, ...)
                                                                 ExceptionHandler.EX_AUTHENTICATION) );          // authentication errors 
                } catch (SVNClientException ex) {
                    ErrorManager.getDefault().notify(ex);
                    invalidMsg = ex.getLocalizedMessage();
                    return;
                }

                repositoryFile = null; // reset

                ISVNInfo info = null;
                try {
                    try {
                        repository.storeConfigValues(); 
                    } catch (InterruptedException ex) {
                        stop();
                    }
                    if(isCanceled()) {
                        return;
                    }
                
                    info = client.getInfo(selectedRepository.getUrl());
                } catch (SVNClientException ex) {
                    annotate(ex);
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

