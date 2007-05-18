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

import javax.lang.model.element.Element;
import org.netbeans.modules.db.api.sql.SQLKeywords;
import org.netbeans.modules.j2ee.jpa.model.AttributeWrapper;
import org.netbeans.modules.j2ee.jpa.verification.JPAEntityAttributeCheck;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.modules.j2ee.jpa.verification.common.Rule;
import org.netbeans.modules.j2ee.persistence.dd.JavaPersistenceQLKeywords;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class ValidColumnName extends JPAEntityAttributeCheck {
    
    public ErrorDescription[] check(ProblemContext ctx, AttributeWrapper attrib) {
        String columnName = attrib.getColumn().getName();
        Element javaElement = attrib.getJavaElement();
        
        if (columnName.length() == 0){
            return new ErrorDescription[]{Rule.createProblem(javaElement, ctx,
                    NbBundle.getMessage(ValidColumnName.class,
                    "MSG_AttrInvalidPersistenceQLIdentifier",
                    columnName))};
        }
        
        if (JavaPersistenceQLKeywords.isKeyword(columnName)){
            return new ErrorDescription[]{Rule.createProblem(javaElement, ctx,
                    NbBundle.getMessage(ValidColumnName.class,
                    "MSG_AttrNamedWithJavaPersistenceQLKeyword", columnName))};
        }
        
        if (SQLKeywords.isSQL99ReservedKeyword(columnName)){
            return new ErrorDescription[]{Rule.createProblem(javaElement, ctx,
                    NbBundle.getMessage(ValidColumnName.class,
                    "MSG_AttrNamedWithReservedSQLKeyword", columnName),
                    Severity.WARNING)};
        }
        
        return null;
    }
}