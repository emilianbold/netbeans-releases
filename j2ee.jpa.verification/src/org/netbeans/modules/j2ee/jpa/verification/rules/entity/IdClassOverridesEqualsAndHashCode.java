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

import java.util.Collections;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule.ClassConstraints;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
public class IdClassOverridesEqualsAndHashCode extends JPAClassRule {
    /** Creates a new instance of IdClassOverridesEqualsMethod */
    public IdClassOverridesEqualsAndHashCode() {
        setClassContraints(Collections.singleton(ClassConstraints.IDCLASS));
    }
    
    @Override public ErrorDescription[] apply(TypeElement subject, ProblemContext ctx){
        boolean hasEquals = false;
        boolean hasHashCode = false;
        
        for (ExecutableElement method : ElementFilter.methodsIn(subject.getEnclosedElements())){
            String methodName = method.getSimpleName().toString();
            
            if ("equals".equals(methodName) //NOI18N
                    && method.getParameters().size() == 1){
                
                if ("java.lang.Object".equals(method.getParameters().get(0).asType().toString())){ //NOI18N
                    hasEquals = true;
                }
            }
            else{
                if ("hashCode".equals(methodName) && method.getParameters().size() == 0){ //NOI18N
                    hasHashCode = true;
                }
            }
            
            if (hasHashCode && hasEquals){
                return null;
            }
        }
        
        return new ErrorDescription[]{createProblem(subject, ctx)};
    }
    
    @Override public String getDescription(){
        return NbBundle.getMessage(IdDefinedInHierarchy.class, "MSG_IdClassDoesNotOverrideEquals");
    }
}
