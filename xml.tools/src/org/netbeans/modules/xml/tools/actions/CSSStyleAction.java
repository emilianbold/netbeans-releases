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
package org.netbeans.modules.xml.tools.actions;

import java.util.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;

import javax.swing.text.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.actions.*;
import org.openide.cookies.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;

import org.netbeans.tax.*;
import org.netbeans.modules.xml.core.DTDDataObject;

import org.netbeans.modules.xml.core.lib.GuiUtil;
import org.netbeans.modules.xml.tools.generator.SelectFileDialog;
import org.netbeans.modules.xml.core.actions.CollectDTDAction;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
/**
 * Creates a CSS draft upon a standalone DTD. Stores it into css file.
 * It does work only with standalone DTD (it is feature).
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public final class CSSStyleAction extends CookieAction implements CollectDTDAction.DTDAction {

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
        
        ErrorManager emgr = ErrorManager.getDefault();
            
        try {

            TreeDocumentRoot result;

            TreeEditorCookie cake = (TreeEditorCookie) dtdo.getCookie(TreeEditorCookie.class);
            if (cake != null) {
                result = cake.openDocumentRoot();
            } else {
                throw new TreeException("DTDDataObject:INTERNAL ERROR"); // NOI18N
            }
            TreeDTD treeDTD = (TreeDTD) result;

            Iterator it = treeDTD.getElementDeclarations().iterator();

            while (it.hasNext()) {
                TreeElementDecl decl = (TreeElementDecl) it.next();
                String name = decl.getName();
                add(name);
            }
            
            // ask for data object location
            
            FileObject primFile = dtdo.getPrimaryFile();
            String name = primFile.getName() + Util.THIS.getString("NAME_SUFFIX_Stylesheet");
            FileObject folder = primFile.getParent();

            FileObject generFile = (new SelectFileDialog (folder, name, "css")).getFileObject(); // NOI18N
            name = generFile.getName();
            
            // create and open data object
            
            DataObject targeto;

            
            try {
                targeto = DataObject.find(generFile);
            } catch (DataObjectNotFoundException eex) {
                return;
            }

            EditorCookie ec = (EditorCookie) targeto.getCookie(EditorCookie.class);
            if (ec != null) {
                Document doc = ec.openDocument();
                
                try {
                    doc.remove(0, doc.getLength());
                    doc.insertString(0, css, null);
                    ec.saveDocument();
                } catch (BadLocationException locex) {
                    emgr.annotate(locex, Util.THIS.getString("MSG_Leaving_CSS_in_clipboard"));
                    emgr.notify(locex);                    
                    
                    StringSelection ss = new StringSelection(css);
                    ExClipboard clipboard = (ExClipboard) Lookup.getDefault().lookup(ExClipboard.class);
                    clipboard.setContents(ss, null);
                    GuiUtil.setStatusText(Util.THIS.getString("MSG_CSS_placed_in_clipboard"));
                    
                }
                
                
                OpenCookie oc = (OpenCookie) targeto.getCookie(OpenCookie.class);
                if (oc != null) oc.open();
                
            }
            
        } catch (UserCancelException ex) {
            //user cancelled do nothing
            
        } catch (IOException ex) {

            emgr.annotate(ex, Util.THIS.getString("MSG_IO_ex_CSS_writing."));
            emgr.notify(ex);
            
        } catch (TreeException ex) {
            
            GuiUtil.setStatusText(Util.THIS.getString("MSG_CSS_fatal_error"));
            
        }

    }

    /** adds a new name to just created CSS. */
    private void add(String name) {
        css += name + " { display: block }\n"; // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    public String getName() {
        return Util.THIS.getString("NAME_Generate_CSS");
    }
    
    protected boolean asynchronous() {
        return false;
    }

}
