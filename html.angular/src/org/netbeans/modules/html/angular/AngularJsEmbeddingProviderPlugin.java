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
        int balance;

        public StackItem(String tag) {
            this.tag = tag;
            this.balance = 1;
        }
    }
    private final LinkedList<StackItem> stack;
    private String lastTagOpen = null;
    private TokenSequence<HTMLTokenId> tokenSequence;
    private Snapshot snapshot;
    private List<Embedding> embeddings;
    private JsIndex index;
    
    private enum AngularAttribute  {
        controller,
        model,
    }

    private AngularAttribute interestedAttr;
    /** keeps mapping from simple property name to the object fqn 
     */
    private HashMap<String, String> propertyToFqn;
    
    public AngularJsEmbeddingProviderPlugin() {
        this.stack = new LinkedList();
        this.propertyToFqn = new HashMap();
        this.interestedAttr = null;
    }

    @Override
//    public boolean startProcessing(HtmlParserResult parserResult, Snapshot snapshot, TokenSequence<HTMLTokenId> tokenSequence, List<Embedding> embeddings) {
    public boolean startProcessing(Snapshot snapshot, TokenSequence<HTMLTokenId> tokenSequence, List<Embedding> embeddings) {
        this.snapshot = snapshot;
        this.tokenSequence = tokenSequence;
        this.embeddings = embeddings;

//        AngularModel model = AngularModel.getModel(parserResult);
//        if(!model.isAngularPage()) {
//            return false;
//        }
        
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
                        stack.pop();
                        embeddings.add(snapshot.create("});\n", Constants.JAVASCRIPT_MIMETYPE));  //NOI18N
                    }
                }
                break;
            case ARGUMENT:
                if (tokenText.length() > 3) {
                    // remove the ng- prefix
                    String attrName = tokenText.toString().trim().toLowerCase().substring(3);
                    try {
                        interestedAttr = AngularAttribute.valueOf(attrName);
                    } catch (IllegalArgumentException e) {
                        interestedAttr = null;
                    }
                }
                break;
            case VALUE:
                if (interestedAttr != null) {
                    String value = WebUtils.unquotedValue(tokenText);
                    switch (interestedAttr) {
                        case controller:
                            processed = processController(value);
                            break;
                        case model:
                            processed = processModel(value);
                        default:        
                    }
                    stack.push(new StackItem(lastTagOpen));
                }
                break;
            case EL_OPEN_DELIMITER:
                if (tokenSequence.moveNext()) {
                    if (tokenSequence.token().id() == HTMLTokenId.EL_CONTENT) {
                        String value = tokenSequence.token().text().toString().trim();
                        String name = value;
                        int parenIndex = name.indexOf('('); //NOI18N
                        if (parenIndex > -1) {
                            name = name.substring(0, parenIndex);
                        }
                        if (propertyToFqn.containsKey(name)) {
                            embeddings.add(snapshot.create(propertyToFqn.get(name) + ".$scope.", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                            embeddings.add(snapshot.create(tokenSequence.offset(), value.length(), Constants.JAVASCRIPT_MIMETYPE));
                            embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                            processed = true;
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
        sb.append(controllerName).append("\n");  //NOI18N
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
                        sb.append(" = new ");   //NOI18N
                        sb.append(type);
                        sb.append("()");    //NOI18N

                    }
            }
            sb.append(";\n");   //NOI18N
        }

        embeddings.add(snapshot.create(sb.toString(), Constants.JAVASCRIPT_MIMETYPE));
        return true;
    }
    
    private boolean processModel(String name) {     
        if (name.isEmpty()) {
            embeddings.add(snapshot.create("( function () {", Constants.JAVASCRIPT_MIMETYPE));
            embeddings.add(snapshot.create(tokenSequence.offset(), tokenSequence.token().length(), Constants.JAVASCRIPT_MIMETYPE));
            embeddings.add(snapshot.create(";})();", Constants.JAVASCRIPT_MIMETYPE));
        } else {
            if (propertyToFqn.containsKey(name)) {
                embeddings.add(snapshot.create(propertyToFqn.get(name) + ".$scope.", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                embeddings.add(snapshot.create(tokenSequence.offset() + 1, name.length(), Constants.JAVASCRIPT_MIMETYPE));
                embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
            }  else {
                // need to create local variable
                if (name.indexOf(' ') == -1 && name.indexOf('(') == -1) {
                    embeddings.add(snapshot.create("var ", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
                }
                embeddings.add(snapshot.create(tokenSequence.offset() + 1, name.length(), Constants.JAVASCRIPT_MIMETYPE));
                embeddings.add(snapshot.create(";\n", Constants.JAVASCRIPT_MIMETYPE)); //NOI18N
            }
        }
        return true;
    }
}
