/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.CompletionContextFinder.CompletionContext;
import org.netbeans.modules.javascript2.editor.index.IndexedElement;
import org.netbeans.modules.javascript2.editor.model.JsElement;
import org.netbeans.modules.javascript2.editor.model.JsFunction;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.model.TypeUsage;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Petr Pisl
 */
public class JsCompletionItem implements CompletionProposal {
    
    protected final CompletionRequest request;
    private final ElementHandle element;
    
    JsCompletionItem(ElementHandle element, CompletionRequest request) {
        this.element = element;
        this.request = request;
    }
    
    @Override
    public int getAnchorOffset() {
        return request.anchor;
    }

    @Override
    public ElementHandle getElement() {
        return element;
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public String getInsertPrefix() {
        return element.getName();
    }

    @Override
    public String getSortText() {
        return getName();
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        formatter.appendText(getName());
        return formatter.getText();
    }

    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        formatter.reset();
        formatter.appendText(getFileNameURL());
        return formatter.getText();
    }

    @Override
    public ElementKind getKind() {
        return element.getKind();
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public Set<Modifier> getModifiers() {
        Set<Modifier> emptyModifiers = Collections.emptySet();
        ElementHandle handle = getElement();
        return (handle != null) ? handle.getModifiers() : emptyModifiers;
    }

    @Override
    public boolean isSmart() {
        // TODO implemented properly
        return false;
    }

    @Override
    public int getSortPrioOverride() {
        return 0;
    }

    @Override
    public String getCustomInsertTemplate() {
        return null;
    }
    
    public String getFileNameURL() {
        ElementHandle elem = getElement(); 
        return elem.getFileObject() != null 
                ? elem.getFileObject().getNameExt()
                : getName();
     }
    
    public static class CompletionRequest {
        public  int anchor;
        public  JsParserResult result;
        public  ParserResult info;
        public  String prefix;
        public  String currentlyEditedFileURL;
        public CompletionContext context;
    }
    
    public static class JsFunctionCompletionItem extends JsCompletionItem {
        
        JsFunctionCompletionItem(ElementHandle element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.emphasis(true);
            formatter.appendText(getName());
            formatter.emphasis(false);
            formatter.appendText("(");
            appendParamsStr(formatter);
            formatter.appendText(")");
            appendReturnTypes(formatter);
            return formatter.getText();
        }

        private void appendParamsStr(HtmlFormatter formatter){
            LinkedHashMap<String, Collection<String>> allParameters = new LinkedHashMap<String, Collection<String>>();

            ElementHandle element = getElement();
            if(element instanceof JsFunction) {
                for (JsObject jsObject: ((JsFunction) element).getParameters()) {
                    Collection<String> types = new ArrayList();
                    for (TypeUsage type : jsObject.getAssignmentForOffset(jsObject.getOffset() + 1)) {
                        types.add(type.getType());
                    }
                    allParameters.put(jsObject.getName(), types);
                }
            } else if (element instanceof IndexedElement.FunctionIndexedElement) {
                allParameters = ((IndexedElement.FunctionIndexedElement) element).getParameters();
            }
            for (Iterator<Map.Entry<String, Collection<String>>> it = allParameters.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, Collection<String>> entry = it.next();
                formatter.parameters(true);
                formatter.appendText(entry.getKey());
                formatter.parameters(false);
                Collection<String> types = entry.getValue();
                if (!types.isEmpty()) {
                    formatter.type(true);
                    formatter.appendText(": ");  //NOI18N
                    for (Iterator<String> itTypes = types.iterator(); itTypes.hasNext();) {
                        formatter.appendText(itTypes.next());
                        if (itTypes.hasNext()) {
                            formatter.appendText("|");   //NOI18N
                        }
                    }
                    formatter.type(false);
                }
                if (it.hasNext()) {
                    formatter.appendText(", ");  //NOI18N
                }    
            }
        }

        private void appendReturnTypes(HtmlFormatter formatter) {
            Collection<String> returnTypes = new ArrayList<String>();

            ElementHandle element = getElement();
            if (element instanceof JsFunction) {
                for (TypeUsage type: ((JsFunction) element).getReturnTypes()) {
                    returnTypes.add((type.getType()));
                }
            } else if (element instanceof IndexedElement.FunctionIndexedElement) {
                returnTypes.addAll(((IndexedElement.FunctionIndexedElement) element).getReturnTypes());
            }
            if (!returnTypes.isEmpty()) {
                formatter.appendText(": "); //NOI18N
                formatter.type(true);
                for (Iterator<String> it = returnTypes.iterator(); it.hasNext();) {
                    formatter.appendText(it.next());
                    if (it.hasNext()) {
                        formatter.appendText("|"); //NOI18N
                    }
                }
                formatter.type(false);
            }
        }

        @Override
        public String getCustomInsertTemplate() {
            StringBuilder template = new StringBuilder();
            template.append(getName());
            template.append("(${cursor})");
            return template.toString();
        }
        
    }

    static class KeywordItem extends JsCompletionItem {
        private static  ImageIcon keywordIcon = null;
        private String keyword = null;

        public KeywordItem(String keyword, CompletionRequest request) {
            super(null, request);
            this.keyword = keyword;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);
            return formatter.getText();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            if (keywordIcon == null) {
                keywordIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/javascript2/editor/resources/javascript.png")); //NOI18N
            }
            return keywordIcon;
        }
        
        @Override
        public String getInsertPrefix() {
            return getName();
        }
        
        @Override
        public String getCustomInsertTemplate() {
            StringBuilder builder = new StringBuilder();
            
            JsKeyWords.CompletionType type = JsKeyWords.KEYWORDS.get(getName());
            if (type == null) {
                return getName();
            }
            //CodeStyle codeStyle = CodeStyle.get(EditorRegistry.lastFocusedComponent().getDocument());
            boolean appendSpace = true;
            String name = null;
            switch(type) {
                case ENDS_WITH_SPACE:
                    builder.append(getName());
                    builder.append(" ${cursor}"); //NOI18N
                    break;
                case CURSOR_INSIDE_BRACKETS:
                    builder.append(getName());
                    builder.append("(${cursor})"); //NOI18N
                    break;
                case ENDS_WITH_CURLY_BRACKETS:
                    builder.append(getName());
                    builder.append(" {${cursor}}"); //NOI18N
                    break;
                case ENDS_WITH_SEMICOLON:
                    builder.append(getName());
                    CharSequence text = request.info.getSnapshot().getText();
                    int index = request.anchor + request.prefix.length();
                    if (index == text.length() || ';' != text.charAt(index)) { //NOI18N
                        builder.append(";"); //NOI18N
                    }
                    break;
                case ENDS_WITH_COLON:
                    builder.append(getName());
                    builder.append(" ${cursor}:"); //NOI18N
                    break;
                case ENDS_WITH_DOT:
                    builder.append(getName());
                    builder.append(".${cursor}"); //NOI18N
                    break;
                default:
                    assert false : type.toString();
                    break;
            }
            return builder.toString();
        }
    }

