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

package org.netbeans.modules.debugger.jpda.projects;

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
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAWatch;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;

import org.openide.nodes.Node;
import org.openide.windows.TopComponent;


public class ToolTipAnnotation extends Annotation implements Runnable {

    
    private String expression;

    public String getShortDescription () {
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        if (currentEngine == null) return null;
        JPDADebugger d = (JPDADebugger) currentEngine.lookupFirst 
            (null, JPDADebugger.class);
        if (d == null) return null;

        Part lp = (Part) getAttachedAnnotatable();
        if (lp == null) return null;
        Line line = lp.getLine ();
        DataObject dob = DataEditorSupport.findDataObject (line);
        if (dob == null) return null;
        EditorCookie ec = (EditorCookie) dob.getCookie (EditorCookie.class);
        if (ec == null) 
            return null;
            // Only for editable dataobjects

        try {
            StyledDocument doc = ec.openDocument ();
            JEditorPane ep = getCurrentEditor ();
            if (ep == null) return null;
            synchronized (this) {
                expression = getIdentifier (
                    doc, 
                    ep,
                    NbDocument.findLineOffset (
                        doc,
                        lp.getLine ().getLineNumber ()
                    ) + lp.getColumn ()
                );
                if (expression == null) return null;
            }
            RequestProcessor.getDefault ().post (this);                    
        } catch (IOException e) {
        }
        return null;
    }

    public void run () {
        String expression;
        synchronized (this) {
            expression = this.expression;
        }
        if (expression == null) return;
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        if (currentEngine == null) return;
        JPDADebugger d = (JPDADebugger) currentEngine.lookupFirst 
            (null, JPDADebugger.class);
        if (d == null) return;
        JPDAThread t = d.getCurrentThread();
        if (t == null || !t.isSuspended()) return ;
        String toolTipText = null;
        try {
            Variable v = d.evaluate (expression);
            String type = v.getType ();
            String value = v.getValue ();
            if (v instanceof ObjectVariable)
                try {
                    toolTipText = expression + " = " + 
                        (type.length () == 0 ? 
                            "" : 
                            "(" + type + ") ") +
                        ((ObjectVariable) v).getToStringValue ();
                } catch (InvalidExpressionException ex) {
                    toolTipText = expression + " = " +
                        (type.length () == 0 ? 
                            "" : 
                            "(" + type + ") ") +
                        value;
                }
            else 
                toolTipText = expression + " = " + 
                    (type.length () == 0 ? 
                        "" : 
                        "(" + type + ") ") +
                    value;
        } catch (InvalidExpressionException e) {
            toolTipText = expression + " = >" + e.getMessage () + "<";
        }
        firePropertyChange (PROP_SHORT_DESCRIPTION, null, toolTipText);
    }

    public String getAnnotationType () {
        return null; // Currently return null annotation type
    }

    private static String getIdentifier (
        StyledDocument doc, 
        JEditorPane ep, 
        int offset
    ) {
        String t = null;
        if ( (ep.getSelectionStart () <= offset) &&
             (offset <= ep.getSelectionEnd ())
        )   t = ep.getSelectedText ();
        if (t != null) return t;
        
        int line = NbDocument.findLineNumber (
            doc,
            offset
        );
        int col = NbDocument.findLineColumn (
            doc,
            offset
        );
        try {
            Element lineElem = 
                NbDocument.findLineRootElement (doc).
                getElement (line);

            if (lineElem == null) return null;
            int lineStartOffset = lineElem.getStartOffset ();
            int lineLen = lineElem.getEndOffset() - lineStartOffset;
            t = doc.getText (lineStartOffset, lineLen);
            int identStart = col;
            while (identStart > 0 && 
                (Character.isJavaIdentifierPart (
                    t.charAt (identStart - 1)
                ) ||
                (t.charAt (identStart - 1) == '.'))) {
                identStart--;
            }
            int identEnd = col;
            while (identEnd < lineLen && 
                   Character.isJavaIdentifierPart(t.charAt(identEnd))
            ) {
                identEnd++;
            }

            if (identStart == identEnd) return null;
            return t.substring (identStart, identEnd);
        } catch (BadLocationException e) {
            return null;
        }
    }
    
    /** 
     * Returns current editor component instance.
     *
     * Used in: ToolTipAnnotation
     */
    private static JEditorPane getCurrentEditor () {
        EditorCookie e = getCurrentEditorCookie ();
        if (e == null) return null;
        JEditorPane[] op = e.getOpenedPanes ();
        if ((op == null) || (op.length < 1)) return null;
        return op [0];
    }
    
    /** 
     * Returns current editor component instance.
     *
     * @return current editor component instance
     */
    private static EditorCookie getCurrentEditorCookie () {
        Node[] nodes = TopComponent.getRegistry ().getActivatedNodes ();
        if ( (nodes == null) ||
             (nodes.length != 1) ) return null;
        Node n = nodes [0];
        return (EditorCookie) n.getCookie (
            EditorCookie.class
        );
    }
}

