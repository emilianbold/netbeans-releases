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
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.lang.model.element.TypeElement;
import org.eclipse.persistence.jpa.internal.jpql.JPQLQueryProblemResourceBundle;
import org.eclipse.persistence.jpa.jpql.JPQLQueryHelper;
import org.eclipse.persistence.jpa.jpql.JPQLQueryProblem;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.jpa.verification.CancelListener;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule.ClassConstraints;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.NamedQuery;
import org.netbeans.modules.j2ee.persistence.spi.jpql.ManagedTypeProvider;
import org.netbeans.modules.j2ee.persistence.spi.jpql.Query;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;

/**
 * Verify content of
 *
 * @NamedQuery query TODO: good to move warning to query level instead of class
 * level
 */
public class JPQLValidation extends JPAClassRule implements CancelListener {

    private ManagedTypeProvider mtp;//need to store as jpql validation may be too long and need to be cancelled if required
    private JPQLQueryHelper helper;

    /**
     * Creates a new instance of NonFinalClass
     */
    public JPQLValidation() {
        setClassContraints(Arrays.asList(ClassConstraints.ENTITY,
                ClassConstraints.EMBEDDABLE,
                ClassConstraints.MAPPED_SUPERCLASS));
    }

    @Override
    public ErrorDescription[] apply(TypeElement subject, ProblemContext ctx) {
        JPAProblemContext jpaCtx = (JPAProblemContext) ctx;
        jpaCtx.addCancelListener(this);
        Object modEl = ctx.getModelElement();
        Entity entity = (Entity) (modEl instanceof Entity ? modEl : null);
        helper = new JPQLQueryHelper();
        Project project = FileOwnerQuery.getOwner(ctx.getFileObject());
        List<JPQLQueryProblem> problems = new ArrayList<JPQLQueryProblem>();
        mtp = new ManagedTypeProvider(project, jpaCtx.getMetaData(), jpaCtx.getCompilationInfo().getElements());
        if (entity != null) {
            for (NamedQuery nq : entity.getNamedQuery()) {
                if(nq!=null && nq.getQuery()!=null){
                    helper.setQuery(new Query(nq, nq.getQuery(), mtp));
                    List<JPQLQueryProblem> tmp = null;
                    try {
                        tmp = helper.validate();
                    } catch (UnsupportedOperationException ex) {
                        JPAProblemFinder.LOG.log(Level.INFO, "Unsupported jpql validation case: " + ex.getMessage(), ex);
                    } catch (NullPointerException ex) {
                        JPAProblemFinder.LOG.log(Level.INFO, "NPE in jpql validation: " + ex.getMessage(), ex);
                    }
                    if (tmp != null && tmp.size() > 0) {
                        problems.addAll(tmp);
                    }
                    helper.dispose();
                }
            }
        }
        ErrorDescription[] ret = null;
        if (!ctx.isCancelled() && problems != null && problems.size() > 0) {
            ret = new ErrorDescription[problems.size()];
            for (int i = 0; i < ret.length; i++) {
                ListResourceBundle msgBundle;
                try {
                    msgBundle = (ListResourceBundle) ResourceBundle.getBundle(JPQLQueryProblemResourceBundle.class.getName());//NOI18N
                } catch (MissingResourceException ex) {//default en
                    msgBundle = (ListResourceBundle) ResourceBundle.getBundle(JPQLQueryProblemResourceBundle.class.getName(), Locale.ENGLISH);//NOI18N
                }
                String message = java.text.MessageFormat.format(msgBundle.getString(problems.get(i).getMessageKey()), (Object[]) problems.get(i).getMessageArguments());
                String pos = "[" + problems.get(i).getStartPosition() + ";" + problems.get(i).getEndPosition() + "]";
                Query q = (Query) problems.get(i).getQuery();
                if (q.getNamedQuery() != null && q.getNamedQuery().getName() != null) {
                    pos = q.getNamedQuery().getName() + pos;
                }
                ret[i] = createProblem(subject, ctx, pos + ": " + message, Severity.WARNING);
            }
        }
        jpaCtx.removeCancelListener(this);
        mtp = null;
        helper = null;
        return ret;
    }

    @Override
    protected boolean isApplicable(TypeElement subject, ProblemContext ctx) {
        JPAProblemContext jpaCtx = (JPAProblemContext) ctx;

        return (jpaCtx.isEntity() || jpaCtx.isMappedSuperClass());
    }

    @Override
    public void cancelled() {
        if (mtp != null) {
            mtp.invalidate();
        }
    }
}
