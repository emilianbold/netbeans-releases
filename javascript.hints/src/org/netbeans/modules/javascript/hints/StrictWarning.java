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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.hints;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.BadLocationException;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.Error;
import org.netbeans.modules.gsf.api.OffsetRange;
import org.netbeans.modules.javascript.editing.AstUtilities;
import org.netbeans.modules.javascript.editing.lexer.LexUtilities;
import org.netbeans.modules.javascript.hints.spi.Description;
import org.netbeans.modules.javascript.hints.spi.ErrorRule;
import org.netbeans.modules.javascript.hints.spi.Fix;
import org.netbeans.modules.javascript.hints.spi.HintSeverity;
import org.netbeans.modules.javascript.hints.spi.RuleContext;
import org.openide.awt.HtmlBrowser;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Wrap builtin strict-warning errors
 * 
 * @todo Gotta pass name/node as error key such that I can properly fill in {0} etc.
 *   in the error messages! Alternatively, use error description...
 * 
 * @todo Go and write the wiki pages for these errors, at
 *         http://wiki.netbeans.org/JavaScript_-key-
 *   where -key- is the error key, minus the "msg." prefix and minus any .'s.,
 *   e.g.  http://wiki.netbeans.org/JavaScript_varhidesarg 
 * 
 *   Resources: http://www.javascriptkit.com/javatutors/serror.shtml
 *              https://bugzilla.mozilla.org/show_bug.cgi?id=378790
 *
 * @author Tor Norbye
 */
public class StrictWarning implements ErrorRule {

    private String key;
    private HintSeverity defaultSeverity = HintSeverity.WARNING;

    public StrictWarning(String key) {
        this.key = key;
    }

    public Set<String> getCodes() {
        return Collections.singleton(key);
    }

    public void run(RuleContext context, Error error, List<Description> result) {
        CompilationInfo info = context.compilationInfo;
        BaseDocument doc = context.doc;

        OffsetRange range = null;
        
        int astOffset = error.getStartPosition();
        int lexOffset = LexUtilities.getLexerOffset(info, astOffset);

        if ("msg.reserved.keyword".equals(key)) {
            String keyword = (String) error.getParameters()[1];
            range = new OffsetRange(lexOffset-keyword.length(), lexOffset);
        } else if (error.getParameters() != null) {
            Node node = (Node) error.getParameters()[0];
            
            final boolean isInconsistentReturn = "msg.return.inconsistent".equals(key); // NOI18N

            if (isInconsistentReturn) {
                // Find the corresponding return node
                Node c = node;
                while (c != null) {
                    if (c.getType() == Token.RETURN) {
                        node = c;
                        break;
                    }
                    c = c.getParentNode();
                }
            }

            OffsetRange astRange = AstUtilities.getRange(node);
            range = LexUtilities.getLexerOffsets(info, astRange);
        } else {
            int errorOffset = lexOffset;
            try {
                int rowLastNonWhite = Utilities.getRowLastNonWhite(doc, errorOffset);
                if (rowLastNonWhite <= errorOffset) {
                    rowLastNonWhite = Utilities.getRowEnd(doc, errorOffset);
                    if (errorOffset == rowLastNonWhite) {
                        errorOffset = Utilities.getRowFirstNonWhite(doc, errorOffset);
                        rowLastNonWhite = Utilities.getRowLastNonWhite(doc, errorOffset)+1;
                    }
                }
                range = new OffsetRange(errorOffset, rowLastNonWhite);
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
                range = OffsetRange.NONE;
            }
        }

        if (range != OffsetRange.NONE) {
            range = limitErrorToLine(doc, range, lexOffset);

            //Fix fix = new InsertParenFix(info, offset, callNode);
            //List<Fix> fixList = Collections.singletonList(fix);
            List<Fix> fixList = Collections.<Fix>singletonList(new MoreInfoFix(key));

            String message = getDisplayName();
            // I don't have the strings to pass to the error here
            // so for now just use the original error message instead
            if (message.indexOf("{0}") != -1) { // NOI18N
                message = error.getDisplayName();
            }

            Description desc = new Description(this, message, info.getFileObject(), range, fixList, 500);
            result.add(desc);
        }
    }

    static OffsetRange limitErrorToLine(BaseDocument doc, OffsetRange range, int offset) {
        try {
            // Adjust offsets, since (a) Node offsets are still kinda shaky, and
            // (b) for things like block nodes we want to limit the errors to
            // a single line!
            if (range.getStart() == 0) {
                // Probably an incorrectly initialized node AST offset somewhere
                int start = Math.max(range.getStart(), Utilities.getRowStart(doc, offset));
                int end = Math.max(start, Math.min(range.getEnd(), doc.getLength()));
                end = Math.min(end, Utilities.getRowEnd(doc, start));
                if (end == start) {
                    end = Utilities.getRowEnd(doc, end);
                }
                range = new OffsetRange(start, end);
            } else {
                int start = range.getStart();
                int end = Math.min(Utilities.getRowEnd(doc, start), range.getEnd());
                range = new OffsetRange(start, end);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        return range;
    }

    public boolean appliesTo(CompilationInfo compilationInfo) {
        return true;
    }

    public String getDisplayName() {
        return NbBundle.getMessage(StrictWarning.class, key);
    }

    public void setDefaultSeverity(HintSeverity defaultSeverity) {
        this.defaultSeverity = defaultSeverity;
    }

    public boolean showInTasklist() {
        return true;
    }

    public HintSeverity getDefaultSeverity() {
        return defaultSeverity;
    }

    public String getId() {
        return key;
    }

    public String getDescription() {
        return NbBundle.getMessage(StrictWarning.class, key + ".desc"); // NOI18N
    }

    public boolean getDefaultEnabled() {
        return true;
    }

    public JComponent getCustomizer(Preferences node) {
        return null;
    }

    private static class MoreInfoFix implements Fix {

        private String key;

        public MoreInfoFix(String key) {
            this.key = key;
        }

        public String getDescription() {
            return NbBundle.getMessage(StrictWarning.class, "ShowInfo");
        }

        public void implement() throws Exception {
            URL url = new URL("http://wiki.netbeans.org/JavaScript_" + key.replace("msg.", "").replace(".", "")); // NOI18N
            HtmlBrowser.URLDisplayer.getDefault().showURL(url);
        }

        public boolean isSafe() {
            return true;
        }

        public boolean isInteractive() {
            return true;
        }
    }
}
