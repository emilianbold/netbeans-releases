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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.hints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.mozilla.nb.javascript.Node;
import org.mozilla.nb.javascript.Token;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.CompilationInfo;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.RuleContext;
import org.netbeans.modules.javascript.editing.AstElement;
import org.netbeans.modules.javascript.editing.AstUtilities;
import org.netbeans.modules.javascript.editing.FunctionAstElement;
import org.netbeans.modules.javascript.editing.JsParseResult;
import org.netbeans.modules.javascript.editing.JsUtils;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.netbeans.modules.javascript.hints.infrastructure.JsAstRule;
import org.netbeans.modules.javascript.hints.infrastructure.JsRuleContext;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Check for unsupported browser calls.
 * 
 * @author Tor Norbye
 */
public class WrongJsDoc extends JsAstRule {
    public WrongJsDoc() {
    }
    
    public boolean appliesTo(RuleContext context) {
        return JsUtils.isJsFile(context.compilationInfo.getFileObject());
    }

    public Set<Integer> getKinds() {
        return Collections.singleton(Token.FUNCTION);
    }
    
    public void run(JsRuleContext context, List<Hint> result) {
        CompilationInfo info = context.compilationInfo;
        Node node = context.node;
        
        AstElement element = (AstElement)node.element;
        if (element == null) {
            JsParseResult jps = AstUtilities.getParseResult(info);
            if (jps != null) {
                jps.getStructure();
                element = (AstElement) node.element;
                if (element == null) {
                    return;
                }
            }
        }
        
        if (element.getKind() != ElementKind.METHOD && element.getKind() != ElementKind.CONSTRUCTOR) {
            return;
        }
        
        if (!(element instanceof FunctionAstElement)) {
            assert false : element;
            return;
        }
        
        FunctionAstElement func = (FunctionAstElement)element;

        Map<String, String> docProps = element.getDocProps();
        if (docProps == null || docProps.size() == 0) {
            return;
        }
        
        // Make sure we actually have some parameters in the doc props
        boolean found = false;
        for (String key : docProps.keySet()) {
            if (!key.startsWith("@")) {
                found = true;
                break;
            }
        }

        // Don't complain about functions that don't have any parameters
        if (!found) {
            return;
        }
        
        // Make sure every parameter is documented
        List<String> params = func.getParameters();
        List<String> missing = null;
        List<String> extra = null;
        for (String param : params) {
            if (!docProps.containsKey(param)) {
                if (missing == null) {
                    missing = new ArrayList<String>();
                }
                missing.add(param);
            }
        }
        
        // TODO - make sure doc props exist even for items without types!!
        for (String key : docProps.keySet()) {
            if (key.startsWith("@")) {
                continue;
            }
            if (!params.contains(key)) {
                if (extra == null) {
                    extra = new ArrayList<String>();
                }
                extra.add(key);
            }
        }
        
        if (missing != null || extra != null) {
            String label;
            if (missing != null && extra != null) {
                label = NbBundle.getMessage(WrongJsDoc.class, "WrongParamsBoth", missing, extra);
            } else if (missing != null) {
                label = NbBundle.getMessage(WrongJsDoc.class, "WrongParamsMissing", missing);
            } else {
                assert extra != null;
                label = NbBundle.getMessage(WrongJsDoc.class, "WrongParamsExtra", extra);
            }
            
            OffsetRange astRange = AstUtilities.getNameRange(node);
            OffsetRange lexRange = LexUtilities.getLexerOffsets(info, astRange);
            if (lexRange == OffsetRange.NONE) {
                return;
            }
            if (lexRange.getEnd() < context.doc.getLength()) {
                try {
                    int startRowEnd = Utilities.getRowEnd(context.doc, lexRange.getStart());
                    if (startRowEnd < lexRange.getEnd() && startRowEnd > lexRange.getStart()) {
                        lexRange = new OffsetRange(lexRange.getStart(), startRowEnd);  
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            List<HintFix> fixList = Collections.<HintFix>singletonList(new MoreInfoFix("wrongjsdoc")); // NOI18N
            Hint desc = new Hint(this, label, info.getFileObject(), lexRange, fixList, 1450);
            result.add(desc);
        }
    }

    public String getId() {
        return "WrongJsDoc"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(UnsupportedCalls.class, "WrongJsDoc");
    }

    public String getDescription() {
        return NbBundle.getMessage(UnsupportedCalls.class, "WrongJsDocDesc");
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public boolean showInTasklist() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return HintSeverity.WARNING;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }
}
