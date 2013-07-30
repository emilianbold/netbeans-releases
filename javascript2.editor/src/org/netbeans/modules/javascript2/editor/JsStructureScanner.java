/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.api.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.model.*;
import org.netbeans.modules.javascript2.editor.model.impl.ModelUtils;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Petr Pisl
 */
public class JsStructureScanner implements StructureScanner {

    //private static final String LAST_CORRECT_FOLDING_PROPERTY = "LAST_CORRECT_FOLDING_PROPERY";

    private static final String FOLD_FUNCTION = "codeblocks"; //NOI18N
    private static final String FOLD_JSDOC = "comments"; //NOI18N
    private static final String FOLD_COMMENT = "initial-comment"; //NOI18N
    private static final String FOLD_OTHER_CODE_BLOCKS = "othercodeblocks"; //NOI18N

    private static final String FONT_GRAY_COLOR = "<font color=\"#999999\">"; //NOI18N
    private static final String CLOSE_FONT = "</font>";                   //NOI18N

    private static final Logger LOGGER = Logger.getLogger(JsStructureScanner.class.getName());

    private final Language<JsTokenId> language;

    public JsStructureScanner(Language<JsTokenId> language) {
        this.language = language;
    }

    @Override
    public List<? extends StructureItem> scan(ParserResult info) {
        final List<StructureItem> items = new ArrayList<StructureItem>();
        long start = System.currentTimeMillis();
        LOGGER.log(Level.FINE, "Structure scanner started at {0} ms", start);
        JsParserResult result = (JsParserResult) info;
        final Model model = result.getModel();
        JsObject globalObject = model.getGlobalObject();
        
        getEmbededItems(result, globalObject, items);
        long end = System.currentTimeMillis();
        LOGGER.log(Level.FINE, "Creating structure took {0} ms", new Object[]{(end - start)});
        return items;
    }
    
    private List<StructureItem> getEmbededItems(JsParserResult result, JsObject jsObject, List<StructureItem> collectedItems) {
        Collection<? extends JsObject> properties = jsObject.getProperties().values();
        boolean countFunctionChild = (jsObject.getJSKind().isFunction() && !jsObject.isAnonymous() && jsObject.getJSKind() != JsElement.Kind.CONSTRUCTOR
                && !containsFunction(jsObject)) 
                || (ModelUtils.PROTOTYPE.equals(jsObject.getName()) && properties.isEmpty());
        
        for (JsObject child : properties) {
            // we do not want to show items from virtual source
            if (result.getSnapshot().getOriginalOffset(child.getOffset()) < 0 && !ModelUtils.PROTOTYPE.equals(child.getName())) {
                continue;
            }
            List<StructureItem> children = new ArrayList<StructureItem>();
            if ((((countFunctionChild && !child.getModifiers().contains(Modifier.STATIC)
                    && !child.getName().equals(ModelUtils.PROTOTYPE)) || child.getJSKind() == JsElement.Kind.ANONYMOUS_OBJECT) &&  child.getJSKind() != JsElement.Kind.OBJECT_LITERAL)
                    || (child.getJSKind().isFunction() && child.isAnonymous() && child.getParent().getJSKind().isFunction() && child.getParent().getJSKind() != JsElement.Kind.FILE)) {
                // don't count children for functions and methods and anonyms
                continue;
            }
            children = getEmbededItems(result, child, children);
            if ((child.hasExactName() || child.isAnonymous()) && child.getJSKind().isFunction()) {
                JsFunction function = (JsFunction)child;
                if (function.isAnonymous()) {
                    collectedItems.addAll(children);
                } else {
                    if (function.isDeclared() && (!jsObject.isAnonymous() || (jsObject.isAnonymous() && jsObject.getFullyQualifiedName().indexOf('.') == -1))) {
                        collectedItems.add(new JsFunctionStructureItem(function, children, result));                          
                    }
                }
            } else if (((child.getJSKind() == JsElement.Kind.OBJECT && (children.size() > 0 || child.isDeclared())) || child.getJSKind() == JsElement.Kind.OBJECT_LITERAL || child.getJSKind() == JsElement.Kind.ANONYMOUS_OBJECT) 
                    && (children.size() > 0 || child.isDeclared())) {
                collectedItems.add(new JsObjectStructureItem(child, children, result));
            } else if (child.getJSKind() == JsElement.Kind.PROPERTY) {
                if(child.isDeclared() && (child.getModifiers().contains(Modifier.PUBLIC)
                        || !(jsObject.getParent() instanceof JsFunction)))
                collectedItems.add(new JsSimpleStructureItem(child, "prop-", result)); //NOI18N
            } else if (child.getJSKind() == JsElement.Kind.VARIABLE && child.isDeclared()
                && (!jsObject.isAnonymous() || (jsObject.isAnonymous() && jsObject.getFullyQualifiedName().indexOf('.') == -1))) {
                    collectedItems.add(new JsSimpleStructureItem(child, "var-", result)); //NOI18N
            }
         }
        
        if (jsObject instanceof JsFunction) {
            for (JsObject param: ((JsFunction)jsObject).getParameters()) {
                if (hasDeclaredProperty(param)) { 
                    final List<StructureItem> items = new ArrayList<StructureItem>();
                    getEmbededItems(result, param, items);
                    collectedItems.add(new JsObjectStructureItem(param, items, result));
                }
            }
        }
        return collectedItems;
    }

