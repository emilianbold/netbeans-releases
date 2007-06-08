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
package org.netbeans.modules.j2ee.ejbverification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.netbeans.modules.j2ee.ejbverification.rules.BMnotPartOfRBIandLBI;
import org.netbeans.modules.j2ee.ejbverification.rules.BeanHasDifferentLBIandRBI;
import org.netbeans.modules.j2ee.ejbverification.rules.BeanImplementsBI;
import org.netbeans.modules.j2ee.ejbverification.rules.HasNoArgContructor;
import org.netbeans.modules.j2ee.ejbverification.rules.LegalModifiers;
import org.netbeans.modules.j2ee.ejbverification.rules.SBSuperClassNotSB;
import org.netbeans.modules.j2ee.ejbverification.rules.WSisSLSB;
import org.netbeans.spi.editor.hints.ErrorDescription;

/**
 * 
 * @author Tomasz.Slota@Sun.COM
 */
public class EJBRulesRegistry {
    private static Collection<? extends EJBVerificationRule> rules = Arrays.asList(
            new HasNoArgContructor(),
            new SBSuperClassNotSB(),
            new BeanImplementsBI(),
            new BMnotPartOfRBIandLBI(),
            new WSisSLSB(),
            new BeanHasDifferentLBIandRBI(),
            new LegalModifiers());
    
    public static Collection<ErrorDescription> check(EJBProblemContext ctx){
        Collection<ErrorDescription> problemsFound = new ArrayList<ErrorDescription>();
        
        for (EJBVerificationRule rule : rules){
            Collection<ErrorDescription> newProblems = rule.check(ctx);
            
            if (newProblems != null){
                problemsFound.addAll(newProblems);
            }
        }
        
        return problemsFound;
    }
}
