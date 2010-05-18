/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.j2ee.jpa.verification.rules.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.jpa.model.AccessType;
import org.netbeans.modules.j2ee.jpa.model.AttributeWrapper;
import org.netbeans.modules.j2ee.jpa.model.ModelUtils;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule;
import org.netbeans.modules.j2ee.jpa.verification.JPAEntityAttributeCheck;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.rules.attribute.GeneratedValueIsId;
import org.netbeans.modules.j2ee.jpa.verification.rules.attribute.MVRelationshipForEntityTypeAttrDefined;
import org.netbeans.modules.j2ee.jpa.verification.rules.attribute.RelationshipForEntityTypeAttrDefined;
import org.netbeans.modules.j2ee.jpa.verification.rules.attribute.TemporalFieldsAnnotated;
import org.netbeans.modules.j2ee.jpa.verification.rules.attribute.ValidBasicType;
import org.netbeans.modules.j2ee.jpa.verification.rules.attribute.ValidColumnName;
import org.netbeans.modules.j2ee.jpa.verification.rules.attribute.ValidModifiers;
import org.netbeans.modules.j2ee.jpa.verification.rules.attribute.ValidVersionType;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Basic;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Embeddable;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Id;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.MappedSuperclass;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Version;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**
 * Perform checks on the attributes
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class ValidAttributes extends JPAClassRule {
    private static JPAEntityAttributeCheck[] attribChecks = new JPAEntityAttributeCheck[]{
        new ValidColumnName(),
        new ValidModifiers(),
        new ValidBasicType(),
        new RelationshipForEntityTypeAttrDefined(),
        new MVRelationshipForEntityTypeAttrDefined(),
        new ValidVersionType(),
        new GeneratedValueIsId(),
        new TemporalFieldsAnnotated()
    };
    
    public ValidAttributes() {
        setClassContraints(Arrays.asList(ClassConstraints.ENTITY,
                ClassConstraints.EMBEDDABLE,
                ClassConstraints.MAPPED_SUPERCLASS));
    }
    
    @Override public ErrorDescription[] apply(TypeElement subject, ProblemContext ctx){
        List<ErrorDescription> problemsFound = new ArrayList<ErrorDescription>();
        List <AttributeWrapper> attrs = new ArrayList<AttributeWrapper>();
        Basic basicFields[] = null;
        Id idFields[] = null;
        Version versionFields[] = null;
        
        if (ctx.getModelElement() instanceof Entity){
            Entity entity = (Entity)ctx.getModelElement();
            basicFields = entity.getAttributes().getBasic();
            
            idFields = entity.getAttributes().getId();
            versionFields = entity.getAttributes().getVersion();
        }
        else if (ctx.getModelElement() instanceof Embeddable){
            Embeddable embeddable = (Embeddable)ctx.getModelElement();
            basicFields = embeddable.getAttributes().getBasic();
            
        } else if (ctx.getModelElement() instanceof MappedSuperclass){
            MappedSuperclass mappedSuperclass = (MappedSuperclass)ctx.getModelElement();
            basicFields = mappedSuperclass.getAttributes().getBasic();
            
            idFields = mappedSuperclass.getAttributes().getId();
            versionFields = mappedSuperclass.getAttributes().getVersion();
        }
        
        if (basicFields != null){
            for (Basic basic: basicFields){
                attrs.add(new AttributeWrapper(basic));
            }
        }
        
        if (idFields != null){
            for (Id id: idFields){
                attrs.add(new AttributeWrapper(id));
            }
        }
        
        if (versionFields != null){
            for (Version version : versionFields){
                attrs.add(new AttributeWrapper(version));
            }
        }
        
        JPAProblemContext jpaCtx = (JPAProblemContext)ctx;
        
        if (jpaCtx.getAccessType() == AccessType.INCONSISTENT){
            JPAProblemFinder.LOG.fine("Skipping attribute checks due to inconsistent access type");
        } else {
            for (AttributeWrapper attr : attrs){
                ModelUtils.resolveJavaElementFromModel(jpaCtx, attr);
                
                if (attr.getJavaElement() == null){
                    JPAProblemFinder.LOG.severe("Failed to resolve java model element for JPA merged model element "
                            + subject.getSimpleName() + "." + attr.getName());
                } else{
                    for (JPAEntityAttributeCheck check : attribChecks){
                        ErrorDescription[] attrProblems = check.check(jpaCtx, attr);
                        
                        if (attrProblems != null){
                            for (ErrorDescription err : attrProblems){
                                problemsFound.add(err);
                            }
                        }
                    }
                }
            }
        }
        
        return problemsFound.toArray(new ErrorDescription[problemsFound.size()]);
    }
}
