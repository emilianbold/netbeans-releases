/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.editor.codegen;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.api.java.source.CodeStyleUtils;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.editor.GuardedDocument;
import org.netbeans.editor.GuardedException;
import org.netbeans.editor.Utilities;
import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public class GeneratorUtils {
    
    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(GeneratorUtils.class.getName());
    private static final String ERROR = "<error>"; //NOI18N
    public static final int GETTERS_ONLY = 1;
    public static final int SETTERS_ONLY = 2;

    private GeneratorUtils() {
    }
    
    public static List<? extends ExecutableElement> findUndefs(CompilationInfo info, TypeElement impl) {
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL))
            ERR.log(ErrorManager.INFORMATIONAL, "findUndefs(" + info + ", " + impl + ")");
        List<? extends ExecutableElement> undef = info.getElementUtilities().findUnimplementedMethods(impl);
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL))
            ERR.log(ErrorManager.INFORMATIONAL, "undef=" + undef);
        return undef;
    }
    
    public static List<? extends ExecutableElement> findOverridable(CompilationInfo info, TypeElement impl) {
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL))
            ERR.log(ErrorManager.INFORMATIONAL, "findOverridable(" + info + ", " + impl + ")");
        List<ExecutableElement> overridable = new ArrayList<ExecutableElement>();
        for (ExecutableElement ee : ElementFilter.methodsIn(info.getElements().getAllMembers(impl))) {
            Set<Modifier> set = EnumSet.copyOf(NOT_OVERRIDABLE);                
            set.removeAll(ee.getModifiers());                
            if (set.size() == NOT_OVERRIDABLE.size()
                    && !overridesPackagePrivateOutsidePackage(ee, impl) //do not offer package private methods in case they're from different package
                    && !isOverridden(info, ee, impl)) {
                overridable.add(ee);
            }
        }
        Collections.reverse(overridable);
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL))
            ERR.log(ErrorManager.INFORMATIONAL, "overridable=" + overridable);

        return overridable;
    }

    public static Map<? extends TypeElement, ? extends List<? extends VariableElement>> findAllAccessibleFields(CompilationInfo info, TypeElement clazz) {
        Map<TypeElement, List<? extends VariableElement>> result = new HashMap<TypeElement, List<? extends VariableElement>>();

        result.put(clazz, findAllAccessibleFields(info, clazz, clazz));

        for (TypeElement te : getAllParents(clazz)) {
            result.put(te, findAllAccessibleFields(info, clazz, te));
        }

        return result;
    }
    
    public static void scanForFieldsAndConstructors(CompilationInfo info, final TreePath clsPath, final Set<VariableElement> initializedFields, final Set<VariableElement> uninitializedFields, final List<ExecutableElement> constructors) {
        final Trees trees = info.getTrees();
        new TreePathScanner<Void, Boolean>() {
            @Override
            public Void visitVariable(VariableTree node, Boolean p) {
                if (ERROR.contentEquals(node.getName()))
                    return null;
                Element el = trees.getElement(getCurrentPath());
                if (el != null && el.getKind() == ElementKind.FIELD && !el.getModifiers().contains(Modifier.STATIC) && node.getInitializer() == null && !initializedFields.remove(el))
                    uninitializedFields.add((VariableElement)el);
                return null;
            }
            @Override
            public Void visitAssignment(AssignmentTree node, Boolean p) {
                Element el = trees.getElement(new TreePath(getCurrentPath(), node.getVariable()));
                if (el != null && el.getKind() == ElementKind.FIELD && !uninitializedFields.remove(el))
                    initializedFields.add((VariableElement)el);
                return null;
            }
            @Override
            public Void visitClass(ClassTree node, Boolean p) {
                //do not analyse the inner classes:
                return p ? super.visitClass(node, false) : null;
            }
            @Override
            public Void visitMethod(MethodTree node, Boolean p) {
                Element el = trees.getElement(getCurrentPath());
                if (el != null && el.getKind() == ElementKind.CONSTRUCTOR)
                    constructors.add((ExecutableElement)el);
                return null;
            }
        }.scan(clsPath, Boolean.TRUE);
    }

    public static void generateAllAbstractMethodImplementations(WorkingCopy wc, TreePath path) {
        assert TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind());
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            ClassTree clazz = (ClassTree)path.getLeaf();
            GeneratorUtilities gu = GeneratorUtilities.get(wc);
            ElementUtilities elemUtils = wc.getElementUtilities();
            clazz = gu.insertClassMembers(clazz, gu.createAbstractMethodImplementations(te, elemUtils.findUnimplementedMethods(te)));
            wc.rewrite(path.getLeaf(), clazz);
        }
    }
    
    public static void generateAbstractMethodImplementations(WorkingCopy wc, TreePath path, List<? extends ExecutableElement> elements, int offset) {
        assert TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind());
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            ClassTree clazz = (ClassTree)path.getLeaf();
            wc.rewrite(clazz, insertClassMembers(wc, clazz, GeneratorUtilities.get(wc).createAbstractMethodImplementations(te, elements), offset));
        }
    }

    public static void generateAbstractMethodImplementation(WorkingCopy wc, TreePath path, ExecutableElement element, int offset) {
        assert TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind());
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            ClassTree clazz = (ClassTree)path.getLeaf();
            wc.rewrite(clazz, insertClassMember(wc, clazz, GeneratorUtilities.get(wc).createAbstractMethodImplementation(te, element), offset));
        }
    }
    
    public static void generateMethodOverrides(WorkingCopy wc, TreePath path, List<? extends ExecutableElement> elements, int offset) {
        assert TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind());
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            ClassTree clazz = (ClassTree)path.getLeaf();
            wc.rewrite(clazz, insertClassMembers(wc, clazz, GeneratorUtilities.get(wc).createOverridingMethods(te, elements), offset));
        }
    }
    
    public static void generateMethodOverride(WorkingCopy wc, TreePath path, ExecutableElement element, int offset) {
        assert TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind());
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            ClassTree clazz = (ClassTree)path.getLeaf();
            wc.rewrite(clazz, insertClassMember(wc, clazz, GeneratorUtilities.get(wc).createOverridingMethod(te, element), offset));
        }
    }

    public static void generateConstructor(WorkingCopy wc, TreePath path, Iterable<? extends VariableElement> initFields, ExecutableElement inheritedConstructor, int offset) {
        ClassTree clazz = (ClassTree)path.getLeaf();
        TypeElement te = (TypeElement) wc.getTrees().getElement(path);
        wc.rewrite(clazz, insertClassMembers(wc, clazz, Collections.singletonList(GeneratorUtilities.get(wc).createConstructor(te, initFields, inheritedConstructor)), offset));
    }
    
    public static void generateConstructors(WorkingCopy wc, TreePath path, Iterable<? extends VariableElement> initFields, List<? extends ExecutableElement> inheritedConstructors, int offset) {
        ClassTree clazz = (ClassTree)path.getLeaf();
        TypeElement te = (TypeElement) wc.getTrees().getElement(path);
        GeneratorUtilities gu = GeneratorUtilities.get(wc);
        List<Tree> members = new ArrayList<Tree>();
        for (ExecutableElement inheritedConstructor : inheritedConstructors) {
            members.add(gu.createConstructor(te, initFields, inheritedConstructor));
        }
        wc.rewrite(clazz, insertClassMembers(wc, clazz, members, offset));
    }
    
    public static void generateGettersAndSetters(WorkingCopy wc, TreePath path, Iterable<? extends VariableElement> fields, int type, int offset) {
        assert TreeUtilities.CLASS_TREE_KINDS.contains(path.getLeaf().getKind());
        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
        if (te != null) {
            GeneratorUtilities gu = GeneratorUtilities.get(wc);
            ClassTree clazz = (ClassTree)path.getLeaf();
            List<Tree> members = new ArrayList<Tree>();
            for(VariableElement element : fields) {
                if (type != SETTERS_ONLY)
                    members.add(gu.createGetter(te, element));
                if (type != GETTERS_ONLY)
                    members.add(gu.createSetter(te, element));
            }
            wc.rewrite(clazz, insertClassMembers(wc, clazz, members, offset));
        }
    }
    
    public static boolean hasGetter(CompilationInfo info, TypeElement typeElement, VariableElement field, Map<String, List<ExecutableElement>> methods, CodeStyle cs) {
        CharSequence name = field.getSimpleName();
        assert name.length() > 0;
        TypeMirror type = field.asType();
        boolean isStatic = field.getModifiers().contains(Modifier.STATIC);
        String getterName = CodeStyleUtils.computeGetterName(name, org.netbeans.modules.editor.java.Utilities.isBoolean(type), isStatic, cs);
        Types types = info.getTypes();
        List<ExecutableElement> candidates = methods.get(getterName);
        if (candidates != null) {
            for (ExecutableElement candidate : candidates) {
                if ((!candidate.getModifiers().contains(Modifier.ABSTRACT) || candidate.getEnclosingElement() == typeElement)
                        && candidate.getParameters().isEmpty()
                        && types.isSameType(candidate.getReturnType(), type))
                    return true;
            }
        }
        return false;
    }
    
    public static boolean hasSetter(CompilationInfo info, TypeElement typeElement, VariableElement field, Map<String, List<ExecutableElement>> methods, CodeStyle cs) {
        CharSequence name = field.getSimpleName();
        assert name.length() > 0;
        TypeMirror type = field.asType();
        boolean isStatic = field.getModifiers().contains(Modifier.STATIC);
        String setterName = CodeStyleUtils.computeSetterName(name, isStatic, cs);
        Types types = info.getTypes();
        List<ExecutableElement> candidates = methods.get(setterName);
        if (candidates != null) {
            for (ExecutableElement candidate : candidates) {
                if ((!candidate.getModifiers().contains(Modifier.ABSTRACT) || candidate.getEnclosingElement() == typeElement)
                        && candidate.getReturnType().getKind() == TypeKind.VOID
                        && candidate.getParameters().size() == 1
                        && types.isSameType(candidate.getParameters().get(0).asType(), type))
                    return true;
            }
        }
        return false;
    }
    
    public static ClassTree insertClassMembers(WorkingCopy wc, ClassTree clazz, List<? extends Tree> members, int offset) throws IllegalStateException {
        if (members.isEmpty())
            return clazz;
        if (offset < 0 || getCodeStyle(wc).getClassMemberInsertionPoint() != CodeStyle.InsertionPoint.CARET_LOCATION)
            return GeneratorUtilities.get(wc).insertClassMembers(clazz, members);
        int index = 0;
        SourcePositions sp = wc.getTrees().getSourcePositions();
        GuardedDocument gdoc = null;
        try {
            Document doc = wc.getDocument();
            if (doc != null && doc instanceof GuardedDocument)
                gdoc = (GuardedDocument)doc;
        } catch (IOException ioe) {}
        Tree lastMember = null;
        Tree nextMember = null;
        for (Tree tree : clazz.getMembers()) {
            if (offset <= sp.getStartPosition(wc.getCompilationUnit(), tree)) {
                if (gdoc == null) {
                    nextMember = tree;
                    break;
                }
                int pos = (int)(lastMember != null ? sp.getEndPosition(wc.getCompilationUnit(), lastMember) : sp.getStartPosition(wc.getCompilationUnit(), clazz));
                pos = gdoc.getGuardedBlockChain().adjustToBlockEnd(pos);
                if (pos <= sp.getStartPosition(wc.getCompilationUnit(), tree)) {
                    nextMember = tree;
                    break;
                }
            }
            index++;
            lastMember = tree;
        }
        if (lastMember != null) {
            moveCommentsBeforeOffset(wc, lastMember, false, members.get(0), offset);
        }
        if (nextMember != null) {
            moveCommentsBeforeOffset(wc, nextMember, true, members.get(0), offset);
        }
        TreeMaker tm = wc.getTreeMaker();
        for (int i = members.size() - 1; i >= 0; i--) {
            clazz = tm.insertClassMember(clazz, index, members.get(i));
        }
        return clazz;
    }
    
    public static ClassTree insertClassMember(WorkingCopy wc, ClassTree clazz, Tree member, int offset) throws IllegalStateException {
        int idx = 0;
        Tree prev = null;
        Tree next = null;
        for (Tree tree : clazz.getMembers()) {
            if (wc.getTrees().getSourcePositions().getStartPosition(wc.getCompilationUnit(), tree) < offset) {
                prev = tree;
                idx++;
            } else {
                next = tree;
                break;
            }
        }
        if (prev != null) {
            moveCommentsBeforeOffset(wc, prev, false, member, offset);
        }
        if (next != null) {
            moveCommentsBeforeOffset(wc, next, true, member, offset);
        }
        return wc.getTreeMaker().insertClassMember(clazz, idx, member);
    }
    
    private static void moveCommentsBeforeOffset(WorkingCopy wc, Tree from, boolean next, Tree to, int offset) {
        List<Comment> toMove = new LinkedList<Comment>();
        int idx = 0;
        for (Comment comment : wc.getTreeUtilities().getComments(from, next)) {
            if (comment.endPos() > offset)
                break;
            toMove.add(comment);
            idx++;
        }
        if (toMove.size() > 0) {
            TreeMaker tm = wc.getTreeMaker();
            Tree tree = from;
            switch (from.getKind()) {
                case METHOD:
                    tree = tm.setLabel(from, ((MethodTree)from).getName());
                    break;
                case VARIABLE:
                    tree = tm.setLabel(from, ((VariableTree)from).getName());
                    break;
                case BLOCK:
                    tree = tm.Block(((BlockTree)from).getStatements(), ((BlockTree)from).isStatic());
                    GeneratorUtilities gu = GeneratorUtilities.get(wc);
                    gu.copyComments(from, tree, true);
                    gu.copyComments(from, tree, false);
                    break;
            }
            for (int i = idx - 1; i >= 0; i--) {
                tm.removeComment(tree, i, next);
            }
            wc.rewrite(from, tree);
            for (Comment comment : toMove) {
                tm.addComment(to, comment, true);
            }
        }
    }

    private static CodeStyle getCodeStyle(CompilationInfo info) {
        if (info != null) {
            try {
                Document doc = info.getDocument();
                if (doc != null) {
                    CodeStyle cs = (CodeStyle)doc.getProperty(CodeStyle.class);
                    return cs != null ? cs : CodeStyle.getDefault(doc);
                }
            } catch (IOException ioe) {
                // ignore
            }
            
            FileObject file = info.getFileObject();
            if (file != null) {
                return CodeStyle.getDefault(file);
            }
        }
        
        return CodeStyle.getDefault((Document)null);
    }
    
    private static List<? extends VariableElement> findAllAccessibleFields(CompilationInfo info, TypeElement accessibleFrom, TypeElement toScan) {
        List<VariableElement> result = new ArrayList<VariableElement>();

        for (VariableElement ve : ElementFilter.fieldsIn(toScan.getEnclosedElements())) {
            //check if ve is accessible from accessibleFrom:
            if (ve.getModifiers().contains(Modifier.PUBLIC)) {
                result.add(ve);
                continue;
            }
            if (ve.getModifiers().contains(Modifier.PRIVATE)) {
                if (accessibleFrom == toScan)
                    result.add(ve);
                continue;
            }
            if (ve.getModifiers().contains(Modifier.PROTECTED)) {
                if (getAllParents(accessibleFrom).contains(toScan))
                    result.add(ve);
                continue;
            }
            //TODO:package private:
        }

        return result;
    }
    
    public static Collection<TypeElement> getAllParents(TypeElement of) {
        Set<TypeElement> result = new HashSet<TypeElement>();
        
        for (TypeMirror t : of.getInterfaces()) {
            TypeElement te = (TypeElement) ((DeclaredType)t).asElement();
            
            if (te != null) {
                result.add(te);
                result.addAll(getAllParents(te));
            } else {
                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                    ERR.log(ErrorManager.INFORMATIONAL, "te=null, t=" + t);
                }
            }
        }
        
        TypeMirror sup = of.getSuperclass();
        TypeElement te = sup.getKind() == TypeKind.DECLARED ? (TypeElement) ((DeclaredType)sup).asElement() : null;
        
        if (te != null) {
            result.add(te);
            result.addAll(getAllParents(te));
        } else {
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "te=null, t=" + of);
            }
        }
        
        return result;
    }

    /**
     * @param info tested file's info
     * @return true if SourceVersion of source represented by provided info supports Override
     */
    public static boolean supportsOverride(@NonNull CompilationInfo info) {
        return SourceVersion.RELEASE_5.compareTo(info.getSourceVersion()) <= 0
               && info.getElements().getTypeElement("java.lang.Override") != null;
    }

    private static List<TypeElement> getAllClasses(TypeElement of) {
        List<TypeElement> result = new ArrayList<TypeElement>();
        TypeMirror sup = of.getSuperclass();
        TypeElement te = sup.getKind() == TypeKind.DECLARED ? (TypeElement) ((DeclaredType)sup).asElement() : null;
        
        result.add(of);
        
        if (te != null) {
            result.addAll(getAllClasses(te));
        } else {
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "te=null, t=" + of);
            }
        }
        
        return result;
    }
    
    private static boolean isOverridden(CompilationInfo info, ExecutableElement methodBase, TypeElement origin) {
        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "isOverridden(" + info + ", " + methodBase + ", " + origin + ")");
        }
        
        Element impl = info.getElementUtilities().getImplementationOf(methodBase, origin);
        if (impl == null || impl == methodBase && origin != methodBase.getEnclosingElement()) {
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "no overriding methods");
            }
            return false;
        }

        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "overrides:");
            ERR.log(ErrorManager.INFORMATIONAL, "impl=" + impl.getEnclosingElement());
            ERR.log(ErrorManager.INFORMATIONAL, "methodImpl=" + impl);
        }
                
        return true;
    }

    private static final Set<Modifier> NOT_OVERRIDABLE = EnumSet.of(Modifier.ABSTRACT, Modifier.STATIC, Modifier.FINAL, Modifier.PRIVATE);

    public static boolean isAccessible(TypeElement from, Element what) {
        if (what.getModifiers().contains(Modifier.PUBLIC))
            return true;

        TypeElement fromTopLevel = SourceUtils.getOutermostEnclosingTypeElement(from);
        TypeElement whatTopLevel = SourceUtils.getOutermostEnclosingTypeElement(what);

        if (fromTopLevel.equals(whatTopLevel))
            return true;

        if (what.getModifiers().contains(Modifier.PRIVATE))
            return false;

        if (what.getModifiers().contains(Modifier.PROTECTED)) {
            if (getAllClasses(fromTopLevel).contains(SourceUtils.getEnclosingTypeElement(what)))
                return true;
        }

        //package private:
        return ((PackageElement) fromTopLevel.getEnclosingElement()).getQualifiedName().toString().contentEquals(((PackageElement) whatTopLevel.getEnclosingElement()).getQualifiedName());
    }
    
    static DialogDescriptor createDialogDescriptor( JComponent content, String label ) {
        JButton[] buttons = new JButton[2];
        buttons[0] = new JButton(NbBundle.getMessage(GeneratorUtils.class, "LBL_generate_button") );
	buttons[0].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GeneratorUtils.class, "A11Y_Generate"));
        buttons[1] = new JButton(NbBundle.getMessage(GeneratorUtils.class, "LBL_cancel_button") );
        return new DialogDescriptor(content, label, true, buttons, buttons[0], DialogDescriptor.DEFAULT_ALIGN, null, null);
        
    }
    
    /**
     * Detects if this element overrides package private element from superclass
     * outside package
     * @param ee elememt to test
     * @return true if it does
     */ 
    private static boolean overridesPackagePrivateOutsidePackage(ExecutableElement ee, TypeElement impl) {
        String elemPackageName = getPackageName(ee);
        String currentPackageName = getPackageName(impl);
        if(!ee.getModifiers().contains(Modifier.PRIVATE) && !ee.getModifiers().contains(Modifier.PUBLIC) && !ee.getModifiers().contains(Modifier.PROTECTED) && !currentPackageName.equals(elemPackageName))
            return true;
        else
            return false;
    }

    private static String getPackageName(Element e) {
        while (e.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
            e = e.getEnclosingElement();
        }
        return ((PackageElement) e.getEnclosingElement()).getQualifiedName().toString();
    }

    public static void guardedCommit(JTextComponent component, ModificationResult mr) throws IOException {
        try {
            mr.commit();
        } catch (IOException e) {
            if (e.getCause() instanceof GuardedException) {
                String message = NbBundle.getMessage(GeneratorUtils.class, "ERR_CannotApplyGuarded");

                Utilities.setStatusBoldText(component, message);
                Logger.getLogger(GeneratorUtils.class.getName()).log(Level.FINE, null, e);
            }
        }
    }
}
