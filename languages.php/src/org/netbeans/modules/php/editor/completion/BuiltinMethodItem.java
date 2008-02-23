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

import java.util.StringTokenizer;
import javax.swing.ImageIcon;

import org.netbeans.modules.gsf.api.Element;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.php.doc.FunctionDoc;

/**
 * This is method completion proposal based on existed PHP function 
 * ( from extensions or existed by default ). They are described
 * in PHP manual.
 * 
 * It differs from user defined function completion proposal. 
 * @author ads
 *
 */
class BuiltinMethodItem extends CompletionItem {

    private static final String COMMA = ", ";            // NOI18N
    private static final String RIGHT_PARENS = ")";             // NOI18N
    private static final String LEFT_PARENS = "(";             // NOI18N
    private static final String OPTIONAL_PARAM_PREFIX = "[";      // NOI18N

    BuiltinMethodItem(FunctionDoc doc, int caretOffset,
            HtmlFormatter formatter) {
        super(caretOffset, formatter);
        myFunction = doc;
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
        List<String> args = new ArrayList<String>();
        for (String arg : myFunction.getArguments()) {
            if (arg.startsWith(OPTIONAL_PARAM_PREFIX)) {
                args.add(arg);
            } else {
                StringTokenizer tokenizer = new StringTokenizer(arg);
                if(tokenizer.hasMoreTokens()) {
                    String paramType = tokenizer.nextToken();
                }
                if(tokenizer.hasMoreTokens()) {
                    String paramName = tokenizer.nextToken();
                    String insertArgName = "$"+ paramName.trim();
                    args.add(insertArgName);
                }
            }
        }
        return args;
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
        formatter.appendText(myFunction.getFullName());
        formatter.name(getKind(), false);


        List<String> args = myFunction.getArguments();
        if ((args != null) && (args.size() > 0)) {

            formatter.appendText(LEFT_PARENS);

            Iterator<String> it = args.iterator();

            while (it.hasNext()) {
                formatter.parameters(true);
                formatter.appendHtml(it.next());
                formatter.parameters(false);

                if (it.hasNext()) {
                    formatter.appendText(COMMA);
                }
            }

            formatter.appendText(RIGHT_PARENS);
        }

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
        return myFunction.getReturnType();
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
            if (paramDesc.startsWith(OPTIONAL_PARAM_PREFIX)) {
                // This and all the next params are optional.
                sb.append("${"); //NOI18N
                sb.append("php-func-"); // NOI18N
                sb.append(getInsertPrefix()); // NOI18N
                sb.append("-optArgs"); // NOI18N
                sb.append(" default=\""); // NOI18N
//                for (; i < paramCount; i++) {
//                    sb.append(paramDesc);
//                    if (i < paramCount - 1) {
//                        sb.append(", "); //NOI18N
//                    }
//                }
                sb.append("\""); // NOI18N
                sb.append("}"); //NOI18N
                break;
            }
            if (i != 0 && i < paramCount) {
                sb.append(", "); //NOI18N
            }
            sb.append("${"); //NOI18N
            sb.append("php-func-"); // NOI18N
            sb.append(getInsertPrefix()); // NOI18N
            sb.append("-arg-"); // NOI18N
            sb.append(Integer.toString(id++));
            sb.append(" default=\""); // NOI18N
            sb.append(paramDesc);
            sb.append("\""); // NOI18N
            sb.append("}"); //NOI18N

        }
        sb.append(delimiters[1]);
        sb.append("${cursor}"); // NOI18N
        return sb.toString();
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
                        return myFunction.getFullName();
                    }

                    public String getDocumentation() {
                        return myFunction.getDocumentation();
                    }
                };
    }
    private FunctionDoc myFunction;
}
