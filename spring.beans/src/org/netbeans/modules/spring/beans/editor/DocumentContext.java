/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.netbeans.modules.spring.beans.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.xml.XMLConstants;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.xml.text.syntax.SyntaxElement;
import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.dom.EmptyTag;
import org.netbeans.modules.xml.text.syntax.dom.EndTag;
import org.netbeans.modules.xml.text.syntax.dom.StartTag;
import org.netbeans.modules.xml.text.syntax.dom.Tag;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Tracks context information for XML files
 * 
 * @author Rohan Ranade
 */
public class DocumentContext {

    public static final String PREFIX = "ns"; //NOI18N
    public static final String XSI_SCHEMALOCATION = "schemaLocation"; //NOI18N
    public static final String XSI_NONS_SCHEMALOCATION = "noNamespaceSchemaLocation"; //NOI18N
    
    private Document document;
    private XMLSyntaxSupport syntaxSupport;
    private int caretOffset = -1;
    private SyntaxElement element;
    private TokenItem token;
    private boolean valid = false;
    private StartTag docRoot;
    private String defaultNamespace;
    private HashMap<String, String> declaredNamespaces =
            new HashMap<String, String>();
    private String schemaLocation;
    private String noNamespaceSchemaLocation;

    DocumentContext(Document document) {
        this.document = document;
        this.syntaxSupport = (XMLSyntaxSupport) ((BaseDocument) document).getSyntaxSupport();
    }

    public void reset(int caretOffset) {
        this.caretOffset = caretOffset;
        initialize();
    }

    private void initialize() {
        valid = true;
        declaredNamespaces.clear();
        try {
            element = syntaxSupport.getElementChain(caretOffset);
            token = syntaxSupport.getPreviousToken(caretOffset);
            this.docRoot = ContextUtilities.getRoot(element);
            populateNamespaces();
        } catch (BadLocationException ex) {
            // No context support available in this case
            valid = false;
        }
    }

    public boolean isValid() {
        return this.valid;
    }

    public int getCurrentTokenId() {
        if (isValid()) {
            return token.getTokenID().getNumericID();
        } else {
            return -1;
        }
    }

    public TokenItem getCurrentToken() {
        if (isValid()) {
            return token;
        } else {
            return null;
        }
    }

    public String getCurrentTokenImage() {
        if (isValid()) {
            return token.getImage();
        } else {
            return null;
        }
    }

    public SyntaxElement getCurrentElement() {
        return this.element;
    }
    
    public List<String> getPathFromRoot() {
        if (isValid()) {
            SyntaxElement elementRef = this.element;
            Stack<SyntaxElement> stack = new Stack<SyntaxElement>();

            while (elementRef != null) {
                if ((elementRef instanceof EndTag) ||
                        (elementRef instanceof EmptyTag && stack.isEmpty()) ||
                        (elementRef instanceof StartTag && stack.isEmpty())) {
                    stack.push(elementRef);
                    elementRef = elementRef.getPrevious();
                    continue;
                }
                if (elementRef instanceof StartTag) {
                    StartTag start = (StartTag) elementRef;
                    if (stack.peek() instanceof EndTag) {
                        EndTag end = (EndTag) stack.peek();
                        if (end.getTagName().equals(start.getTagName())) {
                            stack.pop();
                        }
                    } else {
                        SyntaxElement e = (SyntaxElement) stack.peek();
                        String tagAtTop = (e instanceof StartTag) ? ((StartTag) e).getTagName() : ((EmptyTag) e).getTagName();
                        stack.push(elementRef);
                    }
                }
                elementRef = elementRef.getPrevious();
            }

            return createPath(stack);
        }

        return Collections.emptyList();
    }

    private List<String> createPath(Stack<SyntaxElement> stack) {
        ArrayList<String> pathList = new ArrayList<String>();
        while (!stack.isEmpty()) {
            SyntaxElement top = stack.pop();
            String tagName = (top instanceof StartTag) ? ((StartTag) top).getTagName() : ((EmptyTag) top).getTagName();
            if (tagName != null) {
                pathList.add(tagName);
            }
        }

        return Collections.unmodifiableList(pathList);
    }

    public Document getDocument() {
        return this.document;
    }

    public String lookupNamespacePrefix(String prefix) {
        return declaredNamespaces.get(prefix);
    }

    public StartTag getDocRoot() {
        return docRoot;
    }

    public int getCaretOffset() {
        return this.caretOffset;
    }

    private void populateNamespaces() {
        // Find the a start or empty tag just before the current syntax element.
        SyntaxElement element = this.element;
        while (element != null && !(element instanceof StartTag) && !(element instanceof EmptyTag)) {
            element = element.getPrevious();
        }
        if (element == null) {
            return;
        }

        // To find all namespace declarations active at the caret offset, we
        // need to look at xmlns attributes of the current element and its ancestors.
        Node node = (Node)element;
        while (node != null) {
            if (node instanceof StartTag || node instanceof EmptyTag) {
                NamedNodeMap attributes = ((Tag)node).getAttributes();
                for (int index = 0; index < attributes.getLength(); index++) {
                    Attr attr = (Attr) attributes.item(index);
                    String attrName = attr.getName();
                    String attrValue = attr.getValue();
                    if (attrName == null || attrValue == null) {
                        continue;
                    }
                    String prefix = ContextUtilities.getPrefixFromNamespaceDeclaration(attrName);
                    if (prefix == null) {
                        continue;
                    }
                    // Avoid overwriting a namespace declaration "closer" to the caret offset.
                    if (!declaredNamespaces.containsKey(prefix)) {
                        declaredNamespaces.put(prefix, attrValue);
                    }
                }
            }
            node = node.getParentNode();
        }
    }
    
    public String getNoNamespaceSchemaLocation() {
        return noNamespaceSchemaLocation;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DocumentContext other = (DocumentContext) obj;
        if (this.document != other.document && (this.document == null || !this.document.equals(other.document))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + (this.document != null ? this.document.hashCode() : 0);
        return hash;
    }
}