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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.CompletionProposal;
import org.netbeans.modules.gsf.api.ElementHandle;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.HtmlFormatter;
import org.netbeans.modules.gsf.api.Modifier;
import org.netbeans.modules.php.editor.index.IndexedClass;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.IndexedElement;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.parser.PHPParseResult;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
class PHPCompletionItem implements CompletionProposal {

    protected final CompletionRequest request;
    private final ElementHandle element;

    PHPCompletionItem(ElementHandle element, CompletionRequest request) {
        this.request = request;
        this.element = element;
    }

    public int getAnchorOffset() {
        return request.anchor;
    }

    public ElementHandle getElement() {
        return element;
    }

    public String getName() {
        return element.getName();
    }

    public String getInsertPrefix() {
        return getName();
    }

    public String getSortText() {
        if (getElement() instanceof IndexedElement) {
            IndexedElement indexedElement = (IndexedElement) getElement();

            if (indexedElement.isResolved()) {
                return "-" + getName(); //NOI18N

            }
        }
        return getName();
    }

    public String getLhsHtml() {
        HtmlFormatter formatter = request.formatter;
        formatter.reset();
        formatter.appendText(getName());
        return formatter.getText();
    }

    public ElementKind getKind() {
        return null;
    }

    public ImageIcon getIcon() {
        return null;
    }

    public Set<Modifier> getModifiers() {
        return null;
    }

    public boolean isSmart() {
        // true for elements defined in the currently file
        if (getElement() instanceof IndexedElement) {
            IndexedElement indexedElement = (IndexedElement) getElement();
            String url = indexedElement.getFilenameUrl();
            return url != null && url.equals(request.currentlyEditedFileURL);
        }

        return false;
    }

    public String getCustomInsertTemplate() {
        return null;
    }

    public List<String> getInsertParams() {
        return null;
    }

    public String[] getParamListDelimiters() {
        return new String[]{"(", ")"}; // NOI18N

    }

    public String getRhsHtml() {
        HtmlFormatter formatter = request.formatter;
        formatter.reset();

        if (element.getIn() != null) {
            formatter.appendText(element.getIn());
            return formatter.getText();
        } else if (element instanceof IndexedElement) {
            IndexedElement ie = (IndexedElement) element;
            String filename = ie.getFilenameUrl();
            if (filename != null) {
                int index = filename.lastIndexOf('/');
                if (index != -1) {
                    filename = filename.substring(index + 1);
                }

                formatter.appendText(filename);
                return formatter.getText();
            }
        }

        return null;
    }
    
    static class KeywordItem extends PHPCompletionItem {
        private String description = null;
        private String keyword = null;
        private static final String PHP_KEYWORD_ICON = "org/netbeans/modules/php/editor/resources/php16Key.png"; //NOI18N
        private static ImageIcon keywordIcon = null;
        
        
        KeywordItem(String keyword, CompletionRequest request) {
            super(null, request);
            this.keyword = keyword;
        }

        @Override
        public String getName() {
            return keyword;
        }
        
        @Override public String getLhsHtml() {
            HtmlFormatter formatter = request.formatter;
            formatter.reset();
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);
            
            return formatter.getText();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }
        
        @Override
        public String getRhsHtml() {
            if (description != null) {
                HtmlFormatter formatter = request.formatter;
                formatter.reset();
                formatter.appendHtml(description);
                return formatter.getText();
                
            } else {
                return null;
            }
        }
        
