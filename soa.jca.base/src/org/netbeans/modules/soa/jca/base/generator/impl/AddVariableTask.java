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

package org.netbeans.modules.soa.jca.base.generator.impl;

import org.netbeans.modules.soa.jca.base.generator.api.GeneratorUtil;
import org.netbeans.modules.soa.jca.base.generator.api.ModificationTask;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.VariableTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author echou
 */
public class AddVariableTask<T extends WorkingCopy> extends ModificationTask<WorkingCopy> {

    private Exception myException = null;

    private String varType;
    private String varName;
    private String annotationType;
    private Map<String, Object> annotationArguments;

    public Exception getException() {
        return myException;
    }

    public AddVariableTask(String varType, String varName,
            String annotationType, Map<String, Object> annotationArguments) {
        this.varType = varType;
        this.varName = varName;
        this.annotationType = annotationType;
        this.annotationArguments = annotationArguments;
    }

    public void run(WorkingCopy workingCopy) throws Exception {
        try {
            workingCopy.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);

            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            ClassTree classTree = (ClassTree) cut.getTypeDecls().get(0);

            TreeMaker make = workingCopy.getTreeMaker();

            ModifiersTree modifiers = handleModifiersAndAnnotations(make, workingCopy,
                    annotationType, annotationArguments);

            VariableTree variableTree = GeneratorUtil.createField(make, workingCopy,
                modifiers,
                varName,
                varType,
                null
            );

            ClassTree newClassTree = make.addClassMember(classTree, variableTree);
            workingCopy.rewrite(classTree, newClassTree);
        } catch (Exception e) {
            myException = e;
            throw e;
        }
    }

    private ModifiersTree handleModifiersAndAnnotations(TreeMaker make, WorkingCopy workingCopy,
            String annotationType, Map<String, Object> annotationArguments) throws Exception {
        Set<Modifier> modifierSet = EnumSet.of(Modifier.PRIVATE);

        List<ExpressionTree> annoArgList = new ArrayList<ExpressionTree> ();
        for (String key : annotationArguments.keySet()) {
            Object val = annotationArguments.get(key);
            ExpressionTree annoArg = GeneratorUtil.createAnnotationArgument(make, key, val);
            annoArgList.add(annoArg);
        }
        AnnotationTree annotationTree = GeneratorUtil.createAnnotation(make, workingCopy,
                    annotationType, annoArgList);

        return make.Modifiers(modifierSet, Collections.singletonList(annotationTree));
    }

}
