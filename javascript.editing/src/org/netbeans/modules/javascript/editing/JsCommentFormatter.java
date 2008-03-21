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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.javascript.editing;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript.editing.lexer.JsCommentTokenId;

/**
 *
 * @author Martin Adamek
 */
public class JsCommentFormatter {

    private static final String PARAM_TAG = "@param"; //NOI18N
    private static final String RETURN_TAG = "@return"; //NOI18N
    private static final String THROWS_TAG = "@throws"; //NOI18N
    private static final String DEPRECATED_TAG = "@deprecated"; //NOI18N
    
    private final TokenSequence<? extends JsCommentTokenId> ts;
    private final StringBuilder summary;
    private String returnTag;
    private String deprecation;
    private final List<String> params;
    private final List<String> exceptions;
    // flag to see if this is already formatted comment with all html stuff
    private boolean formattedComment;
    
    @SuppressWarnings("unchecked")
    public JsCommentFormatter(List<String> comments) {
        this.summary = new StringBuilder();
        this.params = new  ArrayList<String>();
        this.exceptions = new  ArrayList<String>();

        StringBuilder sb = new StringBuilder();
        for (String line : comments) {
            sb.append(line);
            sb.append("\n"); // NOI18N
        }
        sb.deleteCharAt(sb.length() - 1);
        TokenHierarchy<?> hi = TokenHierarchy.create(sb.toString(), JsCommentTokenId.language());
        this.ts = (TokenSequence<JsCommentTokenId>) hi.tokenSequence();
        
        process();
    }

    void setSeqName(String name) {
        
    }

    public void setFormattedComment(boolean formattedComment) {
        this.formattedComment = formattedComment;
    }

    String toHtml() {
        StringBuilder sb = new StringBuilder();
        
        if (!formattedComment && summary.length() > 0) {
            String summaryText = summary.toString().trim();
            if (summaryText.length() > 0) {
                sb.append("<b>Summary</b><blockquote>").append(summaryText).append("</blockquote>"); //NOI18N
            }
        } else {
            sb.append(summary);
        }

        if (deprecation != null) {
            boolean hasDescription = deprecation.trim().length() > 0;
            sb.append("<b");
            if (!hasDescription) {
                sb.append(" style=\"background:#ffcccc\"");
            }
            sb.append(">Deprecated</b>");
            sb.append("<blockquote");
            if (hasDescription) {
                sb.append(" style=\"background:#ffcccc\">");
                sb.append(deprecation);
            } else {
                sb.append(">");
            }
            sb.append("</blockquote>"); //NOI18N
        }
        
        if (params.size() > 0) {
            sb.append("<b>Parameters</b><blockquote>"); //NOI18N
            for (String tag : params) {
                sb.append(tag);
                sb.append("<br>"); // NOI18N
            }
            sb.append("</blockquote>"); // NOI18N
        }

        if (returnTag != null) {
            sb.append("<b>Returns</b><blockquote>").append(returnTag).append("</blockquote>"); //NOI18N
        }
        
        if (exceptions.size() > 0) {
            sb.append("<b>Throws</b><blockquote>"); //NOI18N
            for (String tag : exceptions) {
                sb.append(tag);
                sb.append("<br>"); // NOI18N
            }
            sb.append("</blockquote>"); // NOI18N
        }

        return sb.toString();
    }

    String getSummary() {
        return summary.toString().trim();
    }
    
    List<String> getParams() {
        return params;
    }
    
    List<String> getExceptions() {
        return exceptions;
    }
    
    String getReturn() {
        return returnTag;
    }
    
    private void process() {
        while (ts.moveNext() && ts.token().id() != JsCommentTokenId.TAG) {
            summary.append(ts.token().text());
        }
        ts.movePrevious();
        StringBuilder sb = null;
        while (ts.moveNext()) {
            if (ts.token().id() == JsCommentTokenId.TAG) {
                if (sb != null) {
                    processTag(sb.toString().trim());
                }
                sb = new StringBuilder();
            }
            if (sb != null) { // we have some tags
                sb.append(ts.token().text());
            }
        }
        if (sb != null) {
            processTag(sb.toString().trim());
        }
    }

    private void processTag(String tag) {
        if (tag.startsWith(PARAM_TAG)) {
            params.add(tag.substring(PARAM_TAG.length()).trim());
        } else if (tag.startsWith(RETURN_TAG)) {
            returnTag = tag.substring(RETURN_TAG.length()).trim();
        } else if (tag.startsWith(THROWS_TAG)) {
            exceptions.add(tag.substring(THROWS_TAG.length()).trim());
        } else if (tag.startsWith(DEPRECATED_TAG)) {
            deprecation = tag.substring(DEPRECATED_TAG.length()).trim();
        }
    }
}
