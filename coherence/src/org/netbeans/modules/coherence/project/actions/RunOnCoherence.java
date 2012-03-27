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
package org.netbeans.modules.coherence.project.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.coherence.project.CoherenceProjectUtils;
import org.netbeans.modules.coherence.server.CoherenceInstance;
import org.netbeans.modules.coherence.server.CoherenceInstanceProvider;
import org.netbeans.modules.coherence.server.util.ClasspathPropertyUtils;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

@ActionID(id="org.netbeans.modules.coherence.project.actions.RunOnCoherence", category="Project")
@ActionRegistration(displayName="#LBL_RunOnCoherence", lazy=false)
@ActionReferences({
    @ActionReference(path="Projects/org-netbeans-modules-java-j2seproject/Actions", position=845),
    @ActionReference(path="Projects/org-netbeans-modules-j2ee-ejbjarproject/Actions", position=845),
    @ActionReference(path="Projects/org-netbeans-modules-j2ee-earproject/Actions", position=445)
})
public class RunOnCoherence extends AbstractAction implements ContextAwareAction {

    private static final Logger LOG = Logger.getLogger(RunOnCoherence.class.getName());

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        return new ContextAction(context);
    }

    private static final class ContextAction extends AbstractAction {

        private final Project project;

        public ContextAction(Lookup context) {
            project = context.lookup(Project.class);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            setEnabled(CoherenceProjectUtils.isCoherenceProject(project));
            putValue(Action.NAME, NbBundle.getMessage(RunOnCoherence.class, "LBL_RunOnCoherence")); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            AntArtifact[] artifacts = AntArtifactQuery.findArtifactsByType(project, JavaProjectConstants.ARTIFACT_TYPE_JAR);
            if (artifacts.length > 0) {
                URI[] artifactLocs = artifacts[0].getArtifactLocations();
                // if there is at least one location search for it on the Coherence servers CPs
                if (artifactLocs.length > 0) {
                    URI jarLocation = artifactLocs[0];
                    File baseJar = new File(FileUtil.toFile(project.getProjectDirectory()), jarLocation.toString());
                    final List<CoherenceInstance> relatedInstances = getRelatedInstances(baseJar.getAbsolutePath());
                    ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
                    if (ap != null && Arrays.asList(ap.getSupportedActions()).contains(ActionProvider.COMMAND_BUILD) && ap.isActionEnabled(ActionProvider.COMMAND_BUILD, Lookup.EMPTY)) {
                        final AtomicBoolean started = new AtomicBoolean();
                        ap.invokeAction(ActionProvider.COMMAND_BUILD, Lookups.singleton(new ActionProgress() {
                            @Override public void started() {
                                started.set(true);
                            }
                            @Override public void finished(boolean success) {
                                if (success) {
                                    for (CoherenceInstance coherenceInstance : relatedInstances) {
                                        coherenceInstance.getServer().restart();
                                    }
                                }
                            }
                        }));
                        if (!started.get()) {
                            LOG.log(Level.WARNING, "ActionProgress not supported by {0}", project);
                        }
                    }
                }
            }
        }

        private static List<CoherenceInstance> getRelatedInstances(String location) {
            List<CoherenceInstance> related = new ArrayList<CoherenceInstance>();
            List<CoherenceInstance> allinstances = CoherenceInstanceProvider.getCoherenceProvider().getCoherenceInstances();
            for (CoherenceInstance coherenceInstance : allinstances) {
                if (isOnServerClasspath(coherenceInstance, location)) {
                    related.add(coherenceInstance);
                }
            }
            return related;
        }

        private static boolean isOnServerClasspath(CoherenceInstance instance, String location) {
            String[] array = ClasspathPropertyUtils.classpathFromStringToArray(instance.getCoherenceProperties().getClasspath());
            for (String string : array) {
                if (string.equals(location)) {
                    return true;
                }
            }
            return false;
        }

    }
}
