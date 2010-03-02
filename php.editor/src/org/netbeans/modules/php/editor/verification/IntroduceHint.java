/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.verification;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.PHPCompletionItem;
import org.netbeans.modules.php.editor.PredefinedSymbols;
import org.netbeans.modules.php.editor.index.IndexedClass;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.PhpKind;
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
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultTreePathVisitor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * @author Radek Matous
 */
public class IntroduceHint extends AbstractRule {

    public String getId() {
        return "Introduce.Hint";//NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(IntroduceHint.class, "IntroduceHintDesc");//NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(IntroduceHint.class, "IntroduceHintDispName");//NOI18N
    }

    void computeHintsImpl(PHPRuleContext context, List<Hint> hints, PHPHintsProvider.Kind kind) throws BadLocationException {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        final BaseDocument doc = context.doc;
        final int caretOffset = context.caretOffset;
        int lineBegin = -1;
        int lineEnd = -1;
        lineBegin = caretOffset > 0 ? Utilities.getRowStart(doc, caretOffset) : -1;
        lineEnd = (lineBegin != -1) ? Utilities.getRowEnd(doc, caretOffset) : -1;
        if (lineBegin != -1 && lineEnd != -1 && caretOffset > lineBegin) {
            final Model model = phpParseResult.getModel();
            IntroduceFixVisitor introduceFixVisitor = new IntroduceFixVisitor(model,lineBegin, lineEnd);
            phpParseResult.getProgram().accept(introduceFixVisitor);
            IntroduceFix variableFix = introduceFixVisitor.getIntroduceFix();
            if (variableFix != null) {
                hints.add(new Hint(IntroduceHint.this, getDisplayName(),
                        context.parserResult.getSnapshot().getSource().getFileObject(), variableFix.getOffsetRange(),
                        Collections.<HintFix>singletonList(variableFix), 500));
            }
        }
    }

    private class IntroduceFixVisitor extends DefaultTreePathVisitor {

        private int lineBegin;
        private int lineEnd;
        private IntroduceFix fix;
        private Model model;

        IntroduceFixVisitor(Model model, int lineBegin, int lineEnd) {
            this.lineBegin = lineBegin;
            this.lineEnd = lineEnd;
            this.model = model;
        }

        @Override
        public void scan(ASTNode node) {
            if (node != null && (isBefore(node.getStartOffset(), lineEnd))) {
                super.scan(node);
            }
        }

        @Override
        public void visit(ClassInstanceCreation instanceCreation) {
            if (isInside(instanceCreation.getStartOffset(), lineBegin, lineEnd)) {
                String clzName = CodeUtils.extractClassName(instanceCreation.getClassName());
                clzName = (clzName != null && clzName.trim().length() > 0) ? clzName : null;
                PHPIndex index = model.getIndexScope().getIndex();
                Collection<IndexedClass> classes = Collections.emptyList();
                if (clzName != null) {
                    classes = index.getClasses(null, clzName, Kind.EXACT);
                }
                if (clzName != null && classes.isEmpty()) {
                    IndexedClass clz = clzName != null ? getIndexedClass(clzName) : null;
                    if (clz == null && clzName != null) {
                        fix = IntroduceClassFix.getInstance(clzName, model, instanceCreation);
                    }
                }
            }
            super.visit(instanceCreation);
        }

