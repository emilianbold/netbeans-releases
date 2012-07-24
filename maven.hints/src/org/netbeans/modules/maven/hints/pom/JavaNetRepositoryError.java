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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.hints.pom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import static org.netbeans.modules.maven.hints.pom.Bundle.*;
import org.netbeans.modules.maven.hints.pom.spi.Configuration;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixProvider;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.modules.maven.model.pom.Repository;
import org.netbeans.modules.maven.model.pom.RepositoryContainer;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.text.Line;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class JavaNetRepositoryError implements POMErrorFixProvider {
    private Configuration configuration;
    private static final Set<String> forbidden; 
    static{
        forbidden = new HashSet<String>();
        forbidden.add("http://download.java.net/maven/2/");
        forbidden.add("http://download.java.net/maven/1/");
    }

    @NbBundle.Messages({
        "TIT_JavaNetRepositoryError=Uses java.net repository",
        "DESC_JavaNetRepositoryError=References the deprecated java.net repository which was merged into central."
    })
    public JavaNetRepositoryError() {
        configuration = new Configuration("JavaNetRepositoryError", //NOI18N
                TIT_JavaNetRepositoryError(),
                DESC_JavaNetRepositoryError(),
                true, Configuration.HintSeverity.WARNING);
    }


    @Override
    public List<ErrorDescription> getErrorsForDocument(POMModel model, Project prj) {
        assert model != null;
        List<ErrorDescription> toRet = new ArrayList<ErrorDescription>();
        if (prj == null) {
            return toRet;
        }
        
        checkRepositoryList(model.getProject().getRepositories(), model, model.getProject(), false, toRet);
        checkRepositoryList(model.getProject().getPluginRepositories(), model, model.getProject(), true, toRet);
        List<Profile> profiles = model.getProject().getProfiles();
        if (profiles != null) {
            for (Profile prof : profiles) {
                checkRepositoryList(prof.getRepositories(), model, prof, false, toRet);
                checkRepositoryList(prof.getPluginRepositories(), model, prof, true, toRet);
            }
        }
        return toRet;

    }
    @NbBundle.Messages("TXT_UsesJavanetRepository=References an obsolete repository at java.net")
    private void checkRepositoryList( List<Repository> repositories, POMModel model, RepositoryContainer container, boolean isPlugin, List<ErrorDescription> toRet) {
        if (repositories != null) {
            for (Repository rep : repositories) {
                String url = rep.getUrl();
                if (url != null) {
                    if (!url.endsWith("/")) {
                        url = url + "/"; //just to make queries consistent
                    }
                    if (forbidden.contains(url)) {
                        int position = rep.findChildElementPosition(model.getPOMQNames().URL.getQName());
                        Line line = NbEditorUtilities.getLine(model.getBaseDocument(), position, false);
                        toRet.add(ErrorDescriptionFactory.createErrorDescription(
                                       configuration.getSeverity(configuration.getPreferences()).toEditorSeverity(),
                                TXT_UsesJavanetRepository(),
                                Collections.<Fix>singletonList(new OverrideFix(rep, container, isPlugin)),
                                model.getBaseDocument(), line.getLineNumber() + 1));
                    }
                }
            }
        }
    }


    @Override
    public JComponent getCustomizer(Preferences preferences) {
        return null;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    private static class OverrideFix implements Fix, Runnable {
        private final Repository repository;
        private final RepositoryContainer container;
        private final boolean pluginRepo;

        OverrideFix(Repository plg, RepositoryContainer container, boolean isPlugin) {
            repository = plg;
            this.container = container;
            this.pluginRepo = isPlugin;
        }

        @Override
        @NbBundle.Messages("TEXT_UseJavanetRepository=Remove repository delegating to java.net")
        public String getText() {
            return TEXT_UseJavanetRepository();
        }

        @Override
        public void run() {
            if (pluginRepo) {
                container.removePluginRepository(repository);
            } else {
                container.removeRepository(repository);
            }
        }

        @Override
        public ChangeInfo implement() throws Exception {
            ChangeInfo info = new ChangeInfo();
            POMModel mdl = repository.getModel();
            if (!mdl.getState().equals(Model.State.VALID)) {
                return info;
            }
            PomModelUtils.implementInTransaction(mdl, this);
            return info;
        }
    }

}
