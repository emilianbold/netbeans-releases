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
import org.netbeans.modules.javascript2.editor.doc.JsDocumentationCodeCompletion;
import org.netbeans.modules.javascript2.editor.doc.JsDocumentationElement;
import org.netbeans.modules.javascript2.editor.index.IndexedElement;
import org.netbeans.modules.javascript2.editor.index.JsIndex;
import org.netbeans.modules.javascript2.editor.jquery.JQueryCodeCompletion;
import org.netbeans.modules.javascript2.editor.jquery.JQueryModel;
import org.netbeans.modules.javascript2.editor.lexer.JsDocumentationTokenId;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.model.*;
import org.netbeans.modules.javascript2.editor.model.impl.*;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
class JsCodeCompletion implements CodeCompletionHandler {
    private static final Logger LOGGER = Logger.getLogger(JsCodeCompletion.class.getName());

    private boolean caseSensitive;
    private final JQueryCodeCompletion jqueryCC = new JQueryCodeCompletion();
    
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
        FileObject fileObject = ccContext.getParserResult().getSnapshot().getSource().getFileObject();
        JsParserResult jsParserResult = (JsParserResult)info;
        CompletionContext context = CompletionContextFinder.findCompletionContext(info, caretOffset);
        
        LOGGER.log(Level.FINE, String.format("CC context: %s", context.toString()));
        
        JsCompletionItem.CompletionRequest request = new JsCompletionItem.CompletionRequest();
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
        if (ccContext.getQueryType() == QueryType.ALL_COMPLETION) {
            switch (context) {
                case GLOBAL:
                    Collection<IndexedElement> fromIndex = JsIndex.get(fileObject).getGlobalVar(request.prefix);
                    HashMap<String, JsElement> addedGlobal = new HashMap<String, JsElement>();
                    for (IndexedElement indexElement : fromIndex) {
                        JsElement element = addedGlobal.get(indexElement.getName());
                        if (element == null) {
                            if (indexElement.isDeclared()) {
                                resultList.add(JsCompletionItem.Factory.create(indexElement, request));
                            }
                            addedGlobal.put(indexElement.getName(), indexElement);
                        } else if (!element.isDeclared() && indexElement.isDeclared()) {
                            resultList.add(JsCompletionItem.Factory.create(indexElement, request));
                            addedGlobal.put(indexElement.getName(), indexElement);
                        }
                    }
                    break;    
                case EXPRESSION:    
                case OBJECT_MEMBERS:    
                case OBJECT_PROPERTY:
                    Collection<? extends IndexResult> indexResults = JsIndex.get(fileObject).query(JsIndex.FIELD_BASE_NAME, request.prefix, QuerySupport.Kind.PREFIX, JsIndex.TERMS_BASIC_INFO);
                    HashMap<String, JsElement> all = new HashMap<String, JsElement>();
                    for (IndexResult indexResult : indexResults) {
                        IndexedElement indexElement = IndexedElement.create(indexResult);
                        JsElement element = all.get(indexElement.getName());
                        if (element == null) {
                            if (indexElement.isDeclared()) {
                                resultList.add(JsCompletionItem.Factory.create(indexElement, request));
                            }
                            all.put(indexElement.getName(), indexElement);
                        } else if (!element.isDeclared() && indexElement.isDeclared()) {
                            resultList.add(JsCompletionItem.Factory.create(indexElement, request));
                            all.put(indexElement.getName(), indexElement);
                        }
                    }
                    break;
            }
        } else {
            switch (context) {
                case GLOBAL:
                    HashMap<String, JsElement> addedProperties = new HashMap<String, JsElement>();
                    for (JsObject libGlobal : getLibrariesGlobalObjects()) {
                        for (JsObject object : libGlobal.getProperties().values()) {
                            if (startsWith(object.getName(), request.prefix)) {
                                if (object.isDeclared()) {
                                    resultList.add(JsCompletionItem.Factory.create(object, request));
                                }
                                addedProperties.put(object.getName(), object);
                            }
                        }
                    }
                    for (JsObject object : request.result.getModel().getVariables(caretOffset)) {
                        if (!(object instanceof JsFunction && ((JsFunction) object).isAnonymous())
                                && startsWith(object.getName(), request.prefix)) {
                            JsElement element = addedProperties.get(object.getName());
                            if (element == null) {
                                if (object.isDeclared()) {
                                    resultList.add(JsCompletionItem.Factory.create(object, request));
                                }
                                addedProperties.put(object.getName(), object);
                            } else if (!element.isDeclared() && object.isDeclared()) {
                                resultList.add(JsCompletionItem.Factory.create(object, request));
                                addedProperties.put(object.getName(), object);
                            }

                        }
                    }
                    completeKeywords(request, resultList);
                    JsIndex jsIndex = JsIndex.get(fileObject);
                    Collection<IndexedElement> fromIndex = jsIndex.getGlobalVar(request.prefix);
                    //  enhance results for all window properties - see issue #218412, #215863, #218122, ...
                    fromIndex.addAll(jsIndex.getPropertiesWithPrefix("window", request.prefix)); //NOI18N
                    for (IndexedElement indexElement : fromIndex) {
                        if (startsWith(indexElement.getName(), request.prefix)) {
                            JsElement element = addedProperties.get(indexElement.getName());
                            if (element == null) {
                                if (indexElement.isDeclared()) {
                                    resultList.add(JsCompletionItem.Factory.create(indexElement, request));
                                }
                                addedProperties.put(indexElement.getName(), indexElement);
                            } else if (!element.isDeclared() && indexElement.isDeclared()) {
                                resultList.add(JsCompletionItem.Factory.create(indexElement, request));
                                addedProperties.put(indexElement.getName(), indexElement);
                            }
                        }
                    }

                    for (JsElement element : addedProperties.values()) {
                        if (!element.isDeclared()) {
                            resultList.add(JsCompletionItem.Factory.create(element, request));
                        }
                    }
                    break;
                case EXPRESSION:
                    completeExpression(request, resultList);
                    break;
                case OBJECT_PROPERTY:
                    completeObjectProperty(request, resultList);
                    break;
                case OBJECT_MEMBERS:
                    completeObjectMember(request, resultList);
                    break;
                case DOCUMENTATION:
                    JsDocumentationCodeCompletion.complete(request, resultList);
                    break;
                default:
                    break;
            }
        }
        
