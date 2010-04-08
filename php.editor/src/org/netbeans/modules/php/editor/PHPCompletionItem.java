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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.ImageIcon;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.CompletionContextFinder.CompletionContext;
import org.netbeans.modules.php.editor.CompletionContextFinder.KeywordCompletionType;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.QualifiedNameKind;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement.PrintAs;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.ConstantElement;
import org.netbeans.modules.php.editor.api.elements.FieldElement;
import org.netbeans.modules.php.editor.api.elements.FullyQualifiedElement;
import org.netbeans.modules.php.editor.api.elements.FunctionElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.NamespaceElement;
import org.netbeans.modules.php.editor.api.elements.ParameterElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeMemberElement;
import org.netbeans.modules.php.editor.api.elements.TypeResolver;
import org.netbeans.modules.php.editor.api.elements.VariableElement;
import org.netbeans.modules.php.editor.index.PredefinedSymbolElement;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.nodes.NamespaceDeclarationInfo;
import org.netbeans.modules.php.editor.nav.NavUtils;
import org.netbeans.modules.php.editor.options.CodeCompletionPanel.CodeCompletionType;
import org.netbeans.modules.php.editor.options.OptionsUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.project.api.PhpLanguageOptions;
import org.netbeans.modules.php.project.api.PhpLanguageOptions.Properties;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class PHPCompletionItem implements CompletionProposal {
    private static final String PHP_KEYWORD_ICON = "org/netbeans/modules/php/editor/resources/php16Key.png"; //NOI18N
    protected static ImageIcon keywordIcon = null;

    protected final CompletionRequest request;
    private final ElementHandle element;
    protected QualifiedNameKind generateAs;
    private static ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

    PHPCompletionItem(ElementHandle element, CompletionRequest request, QualifiedNameKind generateAs) {
        this.request = request;
        this.element = element;
        keywordIcon = new ImageIcon(ImageUtilities.loadImage(PHP_KEYWORD_ICON));
        this.generateAs = generateAs;
    }

    PHPCompletionItem(ElementHandle element, CompletionRequest request) {
        this(element,request,null);
    }

    @Override
    public int getAnchorOffset() {
        return request.anchor;
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
    public String getSortText() {
        return getName();
    }

    @Override
    public int getSortPrioOverride() {
        return 0;
    }

    @Override
    public String getLhsHtml(HtmlFormatter formatter) {
        formatter.appendText(getName());
        return formatter.getText();
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

    public String getFileNameURL() {
        ElementHandle elem = getElement();
        return (elem instanceof PhpElement) ? ((PhpElement)elem).getFilenameUrl() : "";//NOI18N
     }

    @Override
    public boolean isSmart() {
        String url = getFileNameURL();
        return url != null && url.equals(request.currentlyEditedFileURL);
    }


    private static NamespaceDeclaration findEnclosingNamespace(PHPParseResult info, int offset) {
        final Program program = info.getProgram();
        List<ASTNode> nodes = NavUtils.underCaret(info, Math.min((program != null) ? program.getEndOffset() : offset, offset));
        for(ASTNode node : nodes) {
            if (node instanceof NamespaceDeclaration) {
                return (NamespaceDeclaration) node;
            }
        }
        return null;
    }

    @Override
    public String getCustomInsertTemplate() {
        return null;
    }


    public String getInsertPrefix() {
        StringBuilder template = new StringBuilder();
        ElementHandle elem = getElement();
        if (elem instanceof MethodElement) {
            final MethodElement method = (MethodElement) elem;
            if (method.isConstructor()) {
                elem = method.getType();
            }
        }
        if (elem instanceof FullyQualifiedElement) {
            FullyQualifiedElement ifq = (FullyQualifiedElement) elem;
            final QualifiedName qn = QualifiedName.create(request.prefix);
            final FileObject fileObject = request.result.getSnapshot().getSource().getFileObject();
            Properties props = fileObject != null ? PhpLanguageOptions.getDefault().getProperties(fileObject) : null;
            if (props != null && props.getPhpVersion() == PhpLanguageOptions.PhpVersion.PHP_53) {
                if (generateAs == null) {
                    CodeCompletionType codeCompletionType = OptionsUtils.codeCompletionType();
                    switch (codeCompletionType) {
                        case FULLY_QUALIFIED:
                            template.append(ifq.getFullyQualifiedName());
                            return template.toString();
                        case UNQUALIFIED:
                            template.append(getName());
                            return template.toString();
                        case SMART:
                            generateAs = qn.getKind();
                            break;
                    }

                } else if (generateAs.isQualified() && (ifq instanceof TypeElement)
                        && ifq.getNamespaceName().equals(NamespaceDeclarationInfo.DEFAULT_NAMESPACE_NAME)) {
                    //TODO: this is sort of hack for CCV after use, namespace keywords - should be changed
                    generateAs = QualifiedNameKind.FULLYQUALIFIED;
                }
            } else {
                template.append(getName());
                return template.toString();
            }
            switch (generateAs) {
                case FULLYQUALIFIED:
                    template.append(ifq.getFullyQualifiedName());
                    break;
                case QUALIFIED:
                    final String fqn = ifq.getFullyQualifiedName().toString();
                    int indexOf = fqn.toLowerCase().indexOf(qn.toNamespaceName().toString().toLowerCase());
                    if (indexOf != -1) {
                        template.append(fqn.substring(indexOf == 0 ? 1 : indexOf));
                        break;
                    }
                case UNQUALIFIED:
                    boolean fncFromDefaultNamespace = ((ifq instanceof FunctionElement) && ifq.getIn() == null
                            && NamespaceDeclarationInfo.DEFAULT_NAMESPACE_NAME.equals(ifq.getNamespaceName()));
                    if (!(elem instanceof NamespaceElement) && !fncFromDefaultNamespace) {
                        Model model = request.result.getModel();
                        NamespaceDeclaration namespaceDeclaration = findEnclosingNamespace(request.result, request.anchor);
                        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(namespaceDeclaration, model.getFileScope());

                        if (namespaceScope != null) {
                            LinkedList<String> segments = ifq.getFullyQualifiedName().getSegments();
                            QualifiedName fqna = QualifiedName.create(false, segments);
                            if (!namespaceScope.isDefaultNamespace() || !fqna.getKind().isUnqualified()) {
                                QualifiedName suffix = QualifiedName.getPreferredName(fqna, namespaceScope);
                                if (suffix != null) {
                                    template.append(suffix.toString());
                                    break;
                                }
                            }
                        }
                    }
                    template.append(getName());
                    break;
            }

            return template.toString();
        }
        
        return getName();
    }

    public String getRhsHtml(HtmlFormatter formatter) {
        if (element instanceof TypeMemberElement) {
            TypeMemberElement classMember = (TypeMemberElement) element;
            TypeElement type = classMember.getType();
            QualifiedName qualifiedName = type.getNamespaceName();
            if (qualifiedName.isDefaultNamespace()) {
                formatter.appendText(type.getName());
                return formatter.getText();
            } else {
                formatter.appendText(type.getFullyQualifiedName().toString());
                return formatter.getText();
            }
        }
            final String in = element.getIn();
            if (in != null && in.length() > 0) {
                formatter.appendText(element.getIn());
                return formatter.getText();
            } else if (element instanceof PhpElement) {
                PhpElement ie = (PhpElement) element;
                if (ie.isPlatform()) {
                    return NbBundle.getMessage(PHPCompletionItem.class, "PHPPlatform");
                }

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

    static class NewClassItem extends MethodElementItem {
        public static NewClassItem getItem(final MethodElement methodElement, CompletionRequest request) {
            return new NewClassItem(new FunctionElementItem(methodElement, request, methodElement.getParameters()));
        }

        private NewClassItem(FunctionElementItem function) {
            super(function);
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            if (getElement().getIn() != null) {
                String namespaceName = ((MethodElement)getElement()).getType().getNamespaceName().toString();
                if (namespaceName != null && !NamespaceDeclarationInfo.DEFAULT_NAMESPACE_NAME.equals(namespaceName)) {
                    formatter.appendText(namespaceName);
                    return formatter.getText();
                }
            }
            return super.getRhsHtml(formatter);
        }


        @Override
        public String getName() {
                String in = getElement().getIn();
                return (in != null) ? in : super.getName();
            }

        @Override
        public ElementKind getKind() {
            return ElementKind.CONSTRUCTOR;
        }
    }

    static class MethodElementItem extends FunctionElementItem {
        /**
         * @return more than one instance in case if optional parameters exists
         */
        static List<MethodElementItem> getItems(final MethodElement methodElement, CompletionRequest request) {
            final List<MethodElementItem> retval = new ArrayList<MethodElementItem>();
            List<FunctionElementItem> items = FunctionElementItem.getItems(methodElement, request);
            for (FunctionElementItem functionElementItem : items) {
                retval.add(new MethodElementItem(functionElementItem));
            }
            return retval;
        }

        MethodElementItem(FunctionElementItem function) {
            super(function.getBaseFunctionElement(), function.request, function.parameters);
        }
    }


    static class FunctionElementItem extends PHPCompletionItem {
        private List<ParameterElement> parameters;

        /**
         * @return more than one instance in case if optional parameters exists
         */
        static List<FunctionElementItem> getItems(final BaseFunctionElement function, CompletionRequest request) {
            final List<FunctionElementItem> retval = new ArrayList<FunctionElementItem>();
            final List<ParameterElement> parameters = new ArrayList<ParameterElement>();
            for (ParameterElement param : function.getParameters()) {
                if (!param.isMandatory()) {
                    if (retval.isEmpty()) {
                        retval.add(new FunctionElementItem(function, request, parameters));
                    }
                    parameters.add(param);
                    retval.add(new FunctionElementItem(function, request, parameters));
                } else {
                    //assert retval.isEmpty():param.asString();
                    parameters.add(param);
                }
            }
            if (retval.isEmpty()) {
                retval.add(new FunctionElementItem(function, request, parameters));
            }

            return retval;
        }

        FunctionElementItem(BaseFunctionElement function, CompletionRequest request, List<ParameterElement> parameters) {
            super(function, request);
            this.parameters = new ArrayList<ParameterElement>(parameters);
        }

        public BaseFunctionElement getBaseFunctionElement() {
            return (BaseFunctionElement)getElement();
        }

        @Override
        public ElementKind getKind() {
            return getBaseFunctionElement().getPhpElementKind().getElementKind();
        }

        @Override
        public String getInsertPrefix() {
            StringBuilder template = new StringBuilder();
            String superTemplate = super.getInsertPrefix();
            if (superTemplate != null) {
                template.append(superTemplate);
            } else {
                template.append(getName());
            }

            template.append("("); //NOI18N

            List<String> params = getInsertParams();

            for (int i = 0; i < params.size(); i++) {
                String param = params.get(i);
                if (param.startsWith("&")) {//NOI18N
                    param = param.substring(1);
                }
                template.append(param);

                if (i < params.size() - 1){
                    template.append(", "); //NOI18N
                }
            }

            template.append(')');

            return template.toString();
        }

        @Override public String getLhsHtml(HtmlFormatter formatter) {
            ElementKind kind = getKind();

            formatter.name(kind, true);

            if (emphasisName()){
                formatter.emphasis(true);
                formatter.appendText(getName());
                formatter.emphasis(false);
            } else {
                formatter.appendText(getName());
            }

            formatter.name(kind, false);

            formatter.appendHtml("("); // NOI18N
            formatter.parameters(true);
            appendParamsStr(formatter);
            formatter.parameters(false);
            formatter.appendHtml(")"); // NOI18N

            return formatter.getText();
        }

        protected boolean emphasisName() {
            return true;//getFunction().isResolved();
        }

        public List<String> getInsertParams() {
            List<String> insertParams = new LinkedList<String>();
            for (ParameterElement parameter : parameters) {
                insertParams.add(parameter.getName());
            }
            return insertParams;
        }

        @Override
        public String getSortText() {
            return getName() + parameters.size();
        }

        private void appendParamsStr(HtmlFormatter formatter){
            List<ParameterElement> allParameters = parameters;
            for (int i = 0; i < allParameters.size(); i++) {
                ParameterElement parameter = allParameters.get(i);
                String paramName = parameter.getName();
                if (paramName.startsWith("&")) {//NOI18N
                    paramName = paramName.substring(1);
                }
                if (i != 0) {
                    formatter.appendText(", "); // NOI18N
                }

                if (!parameter.isMandatory()) {
                    formatter.appendText(parameter.asString(true));
                } else {
                    formatter.emphasis(true);
                    formatter.appendText(parameter.asString(true));
                    formatter.emphasis(false);
                }
            }
        }
    }


    static class FieldItem extends PHPCompletionItem {
        public static FieldItem getItem(FieldElement field, CompletionRequest request) {
            return new FieldItem(field, request);
        }

        private FieldItem(FieldElement field, CompletionRequest request) {
            super(field, request);
        }

        FieldElement getField() {
            return (FieldElement) getElement();
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            formatter.type(true);
            formatter.appendText(getTypeName());
            formatter.type(false);
            formatter.appendText(" "); //NOI18N
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);

            return formatter.getText();
        }

        protected String getTypeName() {
            Set<TypeResolver> types = getField().getInstanceTypes();
            String typeName = types.isEmpty() ? "?" : types.size() > 1 ?  "mixed" : "?";//NOI18N
            if (types.size() == 1) {
                TypeResolver typeResolver = types.iterator().next();
                if (typeResolver.isResolved()) {
                    QualifiedName qualifiedName = typeResolver.getTypeName(false);
                    if (qualifiedName != null) {
                        typeName = qualifiedName.toString();
                    }
                }
            }
            return typeName;
        }

        @Override
        public ElementKind getKind() {
            //TODO: variable just because originally VARIABLE was returned and thus all tests fail
            //return ElementKind.FIELD;
            return ElementKind.VARIABLE;
        }

        @Override
        public String getName() {
            final FieldElement field = getField();
            return field.getName(field.isStatic());
        }

        @Override
        public String getInsertPrefix() {
            Completion.get().showToolTip();
            return getName();
        }
    }

    static class TypeConstantItem extends PHPCompletionItem {
        public static TypeConstantItem getItem(TypeConstantElement constant, CompletionRequest request) {
            return new TypeConstantItem(constant, request);
        }

        private TypeConstantItem(TypeConstantElement constant, CompletionRequest request) {
            super(constant, request);
        }

        TypeConstantElement getConstant() {
            return (TypeConstantElement) getElement();
        }

        @Override
        public ElementKind getKind() {
            return ElementKind.CONSTANT;
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            String value = getConstant().getValue();
            formatter.type(true);
            formatter.appendText(value != null ? value : "?");//NOI18N
            formatter.type(false);
            formatter.appendText(" "); //NOI18N
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);

            return formatter.getText();
        }

        @Override
        public String getName() {
            return getConstant().getName();
        }

        @Override
        public String getInsertPrefix() {
            Completion.get().showToolTip();
            return getName();
        }
    }

    public static class MethodDeclarationItem extends MethodElementItem {
        public static MethodDeclarationItem getDeclarationItem(final MethodElement methodElement, CompletionRequest request) {
            return new MethodDeclarationItem(new FunctionElementItem(methodElement, request, methodElement.getParameters()));
        }
        public static MethodDeclarationItem forIntroduceHint(final MethodElement methodElement, CompletionRequest request) {
            return new MethodDeclarationItem(new FunctionElementItem(methodElement, request, methodElement.getParameters())) {
                @Override
                protected String getFunctionBodyForTemplate() {
                    return "\n";//NOI18N
                }
            };
        }
        public static MethodDeclarationItem forMethodName(final MethodElement methodElement, CompletionRequest request) {
            return new MethodDeclarationItem(new FunctionElementItem(methodElement, request, methodElement.getParameters())) {

                @Override
                public String getCustomInsertTemplate() {
                    return super.getNameAndFunctionBodyForTemplate();
                }
            };
        }

        private  MethodDeclarationItem(FunctionElementItem functionItem) {
            super(functionItem);
        }

        public MethodElement getMethod() {
            return (MethodElement) getBaseFunctionElement();
        }

        @Override
        public boolean isSmart() {
            return isMagic()? false : true;
        }

        @Override
        protected boolean emphasisName() {
            return isMagic()? false : super.emphasisName();
        }

        public boolean isMagic() {
            return ((MethodElement)getBaseFunctionElement()).isMagic();
        }

        @Override
        public String getCustomInsertTemplate() {
            StringBuilder template = new StringBuilder();
            String modifierStr = BodyDeclaration.Modifier.toString(getBaseFunctionElement().getFlags());
            if (modifierStr.length() != 0) {
                modifierStr = modifierStr.replace("abstract", "").trim();//NOI18N
                template.append(modifierStr);
            }
            template.append(" ").append("function");//NOI18N
            template.append(getNameAndFunctionBodyForTemplate());
            return template.toString();
        }

        protected String getNameAndFunctionBodyForTemplate() {
            StringBuilder template = new StringBuilder();
            template.append(getBaseFunctionElement().asString(PrintAs.NameAndParamsDeclaration));
            template.append(" ").append("{\n");//NOI18N
            template.append(getFunctionBodyForTemplate());//NOI18N
            template.append("}");//NOI18N
            return template.toString();
        }

        /**
         * @return body or null
         */
        protected String getFunctionBodyForTemplate() {
            StringBuilder template = new StringBuilder();
            MethodElement method = (MethodElement)getBaseFunctionElement();
            TypeElement type = method.getType();
            if (isMagic() || type.isInterface() || method.isAbstract()) {
                template.append("${cursor};\n");//NOI18N
            } else {
                template.append("${cursor}parent::" + getSignature().replace("&$", "$") + ";\n");//NOI18N
            }
            return template.toString();
        }

        private String getSignature() {
            StringBuilder retval = new StringBuilder();
            retval.append(getBaseFunctionElement().getName());
            retval.append("(");
            StringBuilder parametersInfo = new StringBuilder();
            List<ParameterElement> parameters = getBaseFunctionElement().getParameters();
            for (ParameterElement parameter : parameters) {
                if (parametersInfo.length() > 0) {
                    parametersInfo.append(", ");//NOI18N
                }
                parametersInfo.append(parameter.getName());
            }
            retval.append(parametersInfo);
            retval.append(")");//NOI18N
            return retval.toString();
        }

        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            StringBuilder sb = new StringBuilder();
            sb.append(super.getLhsHtml(formatter));
            sb.append(' ').append(NbBundle.getMessage(PHPCompletionItem.class, "Generate"));//NOI18N
            return sb.toString();
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            if (isMagic()) {
                final String message = NbBundle.getMessage(PHPCompletionItem.class, "MagicMethod");//NOI18N
                formatter.appendText(message);
                return formatter.getText();
            }
            return super.getRhsHtml(formatter);
        }

    }

    static class  ClassScopeKeywordItem extends KeywordItem {
        private final String className;
        ClassScopeKeywordItem(final String className, final String keyword, final CompletionRequest request) {
            super(keyword, request);
            this.className = className;
        }
        @Override
        public String getLhsHtml(HtmlFormatter formatter) {
            if (keyword.startsWith("$")) {//NOI18N
                if (className != null) {
                    formatter.type(true);
                    formatter.appendText(className);
                    formatter.type(false);
                }
                formatter.appendText(" "); //NOI18N
            }
            return super.getLhsHtml(formatter);
        }
    }
    static class KeywordItem extends PHPCompletionItem {
        private String description = null;
        String keyword = null;
        private static final List<String> CLS_KEYWORDS =
                Arrays.asList(PHPCodeCompletion.PHP_CLASS_KEYWORDS);


        KeywordItem(String keyword, CompletionRequest request) {
            super(null, request);
            this.keyword = keyword;
        }

        @Override
        public String getName() {
            return keyword;
        }

        @Override public String getLhsHtml(HtmlFormatter formatter) {
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);

            return formatter.getText();
        }

        public ElementKind getKind() {
            return ElementKind.KEYWORD;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            if (description != null) {
                formatter.appendHtml(description);
                return formatter.getText();

            } else {
                return null;
            }
        }

        @Override
        public ImageIcon getIcon() {
            return keywordIcon;
        }

        @Override
        public boolean isSmart() {
            return CLS_KEYWORDS.contains(getName()) ? true : super.isSmart();
        }

        @Override
        public String getInsertPrefix() {
            return getName();
        }

        @Override
        public String getCustomInsertTemplate() {
            StringBuilder builder = new StringBuilder();
            if (CLS_KEYWORDS.contains(getName())) {                
                scheduleShowingCompletion();
            }
            KeywordCompletionType type = PHPCodeCompletion.PHP_KEYWORDS.get(getName());
            if (type == null) {
                return getName();
            }
            switch(type) {
                case SIMPLE:
                    return null;
                case ENDS_WITH_SPACE:
                    builder.append(getName());
                    builder.append(" ${cursor}"); //NOI18N
                    break;
                case CURSOR_INSIDE_BRACKETS:
                    builder.append(getName());
                    builder.append(" (${cursor})"); //NOI18N
                    break;
                case ENDS_WITH_CURLY_BRACKETS:
                    builder.append(getName());
                    builder.append(" {${cursor}"); //NOI18N
                    break;
                case ENDS_WITH_SEMICOLON:
                    builder.append(getName());
                    builder.append(";"); //NOI18N
                    break;
                case ENDS_WITH_COLON:
                    builder.append(getName());
                    builder.append(" ${cursor}:"); //NOI18N
                    break;
                default:
                    assert false : type.toString();
                    break;
            }
            return builder.toString();
        }
    }

    static class SuperGlobalItem extends PHPCompletionItem{
        private String name;

        public SuperGlobalItem(CompletionRequest request, String name) {
            super(new PredefinedSymbolElement(name), request);
            this.name = name;
        }

        @Override public String getLhsHtml(HtmlFormatter formatter) {
            formatter.name(getKind(), true);
            formatter.emphasis(true);
            formatter.appendText(getName());
            formatter.emphasis(false);
            formatter.name(getKind(), false);

            return formatter.getText();
        }

        @Override
        public String getName() {
            return "$" + name; //NOI18N
        }

        @Override
        public String getInsertPrefix() {
            //todo insert array brackets for array vars
            return getName();
        }

        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            formatter.appendText(NbBundle.getMessage(PHPCompletionItem.class, "PHPPlatform"));
            return formatter.getText();
        }

        public String getDocumentation(){
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            return keywordIcon;
        }
    }

    static class NamespaceItem extends PHPCompletionItem {
        Boolean isSmart;
        NamespaceItem(NamespaceElement namespace, CompletionRequest request, QualifiedNameKind generateAs) {
            super(namespace, request, generateAs);
        }
        @Override
        public int getSortPrioOverride() {
            return isSmart() ? -10001 : super.getSortPrioOverride();
        }

        @Override public String getLhsHtml(HtmlFormatter formatter) {
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);

            return formatter.getText();
        }

        @Override
        public String getName() {
            return getNamespaceElement().getName();
        }

        NamespaceElement getNamespaceElement() {
            return (NamespaceElement) getElement();
        }

        public ElementKind getKind() {
            return ElementKind.PACKAGE;
        }

        @Override
        public String getRhsHtml(HtmlFormatter formatter) {
            QualifiedName namespaceName = getNamespaceElement().getNamespaceName();
            if (namespaceName != null && !namespaceName.isDefaultNamespace()) {
                formatter.appendText(namespaceName.toString());
                return formatter.getText();
            }

            return null;
        }

        @Override
        public boolean isSmart() {
            if (isSmart == null) {
                QualifiedName namespaceName = getNamespaceElement().getNamespaceName();
                isSmart =  !(namespaceName == null || !namespaceName.isDefaultNamespace());
                if (!isSmart) {
                    FileScope fileScope = request.result.getModel().getFileScope();
                    NamespaceScope namespaceScope = (fileScope != null) ?
                        ModelUtils.getNamespaceScope(fileScope, request.anchor) : null;
                    if (namespaceScope != null) {
                        NamespaceElement ifq = getNamespaceElement();
                        LinkedList<String> segments = ifq.getFullyQualifiedName().getSegments();
                        QualifiedName fqna = QualifiedName.create(false, segments);
                        Collection<QualifiedName> relativeUses = QualifiedName.getRelativesToUses(namespaceScope, fqna);
                        for (QualifiedName qualifiedName : relativeUses) {
                            if (qualifiedName.getSegments().size() == 1) {
                                isSmart = true;
                                break;
                            }
                        }
                        if (!isSmart) {
                            relativeUses = QualifiedName.getRelativesToNamespace(namespaceScope, fqna);
                            for (QualifiedName qualifiedName : relativeUses) {
                                if (qualifiedName.getSegments().size() == 1) {
                                    isSmart = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            return isSmart;
        }
    }

    static class ConstantItem extends PHPCompletionItem {
        ConstantItem(ConstantElement constant, CompletionRequest request) {
            super(constant, request);
        }

        @Override public String getLhsHtml(HtmlFormatter formatter) {
            formatter.name(getKind(), true);
            if (emphasisName()){
                formatter.emphasis(true);
                formatter.appendText(getName());
                formatter.emphasis(false);
            } else {
                formatter.appendText(getName());
            }

            formatter.name(getKind(), false);

            return formatter.getText();
        }

        protected boolean emphasisName() {
            return true;//cons.isResolved()
        }

        public ElementKind getKind() {
            return ElementKind.GLOBAL;
        }
    }

    static class ClassItem extends PHPCompletionItem {
        private boolean endWithDoubleColon;
        ClassItem(ClassElement clazz, CompletionRequest request, boolean endWithDoubleColon, QualifiedNameKind generateAs) {
            super(clazz, request, generateAs);
            this.endWithDoubleColon = endWithDoubleColon;
        }

        public ElementKind getKind() {
            return ElementKind.CLASS;
        }

        @Override
        public String getCustomInsertTemplate() {
            if (endWithDoubleColon) {
                scheduleShowingCompletion();
            } else if (CompletionContext.NEW_CLASS.equals(request.context)) {
                scheduleShowingCompletion();
            }
            return super.getCustomInsertTemplate();
        }


        @Override
        public String getInsertPrefix() {
            final String superTemplate = super.getInsertPrefix();
            if (endWithDoubleColon) {
                StringBuilder builder = new StringBuilder();
                if (superTemplate != null) {
                    builder.append(superTemplate);
                } else {
                    builder.append(getName());
                }
                builder.append("::"); //NOI18N
                return builder.toString();
            } 
            return superTemplate;
        }
    }

    public static ImageIcon getInterfaceIcon() {
        return InterfaceItem.icon();
    }

    static class InterfaceItem extends PHPCompletionItem {
        private static final String PHP_INTERFACE_ICON = "org/netbeans/modules/php/editor/resources/interface.png"; //NOI18N
        private static ImageIcon INTERFACE_ICON = null;
        private boolean endWithDoubleColon;

        InterfaceItem(InterfaceElement iface, CompletionRequest request, boolean endWithDoubleColon) {
            super(iface, request);
            this.endWithDoubleColon = endWithDoubleColon;
        }
        InterfaceItem(InterfaceElement iface, CompletionRequest request, QualifiedNameKind generateAs, boolean endWithDoubleColon) {
            super(iface, request, generateAs);
            this.endWithDoubleColon = endWithDoubleColon;
        }

        public ElementKind getKind() {
            return ElementKind.CLASS;
        }

        private static ImageIcon icon() {
            if (INTERFACE_ICON == null) {
                INTERFACE_ICON = new ImageIcon(ImageUtilities.loadImage(PHP_INTERFACE_ICON));
            }
            return INTERFACE_ICON;
        }

        @Override
        public ImageIcon getIcon() {
            return icon();
        }

        @Override
        public String getInsertPrefix() {
            final String superTemplate = super.getInsertPrefix();
            if (endWithDoubleColon) {
                StringBuilder builder = new StringBuilder();
                if (superTemplate != null) {
                    builder.append(superTemplate);
                } else {
                    builder.append(getName());
                }
                builder.append("::"); //NOI18N
                scheduleShowingCompletion();
                return builder.toString();
            }
            return superTemplate;
        }    

    }

    static class VariableItem extends PHPCompletionItem {

        VariableItem(VariableElement variable, CompletionRequest request) {
            super(variable, request);
        }

        VariableElement getVariable() {
            return (VariableElement) getElement();
        }

        @Override public String getLhsHtml(HtmlFormatter formatter) {
            formatter.type(true);
            formatter.appendText(getTypeName());
            formatter.type(false);
            formatter.appendText(" "); //NOI18N
            formatter.name(getKind(), true);
            formatter.appendText(getName());
            formatter.name(getKind(), false);

            return formatter.getText();
        }

       

        public ElementKind getKind() {
            return ElementKind.VARIABLE;
        }

        @Override
        public String getInsertPrefix() {
            Completion.get().showToolTip();
             return getName();
        }


        protected String getTypeName() {
            Set<TypeResolver> types = getVariable().getInstanceTypes();
            String typeName = types.isEmpty() ? "?" : types.size() > 1 ?  "mixed" : "?";//NOI18N
            if (types.size() == 1) {
                TypeResolver typeResolver = types.iterator().next();
                if (typeResolver.isResolved()) {
                    QualifiedName qualifiedName = typeResolver.getTypeName(false);
                    if (qualifiedName != null) {
                        typeName = qualifiedName.toString();
                    }
                }
            }
            return typeName;
        }
    }

    static class SpecialFunctionItem extends KeywordItem{
        public SpecialFunctionItem(String fncName, CompletionRequest request) {
            super(fncName, request);
        }

        @Override
        public String getCustomInsertTemplate() {
            StringBuilder builder = new StringBuilder();
            builder.append(getName());
            builder.append(" '${cursor}';"); //NOI18N
            return builder.toString();
        }
    }

    static class ReturnItem extends KeywordItem{
        public ReturnItem(CompletionRequest request) {
            super("return", request); //NOI18N
        }

        @Override
        public String getCustomInsertTemplate() {
            return "return ${cursor};"; //NOI18N
        }
    }




    static class CompletionRequest {
        public  int anchor;
        public  PHPParseResult result;
        public  ParserResult info;
        public  String prefix;
        public  String currentlyEditedFileURL;
        public CompletionContext context;
        ElementQuery.Index index;
    }
    private static void scheduleShowingCompletion() {
        if (OptionsUtils.autoCompletionTypes()) {
            service.schedule(new Runnable() {

                public void run() {
                    Completion.get().showCompletion();
                }
            }, 750, TimeUnit.MILLISECONDS);
        }
    }
}
