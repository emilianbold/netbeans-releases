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
import java.util.TreeSet;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.j2ee.jpa.model.AttributeWrapper;
import org.netbeans.modules.j2ee.jpa.verification.JPAEntityAttributeCheck;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Rule;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Version;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 * @author Tomasz.Slota@Sun.COM
 */
public class ValidVersionType extends JPAEntityAttributeCheck {
    private static Collection<String> validVersionTypes = new TreeSet<String>(Arrays.asList(
            "java.lang.Short", "java.lang.Integer",  // NOI18N
            "java.lang.Long", "short", "int", "long", // NOI18N
            "java.sql.Timestamp" // NOI18N
            ));
    
    public ErrorDescription[] check(JPAProblemContext ctx, AttributeWrapper attrib) {
        if (attrib.getModelElement() instanceof Version){
            TreeUtilities treeUtils = ctx.getCompilationInfo().getTreeUtilities();
            Types types = ctx.getCompilationInfo().getTypes();
            TypeMirror attrType = attrib.getType();
            
            for (String typeName : validVersionTypes){
                TypeMirror type = treeUtils.parseType(typeName,
                        ctx.getJavaClass());
                
                if (type != null && types.isSameType(attrType, type)){
                    return null;
                }
            }
            
            return new ErrorDescription[]{Rule.createProblem(attrib.getJavaElement(),
                    ctx, NbBundle.getMessage(ValidVersionType.class,
                    "MSG_InvalidVersionType"))};
            
        }
        
        return null;       
    }
}
