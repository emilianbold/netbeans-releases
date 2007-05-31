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
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.jpa.model.AttributeWrapper;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAEntityAttributeCheck;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Rule;
import org.netbeans.modules.j2ee.jpa.verification.fixes.CreateUnidirManyToOneRelationship;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;

/**
 * if there is attr of type Collection<EntityType>
 * a multi-valued relationship should be defined for it
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class MVRelationshipForEntityTypeAttrDefined extends JPAEntityAttributeCheck  {
    
    public ErrorDescription[] check(JPAProblemContext ctx, AttributeWrapper attrib) {
        
        TypeMirror type = attrib.getType();
        
        if (type.getKind() == TypeKind.DECLARED){
            List<? extends TypeMirror> typeArgs = ((DeclaredType)type).getTypeArguments();
            
            if (typeArgs.size() == 1){
                Element typeElement = ctx.getCompilationInfo().getTypes().asElement(typeArgs.get(0));
                
                if (typeElement != null && typeElement.getKind() == ElementKind.CLASS){
                    Entity entity = ModelUtils.getEntity(ctx.getMetaData(), ((TypeElement)typeElement));
                    
                    if (entity != null){
                        ElementHandle<TypeElement> classHandle = ElementHandle.create(ctx.getJavaClass());
                        ElementHandle<Element> elemHandle = ElementHandle.create(attrib.getJavaElement());
                        
                        Fix fix1 = new CreateUnidirManyToOneRelationship(ctx.getFileObject(),
                                classHandle, elemHandle);
                        
                        return new ErrorDescription[]{Rule.createProblem(attrib.getJavaElement(),
                                ctx, NbBundle.getMessage(MVRelationshipForEntityTypeAttrDefined.class,
                                "MSG_MVEntityRelationNotDefined"),
                                Severity.WARNING,
                                Arrays.asList(fix1))};
                    }
                }
            }
        }
        
        return null;
    }
}
