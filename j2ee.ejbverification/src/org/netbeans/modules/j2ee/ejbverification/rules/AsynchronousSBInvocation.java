/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.ejbverification.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.modules.j2ee.common.J2eeProjectCapabilities;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbverification.EJBAPIAnnotations;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.EJBVerificationRule;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.util.NbBundle.Messages;

/**
 * Checks that @Asynchronous are annotated methods of 3.1+ Full profile or methods declared in Local or No-Interface
 * View of the EJB3.2 Lite project (project targeting 3.2 Web profile).
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class AsynchronousSBInvocation extends EJBVerificationRule {

    @Messages({
        "RemoteAsyncSBInvocation.err.asynchronous.in.ejb31=Asynchronous Session Bean invocation is not allowed in project targeting JavaEE 6 Lite profile"
    })
    @Override
    public Collection<ErrorDescription> check(EJBProblemContext ctx) {
        List<ErrorDescription> problems = new ArrayList<ErrorDescription>();
        if (ctx.getEjb() instanceof Session) {
            Session session = (Session) ctx.getEjb();
            J2eeProjectCapabilities capabilities = J2eeProjectCapabilities.forProject(ctx.getProject());
            if (!capabilities.isEjb31LiteSupported() && !capabilities.isEjb32LiteSupported()) {
                return null;
            }

            List<ExecutableElement> candidates = new ArrayList<ExecutableElement>();
            for (ExecutableElement method : ElementFilter.methodsIn(ctx.getClazz().getEnclosedElements())) {
                if (isAsynchronousAnnotated(method)) {
                    candidates.add(method);
                }
            }
            if (candidates.isEmpty()) {
                return problems;
            }

            if (capabilities.isEjb31LiteSupported() && !capabilities.isEjb32LiteSupported()) {
                for (ExecutableElement method : candidates) {
                    problems.add(HintsUtils.createProblem(
                            method,
                            ctx.getComplilationInfo(),
                            Bundle.RemoteAsyncSBInvocation_err_asynchronous_in_ejb31()));
                }
            }
        }

        return problems;
    }

    private static boolean isAsynchronousAnnotated(ExecutableElement method) {
        boolean knownClasses = HintsUtils.isContainingKnownClasses(method);
        for (AnnotationMirror am : method.getAnnotationMirrors()) {
            if (EJBAPIAnnotations.ASYNCHRONOUS.equals(am.getAnnotationType().asElement().toString()) && knownClasses) {
                return true;
            }
        }
        return false;
    }

}
