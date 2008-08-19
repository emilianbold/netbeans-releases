/*
 * PersistenceUnitPresent.java
 *
 * Created on January 30, 2007, 3:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.PersistenceUnit;
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
