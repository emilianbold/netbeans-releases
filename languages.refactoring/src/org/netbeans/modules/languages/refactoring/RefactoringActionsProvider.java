/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.languages.refactoring;

import javax.swing.text.Document;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.openide.windows.TopComponent;


/**
 *
 * 
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider.class, position=100)
public class RefactoringActionsProvider extends ActionsImplementationProvider {
    
    private static final String JS_MIME_TYPE = "text/javascript"; // NOI18N
    
    @Override
    public boolean canFindUsages(Lookup lookup) {
        return canRefactor(lookup);
    }

    @Override
    public boolean canRename(Lookup lookup) {
        return canRefactor(lookup);
    }
    
    @Override
    public void doFindUsages(Lookup lookup) {
        try {
            FileObject fobj = getFileObject(lookup);
            Object[] objs = getASTPathAndDocument(lookup);
            ASTPath path = (ASTPath)objs[0];
            Document doc = (Document)objs[1];
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            RefactoringUI ui = new WhereUsedQueryUI(path, fobj, doc);
            UI.openRefactoringUI(ui, activetc);
        } catch (ParseException e) {
            Logger.getLogger(RefactoringActionsProvider.class.getName()).log(Level.SEVERE, "Error parsing document", e);
        }
    }
    
    @Override
    public void doRename(Lookup lookup) {
        try {
            FileObject fobj = getFileObject(lookup);
            Object[] objs = getASTPathAndDocument(lookup);
            ASTPath path = (ASTPath)objs[0];
            Document doc = (Document)objs[1];
            TopComponent activetc = TopComponent.getRegistry().getActivated();
            RefactoringUI ui = new RenameRefactoringUI(path, fobj, doc);
            UI.openRefactoringUI(ui, activetc);
        } catch (ParseException e) {
            Logger.getLogger(RefactoringActionsProvider.class.getName()).log(Level.SEVERE, "Error parsing document", e);
        }
    }
    
    private static FileObject getFileObject(Lookup lookup) {
        Node n = (Node)lookup.lookup(Node.class);
        DataObject dob = n.getCookie(DataObject.class);
        return dob.getPrimaryFile();
    }
    
    private static Object[] getASTPathAndDocument(Lookup lookup) throws ParseException {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        JTextComponent textComp = ec.getOpenedPanes()[0];
        NbEditorDocument doc = (NbEditorDocument)textComp.getDocument();
        String selectedText = textComp.getSelectedText();
        ASTNode node = ParserManager.get (doc).getAST ();
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
        // Disabled - this module only seems to handle JavaScript, not
        // Schliemann filetypes in general, and JavaScript is handled by
        // the javascript.refactoring module now
        return false;
        /*
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
         */
    }
    
}
