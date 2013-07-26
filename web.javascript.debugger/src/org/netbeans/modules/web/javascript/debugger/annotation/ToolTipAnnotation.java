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
package org.netbeans.modules.web.javascript.debugger.annotation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.PopupManager;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ToolTipSupport;
import org.netbeans.modules.web.javascript.debugger.eval.Evaluator;
import org.netbeans.modules.web.javascript.debugger.locals.VariablesModel;
import org.netbeans.modules.web.javascript.debugger.locals.VariablesModel.ScopedRemoteObject;
import org.netbeans.modules.web.javascript.debugger.watches.WatchesModel;
import org.netbeans.modules.web.webkit.debugging.api.Debugger;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject.Type;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 * @author ads
 *
 */
@NbBundle.Messages({
    "# {0} - variable name",
    "var.undefined={0} is not defined"
})
public class ToolTipAnnotation extends Annotation
    implements PropertyChangeListener
{
    
    private static final Set<String> JAVASCRIPT_KEYWORDS = new HashSet<String>(Arrays.asList(new String[] {
        "break",    "case",     "catch",    "continue", "debugger",
        "default",  "delete",   "do",       "else",     "finally",
        "for",      "function", "if",       "in",       "instanceof",
        "new",      "return",   "switch",   /*"this",*/ "throw",
        "try",      "typeof",   "var",      "void",     "while",
        "with"
    }));
    
    private static final Set<String> JAVASCRIPT_RESERVED = new HashSet<String>(Arrays.asList(new String[] {
        "class",    "enum",     "export",   "extends",  "import",   "super",
        "implements","interface","let",     "package",  "private",  "protected",
        "public",   "static",   "yield"
    }));

    private static final RequestProcessor RP = new RequestProcessor("Tool Tip Annotation"); //NOI18N

    /* (non-Javadoc)
     * @see org.openide.text.Annotation#getAnnotationType()
     */
    @Override
    public String getAnnotationType()
    {
        return null; // Currently return null annotation type
    }

    /* (non-Javadoc)
     * @see org.openide.text.Annotation#getShortDescription()
     */
    @Override
    public String getShortDescription()
    {
        Debugger d = getDebugger();
        if (d == null || !d.isSuspended()) {
            return null;
        }

        final Line.Part lp = (Line.Part) getAttachedAnnotatable();
        if (lp == null) return null;
        Line line = lp.getLine ();
        DataObject dob = DataEditorSupport.findDataObject (line);
        if (dob == null) return null;
        final EditorCookie ec = dob.getCookie(EditorCookie.class);
        if (ec == null)
            return null;
            // Only for editable dataobjects

        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                evaluate(lp, ec);
            }
        };
        RP.post(runnable);
        return null;
    }

    private void evaluate(Line.Part lp, EditorCookie ec) {
        Line line = lp.getLine();
        if (line == null) {
            return;
        }
        StyledDocument document;
        try {
            document = ec.openDocument();
        } catch (IOException ex) {
            return;
        }
        if (document == null) {
            return;
        }
        int lineNo = lp.getLine().getLineNumber();
        int column = lp.getColumn();
        final int offset = NbDocument.findLineOffset(document, lineNo) + column;
        final JEditorPane ep = EditorContextDispatcher.getDefault().getMostRecentEditor();
        final String expression = getIdentifier (
            document,
            ep,
            lineNo,
            column,
            offset
        );
        if (expression == null) {
            return;
        }
        
        ScopedRemoteObject tooltipVariable = null;
        String tooltipText = null;
        final Debugger d = getDebugger();
        if (d != null && d.isSuspended()) {
            CallFrame frame = d.getCurrentCallFrame();
            if (frame == null) {
                return;
            }
            VariablesModel.ScopedRemoteObject sv = Evaluator.evaluateExpression(frame, expression, true);
            if (sv != null) {
                RemoteObject var = sv.getRemoteObject();
                String value = var.getValueAsString();
                Type type = var.getType();
                switch (type) {
                    case STRING:
                        value = "\"" + value + "\"";
                        break;
                    case FUNCTION:
                        value = var.getDescription();
                        break;
                    case OBJECT:
                        String clazz = var.getClassName();
                        if (clazz == null) {
                            clazz = type.getName();
                        }
                        if (value.isEmpty()) {
                            value = var.getDescription();
                        }
                        value = "("+clazz+") "+value;
                        tooltipVariable = sv;
                        // TODO: add obj ID
                }
                if (type != Type.UNDEFINED) {
                    tooltipText = expression + " = " + value;
                } else {
                    tooltipText = var.getDescription();
                    if (tooltipText == null) {
                        tooltipText = Bundle.var_undefined(expression);
                    }
                }
            }
        } else {
            return;
        }
        if (tooltipVariable != null) {
            final ScopedRemoteObject var = tooltipVariable;
            final String toolTip = tooltipText;
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    final ToolTipView.ExpandableTooltip et = ToolTipView.createExpandableTooltip(toolTip);
                    et.addExpansionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            et.setBorder(BorderFactory.createLineBorder(et.getForeground()));
                            et.removeAll();
                            et.setWidthCheck(false);
                            final ToolTipView ttView = ToolTipView.getToolTipView(d, expression, var);
                            et.add(ttView);
                            et.revalidate();
                            et.repaint();
                            SwingUtilities.invokeLater(new Runnable() {
                                public @Override void run() {
                                    EditorUI eui = Utilities.getEditorUI(ep);
                                    if (eui != null) {
                                        ttView.setToolTipSupport(eui.getToolTipSupport());
                                        eui.getToolTipSupport().setToolTip(et, PopupManager.ViewPortBounds, PopupManager.AbovePreferred, 0, 0, ToolTipSupport.FLAGS_HEAVYWEIGHT_TOOLTIP);
                                    } else {
                                        firePropertyChange (PROP_SHORT_DESCRIPTION, null, toolTip);
                                    }
                                }
                            });
                        }
                    });
                    EditorUI eui = Utilities.getEditorUI(ep);
                    if (eui != null) {
                        eui.getToolTipSupport().setToolTip(et);
                    } else {
                        firePropertyChange (PROP_SHORT_DESCRIPTION, null, toolTip);
                    }
                }
            });
        } else {
            firePropertyChange(PROP_SHORT_DESCRIPTION, null, tooltipText);
        }
        //TODO: review, replace the code depending on lexer.model - part I
    }
    
    private static String getIdentifier(final StyledDocument doc, final JEditorPane ep,
                                        int line, int column, final int offset) {
        
        String t = null;
        if ((ep.getSelectionStart() <= offset) && (offset <= ep.getSelectionEnd())) {
            t = ep.getSelectedText();
        }
        if (t != null) {
            return t;
        }
        Element lineElem = NbDocument.findLineRootElement(doc).getElement(line);
        if (lineElem == null) return null;
        int lineStartOffset = lineElem.getStartOffset ();
        int lineLen = lineElem.getEndOffset() - lineStartOffset;
        try {
            t = doc.getText (lineStartOffset, lineLen);
        } catch (BadLocationException ble) {
            return null;
        }
        column = Math.min(column, t.length());
        int identStart = column;
        boolean wasDot = false;
        while (identStart > 0) {
            char c = t.charAt (identStart - 1);
            if (Character.isJavaIdentifierPart(c) ||
                (c == '.' && (wasDot = true)) ||        // Please note that '=' is intentional here.
                (wasDot && (c == ']' || c == '['))) {
                
                identStart--;
            } else {
                break;
            }
        }
        int identEnd = column;
        while (identEnd < lineLen &&
                Character.isJavaIdentifierPart(t.charAt(identEnd))) {
            identEnd++;
        }
        if (identStart == identEnd) return null;
        String ident = t.substring (identStart, identEnd).trim();
        if (JAVASCRIPT_KEYWORDS.contains(ident) || JAVASCRIPT_RESERVED.contains(ident)) {
            // JavaScript keyword => Do not show anything
            return null;
        }
        return ident;
    }

