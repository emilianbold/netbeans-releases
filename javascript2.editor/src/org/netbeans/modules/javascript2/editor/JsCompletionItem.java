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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.ImageIcon;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.api.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.index.IndexedElement;
import org.netbeans.modules.javascript2.editor.model.JsElement;
import org.netbeans.modules.javascript2.editor.model.JsFunction;
import org.netbeans.modules.javascript2.editor.model.JsObject;
import org.netbeans.modules.javascript2.editor.model.TypeUsage;
import org.netbeans.modules.javascript2.editor.model.impl.ModelUtils;
import org.netbeans.modules.javascript2.editor.model.impl.TypeUsageImpl;
import org.netbeans.modules.javascript2.editor.options.OptionsUtils;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Petr Pisl
 */
public class JsCompletionItem implements CompletionProposal {
    
    protected final CompletionRequest request;
    private final ElementHandle element;
    
    JsCompletionItem(ElementHandle element, CompletionRequest request) {
        this.element = element;
        this.request = request;
    }
    
    @Override
    public int getAnchorOffset() {
        return LexUtilities.getLexerOffset((JsParserResult)request.info, request.anchor);
    }

    @Override
    public ElementHandle getElement() {
        return element;
    }

    @Override
    public String getName() {
        return element.getName();
    }

    @Override
    public String getInsertPrefix() {
        return element.getName();
    }

    @Override
    public String getSortText() {
        StringBuilder sb = new StringBuilder();
        if (element != null) {
            FileObject sourceFo = request.result.getSnapshot().getSource().getFileObject();
            FileObject elementFo = element.getFileObject();
            if (elementFo != null && sourceFo != null && sourceFo.equals(elementFo)) {
                sb.append("1");     //NOI18N
            } else {
                sb.append("9");     //NOI18N
            }
        }
        sb.append(getName());    
        return sb.toString();
    }
    
