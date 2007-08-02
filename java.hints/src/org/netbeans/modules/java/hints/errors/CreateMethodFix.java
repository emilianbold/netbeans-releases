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

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
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
import org.netbeans.modules.java.editor.codegen.GeneratorUtils;
import org.netbeans.modules.java.hints.infrastructure.ErrorHintsProvider;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan lahoda
 */
public final class CreateMethodFix implements Fix {
    
    private FileObject targetFile;
    private ElementHandle<TypeElement> target;
    private TypeMirrorHandle returnType;
    private List<TypeMirrorHandle> argumentTypes;
    private List<String> argumentNames;
    private ClasspathInfo cpInfo;
    private Set<Modifier> modifiers;
    
    private String name;
    private String inFQN;
    private String methodDisplayName;
    
    public CreateMethodFix(CompilationInfo info, String name, Set<Modifier> modifiers, TypeElement target, TypeMirror returnType, List<? extends TypeMirror> argumentTypes, List<String> argumentNames) {
        this.name = name;
        this.inFQN = target.getQualifiedName().toString();
        this.cpInfo = info.getClasspathInfo();
        this.modifiers = modifiers;
        this.targetFile = SourceUtils.getFile(target, cpInfo);
        this.target = ElementHandle.create(target);
        if (returnType != null && returnType.getKind() == TypeKind.NULL) {
            returnType = info.getElements().getTypeElement("java.lang.Object").asType(); // NOI18N
        }
        this.returnType = returnType != null ? TypeMirrorHandle.create(returnType) : null;
        this.argumentTypes = new ArrayList<TypeMirrorHandle>();
        
        for (TypeMirror tm : argumentTypes) {
            this.argumentTypes.add(TypeMirrorHandle.create(tm));
        }
        
        this.argumentNames = argumentNames;
        
        StringBuilder methodDisplayName = new StringBuilder();
        
        if (returnType != null) {
            methodDisplayName.append(name);
        } else {
            methodDisplayName.append(target.getSimpleName().toString());
        }
        
        methodDisplayName.append('(');
        
        boolean first = true;
        
        for (TypeMirror tm : argumentTypes) {
            if (!first)
                methodDisplayName.append(','); // NOI18N
            first = false;
            methodDisplayName.append(org.netbeans.modules.editor.java.Utilities.getTypeName(tm, true));
        }
        
        methodDisplayName.append(')'); // NOI18N
        
        this.methodDisplayName = methodDisplayName.toString();
    }
    
    public String getText() {
        if (returnType != null) {
            return NbBundle.getMessage(CreateMethodFix.class, "LBL_FIX_Create_Method", methodDisplayName, inFQN );
        } else {
            return NbBundle.getMessage(CreateMethodFix.class, "LBL_FIX_Create_Constructor", methodDisplayName, inFQN );
        }
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
                
                TreePath targetTree = working.getTrees().getPath(targetType);
                
                if (targetTree == null) {
                    ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve target tree: " + targetType.getQualifiedName() + "."); // NOI18N
                    return;
                }
                
                TypeMirrorHandle returnTypeHandle = CreateMethodFix.this.returnType;
                TypeMirror returnType = returnTypeHandle != null ? returnTypeHandle.resolve(working) : null;
                
                if (returnTypeHandle != null && returnType == null) {
                    ErrorHintsProvider.LOG.log(Level.INFO, "Cannot resolve proposed type."); // NOI18N
                    return;
                }
                
                TreeMaker make = working.getTreeMaker();
                
                List<VariableTree>         argTypes = new ArrayList<VariableTree>();
                Iterator<TypeMirrorHandle> typeIt   = CreateMethodFix.this.argumentTypes.iterator();
                Iterator<String>           nameIt   = CreateMethodFix.this.argumentNames.iterator();
                
                while (typeIt.hasNext() && nameIt.hasNext()) {
                    TypeMirrorHandle tmh = typeIt.next();
                    String           argName = nameIt.next();
                    
                    argTypes.add(make.Variable(make.Modifiers(EnumSet.noneOf(Modifier.class)), argName, make.Type(tmh.resolve(working)), null));
                }
                
                BlockTree body = targetType.getKind().isClass() ? createDefaultMethodBody(working, returnType) : null;
                MethodTree mt = make.Method(make.Modifiers(modifiers), name, returnType != null ? make.Type(returnType) : null, Collections.<TypeParameterTree>emptyList(), argTypes, Collections.<ExpressionTree>emptyList(), body, null);
                ClassTree decl = GeneratorUtils.insertClassMember(working, targetTree, mt);
                
                working.rewrite(targetTree.getLeaf(), decl);
            }
        }).commit();
        
        return null;
    }
    
    private void addArguments(CompilationInfo info, StringBuilder value) {
        value.append("("); // NOI18N
        
        Iterator<TypeMirrorHandle> typeIt = CreateMethodFix.this.argumentTypes.iterator();
        Iterator<String>           nameIt = CreateMethodFix.this.argumentNames.iterator();
        boolean                    first  = true;
        
        while (typeIt.hasNext() && nameIt.hasNext()) {
            if (!first) {
                value.append(",");
            }
            first = false;
            
            TypeMirrorHandle tmh = typeIt.next();
            String           argName = nameIt.next();
            
            value.append(org.netbeans.modules.editor.java.Utilities.getTypeName(tmh.resolve(info), true));
            value.append(' '); // NOI18N
            value.append(argName);
        }
        
        value.append(")"); // NOI18N
    }
    
    public String toDebugString(CompilationInfo info) {
        StringBuilder value = new StringBuilder();
        
        if (returnType != null) {
            value.append("CreateMethodFix:"); // NOI18N
            value.append(name);
            addArguments(info, value);
            value.append(org.netbeans.modules.editor.java.Utilities.getTypeName(returnType.resolve(info), true));
        } else {
            value.append("CreateConstructorFix:"); // NOI18N
            addArguments(info, value);
        }
        
        value.append(':'); // NOI18N
        value.append(inFQN); // NOI18N
        
        return value.toString();
    }
    
    //XXX should be moved into the GeneratorUtils:
    private static BlockTree createDefaultMethodBody(WorkingCopy wc, TypeMirror returnType) {
        TreeMaker make = wc.getTreeMaker();
        List<StatementTree> blockStatements = new ArrayList<StatementTree>();
        TypeElement uoe = wc.getElements().getTypeElement("java.lang.UnsupportedOperationException"); // NOI18N
        if (uoe != null) {
            NewClassTree nue = make.NewClass(null, Collections.<ExpressionTree>emptyList(), make.QualIdent(uoe), Collections.singletonList(make.Literal("Not yet implemented")), null);
            blockStatements.add(make.Throw(nue));
        }
        return make.Block(blockStatements, false);
    }
    
}