    private boolean containsFunction(JsObject jsObject) {
        for (JsObject property: jsObject.getProperties().values()) {
            if (property.getJSKind().isFunction() && property.isDeclared() && !property.isAnonymous()) {
                return true;
            }
        }
        return false;
    }

    private boolean isNotAnonymousFunction(TokenSequence ts, int functionKeywordPosition) {
        // expect that the ts in on "{"
        int position = ts.offset();
        boolean value = false;
        // find the function keyword
        ts.move(functionKeywordPosition);
        ts.moveNext();
        Token<? extends JsTokenId> token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE));
        if ((token.id() == JsTokenId.OPERATOR_ASSIGNMENT || token.id() == JsTokenId.OPERATOR_COLON) && ts.movePrevious()) {
            token = LexUtilities.findPrevious(ts, Arrays.asList(JsTokenId.WHITESPACE));
            if (token.id() == JsTokenId.IDENTIFIER) {
                // it's:
                // name : function() ...
                // name = function() ...
                value = true;
            }
        }
        if (!value) {
            ts.move(functionKeywordPosition);
            ts.moveNext(); ts.moveNext();
            token = LexUtilities.findNext(ts, Arrays.asList(JsTokenId.WHITESPACE));
            if (token.id() == JsTokenId.IDENTIFIER) {
                value = true;
            }
        }
        ts.move(position);
        ts.moveNext();
        return value;
    }

    private static class FoldingItem {
        String kind;
        int start;

        public FoldingItem(String kind, int start) {
            this.kind = kind;
            this.start = start;
        }
        
    }
    
    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        long start = System.currentTimeMillis();
        final Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();
         
        TokenHierarchy th = info.getSnapshot().getTokenHierarchy();
        TokenSequence ts = th.tokenSequence(language);
        List<TokenSequence<?>> list = th.tokenSequenceList(ts.languagePath(), 0, info.getSnapshot().getText().length());
        List<FoldingItem> stack = new ArrayList<FoldingItem>();

        for (TokenSequenceIterator tsi = new TokenSequenceIterator(list, false); tsi.hasMore();) {
            ts = tsi.getSequence();

            TokenId tokenId;
            JsTokenId lastContextId = null;
            int functionKeywordPosition = 0;
            ts.moveStart();
            while (ts.moveNext()) {
                tokenId = ts.token().id();
                if (tokenId == JsTokenId.DOC_COMMENT) {
                    // hardcoded values should be ok since token comes in case if it's completed (/** ... */)
                    int startOffset = ts.offset() + 3;
                    int endOffset = ts.offset() + ts.token().length() - 2;
                    appendFold(folds, FOLD_JSDOC,  info.getSnapshot().getOriginalOffset(startOffset),
                            info.getSnapshot().getOriginalOffset(endOffset));
                } else if (tokenId == JsTokenId.BLOCK_COMMENT) {
                    int startOffset = ts.offset() + 2;
                    int endOffset = ts.offset() + ts.token().length() - 2;
                    appendFold(folds, FOLD_COMMENT, info.getSnapshot().getOriginalOffset(startOffset),
                            info.getSnapshot().getOriginalOffset(endOffset));
                } else if (((JsTokenId) tokenId).isKeyword()) {
                    lastContextId = (JsTokenId) tokenId;
                    if(lastContextId == JsTokenId.KEYWORD_FUNCTION) {
                        functionKeywordPosition = ts.offset();
                    }
                } else if (tokenId == JsTokenId.BRACKET_LEFT_CURLY) {
                    String kind;
                    if (lastContextId == JsTokenId.KEYWORD_FUNCTION && isNotAnonymousFunction(ts, functionKeywordPosition)) {
                        kind = FOLD_FUNCTION;
                    } else {
                        kind = FOLD_OTHER_CODE_BLOCKS;
                    }
                    stack.add(new FoldingItem(kind, ts.offset()));
                } else if (tokenId == JsTokenId.BRACKET_RIGHT_CURLY && !stack.isEmpty()) {
                    FoldingItem fromStack = stack.remove(stack.size() - 1);
                    appendFold(folds, fromStack.kind, info.getSnapshot().getOriginalOffset(fromStack.start),
                            info.getSnapshot().getOriginalOffset(ts.offset() + 1));
                }
            }
        }
        long end = System.currentTimeMillis();
        LOGGER.log(Level.FINE, "Folding took %s ms", (end - start));
        return folds;
    }

    private void appendFold(Map<String, List<OffsetRange>> folds, String kind, int startOffset, int endOffset) {
        if (startOffset >= 0 && endOffset >= startOffset) {
            getRanges(folds, kind).add(new OffsetRange(startOffset, endOffset));
        }
    }

    private List<OffsetRange> getRanges(Map<String, List<OffsetRange>> folds, String kind) {
        List<OffsetRange> ranges = folds.get(kind);
        if (ranges == null) {
            ranges = new ArrayList<OffsetRange>();
            folds.put(kind, ranges);
        }
        return ranges;
    }

    @Override
    public Configuration getConfiguration() {
        // TODO return a configuration to alow filter items. 
        return null;
    }

    private boolean hasDeclaredProperty(JsObject jsObject) {
        boolean result =  false;
        
        Iterator<? extends JsObject> it = jsObject.getProperties().values().iterator();
        while (!result && it.hasNext()) {
            JsObject property = it.next();
            result = property.isDeclared();
            if (!result) {
                result = hasDeclaredProperty(property);
            }
        }

        return result;
    }
    
    
    private abstract class JsStructureItem implements StructureItem {

        private JsObject modelElement;
        
        final private List<? extends StructureItem> children;
        final private String sortPrefix;
        final protected JsParserResult parserResult;
        final private String fqn;
        
        public JsStructureItem(JsObject elementHandle, List<? extends StructureItem> children, String sortPrefix, JsParserResult parserResult) {
            this.modelElement = elementHandle;
            this.sortPrefix = sortPrefix;
            this.parserResult = parserResult;
            this.fqn = modelElement.getFullyQualifiedName();
            if (children != null) {
                this.children = children;
            } else {
                this.children = Collections.emptyList();
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final JsStructureItem other = (JsStructureItem) obj;
            if ((this.fqn == null) ? (other.fqn != null) : !this.fqn.equals(other.fqn)) {
                return false;
            }
            if ((this.modelElement == null && other.modelElement != null)
                    || (this.modelElement != null && other.modelElement == null)) {
                return false;
            }
            if (modelElement != other.modelElement) {
                if ((this.modelElement.getJSKind() == null) ? (other.modelElement.getJSKind() != null) :
                        !this.modelElement.getJSKind().equals(other.modelElement.getJSKind())) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + (this.fqn != null ? this.fqn.hashCode() : 0);
            hash = 37 * hash + (this.modelElement != null && this.modelElement.getJSKind() != null ?this.modelElement.getJSKind().hashCode() : 0);
            return hash;
        }

        @Override
        public String getName() {
            return modelElement.getName();
        }

        @Override
        public String getSortText() {
            return sortPrefix + modelElement.getName();
        }

        @Override
        public ElementHandle getElementHandle() {
            return modelElement;
        }

        @Override
        public ElementKind getKind() {
              return modelElement.getKind();
        }

        @Override
        public Set<Modifier> getModifiers() {
            return modelElement.getModifiers();
        }

        @Override
        public boolean isLeaf() {
            return children.isEmpty();
        }

        @Override
        public List<? extends StructureItem> getNestedItems() {
            return children;
        }

        @Override
        public long getPosition() {
            return parserResult.getSnapshot().getOriginalOffset(modelElement.getOffset());
        }

        @Override
        public long getEndPosition() {
            return parserResult.getSnapshot().getOriginalOffset(modelElement.getOffsetRange().getEnd());
        }

        @Override
        public ImageIcon getCustomIcon() {
            return null;
        }
     
        public JsObject getModelElement() {
            return modelElement;
        }
        
        protected void appendTypeInfo(HtmlFormatter formatter, Collection<? extends Type> types) {
            Collection<String> displayNames = Utils.getDisplayNames(types);
            if (!displayNames.isEmpty()) {
                formatter.appendHtml(FONT_GRAY_COLOR);
                formatter.appendText(" : ");
                boolean addDelimiter = false;
                for (String displayName : displayNames) {
                    if (addDelimiter) {
                        formatter.appendText("|");
                    } else {
                        addDelimiter = true;
                    }
                    formatter.appendHtml(displayName);
                }
                formatter.appendHtml(CLOSE_FONT);
            }
        }
        
    }
    
    private static  ImageIcon priviligedIcon = null;
    
    private class JsFunctionStructureItem extends JsStructureItem {

        private final List<TypeUsage> resolvedTypes;

        public JsFunctionStructureItem(JsFunction elementHandle, List<? extends StructureItem> children, JsParserResult parserResult) {
            super(elementHandle, children, "fn", parserResult); //NOI18N
            Collection<? extends TypeUsage> returnTypes = getFunctionScope().getReturnTypes();
            resolvedTypes = new ArrayList<TypeUsage>(ModelUtils.resolveTypes(returnTypes, parserResult));
        }

        public final JsFunction getFunctionScope() {
            return (JsFunction) getModelElement();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            appendFunctionDescription(getFunctionScope(), formatter);
            return formatter.getText();
        }
        
        protected void appendFunctionDescription(JsFunction function, HtmlFormatter formatter) {
            formatter.reset();
            if (function == null) {
                return;
            }
            boolean isDeprecated = getFunctionScope().isDeprecated();
            if (isDeprecated) {
                formatter.deprecated(true);
            }
            formatter.appendText(getFunctionScope().getDeclarationName().getName());
            if (isDeprecated) {
                formatter.deprecated(false);
            }
            formatter.appendText("(");   //NOI18N
            boolean addComma = false;
            for(JsObject jsObject : function.getParameters()) {
                if (addComma) {
                    formatter.appendText(", "); //NOI8N
                } else {
                    addComma = true;
                }
                Collection<? extends TypeUsage> types = jsObject.getAssignmentForOffset(jsObject.getDeclarationName().getOffsetRange().getStart());
                if (!types.isEmpty()) {
                    formatter.appendHtml(FONT_GRAY_COLOR);
                    StringBuilder typeSb = new StringBuilder();
                    for (TypeUsage type : types) {

                        if (typeSb.length() > 0) {
                            typeSb.append("|"); //NOI18N
                        }
                        typeSb.append(type.getType());
                    }
                    if (typeSb.length() > 0) {
                        formatter.appendText(typeSb.toString());
                    }
                    formatter.appendText(" ");   //NOI18N
                    formatter.appendHtml(CLOSE_FONT);
                }
                formatter.appendText(jsObject.getName());
            }
            formatter.appendText(")");   //NOI18N
            appendTypeInfo(formatter, resolvedTypes);
        }

        @Override
        public String getName() {
            return getFunctionScope().getDeclarationName().getName();
        }

        @Override
        public ImageIcon getCustomIcon() {
            if (getModifiers().contains(Modifier.PROTECTED)) {
                if(priviligedIcon == null) {
                    priviligedIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/javascript2/editor/resources/methodPriviliged.png")); //NOI18N
                }
                return priviligedIcon;
            }
            return super.getCustomIcon();
        }
        
        
    }

    private class JsObjectStructureItem extends JsStructureItem {

        public JsObjectStructureItem(JsObject elementHandle, List<? extends StructureItem> children, JsParserResult parserResult) {
            super(elementHandle, children, "ob", parserResult); //NOI18N
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
                formatter.reset();
                appendObjectDescription(getModelElement(), formatter);
                return formatter.getText();
        }
        
        protected void appendObjectDescription(JsObject object, HtmlFormatter formatter) {
            formatter.reset();
            if (object == null) {
                return;
            }
            boolean isDeprecated = object.isDeprecated();
            if (isDeprecated) {
                formatter.deprecated(true);
            }
            formatter.appendText(object.getName());
            if (isDeprecated) {
                formatter.deprecated(false);
            }
        }

    }
    
    private class JsSimpleStructureItem extends JsStructureItem {

        private final JsObject object;

        private final List<TypeUsage> resolvedTypes;
        
        public JsSimpleStructureItem(JsObject elementHandle, String sortPrefix, JsParserResult parserResult) {
            super(elementHandle, null, sortPrefix, parserResult);
            this.object = elementHandle;

            Collection<? extends TypeUsage> assignmentForOffset = object.getAssignmentForOffset(object.getDeclarationName().getOffsetRange().getEnd());
            resolvedTypes = new ArrayList<TypeUsage>(ModelUtils.resolveTypes(assignmentForOffset, parserResult));
        }

        
        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            boolean isDeprecated = object.isDeprecated();
            if (isDeprecated) {
                formatter.deprecated(true);
            }
            formatter.appendText(getElementHandle().getName());
            if (isDeprecated) {
                formatter.deprecated(false);
            }
            appendTypeInfo(formatter, resolvedTypes);
            return formatter.getText();
        }
        
    }
    
    private static final class TokenSequenceIterator {

        private final List<TokenSequence<?>> list;
        private final boolean backward;

        private int index;

        public TokenSequenceIterator(List<TokenSequence<?>> list, boolean backward) {
            this.list = list;
            this.backward = backward;
            this.index = -1;
        }

        public boolean hasMore() {
            return backward ? hasPrevious() : hasNext();
        }

        public TokenSequence<?> getSequence() {
            assert index >= 0 && index < list.size() : "No sequence available, call hasMore() first."; //NOI18N
            return list.get(index);
        }

        private boolean hasPrevious() {
            boolean anotherSeq = false;

            if (index == -1) {
                index = list.size() - 1;
                anotherSeq = true;
            }

            for( ; index >= 0; index--) {
                TokenSequence<?> seq = list.get(index);
                if (anotherSeq) {
                    seq.moveEnd();
                }

                if (seq.movePrevious()) {
                    return true;
                }

                anotherSeq = true;
            }

            return false;
        }

        private boolean hasNext() {
            boolean anotherSeq = false;

            if (index == -1) {
                index = 0;
                anotherSeq = true;
            }

            for( ; index < list.size(); index++) {
                TokenSequence<?> seq = list.get(index);
                if (anotherSeq) {
                    seq.moveStart();
                }

                if (seq.moveNext()) {
                    return true;
                }

                anotherSeq = true;
            }

            return false;
        }
    }
}
