/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.editor.fortran.indent;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import org.netbeans.modules.cnd.editor.fortran.options.FortranCodeStyle;
import org.netbeans.modules.editor.indent.spi.Context;
import org.netbeans.modules.editor.indent.spi.ExtraLock;
import org.netbeans.modules.editor.indent.spi.ReformatTask;

/**
 *
 * @author Alexander Simon
 */
public class FortranReformatter implements ReformatTask {
    private Context context;
    private Document doc;
    private FortranCodeStyle codeStyle;

    public FortranReformatter(Context context) {
        this.context = context;
        this.doc = context.document();
    }

    public FortranReformatter(Document doc, FortranCodeStyle codeStyle) {
        this.doc = doc;
        this.codeStyle = codeStyle;
    }

    public void reformat() throws BadLocationException {
        if (codeStyle == null){
            codeStyle = FortranCodeStyle.get(doc);
        }
        if (context != null) {
            for (Context.Region region : context.indentRegions()) {
                reformatImpl(region);
            }
        } else {
            int endOffset = doc.getLength();
            TokenHierarchy hierarchy = TokenHierarchy.get(doc);
            if (hierarchy == null) {
                return;
            }
            reformatImpl(hierarchy, 0, endOffset);
        }
    }

    private void reformatImpl(Context.Region region) throws BadLocationException {
        int startOffset = region.getStartOffset();
        int endOffset = region.getEndOffset();
        if ("text/x-fortran".equals(context.mimePath())) { //NOI18N
            reformatLanguage(FortranTokenId.languageFortran(), startOffset, endOffset);
        }
    }

    private void reformatLanguage(Language<FortranTokenId> language, int startOffset, int endOffset) throws BadLocationException {
        TokenHierarchy hierarchy = TokenHierarchy.create(doc.getText(0, doc.getLength()), language);
        if (hierarchy == null) {
            return;
        }
        reformatImpl(hierarchy, startOffset, endOffset);
    }


    @SuppressWarnings("unchecked")
    private void reformatImpl(TokenHierarchy hierarchy, int startOffset, int endOffset) throws BadLocationException {
        TokenSequence<?> ts = hierarchy.tokenSequence();
        ts.move(startOffset);
        if (ts.moveNext() && ts.token().id() != FortranTokenId.NEW_LINE){
            while (ts.movePrevious()){
                startOffset = ts.offset();
                if (ts.token().id() != FortranTokenId.NEW_LINE) {
                    break;
                }
            }
        }
        while (ts != null && (startOffset == 0 || ts.moveNext())) {
            ts.move(startOffset);
            if (ts.language() == FortranTokenId.languageFortran()) {
                reformatImpl((TokenSequence<FortranTokenId>) ts, startOffset, endOffset);
                return;
            }
            if (!ts.moveNext() && !ts.movePrevious()) {
                return;
            }
            ts = ts.embedded();
        }
    }

    private void reformatImpl(TokenSequence<FortranTokenId> ts, int startOffset, int endOffset) throws BadLocationException {
        throw new UnsupportedOperationException("Not supported yet."); // NOI18N
    }

    public ExtraLock reformatLock() {
        return null;
    }

    public static class Factory implements ReformatTask.Factory {
        public ReformatTask createTask(Context context) {
            return new FortranReformatter(context);
        }
    }
}
