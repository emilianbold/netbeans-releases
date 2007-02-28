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

package org.netbeans.modules.visualweb.insync.java;

import com.sun.rave.designtime.ContextMethod;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import org.netbeans.api.java.source.Comment;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.visualweb.insync.beans.Naming;

/**
 *
 * @author jdeva
 */
public class TreeMakerUtils {
    WorkingCopy workingCopy;
    
    TreeMaker getTreeMaker() {
        return workingCopy.getTreeMaker();
    }
    
    /** Creates a new instance of TreeMakerUtils */
    public TreeMakerUtils(WorkingCopy workingCopy) {
        this.workingCopy = workingCopy;
    }
    
    /*
     * Creates a variable tree for a property field
     */ 
    public VariableTree createPropertyField(String name, Class type) {
        TreeMaker make = getTreeMaker();
        Tree typeTree = createType(type.getCanonicalName());
        ExpressionTree initializer = make.NewClass(null, 
                Collections.<ExpressionTree>emptyList(), 
                (ExpressionTree)typeTree, 
                Collections.<ExpressionTree>emptyList(),
                null);
        return make.Variable(
                createModifiers(Modifier.PRIVATE), 
                name,
                typeTree,
                initializer);
    }
    
    /*
     * Creates a new method tree for a property getter method
     */ 
    public MethodTree createPropertyGetterMethod(String name, Class type){
        TreeMaker make = getTreeMaker();
        return make.Method(
                createModifiers(Modifier.PUBLIC),
                Naming.getterName(name),
                createType(type.getCanonicalName()),
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                "{ return " + name + "; }", // NOI18N
                null);
    }
    
    /*
     * Creates a new method tree for a property setter method
     */ 
    public MethodTree createPropertySetterMethod(String name, Class type){
        TreeMaker make = getTreeMaker();
        String argName = Naming.paramNames(new Class[] { type }, null)[0];
        return make.Method(
                createModifiers(Modifier.PUBLIC),
                Naming.setterName(name),
                make.PrimitiveType(TypeKind.VOID),
                Collections.<TypeParameterTree>emptyList(),
                Collections.singletonList(createVariable(argName, type)),
                Collections.<ExpressionTree>emptyList(),
                "{ this." + name + " = " + argName + "; }", // NOI18N
                null);
    }
    
    /*
     * Creates a new variable tree for a given name and type
     */     
    public VariableTree createVariable(String name, Class type) {
       return createVariable(name, createType(type.getCanonicalName()));
    }    
    
    /*
     * Creates a new variable tree for a given name and type
     */       
    private VariableTree createVariable(String name, Tree type) {
        TreeMaker make = getTreeMaker();
        return make.Variable(createModifiers(), name, type, null);        
    }
    
    /*
     * Creates a method given context method and return type name
     */       
    public MethodTree createMethod(ContextMethod mInfo, String retTypeName) {
        TreeMaker make = getTreeMaker();
        Class[] pTypes = mInfo.getParameterTypes();
        String[] pNames = mInfo.getParameterNames();
        List<VariableTree> params = Collections.<VariableTree>emptyList();
        for (int i = 0 ; pTypes != null && i < pTypes.length; i++) {
            params.add(createVariable(pNames[i], pTypes[i]));
        }
        
        Class[] excepTypes = mInfo.getExceptionTypes();
        List<ExpressionTree> throwsList = Collections.<ExpressionTree>emptyList();
        for (int i = 0 ; excepTypes != null && i < excepTypes.length; i++) {
            throwsList.add((ExpressionTree)createType(excepTypes[i].getCanonicalName()));
        }
        
        MethodTree mtree = make.Method(createModifiers(mInfo.getModifiers()),
                mInfo.getName(),
                createType(retTypeName),
                Collections.<TypeParameterTree>emptyList(),
                params,
                throwsList,
                mInfo.getMethodBodyText(),
                null
                );
        
         if(mInfo.getCommentText() != null) {
             Comment comment = Comment.create(Comment.Style.JAVADOC, -2, 
                     -2, -2, mInfo.getCommentText());
             make.addComment(mtree, comment, true);
         }
        
        return mtree;
    }
     
