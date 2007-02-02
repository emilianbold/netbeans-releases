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

package org.netbeans.modules.j2ee.jpa.verification;

import java.util.Collection;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Rule;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public abstract class JPAClassRule extends Rule<TypeElement> {
    public enum ClassConstraints {ENTITY, EMBEDDABLE, IDCLASS}
    private Collection<ClassConstraints> classContraints;
    
    protected void setClassContraints(Collection<ClassConstraints> classContraints){
        this.classContraints = classContraints;
    }
    
    @Override protected boolean isApplicable(TypeElement subject, ProblemContext ctx) {
        JPAProblemContext jpaCtx = (JPAProblemContext)ctx;
        
        if (classContraints != null && !(
                jpaCtx.isEntity() && classContraints.contains(ClassConstraints.ENTITY)
                || jpaCtx.isEmbeddable() && classContraints.contains(ClassConstraints.EMBEDDABLE)
                || jpaCtx.isIdClass() && classContraints.contains(ClassConstraints.IDCLASS))){
            return false;
        }
        
        return super.isApplicable(subject, ctx);
    }
}
