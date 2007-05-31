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
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.jpa.model.AttributeWrapper;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAEntityAttributeCheck;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Rule;
import org.netbeans.modules.j2ee.jpa.verification.fixes.CreateUnidirOneToOneRelationship;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class RelationshipForEntityTypeAttrDefined extends JPAEntityAttributeCheck  {
    
    public ErrorDescription[] check(JPAProblemContext ctx, AttributeWrapper attrib) {
        
        Element typeElement = ctx.getCompilationInfo().getTypes().asElement(attrib.getType());
        
        if (typeElement != null && typeElement.getKind() == ElementKind.CLASS){
            Entity entity = ModelUtils.getEntity(ctx.getMetaData(), ((TypeElement)typeElement));
            
            if (entity != null){
                ElementHandle<TypeElement> classHandle = ElementHandle.create(ctx.getJavaClass());
                ElementHandle<Element> elemHandle = ElementHandle.create(attrib.getJavaElement());
                
                Fix fix1 = new CreateUnidirOneToOneRelationship(ctx.getFileObject(),
                        classHandle, elemHandle);
                
                return new ErrorDescription[]{Rule.createProblem(attrib.getJavaElement(),
                        ctx, NbBundle.getMessage(RelationshipForEntityTypeAttrDefined.class,
                        "MSG_EntityRelationNotDefined"),
                        Severity.WARNING,
                        Arrays.asList(fix1))};
            }
        }
        
        return null;
    }
    
}
