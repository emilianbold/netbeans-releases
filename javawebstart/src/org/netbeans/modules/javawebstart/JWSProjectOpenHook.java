/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javawebstart;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.javawebstart.ui.customizer.JWSProjectProperties;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 *
 * @author Tomas Zezula
 */
@ProjectServiceProvider(service=ProjectOpenedHook.class, projectType="org-netbeans-modules-java-j2seproject")
public class JWSProjectOpenHook extends ProjectOpenedHook {

    private final Project prj;
    private final J2SEPropertyEvaluator eval;

    public JWSProjectOpenHook(final Lookup lkp) {
        Parameters.notNull("lkp", lkp); //NOI18N
        this.prj = lkp.lookup(Project.class);
        Parameters.notNull("prj", prj); //NOI18N
        this.eval = lkp.lookup(J2SEPropertyEvaluator.class);
        Parameters.notNull("eval", eval);   //NOI18N
    }

    @Override
    protected void projectOpened() {
        try {
            if (isTrue(eval.evaluator().getProperty(JWSProjectProperties.JNLP_ENABLED))) {  //JNLP_ENABLED - inlined by compiler
                JWSProjectProperties.updateWebStartJars(prj, eval.evaluator());
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    @Override
    protected void projectClosed() {
    }

    //Don't use JWSProjectProperties.isTrue - causes loading of big JWSProjectProperties
    private static boolean isTrue(final String value) {
        return value != null &&
        (value.equalsIgnoreCase("true") ||  //NOI18N
         value.equalsIgnoreCase("yes") ||   //NOI18N
         value.equalsIgnoreCase("on"));     //NOI18N
    }
}
