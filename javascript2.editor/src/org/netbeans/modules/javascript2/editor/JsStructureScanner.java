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
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.index.JsIndex;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.model.*;
import org.netbeans.modules.javascript2.editor.model.impl.ModelUtils;
import org.netbeans.modules.javascript2.editor.model.impl.TypeUsageImpl;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.openide.filesystems.FileObject;
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
    
    @Override
    public List<? extends StructureItem> scan(ParserResult info) {
        final List<StructureItem> items = new ArrayList<StructureItem>();        
        JsParserResult result = (JsParserResult) info;
        final Model model = result.getModel();
        JsObject globalObject = model.getGlobalObject();
        
        getEmbededItems(result, globalObject, items);
        return items;
    }
    
    private List<StructureItem> getEmbededItems(JsParserResult result, JsObject jsObject, List<StructureItem> collectedItems) {
        Collection<? extends JsObject> properties = jsObject.getProperties().values();
        boolean countFunctionChild = (jsObject.getJSKind().isFunction() && !jsObject.isAnonymous() && jsObject.getJSKind() != JsElement.Kind.CONSTRUCTOR
                && !containsFunction(jsObject)) 
                || ("prototype".equals(jsObject.getName()) && properties.isEmpty());
        
        for (JsObject child : properties) {
            List<StructureItem> children = new ArrayList<StructureItem>();
            if (countFunctionChild &&  !child.getModifiers().contains(Modifier.STATIC)) {
                // don't count children for functions and methods
                continue;
            }
            children = getEmbededItems(result, child, children);
            if ((child.hasExactName() || child.isAnonymous()) && child.getJSKind().isFunction()) {
                JsFunction function = (JsFunction)child;
                if (function.isAnonymous()) {
                    collectedItems.addAll(children);
                } else {
                    if (function.isDeclared()) {
                        collectedItems.add(new JsFunctionStructureItem(function, children, result));
                    }
                }
            } else if ((child.getJSKind() == JsElement.Kind.OBJECT || child.getJSKind() == JsElement.Kind.OBJECT_LITERAL || child.getJSKind() == JsElement.Kind.ANONYMOUS_OBJECT) 
                    && (children.size() > 0 || child.isDeclared())) {
                collectedItems.add(new JsObjectStructureItem(child, children, result));
            } else if (child.getJSKind() == JsElement.Kind.PROPERTY) {
                if(child.getModifiers().contains(Modifier.PUBLIC)
                        || !(jsObject.getParent() instanceof JsFunction))
                collectedItems.add(new JsSimpleStructureItem(child, "prop-", result)); //NOI18N
            } else if (child.getJSKind() == JsElement.Kind.VARIABLE && child.isDeclared()
                    /*&& (jsObject.getJSKind() == JsElement.Kind.FILE || jsObject.getJSKind() == JsElement.Kind.CONSTRUCTOR)*/) {
                collectedItems.add(new JsSimpleStructureItem(child, "var-", result)); //NOI18N
            }
         }
        return collectedItems;
    }

    private boolean containsFunction(JsObject jsObject) {
        for (JsObject property: jsObject.getProperties().values()) {
            if (property.getJSKind().isFunction() && property.isDeclared()) {
                return true;
            }
        }
        return false;
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
        TokenSequence ts = th.tokenSequence(JsTokenId.javascriptLanguage());
        List<TokenSequence<?>> list = th.tokenSequenceList(ts.languagePath(), 0, info.getSnapshot().getText().length());
        List<FoldingItem> stack = new ArrayList<FoldingItem>();

        for (TokenSequenceIterator tsi = new TokenSequenceIterator(list, false); tsi.hasMore();) {
            ts = tsi.getSequence();

            TokenId tokenId;
            JsTokenId lastContextId = null;
            while (ts.moveNext()) {
                tokenId = ts.token().id();
                if (tokenId == JsTokenId.DOC_COMMENT) {
                    // hardcoded values should be ok since token comes in case if it's completed (/** ... */)
                    int startOffset = ts.offset() + 3;
                    int endOffset = ts.offset() + ts.token().length() - 2;
                    getRanges(folds, FOLD_JSDOC).add(new OffsetRange(
                            info.getSnapshot().getOriginalOffset(startOffset), 
                            info.getSnapshot().getOriginalOffset(endOffset)));
                } else if (tokenId == JsTokenId.BLOCK_COMMENT) {
                    int startOffset = ts.offset() + 2;
                    int endOffset = ts.offset() + ts.token().length() - 2;
                    getRanges(folds, FOLD_COMMENT).add(new OffsetRange(
                            info.getSnapshot().getOriginalOffset(startOffset), 
                            info.getSnapshot().getOriginalOffset(endOffset)));
                } else if (((JsTokenId) tokenId).isKeyword()) {
                    lastContextId = (JsTokenId) tokenId;
                } else if (tokenId == JsTokenId.BRACKET_LEFT_CURLY) {
                    String kind;
                    if (lastContextId == JsTokenId.KEYWORD_FUNCTION) {
                        kind = FOLD_FUNCTION;
                    } else {
                        kind = FOLD_OTHER_CODE_BLOCKS;
                    }
                    stack.add(new FoldingItem(kind, ts.offset()));
                } else if (tokenId == JsTokenId.BRACKET_RIGHT_CURLY && !stack.isEmpty()) {
                    FoldingItem fromStack = stack.remove(stack.size() - 1);
                    getRanges(folds, fromStack.kind).add(new OffsetRange(
                            info.getSnapshot().getOriginalOffset(fromStack.start),
                            info.getSnapshot().getOriginalOffset(ts.offset() + 1)));
                }
            }
        }
        long end = System.currentTimeMillis();
        LOGGER.log(Level.FINE, "Folding took %s ms", (end - start));
        return folds;
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
        if (jsObject.isDeclared()) {
            result = true;
        } else {
            Iterator <? extends JsObject> it = jsObject.getProperties().values().iterator();
            while (!result && it.hasNext()) {
                result = hasDeclaredProperty(it.next());
            }
        }
        return result;
    }
    
    
    private abstract class JsStructureItem implements StructureItem {

        private JsObject modelElement;
        
        final private List<? extends StructureItem> children;
        final private String sortPrefix;
        final protected JsParserResult parserResult;
        
        public JsStructureItem(JsObject elementHandle, List<? extends StructureItem> children, String sortPrefix, JsParserResult parserResult) {
            this.modelElement = elementHandle;
            this.sortPrefix = sortPrefix;
            this.parserResult = parserResult;
            if (children != null) {
                this.children = children;
            } else {
                this.children = Collections.emptyList();
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            boolean thesame = false;
            if (obj instanceof JsStructureItem) {
                JsStructureItem item = (JsStructureItem) obj;
                if (item.getName() != null && this.getName() != null) {
                    thesame = item.modelElement.getName().equals(modelElement.getName()) 
                            && item.modelElement.getOffsetRange() == modelElement.getOffsetRange();
                }
            }
            return thesame;
        }
        
        @Override
        public int hashCode() {
            int hashCode = 11;
            if (getName() != null) {
                hashCode = 31 * getName().hashCode() + hashCode;
            }
            hashCode = (int) (31 * getPosition() + hashCode);
            return hashCode;
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
            return modelElement.getOffset();
        }

        @Override
        public long getEndPosition() {
            return modelElement.getOffsetRange().getEnd();
        }

        @Override
        public ImageIcon getCustomIcon() {
            return null;
        }
     
        public JsObject getModelElement() {
            return modelElement;
        }
        
        protected void appendTypeInfo(HtmlFormatter formatter, Collection<? extends Type> types) {
            if (!types.isEmpty()) {
                formatter.appendHtml(FONT_GRAY_COLOR);
                formatter.appendText(" : ");
                boolean addDelimiter = false;
                for (Type type : types) {
                    if (addDelimiter) {
                        formatter.appendText("|");
                    } else {
                        addDelimiter = true;
                    }
                    formatter.appendHtml(type.getType());
                }
                formatter.appendHtml(CLOSE_FONT);
            }
        }
        
    }
    
    private static  ImageIcon priviligedIcon = null;
    
    private class JsFunctionStructureItem extends JsStructureItem {
        
        public JsFunctionStructureItem(JsFunction elementHandle, List<? extends StructureItem> children, JsParserResult parserResult) {
            super(elementHandle, children, "fn", parserResult); //NOI18N
        }

        public JsFunction getFunctionScope() {
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
            formatter.appendText(getFunctionScope().getDeclarationName().getName());
            formatter.appendText("(");   //NOI18N
            formatter.parameters(true);
            boolean addComma = false;
            for(JsObject jsObject : function.getParameters()) {
                if (addComma) {
                    formatter.appendText(", "); //NOI8N
                } else {
                    addComma = true;
                }
                formatter.appendText(jsObject.getName());
            }
            formatter.parameters(false);
            formatter.appendText(")");   //NOI18N
            
            appendTypeInfo(formatter, function.getReturnTypes());
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
            formatter.appendText(object.getName());
        }

    }
    
    private class JsSimpleStructureItem extends JsStructureItem {
        private final JsObject object;
        
        public JsSimpleStructureItem(JsObject elementHandle, String sortPrefix, JsParserResult parserResult) {
            super(elementHandle, null, sortPrefix, parserResult);
            this.object = elementHandle;
        }

        
        @Override
        public String getHtml(HtmlFormatter formatter) {
            formatter.reset();
            formatter.appendText(getElementHandle().getName());
            Collection<? extends Type> types = object.getAssignmentForOffset(object.getDeclarationName().getOffsetRange().getEnd());
            Model model = parserResult.getModel();
            FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
            JsIndex jsIndex = JsIndex.get(fo);
            int cycle = 0;
            boolean resolvedAll = false;
            while(!resolvedAll && cycle < 10) {
                cycle++;
                resolvedAll = true;
                Collection<Type> resolved = new ArrayList<Type>();
                for (Type typeUsage : types) {
                    if(!((TypeUsageImpl)typeUsage).isResolved()) {
                        resolvedAll = false;
                        String sexp = typeUsage.getType();
                        if (sexp.startsWith("@exp;")) {
                            sexp = sexp.substring(5);
                            List<String> nExp = new ArrayList<String>();
                            String[] split = sexp.split("@call;");
                            for (int i = split.length - 1; i > -1; i--) {
                                nExp.add(split[i]);
                                if (i == 0) {
                                    nExp.add("@pro");
                                } else {
                                    nExp.add("@mtd");
                                }
                            }
                            resolved.addAll(ModelUtils.resolveTypeFromExpression(model, jsIndex, nExp, cycle));
                        } else {
                            resolved.add(new TypeUsageImpl(typeUsage.getType(), typeUsage.getOffset(), true));
                        }
                    } else {
                        resolved.add((TypeUsage)typeUsage);
                    }
                }
                types.clear();
                types = new ArrayList<Type>(resolved);
            }
            appendTypeInfo(formatter, types);
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
