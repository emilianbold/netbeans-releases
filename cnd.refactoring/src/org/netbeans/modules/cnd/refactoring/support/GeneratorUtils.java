/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.refactoring.support;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.openide.DialogDescriptor;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 * @author Vladimir Voskresensky
 */
public class GeneratorUtils {

    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(GeneratorUtils.class.getName());
    private static final String ERROR = "<error>"; //NOI18N
    public static final int GETTERS_ONLY = 1;
    public static final int SETTERS_ONLY = 2;

    private GeneratorUtils() {
    }

    public static Collection<CsmMember> getAllMembers(CsmClass typeElement) {
        // for now returns only current class elements, but in fact needs full hierarchy
        return typeElement.getMembers();
    }

    public static boolean isConstant(CsmVariable var) {
        return var.getType().isConst();
    }
//    public static ClassTree insertClassMember(WorkingCopy copy, TreePath path, Tree member) {
//        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
//        TreeUtilities tu = copy.getTreeUtilities();
//        int idx = 0;
//        for (Tree tree : ((ClassTree)path.getLeaf()).getMembers()) {
//            if (!tu.isSynthetic(new TreePath(path, tree)) && ClassMemberComparator.compare(member, tree) < 0)
//                break;
//            idx++;
//        }
//        return copy.getTreeMaker().insertClassMember((ClassTree)path.getLeaf(), idx, member);
//    }
//
//    public static List<? extends ExecutableElement> findUndefs(CompilationInfo info, TypeElement impl) {
//        if (ERR.isLoggable(ErrorManager.INFORMATIONAL))
//            ERR.log(ErrorManager.INFORMATIONAL, "findUndefs(" + info + ", " + impl + ")");
//        List<? extends ExecutableElement> undef = info.getElementUtilities().findUnimplementedMethods(impl);
//        if (ERR.isLoggable(ErrorManager.INFORMATIONAL))
//            ERR.log(ErrorManager.INFORMATIONAL, "undef=" + undef);
//        return undef;
//    }
//
//    public static List<? extends ExecutableElement> findOverridable(CompilationInfo info, TypeElement impl) {
//        List<ExecutableElement> overridable = new ArrayList<ExecutableElement>();
//        List<TypeElement> classes = getAllClasses(impl);
//
//        if (ERR.isLoggable(ErrorManager.INFORMATIONAL))
//            ERR.log(ErrorManager.INFORMATIONAL, "classes=" + classes);
//
//        for (TypeElement te : classes.subList(1, classes.size())) {
//            for (ExecutableElement ee : ElementFilter.methodsIn(te.getEnclosedElements())) {
//                Set<Modifier> set = EnumSet.copyOf(NOT_OVERRIDABLE);
//
//                set.removeAll(ee.getModifiers());
//
//                if (set.size() != NOT_OVERRIDABLE.size())
//                    continue;
//
//                if(ee.getModifiers().contains(Modifier.PRIVATE)) //do not offer overriding of private methods
//                    continue;
//
//                if(overridesPackagePrivateOutsidePackage(ee, impl)) //do not offer package private methods in case they're from different package
//                    continue;
//
//                int thisElement = classes.indexOf(te);
//
//                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
//                    ERR.log(ErrorManager.INFORMATIONAL, "ee=" + ee);
//                    ERR.log(ErrorManager.INFORMATIONAL, "thisElement = " + thisElement);
//                    ERR.log(ErrorManager.INFORMATIONAL, "classes.subList(0, thisElement + 1)=" + classes.subList(0, thisElement + 1));
//                    ERR.log(ErrorManager.INFORMATIONAL, "isOverriden(info, ee, classes.subList(0, thisElement + 1))=" + isOverriden(info, ee, classes.subList(0, thisElement + 1)));
//                }
//
//                if (!isOverriden(info, ee, classes.subList(0, thisElement + 1))) {
//                    overridable.add(ee);
//                }
//            }
//        }
//
//        return overridable;
//    }
//
//    public static Map<? extends TypeElement, ? extends List<? extends VariableElement>> findAllAccessibleFields(CompilationInfo info, TypeElement clazz) {
//        Map<TypeElement, List<? extends VariableElement>> result = new HashMap<TypeElement, List<? extends VariableElement>>();
//
//        result.put(clazz, findAllAccessibleFields(info, clazz, clazz));
//
//        for (TypeElement te : getAllParents(clazz)) {
//            result.put(te, findAllAccessibleFields(info, clazz, te));
//        }
//
//        return result;
//    }
//
//    public static void scanForFieldsAndConstructors(CompilationInfo info, final TreePath clsPath, final Set<VariableElement> initializedFields, final Set<VariableElement> uninitializedFields, final List<ExecutableElement> constructors) {
//        final Trees trees = info.getTrees();
//        new TreePathScanner<Void, Boolean>() {
//            @Override
//            public Void visitVariable(VariableTree node, Boolean p) {
//                if (ERROR.contentEquals(node.getName()))
//                    return null;
//                Element el = trees.getElement(getCurrentPath());
//                if (el != null && el.getKind() == ElementKind.FIELD && !el.getModifiers().contains(Modifier.STATIC) && node.getInitializer() == null && !initializedFields.remove(el))
//                    uninitializedFields.add((VariableElement)el);
//                return null;
//            }
//            @Override
//            public Void visitAssignment(AssignmentTree node, Boolean p) {
//                Element el = trees.getElement(new TreePath(getCurrentPath(), node.getVariable()));
//                if (el != null && el.getKind() == ElementKind.FIELD && !uninitializedFields.remove(el))
//                    initializedFields.add((VariableElement)el);
//                return null;
//            }
//            @Override
//            public Void visitClass(ClassTree node, Boolean p) {
//                //do not analyse the inner classes:
//                return p ? super.visitClass(node, false) : null;
//            }
//            @Override
//            public Void visitMethod(MethodTree node, Boolean p) {
//                Element el = trees.getElement(getCurrentPath());
//                if (el != null && el.getKind() == ElementKind.CONSTRUCTOR)
//                    constructors.add((ExecutableElement)el);
//                return null;
//            }
//        }.scan(clsPath, Boolean.TRUE);
//    }
//
//    public static void generateAllAbstractMethodImplementations(WorkingCopy wc, TreePath path) {
//        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
//        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
//        if (te != null) {
//            TreeMaker make = wc.getTreeMaker();
//            ClassTree clazz = (ClassTree)path.getLeaf();
//            List<Tree> members = new ArrayList<Tree>();
//            GeneratorUtilities gu = GeneratorUtilities.get(wc);
//            ElementUtilities elemUtils = wc.getElementUtilities();
//            for(ExecutableElement element : elemUtils.findUnimplementedMethods(te))
//                members.add(gu.createAbstractMethodImplementation(te, element));
//            ClassTree nue = gu.insertClassMembers(clazz, members);
//            wc.rewrite(clazz, nue);
//        }
//    }
//
//    public static void generateAbstractMethodImplementations(WorkingCopy wc, TreePath path, List<? extends ExecutableElement> elements, int index) {
//        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
//        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
//        if (te != null) {
//            TreeMaker make = wc.getTreeMaker();
//            ClassTree clazz = (ClassTree)path.getLeaf();
//            List<Tree> members = new ArrayList<Tree>(clazz.getMembers());
//            GeneratorUtilities gu = GeneratorUtilities.get(wc);
//            members.addAll(index, gu.createAbstractMethodImplementations(te, elements));
//            ClassTree nue = make.Class(clazz.getModifiers(), clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), (List<ExpressionTree>)clazz.getImplementsClause(), members);
//            wc.rewrite(clazz, nue);
//        }
//    }
//
//    public static void generateAbstractMethodImplementation(WorkingCopy wc, TreePath path, ExecutableElement element, int index) {
//        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
//        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
//        if (te != null) {
//            GeneratorUtilities gu = GeneratorUtilities.get(wc);
//            ClassTree decl = wc.getTreeMaker().insertClassMember((ClassTree)path.getLeaf(), index, gu.createAbstractMethodImplementation(te, element));
//            wc.rewrite(path.getLeaf(), decl);
//        }
//    }
//
//    public static void generateMethodOverrides(WorkingCopy wc, TreePath path, List<? extends ExecutableElement> elements, int index) {
//        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
//        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
//        if (te != null) {
//            TreeMaker make = wc.getTreeMaker();
//            ClassTree clazz = (ClassTree)path.getLeaf();
//            List<Tree> members = new ArrayList<Tree>(clazz.getMembers());
//            GeneratorUtilities gu = GeneratorUtilities.get(wc);
//            members.addAll(index, gu.createOverridingMethods(te, elements));
//            ClassTree nue = make.Class(clazz.getModifiers(), clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), (List<ExpressionTree>)clazz.getImplementsClause(), members);
//            wc.rewrite(clazz, nue);
//        }
//    }
//
//    public static void generateMethodOverride(WorkingCopy wc, TreePath path, ExecutableElement element, int index) {
//        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
//        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
//        if (te != null) {
//            GeneratorUtilities gu = GeneratorUtilities.get(wc);
//            ClassTree decl = wc.getTreeMaker().insertClassMember((ClassTree)path.getLeaf(), index, gu.createOverridingMethod(te, element));
//            wc.rewrite(path.getLeaf(), decl);
//        }
//    }
//
//    public static void generateConstructor(WorkingCopy wc, TreePath path, Iterable<? extends VariableElement> initFields, ExecutableElement inheritedConstructor, int index) {
//        TreeMaker make = wc.getTreeMaker();
//        ClassTree clazz = (ClassTree)path.getLeaf();
//        TypeElement te = (TypeElement) wc.getTrees().getElement(path);
//        GeneratorUtilities gu = GeneratorUtilities.get(wc);
//        ClassTree decl = make.insertClassMember(clazz, index, gu.createConstructor(te, initFields, inheritedConstructor)); //NOI18N
//        wc.rewrite(path.getLeaf(), decl);
//    }
//
//    public static void generateConstructors(WorkingCopy wc, TreePath path, Iterable<? extends VariableElement> initFields, List<? extends ExecutableElement> inheritedConstructors, int index) {
//        TreeMaker make = wc.getTreeMaker();
//        ClassTree clazz = (ClassTree)path.getLeaf();
//        TypeElement te = (TypeElement) wc.getTrees().getElement(path);
//        GeneratorUtilities gu = GeneratorUtilities.get(wc);
//        ClassTree decl = clazz;
//        for (ExecutableElement inheritedConstructor : inheritedConstructors) {
//            decl = make.insertClassMember(decl, index, gu.createConstructor(te, initFields, inheritedConstructor)); //NOI18N
//        }
//        wc.rewrite(clazz, decl);
//    }
//
//    public static void generateGettersAndSetters(WorkingCopy wc, TreePath path, Iterable<? extends VariableElement> fields, int type, int index) {
//        assert path.getLeaf().getKind() == Tree.Kind.CLASS;
//        TypeElement te = (TypeElement)wc.getTrees().getElement(path);
//        if (te != null) {
//            TreeMaker make = wc.getTreeMaker();
//            GeneratorUtilities gu = GeneratorUtilities.get(wc);
//            ClassTree clazz = (ClassTree)path.getLeaf();
//            List<Tree> members = new ArrayList<Tree>(clazz.getMembers());
//            List<Tree> methods = new ArrayList<Tree>();
//            for(VariableElement element : fields) {
//                if (type != SETTERS_ONLY)
//                    methods.add(gu.createGetter(te, element));
//                if (type != GETTERS_ONLY)
//                    methods.add(gu.createSetter(te, element));
//            }
//            members.addAll(index, methods);
//            ClassTree nue = make.Class(clazz.getModifiers(), clazz.getSimpleName(), clazz.getTypeParameters(), clazz.getExtendsClause(), (List<ExpressionTree>)clazz.getImplementsClause(), members);
//            wc.rewrite(clazz, nue);
//        }
//    }
//
    public static boolean hasGetter(CsmField field, Map<String, List<CsmMethod>> methods) {
        CharSequence name = field.getName();
        assert name.length() > 0;
        CsmType type = field.getType();
        StringBuilder sb = getCapitalizedName(name);
        sb.insert(0, getTypeKind(type) == TypeKind.BOOLEAN ? "is" : "get"); //NOI18N
//        Types types = info.getTypes();
        List<CsmMethod> candidates = methods.get(sb.toString());
        if (candidates != null) {
            for (CsmMethod candidate : candidates) {
                @SuppressWarnings("unchecked")
                Collection<CsmParameter> parameters = candidate.getParameters();
                if (getTypeKind(candidate.getReturnType()) == TypeKind.VOID && parameters.size() == 1 && isSameType(parameters.iterator().next().getType(), type)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static enum TypeKind {
        VOID,
        BOOLEAN,
        UNKNOWN
    };
    
    private static TypeKind getTypeKind(CsmType type) {
        if (CsmKindUtilities.isBuiltIn(type)) {
            String text = type.getClassifierText().toString();
            if ("void".equals(text)) { // NOI18N
                return TypeKind.VOID;
            } else if ("bool".equals(text) || "boolean".equals(text)) { // NOI18N
                return TypeKind.BOOLEAN;
            }
        }
        return TypeKind.UNKNOWN;
    }

    private static boolean isSameType(CsmType type1, CsmType type2) {
        return type1.equals(type2);
    }

    public static boolean hasSetter(CsmField field, Map<String, List<CsmMethod>> methods) {
        CharSequence name = field.getName();
        assert name.length() > 0;
        CsmType type = field.getType();
        StringBuilder sb = getCapitalizedName(name);
        sb.insert(0, "set"); //NOI18N
//        Types types = info.getTypes();
        List<CsmMethod> candidates = methods.get(sb.toString());
        if (candidates != null) {
            for (CsmMethod candidate : candidates) {
                @SuppressWarnings("unchecked")
                Collection<CsmParameter> parameters = candidate.getParameters();
                if (getTypeKind(candidate.getReturnType()) == TypeKind.VOID && parameters.size() == 1 && isSameType(parameters.iterator().next().getType(), type)) {
                    return true;
                }
            }
        }
        return false;
    }
//
//    public static int findClassMemberIndex(WorkingCopy wc, ClassTree clazz, int offset) {
//        int index = 0;
//        SourcePositions sp = wc.getTrees().getSourcePositions();
//        GuardedDocument gdoc = null;
//        try {
//            Document doc = wc.getDocument();
//            if (doc != null && doc instanceof GuardedDocument)
//                gdoc = (GuardedDocument)doc;
//        } catch (IOException ioe) {}
//        Tree lastMember = null;
//        for (Tree tree : clazz.getMembers()) {
//            if (offset <= sp.getStartPosition(wc.getCompilationUnit(), tree)) {
//                if (gdoc == null)
//                    break;
//                int pos = (int)(lastMember != null ? sp.getEndPosition(wc.getCompilationUnit(), lastMember) : sp.getStartPosition(wc.getCompilationUnit(), clazz));
//                pos = gdoc.getGuardedBlockChain().adjustToBlockEnd(pos);
//                if (pos <= sp.getStartPosition(wc.getCompilationUnit(), tree))
//                    break;
//            }
//            index++;
//            lastMember = tree;
//        }
//        return index;
//    }
//
//    private static List<? extends VariableElement> findAllAccessibleFields(CompilationInfo info, TypeElement accessibleFrom, TypeElement toScan) {
//        List<VariableElement> result = new ArrayList<VariableElement>();
//
//        for (VariableElement ve : ElementFilter.fieldsIn(toScan.getEnclosedElements())) {
//            //check if ve is accessible from accessibleFrom:
//            if (ve.getModifiers().contains(Modifier.PUBLIC)) {
//                result.add(ve);
//                continue;
//            }
//            if (ve.getModifiers().contains(Modifier.PRIVATE)) {
//                if (accessibleFrom == toScan)
//                    result.add(ve);
//                continue;
//            }
//            if (ve.getModifiers().contains(Modifier.PROTECTED)) {
//                if (getAllParents(accessibleFrom).contains(toScan))
//                    result.add(ve);
//                continue;
//            }
//            //TODO:package private:
//        }
//
//        return result;
//    }
//
//    public static Collection<TypeElement> getAllParents(TypeElement of) {
//        Set<TypeElement> result = new HashSet<TypeElement>();
//
//        for (TypeMirror t : of.getInterfaces()) {
//            TypeElement te = (TypeElement) ((DeclaredType)t).asElement();
//
//            if (te != null) {
//                result.add(te);
//                result.addAll(getAllParents(te));
//            } else {
//                if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
//                    ERR.log(ErrorManager.INFORMATIONAL, "te=null, t=" + t);
//                }
//            }
//        }
//
//        TypeMirror sup = of.getSuperclass();
//        TypeElement te = sup.getKind() == TypeKind.DECLARED ? (TypeElement) ((DeclaredType)sup).asElement() : null;
//
//        if (te != null) {
//            result.add(te);
//            result.addAll(getAllParents(te));
//        } else {
//            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
//                ERR.log(ErrorManager.INFORMATIONAL, "te=null, t=" + of);
//            }
//        }
//
//        return result;
//    }
//
//    public static boolean supportsOverride(FileObject file) {
//        return SUPPORTS_OVERRIDE_SOURCE_LEVELS.contains(SourceLevelQuery.getSourceLevel(file));
//    }
//
//    private static final Set<String> SUPPORTS_OVERRIDE_SOURCE_LEVELS;
//
//    static {
//        SUPPORTS_OVERRIDE_SOURCE_LEVELS = new HashSet();
//
//        SUPPORTS_OVERRIDE_SOURCE_LEVELS.add("1.5");
//        SUPPORTS_OVERRIDE_SOURCE_LEVELS.add("1.6");
//    }
//
//    private static List<TypeElement> getAllClasses(TypeElement of) {
//        List<TypeElement> result = new ArrayList<TypeElement>();
//        TypeMirror sup = of.getSuperclass();
//        TypeElement te = sup.getKind() == TypeKind.DECLARED ? (TypeElement) ((DeclaredType)sup).asElement() : null;
//
//        result.add(of);
//
//        if (te != null) {
//            result.addAll(getAllClasses(te));
//        } else {
//            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
//                ERR.log(ErrorManager.INFORMATIONAL, "te=null, t=" + of);
//            }
//        }
//
//        return result;
//    }
//
//    private static boolean isOverriden(CompilationInfo info, ExecutableElement methodBase, List<TypeElement> classes) {
//        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
//            ERR.log(ErrorManager.INFORMATIONAL, "isOverriden(" + info + ", " + methodBase + ", " + classes + ")");
//        }
//
//        for (TypeElement impl : classes) {
//            for (ExecutableElement methodImpl : ElementFilter.methodsIn(impl.getEnclosedElements())) {
//                if (   ERR.isLoggable(ErrorManager.INFORMATIONAL)
//                && info.getElements().overrides(methodImpl, methodBase, impl)) {
//                    ERR.log(ErrorManager.INFORMATIONAL, "overrides:");
//                    ERR.log(ErrorManager.INFORMATIONAL, "impl=" + impl);
//                    ERR.log(ErrorManager.INFORMATIONAL, "methodImpl=" + methodImpl);
//                }
//
//                if (info.getElements().overrides(methodImpl, methodBase, impl))
//                    return true;
//            }
//        }
//
//        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
//            ERR.log(ErrorManager.INFORMATIONAL, "no overriding methods overrides:");
//        }
//
//        return false;
//    }
//
//    private static final Set<Modifier> NOT_OVERRIDABLE = /*EnumSet.noneOf(Modifier.class);/*/EnumSet.of(Modifier.ABSTRACT, Modifier.STATIC, Modifier.FINAL);
//
//    public static boolean isAccessible(TypeElement from, Element what) {
//        if (what.getModifiers().contains(Modifier.PUBLIC))
//            return true;
//
//        TypeElement fromTopLevel = SourceUtils.getOutermostEnclosingTypeElement(from);
//        TypeElement whatTopLevel = SourceUtils.getOutermostEnclosingTypeElement(what);
//
//        if (fromTopLevel.equals(whatTopLevel))
//            return true;
//
//        if (what.getModifiers().contains(Modifier.PRIVATE))
//            return false;
//
//        if (what.getModifiers().contains(Modifier.PROTECTED)) {
//            if (getAllClasses(fromTopLevel).contains(SourceUtils.getEnclosingTypeElement(what)))
//                return true;
//        }
//
//        //package private:
//        return ((PackageElement) fromTopLevel.getEnclosingElement()).getQualifiedName().toString().contentEquals(((PackageElement) whatTopLevel.getEnclosingElement()).getQualifiedName());
//    }
//

    public static DialogDescriptor createDialogDescriptor(JComponent content, String label) {
        JButton[] buttons = new JButton[2];
        buttons[0] = new JButton(NbBundle.getMessage(GeneratorUtils.class, "LBL_generate_button"));
        buttons[0].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(GeneratorUtils.class, "A11Y_Generate"));
        buttons[1] = new JButton(NbBundle.getMessage(GeneratorUtils.class, "LBL_cancel_button"));
        return new DialogDescriptor(content, label, true, buttons, buttons[0], DialogDescriptor.DEFAULT_ALIGN, null, null);

    }
//
//    /**
//     * Detects if this element overrides package private element from superclass
//     * outside package
//     * @param ee elememt to test
//     * @return true if it does
//     */
//    private static boolean overridesPackagePrivateOutsidePackage(ExecutableElement ee, TypeElement impl) {
//        String elemPackageName = ee.getEnclosingElement().getEnclosingElement().getSimpleName().toString();
//        String currentPackageName = impl.getEnclosingElement().getSimpleName().toString();
//        if(!ee.getModifiers().contains(Modifier.PRIVATE) && !ee.getModifiers().contains(Modifier.PUBLIC) && !ee.getModifiers().contains(Modifier.PROTECTED) && !currentPackageName.equals(elemPackageName))
//            return true;
//        else
//            return false;
//    }
//

    public static StringBuilder getCapitalizedName(CharSequence cs) {
        StringBuilder sb = new StringBuilder(cs);
        // remove usual C++ prefixes
        if (sb.toString().startsWith("m_")) { // NOI18N
            sb.delete(0, 1);
        } else if (sb.length() > 1) {
            if (sb.charAt(0) == 'p' && Character.isUpperCase(sb.charAt(1))) {
                // this is like pointer "pValue"
                sb.deleteCharAt(0);
            }
        }
        while (sb.length() > 1 && sb.charAt(0) == '_') //NOI18N
        {
            sb.deleteCharAt(0);
        }
        if (sb.length() > 0) {
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }
        return sb;
    }
//
//    private static class ClassMemberComparator {
//
//        public static int compare(Tree tree1, Tree tree2) {
//            if (tree1 == tree2)
//                return 0;
//            int importanceDiff = getSortPriority(tree1) - getSortPriority(tree2);
//            if (importanceDiff != 0)
//                return importanceDiff;
//            int alphabeticalDiff = getSortText(tree1).compareTo(getSortText(tree2));
//            if (alphabeticalDiff != 0)
//                return alphabeticalDiff;
//            return -1;
//        }
//
//        private static int getSortPriority(Tree tree) {
//            int ret = 0;
//            ModifiersTree modifiers = null;
//            switch (tree.getKind()) {
//            case CLASS:
//                ret = 400;
//                modifiers = ((ClassTree)tree).getModifiers();
//                break;
//            case METHOD:
//                MethodTree mt = (MethodTree)tree;
//                if (mt.getName().contentEquals("<init>"))
//                    ret = 200;
//                else
//                    ret = 300;
//                modifiers = mt.getModifiers();
//                break;
//            case VARIABLE:
//                ret = 100;
//                modifiers = ((VariableTree)tree).getModifiers();
//                break;
//            }
//            if (modifiers != null) {
//                if (!modifiers.getFlags().contains(Modifier.STATIC))
//                    ret += 1000;
//                if (modifiers.getFlags().contains(Modifier.PUBLIC))
//                    ret += 10;
//                else if (modifiers.getFlags().contains(Modifier.PROTECTED))
//                    ret += 20;
//                else if (modifiers.getFlags().contains(Modifier.PRIVATE))
//                    ret += 40;
//                else
//                    ret += 30;
//            }
//            return ret;
//        }
//
//        private static String getSortText(Tree tree) {
//            switch (tree.getKind()) {
//            case CLASS:
//                return ((ClassTree)tree).getSimpleName().toString();
//            case METHOD:
//                MethodTree mt = (MethodTree)tree;
//                StringBuilder sortParams = new StringBuilder();
//                sortParams.append('(');
//                int cnt = 0;
//                for(Iterator<? extends VariableTree> it = mt.getParameters().iterator(); it.hasNext();) {
//                    VariableTree param = it.next();
//                    if (param.getType().getKind() == Tree.Kind.IDENTIFIER)
//                        sortParams.append(((IdentifierTree)param.getType()).getName().toString());
//                    else if (param.getType().getKind() == Tree.Kind.MEMBER_SELECT)
//                        sortParams.append(((MemberSelectTree)param.getType()).getIdentifier().toString());
//                    if (it.hasNext()) {
//                        sortParams.append(',');
//                    }
//                    cnt++;
//                }
//                sortParams.append(')');
//                return mt.getName().toString() + "#" + ((cnt < 10 ? "0" : "") + cnt) + "#" + sortParams.toString(); //NOI18N
//            case VARIABLE:
//                return ((VariableTree)tree).getName().toString();
//            }
//            return ""; //NOI18N
//        }
//    }
//
//    public static void guardedCommit(JTextComponent component, ModificationResult mr) throws IOException {
//        try {
//            mr.commit();
//        } catch (IOException e) {
//            if (e.getCause() instanceof GuardedException) {
//                String message = NbBundle.getMessage(GeneratorUtils.class, "ERR_CannotApplyGuarded");
//
//                Utilities.setStatusBoldText(component, message);
//                Logger.getLogger(GeneratorUtils.class.getName()).log(Level.FINE, null, e);
//            }
//        }
//    }
}
