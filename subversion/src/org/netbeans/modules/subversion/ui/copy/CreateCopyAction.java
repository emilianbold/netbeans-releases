/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.subversion.ui.copy;

import java.io.File;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.ui.actions.ContextAction;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.openide.nodes.Node;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
public class CreateCopyAction extends ContextAction {
    
    /** Creates a new instance of CreateCopyAction */
    public CreateCopyAction() {
        
    }

    protected String getBaseName(Node[] activatedNodes) {
        return "CTL_MenuItem_Copy";    // NOI18N
    }

    protected int getFileEnabledStatus() {
        return    FileInformation.STATUS_MANAGED
               & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED;
    }

    protected int getDirectoryEnabledStatus() {
        return    FileInformation.STATUS_MANAGED 
               & ~FileInformation.STATUS_NOTVERSIONED_EXCLUDED 
               & ~FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY;
    }
    
    protected boolean enable(Node[] nodes) {
        return nodes != null && nodes.length == 1 &&  getContext(nodes).getRoots().size() > 0;
    }   
    
    protected void performContextAction(final Node[] nodes) {
        
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }
        
        Context ctx = getContext(nodes);

        File root = SvnUtils.getActionRoot(ctx);
        if(root == null) return;
        File[] files = Subversion.getInstance().getStatusCache().listFiles(ctx, FileInformation.STATUS_LOCAL_CHANGE);       
        
        final boolean hasChanges = files.length > 0; 
        final SVNUrl repositoryUrl; 
        final SVNUrl fileUrl;        
        try {            
            repositoryUrl = SvnUtils.getRepositoryRootUrl(root); 
            fileUrl = SvnUtils.getRepositoryUrl(root);        
        } catch (SVNClientException ex) {
            SvnClientExceptionHandler.notifyException(ex, true, true);
            return;
        }                   
        final RepositoryFile repositoryFile = new RepositoryFile(repositoryUrl, fileUrl, SVNRevision.HEAD);        
        
        final CreateCopy createCopy = new CreateCopy(repositoryFile, root, hasChanges);
        
        if(createCopy.showDialog()) {
            // XXX don't close dialog if error occures!
            ContextAction.ProgressSupport support = new ContextAction.ProgressSupport(this,  nodes) {
                public void perform() {
                    performCopy(createCopy, this);
                }
            };
            support.start(createRequestProcessor(nodes));
        }
    }

    private void performCopy(CreateCopy createCopy, SvnProgressSupport support) {
        RepositoryFile toRepositoryFile = createCopy.getToRepositoryFile();                
        
        try {                
            SvnClient client;
            try {
                client = Subversion.getInstance().getClient(toRepositoryFile.getRepositoryUrl());
            } catch (SVNClientException ex) {
                SvnClientExceptionHandler.notifyException(ex, true, true);
                return;
            }

            if(!toRepositoryFile.isRepositoryRoot()) {
                SVNUrl folderToCreate = toRepositoryFile.removeLastSegment().getFileUrl();
                ISVNInfo info = null;
                try{
                    info = client.getInfo(folderToCreate);                                                                
                } catch (SVNClientException ex) {                                
                    if(!SvnClientExceptionHandler.isWrongUrl(ex.getMessage())) { 
                        throw ex;
                    }
                }            

                if(support.isCanceled()) {
                    return;
                }

                if(info == null) {
                    client.mkdir(folderToCreate,
                                 true, 
                                 "[Netbeans SVN client generated message: create a new folder for the copy]: '\n" + createCopy.getMessage() + "\n'"); // NOI18N
                } else {
                    if(createCopy.getLocalFile().isFile()) {
                        // we are copying a file to a destination which already exists. Even if it's a folder - we don't use the exactly same
                        // as the commandline svn client:
                        // - the remote file specified in the GUI has to be exactly the file which has to be created at the repository.
                        // - if the destination is an existent folder the file won't be copied into it, as the svn client would do.
                        throw new SVNClientException("File allready exists");                                                             
                    } else {
                        // XXX warnig: do you realy want to? could be already a project folder!
                    }
                }                           
            }                        

            if(support.isCanceled()) {
                return;
            }

            if(createCopy.isLocal()) {
                client.copy(createCopy.getLocalFile(), 
                            toRepositoryFile.getFileUrl(), 
                            createCopy.getMessage());            
            } else {               
                RepositoryFile fromRepositoryFile = createCopy.getFromRepositoryFile();                
                client.copy(fromRepositoryFile.getFileUrl(), 
                            toRepositoryFile.getFileUrl(), 
                            createCopy.getMessage(), 
                            fromRepositoryFile.getRevision());
            }                            
            
            if(support.isCanceled()) {
                return;
            }

            if(createCopy.switchTo()) {
                SwitchToAction.performSwitch(toRepositoryFile, createCopy.getLocalFile(), support);
            }            

        } catch (SVNClientException ex) {
            support.annotate(ex);
        }
    }
}
