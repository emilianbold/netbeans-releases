/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.editor.overridden.AnnotationType;
import org.netbeans.modules.java.editor.overridden.ElementDescription;
import org.netbeans.modules.java.editor.overridden.IsOverriddenAnnotationHandler;
import org.netbeans.modules.java.editor.semantic.Utilities;
import org.netbeans.modules.java.hints.spi.AbstractHint;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

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
        try {
            Document doc = compilationInfo.getDocument();
            
            if (doc == null)
                return null;
            
            TypeElement el = compilationInfo.getElements().getTypeElement("java.lang.Override");
            
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
                    
                    int[] span = Utilities.findIdentifierSpan(treePath, compilationInfo.getCompilationUnit(), compilationInfo.getTrees().getSourcePositions(), doc);
                    
                    if (span[0] != (-1) && span[1] != (-1)) {
                        ErrorDescription ed = ErrorDescriptionFactory.createErrorDescription(getSeverity().toEditorSeverity(), "Add Override Annotation", fixes, doc, doc.createPosition(span[0]), doc.createPosition(span[1]));
                        
                        return Collections.singletonList(ed);
                    }
                }
            }
        } catch (BadLocationException e) {
            Exceptions.printStackTrace(e);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        
        return null;
    }

    public String getId() {
        return AddOverrideAnnotation.class.getName();
    }

    public String getDisplayName() {
        return "Add @Override Annotation";
    }

    public String getDescription() {
        return "Add @Override Annotation by Jan Lahoda";
    }

    public void cancel() {
        // XXX implement me 
    }
    
    public Preferences getPreferences() {
        return null;
    }
    
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
            return "Add @Override Annotation";
        }
        
        private static final Set<Kind> DECLARATION = EnumSet.of(Kind.CLASS, Kind.METHOD, Kind.VARIABLE);
        
        public ChangeInfo implement() {
            try {
                JavaSource js = JavaSource.forFileObject(file);
                
                js.runModificationTask(new CancellableTask<WorkingCopy>() {
                    public void cancel() {}
                    public void run(WorkingCopy copy) throws IOException {
                        copy.toPhase(Phase.RESOLVED); //XXX: performance
                        TreePath path = handle.resolve(copy);
                        
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
                        default: assert false : "Unhandled Tree.Kind";
                        }
                        
                        if (modifiers == null) {
                            return ;
                        }
                        
                        TypeElement el = copy.getElements().getTypeElement("java.lang.Override");
                        
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
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
            
            return null;
        }
    }
    
}
