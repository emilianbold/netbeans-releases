/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CodeCompletionContext;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.CodeCompletionHandler;
import org.netbeans.modules.gsf.api.CodeCompletionResult;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.ParameterInfo;
import org.netbeans.modules.gsf.api.ParserResult;
import org.netbeans.modules.gsf.api.SourceModel;
import org.netbeans.modules.gsf.api.SourceModelFactory;
import org.netbeans.modules.php.editor.index.IndexedClass;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.IndexedElement;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.PHPDOCTagElement;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.api.Utils;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ArrayCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Assignment;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Comment;
import org.netbeans.modules.php.editor.parser.astnodes.DoStatement;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.ExpressionStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForEachStatement;
import org.netbeans.modules.php.editor.parser.astnodes.ForStatement;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.IfStatement;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocBlock;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTag;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.WhileStatement;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPCodeCompletion implements CodeCompletionHandler {
    private static final List<PHPTokenId[]> CLASS_NAME_TOKENCHAINS = Arrays.asList(
        new PHPTokenId[]{PHPTokenId.PHP_NEW},
        new PHPTokenId[]{PHPTokenId.PHP_NEW, PHPTokenId.WHITESPACE},
        new PHPTokenId[]{PHPTokenId.PHP_NEW, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING},
        new PHPTokenId[]{PHPTokenId.PHP_EXTENDS},
        new PHPTokenId[]{PHPTokenId.PHP_EXTENDS, PHPTokenId.WHITESPACE},
        new PHPTokenId[]{PHPTokenId.PHP_EXTENDS, PHPTokenId.WHITESPACE, PHPTokenId.PHP_STRING}
        );
    
    private static final List<PHPTokenId[]> CLASS_MEMBER_TOKENCHAINS = Arrays.asList(
        new PHPTokenId[]{PHPTokenId.PHP_OBJECT_OPERATOR},
        new PHPTokenId[]{PHPTokenId.PHP_OBJECT_OPERATOR, PHPTokenId.PHP_STRING},
        new PHPTokenId[]{PHPTokenId.PHP_OBJECT_OPERATOR, PHPTokenId.PHP_VARIABLE},
        new PHPTokenId[]{PHPTokenId.PHP_OBJECT_OPERATOR, PHPTokenId.PHP_TOKEN}
        );
    
    private static final List<PHPTokenId[]> STATIC_CLASS_MEMBER_TOKENCHAINS = Arrays.asList(
        new PHPTokenId[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM},
        new PHPTokenId[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.PHP_STRING},
        new PHPTokenId[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.PHP_VARIABLE},
        new PHPTokenId[]{PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM, PHPTokenId.PHP_TOKEN}
        );
    
    private static final List<PHPTokenId[]> COMMENT_TOKENCHAINS = Arrays.asList(
            new PHPTokenId[]{PHPTokenId.PHP_COMMENT_START},
            new PHPTokenId[]{PHPTokenId.PHP_COMMENT},
            new PHPTokenId[]{PHPTokenId.PHP_LINE_COMMENT}
            );
    
    private static final List<PHPTokenId[]> PHPDOC_TOKENCHAINS = Arrays.asList(
            new PHPTokenId[]{PHPTokenId.PHPDOC_COMMENT_START},
            new PHPTokenId[]{PHPTokenId.PHPDOC_COMMENT}
            );
    
    private static enum CompletionContext {EXPRESSION, HTML, CLASS_NAME, STRING,
        CLASS_MEMBER, STATIC_CLASS_MEMBER, PHPDOC, NONE};

    private final static String[] PHP_KEYWORDS = {"__FILE__", "exception",
        "__LINE__", "array()", "class", "const", "continue", "die()", "echo()", "empty()", "endif",
        "eval()", "exit()", "for", "foreach", "function", "global", "if",
        "include()", "include_once()", "isset()", "list()", "new",
        "print()", "require()", "require_once()", "return()", "static",
        "switch", "unset()", "use", "var", "while",
        "__FUNCTION__", "__CLASS__", "__METHOD__", "final", "php_user_filter",
        "interface", "implements", "extends", "public", "private",
        "protected", "abstract", "clone", "try", "catch", "throw"
    };

    private final static String[] PHP_CLASS_KEYWORDS = {
        "$this->", "self::", "parent::"
    };
    
    private boolean caseSensitive;
    private NameKind nameKind;
    
    private static CompletionContext findCompletionContext(CompilationInfo info, int caretOffset){
        Document document = info.getDocument();
        if (document == null) {
            return CompletionContext.NONE;
        }
        TokenHierarchy th = TokenHierarchy.get(document);
        TokenSequence<PHPTokenId> tokenSequence = th.tokenSequence();
        tokenSequence.move(caretOffset);
        if (!tokenSequence.moveNext()){
            return CompletionContext.NONE;
        }

        switch (tokenSequence.token().id()){
            case T_INLINE_HTML:
                return CompletionContext.HTML;
            case PHP_CONSTANT_ENCAPSED_STRING:
                if (tokenSequence.token().text().charAt(0) == '"') {
                    return CompletionContext.STRING;
                } else {
                    return CompletionContext.NONE;
                }
            default:
        }

        if (acceptTokenChains(tokenSequence, CLASS_NAME_TOKENCHAINS)){
            return CompletionContext.CLASS_NAME;

        } else if (acceptTokenChains(tokenSequence, CLASS_MEMBER_TOKENCHAINS)){
            return CompletionContext.CLASS_MEMBER;

        } else if (acceptTokenChains(tokenSequence, STATIC_CLASS_MEMBER_TOKENCHAINS)){
            return CompletionContext.STATIC_CLASS_MEMBER;
        } else if (acceptTokenChains(tokenSequence, COMMENT_TOKENCHAINS)){
            return CompletionContext.NONE;
        } else if (acceptTokenChains(tokenSequence, PHPDOC_TOKENCHAINS)){
            return CompletionContext.PHPDOC;
        }
        
        return CompletionContext.EXPRESSION;
    }
    
    private static boolean acceptTokenChains(TokenSequence tokenSequence, List<PHPTokenId[]> tokenIdChains){
        int maxLen = 0;
        
        for (PHPTokenId tokenIds[] : tokenIdChains){
            if (maxLen < tokenIds.length){
                maxLen = tokenIds.length;
            }
        }
        
        Token preceedingTokens[] = getPreceedingTokens(tokenSequence, maxLen);
        
        chain_search:
        for (PHPTokenId tokenIds[] : tokenIdChains){
            
            int startWithinPrefix = preceedingTokens.length - tokenIds.length;
            
            if (startWithinPrefix >= 0){
                for (int i = 0; i < tokenIds.length; i ++){
                    if (tokenIds[i] != preceedingTokens[i + startWithinPrefix].id()){
                        continue chain_search;
                    }
                }
                
                return true;
            }
        }
        
        return false;
    }
    
    private static Token[] getPreceedingTokens(TokenSequence tokenSequence, int maxNumberOfTokens){
        int orgOffset = tokenSequence.offset();
        LinkedList<Token> tokens = new LinkedList<Token>();
        
        for (int i = 0; i < maxNumberOfTokens; i++) {
            if (!tokenSequence.movePrevious()){
                break;
            }
            
            tokens.addFirst(tokenSequence.token());
        }
        
        tokenSequence.move(orgOffset);
        tokenSequence.moveNext();
        return tokens.toArray(new Token[tokens.size()]);
    }

    public CodeCompletionResult complete(CodeCompletionContext completionContext) {
        CompilationInfo info = completionContext.getInfo();
        int caretOffset = completionContext.getCaretOffset();
        String prefix = completionContext.getPrefix();
        this.caseSensitive = completionContext.isCaseSensitive();
        HtmlFormatter formatter = completionContext.getFormatter();
        this.nameKind = caseSensitive ? NameKind.PREFIX : NameKind.CASE_INSENSITIVE_PREFIX;
        
        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();

        PHPParseResult result = (PHPParseResult) info.getEmbeddedResult(PHPLanguage.PHP_MIME_TYPE, caretOffset);
        
        if (result.getProgram() == null){
            return CodeCompletionResult.NONE;
        }
        
        CompletionContext context = findCompletionContext(info, caretOffset);
        
        if (context == CompletionContext.NONE){
            return CodeCompletionResult.NONE;
        }
        
        PHPCompletionItem.CompletionRequest request = new PHPCompletionItem.CompletionRequest();
        request.anchor = caretOffset - prefix.length();
        request.formatter = formatter;
        request.result = result;
        request.info = info;
        request.prefix = prefix;
        request.index = PHPIndex.get(request.info.getIndex(PHPLanguage.PHP_MIME_TYPE));
        
        try {
            request.currentlyEditedFileURL = result.getFile().getFileObject().getURL().toString();
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        
        switch(context){
            case EXPRESSION:
                autoCompleteExpression(proposals, request);
                break;
            case HTML:
                proposals.add(new PHPCompletionItem.KeywordItem("<?php", request)); //NOI18N
                proposals.add(new PHPCompletionItem.KeywordItem("<?=", request)); //NOI18N
                break;
            case CLASS_NAME:
                autoCompleteClassNames(proposals, request);
                break;
            case STRING:
                // LOCAL VARIABLES
                proposals.addAll(getLocalVariableProposals(request.result.getProgram().getStatements(), request));
                break;
            case CLASS_MEMBER:
                autoCompleteClassMembers(proposals, request, false);
                break;
            case STATIC_CLASS_MEMBER:
                autoCompleteClassMembers(proposals, request, true);
                break;
            case PHPDOC:
                PHPDOCCodeCompletion.complete(proposals, request);
                break;
        }
        
        return new PHPCompletionResult(completionContext, proposals);
    }
    
    private void autoCompleteClassNames(List<CompletionProposal> proposals, PHPCompletionItem.CompletionRequest request) {
        for (IndexedClass clazz : request.index.getClasses(request.result, request.prefix, nameKind)) {
            proposals.add(new PHPCompletionItem.ClassItem(clazz, request));
        }
    }
    
    private void autoCompleteClassMembers(List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request, boolean staticContext) {
        Document document = request.info.getDocument();
        if (document == null) {
            return;
        }

        TokenHierarchy th = TokenHierarchy.get(document);
        TokenSequence<PHPTokenId> tokenSequence = th.tokenSequence();
        tokenSequence.move(request.anchor);
        if (tokenSequence.movePrevious())
        {
            boolean instanceContext = !staticContext;
            boolean includeInherited = true;
            boolean moreTokens = true;
            int attrMask = Modifier.PUBLIC;

            if (tokenSequence.token().id() == PHPTokenId.WHITESPACE) {
                moreTokens = tokenSequence.movePrevious();
            }

            moreTokens = tokenSequence.movePrevious();

            String varName = tokenSequence.token().text().toString();
            String typeName = null;
            boolean completeDollarPrefix = true;

            if (varName.equals("self")) { //NOI18N
                ClassDeclaration classDecl = findEnclosingClass(request.info, request.anchor);
                if (classDecl != null) {
                    typeName = classDecl.getName().getName();
                    staticContext = instanceContext = true;
                    includeInherited = false;
                    attrMask |= (Modifier.PROTECTED | Modifier.PRIVATE);
                }
            } else if (varName.equals("parent")) { //NOI18N
                ClassDeclaration classDecl = findEnclosingClass(request.info, request.anchor);
                if (classDecl != null) {
                    Identifier superIdentifier = classDecl.getSuperClass();
                    if (superIdentifier != null) {
                        typeName = superIdentifier.getName();
                        staticContext = instanceContext = true;
                        attrMask |= Modifier.PROTECTED;
                    }
                }
            } else if (varName.equals("$this")) { //NOI18N
                ClassDeclaration classDecl = findEnclosingClass(request.info, request.anchor);
                if (classDecl != null) {
                    typeName = classDecl.getName().getName();
                    staticContext = false;
                    instanceContext = true;
                    completeDollarPrefix = false;
                    attrMask |= (Modifier.PROTECTED | Modifier.PRIVATE);
                }
            } else {
                if (staticContext) {
                    typeName = varName;
                } else {
                    Collection<IndexedConstant> localVars = getLocalVariables(request.result.getProgram().getStatements(), varName, request.anchor, null);

                    if (localVars != null) {
                        for (IndexedConstant var : localVars){
                            if (var.getName().equals(varName)){ // can be just a prefix
                                typeName = var.getTypeName();
                                break;
                            }
                        }
                    }
                }
            }

            if (typeName != null){
                Collection<IndexedFunction> methods = includeInherited ?
                    request.index.getAllMethods(request.result, typeName, request.prefix, nameKind, attrMask) :
                    request.index.getMethods(request.result, typeName, request.prefix, nameKind, attrMask);

                for (IndexedFunction method : methods){
                    if (staticContext && method.isStatic() || instanceContext && !method.isStatic()) {
                        proposals.add(new PHPCompletionItem.FunctionItem(method, request));
                    }
                }

                Collection<IndexedConstant> properties = includeInherited ?
                    request.index.getAllProperties(request.result, typeName, request.prefix, nameKind, attrMask) :
                    request.index.getProperties(request.result, typeName, request.prefix, nameKind, attrMask);

                for (IndexedConstant prop : properties){
                    if (staticContext && prop.isStatic() || instanceContext && !prop.isStatic()) {
                        PHPCompletionItem.VariableItem item = new PHPCompletionItem.VariableItem(prop, request);

                        if (!completeDollarPrefix) {
                            item.doNotInsertDollarPrefix();
                        }

                        proposals.add(item);
                    }
                }

                if (staticContext) {
                    Collection<IndexedConstant> classConstants = request.index.getClassConstants(
                            request.result, typeName, request.prefix, nameKind);

                    for (IndexedConstant constant : classConstants) {
                        proposals.add(new PHPCompletionItem.VariableItem(constant, request));
                    }
                }
            }
        }
    }

    private ClassDeclaration findEnclosingClass(CompilationInfo info, int offset) {
        List<ASTNode> nodes = NavUtils.underCaret(info, offset);
        for(ASTNode node : nodes) {
            if (node instanceof ClassDeclaration) {
                return (ClassDeclaration) node;
            }
        }
        return null;
    }
    
    private void autoCompleteExpression(List<CompletionProposal> proposals, PHPCompletionItem.CompletionRequest request) {
        // KEYWORDS
        for (String keyword : PHP_KEYWORDS) {
            if (startsWith(keyword, request.prefix)) {
                proposals.add(new PHPCompletionItem.KeywordItem(keyword, request));
            }
        }

        // FUNCTIONS
        PHPIndex index = request.index;

        for (IndexedFunction function : index.getFunctions(request.result, request.prefix, nameKind)) {
            proposals.add(new PHPCompletionItem.FunctionItem(function, request));
        }

        // CONSTANTS
        for (IndexedConstant constant : index.getConstants(request.result, request.prefix, nameKind)) {
            proposals.add(new PHPCompletionItem.ConstantItem(constant, request));
        }

        // LOCAL VARIABLES
        proposals.addAll(getLocalVariableProposals(request.result.getProgram().getStatements(), request));
        
        // CLASS NAMES
        // TODO only show classes with static elements
        autoCompleteClassNames(proposals, request);

        // Special keywords applicable only inside a class
        ClassDeclaration classDecl = findEnclosingClass(request.info, request.anchor);
        if (classDecl != null) {
            for (String keyword : PHP_CLASS_KEYWORDS) {
                if (startsWith(keyword, request.prefix)) {
                    proposals.add(new PHPCompletionItem.KeywordItem(keyword, request));
                }
            }
        }
    }

    private Collection<CompletionProposal> getLocalVariableProposals(Collection<Statement> statementList,
            PHPCompletionItem.CompletionRequest request){
        
        Collection<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
        String url = null;
        try {
            url = request.result.getFile().getFile().toURL().toExternalForm();
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        Collection<IndexedConstant> localVars = getLocalVariables(statementList, request.prefix, request.anchor, url);
        
        for (IndexedConstant localVar : localVars){
            CompletionProposal proposal = new PHPCompletionItem.VariableItem(localVar, request);
            proposals.add(proposal);
        }
        
        return proposals;
    }
    
    private void getLocalVariables_indexVariable(Variable var,
            Map<String, IndexedConstant> localVars,
            String namePrefix, String localFileURL, String type) {

        String varName = extractVariableName(var);
        
        if (isPrefix(varName, namePrefix)) {
            IndexedConstant ic = new IndexedConstant(varName, null,
                    null, localFileURL, -1, 0, type);

            localVars.put(varName, ic);
        }
    }
    
    private boolean isPrefix(String name, String prefix){
        return name != null && (name.startsWith(prefix) 
                || nameKind == NameKind.CASE_INSENSITIVE_PREFIX && name.toLowerCase().startsWith(prefix.toLowerCase()));
    }
    
    private void getLocalVariables_indexVariableInAssignment(Expression expr,
            Map<String, IndexedConstant> localVars,
            String namePrefix, String localFileURL) {
        
        if (expr instanceof Assignment) {
            Assignment assignment = (Assignment) expr;
            
            if (assignment.getLeftHandSide() instanceof Variable) {
                Variable variable = (Variable) assignment.getLeftHandSide();
                String varType = extractVariableTypeFromAssignment(assignment);
                
                getLocalVariables_indexVariable(variable, localVars, namePrefix, 
                        localFileURL, varType);
            }
            
            if (assignment.getRightHandSide() instanceof Assignment){
                getLocalVariables_indexVariableInAssignment(assignment.getRightHandSide(),
                        localVars, namePrefix, localFileURL);
            }
        }
    }
    
    private Collection<IndexedConstant> getLocalVariables(Collection<Statement> statementList, String namePrefix, int position, String localFileURL){
        Map<String, IndexedConstant> localVars = new HashMap<String, IndexedConstant>();
        
        for (Statement statement : statementList){
            if (statement.getStartOffset() > position){
                break; // no need to analyze statements after caret offset
            }
            
            if (statement instanceof ExpressionStatement){
                Expression expr = ((ExpressionStatement)statement).getExpression();
                getLocalVariables_indexVariableInAssignment(expr, localVars, namePrefix, localFileURL);
                
            } else if (!offsetWithinStatement(position, statement)){
                continue;
            }
                
            if (statement instanceof Block) {
                Block block = (Block) statement;
                
                getLocalVariables_MergeResults(localVars,
                        getLocalVariables(block.getStatements(), namePrefix, position, localFileURL));
                
            } else if (statement instanceof IfStatement){
                IfStatement ifStmt = (IfStatement)statement;
                getLocalVariables_indexVariableInAssignment(ifStmt.getCondition(), localVars, namePrefix, localFileURL);
                
                if (offsetWithinStatement(position, ifStmt.getTrueStatement())) {
                    getLocalVariables_MergeResults(localVars,
                            getLocalVariables(Collections.singleton(ifStmt.getTrueStatement()), namePrefix, position, localFileURL));
                    
                } else if (ifStmt.getFalseStatement() != null // false statement ('else') is optional
                        && offsetWithinStatement(position, ifStmt.getFalseStatement())) {
                    
                    getLocalVariables_MergeResults(localVars,
                            getLocalVariables(Collections.singleton(ifStmt.getFalseStatement()), namePrefix, position, localFileURL));
                }
            } else if (statement instanceof WhileStatement) {
                WhileStatement whileStatement = (WhileStatement) statement;
                getLocalVariables_indexVariableInAssignment(whileStatement.getCondition(), localVars, namePrefix, localFileURL);
                
                getLocalVariables_MergeResults(localVars,
                        getLocalVariables(Collections.singleton(whileStatement.getBody()), namePrefix, position, localFileURL));
            }  else if (statement instanceof DoStatement) {
                DoStatement doStatement = (DoStatement) statement;
                
                getLocalVariables_MergeResults(localVars,
                        getLocalVariables(Collections.singleton(doStatement.getBody()), namePrefix, position, localFileURL));
            } else if (statement instanceof ForStatement) {
                ForStatement forStatement = (ForStatement) statement;
                
                for (Expression expr : forStatement.getInitializers()){
                    getLocalVariables_indexVariableInAssignment(expr, localVars, namePrefix, localFileURL);
                }
                
                getLocalVariables_MergeResults(localVars,
                        getLocalVariables(Collections.singleton(forStatement.getBody()), namePrefix, position, localFileURL));
            } else if (statement instanceof ForEachStatement) {
                ForEachStatement forEachStatement = (ForEachStatement) statement;
                
                if (forEachStatement.getKey() instanceof Variable) {
                    Variable var = (Variable) forEachStatement.getKey();
                    getLocalVariables_indexVariable(var, localVars, namePrefix, localFileURL, null);
                }
                
                if (forEachStatement.getValue() instanceof Variable) {
                    Variable var = (Variable) forEachStatement.getValue();
                    getLocalVariables_indexVariable(var, localVars, namePrefix, localFileURL, null);
                }
                
                getLocalVariables_indexVariableInAssignment(forEachStatement.getValue(), localVars, namePrefix, localFileURL);
                
                getLocalVariables_MergeResults(localVars,
                        getLocalVariables(Collections.singleton(forEachStatement.getStatement()), namePrefix, position, localFileURL));
            } else if (statement instanceof FunctionDeclaration) {
                FunctionDeclaration functionDeclaration = (FunctionDeclaration) statement;

                for (FormalParameter param : functionDeclaration.getFormalParameters()) {
                    if (param.getParameterName() instanceof Variable) {
                        String varName = extractVariableName((Variable) param.getParameterName());
                        String type = param.getParameterType() != null ? param.getParameterType().getName() : null;
                        
                        if (isPrefix(varName, namePrefix)) {
                            IndexedConstant ic = new IndexedConstant(varName, null,
                                    null, localFileURL, -1, 0, type);

                            localVars.put(varName, ic);
                        }
                    }
                }
                
                getLocalVariables_MergeResults(localVars,
                            getLocalVariables(Collections.singleton((Statement)functionDeclaration.getBody()), namePrefix, position, localFileURL));
                
            } if (statement instanceof MethodDeclaration) {
                MethodDeclaration methodDeclaration = (MethodDeclaration) statement;
                
                getLocalVariables_MergeResults(localVars,
                    getLocalVariables(Collections.singleton((Statement)methodDeclaration.getFunction()), namePrefix, position, localFileURL));
                
            } else if (statement instanceof ClassDeclaration) {
                ClassDeclaration classDeclaration = (ClassDeclaration) statement;
                
                getLocalVariables_MergeResults(localVars,
                    getLocalVariables(Collections.singleton((Statement)classDeclaration.getBody()), namePrefix, position, localFileURL));
            }
            
        }
        
        return localVars.values();
    }
    
    private void getLocalVariables_MergeResults(Map<String, IndexedConstant> existingMap, Collection<IndexedConstant> newValues){
        for (IndexedConstant var : newValues){
            existingMap.put(var.getName(), var);
        }
    }
    
    private static boolean offsetWithinStatement(int offset, Statement statement){
        return statement.getEndOffset() >= offset && statement.getStartOffset() <= offset;
    }
    
    private static String extractVariableTypeFromAssignment(Assignment assignment) {
        Expression rightSideExpression = assignment.getRightHandSide();
        
        if (rightSideExpression instanceof Assignment) {
            // handle nested assignments, e.g. $l = $m = new ObjectName;
            return extractVariableTypeFromAssignment((Assignment)assignment.getRightHandSide());
        }

        if (rightSideExpression instanceof ClassInstanceCreation) {
            ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) rightSideExpression;
            Expression className = classInstanceCreation.getClassName().getName();

            if (className instanceof Identifier) {
                Identifier identifier = (Identifier) className;
                return identifier.getName();
            }
        } else if (rightSideExpression instanceof ArrayCreation) {
            return "array"; //NOI18N
        }

        return null;
    }
    
    private static String extractVariableName(Variable var){
        if (var.getName() instanceof Identifier) {
            Identifier id = (Identifier) var.getName();
            return "$" + id.getName();
        }

        return null;
    }
        
    public String document(CompilationInfo info, ElementHandle element) {
        
        if (element instanceof PHPDOCTagElement) {
            PHPDOCTagElement pHPDOCTagElement = (PHPDOCTagElement) element;
            return pHPDOCTagElement.getDoc();
        } 
        
        if (element instanceof IndexedElement) {
            final IndexedElement indexedElement = (IndexedElement) element;
            StringBuilder builder = new StringBuilder();
            
            builder.append(String.format("<font size=-1>%s</font>" +
                    "<p><font size=+1><code><b>%s</b></code></font></p><br>", //NOI18N
                    indexedElement.getFilenameUrl(), indexedElement.getDisplayName()));
            
            final StringBuilder phpDoc = new StringBuilder();
            
            if (indexedElement.getOffset() > -1) {
                FileObject fo = element.getFileObject();
                SourceModel model = SourceModelFactory.getInstance().getModel(fo);
                try {

                    model.runUserActionTask(new CancellableTask<CompilationInfo>() {

                        public void cancel() {
                        }

                        public void run(CompilationInfo ci) throws Exception {
                            ParserResult presult = ci.getEmbeddedResults(PHPLanguage.PHP_MIME_TYPE).iterator().next();
                            Program program = Utils.getRoot(presult.getInfo());
                            
                            if (program != null) {
                                ASTNode node = Utils.getNodeAtOffset(program, indexedElement.getOffset());
                                Comment comment = Utils.getCommentForNode(program, node);

                                if (comment instanceof PHPDocBlock) {
                                    PHPDocBlock pHPDocBlock = (PHPDocBlock) comment;
                                    phpDoc.append(pHPDocBlock.getDescription());

                                    // list PHPDoc tags
                                    // TODO a better support for PHPDoc tags
                                    phpDoc.append("<br><br><br><table>\n"); //NOI18N

                                    for (PHPDocTag tag : pHPDocBlock.getTags()) {
                                        phpDoc.append(String.format("<tr><td>%s</td><td>%s</td></tr>\n", //NOI18N
                                                tag.getKind().toString(), tag.getValue()));
                                    }

                                    phpDoc.append("</table>\n"); //NOI18N

                                }
                            }

                        }
                    }, true);

                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            if (phpDoc.length() > 0){
                builder.append(phpDoc);
            } else {
                builder.append(NbBundle.getMessage(PHPCodeCompletion.class, "PHPDocNotFound"));
            }

            return builder.toString();
        }

        return null;
    }

    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        return null;
    }
    
    private static final boolean isPHPIdentifierPart(char c){
        return Character.isJavaIdentifierPart(c) || c == '@';
    }

    public String getPrefix(CompilationInfo info, int caretOffset, boolean upToOffset) {
        try {
            BaseDocument doc = (BaseDocument) info.getDocument();
            if (doc == null) {
                return null;
            }

           // TokenHierarchy<Document> th = TokenHierarchy.get((Document) doc);
            doc.readLock(); // Read-lock due to token hierarchy use

            try {
                int lineBegin = Utilities.getRowStart(doc, caretOffset);
                if (lineBegin != -1) {
                    int lineEnd = Utilities.getRowEnd(doc, caretOffset);
                    String line = doc.getText(lineBegin, lineEnd - lineBegin);
                    int lineOffset = caretOffset - lineBegin;
                    int start = lineOffset;
                    if (lineOffset > 0) {
                        for (int i = lineOffset - 1; i >= 0; i--) {
                            char c = line.charAt(i);
                            if (!isPHPIdentifierPart(c)) {
                                break;
                            } else {
                                start = i;
                            }
                        }
                    }

                    // Find identifier end
                    String prefix;
                    if (upToOffset) {
                        prefix = line.substring(start, lineOffset);
                    } else {
                        if (lineOffset == line.length()) {
                            prefix = line.substring(start);
                        } else {
                            int n = line.length();
                            int end = lineOffset;
                            for (int j = lineOffset; j < n; j++) {
                                char d = line.charAt(j);
                                // Try to accept Foo::Bar as well
                                if (!isPHPIdentifierPart(d)) {
                                    break;
                                } else {
                                    end = j + 1;
                                }
                            }
                            prefix = line.substring(start, end);
                        }
                    }

                    if (prefix.length() > 0) {
                        if (prefix.endsWith("::")) {
                            return "";
                        }

                        if (prefix.endsWith(":") && prefix.length() > 1) {
                            return null;
                        }

                        // Strip out LHS if it's a qualified method, e.g.  Benchmark::measure -> measure
                        int q = prefix.lastIndexOf("::");

                        if (q != -1) {
                            prefix = prefix.substring(q + 2);
                        }

                        // The identifier chars identified by JsLanguage are a bit too permissive;
                        // they include things like "=", "!" and even "&" such that double-clicks will
                        // pick up the whole "token" the user is after. But "=" is only allowed at the
                        // end of identifiers for example.
                        if (prefix.length() == 1) {
                            char c = prefix.charAt(0);
                            if (!(isPHPIdentifierPart(c) || c == '@' || c == '$' || c == ':')) {
                                return null;
                            }
                        } else {
                            for (int i = prefix.length() - 2; i >= 0; i--) { // -2: the last position (-1) can legally be =, ! or ?

                                char c = prefix.charAt(i);
                                if (i == 0 && c == ':') {
                                    // : is okay at the begining of prefixes
                                } else if (!(isPHPIdentifierPart(c) || c == '@' || c == '$')) {
                                    prefix = prefix.substring(i + 1);
                                    break;
                                }
                            }
                        }
                    }
                    return prefix;
                }
            } finally {
                doc.readUnlock();
            }
        // Else: normal identifier: just return null and let the machinery do the rest
        } catch (BadLocationException ble) {
            Exceptions.printStackTrace(ble);
        }

        return null;
    }

    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        if(typedText.length() == 0) {
            return QueryType.NONE;
        }
        char lastChar = typedText.charAt(typedText.length() - 1);
        Document document = component.getDocument();
        TokenHierarchy th = TokenHierarchy.get(document);
        TokenSequence<PHPTokenId> ts = th.tokenSequence(PHPTokenId.language());
        int offset = component.getCaretPosition();
        int diff = ts.move(offset);
        if(diff > 0 && ts.moveNext() || ts.movePrevious()) {
            Token t = ts.token();
            if(t.id() == PHPTokenId.PHP_OBJECT_OPERATOR
                    || t.id() == PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM
                    || t.id() == PHPTokenId.PHP_TOKEN && lastChar == '$'
                    || t.id() == PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING && lastChar == '$'
                    || t.id() == PHPTokenId.PHPDOC_COMMENT && lastChar == '@') {
                return QueryType.ALL_COMPLETION;
            }
            
        }
        return QueryType.NONE;
    }

    

    public String resolveTemplateVariable(String variable, CompilationInfo info, int caretOffset, String name, Map parameters) {
        return null;
    }

    public Set<String> getApplicableTemplates(CompilationInfo info, int selectionBegin, int selectionEnd) {
        return null;
    }

    public ParameterInfo parameters(CompilationInfo info, int caretOffset, CompletionProposal proposal) {
        //TODO: return the info for functions and methods
        return ParameterInfo.NONE;
    }

    private boolean startsWith(String theString, String prefix) {
        if (prefix.length() == 0) {
            return true;
        }

        return caseSensitive ? theString.startsWith(prefix)
                : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }
}
