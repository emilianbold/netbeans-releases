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
package org.netbeans.modules.php.rt.providers.impl.actions;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.rt.spi.providers.Command;
import org.netbeans.modules.php.rt.spi.providers.WebServerProvider;
import org.netbeans.modules.php.rt.utils.ActionsDialogs;
import org.netbeans.modules.php.rt.utils.ServerActionsPreferences;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author avk
 */
public abstract class AbstractFilesTransferCommand extends AbstractCommand
    implements Command
{

    static final String LBL_TRANSFER_STARTED_MSG = "LBL_TransferStartedMsg"; // NOI18N

    static final String LBL_TRANSFER_FINISHED_MSG = "LBL_TransferFinishedMsg"; // NOI18N

    static final String LBL_TRANSFER_FINISHED_STATUS
            = "LBL_TransferFinishedStatus"; // NOI18N

    private static final String LBL_COPIED_COUNT_MSG
            = "LBL_CopiedFilesCnt";   // NOI18N
    
    private static final String LBL_SKIPPED_TITLE_MSG
            = "LBL_SkippedFiles_Title";   // NOI18N
    
    private static final String LBL_SKIPPED_FILE_MSG
            = "LBL_SkippedFilePath";   // NOI18N
    
    private static final String LBL_REPLACE_SKIPPED_BY_USER_TITLE_MSG
            = "LBL_ReplaceRejectedByUser_Title";   // NOI18N
    
    private static final String LBL_REPLACE_SKIPPED_BY_OPTION_TITLE_MSG
            = "LBL_ReplaceRejectedByOption_Title";   // NOI18N
    
    public AbstractFilesTransferCommand(Project project, boolean notify, WebServerProvider provider) {
        super(project,provider);

        doNotify = notify;
        PROJECT_XML = getProject().getLookup().
                lookup(AntProjectHelper.class).resolveFile(
                    AntProjectHelper.PROJECT_XML_PATH);
        
        initActionFiles();
    }

    @Override
    public void setActionFiles( FileObject[] files ) {
        //myFiles = cleanupFiles(files);
        super.setActionFiles(files);
    }
    
    @Override
    protected FileObject[] getFileObjects() {
        //return myFiles;
        return super.getFileObjects();
    }

    protected void notifyTransferStarted(){
        notifyStartedToOutput();
    }
    
    protected void notifyTransferFinished(boolean successed){
        notifyNotOverwrittenFiles();
        notifySkippedFiles();

        notifyCopiedCount();
        notifyFinishedToOutput();
        if (successed && needNotificate()){
            notifyFinishedToStatusBar();
        }
    }
    
    protected FileObject getSourceRootObject() {
        FileObject[] sources = getSourceObjects(getProject());
        if (sources == null || sources.length == 0) {
            return null;
        }
        /*
         * I choose only first source root.
         * TODO: change if we decide to support multiple src roots
         */
        return sources[0];
    }

    @Override
    protected void refresh() {
        super.refresh();
        mySkippedFiles = new LinkedList<String>();
        myNotOverwrittenFiles = new LinkedList<String>();
        myCopiedFiles = 0;

    }

    protected void rememberSkippedFile(String skippedFile){
        mySkippedFiles.add(skippedFile);
    }
            
    protected List<String> getSkippedFiles(){
        return mySkippedFiles;
    }
            
    protected void rememberNotOverwrittenFile(String skippedFile){
        myNotOverwrittenFiles.add(skippedFile);
    }
            
    protected List<String> getNotOverwrittenFiles(){
        return myNotOverwrittenFiles;
    }
            
    protected void rememberCopiedFile(String copiedFile){
        myCopiedFiles++;
    }
            
    protected int getCopiedFilesCnt(){
        return myCopiedFiles;
    }
            
    protected boolean needNotificate() {
        return doNotify;
    }

    protected boolean confirmOverwrite(String file) {
        Boolean overwriteFiles = getOverwriteFiles();
        if (overwriteFiles != null) {
            return overwriteFiles.booleanValue();
        }
        boolean[] dontAskConfirm = new boolean[]{false};
        boolean confirm = ActionsDialogs.userConfirmRewrite(file, dontAskConfirm);
        if (dontAskConfirm[0]) {
            ServerActionsPreferences.getInstance().setFileOverwrite(confirm);
        }
        return confirm;
    }
    protected Boolean getOverwriteFiles(){
        return ServerActionsPreferences.getInstance().getFileOverwrite();
    }
    
    private void initActionFiles(){
         /*
          * This method should be called in constructor of class because
          * <code>nodes</code> array could be changed while action 
          * execution. So one need to initialize fileObjects array once
          * and use it for access to action files.   
          */
        myFiles = super.getFileObjects();
    }
    
    protected void cleanupFiles(){
        myFiles = cleanupFiles(getFileObjects());
    }
    
    protected boolean isNbProject(FileObject file) {
        FileObject fileObject = FileUtil.toFileObject(PROJECT_XML.getParentFile());
        return file.equals(fileObject);
    }

    protected boolean isNbProject(File file) {
        File nbProjectFile = PROJECT_XML.getParentFile();
        return file.equals(nbProjectFile);
    }

    /**
     * If there is a folder in array that isParent of another FileObject in 
     * array, child will be removed.
     * <p>
     * TODO perform cleanup
     */
    private FileObject[] cleanupFiles(FileObject[] fileObjects){
        return fileObjects;
    }
    
    private void notifyStartedToOutput(){
        notifyMsg( LBL_TRANSFER_STARTED_MSG, 
                AbstractFilesTransferCommand.class, getLabel(), getHost() );
    }
    
    private void notifyFinishedToOutput(){
        notifyMsg( LBL_TRANSFER_FINISHED_MSG, 
                AbstractFilesTransferCommand.class, getLabel(), getHost() );
    }
    
    private void notifyFinishedToStatusBar(){
        statusMsg( LBL_TRANSFER_FINISHED_STATUS, 
                AbstractFilesTransferCommand.class, getLabel() );
    }
    
    private void notifyCopiedCount(){
        notifyMsg( LBL_COPIED_COUNT_MSG, 
                AbstractFilesTransferCommand.class, getCopiedFilesCnt() );
    }

    private void notifySkippedFiles(){
        int skippedCnt = getSkippedFiles().size();
        if (skippedCnt > 0){
            notifyMsg( LBL_SKIPPED_TITLE_MSG, 
                    AbstractFilesTransferCommand.class, skippedCnt );
        
            for (String file : getSkippedFiles()){
                notifyMsg( LBL_SKIPPED_FILE_MSG,  
                        AbstractFilesTransferCommand.class, file );
            }
        }
    }

    private void notifyNotOverwrittenFiles(){
        int skippedCnt = getNotOverwrittenFiles().size();
        if (skippedCnt > 0){
            Boolean overwrite = getOverwriteFiles();
            if (overwrite != null && !overwrite.booleanValue()){
                notifyMsg( LBL_REPLACE_SKIPPED_BY_OPTION_TITLE_MSG,
                        AbstractFilesTransferCommand.class, skippedCnt );
            } else {
                notifyMsg( LBL_REPLACE_SKIPPED_BY_USER_TITLE_MSG,
                        AbstractFilesTransferCommand.class, skippedCnt );
            }
        }
    }

    private final boolean doNotify;

    private List<String> mySkippedFiles = null;
    private List<String> myNotOverwrittenFiles = null;

    private int myCopiedFiles;

    protected final File PROJECT_XML;

    private FileObject[] myFiles;
    
}
