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

package org.netbeans.modules.web.debug;

import java.io.*;
import javax.swing.JEditorPane;
import javax.swing.text.*;

import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.text.*;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

import org.netbeans.modules.web.debug.util.Utils;
import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;
import org.openide.loaders.DataObject;

public class JspToolTipAnnotation extends Annotation implements Runnable {
    
    private String toolTipText = null;

    private StyledDocument doc;

    public String getShortDescription() {
        Utils.getEM().log("JspTooltip: getShortDescription");
        
        toolTipText = null;
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        if (currentEngine == null) return null;
        JPDADebugger d = (JPDADebugger) currentEngine.lookupFirst (null, JPDADebugger.class);
        if (d == null) return null;

        Line.Part lp = (Line.Part) getAttachedAnnotatable();
        if (lp != null) {
            Line line = lp.getLine ();
            DataObject dob = DataEditorSupport.findDataObject(line);
            EditorCookie ec = (EditorCookie) dob.getCookie(EditorCookie.class);

            if (ec != null) { // Only for editable dataobjects
                try {
                    doc = ec.openDocument ();                    
                    RequestProcessor.getDefault().post(this);                    
                } catch (IOException e) {
                }
            }
        }
        return toolTipText;
    }

    public void run () {

        Utils.getEM().log("JspTooltip: run");

        //1) get tooltip text
        Line.Part lp = (Line.Part)getAttachedAnnotatable();
        JEditorPane ep = Utils.getCurrentEditor();
        String textForTooltip = "";
        
        if ((lp == null) || (ep == null)) {
            return;
        }
        
        //first try EL
        String text = Utils.getELIdentifier(doc, ep,NbDocument.findLineOffset(doc, lp.getLine().getLineNumber()) + lp.getColumn());
        Utils.getEM().log("JspTooltip: ELIdentifier = " + text);

        boolean isScriptlet = Utils.isScriptlet(
            doc, ep, NbDocument.findLineOffset(doc, lp.getLine().getLineNumber()) + lp.getColumn()
        );
        Utils.getEM().log("isScriptlet: " + isScriptlet);
        
        //if not, try Java
        if ((text == null) && (isScriptlet)) {
            text = Utils.getJavaIdentifier(
                doc, ep, NbDocument.findLineOffset(doc, lp.getLine().getLineNumber()) + lp.getColumn()
            );
            textForTooltip = text;
            Utils.getEM().log("JspTooltip: javaIdentifier = " + text);
            if (text == null) {
                return;
            }
        } else {
            if (text == null) {
                return;
            }
            textForTooltip = text;
            String textEscaped = org.openide.util.Utilities.replaceString(text, "\"", "\\\"");
            text = "pageContext.getExpressionEvaluator().evaluate(\"" + textEscaped +
                                "\", java.lang.String.class, (javax.servlet.jsp.PageContext)pageContext, null)";
        }
        
        Utils.getEM().log("JspTooltip: fullWatch = " + text);
        
        //3) obtain text representation of value of watch
        String old = toolTipText;
        toolTipText = null;
        
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (currentEngine == null) return;
        JPDADebugger d = (JPDADebugger) currentEngine.lookupFirst (null, JPDADebugger.class);
        if (d == null) return;
        
        try {
            Variable v = d.evaluate(text);
            if (v instanceof ObjectVariable) {
                toolTipText = textForTooltip + " = (" + v.getType() + ")" + ((ObjectVariable)v).getToStringValue();
            } else {
                toolTipText = textForTooltip + " = (" + v.getType() + ")" + v.getValue();
            }
        } catch (InvalidExpressionException e) {
            toolTipText = text + " = >" + e.getMessage() + "<";
        }
        Utils.getEM().log("JspTooltip: " + toolTipText);
        firePropertyChange (PROP_SHORT_DESCRIPTION, old, toolTipText);       
    }

    public String getAnnotationType () {
        return null;
    }
    
}
