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
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.GeneratorUtilities;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
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
    
    public CreateFieldFix(CompilationInfo info, String name, Set<Modifier> modifiers, TypeElement target, TypeMirror proposedType, FileObject targetFile) {
        this.name = name;
        this.inFQN = target.getQualifiedName().toString();
        this.cpInfo = info.getClasspathInfo();
        this.modifiers = modifiers;
        this.targetFile = targetFile;
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
        
        ModificationResult diff = js.runModificationTask(new Task<WorkingCopy>() {
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
                ClassTree decl = GeneratorUtilities.get(working).insertClassMember(targetTree, var);
                working.rewrite(targetTree, decl);
            }
        });
        
        return Utilities.commitAndComputeChangeInfo(targetFile, diff);
    }
    
    String toDebugString(CompilationInfo info) {
        return "CreateFieldFix:" + name + ":" + target.getQualifiedName() + ":" + proposedType.resolve(info).toString() + ":" + modifiers; // NOI18N
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CreateFieldFix other = (CreateFieldFix) obj;
        if (this.target != other.target && (this.target == null || !this.target.equals(other.target))) {
            return false;
        }
        if (this.modifiers != other.modifiers && (this.modifiers == null || !this.modifiers.equals(other.modifiers))) {
            return false;
        }
        if (this.name != other.name && (this.name == null || !this.name.equals(other.name))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.target != null ? this.target.hashCode() : 0);
        hash = 79 * hash + (this.modifiers != null ? this.modifiers.hashCode() : 0);
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
    
    
}
