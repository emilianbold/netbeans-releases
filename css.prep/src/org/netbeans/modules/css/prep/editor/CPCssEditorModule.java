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
package org.netbeans.modules.css.prep.editor;

import java.io.IOException;
import org.netbeans.modules.css.prep.editor.model.CPModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.ColoringAttributes;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.css.editor.module.spi.CompletionContext;
import org.netbeans.modules.css.editor.module.spi.CssEditorModule;
import org.netbeans.modules.css.editor.module.spi.EditorFeatureContext;
import org.netbeans.modules.css.editor.module.spi.FeatureContext;
import org.netbeans.modules.css.editor.module.spi.FutureParamTask;
import org.netbeans.modules.css.editor.module.spi.SemanticAnalyzer;
import org.netbeans.modules.css.editor.module.spi.Utilities;
import org.netbeans.modules.css.lib.api.CssTokenId;
import org.netbeans.modules.css.lib.api.Node;
import org.netbeans.modules.css.lib.api.NodeType;
import org.netbeans.modules.css.lib.api.NodeUtil;
import org.netbeans.modules.css.lib.api.NodeVisitor;
import org.netbeans.modules.css.prep.editor.model.CPElement;
import org.netbeans.modules.css.prep.editor.model.CPElementHandle;
import org.netbeans.modules.css.prep.editor.model.CPElementType;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.DependencyType;
import org.netbeans.modules.web.common.api.LexerUtils;
import org.netbeans.modules.web.common.api.Lines;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.lookup.ServiceProvider;

/**
 * CSS preprocessor {@link CssEditorModule} implementation.
 *
 * TODO fix the instant rename and the mark occurrences - they are pretty naive
 * - not scoped at all :-)
 *
 * @author marekfukala
 */
@ServiceProvider(service = CssEditorModule.class)
public class CPCssEditorModule extends CssEditorModule {

    private final SemanticAnalyzer semanticAnalyzer = new CPSemanticAnalyzer();
    private static Map<NodeType, ColoringAttributes> COLORINGS;

    @Override
    public SemanticAnalyzer getSemanticAnalyzer() {
        return semanticAnalyzer;
    }

