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
package org.netbeans.modules.web.jsf.hints.rules;

import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.web.beans.CdiUtil;
import org.netbeans.modules.web.jsf.hints.JsfHintsContext;
import org.netbeans.modules.web.jsf.hints.JsfHintsRule;
import org.netbeans.modules.web.jsf.hints.JsfHintsUtils;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle.Messages;

/**
 * Checks FlowScoped bean in non-CDI project.
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class FlowScopedBeanWithoutCdi implements JsfHintsRule {

    private static final String FLOW_SCOPED = "javax.faces.flow.FlowScoped"; //NOI18N

    @Messages({
        "FlowScopedBeanWithoutCdi.lbl.flow.scoped.without.cdi=@FlowScoped bean in the non-CDI capable project"
    })
    @Override
    public Collection<ErrorDescription> check(JsfHintsContext ctx) {
        CompilationInfo info = ctx.getCompilationInfo();
        List<ErrorDescription> hints = new ArrayList<>();

        Project project = ctx.getProject();
        if (project == null) {
            return hints;
        }

        for (TypeElement typeElement : info.getTopLevelElements()) {
            for (AnnotationMirror annotationMirror : typeElement.getAnnotationMirrors()) {
                if (FLOW_SCOPED.equals(annotationMirror.getAnnotationType().toString())) {
                    // it's FlowScoped bean -> check the CDI
                    CdiUtil cdiUtil = project.getLookup().lookup(CdiUtil.class);
                    if (cdiUtil == null || !cdiUtil.isCdiEnabled()) {
                        Tree tree = info.getTrees().getTree(typeElement, annotationMirror);
                        hints.add(JsfHintsUtils.createProblem(
                                tree,
                                info,
                                Bundle.FlowScopedBeanWithoutCdi_lbl_flow_scoped_without_cdi(),
                                Severity.WARNING,
                                Arrays.<Fix>asList(new FixCdiAvailability(project))));
                    }
                }
            }
        }
        return hints;
    }

    /**
     * Fix for enabling CDI in the project.
     */
    private static class FixCdiAvailability implements Fix {

        private final Project project;

        public FixCdiAvailability(Project project) {
            this.project = project;
        }

        @Messages({
            "# {0} - project display name",
            "FixCdiAvailability.lbl.enable.cdi=Enable CDI in project {0}"
        })
        @Override
        public String getText() {
            ProjectInformation information = ProjectUtils.getInformation(project);
            return Bundle.FixCdiAvailability_lbl_enable_cdi(information.getDisplayName());
        }

        @Override
        public ChangeInfo implement() throws Exception {
            CdiUtil cdiUtil = project.getLookup().lookup(CdiUtil.class);
            if (cdiUtil != null) {
                cdiUtil.enableCdi();
            }
            return null;
        }
    }
}
