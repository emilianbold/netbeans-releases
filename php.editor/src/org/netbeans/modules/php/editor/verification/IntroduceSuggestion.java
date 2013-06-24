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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.verification;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.PHPCompletionItem;
import org.netbeans.modules.php.editor.PHPCompletionItem.MethodDeclarationItem;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.BaseFunctionElement.PrintAs;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.FieldElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.elements.MethodElementImpl;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.MethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.StaticConstantAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticFieldAccess;
import org.netbeans.modules.php.editor.parser.astnodes.StaticMethodInvocation;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.VariableBase;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

/**
 * @author Radek Matous
 */
public class IntroduceSuggestion extends SuggestionRule {

    private static final String UNKNOWN_FILE_NAME = "?"; //NOI18N

    @Override
    public String getId() {
        return "Introduce.Hint"; //NOI18N
    }

    @Override
    @Messages("IntroduceHintDesc=Introduce Hint")
    public String getDescription() {
        return Bundle.IntroduceHintDesc();
    }

    @Override
    @Messages("IntroduceHintDispName=Introduce Hint")
    public String getDisplayName() {
        return Bundle.IntroduceHintDispName();
    }

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileObject == null) {
            return;
        }
        int caretOffset = getCaretOffset();
        final BaseDocument doc = context.doc;
        OffsetRange lineBounds = VerificationUtils.createLineBounds(caretOffset, doc);
        if (lineBounds.containsInclusive(caretOffset)) {
            final Model model = phpParseResult.getModel();
            IntroduceFixVisitor introduceFixVisitor = new IntroduceFixVisitor(model, lineBounds);
            phpParseResult.getProgram().accept(introduceFixVisitor);
            IntroduceFix variableFix = introduceFixVisitor.getIntroduceFix();
            if (variableFix != null) {
                hints.add(new Hint(IntroduceSuggestion.this, getDisplayName(),
                        fileObject, variableFix.getOffsetRange(),
                        Collections.<HintFix>singletonList(variableFix), 500));
            }
        }
    }

    private static class IntroduceFixVisitor extends DefaultTreePathVisitor {

        private IntroduceFix fix;
        private Model model;
        private final OffsetRange lineBounds;

        IntroduceFixVisitor(Model model, OffsetRange lineBounds) {
            this.lineBounds = lineBounds;
            this.model = model;
        }

        @Override
        public void scan(ASTNode node) {
            if (node != null && (VerificationUtils.isBefore(node.getStartOffset(), lineBounds.getEnd()))) {
                super.scan(node);
            }
        }

        @Override
        public void visit(ClassInstanceCreation instanceCreation) {
            if (lineBounds.containsInclusive(instanceCreation.getStartOffset())) {
                String clzName = CodeUtils.extractClassName(instanceCreation.getClassName());
                clzName = (clzName != null && clzName.trim().length() > 0) ? clzName : null;
                ElementQuery.Index index = model.getIndexScope().getIndex();
                Set<ClassElement> classes = Collections.emptySet();
                if (StringUtils.hasText(clzName)) {
                    classes = index.getClasses(NameKind.exact(clzName));
                }
                if (clzName != null && classes.isEmpty()) {
                    ClassElement clz = getIndexedClass(clzName);
                    if (clz == null) {
                        fix = IntroduceClassFix.getInstance(clzName, model, instanceCreation);
                    }
                }
            }
            super.visit(instanceCreation);
        }

        @Override
        public void visit(MethodInvocation methodInvocation) {
            if (lineBounds.containsInclusive(methodInvocation.getStartOffset())) {
                String methName = CodeUtils.extractFunctionName(methodInvocation.getMethod());
                if (StringUtils.hasText(methName)) {
                    Collection<? extends TypeScope> allTypes = ModelUtils.resolveType(model, methodInvocation);
                    if (allTypes.size() == 1) {
                        TypeScope type = ModelUtils.getFirst(allTypes);
                        ElementQuery.Index index = model.getIndexScope().getIndex();
                        Set<MethodElement> allMethods = ElementFilter.forName(NameKind.exact(methName)).filter(index.getAllMethods(type));
                        if (allMethods.isEmpty()) {
                            FileObject fileObject = type.getFileObject();
                            BaseDocument document = fileObject != null ? GsfUtilities.getDocument(fileObject, true) : null;
                            if (document != null && fileObject.canWrite()) {
                                fix = new IntroduceMethodFix(document, methodInvocation, type);
                            }
                        }
                    }
                }
            }
            super.visit(methodInvocation);
        }

        @Override
        public void visit(StaticMethodInvocation methodInvocation) {
            if (lineBounds.containsInclusive(methodInvocation.getStartOffset())) {
                String methName = CodeUtils.extractFunctionName(methodInvocation.getMethod());
                String clzName = CodeUtils.extractUnqualifiedClassName(methodInvocation);

                if (clzName != null && StringUtils.hasText(methName)) {
                    Collection<? extends TypeScope> allTypes = ModelUtils.resolveType(model, methodInvocation);
                    if (allTypes.size() == 1) {
                        TypeScope type = ModelUtils.getFirst(allTypes);
                        ElementQuery.Index index = model.getIndexScope().getIndex();
                        final ElementFilter nameFilter = ElementFilter.forName(NameKind.exact(methName));
                        final ElementFilter staticFilter = ElementFilter.forStaticModifiers(true);
                        Set<MethodElement> allMethods = ElementFilter.allOf(nameFilter, staticFilter).filter(index.getAllMethods(type));
                        if (allMethods.isEmpty()) {
                            FileObject fileObject = type.getFileObject();
                            BaseDocument document = fileObject != null ? GsfUtilities.getDocument(fileObject, true) : null;
                            if (document != null && fileObject.canWrite()) {
                                fix = new IntroduceStaticMethodFix(document, methodInvocation, type);
                            }
                        }
                    }
                }
            }
            super.visit(methodInvocation);
        }

        @Override
        public void visit(FieldAccess fieldAccess) {
            if (lineBounds.containsInclusive(fieldAccess.getStartOffset())) {
                String fieldName = CodeUtils.extractVariableName(fieldAccess.getField());
                if (StringUtils.hasText(fieldName)) {
                    Collection<? extends TypeScope> allTypes = ModelUtils.resolveType(model, fieldAccess);
                    if (allTypes.size() == 1) {
                        TypeScope type = ModelUtils.getFirst(allTypes);
                        ElementQuery.Index index = model.getIndexScope().getIndex();
                        Set<FieldElement> allFields = ElementFilter.forName(NameKind.exact(fieldName)).filter(index.getAlllFields(type));
                        if (allFields.isEmpty()) {
                            FileObject fileObject = type.getFileObject();
                            BaseDocument document = fileObject != null ? GsfUtilities.getDocument(fileObject, false) : null;
                            if (document != null && fileObject.canWrite() && type instanceof ClassScope) {
                                fix = new IntroduceFieldFix(document, fieldAccess, (ClassScope) type);
                            }
                        }

                    }
                }
            }
            super.visit(fieldAccess);
        }

        @Override
        public void visit(StaticFieldAccess fieldAccess) {
            if (lineBounds.containsInclusive(fieldAccess.getStartOffset())) {
                final Variable field = fieldAccess.getField();
                String clzName = CodeUtils.extractUnqualifiedClassName(fieldAccess);
                if (clzName != null) {
                    String fieldName = CodeUtils.extractVariableName(field);
                    if (!StringUtils.hasText(fieldName)) {
                        return;
                    }
                    if (fieldName.startsWith("$")) { //NOI18N
                        fieldName = fieldName.substring(1);
                    }

                    Collection<? extends TypeScope> allTypes = ModelUtils.resolveType(model, fieldAccess);
                    if (allTypes.size() == 1) {
                        TypeScope type = ModelUtils.getFirst(allTypes);
                        ElementQuery.Index index = model.getIndexScope().getIndex();
                        ElementFilter staticFieldsFilter = ElementFilter.allOf(
                                ElementFilter.forName(NameKind.exact(fieldName)),
                                ElementFilter.forStaticModifiers(true));
                        Set<FieldElement> allFields = staticFieldsFilter.filter(index.getAlllFields(type));
                        if (allFields.isEmpty()) {
                            FileObject fileObject = type.getFileObject();
                            BaseDocument document = fileObject != null ? GsfUtilities.getDocument(fileObject, false) : null;
                            if (document != null && fileObject.canWrite() && type instanceof ClassScope) {
                                fix = new IntroduceStaticFieldFix(document, fieldAccess, (ClassScope) type);
                            }
                        }

                    }
                }
            }
            super.visit(fieldAccess);
        }

        @Override
        public void visit(StaticConstantAccess staticConstantAccess) {
            if (lineBounds.containsInclusive(staticConstantAccess.getStartOffset())) {
                String constName = staticConstantAccess.getConstant().getName();
                String clzName = CodeUtils.extractUnqualifiedClassName(staticConstantAccess);

                if (clzName != null && StringUtils.hasText(constName)) {
                    Collection<? extends TypeScope> allTypes = ModelUtils.resolveType(model, staticConstantAccess);
                    if (allTypes.size() == 1) {
                        TypeScope type = ModelUtils.getFirst(allTypes);
                        ElementQuery.Index index = model.getIndexScope().getIndex();
                        Set<TypeConstantElement> allConstants = ElementFilter.forName(NameKind.exact(constName)).filter(index.getAllTypeConstants(type));
                        if (allConstants.isEmpty()) {
                            FileObject fileObject = type.getFileObject();
                            BaseDocument document = fileObject != null ? GsfUtilities.getDocument(fileObject, false) : null;
                            if (document != null && fileObject.canWrite()) {
                                fix = new IntroduceClassConstantFix(document, staticConstantAccess, (TypeScope) type);
                            }
                        }
                    }
                }
            }

            super.visit(staticConstantAccess);
        }

        /**
         * @return or null
         */
        public IntroduceFix getIntroduceFix() {
            return fix;
        }

        private ClassElement getIndexedClass(String name) {
            ClassElement retval = null;
            ElementQuery.Index index = model.getIndexScope().getIndex();
            Collection<ClassElement> classes = Collections.emptyList();
            if ("self".equals(name) || "parent".equals(name)) { //NOI18N
                ClassDeclaration classDeclaration = null;
                for (ASTNode aSTNode : getPath()) {
                    if (aSTNode instanceof ClassDeclaration) {
                        classDeclaration = (ClassDeclaration) aSTNode;
                        break;
                    }
                }
                if (classDeclaration != null) {
                    String clzName = CodeUtils.extractClassName(classDeclaration);
                    classes = index.getClasses(NameKind.exact(clzName));
                }
            } else {
                classes = index.getClasses(NameKind.exact(name));
            }
            if (classes.size() == 1) {
                retval = classes.iterator().next();
                if ("parent".equals(name)) {
                    QualifiedName superClassQualifiedName = retval.getSuperClassName();
                    if (superClassQualifiedName != null) {
                        String superClassName = superClassQualifiedName.getName();
                        if (superClassName != null) {
                            classes = index.getClasses(NameKind.exact(superClassName));
                            retval = (classes.size() == 1) ? classes.iterator().next() : null;
                        }
                    }
                }
            }
            return retval;
        }
    }

    private static class IntroduceClassFix extends IntroduceFix {

        private String clsName;
        private FileObject folder;
        private FileObject template;

        static IntroduceClassFix getInstance(String className, Model model, ClassInstanceCreation instanceCreation) {
            FileObject currentFile = model.getFileScope().getFileObject();
            FileObject folder = currentFile == null ? null : currentFile.getParent();
            String templatePath = "Templates/Scripting/PHPClass.php"; //NOI18N
            FileObject template = FileUtil.getConfigFile(templatePath);
            return (template != null && folder != null && folder.canWrite())
                    ? new IntroduceClassFix(className, template, folder, instanceCreation) : null;
        }

        IntroduceClassFix(String className, FileObject template, FileObject folder, ClassInstanceCreation instanceCreation) {
            super(null, instanceCreation);
            this.clsName = className;
            this.template = template;
            this.folder = folder;
        }

        @Override
        public void implement() throws Exception {
            final DataFolder dataFolder = DataFolder.findFolder(folder);
            final DataObject configDataObject = DataObject.find(template);
            final FileObject[] clsFo = new FileObject[1];
            FileUtil.runAtomicAction(new Runnable() {

                @Override
                public void run() {
                    try {
                        DataObject clsDataObject = configDataObject.createFromTemplate(dataFolder, clsName);
                        clsFo[0] = clsDataObject.getPrimaryFile();
                        FileObject fo = clsFo[0];
                        FileLock lock = fo.lock();
                        try {
                            fo.rename(lock, fo.getName(), "php"); //NOI18N
                        } finally {
                            lock.releaseLock();
                        }
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            });
            if (clsFo[0] != null) {
                UiUtils.open(clsFo[0], 0);
            }
        }

        @Override
        @Messages({
            "# {0} - Class name",
            "# {1} - File name",
            "IntroduceHintClassDesc=Create Class \"{0}\" in {1}"
        })
        public String getDescription() {
            String fileName = FileUtil.getFileDisplayName(folder);
            int length = fileName.length();
            if (length > 30) {
                fileName = fileName.substring(length - 30);
                final int indexOf = fileName.indexOf("/");
                if (indexOf != -1) { //NOI18N
                    fileName = fileName.substring(indexOf);
                }
                fileName = String.format("...%s/%s.php", fileName, clsName); //NOI18N
            }
            return Bundle.IntroduceHintClassDesc(clsName, fileName);
        }
    }

    private static class IntroduceMethodFix extends IntroduceFix {

        private TypeScope type;
        private MethodDeclarationItem item;

        public IntroduceMethodFix(BaseDocument doc, MethodInvocation node, TypeScope type) {
            super(doc, node);
            this.type = type;
            this.item = createMethodDeclarationItem(type, node);
        }

        @Override
        public void implement() throws Exception {
            int templateOffset = getOffset();
            EditList edits = new EditList(doc);
            edits.replace(templateOffset, 0, "\n" + item.getCustomInsertTemplate(), true, 0); //NOI18N
            edits.apply();
            templateOffset = Utilities.getRowEnd(doc, templateOffset + 1);
            UiUtils.open(type.getFileObject(), Utilities.getRowEnd(doc, templateOffset + 1) - 1);
        }

        @Override
        @Messages({
            "# {0} - Method name",
            "# {1} - Class name",
            "# {2} - File name",
            "IntroduceHintMethodDesc=Create Method \"{0}\" in Class \"{1}\" ({2})"
        })
        public String getDescription() {
            String clsName = type.getName();
            FileObject fileObject = type.getFileObject();
            String fileName = fileObject == null ? UNKNOWN_FILE_NAME : fileObject.getNameExt();
            return Bundle.IntroduceHintMethodDesc(item.getMethod().asString(PrintAs.NameAndParamsDeclaration), clsName, fileName);

        }

        int getOffset() throws BadLocationException {
            return IntroduceSuggestion.getOffset(doc, type, PhpElementKind.METHOD);
        }
    }

    private static class IntroduceStaticMethodFix extends IntroduceFix {

        private TypeScope type;
        private MethodDeclarationItem item;

        public IntroduceStaticMethodFix(BaseDocument doc, StaticMethodInvocation node, TypeScope type) {
            super(doc, node);
            this.type = type;
            this.item = createMethodDeclarationItem(type, node);
        }

        @Override
        public void implement() throws Exception {
            int templateOffset = getOffset();
            EditList edits = new EditList(doc);
            edits.replace(templateOffset, 0, "\n" + item.getCustomInsertTemplate(), true, 0); //NOI18N
            edits.apply();
            templateOffset = Utilities.getRowEnd(doc, templateOffset + 1);
            UiUtils.open(type.getFileObject(), Utilities.getRowEnd(doc, templateOffset + 1) - 1);
        }

        @Override
        @Messages({
            "# {0} - Method name",
            "# {1} - Class name",
            "# {2} - File name",
            "IntroduceHintStaticMethodDesc=Create Method \"{0}\" in Class \"{1}\" ({2})"
        })
        public String getDescription() {
            String clsName = type.getName();
            FileObject fileObject = type.getFileObject();
            String fileName = fileObject == null ? UNKNOWN_FILE_NAME : fileObject.getNameExt();
            return Bundle.IntroduceHintStaticMethodDesc(item.getMethod().asString(PrintAs.NameAndParamsDeclaration), clsName, fileName);
        }

        int getOffset() throws BadLocationException {
            return IntroduceSuggestion.getOffset(doc, type, PhpElementKind.METHOD);
        }
    }

    private static class IntroduceFieldFix extends IntroduceFix {

        private ClassScope clz;
        private String templ;
        private String fieldName;
        private final VariableBase dispatcher;

        public IntroduceFieldFix(BaseDocument doc, FieldAccess node, ClassScope clz) {
            super(doc, node);
            this.clz = clz;
            this.dispatcher = node.getDispatcher();
            this.templ = createTemplate();
        }

        @Override
        public void implement() throws Exception {
            int templateOffset = getOffset();
            EditList edits = new EditList(doc);
            edits.replace(templateOffset, 0, "\n" + templ, true, 0); //NOI18N
            edits.apply();
            templateOffset = Utilities.getRowEnd(doc, templateOffset + 1) - 2;
            UiUtils.open(clz.getFileObject(), templateOffset);
        }

        @Override
        @Messages({
            "# {0} - Field name",
            "# {1} - Class name",
            "# {2} - File name",
            "IntroduceHintFieldDesc=Create Field \"{0}\" in Class \"{1}\" ({2})"
        })
        public String getDescription() {
            String clsName = clz.getName();
            FileObject fileObject = clz.getFileObject();
            String fileName = fileObject == null ? UNKNOWN_FILE_NAME : fileObject.getNameExt();
            return Bundle.IntroduceHintFieldDesc(templ, clsName, fileName);
        }

        int getOffset() throws BadLocationException {
            return IntroduceSuggestion.getOffset(doc, clz, PhpElementKind.FIELD);
        }

        private String createTemplate() {
            Variable fieldVar = ((FieldAccess) node).getField();
            this.fieldName = CodeUtils.extractVariableName(fieldVar);
            if (!fieldVar.isDollared()) {
                this.fieldName = "$" + this.fieldName; //NOI18N
            }
            return String.format("%s %s;", isInternal() ? "private" : "public", fieldName); //NOI18N
        }

        private boolean isInternal() {
            boolean result = false;
            if (dispatcher instanceof Variable) {
                Variable variable = (Variable) dispatcher;
                String dispatcherName = CodeUtils.extractVariableName(variable);
                result = "$this".equals(dispatcherName) ? true : false; //NOI18N
            }
            return result;
        }
    }

    private static class IntroduceStaticFieldFix extends IntroduceFix {

        private ClassScope clz;
        private String templ;
        private String fieldName;

        public IntroduceStaticFieldFix(BaseDocument doc, StaticFieldAccess node, ClassScope clz) {
            super(doc, node);
            this.clz = clz;
            this.templ = createTemplate();
        }

        @Override
        public void implement() throws Exception {
            int templateOffset = getOffset();
            EditList edits = new EditList(doc);
            edits.replace(templateOffset, 0, "\n" + templ, true, 0); //NOI18N
            edits.apply();
            templateOffset = Utilities.getRowEnd(doc, templateOffset + 1) - 2;
            UiUtils.open(clz.getFileObject(), templateOffset);
        }

        @Override
        @Messages({
            "# {0} - Field name",
            "# {1} - Class name",
            "# {2} - File name",
            "IntroduceHintStaticFieldDesc=Create Field \"{0}\" in Class \"{1}\" ({2})"
        })
        public String getDescription() {
            String clsName = clz.getName();
            FileObject fileObject = clz.getFileObject();
            String fileName = fileObject == null ? UNKNOWN_FILE_NAME : fileObject.getNameExt();
            return Bundle.IntroduceHintStaticFieldDesc(fieldName, clsName, fileName);

        }

        int getOffset() throws BadLocationException {
            return IntroduceSuggestion.getOffset(doc, clz, PhpElementKind.FIELD);
        }

        private String createTemplate() {
            Variable fieldVar = ((StaticFieldAccess) node).getField();
            fieldName = CodeUtils.extractVariableName(fieldVar);
            if (!fieldVar.isDollared()) {
                fieldName = "$" + fieldName; //NOI18N
            }
            return String.format("static %s = \"\";", fieldName);
        }
    }

    private static class IntroduceClassConstantFix extends IntroduceFix {

        private TypeScope type;
        private String templ;
        private String constantName;

        public IntroduceClassConstantFix(BaseDocument doc, StaticConstantAccess node, TypeScope type) {
            super(doc, node);
            this.type = type;
            this.constantName = ((StaticConstantAccess) node).getConstant().getName();
            this.templ = String.format("const %s = \"\";", constantName);
        }

        @Override
        public void implement() throws Exception {
            int templateOffset = getOffset();
            EditList edits = new EditList(doc);
            edits.replace(templateOffset, 0, "\n" + templ, true, 0); //NOI18N
            edits.apply();
            templateOffset = Utilities.getRowEnd(doc, templateOffset + 1) - 2;
            UiUtils.open(type.getFileObject(), templateOffset);
        }

        @Override
        @Messages({
            "# {0} - Constant name",
            "# {1} - Class name",
            "# {2} - File name",
            "IntroduceHintClassConstDesc=Create Constant \"{0}\" in Class \"{1}\" ({2})"
        })
        public String getDescription() {
            String clsName = type.getName();
            FileObject fileObject = type.getFileObject();
            String fileName = fileObject == null ? UNKNOWN_FILE_NAME : fileObject.getNameExt();
            return Bundle.IntroduceHintClassConstDesc(constantName, clsName, fileName);
        }

        int getOffset() throws BadLocationException {
            return IntroduceSuggestion.getOffset(doc, type, PhpElementKind.TYPE_CONSTANT);
        }
    }

    abstract static class IntroduceFix implements HintFix {

        BaseDocument doc;
        ASTNode node;

        public IntroduceFix(BaseDocument doc, ASTNode node) {
            this.doc = doc;
            this.node = node;
        }

        OffsetRange getOffsetRange() {
            return new OffsetRange(node.getStartOffset(), node.getEndOffset());
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

        @Override
        public boolean isSafe() {
            return true;
        }
    }

    private static String getParameters(final List<Expression> parameters) {
        StringBuilder paramNames = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            Expression expression = parameters.get(i);
            String varName = null;
            if (expression instanceof Variable) {
                varName = CodeUtils.extractVariableName((Variable) expression);
            }
            if (varName == null) {
                varName = String.format("$param%d", i); //NOI18N
            }
            if (i > 0) {
                paramNames.append(", ");
            }
            paramNames.append(varName);
        }
        return paramNames.toString();
    }

    private static int getOffset(BaseDocument doc, TypeScope typeScope, PhpElementKind kind) throws BadLocationException {
        int offset = -1;
        Collection<ModelElement> elements = new HashSet<>();
        elements.addAll(typeScope.getDeclaredConstants());
        switch (kind) {
            case METHOD:
                if (typeScope instanceof ClassScope) {
                    ClassScope clz = (ClassScope) typeScope;
                    elements.addAll(clz.getDeclaredFields());
                    elements.addAll(clz.getDeclaredMethods());
                }
                break;
            case FIELD:
                if ((typeScope instanceof ClassScope)) {
                    ClassScope clz = (ClassScope) typeScope;
                    elements.addAll(clz.getDeclaredFields());
                }
                break;
            default:
                assert false;
        }
        int newOffset;
        for (ModelElement elem : elements) {
            newOffset = elem.getOffset();
            if (elem instanceof MethodScope) {
                newOffset = getOffsetAfterBlockCloseCurly(doc, newOffset);
            } else {
                newOffset = getOffsetAfterNextSemicolon(doc, newOffset);
            }
            if (newOffset > offset) {
                offset = newOffset;
            }
        }
        if (offset == -1) {
            offset = getOffsetAfterClassOpenCurly(doc, typeScope.getOffset());
        }
        return offset;
    }

    private static int getOffsetAfterBlockCloseCurly(BaseDocument doc, int offset) throws BadLocationException {
        int retval = offset;
        doc.readLock();
        try {
            TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, retval);
            if (ts != null) {
                ts.move(retval);
                int curlyMatch = 0;
                while (ts.moveNext()) {
                    Token t = ts.token();
                    if (t.id() == PHPTokenId.PHP_CURLY_OPEN || t.id() == PHPTokenId.PHP_CURLY_CLOSE) {
                        if (t.id() == PHPTokenId.PHP_CURLY_OPEN) {
                            curlyMatch++;
                        } else if (t.id() == PHPTokenId.PHP_CURLY_CLOSE) {
                            curlyMatch--;
                        }
                        if (curlyMatch == 0) {
                            ts.moveNext();
                            retval = ts.offset();
                            break;
                        }
                    } else {
                        continue;
                    }
                }
            }
        } finally {
            doc.readUnlock();
        }
        return retval;
    }

    private static int getOffsetAfterNextSemicolon(BaseDocument doc, int offset) throws BadLocationException {
        return getOffsetAfterNextTokenId(doc, offset, PHPTokenId.PHP_SEMICOLON);
    }

    private static int getOffsetAfterClassOpenCurly(BaseDocument doc, int offset) throws BadLocationException {
        return getOffsetAfterNextTokenId(doc, offset, PHPTokenId.PHP_CURLY_OPEN);
    }

    private static int getOffsetAfterNextTokenId(BaseDocument doc, int offset, PHPTokenId tokenId) throws BadLocationException {
        int retval = offset;
        doc.readLock();
        try {
            TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, retval);
            if (ts != null) {
                ts.move(retval);
                while (ts.moveNext()) {
                    Token t = ts.token();
                    if (t.id() == tokenId) {
                        ts.moveNext();
                        retval = ts.offset();
                        break;
                    }
                }
            }
        } finally {
            doc.readUnlock();
        }
        return retval;
    }

    private static PHPCompletionItem.MethodDeclarationItem createMethodDeclarationItem(final TypeScope typeScope, final MethodInvocation node) {
        final String methodName = CodeUtils.extractFunctionName(node.getMethod());
        final MethodElement method = MethodElementImpl.createMagicMethod(typeScope,
                methodName, 0, getParameters(node.getMethod().getParameters()));
        return typeScope.isInterface()
                ? PHPCompletionItem.MethodDeclarationItem.forIntroduceInterfaceHint(method, null)
                : PHPCompletionItem.MethodDeclarationItem.forIntroduceHint(method, null);
    }

    private static PHPCompletionItem.MethodDeclarationItem createMethodDeclarationItem(final TypeScope typeScope, final StaticMethodInvocation node) {
        final String methodName = CodeUtils.extractFunctionName(node.getMethod());
        final MethodElement method = MethodElementImpl.createMagicMethod(typeScope, methodName,
                Modifier.STATIC, getParameters(node.getMethod().getParameters()));
        return PHPCompletionItem.MethodDeclarationItem.forIntroduceHint(method, null);
    }
}
