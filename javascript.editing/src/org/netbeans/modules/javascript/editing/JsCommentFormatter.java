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
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class JsCommentFormatter {

    private static final String PARAM_TAG = "@param"; //NOI18N
    private static final String RETURN_TAG = "@return"; //NOI18N
    private static final String TYPE_TAG = "@type"; //NOI18N
    private static final String THROWS_TAG = "@throws"; //NOI18N
    private static final String DEPRECATED_TAG = "@deprecated"; //NOI18N
    private static final String CODE_TAG = "@code"; //NOI18N
    private static final String EXAMPLE_TAG = "@example"; //NOI18N
    private static final String DESCRIPTION_TAG = "@description"; //NOI18N
    
    private final TokenSequence<? extends JsCommentTokenId> ts;
    private final StringBuilder summary;
    private String returnTag;
    private String returnType;
    private String deprecation;
    private String code;
    private final List<String> params;
    private final List<String> exceptions;
    // flag to see if this is already formatted comment with all html stuff
    private boolean formattedComment;
    private final StringBuilder rest;
    
    @SuppressWarnings("unchecked")
    public JsCommentFormatter(List<String> comments) {
        this.summary = new StringBuilder();
        this.rest = new StringBuilder();
        this.params = new  ArrayList<String>();
        this.exceptions = new  ArrayList<String>();

        StringBuilder sb = new StringBuilder();
        for (String line : comments) {
            sb.append(line);
            sb.append("\n"); // NOI18N
        }
        // Determine whether this is preformatted MDC content or should get
        // normal jsdoc rendering
        boolean haveJsTag = false;
        boolean haveMDC = false;
        for (int i = 0, n = comments.size(); i < n; i++) {
            String s = comments.get(i);
            if (s.indexOf("MDC:Copyrights") != -1) { // NOI18N
                haveMDC = true;
                break; // We know that it must be preformatted
            } else if (s.indexOf('@') != -1) {
                haveJsTag = true;
            }
        }
        if (haveMDC) {
            formattedComment = true;
        } else if (!haveJsTag) {
            formattedComment = true;
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length()-1);
        }
        TokenHierarchy<?> hi = TokenHierarchy.create(sb.toString(), JsCommentTokenId.language());
        this.ts = hi.tokenSequence(JsCommentTokenId.language());
        
        process();
    }

    void setSeqName(String name) {
        
    }

    String toHtml() {
        StringBuilder sb = new StringBuilder();
        
        if (!formattedComment && summary.length() > 0) {
            String summaryText = summary.toString().trim();
            if (summaryText.length() > 0) {
                sb.append("<b>");
                sb.append(NbBundle.getMessage(JsCommentFormatter.class, "Summary"));
                sb.append("</b><blockquote>").append(summaryText).append("</blockquote>"); //NOI18N
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
            sb.append(">");
            sb.append(NbBundle.getMessage(JsCommentFormatter.class, "Deprecated"));
            sb.append("</b>");
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
            sb.append("<b>");
            sb.append(NbBundle.getMessage(JsCommentFormatter.class, "Parameters"));
            sb.append("</b><blockquote>"); //NOI18N
            for (int i = 0, n = params.size(); i < n; i++) {
                if (i > 0) {
                    sb.append("<br><br>"); // NOI18N
                }
                String tag = params.get(i);
                sb.append(tag);
            }
            sb.append("</blockquote>"); // NOI18N
        }

        if (returnTag != null || returnType != null) {
            sb.append("<b>"); // NOI18N
            sb.append(NbBundle.getMessage(JsCommentFormatter.class, "Returns"));
            sb.append("</b><blockquote>"); //NOI18N
            if (returnTag != null) {
                sb.append(returnTag);
                if (returnType != null) {
                    sb.append("<br>"); // NOI18N
                }
            }
            if (returnType != null) {
                sb.append(NbBundle.getMessage(JsCommentFormatter.class, "ReturnType"));
                sb.append(" <i>"); // NOI18N
                sb.append(returnType);
                sb.append("</i>"); // NOI18N
            }
            sb.append("</blockquote>"); //NOI18N
        }
        
        if (exceptions.size() > 0) {
            sb.append("<b>");
            sb.append(NbBundle.getMessage(JsCommentFormatter.class, "Throws"));
            sb.append("</b><blockquote>"); //NOI18N
            for (String tag : exceptions) {
                sb.append(tag);
                sb.append("<br>"); // NOI18N
            }
            sb.append("</blockquote>"); // NOI18N
        }
        
        if (code != null) {
            sb.append("<b>");
            sb.append(NbBundle.getMessage(JsCommentFormatter.class, "CodeExample"));
            sb.append("</b><blockquote>"); //NOI18N
            sb.append("<pre>").append(code).append("</pre></blockquote>"); //NOI18N
        }
        
        if (rest.length() > 0) {
            sb.append("<b>");
            sb.append(NbBundle.getMessage(JsCommentFormatter.class, "Miscellaneous"));
            sb.append("</b><blockquote>"); //NOI18N
            sb.append(rest);
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
        while (ts.moveNext() && ts.token().id() != JsCommentTokenId.COMMENT_TAG) {
            summary.append(ts.token().text());
        }
        ts.movePrevious();
        StringBuilder sb = null;
        while (ts.moveNext()) {
            if (ts.token().id() == JsCommentTokenId.COMMENT_TAG) {
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
            // Try to make the parameter name bold, and the type italic
            String s = tag.substring(PARAM_TAG.length()).trim();
            if (s.length() == 0) {
                return;
            }
            StringBuilder sb = new StringBuilder();
            int index = 0;
            if (s.charAt(0) == '{') {
                // We have a type
                int end = s.indexOf('}');
                if (end != -1) {
                    end++;
                    sb.append("<i>"); // NOI18N
                    sb.append(s.substring(0, end));
                    sb.append("</i>"); // NOI18N
                }
                index = end;
                for (; index < s.length(); index++) {
                    if (!Character.isWhitespace((s.charAt(index)))) {
                        break;
                    }
                }
            }
            if (index < s.length()) {
                int end = index;
                for (; end < s.length(); end++) {
                    if (Character.isWhitespace((s.charAt(end)))) {
                        break;
                    }
                }
                if (end < s.length()) {
                    sb.append(" <b>"); // NOI18N
                    sb.append(s.substring(index, end));
                    sb.append("</b>"); // NOI18N
                    sb.append(s.substring(end));
                    params.add(sb.toString());
                    return;
                }
            }
            params.add(s);
        } else if (tag.startsWith(DESCRIPTION_TAG)) {
            String desc = tag.substring(DESCRIPTION_TAG.length()).trim();
            summary.insert(0, desc);
        } else if (tag.startsWith(RETURN_TAG)) {
            returnTag = tag.substring(RETURN_TAG.length()).trim();
        } else if (tag.startsWith(TYPE_TAG)) {
            returnType = tag.substring(TYPE_TAG.length()).trim();
        } else if (tag.startsWith(THROWS_TAG)) {
            exceptions.add(tag.substring(THROWS_TAG.length()).trim());
        } else if (tag.startsWith(DEPRECATED_TAG)) {
            deprecation = tag.substring(DEPRECATED_TAG.length()).trim();
        } else if (tag.startsWith(CODE_TAG)) {
            code = tag.substring(CODE_TAG.length()).trim();
            code = code.replace("&", "&amp;"); // NOI18N
            code = code.replace("<", "&lt;"); // NOI18N
            code = code.replace(">", "&gt;"); // NOI18N
        } else if (tag.startsWith(EXAMPLE_TAG)) {
            code = tag.substring(EXAMPLE_TAG.length()).trim();
            code = code.replace("&", "&amp;"); // NOI18N
            code = code.replace("<", "&lt;"); // NOI18N
            code = code.replace(">", "&gt;"); // NOI18N
        } else { // NOI18N
            // Store up the rest of the stuff so we don't miss unexpected tags,
            // like @private, @config, etc.
            if (!tag.startsWith("@id ") && !tag.startsWith("@name ") && // NOI18N
                    !tag.startsWith("@attribute") && // NOI18N
                    !tag.startsWith("@compat") && // NOI18N
                    !tag.startsWith("@method") && !tag.startsWith("@property")) { // NOI18N
                rest.append(tag);
                rest.append("<br>"); // NOI18N
            }
        }
    }
}
