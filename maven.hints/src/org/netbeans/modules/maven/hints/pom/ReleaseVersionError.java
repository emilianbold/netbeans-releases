/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.text.Position;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.maven.hints.pom.spi.Configuration;
import org.netbeans.modules.maven.hints.pom.spi.POMErrorFixProvider;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.BuildBase;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.PluginManagement;
import org.netbeans.modules.maven.model.pom.Profile;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.spi.editor.hints.ChangeInfo;
import org.netbeans.spi.editor.hints.ChangeInfo.Change;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.ErrorDescriptionFactory;
import org.netbeans.spi.editor.hints.Fix;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;
import org.openide.text.Line;
import org.openide.util.NbBundle;

/**
 *
 * @author mkleint
 */
public class ReleaseVersionError implements POMErrorFixProvider {
    private Configuration configuration;

    public ReleaseVersionError() {
        configuration = new Configuration("ReleaseVersionError", //NOI18N
                "No metaversions for plugins",
                "Shall not use release/latest/snapshot versions for plugins.",
                true, Configuration.HintSeverity.WARNING);
    }

    public List<ErrorDescription> getErrorsForDocument(POMModel model, Project prj) {
        assert model != null;
        List<ErrorDescription> toRet = new ArrayList<ErrorDescription>();
        if (!configuration.isEnabled(configuration.getPreferences())) {
            return toRet;
        }
        Build bld = model.getProject().getBuild();
        if (bld != null) {
            checkPluginList(bld.getPlugins(), model, toRet);
            PluginManagement pm = bld.getPluginManagement();
            if (pm != null) {
                checkPluginList(pm.getPlugins(), model, toRet);
            }
        }
        List<Profile> profiles = model.getProject().getProfiles();
        if (profiles != null) {
            for (Profile prof : profiles) {
                BuildBase base = prof.getBuildBase();
                if (base != null) {
                    checkPluginList(base.getPlugins(), model, toRet);
                    PluginManagement pm = base.getPluginManagement();
                    if (pm != null) {
                        checkPluginList(pm.getPlugins(), model, toRet);
                    }
                }
            }
        }
        return toRet;
    }

//    private static class ReleaseFix implements Fix {
//        private Plugin plugin;
//
//        ReleaseFix(Plugin plg) {

//            plugin = plg;
//        }
//
//        public String getText() {
//            return "Change to specific version";
//        }
//
//        public ChangeInfo implement() throws Exception {
//            ChangeInfo info = new ChangeInfo();
//            POMModel mdl = plugin.getModel();
//            if (!mdl.getState().equals(Model.State.VALID)) {
//                return info;
//            }
//            mdl.startTransaction();
//            try {
//                plugin.setVersion("XXX");
//            } finally {
//                mdl.endTransaction();
//            }
//            return info;
//        }
//
//    }

    private void checkPluginList(List<Plugin> plugins, POMModel model, List<ErrorDescription> toRet) {
        if (plugins != null) {
            for (Plugin plg : plugins) {
                String ver = plg.getVersion();
                if (ver != null && ("RELEASE".equals(ver) || "LATEST".equals(ver))) { //NOI18N
                    int position = plg.findChildElementPosition(model.getPOMQNames().VERSION.getQName());
                    Line line = NbEditorUtilities.getLine(model.getBaseDocument(), position, false);
                    toRet.add(ErrorDescriptionFactory.createErrorDescription(
                                   configuration.getSeverity(configuration.getPreferences()).toEditorSeverity(),
                            NbBundle.getMessage(ReleaseVersionError.class, "DESC_RELEASE_VERSION"),
                            Collections.<Fix>emptyList(), //Collections.<Fix>singletonList(new ReleaseFix(plg)),
                            model.getBaseDocument(), line.getLineNumber() + 1));
                }
            }
        }
    }

    public JComponent getCustomizer(Preferences preferences) {
        return null;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

}
