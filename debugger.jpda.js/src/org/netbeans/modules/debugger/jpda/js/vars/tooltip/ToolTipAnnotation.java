/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.js.vars.tooltip;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.js.JSUtils;
import org.netbeans.modules.debugger.jpda.js.vars.DebuggerSupport;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Martin
 */
public class ToolTipAnnotation extends Annotation implements Runnable {
    
    private static final Set<String> JS_KEYWORDS = new HashSet<>(Arrays.asList(new String[] {
        "break",    "case",     "catch",    "class",    "continue",
        "debugger", "default",  "delete",   "do",       "else",
        "enum",     "export",   "extends",  "finally",  "for",
        "function", "if",       "implements","import",  "in",
        "instanceof","interface","let",     "new",      "return",
        "package",  "private",  "protected","public",   "static",
        "super",    "switch",   /*"this",*/ "throw",    "try",
        "typeof",   "var",      "void",     "while",    "with",
        "yield",
    }));
    private static final int MAX_TOOLTIP_TEXT = 100000;

    private Line.Part lp;
    private EditorCookie ec;

    @Override
    public String getShortDescription () {
        Session session = DebuggerManager.getDebuggerManager ().getCurrentSession();
        if (session == null) {
            return null;
        }
        DebuggerEngine engine = session.getCurrentEngine();
        if (engine != session.getEngineForLanguage(JSUtils.JS_STRATUM)) {
            return null;
        }
        JPDADebugger d = engine.lookupFirst(null, JPDADebugger.class);
        if (d == null) {
            return null;
        }

        Line.Part lp = (Line.Part) getAttachedAnnotatable();
        if (lp == null) {
            return null;
        }
        Line line = lp.getLine ();
        DataObject dob = DataEditorSupport.findDataObject (line);
        if (dob == null) {
            return null;
        }
        EditorCookie ec = dob.getLookup().lookup(EditorCookie.class);
        if (ec == null) {
            return null;
            // Only for editable dataobjects
        }

        this.lp = lp;
        this.ec = ec;
        RequestProcessor rp = engine.lookupFirst(null, RequestProcessor.class);
        if (rp == null) {
            // Debugger is likely finishing...
            rp = RequestProcessor.getDefault();
        }
        rp.post (this);
        return null;
    }

    @Override
    public String getAnnotationType() {
        return null;
    }

    @Override
    public void run() {
        ObjectVariable tooltipVariable = null;
        if (lp == null || ec == null) {
            return ;
        }
        StyledDocument doc;
        try {
            doc = ec.openDocument();
        } catch (IOException ex) {
            return ;
        }
        final JEditorPane ep = EditorContextDispatcher.getDefault().getMostRecentEditor ();
        if (ep == null || ep.getDocument() != doc) {
            return ;
        }
        Session session = DebuggerManager.getDebuggerManager ().getCurrentSession();
        if (session == null) {
            return ;
        }
        DebuggerEngine engine = session.getCurrentEngine();
        if (engine != session.getEngineForLanguage(JSUtils.JS_STRATUM)) {
            return ;
        }
        JPDADebugger d = engine.lookupFirst(null, JPDADebugger.class);
        if (d == null) {
            return ;
        }
        CallStackFrame frame = d.getCurrentCallStackFrame();
        if (frame == null) {
            return ;
        }

        int offset;
        boolean[] isFunctionPtr = new boolean[] { false };
        final String expression = getIdentifier (
            d,
            doc,
            ep,
            offset = NbDocument.findLineOffset (
                doc,
                lp.getLine ().getLineNumber ()
            ) + lp.getColumn (),
            isFunctionPtr
        );
        if (expression == null) {
            return;
        }

        String toolTipText;
        if (isFunctionPtr[0]) {
            //return ; // We do not call functions
        }
        try {
            Variable result = DebuggerSupport.evaluate(d, frame, expression);
            if (result == null) {
                return ; // Something went wrong...
            }
            toolTipText = expression + " = " + result.getValue();
        } catch (InvalidExpressionException ex) {
            toolTipText = expression + " = >" + ex.getMessage () + "<";
        }
        toolTipText = truncateLongText(toolTipText);
        
        firePropertyChange (PROP_SHORT_DESCRIPTION, null, toolTipText);
    }
    
    private static String getIdentifier (
        JPDADebugger debugger,
        StyledDocument doc,
        JEditorPane ep,
        int offset,
        boolean[] isFunctionPtr
    ) {
        // do always evaluation if the tooltip is invoked on a text selection
        String t = null;
        if ( (ep.getSelectionStart () <= offset) &&
             (offset <= ep.getSelectionEnd ())
        ) {
            t = ep.getSelectedText ();
        }
        if (t != null) {
            return t;
        }
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

            if (lineElem == null) {
                return null;
            }
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

            if (identStart == identEnd) {
                return null;
            }

            String ident = t.substring (identStart, identEnd);
            if (JS_KEYWORDS.contains(ident)) {
                // Java keyword => Do not show anything
                return null;
            }

            while (identEnd < lineLen &&
                   Character.isWhitespace(t.charAt(identEnd))
            ) {
                identEnd++;
            }
            if (identEnd < lineLen && t.charAt(identEnd) == '(') {
                // We're at a function call
                isFunctionPtr[0] = true;
            }
            return ident;
        } catch (BadLocationException e) {
            return null;
        }
    }

    private static String truncateLongText(String text) {
        if (text.length() > MAX_TOOLTIP_TEXT) {
            text = text.substring(0, MAX_TOOLTIP_TEXT) + "...";
        }
        return text;
    }

}
