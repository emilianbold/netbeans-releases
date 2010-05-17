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

package org.netbeans.modules.pdf;

import java.io.File;
import java.io.IOException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import static org.openide.util.Utilities.OS_MAC;

/**
 * Permits a PDF file to be opened in an external viewer.
 *
 * @author Jesse Glick
 * @author Marian Petras
 */
class PDFOpenSupport implements OpenCookie {

    private static final String DEFAULT_MACOS_VIEWER = "open";          //NOI18N
    private static final String[] APP_DIRS = new String[] {
            "/usr/bin", "/usr/local/bin" };                             //NOI18N
    private static final String[] VIEWER_NAMES = new String[] {
            "evince", "xpdf", "kghostview", "ggv", "acroread" };        //NOI18N
    static final String FALLBACK_VIEWER_NAME = "acroread";              //NOI18N

    private File f;
    private DataObject dObj;
    
    /**
     * @exception  java.lang.IllegalArgumentException
     *             if the specified file does not exist or is not a plain file
     */
    public PDFOpenSupport(File f) {
        this.f = f;
        try {
            this.dObj = DataObject.find(FileUtil.toFileObject(f));
        } catch (DataObjectNotFoundException ex) {
        }
    }

    public PDFOpenSupport(DataObject dObj) {
        this.dObj = dObj;
        this.f = FileUtil.toFile(dObj.getPrimaryFile());
    }

    public void open() {
        if(dObj != null){
            f = FileUtil.toFile(dObj.getPrimaryFile());
        }
        final String filePath = f.getAbsolutePath();
 
        if (Utilities.isWindows()) {
            /*
             * To run the PDF viewer, we need to execute:
             * 
             *      cmd.exe /C start <file name>
             * 
             * This works well except for cases that there is a space
             * in the file's path. In this case, the file's path must be
             * enclosed to quotes to make the command-line interpreter (cmd.exe)
             * consider it a single argument. BUT: If the first argument
             * to the "start" command is enclosed in quotes, it is considered
             * to be a window title. So, to make sure the file path argument
             * is not considered a window title, we must pass some dummy
             * argument enclosed in quotes as the first argument and the
             * actual file path as the second argument (of the 'start' command):
             * 
             *      cmd.exe /C start "PDF Viewer" <file name>
             * 
             * (see also bug #122221)
             */
            tryCommand(new String[] {"cmd.exe", "/C", "start",          //NOI18N
                                     "\"PDF Viewer\"",  //win.title     //NOI18N
                                     filePath});
            return;
        }

        Settings sett = Settings.getDefault();
        
        File viewer = sett.getPDFViewer();
        boolean viewerUnset = (viewer == null);

        if (viewerUnset && (Utilities.getOperatingSystem() == OS_MAC)) {
            String cmd = DEFAULT_MACOS_VIEWER;
            if (tryCommand(new String[] {cmd, filePath})) {
                sett.setPDFViewer(new File(cmd));
                return;
            }
        }
        
        if (viewerUnset) {
            viewer = tryPredefinedViewers(filePath);
            if (viewer != null) {
                sett.setPDFViewer(viewer);
                return;
            }
        }
        
        if (viewerUnset) {
            viewer = new File(FALLBACK_VIEWER_NAME);
        }
        
        boolean viewerFailed = false;
        do {
            try {
                tryCommandExc(new String[] {viewer.getPath(), filePath});
                if (viewerUnset || viewerFailed) {
                    sett.setPDFViewer(viewer);
                }
                break;
            } catch (IOException ioe) {
                viewerFailed = true;
                
                // Try to reconfigure.
                String excmessage = ioe.getLocalizedMessage();
                /* [PENDING] does not work (no properties show in sheet, though node has them):
                Node n;
                try {
                    n = new BeanNode (sett);
                } catch (IntrospectionException ie) {
                    TopManager.getDefault ().notifyException (ie);
                    return;
                }
                PropertySheet sheet = new PropertySheet ();
                sheet.setNodes (new Node[] { n });
                //TopManager.getDefault ().getNodeOperation ().explore (n);
                 */
                ReconfigureReaderPanel configPanel
                        = new ReconfigureReaderPanel(viewer, excmessage);
                String title = NbBundle.getMessage(
                                       PDFOpenSupport.class,
                                       "TITLE_pick_a_viewer");          //NOI18N
                DialogDescriptor d = new DialogDescriptor(configPanel, title);
                if (DialogDisplayer.getDefault().notify(d)
                        == DialogDescriptor.OK_OPTION) {
                    sett.setPDFViewer(viewer = configPanel.getSelectedFile());
                } else {
                    break;
                }
            }
        } while (true);
    }

    /**
     */
    private static File tryPredefinedViewers(String filePath) {
        for (int i = 0; i < APP_DIRS.length; i++) {

            File dir = new File(APP_DIRS[i]);
            if (!dir.exists() || !dir.isDirectory()) {
                continue;
            }
            
            for (int j = 0; j < VIEWER_NAMES.length; j++) {
                String viewerPath = APP_DIRS[i] + File.separatorChar
                                    + VIEWER_NAMES[j];
                File viewer = new File(viewerPath);
                if (!viewer.exists()) {
                    continue;
                }
                
                if (tryCommand(new String[] {viewerPath, filePath})) {
                    return viewer;
                }
                //else: never mind, try the next predefined viewer
            }
        }
        
        return null;
    }

    /**
     * Tries to execute the specified command and arguments.
     *
     * @param  cmdArray  array containing the command to call and its arguments
     * @return  {@code true} if the execution was successful,
     *          {@code false} otherwise
     */
    private static boolean tryCommand(final String[] cmdArray) {
        try {
            tryCommandExc(cmdArray);
            return true;
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
     * Tries to execute the specified command and arguments.
     *
     * @param  cmdArray  array containing the command to call and its arguments
     * @return  {@code true} if the execution was successful,
     *          {@code false} otherwise
     * @exception  java.io.IOException
     *             
     */
    private static void tryCommandExc(final String[] cmdArray)
                                                        throws IOException {
        Runtime.getRuntime().exec(cmdArray);
        // [PENDING] redirect the process' output
    }

}
