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

import com.sun.el.parser.AstIdentifier;
import com.sun.el.parser.AstMethodSuffix;
import com.sun.el.parser.AstPropertySuffix;
import com.sun.el.parser.AstString;
import com.sun.el.parser.Node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.ElementFilter;
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
import org.netbeans.modules.web.core.syntax.completion.api.ElCompletionItem.ELImplicitObject;
import org.netbeans.modules.web.core.syntax.spi.ImplicitObjectProvider;
import org.netbeans.modules.web.el.AstPath;
import org.netbeans.modules.web.el.ELElement;
import org.netbeans.modules.web.el.ELParserResult;
import org.netbeans.modules.web.el.ELTypeUtilities;
import org.netbeans.modules.web.el.ELVariableResolvers;
import org.netbeans.modules.web.el.ResourceBundles;
import org.netbeans.modules.web.el.refactoring.RefactoringUtil;
import org.netbeans.modules.web.el.spi.ELVariableResolver.VariableInfo;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

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
        final String prefix = context.getPrefix() != null ? context.getPrefix() : "";
        if (element == null || !element.isValid()) {
            return CodeCompletionResult.NONE;
        }

        Node target = element.findNodeAt(context.getCaretOffset());
        AstPath path = new AstPath(element.getNode());
        List<Node> rootToNode = path.rootToNode(target);
        if (rootToNode.isEmpty()) {
            return result;
        }
        
        if (target instanceof AstString) {
            ResourceBundles bundle = ResourceBundles.get(getFileObject(context));
            String bundleIdentifier = bundle.findResourceBundleIdentifier(path);
            if (bundleIdentifier != null) {
                proposeBundleKeys(context, prefix, element, bundleIdentifier, (AstString) target, proposals);
                return proposals.isEmpty() ? CodeCompletionResult.NONE : result;
            }
        }

        ELTypeUtilities typeUtilities = ELTypeUtilities.create(getFileObject(context));
        Node previous = rootToNode.get(rootToNode.size() - 1);

        // due to the ast structure in the case of identifiers we need to try to
        // resolve the type of the identifier, otherwise the type of the preceding
        // node.
        Node nodeToResolve;
        if (target instanceof AstIdentifier &&
                (previous instanceof AstIdentifier
                || previous instanceof AstPropertySuffix
                || previous instanceof AstMethodSuffix)) {
            nodeToResolve = target;
        } else {
            nodeToResolve = previous;
        }

        Element resolved = typeUtilities.resolveElement(element, nodeToResolve);

        if (resolved == null) {
            proposeManagedBeans(context, prefix, element, typeUtilities, proposals);
            proposeVariables(context, prefix, element, typeUtilities, proposals);
            proposeImpicitObjects(context, prefix, element, typeUtilities, proposals);
            proposeKeywords(context, prefix, element, typeUtilities, proposals);
        } else {
            proposeMethods(context, resolved, prefix, element, typeUtilities, proposals);
        }

        return proposals.isEmpty() ? CodeCompletionResult.NONE : result;
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

    private FileObject getFileObject(CodeCompletionContext context) {
        return context.getParserResult().getSnapshot().getSource().getFileObject();
    }

    private void proposeMethods(CodeCompletionContext context, Element resolved,
            String prefix, ELElement elElement, ELTypeUtilities typeUtilities, List<CompletionProposal> proposals) {

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
            if (!propertyName.startsWith(prefix)) {
                continue;
            }
            ELJavaCompletionItem item = new ELJavaCompletionItem(enclosed, elElement, typeUtilities);
            item.setSmart(true);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            proposals.add(item);
        }
    }

    private void proposeImpicitObjects(CodeCompletionContext context,
            String prefix, ELElement elElement, ELTypeUtilities typeUtilities, List<CompletionProposal> proposals) {

        for (org.netbeans.modules.web.core.syntax.spi.ELImplicitObject implicitObject : ELTypeUtilities.getImplicitObjects()) {
            if (implicitObject.getName().startsWith(prefix)) {
                ELImplictObjectCompletionItem item = new ELImplictObjectCompletionItem(implicitObject.getName(), implicitObject.getClazz());
                item.setAnchorOffset(context.getCaretOffset() - prefix.length());
                item.setSmart(true);
                proposals.add(item);
            }
        }
    }
    
    private void proposeKeywords(CodeCompletionContext context,
            String prefix, ELElement elElement, ELTypeUtilities typeUtilities, List<CompletionProposal> proposals) {

        for (ELTokenId elToken : ELTokenId.values()) {
            if (!ELTokenId.ELTokenCategories.KEYWORDS.hasCategory(elToken)) {
                continue;
            }
            if (elToken.fixedText() == null) {
                continue;
            }
            if (elToken.fixedText().startsWith(prefix)) {
                ELKeywordCompletionItem item = new ELKeywordCompletionItem(elToken.fixedText());
                item.setAnchorOffset(context.getCaretOffset() - prefix.length());
                proposals.add(item);
            }

        }
    }

    private void proposeManagedBeans(CodeCompletionContext context,
            String prefix, ELElement elElement, ELTypeUtilities typeUtilities, List<CompletionProposal> proposals) {
        
        for (VariableInfo bean : ELVariableResolvers.getManagedBeans(getFileObject(context))) {
            if (!bean.name.startsWith(prefix)) {
                continue;
            }
            Element element = typeUtilities.getElementForType(bean.clazz);
            ELJavaCompletionItem item = new ELJavaCompletionItem(element, elElement, typeUtilities);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            item.setSmart(true);
            proposals.add(item);
        }
    }

    private void proposeVariables(CodeCompletionContext context,
            String prefix, ELElement elElement, ELTypeUtilities typeUtilities, List<CompletionProposal> proposals) {

        for (VariableInfo bean : ELVariableResolvers.getVariables(context.getParserResult().getSnapshot(), context.getCaretOffset())) {
            if (!bean.name.startsWith(prefix)) {
                continue;
            }
            Element element = typeUtilities.getElementForType(bean.clazz);
            ELJavaCompletionItem item = new ELJavaCompletionItem(element, elElement, typeUtilities);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            item.setSmart(true);
            proposals.add(item);
        }
    }

    private void proposeBundleKeys(CodeCompletionContext context,
            String prefix, ELElement elElement, String bundleKey, AstString target, List<CompletionProposal> proposals) {
        
        if (target.getImage().isEmpty()
                || elElement.getOriginalOffset(target).getStart() >= context.getCaretOffset()) {
            return;
        }
        ResourceBundles resourceBundles = ResourceBundles.get(getFileObject(context));
        if (!resourceBundles.canHaveBundles()) {
            return;
        }
        for (Map.Entry<String, String> entry : resourceBundles.getEntries(bundleKey).entrySet()) {
            if (!entry.getKey().startsWith(prefix)) {
                continue;
            }
            ELResourceBundleCompletionItem item = new ELResourceBundleCompletionItem(entry.getKey(), entry.getValue(), elElement);
            item.setSmart(true);
            item.setAnchorOffset(context.getCaretOffset() - prefix.length());
            proposals.add(item);
        }
    }


    @Override
    public String document(ParserResult info, final ElementHandle element) {
        if (!(element instanceof ELJavaCompletionItem.ElementHandleAdapter)) {
            return null;
        }
        final String[] result = new String[1];
        try {
            ClasspathInfo cp = ClasspathInfo.create(info.getSnapshot().getSource().getFileObject());
            JavaSource source = JavaSource.create(cp);
            if (source == null) {
                return null;
            }
            source.runUserActionTask(new Task<CompilationController>() {

                @Override
                public void run(CompilationController parameter) throws Exception {
                    Element javaElement = ((ELJavaCompletionItem.ElementHandleAdapter) element).getOriginalElement();
                    result[0] = parameter.getElements().getDocComment(javaElement);
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result[0];
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
    public Set<String> getApplicableTemplates(ParserResult info, int selectionBegin, int selectionEnd) {
        return Collections.emptySet();
    }

    @Override
    public ParameterInfo parameters(ParserResult info, int caretOffset, CompletionProposal proposal) {
        return ParameterInfo.NONE;
    }

}
