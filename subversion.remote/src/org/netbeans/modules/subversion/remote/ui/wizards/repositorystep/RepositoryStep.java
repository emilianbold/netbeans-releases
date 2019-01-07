/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.subversion.remote.ui.wizards.repositorystep;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.modules.subversion.remote.RepositoryFile;
import org.netbeans.modules.subversion.remote.Subversion;
import org.netbeans.modules.subversion.remote.SvnModuleConfig;
import org.netbeans.modules.subversion.remote.api.ISVNInfo;
import org.netbeans.modules.subversion.remote.api.SVNClientException;
import org.netbeans.modules.subversion.remote.api.SVNRevision;
import org.netbeans.modules.subversion.remote.api.SVNUrl;
import org.netbeans.modules.subversion.remote.client.SvnClient;
import org.netbeans.modules.subversion.remote.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.remote.client.WizardStepProgressSupport;
import org.netbeans.modules.subversion.remote.ui.repository.Repository;
import org.netbeans.modules.subversion.remote.ui.repository.RepositoryConnection;
import org.netbeans.modules.subversion.remote.ui.wizards.AbstractStep;
import org.netbeans.modules.subversion.remote.util.Context;
import org.netbeans.modules.subversion.remote.util.SvnUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileSystem;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
        
/**
 *
 *
 *
 * 
 */
public class RepositoryStep extends AbstractStep implements WizardDescriptor.AsynchronousValidatingPanel, PropertyChangeListener {

    public static final String IMPORT_HELP_ID = "org.netbeans.modules.subversion.ui.wizards.repositorystep.RepositoryStep.import"; //NOI18N
    public static final String CHECKOUT_HELP_ID = "org.netbeans.modules.subversion.ui.wizards.repositorystep.RepositoryStep.checkout"; //NOI18N
    public static final String URL_PATTERN_HELP_ID = "org.netbeans.modules.subversion.ui.wizards.repositorystep.RepositoryStep.urlPattern"; //NOI18N
    
    private Repository repository;        
    private RepositoryStepPanel panel;    
    private RepositoryFile repositoryFile;    
    private int repositoryModeMask;
    private WizardStepProgressSupport support;
    private final FileSystem fileSystem;
    private final String helpID;
    
    public RepositoryStep(FileSystem fileSystem, int repositoryModeMask, String helpID) {
        this.repositoryModeMask = repositoryModeMask;
        this.helpID = helpID;
        this.fileSystem = fileSystem;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(helpID);
    }        

    @Override
    protected JComponent createComponent() {
        if (repository == null) {         
            repositoryModeMask = repositoryModeMask | Repository.FLAG_URL_EDITABLE | Repository.FLAG_URL_ENABLED | Repository.FLAG_SHOW_HINTS | Repository.FLAG_SHOW_PROXY;
            String title = org.openide.util.NbBundle.getMessage(RepositoryStep.class, "CTL_Repository_Location");       // NOI18N
            repository = new Repository(fileSystem, repositoryModeMask, title); 
            repository.addPropertyChangeListener(this);
            panel = new RepositoryStepPanel();            
            panel.repositoryPanel.add(repository.getPanel());
            Dimension size = panel.getPreferredSize();
            panel.setPreferredSize(new Dimension(size.width, size.height + new JLabel("A").getPreferredSize().height + new JButton("A").getPreferredSize().height + 20)); //NOI18N
            valid();
        }                        
        return panel;
    }

    @Override
    protected void validateBeforeNext() {            
        try {
            support = new RepositoryStepProgressSupport(panel.progressPanel);        
            SVNUrl url = getUrl();
            if (url != null) {
                support.setRepositoryRoot(url);
                RequestProcessor rp = Subversion.getInstance().getRequestProcessor(url);
                RequestProcessor.Task task = support.start(rp, url, NbBundle.getMessage(RepositoryStep.class, "BK2012"));
                task.waitFinished();
            }
        } finally {
            support = null;
        }
    }
    
    @Override
    public void prepareValidation() {                
    }

    private SVNUrl getUrl() {        
        try {
            return getSelectedRepositoryConnection().getSvnUrl();                
        } catch (MalformedURLException mue) {
            // probably a synchronization issue
            invalid(new WizardMessage(mue.getLocalizedMessage(), false));
        }                                
        return null;
    }
    
    private void storeHistory() {        
        RepositoryConnection rc = getSelectedRepositoryConnection();
        if(rc != null) {  
            SvnModuleConfig.getDefault(fileSystem).insertRecentUrl(rc);           
        }        
    }
    
    public RepositoryFile getRepositoryFile() {
        return repositoryFile;
    }               
    
