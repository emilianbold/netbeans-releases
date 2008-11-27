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

package org.netbeans.modules.languages.features;

import java.util.Collections;
import org.netbeans.api.languages.ASTPath;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ParserResult;
import org.netbeans.api.languages.database.DatabaseContext;
import org.netbeans.api.languages.database.DatabaseDefinition;
import org.netbeans.api.languages.database.DatabaseUsage;
import org.netbeans.api.languages.database.DatabaseItem;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.ParseException;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
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
        final JTextComponent comp = getTextComponent(null);
        if (comp == null) return;
        final NbEditorDocument doc = (NbEditorDocument)comp.getDocument();
        Source source = Source.create (doc);
        try {
            ParserManager.parse (Collections.<Source>singleton (source), new UserTask () {
                @Override
                public void run (ResultIterator resultIterator) throws ParseException {
                    int position = comp.getCaretPosition();
                    ParserResult result = (ParserResult) resultIterator.getParserResult ();
                    ASTPath path = result.getRootNode ().findPath (position);
                    DatabaseContext root = result.getSemanticStructure ();
                    if (root == null) return;
                    DatabaseItem item = root.getDatabaseItem (path.getLeaf ().getOffset ());
                    if (item == null) return;
                    if (item instanceof DatabaseUsage) {
                        item = ((DatabaseUsage) item).getDefinition();
                    }

                    int offset = item.getOffset();
                    DataObject dobj = null;
                    StyledDocument docToGo = null;
                    URL url = ((DatabaseDefinition) item).getSourceFileUrl();
                    if (url == null) {
                        dobj = NbEditorUtilities.getDataObject (doc);
                        docToGo = doc;
                    } else {
                        File file = null;
                        try {
                            file = new File(url.toURI());
                        } catch (URISyntaxException ex) {
                            ex.printStackTrace();
                        }

                        if (file != null && file.exists()) {
                            /** convert file to an uni absolute pathed file (../ etc will be coverted) */
                            file = FileUtil.normalizeFile(file);
                            FileObject fobj = FileUtil.toFileObject(file);
                            try {
                                dobj = DataObject.find(fobj);
                            } catch (DataObjectNotFoundException ex) {
                                ex.printStackTrace();
                            }
                            if (dobj != null) {
                                Node nodeOfDobj = dobj.getNodeDelegate();
                                EditorCookie ec = nodeOfDobj.getCookie(EditorCookie.class);
                                try {
                                    docToGo = ec.openDocument();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }

                        }
                    }

                    if (dobj == null) {
                        return;
                    }

                    LineCookie lc = (LineCookie)dobj.getCookie(LineCookie.class);
                    Line.Set lineSet = lc.getLineSet();
                    Line line = lineSet.getCurrent(NbDocument.findLineNumber(docToGo, offset));
                    int column = NbDocument.findLineColumn (docToGo, offset);
                    line.show (Line.SHOW_GOTO, column);
                }
            });
        } catch (ParseException ex) {
            ex.printStackTrace ();
        }
    }
    
    public boolean isEnabled() {
        JTextComponent comp = getTextComponent(null);
        if (comp == null)
            return false;
        return true;
//!        ASTNode node = getASTNode(comp);
//        int position = comp.getCaretPosition();
//        ASTPath path = node.findPath(position);
//        if (path == null)
//            return false;
//        DatabaseContext root = DatabaseManager.getRoot((ASTNode) path.getRoot());
//        if (root == null)
//            return false;
//        DatabaseItem item = root.getDatabaseItem (path.getLeaf ().getOffset ());
//        return item != null;
    }
}
