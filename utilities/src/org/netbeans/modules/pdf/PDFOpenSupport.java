/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.pdf;

import java.io.*;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.OpenCookie;
import org.openide.util.HelpCtx;
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
            "xpdf", "kghostview", "ggv", "acroread" };                  //NOI18N
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
