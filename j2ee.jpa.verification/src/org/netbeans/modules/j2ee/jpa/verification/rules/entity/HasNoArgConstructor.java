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

import java.util.Arrays;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 * The class must have a public or protected, no-argument constructor.
 * 
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
public class HasNoArgConstructor extends JPAClassRule {
    
    /** Creates a new instance of HasNoArgConstructor */
    public HasNoArgConstructor() {
        setClassContraints(Arrays.asList(ClassConstraints.ENTITY,
                ClassConstraints.EMBEDDABLE, ClassConstraints.IDCLASS));
    }
    
    @Override public ErrorDescription[] apply(TypeElement subject, ProblemContext ctx){
        for (Element element : subject.getEnclosedElements()){
            if (element.getKind() == ElementKind.CONSTRUCTOR){
                ExecutableElement constr = (ExecutableElement) element;
                
                if (constr.getParameters().size() == 0
                        && (constr.getModifiers().contains(Modifier.PUBLIC)
                        || constr.getModifiers().contains(Modifier.PROTECTED))){
                    return null; // found appropriate constructor
                }
            }
        }
        
        return new ErrorDescription[]{createProblem(subject, ctx)};
    }
    
    public String getDescription(){
        return NbBundle.getMessage(IdDefinedInHierarchy.class, "MSG_HasNoNoArgConstructor");
    }
}
