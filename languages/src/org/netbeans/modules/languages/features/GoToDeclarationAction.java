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
        if (path == null)
            return false;
        DatabaseContext root = DatabaseManager.getRoot((ASTNode) path.getRoot());
        if (root == null)
            return false;
        DatabaseItem item = root.getDatabaseItem (path.getLeaf ().getOffset ());
        return item != null;
    }
    
    private ASTNode getASTNode(JTextComponent comp) {
        return ParserManagerImpl.getImpl ((NbEditorDocument)comp.getDocument ()).getAST ();
    }
}
