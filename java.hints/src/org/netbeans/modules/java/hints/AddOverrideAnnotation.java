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
package org.netbeans.modules.java.hints;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.swing.JComponent;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.editor.overridden.AnnotationType;
import org.netbeans.modules.java.editor.overridden.ElementDescription;
import org.netbeans.modules.java.editor.overridden.IsOverriddenAnnotationHandler;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class AddOverrideAnnotation extends AbstractHint {
    
    /** Creates a new instance of AddOverrideAnnotation */
    public AddOverrideAnnotation() {
        super( true, true, AbstractHint.HintSeverity.WARNING );
    }
    
    public Set<Kind> getTreeKinds() {
        return EnumSet.of(Kind.METHOD);
    }

    public List<ErrorDescription> run(CompilationInfo compilationInfo,
                                      TreePath treePath) {
        TypeElement el = compilationInfo.getElements().getTypeElement("java.lang.Override"); //NOI18N

        if (el == null || !GeneratorUtils.supportsOverride(compilationInfo.getFileObject()))
            return null;

        Element e = compilationInfo.getTrees().getElement(treePath);

        if (e != null && e.getKind() == ElementKind.METHOD) {
            ExecutableElement ee = (ExecutableElement) e;
            List<ElementDescription> result = new ArrayList<ElementDescription>();

            AnnotationType type = IsOverriddenAnnotationHandler.detectOverrides(compilationInfo, (TypeElement) ee.getEnclosingElement(), ee, result);

            boolean hasOverriddenAnnotation = false;

            for (AnnotationMirror am : ee.getAnnotationMirrors()) {
                if (compilationInfo.getTypes().isSameType(am.getAnnotationType(), el.asType())) {
                    hasOverriddenAnnotation = true;
                    break;
                }
            }

            if (hasOverriddenAnnotation) {
                return null;
            }

            boolean addHint = false;

            if (type == AnnotationType.OVERRIDES) {
                addHint = true;
            } else {
                if (type == AnnotationType.IMPLEMENTS) {
                    String sourceLevel = SourceLevelQuery.getSourceLevel(compilationInfo.getFileObject());

                    if (!"1.5".equals(sourceLevel))
                        addHint = true;
                }
            }

            if (addHint) {
                List<Fix> fixes = Collections.<Fix>singletonList(new FixImpl(TreePathHandle.create(treePath, compilationInfo), compilationInfo.getFileObject()));

                int[] span = compilationInfo.getTreeUtilities().findNameSpan((MethodTree) treePath.getLeaf());

                if (span != null) {
                    String desc = NbBundle.getMessage(AddOverrideAnnotation.class, "HINT_AddOverrideAnnotation");
                    ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), desc, fixes, compilationInfo.getFileObject(), span[0], span[1]);

                    return Collections.singletonList(ed);
                }
            }
        }
        
        return null;
    }

    public String getId() {
        return AddOverrideAnnotation.class.getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(AddOverrideAnnotation.class, "DN_AddOverrideAnnotation");
    }

    public String getDescription() {
        return NbBundle.getMessage(AddOverrideAnnotation.class, "DESC_AddOverrideAnnotation");
    }

    public void cancel() {
        // XXX implement me 
    }
    
    public Preferences getPreferences() {
        return null;
    }
    
    @Override
    public JComponent getCustomizer(Preferences node) {
        return null;
    }    
          
    private static final class FixImpl implements Fix {
        
        private TreePathHandle handle;
        private FileObject file;
        
        public FixImpl(TreePathHandle handle, FileObject file) {
            this.handle = handle;
            this.file = file;
        }
        
        public String getText() {
            return NbBundle.getMessage(AddOverrideAnnotation.class, "FIX_AddOverrideAnnotation");
        }
        
        private static final Set<Kind> DECLARATION = EnumSet.of(Kind.CLASS, Kind.METHOD, Kind.VARIABLE);
        
        public ChangeInfo implement() throws IOException {
            JavaSource js = JavaSource.forFileObject(file);

            js.runModificationTask(new Task<WorkingCopy>() {
                public void run(WorkingCopy copy) throws IOException {
                    copy.toPhase(Phase.RESOLVED); //XXX: performance
                    TreePath path = handle.resolve(copy);

                    assert path != null;

                    while (path.getLeaf().getKind() != Kind.COMPILATION_UNIT && !DECLARATION.contains(path.getLeaf().getKind())) {
                        path = path.getParentPath();
                    }

                    if (path.getLeaf().getKind() == Kind.COMPILATION_UNIT) {
                        return ;
                    }

                    Tree top = path.getLeaf();
                    ModifiersTree modifiers = null;

                    switch (top.getKind()) {
                    case METHOD:
                        modifiers = ((MethodTree) top).getModifiers();
                        break;
                    default: assert false : "Unhandled Tree.Kind"; //NOI18N
                    }

                    if (modifiers == null) {
                        return ;
                    }

                    TypeElement el = copy.getElements().getTypeElement("java.lang.Override"); //NOI18N

                    if (el == null) {
                        return ;
                    }

                    //verify @Override annotation still does not exist:
                    for (AnnotationTree at : modifiers.getAnnotations()) {
                        TreePath tp = new TreePath(new TreePath(path, at), at.getAnnotationType());
                        Element  e  = copy.getTrees().getElement(tp);

                        if (el.equals(e)) {
                            //found existing :
                            return ;
                        }
                    }

                    List<AnnotationTree> annotations = new ArrayList<AnnotationTree>(modifiers.getAnnotations());
                    annotations.add(copy.getTreeMaker().Annotation(copy.getTreeMaker().QualIdent(el), Collections.<ExpressionTree>emptyList()));

                    ModifiersTree nueMods = copy.getTreeMaker().Modifiers(modifiers, annotations);

                    copy.rewrite(modifiers, nueMods);
                }
            }).commit();
            
            return null;
        }
    }
    
}
