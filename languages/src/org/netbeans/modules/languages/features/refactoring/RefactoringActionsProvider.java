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

package org.netbeans.modules.languages.features.refactoring;

import javax.swing.JEditorPane;
import javax.swing.text.Document;
import java.util.Collection;
import javax.swing.text.JTextComponent;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.windows.TopComponent;

/**
 *
 * @author Daniel Prusa
 */
public class RefactoringActionsProvider extends ActionsImplementationProvider {
    
    private static final String JS_MIME_TYPE = "text/javascript"; // NOI18N
    
    public boolean canFindUsages(Lookup lookup) {
        return canRefactor(lookup);
    }

    public boolean canRename(Lookup lookup) {
        return canRefactor(lookup);
    }
    
    public void doFindUsages(Lookup lookup) {
        FileObject fobj = getFileObject(lookup);
        Object[] objs = getASTPathAndDocument(lookup);
        ASTPath path = (ASTPath)objs[0];
        Document doc = (Document)objs[1];
        TopComponent activetc = TopComponent.getRegistry().getActivated();
        RefactoringUI ui = new WhereUsedQueryUI(path, fobj, doc);
        UI.openRefactoringUI(ui, activetc);
    }
    
    public void doRename(Lookup lookup) {
        FileObject fobj = getFileObject(lookup);
        Object[] objs = getASTPathAndDocument(lookup);
        ASTPath path = (ASTPath)objs[0];
        Document doc = (Document)objs[1];
        TopComponent activetc = TopComponent.getRegistry().getActivated();
        RefactoringUI ui = new RenameRefactoringUI(path, fobj, doc);
        UI.openRefactoringUI(ui, activetc);
    }
    
    private static FileObject getFileObject(Lookup lookup) {
        Node n = (Node)lookup.lookup(Node.class);
        DataObject dob = n.getCookie(DataObject.class);
        return dob.getPrimaryFile();
    }
    
    private static Object[] getASTPathAndDocument(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        JTextComponent textComp = ec.getOpenedPanes()[0];
        NbEditorDocument doc = (NbEditorDocument)textComp.getDocument();
        String selectedText = textComp.getSelectedText();
        ASTNode node = null;
        try {
            node = ParserManager.get(doc).getAST();
        } catch (ParseException e) {
            return null;
        }
        int position = 0;
        if (selectedText != null) {
            position = textComp.getSelectionStart();
            for (int x = 0; x < selectedText.length(); x++) {
                if (Character.isWhitespace(selectedText.charAt(x))) {
                    position++;
                } else {
                    break;
                }
            }
        } else {
            position = textComp.getCaretPosition();
        }
        return new Object[] {node.findPath(position), doc};
    }
    
    private static boolean canRefactor(Lookup lookup) {
        Collection<? extends Node> nodes = lookup.lookupAll(Node.class);
        if (nodes.size() != 1) {
            return false;
        }
        Node n = nodes.iterator().next();
        DataObject dob = n.getCookie(DataObject.class);
        if ((dob != null) && (JS_MIME_TYPE.equals(dob.getPrimaryFile().getMIMEType()))) {
            EditorCookie ec = lookup.lookup(EditorCookie.class);
            if (ec == null) {
                return false;
            }
            JEditorPane[] panes = ec.getOpenedPanes();
            // check if it is called from editor
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            if (!(activetc instanceof CloneableEditorSupport.Pane)) {
                return false;
            }
            if (panes == null || panes.length == 0) {
                return false;
            }
            JTextComponent textComp = panes[0];
            Document doc = textComp.getDocument();
            if (!(doc instanceof NbEditorDocument)) {
                return false;
            }
            ASTNode node = null;
            try {
                node = ParserManager.get((NbEditorDocument)doc).getAST();
            } catch (ParseException e) {
            }
            return node != null;
        }
        return false;
    }
    
}