    /* 
     *
     */
    public MethodTree updateMethod(ContextMethod cm, MethodTree methTree) {
        TreeMaker make = getTreeMaker();
        String[] pNames = cm.getParameterNames();
     
        int mods = cm.getModifiers();
        ModifiersTree modsTree = methTree.getModifiers();
        if (mods > 0) {
            modsTree = createModifiers(mods);
        }
            
        //Update param name(s)
        List<? extends VariableTree> params = methTree.getParameters();
        if(cm.getParameterTypes().length > 0) {
            List<VariableTree> newParams = Collections.<VariableTree>emptyList();
            String[] paramNames = cm.getParameterNames();
            for (int i = 0; i < paramNames.length; i++) {
                VariableTree param = params.get(i);
                newParams.add(createVariable(param.getName().toString(), param.getType()));
            }
            params = newParams;
        }

        //Update comment
        String commentText = cm.getCommentText();
        if(commentText != null) {
            //m.setJavadocText(commentText);
        }

        //Update body
        String code = cm.getMethodBodyText();

        //Update return type
        Tree retTree = methTree.getReturnType();
        if(!cm.getName().equals("<init>")) {
            Class retType = cm.getReturnType();
            if(retType != null) {
                retTree = createType(retType.getCanonicalName());
            }
        }

        return make.Method(modsTree, cm.getName(), retTree, methTree.getTypeParameters(),
                params, methTree.getThrows(), code, null);
            
    }
    
    /*
     * Returns a tree for a given type in string format
     * Note that import for type is handled by make.QualIdent()
     */ 
    public Tree createType(String typeName) {
        TreeMaker make = getTreeMaker();
        TypeKind primitiveTypeKind = null;
        if ("boolean".equals(typeName)) {           // NOI18N
            primitiveTypeKind = TypeKind.BOOLEAN;
        } else if ("byte".equals(typeName)) {       // NOI18N
            primitiveTypeKind = TypeKind.BYTE;
        } else if ("short".equals(typeName)) {      // NOI18N
            primitiveTypeKind = TypeKind.SHORT;
        } else if ("int".equals(typeName)) {        // NOI18N
            primitiveTypeKind = TypeKind.INT;
        } else if ("long".equals(typeName)) {       // NOI18N
            primitiveTypeKind = TypeKind.LONG;
        } else if ("char".equals(typeName)) {       // NOI18N
            primitiveTypeKind = TypeKind.CHAR;
        } else if ("float".equals(typeName)) {      // NOI18N
            primitiveTypeKind = TypeKind.FLOAT;
        } else if ("double".equals(typeName)) {     // NOI18N
            primitiveTypeKind = TypeKind.DOUBLE;
        }
        if (primitiveTypeKind != null) {
            return make.PrimitiveType(primitiveTypeKind);
        }
        
        TypeElement typeElement = workingCopy.getElements().getTypeElement(typeName);
        if (typeElement == null) {
            throw new IllegalArgumentException("Type " + typeName + " cannot be found"); // NOI18N
        }
        return make.QualIdent(typeElement);        
    }
   
    
    public ModifiersTree createModifiers() {
        return getTreeMaker().Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
    }
    
    /*
     * Creates a modifier tree given a single modifier
     */ 
    public ModifiersTree createModifiers(Modifier modifier) {
        return getTreeMaker().Modifiers(EnumSet.of(modifier), Collections.<AnnotationTree>emptyList());
    }
    
    /*
     * Creates a modifier tree givena single modifier
     */ 
    public ModifiersTree createModifiers(long flags) {
        return getTreeMaker().Modifiers(flags, Collections.<AnnotationTree>emptyList());
    }
    
    /*
     * Creates a method invocation expression of the form a.b(arg1, ...)
     */ 
    public static MethodInvocationTree createMethodInvocation(WorkingCopy wc, String beanName, String methodName, List<ExpressionTree> args) {
        TreeMaker treeMaker = wc.getTreeMaker();
        return treeMaker.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                treeMaker.MemberSelect(treeMaker.Identifier(beanName), methodName), args);
    }
    
    /*
     * Creates a new class expression with empty class body
     */ 
    public static NewClassTree createNewClassExpression(WorkingCopy wc, String className) {
        TreeMaker treeMaker = wc.getTreeMaker();
        TreeMakerUtils treeMakerUtils = new TreeMakerUtils(wc);
        Tree type = treeMakerUtils.createType(className);
        ClassTree classTree = treeMaker.Class(treeMakerUtils.createModifiers(), null, 
                Collections.<TypeParameterTree>emptyList(), null,
                Collections.<ExpressionTree>emptyList(), Collections.<Tree>emptyList());
        return treeMaker.NewClass(null, Collections.<ExpressionTree>emptyList(), 
                (ExpressionTree)type, Collections.<ExpressionTree>emptyList(), classTree);
    }        
}
