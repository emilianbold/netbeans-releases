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
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule.ClassConstraints;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 * An entity sub-class can not have Id field or property. Because that will lead
 * to multiple Ids in hierarchy. Thus, an entity subclass can not also have
 * IdClass.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
public class NoIdClassOnEntitySubclass extends JPAClassRule {
    
    /** Creates a new instance of NoIdClassOnEntitySubclass */
    public NoIdClassOnEntitySubclass() {
        // not applicable to MappedSuperclass
        setClassContraints(Collections.singleton(ClassConstraints.ENTITY));
    }
    
    @Override public ErrorDescription[] apply(TypeElement subject, ProblemContext ctx){
        if (!Utilities.hasAnnotation(subject, JPAAnnotations.ID_CLASS)){
            
            TypeMirror superClassType = subject.getSuperclass();
            
            if (superClassType.getKind() == TypeKind.DECLARED){
                Element superClassElem = ((DeclaredType)superClassType).asElement();
                
                if (Utilities.hasAnnotation(superClassElem, JPAAnnotations.ENTITY)){
                    return new ErrorDescription[]{createProblem(subject, ctx)};
                }
            }
        }
        
        return null;
    }
    
    @Override public String getDescription(){
        return NbBundle.getMessage(IdDefinedInHierarchy.class, "MSG_EntitySubclassHasIdClass");
    }
    
}
