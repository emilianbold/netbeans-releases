/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.visualweb.insync.java;

import com.sun.rave.designtime.ContextMethod;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.util.ArrayList;
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
    
    /*
     * Creates a variable tree for a property field
     */ 
    public static VariableTree createPropertyField(WorkingCopy wc, String name, Class type) {
        TreeMaker make = wc.getTreeMaker();
        Tree typeTree = createType(wc, type.getCanonicalName());
        ExpressionTree initializer = make.NewClass(null, 
                Collections.<ExpressionTree>emptyList(), 
                (ExpressionTree)typeTree, 
                Collections.<ExpressionTree>emptyList(),
                null);
        return make.Variable(
                createModifiers(wc, Modifier.PRIVATE), 
                name,
                typeTree,
                initializer);
    }
    
    /*
     * Creates a new method tree for a property getter method
     */ 
    public static MethodTree createPropertyGetterMethod(WorkingCopy wc, String name, Class type){
        TreeMaker make = wc.getTreeMaker();
        return make.Method(
                createModifiers(wc, Modifier.PUBLIC),
                Naming.getterName(name),
                createType(wc, type.getCanonicalName()),
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                "{ return " + name + "; }", // NOI18N
                null);
    }
    
    /*
     * Creates a new method tree for a property setter method
     */ 
    public static MethodTree createPropertySetterMethod(WorkingCopy wc, String name, Class type){
        TreeMaker make = wc.getTreeMaker();
        String argName = Naming.paramNames(new Class[] { type }, null)[0];
        return make.Method(
                createModifiers(wc, Modifier.PUBLIC),
                Naming.setterName(name),
                make.PrimitiveType(TypeKind.VOID),
                Collections.<TypeParameterTree>emptyList(),
                Collections.singletonList(createVariable(wc, argName, type)),
                Collections.<ExpressionTree>emptyList(),
                "{ this." + name + " = " + argName + "; }", // NOI18N
                null);
    }
    
    /*
     * Creates a new variable tree for a given name and type
     */     
    public static VariableTree createVariable(WorkingCopy wc, String name, Class type) {
       return createVariable(wc, name, createType(wc, type.getCanonicalName()));
    }    
    
    /*
     * Creates a new variable tree for a given name and type
     */       
    private static VariableTree createVariable(WorkingCopy wc, String name, Tree type) {
        TreeMaker make = wc.getTreeMaker();
        return make.Variable(createModifiers(wc), name, type, null);        
    }
    
    /*
     * Creates a method given context method and return type name
     */       
    public static MethodTree createMethod(WorkingCopy wc, ContextMethod mInfo, String retTypeName) {
        TreeMaker make = wc.getTreeMaker();
        Class[] pTypes = mInfo.getParameterTypes();
        String[] pNames = mInfo.getParameterNames();
        List<VariableTree> params = new ArrayList<VariableTree>();
        for (int i = 0 ; pTypes != null && i < pTypes.length; i++) {
            VariableTree vtree = createVariable(wc, pNames[i], pTypes[i]);
            params.add(vtree);
        }
        
        Class[] excepTypes = mInfo.getExceptionTypes();
        List<ExpressionTree> throwsList = new ArrayList<ExpressionTree>();
        for (int i = 0 ; excepTypes != null && i < excepTypes.length; i++) {
            throwsList.add((ExpressionTree)createType(wc, excepTypes[i].getCanonicalName()));
        }
        
        String body = mInfo.getMethodBodyText();
        if(body == null) {
            body = "";
        }
        
        MethodTree mtree = make.Method(createModifiers(wc, mInfo.getModifiers()),
                mInfo.getName(),
                createType(wc, retTypeName),
                Collections.<TypeParameterTree>emptyList(),
                params,
                throwsList,
                "{" + body + "}",
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
    public static MethodTree updateMethod(WorkingCopy wc, ContextMethod cm, MethodTree methTree) {
        TreeMaker make = wc.getTreeMaker();
        String[] pNames = cm.getParameterNames();
     
        int mods = cm.getModifiers();
        ModifiersTree modsTree = methTree.getModifiers();
        if (mods > 0) {
            modsTree = createModifiers(wc, mods);
        }
            
        //Update param name(s)
        List<? extends VariableTree> params = methTree.getParameters();
        if(cm.getParameterTypes().length > 0) {
            List<VariableTree> newParams = new ArrayList<VariableTree>();
            String[] paramNames = cm.getParameterNames();
            for (int i = 0; i < paramNames.length; i++) {
                VariableTree param = params.get(i);
                newParams.add(createVariable(wc, param.getName().toString(), param.getType()));
            }
            params = newParams;
        }

        //Update comment
        String commentText = cm.getCommentText();
        if(commentText != null) {
            //m.setJavadocText(commentText);
        }

        //Update body
        String code = "{" + cm.getMethodBodyText() + "}";

        //Update return type
        Tree retTree = methTree.getReturnType();
        if(!cm.getName().equals("<init>")) {    //NOI18N
            Class retType = cm.getReturnType();
            if(retType != null) {
                retTree = createType(wc, retType.getCanonicalName());
            }
        }

        return make.Method(modsTree, cm.getName(), retTree, methTree.getTypeParameters(),
                params, methTree.getThrows(), code, null);
            
    }
    
    /*
     * Returns a tree for a given type in string format
     * Note that import for type is handled by make.QualIdent()
     */ 
    public static Tree createType(WorkingCopy wc, String typeName) {
        if(typeName == null) {
            return null;
        }
        
        TreeMaker make = wc.getTreeMaker();
        if (typeName.endsWith("[]")) { // NOI18N
            String elementTypeName = typeName.substring(0, typeName.length()-2);
            return make.ArrayType(createType(wc, elementTypeName));
        }
        
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
        } else if ("void".equals(typeName)) {
            primitiveTypeKind = TypeKind.VOID;
        }
        if (primitiveTypeKind != null) {
            return make.PrimitiveType(primitiveTypeKind);
        }
        
        TypeElement typeElement = wc.getElements().getTypeElement(typeName);
        if (typeElement == null) {
            throw new IllegalArgumentException("Type " + typeName + " cannot be found" +
                    " among elements of working copy, wc=" + wc + ", elements=" + wc.getElements()); // NOI18N
        }
        return make.QualIdent(typeElement);        
    }
   
    
    /*
     * Returns a import tree for a given type in string format
     *
     */ 
    public static ImportTree createImport(WorkingCopy wc, String typeName) {
        Tree tree = createType(wc, typeName);
        return wc.getTreeMaker().Import(tree, false);
    }    
    
    public static ModifiersTree createModifiers(WorkingCopy wc) {
        return wc.getTreeMaker().Modifiers(Collections.<Modifier>emptySet(), Collections.<AnnotationTree>emptyList());
    }
    
    /*
     * Creates a modifier tree given a single modifier
     */ 
    public static ModifiersTree createModifiers(WorkingCopy wc, Modifier modifier) {
        return wc.getTreeMaker().Modifiers(EnumSet.of(modifier), Collections.<AnnotationTree>emptyList());
    }
    
    /*
     * Creates a modifier tree givena single modifier
     */ 
    public static ModifiersTree createModifiers(WorkingCopy wc, long flags) {
        return wc.getTreeMaker().Modifiers(flags, Collections.<AnnotationTree>emptyList());
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
     * Creates a method invocation expression of the form b(arg1, ...)
     */ 
    public static MethodInvocationTree createMethodInvocation(WorkingCopy wc, String methodName, String[] pNames) {
        TreeMaker treeMaker = wc.getTreeMaker();
        List<ExpressionTree> args = new ArrayList<ExpressionTree>();
        for (int i = 0 ; pNames != null && i < pNames.length; i++) {
            ExpressionTree tree = treeMaker.Identifier(pNames[i]);
            args.add(tree);
        }        
        return treeMaker.MethodInvocation(Collections.<ExpressionTree>emptyList(), 
                treeMaker.Identifier(methodName), args);
    }
    
    /*
     * Creates a new class expression with empty class body
     */ 
    public static NewClassTree createNewClassExpression(WorkingCopy wc, String className) {
        TreeMaker treeMaker = wc.getTreeMaker();
        Tree type = TreeMakerUtils.createType(wc, className);
        ClassTree classTree = treeMaker.Class(TreeMakerUtils.createModifiers(wc), className, 
                Collections.<TypeParameterTree>emptyList(), null,
                Collections.<ExpressionTree>emptyList(), Collections.<Tree>emptyList());
        return treeMaker.NewClass(null, Collections.<ExpressionTree>emptyList(), 
                (ExpressionTree)type, Collections.<ExpressionTree>emptyList(), classTree);
    }        
}
