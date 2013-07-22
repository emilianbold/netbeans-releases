/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.angular;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.angular.model.AngularModel;
import org.netbeans.modules.html.angular.model.Directive;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.spi.embedding.JsEmbeddingProviderPlugin;
import org.netbeans.modules.javascript2.editor.index.IndexedElement;
import org.netbeans.modules.javascript2.editor.index.JsIndex;
import static org.netbeans.modules.javascript2.editor.model.JsElement.Kind.METHOD;
import org.netbeans.modules.javascript2.editor.model.TypeUsage;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl, mfukala@netbeans.org
 */
@MimeRegistration(mimeType = "text/html", service = JsEmbeddingProviderPlugin.class)
public class AngularJsEmbeddingProviderPlugin extends JsEmbeddingProviderPlugin {

    private static class StackItem {

        final String tag;
        final String finishText;
        int balance;

        public StackItem(String tag, String finishText) {
            this.tag = tag;
            this.balance = 1;
            this.finishText = finishText;
        }
    }
    private final LinkedList<StackItem> stack;
    private String lastTagOpen = null;
    private TokenSequence<HTMLTokenId> tokenSequence;
    private Snapshot snapshot;
    private List<Embedding> embeddings;
    private JsIndex index;
  
    private Directive interestedAttr;
    /** keeps mapping from simple property name to the object fqn 
     */
    private HashMap<String, String> propertyToFqn;
    
    public AngularJsEmbeddingProviderPlugin() {
        this.stack = new LinkedList();
        this.propertyToFqn = new HashMap();
    }

    @Override
    public boolean startProcessing(HtmlParserResult parserResult, Snapshot snapshot, TokenSequence<HTMLTokenId> tokenSequence, List<Embedding> embeddings) {
        this.snapshot = snapshot;
        this.tokenSequence = tokenSequence;
        this.embeddings = embeddings;
        this.stack.clear();
        AngularModel model = AngularModel.getModel(parserResult);
        if(!model.isAngularPage()) {
            return false;
        }
        
        FileObject file = snapshot.getSource().getFileObject();
        if (file == null) {
            return false;
        }

        this.index = JsIndex.get(file);
        
        return true;
    }

