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
package org.netbeans.modules.xml.tools.actions;

import java.util.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;

import javax.swing.text.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.cookies.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;

import org.netbeans.tax.*;
import org.netbeans.modules.xml.core.DTDDataObject;

import org.netbeans.modules.xml.tools.lib.GuiUtil;
import org.netbeans.modules.xml.tools.generator.SelectFileDialog;

/**
 * Creates a CSS draft upon a standalone DTD. Stores it into css file.
 * It does work only with standalone DTD (it is feature).
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class CSSStyleAction extends CookieAction {

    /** Serial Version UID */
    private static final long serialVersionUID = 7867855746468L;
    
    /** Creates new CSSStyleAction */
    public CSSStyleAction() {
    }

    public Class[] cookieClasses() {
        return new Class[] { DTDDataObject.class };
    }

    public int mode() {
        return MODE_ONE;
    }

    private String css;

    
    public void performAction(Node[] nodes) {
        if (nodes == null) return;
        if (nodes.length != 1) return;

        Node dtd = nodes[0];

        css = ""; //"@charset \"UTF-8\";\n"; // NOI18N
        css += "/* Cascade style sheet based on " + dtd.getDisplayName() + " DTD */\n"; // NOI18N

        DTDDataObject dtdo = (DTDDataObject) dtd.getCookie(DTDDataObject.class);
        
        ErrorManager emgr = TopManager.getDefault().getErrorManager();
            
        try {
        
            TreeDTD treeDTD = (TreeDTD) dtdo.getDocumentRoot();

            Iterator it = treeDTD.getElementDeclarations().iterator();

            while (it.hasNext()) {
                TreeElementDecl decl = (TreeElementDecl) it.next();
                String name = decl.getName();
                add(name);
            }
            
            // ask for data object location
            
            FileObject primFile = dtdo.getPrimaryFile();
            String name = primFile.getName() + Util.getString("NAME_SUFFIX_Stylesheet");
            FileObject folder = primFile.getParent();
            String packageName = folder.getPackageName ('.');

            FileObject generFile = (new SelectFileDialog (folder, name, "css")).getFileObject(); // NOI18N
            name = generFile.getName();
            
            // create and open data object
            
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
                    doc.insertString(0, css, null);
                    ec.saveDocument();
                } catch (BadLocationException locex) {
                    emgr.annotate(locex, Util.getString("MSG_Leaving_CSS_in_clipboard"));
                    emgr.notify(locex);                    
                    
                    StringSelection ss = new StringSelection(css);
                    TopManager.getDefault().getClipboard().setContents(ss, null);
                    GuiUtil.setStatusText(Util.getString("MSG_CSS_placed_in_clipboard"));
                    
                }
                
                
                OpenCookie oc = (OpenCookie) targeto.getCookie(OpenCookie.class);
                if (oc != null) oc.open();
                
            }
            
        } catch (UserCancelException ex) {
            //user cancelled do nothing
            
        } catch (IOException ex) {

            emgr.annotate(ex, Util.getString("MSG_IO_ex_CSS_writing."));
            emgr.notify(ex);
            
        } catch (TreeException ex) {
            
            GuiUtil.setStatusText(Util.getString("MSG_CSS_fatal_error"));
            
        }

    }

    /** adds a new name to just created CSS. */
    private void add(String name) {
        css += name + " { display: block }\n"; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

    public String getName() {
        return Util.getString("NAME_Generate_CSS");
    }

}