    @Override
    public List<CompletionProposal> getCompletionProposals(final CompletionContext context) {
        final List<CompletionProposal> proposals = new ArrayList<>();

        CPModel model = CPModel.getModel(context.getParserResult());
        if (model == null) {
            return Collections.emptyList();
        }
        List<CompletionProposal> allVars = new ArrayList<>(getVariableCompletionProposals(context, model));

        //errorneous source
        TokenSequence<CssTokenId> ts = context.getTokenSequence();
        Token<CssTokenId> token = ts.token();
        if(token == null) {
            return Collections.emptyList();
        }
        CssTokenId tid = token.id();
        CharSequence ttext = token.text();
        char first = ttext.charAt(0);

        switch (tid) {
            case ERROR:
                switch (first) {
                    case '$':
                        //"$" as a prefix - user likely wants to type variable
                        //check context
                        if (NodeUtil.getAncestorByType(context.getActiveTokenNode(), NodeType.rule) != null
                                || NodeUtil.getAncestorByType(context.getActiveTokenNode(), NodeType.cp_mixin_block) != null) {
                            //in declarations node -> offer all vars
                            return Utilities.filterCompletionProposals(allVars, context.getPrefix(), true);
                        }
                        break;

                    case '@':
                        //may be:
                        //1. @-rule beginning
                        //2. less variable

                        //1.@-rule
                        proposals.addAll(Utilities.createRAWCompletionProposals(model.getDirectives(), ElementKind.KEYWORD, context.getAnchorOffset()));
                        
                        //2.less variables
                        if(model.getPreprocessorType() == CPType.LESS) {
                            proposals.addAll(allVars);
                        }
                        return Utilities.filterCompletionProposals(proposals, context.getPrefix(), true);
                }
                break;
                
            case SASS_VAR:
                //sass variable: $v|
                if(model.getPreprocessorType() == CPType.SCSS) {
                    return Utilities.filterCompletionProposals(allVars, context.getPrefix(), true);
                }

            case AT_IDENT:
                //not complete keyword (complete keyword have their own token types,
                //but no need to complete them except documentation completion request
                List<CompletionProposal> props = Utilities.createRAWCompletionProposals(model.getDirectives(), ElementKind.KEYWORD, context.getAnchorOffset());
                
                //less variable: @va|
                if(model.getPreprocessorType() == CPType.LESS) {
                    return Utilities.filterCompletionProposals(allVars, context.getPrefix(), true);
                }
                return Utilities.filterCompletionProposals(props, context.getPrefix(), true);
        }

        Node activeNode = context.getActiveNode();
        boolean isError = false;
        //skip to first non error or recovery parent
        while(activeNode.type() == NodeType.error || activeNode.type() == NodeType.recovery) {
            isError = true;
            activeNode = activeNode.parent();
        }
//        NodeUtil.dumpTree(context.getParseTreeRoot());

        switch (activeNode.type()) {
            case bodyItem:
                switch(tid) {
                    case WS:
                        //in stylesheet main body: @include |
                        //check the previous token
                        if(ts.movePrevious()) {
                            Token<CssTokenId> previousToken = ts.token();
                            if(previousToken.id() == CssTokenId.SASS_INCLUDE) {
                                //add all mixins
                                proposals.addAll(getMixinsCompletionProposals(context, model));
                            }
                        }
                        break;
                        
                    case IDENT:
                        //in stylesheet main body: @include mix|
                        if(LexerUtils.followsToken(ts, CssTokenId.SASS_INCLUDE, true, false, CssTokenId.WS) != null) {
                            //ok so the ident if preceeded by WS and then by SASS_INCLUDE token
                            proposals.addAll(getMixinsCompletionProposals(context, model));
                        }
                        break;
                }
                break;
            case cp_mixin_call:
            //@include |
            case cp_mixin_name:
                //@include mymi|
                proposals.addAll(getMixinsCompletionProposals(context, model));
                break;

            case cp_variable:
                //already in the prefix
                proposals.addAll(allVars);
                break;
            case propertyValue:
                //just $ or @ prefix
                if (context.getPrefix().length() == 1 && context.getPrefix().charAt(0) == model.getPreprocessorType().getVarPrefix()) {
                    proposals.addAll(allVars);
                }
                break;
            case declaration:
                if(tid == CssTokenId.DOT) {
                    //div { .| } -- less mixin call
                    proposals.addAll(getMixinsCompletionProposals(context, model));
                }

        }
        return Utilities.filterCompletionProposals(proposals, context.getPrefix(), true);
    }