        @Override
        public void visit(MethodInvocation methodInvocation) {
            if (isInside(methodInvocation.getStartOffset(), lineBegin, lineEnd)) {
                String methName = CodeUtils.extractFunctionName(methodInvocation.getMethod());
                if (methName != null) {
                    Collection<? extends TypeScope> allTypes = ModelUtils.resolveType(model, methodInvocation);
                    if (allTypes.size() == 1) {
                        TypeScope type = ModelUtils.getFirst(allTypes);
                        PHPIndex index = model.getIndexScope().getIndex();
                        Collection<IndexedFunction> allMethods = PHPIndex.toMembers(index.getAllMethods(null, type.getName(),
                                methName, Kind.EXACT, PHPIndex.ANY_ATTR));
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
            if (isInside(methodInvocation.getStartOffset(), lineBegin, lineEnd)) {
                String methName = CodeUtils.extractFunctionName(methodInvocation.getMethod());
                String clzName = CodeUtils.extractUnqualifiedClassName(methodInvocation);

                if (clzName != null) {
                    Collection<? extends TypeScope> allTypes = ModelUtils.resolveType(model, methodInvocation);
                    if (allTypes.size() == 1) {
                        TypeScope type = ModelUtils.getFirst(allTypes);
                        PHPIndex index = model.getIndexScope().getIndex();
                        Collection<IndexedFunction> allMethods = PHPIndex.toMembers(index.getAllMethods(null, type.getName(),
                                methName, Kind.EXACT, PHPIndex.ANY_ATTR));
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
            if (isInside(fieldAccess.getStartOffset(), lineBegin, lineEnd)) {
                String fieldName = CodeUtils.extractVariableName(fieldAccess.getField());
                if (fieldName != null) {
                    Collection<? extends TypeScope> allTypes = ModelUtils.resolveType(model, fieldAccess);
                    if (allTypes.size() == 1) {
                        TypeScope type = ModelUtils.getFirst(allTypes);
                        PHPIndex index = model.getIndexScope().getIndex();
                        Collection<IndexedConstant> allFields = PHPIndex.toMembers(index.getAllFields(null, type.getName(), fieldName, Kind.EXACT, PHPIndex.ANY_ATTR));
                        if (allFields.isEmpty()) {
                            FileObject fileObject = type.getFileObject();
                            BaseDocument document = fileObject != null ? GsfUtilities.getDocument(fileObject, false) : null;
                            if (document != null && fileObject.canWrite() && type instanceof ClassScope ) {
                                fix = new IntroduceFieldFix(document, fieldAccess, (ClassScope)type);
                            }
                        }

                    }
                }
            }
            super.visit(fieldAccess);
        }

        @Override
        public void visit(StaticFieldAccess fieldAccess) {
            if (isInside(fieldAccess.getStartOffset(), lineBegin, lineEnd)) {
                final Variable field = fieldAccess.getField();
                String clzName = CodeUtils.extractUnqualifiedClassName(fieldAccess);
                if (clzName != null) {
                    String fieldName = CodeUtils.extractVariableName(field);
                    if (fieldName == null) {
                        return;
                    }
                    if (fieldName.startsWith("$")) {//NOI18N
                        fieldName = fieldName.substring(1);
                    }

                    Collection<? extends TypeScope> allTypes = ModelUtils.resolveType(model, fieldAccess);
                    if (allTypes.size() == 1) {
                        TypeScope type = ModelUtils.getFirst(allTypes);
                        PHPIndex index = model.getIndexScope().getIndex();
                        Collection<IndexedConstant> allFields = PHPIndex.toMembers(index.getAllFields(null, type.getName(), fieldName, Kind.EXACT, PHPIndex.ANY_ATTR));
                        if (allFields.isEmpty()) {
                            FileObject fileObject = type.getFileObject();
                            BaseDocument document = fileObject != null ? GsfUtilities.getDocument(fileObject, false) : null;
                            if (document != null && fileObject.canWrite() && type instanceof ClassScope ) {
                                fix = new IntroduceStaticFieldFix(document, fieldAccess, (ClassScope)type);
                            }
                        }

                    }
                }
            }
            super.visit(fieldAccess);
        }

        @Override
        public void visit(StaticConstantAccess staticConstantAccess) {
            if (isInside(staticConstantAccess.getStartOffset(), lineBegin, lineEnd)) {
                String constName = staticConstantAccess.getConstant().getName();
                String clzName = CodeUtils.extractUnqualifiedClassName(staticConstantAccess);

                if (clzName != null) {
                    if (constName != null) {
                        Collection<? extends TypeScope> allTypes = ModelUtils.resolveType(model, staticConstantAccess);
                        if (allTypes.size() == 1) {
                            TypeScope type = ModelUtils.getFirst(allTypes);
                            PHPIndex index = model.getIndexScope().getIndex();
                            Collection<IndexedConstant> allConstants = PHPIndex.toMembers(index.getAllTypeConstants(null, type.getName(), constName, Kind.EXACT));
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
            }

            super.visit(staticConstantAccess);
        }

        /**
         * @return or null
         */
        public IntroduceFix getIntroduceFix() {
            return fix;
        }

        private IndexedClass getIndexedClass(String name) {
            IndexedClass retval = null;
            PHPIndex index = model.getIndexScope().getIndex();
            Collection<IndexedClass> classes = Collections.emptyList();
            if ("self".equals(name) || "parent".equals(name)) {
                //NOI18N
                List<ASTNode> path = getPath();
                for (ASTNode aSTNode : path) {
                    if (aSTNode instanceof ClassDeclaration) {
                        classes = index.getClasses(null, CodeUtils.extractClassName((ClassDeclaration) aSTNode), Kind.EXACT);
                        break;
                    }
                }
            } else {
                classes = index.getClasses(null, name, Kind.EXACT);
            }
            if (classes.size() == 1) {
                retval = classes.iterator().next();
                if ("parent".equals(name)) {
                    String superClassName = retval.getSuperClass();

                    if (superClassName != null){
                        classes = index.getClasses(null, superClassName, Kind.EXACT);
                        retval = (classes.size() == 1) ? classes.iterator().next() : null;
                    }
                }
            }
            return retval;
        }
    }

    private static class IntroduceClassFix extends IntroduceFix {

        private Model model;
        private String clsName;
        private FileObject folder;
        private FileObject template;

        static IntroduceClassFix getInstance(String className, Model model, ClassInstanceCreation instanceCreation) {
            FileObject currentFile = model.getFileScope().getFileObject();
            FileObject folder = currentFile.getParent();
            String templatePath = "Templates/Scripting/PHPClass";//NOI18N
            FileObject template = FileUtil.getConfigFile(templatePath);
            return (template != null && folder != null && folder.canWrite()) ?
                new IntroduceClassFix(className, template, folder, model, instanceCreation) : null;
        }

        IntroduceClassFix(String className, FileObject template, FileObject folder,
                Model model, ClassInstanceCreation instanceCreation) {
            super(null, instanceCreation);
            this.model = model;
            this.clsName = className;
            this.template = template;
            this.folder = folder;
        }

        public void implement() throws Exception {
            final DataFolder dataFolder = DataFolder.findFolder(folder);
            final DataObject configDataObject = DataObject.find(template);
            final FileObject[] clsFo = new FileObject[1];
            FileUtil.runAtomicAction(new Runnable() {

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
        public String getDescription() {
            String fileName = FileUtil.getFileDisplayName(folder);
            int length = fileName.length();
            if (length > 30) {
                fileName = fileName.substring(length - 30);
                final int indexOf = fileName.indexOf("/");
                if (indexOf != -1) {//NOI18N
                    fileName = fileName.substring(indexOf);
                }
                fileName = String.format("...%s/%s.php", fileName, clsName);//NOI18N
            }
            return NbBundle.getMessage(IntroduceHint.class, "IntroduceHintClassDesc",
                    clsName, fileName);//NOI18N
        }
    }

    private static class IntroduceMethodFix extends IntroduceFix {

        private TypeScope type;
        private MethodDeclarationItem item;

        public IntroduceMethodFix(BaseDocument doc, MethodInvocation node, TypeScope type) {
            super(doc, node);
            this.type = type;
            this.item = createMethodDeclarationItem(node);
        }

        public void implement() throws Exception {
            int templateOffset = getOffset();
            EditList edits = new EditList(doc);
            edits.replace(templateOffset, 0, "\n" + item.getCustomInsertTemplate(), true, 0);//NOI18N
            edits.apply();
            templateOffset = Utilities.getRowEnd(doc, templateOffset + 1);
            UiUtils.open(type.getFileObject(), Utilities.getRowEnd(doc, templateOffset + 1) - 1);
        }

        @Override
        public String getDescription() {
            String clsName = type.getName();
            String fileName = type.getFileObject().getNameExt();
            return NbBundle.getMessage(IntroduceHint.class, "IntroduceHintMethodDesc",
                    item.getFunction().getFunctionSignature(true), clsName, fileName);//NOI18N

        }

        static MethodDeclarationItem createMethodDeclarationItem(MethodInvocation node) {
            return new MethodDeclarationItem((MethodInvocation) node, 0);
        }

        int getOffset() throws BadLocationException {
            return IntroduceHint.getOffset(doc, type, PhpKind.METHOD);
        }
    }

    private static class IntroduceStaticMethodFix extends IntroduceFix {

        private TypeScope type;
        private MethodDeclarationItem item;

        public IntroduceStaticMethodFix(BaseDocument doc, StaticMethodInvocation node, TypeScope type) {
            super(doc, node);
            this.type = type;
            this.item = createMethodDeclarationItem(node);
        }

        public void implement() throws Exception {
            int templateOffset = getOffset();
            EditList edits = new EditList(doc);
            edits.replace(templateOffset, 0, "\n" + item.getCustomInsertTemplate(), true, 0);//NOI18N
            edits.apply();
            templateOffset = Utilities.getRowEnd(doc, templateOffset + 1);
            UiUtils.open(type.getFileObject(), Utilities.getRowEnd(doc, templateOffset + 1) - 1);
        }

        @Override
        public String getDescription() {
            String clsName = type.getName();
            String fileName = type.getFileObject().getNameExt();
            return NbBundle.getMessage(IntroduceHint.class, "IntroduceHintStaticMethodDesc",
                    item.getFunction().getFunctionSignature(true), clsName, fileName);//NOI18N
        }

        static MethodDeclarationItem createMethodDeclarationItem(StaticMethodInvocation node) {
            return new MethodDeclarationItem(node, Modifier.STATIC);
        }

        int getOffset() throws BadLocationException {
            return IntroduceHint.getOffset(doc, type, PhpKind.METHOD);
        }
    }

    private static class IntroduceFieldFix extends IntroduceFix {

        private ClassScope clz;
        private String templ;
        private String fieldName;

        public IntroduceFieldFix(BaseDocument doc, FieldAccess node, ClassScope clz) {
            super(doc, node);
            this.clz = clz;
            this.templ = createTemplate();//NOI18N
        }

        public void implement() throws Exception {
            int templateOffset = getOffset();
            EditList edits = new EditList(doc);
            edits.replace(templateOffset, 0, "\n" + templ, true, 0);//NOI18N
            edits.apply();
            templateOffset = Utilities.getRowEnd(doc, templateOffset + 1) - 2;
            UiUtils.open(clz.getFileObject(), templateOffset);
        }

        @Override
        public String getDescription() {
            String clsName = clz.getName();
            String fileName = clz.getFileObject().getNameExt();
            return NbBundle.getMessage(IntroduceHint.class, "IntroduceHintFieldDesc",
                    templ, clsName, fileName);//NOI18N
        }

        int getOffset() throws BadLocationException {
            return IntroduceHint.getOffset(doc, clz, PhpKind.FIELD);
        }

        private String createTemplate() {
            Variable fieldVar = ((FieldAccess) node).getField();
            this.fieldName = CodeUtils.extractVariableName(fieldVar);
            if (!fieldVar.isDollared()) {
                this.fieldName = "$" + this.fieldName;//NOI18N
            }
            return String.format("public %s = \"\";", fieldName);
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

        public void implement() throws Exception {
            int templateOffset = getOffset();
            EditList edits = new EditList(doc);
            edits.replace(templateOffset, 0, "\n" + templ, true, 0);//NOI18N
            edits.apply();
            templateOffset = Utilities.getRowEnd(doc, templateOffset + 1) - 2;
            UiUtils.open(clz.getFileObject(), templateOffset);
        }

        @Override
        public String getDescription() {
            String clsName = clz.getName();
            String fileName = clz.getFileObject().getNameExt();
            return NbBundle.getMessage(IntroduceHint.class, "IntroduceHintStaticFieldDesc",
                    fieldName, clsName, fileName);//NOI18N

        }

        int getOffset() throws BadLocationException {
            return IntroduceHint.getOffset(doc, clz, PhpKind.FIELD);
        }

        private String createTemplate() {
            Variable fieldVar = ((StaticFieldAccess) node).getField();
            fieldName = CodeUtils.extractVariableName(fieldVar);
            if (!fieldVar.isDollared()) {
                fieldName = "$" + fieldName;//NOI18N
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

        public void implement() throws Exception {
            int templateOffset = getOffset();
            EditList edits = new EditList(doc);
            edits.replace(templateOffset, 0, "\n" + templ, true, 0);//NOI18N
            edits.apply();
            templateOffset = Utilities.getRowEnd(doc, templateOffset + 1) - 2;
            UiUtils.open(type.getFileObject(), templateOffset);
        }

        @Override
        public String getDescription() {
            String clsName = type.getName();
            String fileName = type.getFileObject().getNameExt();
            return NbBundle.getMessage(IntroduceHint.class, "IntroduceHintClassConstDesc",
                    constantName, clsName, fileName);//NOI18N
        }

        int getOffset() throws BadLocationException {
            return IntroduceHint.getOffset(doc, type, PhpKind.CLASS_CONSTANT);
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

        public boolean isInteractive() {
            return false;
        }

        public boolean isSafe() {
            return true;
        }
    }

    private static boolean isInside(int carret, int left, int right) {
        return carret >= left && carret <= right;
    }

    private static boolean isBefore(int carret, int margin) {
        return carret <= margin;
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
                varName = String.format("$param%d", i);//NOI18N
            }
            if (i > 0) {
                paramNames.append(", ");
            }
            paramNames.append(varName);
        }
        return paramNames.toString();
    }

    private static class MethodDeclarationItem extends PHPCompletionItem.FunctionDeclarationItem {

        MethodDeclarationItem(MethodInvocation methodInvocation, int flags) {
            super(PredefinedSymbols.createMagicFunction(CodeUtils.extractFunctionName(methodInvocation.getMethod()),
                    getParameters(methodInvocation.getMethod().getParameters()), 0), null, flags, false);
        }

        MethodDeclarationItem(StaticMethodInvocation methodInvocation, int flags) {
            super(PredefinedSymbols.createMagicFunction(CodeUtils.extractFunctionName(methodInvocation.getMethod()),
                    getParameters(methodInvocation.getMethod().getParameters()), flags), null, 0, false);
        }

        @Override
        protected String getFunctionBodyForTemplate() {
            return "\n";//NOI18N
        }
    }

    private static int  getOffset(BaseDocument doc, TypeScope typeScope, PhpKind kind) throws BadLocationException {
        int offset = -1;
        Collection<ModelElement> elements = new HashSet<ModelElement>();
        switch (kind) {
            case CLASS_CONSTANT:
                elements.addAll(typeScope.getDeclaredConstants());
                break;
            case METHOD:
            case FIELD:
                elements.addAll(typeScope.getDeclaredConstants());
                if ((typeScope instanceof ClassScope)) {
                    ClassScope clz = (ClassScope) typeScope;
                    elements.addAll(clz.getDeclaredFields());
                }
                break;
        }
        for (ModelElement elem : elements) {
            if (elem.getOffset() > offset) {
                offset = elem.getOffset();
            }
        }
        if (offset != -1) {
            offset = Utilities.getRowEnd(doc, offset);
        } else {
            offset = getClassCurlyOpenOffset(doc, typeScope.getOffset());
        }
        return offset;
    }

    private static int getClassCurlyOpenOffset(BaseDocument doc, int offset) throws BadLocationException {
        int retval = offset;
        TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, retval);
        if (ts != null) {
            ts.move(retval);
            while (ts.moveNext()) {
                Token t = ts.token();
                if (t.id() == PHPTokenId.PHP_CURLY_OPEN) {
                    retval = ts.offset();
                    break;
                }
            }
        }
        return Utilities.getRowEnd(doc, retval);
    }
}
