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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.verification;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.*;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.elements.*;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement.PrintAs;
import org.netbeans.modules.php.editor.elements.TypeNameResolverImpl;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.*;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 * @author Radek Matous
 */
public class ImplementAbstractMethodsHint extends AbstractRule {

    private static final String HINT_ID = "Implement.Abstract.Methods"; //NOI18N
    private static final String ABSTRACT_PREFIX = "abstract "; //NOI18N
    private static final Logger LOGGER = Logger.getLogger(ImplementAbstractMethodsHint.class.getName());

    @Override
    public String getId() {
        return HINT_ID;
    }

    @Override
    @Messages("ImplementAbstractMethodsDesc=Implement All Abstract Methods")
    public String getDescription() {
        return Bundle.ImplementAbstractMethodsDesc();
    }

    @Override
    @Messages("ImplementAbstractMethodsDispName=Implement All Abstract Methods")
    public String getDisplayName() {
        return Bundle.ImplementAbstractMethodsDispName();
    }

    @Override
    public HintSeverity getDefaultSeverity() {
        return HintSeverity.ERROR;
    }

    @Override
    @Messages({
        "# {0} - Class name",
        "# {1} - Abstract method name",
        "# {2} - Owner (class) of abstract method",
        "ImplementAbstractMethodsHintDesc={0} is not abstract and does not override abstract method {1} in {2}"
    })
    void computeHintsImpl(PHPRuleContext context, List<Hint> hints, PHPHintsProvider.Kind kind) throws BadLocationException {
        FileScope fileScope = context.fileScope;
        if (fileScope != null) {
            Collection<? extends ClassScope> allClasses = ModelUtils.getDeclaredClasses(fileScope);
            FileObject fileObject = context.parserResult.getSnapshot().getSource().getFileObject();
            for (FixInfo fixInfo : checkHints(allClasses, context)) {
                hints.add(new Hint(ImplementAbstractMethodsHint.this, Bundle.ImplementAbstractMethodsHintDesc(fixInfo.className, fixInfo.lastMethodDeclaration, fixInfo.lastMethodOwnerName), fileObject, fixInfo.classNameRange, createHintFixes(context.doc, fixInfo), 500));
            }
        }
    }

    private List<HintFix> createHintFixes(BaseDocument doc, FixInfo fixInfo) {
        List<HintFix> hintFixes = new LinkedList<HintFix>();
        hintFixes.add(new ImplementAllFix(doc, fixInfo));
        hintFixes.add(new AbstractClassFix(doc, fixInfo));
        return Collections.unmodifiableList(hintFixes);
    }

