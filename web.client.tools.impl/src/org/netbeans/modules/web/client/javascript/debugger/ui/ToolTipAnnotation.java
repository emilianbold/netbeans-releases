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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.web.client.javascript.debugger.ui;

import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.javascript.editing.lexer.Call;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSDebugger;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSCallStackFrame;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSObject;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSProperty;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSValue;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.Line.Part;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;


public final class ToolTipAnnotation extends Annotation implements Runnable {
    
    private Part lp;
    private EditorCookie ec;
    
    public String getShortDescription() {
        NbJSDebugger debugger = getDebugger();
        if (debugger == null) {
            return null;
        }
        JSCallStackFrame callStackFrame = debugger.getSelectedFrame();
        if (callStackFrame == null) {
            return null;
        }
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
        NbJSDebugger debugger = getDebugger();
        if (debugger == null) {
            return;
        }
        JSCallStackFrame callStackFrame = debugger.getSelectedFrame();
        if (callStackFrame == null) {
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
        if (expression.trim().length() == 0) { return; }
        JSProperty property = callStackFrame.eval(expression);
        if (property == null ) { return; }
        final JSValue value = property.getValue();
        if (value == null) { return; }
        String stringVal = (value instanceof JSObject ? ((JSObject)value).getClassName() : value.getDisplayValue());
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

        if (doc instanceof BaseDocument) {
            try {
                String expression = Call.getCallExpression((BaseDocument) doc, offset);
                if (expression != null) {
                    return expression;
                };
            } catch (BadLocationException ex) {
            }
        }
        try {
            int[] identifierBlock = Utilities.getIdentifierBlock(ep, offset);
            if (identifierBlock != null) {
                return doc.getText(identifierBlock[0], identifierBlock[1] - identifierBlock[0]);
            }
        } catch (BadLocationException e) {
        }
        return null;
    }

    private static NbJSDebugger getDebugger() {
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        return (currentEngine == null) ? null :
            currentEngine.lookupFirst(null, NbJSDebugger.class);
    }

}

