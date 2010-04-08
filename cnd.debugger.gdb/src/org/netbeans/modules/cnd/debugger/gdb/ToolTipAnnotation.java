/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.debugger.gdb;

import java.io.IOException;
import javax.swing.JEditorPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.modules.cnd.debugger.gdb.models.GdbWatchVariable;
import org.netbeans.modules.cnd.debugger.gdb.models.ValuePresenter;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.DataEditorSupport;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.text.Line.Part;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;

/*
 * ToolTipAnnotation.java
 * This class implements "Balloon Evaluation" feature.
 *
 * @author Nik Molchanov (copied from JPDA implementation)
 */
public class ToolTipAnnotation extends Annotation implements Runnable {
    
    private Part lp;
    private EditorCookie ec;
    
    public String getShortDescription() {
        GdbDebugger debugger = GdbDebugger.getGdbDebugger();
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
        EditorCookie ec = dob.getCookie(EditorCookie.class);
        if (ec == null) {
            return null;
        }
        
        this.lp = lp;
        this.ec = ec;
        GdbUtils.getGdbRequestProcessor().post(this);
        return null;
    }
        
    public void run() {
        if (lp == null || ec == null) {
            return;
        }
        StyledDocument doc;
        try {
            doc = ec.openDocument();
        } catch (IOException ex) {
            return;
        }                    
        JEditorPane ep = EditorContextDispatcher.getDefault().getCurrentEditor();
        if (ep == null) {
            return;
        }

        final int offset = NbDocument.findLineOffset(doc, lp.getLine().getLineNumber()) + lp.getColumn();
        
        String expression = getIdentifier(doc, ep, offset);
        
        if (expression == null) {
            return;
        }
        
        GdbDebugger debugger = GdbDebugger.getGdbDebugger();
        if (debugger == null) {
            return;
        }
        
        if (!debugger.isStopped()) {
            return;
        }

        expression = GdbWatchVariable.expandMacro(debugger, expression);

        // Do not evaluate empty strings, see IZ 166207
        if (expression == null || expression.length() == 0) {
            return;
        }

        String type = debugger.requestWhatis(expression);
        String value = debugger.evaluate(expression);

        String res = ValuePresenter.getValue(type, value);
        
        firePropertyChange(PROP_SHORT_DESCRIPTION, null, res);
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

            // Scan for :: -> . and symbols
            while (identStart > 0) {
                char token = t.charAt(identStart - 1);
                if (identStart > 1) {
                    char prevToken = t.charAt(identStart - 2);
                    if ((prevToken == '-' && token == '>') ||
                        (prevToken == ':' && token == ':')) {
                        identStart -= 2;
                        continue;
                    }
                }
                if (Character.isJavaIdentifierPart(token) || token == '.') {
                    identStart--;
                    continue;
                }
                break;
            }

            int identEnd = col;
            while (identEnd < lineLen && Character.isJavaIdentifierPart(t.charAt(identEnd))) {
                identEnd++;
            }

            if (identStart == identEnd) {
                return null;
            }
            
            String ident = t.substring (identStart, identEnd);
            if (CndLexerUtilities.isKeyword(ident)) {
                // keyword => Do not show anything
                return null;
            }
            return ident;
        } catch (BadLocationException e) {
            return null;
        }
    }
    
}

