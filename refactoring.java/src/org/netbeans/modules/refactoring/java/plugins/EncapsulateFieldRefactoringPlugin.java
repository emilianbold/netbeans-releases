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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.refactoring.java.plugins;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.RetoucheUtils;
import org.netbeans.modules.refactoring.java.api.EncapsulateFieldRefactoring;
import org.netbeans.modules.refactoring.java.spi.JavaRefactoringPlugin;
import org.netbeans.modules.refactoring.java.spi.RefactoringVisitor;
import org.netbeans.modules.refactoring.java.spi.ToPhaseException;
import org.netbeans.modules.refactoring.java.ui.EncapsulateFieldPanel.InsertPoint;
import org.netbeans.modules.refactoring.java.ui.EncapsulateFieldPanel.Javadoc;
import org.netbeans.modules.refactoring.java.ui.EncapsulateFieldPanel.SortBy;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Hurka
 * @author Jan Becicka
 * @author Jan Pokorsky
 */
public final class EncapsulateFieldRefactoringPlugin extends JavaRefactoringPlugin {
    
    private static final Logger LOG = Logger.getLogger(EncapsulateFieldRefactoringPlugin.class.getName());
    
    private ElementHandle<TypeElement> fieldEncloserHandle;
    /**
     * most restrictive accessibility modifier on tree path 
     */
    private Modifier fieldEncloserAccessibility;
    /**
     * present accessibility of field
     */
    private Set<Modifier> fieldAccessibility;
    private ElementHandle<ExecutableElement> currentGetter;
    private ElementHandle<ExecutableElement> currentSetter;
    private static Set<Modifier> accessModifiers = EnumSet.of(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.PUBLIC);
    private static List<Modifier> MODIFIERS = Arrays.asList(Modifier.PRIVATE, null, Modifier.PROTECTED, Modifier.PUBLIC);
    private final EncapsulateFieldRefactoring refactoring;
    public static final String CLASS_FIELD_PREFIX = "_"; // NOI18N
    /**
     * path in source with field declaration; refactoring.getSelectedObject()
     * may contain path to a reference
     */
    private TreePathHandle sourceType;
    
    /** Creates a new instance of RenameRefactoring */
    public EncapsulateFieldRefactoringPlugin(EncapsulateFieldRefactoring refactoring) {
        this.refactoring = refactoring;
    }

    protected JavaSource getJavaSource(Phase p) {
        TreePathHandle handle = sourceType != null? sourceType: refactoring.getSourceType();
        FileObject fo = handle.getFileObject();
        return JavaSource.forFileObject(fo);
    }
    
