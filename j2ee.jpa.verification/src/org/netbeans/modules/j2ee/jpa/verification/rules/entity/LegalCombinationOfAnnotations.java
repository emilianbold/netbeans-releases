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
package org.netbeans.modules.j2ee.jpa.verification.rules.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule.ClassConstraints;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;
import static org.netbeans.modules.j2ee.jpa.model.JPAAnnotations.*;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class LegalCombinationOfAnnotations extends JPAClassRule {
    //TODO: Add more rules
    private static Collection<IllegalCombination> illegalClassAnnotationCombinations = Arrays.asList(
            new IllegalCombination(Collections.singleton(ENTITY), Arrays.asList(EMBEDDABLE, MAPPED_SUPERCLASS))
            );
    private static Collection<IllegalCombination> illegalAttrAnnotationCombinations = Arrays.asList(
            );
    
    public LegalCombinationOfAnnotations() {
        setClassContraints(Arrays.asList(ClassConstraints.ENTITY,
                ClassConstraints.EMBEDDABLE,
                ClassConstraints.MAPPED_SUPERCLASS));
    }
    
    @Override public ErrorDescription[] apply(TypeElement subject, ProblemContext ctx){
        List<ErrorDescription> problemsFound = new ArrayList<ErrorDescription>();
        
        Collection<String> annotationsOnClass = ModelUtils.extractAnnotationNames(subject);
        
        for (IllegalCombination ic : illegalClassAnnotationCombinations){
            ic.check(ctx, subject, problemsFound, annotationsOnClass);
        }
        
        for (Element elem : subject.getEnclosedElements()){
            Collection<String> annotationsOnElement = ModelUtils.extractAnnotationNames(elem);
            
            for (IllegalCombination ic : illegalAttrAnnotationCombinations){
                ic.check(ctx, elem, problemsFound, annotationsOnElement);
            }
        }
        
        return problemsFound.toArray(new ErrorDescription[problemsFound.size()]);
    }
    
    private static class IllegalCombination{
        private Collection<String> set1;
        private Collection<String> set2;
        
        IllegalCombination(Collection<String> set1, Collection<String> set2){
            this.set1 = set1;
            this.set2 = set2;
        }
        
        void check(ProblemContext ctx,
                Element elem,
                Collection<ErrorDescription> errorList,
                Collection<String> annotations){
            
            for (String ann : annotations){
                if (set1.contains(ann)){
                    for (String forbiddenAnn : set2){
                        if (annotations.contains(forbiddenAnn)){
                            ErrorDescription error = createProblem(elem, ctx,
                                    NbBundle.getMessage(LegalCombinationOfAnnotations.class,
                                    "MSG_IllegalAnnotationCombination",
                                    ModelUtils.shortAnnotationName(ann),
                                    ModelUtils.shortAnnotationName(forbiddenAnn)));
                            
                            errorList.add(error);
                        }
                    }
                }
            }
        }
    }
}
