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
package org.netbeans.modules.cnd.refactoring.plugins;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.api.EncapsulateFieldRefactoring;
import org.netbeans.modules.cnd.refactoring.support.DeclarationGenerator;
import org.netbeans.modules.cnd.refactoring.support.GeneratorUtils;
import org.netbeans.modules.cnd.refactoring.support.GeneratorUtils.InsertInfo;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult.Difference;
import org.netbeans.modules.cnd.refactoring.ui.EncapsulateFieldPanel.InsertPoint;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionRef;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Hurka
 * @author Jan Becicka
 * @author Jan Pokorsky
 */
public final class EncapsulateFieldRefactoringPlugin extends CsmModificationRefactoringPlugin {
    
    private static final Logger LOG = Logger.getLogger(EncapsulateFieldRefactoringPlugin.class.getName());

    // objects affected by refactoring
    private Collection<CsmObject> referencedObjects;
    
    private CsmClass fieldEncloser;
    /**
     * most restrictive accessibility modifier on tree path 
     */
    private CsmVisibility fieldEncloserAccessibility = CsmVisibility.PUBLIC;
    /**
     * present accessibility of field
     */
    private CsmVisibility fieldAccessibility;
    private CsmMethod currentGetter;
    private CsmMethod currentSetter;
    private static Set<CsmVisibility> accessModifiers = EnumSet.of(CsmVisibility.PRIVATE, CsmVisibility.PROTECTED, CsmVisibility.PUBLIC);
    private static List<CsmVisibility> MODIFIERS = Arrays.asList(CsmVisibility.PRIVATE, null, CsmVisibility.PROTECTED, CsmVisibility.PUBLIC);
    private final EncapsulateFieldRefactoring refactoring;
    public static final String CLASS_FIELD_PREFIX = "_"; // NOI18N
    /**
     * path in source with field declaration; refactoring.getSelectedObject()
     * may contain path to a reference
     */
    private CsmObject sourceType;
    
    /** Creates a new instance of RenameRefactoring */
    public EncapsulateFieldRefactoringPlugin(EncapsulateFieldRefactoring refactoring) {
        super(refactoring);
        this.refactoring = refactoring;
    }
    
    @Override
    public Problem preCheck() {
        fireProgressListenerStart(AbstractRefactoring.PRE_CHECK, 2);
        try {
            CsmField field = refactoring.getSourceField();
            Problem result = checkIfModificationPossible(null, field, "", ""); // NOI18N
            if (result != null) {
                return result;
            }
            fireProgressListenerStep();
//            if (ElementKind.FIELD == field.getKind()) {
//               TreePath tp = javac.getTrees().getPath(field);
//               sourceType = TreePathHandle.create(tp, javac);
//            } else {
//                return createProblem(result, true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateWrongType"));
//            }
//            if (!RetoucheUtils.isElementInOpenProject(sourceType.getFileObject())) {
//                return new Problem(true, NbBundle.getMessage(EncapsulateFieldRefactoring.class, "ERR_ProjectNotOpened"));
//            }
//
            fieldEncloser = field.getContainingClass();
            fieldAccessibility = field.getVisibility();
//
//            fieldAccessibility = field.getModifiers();
//            fieldEncloserAccessibility = resolveVisibility(fieldEncloser);
//
            return result;
        } finally {
            fireProgressListenerStop();
        }
    }
    
    @Override
    public Problem fastCheckParameters() {
        return fastCheckParameters(refactoring.getGetterName(), refactoring.getSetterName());
    }

    @Override
    public Problem checkParameters() {
        Problem p = null;
        CsmField field = refactoring.getSourceField();
        CsmClass clazz = field.getContainingClass();
        String getname = refactoring.getGetterName();
        String setname = refactoring.getSetterName();
        CsmMethod getter = null;
        CsmMethod setter = null;

        if (getname != null) {
            getter = findMethod(clazz, getname, Collections.<CsmVariable>emptyList(), true);
        }

        if (getter != null) {
            if (!GeneratorUtils.isSameType(field.getType(), getter.getReturnType())) {
                String msg = NbBundle.getMessage(
                        EncapsulateFieldRefactoringPlugin.class,
                        "ERR_EncapsulateWrongGetter", // NOI18N
                        getname,
                        getter.getReturnType().getCanonicalText());
                p = createProblem(p, false, msg);
            }
            if (clazz.equals(getter.getContainingClass())) {
                currentGetter = getter;
            }
        }

        if (setname != null) {
            setter = findMethod(clazz, setname, Collections.singletonList(field), true);
        }

        if (setter != null) {
            if (GeneratorUtils.getTypeKind(setter.getReturnType()) != GeneratorUtils.TypeKind.VOID) {
                p = createProblem(p, false, NbBundle.getMessage(
                        EncapsulateFieldRefactoringPlugin.class,
                        "ERR_EncapsulateWrongSetter", // NOI18N
                        setname,
                        setter.getReturnType().getCanonicalText()));
            }

            if (clazz.equals(setter.getContainingClass())) {
                currentSetter = setter;
            }
        }
        return p;
    }
    
