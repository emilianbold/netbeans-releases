/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.el.completion;

import com.sun.el.parser.AstDeferredExpression;
import com.sun.el.parser.AstDynamicExpression;
import com.sun.el.parser.AstIdentifier;
import com.sun.el.parser.AstMethodSuffix;
import com.sun.el.parser.AstPropertySuffix;
import com.sun.el.parser.AstString;
import com.sun.el.parser.Node;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.modules.web.el.AstPath;
import org.netbeans.modules.web.el.ELElement;
import org.netbeans.modules.web.el.ELParserResult;
import org.netbeans.modules.web.el.ELTypeUtilities;
import org.netbeans.modules.web.el.ELVariableResolvers;
import org.netbeans.modules.web.el.ResourceBundles;
import org.netbeans.modules.web.el.refactoring.RefactoringUtil;
import org.netbeans.modules.web.el.spi.ELVariableResolver.VariableInfo;
import org.openide.filesystems.FileObject;

/**
 * Code completer for Expression Language.
 *
 * @author Erno Mononen
 */
public final class ELCodeCompletionHandler implements CodeCompletionHandler {

    @Override
    public CodeCompletionResult complete(CodeCompletionContext context) {
        List<CompletionProposal> proposals = new ArrayList<CompletionProposal>(50);
        CodeCompletionResult result = new DefaultCompletionResult(proposals, false);
        ELElement element = getElementAt(context.getParserResult(), context.getCaretOffset());
        if (element == null || !element.isValid()) {
            return CodeCompletionResult.NONE;
        }
        Node target = getTargetNode(element, context.getCaretOffset());
        AstPath path = new AstPath(element.getNode());
        List<Node> rootToNode = path.rootToNode(target);
        if (rootToNode.isEmpty()) {
            return result;
        }
        final PrefixMatcher prefixMatcher = PrefixMatcher.create(target, context);
        if (prefixMatcher == null) {
            return CodeCompletionResult.NONE;
        }
        // see if it is bundle key and if so complete them
        if (target instanceof AstString) {
            ResourceBundles bundle = ResourceBundles.get(getFileObject(context));
            String bundleIdentifier = bundle.findResourceBundleIdentifier(path);
            if (bundleIdentifier != null) {
                proposeBundleKeys(context, prefixMatcher, element, bundleIdentifier, (AstString) target, proposals);
                return proposals.isEmpty() ? CodeCompletionResult.NONE : result;
            }
        }

        ELTypeUtilities typeUtilities = ELTypeUtilities.create(getFileObject(context));
        Node previous = rootToNode.get(rootToNode.size() - 1);

        Node nodeToResolve = getNodeToResolve(target, previous);
        Element resolved =
                typeUtilities.resolveElement(element, nodeToResolve);

        if (typeUtilities.isRawObject(nodeToResolve)) {
            proposeRawObjectProperties(context, prefixMatcher, element, nodeToResolve, typeUtilities, proposals);            
        } else if(typeUtilities.isScopeObject(nodeToResolve)) {
            // seems to be something like "sessionScope.^", so complete beans from the scope
            proposeBeansFromScope(context, prefixMatcher, element, nodeToResolve, typeUtilities, proposals);
        } else if(resolved == null) {
            // not yet working properly
            //proposeFunctions(context, prefix, element, prefix, typeUtilities, proposals);
            proposeManagedBeans(context, prefixMatcher, element, typeUtilities, proposals);
            proposeBundles(context, prefixMatcher, element, proposals);
            proposeVariables(context, prefixMatcher, element, typeUtilities, proposals);
            proposeImpicitObjects(context, prefixMatcher, element, typeUtilities, proposals);
            proposeKeywords(context, prefixMatcher, element, typeUtilities, proposals);
        } else {
            proposeMethods(context, resolved, prefixMatcher, element, typeUtilities, proposals);
        }

        return proposals.isEmpty() ? CodeCompletionResult.NONE : result;
    }

    private Node getNodeToResolve(Node target, Node previous) {
        // due to the ast structure in the case of identifiers we need to try to
        // resolve the type of the identifier, otherwise the type of the preceding
        // node.
        if (target instanceof AstIdentifier
                && (previous instanceof AstIdentifier
                || previous instanceof AstPropertySuffix
                || previous instanceof AstMethodSuffix)) {
            return target;
        } else {
            return previous;
        }
    }

    private ELElement getElementAt(ParserResult parserResult, int offset) {
        ELParserResult elParserResult = (ELParserResult) parserResult;
        ELElement result = elParserResult.getElementAt(offset);
        if (result == null || result.isValid()) {
            return result;
        }
        // try to sanitize
        ELSanitizer sanitizer = new ELSanitizer(result);
        return sanitizer.sanitized();
    }

    private Node getTargetNode(ELElement element, int offset) {
        Node result = element.findNodeAt(offset);
        // in EL AST for example #{foo.bar^} the caret is at a deferred expression node, whereas
        // for code completion we need the "bar" property node; the code below tries to accomplish
        // that
        if (result instanceof AstDeferredExpression || result instanceof AstDynamicExpression) {
            Node realTarget = element.findNodeAt(offset - 1);
            if (realTarget != null) {
                result = realTarget;
            }
        }
        return result;
    }