    private RepositoryConnection getSelectedRepositoryConnection() {      
        try {
            return repository.getSelectedRC();
        } catch (Exception ex) {
            invalid(new AbstractStep.WizardMessage(ex.getLocalizedMessage(), false));
            return null;
        }
    }                       

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Repository.PROP_VALID)) {
            if(repository.isValid()) {
                valid(new AbstractStep.WizardMessage(repository.getMessage(), false));
            } else {
                invalid(new AbstractStep.WizardMessage(repository.getMessage(), false));
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
            super(fileSystem, panel);
        }

        @Override
        public void perform() {
            final RepositoryConnection rc = getSelectedRepositoryConnection();
            if (rc == null) {
                return;
            }
            storeHistory();
            AbstractStep.WizardMessage invalidMsg = null;
            try {
                invalid(null);
                Context context = new Context(VCSFileProxy.createFileProxy(fileSystem.getRoot()));
                SvnClient client;
                SVNUrl url = rc.getSvnUrl();
                try {
                    int handledExceptions = (SvnClientExceptionHandler.EX_DEFAULT_HANDLED_EXCEPTIONS) ^ // the default without
                                            (SvnClientExceptionHandler.EX_NO_HOST_CONNECTION |          // host connection errors (misspeled host or proxy urls, ...)
                                             SvnClientExceptionHandler.EX_AUTHENTICATION |              // authentication errors
                                             SvnClientExceptionHandler.EX_SSL_NEGOTIATION_FAILED);      // client cert errors
                    client = Subversion.getInstance().getClient(context, url, rc.getUsername(), rc.getPassword(), handledExceptions);
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(context, ex, true, true);
                    invalidMsg = new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(RepositoryStep.class, "CTL_Repository_Invalid", rc.getUrl()), false);
                    return;
                }
                    
                repositoryFile = null; // reset
                ISVNInfo info = null;    
                try {
                    repository.storeConfigValues();
                    setCancellableDelegate(client);
                    info = client.getInfo(context, url);
                } catch (SVNClientException ex) {
                    if (SvnClientExceptionHandler.isAuthentication(ex.getMessage())) {
                        invalidMsg = new AbstractStep.WizardMessage(NbBundle.getMessage(Repository.class, "MSG_Repository.kenai.insufficientRights.read"), false); //NOI18N
                        return;
                    }
                    annotate(ex);
                    invalidMsg = new AbstractStep.WizardMessage(SvnClientExceptionHandler.parseExceptionMessage(ex), false);
                } 
                if(isCanceled()) {
                    return;
                }

                if(info != null) {
                    SVNUrl repositoryUrl = SvnUtils.decode(info.getRepository());
                    if(repositoryUrl==null) {
                        // XXX see issue #72810 and #72921. workaround!
                        repositoryUrl = rc.getSvnUrl();
                    }
                    SVNRevision revision = rc.getSvnRevision();
                    String[] repositorySegments = repositoryUrl.getPathSegments();
                    String[] selectedSegments = rc.getSvnUrl().getPathSegments();
                    if (selectedSegments.length < repositorySegments.length && SvnUtils.decodeToString(rc.getSvnUrl()).contains("\\")) { //NOI18N
                        // WA for bug #196830 with svnkit: the entered url contains backslashes. While javahl does not like backslashes and a warning is reported earlier, svnkit internally 
                        // translates them into normal slashes and does not complain. However rc.getUrl still returns the url with backslashes
                        invalidMsg = new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(RepositoryStep.class, "CTL_Repository_Invalid", rc.getUrl()), false); // NOI18N
                        return;
                    }
                    String[] repositoryFolder = new String[selectedSegments.length - repositorySegments.length];
                    System.arraycopy(selectedSegments, repositorySegments.length,
                                     repositoryFolder, 0,
                                     repositoryFolder.length);

                    repositoryFile = new RepositoryFile(fileSystem, repositoryUrl, repositoryFolder, revision);
                } else {
                    invalidMsg = new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(RepositoryStep.class, "CTL_Repository_Invalid", rc.getUrl()), false); // NOI18N
                    return;
                }
            } catch (MalformedURLException ex) {
                // probably a synchronization issue
                invalidMsg = new WizardMessage(ex.getLocalizedMessage(), false);
            } finally {
                if(isCanceled()) {
                    valid(new AbstractStep.WizardMessage(org.openide.util.NbBundle.getMessage(RepositoryStep.class, "CTL_Repository_Canceled"), false)); // NOI18N
                } else if(invalidMsg == null) {
                  valid();
                } else {
                  valid(invalidMsg);
                }                
            }
        }

        @Override
        public void setEditable(boolean editable) {
            repository.setEditable(editable);        
        }        
    };

}

