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

import com.sun.el.parser.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.csl.api.CodeCompletionContext;
import org.netbeans.modules.csl.api.CodeCompletionHandler;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ParameterInfo;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.modules.web.el.*;
import org.netbeans.modules.web.el.refactoring.RefactoringUtil;
import org.netbeans.modules.web.el.spi.ELPlugin;
import org.netbeans.modules.web.el.spi.ELVariableResolver.VariableInfo;
import org.netbeans.modules.web.el.spi.Function;
import org.netbeans.modules.web.el.spi.ResourceBundle;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Code completer for Expression Language.
 *
 * @author Erno Mononen
 */
public final class ELCodeCompletionHandler implements CodeCompletionHandler {

    private static Set<String> keywordFixedTexts = null;

    /**
     * Gets Set of {@link ELTokenId#fixedText()} values with {@link ELTokenId.ELTokenCategories.KEYWORDS} category.<br/>
     * Result is resolved and stored on first method call into static field {@link #keywordFixedTexts}.
     * On next call is returned the same Set instance.
     * @return Set of {@link ELTokenId#fixedText()} values with {@link ELTokenId.ELTokenCategories.KEYWORDS} category.
     */
    private static synchronized Set<String> getKeywordFixedTexts() {
        if (keywordFixedTexts == null) {
            keywordFixedTexts = new HashSet<String>();
            for (ELTokenId elTokenId : ELTokenId.values()) {
                if (ELTokenId.ELTokenCategories.KEYWORDS.hasCategory(elTokenId)) {
                    keywordFixedTexts.add(elTokenId.fixedText());
                }
            }
        }
        return keywordFixedTexts;
    }
 
