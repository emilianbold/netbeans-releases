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
import javax.swing.ImageIcon;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.model.*;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;

/**
 *
 * @author Petr Pisl
 */
public class JsStructureScanner implements StructureScanner {

    //private static final String LAST_CORRECT_FOLDING_PROPERTY = "LAST_CORRECT_FOLDING_PROPERY";
    
    private static final String FOLD_CODE_BLOCKS = "codeblocks"; //NOI18N
    
    @Override
    public List<? extends StructureItem> scan(ParserResult info) {
        final List<StructureItem> items = new ArrayList<StructureItem>();        
        JsParserResult result = (JsParserResult) info;
        final Model model = result.getModel();
        FileScope fileScope = model.getFileScope();
        
        for(Scope scope : fileScope.getLogicalElements()) {
            if (scope instanceof FunctionScope) {
                List<StructureItem> children = new ArrayList<StructureItem>();
                children = getEmbededItems(scope, children);
                items.add(new JsFunctionStructureItem((FunctionScope)scope, children));
            } else if (scope instanceof ObjectScope) {
                List<StructureItem> children = new ArrayList<StructureItem>();
                children = getEmbededItems(scope, children);
                items.add(new JsObjectStructureItem((ObjectScope)scope, children));
            }
        }
        
        return items;
    }
    
     private List<StructureItem>  getEmbededItems(Scope scope, List<StructureItem> collectedItems) {
        
        List<? extends ModelElement> elements = scope.getElements();
        for (ModelElement element : elements) {
            if (element instanceof FunctionScope) {
                FunctionScope function = (FunctionScope) element;
                if (function.getJSKind() == JsElement.Kind.METHOD) {
                    List<StructureItem> children = new ArrayList<StructureItem>();
                    children = getEmbededItems((Scope) element, children);
                    collectedItems.add(new JsFunctionStructureItem(function, children));
                } else if (function.getJSKind() == JsElement.Kind.CONSTRUCTOR) {
                    collectedItems.add(new JsFunctionStructureItem(function, null));
                    List<StructureItem> children = new ArrayList<StructureItem>();
                    children = getEmbededItems((Scope) element, children);
                    collectedItems.addAll(children);
                }
            } else  if (element instanceof ObjectScope) {
                List<StructureItem> children = new ArrayList<StructureItem>();
                children = getEmbededItems((Scope) element, children);
                collectedItems.add(new JsObjectStructureItem((ObjectScope)element, children));
            }
        }
        return collectedItems;
    }

    private JsFunctionStructureItem createItem(FunctionScope scope, List<StructureItem> children) {
        return new JsFunctionStructureItem(scope, children);
    }
    
    private JsObjectStructureItem createItem(ObjectScope scope, List<StructureItem> children) {
        return new JsObjectStructureItem(scope, children);
    }
     
    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        final Map<String, List<OffsetRange>> folds = new HashMap<String, List<OffsetRange>>();
         
        JsParserResult result = (JsParserResult) info;
        final Model model = result.getModel();
        FileScope fileScope = model.getFileScope();
        
        
            
            List<Scope> scopes = getEmbededScopes(fileScope, null);
            for (Scope scope : scopes) {
                OffsetRange offsetRange = scope.getBlockRange();
                if (offsetRange == null) continue;
                    
                getRanges(folds, FOLD_CODE_BLOCKS).add(offsetRange);
                
            }
//            Source source = info.getSnapshot().getSource();
//            assert source != null : "source was null";
//            Document doc = source.getDocument(false);
//            
//            if (doc != null){
//                doc.putProperty(LAST_CORRECT_FOLDING_PROPERTY, folds);
//            }
            return folds;
        
        //return Collections.emptyMap();
    }
    
    private List<OffsetRange> getRanges(Map<String, List<OffsetRange>> folds, String kind) {
        List<OffsetRange> ranges = folds.get(kind);
        if (ranges == null) {
            ranges = new ArrayList<OffsetRange>();
            folds.put(kind, ranges);
        }
        return ranges;
    }
    
    private List<Scope>  getEmbededScopes(Scope scope, List<Scope> collectedScopes) {
        if (collectedScopes == null) {
            collectedScopes = new ArrayList<Scope>();
        }
        List<? extends ModelElement> elements = scope.getElements();
        for (ModelElement element : elements) {
            if (element instanceof Scope) {
                collectedScopes.add((Scope) element);
                getEmbededScopes((Scope) element, collectedScopes);
            }
        }
        return collectedScopes;
    }

    @Override
    public Configuration getConfiguration() {
        // TODO return a configuration to alow filter items. 
        return null;
    }
    
    private abstract class JsStructureItem implements StructureItem {

        private ModelElement modelElement;
        
        final private List<? extends StructureItem> children;
        final private String sortPrefix;

        public JsStructureItem(ModelElement elementHandle, List<? extends StructureItem> children, String sortPrefix) {
            this.modelElement = elementHandle;
            this.sortPrefix = sortPrefix;
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
                            && item.modelElement.getOffsetRange(null) == modelElement.getOffsetRange(null);
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
            return modelElement.getOffsetRange(null).getEnd();
        }

        @Override
        public ImageIcon getCustomIcon() {
            return null;
        }
     
        public ModelElement getModelElement() {
            return modelElement;
        }
        
    }
    
    private class JsFunctionStructureItem extends JsStructureItem {

        public JsFunctionStructureItem(FunctionScope elementHandle, List<? extends StructureItem> children) {
            super(elementHandle, children, "fn"); //NOI18N
        }

        public FunctionScope getFunctionScope() {
            return (FunctionScope) getModelElement();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
                formatter.reset();
                appendFunctionDescription(getFunctionScope(), formatter);
                return formatter.getText();
        }
        
        protected void appendFunctionDescription(FunctionScope function, HtmlFormatter formatter) {
            formatter.reset();
            if (function == null) {
                return;
            }
            formatter.appendText(getFunctionScope().getDeclarationName().getName());
            formatter.appendText("()");   //NOI18N
        }

        @Override
        public String getName() {
            return getFunctionScope().getDeclarationName().getName();
        }
    }

    private class JsObjectStructureItem extends JsStructureItem {

        public JsObjectStructureItem(ObjectScope elementHandle, List<? extends StructureItem> children) {
            super(elementHandle, children, "ob"); //NOI18N
        }

        public ObjectScope getObjectScope() {
            return (ObjectScope) getModelElement();
        }

        @Override
        public String getHtml(HtmlFormatter formatter) {
                formatter.reset();
                appendObjectDescription(getObjectScope(), formatter);
                return formatter.getText();
        }
        
        protected void appendObjectDescription(ObjectScope object, HtmlFormatter formatter) {
            formatter.reset();
            if (object == null) {
                return;
            }
            StringBuilder name = new StringBuilder();
            int identCount = object.getFQDeclarationName().size();
            for (int i = 0; i < identCount; i++){
                name.append(object.getFQDeclarationName().get(i).getName());
                if (i < (identCount - 1)) {
                    name.append(".");
                }
            }
            formatter.appendText(name.toString());
        }

    }
    
}