        long end = System.currentTimeMillis();
        LOGGER.log(Level.FINE, "Counting JS CC took {0}ms ",  (end - start));
        resultList.addAll(jqueryCC.complete(ccContext, context, pref));
        if (!resultList.isEmpty()) {
            return new DefaultCompletionResult(resultList, false);
        }
        return CodeCompletionResult.NONE;
    }

    @Override
    public String document(ParserResult info, ElementHandle element) {
        final StringBuilder documentation = new StringBuilder();
        if(element instanceof IndexedElement) {
            final IndexedElement indexedElement = (IndexedElement)element;
            FileObject nextFo = indexedElement.getFileObject();
            
            try {
                ParserManager.parse(Collections.singleton(Source.create(nextFo)), new UserTask () {

                    @Override
                    public void run(ResultIterator resultIterator) throws Exception {
                        Result parserResult = resultIterator.getParserResult();
                        if (parserResult instanceof JsParserResult) {
                            JsParserResult jsInfo = (JsParserResult)parserResult;
                            
                            String fqn = indexedElement.getFQN();
                            JsObject jsObjectGlobal  = jsInfo.getModel().getGlobalObject();
                            JsObject property = ModelUtils.findJsObjectByName(jsObjectGlobal, fqn);
                            if (property != null) {
                                String doc = property.getDocumentation();
                                if (doc != null && !doc.isEmpty()) {
                                    documentation.append(doc);
                                }
                            }
                            
                        }
                        LOGGER.log(Level.INFO, "Not instance of JsParserResult: {0}", parserResult);
                    }
                    
                });
            } catch (ParseException ex) {
                LOGGER.log(Level.WARNING, null, ex);
            }
        } else if (element instanceof JsObject) {
            JsObject jsObject = (JsObject) element;
            if (jsObject.getDocumentation() != null) {
                documentation.append(jsObject.getDocumentation());
            }
        }
        if (documentation.length() == 0) {
            String doc = jqueryCC.getHelpDocumentation(info, element);
            if (doc != null && !doc.isEmpty()) {
                documentation.append(doc);
            }
        }
        if (element instanceof JsDocumentationElement) {
            return ((JsDocumentationElement) element).getDocumentation();
        }
        if (documentation.length() == 0) {
            documentation.append(NbBundle.getMessage(JsCodeCompletion.class, "MSG_DocNotAvailable"));
        }
        return documentation.toString();
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

        caretOffset = info.getSnapshot().getEmbeddedOffset(caretOffset);
        TokenSequence<? extends JsTokenId> ts = LexUtilities.getJsTokenSequence(info.getSnapshot(), caretOffset);
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
                if (ts.token().id() == JsTokenId.STRING_BEGIN) {
                    return "";
                } else {
                    ts.moveNext();
                }
            }
            if (id == JsTokenId.IDENTIFIER || id.isKeyword() || id == JsTokenId.STRING) {
                prefix = token.text().toString();
                if (upToOffset) {
                    prefix = prefix.substring(0, caretOffset - ts.offset());
                }
            }
            if (id == JsTokenId.DOC_COMMENT) {
                TokenSequence<? extends JsDocumentationTokenId> docTokenSeq =
                        LexUtilities.getJsDocumentationTokenSequence(info.getSnapshot(), caretOffset);
                if (docTokenSeq == null) {
                    return null;
                }

                docTokenSeq.move(caretOffset);
                // initialize moved token
                if (!docTokenSeq.moveNext() && !docTokenSeq.movePrevious()) {
                    return null;
                }

                if (docTokenSeq.token().id() == JsDocumentationTokenId.KEYWORD) {
                    // inside the keyword tag
                    prefix = docTokenSeq.token().text().toString();
                    if (upToOffset) {
                        prefix = prefix.substring(0, caretOffset - docTokenSeq.offset());
                    }
                } else {
                    // get the token before
                    docTokenSeq.movePrevious();
                    prefix = docTokenSeq.token().text().toString();
                }
            }
            if (id.isError()) {
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
        // must return null - CSL reasons, see #217101 for more information
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
        fromIndex.addAll(index.getPropertiesWithPrefix("window", request.prefix));  //NOI18N
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
        
        // from libraries
        for (JsObject libGlobal : getLibrariesGlobalObjects()) {
            for (JsObject object : libGlobal.getProperties().values()) {
                if (startsWith(object.getName(), request.prefix)) {
                    foundObjects.put(object.getName(), object);
                }
            }
        }
        
        // from model
        //int offset = request.info.getSnapshot().getEmbeddedOffset(request.anchor);
        for(JsObject object : request.result.getModel().getVariables(request.anchor)) {
            if (!(object instanceof JsFunction && ((JsFunction) object).isAnonymous())
                    && startsWith(object.getName(), request.prefix)) {
                JsElement fobject = foundObjects.get(object.getName());
                if(fobject == null) {
                    if (!(object.getName().equals(request.prefix)
                            && object.getDeclarationName().getOffsetRange().getStart() == request.anchor)) {
                        foundObjects.put(object.getName(), object);
                    }
                } else {
                    if (object.isDeclared()) {
//                        if (fobject.isDeclared()) {
                            // put to the cc result both
//                            resultList.add(JsCompletionItem.Factory.create(object, request));
//                        } else {
                            // replace with the one, which is declared
                            foundObjects.put(object.getName(), object);
//                        }
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

        int offset = request.info.getSnapshot().getEmbeddedOffset(request.anchor);
        ts.move(offset);
        if (ts.movePrevious() && (ts.moveNext() || ((ts.offset() + ts.token().length()) == request.result.getSnapshot().getText().length()))) {
            if (ts.token().id() != JsTokenId.OPERATOR_DOT) {
                ts.movePrevious();
            }
            Token<? extends JsTokenId> token = ts.token();
            int parenBalancer = 0;
            boolean methodCall = false;
            boolean wasLastDot = false;
            List<String> exp = new ArrayList();
            
            while (token.id() != JsTokenId.WHITESPACE && token.id() != JsTokenId.OPERATOR_SEMICOLON
                    && token.id() != JsTokenId.BRACKET_RIGHT_CURLY && token.id() != JsTokenId.BRACKET_LEFT_CURLY
                    && token.id() != JsTokenId.BRACKET_LEFT_PAREN
                    && token.id() != JsTokenId.BLOCK_COMMENT
                    && token.id() != JsTokenId.LINE_COMMENT
                    && token.id() != JsTokenId.OPERATOR_ASSIGNMENT
                    && token.id() != JsTokenId.OPERATOR_PLUS) {

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
                            wasLastDot = false;
                        }
                    } else {
                        wasLastDot = true;
                    }
                } else {
                    if (!wasLastDot && ts.movePrevious()) {
                        // check whether it's continuatino of previous line
                        token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE, JsTokenId.BLOCK_COMMENT, JsTokenId.LINE_COMMENT));
                        if (token.id() != JsTokenId.OPERATOR_DOT) {
                            // the dot was not found => it's not continuation of expression
                            break;
                        }
                    }
                }
                if (!ts.movePrevious()) {
                    break;
                }
                token = ts.token();
            }
            
            FileObject fo = request.info.getSnapshot().getSource().getFileObject();
            JsIndex jsIndex = JsIndex.get(fo);
            Collection<TypeUsage> resolveTypeFromExpression = new ArrayList<TypeUsage>();
            resolveTypeFromExpression.addAll(ModelUtils.resolveTypeFromExpression(request.result.getModel(), jsIndex, exp, offset));
            
            int cycle = 0;
            boolean resolvedAll = false;
            while(!resolvedAll && cycle < 10) {
                cycle++;
                resolvedAll = true;
                Collection<TypeUsage> resolved = new ArrayList<TypeUsage>();
                for (TypeUsage typeUsage : resolveTypeFromExpression) {
                    if(!((TypeUsageImpl)typeUsage).isResolved()) {
                        resolvedAll = false;
                        String sexp = typeUsage.getType();
                        if (sexp.startsWith("@exp;")) {
                            sexp = sexp.substring(5);
                            List<String> nExp = new ArrayList<String>();
                            String[] split = sexp.split("@call;");
                            for (int i = split.length - 1; i > -1; i--) {
                                String string = split[i];
                                nExp.add(split[i]);
                                if (i == 0) {
                                    nExp.add("@pro");
                                } else {
                                    nExp.add("@mtd");
                                }
                            }
                            resolved.addAll(ModelUtils.resolveTypeFromExpression(request.result.getModel(), jsIndex, nExp, cycle));
                        } else {
                            resolved.add(new TypeUsageImpl(typeUsage.getType(), typeUsage.getOffset(), true));
                        }
                    } else {
                        resolved.add(typeUsage);
                    }
                }
                resolveTypeFromExpression.clear();
                resolveTypeFromExpression.addAll(resolved);
            }
            Collection<String> prototypeChain = new ArrayList<String>();
            for (TypeUsage typeUsage : resolveTypeFromExpression) {
                prototypeChain.addAll(ModelUtils.findPrototypeChain(typeUsage.getType(), jsIndex));
            }
            
            for (String string : prototypeChain) {
                resolveTypeFromExpression.add(new TypeUsageImpl(string));
            }
            
            HashMap<String, JsElement> addedProperties = new HashMap<String, JsElement>();
            boolean isFunction = false; // addding Function to the prototype chain?
            List<JsObject> lastResolvedObjects = new ArrayList<JsObject>();
            for (TypeUsage typeUsage : resolveTypeFromExpression) {
                // at first try to find the type in the model
                JsObject jsObject = ModelUtils.findJsObjectByName(request.result.getModel(), typeUsage.getType());
                if (jsObject != null) {
                    lastResolvedObjects.add(jsObject);
                }
                //if (jsObject == null) {
                    for (JsObject libGlobal : getLibrariesGlobalObjects()) {
                        for (JsObject object : libGlobal.getProperties().values()) {
                            if (object.getName().equals(typeUsage.getType())) {
                                jsObject = object;
                                lastResolvedObjects.add(jsObject);
                                break;
                            }
                        }
                        if (jsObject != null) {
                            break;
                        }
                    }
                //}
                if(jsObject == null || !jsObject.isDeclared()){
                    // look at the index
//                    if (exp.get(1).equals("@pro")) {
                    boolean isObject = typeUsage.getType().equals("Object");
                    if(exp.get(1).equals("@pro") && !isObject) {
                        for(IndexResult indexResult : jsIndex.findFQN(typeUsage.getType())){
                            JsElement.Kind kind = IndexedElement.Flag.getJsKind(Integer.parseInt(indexResult.getValue(JsIndex.FIELD_FLAG)));
                            if (kind.isFunction()) {
                                isFunction = true;
                            }
                        }
                    }
//                    }
                    if (!isObject) {
                        addObjectPropertiesFromIndex(typeUsage.getType(), jsIndex, request, addedProperties);
                    }
                }
            }
            for (JsObject resolved : lastResolvedObjects) {
                if(!isFunction && resolved.getJSKind().isFunction()) {
                    isFunction = true;
                }
                addObjectPropertiesToCC(resolved, request, addedProperties);
                if (!resolved.isDeclared()) {
                    // if the object is not defined here, look to the index as well
                    addObjectPropertiesFromIndex(ModelUtils.createFQN(resolved), jsIndex, request, addedProperties);
                }
            }
            
            if (isFunction) {
                addObjectPropertiesFromIndex("Function", jsIndex, request, addedProperties); //NOI18N
            }
            
            addObjectPropertiesFromIndex("Object", jsIndex, request, addedProperties); //NOI18N
                        
            // now look to the index again for declared item outside
            StringBuilder fqn = new StringBuilder();
            for (int i = exp.size() - 1; i > -1; i--) {
                fqn.append(exp.get(--i));
                fqn.append('.');
            }
            fqn.append(request.prefix);
            Collection<? extends IndexResult> indexResults = jsIndex.query(JsIndex.FIELD_FQ_NAME, fqn.toString(), QuerySupport.Kind.PREFIX, JsIndex.TERMS_BASIC_INFO);
            for (IndexResult indexResult : indexResults) {
                IndexedElement indexedElement = IndexedElement.create(indexResult);
                JsElement element = addedProperties.get(indexedElement.getName());
                if (startsWith(indexedElement.getName(), request.prefix)
                        && !indexedElement.isAnonymous()
                        && indexedElement.getFQN().indexOf('.', fqn.length()) == -1 
                        && indexedElement.getModifiers().contains(Modifier.PUBLIC)
                        && (element == null || (!element.isDeclared() && indexedElement.isDeclared()))) {
                    addedProperties.put(indexedElement.getName(), indexedElement);
                }
            }
            
            // create code completion results
            for (JsElement element : addedProperties.values()) {
                resultList.add(JsCompletionItem.Factory.create(element, request));
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
        
        completeObjectMembers(jsObject, request, properties);
        
        if (jsObject.getName().equals("prototype")) {  //NOI18N
            completeObjectMembers(jsObject.getParent(), request, properties);
        }
        
        for (JsElement element : properties.values()) {
            if (element instanceof JsObject)
                resultList.add(JsCompletionItem.Factory.create((JsObject)element, request));
            else if (element instanceof IndexedElement){
                resultList.add(JsCompletionItem.Factory.create((IndexedElement)element, request));
            }
        }
    }
    
    private void completeObjectMembers(JsObject jsObject, CompletionRequest request, HashMap<String, JsElement> properties) {
        if (jsObject.getJSKind() == JsElement.Kind.OBJECT || jsObject.getJSKind() == JsElement.Kind.CONSTRUCTOR) {
            for (JsObject property : jsObject.getProperties().values()) {
                if(!property.getModifiers().contains(Modifier.PRIVATE)
                        && startsWith(property.getName(), request.prefix)) {
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
    
    private void addObjectPropertiesToCC(JsObject jsObject, CompletionRequest request, Map<String, JsElement> addedProperties) {
        boolean filter = true;
        if (request.prefix == null || request.prefix.isEmpty()) {
            filter = false;
        }
        JsObject prototype = jsObject.getProperty("prototype"); // NOI18N
        if (prototype != null) {
            // at first add all prototype properties
            // if the same property is declared in the project directly, then this is replaced.
            addObjectPropertiesToCC(prototype, request, addedProperties);
        }
        for (JsObject property : jsObject.getProperties().values()) {
            String propertyName = property.getName();
            if (!(property instanceof JsFunction && ((JsFunction) property).isAnonymous())
                    && !property.getModifiers().contains(Modifier.PRIVATE)
                    && (!filter || startsWith(propertyName, request.prefix))
                    && !property.getJSKind().isPropertyGetterSetter()) {
                JsElement element = addedProperties.get(propertyName);
                if (element == null || (!element.isDeclared() && jsObject.isDeclared())) {
                    addedProperties.put(propertyName, property);
                }
            }
        }
    }
    
    private void addObjectPropertiesFromIndex(String fqn, JsIndex jsIndex, CompletionRequest request, Map<String, JsElement> addedProperties) {
        Collection<IndexedElement> properties = jsIndex.getProperties(fqn);
        String prototypeFQN = null;
        for (IndexedElement indexedElement : properties) {
            JsElement element = addedProperties.get(indexedElement.getName());
            if (startsWith(indexedElement.getName(), request.prefix)
                    && (element == null || (!element.isDeclared() && indexedElement.isDeclared()))) {
                addedProperties.put(indexedElement.getName(), indexedElement);
            }
            if ("prototype".equals(indexedElement.getName())) {
                prototypeFQN = indexedElement.getFQN();
            }
        }
        if (prototypeFQN != null) {
            properties = jsIndex.getProperties(prototypeFQN);
            for (IndexedElement indexedElement : properties) {
                JsElement element = addedProperties.get(indexedElement.getName());
                if (startsWith(indexedElement.getName(), request.prefix)
                        && (element == null || (!element.isDeclared() && indexedElement.isDeclared()))) {
                    addedProperties.put(indexedElement.getName(), indexedElement);
                }
            }
        }
    }
                 
    private Collection<JsObject> getLibrariesGlobalObjects() {
        Collection<JsObject> result = new ArrayList<JsObject>();
        JsObject libGlobal = JQueryModel.getGlobalObject();
        if (libGlobal != null) {
            result.add(libGlobal);
        }
        return result;
    }
}
