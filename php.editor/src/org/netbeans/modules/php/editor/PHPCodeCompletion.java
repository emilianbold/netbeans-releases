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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
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
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.editor.PhpElement;
import org.netbeans.modules.php.editor.PHPCompletionItem.CompletionRequest;
import org.netbeans.modules.php.editor.PredefinedSymbols.MagicIndexedFunction;
import org.netbeans.modules.php.editor.CompletionContextFinder.KeywordCompletionType;
import org.netbeans.modules.php.editor.index.IndexedClass;
import org.netbeans.modules.php.editor.index.IndexedClassMember;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.IndexedElement;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.IndexedInterface;
import org.netbeans.modules.php.editor.index.IndexedNamespace;
import org.netbeans.modules.php.editor.index.IndexedType;
import org.netbeans.modules.php.editor.index.IndexedVariable;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.Parameter;
import org.netbeans.modules.php.editor.model.ParameterInfoSupport;
import org.netbeans.modules.php.editor.model.PhpKind;
import org.netbeans.modules.php.editor.model.QualifiedName;
import org.netbeans.modules.php.editor.model.QualifiedNameKind;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.model.VariableScope;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.options.CodeCompletionPanel.VariablesScope;
import org.netbeans.modules.php.editor.options.OptionsUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
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
            request.index = PHPIndex.get(info);

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
                    autoCompleteExternals(proposals, request);
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
                    proposals.addAll(getVariableProposals(request.result.getProgram(), request));
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
                    autoCompleteMethodName(info, caretOffset, proposals, request);
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

    private static class ConstructorMember extends IndexedClassMember<IndexedFunction> {

        private static ConstructorMember createDefaultConstructor(IndexedClass clz) {
            return new ConstructorMember(clz, Collections.<Parameter>emptyList());
        }
        private static ConstructorMember createInstance(IndexedClass clz, IndexedFunction constructor) {
            return new ConstructorMember(clz, constructor.getParameters());
        }

        private static ConstructorMember createClassFake(IndexedClass clz) {
            return new ConstructorMember(clz, null);
        }
        
        private ConstructorMember(final IndexedClass clz, List<Parameter> params ) {
            super(clz, params == null ? null : new IndexedFunction(clz.getName(),
                    clz.getName(),
                    clz.getIndex(), clz.getFilenameUrl(), 
                    params,
                    clz.getOffset(), Modifier.PUBLIC, ElementKind.METHOD) {//NOI18N

                @Override
                public String getFullyQualifiedName() {
                    return clz.getFullyQualifiedName();
                }
            });
        }
    }

    /*
     * returns instance of IndexedClassMember whereas getMember() may return null
     */
    private Collection<IndexedClassMember<IndexedFunction>> getAllConstructorsForCC(PHPIndex index, PHPParseResult result, QualifiedName qualifiedName, QuerySupport.Kind kind) {
        NamespaceIndexFilter<IndexedFunction> constrName = new NamespaceIndexFilter<IndexedFunction>(qualifiedName);
        NamespaceIndexFilter<IndexedClass> clzName = new NamespaceIndexFilter<IndexedClass>(qualifiedName);
        Collection<IndexedClassMember<IndexedFunction>> retval = new HashSet<IndexedClassMember<IndexedFunction>>();
        final Collection<IndexedClass> allClasses = clzName.filter(index.getClasses(result, clzName.getName(), kind));
        final Map<String, IndexedFunction> name2constructs = new HashMap<String, IndexedFunction>();
        for (IndexedFunction constr : constrName.filter(index.getConstructors(result, constrName.getName()))) {
            name2constructs.put(constr.getName(), constr);
        }
        for (final IndexedClass clz : allClasses) {
            IndexedFunction constr = name2constructs.get(clz.getName());
            if (constr == null) {
                boolean addDefaultConstructor = true;
                if (clz.getSuperClass() != null) {
                    if (ModelUtils.nameKindMatch(clz.getName(), QuerySupport.Kind.EXACT, clzName.getName())) {
                        Collection<IndexedType> classAncestors = index.getClassAncestors(result, clzName.getName());
                        for (IndexedType indexedType : classAncestors) {
                            if (indexedType instanceof IndexedClass) {
                                Collection<IndexedClassMember<IndexedFunction>> tmpConsts = index.getConstructors(result, indexedType);
                                if (!tmpConsts.isEmpty()) {
                                    addDefaultConstructor = false;
                                    for (IndexedClassMember<IndexedFunction> indexedClassMember : tmpConsts) {
                                        retval.add(ConstructorMember.createInstance(clz, indexedClassMember.getMember()));
                                    }
                                    break;
                                }
                            }
                        }
                    } else {
                        addDefaultConstructor = false;
                        retval.add(ConstructorMember.createClassFake(clz));
                    }
                }
                if (addDefaultConstructor) {
                    retval.add(ConstructorMember.createDefaultConstructor(clz));//NOI18N
                }
            } else {
                if (ModelUtils.nameKindMatch(constr.getName(), kind, constrName.getName())) {
                    retval.add(ConstructorMember.createInstance(clz,constr));
                }
            }
        }
        return retval;
    }

    private void autoCompleteNewClass(List<CompletionProposal> proposals, PHPCompletionItem.CompletionRequest request) {
        Collection<IndexedClassMember<IndexedFunction>> members = getAllConstructorsForCC(request.index, request.result, QualifiedName.create(request.prefix), nameKind);
        for (IndexedClassMember<IndexedFunction> element : members) {
            IndexedType type = element.getType();
            if (type instanceof IndexedClass && type.isAbstract()) {
                continue;
            }
            if (element.getMember() != null) {
                IndexedFunction fnc = element.getMember();
                int[] optionalArgs = fnc.getOptionalArgs();
                for (int i = 0; i <= optionalArgs.length; i++) {
                    proposals.add(new PHPCompletionItem.NewClassItem(fnc, request, i));
                }
            } else if (element.getType() instanceof IndexedClass) {
                IndexedClass indexedClass = (IndexedClass) element.getType();
                proposals.add(new PHPCompletionItem.ClassItem(indexedClass, request, false, null));
            }
        }
    }

    private void autoCompleteClassNames(List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request,boolean endWithDoubleColon) {
        autoCompleteClassNames(proposals, request, endWithDoubleColon, null);
    }

    private void autoCompleteClassNames(List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request,boolean endWithDoubleColon, QualifiedNameKind kind) {
        NamespaceIndexFilter<IndexedClass> completionSupport = new NamespaceIndexFilter<IndexedClass>(request.prefix);
        final Collection<IndexedClass> classes = request.index.getClasses(request.result, completionSupport.getName(), nameKind);
        for (IndexedClass clazz : completionSupport.filter(classes)) {
            proposals.add(new PHPCompletionItem.ClassItem(clazz, request, endWithDoubleColon, kind));
        }
    }

    private void autoCompleteInterfaceNames(List<CompletionProposal> proposals, PHPCompletionItem.CompletionRequest request) {
        autoCompleteInterfaceNames(proposals, request, null);
    }
    private void autoCompleteInterfaceNames(List<CompletionProposal> proposals, PHPCompletionItem.CompletionRequest request, QualifiedNameKind kind) {
        NamespaceIndexFilter<IndexedInterface> completionSupport = new NamespaceIndexFilter<IndexedInterface>(request.prefix);
        final Collection<IndexedInterface> interfaces = request.index.getInterfaces(request.result, completionSupport.getName(), nameKind);
        for (IndexedInterface iface : completionSupport.filter(interfaces)) {
            proposals.add(new PHPCompletionItem.InterfaceItem(iface, request, kind, false));
        }
    }
    private void autoCompleteTypeNames(List<CompletionProposal> proposals, PHPCompletionItem.CompletionRequest request) {
        autoCompleteTypeNames(proposals, request, null);
    }
    private void autoCompleteTypeNames(List<CompletionProposal> proposals, PHPCompletionItem.CompletionRequest request, QualifiedNameKind kind) {
        NamespaceIndexFilter<IndexedInterface> ifaceSupport = new NamespaceIndexFilter<IndexedInterface>(request.prefix);
        if (ifaceSupport.getName().trim().length() > 0) {
            final Collection<IndexedInterface> interfaces = request.index.getInterfaces(request.result, ifaceSupport.getName(), nameKind);
            for (IndexedInterface iface : ifaceSupport.filter(interfaces)) {
                proposals.add(new PHPCompletionItem.InterfaceItem(iface, request, kind, false));
            }
            NamespaceIndexFilter<IndexedClass> classSupport = new NamespaceIndexFilter<IndexedClass>(request.prefix);
            final Collection<IndexedClass> classes = request.index.getClasses(request.result, ifaceSupport.getName(), nameKind);
            for (IndexedClass clazz : classSupport.filter(classes)) {
                proposals.add(new PHPCompletionItem.ClassItem(clazz, request, false, kind));
            }
        } else {
            NamespaceIndexFilter<IndexedElement> completionSupport = new NamespaceIndexFilter<IndexedElement>(request.prefix);
            VariablesScope ccVariablesScope = OptionsUtils.codeCompletionVariablesScope();
            Collection<IndexedElement> allTopLevel = null;
            if (ccVariablesScope.equals(VariablesScope.ALL)) {
                allTopLevel = request.index.getAllTopLevel(request.result, completionSupport.getName(), nameKind);
            } else {
                final EnumSet<PhpKind> allOf = EnumSet.<PhpKind>allOf(PhpKind.class);
                allOf.remove(PhpKind.VARIABLE);
                allTopLevel = request.index.getAllTopLevel(request.result, completionSupport.getName(), nameKind, allOf);
            }
            for (IndexedElement indexedElement : completionSupport.filter(allTopLevel)) {
                if (indexedElement instanceof IndexedClass) {
                    proposals.add(new PHPCompletionItem.ClassItem((IndexedClass)indexedElement, request, false, kind));
                } else if (indexedElement instanceof IndexedInterface) {
                    proposals.add(new PHPCompletionItem.InterfaceItem((IndexedInterface)indexedElement, request, kind, false));
                }
            }
        }

    }

    private void autoCompleteMagicItems(List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request,final Collection<String> proposedTexts,
            boolean completeNameAndBodyOnly, Set<String> insideNames) {
        for (String keyword : proposedTexts) {
            if (keyword.startsWith(request.prefix) && !insideNames.contains(keyword)) {
                IndexedFunction magicFunc = PredefinedSymbols.MAGIC_METHODS.get(keyword);
                if (magicFunc != null) {
                    if (completeNameAndBodyOnly) {
                        proposals.add(new PHPCompletionItem.MagicMethodNameItem(magicFunc, request));
                    } else {
                        proposals.add(new PHPCompletionItem.MagicMethodItem(magicFunc, request));
                    }
                }
            }
        }
    //autoCompleteKeywords(proposals, request, METHOD_NAME_PROPOSALS);
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
        NamespaceIndexFilter<IndexedNamespace> completionSupport = new NamespaceIndexFilter<IndexedNamespace>(request.prefix);
        final String prefix = completionSupport.getName();
        Collection<IndexedNamespace> namespaces = request.index.getNamespaces(request.result,prefix);//NOI18N

        namespaces = completionSupport.filter(namespaces);
        for (IndexedNamespace namespace : namespaces) {
            proposals.add(new PHPCompletionItem.NamespaceItem(namespace, request, kind));
        }
    }

    private void autoCompleteMethodName(ParserResult info, int caretOffset, List<CompletionProposal> proposals,
            PHPCompletionItem.CompletionRequest request) {
        ClassDeclaration enclosingClass = findEnclosingClass(info, lexerToASTOffset(info, caretOffset));
        if (enclosingClass != null) {
            String clsName = enclosingClass.getName().getName();
            Set<String> insideNames = new HashSet<String>();
            Collection<IndexedFunction> methods = request.index.getMethods(
                    request.result, clsName, request.prefix,
                    QuerySupport.Kind.CASE_INSENSITIVE_PREFIX, PHPIndex.ANY_ATTR);
            for (IndexedFunction meth : methods) {
                insideNames.add(meth.getName());
            }
            autoCompleteMagicItems(proposals, request, PredefinedSymbols.MAGIC_METHODS.keySet(),true, insideNames);
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
        ClassDeclaration enclosingClass = findEnclosingClass(info, lexerToASTOffset(info, caretOffset));
        Set<String> insideNames = new HashSet<String>();
        Set<String> methodNames = new HashSet<String>();
        if (enclosingClass != null) {
            String clsName = enclosingClass.getName().getName();
            Collection<IndexedFunction> methods = request.index.getMethods(
                    request.result, clsName, request.prefix,
                    QuerySupport.Kind.CASE_INSENSITIVE_PREFIX, PHPIndex.ANY_ATTR);
            for (IndexedFunction meth : methods) {
                insideNames.add(meth.getName());
                methodNames.add(meth.getName());
            }
        }
        if (enclosingClass != null && offerMagicAndInherited) {
            Expression superClass = enclosingClass.getSuperClass();
            if (superClass != null) {
                String superClsName = CodeUtils.extractUnqualifiedSuperClassName(enclosingClass);
                Collection<IndexedClassMember<IndexedFunction>> superMethods = request.index.getAllMethods(
                        request.result, superClsName, request.prefix,
                        QuerySupport.Kind.CASE_INSENSITIVE_PREFIX, Modifier.PUBLIC | Modifier.PROTECTED);
                for (IndexedClassMember<IndexedFunction> classMember: superMethods) {
                    IndexedFunction superMeth = classMember.getMember();
                    if (superMeth.getName().startsWith(request.prefix) &&
                            !superMeth.isFinal() &&
                            !insideNames.contains(superMeth.getName()) &&
                            !methodNames.contains(superMeth.getName())) {
                        for (int i = 0; i <= superMeth.getOptionalArgs().length; i++) {
                            methodNames.add(superMeth.getName());
                            proposals.add(new PHPCompletionItem.FunctionDeclarationItem(superMeth, request, i, false));
                        }
                    }
                }
            }
            List<Expression> interfaces = enclosingClass.getInterfaes();
            for (Expression identifier : interfaces) {
                String ifaceName = CodeUtils.extractUnqualifiedName(identifier);
                Collection<IndexedClassMember<IndexedFunction>> superMethods = request.index.getAllMethods(
                        request.result, ifaceName, request.prefix,
                        QuerySupport.Kind.CASE_INSENSITIVE_PREFIX, Modifier.PUBLIC | Modifier.PROTECTED);
                for (IndexedClassMember<IndexedFunction> classMember : superMethods) {
                    IndexedFunction ifaceMeth = classMember.getMember();
                    if (ifaceMeth.getName().startsWith(request.prefix) && !ifaceMeth.isFinal() && !methodNames.contains(ifaceMeth.getName())) {
                        for (int i = 0; i <= ifaceMeth.getOptionalArgs().length; i++) {
                            methodNames.add(ifaceMeth.getName());
                            proposals.add(new PHPCompletionItem.FunctionDeclarationItem(ifaceMeth, request, i, true));
                        }
                    }
                }

            }
            List<String> magicMethods = new ArrayList<String>();
            for (String name : PredefinedSymbols.MAGIC_METHODS.keySet()) {
                if (!methodNames.contains(name)) {
                    methodNames.add(name);
                    magicMethods.add(name);
                }
            }
            autoCompleteMagicItems(proposals, request, magicMethods, false, insideNames);
        }

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
            int attrMask = Modifier.PUBLIC;

            if (tokenSequence.token().id() == PHPTokenId.WHITESPACE) {
                tokenSequence.movePrevious();
            }
            tokenSequence.movePrevious();

            String varName = tokenSequence.token().text().toString();
            Collection<? extends TypeScope> types = Collections.emptyList();
            List<String> invalidProposalsForClsMembers = INVALID_PROPOSALS_FOR_CLS_MEMBERS;
            Model model = request.result.getModel();
            if (varName.equals("self")) { //NOI18N
                types = ModelUtils.resolveTypeAfterReferenceToken(model, tokenSequence, request.anchor);
                if (!types.isEmpty()) {
                    staticContext = true;
                    attrMask |= (Modifier.PROTECTED | Modifier.PRIVATE);
                }
            } else if (varName.equals("parent")) { //NOI18N
                invalidProposalsForClsMembers = Collections.emptyList();
                types = ModelUtils.resolveTypeAfterReferenceToken(model, tokenSequence, request.anchor);
                if (!types.isEmpty()) {
                    TypeScope type = ModelUtils.getFirst(types);
                    if (type != null) {
                        staticContext = instanceContext = true;
                        attrMask |= Modifier.PROTECTED;
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
                    attrMask |= (Modifier.PROTECTED | Modifier.PRIVATE);
                }
            } else {
                if (staticContext) {
                    if (varName.startsWith("$")) {//NOI18N
                        return;
                    }
                }
                types = ModelUtils.resolveTypeAfterReferenceToken(model, tokenSequence, request.anchor);

                if (types.isEmpty()) {
                    // XXX - refactor for 6.8+
                    // ask frameworks
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
                ClassDeclaration enclosingClass = findEnclosingClass(request.info, lexerToASTOffset(request.result, request.anchor));
                for (TypeScope typeScope : types) {
                    if (enclosingClass != null) {
                        String clsName = CodeUtils.extractClassName(enclosingClass);
                        if (clsName != null && clsName.equalsIgnoreCase(typeScope.getName())) {
                            attrMask |= (Modifier.PROTECTED | Modifier.PRIVATE);
                        }
                    }
                    String typeName = typeScope.getName();
                    boolean staticAllowed = OptionsUtils.codeCompletionStaticMethods();
                    boolean nonstaticAllowed = OptionsUtils.codeCompletionNonStaticMethods();
                    final QualifiedName qualifiedTypeName = typeScope.getNamespaceName().append(typeName);
                    if (!processedTypeNames.add(qualifiedTypeName)) continue;
                    Collection<IndexedClassMember<IndexedFunction>> methods =
                            request.index.getAllMethods(request.result,qualifiedTypeName, request.prefix, nameKind, attrMask);

                    for (IndexedClassMember<IndexedFunction> classMember: methods){
                        IndexedFunction method = classMember.getMember();
                        if ((staticContext && (method.isStatic() || nonstaticAllowed)) ||
                                (instanceContext && (!method.isStatic() || staticAllowed))) {
                            for (int i = 0; i <= method.getOptionalArgs().length; i ++){
                                if (!invalidProposalsForClsMembers.contains(method.getName())) {
                                    proposals.add(new PHPCompletionItem.FunctionItem(classMember, request, i));
                                }
                            }
                        }
                    }

                    String prefix = (staticContext && request.prefix.startsWith("$")) //NOI18N
                            ? request.prefix.substring(1) : request.prefix;
                    Collection<IndexedClassMember<IndexedConstant>> properties =
                            request.index.getAllFields(request.result, qualifiedTypeName, prefix, nameKind, attrMask);

                    for (IndexedClassMember<IndexedConstant> classMember : properties){
                        IndexedConstant prop = classMember.getMember();
                        if (staticContext && prop.isStatic() || instanceContext && !prop.isStatic()) {
                            PHPCompletionItem.VariableItem item = new PHPCompletionItem.VariableItem(classMember, request);

                            if (!staticContext) {
                                item.doNotInsertDollarPrefix();
                            }

                            proposals.add(item);
                        }
                    }

                    if (staticContext) {
                        Collection<IndexedClassMember<IndexedConstant>> allClassConstants =
                                request.index.getAllTypeConstants(request.result, qualifiedTypeName, request.prefix, nameKind);
                        for (IndexedClassMember<IndexedConstant> indexedClassMember : allClassConstants) {
                            proposals.add(new PHPCompletionItem.ClassConstantItem(indexedClassMember, request));
                        }
                    }
                }
            }
        }
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

        // end: KEYWORDS

        PHPIndex index = request.index;

        // get local toplevelvariables
        LocalVariables localVars = getLocalVariables(request.result,
                request.prefix, request.anchor, request.currentlyEditedFileURL);

        // all toplevel variables from index, which are unique. They comes only from one file
        Map<String, IndexedVariable> allUniqueVars = new LinkedHashMap<String, IndexedVariable>();
        // all toplevel variables, wchich are defined in more files
        Map<String, IndexedVariable> allUnUniqueVars = new LinkedHashMap<String, IndexedVariable>();

        NamespaceIndexFilter<IndexedElement> completionSupport = new NamespaceIndexFilter<IndexedElement>(request.prefix);
        QualifiedNameKind kind = completionSupport.getKind();
        VariablesScope ccVariablesScope = OptionsUtils.codeCompletionVariablesScope();
        Collection<IndexedElement> allTopLevel = null;
        if (ccVariablesScope.equals(VariablesScope.ALL)) {
            allTopLevel = request.index.getAllTopLevel(request.result, completionSupport.getName(), nameKind);
        } else {
            final EnumSet<PhpKind> allOf = EnumSet.<PhpKind>allOf(PhpKind.class);
            allOf.remove(PhpKind.VARIABLE);
            allTopLevel = request.index.getAllTopLevel(request.result, completionSupport.getName(), nameKind, allOf);
        }
        if (!kind.isUnqualified()) {
            allTopLevel = completionSupport.filter(allTopLevel);
        }

        //Obtain all top level statment from index
        for (IndexedElement element : allTopLevel) {
            if (element instanceof IndexedFunction) {
                IndexedFunction function = (IndexedFunction) element;
                for (int i = 0; i <= function.getOptionalArgs().length; i++) {
                    proposals.add(new PHPCompletionItem.FunctionItem(function, request, i));
                }
            }
            else if (element instanceof IndexedClass) {
                proposals.add(new PHPCompletionItem.ClassItem((IndexedClass) element, request, true, null));
            } else  if (element instanceof IndexedInterface) {
                proposals.add(new PHPCompletionItem.InterfaceItem((IndexedInterface) element, request, true));
            } else if (element instanceof IndexedVariable) {
                if (localVars.globalContext) {
                    // are we in global context?
                    IndexedVariable topLevelVar = (IndexedVariable) element;
                    if (!request.currentlyEditedFileURL.equals(topLevelVar.getFilenameUrl())) {
                        IndexedVariable localVar = allUniqueVars.get(topLevelVar.getName());
                        if (localVar == null) {
                            // the indexed variable is unique or first one, with the name
                            allUniqueVars.put(topLevelVar.getName(), topLevelVar);
                        }
                        else {
                            // already there is an variable with the same name
                            allUnUniqueVars.put(topLevelVar.getName(), topLevelVar);
                        }
                    }
                }
            }
            else if (element instanceof IndexedConstant) {
                proposals.add(new PHPCompletionItem.ConstantItem((IndexedConstant) element, request));
            }
        }


        // add local variables
        for (IndexedVariable var : localVars.vars) {
            allUniqueVars.put(var.getName(), var);
            // remove local varibales from the indexed varibles
            allUnUniqueVars.remove(var.getName());
        }

        for (IndexedVariable var : allUnUniqueVars.values()) {
            // remove ununique variables from unique varibles
            allUniqueVars.remove(var.getName());
            CompletionProposal proposal = new PHPCompletionItem.UnUniqueVaraibaleItems(var, request);
            proposals.add(proposal);
        }

        for (IndexedVariable var : allUniqueVars.values()) {
            CodeUtils.resolveFunctionType(request.result, index, allUniqueVars, var);
            CompletionProposal proposal = new PHPCompletionItem.VariableItem(var, request);
            proposals.add(proposal);
        }

        for (String name : PredefinedSymbols.SUPERGLOBALS){
            if (isPrefix("$" + name, request.prefix)) { //NOI18N
                CompletionProposal proposal = new PHPCompletionItem.SuperGlobalItem(request, name);
                proposals.add(proposal);
            }
        }

        // Special keywords applicable only inside a class
        final ClassDeclaration classDecl = findEnclosingClass(request.info, lexerToASTOffset(request.result, request.anchor));
        if (classDecl != null) {
            for (final String keyword : PHP_CLASS_KEYWORDS) {
                if (startsWith(keyword, request.prefix)) {
                    proposals.add(new PHPCompletionItem.KeywordItem(keyword, request) {

                        @Override
                        public String getLhsHtml(HtmlFormatter formatter) {
                            if (keyword.startsWith("$")) {//NOI18N
                                String clsName = CodeUtils.extractClassName(classDecl);
                                if (clsName != null) {
                                    formatter.type(true);
                                    formatter.appendText(clsName);
                                    formatter.type(false);
                                }
                                formatter.appendText(" "); //NOI18N
                            }
                            return super.getLhsHtml(formatter);
                        }
                    });
                }
            }
        }
    }
    private void autoCompleteGlobals(List<CompletionProposal> proposals, PHPCompletionItem.CompletionRequest request) {
        Map<String, IndexedVariable> allVars = new LinkedHashMap<String, IndexedVariable>();
        VariablesScope ccVariablesScope = OptionsUtils.codeCompletionVariablesScope();
        Collection<IndexedElement> allTopLevel = null;
        if (ccVariablesScope.equals(VariablesScope.ALL)) {
            allTopLevel = request.index.getAllTopLevel(request.result, request.prefix, nameKind);
        } else {
            final EnumSet<PhpKind> allOf = EnumSet.<PhpKind>allOf(PhpKind.class);
            allOf.remove(PhpKind.VARIABLE);
            allTopLevel = request.index.getAllTopLevel(request.result, request.prefix, nameKind, allOf);
        }
        for (IndexedElement element : allTopLevel) {
            if (element instanceof IndexedVariable) {
                IndexedVariable topLevelVar = (IndexedVariable) element;
                allVars.put(topLevelVar.getName(), topLevelVar);
            }
        }
        Collection<IndexedVariable> values = allVars.values();
        for (IndexedVariable idxConstant : values) {
            String tName = idxConstant.getTypeName();
            //TODO: just impl. as hotfix - should be reviewed
            if (idxConstant.isResolved() && (tName == null || !tName.startsWith("@"))) {//NOI18N
                proposals.add(new PHPCompletionItem.VariableItem(idxConstant, request));
            } else {
                proposals.add(new PHPCompletionItem.UnUniqueVaraibaleItems(idxConstant, request));
            }
        }
    }

    private Collection<CompletionProposal> getVariableProposals(Program program,
            PHPCompletionItem.CompletionRequest request){

        Collection<CompletionProposal> proposals = new ArrayList<CompletionProposal>();
        Collection<IndexedVariable> allVars = getVariables(request.result, request.index,
                request.prefix, request.anchor, request.currentlyEditedFileURL);

        for (IndexedVariable localVar : allVars){
            CompletionProposal proposal = new PHPCompletionItem.VariableItem(localVar, request);
            proposals.add(proposal);
        }

        for (String name : PredefinedSymbols.SUPERGLOBALS){
            if (isPrefix("$" + name, request.prefix)) { //NOI18N
                CompletionProposal proposal = new PHPCompletionItem.SuperGlobalItem(request, name);
                proposals.add(proposal);
            }
        }

        return proposals;
    }

    public Collection<IndexedVariable> getVariables(PHPParseResult context,  PHPIndex index,
            String namePrefix, int position, String localFileURL){

        LocalVariables localVars = getLocalVariables(context, namePrefix, position, localFileURL);
        Map<String, IndexedVariable> allVars = new LinkedHashMap<String, IndexedVariable>();

        for (IndexedVariable var : localVars.vars){
            allVars.put(var.getName(), var);
        }

        if (localVars.globalContext){
            for (IndexedVariable topLevelVar : index.getTopLevelVariables(context, namePrefix, QuerySupport.Kind.PREFIX)){
                if (!localFileURL.equals(topLevelVar.getFilenameUrl())){
                    IndexedVariable localVar = allVars.get(topLevelVar.getName());
                    // TODO this is not good solution. The varibles, which
                    // are not unique (are defined in more files),
                    // should be presented in different way. It is solved
                    // in autoCompleteExpression method. No time to rewrite for 6.5
                     if (localVar == null || localVar.getOffset() != topLevelVar.getOffset()){
                        IndexedVariable original = allVars.put(topLevelVar.getName(), topLevelVar);
                        if (original != null && localVars.vars.contains(original)) {
                            allVars.put(original.getName(), original);
                        }
                     }
                }
            }
        }

        for (IndexedVariable var : allVars.values()){
            CodeUtils.resolveFunctionType(context, index, allVars, var);
        }

        return allVars.values();
    }


    private boolean isPrefix(String name, String prefix){
        return name != null && (name.startsWith(prefix)
                || nameKind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && name.toLowerCase().startsWith(prefix.toLowerCase()));
    }


    private void autoCompleteExternals(List<CompletionProposal> proposals, CompletionRequest request) {
        FileObject fileObject = request.result.getSnapshot().getSource().getFileObject();
        EditorExtender editorExtender = PhpEditorExtender.forFileObject(fileObject);
        for (PhpElement element : editorExtender.getElementsForCodeCompletion(fileObject)) {
            proposals.add(PhpElementCompletionItem.fromPhpElement(element, request));
        }
    }

    private class LocalVariables{
        Collection<IndexedVariable> vars;
        boolean globalContext;
    }

    private LocalVariables getLocalVariables(final PHPParseResult context, final String namePrefix, final int position, final String localFileURL) {
        Map<String, IndexedVariable> localVars = new HashMap<String, IndexedVariable>();
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
                    IndexedVariable ic = new IndexedVariable(name, null, null, localFileURL, -1, 0, typeName);
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
                 tooltip = mElem.getPhpKind()+": "+parentElem.getName()+"<b> "+mElem.getName() + " </b>"+ "("+ fName+")";//NOI18N
            } else {
                tooltip = mElem.getPhpKind()+":<b> "+mElem.getName() + " </b>"+ "("+ fName+")";//NOI18N
            }
            return String.format("<div align=\"right\"><font size=-1>%s</font></div>", tooltip);
        }

        return (element instanceof MagicIndexedFunction) ? null :
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
 }