    private Problem fastCheckParameters(String getter, String setter) {
        
        if ((getter != null && !Utilities.isJavaIdentifier(getter))
                || (setter != null && !Utilities.isJavaIdentifier(setter))
                || (getter == null && setter == null)) {
            // user doesn't use valid java identifier, it cannot be used
            // as getter/setter name
            return new Problem(true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateMethods"));
        } else {
            // we have no problems
            return null;
        }
    }

//    private CsmVisibility resolveVisibility(CsmClass clazz) {
//        NestingKind nestingKind = clazz.getNestingKind();
//
//        if (nestingKind == NestingKind.ANONYMOUS || nestingKind == NestingKind.LOCAL) {
//            return CsmVisibility.PRIVATE;
//        }
//
//        Set<CsmVisibility> mods = clazz.getModifiers();
//        if (nestingKind == NestingKind.TOP_LEVEL) {
//            return mods.contains(CsmVisibility.PUBLIC)
//                    ? CsmVisibility.PUBLIC
//                    : null;
//        }
//
//        if (mods.contains(CsmVisibility.PRIVATE)) {
//            return CsmVisibility.PRIVATE;
//
//        }
//        CsmVisibility mod1 = resolveVisibility((CsmClass) clazz.getEnclosingElement());
//        CsmVisibility mod2 = null;
//        if (mods.contains(CsmVisibility.PUBLIC)) {
//            mod2 = CsmVisibility.PUBLIC;
//        } else if (mods.contains(CsmVisibility.PROTECTED)) {
//            mod2 = CsmVisibility.PROTECTED;
//        }
//
//        return max(mod1, mod2);
//    }
//
//    private CsmVisibility max(CsmVisibility a, CsmVisibility b) {
//        if (a == b) {
//            return a;
//        }
//        int ai = MODIFIERS.indexOf(a);
//        int bi = MODIFIERS.indexOf(b);
//        return ai > bi? a: b;
//    }
//
//    private static CsmVisibility getAccessibility(Set<CsmVisibility> mods) {
//        if (mods.isEmpty()) {
//            return null;
//        }
//        Set<CsmVisibility> s = new HashSet<CsmVisibility>(mods);
//        s.retainAll(accessModifiers);
//        return s.isEmpty()? null: s.iterator().next();
//    }
//
//    private static Set<CsmVisibility> replaceAccessibility(CsmVisibility currentAccess, CsmVisibility futureAccess, Element elm) {
//        Set<CsmVisibility> mods = new HashSet<CsmVisibility>(elm.getModifiers());
//        if (currentAccess != null) {
//            mods.remove(currentAccess);
//        }
//        if (futureAccess != null) {
//            mods.add(futureAccess);
//        }
//        return mods;
//    }
//
    public static CsmMethod findMethod(CsmClass clazz, String name, Collection<? extends CsmVariable> params, boolean includeSupertypes) {
        if (name == null || name.length() == 0) {
            return null;
        }

        CsmClass c = clazz;
//        while (true) {
            for (CsmMember elm : c.getMembers()) {
                if (CsmKindUtilities.isMethod(elm)) {
                    CsmMethod m = (CsmMethod) elm;
                    if (name.contentEquals(m.getName())
                            && compareParams(params, m.getParameters())
                            /*&& isAccessible(clazz, m)*/) {
                        return m;
                    }
                }
            }
//
//            TypeMirror superType = c.getSuperclass();
//            if (!includeSupertypes || superType.getKind() == TypeKind.NONE) {
                return null;
//            }
//            c = (CsmClass) ((DeclaredType) superType).asElement();
//        }
    }

    /**
     * returns true if elm is accessible from clazz. elm must be member of clazz
     * or its superclass
     */
//    private static boolean isAccessible(CompilationInfo javac, CsmClass clazz, Element elm) {
//        if (clazz == elm.getEnclosingElement()) {
//            return true;
//        }
//        Set<CsmVisibility> mods = elm.getModifiers();
//        if (mods.contains(CsmVisibility.PUBLIC) || mods.contains(CsmVisibility.PROTECTED)) {
//            return true;
//        } else if (mods.contains(CsmVisibility.PRIVATE)) {
//            return false;
//        }
//        Elements utils = javac.getElements();
//        return utils.getPackageOf(elm) == utils.getPackageOf(clazz);
//    }
    
    private static boolean compareParams(Collection<? extends CsmVariable> params1, Collection<? extends CsmVariable> params2) {
        if (params1.size() == params2.size()) {
            Iterator<? extends CsmVariable> it1 = params1.iterator();
            for (CsmVariable ve : params2) {
                if ((ve.getType() == null && it1.next().getType() == null)
                  || (!ve.getType().equals(it1.next().getType()))){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected Collection<CsmFile> getRefactoredFiles() {
        return Collections.emptySet();
    }

//    @Override
//    protected Collection<CsmObject> getRefactoredObjects() {
//        return referencedObjects;
//    }

    @Override
    protected void processFile(CsmFile csmFile, ModificationResult mr, AtomicReference<Problem> outProblem) {
        // add declaration/definition to file
        InsertPoint insPt = refactoring.getContext().lookup(InsertPoint.class);
        CsmField field = refactoring.getSourceField();
        CsmFile classDeclarationFile = refactoring.getClassDeclarationFile();
        CsmFile classDefinitionFile = refactoring.getClassDefinitionFile();
        CloneableEditorSupport ces = null;
        FileObject fo = null;

        final String getterName = refactoring.getGetterName();
        final String setterName = refactoring.getSetterName();
        // prepare to generate declaration/definition
        if ((getterName != null || setterName != null) && (csmFile.equals(classDeclarationFile) || csmFile.equals(classDefinitionFile))) {
            fo = CsmUtilities.getFileObject(csmFile);
            CsmClass enclosing = refactoring.getEnclosingClass();
            InsertInfo[] insertPositons = GeneratorUtils.getInsertPositons(null, enclosing, insPt);
            if (csmFile.equals(classDeclarationFile)) {
                DeclarationGenerator.Kind declKind = refactoring.isMethodInline() ? DeclarationGenerator.Kind.INLINE_DEFINITION : DeclarationGenerator.Kind.DECLARATION;
                // create declaration
                InsertInfo declInsert = insertPositons[0];
                final StringBuilder text = new StringBuilder();
                if (getterName != null) {
                    text.append(DeclarationGenerator.createGetter(field, getterName, declKind));
                }
                if (setterName != null) {
                    text.append(DeclarationGenerator.createSetter(field, setterName, declKind));
                }
                ces = declInsert.ces;
                String indent = getIndent(csmFile, ces.getDocument(), declInsert.start);
                String declText = getFormattedText(csmFile, text.toString(), indent);
                String descr = NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "EncapsulateFieldInsertDeclartion"); // NOI8N
                Difference declDiff = new Difference(Difference.Kind.INSERT, declInsert.start, declInsert.end, "", declText, descr); // NOI18N
                mr.addDifference(fo, declDiff);
            }
            if (!refactoring.isMethodInline() && csmFile.equals(classDefinitionFile)) {
                // create definition
                DeclarationGenerator.Kind defKind = DeclarationGenerator.Kind.EXTERNAL_DEFINITION;
                InsertInfo defInsert = insertPositons[1];
                final StringBuilder text = new StringBuilder();
                if (getterName != null) {
                    text.append(DeclarationGenerator.createGetter(field, getterName, defKind));
                }
                if (setterName != null) {
                    text.append(DeclarationGenerator.createSetter(field, setterName, defKind));
                }
                ces = defInsert.ces;
                String indent = getIndent(csmFile, ces.getDocument(), defInsert.start);
                String declText = getFormattedText(csmFile, text.toString(), indent);
                String descr = NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "EncapsulateFieldInsertDefinition"); // NOI8N
                Difference declDiff = new Difference(Difference.Kind.INSERT, defInsert.start, defInsert.end, "", declText, descr); // NOI18N
                mr.addDifference(fo, declDiff);
            }
            if (refactoring.isAlwaysUseAccessors()) {
                // change all references
            }
        }
//        SortBy sortBy = refactoring.getContext().lookup(SortBy.class);
        // replace all useges
    }

    private String getFormattedText(CsmFile csmFile, String content, String indent) {
        try {
            FileSystem fs = FileUtil.createMemoryFileSystem();
            FileObject root = fs.getRoot();
            FileObject data = FileUtil.createData(root, csmFile.getAbsolutePath().toString());
            Writer writer = new OutputStreamWriter(data.getOutputStream());
            try {
                writer.append(content);
                writer.flush();
            } finally {
                writer.close();
            }
            DataObject dob = DataObject.find(data);
            EditorCookie ec = dob.getCookie(EditorCookie.class);
            if (ec != null) {
                StyledDocument doc = ec.openDocument();
                Reformat fmt = Reformat.get(doc);
                fmt.lock();
                try {
                    try {
                        fmt.reformat(0, doc.getLength());
                    } catch (BadLocationException ex) {
                    }
                } finally {
                    fmt.unlock();
                }
                final String text = doc.getText(0, doc.getLength());
                StringBuilder declText = new StringBuilder("\n"); // NOI18N
                final int len = text.length();
                for (int i = 0; i < len; i++) {
                    final char charAt = text.charAt(i);                    
                    if (charAt == '\n') { // NOI18N
                        if (i < len - 1) {
                            declText.append(charAt);
                            declText.append(indent);
                        }
                    } else {
                        declText.append(charAt);
                    }
                }
                return declText.toString();
            }
        } catch (BadLocationException ex) {
        } catch (IOException ex) {
        }
        return content;
    }

    private String getIndent(CsmFile file, StyledDocument document, PositionRef start) {
        return file.isHeaderFile() ? "    " : "";// NOI18N
    }
//    @Override
//    public Problem prepare(RefactoringElementsBag bag) {
//
//        fireProgressListenerStart(AbstractRefactoring.PREPARE, 9);
//        try {
//            fireProgressListenerStep();
//
//            EncapsulateDesc desc = prepareEncapsulator(null);
//            if (desc.p != null && desc.p.isFatal()) {
//                return desc.p;
//            }
//
//            Encapsulator encapsulator = new Encapsulator(
//                    Collections.singletonList(desc), desc.p,
//                    refactoring.getContext().lookup(InsertPoint.class),
//                    refactoring.getContext().lookup(SortBy.class),
//                    refactoring.getContext().lookup(Javadoc.class)
//                    );
//
//            Problem problem = createAndAddElements(
//                    desc.refs,
//                    new TransformTask(encapsulator, desc.fieldHandle),
//                    bag, refactoring);
//
//            return problem != null ? problem : encapsulator.getProblem();
//        } finally {
//            fireProgressListenerStop();
//        }
//    }
    
//    EncapsulateDesc prepareEncapsulator(Problem previousProblem) {
//        Set<FileObject> refs = getRelevantFiles();
//        EncapsulateDesc etask = new EncapsulateDesc();
//
//        if (refactoring.isAlwaysUseAccessors()
//                && refactoring.getMethodModifiers().contains(CsmVisibility.PRIVATE)
//                // is reference fromother files?
//                && refs.size() > 1) {
//            // breaks code
//            etask.p = createProblem(previousProblem, true, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateMethodsAccess"));
//            return etask;
//        }
//        if (refactoring.isAlwaysUseAccessors()
//                // is default accessibility?
//                && getAccessibility(refactoring.getMethodModifiers()) == null
//                // is reference fromother files?
//                && refs.size() > 1) {
//            // breaks code likely
//            etask.p = createProblem(previousProblem, false, NbBundle.getMessage(EncapsulateFieldRefactoringPlugin.class, "ERR_EncapsulateMethodsDefaultAccess"));
//        }
//
//        etask.fieldHandle = sourceType;
//        etask.refs = refs;
//        etask.currentGetter = currentGetter;
//        etask.currentSetter = currentSetter;
//        etask.refactoring = refactoring;
//        return etask;
//    }
    
//    private Set<FileObject> getRelevantFiles() {
//        // search class index just in case Use accessors even when the field is accessible == true
//        // or the field is accessible:
//        // * private eclosers|private field -> CP: .java (project) => JavaSource.forFileObject
//        // * default enclosers|default field -> CP: package (project)
//        // * public|protected enclosers&public|protected field -> CP: project + dependencies
//        Set<FileObject> refs;
//        FileObject source = sourceType.getFileObject();
//        if (fieldAccessibility.contains(CsmVisibility.PRIVATE) || fieldEncloserAccessibility == CsmVisibility.PRIVATE) {
//            // search file
//            refs = Collections.singleton(source);
//        } else { // visible field
//            ClasspathInfo cpinfo;
//            if (fieldEncloserAccessibility == CsmVisibility.PUBLIC
//                    && (fieldAccessibility.contains(CsmVisibility.PUBLIC) || fieldAccessibility.contains(CsmVisibility.PROTECTED))) {
//                // search project and dependencies
//                cpinfo = RetoucheUtils.getClasspathInfoFor(true, source);
//            } else {
//                // search project
//                cpinfo = RetoucheUtils.getClasspathInfoFor(false, source);
//            }
//            ClassIndex index = cpinfo.getClassIndex();
//            refs = index.getResources(fieldEncloserHandle, EnumSet.of(ClassIndex.SearchKind.FIELD_REFERENCES), EnumSet.of(ClassIndex.SearchScope.SOURCE));
//            if (!refs.contains(source)) {
//                refs = new HashSet<FileObject>(refs);
//                refs.add(source);
//            }
//        }
//        return refs;
//    }
    
//    private static boolean isSubclassOf(CsmClass subclass, CsmClass superclass) {
//        TypeMirror superType = subclass.getSuperclass();
//        while(superType.getKind() != TypeKind.NONE) {
//            CsmClass superTypeElm = (CsmClass) ((DeclaredType) superType).asElement();
//            if (superclass == superTypeElm) {
//                return true;
//            }
//            superType = superTypeElm.getSuperclass();
//        }
//        return false;
//    }
    
//    static final class Encapsulator extends RefactoringVisitor {
//
//        private final FileObject sourceFile;
//        private final InsertPoint insertPoint;
//        private final SortBy sortBy;
//        private final Javadoc javadocType;
//        private Problem problem;
//        private List<EncapsulateDesc> descs;
//        private Map<CsmField, EncapsulateDesc> fields;
//
//        public Encapsulator(List<EncapsulateDesc> descs, Problem problem, InsertPoint ip, SortBy sortBy, Javadoc jd) {
//            assert descs != null && descs.size() > 0;
//            this.sourceFile = descs.get(0).fieldHandle.getFileObject();
//            this.descs = descs;
//            this.problem = problem;
//            this.insertPoint = ip == null ? InsertPoint.DEFAULT : ip;
//            this.sortBy = sortBy == null ? SortBy.PAIRS : sortBy;
//            this.javadocType = jd == null ? Javadoc.NONE : jd;
//        }
//
//        public Problem getProblem() {
//            return problem;
//        }
//
//        @Override
//        public void setWorkingCopy(WorkingCopy workingCopy) throws ToPhaseException {
//            super.setWorkingCopy(workingCopy);
//
//            // init caches
//            fields = new HashMap<CsmField, EncapsulateDesc>(descs.size());
//            for (EncapsulateDesc desc : descs) {
//                desc.field = (CsmField) desc.fieldHandle.resolveElement(workingCopy);
//                fields.put(desc.field, desc);
//            }
//        }
//
//        @Override
//        public Tree visitCompilationUnit(CompilationUnitTree node, Element field) {
//            return scan(node.getTypeDecls(), field);
//        }
//
//        @Override
//        public Tree visitClass(ClassTree node, Element field) {
//            CsmClass clazz = (CsmClass) workingCopy.getTrees().getElement(getCurrentPath());
//            boolean[] origValues = new boolean[descs.size()];
//            int counter = 0;
//            for (EncapsulateDesc desc : descs) {
//                origValues[counter++] = desc.useAccessors;
//                desc.useAccessors = resolveUseAccessor(clazz, desc);
//            }
//
//            if (sourceFile == workingCopy.getFileObject()) {
//                Element el = workingCopy.getTrees().getElement(getCurrentPath());
//                if (el == descs.get(0).field.getEnclosingElement()) {
//                    // all fields come from the same class so testing the first field should be enough
//                    ClassTree nct = node;
//                    List<MethodTree> newMethods = new ArrayList<MethodTree>();
//                    int getterIdx = 0;
//                    for (EncapsulateDesc desc : descs) {
//                        MethodTree[] ms = createGetterAndSetter(
//                                desc.field,
//                                desc.refactoring.getGetterName(),
//                                desc.refactoring.getSetterName(),
//                                desc.refactoring.getMethodModifiers());
//                        if (ms[0] != null) {
//                            newMethods.add(getterIdx++, ms[0]);
//                        }
//                        if (ms[1] != null) {
//                            int setterIdx = sortBy == SortBy.GETTERS_FIRST
//                                    ? newMethods.size()
//                                    : getterIdx++;
//                            newMethods.add(setterIdx, ms[1]);
//                        }
//                    }
//
//                    if (!newMethods.isEmpty()) {
//                        if (sortBy == SortBy.ALPHABETICALLY) {
//                            Collections.sort(newMethods, new SortMethodsByNameComparator());
//                        }
//                        if (insertPoint == InsertPoint.DEFAULT) {
//                            nct = GeneratorUtilities.get(workingCopy).insertClassMembers(node, newMethods);
//                        } else {
//                            List<? extends Tree> members = node.getMembers();
//                            if (insertPoint.getIndex() >= members.size()) {
//                                // last method
//                                for (MethodTree mt : newMethods) {
//                                    nct = make.addClassMember(nct, mt);
//                                }
//                            } else {
//                                int idx = insertPoint.getIndex();
//                                for (MethodTree mt : newMethods) {
//                                    nct = make.insertClassMember(nct, idx++, mt);
//                                }
//                            }
//                        }
//                        rewrite(node, nct);
//                    }
//                }
//            }
//
//            Tree result = scan(node.getMembers(), field);
//            counter = 0;
//            for (EncapsulateDesc desc : descs) {
//                desc.useAccessors = origValues[counter++];
//            }
//            return result;
//        }
//
//        private static final class SortMethodsByNameComparator implements Comparator<MethodTree> {
//
//            public int compare(MethodTree o1, MethodTree o2) {
//                String n1 = o1.getName().toString();
//                String n2 = o2.getName().toString();
//                return n1.compareTo(n2);
//            }
//
//        }
//
//        @Override
//        public Tree visitVariable(VariableTree node, Element field) {
//            if (sourceFile == workingCopy.getFileObject()) {
//                Element el = workingCopy.getTrees().getElement(getCurrentPath());
//                EncapsulateDesc desc = fields.get(el);
//                if (desc != null) {
//                    resolveFieldDeclaration(node, desc);
//                    return null;
//                }
//            }
//            return scan(node.getInitializer(), field);
//        }
//
//        @Override
//        public Tree visitAssignment(AssignmentTree node, Element field) {
//            ExpressionTree variable = node.getVariable();
//            boolean isArray = false;
//            while (variable.getKind() == Tree.Kind.ARRAY_ACCESS) {
//                isArray = true;
//                variable = ((ArrayAccessTree) variable).getExpression();
//            }
//
//            Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), variable));
//            EncapsulateDesc desc = fields.get(el);
//            if (desc != null && desc.useAccessors && desc.refactoring.getSetterName() != null
//                    // check (field = 3) == 3
//                    && (isArray || checkAssignmentInsideExpression())
//                    && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
//                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
//                if (isArray) {
//                    ExpressionTree invkgetter = createGetterInvokation(variable, desc.refactoring.getGetterName());
//                    rewrite(variable, invkgetter);
//                } else {
//                    ExpressionTree setter = createMemberSelection(variable, desc.refactoring.getSetterName());
//
//                    // resolve types
//                    Trees trees = workingCopy.getTrees();
//                    ExpressionTree expTree = node.getExpression();
//                    ExpressionTree newExpTree;
//                    TreePath varPath = trees.getPath(workingCopy.getCompilationUnit(), variable);
//                    TreePath expPath = trees.getPath(workingCopy.getCompilationUnit(), expTree);
//                    TypeMirror varType = trees.getTypeMirror(varPath);
//                    TypeMirror expType = trees.getTypeMirror(expPath);
//                    if (workingCopy.getTypes().isSubtype(expType, varType)) {
//                        newExpTree = expTree;
//                    } else {
//                        newExpTree = make.TypeCast(make.Type(varType), expTree);
//                    }
//
//                    MethodInvocationTree invksetter = make.MethodInvocation(
//                            Collections.<ExpressionTree>emptyList(),
//                            setter,
//                            Collections.singletonList(newExpTree));
//                    rewrite(node, invksetter);
//                }
//            }
//            return scan(node.getExpression(), field);
//        }
//
//        @Override
//        public Tree visitCompoundAssignment(CompoundAssignmentTree node, Element field) {
//            ExpressionTree variable = node.getVariable();
//            boolean isArray = false;
//            while (variable.getKind() == Tree.Kind.ARRAY_ACCESS) {
//                isArray = true;
//                variable = ((ArrayAccessTree) variable).getExpression();
//            }
//
//            Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), variable));
//            EncapsulateDesc desc = fields.get(el);
//            if (desc != null && desc.useAccessors && desc.refactoring.getSetterName() != null
//                    // check (field += 3) == 3
//                    && (isArray || checkAssignmentInsideExpression())
//                    && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
//                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
//                if (isArray) {
//                    ExpressionTree invkgetter = createGetterInvokation(variable, desc.refactoring.getGetterName());
//                    rewrite(variable, invkgetter);
//                } else {
//                    ExpressionTree setter = createMemberSelection(variable, desc.refactoring.getSetterName());
//
//                    // translate compound op to binary op; ADD_ASSIGNMENT -> ADD
//                    String s = node.getKind().name();
//                    s = s.substring(0, s.length() - "_ASSIGNMENT".length()); // NOI18N
//                    Tree.Kind operator = Tree.Kind.valueOf(s);
//
//                    ExpressionTree invkgetter = createGetterInvokation(variable, desc.refactoring.getGetterName());
//
//                    // resolve types
//                    Trees trees = workingCopy.getTrees();
//                    ExpressionTree expTree = node.getExpression();
//                    ExpressionTree newExpTree;
//                    TreePath varPath = trees.getPath(workingCopy.getCompilationUnit(), variable);
//                    TreePath expPath = trees.getPath(workingCopy.getCompilationUnit(), expTree);
//                    TypeMirror varType = trees.getTypeMirror(varPath);
//                    // getter need not exist yet, use variable to resolve type of binary expression
//                    ExpressionTree expTreeFake = make.Binary(operator, variable, expTree);
//                    TypeMirror expType = workingCopy.getTreeUtilities().attributeTree(expTreeFake, trees.getScope(expPath));
//
//                    newExpTree = make.Binary(operator, invkgetter, expTree);
//                    if (!workingCopy.getTypes().isSubtype(expType, varType)) {
//                        newExpTree = make.TypeCast(make.Type(varType), make.Parenthesized(newExpTree));
//                    }
//
//                    MethodInvocationTree invksetter = make.MethodInvocation(
//                            Collections.<ExpressionTree>emptyList(),
//                            setter,
//                            Collections.singletonList(newExpTree));
//                    rewrite(node, invksetter);
//                }
//            }
//            return scan(node.getExpression(), field);
//        }
//
//        @Override
//        public Tree visitUnary(UnaryTree node, Element field) {
//            ExpressionTree t = node.getExpression();
//            Kind kind = node.getKind();
//            boolean isArrayOrImmutable = kind != Kind.POSTFIX_DECREMENT
//                    && kind != Kind.POSTFIX_INCREMENT
//                    && kind != Kind.PREFIX_DECREMENT
//                    && kind != Kind.PREFIX_INCREMENT;
//            while (t.getKind() == Tree.Kind.ARRAY_ACCESS) {
//                isArrayOrImmutable = true;
//                t = ((ArrayAccessTree) t).getExpression();
//            }
//            Element el = workingCopy.getTrees().getElement(new TreePath(getCurrentPath(), t));
//            EncapsulateDesc desc = fields.get(el);
//            if (desc != null && desc.useAccessors
//                    && desc.refactoring.getGetterName() != null
//                    && (isArrayOrImmutable || checkAssignmentInsideExpression())
//                    && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
//                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
//                // check (++field + 3)
//                ExpressionTree invkgetter = createGetterInvokation(t, desc.refactoring.getGetterName());
//                if (isArrayOrImmutable) {
//                    rewrite(t, invkgetter);
//                } else if (desc.refactoring.getSetterName() != null) {
//                    ExpressionTree setter = createMemberSelection(node.getExpression(), desc.refactoring.getSetterName());
//
//                    Tree.Kind operator = kind == Tree.Kind.POSTFIX_INCREMENT || kind == Tree.Kind.PREFIX_INCREMENT
//                            ? Tree.Kind.PLUS
//                            : Tree.Kind.MINUS;
//
//                    // resolve types
//                    Trees trees = workingCopy.getTrees();
//                    ExpressionTree expTree = node.getExpression();
//                    TreePath varPath = trees.getPath(workingCopy.getCompilationUnit(), expTree);
//                    TypeMirror varType = trees.getTypeMirror(varPath);
//                    TypeMirror expType = workingCopy.getTypes().getPrimitiveType(TypeKind.INT);
//                    ExpressionTree newExpTree = make.Binary(operator, invkgetter, make.Literal(1));
//                    if (!workingCopy.getTypes().isSubtype(expType, varType)) {
//                        newExpTree = make.TypeCast(make.Type(varType), make.Parenthesized(newExpTree));
//                    }
//
//                    MethodInvocationTree invksetter = make.MethodInvocation(
//                            Collections.<ExpressionTree>emptyList(),
//                            setter,
//                            Collections.singletonList(newExpTree));
//                    rewrite(node, invksetter);
//                }
//            }
//            return null;
//        }
//
//        @Override
//        public Tree visitMemberSelect(MemberSelectTree node, Element field) {
//            Element el = workingCopy.getTrees().getElement(getCurrentPath());
//            EncapsulateDesc desc = fields.get(el);
//            if (desc != null && desc.useAccessors && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
//                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
//                ExpressionTree nodeNew = createGetterInvokation(node, desc.refactoring.getGetterName());
//                rewrite(node, nodeNew);
//            }
//            return super.visitMemberSelect(node, field);
//        }
//
//        @Override
//        public Tree visitIdentifier(IdentifierTree node, Element field) {
//            Element el = workingCopy.getTrees().getElement(getCurrentPath());
//            EncapsulateDesc desc = fields.get(el);
//            if (desc != null && desc.useAccessors && !isInConstructorOfFieldClass(getCurrentPath(), desc.field)
//                    && !isInGetterSetter(getCurrentPath(), desc.currentGetter, desc.currentSetter)) {
//                ExpressionTree nodeNew = createGetterInvokation(node, desc.refactoring.getGetterName());
//                rewrite(node, nodeNew);
//            }
//            return null;
//        }
//
//        private boolean checkAssignmentInsideExpression() {
//            Tree exp1 = getCurrentPath().getLeaf();
//            Tree parent = getCurrentPath().getParentPath().getLeaf();
//            if (parent.getKind() != Tree.Kind.EXPRESSION_STATEMENT) {
//                // XXX would be useful if Problems support HTML
////                String code = parent.toString();
////                String replace = exp1.toString();
////                code = code.replace(replace, "&lt;b&gt;" + replace + "&lt;/b&gt;");
//                problem = createProblem(
//                        problem,
//                        false,
//                        NbBundle.getMessage(
//                                EncapsulateFieldRefactoringPlugin.class,
//                                "ERR_EncapsulateInsideAssignment", // NOI18N
//                                exp1.toString(),
//                                parent.toString(),
//                                FileUtil.getFileDisplayName(workingCopy.getFileObject())));
//                return false;
//            }
//            return true;
//        }
//
//        /**
//         * replace current expresion with the proper one.<p>
//         * c.field -> c.getField()
//         * field -> getField()
//         * or copy in case of refactoring.getGetterName() == null
//         */
//        private ExpressionTree createGetterInvokation(ExpressionTree current, String getterName) {
//            // check if exist refactoring.getGetterName() != null and visibility (subclases)
//            if (getterName == null) {
//                return current;
//            }
//            ExpressionTree getter = createMemberSelection(current, getterName);
//
//            MethodInvocationTree invkgetter = make.MethodInvocation(
//                    Collections.<ExpressionTree>emptyList(),
//                    getter,
//                    Collections.<ExpressionTree>emptyList());
//            return invkgetter;
//        }
//
//        private ExpressionTree createMemberSelection(ExpressionTree node, String name) {
//            ExpressionTree selector;
//            if (node.getKind() == Tree.Kind.MEMBER_SELECT) {
//                MemberSelectTree select = (MemberSelectTree) node;
//                selector = make.MemberSelect(select.getExpression(), name);
//            } else {
//                selector = make.Identifier(name);
//            }
//            return selector;
//        }
//
//        private MethodTree[] createGetterAndSetter(
//                CsmField field, String getterName,
//                String setterName, Set<CsmVisibility> useModifiers) {
//
//            String fieldName = field.getSimpleName().toString();
//            boolean staticMod = field.getModifiers().contains(CsmVisibility.STATIC);
//            String parName = staticMod ? "a" + getCapitalizedName(field) : stripPrefix(fieldName); //NOI18N
//            String getterBody = "{return " + fieldName + ";}"; //NOI18N
//            String setterBody = (staticMod? "{": "{this.") + fieldName + " = " + parName + ";}"; //NOI18N
//
//            Set<CsmVisibility> mods = new HashSet<CsmVisibility>(useModifiers);
//            if (staticMod) {
//                mods.add(CsmVisibility.STATIC);
//            }
//
//            VariableTree fieldTree = (VariableTree) workingCopy.getTrees().getTree(field);
//            MethodTree[] result = new MethodTree[2];
//
//            ExecutableElement getterElm = null;
//            if (getterName != null) {
//                getterElm = findMethod(
//                        workingCopy,
//                        (CsmClass) field.getEnclosingElement(),
//                        getterName,
//                        Collections.<CsmField>emptyList(), false);
//            }
//            if (getterElm == null && getterName != null) {
//                MethodTree getter = make.Method(
//                        make.Modifiers(mods),
//                        getterName,
//                        fieldTree.getType(),
//                        Collections.<TypeParameterTree>emptyList(),
//                        Collections.<VariableTree>emptyList(),
//                        Collections.<ExpressionTree>emptyList(),
//                        getterBody,
//                        null);
//                result[0] = getter;
//                String jdText = null;
//                if (javadocType == Javadoc.COPY) {
//                    jdText = workingCopy.getElements().getDocComment(field);
//                    jdText = trimNewLines(jdText);
//                }
//                if (javadocType == Javadoc.DEFAULT || javadocType == Javadoc.COPY) {
//                    String prefix = jdText == null ? "" : jdText + "\n"; // NOI18N
//                    Comment comment = Comment.create(
//                            Comment.Style.JAVADOC, -2, -2, -2,
//                            prefix + "@return the " + field.getSimpleName()); // NOI18N
//                    make.addComment(getter, comment, true);
//                }
//            }
//
//            ExecutableElement setterElm = null;
//            if (setterName != null) {
//                setterElm = findMethod(
//                        workingCopy,
//                        (CsmClass) field.getEnclosingElement(),
//                        setterName,
//                        Collections.<CsmField>singletonList(field), false);
//            }
//            if (setterElm == null && setterName != null) {
//                VariableTree paramTree = make.Variable(
//                        make.Modifiers(Collections.<CsmVisibility>emptySet()), parName, fieldTree.getType(), null);
//                MethodTree setter = make.Method(
//                        make.Modifiers(mods),
//                        setterName,
//                        make.PrimitiveType(TypeKind.VOID),
//                        Collections.<TypeParameterTree>emptyList(),
//                        Collections.singletonList(paramTree),
//                        Collections.<ExpressionTree>emptyList(),
//                        setterBody,
//                        null);
//                result[1] = setter;
//
//                String jdText = null;
//                if (javadocType == Javadoc.COPY) {
//                    jdText = workingCopy.getElements().getDocComment(field);
//                    jdText = trimNewLines(jdText);
//                }
//                if (javadocType == Javadoc.DEFAULT || javadocType == Javadoc.COPY) {
//                    String prefix = jdText == null ? "" : jdText + "\n"; // NOI18N
//                    Comment comment = Comment.create(
//                            Comment.Style.JAVADOC, -2, -2, -2,
//                            prefix + String.format("@param %s the %s to set", parName, fieldName)); // NOI18N
//                    make.addComment(setter, comment, true);
//                }
//            }
//
//            return result;
//        }
//
//        private String trimNewLines(String javadoc) {
//            if (javadoc == null) {
//                return null;
//            }
//
//            int len = javadoc.length();
//            int st = 0;
//            int off = 0;      /* avoid getfield opcode */
//            char[] val = javadoc.toCharArray();    /* avoid getfield opcode */
//
//            while ((st < len) && Character.isWhitespace(val[off + st])/* && (val[off + st] <= '\n')*/) {
//                st++;
//            }
//            while ((st < len) && Character.isWhitespace(val[off + len - 1])/*val[off + len - 1] <= '\n')*/) {
//                len--;
//            }
//            return ((st > 0) || (len < val.length)) ? javadoc.substring(st, len) : javadoc;
//        }
//
//        private void resolveFieldDeclaration(VariableTree node, EncapsulateDesc desc) {
//            CsmVisibility currentAccess = getAccessibility(desc.field.getModifiers());
//            CsmVisibility futureAccess = getAccessibility(desc.refactoring.getFieldModifiers());
//            ModifiersTree newModTree = null;
//            if (currentAccess != futureAccess) {
//                newModTree = make.Modifiers(
//                        replaceAccessibility(currentAccess, futureAccess, desc.field),
//                        node.getModifiers().getAnnotations());
//            }
//
//            if (node.getModifiers().getFlags().contains(CsmVisibility.FINAL)
//                    && desc.refactoring.getSetterName() != null) {
//                // remove final flag in case user wants to create setter
//                ModifiersTree mot = newModTree == null ? node.getModifiers(): newModTree;
//                Set<CsmVisibility> flags = new HashSet<CsmVisibility>(mot.getFlags());
//                flags.remove(CsmVisibility.FINAL);
//                newModTree = make.Modifiers(flags, mot.getAnnotations());
//            }
//
//            if (newModTree != null) {
//                VariableTree newNode = make.Variable(
//                        newModTree, node.getName(), node.getType(), node.getInitializer());
//                rewrite(node, newNode);
//            }
//        }
//
//        private boolean resolveUseAccessor(CsmClass where, EncapsulateDesc desc) {
//            if (desc.refactoring.isAlwaysUseAccessors()) {
//                return true;
//            }
//
//            // target field accessibility
//            Set<CsmVisibility> mods = desc.refactoring.getFieldModifiers();
//            if (mods.contains(CsmVisibility.PRIVATE)) {
//                // check enclosing top level class
//                // return SourceUtils.getOutermostEnclosingTypeElement(where) != SourceUtils.getOutermostEnclosingTypeElement(desc.field);
//                return where != desc.field.getEnclosingElement();
//            }
//
//            if (mods.contains(CsmVisibility.PROTECTED)) {
//                // check inheritance
//                if (isSubclassOf(where, (CsmClass) desc.field.getEnclosingElement())) {
//                    return false;
//                }
//                // check same package
//                return workingCopy.getElements().getPackageOf(where) != workingCopy.getElements().getPackageOf(desc.field);
//            }
//
//            if (mods.contains(CsmVisibility.PUBLIC)) {
//                return false;
//            }
//
//            // default access
//            // check same package
//            return workingCopy.getElements().getPackageOf(where) != workingCopy.getElements().getPackageOf(desc.field);
//        }
//
//        private boolean isInConstructorOfFieldClass(TreePath path, Element field) {
//            Tree leaf = path.getLeaf();
//            Kind kind = leaf.getKind();
//            while (true) {
//                switch (kind) {
//                case METHOD:
//                    if (workingCopy.getTreeUtilities().isSynthetic(path)) {
//                        return false;
//                    }
//                    Element m = workingCopy.getTrees().getElement(path);
//                    return m.getKind() == ElementKind.CONSTRUCTOR
//                            && (m.getEnclosingElement() == field.getEnclosingElement()
//                                || isSubclassOf((CsmClass) m.getEnclosingElement(), (CsmClass) field.getEnclosingElement()));
//                case COMPILATION_UNIT:
//                case CLASS:
//                case NEW_CLASS:
//                    return false;
//                }
//                path = path.getParentPath();
//                leaf = path.getLeaf();
//                kind = leaf.getKind();
//            }
//        }
//
//        private boolean isInGetterSetter(
//                TreePath path,
//                ElementHandle<ExecutableElement> currentGetter,
//                ElementHandle<ExecutableElement> currentSetter) {
//
//            if (sourceFile != workingCopy.getFileObject()) {
//                return false;
//            }
//
//            Tree leaf = path.getLeaf();
//            Kind kind = leaf.getKind();
//            while (true) {
//                switch (kind) {
//                case METHOD:
//                    if (workingCopy.getTreeUtilities().isSynthetic(path)) {
//                        return false;
//                    }
//                    Element m = workingCopy.getTrees().getElement(path);
//                    return currentGetter != null && m == currentGetter.resolve(workingCopy)
//                            || currentSetter != null && m == currentSetter.resolve(workingCopy);
//                case COMPILATION_UNIT:
//                case CLASS:
//                case NEW_CLASS:
//                    return false;
//                }
//                path = path.getParentPath();
//                leaf = path.getLeaf();
//                kind = leaf.getKind();
//            }
//        }
//
//    }
    
//    /**
//     * A descriptor of the encapsulated field for Encapsulator.
//     */
//    static final class EncapsulateDesc {
//        Problem p;
//        Set<FileObject> refs;
//        TreePathHandle fieldHandle;
//
//        // following fields are used solely by Encapsulator
//        CsmField field;
//        private ElementHandle<ExecutableElement> currentGetter;
//        private ElementHandle<ExecutableElement> currentSetter;
//        private EncapsulateFieldRefactoring refactoring;
//        private boolean useAccessors;
//    }
    
}
