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
package org.netbeans.modules.java.hints.suggestions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.swing.text.JTextComponent;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;

import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.api.java.source.support.CaretAwareJavaSourceTaskFactory;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.java.editor.codegen.ConstructorGenerator;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingController;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.Hint.Kind;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Dusan Balek
 */
@Hint(displayName = "#DN_org.netbeans.modules.java.hints.suggestions.CreateSubclass", description = "#DESC_org.netbeans.modules.java.hints.suggestions.CreateSubclass", category = "suggestions", hintKind = Kind.SUGGESTION, severity = Severity.HINT)
public class CreateSubclass {

    @TriggerTreeKind({Tree.Kind.CLASS, Tree.Kind.INTERFACE})
    public static ErrorDescription check(HintContext context) {
        TreePath tp = context.getPath();
        ClassTree cls = (ClassTree) tp.getLeaf();
        CompilationInfo info = context.getInfo();
        SourcePositions sourcePositions = info.getTrees().getSourcePositions();
        int startPos = (int) sourcePositions.getStartPosition(tp.getCompilationUnit(), cls);
        int caret = CaretAwareJavaSourceTaskFactory.getLastPosition(context.getInfo().getFileObject());
        String code = context.getInfo().getText();
        if (startPos < 0 || caret < 0 || caret < startPos || caret >= code.length()) {
            return null;
        }

        String headerText = code.substring(startPos, caret);
        int idx = headerText.indexOf('{'); //NOI18N
        if (idx >= 0) {
            return null;
        }

        TypeElement typeElement = (TypeElement) info.getTrees().getElement(tp);

        if (typeElement.getModifiers().contains(Modifier.FINAL)) return null;
        
        ClassPath cp = info.getClasspathInfo().getClassPath(PathKind.SOURCE);
        FileObject root = cp.findOwnerRoot(info.getFileObject());
        if (root == null) { //File not part of any project
            return null;
        }

        PackageElement packageElement = (PackageElement) info.getElementUtilities().outermostTypeElement(typeElement).getEnclosingElement();
        CreateSubclassFix fix = new CreateSubclassFix(info, root, packageElement.getQualifiedName().toString(), typeElement.getSimpleName().toString() + "Impl", typeElement); //NOI18N
        return ErrorDescriptionFactory.forTree(context, context.getPath(), NbBundle.getMessage(CreateSubclass.class, typeElement.getKind() == ElementKind.CLASS
                ? typeElement.getModifiers().contains(Modifier.ABSTRACT) ? "ERR_ImplementAbstractClass" : "ERR_CreateSubclass" : "ERR_ImplementInterface"), fix); //NOI18N
    }

    private static final class CreateSubclassFix implements Fix, PropertyChangeListener {

        private FileObject targetSourceRoot;
        private String packageName;
        private String simpleName;
        private ElementHandle<TypeElement> superType;
        private boolean isAbstract;
        private boolean hasNonDefaultConstructor = false;
        private FileObject target = null;

        public CreateSubclassFix(CompilationInfo info, FileObject targetSourceRoot, String packageName, String simpleName, TypeElement typeElement) {
            this.targetSourceRoot = targetSourceRoot;
            this.packageName = packageName;
            this.simpleName = simpleName;
            this.isAbstract = typeElement.getModifiers().contains(Modifier.ABSTRACT);
            this.superType = ElementHandle.create(typeElement);
        }

        @Override
        public String getText() {
            return NbBundle.getMessage(CreateSubclass.class, superType.getKind() == ElementKind.CLASS ? isAbstract ? "FIX_ImplementAbstractClass" : "FIX_CreateSubclass" : "FIX_ImplementInterface"); //NOI18N
        }

        @Override
        public ChangeInfo implement() throws Exception {
            IndexingController.getDefault().enterProtectedMode();
            try {
                final NameAndPackagePanel panel = new NameAndPackagePanel(simpleName, packageName);
                final DialogDescriptor desc = new DialogDescriptor(panel, getText());
                panel.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (NameAndPackagePanel.IS_VALID.equals(evt.getPropertyName())) {
                            desc.setValid(panel.isValidData());
                        }
                    }
                });
                desc.setValid(panel.isValidData());
                if (DialogDisplayer.getDefault().notify(desc) != DialogDescriptor.OK_OPTION) {
                    return null;
                }
                simpleName = panel.getClassName();
                packageName = panel.getPackageName();