    @Override
    public CodeCompletionResult complete(final CodeCompletionContext context) {
        final List<CompletionProposal> proposals = new ArrayList<CompletionProposal>(50);
        CodeCompletionResult result = new DefaultCompletionResult(proposals, false);
        final ELElement element = getElementAt(context.getParserResult(), context.getCaretOffset());
        if (element == null || !element.isValid()) {
            return CodeCompletionResult.NONE;
        }
        Node target = getTargetNode(element, context.getCaretOffset());
        if(target == null) {
            //completion called outside of the EL content, resp. inside the
            //delimiters #{ or } area
            return CodeCompletionResult.NONE;
        }
        AstPath path = new AstPath(element.getNode());
        final List<Node> rootToNode = path.rootToNode(target);
        if (rootToNode.isEmpty()) {
            return result;
        }
        final PrefixMatcher prefixMatcher = PrefixMatcher.create(target, context);
        if (prefixMatcher == null) {
            return CodeCompletionResult.NONE;
        }
        // see if it is bundle key in the array notation and if so complete them
        if (target instanceof AstString) {
            ResourceBundles bundle = ResourceBundles.get(getFileObject(context));
            String bundleIdentifier = bundle.findResourceBundleIdentifier(path);
            if (bundleIdentifier != null) {
                proposeBundleKeysInArrayNotation(context, prefixMatcher, element, bundleIdentifier, (AstString) target, proposals);
                return proposals.isEmpty() ? CodeCompletionResult.NONE : result;
            }
        }

        Node previous = rootToNode.get(rootToNode.size() - 1);
        final Node nodeToResolve = getNodeToResolve(target, previous);
        
        final FileObject file = context.getParserResult().getSnapshot().getSource().getFileObject();
        JavaSource jsource = JavaSource.create(ClasspathInfo.create(file));
        try {
            jsource.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController info) throws Exception {
                    info.toPhase(JavaSource.Phase.RESOLVED);
                    CompilationContext ccontext = CompilationContext.create(file, info);
                    
                    Element resolved =
                            ELTypeUtilities.resolveElement(ccontext, element, nodeToResolve);
                    
                    if (ELTypeUtilities.isRawObjectReference(ccontext, nodeToResolve)) {
                        proposeRawObjectProperties(ccontext, context, prefixMatcher, nodeToResolve, proposals);            
                    } else if(ELTypeUtilities.isScopeObject(ccontext, nodeToResolve)) {
                        // seems to be something like "sessionScope.^", so complete beans from the scope
                        proposeBeansFromScope(ccontext, context, prefixMatcher, element, nodeToResolve, proposals);
                    } else if(ELTypeUtilities.isResourceBundleVar(ccontext, nodeToResolve)) {
                        proposeBundleKeysInDotNotation(context, prefixMatcher, element, nodeToResolve, proposals);
                    } else if(resolved == null) {
                        proposeFunctions(ccontext, context, prefixMatcher, element, proposals);
                        proposeManagedBeans(ccontext, context, prefixMatcher, element, proposals);
                        proposeBundles(ccontext, context, prefixMatcher, element, proposals);
                        proposeVariables(ccontext, context, prefixMatcher, element, proposals);
                        proposeImpicitObjects(ccontext, context, prefixMatcher, proposals);
                        proposeKeywords(context, prefixMatcher, proposals);
                    } else {
                        proposeMethods(ccontext, context, resolved, prefixMatcher, element, proposals, rootToNode);
                    }
                    
                    
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        

        return proposals.isEmpty() ? CodeCompletionResult.NONE : result;
    }

    private Node getNodeToResolve(Node target, Node previous) {
        // due to the ast structure in the case of identifiers we need to try to
        // resolve the type of the identifier, otherwise the type of the preceding
        // node.
        if (target instanceof AstIdentifier
                && (previous instanceof AstIdentifier
                || previous instanceof AstDotSuffix
                || NodeUtil.isMethodCall(previous))) {
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
        ELSanitizer sanitizer = new ELSanitizer(result, offset - result.getOriginalOffset().getStart());
        return sanitizer.sanitized();
    }

    /**
     * @param offset swing <b>document</b> offset
     */
    private Node getTargetNode(ELElement element, int offset) {
        Node result = element.findNodeAt(offset);
        // in EL AST for example #{foo.bar^} the caret is at a deferred expression node, whereas
        // for code completion we need the "bar" property node; the code below tries to accomplish
        // that
        if (result instanceof AstDeferredExpression || result instanceof AstDynamicExpression) {
            //ensure we do not jump out from the expression && do not offer EL stuff in delimiters
            int relativeOffset = offset - element.getOriginalOffset().getStart();
            assert relativeOffset >= 0;
            
            //do the workaround only if the completion called at or after 
            //the opening EL delimiter #{ or ${, not before or inside it
            if(relativeOffset >= 2) { 
                Node realTarget = element.findNodeAt(offset - 1);
                if (realTarget != null) {
                    result = realTarget;
                }
            } else {
                //ensure no EL completion before or inside the delimiters
                return null; 
            }
        }
        return result;
    }

    private FileObject getFileObject(CodeCompletionContext context) {
        return context.getParserResult().getSnapshot().getSource().getFileObject();
    }

    private void proposeMethods(CompilationContext info, CodeCompletionContext context, Element resolved,
            PrefixMatcher prefix, ELElement elElement,List<CompletionProposal> proposals, List<Node> rootToNode) {
        
        List<Element> allTypes = ELTypeUtilities.getSuperTypesFor(info, resolved, elElement, rootToNode);
        for(Element element : allTypes) {
            for (ExecutableElement enclosed : ElementFilter.methodsIn(element.getEnclosedElements())) {
                //do not propose Object's members
                if(element.getSimpleName().contentEquals("Object")) { //NOI18N
                    //XXX hmm, not an ideal non-fqn check
                    continue;
                }
                
                if (!enclosed.getModifiers().contains(Modifier.PUBLIC) ||
                        enclosed.getModifiers().contains(Modifier.STATIC)) {
                    continue;
                }
                String methodName = enclosed.getSimpleName().toString();
                String propertyName = RefactoringUtil.getPropertyName(methodName, true);
                if (!prefix.matches(propertyName)) {
                    continue;
                }
                
                // Now check methodName or propertyName is EL keyword.
                if (getKeywordFixedTexts().contains(propertyName) || 
                        getKeywordFixedTexts().contains(methodName) ) {
                    continue;
                }
                if (ELPlugin.Query.isValidProperty(enclosed, context.getParserResult().getSnapshot().getSource(), info, context)) {
                    ELVariableCompletionItem completionItem = new ELVariableCompletionItem(propertyName, ELTypeUtilities.getTypeNameFor(info, enclosed));
                    completionItem.setSmart(true);
                    completionItem.setAnchorOffset(context.getCaretOffset() - prefix.length());

                    proposals.add(completionItem);
                } else {
                    ELJavaCompletionItem completionItem;
                    
                    if (enclosed.getParameters().isEmpty() && !contains(proposals, propertyName)) {
                        completionItem = new ELJavaCompletionItem(info, enclosed, elElement); //
                    } else {
                        completionItem = new ELJavaCompletionItem(info, enclosed, methodName, elElement);
                    }
                    
                    completionItem.setSmart(false);
                    completionItem.setAnchorOffset(context.getCaretOffset() - prefix.length());

                    proposals.add(completionItem);
                }
            }
        }
    }
    
    private boolean contains(List<CompletionProposal> completionProposals, String propertyName) {
        if (propertyName == null || propertyName.isEmpty()) {
            return true;
        }
        for (CompletionProposal completionProposal : completionProposals) {
            if (propertyName.equals(completionProposal.getName())) {
                return true;
            }
        }
        return false;
    }

    private void proposeImpicitObjects(CompilationContext info, CodeCompletionContext context,
            PrefixMatcher prefix, List<CompletionProposal> proposals) {

        for (org.netbeans.modules.web.el.spi.ImplicitObject implicitObject : ELTypeUtilities.getImplicitObjects(info)) {
            if (prefix.matches(implicitObject.getName())) {
                ELImplictObjectCompletionItem item = new ELImplictObjectCompletionItem(implicitObject.getName(), implicitObject.getClazz());
                item.setAnchorOffset(context.getCaretOffset() - prefix.length());
                item.setSmart(true);
                proposals.add(item);
            }
        }
    }

    private void proposeKeywords(CodeCompletionContext context,
            PrefixMatcher prefix, List<CompletionProposal> proposals) {

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

    private void proposeManagedBeans(CompilationContext info, CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, List<CompletionProposal> proposals) {

        for (VariableInfo bean : ELVariableResolvers.getManagedBeans(info, getFileObject(context))) {
            if (!prefix.matches(bean.name)) {
                continue;
            }
            Element element = ELTypeUtilities.getElementForType(info, bean.clazz);
            if(element == null) {
                continue; //unresolvable bean class name
            }
            ELJavaCompletionItem item = new ELJavaCompletionItem(info, element, bean.name, elElement);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            item.setSmart(true);
            proposals.add(item);
        }
    }

    private void proposeBeansFromScope(CompilationContext info, CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, Node scopeNode, List<CompletionProposal> proposals) {

        String scope = scopeNode.getImage();
        // this is ugly, but in the JSF model beans
        // are stored to "session", "application" etc (instead of "sessionScope").
        // see org.netbeans.modules.web.jsf.api.facesmodel.ManagedBean.Scope
        final String scopeString = "Scope";//NOI18N
        if (scope.endsWith(scopeString)) {
            scope = scope.substring(0, scope.length() - scopeString.length());
        }

        for (VariableInfo bean : ELVariableResolvers.getBeansInScope(info, scope, context.getParserResult().getSnapshot())) {
            if (!prefix.matches(bean.name)) {
                continue;
            }
            Element element = ELTypeUtilities.getElementForType(info, bean.clazz);
            ELJavaCompletionItem item = new ELJavaCompletionItem(info, element, elElement);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            item.setSmart(true);
            proposals.add(item);
        }
    }

    private void proposeRawObjectProperties(CompilationContext info, CodeCompletionContext context,
            PrefixMatcher prefix, Node scopeNode, List<CompletionProposal> proposals) {

        for (VariableInfo property : ELVariableResolvers.getRawObjectProperties(info, scopeNode.getImage(), context.getParserResult().getSnapshot())) {
            if (!prefix.matches(property.name)) {
                continue;
            }
            ELRawObjectPropertyCompletionItem item = new ELRawObjectPropertyCompletionItem(property.name);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            item.setSmart(true);
            proposals.add(item);
        }
    }

    private void proposeVariables(CompilationContext info, CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, List<CompletionProposal> proposals) {

        for (VariableInfo bean : ELVariableResolvers.getVariables(info, context.getParserResult().getSnapshot(), context.getCaretOffset())) {
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
                Element element = ELTypeUtilities.getElementForType(info, bean.clazz);
                if (element == null) {
                    continue;
                }
                ELJavaCompletionItem item = new ELJavaCompletionItem(info, element, elElement);
                item.setAnchorOffset(context.getCaretOffset() - prefix.length());
                item.setSmart(true);
                proposals.add(item);
            }
        }
    }

    private void proposeBundles(CompilationContext ccontext, CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, List<CompletionProposal> proposals) {

        ResourceBundles resourceBundles = ResourceBundles.get(getFileObject(context));
        if (!resourceBundles.canHaveBundles()) {
            return;
        }
        for (ResourceBundle bundle : resourceBundles.getBundles(ccontext.context())) {
            if (!prefix.matches(bundle.getVar())) {
                continue;
            }
            ELResourceBundleCompletionItem item = new ELResourceBundleCompletionItem(ccontext.file(), bundle, resourceBundles);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            proposals.add(item);
        }

    }

    private void proposeBundleKeysInArrayNotation(CodeCompletionContext context,
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
    
    // "msg.key" notation
    private void proposeBundleKeysInDotNotation(CodeCompletionContext context,
            PrefixMatcher prefix, 
            ELElement elElement, 
            Node baseObjectNode,
            List<CompletionProposal> proposals) {
        
        String bundleKey = baseObjectNode.getImage();
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

    private void proposeFunctions(CompilationContext info, CodeCompletionContext context,
            PrefixMatcher prefix, ELElement elElement, List<CompletionProposal> proposals) {

        for (Function function : ELTypeUtilities.getELFunctions(info)) {
            if (!prefix.matches(function.getName())) {
                continue;
            }
            ELFunctionCompletionItem item = new ELFunctionCompletionItem(
                    function.getName(),
                    function.getReturnType(),
                    function.getParameters(),
                    function.getDescription());
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            proposals.add(item);
        }
    }

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
        assert typedText.length() > 0;
        char last = typedText.charAt(typedText.length() - 1);
        switch(last) {
            case '.':
                return QueryType.COMPLETION;
        }
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