    private FileObject getFileObject(CodeCompletionContext context) {
        return context.getParserResult().getSnapshot().getSource().getFileObject();
    }

    private void proposeMethods(CodeCompletionContext context, Element resolved,
            PrefixMatcher prefix, ELElement elElement, ELTypeUtilities typeUtilities, List<CompletionProposal> proposals) {

        resolved = typeUtilities.getTypeFor(resolved);
        if (resolved == null || resolved.getKind() == ElementKind.TYPE_PARAMETER) {
            return;
        }
        for (ExecutableElement enclosed : ElementFilter.methodsIn(resolved.getEnclosedElements())) {
            if (!enclosed.getModifiers().contains(Modifier.PUBLIC)) {
                continue;
            }
            String methodName = enclosed.getSimpleName().toString();
            String propertyName = RefactoringUtil.getPropertyName(methodName, true);
            if (!prefix.matches(propertyName)) {
                continue;
            }
            ELJavaCompletionItem item = new ELJavaCompletionItem(enclosed, elElement, typeUtilities);
            item.setSmart(true);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            proposals.add(item);
        }
    }

    private void proposeImpicitObjects(CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, ELTypeUtilities typeUtilities, List<CompletionProposal> proposals) {

        for (org.netbeans.modules.web.el.spi.ImplicitObject implicitObject : typeUtilities.getImplicitObjects()) {
            if (prefix.matches(implicitObject.getName())) {
                ELImplictObjectCompletionItem item = new ELImplictObjectCompletionItem(implicitObject.getName(), implicitObject.getClazz());
                item.setAnchorOffset(context.getCaretOffset() - prefix.length());
                item.setSmart(true);
                proposals.add(item);
            }
        }
    }

    private void proposeKeywords(CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, ELTypeUtilities typeUtilities, List<CompletionProposal> proposals) {

        for (ELTokenId elToken : ELTokenId.values()) {
            if (!ELTokenId.ELTokenCategories.KEYWORDS.hasCategory(elToken)) {
                continue;
            }
            if (elToken.fixedText() == null) {
                continue;
            }
            if (prefix.matches(elToken.fixedText())) {
                ELKeywordCompletionItem item = new ELKeywordCompletionItem(elToken.fixedText());
                item.setAnchorOffset(context.getCaretOffset() - prefix.length());
                proposals.add(item);
            }

        }
    }

    private void proposeManagedBeans(CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, ELTypeUtilities typeUtilities, List<CompletionProposal> proposals) {

        for (VariableInfo bean : ELVariableResolvers.getManagedBeans(getFileObject(context))) {
            if (!prefix.matches(bean.name)) {
                continue;
            }
            Element element = typeUtilities.getElementForType(bean.clazz);
            if(element == null) {
                continue; //unresolvable bean class name
            }
            ELJavaCompletionItem item = new ELJavaCompletionItem(element, bean.name, elElement, typeUtilities);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            item.setSmart(true);
            proposals.add(item);
        }
    }

    private void proposeBeansFromScope(CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, Node scopeNode, ELTypeUtilities typeUtilities, List<CompletionProposal> proposals) {

        String scope = scopeNode.getImage();
        // this is ugly, but in the JSF model beans
        // are stored to "session", "application" etc (instead of "sessionScope").
        // see org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean.Scope
        final String scopeString = "Scope";//NOI18N
        if (scope.endsWith(scopeString)) {
            scope = scope.substring(0, scope.length() - scopeString.length());
        }

        for (VariableInfo bean : ELVariableResolvers.getBeansInScope(scope, context.getParserResult().getSnapshot())) {
            if (!prefix.matches(bean.name)) {
                continue;
            }
            Element element = typeUtilities.getElementForType(bean.clazz);
            ELJavaCompletionItem item = new ELJavaCompletionItem(element, elElement, typeUtilities);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            item.setSmart(true);
            proposals.add(item);
        }
    }

    private void proposeRawObjectProperties(CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, Node scopeNode, ELTypeUtilities typeUtilities, List<CompletionProposal> proposals) {

        for (VariableInfo property : ELVariableResolvers.getRawObjectProperties(scopeNode.getImage(), context.getParserResult().getSnapshot())) {
            if (!prefix.matches(property.name)) {
                continue;
            }
            ELRawObjectPropertyCompletionItem item = new ELRawObjectPropertyCompletionItem(property.name);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            item.setSmart(true);
            proposals.add(item);
        }
    }