    protected boolean isDeprecated() {
        return element.getModifiers().contains(Modifier.DEPRECATED);
    }
    
    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        formatName(formatter);
        return formatter.getText();
    }

    protected void formatName(HtmlFormatter formatter) {
        if (isDeprecated()) {
            formatter.deprecated(true);
            formatter.appendText(getName());
            formatter.deprecated(false);
        } else {
            formatter.appendText(getName());
        }
    }
    
    @Messages("JsCompletionItem.lbl.js.platform=JS Platform")
    @Override
    public String getRhsHtml(HtmlFormatter formatter) {
        String location = null;
        if (element instanceof JsElement) {
            JsElement jsElement = (JsElement) element;
            if (jsElement.isPlatform()) {
                location = Bundle.JsCompletionItem_lbl_js_platform();
            } else if (jsElement.getSourceLabel() != null) {
                location = jsElement.getSourceLabel();
            }
        }
        if (location == null) {
            location = getFileNameURL();
        }
        if (location == null) {
            return null;
        }

        formatter.reset();
        formatter.appendText(location);
        return formatter.getText();
    }

    @Override
    public ElementKind getKind() {
        return element.getKind();
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public Set<Modifier> getModifiers() {
        Set<Modifier> emptyModifiers = Collections.emptySet();
        ElementHandle handle = getElement();
        return (handle != null) ? handle.getModifiers() : emptyModifiers;
    }

    @Override
    public boolean isSmart() {
        // TODO implemented properly
        return false;
    }

    @Override
    public int getSortPrioOverride() {
        return (element != null && ((JsElement)element).isPlatform()) ? 0 : 100;
    }

    @Override
    public String getCustomInsertTemplate() {
        return null;
    }

    @CheckForNull
    public final String getFileNameURL() {
        ElementHandle elem = getElement();
        if (elem == null) {
            return null;
        }
        FileObject fo = elem.getFileObject();
        if (fo != null) {
            return fo.getNameExt();
        }
        return getName();
     }
    
    public static class CompletionRequest {
        public int anchor;
        public JsParserResult result;
        public ParserResult info;
        public String prefix;
    }
    
    private static  ImageIcon priviligedIcon = null;
    
    public static class JsFunctionCompletionItem extends JsCompletionItem {
        
        private final Set<String> returnTypes;
        private final Map<String, Set<String>> parametersTypes;
        JsFunctionCompletionItem(ElementHandle element, CompletionRequest request, Set<String> resolvedReturnTypes, Map<String, Set<String>> parametersTypes) {
            super(element, request);
            this.returnTypes = resolvedReturnTypes != null ? resolvedReturnTypes : Collections.EMPTY_SET;
            this.parametersTypes = parametersTypes != null ? parametersTypes : Collections.EMPTY_MAP;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.emphasis(true);
            formatName(formatter);
            formatter.emphasis(false);
            formatter.appendText("(");
            appendParamsStr(formatter);
            formatter.appendText(")");
            appendReturnTypes(formatter);
            return formatter.getText();
        }

        private void appendParamsStr(HtmlFormatter formatter){
            for (Iterator<Map.Entry<String, Set<String>>> it = parametersTypes.entrySet().iterator(); it.hasNext();) {
                Map.Entry<String, Set<String>> entry = it.next();
                formatter.parameters(true);
                formatter.appendText(entry.getKey());
                formatter.parameters(false);
                Collection<String> types = entry.getValue();
                if (!types.isEmpty()) {
                    formatter.type(true);
                    formatter.appendText(": ");  //NOI18N
                    for (Iterator<String> itTypes = types.iterator(); itTypes.hasNext();) {
                        formatter.appendText(itTypes.next());
                        if (itTypes.hasNext()) {
                            formatter.appendText("|");   //NOI18N
                        }
                    }
                    formatter.type(false);
                }
                if (it.hasNext()) {
                    formatter.appendText(", ");  //NOI18N
                }    
            }
        }

        private void appendReturnTypes(HtmlFormatter formatter) {
            if (!returnTypes.isEmpty()) {
                formatter.appendText(": "); //NOI18N
                formatter.type(true);
                for (Iterator<String> it = returnTypes.iterator(); it.hasNext();) {
                    formatter.appendText(it.next());
                    if (it.hasNext()) {
                        formatter.appendText("|"); //NOI18N
                    }
                }
                formatter.type(false);
            }
        }

        @Override
        public String getCustomInsertTemplate() {
            StringBuilder template = new StringBuilder();
            template.append(getName());
            template.append("(${cursor})");
            return template.toString();
        }

        @Override
        public ImageIcon getIcon() {
            if (getModifiers().contains(Modifier.PROTECTED)) {
                if(priviligedIcon == null) {
                    priviligedIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/javascript2/editor/resources/methodPriviliged.png")); //NOI18N
                }
                return priviligedIcon;
            }
            return super.getIcon(); //To change body of generated methods, choose Tools | Templates.
        }
        
        
    }

    static class KeywordItem extends JsCompletionItem {
        private static  ImageIcon keywordIcon = null;
        private String keyword = null;

        public KeywordItem(String keyword, CompletionRequest request) {
            super(null, request);
            this.keyword = keyword;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
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
        public String getRhsHtml(HtmlFormatter formatter) {
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            if (keywordIcon == null) {
                keywordIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/javascript2/editor/resources/javascript.png")); //NOI18N
            }
            return keywordIcon;
        }
        
        @Override
        public String getInsertPrefix() {
            return getName();
        }
        
        @Override
        public String getCustomInsertTemplate() {
            StringBuilder builder = new StringBuilder();
            
            JsKeyWords.CompletionType type = JsKeyWords.KEYWORDS.get(getName());
            if (type == null) {
                return getName();
            }
            
            switch(type) {
                case SIMPLE:
                    builder.append(getName());
                    break;
                case ENDS_WITH_SPACE:
                    builder.append(getName());
                    builder.append(" ${cursor}"); //NOI18N
                    break;
                case CURSOR_INSIDE_BRACKETS:
                    builder.append(getName());
                    builder.append("(${cursor})"); //NOI18N
                    break;
                case ENDS_WITH_CURLY_BRACKETS:
                    builder.append(getName());
                    builder.append(" {${cursor}}"); //NOI18N
                    break;
                case ENDS_WITH_SEMICOLON:
                    builder.append(getName());
                    CharSequence text = request.info.getSnapshot().getText();
                    int index = request.anchor + request.prefix.length();
                    if (index == text.length() || ';' != text.charAt(index)) { //NOI18N
                        builder.append(";"); //NOI18N
                    }
                    break;
                case ENDS_WITH_COLON:
                    builder.append(getName());
                    builder.append(" ${cursor}:"); //NOI18N
                    break;
                case ENDS_WITH_DOT:
                    builder.append(getName());
                    builder.append(".${cursor}"); //NOI18N
                    break;
                default:
                    assert false : type.toString();
                    break;
            }
            return builder.toString();
        }

        @Override
        public int getSortPrioOverride() {
            return 110;
        }
    }

    public static class JsPropertyCompletionItem extends JsCompletionItem {

        private final Set<String> resolvedTypes;
        
        JsPropertyCompletionItem(ElementHandle element, CompletionRequest request, Set<String> resolvedTypes) {
            super(element, request);
            this.resolvedTypes = resolvedTypes != null ? resolvedTypes : Collections.EMPTY_SET;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatName(formatter);
            if (!resolvedTypes.isEmpty()) {
                formatter.type(true);
                formatter.appendText(": ");  //NOI18N
                for (Iterator<String> it = resolvedTypes.iterator(); it.hasNext();) {
                    formatter.appendText(it.next());
                    if (it.hasNext()) {
                        formatter.appendText("|");   //NOI18N
                    }
                }
                formatter.type(false);
            }
            return formatter.getText();
        }
    }

    public static class Factory {
        
        public static void create( Map<String, List<JsElement>> items, CompletionRequest request, List<CompletionProposal> result) {
            // This maps unresolved types to the display name of the resolved type. 
            // It should save time to not resolve one type more times
            HashMap<String, Set<String>> resolvedTypes = new HashMap<String, Set<String>>();

            for (Map.Entry<String, List<JsElement>> entry: items.entrySet()) {

                // this helps to eleminate items that will look as the same items in the cc
                HashMap<String, JsCompletionItem> signatures = new HashMap<String, JsCompletionItem>();
                for (JsElement element : entry.getValue()) {
                    switch (element.getJSKind()) {
                        case CONSTRUCTOR:
                        case FUNCTION:
                        case METHOD:
                            Set<String> returnTypes = new HashSet<String>();
                            HashMap<String, Set<String>> allParameters = new LinkedHashMap<String, Set<String>>();
                            if (element instanceof JsFunction) {
                                // count return types
                                Collection<TypeUsage> resolveTypes = ModelUtils.resolveTypes(((JsFunction) element).getReturnTypes(), (JsParserResult)request.info,
                                        OptionsUtils.forLanguage(JsTokenId.javascriptLanguage()).autoCompletionTypeResolution());
                                returnTypes.addAll(Utils.getDisplayNames(resolveTypes));
                                // count parameters type
                                for (JsObject jsObject : ((JsFunction) element).getParameters()) {
                                    Set<String> paramTypes = new HashSet<String>();
                                    for (TypeUsage type : jsObject.getAssignmentForOffset(jsObject.getOffset() + 1)) {
                                        Set<String> resolvedType = resolvedTypes.get(type.getType());
                                        if (resolvedType == null) {
                                            resolvedType = new HashSet(1);
                                            String displayName = type.getDisplayName();
                                            if (!displayName.isEmpty()) {
                                                resolvedType.add(displayName);
                                            }
                                            resolvedTypes.put(type.getType(), resolvedType);
                                        }
                                        paramTypes.addAll(resolvedType);
                                    }
                                    allParameters.put(jsObject.getName(), paramTypes);
                                }
                            } else if (element instanceof IndexedElement.FunctionIndexedElement) {
                                // count return types
                                HashSet<TypeUsage> returnTypeUsages = new HashSet<TypeUsage>();
                                for (String type : ((IndexedElement.FunctionIndexedElement) element).getReturnTypes()) {
                                    returnTypeUsages.add(new TypeUsageImpl(type, -1, false));
                                }
                                Collection<TypeUsage> resolveTypes = ModelUtils.resolveTypes(returnTypeUsages, (JsParserResult)request.info,
                                        OptionsUtils.forLanguage(JsTokenId.javascriptLanguage()).autoCompletionTypeResolution());
                                returnTypes.addAll(Utils.getDisplayNames(resolveTypes));
                                // count parameters type
                                LinkedHashMap<String, Collection<String>> parameters = ((IndexedElement.FunctionIndexedElement) element).getParameters();
                                for (Map.Entry<String, Collection<String>> paramEntry : parameters.entrySet()) {
                                    Set<String> paramTypes = new HashSet<String>();
                                    for (String type : paramEntry.getValue()) {
                                        Set<String> resolvedType = resolvedTypes.get(type);
                                        if (resolvedType == null) {
                                            resolvedType = new HashSet(1);
                                            String displayName = ModelUtils.getDisplayName(type);
                                            if (!displayName.isEmpty()) {
                                                resolvedType.add(displayName);
                                            }
                                            resolvedTypes.put(type, resolvedType);
                                        }
                                        paramTypes.addAll(resolvedType);
                                    }
                                    allParameters.put(paramEntry.getKey(), paramTypes);
                                }
                            }
                            // create signature
                            String signature = createFnSignature(entry.getKey(), allParameters, returnTypes);
                            if (!signatures.containsKey(signature)) {
                                JsCompletionItem item = new JsFunctionCompletionItem(element, request, returnTypes, allParameters);
                                signatures.put(signature, item);
                            }
                            break;
                        case PARAMETER:
                        case PROPERTY:
                        case PROPERTY_GETTER:
                        case PROPERTY_SETTER:
                        case FIELD:
                        case VARIABLE:
                            Set<String> typesToDisplay = new HashSet<String>();
                            Collection<? extends TypeUsage> assignment = null;
                            if (element instanceof JsObject) {
                                JsObject jsObject = (JsObject) element;
                                assignment = jsObject.getAssignmentForOffset(request.anchor);
                            } else if (element instanceof IndexedElement) {
                                IndexedElement iElement = (IndexedElement) element;
                                assignment = iElement.getAssignments();
                            }
                            if (assignment != null && !assignment.isEmpty()) {
                                HashSet<TypeUsage> toResolve = new HashSet<TypeUsage>();
                                for (TypeUsage type : assignment) {
                                    if (type.isResolved()) {
                                        typesToDisplay.add(type.getDisplayName());
                                    } else {
                                        Set<String> resolvedType = resolvedTypes.get(type.getType());
                                        if (resolvedType == null) {
                                            toResolve.clear();
                                            toResolve.add(type);
                                            resolvedType = new HashSet(1);
                                            Collection<TypeUsage> resolved = ModelUtils.resolveTypes(toResolve, request.result,
                                                    OptionsUtils.forLanguage(JsTokenId.javascriptLanguage()).autoCompletionTypeResolution());
                                            for (TypeUsage rType : resolved) {
                                                String displayName = rType.getDisplayName();
                                                if (!displayName.isEmpty()) {
                                                    resolvedType.add(displayName);
                                                }
                                            }
                                            resolvedTypes.put(type.getType(), resolvedType);
                                        }
                                        typesToDisplay.addAll(resolvedType);
                                    }
                                }
                            }
                            // signatures
                            signature = element.getName() + ":" + createTypeSignature(typesToDisplay);
                            if (!signatures.containsKey(signature)) {
                                // add the item to the cc only if doesn't exist any similar
                                JsCompletionItem item = new JsPropertyCompletionItem(element, request, typesToDisplay);
                                signatures.put(signature, item);
                            }
                            break;
                        default:
                            signature = element.getName();
                            if (!signatures.containsKey(signature)) {
                                JsCompletionItem item = new JsCompletionItem(element, request);
                                signatures.put(signature, item);
                            }
                    }
                }
                for (JsCompletionItem item: signatures.values()) {
                    result.add(item);
                }
            }
        }
        
        private static String createFnSignature(String name, HashMap<String, Set<String>> params, Set<String> returnTypes) {
            StringBuilder sb = new StringBuilder();
            sb.append(name).append('(');
            for (Map.Entry<String, Set<String>> entry : params.entrySet()) {
                sb.append(entry.getKey()).append(':');
                sb.append(createTypeSignature(entry.getValue()));
                sb.append(',');
            }
            sb.append(')');
            sb.append(createTypeSignature(returnTypes));
            return sb.toString();
        }
        
        private static String createTypeSignature(Set<String> types) {
            StringBuilder sb = new StringBuilder();
            for(String name: types){
                sb.append(name).append('|');
            }
            return sb.toString();
        }
    }
}
