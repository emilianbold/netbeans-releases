/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tools.doclet;

import java.util.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;

import javax.swing.text.*;

import org.openide.*;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.*;
import org.openide.cookies.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.actions.*;

import org.netbeans.tax.*;
import org.netbeans.modules.xml.core.*;
import org.netbeans.modules.xml.core.actions.CollectDTDAction;
import org.netbeans.modules.xml.tools.generator.*;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;

/**
 * Creates a documentation upon a standalone DTD. Stores it into html.
 * It does work only with standalone DTD (it is feature).
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public final class DocletAction extends CookieAction implements CollectDTDAction.DTDAction {

    /** Stream serialVersionUID. */
    private static final long serialVersionUID = -4037098165368211623L;
    
    
    /** Creates new CSSStyleAction */
    public DocletAction() {
    }

    public Class[] cookieClasses() {
        return new Class[] { DTDDataObject.class };
    }

    public int mode() {
        return MODE_ONE;
    }

    public void performAction(Node[] nodes) {
            
        if (nodes == null) return;
        if (nodes.length != 1) return;

        final StringBuffer text = new StringBuffer();                
        final Node dtd = nodes[0];

        final DTDDataObject dtdo = (DTDDataObject) dtd.getCookie(DTDDataObject.class);

        Thread thread = null;
        ErrorManager emgr = ErrorManager.getDefault();
        
        try {

            TreeDocumentRoot result;

            TreeEditorCookie cake = (TreeEditorCookie) dtdo.getCookie(TreeEditorCookie.class);
            if (cake != null) {
                result = cake.openDocumentRoot();
            } else {
                throw new TreeException("DTDDataObject:INTERNAL ERROR"); // NOI18N
            }
            final TreeDTD treeDTD = (TreeDTD) result;

            final DTDDoclet doclet = new DTDDoclet();

            Runnable task = new Runnable() {
                public void run() {
                    text.append(doclet.createDoclet (treeDTD));
                }
            };

            //start task in paralel with user input
            
            thread = new Thread(task, "Creating XML doc..."); // NOI18N        
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setDaemon(true);                       
            thread.start();


            try {

                // ask for file object location

                FileObject primFile = dtdo.getPrimaryFile();
                String name = primFile.getName() + Util.THIS.getString("NAME_SUFFIX_Documentation");
                FileObject folder = primFile.getParent();

                FileObject generFile = (new SelectFileDialog (folder, name, "html")).getFileObject(); // NOI18N
                name = generFile.getName();

                // wait until documentation generated            
                thread.join();

                // fill result file

                FileLock lock = null;
                try {
                     lock = generFile.lock();
                     OutputStream fout = generFile.getOutputStream(lock);
                     try {
                         OutputStream out = new BufferedOutputStream(fout);
                         Writer writer = new OutputStreamWriter(out, "UTF8");  //NOI18N
                         writer.write(text.toString());
                         writer.flush();
                     } finally {
                         if (fout != null) fout.close();
                     }
                     
                } catch (IOException ex) {
                    emgr.annotate(ex, Util.THIS.getString("MSG_error_leaving_in_clipboard"));
                    emgr.notify(ex);

                    leaveInClipboard(text.toString());
                    return;
                    
                } finally {
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }

                // open results in a browser if exists
                
                try {
                    DataObject html = DataObject.find(generFile);
                
                    ViewCookie vc = (ViewCookie) html.getCookie(ViewCookie.class);
                    if (vc != null) vc.view();
                } catch (DataObjectNotFoundException dex) {
                    // just do not show
                }

                
            } catch (UserCancelException ex) {
                //user cancelled do nothing
                
            } catch (InterruptedException ex) {

                emgr.annotate(ex, Util.THIS.getString("MSG_generating_interrupted"));
                emgr.notify(ex);            
            }
            
        } catch (IOException ioex) {
            
            emgr.annotate(ioex, Util.THIS.getString("MSG_IO_ex_docwriting"));
            emgr.notify(ioex);
            
        } catch (TreeException tex) {
            
            StatusDisplayer.getDefault().setStatusText(Util.THIS.getString("MSG_doclet_fatal_error"));
        
        } finally {
            if (thread != null) thread.interrupt();
        }
                                    
    }


    private void leaveInClipboard(String text) {
        StringSelection ss = new StringSelection(text);
        ExClipboard clipboard = (ExClipboard) Lookup.getDefault().lookup(ExClipboard.class);
        clipboard.setContents(ss, null);
        StatusDisplayer.getDefault().setStatusText(Util.THIS.getString("MSG_documentation_in_clipboard"));
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    public String getName() {
        return Util.THIS.getString("NAME_Generate_Documentation");
    }

    protected boolean asynchronous() {
        return false;
    }

}
