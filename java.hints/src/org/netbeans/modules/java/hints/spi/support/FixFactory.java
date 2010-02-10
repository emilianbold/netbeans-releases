/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.spi.support;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/** Factory for creating fixes, which add @SuppressWarnings to given Element
 *
 * @author Petr Hrebejk
 */
public final class FixFactory {

    private static final Set<Kind> DECLARATION = EnumSet.of(Kind.CLASS, Kind.METHOD, Kind.VARIABLE);
    
    private FixFactory() {}

    /** Creates a fix, which when invoked adds a set of modifiers to the existing ones
     * @param compilationInfo CompilationInfo to work on
     * @param treePath TreePath to a ModifiersTree.
     * @param toAdd set of Modifiers to add
     * @param text text displayed as a fix description
     */
    public static final Fix addModifiersFix(CompilationInfo compilationInfo, TreePath treePath, Set<Modifier> toAdd, String text) {
        Parameters.notNull("compilationInfo", compilationInfo);
        Parameters.notNull("treePath", treePath);
        Parameters.notNull("toAdd", toAdd);
        Parameters.notNull("text", text);

        return changeModifiersFix(compilationInfo, treePath, toAdd, Collections.<Modifier>emptySet(), text);
    }

    /** Creates a fix, which when invoked removes a set of modifiers from the existing ones
     * @param compilationInfo CompilationInfo to work on
     * @param treePath TreePath to a ModifiersTree.
     * @param toRemove set of Modifiers to remove
     * @param text text displayed as a fix description
     */
    public static final Fix removeModifiersFix(CompilationInfo compilationInfo, TreePath treePath, Set<Modifier> toRemove, String text) {
        Parameters.notNull("compilationInfo", compilationInfo);
        Parameters.notNull("treePath", treePath);
        Parameters.notNull("toRemove", toRemove);
        Parameters.notNull("text", text);

        return changeModifiersFix(compilationInfo, treePath, Collections.<Modifier>emptySet(), toRemove, text);
    }

    /** Creates a fix, which when invoked changes the existing modifiers
     * @param compilationInfo CompilationInfo to work on
     * @param treePath TreePath to a ModifiersTree.
     * @param toAdd set of Modifiers to add
     * @param toRemove set of Modifiers to remove
     * @param text text displayed as a fix description
     */
    public static final Fix changeModifiersFix(CompilationInfo compilationInfo, TreePath treePath, Set<Modifier> toAdd, Set<Modifier> toRemove, String text) {
        Parameters.notNull("compilationInfo", compilationInfo);
        Parameters.notNull("treePath", treePath);
        Parameters.notNull("toAdd", toAdd);
        Parameters.notNull("toRemove", toRemove);
        Parameters.notNull("text", text);

        if (treePath.getLeaf().getKind() != Kind.MODIFIERS) {
            return null;
        }
        return new ChangeModifiersFixImpl(TreePathHandle.create(treePath, compilationInfo), toAdd, toRemove, text);
    }

    /** Creates a fix, which when invoked adds @SuppresWarnings(keys) to
     * nearest declaration.
     * @param compilationInfo CompilationInfo to work on
     * @param treePath TreePath to a tree. The method will find nearest outer
     *        declaration. (type, method, field or local variable)
     * @param keys keys to be contained in the SuppresWarnings annotation. E.g.
     *        @SuppresWarnings( "key" ) or @SuppresWarnings( {"key1", "key2", ..., "keyN" } ).
     * @throws IllegalArgumentException if keys are null or empty or id no suitable element
     *         to put the annotation on is found (e.g. if TreePath to CompilationUnit is given")
     */
    public static Fix createSuppressWarningsFix(CompilationInfo compilationInfo, TreePath treePath, String... keys ) {
        Parameters.notNull("compilationInfo", compilationInfo);
        Parameters.notNull("treePath", treePath);
        Parameters.notNull("keys", keys);

        if (keys.length == 0) {
            throw new IllegalArgumentException("key must not be empty"); // NOI18N
        }

        if (!isSuppressWarningsSupported(compilationInfo)) {
            return null;
        }

        while (treePath.getLeaf().getKind() != Kind.COMPILATION_UNIT && !DECLARATION.contains(treePath.getLeaf().getKind())) {
            treePath = treePath.getParentPath();
        }

        if (treePath.getLeaf().getKind() != Kind.COMPILATION_UNIT) {
            return new FixImpl(TreePathHandle.create(treePath, compilationInfo), compilationInfo.getFileObject(), keys);
        } else {
            return null;
        }
    }

