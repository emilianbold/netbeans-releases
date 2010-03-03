/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.refactoring;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.Document;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.html.editor.api.Utils;
import org.netbeans.modules.html.editor.refactoring.api.ExtractInlinedStyleRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.web.common.api.WebUtils;

/**
 *
 * @author marekfukala
 */
public class ExtractInlinedStyleRefactoringPlugin implements RefactoringPlugin {

    private ExtractInlinedStyleRefactoring refactoring;

    public ExtractInlinedStyleRefactoringPlugin(ExtractInlinedStyleRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    @Override
    public Problem preCheck() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Problem checkParameters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Problem fastCheckParameters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cancelRequest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElements) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void extractInlinedStyle(final Document doc, final int from, final int to) {
        final AtomicReference<ActionContext> ref = new AtomicReference<ActionContext>();
        doc.render(new Runnable() {

            @Override
            public void run() {
                ActionContext context = new ActionContext();
                context.infos = findInlinedStyles(doc, from, to);
                ref.set(context);
            }
        });

        ActionContext context = ref.get();
        if (context == null) {
            return;
        }
        if (context.infos.isEmpty()) {
            return; //nothing to refactor
        }

        if (context.firstEmbeddedStyleRange == null) {
            //no embedded css in the file
            //what to do?
            //1. check existing links to css's => open dialog so user may choose which one to use
            //2. if there's none open a dialog where user can choose if he want's to generate embedded
            //   css section or create a new file or link an existing one an use it.
            //3. merge all above into one dialog!
            //
            //TODO implement
        }

        //ok there's an embedded css section we may use to generate the id selectors





    }

    private List<InlinedStyleInfo> findInlinedStyles(Document doc, int from, int to) {
        List<InlinedStyleInfo> found = new LinkedList<InlinedStyleInfo>();
        TokenSequence<HTMLTokenId> ts = Utils.getJoinedHtmlSequence(doc, from);
        //the joined ts is moved to the from offset and a token already selected (moveNext/Previous() == true)
        //seek for all tag's attributes with css embedding representing an inlined style
        String tag = null;
        String attr = null;
        String tagsClass = null;
        OffsetRange range = null;
        do {
            Token<HTMLTokenId> t = ts.token();
            if (t.id() == HTMLTokenId.TAG_OPEN) {
                tag = t.text().toString();
                attr = tagsClass = null;
                range = null;
            } else if (t.id() == HTMLTokenId.TAG_CLOSE_SYMBOL) {
                //closing tag, produce the info
                if (tag != null && range != null) {
                    //some inlined code found
                    found.add(new InlinedStyleInfo(tag, tagsClass, attr, range));
                }
            } else if (t.id() == HTMLTokenId.ARGUMENT) {
                attr = t.text().toString();
            } else if (t.id() == HTMLTokenId.VALUE_CSS) {
                //check if this is an inlined code, not class or id representation
                String csstype = (String) t.getProperty(HTMLTokenId.VALUE_CSS_TOKEN_TYPE_PROPERTY);
                if (csstype == null) {
                    //inlined code
                    int diff = WebUtils.isValueQuoted(t.text()) ? 1 : 0;
                    range = new OffsetRange(ts.offset() + diff, ts.offset() + t.length() - diff);
                }
            } else if (t.id() == HTMLTokenId.VALUE) {
                //TODO use TagMetadata for getting the info a the attribute represents a css or not
                if ("class".equals(attr)) { //NOI18N
                    tagsClass = t.text().toString();
                }
            }

        } while (ts.moveNext() && ts.offset() <= to);

        return found;
    }

    private static class ActionContext {

        List<InlinedStyleInfo> infos;
        OffsetRange firstEmbeddedStyleRange;
    }

    private static class InlinedStyleInfo {

        private String tag, tagsClass, attr;
        private OffsetRange range;

        public InlinedStyleInfo(String tag, String tagsClass, String attr, OffsetRange range) {
            this.tag = tag;
            this.tagsClass = tagsClass;
            this.attr = attr;
            this.range = range;
        }

        public String getAttr() {
            return attr;
        }

        public OffsetRange getRange() {
            return range;
        }

        public String getTag() {
            return tag;
        }

        public String getTagsClass() {
            return tagsClass;
        }
    }
}
