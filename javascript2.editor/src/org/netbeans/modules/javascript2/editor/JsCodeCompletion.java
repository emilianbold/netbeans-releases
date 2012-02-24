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

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.CompletionContextFinder.CompletionContext;
import org.netbeans.modules.javascript2.editor.JsCompletionItem.CompletionRequest;
import org.netbeans.modules.javascript2.editor.index.IndexedElement;
import org.netbeans.modules.javascript2.editor.index.JsIndex;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.model.JsElement;
import org.netbeans.modules.javascript2.editor.model.JsFunction;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.model.Type;
import org.netbeans.modules.javascript2.editor.model.TypeUsage;
import org.netbeans.modules.javascript2.editor.model.impl.ModelUtils;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
class JsCodeCompletion implements CodeCompletionHandler {
    private static final Logger LOGGER = Logger.getLogger(JsCodeCompletion.class.getName());

    private boolean caseSensitive;
    
    @Override
    public CodeCompletionResult complete(CodeCompletionContext ccContext) {
         long start = System.currentTimeMillis();
        
        
        BaseDocument doc = (BaseDocument) ccContext.getParserResult().getSnapshot().getSource().getDocument(false);
        if (doc == null) {
            return CodeCompletionResult.NONE;
        }

        this.caseSensitive = ccContext.isCaseSensitive();
        
        ParserResult info = ccContext.getParserResult();
        int caretOffset = ccContext.getCaretOffset();
        JsParserResult jsParserResult = (JsParserResult)info;
        CompletionContext context = CompletionContextFinder.findCompletionContext(info, caretOffset);
        
        LOGGER.log(Level.FINE, String.format("CC context: %s", context.toString()));
        CodeCompletionResult result = CodeCompletionResult.NONE;
        
        JsCompletionItem.CompletionRequest request = new JsCompletionItem.CompletionRequest();
            request.context = context;
            String pref = getPrefix(info, caretOffset, true);
            pref = pref == null ? "" : pref;

            request.anchor = caretOffset
                    // can't just use 'prefix.getLength()' here cos it might have been calculated with
                    // the 'upToOffset' flag set to false
                    - pref.length();
            request.result = jsParserResult;
            request.info = info;
            request.prefix = pref;
            
        final List<CompletionProposal> resultList = new ArrayList<CompletionProposal>();
        switch (context) {
            case GLOBAL:
                for(JsObject object : request.result.getModel().getVariables(caretOffset)) {
                    if (!(object instanceof JsFunction && ((JsFunction)object).isAnonymous()))
                        resultList.add(JsCompletionItem.Factory.create(object, request));
                }
                completeKeywords(request, resultList);
                break;
            case EXPRESSION:
                completeExpression(request, resultList);
                break;
            case OBJECT_PROPERTY:
                completeObjectProperty(request, resultList);
                break;
            case OBJECT_MEMBERS:
                completeObjectMember(request, resultList);
            default:
                result = CodeCompletionResult.NONE;
        }
        
        long end = System.currentTimeMillis();
        LOGGER.log(Level.FINE, "Counting JS CC took {0}ms ",  (end - start));
        if (!resultList.isEmpty()) {
            return new DefaultCompletionResult(resultList, false);
        }
        return CodeCompletionResult.NONE;
    }

    @Override
    public String document(ParserResult info, ElementHandle element) {
        // TODO needs to be implemented
        return null;
    }