    /** Creates a fix, which when invoked adds @SuppresWarnings(keys) to
     * nearest declaration.
     * @param compilationInfo CompilationInfo to work on
     * @param treePath TreePath to a tree. The method will find nearest outer
     *        declaration. (type, method, field or local variable)
     * @param keys keys to be contained in the SuppresWarnings annotation. E.g. 
     *        @SuppresWarnings( "key" ) or @SuppresWarnings( {"key1", "key2", ..., "keyN" } ).
     * @throws IllegalArgumentException if keys are null or empty or id no suitable element 
     *         to put the annotation on is found (e.g. if TreePath to CompilationUnit is given")
     */ 
    public static List<Fix> createSuppressWarnings(CompilationInfo compilationInfo, TreePath treePath, String... keys ) {
        Parameters.notNull("compilationInfo", compilationInfo);
        Parameters.notNull("treePath", treePath);
        Parameters.notNull("keys", keys);

        if (keys.length == 0) {
            throw new IllegalArgumentException("key must not be empty"); // NOI18N
        }

        Fix f = createSuppressWarningsFix(compilationInfo, treePath, keys);

        if (f != null) {
            return Collections.<Fix>singletonList(f);
        } else {
            return Collections.emptyList();
        }        
    }

    //XXX: probably should not be in the "SPI"
    public static boolean isSuppressWarningsFix(Fix f) {
        return f instanceof FixImpl;
    }
    
    private static boolean isSuppressWarningsSupported(CompilationInfo info) {
        //cannot suppress if there is no SuppressWarnings annotation in the platform:
        if (info.getElements().getTypeElement("java.lang.SuppressWarnings") == null)
            return false;

        return info.getSourceVersion().compareTo(SourceVersion.RELEASE_5) >= 0;
    }
    
    private static final class FixImpl implements Fix {

        private String keys[];
        private TreePathHandle handle;
        private FileObject file;

        public FixImpl(TreePathHandle handle, FileObject file, String... keys) {
            this.keys = keys;
            this.handle = handle;
            this.file = file;
        }

        public String getText() {
            StringBuilder keyNames = new StringBuilder();
            for (int i = 0; i < keys.length; i++) {
                String string = keys[i];
                keyNames.append(string);
                if ( i < keys.length - 1) {
                    keyNames.append(", "); // NOI18N
                }
            }

            return NbBundle.getMessage(FixFactory.class, "LBL_FIX_Suppress_Waning",  keyNames.toString() );  // NOI18N
        }

        private static final Set<Kind> DECLARATION = EnumSet.of(Kind.CLASS, Kind.METHOD, Kind.VARIABLE);

