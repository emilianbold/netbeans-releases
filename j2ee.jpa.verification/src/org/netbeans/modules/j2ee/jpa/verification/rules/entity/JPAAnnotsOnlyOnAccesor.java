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
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.j2ee.jpa.model.AccessType;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 * Only accesor methods can have JPA Annotations
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class JPAAnnotsOnlyOnAccesor extends JPAClassRule {
    
    public JPAAnnotsOnlyOnAccesor() {
        setClassContraints(Arrays.asList(ClassConstraints.ENTITY,
                ClassConstraints.EMBEDDABLE,
                ClassConstraints.MAPPED_SUPERCLASS));
    }
    
    @Override public ErrorDescription[] apply(TypeElement subject, ProblemContext ctx){
        
        if (((JPAProblemContext)ctx).getAccessType() != AccessType.PROPERTY){
            return null;
        }
        
        List<ErrorDescription> problemsFound = new ArrayList<ErrorDescription>();
        
        for (ExecutableElement method : ElementFilter.methodsIn(subject.getEnclosedElements())){
            if (!(method.getSimpleName().toString().startsWith("get") && //NOI18N
                    method.getParameters().size() == 0)){
                for (String annotName : ModelUtils.extractAnnotationNames(method)){
                    if (JPAAnnotations.MEMBER_LEVEL.contains(annotName)){
                        ErrorDescription error = createProblem(method, ctx,
                                    NbBundle.getMessage(JPAAnnotsOnlyOnAccesor.class,
                                    "MSG_JPAAnnotsOnlyOnAccesor",
                                    ModelUtils.shortAnnotationName(annotName)));
                                    
                        problemsFound.add(error);
                        break;
                    }
                }
            }
        }
        
        return problemsFound.toArray(new ErrorDescription[problemsFound.size()]);
    }
    
}
