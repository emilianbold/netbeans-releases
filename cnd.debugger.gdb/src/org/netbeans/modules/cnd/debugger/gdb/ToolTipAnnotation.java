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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb;

import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.text.Line.Part;
import org.openide.util.RequestProcessor;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.InvalidExpressionException;
import org.netbeans.modules.cnd.debugger.gdb.LocalVariable;
import org.netbeans.modules.cnd.debugger.gdb.Variable;

import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/*
 * ToolTipAnnotation.java
 * This class implements "Balloon Evaluation" feature.
 *
 * @author Nik Molchanov (copied from JPDA implementation)
 */
public class ToolTipAnnotation extends Annotation {
    
    private String expression;

    public String getShortDescription() {
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (currentEngine == null) {
            return null;
        }
        GdbDebugger debugger = (GdbDebugger) currentEngine.lookupFirst(null, GdbDebugger.class);
        if (debugger == null) {
            return null;
        }
        Part lp = (Part) getAttachedAnnotatable();
        if (lp == null) {
            return null;
        }
        Line line = lp.getLine();
        DataObject dob = DataEditorSupport.findDataObject(line);
        if (dob == null) {
            return null;
        }
        EditorCookie ec = (EditorCookie) dob.getCookie(EditorCookie.class);
        if (ec == null) {
            return null;
        }

        try {
            StyledDocument doc = ec.openDocument();
            JEditorPane ep = getCurrentEditor();
            if (ep == null) {
                return null;
            }
            synchronized (this) {
                expression = getIdentifier(doc, ep, NbDocument.findLineOffset(doc,
                        lp.getLine().getLineNumber()) + lp.getColumn());
                if (expression == null) {
                    return null;
                }
            }
            try {
                // There is a small window during debugger startup when getGdbProxy() returns null
                int token = debugger.getGdbProxy().data_evaluate_expression(expression);
                debugger.completeToolTip(token, this);
            } catch (NullPointerException npe) {
            }
        } catch (IOException e) {
        }
        return null;
    }

    public void postToolTip(String value) {
        if (expression == null) {
            return;
        }
        int i = expression.indexOf('\n');
        if (i >= 0) {
            expression = expression.substring(0, i);
        }
        
        final String toolTipText = expression + " = " + value; // NOI18N
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                firePropertyChange(PROP_SHORT_DESCRIPTION, null, toolTipText);
            }
        });
    }

    public String getAnnotationType () {
        return null; // Currently return null annotation type
    }

    private static String getIdentifier(StyledDocument doc, JEditorPane ep, int offset) {
        String t = null;
        if ((ep.getSelectionStart() <= offset) && (offset <= ep.getSelectionEnd())) {
            t = ep.getSelectedText();
        }
        if (t != null) {
            return t;
        }
        
        int line = NbDocument.findLineNumber(doc, offset);
        int col = NbDocument.findLineColumn(doc, offset);
        try {
            Element lineElem = NbDocument.findLineRootElement(doc).getElement(line);

            if (lineElem == null) {
                return null;
            }
            int lineStartOffset = lineElem.getStartOffset();
            int lineLen = lineElem.getEndOffset() - lineStartOffset;
            t = doc.getText(lineStartOffset, lineLen);
            int identStart = col;
            while (identStart > 0 && (Character.isJavaIdentifierPart(t.charAt(identStart - 1)) ||
                        (t.charAt (identStart - 1) == '.'))) {
                identStart--;
            }
            int identEnd = col;
            while (identEnd < lineLen && Character.isJavaIdentifierPart(t.charAt(identEnd))) {
                identEnd++;
            }

            if (identStart == identEnd) {
                return null;
            }
            return t.substring(identStart, identEnd);
        } catch (BadLocationException e) {
            return null;
        }
    }
    
    /** 
     * Returns current editor component instance.
     *
     * Used in: ToolTipAnnotation
     */
    private static JEditorPane getCurrentEditor() {
        EditorCookie e = getCurrentEditorCookie();
        if (e == null) {
            return null;
        }
        JEditorPane[] op = EditorContextImpl.getOpenedPanes(e);
        if ((op == null) || (op.length < 1)) {
            return null;
        }
        return op[0];
    }
    
    /** 
     * Returns current editor component instance.
     *
     * @return current editor component instance
     */
    private static EditorCookie getCurrentEditorCookie() {
        Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
        if (nodes == null || nodes.length != 1) {
            return null;
        }
        Node n = nodes[0];
        return (EditorCookie) n.getCookie(EditorCookie.class);
    }
}

