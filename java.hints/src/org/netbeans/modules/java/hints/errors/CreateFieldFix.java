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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.errors;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.TypeMirrorHandle;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public final class CreateFieldFix implements Fix {
    
    private FileObject targetFile;
    private ElementHandle<TypeElement> target;
    private TypeMirrorHandle proposedType;
    private ClasspathInfo cpInfo;
    private Set<Modifier> modifiers;
    
    private String name;
    private String inFQN;
    
    public CreateFieldFix(CompilationInfo info, String name, Set<Modifier> modifiers, TypeElement target, TypeMirror proposedType) {
        this.name = name;
        this.inFQN = target.getQualifiedName().toString();
        this.cpInfo = info.getClasspathInfo();
        this.modifiers = modifiers;
        this.targetFile = SourceUtils.getFile(target, cpInfo);
        this.target = ElementHandle.create(target);
        if (proposedType.getKind() == TypeKind.NULL) {
            proposedType = info.getElements().getTypeElement("java.lang.Object").asType(); // NOI18N
        }
        this.proposedType = TypeMirrorHandle.create(proposedType);
    }
    
    public String getText() {
        return NbBundle.getMessage(CreateFieldFix.class, "LBL_FIX_Create_Field", name, inFQN);        
    }
    
    public ChangeInfo implement() throws IOException {
        //use the original cp-info so it is "sure" that the proposedType can be resolved:
        JavaSource js = JavaSource.create(cpInfo, targetFile);
        
        js.runModificationTask(new Task<WorkingCopy>() {

            public void run(final WorkingCopy working) throws IOException {
                working.toPhase(Phase.RESOLVED);
                TypeElement targetType = target.resolve(working);
                
                if (targetType == null) {
                    ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve target."); // NOI18N
                    return;
                }
                
                ClassTree targetTree = working.getTrees().getTree(targetType);
                
                if (targetTree == null) {
                    ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve target tree: " + targetType.getQualifiedName() + "."); // NOI18N
                    return;
                }
                
                TypeMirror proposedType = CreateFieldFix.this.proposedType.resolve(working);
                
                if (proposedType == null) {
                    ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve proposed type."); // NOI18N
                    return;
                }
                
                TreeMaker make = working.getTreeMaker();
                TypeMirror tm = proposedType;
                VariableTree var = null;
                
                if (tm.getKind() == TypeKind.DECLARED || tm.getKind() == TypeKind.ARRAY) {
                    var = make.Variable(make.Modifiers(modifiers), name, make.Type(tm), null);
                }
                
                if (tm.getKind().isPrimitive()) {
                    var = make.Variable(make.Modifiers(modifiers), name, make.Type(tm), null);
                }
                
                assert var != null : tm.getKind();
                ClassTree decl = make.addClassMember(targetTree, var);
                working.rewrite(targetTree, decl);
            }
        }).commit();
        
        return null;
    }
    
    String toDebugString(CompilationInfo info) {
        return "CreateFieldFix:" + name + ":" + target.getQualifiedName() + ":" + proposedType.resolve(info).toString() + ":" + modifiers; // NOI18N
    }
}
