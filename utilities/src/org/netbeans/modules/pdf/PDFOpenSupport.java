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

package org.netbeans.modules.pdf;

import java.io.File;
import java.io.IOException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.OpenCookie;
import org.openide.util.NbBundle;

/**
 * Permits a PDF file to be opened in an external viewer.
 *
 * @author Jesse Glick
 * @author Marian Petras
 */
class PDFOpenSupport implements OpenCookie {

    private static final String[] APP_DIRS = new String[] {
            "/usr/bin", "/usr/local/bin" };                             //NOI18N
    private static final String[] VIEWER_NAMES = new String[] {
            "evince", "xpdf", "kghostview", "ggv", "acroread" };        //NOI18N
    private static final String[] NO_PATH_VIEWERS = new String[] {
            "acroread", "open" };                                       //NOI18N
    static final String FALLBACK_VIEWER_NAME = "acroread";              //NOI18N

    private File f;
    
    /**
     * @exception  java.lang.IllegalArgumentException
     *             if the specified file does not exist or is not a plain file
     */
    public PDFOpenSupport(File f) {
        this.f = f;
    }

    public void open() {
        Settings sett = Settings.getDefault();
        
        File viewer = sett.getPDFViewer();
        boolean viewerUnset = (viewer == null);
        
        if (viewerUnset) {
            viewer = tryPredefinedViewers(f);
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
                Process p = Runtime.getRuntime().exec(
                        new String[] {viewer.getPath(),
                                      f.getAbsolutePath()
                });
                if (viewerUnset || viewerFailed) {
                    sett.setPDFViewer(viewer);
                }
                break;
                // [PENDING] redirect p's output
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
    private static File tryPredefinedViewers(File fileToOpen) {
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
                
                try {
                    Process p = Runtime.getRuntime().exec(
                            new String[] {viewerPath,
                                          fileToOpen.getAbsolutePath()});
                    return viewer;
                } catch (IOException ex) {
                    //never mind, try the next predefined viewer
                }
            }
        }
        
        for (int i = 0; i < NO_PATH_VIEWERS.length; i++) {
            try {
                Process p = Runtime.getRuntime().exec(
                        new String[] {NO_PATH_VIEWERS[i],
                                      fileToOpen.getAbsolutePath()});
                return new File(NO_PATH_VIEWERS[i]);
            } catch (IOException ex) {
                //never mind, try the next predefined viewer
            }
        }
        
        return null;
    }

    
}
