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
package org.netbeans.modules.php.project;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JFileChooser;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.project.options.ProjectActionsPreferences;
import org.netbeans.modules.php.rt.utils.ActionsDialogs;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.windows.WindowManager;


/**
 * @author ads
 *
 */
class ImportCommand extends AbstractCommand implements Cancellable {

    public static final String IMPORT_ACTION = "import"; // NOI18N
    public static final String CHOOSE_FILES = "LBL_ChooseFiles"; // NOI18N
    private static final String LABEL = PhpActionProvider.LBL_IMPORT_FILE;
    static final String LBL_FILES_IMPORTED = "LBL_FilesImported"; // NOI18N
    private static final String LBL_WARNING_MSG = "LBL_ImportWarningMsg"; // NOI18N
    

    //private static Logger LOGGER = Logger.getLogger(RunLocalCommand.class.getName());

    ImportCommand(Project project) {
        super(project);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.Command#getId()
     */
    public String getId() {
        return IMPORT_ACTION;
    }

    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.rt.spi.providers.Command#getLabel()
     */
    public String getLabel() {
        return NbBundle.getMessage(ImportCommand.class, LABEL);
    }

    /**
     * This command should be started in AWT thread because it uses WindowsAPI.
     * Will run copying routine in separate thread itself.
     * @return false - it will be performed synchronously as called in the event thread.
     */
    @Override
    public boolean asynchronous() {
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        refresh();
        
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(NbBundle.getMessage(
                ImportCommand.class, CHOOSE_FILES));
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setMultiSelectionEnabled(true);

        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(
                WindowManager.getDefault().getMainWindow())) 
        {
            File[] files = chooser.getSelectedFiles();
            if (files == null) {
                return;
            }

            importFiles(files);
        }
    }

    protected void notifyFileImported() {
        statusMsg(LBL_FILES_IMPORTED, ImportCommand.class);
    }

    private void importFiles(final File[] files) {
            Runnable importThread = new Runnable(){

                public void run() {
                    runImportFilesWithStatus(files);
                }
            };
            RequestProcessor.getDefault().post( importThread );
            
    }

    private void runImportFilesWithStatus(File[] files) {
        ProgressHandle progress = ProgressHandleFactory.createHandle(getLabel());
        progress.start();

        //initOutputTabWriter(getOutputTabTitle());

        try {
            copyFiles(files);
        } finally {
            progress.finish();
            closeOutputTabWriter();
        }
    }
    
    private OutputWriter initOutputTabWriter(String title){
        if (myOutputTabWriter != null){
            closeOutputTabWriter();
        }
        InputOutput io = IOProvider.getDefault().getIO(title, false);
        io.select();
        myOutputTabWriter = io.getOut();
        return myOutputTabWriter;
    }
    
    /**
     * is used to log errors to output tab.
     * Because inherited notifyMag creates writer for each msg. 
     * @param bundleKey
     * @param args
     */
    private void logToOutputTab(String bundleKey, Object... args){
        if (myOutputTabWriter != null){
            String msg = loadFormattedMsg(bundleKey, getClass(), args);
            myOutputTabWriter.println(msg);
            myOutputTabWriter.flush();
        } else {
            initOutputTabWriter(getOutputTabTitle());
        }
    }
    
    private void closeOutputTabWriter(){
        if (myOutputTabWriter != null){
            myOutputTabWriter.close();
            myOutputTabWriter = null;
        }
    }
    
    private boolean copyFiles(File[] files) {
        boolean[] success = new boolean[]{true};
        File sourceRoot = FileUtil.toFile(getSourceObject());
        for (File file : files) {
            File to = new File(sourceRoot, file.getName());
            if (skipTargetFile(to)) {
                continue;
            }
            if (file.isDirectory()) {
                copyFolder(success, file, to);
            } else {
                try {
                    to.getParentFile().mkdirs();
                    if (to.exists()) {
                        overwriteFile(file, to);
                    } else {
                        copyFile(file, to);
                    }
                } catch (IOException e) {
                    logToOutputTab(LBL_WARNING_MSG, e.getMessage());
                    success[0] = false;
                }
            }
        }
        return success[0];
    }

