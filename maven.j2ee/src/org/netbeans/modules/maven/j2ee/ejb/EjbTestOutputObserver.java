/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.j2ee.ejb;

import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.output.TestOutputObserver;
import org.netbeans.modules.maven.api.problem.ProblemReport;
import org.netbeans.modules.maven.api.problem.ProblemReporter;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author petrjiricka
 */
@ServiceProvider(service=TestOutputObserver.class)
public class EjbTestOutputObserver implements TestOutputObserver {

    private Pattern ejbContainerPattern = Pattern.compile("java.lang.ClassFormatError: Absent Code attribute in method that is not native or abstract in class file javax/ejb/embeddable/EJBContainer[\\s]*"); // NOI18N

    @Override
    public void processLine(String line, Project p) {
        Matcher ejb = ejbContainerPattern.matcher(line);
        if (ejb.matches()) {
            if (p != null) {
                ProblemReporter report = p.getLookup().lookup(ProblemReporter.class);
                if (report != null) {
                    ProblemReport rep = new ProblemReport(ProblemReport.SEVERITY_HIGH,
                            NbBundle.getMessage(EjbTestOutputObserver.class, "MSG_MissingEmbeddableContainer"),
                            NbBundle.getMessage(EjbTestOutputObserver.class, "MSG_MissingEmbeddableContainerDesc"),
                            new ResolveEjbContainerAction(p));
                    report.addReport(rep);
                }
            }
        }
    }

    private static class ResolveEjbContainerAction extends AbstractAction {
        private Project prj;
        private ResolveEjbContainerAction(Project project) {
            prj = project;
            putValue(Action.NAME,
                  NbBundle.getMessage(EjbTestOutputObserver.class, "LBL_AddEmbeddableContainer"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                boolean added = addPomToTestScope(new URL("http://download.java.net/maven/glassfish/org/glassfish/extras/glassfish-embedded-all/3.0/glassfish-embedded-all-3.0.pom"));
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        private boolean addPomToTestScope(final URL pomUrl) {
            final Boolean[] added = new Boolean[1];
            ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                @Override
                public void performOperation(POMModel model) {
                    added[0] = checkAndAddPom(pomUrl, model, "test", null, null);
                }
            };
            FileObject pom = prj.getProjectDirectory().getFileObject("pom.xml");//NOI18N
            org.netbeans.modules.maven.model.Utilities.performPOMModelOperations(pom, Collections.singletonList(operation));
            //TODO is the manual reload necessary if pom.xml file is being saved?
    //                NbMavenProject.fireMavenProjectReload(project);
            if (added[0]) {
                prj.getLookup().lookup(NbMavenProject.class).triggerDependencyDownload();
            }
            return added[0];
        }

        private boolean checkAndAddPom(URL pom, POMModel model, String scope, String repoId, String repoName) {
            Set<String> repos = RepositoryPreferences.getInstance().getKnownRepositoryUrls();
            ModelUtils.LibraryDescriptor result = ModelUtils.checkLibrary(pom, repos);
            if (result != null) {
                //set dependency
                Dependency dep = ModelUtils.checkModelDependency(model, result.getGroupId(), result.getArtifactId(), true);
                dep.setVersion(result.getVersion());
                if (scope != null) {
                    dep.setScope(scope);
                }
                if (result.getClassifier() != null) {
                    dep.setClassifier(result.getClassifier());
                }
                //set repository
                NbMavenProject mavenPrj = prj.getLookup().lookup(NbMavenProject.class);
                if (mavenPrj != null) {
                    org.netbeans.modules.maven.model.pom.Repository reposit = ModelUtils.addModelRepository(
                            mavenPrj.getMavenProject(), model, result.getRepoRoot());
                    if (reposit != null) {
                        if (repoId == null) {
                            repoId = result.getRepoRoot();
                        }
                        reposit.setId(repoId);
                        reposit.setLayout(result.getRepoType());
                        reposit.setName(repoName); //NOI18N - content coming into the pom.xml file
                    }
                }
                return true;
            } else {
                return false;
            }
         }
     }

}
