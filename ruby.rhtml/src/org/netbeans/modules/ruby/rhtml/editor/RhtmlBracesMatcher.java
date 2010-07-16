/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.ruby.rhtml.editor;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Segment;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;
import org.netbeans.spi.editor.bracesmatching.support.BracesMatcherSupport;

/**
 * BracesMatcher for RHTML. The main purpose of this class is to handle '=>' in hash
 * assigments; for other cases this just delegates to the default matcher.
 *
 * @author Erno Mononen
 */
public class RhtmlBracesMatcher implements BracesMatcher {

    private final MatcherContext context;
    private final BracesMatcher delegate;


    RhtmlBracesMatcher(MatcherContext context) {
        this.context = context;
        this.delegate = BracesMatcherSupport.defaultMatcher(context, -1, -1);
    }

    public int [] findOrigin() throws InterruptedException, BadLocationException {
        return delegate.findOrigin();
    }

    public int [] findMatches() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            BaseDocument doc = (BaseDocument) context.getDocument();
            int offset = context.getSearchOffset();
            char[] origin = doc.getChars(offset, 1);
            
            if (isRhtmlStartTag(doc, offset, origin)) {
                int limit = doc.getText().length();
                int matching = matchChar(doc, offset, limit, '>');
                while (matching != -1 && doc.getChars(matching - 1, 1)[0] == '=') {
                    matching = matchChar(doc, matching + 1, limit,  '>');
                }
                return new int[] {matching, matching + 1};

            } else if (isRhtmlEndTag(doc, offset, origin)) {
                int limit = 0;
                int matching = matchChar(doc, offset, limit, '<');
                if (matching != -1) {
                    return new int[] {matching, matching + 1};
                }
            }

        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
        return delegate.findMatches();
    }


    private boolean isRhtmlEndTag(BaseDocument doc, int offset, char[] origin) throws BadLocationException {
        if (offset == 0) {
            return false;
        }
        int length = doc.getText().length();
        if (length <= 1) {
            return false;
        }
        if (origin[0] == '%' && length >= offset + 1 && doc.getChars(offset + 1, 1)[0] == '>') {
            return true;
        }
        if (origin[0] == '>') {
            return doc.getChars(offset - 1, 1)[0] != '=';
        }
        if (length >= 3 && offset - 2 > 0) {
            char[] chars = doc.getChars(offset - 2, 3);
            if (chars[1] == '>') {
                return chars[0] == '%';
            }
        }
        return false;
    }

    private boolean isRhtmlStartTag(BaseDocument doc, int offset, char[] origin) throws BadLocationException {
        int length = doc.getText().length();
        if (length < offset + 1) {
            return false;
        }
        if (origin[0] == '<' && length >= offset + 1 && doc.getChars(offset + 1, 1)[0] == '%') {
            return true;
        }
        if (origin[0] == '%' && offset - 1 > 0) {
            return doc.getChars(offset - 1, 1)[0] == '<';
        }
        return false;
    }

    // based on BracesMatcherSupport#matchChar but doesn't track additional pairs of orig - matching chars.
    private static int matchChar(Document document, int offset, int limit, char matching) throws BadLocationException {
        boolean backward = limit < offset;
        int lookahead = backward ? offset - limit : limit - offset;
        if (backward) {
            // check the character at the left from the caret
            Segment text = new Segment();
            document.getText(offset - lookahead, lookahead, text);

            for (int i = lookahead - 1; i >= 0; i--) {
                if (MatcherContext.isTaskCanceled()) {
                    return -1;
                }
                if (matching == text.array[text.offset + i]) {
                    return offset - (lookahead - i);
                }
            }
        } else {
            // check the character at the right from the caret
            Segment text = new Segment();
            document.getText(offset, lookahead, text);

            for (int i = 0; i < lookahead; i++) {
                if (MatcherContext.isTaskCanceled()) {
                    return -1;
                }
                if (matching == text.array[text.offset + i]) {
                    return offset + i;
                }
            }
        }

        return -1;
    }

}