    private static Collection<CompletionProposal> getVariableCompletionProposals(final CompletionContext context, CPModel model) {
        //filter the variable at the current location (being typed)
        Collection<CompletionProposal> proposals = new LinkedHashSet<>();
        for (CPElement var : model.getVariables(context.getCaretOffset())) {
            if (var.getType() != CPElementType.VARIABLE_USAGE && !var.getRange().containsInclusive(context.getCaretOffset())) {
                ElementHandle handle = new CPCslElementHandle(context.getFileObject(), var.getName());
                VariableCompletionItem item = new VariableCompletionItem(
                        handle,
                        var.getHandle(),
                        context.getAnchorOffset(),
                        null); //no origin for current file
//                        var.getFile() == null ? null : var.getFile().getNameExt());

                proposals.add(item);
            }
        }
        try {
            //now gather global vars from all linked sheets
            FileObject file = context.getFileObject();
            if (file != null) {
                Map<FileObject, CPCssIndexModel> indexModels = CPUtils.getIndexModels(file, DependencyType.REFERRED, true);
                for (Entry<FileObject, CPCssIndexModel> entry : indexModels.entrySet()) {
                    FileObject reff = entry.getKey();
                    CPCssIndexModel cpIndexModel = entry.getValue();
                    Collection<org.netbeans.modules.css.prep.editor.model.CPElementHandle> variables = cpIndexModel.getVariables();
                    for (org.netbeans.modules.css.prep.editor.model.CPElementHandle var : variables) {
                        if (var.getType() == CPElementType.VARIABLE_GLOBAL_DECLARATION) {
                            ElementHandle handle = new CPCslElementHandle(context.getFileObject(), var.getName());
                            VariableCompletionItem item = new VariableCompletionItem(
                                    handle,
                                    var,
                                    context.getAnchorOffset(),
                                    reff.getNameExt());

                            proposals.add(item);
                        }

                    }

                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return proposals;
    }

    private static Collection<CompletionProposal> getMixinsCompletionProposals(final CompletionContext context, CPModel model) {
        //filter the variable at the current location (being typed)
        Collection<CompletionProposal> proposals = new LinkedHashSet<>();
        for (CPElement mixin : model.getMixins()) {
            if (mixin.getType() == CPElementType.MIXIN_DECLARATION) {
                ElementHandle handle = new CPCslElementHandle(context.getFileObject(), mixin.getName());
                MixinCompletionItem item = new MixinCompletionItem(
                        handle,
                        mixin.getHandle(),
                        context.getAnchorOffset(),
                        null); //no origin for current file
//                        var.getFile() == null ? null : var.getFile().getNameExt());

                proposals.add(item);
            }
        }
        try {
            //now gather global vars from all linked sheets
            FileObject file = context.getFileObject();
            if (file != null) {
                Map<FileObject, CPCssIndexModel> indexModels = CPUtils.getIndexModels(file, DependencyType.REFERRED, true);
                for (Entry<FileObject, CPCssIndexModel> entry : indexModels.entrySet()) {
                    FileObject reff = entry.getKey();
                    CPCssIndexModel cpIndexModel = entry.getValue();
                    Collection<org.netbeans.modules.css.prep.editor.model.CPElementHandle> mixins = cpIndexModel.getMixins();
                    for (org.netbeans.modules.css.prep.editor.model.CPElementHandle mixin : mixins) {
                        if (mixin.getType() == CPElementType.MIXIN_DECLARATION) {
                            ElementHandle handle = new CPCslElementHandle(context.getFileObject(), mixin.getName());
                            MixinCompletionItem item = new MixinCompletionItem(
                                    handle,
                                    mixin,
                                    context.getAnchorOffset(),
                                    reff.getNameExt());

                            proposals.add(item);
                        }

                    }

                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return proposals;
    }

    

    @Override
    public <T extends Map<OffsetRange, Set<ColoringAttributes>>> NodeVisitor<T> getSemanticHighlightingNodeVisitor(FeatureContext context, T result) {
        final Snapshot snapshot = context.getSnapshot();
        return new NodeVisitor<T>(result) {
            @Override
            public boolean visit(Node node) {
                ColoringAttributes coloring = getColorings().get(node.type());
                if (coloring != null) {
                    int dso = snapshot.getOriginalOffset(node.from());
                    int deo = snapshot.getOriginalOffset(node.to());
                    if (dso >= 0 && deo >= 0) { //filter virtual nodes
                        //check vendor speficic property
                        OffsetRange range = new OffsetRange(dso, deo);
                        getResult().put(range, Collections.singleton(coloring));
                    }
                }
                return false;
            }
        };
    }

    private static Map<NodeType, ColoringAttributes> getColorings() {
        if (COLORINGS == null) {
            COLORINGS = new EnumMap<>(NodeType.class);
            COLORINGS.put(NodeType.cp_variable, ColoringAttributes.LOCAL_VARIABLE);
            COLORINGS.put(NodeType.cp_mixin_name, ColoringAttributes.PRIVATE);
        }
        return COLORINGS;
    }

    @Override
    public <T extends Set<OffsetRange>> NodeVisitor<T> getMarkOccurrencesNodeVisitor(EditorFeatureContext context, T result) {
        return Utilities.createMarkOccurrencesNodeVisitor(context, result, NodeType.cp_variable, NodeType.cp_mixin_name);
    }

    @Override
    public boolean isInstantRenameAllowed(EditorFeatureContext context) {
        TokenSequence<CssTokenId> tokenSequence = context.getTokenSequence();
        int diff = tokenSequence.move(context.getCaretOffset());
        if (diff > 0 && tokenSequence.moveNext() || diff == 0 && tokenSequence.movePrevious()) {
            Token<CssTokenId> token = tokenSequence.token();
            return token.id() == CssTokenId.AT_IDENT //less 
                    || token.id() == CssTokenId.SASS_VAR //sass
                    || token.id() == CssTokenId.IDENT; //sass/less mixin name

        }
        return false;
    }

    @Override
    public <T extends Set<OffsetRange>> NodeVisitor<T> getInstantRenamerVisitor(EditorFeatureContext context, T result) {
        TokenSequence<CssTokenId> tokenSequence = context.getTokenSequence();
        int diff = tokenSequence.move(context.getCaretOffset());
        if (diff > 0 && tokenSequence.moveNext() || diff == 0 && tokenSequence.movePrevious()) {
            Token<CssTokenId> token = tokenSequence.token();
            final CharSequence elementName = token.text();
            return new NodeVisitor<T>(result) {
                @Override
                public boolean visit(Node node) {
                    switch (node.type()) {
                        case cp_mixin_name:
                        case cp_variable:
                            if (LexerUtils.equals(elementName, node.image(), false, false)) {
                                OffsetRange range = new OffsetRange(node.from(), node.to());
                                getResult().add(range);
                                break;
                            }
                    }
                    return false;
                }
            };

        }
        return null;
    }

    //TODO - fix the inability to return more than one DeclarationLocation from the task - need to change the css.editor SPI
    @Override
    public Pair<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>> getDeclaration(Document document, int caretOffset) {
        //first try to find the reference span
        TokenSequence<CssTokenId> ts = LexerUtils.getJoinedTokenSequence(document, caretOffset, CssTokenId.language());
        if (ts == null) {
            return null;
        }

        OffsetRange foundRange = null;
        Token<CssTokenId> token = ts.token();
        int quotesDiff = WebUtils.isValueQuoted(ts.token().text().toString()) ? 1 : 0;
        OffsetRange range = new OffsetRange(ts.offset() + quotesDiff, ts.offset() + ts.token().length() - quotesDiff);
        CharSequence mixinName;

        //MIXINs go to declaration
        switch (token.id()) {
            case IDENT:
                mixinName = token.text();

                //check if there is @import token before
                while (ts.movePrevious() && ts.token().id() == CssTokenId.WS) {
                }

                Token t = ts.token();
                if (t != null) {
                    if (t.id() == CssTokenId.DOT || t.id() == CssTokenId.SASS_INCLUDE) {
                        //gotcha!
                        //@import xxx --sass
                        //.xxx --less
                        foundRange = range;
                    }
                }
                if (foundRange == null) {
                    return null;
                }
                final CharSequence searchedMixinName = mixinName;
                FutureParamTask<DeclarationLocation, EditorFeatureContext> callable = new FutureParamTask<DeclarationLocation, EditorFeatureContext>() {
                    @Override
                    public DeclarationLocation run(EditorFeatureContext context) {
                        //TODO - once the css.editor allows to return several DeclarationLocation from one task, update the following code!
                        //first look at the current file
                        CPModel model = CPModel.getModel(context.getParserResult());
                        for (CPElement mixin : model.getMixins()) {
                            if (mixin.getType() == CPElementType.MIXIN_DECLARATION) {
                                if (LexerUtils.equals(searchedMixinName, mixin.getName(), false, false)) {
                                    return new DeclarationLocation(context.getFileObject(), mixin.getRange().getStart());
                                }
                            }
                        }

                        //then look at the referred files
                        try {
                            Map<FileObject, CPCssIndexModel> indexModels = CPUtils.getIndexModels(context.getFileObject(), DependencyType.REFERRED, true);
                            for (Entry<FileObject, CPCssIndexModel> entry : indexModels.entrySet()) {
                                CPCssIndexModel im = entry.getValue();
                                FileObject file = entry.getKey();
                                for (CPElementHandle mixin : im.getMixins()) {
                                    if (mixin.getType() == CPElementType.MIXIN_DECLARATION
                                            && LexerUtils.equals(searchedMixinName, mixin.getName(), false, false)) {
                                        CPElement element = mixin.resolve(CPModel.getModel(file));
                                        if (element != null) {
                                            OffsetRange elementRange = element.getRange();
                                            return new DeclarationLocation(file, elementRange.getStart());
                                        }
                                    }
                                }
                            }
                        } catch (ParseException | IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                        return DeclarationLocation.NONE;
                    }
                };
                return Pair.<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>>of(foundRange, callable);

            case SASS_VAR:
            case AT_IDENT: //less var //TODO - add default directives - see the css grammar file comment about that
                //cp variable
                final String varName = token.text().toString();
                foundRange = new OffsetRange(ts.offset(), ts.offset() + ts.token().length());

                callable = new FutureParamTask<DeclarationLocation, EditorFeatureContext>() {
                    @Override
                    public DeclarationLocation run(EditorFeatureContext context) {
                        //TODO - once the css.editor allows to return several DeclarationLocation from one task, update the following code!
                        //first look at the current file
                        CPModel model = CPModel.getModel(context.getParserResult());
                        for (CPElement var : model.getVariables()) {
                            if (var.getType().isOfTypes(CPElementType.VARIABLE_GLOBAL_DECLARATION, CPElementType.VARIABLE_LOCAL_DECLARATION, CPElementType.VARIABLE_DECLARATION_IN_BLOCK_CONTROL)) {
                                if (LexerUtils.equals(varName, var.getName(), false, false)) {
                                    return new DeclarationLocation(context.getFileObject(), var.getRange().getStart());
                                }
                            }
                        }
                        try {
                            //then look at the referred files
                            Map<FileObject, CPCssIndexModel> indexModels = CPUtils.getIndexModels(context.getFileObject(), DependencyType.REFERRED, true);
                            for (Entry<FileObject, CPCssIndexModel> entry : indexModels.entrySet()) {
                                CPCssIndexModel im = entry.getValue();
                                FileObject file = entry.getKey();
                                for (CPElementHandle var : im.getVariables()) {
                                    if (var.getType() == CPElementType.VARIABLE_GLOBAL_DECLARATION && var.getName().equals(varName)) {
                                        CPElement element = var.resolve(CPModel.getModel(file));
                                        if (element != null) {
                                            OffsetRange elementRange = element.getRange();
                                            return new DeclarationLocation(file, elementRange.getStart());
                                        }
                                    }
                                }
                            }
                        } catch (                ParseException | IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }

                        return DeclarationLocation.NONE;
                    }
                };
                return Pair.<OffsetRange, FutureParamTask<DeclarationLocation, EditorFeatureContext>>of(foundRange, callable);

            default:
                return null;
        }

    }

    @Override
    public <T extends Map<String, List<OffsetRange>>> NodeVisitor<T> getFoldsNodeVisitor(FeatureContext context, T result) {
        final Snapshot snapshot = context.getSnapshot();
        final Lines lines = new Lines(snapshot.getText());

        return new NodeVisitor<T>(result) {
            @Override
            public boolean visit(Node node) {
                switch (node.type()) {
                    case sass_control_block:
                        //find the ruleSet curly brackets and create the fold between them inclusive
                        int from = node.from();
                        int to = node.to();
                        try {
                            //do not creare one line folds
                            if (lines.getLineIndex(from) < lines.getLineIndex(to)) {
                                List<OffsetRange> codeblocks = getResult().get("codeblocks"); //NOI18N
                                if (codeblocks == null) {
                                    codeblocks = new ArrayList<>();
                                    getResult().put("codeblocks", codeblocks); //NOI18N
                                }

                                codeblocks.add(new OffsetRange(from, to));
                            }
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                }
                return false;
            }
        };

    }
    
    @Override
    public <T extends List<StructureItem>> NodeVisitor<T> getStructureItemsNodeVisitor(FeatureContext context, T result) {

        final Set<StructureItem> vars = new HashSet<>();
        final Set<StructureItem> mixins = new HashSet<>();

        CPModel model = CPModel.getModel(context.getParserResult());
        for(CPElement element : model.getElements()) {
            switch(element.getType()) {
                case MIXIN_DECLARATION:
                    mixins.add(new CPStructureItem.Mixin(element));
                    break;
//                case VARIABLE_DECLARATION_IN_BLOCK_CONTROL:
                case VARIABLE_GLOBAL_DECLARATION:
//                case VARIABLE_LOCAL_DECLARATION:
                    vars.add(new CPStructureItem.Variable(element));
                    break;
            }
        }
        
        if(!vars.isEmpty()) {
            result.add(new CPCategoryStructureItem.Variables(vars));
        }
        if(!mixins.isEmpty()) {
            result.add(new CPCategoryStructureItem.Mixins(mixins, context));
        }

        //XXX ugly - we need no visitor, but still forced to return one
        return new NodeVisitor<T>() {
            @Override
            public boolean visit(Node node) {
                return true;
            }
        };
        

    }

    
}