//    private static String getIdentifier(final StyledDocument doc, final JEditorPane ep, final int offset) {
//        String t = null;
//        if ((ep.getSelectionStart() <= offset) && (offset <= ep.getSelectionEnd())) {
//            t = ep.getSelectedText();
//        }
//        if (t != null) { return t; }
//        int line = NbDocument.findLineNumber(doc, offset);
//        int col = NbDocument.findLineColumn(doc, offset);
//        Element lineElem = NbDocument.findLineRootElement(doc).getElement(line);
//        try {
//            if (lineElem == null) { return null; }
//            int lineStartOffset = lineElem.getStartOffset();
//            int lineLen = lineElem.getEndOffset() - lineStartOffset;
//            if (col + 1 >= lineLen) {
//                // do not evaluate when mouse hover behind the end of line (112662)
//                return null;
//            }
//            t = doc.getText(lineStartOffset, lineLen);
//            return getExpressionToEvaluate(t, col);
//        } catch (BadLocationException e) {
//            return null;
//        }
//    }
//
//    static String getExpressionToEvaluate(String text, int col) {
//        int identStart = col;
//        boolean isInFieldDeclaration = false;
//        while (identStart > 0 && ((text.charAt(identStart - 1) == ' ') || isPHPIdentifier(text.charAt(identStart - 1)) || (text.charAt(identStart - 1) == '.') || (text.charAt(identStart - 1) == '>'))) {
//            identStart--;
//            if (text.charAt(identStart) == '>') { // NOI18N
//                if (text.charAt(identStart - 1) == '-') { // NOI18N
//                    identStart--; // matched object operator ->
//                } else {
//                    identStart++;
//                    break;
//                }
//            }
//            if (text.charAt(identStart) == ' ') {
//                String possibleAccessModifier = text.substring(0, identStart).trim();
//                if (endsWithAccessModifier(possibleAccessModifier)) {
//                    isInFieldDeclaration = true;
//                }
//                break;
//            }
//        }
//        int identEnd = col;
//        while (identEnd < text.length() && Character.isJavaIdentifierPart(text.charAt(identEnd))) {
//            identEnd++;
//        }
//        if (identStart == identEnd) {
//            return null;
//        }
//        String simpleExpression = text.substring(identStart, identEnd).trim();
//        String result = simpleExpression;
//        if (isInFieldDeclaration && simpleExpression.length() > 1) {
//            result = "$this->" + simpleExpression.substring(1); //NOI18N
//        }
//        return result;
//    }
//
//    private static boolean endsWithAccessModifier(final String possibleAccessModifier) {
//        String lowerCased = possibleAccessModifier.toLowerCase();
//        return lowerCased.endsWith("private") || lowerCased.endsWith("protected") || lowerCased.endsWith("public") || lowerCased.endsWith("var"); //NOI18N
//    }
//
//    static boolean isPHPIdentifier(char ch) {
//        return isDollarMark(ch) || Character.isJavaIdentifierPart(ch);
//    }
//
//    static boolean isPHPIdentifier(String text) {
//        for (int i = 0; i < text.length(); i++) {
//            if (!isPHPIdentifier(text.charAt(i))) {
//                return false;
//            }
//        }
//        return text.length() > 0;
//    }
//
//    private static boolean isDollarMark(char ch) {
//        return ch == '$';//NOI18N
//    }
//
//    private boolean isPhpDataObject( DataObject dataObject ) {
//        return Utils.isPhpFile( dataObject.getPrimaryFile() );
//    }
//
//    //TODO: review, replace the code depending on lexer.model - part II (methods computeVariable, getExpression)
//    /*private void computeVariable( FileObject fObject, int offset ){
//        PhpModel model = ModelAccess.getAccess().getModel(
//                ModelAccess.getModelOrigin( fObject ));
//        if ( model == null ){
//            return;
//        }
//        SourceElement element = model.findSourceElement(offset);
//        if ( element == null ){
//            return;
//        }
//        String expression = getExpression( element );
//        if ( expression != null ) {
//            sendEvalCommand( expression );
//        }
//    }
//
//    private String getExpression( SourceElement element ) {
//        if ( element == null ){
//            return null;
//        }
//        if ( element instanceof Expression ){
//            return element.getText();
//        }
//        else {
//            return getExpression( element.getParent() );
//        }
//    }*/
//
//    private void sendEvalCommand( String str ){
//        DebugSession session = getSession();
//        if ( session == null ){
//            return;
//        }
//        EvalCommand command = new EvalCommand( session.getTransactionId() );
//        command.setData( str );
//        command.addPropertyChangeListener( this );
//        session.sendCommandLater(command);
//    }
//    private void sendPropertyGetCommand( String str ){
//        DebugSession session = getSession();
//        if ( session == null ){
//            return;
//        }
//        PropertyGetCommand command = new PropertyGetCommand( session.getTransactionId() );
//        command.setName( str );
//        command.addPropertyChangeListener( this );
//        session.sendCommandLater(command);
//    }

    private String getSelectedText( JEditorPane pane , int offset ){
        if ((pane != null && pane.getSelectionStart() <= offset) &&
                (offset <= pane.getSelectionEnd()))
        {
            return pane.getSelectedText();
        }
        return null;
    }

    private Debugger getDebugger() {
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager()
                .getCurrentEngine();
        if (currentEngine == null) {
            return null;
        }
        return currentEngine.lookupFirst(null, Debugger.class);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
    }


}
