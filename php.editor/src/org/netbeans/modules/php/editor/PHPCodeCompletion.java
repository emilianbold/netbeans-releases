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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.editor.PHPCompletionItem.CompletionRequest;
import org.netbeans.modules.php.editor.PHPCompletionItem.MethodElementItem;
import org.netbeans.modules.php.editor.CompletionContextFinder.KeywordCompletionType;
import org.netbeans.modules.php.editor.PHPCompletionItem.FieldItem;
import org.netbeans.modules.php.editor.PHPCompletionItem.TypeConstantItem;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.NameKind.Prefix;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.NamespaceElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.ParameterInfoSupport;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.QualifiedNameKind;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.api.elements.ConstantElement;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.FieldElement;
import org.netbeans.modules.php.editor.api.elements.FunctionElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.VariableElement;
import org.netbeans.modules.php.editor.elements.TypeResolverImpl;
import org.netbeans.modules.php.editor.elements.VariableElementImpl;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.options.CodeCompletionPanel.VariablesScope;
import org.netbeans.modules.php.editor.options.OptionsUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.project.api.PhpEditorExtender;
import org.netbeans.modules.php.spi.editor.EditorExtender;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.util.Exceptions;
import static org.netbeans.modules.php.editor.CompletionContextFinder.CompletionContext;
import static org.netbeans.modules.php.editor.CompletionContextFinder.lexerToASTOffset;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPCodeCompletion implements CodeCompletionHandler {
    private static final Logger LOGGER = Logger.getLogger(PHPCodeCompletion.class.getName());


    final static Map<String,KeywordCompletionType> PHP_KEYWORDS = new HashMap<String, KeywordCompletionType>();
    static {
        PHP_KEYWORDS.put("__FILE__", KeywordCompletionType.SIMPLE);
        PHP_KEYWORDS.put("__LINE__", KeywordCompletionType.SIMPLE);
        PHP_KEYWORDS.put("__FUNCTION__", KeywordCompletionType.SIMPLE);
        PHP_KEYWORDS.put("__CLASS__", KeywordCompletionType.SIMPLE);
        PHP_KEYWORDS.put("__METHOD__", KeywordCompletionType.SIMPLE);
        PHP_KEYWORDS.put("use", KeywordCompletionType.SIMPLE);
        PHP_KEYWORDS.put("namespace", KeywordCompletionType.SIMPLE);
        PHP_KEYWORDS.put("php_user_filter", KeywordCompletionType.SIMPLE);
        PHP_KEYWORDS.put("class", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("const", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("continue", KeywordCompletionType.ENDS_WITH_SEMICOLON);
        PHP_KEYWORDS.put("function", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("new", KeywordCompletionType.SIMPLE);
        PHP_KEYWORDS.put("static", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("var", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("final", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("interface", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("instanceof", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("implements", KeywordCompletionType.SIMPLE);
        PHP_KEYWORDS.put("extends", KeywordCompletionType.SIMPLE);
        PHP_KEYWORDS.put("public", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("private", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("protected", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("abstract", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("clone", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("global", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("goto", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("throw", KeywordCompletionType.ENDS_WITH_SPACE);
        PHP_KEYWORDS.put("if", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("switch", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("for", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("array", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("die", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("eval", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("exit", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("empty", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("foreach", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("isset", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("list", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("print", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("unset", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("while", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("catch", KeywordCompletionType.CURSOR_INSIDE_BRACKETS);
        PHP_KEYWORDS.put("try", KeywordCompletionType.ENDS_WITH_CURLY_BRACKETS);
        PHP_KEYWORDS.put("endif", KeywordCompletionType.ENDS_WITH_SEMICOLON);
	PHP_KEYWORDS.put("endfor", KeywordCompletionType.ENDS_WITH_SEMICOLON);
	PHP_KEYWORDS.put("endforeach", KeywordCompletionType.ENDS_WITH_SEMICOLON);
	PHP_KEYWORDS.put("endwhile", KeywordCompletionType.ENDS_WITH_SEMICOLON);
	PHP_KEYWORDS.put("endswitch", KeywordCompletionType.ENDS_WITH_SEMICOLON);
        PHP_KEYWORDS.put("case", KeywordCompletionType.ENDS_WITH_COLON);
    }

    private final static String[] PHP_KEYWORD_FUNCTIONS = {
        "echo", "include", "include_once", "require", "require_once"}; //NOI18N

    final static String[] PHP_CLASS_KEYWORDS = {
        "$this->", "self::", "parent::"
    };

    private final static Collection<Character> AUTOPOPUP_STOP_CHARS = new TreeSet<Character>(
            Arrays.asList('=', ';', '+', '-', '*', '/',
                '%', '(', ')', '[', ']', '{', '}', '?'));

    private final static Collection<PHPTokenId> TOKENS_TRIGGERING_AUTOPUP_TYPES_WS =
            Arrays.asList(PHPTokenId.PHP_NEW, PHPTokenId.PHP_EXTENDS, PHPTokenId.PHP_IMPLEMENTS, PHPTokenId.PHP_INSTANCEOF);

    private static final List<String> INVALID_PROPOSALS_FOR_CLS_MEMBERS =
            Arrays.asList(new String[] {"__construct","__destruct"});//NOI18N

    private static final List<String> CLASS_CONTEXT_KEYWORD_PROPOSAL =
            Arrays.asList(new String[] {"abstract","const","function", "private", "final",
            "protected", "public", "static", "var"});//NOI18N

    private static final List<String> INHERITANCE_KEYWORDS =
            Arrays.asList(new String[] {"extends","implements"});//NOI18N

    private boolean caseSensitive;
    private QuerySupport.Kind nameKind;



    public CodeCompletionResult complete(CodeCompletionContext completionContext) {
         long startTime = 0;

        if (LOGGER.isLoggable(Level.FINE)){
            startTime = System.currentTimeMillis();
        }

        String prefix = completionContext.getPrefix();
        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
        BaseDocument doc = (BaseDocument) completionContext.getParserResult().getSnapshot().getSource().getDocument(false);
        if (doc == null) {
            return CodeCompletionResult.NONE;
        }

        // TODO: separate the code that uses informatiom from lexer
        // and avoid running the index/ast analysis under read lock
        // in order to improve responsiveness
        //doc.readLock();        //TODO: use token hierarchy from snapshot and not use read lock in CC #171702


        try {
            ParserResult info = completionContext.getParserResult();
            int caretOffset = completionContext.getCaretOffset();

            this.caseSensitive = completionContext.isCaseSensitive();
            this.nameKind = caseSensitive ? QuerySupport.Kind.PREFIX : QuerySupport.Kind.CASE_INSENSITIVE_PREFIX;

            PHPParseResult result = (PHPParseResult) info;

            if (result.getProgram() == null) {
                return CodeCompletionResult.NONE;
            }

            CompletionContext context = CompletionContextFinder.findCompletionContext(info, caretOffset);
            LOGGER.fine("CC context: " + context);

            if (context == CompletionContext.NONE) {
                return CodeCompletionResult.NONE;
            }

            PHPCompletionItem.CompletionRequest request = new PHPCompletionItem.CompletionRequest();
            request.context = context;
            request.anchor = caretOffset
                    // can't just use 'prefix.getLength()' here cos it might have been calculated with
                    // the 'upToOffset' flag set to false
                    - getPrefix(info, caretOffset, true).length();

            request.result = result;
            request.info = info;
            request.prefix = prefix;
            request.index = ElementQueryFactory.getIndexQuery(QuerySupportFactory.get(info));

            try {
                request.currentlyEditedFileURL = result.getSnapshot().getSource().getFileObject().getURL().toString();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }

            switch (context) {
                case NAMESPACE_KEYWORD:
                    autoCompleteNamespaces(proposals, request, QualifiedNameKind.QUALIFIED);
                    break;
                case GLOBAL:
                    autoCompleteGlobals(proposals, request);
                    break;
                case EXPRESSION:
                    autoCompleteNamespaces(proposals, request);
                    autoCompleteExpression(proposals, request);
                    autoCompleteExternals(proposals, request, prefix);
                    break;
                case HTML:
                    proposals.add(new PHPCompletionItem.KeywordItem("<?php", request)); //NOI18N
                    proposals.add(new PHPCompletionItem.KeywordItem("<?=", request)); //NOI18N
                    break;
                case NEW_CLASS:
                    autoCompleteNamespaces(proposals, request);
                    autoCompleteNewClass(proposals, request);
                    break;
                case CLASS_NAME:
                    autoCompleteNamespaces(proposals, request);
                    autoCompleteClassNames(proposals, request, false);
                    break;
                case INTERFACE_NAME:
                    autoCompleteNamespaces(proposals, request);
                    autoCompleteInterfaceNames(proposals, request);
                    break;
                case USE_KEYWORD:
                    autoCompleteNamespaces(proposals, request, QualifiedNameKind.QUALIFIED);
                    autoCompleteTypeNames(proposals, request, QualifiedNameKind.QUALIFIED);
                    break;
                case TYPE_NAME:
                    autoCompleteNamespaces(proposals, request);
                    autoCompleteTypeNames(proposals, request);
                    break;
                case STRING:
                    // LOCAL VARIABLES
                    proposals.addAll(getVariableProposals(request, null));
                    break;
                case CLASS_MEMBER:
                    autoCompleteClassMembers(proposals, request, false);
                    break;
                case STATIC_CLASS_MEMBER:
                    autoCompleteClassMembers(proposals, request, true);
                    break;
                case PHPDOC:
                    if (PHPDOCCodeCompletion.isTypeCtx(request)) {
                        autoCompleteTypeNames(proposals, request);
                    } else {
                        PHPDOCCodeCompletion.complete(proposals, request);
                    }
                    break;
                case CLASS_CONTEXT_KEYWORDS:
                    autoCompleteInClassContext(info, caretOffset, proposals, request);
                    break;
                case METHOD_NAME:
                    //autoCompleteMethodName(info, caretOffset, proposals, request);
                    break;
                case IMPLEMENTS:
                    autoCompleteKeywords(proposals, request, Collections.singletonList("implements"));//NOI18N
                    break;
                case EXTENDS:
                    autoCompleteKeywords(proposals, request, Collections.singletonList("extends"));//NOI18N
                    break;
                case INHERITANCE:
                    autoCompleteKeywords(proposals, request, INHERITANCE_KEYWORDS);
                    break;
                case SERVER_ENTRY_CONSTANTS:
                    //TODO: probably better PHPCompletionItem instance should be used
                    //autoCompleteMagicItems(proposals, request, PredefinedSymbols.SERVER_ENTRY_CONSTANTS);
                    for (String keyword : PredefinedSymbols.SERVER_ENTRY_CONSTANTS) {
                        if (keyword.startsWith(request.prefix)) {
                            proposals.add(new PHPCompletionItem.KeywordItem(keyword, request) {
                                @Override
                                public ImageIcon getIcon() {
                                    return null;
                                }
                            });
                        }
                    }

                    break;
            }
        } finally {
            //doc.readUnlock();
        }

        if (LOGGER.isLoggable(Level.FINE)){
            long time = System.currentTimeMillis() - startTime;
            LOGGER.fine(String.format("complete() took %d ms, result contains %d items", time, proposals.size()));
        }

        // a hotfix for #151890
        // TODO: move the check forward to optimize performance
        List<CompletionProposal> filteredProposals = proposals;

        if (!completionContext.isPrefixMatch()){
            filteredProposals = new ArrayList<CompletionProposal>();

            for (CompletionProposal proposal : proposals){
                if (prefix.equals(proposal.getName())){
                    filteredProposals.add(proposal);
                }
            }
        }
        // end of hotfix for #151890

        return new PHPCompletionResult(completionContext, filteredProposals);
    }

    private List<ElementFilter> createTypeFilter(final ClassDeclaration enclosingClass) {
        List<ElementFilter> superTypeIndices = new ArrayList<ElementFilter>();
        Expression superClass = enclosingClass.getSuperClass();
        if (superClass != null) {
            String superClsName = CodeUtils.extractUnqualifiedSuperClassName(enclosingClass);
            superTypeIndices.add(ElementFilter.forSuperClassName(QualifiedName.create(superClsName)));
        }
        List<Expression> interfaces = enclosingClass.getInterfaes();
        Set<QualifiedName> superIfaceNames = new HashSet<QualifiedName>();
        for (Expression identifier : interfaces) {
            String ifaceName = CodeUtils.extractUnqualifiedName(identifier);
            if (ifaceName != null) {
                superIfaceNames.add(QualifiedName.create(ifaceName));
            }
        }
        if (!superIfaceNames.isEmpty()) {
            superTypeIndices.add(ElementFilter.forSuperInterfaceNames(superIfaceNames));
        }
        return superTypeIndices;
    }

    private void autoCompleteNewClass(List<CompletionProposal> proposals, PHPCompletionItem.CompletionRequest request) {
        final QualifiedName prefix = QualifiedName.create(request.prefix).toNotFullyQualified();
        Set<MethodElement> constructors = request.index.getConstructors(NameKind.prefix(prefix));
        for (MethodElement constructor : constructors) {
            proposals.add(PHPCompletionItem.NewClassItem.getItem(constructor, request));
        }
    }

    private void autoCompleteClassNames(List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request,boolean endWithDoubleColon) {
        autoCompleteClassNames(proposals, request, endWithDoubleColon, null);
    }

    private void autoCompleteClassNames(List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request,boolean endWithDoubleColon, QualifiedNameKind kind) {
        Set<ClassElement> classes = request.index.getClasses(NameKind.prefix(QualifiedName.create(request.prefix)));
        for (ClassElement clazz : classes) {
            proposals.add(new PHPCompletionItem.ClassItem(clazz, request, endWithDoubleColon, kind));
        }
    }

    private void autoCompleteInterfaceNames(List<CompletionProposal> proposals, PHPCompletionItem.CompletionRequest request) {
        autoCompleteInterfaceNames(proposals, request, null);
    }
    private void autoCompleteInterfaceNames(List<CompletionProposal> proposals, PHPCompletionItem.CompletionRequest request, QualifiedNameKind kind) {
        Set<InterfaceElement> interfaces = request.index.getInterfaces(NameKind.prefix(QualifiedName.create(request.prefix)));
        for (InterfaceElement iface : interfaces) {
            proposals.add(new PHPCompletionItem.InterfaceItem(iface, request, kind, false));
        }
    }
    private void autoCompleteTypeNames(List<CompletionProposal> proposals, PHPCompletionItem.CompletionRequest request) {
        autoCompleteTypeNames(proposals, request, null);
    }
    private void autoCompleteTypeNames(List<CompletionProposal> proposals, PHPCompletionItem.CompletionRequest request, QualifiedNameKind kind) {
        if (request.prefix.trim().length() > 0) {
            final Prefix prefix = NameKind.prefix(QualifiedName.create(request.prefix));
            Set<InterfaceElement> interfaces = request.index.getInterfaces(prefix);
            for (InterfaceElement iface : interfaces) {
                proposals.add(new PHPCompletionItem.InterfaceItem(iface, request, kind, false));
            }
            Set<ClassElement> classes = request.index.getClasses(prefix);
            for (ClassElement clazz : classes) {
                proposals.add(new PHPCompletionItem.ClassItem(clazz, request, false, kind));
            }
        } else {
            Collection<PhpElement> allTopLevel = request.index.getTopLevelElements(NameKind.empty());
            for (PhpElement element : allTopLevel) {
                if (element instanceof ClassElement) {
                    proposals.add(new PHPCompletionItem.ClassItem((ClassElement)element, request, false, kind));
                } else if (element instanceof InterfaceElement) {
                    proposals.add(new PHPCompletionItem.InterfaceItem((InterfaceElement)element, request, kind, false));
                }
            }
        }

    }

    private void autoCompleteKeywords(List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request, List<String> keywordList) {
        for (String keyword : keywordList) {
            if (keyword.startsWith(request.prefix)) {
                proposals.add(new PHPCompletionItem.KeywordItem(keyword, request));
            }
        }

    }

     private void autoCompleteNamespaces(List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request) {
         autoCompleteNamespaces(proposals, request, null);
     }
     private void autoCompleteNamespaces(List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request, QualifiedNameKind kind) {
        final QualifiedName prefix = QualifiedName.create(request.prefix).toNotFullyQualified();
        Set<NamespaceElement> namespaces = request.index.getNamespaces(NameKind.prefix(prefix));
        for (NamespaceElement namespace : namespaces) {
            proposals.add(new PHPCompletionItem.NamespaceItem(namespace, request, kind));
        }
    }

    private void autoCompleteInClassContext(ParserResult info, int caretOffset, List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request) {
        TokenHierarchy<?> th = info.getSnapshot().getTokenHierarchy();
        TokenSequence<PHPTokenId> tokenSequence = th.tokenSequence(PHPTokenId.language());
        assert tokenSequence != null;

        tokenSequence.move(caretOffset);
        boolean offerMagicAndInherited = true;
        if (!(!tokenSequence.moveNext() && !tokenSequence.movePrevious())) {
            Token<PHPTokenId> token = tokenSequence.token();
            int tokenIdOffset = tokenSequence.token().offset(th);
            offerMagicAndInherited = !CompletionContextFinder.lineContainsAny(token, caretOffset - tokenIdOffset, tokenSequence, Arrays.asList(new PHPTokenId[]{
                        PHPTokenId.PHP_PRIVATE,
                        PHPTokenId.PHP_PUBLIC,
                        PHPTokenId.PHP_PROTECTED,
                        PHPTokenId.PHP_ABSTRACT,
                        PHPTokenId.PHP_VAR,
                        PHPTokenId.PHP_STATIC,
                        PHPTokenId.PHP_CONST
                    }));
        }

        autoCompleteKeywords(proposals, request, CLASS_CONTEXT_KEYWORD_PROPOSAL);
        if (offerMagicAndInherited) {
            ClassDeclaration enclosingClass = findEnclosingClass(info, lexerToASTOffset(info, caretOffset));
            if (enclosingClass != null) {
                List<ElementFilter> superTypeIndices = createTypeFilter(enclosingClass);
                String clsName = enclosingClass.getName().getName();
                if (clsName != null) {
                    final FileObject fileObject = request.result.getSnapshot().getSource().getFileObject();
                    final ElementFilter classFilter = ElementFilter.allOf(
                            ElementFilter.forFiles(fileObject), ElementFilter.allOf(superTypeIndices));
                    Set<ClassElement> classes = classFilter.filter(request.index.getClasses(NameKind.exact(clsName)));
                    for (ClassElement classElement : classes) {
                        ElementFilter methodFilter = ElementFilter.allOf(
                                ElementFilter.forExcludedNames(toNames(request.index.getDeclaredMethods(classElement)), PhpElementKind.METHOD),
                                ElementFilter.forName(NameKind.prefix(QualifiedName.create(request.prefix))));
                        Set<MethodElement> accessibleMethods = methodFilter.filter(request.index.getAccessibleMethods(classElement, classElement));
                        for (MethodElement method : accessibleMethods) {
                            if (!method.isFinal()) {
                                proposals.add(PHPCompletionItem.MethodDeclarationItem.getDeclarationItem(method, request));
                            }
                        }
                        Set<MethodElement> magicMethods = methodFilter.filter(request.index.getAccessibleMagicMethods(classElement));
                        for (MethodElement magicMethod : magicMethods) {
                            if (magicMethod != null) {
                                proposals.add(PHPCompletionItem.MethodDeclarationItem.getDeclarationItem(magicMethod, request));
                            }
                        }
                        break;
                    }
                }
            }
        }        
    }

    private static Set<String> toNames(Set<? extends PhpElement> elements) {
        Set<String> names = new HashSet<String>();
        for (PhpElement elem : elements) {
            names.add(elem.getName());
        }
        return names;
    }

    private void autoCompleteClassMembers(List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request, boolean staticContext) {
        // TODO: remove duplicate/redundant code from here

        TokenHierarchy<?> th = request.info.getSnapshot().getTokenHierarchy();
        TokenSequence<PHPTokenId> tokenSequence = LexUtilities.getPHPTokenSequence(th, request.anchor);

        if (tokenSequence == null){
            return;
        }

        tokenSequence.move(request.anchor);
        if (tokenSequence.movePrevious())
        {
            boolean instanceContext = !staticContext;

            if (tokenSequence.token().id() == PHPTokenId.WHITESPACE) {
                tokenSequence.movePrevious();
            }
            tokenSequence.movePrevious();

            String varName = tokenSequence.token().text().toString();
            List<String> invalidProposalsForClsMembers = INVALID_PROPOSALS_FOR_CLS_MEMBERS;
            Model model = request.result.getModel();
            Collection<? extends TypeScope> types = Collections.emptyList();

            if (varName.equals("self")) { //NOI18N
                types = ModelUtils.resolveTypeAfterReferenceToken(model, tokenSequence, request.anchor);
                if (!types.isEmpty()) {
                    staticContext = true;
                }
            } else if (varName.equals("parent")) { //NOI18N
                invalidProposalsForClsMembers = Collections.emptyList();
                types = ModelUtils.resolveTypeAfterReferenceToken(model, tokenSequence, request.anchor);
                if (!types.isEmpty()) {
                    TypeScope type = ModelUtils.getFirst(types);
                    if (type != null) {
                        staticContext = instanceContext = true;
                    }
                }
            } else if (varName.equals("$this")) { //NOI18N
                if (staticContext) {
                    return;
                }
                types = ModelUtils.resolveTypeAfterReferenceToken(model, tokenSequence, request.anchor);
                if (!types.isEmpty()) {
                    staticContext = false;
                    instanceContext = true;
                }
            } else {
                if (staticContext) {
                    if (varName.startsWith("$")) {//NOI18N
                        return;
                    }
                }
                types = ModelUtils.resolveTypeAfterReferenceToken(model, tokenSequence, request.anchor);

                if (types.isEmpty()) {
                    // frameworks
                    VariableScope variableScope = model.getVariableScope(request.anchor);
                    if (variableScope != null) {
                        tokenSequence.move(request.anchor);
                        String variableName = VariousUtils.getVariableName(VariousUtils.getSemiType(tokenSequence, VariousUtils.State.START, variableScope));
                        if (variableName != null) {
                            FileObject fileObject = request.result.getSnapshot().getSource().getFileObject();
                            EditorExtender editorExtender = PhpEditorExtender.forFileObject(fileObject);
                            PhpClass phpClass = editorExtender.getClass(fileObject, variableName);
                            if (phpClass != null) {
                                types = VariousUtils.getType(variableScope, phpClass.getFullyQualifiedName(), request.anchor, true);
                            }
                        }
                    }
                }
            }

            if (types != null) {
                Set<QualifiedName> processedTypeNames = new HashSet<QualifiedName>();
                TypeElement enclosingType = getEnclosingType(request, types);                    

                for (TypeScope typeScope : types) {
                    String typeName = typeScope.getName();
                    final QualifiedName qualifiedTypeName = typeScope.getNamespaceName().append(typeName);
                    if (!processedTypeNames.add(qualifiedTypeName)) continue;
                    final StaticOrInstanceMembersFilter staticFlagFilter =
                            new StaticOrInstanceMembersFilter(staticContext, instanceContext);
                    
                    final ElementFilter methodsFilter = ElementFilter.allOf(
                            ElementFilter.forKind(PhpElementKind.METHOD),
                            ElementFilter.forName(NameKind.prefix(request.prefix)), 
                            staticFlagFilter,
                            ElementFilter.forExcludedNames(invalidProposalsForClsMembers, PhpElementKind.METHOD),
                            ElementFilter.forInstanceOf(MethodElement.class)
                            );
                    final ElementFilter fieldsFilter = ElementFilter.allOf(
                            ElementFilter.forKind(PhpElementKind.FIELD),
                            ElementFilter.forName(NameKind.prefix(request.prefix)),
                            staticFlagFilter,
                            ElementFilter.forInstanceOf(FieldElement.class)
                            );
                    final ElementFilter constantsFilter = ElementFilter.allOf(
                            ElementFilter.forKind(PhpElementKind.TYPE_CONSTANT),
                            ElementFilter.forName(NameKind.prefix(request.prefix)),
                            ElementFilter.forInstanceOf(TypeConstantElement.class)
                            );

                    for (final PhpElement phpElement : request.index.getAccessibleTypeMembers(typeScope, enclosingType)) {
                        if (methodsFilter.isAccepted(phpElement)) {
                            MethodElement method = (MethodElement) phpElement;
                            List<MethodElementItem> items = PHPCompletionItem.MethodElementItem.getItems(method, request);
                            for (MethodElementItem methodItem : items) {
                                proposals.add(methodItem);
                            }
                        } else if (fieldsFilter.isAccepted(phpElement)) {
                            FieldElement field = (FieldElement) phpElement;
                            FieldItem fieldItem = PHPCompletionItem.FieldItem.getItem(field, request);
                            proposals.add(fieldItem);                            
                        } else if (staticContext && constantsFilter.isAccepted(phpElement)) {
                            TypeConstantElement constant = (TypeConstantElement) phpElement;
                            TypeConstantItem constantItem = PHPCompletionItem.TypeConstantItem.getItem(constant, request);
                            proposals.add(constantItem);
                        }
                    }
                }
            }
        }
    }

    private TypeElement getEnclosingType(CompletionRequest request, Collection<? extends TypeScope> types) {
        final ClassDeclaration enclosingClass = findEnclosingClass(request.info, lexerToASTOffset(request.result, request.anchor));
        final String enclosingClassName = (enclosingClass != null) ? CodeUtils.extractClassName(enclosingClass) : null;
        final NameKind.Exact enclosingClassNameKind = (enclosingClassName != null && !enclosingClassName.trim().isEmpty()) ? NameKind.exact(enclosingClassName) : null;
        Set<FileObject> preferedFileObjects = new HashSet<FileObject>();
        Set<TypeElement> enclosingTypes = null;
        FileObject currentFile = request.result.getSnapshot().getSource().getFileObject();
        if (currentFile != null) {
            preferedFileObjects.add(currentFile);
        }
        for (TypeScope typeScope : types) {
            final FileObject fileObject = typeScope.getFileObject();
            if (fileObject != null) {
                preferedFileObjects.add(fileObject);
            }
            if (enclosingClassNameKind != null && enclosingTypes == null) {
                if (enclosingClassNameKind.matchesName(typeScope)) {
                    enclosingTypes = Collections.<TypeElement>singleton((TypeElement) typeScope);
                }
            }
        }
        if (enclosingClassNameKind != null && enclosingTypes == null) {
            final ElementFilter forFiles = ElementFilter.forFiles(preferedFileObjects.toArray(new FileObject[preferedFileObjects.size()]));
            Set<ClassElement> classes = forFiles.prefer(request.index.getClasses(enclosingClassNameKind));
            if (!classes.isEmpty()) {
                enclosingTypes = new HashSet<TypeElement>(classes);
            }
        }
        return (enclosingTypes == null || enclosingTypes.isEmpty()) ? null : enclosingTypes.iterator().next();
    }

    private static ClassDeclaration findEnclosingClass(ParserResult info, int offset) {
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
        for (String keyword : PHP_KEYWORDS.keySet()) {
            if (startsWith(keyword, request.prefix)) {
                proposals.add(new PHPCompletionItem.KeywordItem(keyword, request));
            }
        }

        for (String keyword : PHP_KEYWORD_FUNCTIONS) {
            if (startsWith(keyword, request.prefix)) {
                proposals.add(new PHPCompletionItem.SpecialFunctionItem(keyword, request));
            }
        }

        if (startsWith("return", request.prefix)){ //NOI18N
            proposals.add(new PHPCompletionItem.ReturnItem(request));
        }

        final boolean offerGlobalVariables = OptionsUtils.codeCompletionVariablesScope().equals(VariablesScope.ALL);
        final Prefix prefix = NameKind.prefix(QualifiedName.create(request.prefix));
        final Set<VariableElement> globalVariables = new HashSet<VariableElement>();
       
        for (final PhpElement element : request.index.getTopLevelElements(prefix)) {
            if (element instanceof FunctionElement) {
                for (final PHPCompletionItem.FunctionElementItem functionItem :
                    PHPCompletionItem.FunctionElementItem.getItems((FunctionElement) element, request)) {
                    proposals.add(functionItem);
                }
            } else if (element instanceof ClassElement) {
                proposals.add(new PHPCompletionItem.ClassItem((ClassElement) element, request, true, null));
            } else if (element instanceof InterfaceElement) {
                proposals.add(new PHPCompletionItem.InterfaceItem((InterfaceElement) element, request, true));
            } else if (offerGlobalVariables && element instanceof VariableElement) {
                globalVariables.add((VariableElement)element);
            } else if (element instanceof ConstantElement) {
                proposals.add(new PHPCompletionItem.ConstantItem((ConstantElement) element, request));
            }
        }
        FileObject fileObject = request.result.getSnapshot().getSource().getFileObject();
        final ElementFilter forCurrentFile = ElementFilter.forFiles(fileObject);
        proposals.addAll(getVariableProposals(request, forCurrentFile.reverseFilter(globalVariables)));

        // Special keywords applicable only inside a class
        final ClassDeclaration classDecl = findEnclosingClass(request.info, lexerToASTOffset(request.result, request.anchor));
        if (classDecl != null) {
            final String className = CodeUtils.extractClassName(classDecl);
            if (className != null) {
                for (final String keyword : PHP_CLASS_KEYWORDS) {
                    if (startsWith(keyword, request.prefix)) {
                        proposals.add(new PHPCompletionItem.ClassScopeKeywordItem(className, keyword, request));
                    }
                }
            }            
        }
    }

    private void autoCompleteGlobals(List<CompletionProposal> proposals, PHPCompletionItem.CompletionRequest request) {
        if (OptionsUtils.codeCompletionVariablesScope().equals(VariablesScope.ALL)) {
            final Prefix prefix = NameKind.prefix(QualifiedName.create(request.prefix));
            for (VariableElement variableElement : request.index.getTopLevelVariables(prefix)) {
                proposals.add(new PHPCompletionItem.VariableItem(variableElement, request));
            }
        }
    }

    /**
     * @param globalVariables (can be bull) if null then will be looked up in index
     */
    private Collection<CompletionProposal> getVariableProposals(final CompletionRequest request, Set<VariableElement> globalVariables) {
        final Map<String, VariableElement> variables = new LinkedHashMap<String, VariableElement>();
        final Collection<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
         final LocalVariables localVars = getLocalVariables(request.result, request.prefix, request.anchor, request.currentlyEditedFileURL);
        if (localVars.globalContext) {
            if (globalVariables == null) {
                FileObject fileObject = request.result.getSnapshot().getSource().getFileObject();
                final ElementFilter forCurrentFile = ElementFilter.forFiles(fileObject);
                globalVariables = forCurrentFile.
                        reverseFilter(request.index.getTopLevelVariables(NameKind.prefix(QualifiedName.create(request.prefix))));
            }

            for (final VariableElement globalVariable : globalVariables) {
                variables.put(globalVariable.getName(), globalVariable);
            }
        }

        for (final VariableElement localVariable : localVars.vars) {
            variables.put(localVariable.getName(), localVariable);
        }
        for (final VariableElement variable : variables.values()) {
            proposals.add(new PHPCompletionItem.VariableItem(variable, request));
        }
        for (final String name : PredefinedSymbols.SUPERGLOBALS) {
            if (isPrefix("$" + name, request.prefix)) {//NOI18N
                CompletionProposal proposal = new PHPCompletionItem.SuperGlobalItem(request, name);
                proposals.add(proposal);
            }
        }
        return proposals;
    }

    private boolean isPrefix(String name, String prefix){
        return name != null && (name.startsWith(prefix)
                || nameKind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && name.toLowerCase().startsWith(prefix.toLowerCase()));
    }


    private void autoCompleteExternals(List<CompletionProposal> proposals, CompletionRequest request, String prefix) {
        FileObject fileObject = request.result.getSnapshot().getSource().getFileObject();
        // frameworks
        // XXX add this to model! (so go to source etc. could work)
        EditorExtender editorExtender = PhpEditorExtender.forFileObject(fileObject);
        for (PhpBaseElement element : editorExtender.getElementsForCodeCompletion(fileObject)) {
            if (prefix == null
                    || element.getName().startsWith(prefix)) {
                CompletionProposal variable = PhpElementCompletionItem.fromPhpElement(element, request);
                String variableName = variable.getName();

                Iterator<CompletionProposal> iter = proposals.iterator();
                while (iter.hasNext()) {
                    CompletionProposal proposal = iter.next();
                    if (variableName.equals(proposal.getName())) {
                        iter.remove();
                        break;
                    }
                }
                proposals.add(variable);
            }
        }
    }

    private class LocalVariables{
        Collection<VariableElement> vars;
        boolean globalContext;
    }

    private LocalVariables getLocalVariables(final PHPParseResult context, final String namePrefix, final int position, final String localFileURL) {
        Map<String, VariableElement> localVars = new HashMap<String, VariableElement>();
        LocalVariables result = new LocalVariables();
        result.vars = localVars.values();
        Model model = context.getModel();
        VariableScope variableScope = model.getVariableScope(position);
        if (variableScope != null) {
            result.globalContext = variableScope instanceof NamespaceScope;
            Collection<? extends VariableName> declaredVariables = ModelUtils.filter(variableScope.getDeclaredVariables(), QuerySupport.Kind.CASE_INSENSITIVE_PREFIX, namePrefix);
            final int caretOffset = position + namePrefix.length();
            for (VariableName varName : declaredVariables) {
                if (varName.getNameRange().getEnd() < caretOffset) {
                    final String name = varName.getName();
                    String notDollaredName = name.startsWith("$") ? name.substring(1) : name;
                    if (PredefinedSymbols.SUPERGLOBALS.contains(notDollaredName)) {
                        continue;
                    }
                    if (varName.representsThis()) {
                        continue;
                    }
                    final Collection<? extends String> typeNames = varName.getTypeNames(position);
                    String typeName = typeNames.size() > 1 ? "mixed" : ModelUtils.getFirst(typeNames);//NOI18N
                    final Set<QualifiedName> qualifiedNames = typeName != null ?
                        Collections.singleton(QualifiedName.create(typeName)) :
                        Collections.<QualifiedName>emptySet();
                    VariableElement ic = new VariableElementImpl(name, 0, localFileURL,
                            varName.getElementQuery(), TypeResolverImpl.forNames(qualifiedNames));
                    localVars.put(name, ic);
                }
            }
        }
        return result;
    }

    public String document(ParserResult info, ElementHandle element) {
        if (element instanceof ModelElement) {
            ModelElement mElem = (ModelElement) element;
            ModelElement parentElem = mElem.getInScope();
            String fName = mElem.getFileObject().getNameExt();
            String tooltip = null;
            if (parentElem instanceof TypeScope) {
                 tooltip = mElem.getPhpElementKind()+": "+parentElem.getName()+"<b> "+mElem.getName() + " </b>"+ "("+ fName+")";//NOI18N
            } else {
                tooltip = mElem.getPhpElementKind()+":<b> "+mElem.getName() + " </b>"+ "("+ fName+")";//NOI18N
            }
            return String.format("<div align=\"right\"><font size=-1>%s</font></div>", tooltip);
        }

        return ((element instanceof MethodElement) && ((MethodElement)element).isMagic()) ? null :
            DocRenderer.document(info, element);
    }

    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        return null;
    }

    private static final boolean isPHPIdentifierPart(char c){
        return Character.isJavaIdentifierPart(c) || c == '@';
    }

    private static final boolean isPrefixBreaker(char c){
        return !(isPHPIdentifierPart(c) || c == '\\' || c == '$' || c == ':');
    }

    public String getPrefix(ParserResult info, int caretOffset, boolean upToOffset) {
        try {
            BaseDocument doc = (BaseDocument) info.getSnapshot().getSource().getDocument(false);
            if (doc == null) {
                return null;
            }
            int lineBegin = Utilities.getRowStart(doc, caretOffset);
            if (lineBegin != -1) {
                int lineEnd = Utilities.getRowEnd(doc, caretOffset);
                String line = doc.getText(lineBegin, lineEnd - lineBegin);
                int lineOffset = caretOffset - lineBegin;
                int start = lineOffset;
                if (lineOffset > 0) {
                    for (int i = lineOffset - 1; i >= 0; i--) {
                        char c = line.charAt(i);
                        if (!isPHPIdentifierPart(c) && c != '\\') {
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
                    int lastIndexOfDollar = prefix.lastIndexOf('$');//NOI18N
                    if (lastIndexOfDollar > 0) {
                        prefix = prefix.substring(lastIndexOfDollar);
                    }
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
                        if (isPrefixBreaker(c)) {
                            return null;
                        }
                    } else {
                        for (int i = prefix.length() - 2; i >= 0; i--) { // -2: the last position (-1) can legally be =, ! or ?

                            char c = prefix.charAt(i);
                            if (i == 0 && c == ':') {
                                // : is okay at the begining of prefixes
                                } else if (isPrefixBreaker(c)) {
                                prefix = prefix.substring(i + 1);
                                break;
                            }
                        }
                    }
                }

                if (prefix != null && prefix.startsWith("@")) {//NOI18N
                    final TokenHierarchy<?> tokenHierarchy = info.getSnapshot().getTokenHierarchy();
                    TokenSequence<PHPTokenId> tokenSequence = tokenHierarchy != null ? LexUtilities.getPHPTokenSequence(tokenHierarchy, caretOffset) : null;
                    if (tokenSequence != null) {
                        tokenSequence.move(caretOffset);
                        if (tokenSequence.moveNext() && tokenSequence.movePrevious()) {
                            Token<PHPTokenId> token = tokenSequence.token();
                            PHPTokenId id = token.id();
                            if (id.equals(PHPTokenId.PHP_STRING) || id.equals(PHPTokenId.PHP_TOKEN)) {
                                prefix = prefix.substring(1);
                            }
                        }
                    }
                }
                return prefix;
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

        if (AUTOPOPUP_STOP_CHARS.contains(Character.valueOf(lastChar))){
            return QueryType.STOP;
        }

        Document document = component.getDocument();
        //TokenHierarchy th = TokenHierarchy.get(document);
        int offset = component.getCaretPosition();
        TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(document, offset);
        if (ts == null) {
            return QueryType.STOP;
        }
       Token t = null;
        int diff = ts.move(offset);
        if(diff > 0 && ts.moveNext() || ts.movePrevious()) {
            t = ts.token();
            if (OptionsUtils.autoCompletionTypes()) {
                if (lastChar == ' ' || lastChar == '\t'){
                    if (ts.movePrevious()
                            && TOKENS_TRIGGERING_AUTOPUP_TYPES_WS.contains(ts.token().id())){

                        return QueryType.ALL_COMPLETION;
                    } else {
                        return QueryType.STOP;
                    }
                }

                if(t.id() == PHPTokenId.PHP_OBJECT_OPERATOR || t.id() == PHPTokenId.PHP_PAAMAYIM_NEKUDOTAYIM) {
                        return QueryType.ALL_COMPLETION;
                    }
                }
            if (OptionsUtils.autoCompletionVariables()) {
                if((t.id() == PHPTokenId.PHP_TOKEN && lastChar == '$') ||
                        (t.id() == PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING && lastChar == '$')) {
                    return QueryType.ALL_COMPLETION;
                }
            }
            if (OptionsUtils.autoCompletionNamespaces()) {                
                if(t.id() == PHPTokenId.PHP_NS_SEPARATOR) {
                    return isPhp_53(document) ? QueryType.ALL_COMPLETION : QueryType.NONE;
                }
            }
            if (t.id() == PHPTokenId.PHPDOC_COMMENT && lastChar == '@') {
                return QueryType.ALL_COMPLETION;
            }
            if (OptionsUtils.autoCompletionFull() && t != null) {
                TokenId id = t.id();
                if ((id.equals(PHPTokenId.PHP_STRING) || id.equals(PHPTokenId.PHP_VARIABLE)) && t.length() > 0) {
                    return QueryType.ALL_COMPLETION;
                }
            }
        }
        return QueryType.NONE;
    }

    public static  boolean isPhp_53(Document document) {
        final FileObject fileObject = CodeUtils.getFileObject(document);
        assert fileObject != null;
        return fileObject != null ? CodeUtils.isPhp_53(fileObject) : false;
    }


    public String resolveTemplateVariable(String variable, ParserResult info, int caretOffset, String name, Map parameters) {
        return null;
    }

    public Set<String> getApplicableTemplates(ParserResult info, int selectionBegin, int selectionEnd) {
        return null;
    }

    public ParameterInfo parameters(final ParserResult info, final int caretOffset, CompletionProposal proposal) {
        final org.netbeans.modules.php.editor.model.Model model = ((PHPParseResult)info).getModel();
        ParameterInfoSupport infoSupport = model.getParameterInfoSupport(caretOffset);
        return infoSupport.getParameterInfo();
    }

    private boolean startsWith(String theString, String prefix) {
        if (prefix.length() == 0) {
            return true;
        }

        return caseSensitive ? theString.startsWith(prefix)
                : theString.toLowerCase().startsWith(prefix.toLowerCase());
    }

    private static class StaticOrInstanceMembersFilter extends ElementFilter {
        private final boolean forStaticContext;
        private final boolean forInstanceContext;
        private final boolean staticAllowed;
        private final boolean nonstaticAllowed;
        public StaticOrInstanceMembersFilter(final boolean forStaticContext, final boolean forInstanceContext) {
            this.forStaticContext = forStaticContext;
            this.forInstanceContext = forInstanceContext;
            this.staticAllowed = OptionsUtils.codeCompletionStaticMethods();
            this.nonstaticAllowed = OptionsUtils.codeCompletionNonStaticMethods();
        }

        @Override
        public boolean isAccepted(final PhpElement element) {
            if (forStaticContext && isAcceptedForStaticContext(element)) {
                return true;
            }
            if (forInstanceContext && isAcceptedForNotStaticContext(element)) {
                return true;
            }
            return false;
        }

        private boolean isAcceptedForNotStaticContext(final PhpElement element) {
            final boolean isStatic = element.getPhpModifiers().isStatic();
            return !isStatic || (staticAllowed && element.getPhpElementKind().equals(PhpElementKind.METHOD));
        }

        private boolean isAcceptedForStaticContext(final PhpElement element) {
            final boolean isStatic = element.getPhpModifiers().isStatic();
            return isStatic || (nonstaticAllowed && element.getPhpElementKind().equals(PhpElementKind.METHOD));
        }
    }
 }
