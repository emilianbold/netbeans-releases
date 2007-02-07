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

package org.netbeans.modules.j2ee.jpa.verification.rules.entity;

import java.util.Collections;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.db.api.sql.SQLKeywords;
import org.netbeans.modules.j2ee.jpa.model.JPAHelper;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.modules.j2ee.persistence.dd.JavaPersistenceQLKeywords;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;

/**
 * Table mapped to the entity class name must not be a reserved JPA-QL keyword
 * 
 * @author Tomasz.Slota@Sun.COM
 */
public class ValidPrimaryTableName extends JPAClassRule {
    private String errorMsg = null;
    private Severity severity = null;
    
    /** Creates a new instance of LegalName */
    public ValidPrimaryTableName() {
        setClassContraints(Collections.singleton(ClassConstraints.ENTITY));
    }
    
    @Override public ErrorDescription[] apply(TypeElement subject, ProblemContext ctx){
        String entityName = JPAHelper.getPrimaryTableName(subject);
        
        if (entityName.length() == 0){ //TODO: 
            severity = Severity.ERROR;
            errorMsg = NbBundle.getMessage(IdDefinedInHierarchy.class, "MSG_InvalidPersistenceQLIdentifier");
            return new ErrorDescription[]{createProblem(subject, ctx)};
        }
        
        if (JavaPersistenceQLKeywords.isKeyword(entityName)){
            severity = Severity.ERROR;
            errorMsg = NbBundle.getMessage(IdDefinedInHierarchy.class, "MSG_ClassNamedWithJavaPersistenceQLKeyword");
            return new ErrorDescription[]{createProblem(subject, ctx)};
        }
        
        if (SQLKeywords.isSQL99ReservedKeyword(entityName)){
            severity = Severity.WARNING;
            errorMsg = NbBundle.getMessage(IdDefinedInHierarchy.class, "MSG_ClassNamedWithReservedSQLKeyword");
            return new ErrorDescription[]{createProblem(subject, ctx)};
        }
        
        return null;
    }
    
    @Override public String getDescription(){
        return errorMsg;
    }
    
    @Override public Severity getSeverity(){
        return severity;
    }
}
