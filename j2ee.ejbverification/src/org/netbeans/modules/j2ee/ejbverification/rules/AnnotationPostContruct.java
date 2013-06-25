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
package org.netbeans.modules.j2ee.ejbverification.rules;

import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.Util;
import org.netbeans.modules.j2ee.dd.api.ejb.Session;
import org.netbeans.modules.j2ee.ejbverification.EJBAPIAnnotations;
import org.netbeans.modules.j2ee.ejbverification.EJBProblemContext;
import org.netbeans.modules.j2ee.ejbverification.HintsUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class AnnotationPostContruct {

    @Messages({
        "AnnotationPostContruct.display.name=Annotation @PostConstruct",
        "AnnotationPostContruct.description=Checks usage of @PostConstruct annotation. Its return value, singularity per class, parameters etc.",
        "AnnotationPostContruct_too_much_annotations=There cannot be more than one method annotated @PostConstruct",
        "AnnotationPostContruct_wrong_return_type=Return type of @PostConstruct annotated method must be void.",
        "AnnotationPostContruct_thrown_checked_exceptions=@PostConstruct annotated method must not throw a checked exception.",
        "AnnotationPostContruct_wrong_parameters=@PostConstruct annotated method must not have any parameters except in the case of EJB interceptors in which case it takes an InvocationContext."
    })
    @Hint(displayName = "#AnnotationPostContruct.display.name",
            description = "#AnnotationPostContruct.description",
            category = "JavaEE",
            enabled = true,
            suppressWarnings = "AnnotationPostContruct")
    @TriggerTreeKind(Tree.Kind.CLASS)
    public static List<ErrorDescription> run(HintContext hintCtx) {
        EJBProblemContext ctx = HintsUtils.getOrCacheContext(hintCtx);
        if (ctx.getEjb() instanceof Session) {
            EjbJar ejbModule = ctx.getEjbModule();
            Profile profile = ejbModule.getJ2eeProfile();

            // not EE6+ project
            if (profile == null || !Util.isAtLeastJavaEE6Web(profile)) {
                return null;
            }

            List<ExecutableElement> allMethods = ElementFilter.methodsIn(ctx.getClazz().getEnclosedElements());
            List<ExecutableElement> eligibleMethods = new ArrayList<>();
            List<ErrorDescription> problems = new ArrayList<>();

            for (ExecutableElement method : allMethods) {
                if (isEligibleMethod(method)) {
                    eligibleMethods.add(method);
                }
            }

            // more than one annotated method
            if (eligibleMethods.size() > 1) {
                for (ExecutableElement problematicMethods : eligibleMethods) {
                    problems.add(HintsUtils.createProblem(
                            problematicMethods,
                            ctx.getComplilationInfo(),
                            Bundle.AnnotationPostContruct_too_much_annotations()));
                }
            }

            for (ExecutableElement method : eligibleMethods) {
                // wrong return type
                if (!"void".equals(method.getReturnType().toString())) { //NOI18N
                    problems.add(HintsUtils.createProblem(
                            method,
                            ctx.getComplilationInfo(),
                            Bundle.AnnotationPostContruct_wrong_return_type()));
                }
                // cannot throw unchecked exceptions
                if (!method.getThrownTypes().isEmpty()) {
                    problems.add(HintsUtils.createProblem(
                            method,
                            ctx.getComplilationInfo(),
                            Bundle.AnnotationPostContruct_thrown_checked_exceptions()));
                }
                // no parameter except in the case of EJB interceptor
                List<? extends VariableElement> parameters = method.getParameters();
                if (!parameters.isEmpty()
                        && (parameters.size() > 1 || !isEjbInterceptor(method))) {
                    problems.add(HintsUtils.createProblem(
                            method,
                            ctx.getComplilationInfo(),
                            Bundle.AnnotationPostContruct_wrong_parameters()));
                }
            }

            return problems;
        }
        return null;
    }

    private static boolean isEjbInterceptor(ExecutableElement method) {
        VariableElement parameter = method.getParameters().get(0);
        if ("javax.interceptor.InvocationContext".equals(parameter.asType().toString())) { //NOI18N
            for (AnnotationMirror am : method.getAnnotationMirrors()) {
                if (EJBAPIAnnotations.AROUND_INVOKE.equals(am.getAnnotationType().asElement().toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isEligibleMethod(ExecutableElement method) {
        boolean knownClasses = HintsUtils.isContainingKnownClasses(method);
        for (AnnotationMirror am : method.getAnnotationMirrors()) {
            if (EJBAPIAnnotations.POST_CONSTRUCT.equals(am.getAnnotationType().asElement().toString())
                    && knownClasses) {
                return true;
            }
        }
        return false;
    }

}
