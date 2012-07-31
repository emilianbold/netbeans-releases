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
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import org.eclipse.persistence.jpa.internal.jpql.JPQLQueryProblemResourceBundle;
import org.eclipse.persistence.jpa.jpql.JPQLQueryHelper;
import org.eclipse.persistence.jpa.jpql.JPQLQueryProblem;
import org.eclipse.persistence.jpa.jpql.spi.IQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.jpa.model.JPAAnnotations;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule.ClassConstraints;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Utilities;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.NamedQuery;
import org.netbeans.modules.j2ee.persistence.spi.jpql.ManagedTypeProvider;
import org.netbeans.modules.j2ee.persistence.spi.jpql.Query;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;

/**
 * Verify content of @NamedQuery query
 * TODO: good to move warning to query level instead of class level
 */
public class JPQLValidation extends JPAClassRule {
    
    /** Creates a new instance of NonFinalClass */
    public JPQLValidation() {
        setClassContraints(Arrays.asList(ClassConstraints.ENTITY,
                ClassConstraints.EMBEDDABLE,
                ClassConstraints.MAPPED_SUPERCLASS));
    }
    
    @Override public ErrorDescription[] apply(TypeElement subject, ProblemContext ctx){
        long start  = System.nanoTime();
        Object modEl = ctx.getModelElement();
        Entity entity = (Entity) (modEl instanceof Entity ? modEl : null);
        List<AnnotationMirror> first = Utilities.findAnnotations(subject, JPAAnnotations.NAMED_QUERY);
        ArrayList<String> values = new ArrayList<String>();
        ArrayList<String> names = new ArrayList<String>();
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
                                AnnotationValue qAttrValue = Utilities.getAnnotationAttrValue(am, "query");
                                AnnotationValue nmAttrValue = Utilities.getAnnotationAttrValue(am, "name");
                                if(qAttrValue != null){
                                    values.add(qAttrValue.getValue().toString());
                                    names.add(nmAttrValue == null ? "" : nmAttrValue.getValue().toString());
                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            for(AnnotationMirror mr:first){
                AnnotationValue qAttrValue = Utilities.getAnnotationAttrValue(mr, "query");
                AnnotationValue nmAttrValue = Utilities.getAnnotationAttrValue(mr, "name");
                if(qAttrValue != null){
                    values.add(qAttrValue.getValue().toString());
                    names.add(nmAttrValue == null ? "" : nmAttrValue.getValue().toString());
                }
            }
        }
        JPQLQueryHelper helper = new JPQLQueryHelper();
        Project project = FileOwnerQuery.getOwner(ctx.getFileObject());
        List<JPQLQueryProblem> problems = new ArrayList<JPQLQueryProblem>();
        ManagedTypeProvider mtp = new ManagedTypeProvider(project, ((JPAProblemContext)ctx).getMetaData());
        for(int index=0;index<values.size();index++){
            String value = values.get(index);
            String qName = names.get(index);
            NamedQuery nq = null;
            if(entity != null) {
                nq = entity.newNamedQuery();
                nq.setQuery(value);
                nq.setName(qName);
            }
            helper.setQuery(new Query(nq, value, mtp));
            List<JPQLQueryProblem> tmp = null;
            try{
                tmp = helper.validate();
            } catch (UnsupportedOperationException ex) {
                JPAProblemFinder.LOG.log(Level.INFO, "Unsupported jpql validation case: " + ex.getMessage(), ex);
            }catch (NullPointerException ex) {
                JPAProblemFinder.LOG.log(Level.INFO, "NPE in jpql validation: " + ex.getMessage(), ex);
            }
            if(tmp!=null && tmp.size()>0)problems.addAll(tmp);
            helper.dispose();
            //System.out.println("TIME TO COMPLETE: "+(System.nanoTime() - start));
        }
        if (problems != null && problems.size()>0){
            ErrorDescription[] ret = new ErrorDescription[problems.size()];
            for(int i=0;i<ret.length;i++){
                ListResourceBundle msgBundle = null;
                try{
                    msgBundle = (ListResourceBundle) ResourceBundle.getBundle(JPQLQueryProblemResourceBundle.class.getName());//NOI18N
                } catch (MissingResourceException ex) {//default en
                    msgBundle = (ListResourceBundle) ResourceBundle.getBundle(JPQLQueryProblemResourceBundle.class.getName(), Locale.ENGLISH);//NOI18N
                }
                String message = java.text.MessageFormat.format(msgBundle.getString(problems.get(i).getMessageKey()), (Object[])  problems.get(i).getMessageArguments());
                String pos = "["+problems.get(i).getStartPosition() + ";"+problems.get(i).getEndPosition()+"]";
                Query q = (Query) problems.get(i).getQuery();
                if(q.getNamedQuery() != null && q.getNamedQuery().getName()!=null){
                    pos = q.getNamedQuery().getName()+pos;
                }
                ret[i] = createProblem(subject, ctx, pos + ": " + message , Severity.WARNING);
            }
            return ret;
        }
        return null;
    }
    
    @Override protected boolean isApplicable(TypeElement subject, ProblemContext ctx) {
        JPAProblemContext jpaCtx = (JPAProblemContext)ctx;
        
        return (jpaCtx.isEntity() || jpaCtx.isMappedSuperClass());
    }
}
