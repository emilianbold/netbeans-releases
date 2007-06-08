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
import java.util.LinkedList;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.EJBVerificationRule;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class LegalModifiers extends EJBVerificationRule {
    
    public Collection<ErrorDescription> check(EJBProblemContext ctx) {
        
        if (ctx.getEjb() != null){
            Collection<ErrorDescription> problemsFounds = new LinkedList<ErrorDescription>();
            
            Set<Modifier> modifiers = ctx.getClazz().getModifiers();
            
            if (!modifiers.contains(Modifier.PUBLIC)){
                ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), ctx.getComplilationInfo(),
                NbBundle.getMessage(LegalModifiers.class, "MSG_BeanClassMustBePublic"));
                
                problemsFounds.add(err);
            }
            
            if (modifiers.contains(Modifier.FINAL)){
                ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), ctx.getComplilationInfo(),
                NbBundle.getMessage(LegalModifiers.class, "MSG_BeanClassMustNotBeFinal"));
                
                problemsFounds.add(err);
            }
            
            if (modifiers.contains(Modifier.ABSTRACT)){
                ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), ctx.getComplilationInfo(),
                NbBundle.getMessage(LegalModifiers.class, "MSG_BeanClassMustNotBeAbstract"));
                
                problemsFounds.add(err);
            }
            
            return problemsFounds;
        }
        
        return null;
    }
    
}
