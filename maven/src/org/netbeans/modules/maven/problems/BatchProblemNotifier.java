/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.problems;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.modules.maven.configurations.M2ConfigProvider;
import org.netbeans.modules.maven.configurations.M2Configuration;
import org.netbeans.modules.maven.execute.BeanRunConfig;
import org.netbeans.modules.maven.execute.ReactorChecker;
import org.netbeans.modules.maven.execute.ui.RunGoalsPanel;
import static org.netbeans.modules.maven.problems.Bundle.*;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle.Messages;

/**
 * Keeps track of problems in all open Maven projects and offers to do reactor sanity builds.
 */
public class BatchProblemNotifier {

    private static final Map<File,Set<String>> projectsByReactor = new HashMap<File,Set<String>>();

    @Messages({
        "# {0} - directory basename", "build_title=Build {0}",
        "# {0} - full directory path", "build_details=Run priming build in {0}"
    })
    public static void opened(NbMavenProjectImpl p) {
        ProblemReporterImpl pr = p.getProblemReporter();
        pr.doBaseProblemChecks(p.getOriginalMavenProject());
        if (!pr.getMissingArtifacts().isEmpty()) {
            File basedir = p.getPOMFile().getParentFile();
            // XXX do we need to do anything special for error:error:pom:0?
            final File reactor = ReactorChecker.findReactor(p.getProjectWatcher()).getMavenProject().getBasedir();
            boolean nue = false;
            synchronized (projectsByReactor) {
                Set<String> projects = projectsByReactor.get(reactor);
                if (projects == null) {
                    projects = new TreeSet<String>();
                    projectsByReactor.put(reactor, projects);
                    nue = true;
                }
                String path = FileUtilities.relativizeFile(reactor, basedir);
                if (path == null) {
                    path = basedir.getAbsolutePath();
                }
                projects.add(path);
            }
            if (nue) {
                NotificationDisplayer.getDefault().notify(
                    build_title(reactor.getName()),
                    ImageUtilities.image2Icon(ImageUtilities.mergeImages(
                        ImageUtilities.loadImage("org/netbeans/modules/maven/resources/Maven2Icon.gif", true),
                        ImageUtilities.loadImage("org/netbeans/modules/maven/brokenProjectBadge.png", true), 8, 0)),
                    build_details(reactor),
                    new ActionListener() {
                        @Override public void actionPerformed(ActionEvent e) {
                            showUI(reactor);
                        }
                    });
            }
        }
    }

    @Messages({"dialog_title=Run Priming Build", "build_label=Priming Build"})
    private static void showUI(File reactor) {
        Set<String> projects;
        synchronized (projectsByReactor) {
            projects = projectsByReactor.remove(reactor);
        }
        RunGoalsPanel pnl = new RunGoalsPanel();
        BeanRunConfig cfg = new BeanRunConfig();
        cfg.setExecutionName(dialog_title());
        cfg.setTaskDisplayName(dialog_title());
        cfg.setExecutionDirectory(reactor);
        try {
            FileObject reactorFO = FileUtil.toFileObject(reactor);
            if (reactorFO != null && reactorFO.isFolder()) {
                Project reactorP = ProjectManager.getDefault().findProject(reactorFO);
                if (reactorP != null) {
                    cfg.setProject(reactorP);
                    // Similar to ReactorChecker, except there can be multiple submodules to build.
                    M2Configuration m2c = reactorP.getLookup().lookup(M2ConfigProvider.class).getActiveConfiguration();
                    if (m2c != null) {
                        cfg.setActivatedProfiles(m2c.getActivatedProfiles());
                    }
                }
            }
        } catch (IOException x) {
            Logger.getLogger(BatchProblemNotifier.class.getName()).log(Level.FINE, null, x);
        }
        StringBuilder pl = new StringBuilder();
        for (String project : projects) {
            if (pl.length() > 0) {
                pl.append(',');
            }
            pl.append(project);
        }
        // validate, test-compile, dependency:go-offline also possible
        cfg.setGoals(Arrays.asList("--fail-at-end", "--also-make", "--projects", pl.toString(), "install"));
        pnl.readConfig(cfg);
        DialogDescriptor dd = new DialogDescriptor(pnl, dialog_title());
        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            pnl.applyValues(cfg);
            RunUtils.run(cfg);
        }
    }

    private BatchProblemNotifier() {}

}
