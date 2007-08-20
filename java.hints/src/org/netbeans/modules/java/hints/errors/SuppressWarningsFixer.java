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
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.modules.java.hints.spi.ErrorRule;
import org.netbeans.modules.java.hints.spi.ErrorRule.Data;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class SuppressWarningsFixer implements ErrorRule<Void> {
    
    /** Creates a new instance of SuppressWarningsFixer */
    public SuppressWarningsFixer() {
    }
    
    private static final Map<String, String> KEY2SUPRESS_KEY;
    
    static {
        Map<String, String> map = new HashMap<String, String>();
        
        String uncheckedKey = "unchecked";
        
        map.put("compiler.warn.prob.found.req", uncheckedKey); // NOI18N
        map.put("compiler.warn.unchecked.cast.to.type", uncheckedKey);  // NOI18N
        map.put("compiler.warn.unchecked.assign", uncheckedKey);  // NOI18N
        map.put("compiler.warn.unchecked.assign.to.var", uncheckedKey);  // NOI18N
        map.put("compiler.warn.unchecked.call.mbr.of.raw.type", uncheckedKey);  // NOI18N
        map.put("compiler.warn.unchecked.meth.invocation.applied", uncheckedKey);  // NOI18N
        map.put("compiler.warn.unchecked.generic.array.creation", uncheckedKey);  // NOI18N
        
        String fallThroughKey = "fallthrough"; // NOI18N
        
        map.put("compiler.warn.possible.fall-through.into.case", fallThroughKey);  // NOI18N
        
        String deprecationKey = "deprecation";  // NOI18N
        
        map.put("compiler.warn.has.been.deprecated", deprecationKey);  // NOI18N
        
        KEY2SUPRESS_KEY = Collections.unmodifiableMap(map); 
    }
    
    public Set<String> getCodes() {
        return KEY2SUPRESS_KEY.keySet();
    }

    public List<Fix> run(CompilationInfo compilationInfo, String diagnosticKey,
                         int offset, TreePath treePath,
                         Data<Void> data) {
        String suppressKey = KEY2SUPRESS_KEY.get(diagnosticKey);
	
	final Set<Kind> DECLARATION = EnumSet.of(Kind.CLASS, Kind.METHOD, Kind.VARIABLE);
	
	while (treePath.getLeaf().getKind() != Kind.COMPILATION_UNIT && !DECLARATION.contains(treePath.getLeaf().getKind())) {
	    treePath = treePath.getParentPath();
	}
	
        if (suppressKey != null && treePath.getLeaf().getKind() != Kind.COMPILATION_UNIT) {
            return Collections.singletonList((Fix) new FixImpl(suppressKey, TreePathHandle.create(treePath, compilationInfo), compilationInfo.getFileObject()));
        }
        
        return Collections.<Fix>emptyList();
    }

    public void cancel() {
    }

    public String getId() {
        return "SuppressWarningsFixer";  // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(SuppressWarningsFixer.class, "LBL_Suppress_Waning");  // NOI18N
    }

    public String getDescription() {
        return NbBundle.getMessage(SuppressWarningsFixer.class, "LBL_Suppress_Waning");  // NOI18N
    }

    private static final class FixImpl implements Fix {
        
        private String key;
        private TreePathHandle handle;
        private FileObject file;
        
        public FixImpl(String key, TreePathHandle handle, FileObject file) {
            this.key = key;
            this.handle = handle;
            this.file = file;
        }
    
        public String getText() {
            return NbBundle.getMessage(SuppressWarningsFixer.class, "LBL_FIX_Suppress_Waning",  key );  // NOI18N
        }
        
        private static final Set<Kind> DECLARATION = EnumSet.of(Kind.CLASS, Kind.METHOD, Kind.VARIABLE);
        
        public ChangeInfo implement() {
            try {
                JavaSource js = JavaSource.forFileObject(file);
                
                js.runModificationTask(new Task<WorkingCopy>() {

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
                                    Logger.getLogger(SuppressWarningsFixer.class.getName()).log(Level.INFO, "SupressWarnings annotation has incorrect number of arguments - {0}.", arguments.size());  // NOI18N
                                    return ;
                                }
                                
                                ExpressionTree et = at.getArguments().get(0);
                                
                                if (et.getKind() != Kind.ASSIGNMENT) {
                                    Logger.getLogger(SuppressWarningsFixer.class.getName()).log(Level.INFO, "SupressWarnings annotation's argument is not an assignment - {0}.", et.getKind());  // NOI18N
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
                                
                                values.add(copy.getTreeMaker().Literal(key));
                                
                                copy.rewrite(assignment.getExpression(), copy.getTreeMaker().NewArray(null, Collections.<ExpressionTree>emptyList(), values));
                                return ;
                            }
                        }
                        
                        List<AnnotationTree> annotations = new ArrayList<AnnotationTree>(modifiers.getAnnotations());
                        annotations.add(copy.getTreeMaker().Annotation(copy.getTreeMaker().QualIdent(el), Collections.singletonList(copy.getTreeMaker().Literal(key))));
                        
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