                EditorRegistry.addPropertyChangeListener(this);

                FileObject pack = FileUtil.createFolder(targetSourceRoot, packageName.replace('.', '/')); // NOI18N
                FileObject classTemplate = FileUtil.getConfigFile("Templates/Classes/Class.java"); // NOI18N

                if (classTemplate != null) {
                    DataObject classTemplateDO = DataObject.find(classTemplate);
                    DataObject od = classTemplateDO.createFromTemplate(DataFolder.findFolder(pack), simpleName);

                    target = od.getPrimaryFile();
                } else {
                    target = FileUtil.createData(pack, simpleName + ".java"); //NOI18N
                }

                final boolean fromTemplate = classTemplate != null;
                final JavaSource js = JavaSource.forFileObject(target);
                js.runModificationTask(new Task<WorkingCopy>() {

                    @Override
                    public void run(WorkingCopy parameter) throws Exception {
                        parameter.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

                        TreeMaker make = parameter.getTreeMaker();
                        CompilationUnitTree cut = parameter.getCompilationUnit();
                        TypeElement superTypeElement = superType.resolve(parameter);
                        ExpressionTree pack = fromTemplate ? cut.getPackageName() : make.Identifier(packageName);
                        ClassTree source = fromTemplate
                                ? (ClassTree) cut.getTypeDecls().get(0)
                                : make.Class(make.Modifiers(EnumSet.of(Modifier.PUBLIC)),
                                simpleName,
                                Collections.<TypeParameterTree>emptyList(),
                                null,
                                Collections.<Tree>emptyList(),
                                Collections.<Tree>emptyList());
                        if (superTypeElement != null) {
                            if (superTypeElement.getKind() == ElementKind.CLASS) {
                                source = make.Class(source.getModifiers(), source.getSimpleName(), source.getTypeParameters(), make.Type(superTypeElement.asType()), source.getImplementsClause(), source.getMembers());
                                for (ExecutableElement ctor : ElementFilter.constructorsIn(superTypeElement.getEnclosedElements())) {
                                    if (!ctor.getParameters().isEmpty()) {
                                        hasNonDefaultConstructor = true;
                                        break;
                                    }
                                }
                            } else {
                                source = make.Class(source.getModifiers(), source.getSimpleName(), source.getTypeParameters(), source.getExtendsClause(), Collections.singletonList(make.Type(superTypeElement.asType())), source.getMembers());
                            }
                        }
                        parameter.rewrite(cut, make.CompilationUnit(pack, cut.getImports(), Collections.singletonList(source), cut.getSourceFile()));
                    }
                }).commit();

                if (!hasNonDefaultConstructor && !isAbstract) {
                    EditorRegistry.removePropertyChangeListener(CreateSubclassFix.this);
                }
                return new ChangeInfo(target, null, null);
            } finally {
                IndexingController.getDefault().exitProtectedMode(null);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final JTextComponent component = EditorRegistry.focusedComponent();
            FileObject fo = component != null ? NbEditorUtilities.getFileObject(component.getDocument()) : null;
            if (target == null || target != fo) {
                return;
            }
            EditorRegistry.removePropertyChangeListener(this);
            RequestProcessor.getDefault().post(new Runnable() {

                @Override
                public void run() {
                    try {
                        JavaSource js = JavaSource.forDocument(component.getDocument());
                        js.runModificationTask(new Task<WorkingCopy>() {

                            @Override
                            public void run(WorkingCopy parameter) throws Exception {
                                parameter.toPhase(JavaSource.Phase.RESOLVED);
                                CompilationUnitTree cut = parameter.getCompilationUnit();
                                TreePath path = TreePath.getPath(cut, cut.getTypeDecls().get(0));
                                if (isAbstract) {
                                    GeneratorUtils.generateAllAbstractMethodImplementations(parameter, path);
                                }
                                if (hasNonDefaultConstructor) {
                                    ConstructorGenerator.Factory factory = new ConstructorGenerator.Factory();
                                    Iterator<? extends CodeGenerator> generators = factory.create(Lookups.fixed(component, parameter, path)).iterator();
                                    if (generators.hasNext()) {
                                        generators.next().invoke();
                                    }
                                }
                            }
                        }).commit();
                    } catch (IOException ioe) {
                    }
                }
            });
        }
    }
}