    private void proposeVariables(CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, ELTypeUtilities typeUtilities, List<CompletionProposal> proposals) {

        for (VariableInfo bean : ELVariableResolvers.getVariables(context.getParserResult().getSnapshot(), context.getCaretOffset())) {
            if (!prefix.matches(bean.name)) {
                continue;
            }
            if(bean.clazz == null) {
                //probably a refered (w/o type) variable, just show it in the completion w/o type
                ELVariableCompletionItem item = new ELVariableCompletionItem(bean.name, bean.expression);
                item.setAnchorOffset(context.getCaretOffset() - prefix.length());
                item.setSmart(true);
                proposals.add(item);

            } else {
                //resolved variable
                Element element = typeUtilities.getElementForType(bean.clazz);
                if (element == null) {
                    continue;
                }
                ELJavaCompletionItem item = new ELJavaCompletionItem(element, elElement, typeUtilities);
                item.setAnchorOffset(context.getCaretOffset() - prefix.length());
                item.setSmart(true);
                proposals.add(item);
            }
        }
    }

    private void proposeBundles(CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, List<CompletionProposal> proposals) {

        ResourceBundles resourceBundles = ResourceBundles.get(getFileObject(context));
        if (!resourceBundles.canHaveBundles()) {
            return;
        }
        for (String bundle : resourceBundles.getBundles()) {
            if (!prefix.matches(bundle)) {
                continue;
            }
            ELResourceBundleCompletionItem item = new ELResourceBundleCompletionItem(bundle);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            proposals.add(item);
        }

    }

    private void proposeBundleKeys(CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, String bundleKey, AstString target, List<CompletionProposal> proposals) {

        if (target.getImage().isEmpty()
                || elElement.getOriginalOffset(target).getStart() >= context.getCaretOffset()) {
            return;
        }
        ResourceBundles resourceBundles = ResourceBundles.get(getFileObject(context));
        if (!resourceBundles.canHaveBundles()) {
            return;
        }
        for (Map.Entry<String, String> entry : resourceBundles.getEntries(bundleKey).entrySet()) {
            if (!prefix.matches(entry.getKey())) {
                continue;
            }
            ELResourceBundleKeyCompletionItem item = new ELResourceBundleKeyCompletionItem(entry.getKey(), entry.getValue(), elElement);
            item.setSmart(true);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            proposals.add(item);
        }
    }

//    private void proposeFunctions(CodeCompletionContext context,
//            PrefixMatcher prefix, ELElement elElement, String bundleKey, ELTypeUtilities typeUtilities, List<CompletionProposal> proposals) {
//
//        for (Function function : ELFunctions.getFunctions(getFileObject(context), prefix.prefix)) {
//            ELFunctionCompletionItem item =
//                    new ELFunctionCompletionItem(function.getName(), function.getFunctionInfo().getFunctionClass());
//            item.setSmart(true);
//            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
//            proposals.add(item);
//        }
//    }

    @Override
    public String document(ParserResult info, final ElementHandle element) {
        if (!(element instanceof ELElementHandle)) {
            return null;
        }
        return ((ELElementHandle) element).document(info);
    }

    @Override
    public ElementHandle resolveLink(String link, ElementHandle originalHandle) {
        return null;
    }

    @Override
    public String getPrefix(ParserResult info, int caretOffset, boolean upToOffset) {
        ELElement element = getElementAt(info, caretOffset);
        if (element == null) {
            return null;
        }
        Node node = element.findNodeAt(caretOffset);
        // get the prefix for bundle keys
        if (node instanceof AstString) {
            int startOffset = element.getOriginalOffset(node).getStart();
            int end = caretOffset - startOffset;
            String image = node.getImage();
            if (end > 0 && !image.isEmpty()) {
                // index 0 in AstString#image is either ' or ",
                // so start from 1
                return image.substring(1, end);
            }
        }
        // use the default CSL behavior for getting the prefix
        return null;
    }

    @Override
    public QueryType getAutoQuery(JTextComponent component, String typedText) {
        return QueryType.NONE;
    }

    @Override
    public String resolveTemplateVariable(String variable, ParserResult info, int caretOffset, String name, Map parameters) {
        return null;
    }

    @Override
    public Set<String> getApplicableTemplates(Document doc, int selectionBegin, int selectionEnd) {
        return Collections.emptySet();
    }

    @Override
    public ParameterInfo parameters(ParserResult info, int caretOffset, CompletionProposal proposal) {
        return ParameterInfo.NONE;
    }

    private static class PrefixMatcher {

        private final String prefix;
        private final boolean exact;

        private PrefixMatcher(String value, boolean exact) {
            this.prefix = value;
            this.exact = exact;
        }

        static PrefixMatcher create(Node target, CodeCompletionContext context) {
            String prefix = context.getPrefix() != null ? context.getPrefix() : "";
            boolean isDoc = context.getQueryType() == QueryType.DOCUMENTATION;
            if (isDoc) {
                prefix = getPrefixForDocumentation(target);
            }
            // for documentation we need full prefix
            if (isDoc && prefix.isEmpty()) {
                return null;
            }
            return new PrefixMatcher(prefix, isDoc);
        }

        private static String getPrefixForDocumentation(Node target) {
            if (target instanceof AstString) {
                return ((AstString) target).getString();
            }
            return target.getImage() == null ? "" : target.getImage();
        }

        boolean matches(String str) {
            if (exact) {
                return prefix.equals(str);
            }
            return str.startsWith(prefix);
        }

        int length() {
            return prefix.length();
        }
    }
}
