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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.wizards;

import java.io.IOException;
import java.util.Collections;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;

import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

import org.netbeans.modules.j2ee.common.source.AbstractTask;
import org.netbeans.modules.j2ee.common.source.GenerationUtils;

/**
 * Generator of attributes for tag handler class
 *
 * @author  milan.kuchtiak@sun.com
 * Created on May, 2004
 */
public class TagHandlerGenerator {
    private Object[][] attributes;
    private JavaSource clazz;
    private boolean isBodyTag;
    private boolean evaluateBody;

    /** Creates a new instance of ListenerGenerator */
    public TagHandlerGenerator(JavaSource clazz, Object[][] attributes, boolean isBodyTag, boolean evaluateBody) {
        this.clazz=clazz;
        this.attributes=attributes;
        this.isBodyTag=isBodyTag;
        this.evaluateBody=evaluateBody;
    }
    
    public void generate() throws IOException {
        AbstractTask task = new AbstractTask<WorkingCopy>() {
            public void run(WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                GenerationUtils gu = GenerationUtils.newInstance(workingCopy);
                ClassTree oldClassTree = gu.getClassTree();
                ClassTree classTree = addFields(gu, make, oldClassTree);
                if (isBodyTag) {
                    classTree = make.addClassMember(classTree, addBodyEvaluatorCheck(evaluateBody, make));
                }
                classTree = addSetters(gu, make, classTree);
                workingCopy.rewrite(oldClassTree, classTree);
            }
        };
        ModificationResult result = clazz.runModificationTask(task);
        result.commit();
    }
    
    private ClassTree addFields(GenerationUtils gu, TreeMaker make, ClassTree classTree) throws IOException {
        for (int i = 0; i < attributes.length; i++) {
            VariableTree field = gu.createField(Modifier.PRIVATE, (String) attributes[i][1], (String) attributes[i][0]);
            classTree = make.insertClassMember(classTree, i, field);
            
            //TODO: generate Javadoc
        }
        
        return classTree;
    }

    private ClassTree addSetters(GenerationUtils gu, TreeMaker make, ClassTree newClassTree) throws IOException {
        for (int i = 0; i < attributes.length; i++) {
            MethodTree setter = gu.createPropertySetterMethod((String) attributes[i][1], (String) attributes[i][0]);
            newClassTree = make.addClassMember(newClassTree, setter);
            
            //TODO: generate Javadoc
        }
        
        return newClassTree;
    }

    private MethodTree addBodyEvaluatorCheck(boolean evaluateBody, TreeMaker make) throws IOException {        
        StringBuffer methodBody = new StringBuffer();
        methodBody.append("{\n//"); //NOI18N
        methodBody.append("\n// TODO: code that determines whether the body should be"); //NOI18N
        methodBody.append("\n//       evaluated should be placed here."); //NOI18N
        methodBody.append("\n//       Called from the doStartTag() method."); //NOI18N
        methodBody.append("\n//"); //NOI18N
        methodBody.append("\nreturn " + (evaluateBody ? "true;" : "false;")); //NOI18N
        methodBody.append("\n}"); //NOI18N
        
        MethodTree method = make.Method(
                make.Modifiers(Collections.<Modifier>singleton(Modifier.PRIVATE)),
                "theBodyShouldBeEvaluated", //NOI18N
                make.PrimitiveType(TypeKind.BOOLEAN),
                Collections.<TypeParameterTree>emptyList(),
                Collections.<VariableTree>emptyList(),
                Collections.<ExpressionTree>emptyList(),
                methodBody.toString(),
                null);
        
        //TODO: generate Javadoc
        
        return method;
    }

}