    private boolean skipTargetFile(File file) {
        return isNbProject(file);
    }
    
    
    private void copyFilesRecursively(File from, File to, boolean[] errors) {
        if (!from.isDirectory()) {
            return;
        }

        File[] children = from.listFiles();
        for (File child : children) {
            File dst = new File(to, child.getName());
            //if (skipFile(child)) {
            //    continue;
            //}
            if (child.isDirectory()) {
                copyFolder(errors, child, dst);
            } else {
                try {
                    if (dst.exists()) {
                        overwriteFile(child, dst);
                    } else {
                        copyFile(child, dst);
                    }
                } catch (IOException e) {
                    logToOutputTab(LBL_WARNING_MSG, e.getMessage());
                    errors[0] = false;
                }
            }
        }
    }

    private void copyFolder(boolean[] errors, File src, File dst) {
        boolean success = dst.mkdirs();
        if (!success && !dst.exists()) {
            errors[0] = false;
        } else {
            copyFilesRecursively(src, dst, errors);
        }
    }

    private void overwriteFile(File from, File to) throws IOException {
        boolean overwrite = false;
        String fileFullName = to.getPath();
        
        overwrite = getOverwriteFiles() == null
                //? Boolean.TRUE
                ? confirmOverwrite(fileFullName)
                : getOverwriteFiles().booleanValue();
 
        if (overwrite) {
            File toTmp = getNotExistingTmpFile(to);
            
            copyFile(from, toTmp);
            to.delete();
            toTmp.renameTo(to);
        } else {
            // do nothing
        }
    }

    private File getNotExistingTmpFile(File to) {
        File toTmp = null;
        do {
            toTmp = getTmpFile(to);
        } while (toTmp.exists());
        return toTmp;
    }

    private File getTmpFile(File to) {
        File parentFolder = to.getParentFile();
        File toTmp = new File(parentFolder, 
                System.currentTimeMillis() + to.getName() + PhpProject.TMP_FILE_POSTFIX);
        return toTmp;
    }

    static FileObject copyFile(File from, File to) throws IOException {
        assert to != null;
        assert from != null;

        // do not use the fiollowing code because File 'to' doesn't have method to
        // get name without ext easily. FileUtil.copyFile needs name without ext.
        //FileObject fromFileObject = FileUtil.toFileObject(FileUtil.normalizeFile(from));
        //FileObject destFolder = FileUtil.toFileObject(to.getParentFile());
        //if (fromFileObject != null && destFolder != null) {
        //    return FileUtil.copyFile(fromFileObject, destFolder, to.getName());
        //}
        FileObject dest = FileUtil.createData(to);

        FileLock lock = null;
        InputStream bufIn = null;
        OutputStream bufOut = null;

        try {
            lock = dest.lock();
            bufIn = new BufferedInputStream(new FileInputStream(from));

            bufOut = dest.getOutputStream(lock);

            FileUtil.copy(bufIn, bufOut);
        } finally {
            if (bufIn != null) {
                bufIn.close();
            }

            if (bufOut != null) {
                bufOut.close();
            }

            if (lock != null) {
                lock.releaseLock();
            }
        }

        return dest;
    }

    protected boolean confirmOverwrite(String file) {
        Boolean overwriteFiles = getOverwriteFiles();
        if (overwriteFiles != null) {
            return overwriteFiles.booleanValue();
        }
        boolean[] dontAskConfirm = new boolean[]{false};
        boolean confirm = ActionsDialogs.userConfirmRewrite(file, dontAskConfirm);
        if (dontAskConfirm[0]) {
            ProjectActionsPreferences.getInstance().setFileOverwrite(confirm);
        }
        return confirm;
    }
    protected Boolean getOverwriteFiles(){
        return ProjectActionsPreferences.getInstance().getFileOverwrite();
    }
    
    
    /*
     * implementation of org.openide.util.Cancellable
     */
    
    public boolean cancel() {
        return false;
    }
    
    OutputWriter myOutputTabWriter = null;
 }