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
package org.netbeans.modules.css.actions;

import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.net.*;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.NbEditorUtilities;

import org.openide.*;
import org.openide.awt.StatusDisplayer;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.datatransfer.ExClipboard;
import org.openide.util.actions.*;
import org.openide.filesystems.*;

import org.netbeans.modules.css.*;

/**
 * Action that put XML style processing instruction in clipboard.
 * TODO add PI flavor.
 *
 * @author Petr Kuzel
 */
public abstract class CopyStyleAction extends BaseAction {
    
    protected static final String comment = NbBundle.getMessage(CopyStyleAction.class, "Style-Comment") + "\n"; // NOI18N
    
    public CopyStyleAction(String name) {
        super(name);
    }
    
    public void actionPerformed(ActionEvent evt, JTextComponent target) {
        BaseDocument bdoc = Utilities.getDocument(target);
        if(bdoc == null) {
            return ; //no document?!?!
        }
        DataObject csso = NbEditorUtilities.getDataObject(bdoc);
        if(csso == null) {
            return ; //document not backuped by DataObject
        }
        
        String pi = createText(csso);
        StringSelection ss = new StringSelection(pi);
        ExClipboard clipboard = Lookup.getDefault().lookup(ExClipboard.class);
        clipboard.setContents(ss, null);
        StatusDisplayer.getDefault().setStatusText( NbBundle.getMessage(CheckStyleAction.class, "MSG_Style_tag_in_clipboard"));  // NOI18N
        
    }
    
    /** A method that creates particular clipboard text.
     * @return text to be placed to clip board.
     */
    protected abstract String createText(DataObject dobj);
    
    /** Get help context for the action.
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }
    
    /** Converts CSS fileobject to its href that is valid during IDE runtime. */
    protected String getHref(FileObject fo) {
        URL u = URLMapper.findURL(fo, URLMapper.NETWORK);
        if (u != null) {
            return u.toExternalForm();
        } else {
            return fo.getPath();
        }
    }
    
    /** Produces XML PI text. */
    public final static class XML extends CopyStyleAction {
        
        public static final String copyStyleAction = "copy-xml-style"; // NOI18N
        
        public XML() {
            super(copyStyleAction);
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(CopyStyleAction.class, "Copy-XML-Style"));
        }
        
        protected String createText(DataObject csso) {
            return comment + "<?xml-stylesheet type=\"text/css\" href=\"" + this.getHref(csso.getPrimaryFile()) + "\" ?>"; // NOI18N
        }
        
    }
    
    /** Produces HTML style text. */
    public final static class HTML extends CopyStyleAction {

        public static final String copyStyleAction = "copy-html-style"; // NOI18N
        
        public HTML() {
            super(copyStyleAction);
            putValue(SHORT_DESCRIPTION, NbBundle.getMessage(CopyStyleAction.class, "Copy-HTML-Style"));
        }
        
        protected String createText(DataObject csso) {
            return comment + "<link rel=\"StyleSheet\" type=\"text/css\" href=\"" + this.getHref(csso.getPrimaryFile()) + "\" media=\"screen\" >";  // NOI18N
        }

    }
}