        public ChangeInfo implement() throws IOException {
            JavaSource js = JavaSource.forFileObject(file);

            js.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy copy) throws IOException {
                    copy.toPhase(Phase.RESOLVED); //XXX: performance
                    TreePath path = handle.resolve(copy);

                    while (path != null && path.getLeaf().getKind() != Kind.COMPILATION_UNIT && !DECLARATION.contains(path.getLeaf().getKind())) {
                        path = path.getParentPath();
                    }

                    if (path.getLeaf().getKind() == Kind.COMPILATION_UNIT) {
                        return ;
                    }

                    Tree top = path.getLeaf();
                    ModifiersTree modifiers = null;

                    switch (top.getKind()) {
                        case CLASS:
                            modifiers = ((ClassTree) top).getModifiers();
                            break;
                        case METHOD:
                            modifiers = ((MethodTree) top).getModifiers();
                            break;
                        case VARIABLE:
                            modifiers = ((VariableTree) top).getModifiers();
                            break;
                        default: assert false : "Unhandled Tree.Kind";  // NOI18N
                    }

                    if (modifiers == null) {
                        return ;
                    }

                    TypeElement el = copy.getElements().getTypeElement("java.lang.SuppressWarnings");  // NOI18N

                    if (el == null) {
                        return ;
                    }

                    //check for already existing SuppressWarnings annotation:
                    for (AnnotationTree at : modifiers.getAnnotations()) {
                        TreePath tp = new TreePath(new TreePath(path, at), at.getAnnotationType());
                        Element  e  = copy.getTrees().getElement(tp);

                        if (el.equals(e)) {
                            //found SuppressWarnings:
                            List<? extends ExpressionTree> arguments = at.getArguments();

                            if (arguments.isEmpty() || arguments.size() > 1) {
                                Logger.getLogger(FixFactory.class.getName()).log(Level.INFO, "SupressWarnings annotation has incorrect number of arguments - {0}.", arguments.size());  // NOI18N
                                return ;
                            }

                            ExpressionTree et = at.getArguments().get(0);

                            if (et.getKind() != Kind.ASSIGNMENT) {
                                Logger.getLogger(FixFactory.class.getName()).log(Level.INFO, "SupressWarnings annotation's argument is not an assignment - {0}.", et.getKind());  // NOI18N
                                return ;
                            }

                            AssignmentTree assignment = (AssignmentTree) et;
                            List<? extends ExpressionTree> currentValues = null;

                            if (assignment.getExpression().getKind() == Kind.NEW_ARRAY) {
                                currentValues = ((NewArrayTree) assignment.getExpression()).getInitializers();
                            } else {
                                currentValues = Collections.singletonList(assignment.getExpression());
                            }

                            assert currentValues != null;

                            List<ExpressionTree> values = new ArrayList<ExpressionTree>(currentValues);

                            for (String key : keys) {
                                values.add(copy.getTreeMaker().Literal(key));
                            }


                            copy.rewrite(assignment.getExpression(), copy.getTreeMaker().NewArray(null, Collections.<ExpressionTree>emptyList(), values));
                            return ;
                        }
                    }

                    List<AnnotationTree> annotations = new ArrayList<AnnotationTree>(modifiers.getAnnotations());


                    if ( keys.length > 1 ) {
                        List<LiteralTree> keyLiterals = new ArrayList<LiteralTree>(keys.length);
                        for (String key : keys) {
                            keyLiterals.add(copy.getTreeMaker().Literal(key));
                        }
                        annotations.add(copy.getTreeMaker().Annotation(copy.getTreeMaker().QualIdent(el),
                                Collections.singletonList(
                                    copy.getTreeMaker().NewArray(null, Collections.<ExpressionTree>emptyList(), keyLiterals))));
                    }
                    else {
                        annotations.add(copy.getTreeMaker().Annotation(copy.getTreeMaker().QualIdent(el), Collections.singletonList(copy.getTreeMaker().Literal(keys[0]))));
                    }
                    ModifiersTree nueMods = copy.getTreeMaker().Modifiers(modifiers, annotations);

                    copy.rewrite(modifiers, nueMods);
                }
            }).commit();

            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FixImpl other = (FixImpl) obj;
            if (!Arrays.deepEquals(this.keys, other.keys)) {
                return false;
            }
            if (this.handle != other.handle && (this.handle == null || !this.handle.equals(other.handle))) {
                return false;
            }
            if (this.file != other.file && (this.file == null || !this.file.equals(other.file))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 79 * hash + Arrays.deepHashCode(this.keys);
            hash = 79 * hash + (this.handle != null ? this.handle.hashCode() : 0);
            hash = 79 * hash + (this.file != null ? this.file.hashCode() : 0);
            return hash;
        }
    }

    private static final class ChangeModifiersFixImpl implements Fix {

        private final TreePathHandle modsHandle;
        private final Set<Modifier> toAdd;
        private final Set<Modifier> toRemove;
        private final String text;

        public ChangeModifiersFixImpl(TreePathHandle modsHandle, Set<Modifier> toAdd, Set<Modifier> toRemove, String text) {
            this.modsHandle = modsHandle;
            this.toAdd = toAdd;
            this.toRemove = toRemove;
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public ChangeInfo implement() throws Exception {
            JavaSource.forFileObject(modsHandle.getFileObject()).runModificationTask(new Task<WorkingCopy>() {

                public void run(WorkingCopy wc) throws Exception {
                    wc.toPhase(Phase.RESOLVED);
                    TreePath path = modsHandle.resolve(wc);
                    if (path == null) {
                        return;
                    }
                    ModifiersTree mt = (ModifiersTree) path.getLeaf();
                    Set<Modifier> modifiers = (mt.getFlags().isEmpty()) ?
                        EnumSet.noneOf(Modifier.class) :
                        EnumSet.copyOf(mt.getFlags());
                    modifiers.addAll(toAdd);
                    modifiers.removeAll(toRemove);
                    ModifiersTree newMod = wc.getTreeMaker().Modifiers(modifiers, mt.getAnnotations());
                    wc.rewrite(mt, newMod);
                }
            }).commit();
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ChangeModifiersFixImpl other = (ChangeModifiersFixImpl) obj;
            if (this.modsHandle != other.modsHandle && (this.modsHandle == null || !this.modsHandle.equals(other.modsHandle))) {
                return false;
            }
            if (this.toAdd != other.toAdd && (this.toAdd == null || !this.toAdd.equals(other.toAdd))) {
                return false;
            }
            if (this.toRemove != other.toRemove && (this.toRemove == null || !this.toRemove.equals(other.toRemove))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 71 * hash + (this.modsHandle != null ? this.modsHandle.hashCode() : 0);
            hash = 71 * hash + (this.toAdd != null ? this.toAdd.hashCode() : 0);
            hash = 71 * hash + (this.toRemove != null ? this.toRemove.hashCode() : 0);
            return hash;
        }
    }
}
