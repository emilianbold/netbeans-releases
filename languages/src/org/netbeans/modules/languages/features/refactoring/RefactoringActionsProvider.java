/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.languages.features.refactoring;

import java.util.Collections;
import javax.swing.text.JTextComponent;

import org.netbeans.api.languages.ASTNode;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.api.languages.ParserResult;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.parsing.api.MultiLanguageUserTask;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.spi.ParseException;
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
        final FileObject fobj = getFileObject(lookup);
        EditorCookie ec = lookup.lookup (EditorCookie.class);
        final JTextComponent textComp = ec.getOpenedPanes()[0];
        final NbEditorDocument doc = (NbEditorDocument )textComp.getDocument ();
        final String selectedText = textComp.getSelectedText ();
        Source source = Source.create (doc);
        try {
            ParserManager.parse (Collections.<Source>singleton (source), new MultiLanguageUserTask () {
                @Override
                public void run (ResultIterator resultIterator) throws ParseException {
                    ParserResult parserResult = (ParserResult) resultIterator.getParserResult ();
                    int position = 0;
                    if (selectedText != null) {
                        position = textComp.getSelectionStart();
                        for (int x = 0; x < selectedText.length(); x++) {
                            if (Character.isWhitespace (selectedText.charAt (x))) {
                                position++;
                            } else {
                                break;
                            }
                        }
                    } else {
                        position = textComp.getCaretPosition ();
                    }
                    TopComponent activetc = TopComponent.getRegistry ().getActivated ();
                    ASTNode node = parserResult.getRootNode ();
                    RefactoringUI ui = new WhereUsedQueryUI (node.findPath (position), fobj, doc);
                    UI.openRefactoringUI(ui, activetc);
                }
            });
        } catch (ParseException ex) {
            ex.printStackTrace ();
        }
    }
    
    public void doRename(Lookup lookup) {
        final FileObject fobj = getFileObject(lookup);
        EditorCookie ec = lookup.lookup (EditorCookie.class);
        final JTextComponent textComp = ec.getOpenedPanes()[0];
        final NbEditorDocument doc = (NbEditorDocument )textComp.getDocument ();
        final String selectedText = textComp.getSelectedText ();
        Source source = Source.create (doc);
        try {
            ParserManager.parse (Collections.<Source>singleton (source), new MultiLanguageUserTask () {
                @Override
                public void run (ResultIterator resultIterator) throws ParseException {
                    ParserResult parserResult = (ParserResult) resultIterator.getParserResult ();
                    int position = 0;
                    if (selectedText != null) {
                        position = textComp.getSelectionStart();
                        for (int x = 0; x < selectedText.length(); x++) {
                            if (Character.isWhitespace (selectedText.charAt (x))) {
                                position++;
                            } else {
                                break;
                            }
                        }
                    } else {
                        position = textComp.getCaretPosition ();
                    }
                    TopComponent activetc = TopComponent.getRegistry ().getActivated ();
                    ASTNode node = parserResult.getRootNode ();
                    RefactoringUI ui = new RenameRefactoringUI (parserResult, node.findPath (position), fobj, doc);
                    UI.openRefactoringUI(ui, activetc);
                }
            });
        } catch (ParseException ex) {
            ex.printStackTrace ();
        }
    }
    
    private static FileObject getFileObject(Lookup lookup) {
        Node n = (Node)lookup.lookup(Node.class);
        DataObject dob = n.getCookie(DataObject.class);
        return dob.getPrimaryFile();
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
