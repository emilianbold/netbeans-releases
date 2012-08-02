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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.openfile;

import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.UserCancelException;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action which allows user open file from disk. It is installed
 * in Menu | File | Open file... .
 *
 * @author Jesse Glick
 * @author Marian Petras
 */
@ActionRegistration(
    displayName="#LBL_openFile", 
    iconBase="org/netbeans/modules/openfile/openFile.png",
    iconInMenu=false
)
@ActionID(category="System", id="org.netbeans.modules.openfile.OpenFileAction")
@ActionReference(path="Menu/File", position=800)
public class OpenFileAction implements ActionListener {

    /** stores the last current directory of the file chooser */
    private static File currentDirectory = null;

    public OpenFileAction() {
    }

    private HelpCtx getHelpCtx() {
        return new HelpCtx(OpenFileAction.class);
    }

    /**
     * Creates and initializes a file chooser.
     *
     * @return  the initialized file chooser
     */
    protected JFileChooser prepareFileChooser() {
        JFileChooser chooser = new FileChooser();
        chooser.setCurrentDirectory(getCurrentDirectory());
        HelpCtx.setHelpIDString(chooser, getHelpCtx().getHelpID());
        
        return chooser;
    }
    
    /**
     * Displays the specified file chooser and returns a list of selected files.
     *
     * @param  chooser  file chooser to display
     * @return  array of selected files,
     * @exception  org.openide.util.UserCancelException
     *                     if the user cancelled the operation
     */
    public static File[] chooseFilesToOpen(JFileChooser chooser)
            throws UserCancelException {
        File[] files;
        do {
            int selectedOption = chooser.showOpenDialog(
                WindowManager.getDefault().getMainWindow());
            
            if (selectedOption != JFileChooser.APPROVE_OPTION) {
                throw new UserCancelException();
            }
            files = chooser.getSelectedFiles();
        } while (files.length == 0);
        return files;
    }

    private static boolean running;
    /**
     * {@inheritDoc} Displays a file chooser dialog
     * and opens the selected files.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            return;
        }
        try {
            running = true;
            JFileChooser chooser = prepareFileChooser();
            File[] files;
            try {
                if( Boolean.getBoolean("nb.native.filechooser") ) { //NOI18N
                    String oldFileDialogProp = System.getProperty("apple.awt.fileDialogForDirectories"); //NOI18N
                    System.setProperty("apple.awt.fileDialogForDirectories", "false"); //NOI18N
                    FileDialog fileDialog = new FileDialog(WindowManager.getDefault().getMainWindow());
                    fileDialog.setMode(FileDialog.LOAD);
                    fileDialog.setDirectory(getCurrentDirectory().getAbsolutePath());
                    fileDialog.setVisible(true);
                    if( null != oldFileDialogProp ) {
                        System.setProperty("apple.awt.fileDialogForDirectories", oldFileDialogProp); //NOI18N
                    } else {
                        System.clearProperty("apple.awt.fileDialogForDirectories"); //NOI18N
                    }

                    if( fileDialog.getDirectory() != null && fileDialog.getFile() != null ) {
                        String selFile = fileDialog.getFile();
                        File dir = new File( fileDialog.getDirectory() );
                        files = new File[] { new File( dir, selFile ) };
                        currentDirectory = dir;
                    } else {
                        throw new UserCancelException();
                    }
                } else {
                    files = chooseFilesToOpen(chooser);
                    currentDirectory = chooser.getCurrentDirectory();
                }
            } catch (UserCancelException ex) {
                return;
            }
            for (int i = 0; i < files.length; i++) {
                OpenFile.openFile(files[i], -1);
            }
        } finally {
            running = false;
        }
    }
    
    private static File getCurrentDirectory() {
        if (Boolean.getBoolean("netbeans.openfile.197063")) {
            // Prefer to open from parent of active editor, if any.
            TopComponent activated = TopComponent.getRegistry().getActivated();
            if (activated != null && WindowManager.getDefault().isOpenedEditorTopComponent(activated)) {
                DataObject d = activated.getLookup().lookup(DataObject.class);
                if (d != null) {
                    File f = FileUtil.toFile(d.getPrimaryFile());
                    if (f != null) {
                        return f.getParentFile();
                    }
                }
            }
        }
        // Otherwise, use last-selected directory, if any.
        if(currentDirectory != null && currentDirectory.exists()) {
            return currentDirectory;
        }
        // Fall back to default location ($HOME or similar).
        currentDirectory =
                FileSystemView.getFileSystemView().getDefaultDirectory();
        return currentDirectory;
    }

}
