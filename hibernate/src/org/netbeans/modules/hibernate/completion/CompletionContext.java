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

package org.netbeans.modules.hibernate.completion;

import org.netbeans.modules.hibernate.editor.EditorContextFactory;
import org.netbeans.modules.hibernate.editor.DocumentContext;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.xml.text.api.XMLDefaultTokenContext;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.dom.EmptyTag;
import org.netbeans.modules.xml.text.syntax.dom.EndTag;
import org.netbeans.modules.xml.text.syntax.dom.StartTag;
import org.netbeans.modules.xml.text.syntax.dom.Tag;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Tracks context information for a code completion scenario
 * 
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class CompletionContext {
    private ArrayList<String> existingAttributes;

    public static enum CompletionType {
        TAG,
        VALUE,
        ATTRIBUTE,
        ATTRIBUTE_VALUE,
        NONE
    };
    
    private CompletionType completionType = CompletionType.NONE;
    private Document doc;
    private int caretOffset;
    private DocumentContext documentContext;
    private String typedChars = "";
    private char lastTypedChar;
    private XMLSyntaxSupport support;

    public CompletionContext(Document doc, int caretOffset) {
        this.doc = doc;
        this.caretOffset = caretOffset;
        this.support = (XMLSyntaxSupport) ((BaseDocument)doc).getSyntaxSupport();
        this.documentContext = EditorContextFactory.getDocumentContext(doc, caretOffset);
        this.lastTypedChar = support.lastTypedChar();
        initContext();
    }
    
    private void initContext() {
        TokenItem token = documentContext.getCurrentToken();
        if(token == null)
            return;
        
        boolean tokenBoundary = (token.getOffset() == caretOffset) 
                || ((token.getOffset() + token.getImage().length()) == caretOffset);
        
        int id = token.getTokenID().getNumericID();
        SyntaxElement element = documentContext.getCurrentElement();
        switch (id) {
            //user enters < character
            case XMLDefaultTokenContext.TEXT_ID:
                String chars = token.getImage().trim();
                if (chars != null && chars.equals("") &&
                        token.getPrevious().getImage().trim().equals("/>")) { // NOI18N
                    completionType = CompletionType.NONE;
                    break;
                }
                if (chars != null && chars.equals("") &&
                        token.getPrevious().getImage().trim().equals(">")) { // NOI18N
                    completionType = CompletionType.VALUE;
                    break;
                }
                if (chars != null && !chars.startsWith("<") &&
                        token.getPrevious().getImage().trim().equals(">")) { // NOI18N

                    completionType = CompletionType.VALUE;
                    typedChars = "";
                    break;
                }
                if (chars != null && !chars.equals("<") &&
                        token.getPrevious().getImage().trim().equals(">")) { // NOI18N
                    completionType = CompletionType.NONE;
                    break;
                }
                if (chars != null && chars.startsWith("<")) { // NOI18N
                    typedChars = chars.substring(1);
                }
                completionType = CompletionType.TAG;
                break;

            //start tag of an element
            case XMLDefaultTokenContext.TAG_ID:
                if (element instanceof EndTag) {
                    completionType = CompletionType.NONE;
                    break;
                }
                if (element instanceof EmptyTag) {
                    if (token != null &&
                            token.getImage().trim().equals("/>")) {
                        completionType = CompletionType.NONE;
                        break;
                    }
                    EmptyTag tag = (EmptyTag) element;
                    if (element.getElementOffset() + 1 == this.caretOffset) {
                        completionType = CompletionType.TAG;
                        break;
                    }
                    if (caretOffset > element.getElementOffset() + 1 &&
                            caretOffset <= element.getElementOffset() + 1 + tag.getTagName().length()) {
                        completionType = CompletionType.TAG;
                        typedChars = tag.getTagName();
                        break;
                    }
                    completionType = CompletionType.ATTRIBUTE;
                    break;
                }

                if (element instanceof StartTag) {
                    if (token != null &&
                            token.getImage().trim().equals(">")) {
                        completionType = CompletionType.NONE;
                        break;
                    }
                    if (token != null &&
                            token.getImage().trim().startsWith("</")) {
                        typedChars = "";
                        completionType = CompletionType.VALUE;
                        break;
                    }
                    if (element.getElementOffset() + 1 != this.caretOffset) {
                        StartTag tag = (StartTag) element;
                        typedChars = tag.getTagName();
                    }
                }
                
                if (element instanceof Text) {
                    if (token != null &&
                            token.getImage().trim().startsWith("</")) {
                        typedChars = token.getPrevious().getImage().trim();
                        completionType = CompletionType.VALUE;
                        break;
                    }
                }
                
                if (lastTypedChar == '>') {
                    completionType = CompletionType.VALUE;
                    break;
                }
                completionType = CompletionType.TAG;
                break;

            //user enters an attribute name
            case XMLDefaultTokenContext.ARGUMENT_ID:
                completionType = CompletionType.ATTRIBUTE;
                typedChars = token.getImage().substring(0, caretOffset - token.getOffset());;
                break;

            //some random character
            case XMLDefaultTokenContext.CHARACTER_ID:
            //user enters = character, we should ignore all other operators
            case XMLDefaultTokenContext.OPERATOR_ID:
                completionType = CompletionType.NONE;
                break;
            //user enters either ' or "
            case XMLDefaultTokenContext.VALUE_ID:
                if(!tokenBoundary) {
                    completionType = CompletionType.ATTRIBUTE_VALUE;
                    typedChars = token.getImage().substring(1, caretOffset - token.getOffset());
                } else {
                    completionType = CompletionType.NONE;
                }
                break;

            //user enters white-space character
            case XMLDefaultTokenContext.WS_ID:
                completionType = CompletionType.NONE;
                TokenItem prev = token.getPrevious();
                while (prev != null &&
                        (prev.getTokenID().getNumericID() == XMLDefaultTokenContext.WS_ID)) {
                    prev = prev.getPrevious();
                }
                
                if(prev.getTokenID().getNumericID() == XMLDefaultTokenContext.ARGUMENT_ID) {
                    typedChars = prev.getImage();
                    completionType = CompletionType.ATTRIBUTE;
                } else if ((prev.getTokenID().getNumericID() == XMLDefaultTokenContext.VALUE_ID) ||
                        (prev.getTokenID().getNumericID() == XMLDefaultTokenContext.TAG_ID)) {
                    completionType = CompletionType.ATTRIBUTE;
                }
                break;

            default:
                completionType = CompletionType.NONE;
                break;
        }
    }

    public CompletionType getCompletionType() {
        return completionType;
    }
    
    public String getTypedPrefix() {
        return typedChars;
    }
    
    public Document getDocument() {
        return this.doc;
    }
    
    public DocumentContext getDocumentContext() {
        return this.documentContext;
    }
    
    public int getCaretOffset() {
        return caretOffset;
    }
    
    public Node getTag() {
        SyntaxElement element = documentContext.getCurrentElement();
        return (element instanceof Tag) ? (Node) element : null;
    }
    
    public TokenItem getCurrentToken() {
        return documentContext.getCurrentToken();
    }
    
    public List<String> getExistingAttributes() {
        if(existingAttributes != null)
            return existingAttributes;
        existingAttributes = new ArrayList<String>();
        TokenItem item = documentContext.getCurrentToken().getPrevious();
        while(item != null) {
            if(item.getTokenID().getNumericID() ==
                    XMLDefaultTokenContext.TAG_ID)
                break;
            if(item.getTokenID().getNumericID() ==
                    XMLDefaultTokenContext.ARGUMENT_ID) {
                existingAttributes.add(item.getImage());
            }
            item = item.getPrevious();
        }
        return existingAttributes;
    }
}
