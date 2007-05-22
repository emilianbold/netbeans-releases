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

import java.util.Arrays;
import java.util.Collection;
import org.netbeans.modules.j2ee.jpa.model.AttributeWrapper;
import org.netbeans.modules.j2ee.jpa.verification.JPAEntityAttributeCheck;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Rule;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class TemporalFieldsAnnotated extends JPAEntityAttributeCheck {
    private static Collection<String> temporalTypes = 
            Arrays.asList("java.util.Calendar", "java.util.Date"); //NOI18N

    public ErrorDescription[] check(JPAProblemContext ctx, AttributeWrapper attrib) {
        String temporal = attrib.getTemporal();
        
        if (temporal == null || temporal.length() == 0){
            if (temporalTypes.contains(attrib.getInstanceVariable().asType().toString())){
                return new ErrorDescription[]{Rule.createProblem(attrib.getJavaElement(), ctx,
                    NbBundle.getMessage(ValidColumnName.class,
                    "MSG_TemporalAttrNotAnnotatedProperly"))};
            }
        }
        
        return null;        
    }

}
