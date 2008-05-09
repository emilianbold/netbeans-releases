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
package org.netbeans.modules.php.editor.completion;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import javax.swing.text.Document;
import org.netbeans.modules.gsf.api.Element;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.php.editor.TokenUtils;
import org.netbeans.modules.php.model.FormalParameter;
import org.netbeans.modules.php.model.FunctionDeclaration;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.SourceElement;

/**
 * This is function completion proposal based on user defined PHP function. 
 * 
 * @author Victor G. Vasilyev
 *
 */
class UserDefinedMethodItem extends CompletionItem {

    private static final String COMMA = ", ";            // NOI18N
    private static final String RIGHT_PARENS = ")";             // NOI18N
    private static final String LEFT_PARENS = "(";             // NOI18N

    UserDefinedMethodItem(FunctionDeclaration func, int caretOffset,
            HtmlFormatter formatter) {
        super(caretOffset, formatter);
        myFunction = func;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getIcon()
     */
    public ImageIcon getIcon() {
        // TODO An Icon for User Defined Method is required.
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getInsertParams()
     */
    @Override
    public List<String> getInsertParams() {
        List<String> params = new ArrayList<String>();
        List<FormalParameter> fpl = getFormalParameterList();
        for (FormalParameter fp : fpl) {
            StringBuilder sb = new StringBuilder();
            sb.append(fp.getName());
            params.add(sb.toString());
        }
        return params;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getInsertPrefix()
     */
    public String getInsertPrefix() {
        return myFunction.getName();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getKind()
     */
    public ElementKind getKind() {
        return ElementKind.METHOD;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getLhsHtml()
     */
    public String getLhsHtml() {
        HtmlFormatter formatter = getFormatter();
        formatter.reset();
        formatter.name(getKind(), true);
        formatter.appendText(myFunction.getName());
        formatter.name(getKind(), false);
        formatter.appendText(LEFT_PARENS);
        List<FormalParameter> fpl = getFormalParameterList();
        Iterator<FormalParameter> it = fpl.iterator();
        while (it.hasNext()) {
            FormalParameter fp = it.next();
            formatter.parameters(true);
            formatter.appendText(fp.getText());
            formatter.parameters(false);
            if (it.hasNext()) {
                formatter.appendText(COMMA);
            }
        }
        formatter.appendText(RIGHT_PARENS);
        return formatter.getText();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getModifiers()
     */
    public Set<Modifier> getModifiers() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getName()
     */
    public String getName() {
        return myFunction.getName();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getRhsHtml()
     */
    public String getRhsHtml() {
        // TODO If it is possible then a text explained the return type 
        // should be returned.
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#isSmart()
     */
    public boolean isSmart() {
        return true;
    }

    @Override
    public String getCustomInsertTemplate() {
        List<String> params = getInsertParams();
        String[] delimiters = getParamListDelimiters();
        assert delimiters.length == 2;
        int paramCount = params.size();
 
        StringBuilder sb = new StringBuilder();
        sb.append(getInsertPrefix());
        sb.append(delimiters[0]);
        int id = 1;
        for (int i = 0; i < paramCount; i++) {
            String paramDesc = params.get(i);
            sb.append("${"); //NOI18N
            sb.append("php-cc-"); // NOI18N
            sb.append(Integer.toString(id++));
            sb.append(" default=\""); // NOI18N
            sb.append(paramDesc);
            sb.append("\""); // NOI18N
            sb.append("}"); //NOI18N
            if (i < paramCount - 1) {
                sb.append(", "); //NOI18N
            }
        }
        sb.append(delimiters[1]);
        sb.append("${cursor}"); // NOI18N
        return sb.toString();
    }

    private List<FormalParameter> getFormalParameterList() {
        PhpModel model = myFunction.getModel();
        model.sync();
        model.readLock();
        try {
            return myFunction.getParamaterList().getParameters();
        } finally {
            model.readUnlock();
        }

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.gsf.api.CompletionProposal#getElement()
     */
    @Override
    public Element getElement() {
        return new DocumentableElement() {

                    public String getIn() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    public ElementKind getKind() {
                        return ElementKind.METHOD;
                    }

                    public Set<Modifier> getModifiers() {
                        // TODO Auto-generated method stub
                        return null;
                    }

                    public String getName() {
                        return myFunction.getName();
                    }

                    public String getDocumentation() {
                        return getDocumentationText(getDocCommentText());
                    }
                };
    }

    protected String getDocCommentText() {
        int offset = myFunction.getOffset();
        return TokenUtils.getDocComentText(getDocument(), offset);
    }

    protected String getDocumentationText(String docCommentText) {
        StringBuilder sb = new StringBuilder();
        // TODO: a name of the file where the method is defined
        // TODO: a link that opens the file in the editor 
        sb.append("<h1>");
        sb.append(getName());
        sb.append("</h1>");
        if (docCommentText != null) {
            sb.append(getNormalizedText(docCommentText));
        } else {
            sb.append("<h2>");
            sb.append("Description");
            sb.append("</h2>");
            // TODO return type
            sb.append("<b>");
            sb.append(getName());
            sb.append("</b>");
            sb.append(LEFT_PARENS);
            List<FormalParameter> fpl = getFormalParameterList();
            Iterator<FormalParameter> it = fpl.iterator();
            while (it.hasNext()) {
                FormalParameter fp = it.next();
                sb.append(fp.getText());
                if (it.hasNext()) {
                    sb.append(COMMA);
                }
            }
            sb.append(RIGHT_PARENS);

            sb.append("<DIV CLASS='warning'><P ></P >");
            sb.append("<TABLE CLASS='warning' BORDER='1' WIDTH='100%'>");
            sb.append("<TR ><TD ALIGN='CENTER'><B >Warning</B ></TD ></TR >");
            sb.append("<TR ><TD ALIGN='LEFT'><P >");
            sb.append("This function is currently not documented");
            sb.append("."); // sb.append("; only the argument list is available.");
            sb.append("</P ></TD ></TR >");
            sb.append("</TABLE >");
            sb.append("</DIV >");
        }
        return sb.toString();
    }

    public static String getNormalizedText(String docCommentText) {
        if (docCommentText == null) {
            return null;
        }
        String text = docCommentText;
        int beginIndex = 0;
        int endIndex = text.length();
        if (text.startsWith(BLOCK_COMMENT_START)) {
            beginIndex = BLOCK_COMMENT_START.length();
        }
        if (text.endsWith(BLOCK_COMMENT_END)) {
            endIndex = text.length() - BLOCK_COMMENT_END.length();
        }
        text = text.substring(beginIndex, endIndex).trim();
        text = text.replaceAll("[ \t\n\f\r]*\\*", "\n");
        return text;
    }
    public static final String BLOCK_COMMENT_START = "/*";
    public static final String BLOCK_COMMENT_END = "*/";

    private Document getDocument() {
        return myFunction.getModel().getDocument();
    }

    private boolean isCaretInside(int caretOffset, SourceElement e) {
        return e.getOffset() <= caretOffset && e.getEndOffset() >= caretOffset;
    }
    private FunctionDeclaration myFunction;
}