    @Override
    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        return null;
    }

    @Override
    public String getPrefix(ParserResult info, int caretOffset, boolean upToOffset) {
        String prefix = "";
        BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(false);
        if (doc == null) {
            return null;
        }


        TokenHierarchy<Document> th = TokenHierarchy.get((Document) doc);


        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, caretOffset);

        if (ts == null) {
            return null;
        }

        ts.move(caretOffset);

        if (!ts.moveNext() && !ts.movePrevious()) {
            return null;
        }
        
        if (ts.offset() == caretOffset) {
            // We're looking at the offset to the RIGHT of the caret
            // and here I care about what's on the left
            ts.movePrevious();
        }

        Token<? extends JsTokenId> token = ts.token();

        if (token != null && token.id() != JsTokenId.EOL) {
            JsTokenId id = token.id();
            if (id == JsTokenId.STRING_END && ts.movePrevious()) {
                token = ts.token();
                id = token.id();
                if (id == JsTokenId.STRING_BEGIN) {
                    return "";
                }
            }
            if (id == JsTokenId.IDENTIFIER || id.isKeyword() || id == JsTokenId.STRING) {
                prefix = token.text().toString();
                if (upToOffset) {
                    prefix = prefix.substring(0, caretOffset - ts.offset());
                }
            }
        }
        LOGGER.log(Level.FINE, String.format("Prefix for cc: %s", prefix));
        return prefix.length() > 0 ? prefix : null;
    }

    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        return QueryType.NONE;
    }

    @Override
    public String resolveTemplateVariable(String variable, ParserResult info, int caretOffset, String name, Map parameters) {
        return null;
    }

    @Override
    public Set<String> getApplicableTemplates(Document doc, int selectionBegin, int selectionEnd) {
        return null;
    }

    @Override
    public ParameterInfo parameters(ParserResult info, int caretOffset, CompletionProposal proposal) {
        // TODO needs to be implemented.
        return ParameterInfo.NONE;
    }

    private void completeExpression(CompletionRequest request, List<CompletionProposal> resultList) {
        HashMap <String, JsElement> foundObjects = new HashMap<String, JsElement>();
        
        FileObject fo = request.info.getSnapshot().getSource().getFileObject();
        // from index
        JsIndex index = JsIndex.get(fo);
        Collection<IndexedElement> fromIndex = index.getGlobalVar(request.prefix);
        for (IndexedElement indexedElement : fromIndex) {
            JsElement object = foundObjects.get(indexedElement.getName());
            if(object == null) {
                foundObjects.put(indexedElement.getName(), indexedElement);
            } else {
                if (indexedElement.isDeclared()) {
                    if (object.isDeclared()) {
                        // put to the cc result both
                        resultList.add(JsCompletionItem.Factory.create(indexedElement, request));
                    } else {
                        // replace with the one, which is declared
                        foundObjects.put(indexedElement.getName(), indexedElement);
                    }
                } 
            }
        }
        
        // from model
        for(JsObject object : request.result.getModel().getVariables(request.anchor)) {
            if (!(object instanceof JsFunction && ((JsFunction) object).isAnonymous())
                    && startsWith(object.getName(), request.prefix)) {
                JsElement fobject = foundObjects.get(object.getName());
                if(fobject == null) {
                    foundObjects.put(object.getName(), object);
                } else {
                    if (object.isDeclared()) {
                        if (fobject.isDeclared()) {
                            // put to the cc result both
                            resultList.add(JsCompletionItem.Factory.create(object, request));
                        } else {
                            // replace with the one, which is declared
                            foundObjects.put(object.getName(), object);
                        }
                    }
                }
            }
        }
        
        for(JsElement element: foundObjects.values()) {
            resultList.add(JsCompletionItem.Factory.create(element, request));
        }
    }
    
    private void completeObjectProperty(CompletionRequest request, List<CompletionProposal> resultList) {
        TokenHierarchy<?> th = request.info.getSnapshot().getTokenHierarchy();
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(th, request.anchor);


        if (ts == null){
            return;
        }

        ts.move(request.anchor);
        if (ts.movePrevious() && ts.moveNext()) {
            if (ts.token().id() != JsTokenId.OPERATOR_DOT) {
                ts.movePrevious();
            }
            Token<? extends JsTokenId> token = ts.token();
            int parenBalancer = 0;
            boolean methodCall = false;
            List<String> exp = new ArrayList();
            
            while (token.id() != JsTokenId.WHITESPACE && token.id() != JsTokenId.OPERATOR_SEMICOLON
                    && token.id() != JsTokenId.BRACKET_RIGHT_CURLY && token.id() != JsTokenId.BRACKET_LEFT_CURLY) {
                
                if (token.id() != JsTokenId.EOL) {
                    if (token.id() != JsTokenId.OPERATOR_DOT) {
                        if (token.id() == JsTokenId.BRACKET_RIGHT_PAREN) {
                            parenBalancer++;
                            methodCall = true;
                            while (parenBalancer > 0 && ts.movePrevious()) {
                                token = ts.token();
                                if (token.id() == JsTokenId.BRACKET_RIGHT_PAREN) {
                                    parenBalancer++;
                                } else {
                                    if (token.id() == JsTokenId.BRACKET_LEFT_PAREN) {
                                        parenBalancer--;
                                    }
                                }
                            }
                        } else {
                            exp.add(token.text().toString());
                            if (!methodCall) {
                                exp.add("@pro");   // NOI18N
                            } else {
                                exp.add("@mtd");   // NOI18N
                                methodCall = false;
                            }
                        }
                    }
                }
                if (!ts.movePrevious()) {
                    break;
                }
                token = ts.token();
            }
            
            JsObject type = null;
            List lastResovled = new ArrayList();
            
            for (int i = exp.size() - 1; i > -1; i--) {
                String kind = exp.get(i);
                String name = exp.get(--i);
                if (i == (exp.size() - 2)) {
                    // resolving the first part of expression
                    for (JsObject object : request.result.getModel().getVariables(request.anchor)) {
                        if (object.getName().equals(name)) {
                            type = object;
                            break;
                        }
                    }
                    if (type == null) {
                        // try to find through index
                    } else {
                        if ("@mtd".equals(kind)) {  //NOI18N
                            if (type.getJSKind() == JsElement.Kind.METHOD
                                    || type.getJSKind() == JsElement.Kind.FUNCTION
                                    || type.getJSKind() == JsElement.Kind.CONSTRUCTOR) {
                                lastResovled.addAll(((JsFunction) type).getReturnTypes());
                            }
                        } else {
                            // just property
                            Collection<? extends Type> lastTypeAssignment = type.getAssignmentForOffset(request.anchor);

                            if (lastTypeAssignment.isEmpty()) {
                                lastResovled.add(type);
                            } else {
                                for (Type typeName : lastTypeAssignment) {
                                    for (JsObject object : request.result.getModel().getVariables(request.anchor)) {
                                        if (object.getName().equals(typeName.getType())) {
                                            lastResovled.add(object);
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                } else {
                    List newResolved = new ArrayList();
                    for (Object resolved : lastResovled) {
                        if(resolved instanceof JsObject) {
                            JsObject property = ((JsObject)resolved).getProperty(name);
                            if ("@mtd".equals(kind)) {  //NOI18N
                                if (property.getJSKind() == JsElement.Kind.METHOD
                                        || property.getJSKind() == JsElement.Kind.FUNCTION
                                        || property.getJSKind() == JsElement.Kind.CONSTRUCTOR) {
                                    newResolved.addAll(((JsFunction) property).getReturnTypes());
                                }
                            } else {
                                newResolved.add(property);
                            }
                        } else {
                            FileObject fo = request.info.getSnapshot().getSource().getFileObject();
                            Collection<? extends IndexResult> indexResults = JsIndex.get(fo).findFQN(((TypeUsage)resolved).getType() + "." +  name);
                            for (IndexResult indexResult : indexResults) {
                                JsElement.Kind jsKind = JsElement.Kind.fromId(Integer.parseInt(indexResult.getValue(JsIndex.FIELD_JS_KIND)));
                                if ("@mtd".equals(kind) && (jsKind == JsElement.Kind.METHOD // NOI18N
                                        || jsKind == JsElement.Kind.FUNCTION
                                        || jsKind == JsElement.Kind.CONSTRUCTOR)) {
                                    newResolved.addAll(IndexedElement.getReturnTypes(indexResult));
                                } else {
                                    
                                }
                            }
                        }
                    }
                    lastResovled = newResolved;
                }
            }
            
            for (Object resolved : lastResovled) {
                if(resolved instanceof JsObject) {
                    addObjectPropertiesToCC((JsObject)resolved, request, resultList);
                } else {
                    TypeUsage typeUsage = (TypeUsage)resolved;
                    // at first try to find the type in the model
                    JsObject jsObject = ModelUtils.findJsObjectByName(request.result.getModel(), typeUsage.getType());
                    if (jsObject != null) {
                        addObjectPropertiesToCC(jsObject, request, resultList);
                    } else {
                        // look at the index
                        FileObject fo = request.info.getSnapshot().getSource().getFileObject();
                        Collection<IndexedElement> properties = JsIndex.get(fo).getProperties(((TypeUsage)resolved).getType()); 
                        for(IndexedElement indexedElement : properties) {
                            if (startsWith(indexedElement.getName(), request.prefix)) {
                                resultList.add(JsCompletionItem.Factory.create(indexedElement, request));
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void completeObjectMember(CompletionRequest request, List<CompletionProposal> resultList) {
        JsParserResult result = (JsParserResult)request.info;
        JsObject jsObject = (JsObject)ModelUtils.getDeclarationScope(result.getModel(), request.anchor);
        HashMap<String, JsElement> properties = new HashMap<String, JsElement>();
        
        if (jsObject.getJSKind() == JsElement.Kind.METHOD) {
            jsObject = jsObject.getParent();
        }
        if (jsObject.getJSKind() == JsElement.Kind.OBJECT) {
            for (JsObject property : jsObject.getProperties().values()) {
                if(startsWith(property.getName(), request.prefix)) {
                    JsElement element = properties.get(property.getName());
                    if(element == null || (!element.isDeclared() && property.isDeclared())) {
                        properties.put(property.getName(), property);
                    }
                }
            }
        }
        
        String fqn = ModelUtils.createFQN(jsObject);
        
        FileObject fo = request.info.getSnapshot().getSource().getFileObject();
        Collection<IndexedElement> indexedProperties = JsIndex.get(fo).getProperties(fqn);
        for (IndexedElement indexedElement : indexedProperties) {
            if (startsWith(indexedElement.getName(), request.prefix)) {
                JsElement element = properties.get(indexedElement.getName());
                if (element == null || (!element.isDeclared() && indexedElement.isDeclared())) {
                    properties.put(indexedElement.getName(), indexedElement);
                }
            }
        }
        
        for (JsElement element : properties.values()) {
            if (element instanceof JsObject)
                resultList.add(JsCompletionItem.Factory.create((JsObject)element, request));
            else if (element instanceof IndexedElement){
                resultList.add(JsCompletionItem.Factory.create((IndexedElement)element, request));
            }
        }
    }

    private void completeKeywords(CompletionRequest request, List<CompletionProposal> resultList) {
        for (String keyword : JsKeyWords.KEYWORDS.keySet()) {
            if (startsWith(keyword, request.prefix)) {
                resultList.add(new JsCompletionItem.KeywordItem(keyword, request));
            }
        }
    }
    
    private boolean startsWith(String theString, String prefix) {
        if (prefix.length() == 0) {
            return true;
        }

        return caseSensitive ? theString.startsWith(prefix)
                : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }
    
    private void addObjectPropertiesToCC(JsObject jsObject, CompletionRequest request, List<CompletionProposal> resultList) {
        boolean filter = true;
        if (request.prefix == null || request.prefix.isEmpty()) {
            filter = false;
        }
        for (JsObject property : jsObject.getProperties().values()) {
            if (!(property instanceof JsFunction && ((JsFunction) property).isAnonymous())
                    && (!filter || startsWith(property.getName(), request.prefix))) {
                resultList.add(JsCompletionItem.Factory.create(property, request));
            }
        }
    }
}