    private Collection<FixInfo> checkHints(Collection<? extends ClassScope> allClasses, PHPRuleContext context) throws BadLocationException{
        List<FixInfo> retval = new ArrayList<FixInfo>();
        for (ClassScope classScope : allClasses) {
            if (!classScope.isAbstract()) {
                Index index = context.getIndex();
                Set<String> allValidMethods = new HashSet<String>();
                allValidMethods.addAll(toNames(getValidInheritedMethods(getInheritedMethods(classScope, index))));
                allValidMethods.addAll(toNames(index.getDeclaredMethods(classScope)));
                ElementFilter declaredMethods = ElementFilter.forExcludedNames(allValidMethods, PhpElementKind.METHOD);
                Set<MethodElement> accessibleMethods = declaredMethods.filter(index.getAccessibleMethods(classScope, classScope));
                LinkedHashSet<String> methodSkeletons = new LinkedHashSet<String>();
                MethodElement lastMethodElement = null;
                FileObject lastFileObject = null;
                FileScope fileScope = null;
                for (MethodElement methodElement : accessibleMethods) {
                    final TypeElement type = methodElement.getType();
                    if ((type.isInterface() || methodElement.isAbstract()) && !methodElement.isFinal()) {
                        FileObject fileObject = methodElement.getFileObject();
                        if (lastFileObject != fileObject) {
                            lastFileObject = fileObject;
                            fileScope = getFileScope(fileObject);
                        }
                        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(fileScope, methodElement.getOffset());
                        List typeNameResolvers = new ArrayList<TypeNameResolver>();
                        if (fileObject != null && CodeUtils.isPhp_52(fileObject)) {
                            typeNameResolvers.add(TypeNameResolverImpl.forUnqualifiedName());
                        } else {
                            typeNameResolvers.add(TypeNameResolverImpl.forFullyQualifiedName(namespaceScope, methodElement.getOffset()));
                            typeNameResolvers.add(TypeNameResolverImpl.forSmartName(classScope, classScope.getOffset()));
                        }
                        TypeNameResolver typeNameResolver = TypeNameResolverImpl.forChainOf(typeNameResolvers);
                        String skeleton = methodElement.asString(PrintAs.DeclarationWithEmptyBody, typeNameResolver);
                        skeleton = skeleton.replace(ABSTRACT_PREFIX, ""); //NOI18N
                        methodSkeletons.add(skeleton);
                        lastMethodElement = methodElement;
                    }
                }
                if (!methodSkeletons.isEmpty() && lastMethodElement != null) {
                    int newMethodsOffset = getNewMethodsOffset(classScope, context.doc);
                    int classDeclarationOffset = getClassDeclarationOffset(context.parserResult.getSnapshot().getTokenHierarchy(), classScope.getOffset());
                    if (newMethodsOffset != -1 && classDeclarationOffset != -1) {
                        retval.add(new FixInfo(classScope, methodSkeletons, lastMethodElement, newMethodsOffset, classDeclarationOffset));
                    }
                }
            }
        }
        return retval;
    }

    private FileScope getFileScope(final FileObject fileObject) {
        final FileScope[] fileScope = new FileScope[1];
        try {
            ParserManager.parse(Collections.singletonList(Source.create(fileObject)), new UserTask() {

                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    Result parserResult = resultIterator.getParserResult();
                    PHPParseResult phpResult = (PHPParseResult) parserResult;
                    fileScope[0] = phpResult.getModel().getFileScope();
                }
        });
        } catch (ParseException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
        return fileScope[0];
    }

    private Set<MethodElement> getInheritedMethods(final ClassScope classScope, final Index index) {
        Set<MethodElement> inheritedMethods = new HashSet<MethodElement>();
        Set<MethodElement> declaredSuperMethods =  new HashSet<MethodElement>();
        Set<MethodElement> accessibleSuperMethods =  new HashSet<MethodElement>();
        Collection<? extends ClassScope> superClasses = classScope.getSuperClasses();
        for (ClassScope cls : superClasses) {
            declaredSuperMethods.addAll(index.getDeclaredMethods(cls));
            accessibleSuperMethods.addAll(index.getAccessibleMethods(cls, classScope));
        }
        Collection<? extends InterfaceScope> superInterface = classScope.getSuperInterfaceScopes();
        for (InterfaceScope interfaceScope : superInterface) {
            declaredSuperMethods.addAll(index.getDeclaredMethods(interfaceScope));
            accessibleSuperMethods.addAll(index.getAccessibleMethods(interfaceScope, classScope));
        }
        Collection<? extends TraitScope> traits = classScope.getTraits();
        for (TraitScope traitScope : traits) {
            declaredSuperMethods.addAll(index.getDeclaredMethods(traitScope));
            accessibleSuperMethods.addAll(index.getAccessibleMethods(traitScope, classScope));
        }
        inheritedMethods.addAll(declaredSuperMethods);
        inheritedMethods.addAll(accessibleSuperMethods);
        return inheritedMethods;
    }

    private Set<MethodElement> getValidInheritedMethods(Set<MethodElement> inheritedMethods) {
        Set<MethodElement> retval = new HashSet<MethodElement>();
        for (MethodElement methodElement : inheritedMethods) {
            if (!methodElement.isAbstract()) {
                retval.add(methodElement);
            }
        }
        return retval;
    }

