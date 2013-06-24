/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.jpa.verification.rules.entity;

import org.netbeans.modules.j2ee.jpa.verification.rules.entity.IdDefinedInHierarchy;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule.ClassConstraints;
import org.netbeans.modules.j2ee.jpa.verification.JPAProblemFinder;
import org.netbeans.modules.j2ee.jpa.verification.api.JPAVerificationWarningIds;
import org.netbeans.modules.j2ee.jpa.verification.api.VerificationWarningOverrider;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.fixes.CreatePersistenceUnit;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceMetadata;
import org.netbeans.modules.j2ee.persistence.dd.PersistenceUtils;
import org.netbeans.modules.j2ee.persistence.dd.common.PersistenceUnit;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class PersistenceUnitPresent extends JPAClassRule {

    /** Creates a new instance of PersistenceUnitPresent */
    public PersistenceUnitPresent() {
        setClassContraints(Arrays.asList(ClassConstraints.ENTITY));
    }

    @Override
    public ErrorDescription[] apply(TypeElement subject, ProblemContext ctx) {
        Project project = FileOwnerQuery.getOwner(ctx.getFileObject());

        if (project == null) {
            // Can't perform this check for a file that does not belong to a project
            return null;
        }

        PersistenceScope[] scopes = PersistenceUtils.getPersistenceScopes(project);

        for (PersistenceScope scope : scopes) {
            if (scope.getClassPath().contains(ctx.getFileObject())) {

                try {
                    FileObject persistenceXML = scope.getPersistenceXml();

                    if (persistenceXML != null) {
                        PersistenceUnit pus[] = PersistenceMetadata.getDefault().getRoot(persistenceXML).getPersistenceUnit();

                        if (pus != null && pus.length > 0) {
                            // persistence unit found, no warning
                            return null;
                        }
                    }
                } catch (IOException e) {
                    JPAProblemFinder.LOG.log(Level.SEVERE, e.getMessage(), e);
                } catch (RuntimeException e) {
                    JPAProblemFinder.LOG.log(Level.SEVERE, e.getMessage(), e);
                }
            }


        }

        // See if any module has turned off this particular warning, such as, the Hibernate Support module
        for (VerificationWarningOverrider wo : project.getLookup().lookupAll(VerificationWarningOverrider.class)) {
            if (wo.suppressWarning(JPAVerificationWarningIds.NO_PERSISTENCE_UNIT_WARNING)) {
                return null;
            }
        }

        return new ErrorDescription[]{createProblem(subject, ctx,
                    NbBundle.getMessage(IdDefinedInHierarchy.class, "MSG_MissingPersistenceUnitHint"),
                    Severity.WARNING, new CreatePersistenceUnit(project))
                };
    }
}
