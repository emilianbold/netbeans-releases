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
package org.netbeans.modules.j2ee.ejbverification.rules;

import java.util.Collection;
import java.util.Collections;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.EJBVerificationRule;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.modules.j2ee.ejbverification.JavaUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 * Only stateful session bean(SFSB) can implement SessionSynchronization i/f.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author Tomasz.Slota@Sun.COM
 */
public class SessionSynchImplementedBySFSBOnly extends EJBVerificationRule {
    private static final String SESSION_SYNC_IFACE = "javax.ejb.SessionSynchronization"; //NOI18N
    
    public Collection<ErrorDescription> check(EJBProblemContext ctx) {
        if (ctx.getEjb() instanceof Session){
            if (Session.SESSION_TYPE_STATEFUL.equals(
                    ((Session)ctx.getEjb()).getSessionType())){
                return null; // OK, stateful session bean
            }
        }
        
        for (TypeMirror iface : ctx.getClazz().getInterfaces()){
            String ifaceName = JavaUtils.extractClassNameFromType(iface);
            
            if (SESSION_SYNC_IFACE.equals(ifaceName)){
                
                ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), ctx.getComplilationInfo(),
                        NbBundle.getMessage(SessionSynchImplementedBySFSBOnly.class,
                        "MSG_SessionSynchImplementedBySFSBOnly"));
                
                return Collections.singletonList(err);
            }
        }
        
        return null;
    }
}