    private static Set<String> toNames(Set<? extends PhpElement> elements) {
        Set<String> names = new HashSet<String>();
        for (PhpElement elem : elements) {
            names.add(elem.getName());
        }
        return names;
    }

    private static int getClassDeclarationOffset(TokenHierarchy<?> th, int classNameOffset) {
        TokenSequence<PHPTokenId> ts = LexUtilities.getPHPTokenSequence(th, classNameOffset);
        ts.move(classNameOffset);
        ts.movePrevious();
        Token<? extends PHPTokenId> previousToken = LexUtilities.findPreviousToken(ts, Collections.<PHPTokenId>singletonList(PHPTokenId.PHP_CLASS));
        return previousToken.offset(th);
    }

    private static int getNewMethodsOffset(ClassScope classScope, BaseDocument doc) throws BadLocationException {
        int offset = -1;
        Collection<? extends MethodScope> declaredMethods = classScope.getDeclaredMethods();
        for (MethodScope methodScope : declaredMethods) {
            OffsetRange blockRange = methodScope.getBlockRange();
            if (blockRange != null && blockRange.getEnd() > offset) {
                offset = blockRange.getEnd();
            }
        }
        if (offset == -1 && classScope.getBlockRange() != null) {
            int rowStartOfClassEnd = Utilities.getRowStart(doc, classScope.getBlockRange().getEnd());
            int rowEndOfPreviousRow = rowStartOfClassEnd - 1;
            offset = Utilities.getRowStart(doc, rowEndOfPreviousRow);
        }
        return offset;
    }

    private class ImplementAllFix implements HintFix {

        private BaseDocument doc;
        private final FixInfo fixInfo;

        ImplementAllFix(BaseDocument doc, FixInfo fixInfo) {
            this.doc = doc;
            this.fixInfo = fixInfo;
        }

        @Override
        public String getDescription() {
            return ImplementAbstractMethodsHint.this.getDescription();
        }

        @Override
        public void implement() throws Exception {
            getEditList().apply();
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

        EditList getEditList() throws Exception {
            EditList edits = new EditList(doc);
            edits.setFormatAll(true);
            for (String methodScope : fixInfo.methodSkeletons) {
                edits.replace(fixInfo.newMethodsOffset, 0, methodScope, true, 0);
            }
            return edits;
        }
    }

    private static class AbstractClassFix implements HintFix {
        private final BaseDocument doc;
        private final FixInfo fixInfo;

        public AbstractClassFix(BaseDocument doc, FixInfo fixInfo) {
            this.doc = doc;
            this.fixInfo = fixInfo;
        }

        @Override
        @Messages("AbstractClassFixDesc=Declare Abstract Class")
        public String getDescription() {
            return Bundle.AbstractClassFixDesc();
        }

        @Override
        public void implement() throws Exception {
            EditList edits = new EditList(doc);
            edits.replace(fixInfo.classDeclarationOffset, 0, ABSTRACT_PREFIX, true, 0);
            edits.apply();
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

    }

    private static class FixInfo {
        private List<String> methodSkeletons;
        private String className;
        private int newMethodsOffset;
        private OffsetRange classNameRange;
        private final String lastMethodDeclaration;
        private final String lastMethodOwnerName;
        private final int classDeclarationOffset;

        FixInfo(ClassScope classScope, LinkedHashSet<String> methodSkeletons, MethodElement lastMethodElement, int newMethodsOffset, int classDeclarationOffset) {
            this.methodSkeletons = new ArrayList<String>(methodSkeletons);
            className = classScope.getFullyQualifiedName().toString();
            Collections.sort(this.methodSkeletons);
            this.classNameRange = classScope.getNameRange();
            this.classDeclarationOffset = classDeclarationOffset;
            this.newMethodsOffset = newMethodsOffset;
            lastMethodDeclaration = lastMethodElement.asString(PrintAs.NameAndParamsDeclaration);
            lastMethodOwnerName = lastMethodElement.getType().getFullyQualifiedName().toString();
        }
    }
}
