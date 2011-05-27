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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.eclipse.persistence.jpa.jpql.JPQLQueryHelper;
import org.eclipse.persistence.jpa.jpql.JPQLQueryProblem;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule.ClassConstraints;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.modules.j2ee.persistence.spi.jpql.ManagedTypeProvider;
import org.netbeans.modules.j2ee.persistence.spi.jpql.Query;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;

/**
 * Verify content of @NamedQuery query
 */
public class JPQLValidation extends JPAClassRule {
    
    /** Creates a new instance of NonFinalClass */
    public JPQLValidation() {
        setClassContraints(Arrays.asList(ClassConstraints.ENTITY,
                ClassConstraints.EMBEDDABLE,
                ClassConstraints.MAPPED_SUPERCLASS));
    }
    
    @Override public ErrorDescription[] apply(TypeElement subject, ProblemContext ctx){
        List<AnnotationMirror> first = Utilities.findAnnotations(subject, JPAAnnotations.NAMED_QUERY);
        ArrayList<String> values = new ArrayList<String>();
        if(first == null || first.size()==0){
            AnnotationMirror qs = Utilities.findAnnotation(subject, JPAAnnotations.NAMED_QUERIES);
            if(qs != null){
                Map<? extends ExecutableElement, ? extends AnnotationValue> maps = qs.getElementValues();
                for(AnnotationValue vl:maps.values()){
                    List  lst = (List) vl.getValue();
                    for(Object val:lst){
                        if(val instanceof AnnotationMirror){
                            AnnotationMirror am = (AnnotationMirror) val;
                            if(JPAAnnotations.NAMED_QUERY.equals(am.getAnnotationType().toString())){
                                values.add(Utilities.getAnnotationAttrValue(am, "query").toString());
                            }
                        }
                    }
                }
            }
        }
        else {
            for(AnnotationMirror mr:first)values.add(Utilities.getAnnotationAttrValue(mr, "query").toString());
        }
        JPQLQueryHelper helper = new JPQLQueryHelper();
        Project project = FileOwnerQuery.getOwner(ctx.getFileObject());
        List<JPQLQueryProblem> problems = new ArrayList<JPQLQueryProblem>();
        for(String value:values){
            helper.setQuery(new Query(value, new ManagedTypeProvider(project)));
            List<JPQLQueryProblem> tmp = helper.validateGrammar();
            if(tmp!=null && tmp.size()>0)problems.addAll(tmp);
        }
        if (problems != null && problems.size()>0){
            return new ErrorDescription[]{createProblem(subject, ctx, "JPQL :" + problems, Severity.WARNING)};
        }
        return null;
    }
    
    @Override protected boolean isApplicable(TypeElement subject, ProblemContext ctx) {
        JPAProblemContext jpaCtx = (JPAProblemContext)ctx;
        
        return (jpaCtx.isEntity() || jpaCtx.isMappedSuperClass());
    }
    
    private AnnotationMirror getFirstAnnotationFromGivenSet(TypeElement subject,
            Collection<String> annotationClasses){
        
        for (String annClass : annotationClasses){
            AnnotationMirror foundAnn = Utilities.findAnnotation(subject, annClass);
            
            if (foundAnn != null){
                return foundAnn;
            }
        }
        
        return null;
    }
}