    @Override
    protected Problem preCheck(CompilationController javac) throws IOException {
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 2);
        try {
            javac.toPhase(JavaSource.Phase.RESOLVED);
            sourceType = this.refactoring.getSourceType();
            Problem result = isElementAvail(sourceType, javac);
            if (result != null) {
                return result;
            }

            Element field = sourceType.resolveElement(javac);
            fireProgressListenerStep();
            if (ElementKind.FIELD == field.getKind()) {
               TreePath tp = javac.getTrees().getPath(field);
               sourceType = TreePathHandle.create(tp, javac);
            } else {
                return createProblem(result, true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateWrongType"));
            }
            if (!RetoucheUtils.isElementInOpenProject(sourceType.getFileObject())) {
                return new Problem(true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_ProjectNotOpened", FileUtil.getFileDisplayName(sourceType.getFileObject())));
            }
            
            TypeElement encloser = (TypeElement) field.getEnclosingElement();
            ElementKind classKind = encloser.getKind();
            if (classKind == ElementKind.INTERFACE || classKind == ElementKind.ANNOTATION_TYPE) {
                return createProblem(result, true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateInIntf"));
            }
            
            fieldEncloserHandle = ElementHandle.create(encloser);
            fieldAccessibility = field.getModifiers();
            fieldEncloserAccessibility = resolveVisibility(encloser);
            
            return result;
        } finally {
            fireProgressListenerStop();
        }
    }
    
    @Override
    public Problem fastCheckParameters() {
        return fastCheckParameters(refactoring.getGetterName(), refactoring.getSetterName(), refactoring.getMethodModifiers(), refactoring.getFieldModifiers(), refactoring.isAlwaysUseAccessors());
    }

    @Override
    protected Problem checkParameters(CompilationController javac) throws IOException {
        Problem p = null;
        Element field = sourceType.resolveElement(javac);
        TypeElement clazz = (TypeElement) field.getEnclosingElement();
        String getname = refactoring.getGetterName();
        String setname = refactoring.getSetterName();
        ExecutableElement getter = null;
        ExecutableElement setter = null;
        
        if (getname != null) {
            getter = findMethod(javac, clazz, getname, Collections.<VariableElement>emptyList(), true);
        }
        
        if (getter != null) {
            Types types = javac.getTypes();
            if (!types.isSameType(field.asType(), getter.getReturnType())) {
                String msg = NbBundle.getMessage(
                        EncapsulateFieldRefactoringPlugin.class,
                        "ERR_EncapsulateWrongGetter",
                        getname,
                        getter.getReturnType().toString());
                p = createProblem(p, false, msg);
            }
            if (getter.getEnclosingElement() == field.getEnclosingElement()) {
                currentGetter = ElementHandle.create(getter);
            }
        }
        
        if (setname != null) {
            setter = findMethod(javac, clazz, setname, Collections.singletonList((VariableElement) field), true);
        }
        
        if (setter != null) {
            if (TypeKind.VOID != setter.getReturnType().getKind()) {
                p = createProblem(p, false, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateWrongSetter", setname, setter.getReturnType()));
            }
            if (setter.getEnclosingElement() == field.getEnclosingElement()) {
                currentSetter = ElementHandle.create(setter);
            }
        }
        return p;
    }
    
    private Problem fastCheckParameters(String getter, String setter,
            Set<Modifier> methodModifier, Set<Modifier> fieldModifier,
            boolean alwaysUseAccessors) {
        
        if ((getter != null && !Utilities.isJavaIdentifier(getter))
                || (setter != null && !Utilities.isJavaIdentifier(setter))
                || (getter == null && setter == null)) {
            // user doesn't use valid java identifier, it cannot be used
            // as getter/setter name
            return new Problem(true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateMethods"));
        } else {
            // we have no problem :-)
            return null;
        }
    }

    private Modifier resolveVisibility(TypeElement clazz) {
        NestingKind nestingKind = clazz.getNestingKind();
        
        if (nestingKind == NestingKind.ANONYMOUS || nestingKind == NestingKind.LOCAL) {
            return Modifier.PRIVATE;
        }
        
        Set<Modifier> mods = clazz.getModifiers();
        if (nestingKind == NestingKind.TOP_LEVEL) {
            return mods.contains(Modifier.PUBLIC)
                    ? Modifier.PUBLIC
                    : null;
        }
        
        if (mods.contains(Modifier.PRIVATE)) {
            return Modifier.PRIVATE;
        
        }
        Modifier mod1 = resolveVisibility((TypeElement) clazz.getEnclosingElement());
        Modifier mod2 = null;
        if (mods.contains(Modifier.PUBLIC)) {
            mod2 = Modifier.PUBLIC;
        } else if (mods.contains(Modifier.PROTECTED)) {
            mod2 = Modifier.PROTECTED;
        }
        
        return max(mod1, mod2);
    }
    
    private Modifier max(Modifier a, Modifier b) {
        if (a == b) {
            return a;
        }
        int ai = MODIFIERS.indexOf(a);
        int bi = MODIFIERS.indexOf(b);
        return ai > bi? a: b;
    }

    private static Modifier getAccessibility(Set<Modifier> mods) {
        if (mods.isEmpty()) {
            return null;
        }
        Set<Modifier> s = new HashSet<Modifier>(mods);
        s.retainAll(accessModifiers);
        return s.isEmpty()? null: s.iterator().next();
    }

    private static Set<Modifier> replaceAccessibility(Modifier currentAccess, Modifier futureAccess, Element elm) {
        Set<Modifier> mods = new HashSet<Modifier>(elm.getModifiers());
        if (currentAccess != null) {
            mods.remove(currentAccess);
        }
        if (futureAccess != null) {
            mods.add(futureAccess);
        }
        return mods;
    }

    public static ExecutableElement findMethod(CompilationInfo javac, TypeElement clazz, String name, List<? extends VariableElement> params, boolean includeSupertypes) {
        if (name == null || name.length() == 0) {
            return null;
        }
        
        TypeElement c = clazz;
        while (true) {
            for (Element elm : c.getEnclosedElements()) {
                if (ElementKind.METHOD == elm.getKind()) {
                    ExecutableElement m = (ExecutableElement) elm;
                    if (name.contentEquals(m.getSimpleName())
                            && compareParams(params, m.getParameters())
                            && isAccessible(javac, clazz, m)) {
                        return m;
                    }
                }
            }
            
            TypeMirror superType = c.getSuperclass();
            if (!includeSupertypes || superType.getKind() == TypeKind.NONE) {
                return null;
            }
            c = (TypeElement) ((DeclaredType) superType).asElement();
        }
    }

    /**
     * returns true if elm is accessible from clazz. elm must be member of clazz
     * or its superclass
     */
    private static boolean isAccessible(CompilationInfo javac, TypeElement clazz, Element elm) {
        if (clazz == elm.getEnclosingElement()) {
            return true;
        }
        Set<Modifier> mods = elm.getModifiers();
        if (mods.contains(Modifier.PUBLIC) || mods.contains(Modifier.PROTECTED)) {
            return true;
        } else if (mods.contains(Modifier.PRIVATE)) {
            return false;
        }
        Elements utils = javac.getElements();
        return utils.getPackageOf(elm) == utils.getPackageOf(clazz);
    }
    
    private static boolean compareParams(List<? extends VariableElement> params1, List<? extends VariableElement> params2) {
        if (params1.size() == params2.size()) {
            Iterator<? extends VariableElement> it1 = params1.iterator();
            for (VariableElement ve : params2) {
                if (ve.asType() != it1.next().asType()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    /**
     * Removes the class field prefix from  the identifer of a field.
     * For example, if the class field prefix is "_", the identifier "_name" 
     * is stripped to become "name".
     * @param identifierString The identifer to strip.
     * @return The stripped identifier.
     */
    private static String stripPrefix(String identifierString){
        String stripped;
        if(identifierString.startsWith(CLASS_FIELD_PREFIX) && identifierString.length() > 1){
            stripped = identifierString.substring(CLASS_FIELD_PREFIX.length());
        }
        else{
             stripped = identifierString;
        }
        return stripped;
    }
    
    private static StringBuilder getCapitalizedName(VariableElement field) {        
        StringBuilder name = new StringBuilder(stripPrefix(field.getSimpleName().toString()));
        
        name.setCharAt(0, Character.toUpperCase(name.charAt(0)));
        return name;
    }
    
    
    public static String computeSetterName(VariableElement field) {
        StringBuilder name = getCapitalizedName(field);
        
        name.insert(0, "set"); //NOI18N
        return name.toString();
    }
    
    public static String computeGetterName(VariableElement field) {
        StringBuilder name = getCapitalizedName(field);
        
        if (TypeKind.BOOLEAN == field.asType().getKind()) { // XXX check autoboxing???
            name.insert(0, "is"); //NOI18N
        } else {
            name.insert(0, "get"); //NOI18N
        }
        
        return name.toString();
    }
    
    public Problem prepare(RefactoringElementsBag bag) {
        
        fireProgressListenerStart(AbstractRefactoring.PREPARE, 9);
        try {
            fireProgressListenerStep();
            
            EncapsulateDesc desc = prepareEncapsulator(null);
            if (desc.p != null && desc.p.isFatal()) {
                return desc.p;
            }
            
            Encapsulator encapsulator = new Encapsulator(
                    Collections.singletonList(desc), desc.p,
                    refactoring.getContext().lookup(InsertPoint.class),
                    refactoring.getContext().lookup(SortBy.class),
                    refactoring.getContext().lookup(Javadoc.class)
                    );
            
            Problem problem = createAndAddElements(
                    desc.refs,
                    new TransformTask(encapsulator, desc.fieldHandle),
                    bag, refactoring);
            
            return problem != null ? problem : encapsulator.getProblem();
        } finally {
            fireProgressListenerStop();
        }
    }
    
    EncapsulateDesc prepareEncapsulator(Problem previousProblem) {
        Set<FileObject> refs = getRelevantFiles();
        EncapsulateDesc etask = new EncapsulateDesc();

        if (refactoring.isAlwaysUseAccessors()
                && refactoring.getMethodModifiers().contains(Modifier.PRIVATE)
                // is reference fromother files?
                && refs.size() > 1) {
            // breaks code
            etask.p = createProblem(previousProblem, true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateMethodsAccess"));
            return etask;
        }
        if (refactoring.isAlwaysUseAccessors()
                // is default accessibility?
                && getAccessibility(refactoring.getMethodModifiers()) == null
                // is reference fromother files?
                && refs.size() > 1) {
            // breaks code likely
            etask.p = createProblem(previousProblem, false, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateMethodsDefaultAccess"));
        }

        etask.fieldHandle = sourceType;
        etask.refs = refs;
        etask.currentGetter = currentGetter;
        etask.currentSetter = currentSetter;
        etask.refactoring = refactoring;
        return etask;
    }
    
    private Set<FileObject> getRelevantFiles() {
        // search class index just in case Use accessors even when the field is accessible == true
        // or the field is accessible:
        // * private eclosers|private field -> CP: .java (project) => JavaSource.forFileObject
        // * default enclosers|default field -> CP: package (project)
        // * public|protected enclosers&public|protected field -> CP: project + dependencies
        Set<FileObject> refs;
        FileObject source = sourceType.getFileObject();
        if (fieldAccessibility.contains(Modifier.PRIVATE) || fieldEncloserAccessibility == Modifier.PRIVATE) {
            // search file
            refs = Collections.singleton(source);
        } else { // visible field
            ClasspathInfo cpinfo;
            if (fieldEncloserAccessibility == Modifier.PUBLIC
                    && (fieldAccessibility.contains(Modifier.PUBLIC) || fieldAccessibility.contains(Modifier.PROTECTED))) {
                // search project and dependencies
                cpinfo = RetoucheUtils.getClasspathInfoFor(true, source);
            } else {
                // search project
                cpinfo = RetoucheUtils.getClasspathInfoFor(false, source);
            }
            ClassIndex index = cpinfo.getClassIndex();
            refs = index.getResources(fieldEncloserHandle, EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE));
            if (!refs.contains(source)) {
                refs = new HashSet<FileObject>(refs);
                refs.add(source);
            }
        }
        return refs;
    }
    
    private static boolean isSubclassOf(TypeElement subclass, TypeElement superclass) {
        TypeMirror superType = subclass.getSuperclass();
        while(superType.getKind() != TypeKind.NONE) {
            TypeElement superTypeElm = (TypeElement) ((DeclaredType) superType).asElement();
            if (superclass == superTypeElm) {
                return true;
            }
            superType = superTypeElm.getSuperclass();
        }
        return false;
    }
    
    static final class Encapsulator extends RefactoringVisitor {
        
        private final FileObject sourceFile;
        private final InsertPoint insertPoint;
        private final SortBy sortBy;
        private final Javadoc javadocType;
        private Problem problem;
        private List<EncapsulateDesc> descs;
        private Map<VariableElement, EncapsulateDesc> fields;

        public Encapsulator(List<EncapsulateDesc> descs, Problem problem, InsertPoint ip, SortBy sortBy, Javadoc jd) {
            assert descs != null && descs.size() > 0;
            this.sourceFile = descs.get(0).fieldHandle.getFileObject();
            this.descs = descs;
            this.problem = problem;
            this.insertPoint = ip == null ? InsertPoint.DEFAULT : ip;
            this.sortBy = sortBy == null ? SortBy.PAIRS : sortBy;
            this.javadocType = jd == null ? Javadoc.NONE : jd;
        }

        public Problem getProblem() {
            return problem;
        }

        @Override
        public void setWorkingCopy(WorkingCopy workingCopy) throws ToPhaseException {
            super.setWorkingCopy(workingCopy);
            
            // init caches
            fields = new HashMap<VariableElement, EncapsulateDesc>(descs.size());
            for (EncapsulateDesc desc : descs) {
                desc.field = (VariableElement) desc.fieldHandle.resolveElement(workingCopy);
                fields.put(desc.field, desc);
            }
        }
        
        @Override
        public Tree visitCompilationUnit(CompilationUnitTree node, Element field) {
            return scan(node.getTypeDecls(), field);
        }
        
        @Override
        public Tree visitClass(ClassTree node, Element field) {
            TypeElement clazz = (TypeElement) workingCopy.getTrees().getElement(getCurrentPath());
            boolean[] origValues = new boolean[descs.size()];
            int counter = 0;
            for (EncapsulateDesc desc : descs) {
                origValues[counter++] = desc.useAccessors;
                desc.useAccessors = resolveUseAccessor(clazz, desc);
            }
            
            if (sourceFile == workingCopy.getFileObject()) {
                Element el = workingCopy.getTrees().getElement(getCurrentPath());
                if (el == descs.get(0).field.getEnclosingElement()) {
                    // all fields come from the same class so testing the first field should be enough
                    ClassTree nct = node;
                    List<MethodTree> newMethods = new ArrayList<MethodTree>();
                    int getterIdx = 0;
                    for (EncapsulateDesc desc : descs) {
                        MethodTree[] ms = createGetterAndSetter(
                                desc.field,
                                desc.refactoring.getGetterName(),
                                desc.refactoring.getSetterName(),
                                desc.refactoring.getMethodModifiers());
                        if (ms[0] != null) {
                            newMethods.add(getterIdx++, ms[0]);
                        }
                        if (ms[1] != null) {
                            int setterIdx = sortBy == SortBy.GETTERS_FIRST
                                    ? newMethods.size()
                                    : getterIdx++;
                            newMethods.add(setterIdx, ms[1]);
                        }
                    }
                    
                    if (!newMethods.isEmpty()) {
                        if (sortBy == SortBy.ALPHABETICALLY) {
                            Collections.sort(newMethods, new SortMethodsByNameComparator());
                        }
                        if (insertPoint == InsertPoint.DEFAULT) {
                            nct = GeneratorUtilities.get(workingCopy).insertClassMembers(node, newMethods);
                        } else {
                            List<? extends Tree> members = node.getMembers();
                            if (insertPoint.getIndex() >= members.size()) {
                                // last method
                                for (MethodTree mt : newMethods) {
                                    nct = make.addClassMember(nct, mt);
                                }
                            } else {
                                int idx = insertPoint.getIndex();
                                for (MethodTree mt : newMethods) {
                                    nct = make.insertClassMember(nct, idx++, mt);
                                }
                            }
                        }
                        rewrite(node, nct);
                    }
                }
            }
            
            Tree result = scan(node.getMembers(), field);
            counter = 0;
            for (EncapsulateDesc desc : descs) {
                desc.useAccessors = origValues[counter++];
            }
            return result;
        }
        
        private static final class SortMethodsByNameComparator implements Comparator<MethodTree> {

            public int compare(MethodTree o1, MethodTree o2) {
                String n1 = o1.getName().toString();
                String n2 = o2.getName().toString();
                return n1.compareTo(n2);
            }
            
        }
        
        @Override
        public Tree visitVariable(VariableTree node, Element field) {
            if (sourceFile == workingCopy.getFileObject()) {
                Element el = workingCopy.getTrees().getElement(getCurrentPath());
                EncapsulateDesc desc = fields.get(el);
                if (desc != null) {
                    resolveFieldDeclaration(node, desc);
                    return null;
                }
            }
            return scan(node.getInitializer(), field);
        }
        
        @Override
        public Tree visitAssignment(AssignmentTree node, Element field) {
            ExpressionTree variable = node.getVariable();
            boolean isArray = false;
            while (variable.getKind() == Tree.Kind.ARRAY_ACCESS) {
                isArray = true;
                variable = ((ArrayAccessTree) variable).getExpression();
            }
            
            Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), variable));
            EncapsulateDesc desc = fields.get(el);
            if (desc != null && desc.useAccessors && desc.refactoring.getSetterName() != null
                    // check (field = 3) == 3
                    && (isArray || checkAssignmentInsideExpression())
                    && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
                if (isArray) {
                    ExpressionTree invkgetter = createGetterInvokation(variable, desc.refactoring.getGetterName());
                    rewrite(variable, invkgetter);
                } else {
                    ExpressionTree setter = createMemberSelection(variable, desc.refactoring.getSetterName());
                    
                    // resolve types
                    Trees trees = workingCopy.getTrees();
                    ExpressionTree expTree = node.getExpression();
                    ExpressionTree newExpTree;
                    TreePath varPath = trees.getPath(workingCopy.getCompilationUnit(), variable);
                    TreePath expPath = trees.getPath(workingCopy.getCompilationUnit(), expTree);
                    TypeMirror varType = trees.getTypeMirror(varPath);
                    TypeMirror expType = trees.getTypeMirror(expPath);
                    if (workingCopy.getTypes().isSubtype(expType, varType)) {
                        newExpTree = expTree;
                    } else {
                        newExpTree = make.TypeCast(make.Type(varType), expTree);
                    }
                    
                    MethodInvocationTree invksetter = make.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(),
                            setter,
                            Collections.singletonList(newExpTree));
                    rewrite(node, invksetter);
                }
            }
            return scan(node.getExpression(), field);
        }

        @Override
        public Tree visitCompoundAssignment(CompoundAssignmentTree node, Element field) {
            ExpressionTree variable = node.getVariable();
            boolean isArray = false;
            while (variable.getKind() == Tree.Kind.ARRAY_ACCESS) {
                isArray = true;
                variable = ((ArrayAccessTree) variable).getExpression();
            }
            
            Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), variable));
            EncapsulateDesc desc = fields.get(el);
            if (desc != null && desc.useAccessors && desc.refactoring.getSetterName() != null
                    // check (field += 3) == 3
                    && (isArray || checkAssignmentInsideExpression())
                    && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
                if (isArray) {
                    ExpressionTree invkgetter = createGetterInvokation(variable, desc.refactoring.getGetterName());
                    rewrite(variable, invkgetter);
                } else {
                    ExpressionTree setter = createMemberSelection(variable, desc.refactoring.getSetterName());

                    // translate compound op to binary op; ADD_ASSIGNMENT -> ADD
                    String s = node.getKind().name();
                    s = s.substring(0, s.length() - "_ASSIGNMENT".length()); // NOI18N
                    Tree.Kind operator = Tree.Kind.valueOf(s);

                    ExpressionTree invkgetter = createGetterInvokation(variable, desc.refactoring.getGetterName());
                    
                    // resolve types
                    Trees trees = workingCopy.getTrees();
                    ExpressionTree expTree = node.getExpression();
                    ExpressionTree newExpTree;
                    TreePath varPath = trees.getPath(workingCopy.getCompilationUnit(), variable);
                    TreePath expPath = trees.getPath(workingCopy.getCompilationUnit(), expTree);
                    TypeMirror varType = trees.getTypeMirror(varPath);
                    // getter need not exist yet, use variable to resolve type of binary expression
                    ExpressionTree expTreeFake = make.Binary(operator, variable, expTree);
                    TypeMirror expType = workingCopy.getTreeUtilities().attributeTree(expTreeFake, trees.getScope(expPath));
                    
                    newExpTree = make.Binary(operator, invkgetter, expTree);
                    if (!workingCopy.getTypes().isSubtype(expType, varType)) {
                        newExpTree = make.TypeCast(make.Type(varType), make.Parenthesized(newExpTree));
                    }
                    
                    MethodInvocationTree invksetter = make.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(),
                            setter,
                            Collections.singletonList(newExpTree));
                    rewrite(node, invksetter);
                }
            }
            return scan(node.getExpression(), field);
        }

        @Override
        public Tree visitUnary(UnaryTree node, Element field) {
            ExpressionTree t = node.getExpression();
            Kind kind = node.getKind();
            boolean isArrayOrImmutable = kind != Kind.POSTFIX_DECREMENT
                    && kind != Kind.POSTFIX_INCREMENT
                    && kind != Kind.PREFIX_DECREMENT
                    && kind != Kind.PREFIX_INCREMENT;
            while (t.getKind() == Tree.Kind.ARRAY_ACCESS) {
                isArrayOrImmutable = true;
                t = ((ArrayAccessTree) t).getExpression();
            }
            Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), t));
            EncapsulateDesc desc = fields.get(el);
            if (desc != null && desc.useAccessors
                    && desc.refactoring.getGetterName() != null
                    && (isArrayOrImmutable || checkAssignmentInsideExpression())
                    && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
                // check (++field + 3)
                ExpressionTree invkgetter = createGetterInvokation(t, desc.refactoring.getGetterName());
                if (isArrayOrImmutable) {
                    rewrite(t, invkgetter);
                } else if (desc.refactoring.getSetterName() != null) {
                    ExpressionTree setter = createMemberSelection(node.getExpression(), desc.refactoring.getSetterName());

                    Tree.Kind operator = kind == Tree.Kind.POSTFIX_INCREMENT || kind == Tree.Kind.PREFIX_INCREMENT
                            ? Tree.Kind.PLUS
                            : Tree.Kind.MINUS;

                    // resolve types
                    Trees trees = workingCopy.getTrees();
                    ExpressionTree expTree = node.getExpression();
                    TreePath varPath = trees.getPath(workingCopy.getCompilationUnit(), expTree);
                    TypeMirror varType = trees.getTypeMirror(varPath);
                    TypeMirror expType = workingCopy.getTypes().getPrimitiveType(TypeKind.INT);
                    ExpressionTree newExpTree = make.Binary(operator, invkgetter, make.Literal(1));
                    if (!workingCopy.getTypes().isSubtype(expType, varType)) {
                        newExpTree = make.TypeCast(make.Type(varType), make.Parenthesized(newExpTree));
                    }

                    MethodInvocationTree invksetter = make.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(),
                            setter,
                            Collections.singletonList(newExpTree));
                    rewrite(node, invksetter);
                }
            }
            return null;
        }

        @Override
        public Tree visitMemberSelect(MemberSelectTree node, Element field) {
            Element el = workingCopy.getTrees().getElement(getCurrentPath());
            EncapsulateDesc desc = fields.get(el);
            if (desc != null && desc.useAccessors && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
                ExpressionTree nodeNew = createGetterInvokation(node, desc.refactoring.getGetterName());
                rewrite(node, nodeNew);
            }
            return super.visitMemberSelect(node, field);
        }

        @Override
        public Tree visitIdentifier(IdentifierTree node, Element field) {
            Element el = workingCopy.getTrees().getElement(getCurrentPath());
            EncapsulateDesc desc = fields.get(el);
            if (desc != null && desc.useAccessors && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
                ExpressionTree nodeNew = createGetterInvokation(node, desc.refactoring.getGetterName());
                rewrite(node, nodeNew);
            }
            return null;
        }

        private boolean checkAssignmentInsideExpression() {
            Tree exp1 = getCurrentPath().getLeaf();
            Tree parent = getCurrentPath().getParentPath().getLeaf();
            if (parent.getKind() != Tree.Kind.EXPRESSION_STATEMENT) {
                // XXX would be useful if Problems support HTML
//                String code = parent.toString();
//                String replace = exp1.toString();
//                code = code.replace(replace, "&lt;b&gt;" + replace + "&lt;/b&gt;");
                problem = createProblem(
                        problem,
                        false,
                        NbBundle.getMessage(
                                EncapsulateFieldRefactoringPlugin.class,
                                "ERR_EncapsulateInsideAssignment", // NOI18N
                                exp1.toString(),
                                parent.toString(),
                                FileUtil.getFileDisplayName(workingCopy.getFileObject())));
                return false;
            }
            return true;
        }
        
        /**
         * replace current expresion with the proper one.<p>
         * c.field -> c.getField()
         * field -> getField()
         * or copy in case of refactoring.getGetterName() == null
         */
        private ExpressionTree createGetterInvokation(ExpressionTree current, String getterName) {
            // check if exist refactoring.getGetterName() != null and visibility (subclases)
            if (getterName == null) {
                return current;
            }
            ExpressionTree getter = createMemberSelection(current, getterName);
            
            MethodInvocationTree invkgetter = make.MethodInvocation(
                    Collections.<ExpressionTree>emptyList(),
                    getter,
                    Collections.<ExpressionTree>emptyList());
            return invkgetter;
        }
        
        private ExpressionTree createMemberSelection(ExpressionTree node, String name) {
            ExpressionTree selector;
            if (node.getKind() == Tree.Kind.MEMBER_SELECT) {
                MemberSelectTree select = (MemberSelectTree) node;
                selector = make.MemberSelect(select.getExpression(), name);
            } else {
                selector = make.Identifier(name);
            }
            return selector;
        }
        
        private MethodTree[] createGetterAndSetter(
                VariableElement field, String getterName,
                String setterName, Set<Modifier> useModifiers) {
            
            String fieldName = field.getSimpleName().toString();
            boolean staticMod = field.getModifiers().contains(Modifier.STATIC);
            String parName = staticMod ? "a" + getCapitalizedName(field) : stripPrefix(fieldName); //NOI18N
            String getterBody = "{return " + fieldName + ";}"; //NOI18N
            String setterBody = (staticMod? "{": "{this.") + fieldName + " = " + parName + ";}"; //NOI18N
            
            Set<Modifier> mods = new HashSet<Modifier>(useModifiers);
            if (staticMod) {
                mods.add(Modifier.STATIC);
            }
            
            VariableTree fieldTree = (VariableTree) workingCopy.getTrees().getTree(field);
            MethodTree[] result = new MethodTree[2];

            ExecutableElement getterElm = null;
            if (getterName != null) {
                getterElm = findMethod(
                        workingCopy,
                        (TypeElement) field.getEnclosingElement(),
                        getterName,
                        Collections.<VariableElement>emptyList(), false);
            }
            if (getterElm == null && getterName != null) {
                MethodTree getter = make.Method(
                        make.Modifiers(mods),
                        getterName,
                        fieldTree.getType(),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.<VariableTree>emptyList(),
                        Collections.<ExpressionTree>emptyList(),
                        getterBody,
                        null);
                result[0] = getter;
                String jdText = null;
                if (javadocType == Javadoc.COPY) {
                    jdText = workingCopy.getElements().getDocComment(field);
                    jdText = trimNewLines(jdText);
                }
                if (javadocType == Javadoc.DEFAULT || javadocType == Javadoc.COPY) {
                    String prefix = jdText == null ? "" : jdText + "\n"; // NOI18N
                    Comment comment = Comment.create(
                            Comment.Style.JAVADOC, -2, -2, -2,
                            prefix + "@return the " + field.getSimpleName()); // NOI18N
                    make.addComment(getter, comment, true);
                }
            }
            
            ExecutableElement setterElm = null;
            if (setterName != null) {
                setterElm = findMethod(
                        workingCopy,
                        (TypeElement) field.getEnclosingElement(),
                        setterName,
                        Collections.<VariableElement>singletonList(field), false);
            }
            if (setterElm == null && setterName != null) {
                VariableTree paramTree = make.Variable(
                        make.Modifiers(Collections.<Modifier>emptySet()), parName, fieldTree.getType(), null);
                MethodTree setter = make.Method(
                        make.Modifiers(mods),
                        setterName,
                        make.PrimitiveType(TypeKind.VOID),
                        Collections.<TypeParameterTree>emptyList(),
                        Collections.singletonList(paramTree),
                        Collections.<ExpressionTree>emptyList(),
                        setterBody,
                        null);
                result[1] = setter;
                
                String jdText = null;
                if (javadocType == Javadoc.COPY) {
                    jdText = workingCopy.getElements().getDocComment(field);
                    jdText = trimNewLines(jdText);
                }
                if (javadocType == Javadoc.DEFAULT || javadocType == Javadoc.COPY) {
                    String prefix = jdText == null ? "" : jdText + "\n"; // NOI18N
                    Comment comment = Comment.create(
                            Comment.Style.JAVADOC, -2, -2, -2,
                            prefix + String.format("@param %s the %s to set", parName, fieldName)); // NOI18N
                    make.addComment(setter, comment, true);
                }
            }
            
            return result;
        }
        
        private String trimNewLines(String javadoc) {
            if (javadoc == null) {
                return null;
            }
            
            int len = javadoc.length();
            int st = 0;
            int off = 0;      /* avoid getfield opcode */
            char[] val = javadoc.toCharArray();    /* avoid getfield opcode */

            while ((st < len) && Character.isWhitespace(val[off + st])/* && (val[off + st] <= '\n')*/) {
                st++;
            }
            while ((st < len) && Character.isWhitespace(val[off + len - 1])/*val[off + len - 1] <= '\n')*/) {
                len--;
            }
            return ((st > 0) || (len < val.length)) ? javadoc.substring(st, len) : javadoc;
        }
        
        private void resolveFieldDeclaration(VariableTree node, EncapsulateDesc desc) {
            Modifier currentAccess = getAccessibility(desc.field.getModifiers());
            Modifier futureAccess = getAccessibility(desc.refactoring.getFieldModifiers());
            ModifiersTree newModTree = null;
            if (currentAccess != futureAccess) {
                newModTree = make.Modifiers(
                        replaceAccessibility(currentAccess, futureAccess, desc.field),
                        node.getModifiers().getAnnotations());
            }
            
            if (node.getModifiers().getFlags().contains(Modifier.FINAL)
                    && desc.refactoring.getSetterName() != null) {
                // remove final flag in case user wants to create setter
                ModifiersTree mot = newModTree == null ? node.getModifiers(): newModTree;
                Set<Modifier> flags = new HashSet<Modifier>(mot.getFlags());
                flags.remove(Modifier.FINAL);
                newModTree = make.Modifiers(flags, mot.getAnnotations());
            }
            
            if (newModTree != null) {
                VariableTree newNode = make.Variable(
                        newModTree, node.getName(), node.getType(), node.getInitializer());
                rewrite(node, newNode);
            }
        }
        
        private boolean resolveUseAccessor(TypeElement where, EncapsulateDesc desc) {
            if (desc.refactoring.isAlwaysUseAccessors()) {
                return true;
            }
            
            // target field accessibility
            Set<Modifier> mods = desc.refactoring.getFieldModifiers();
            if (mods.contains(Modifier.PRIVATE)) {
                // check enclosing top level class
                // return SourceUtils.getOutermostEnclosingTypeElement(where) != SourceUtils.getOutermostEnclosingTypeElement(desc.field);
                return where != desc.field.getEnclosingElement();
            }
            
            if (mods.contains(Modifier.PROTECTED)) {
                // check inheritance
                if (isSubclassOf(where, (TypeElement) desc.field.getEnclosingElement())) {
                    return false;
                }
                // check same package
                return workingCopy.getElements().getPackageOf(where) != workingCopy.getElements().getPackageOf(desc.field);
            }
            
            if (mods.contains(Modifier.PUBLIC)) {
                return false;
            }
            
            // default access
            // check same package
            return workingCopy.getElements().getPackageOf(where) != workingCopy.getElements().getPackageOf(desc.field);
        }

        private boolean isInConstructorOfFieldClass(TreePath path, Element field) {
            Tree leaf = path.getLeaf();
            Kind kind = leaf.getKind();
            while (true) {
                switch (kind) {
                case METHOD:
                    if (workingCopy.getTreeUtilities().isSynthetic(path)) {
                        return false;
                    }
                    Element m = workingCopy.getTrees().getElement(path);
                    return m.getKind() == ElementKind.CONSTRUCTOR
                            && (m.getEnclosingElement() == field.getEnclosingElement()
                                || isSubclassOf((TypeElement) m.getEnclosingElement(), (TypeElement) field.getEnclosingElement()));
                case COMPILATION_UNIT:
                case CLASS:
                case NEW_CLASS:
                    return false;
                }
                path = path.getParentPath();
                leaf = path.getLeaf();
                kind = leaf.getKind();
            }
        }

        private boolean isInGetterSetter(
                TreePath path,
                ElementHandle<ExecutableElement> currentGetter,
                ElementHandle<ExecutableElement> currentSetter) {
            
            if (sourceFile != workingCopy.getFileObject()) {
                return false;
            }
            
            Tree leaf = path.getLeaf();
            Kind kind = leaf.getKind();
            while (true) {
                switch (kind) {
                case METHOD:
                    if (workingCopy.getTreeUtilities().isSynthetic(path)) {
                        return false;
                    }
                    Element m = workingCopy.getTrees().getElement(path);
                    return currentGetter != null && m == currentGetter.resolve(workingCopy)
                            || currentSetter != null && m == currentSetter.resolve(workingCopy);
                case COMPILATION_UNIT:
                case CLASS:
                case NEW_CLASS:
                    return false;
                }
                path = path.getParentPath();
                leaf = path.getLeaf();
                kind = leaf.getKind();
            }
        }
        
    }
    
    /**
     * A descriptor of the encapsulated field for Encapsulator.
     */
    static final class EncapsulateDesc {
        Problem p;
        Set<FileObject> refs;
        TreePathHandle fieldHandle;
        
        // following fields are used solely by Encapsulator
        VariableElement field;
        private ElementHandle<ExecutableElement> currentGetter;
        private ElementHandle<ExecutableElement> currentSetter;
        private EncapsulateFieldRefactoring refactoring;
        private boolean useAccessors;
    }
    
}
