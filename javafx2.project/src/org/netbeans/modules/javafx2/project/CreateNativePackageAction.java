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
package org.netbeans.modules.javafx2.project;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Task;
import org.openide.util.TaskListener;

@ActionID(category = "Project", id = "org.netbeans.modules.javafx2.project.CreateNativePackageAction")
@ActionRegistration(lazy = false, displayName = "#CTL_CreateNativePackageAction")
@ActionReferences({
    @ActionReference(path = "Projects/org-netbeans-modules-java-j2seproject/Actions", position = 350),
})
@Messages("CTL_CreateNativePackageAction=Build Nati&ve Package")
public final class CreateNativePackageAction extends AbstractAction implements ContextAwareAction {
    public @Override void actionPerformed(ActionEvent e) {assert false;}
    public @Override Action createContextAwareInstance(Lookup context) {
        return new ContextAction(context);
    }
    private static final class ContextAction extends AbstractAction {
        
        private final Project prj;
        private final J2SEPropertyEvaluator ep;
        private boolean isJSAvailable = true;
        private boolean isJSAvailableChecked = false;
        
        public ContextAction(Lookup context) {
            prj = context.lookup(Project.class);
            ep = prj.getLookup().lookup(J2SEPropertyEvaluator.class);
            if(!isFXProject(ep)) {
                setEnabled(false);
            } else {
                String nativeEnabled = ep.evaluator().getProperty("javafx.native.bundling.enabled"); // NOI18N
                String nativeType = ep.evaluator().getProperty("javafx.native.bundling.type"); // NOI18N
                setEnabled( nativeEnabled != null && nativeType != null &&
                        isTrue(nativeEnabled) && !nativeType.equalsIgnoreCase("none")); // NOI18N
            }
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            putValue(NAME, Bundle.CTL_CreateNativePackageAction()); // NOI18N
        }
        
        public @Override void actionPerformed(ActionEvent e) {
            FileObject buildFo = findBuildXml();
            assert buildFo != null && buildFo.isValid();
            String noScript = isJavaScriptAvailable() ? "" : "-noscript"; // NOI18N
            final ActionProgress listener = ActionProgress.start(prj.getLookup());
            try {
                String target = "jfx-build-native".concat(noScript); // NOI18N
                Properties props = new Properties();
                assert ep != null;
                String nativeType = ep.evaluator().getProperty("javafx.native.bundling.type"); // NOI18N
                if(nativeType == null || nativeType.equalsIgnoreCase("none")) { // NOI18N
                    nativeType = "all"; // NOI18N
                }
                props.setProperty("javafx.native.bundling.type", nativeType); // NOI18N
                ActionUtils.runTarget(buildFo, new String[] {target}, props).addTaskListener(new TaskListener() {
                    @Override public void taskFinished(Task task) {
                        listener.finished(((ExecutorTask) task).result() == 0);
                    }
                });
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                listener.finished(false);
            }
        }

        @NonNull
        private static String getBuildXmlName (@NonNull final PropertyEvaluator evaluator) {
            String buildScriptPath = evaluator.getProperty("buildfile");    //NOI18N
            if (buildScriptPath == null) {
                buildScriptPath = GeneratedFilesHelper.BUILD_XML_PATH;
            }
            return buildScriptPath;
        }

        @CheckForNull
        private FileObject findBuildXml () {
            assert ep != null;
            return prj.getProjectDirectory().getFileObject (getBuildXmlName(ep.evaluator()));
        }

        private boolean isJavaScriptAvailable() {
            if(isJSAvailableChecked) {
                return isJSAvailable;
            }
            ScriptEngineManager mgr = new ScriptEngineManager();
            List<ScriptEngineFactory> factories = mgr.getEngineFactories();
            for (ScriptEngineFactory factory: factories) {
                List<String> engNames = factory.getNames();
                for(String name: engNames) {
                    if(name.equalsIgnoreCase("js") || name.equalsIgnoreCase("javascript")) { //NOI18N
                        isJSAvailableChecked = true;
                        isJSAvailable = true;
                        return isJSAvailable;
                    }
                }
            }
            isJSAvailableChecked = true;
            isJSAvailable = false;
            return isJSAvailable;
        }

        private static boolean isFXProject(@NonNull final J2SEPropertyEvaluator ep) {
            if (ep == null) {
                return false;
            }
            return isTrue(ep.evaluator().getProperty("javafx.enabled")); // NOI18N
        }

        private static boolean isTrue(final String value) {
            return value != null &&
                    (value.equalsIgnoreCase("true") ||  //NOI18N
                     value.equalsIgnoreCase("yes") ||   //NOI18N
                     value.equalsIgnoreCase("on"));     //NOI18N
        }

    }
}