    public static class JsPropertyCompletionItem extends JsCompletionItem {

        JsPropertyCompletionItem(ElementHandle element, CompletionRequest request) {
            super(element, request);
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.appendText(getName());
            Collection<? extends TypeUsage> assignment = null;
            if (getElement() instanceof JsObject) {
                JsObject jsObject = (JsObject) getElement();
                assignment = jsObject.getAssignmentForOffset(request.anchor);
            } else if (getElement() instanceof IndexedElement) {
                IndexedElement iElement = (IndexedElement)getElement();
                assignment = iElement.getAssignments();
            }
            if (assignment != null) {
                if (!assignment.isEmpty()) {
                    formatter.type(true);
                    formatter.appendText(": ");  //NOI18N
                    for (Iterator<? extends TypeUsage> it = assignment.iterator(); it.hasNext();) {
                        formatter.appendText(it.next().getType());
                        if (it.hasNext()) {
                            formatter.appendText("|");   //NOI18N
                        }
                    }
                    formatter.type(false);
                }
            }
            return formatter.getText();
        }
    }

    public static class Factory {
        
        public static JsCompletionItem create(JsElement object, CompletionRequest request) {
            JsCompletionItem result;
            switch (object.getJSKind()) {
                case CONSTRUCTOR:
                case FUNCTION:
                case METHOD:
                    result = new JsFunctionCompletionItem(object, request);
                    break;
                case PROPERTY:
                case PROPERTY_GETTER:
                case PROPERTY_SETTER:
                case FIELD:
                    result = new JsPropertyCompletionItem(object, request);
                    break;
                default:
                    result = new JsCompletionItem(object, request);
            }
            return result;
        }
    }
}
