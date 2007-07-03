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

package org.netbeans.modules.ruby.debugger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.Annotation;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.Line.Part;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.rubyforge.debugcommons.model.RubyValue;
import org.rubyforge.debugcommons.model.RubyVariable;

public final class ToolTipAnnotation extends Annotation implements Runnable {
    
    private Part lp;
    private EditorCookie ec;
    
    public String getShortDescription() {
        RubySession session = Util.getCurrentSession();
        if (session == null) { return null; }
        Part lp = (Part) getAttachedAnnotatable();
        if (lp == null) { return null; }
        Line line = lp.getLine();
        DataObject dob = DataEditorSupport.findDataObject(line);
        if (dob == null) { return null; }
        EditorCookie ec = dob.getCookie(EditorCookie.class);
        if (ec == null) { return null; }
        this.lp = lp;
        this.ec = ec;
        RequestProcessor.getDefault().post(this);
        return null;
    }
    
    public void run() {
        if (lp == null || ec == null) { return; }
        StyledDocument doc;
        try {
            doc = ec.openDocument();
        } catch (IOException ex) {
            return;
        }
        JEditorPane ep = getCurrentEditor();
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
        String t = null;
        if ((ep.getSelectionStart() <= offset) && (offset <= ep.getSelectionEnd())) {
            t = ep.getSelectedText();
        }
        if (t != null) { return t; }
        int line = NbDocument.findLineNumber(doc, offset);
        int col = NbDocument.findLineColumn(doc, offset);
        Element lineElem = NbDocument.findLineRootElement(doc).getElement(line);
        try {
            if (lineElem == null) { return null; }
            int lineStartOffset = lineElem.getStartOffset();
            int lineLen = lineElem.getEndOffset() - lineStartOffset;
            t = doc.getText(lineStartOffset, lineLen);
            int identStart = col;
            while (identStart > 0 && (Character.isJavaIdentifierPart(t.charAt(identStart - 1)) || (t.charAt(identStart - 1) == '.'))) {
                identStart--;
            }
            int identEnd = col;
            while (identEnd < lineLen && Character.isJavaIdentifierPart(t.charAt(identEnd))) {
                identEnd++;
            }
            if (identStart == identEnd) { return null; }
            return t.substring(identStart, identEnd);
        } catch (BadLocationException e) {
            return null;
        }
    }
    
    /** Returns current editor component instance. */
    private static JEditorPane getCurrentEditor_() {
        EditorCookie e = getCurrentEditorCookie();
        if (e == null) { return null; }
        JEditorPane[] op = e.getOpenedPanes();
        if ((op == null) || (op.length < 1)) { return null; }
        return op[0];
    }
    
    private static JEditorPane getCurrentEditor() {
        if (SwingUtilities.isEventDispatchThread()) {
            return getCurrentEditor_();
        } else {
            final JEditorPane[] ce = new JEditorPane[1];
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        ce[0] = getCurrentEditor_();
                    }
                });
            } catch (InvocationTargetException ex) {
                ErrorManager.getDefault().notify(ex.getTargetException());
            } catch (InterruptedException ex) {
                ErrorManager.getDefault().notify(ex);
            }
            return ce[0];
        }
    }
    
    /**
     * Returns current editor component instance.
     *
     * @return current editor component instance
     */
    private static EditorCookie getCurrentEditorCookie() {
        Node[] nodes = TopComponent.getRegistry().getActivatedNodes();
        if ((nodes == null) || (nodes.length != 1)) {
            return null;
        }
        Node n = nodes[0];
        return n.getCookie(EditorCookie.class);
    }
    
}

