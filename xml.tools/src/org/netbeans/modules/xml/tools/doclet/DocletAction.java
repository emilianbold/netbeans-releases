/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.tools.doclet;

import java.util.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;

import javax.swing.text.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.cookies.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import org.netbeans.tax.*;
import org.netbeans.modules.xml.core.*;
import org.netbeans.modules.xml.core.actions.CollectDTDAction;
import org.netbeans.modules.xml.tools.generator.*;

/**
 * Creates a documentation upon a standalone DTD. Stores it into html.
 * It does work only with standalone DTD (it is feature).
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class DocletAction extends CookieAction implements CollectDTDAction.DTDAction {

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
        ErrorManager emgr = TopManager.getDefault().getErrorManager();
        
        try {
            
            final TreeDTD treeDTD = (TreeDTD) dtdo.getDocumentRoot();

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

                // ask for data object location

                FileObject primFile = dtdo.getPrimaryFile();
                String name = primFile.getName() + Util.getString("NAME_SUFFIX_Documentation");
                FileObject folder = primFile.getParent();
                String packageName = folder.getPackageName ('.');

                FileObject generFile = (new SelectFileDialog (folder, name, "html")).getFileObject(); // NOI18N
                name = generFile.getName();

                //wait until documentation generated            
                thread.join();

                // create and view data object

                DataObject targeto;


                try {
                    targeto = TopManager.getDefault().getLoaderPool().findDataObject(generFile);
                } catch (DataObjectExistsException eex) {
                    targeto = eex.getDataObject();
                }

                EditorCookie ec = (EditorCookie) targeto.getCookie(EditorCookie.class);
                if (ec != null) {
                    Document doc = ec.openDocument();

                    try {
                        doc.remove(0, doc.getLength());
                        doc.insertString(0, text.toString(), null);
                        ec.saveDocument();
                    } catch (BadLocationException locex) {
                        emgr.annotate(locex, Util.getString("MSG_error_leaving_in_clipboard"));
                        emgr.notify(locex);                    

                        StringSelection ss = new StringSelection(text.toString());
                        TopManager.getDefault().getClipboard().setContents(ss, null);
                        TopManager.getDefault().setStatusText(Util.getString("MSG_documentation_in_clipboard"));

                    }

                    ViewCookie vc = (ViewCookie) targeto.getCookie(ViewCookie.class);
                    if (vc != null) vc.view();

                }
                
            } catch (UserCancelException ex) {
                //user cancelled do nothing
                
            } catch (InterruptedException ex) {

                emgr.annotate(ex, Util.getString("MSG_generating_interrupted"));
                emgr.notify(ex);            
            }
            
        } catch (IOException ioex) {
            
            emgr.annotate(ioex, Util.getString("MSG_IO_ex_docwriting"));
            emgr.notify(ioex);
            
        } catch (TreeException tex) {
            
            TopManager.getDefault().setStatusText(Util.getString("MSG_doclet_fatal_error"));
        
        } finally {
            if (thread != null) thread.interrupt();
        }
            
                        

    }


    public HelpCtx getHelpCtx() {
        return null;
    }

    public String getName() {
        return Util.getString("NAME_Generate_Documentation");
    }

}
