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

package org.netbeans.modules.j2ee.jpa.verification;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.j2ee.jpa.verification.common.Rule;
import org.netbeans.modules.j2ee.jpa.verification.common.RulesEngine;
import org.netbeans.modules.j2ee.jpa.verification.rules.IdDefinedInHierarchy;
import org.netbeans.modules.j2ee.jpa.verification.rules.PersistenceUnitPresent;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class JPARulesEngine extends RulesEngine {
    private static final List<Rule<TypeElement>> classRules = new LinkedList<Rule<TypeElement>>();
    
    static{
        classRules.add(new PersistenceUnitPresent());
        classRules.add(new IdDefinedInHierarchy());
    }
    
    protected Collection<Rule<TypeElement>> getClassRules() {
        return classRules;
    }

}
