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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.rt.providers.impl.actions;

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author avk
 */
public abstract class DownloadFilesCommand extends AbstractFilesTransferCommand {

    static final String LBL_DOWNLOAD            = "LBL_DownloadFilesFromServer";

    public static final String DOWNLOAD         = "download"; // NOI18N

    private static final String LBL_FILE_OR_DIR_WRITE_ERROR  
            = "LBL_FileOrDirWriteError";    // NOI18N

    private static final String LBL_DOWNLOADED_COUNT_MSG    
            = "LBL_DownloadedFilesCnt";   // NOI18N
    
    public DownloadFilesCommand(Project project, WebServerProvider provider) 
    {
        this(project, true, provider);
    }

    public DownloadFilesCommand(Project project, boolean notify, 
            WebServerProvider provider) 
    {
        super(project, notify, provider);
    }

    public boolean isEnabled() {
        return true;
    }

    
    public String getLabel() {
        return NbBundle.getMessage(DownloadFilesCommand.class, LBL_DOWNLOAD);
    }

    protected boolean checkDestinationFile(FileObject fileObject){
            if ( !fileObject.isValid() || !fileObject.canWrite() ){
                notifyMsg(LBL_FILE_OR_DIR_WRITE_ERROR , fileObject.getName()  );
                return false;
            }
        return true;
    }
    
    protected void refreshParent(File file){
        FileObject fileObject = FileUtil.toFileObject(file);
        if (fileObject == null){
            return;
        }
        
        FileObject parentObject = fileObject.getParent();
        if (parentObject != null){
            parentObject.refresh();
        }
    }

    protected File getNotExistingTmpFile(File to) {
        File toTmp = null;
        do {
            toTmp = getTmpFile(to);
        } while (toTmp.exists());
        return toTmp;
    }

    private File getTmpFile(File to) {
        File parentFolder = to.getParentFile();
        File toTmp = new File(parentFolder, 
                System.currentTimeMillis() + to.getName() + TMP_FILE_POSTFIX);
        return toTmp;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.Command#getId()
     */
    public String getId() {
        return DOWNLOAD;
    }

    protected void notifyCopiedCount(){
        notifyMsg( LBL_DOWNLOADED_COUNT_MSG, 
                DownloadFilesCommand.class, getCopiedFilesCnt()  );
    }
}
