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
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.jpa.model.AccessType;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.fixes.UnifyAccessType;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class ConsistentAccessType extends JPAClassRule {
    
    /** Creates a new instance of ConsistentAccessType */
    public ConsistentAccessType() {
        setClassContraints(Arrays.asList(ClassConstraints.ENTITY,
                ClassConstraints.MAPPED_SUPERCLASS, ClassConstraints.EMBEDDABLE, ClassConstraints.IDCLASS));
    }
    
    @Override public ErrorDescription[] apply(TypeElement subject, ProblemContext ctx){
        if (((JPAProblemContext)ctx).getAccessType() == AccessType.INCONSISTENT){
            ElementHandle<TypeElement> classHandle = ElementHandle.create(ctx.getJavaClass());
            
            Fix fix1 = new UnifyAccessType.UnifyFieldAccess(ctx.getFileObject(), classHandle);
            Fix fix2 = new UnifyAccessType.UnifyPropertyAccess(ctx.getFileObject(), classHandle);
            
            return new ErrorDescription[]{createProblem(subject, ctx,
                    NbBundle.getMessage(IdDefinedInHierarchy.class, "MSG_InconsistentAccessType"),
                    Severity.ERROR, Arrays.asList(fix1, fix2))};
        }
        
        return null;
    }
}
