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

package org.netbeans.modules.languages.features;

import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParseException;
import java.awt.event.ActionEvent;
import javax.swing.text.JTextComponent;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ParseException;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.openide.ErrorManager;
import org.openide.cookies.LineCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.NbBundle;

/**
 *
 * @author Daniel Prusa
 */
public class GoToDeclarationAction extends BaseAction {
    
    public GoToDeclarationAction () {
        super(NbBundle.getBundle(GoToDeclarationAction.class).getString("LBL_GoToDeclaration"));
    }
    
    public void actionPerformed (ActionEvent e, JTextComponent component) {
        JTextComponent comp = getTextComponent(null);
        if (comp == null) return;
        ASTNode node = getASTNode(comp);
        if (node == null) return;
        NbEditorDocument doc = (NbEditorDocument)comp.getDocument();
        int position = comp.getCaretPosition();
        ASTPath path = node.findPath(position);
        DatabaseContext root = DatabaseManager.getRoot((ASTNode) path.getRoot());
        if (root == null) return;
        DatabaseItem item = root.getDatabaseItem (path.getLeaf ().getOffset ());
        if (item == null) return;
        if (item instanceof DatabaseUsage) {
            item = ((DatabaseUsage) item).getDefinition();
        }
        int offset = item.getOffset();
        DataObject dobj = NbEditorUtilities.getDataObject (doc);
        LineCookie lc = (LineCookie)dobj.getCookie(LineCookie.class);
        Line.Set lineSet = lc.getLineSet();
        Line line = lineSet.getCurrent(NbDocument.findLineNumber(doc, offset));
        int column = NbDocument.findLineColumn (doc, offset);
        line.show (Line.SHOW_GOTO, column);
    }
    
    public boolean isEnabled() {
        JTextComponent comp = getTextComponent(null);
        if (comp == null)
            return false;
        ASTNode node = getASTNode(comp);
        if (node == null)
            return false;
        int position = comp.getCaretPosition();
        ASTPath path = node.findPath(position);
        DatabaseContext root = DatabaseManager.getRoot((ASTNode) path.getRoot());
        if (root == null)
            return false;
        DatabaseItem item = root.getDatabaseItem (path.getLeaf ().getOffset ());
        return item != null;
    }
    
    private ASTNode getASTNode(JTextComponent comp) {
        try {
            return ParserManagerImpl.get((NbEditorDocument)comp.getDocument()).getAST();
        } catch (ParseException ex) {
            ErrorManager.getDefault().notify(ex);
        } 
        return null;
    }
    
}
