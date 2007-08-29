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
import java.util.TreeSet;
import java.util.logging.Level;
import org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemFinder;
import org.netbeans.modules.j2ee.ejbverification.EJBVerificationRule;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle;

/**
 * The same business interface cannot be both
 * a local and a remote business interface of the bean
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class BeanHasDifferentLBIandRBI extends EJBVerificationRule {

    public Collection<ErrorDescription> check(EJBProblemContext ctx) {
        if (ctx.getEjb() instanceof Session) {
            Session session = (Session) ctx.getEjb();
            Collection<String> localInterfaces = new TreeSet<String>();

            try {
                if (session.getBusinessLocal() != null) {
                    for (String localInterface : session.getBusinessLocal()) {
                        localInterfaces.add(localInterface);
                    }
                }

                if (session.getBusinessRemote() != null) {
                    for (String remoteInterface : session.getBusinessRemote()) {
                        if (localInterfaces.contains(remoteInterface)) {

                            ErrorDescription err = HintsUtils.createProblem(ctx.getClazz(), ctx.getComplilationInfo(), NbBundle.getMessage(BMnotPartOfRBIandLBI.class, "MSG_BeanHasDifferentLBIandRBI"));

                            return Collections.singletonList(err);
                        }
                    }
                }
            } catch (VersionNotSupportedException ex) {
                EJBProblemFinder.LOG.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        return null;
    }
}
