/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.soa.pojo.util;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;


/**
 * Add POJO Task.
 * @author sgenipudi
 */
class AddPOJOTask<T extends WorkingCopy> extends ModificationTask<WorkingCopy> {
    private Exception myException = null;

    private String methodReturnType;
    private String methodName;
    private String annotationType;
    private Map<String, Object> annotationArguments;
    private List<String> methodArgumentType;
    private Map<String, Object> operationArguments;

    public Exception getException() {
        return myException;
    }
    public AddPOJOTask(String methodName, String methodReturnType, List<String> methodArgumentType,  String annotationType, Map<String, Object> annotationArguments, Map<String, Object> operationArguments) {
        this.methodName = methodName;
        this.methodReturnType = methodReturnType;
        this.annotationArguments = annotationArguments;
        this.annotationType = annotationType;
        this.methodArgumentType = methodArgumentType;
        this.operationArguments = operationArguments;
    }
    private void displayElements(Element el, int level) {
            for (int x =0; x <level; x++) System.err.print("-");
            System.err.println();
            System.err.println("Element name "+el.getName());
            AttributeSet attrSet = el.getAttributes();
            Enumeration enumAttr = attrSet.getAttributeNames();
            while (enumAttr != null &&  enumAttr.hasMoreElements()) {
                System.err.println(" attr value "+enumAttr.nextElement().toString());
            }
            int childElemCt = el.getElementCount();
                for ( int ix = 0; ix < childElemCt; ix++) {
                    Element el1 = el.getElement(ix);
                    displayElements(el1,++level);
            }        
    }
   public void run(WorkingCopy workingCopy) throws Exception {
        try {
            workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);

            TreeMaker make = workingCopy.getTreeMaker();

            
            //Import org.glassfish.openesb.pojose.api.annotation.POJO
            //       org.glassfish.openesb.pojose.api.annotation.Operation
            
            CompilationUnitTree copy = make.addCompUnitImport(
                cut, 
                make.Import(make.Identifier(GeneratorUtil.PROVIDER_QUAL_CLASS_ANNOTATION), false)
            );
            //workingCopy.rewrite(cut, copy);
            //cut = workingCopy.getCompilationUnit();
            //classTree = (ClassTree) cut.getTypeDecls().get(0);
            
            CompilationUnitTree copy1 = make.addCompUnitImport(
                copy, 
                make.Import(make.Identifier(GeneratorUtil.POJO_QUAL_OPERATION_ANNOTATION_CLASS), false)
            );
            
            copy = make.addCompUnitImport(
                copy1, 
                make.Import(make.Identifier(GeneratorUtil.CTX_QUAL_CLASS_ANNOTATION), false)
            );
            
            copy1 = make.addCompUnitImport(
                copy, 
                make.Import(make.Identifier(GeneratorUtil.RSRC_QUAL_CLASS_ANNOTATION), false)
            );
                    
            workingCopy.rewrite(cut, copy1);
            cut = workingCopy.getCompilationUnit();
             classTree = (ClassTree) cut.getTypeDecls().get(0);
            
             
            //ADD POJO CLASS TYPE ANNOTATION
            /*
            Document dox = workingCopy.getDocument();
            Element[] arrayOfElements = dox.getRootElements();
            for ( Element el : arrayOfElements) {
               displayElements(el,0);
            }*/

            List<ExpressionTree> argumentValueList = new ArrayList<ExpressionTree>();
            if ( annotationArguments != null) {
                Set<Map.Entry<String,Object>> annArgValSet = annotationArguments.entrySet();
                for (Map.Entry<String,Object> mapenty: annArgValSet ) {
                    argumentValueList.add(GeneratorUtil.createAnnotationArgument(make, mapenty.getKey(), mapenty.getValue()));            
                }
            }            
            AnnotationTree pojoAnnTypeTree =  GeneratorUtil.createAnnotation(make, workingCopy, GeneratorUtil.PROVIDER_CLASS_ANNOTATION,
                   argumentValueList);

            
            ModifiersTree oldTree = classTree.getModifiers();
            ModifiersTree newTree = make.Modifiers(oldTree,Collections.singletonList(pojoAnnTypeTree));
            workingCopy.rewrite(oldTree, newTree);
            cut = workingCopy.getCompilationUnit();
            classTree = (ClassTree) cut.getTypeDecls().get(0);
            
            ModifiersTree modifiers = handleModifiersAndAnnotations(make, workingCopy,
                        GeneratorUtil.RSRC_ANNOTATION, null);

                VariableTree variableTree = GeneratorUtil.createField(make, workingCopy,
                    modifiers,
                    GeneratorUtil.POJO_CTX_VARIABLE, //TODO: Check if this variable is not used by this class. 
                    GeneratorUtil.CTX_ANNOTATION, //NOI18N
                    null
                );
               

                ClassTree newClassTree1 = make.addClassMember(classTree, variableTree);
               // workingCopy.rewrite(classTree, newClassTree1);             
            
            //CompilationUnitTree modClassTree = make.addCompUnitTypeDecl(cut, pojoAnnTypeTree);
            //workingCopy.rewrite(cut, modClassTree);
            cut = workingCopy.getCompilationUnit();
            classTree = newClassTree1;//(ClassTree) cut.getTypeDecls().get(0);
            //ADD POJO OPERATION.
           boolean bOperationReturnsVoid =  methodReturnType == null  ||  methodReturnType.equals(GeneratorUtil.GENERATE_VOID);
           argumentValueList = new ArrayList<ExpressionTree>();
            if (!bOperationReturnsVoid && this.operationArguments != null) {
                Set<Map.Entry<String,Object>> annArgValSet = operationArguments.entrySet();
                for (Map.Entry<String,Object> mapenty: annArgValSet ) {
                    argumentValueList.add(GeneratorUtil.createAnnotationArgument(make, mapenty.getKey(), mapenty.getValue()));            
                }
            }
            AnnotationTree anTree = GeneratorUtil.createAnnotation(make, workingCopy, annotationType, argumentValueList);
            Tree returnType = null;
            boolean bReturnVoid = false;
            if ( bOperationReturnsVoid) {
                returnType = make.PrimitiveType(TypeKind.VOID); // return type
                bReturnVoid = true;
            } else {
                returnType = GeneratorUtil.createType(make, workingCopy, methodReturnType);
            }
            
            List<TypeParameterTree> listOfInputTypes = new ArrayList<TypeParameterTree>();
            List<VariableTree> listOfInputVariables = new ArrayList<VariableTree>();
         //   ModifiersTree modTree = make.Modifiers(Collections.singleton(Modifier.PUBLIC));
            ModifiersTree parMods = make.Modifiers(Collections.EMPTY_SET, Collections.EMPTY_LIST);

            int ix =0;
            String inputVariableName = null;
            String inputVariableType = null;
            VariableTree inputIdentifier = null;
            for ( String argumentType: methodArgumentType){
                 if (! argumentType.equals("")) {
               //  listOfInputTypes.add(  make.TypeParameter(argumentType, null));
                     inputVariableName = GeneratorUtil.POJO_VARIABLE_NAME_PREFIX+(ix++);
                     inputVariableType = argumentType;
                     inputIdentifier = GeneratorUtil.createField(make, workingCopy, parMods /**modTree**/,  inputVariableName,argumentType, null);
                    listOfInputVariables.add(inputIdentifier);                 
                 }
            }
            
            BlockTree blockTree = null;
            if ( bReturnVoid) {
                blockTree = make.Block(Collections.EMPTY_LIST, false);
            } else {
                //List<ExpressionTree> returnExpList = Collections.singletonList((ExpressionTree)make.Identifier("null"));
                if ( inputVariableType != null && this.methodReturnType != null && methodReturnType.equals(inputVariableType)) {
                    ReturnTree retTree   =  make.Return(make.Identifier(inputVariableName));
                    blockTree = make.Block(Collections.singletonList(retTree), false);
                    
                } else {
                    ReturnTree retTree   =  make.Return(make.Identifier("null"));
                    blockTree = make.Block(Collections.singletonList(retTree), false);
                
                }
            }
            
            MethodTree newMethod = make.Method(
                   make.Modifiers(
                      Collections.singleton(Modifier.PUBLIC), // modifiers
                      Collections.singletonList(anTree) 
                   ), // modifiers and annotations
                   methodName, // name
                   returnType, // return type
                   Collections.EMPTY_LIST, //listOfInputTypes, // type parameters for parameters
                   listOfInputVariables, // parameters
                   Collections.EMPTY_LIST,
                   blockTree, // empty statement block
                   null // default value - not applicable here, used by annotations
               );
            ClassTree oldClassTree = (ClassTree) cut.getTypeDecls().get(0);
            ClassTree newClassTree = make.addClassMember(classTree, newMethod);
            workingCopy.rewrite(oldClassTree, newClassTree);
        } catch (Exception e) {
            myException = e;
            throw e;
        }
    }

    private ModifiersTree handleModifiersAndAnnotations(TreeMaker make, WorkingCopy workingCopy,
            String annotationType, Map<String, Object> annotationArguments) throws Exception {
        Set<Modifier> modifierSet = EnumSet.of(Modifier.PRIVATE);

        List<ExpressionTree> annoArgList = new ArrayList<ExpressionTree> ();
        if (annotationArguments != null) {
            for (String key : annotationArguments.keySet()) {
                Object val = annotationArguments.get(key);
                ExpressionTree annoArg = GeneratorUtil.createAnnotationArgument(make, key, val);
                annoArgList.add(annoArg);
            }
        } 
        AnnotationTree annotationTree = GeneratorUtil.createAnnotation(make, workingCopy,
                    annotationType, annoArgList);

        return make.Modifiers(modifierSet, Collections.singletonList(annotationTree));
    }  

}