    @Override
    public boolean processToken() {
        boolean processed = false;
        CharSequence tokenText = tokenSequence.token().text();        
        switch (tokenSequence.token().id()) {
            case TAG_OPEN:
                lastTagOpen = tokenText.toString();
                StackItem top = stack.peek();
                if (top != null && LexerUtils.equals(top.tag, lastTagOpen, false, false)) {
                    top.balance++;
                }
                break;
            case TAG_CLOSE:
                top = stack.peek();
                if (top != null && LexerUtils.equals(top.tag, tokenText, false, false)) {
                    top.balance--;
                    if (top.balance == 0) {
                        processed = true;
                        embeddings.add(snapshot.create(top.finishText, Constants.JAVASCRIPT_MIMETYPE));  //NOI18N
                        stack.pop();
                        top = stack.peek();
                        if (top != null && LexerUtils.equals(top.tag, tokenText, false, false)) {
                            top.balance--;
                        }
                        
                    }
                }
                break;
            case ARGUMENT:
                Directive ajsDirective = Directive.getDirective(tokenText.toString().trim().toLowerCase());
                if(ajsDirective != null) {
                    interestedAttr = ajsDirective;
                } else {
                    interestedAttr = null;
                }
                break;
            case VALUE:
                if (interestedAttr != null) {
                    String value = WebUtils.unquotedValue(tokenText);
                    switch (interestedAttr) {
                        case controller:
                            processed = processController(value);
                            stack.push(new StackItem(lastTagOpen, "});\n")); //NOI18N
                            break;
                        case model:
                        case disabled:
                        case click:
                            processed = processModel(value);
                            break;
                        case repeat:
                            processed = processRepeat(value);
                            stack.push(new StackItem(lastTagOpen, "}\n")); //NOI18N
                            break;
                        default:   
                            processed = processExpression(value);
                    }
                }
                break;
            case EL_OPEN_DELIMITER:
                 if (tokenSequence.moveNext()) {
                    if (tokenSequence.token().id() == HTMLTokenId.EL_CONTENT) {
                        String value = tokenSequence.token().text().toString().trim();
                        int indexStart = 0;
                        String name = value;
                        if (value.startsWith("(")) {
                            name = value.substring(1);
                            indexStart = 1;
                        }
                        int parenIndex = name.indexOf('('); //NOI18N
                        if (parenIndex > -1) {
                            name = name.substring(0, parenIndex);
                        } 
                        if (propertyToFqn.containsKey(name)) {
                            embeddings.add(snapshot.create(propertyToFqn.get(name) + ".$scope.", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                            embeddings.add(snapshot.create(tokenSequence.offset(), value.length(), Constants.JAVASCRIPT_MIMETYPE));
                            embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                            processed = true;
                        } else if (!name.contains("|") && !name.contains(":")){ //NOI18N
                            embeddings.add(snapshot.create(tokenSequence.offset(), value.length(), Constants.JAVASCRIPT_MIMETYPE));
                            embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                            processed = true;
                        } else if (name.contains("|")){
                            int indexEnd = name.indexOf('|');
                            name = name.substring(0, indexEnd);
                            if (name.startsWith("-")) {
                                indexStart++;
                                name = name.substring(1);
                            }
                            if(propertyToFqn.containsKey(name)) {
                                embeddings.add(snapshot.create(propertyToFqn.get(name) + ".$scope.", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                                embeddings.add(snapshot.create(tokenSequence.offset() + indexStart, name.length(), Constants.JAVASCRIPT_MIMETYPE));
                                embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                                processed = true;
                            } else {
                                embeddings.add(snapshot.create(tokenSequence.offset() + indexStart, name.length(), Constants.JAVASCRIPT_MIMETYPE));
                                embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                                processed = true;
                            }
                        } else {
                            tokenSequence.movePrevious();
                        }
                    } else {
                        tokenSequence.movePrevious();
                    }
                }
                break;    
            default:
        }
        return processed;
    }
    
    private boolean processController(String controllerName) {
        StringBuilder sb = new StringBuilder();
        sb.append("(function () { // generated function for scope ");   //NOI18N
        if (!controllerName.trim().isEmpty()) {
            sb.append(controllerName).append("\n");  //NOI18N
        }
        embeddings.add(snapshot.create(sb.toString(), Constants.JAVASCRIPT_MIMETYPE));
        embeddings.add(snapshot.create(tokenSequence.offset() + 1, controllerName.length(), Constants.JAVASCRIPT_MIMETYPE));
        sb = new StringBuilder();
        sb.append("();\n"); //NOI18N
        Collection<IndexedElement> properties = index.getProperties(controllerName + ".$scope"); //NOI18N
        for (IndexedElement indexedElement : properties) {
            propertyToFqn.put(indexedElement.getName(), controllerName);
            sb.append("var ");  //NOI18N
            sb.append(indexedElement.getName());

            switch (indexedElement.getJSKind()) {
                case METHOD:
                    IndexedElement.FunctionIndexedElement function = (IndexedElement.FunctionIndexedElement) indexedElement;
                    sb.append(" = function(");  //NOI18N
                    boolean first = true;
                    for (String param : function.getParameters().keySet()) {
                        if (!first) {
                            sb.append(", ");    //NOI18N
                        } else {
                            first = false;
                        }
                        sb.append(param);
                    }

                    sb.append("){}");   //NOI18N
                    break;

                default:
                    //try to obtain the element type from the stored
                    //assignment
                    List<TypeUsage> typeUsages = new ArrayList<>(indexedElement.getAssignments());
                    if (!typeUsages.isEmpty()) {
                        //use the last assignment
                        TypeUsage typeUsage = typeUsages.get(typeUsages.size() - 1);
                        String type = typeUsage.getType();
                        if (type.indexOf('@') == -1) {
                            // don't use unresolved types
                            // TODO there should be the unresolved type resolved
                            sb.append(" = new ");   //NOI18N
                            sb.append(type);
                            sb.append("()");    //NOI18N
                        }

                    } else {
                        sb.append(" = "); //NOI18N
                        sb.append(indexedElement.getFQN());
                    }
            }
            sb.append(";\n");   //NOI18N
        }

        embeddings.add(snapshot.create(sb.toString(), Constants.JAVASCRIPT_MIMETYPE));
        return true;
    }
    
    private boolean processModel(String value) {     
         if (value.isEmpty()) {
            embeddings.add(snapshot.create("( function () {", Constants.JAVASCRIPT_MIMETYPE));
            embeddings.add(snapshot.create(tokenSequence.offset(), 1, Constants.JAVASCRIPT_MIMETYPE));
            embeddings.add(snapshot.create(";})();\n", Constants.JAVASCRIPT_MIMETYPE));
        } else {
            int parenStart = value.indexOf('('); //NOI18N
            String name = value;
            int lenght = name.length();
            if (parenStart > -1) {
                name = name.substring(0, parenStart).trim();
            }
            if (name.indexOf('=') > -1) {
                name = name.substring(0, name.indexOf('=')).trim();
            }
            if (propertyToFqn.containsKey(name)) {
                embeddings.add(snapshot.create(propertyToFqn.get(name) + ".$scope.", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                
                if(parenStart > -1) {
                    int parenEnd = parenStart;
                    int balance = 1;
                    while (balance > 0 && parenEnd < value.length()) {
                        char ch = value.charAt(parenEnd);
                        if (ch == '(') {
                            balance++;
                        } else if (ch == ')') {
                            balance--;
                        }
                        if (balance > 0) {
                            parenEnd++;
                        }
                    }
                    embeddings.add(snapshot.create(tokenSequence.offset() + 1, parenEnd, Constants.JAVASCRIPT_MIMETYPE));
                } else {
                    embeddings.add(snapshot.create(tokenSequence.offset() + 1, lenght, Constants.JAVASCRIPT_MIMETYPE));
                } 
                embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
            }  else {
                // need to create local variable
                if (value.indexOf(' ') == -1 && parenStart == -1 && value.indexOf('.') == -1) {
                    embeddings.add(snapshot.create("var ", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                }
                embeddings.add(snapshot.create(tokenSequence.offset() + 1, value.length(), Constants.JAVASCRIPT_MIMETYPE));
                embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
            }
        }
        return true;
    }
    
    private boolean processRepeat(String expression) {
        boolean processed = false;
        // split the expression with |
        // we expect that the first part is the for cycle and the rest are conditions
        // and attributes like orderby, filter etc.
        String[] parts = expression.split("\\|");
        if (parts.length > 0) {
            // try to create the for cycle in virtual source
             if (parts[0].contains(" in ")) {
                // we understand only "in"  now
                String[] forParts = parts[0].trim().split(" in ");   // NOI18N
                embeddings.add(snapshot.create("for (var ", Constants.JAVASCRIPT_MIMETYPE));
                // forParts keeps value, collection
                // now need to check, whether the value is simple or (key, value) - issue #230223
                if (!forParts[0].contains(",")) {
                    // create virtual source for simple case:  value in collection
                    if (forParts.length == 2 && propertyToFqn.containsKey(forParts[1])) {
                        // if we know the collection from a controller ....
                        int lastPartPos = expression.indexOf(forParts[1]); // the start position of the collection name
                        embeddings.add(snapshot.create(tokenSequence.offset() + 1, lastPartPos, Constants.JAVASCRIPT_MIMETYPE));
                        embeddings.add(snapshot.create(propertyToFqn.get(forParts[1]) + ".$scope.", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                        embeddings.add(snapshot.create(tokenSequence.offset() + 1 + lastPartPos, forParts[1].length(), Constants.JAVASCRIPT_MIMETYPE));
                    } else {
                        // if we don't know the collection from a controller, put it to the virtual source at it is
                        embeddings.add(snapshot.create(tokenSequence.offset() + 1, parts[0].length(), Constants.JAVASCRIPT_MIMETYPE));
                    }
                    embeddings.add(snapshot.create(") {\n", Constants.JAVASCRIPT_MIMETYPE));  //NOI18N
                } else {
                    // expect that thre is expression like: (key, value) in collection
                    // such expression should be translated to: for (var key in collectoin) { var value = collection[key];
                    String valueExp = forParts[0].trim();
                    if (valueExp.startsWith("(")) {     // NOI18N
                        valueExp = valueExp.substring(1);
                    }
                    if (valueExp.endsWith(")")) {   // NOI18N
                        valueExp = valueExp.substring(0, valueExp.length() - 1);
                    }
                    valueExp = valueExp.trim();
                    String[] keyValue = valueExp.split(",");    // NOI18N
                    int lastPartPos = expression.indexOf(forParts[1]); // the start position of the collection name
                    int keyPos = expression.indexOf(keyValue[0]);
                    // map the key name
                    embeddings.add(snapshot.create(tokenSequence.offset() + 1 + keyPos, keyValue[0].length(), Constants.JAVASCRIPT_MIMETYPE));
                    // map " in " 
                    embeddings.add(snapshot.create(" in ", Constants.JAVASCRIPT_MIMETYPE));  //NOI18N
                    if (forParts.length == 2 && propertyToFqn.containsKey(forParts[1])) {
                        // if we know the collection from a controller ....
                        // map the collection
                        embeddings.add(snapshot.create(propertyToFqn.get(forParts[1]) + ".$scope.", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                        embeddings.add(snapshot.create(tokenSequence.offset() + 1 + lastPartPos, forParts[1].length(), Constants.JAVASCRIPT_MIMETYPE));
                    } else {
                        // map the collection
                        embeddings.add(snapshot.create(tokenSequence.offset() + 1 + lastPartPos, forParts[1].length(), Constants.JAVASCRIPT_MIMETYPE));
                    }
                    embeddings.add(snapshot.create(") {\nvar ", Constants.JAVASCRIPT_MIMETYPE));    //NOI18N
                    int valuePos = expression.indexOf(keyValue[1]);
                    embeddings.add(snapshot.create(tokenSequence.offset() + 1 + valuePos, keyValue[1].length(), Constants.JAVASCRIPT_MIMETYPE));
                    embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE));      //NOI18N
                }
                // the for cycle should be closed in appropriate CLOSE_TAG token
                processed = true;
            }
            int partIndex = 1;
            int lastPartPos = parts[0].length() + 1;
            while (partIndex < parts.length) { // are there any condition / attributes of the cycle?
                 if (parts[partIndex].contains(":")) {
                    String[] conditionParts = parts[partIndex].trim().split(":");
                    if(conditionParts.length > 1) {
                        String propName = conditionParts[1].trim();
                        int position = lastPartPos + parts[partIndex].indexOf(propName) + 1;
                        if (propertyToFqn.containsKey(propName)) {
                            embeddings.add(snapshot.create(propertyToFqn.get(propName) + ".$scope.", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N                            
                        }
                        embeddings.add(snapshot.create(tokenSequence.offset() + position, propName.length(), Constants.JAVASCRIPT_MIMETYPE));
                        embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                    }
                }
                lastPartPos = lastPartPos + parts[partIndex].length() + 1;
                partIndex++;
            }
        }
        return processed;
    }
    
    private boolean processExpression(String value) {
        boolean processed = false;
        if (value.isEmpty()) {
            embeddings.add(snapshot.create("( function () {", Constants.JAVASCRIPT_MIMETYPE));
            embeddings.add(snapshot.create(tokenSequence.offset(), 1, Constants.JAVASCRIPT_MIMETYPE));
            embeddings.add(snapshot.create(";})();\n", Constants.JAVASCRIPT_MIMETYPE));
            processed = true;
        } else {
            int lastPartPos = 0;
            if (value.startsWith("{")) {
                value = value.substring(1);
                lastPartPos = 1;
            }
            String valueTrim = value.trim();
            if (valueTrim.endsWith("}")) {
                value = valueTrim.substring(0, valueTrim.length() - 1);
            }
            int index = value.indexOf(':'); // are there pairs like name: expr?
            if (index > -1) {
                
                String[] parts = value.split(","); // example: ng-class="{completed: todo.completed, editing: todo == editedTodo}"
                for (String part : parts) {
                    index = value.indexOf(':');
                    if (index > 0) {
                        String[] conditionParts = part.trim().split(":");
                        if(conditionParts.length > 1) {
                            String propName = conditionParts[1].trim();
                            int position = lastPartPos + part.indexOf(propName) + 1;
                            if (propertyToFqn.containsKey(propName)) {
                                embeddings.add(snapshot.create(propertyToFqn.get(propName) + ".$scope.", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N                            
                            }
                            embeddings.add(snapshot.create(tokenSequence.offset() + position, propName.length(), Constants.JAVASCRIPT_MIMETYPE));
                            embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                            processed = true;
                        }
                    }
                    lastPartPos = lastPartPos + part.length() + 1;
                }
            }
        }
        return processed;
    }
}
