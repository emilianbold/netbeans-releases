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
package org.netbeans.modules.j2ee.jpa.verification.rules.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import org.netbeans.modules.j2ee.jpa.model.AccessType;
import org.netbeans.modules.j2ee.jpa.model.AttributeWrapper;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAEntityAttributeCheck;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Rule;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 * - accesor method for persistent field *must* be:
 *   - public or protected
 *   - non-final
 * - persistent fields *must not* be public
 * @author Tomasz.Slota@Sun.COM
 */
public class ValidModifiers extends JPAEntityAttributeCheck {
    
    public ErrorDescription[] check(JPAProblemContext ctx, AttributeWrapper attrib) {
        VariableElement field = null;
        ExecutableElement accesor = null;
        
        if (ctx.getAccessType() == AccessType.FIELD){
            field = (VariableElement) attrib.getJavaElement();
            accesor = ModelUtils.getAccesor(ctx.getJavaClass(), field.getSimpleName().toString());
        } else if (ctx.getAccessType() == AccessType.PROPERTY) {
            accesor = (ExecutableElement) attrib.getJavaElement();
            String accesorName = ModelUtils.getFieldNameFromAccessor(accesor.getSimpleName().toString());
            field = ModelUtils.getField(ctx.getJavaClass(), accesorName);
        } else {
            return null;
        }
        
        if (field != null && accesor != null){
            Set<Modifier> fieldModifiers = field.getModifiers();
            Set<Modifier> accesorModifiers = accesor.getModifiers();
            
            List<ErrorDescription> errors = new ArrayList<ErrorDescription>();
            
            if (fieldModifiers.contains(Modifier.PUBLIC)){
                errors.add(Rule.createProblem(field, ctx,
                        NbBundle.getMessage(ValidModifiers.class, "MSG_PublicVariable")));
            }
            
            if (!accesorModifiers.contains(Modifier.PUBLIC) 
                    && !accesorModifiers.contains(Modifier.PROTECTED)){
                errors.add(Rule.createProblem(accesor, ctx,
                        NbBundle.getMessage(ValidModifiers.class, "MSG_NonPublicAccesor")));
            }
            
            if (accesorModifiers.contains(Modifier.FINAL)){
                errors.add(Rule.createProblem(accesor, ctx,
                        NbBundle.getMessage(ValidModifiers.class, "MSG_FinalAccesor")));
            }
            
            return errors.toArray(new ErrorDescription[errors.size()]);
        }
        return null;
    }
}
