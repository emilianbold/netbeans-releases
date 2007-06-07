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
package org.netbeans.modules.j2ee.ejbverification.rules;

import java.util.Collection;
import java.util.Collections;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.EJBVerificationRule;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class HasNoArgContructor extends EJBVerificationRule {
    
    public Collection<ErrorDescription> check(EJBProblemContext ctx) {
        boolean hasDefaultContructor = true;
        
        for (ExecutableElement constr : ElementFilter.constructorsIn(ctx.getClazz().getEnclosedElements())){
            hasDefaultContructor = false;
            
            if (constr.getParameters().size() == 0
                    && (constr.getModifiers().contains(Modifier.PUBLIC)
                    || constr.getModifiers().contains(Modifier.PROTECTED))){
                return null; // found appropriate constructor
            }
        }
        
        if (hasDefaultContructor){
            return null; // OK
        }
        
        ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), ctx.getComplilationInfo(),
                NbBundle.getMessage(HasNoArgContructor.class, "MSG_HasNoNoArgConstructor"));
        
        return Collections.singletonList(err);
    }
    
}
