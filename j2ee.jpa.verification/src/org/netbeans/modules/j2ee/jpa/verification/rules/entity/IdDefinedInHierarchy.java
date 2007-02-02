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
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.j2ee.jpa.model.JPAHelper;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 * An entity hierarchy must have an Id. It could be a simple Id, cmposite Id or
 * embedded Id. In case of simple and composite id, it must have atleast one
 * field or property annotated as Id, where as in case of emdebedded id, it must
 * have a field or property annotated as Embedded.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
public class IdDefinedInHierarchy extends JPAClassRule {
    
    public IdDefinedInHierarchy(){
        setClassContraints(Collections.singleton(ClassConstraints.ENTITY));
    }
    
    @Override public ErrorDescription[] apply(TypeElement subject, ProblemContext ctx){
        TypeElement javaClass = subject;
        
        do{
            if (JPAHelper.isAnyMemberAnnotatedAsIdOrEmbeddedId(javaClass)){
                return null; // OK
            }
            
            TypeMirror parentType = javaClass.getSuperclass();
            javaClass = null;
            
            if (!"java.lang.Object".equals(parentType.toString())){ //NOI18N
                if (parentType.getKind() == TypeKind.DECLARED){
                    Element parent = ((DeclaredType)parentType).asElement();
                    
                    if (parent.getKind() == ElementKind.CLASS){
                        javaClass = (TypeElement) parent;
                    }
                }
            }
            
        } while (javaClass != null);
        
        return new ErrorDescription[]{createProblem(subject, ctx)};
    }
    
    public String getDescription(){
        return NbBundle.getMessage(IdDefinedInHierarchy.class, "MSG_NoIdDefinedInHierarchy");
    }
}
