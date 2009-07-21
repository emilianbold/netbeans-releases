/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.ruby.debugger;

import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.jrubyparser.ast.INameNode;
import org.jrubyparser.ast.InstVarNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.netbeans.modules.ruby.AstUtilities;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.Line.Part;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;
import org.rubyforge.debugcommons.model.RubyValue;
import org.rubyforge.debugcommons.model.RubyVariable;

public final class ToolTipAnnotation extends Annotation implements Runnable {

    private static final Boolean SKIP_BALLOON_EVAL = Boolean.getBoolean("ruby.debugger.skip.balloon.evaluation"); // NOI18N
    
    private Part lp;
    private EditorCookie ec;
    
    public String getShortDescription() {
        RubySession session = Util.getCurrentSession();
        if (session == null) { return null; }
        Part _lp = (Part) getAttachedAnnotatable();
        if (_lp == null) { return null; }
        Line line = _lp.getLine();
        DataObject dob = DataEditorSupport.findDataObject(line);
        if (dob == null) { return null; }
        EditorCookie _ec = dob.getCookie(EditorCookie.class);
        if (_ec == null) { return null; }
        this.lp = _lp;
        this.ec = _ec;
        RequestProcessor.getDefault().post(this);
        return null;
    }
    
    public void run() {
        if (SKIP_BALLOON_EVAL) {
            return;
        }
        if (lp == null || ec == null) { return; }
        StyledDocument doc;
        try {
            doc = ec.openDocument();
        } catch (IOException ex) {
            return;
        }
        JEditorPane ep = EditorContextDispatcher.getDefault().getCurrentEditor();
        if (ep == null) { return; }
        String expression = getIdentifier(doc, ep, NbDocument.findLineOffset(doc, lp.getLine().getLineNumber()) + lp.getColumn());
        if (expression == null) { return; }
        RubySession session = Util.getCurrentSession();
        if (session == null) { return; }
        RubyVariable var = session.inspectExpression(expression);
        if (var == null) { return; }
        RubyValue value = var.getValue();
        if (value == null) { return; }
        String stringVal = value.getValueString();
        if (stringVal == null || stringVal.equals(expression)) { return; }
        String toolTipText = expression + " = " + stringVal; // NOI18N
        firePropertyChange(PROP_SHORT_DESCRIPTION, null, toolTipText);
    }
    
    public String getAnnotationType() {
        return null; // Currently return null annotation type
    }
    
    /** TODO: based on the Java. Tune it up appropriately for Ruby. */
    private static String getIdentifier(final StyledDocument doc, final JEditorPane ep, final int offset) {
        if ((ep.getSelectionStart() <= offset) && (offset <= ep.getSelectionEnd())) {
            return ep.getSelectedText();
        }
        int line = NbDocument.findLineNumber(doc, offset);
        int col = NbDocument.findLineColumn(doc, offset);
        Element lineElem = NbDocument.findLineRootElement(doc).getElement(line);
        if (lineElem == null) {
            return null;
        }
        int lineStartOffset = lineElem.getStartOffset();
        int lineLen = lineElem.getEndOffset() - lineStartOffset;
        if (col + 1 >= lineLen) {
            // do not evaluate when mouse hover behind the end of line (112662)
            return null;
        }
        FileObject fo = EditorContextDispatcher.getDefault().getCurrentFile();
        if (fo == null) {
            // can this happen??
            return null;
        }
        return getExpressionToEvaluate(fo, offset);
    }

    static String getExpressionToEvaluate(FileObject fo, int offset) {
        Node root = AstUtilities.getRoot(fo);
        if (root == null) {
            return null;
        }
        Node node = AstUtilities.findNodeAtOffset(root, offset);
        if (node == null) {
            return null;
        }
        // handles the case when the caret is placed just before the
        // expression to evaluate, e.g. "^var.foo"
        if (node.getNodeType() == NodeType.NEWLINENODE) {
            node = AstUtilities.findNodeAtOffset(root, offset + 1);
        }
        if (shouldEvaluate(node) && node instanceof INameNode) {
            return AstUtilities.getName(node);
        }
        return null;

    }

    private static boolean shouldEvaluate(Node node) {
        if (node.getNodeType() == null) {
            return false;
        }
        // the types we want to evaluate without forcing the user
        // to select anything. these should be safe to evaluate without 
        // side effects (unlike evaluating e.g. method calls).
        switch (node.getNodeType()) {
            case ARGUMENTNODE:
            case DVARNODE:
            case DASGNNODE:
            case SELFNODE:
            case LOCALVARNODE:
            case LOCALASGNNODE:
            case INSTVARNODE:
            case INSTASGNNODE:
            case GLOBALVARNODE:
            case GLOBALASGNNODE:
            case CONSTNODE:
            case CONSTDECLNODE:
            case CLASSVARNODE:
            case CLASSVARASGNNODE:
            case NILNODE:
            case TRUENODE:
            case FALSENODE:
                return true;
            default:
                return false;
        }
    }
}

