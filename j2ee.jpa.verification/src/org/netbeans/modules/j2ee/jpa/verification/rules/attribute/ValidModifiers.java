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
import javax.lang.model.element.Modifier;
import org.netbeans.modules.j2ee.jpa.model.AttributeWrapper;
import org.netbeans.modules.j2ee.jpa.verification.JPAEntityAttributeCheck;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Rule;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Id;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
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
        
        Set<Modifier> fieldModifiers = attrib.getInstanceVariable() == null ? null
                : attrib.getInstanceVariable().getModifiers();
        
        Set<Modifier> accesorModifiers = attrib.getAccesor() == null ? null
                : attrib.getAccesor().getModifiers();
        
        Set<Modifier> mutatorModifiers = attrib.getMutator() == null ? null
                : attrib.getMutator().getModifiers();
        
        List<ErrorDescription> errors = new ArrayList<ErrorDescription>();
        
        if (fieldModifiers != null){
            if (fieldModifiers.contains(Modifier.PUBLIC)){
                errors.add(Rule.createProblem(attrib.getInstanceVariable(), ctx,
                        NbBundle.getMessage(ValidModifiers.class, "MSG_PublicVariable")));
            }
        }
        
        if (accesorModifiers != null){
            if (!accesorModifiers.contains(Modifier.PUBLIC)
                    && !accesorModifiers.contains(Modifier.PROTECTED)){
                errors.add(Rule.createProblem(attrib.getAccesor(), ctx,
                        NbBundle.getMessage(ValidModifiers.class, "MSG_NonPublicAccesor")));
            }
            
            if (accesorModifiers.contains(Modifier.FINAL)){
                errors.add(Rule.createProblem(attrib.getAccesor(), ctx,
                        NbBundle.getMessage(ValidModifiers.class, "MSG_FinalAccesor")));
            }
        }
        
        if (mutatorModifiers != null){
            if (!mutatorModifiers.contains(Modifier.PUBLIC)
                    && !mutatorModifiers.contains(Modifier.PROTECTED)){
                errors.add(Rule.createProblem(attrib.getMutator(), ctx,
                        NbBundle.getMessage(ValidModifiers.class, "MSG_NonPublicMutator")));
            }
            // see issue #108876
//            else if (attrib.getModelElement() instanceof Id
//                    && mutatorModifiers.contains(Modifier.PUBLIC)){
//                errors.add(Rule.createProblem(attrib.getMutator(), ctx,
//                        NbBundle.getMessage(ValidModifiers.class, "MSG_PublicIdMutatorDiscouraged"),
//                        Severity.WARNING));
//            }
            
            if (mutatorModifiers.contains(Modifier.FINAL)){
                errors.add(Rule.createProblem(attrib.getMutator(), ctx,
                        NbBundle.getMessage(ValidModifiers.class, "MSG_FinalMutator")));
            }
        }
        
        return errors.toArray(new ErrorDescription[errors.size()]);
    }
}
