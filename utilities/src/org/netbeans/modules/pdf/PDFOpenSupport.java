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
 */
class PDFOpenSupport implements OpenCookie {
    
    private File f;
    
    /**
     * @exception  java.lang.IllegalArgumentException
     *             if the specified file does not exist or is not a plain file
     */
    public PDFOpenSupport(File f) {
        if (!f.isFile()) {
            String msg = NbBundle.getMessage(PDFOpenSupport.class,
                                             "EXC_no_such_pdf",         //NOI18N
                                             f.getPath());
            throw new IllegalArgumentException(msg);
        }
        this.f = f;
    }
    
    public void open() {
        Settings sett = Settings.getDefault();
        try {
            Process p = Runtime.getRuntime().exec(
                    new String[] {sett.getPDFViewer().getAbsolutePath(),
                                  f.getAbsolutePath()
            });
            // [PENDING] redirect p's output
        } catch (IOException ioe) {
            // Try to reconfigure.
            String excmessage = ioe.getLocalizedMessage();
            String exceptionType = ioe.getClass().getName();
            int idx = exceptionType.lastIndexOf('.');
            if (idx != -1) {
                exceptionType = exceptionType.substring(idx + 1);
            }
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
            DialogDescriptor d = new DialogDescriptor(
                new ReconfigureReaderPanel(exceptionType, excmessage), // content pane
                NbBundle.getMessage(PDFOpenSupport.class, "TITLE_pick_a_viewer"), // title
                true, // modal
                DialogDescriptor.OK_CANCEL_OPTION, // option type
                DialogDescriptor.OK_OPTION,        // default value
                DialogDescriptor.DEFAULT_ALIGN,    // alignment
                new HelpCtx(PDFOpenSupport.class.getName()
                            + ".dialog"),          //help context       //NOI18N
                null); // action listener
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                open ();
            }
        }
    }
    
}