        @Override
        public ImageIcon getIcon() {
            if (keywordIcon == null) {
                keywordIcon = new ImageIcon(org.openide.util.Utilities.loadImage(PHP_KEYWORD_ICON));
            }

            return keywordIcon;
        }
    }
    
    static class ConstantItem extends PHPCompletionItem {
        private IndexedConstant constant = null;

        ConstantItem(IndexedConstant constant, CompletionRequest request) {
            super(constant, request);
            this.constant = constant;
        }
        
        @Override public String getLhsHtml() {
            HtmlFormatter formatter = request.formatter;
            IndexedConstant constant = ((IndexedConstant)getElement());
            formatter.reset();
            formatter.name(getKind(), true);
            
            if (constant.isResolved()){
                formatter.emphasis(true);
                formatter.appendText(getName());
                formatter.emphasis(false);
            } else {
                formatter.appendText(getName());
            }
            
            formatter.name(getKind(), false);
            
            return formatter.getText();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.GLOBAL;
        }
    }
    
    static class ClassItem extends PHPCompletionItem {
        ClassItem(IndexedClass clazz, CompletionRequest request) {
            super(clazz, request);
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CLASS;
        }
    }
    
    static class VariableItem extends PHPCompletionItem {
        private boolean insertDollarPrefix = true;

        VariableItem(IndexedConstant constant, CompletionRequest request) {
            super(constant, request);
        }
        
        @Override public String getLhsHtml() {
            HtmlFormatter formatter = request.formatter;
            String typeName = ((IndexedConstant)getElement()).getTypeName();
            formatter.reset();
            
            if (typeName == null) {
                typeName = "?"; //NOI18N
            }
            
            formatter.type(true);
            formatter.appendText(typeName);
            formatter.type(false);
            formatter.appendText(" "); //NOI18N
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);
            
            return formatter.getText();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }

        @Override
        public String getName() {
            String name = super.getName();
            
            if (!insertDollarPrefix && name.startsWith("$")){ //NOI18N
                return name.substring(1);
            }
            
            return name;
        }
        
        void doNotInsertDollarPrefix(){
            insertDollarPrefix = false;
        }
    }
    
    static class FunctionItem extends PHPCompletionItem {

        FunctionItem(IndexedFunction function, CompletionRequest request) {
            super(function, request);
        }
        
        public IndexedFunction getFunction(){
            return (IndexedFunction)getElement();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.METHOD;
        }
        
        @Override
        public String getCustomInsertTemplate() {
            StringBuilder template = new StringBuilder();
            template.append(getName());
            template.append("("); //NOI18N
            
            List<String> params = getInsertParams();
            
            for (int i = 0; i < params.size(); i++) {
                String param = params.get(i);
                template.append("${php-cc-"); //NOI18N
                template.append(Integer.toString(i));
                template.append(" default=\""); // NOI18N
                template.append(param);
                template.append("\"}"); //NOI18N
                
                if (i < params.size() - 1){
                    template.append(", "); //NOI18N
                }
            }
            
            template.append(')');
            
            return template.toString();
        }
        
        @Override public String getLhsHtml() {
            ElementKind kind = getKind();
            HtmlFormatter formatter = request.formatter;
            formatter.reset();
            
            formatter.name(kind, true);
            
            if (getFunction().isResolved()){
                formatter.emphasis(true);
                formatter.appendText(getName());
                formatter.emphasis(false);
            } else {
                formatter.appendText(getName());
            }
            
            formatter.name(kind, false);
       
            formatter.appendHtml("("); // NOI18N
            formatter.parameters(true);
            formatter.appendText(getParamsStr());
            formatter.parameters(false);
            formatter.appendHtml(")"); // NOI18N
            
            return formatter.getText();
        }
        
        @Override
        public List<String> getInsertParams() {
            return getFunction().getParameters();
        }
        
        private String getParamsStr(){
            StringBuilder builder = new StringBuilder();
            Collection<String> parameters = getFunction().getParameters();
            
            if ((parameters != null) && (parameters.size() > 0)) {
                Iterator<String> it = parameters.iterator();

                while (it.hasNext()) { // && tIt.hasNext()) {
                    String param = it.next();
                    builder.append(param);

                    if (it.hasNext()) {
                        builder.append(", "); // NOI18N
                    }
                }
            }
            
            return builder.toString();
        }
    }

    static class CompletionRequest {
        public  HtmlFormatter formatter;
        public  int anchor;
        public  PHPParseResult result;
        public  CompilationInfo info;
        public  String prefix;
        public  String currentlyEditedFileURL;
        PHPIndex index;
    }
}
