/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.osgi.customizer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.osgi.OSGIConstants;
import org.netbeans.modules.maven.spi.customizer.SelectedItemsTablePersister;

/**
 *
 * @author dafe
 */
public class FelixExportPersister implements SelectedItemsTablePersister {

    private final ModelHandle handle;
    private final Project project;

    public FelixExportPersister (Project project, ModelHandle handle) {
        this.project = project;
        this.handle = handle;
    }

    public Map<String, Boolean> read() {
        String[] exports = PluginPropertyUtils.getPluginPropertyList(project,
                OSGIConstants.GROUPID_FELIX, OSGIConstants.ARTIFACTID_BUNDLE_PLUGIN,
                OSGIConstants.PARAM_INSTRUCTIONS, OSGIConstants.EXPORT_PACKAGE,
                OSGIConstants.GOAL_MANIFEST);
        String exportInstruction = null;
        if (exports != null && exports.length == 1) {
            exportInstruction = exports[0];
        }
        String[] privates = PluginPropertyUtils.getPluginPropertyList(project,
                OSGIConstants.GROUPID_FELIX, OSGIConstants.ARTIFACTID_BUNDLE_PLUGIN,
                OSGIConstants.PARAM_INSTRUCTIONS, OSGIConstants.PRIVATE_PACKAGE,
                OSGIConstants.GOAL_MANIFEST);
        String privateInstruction = null;
        if (privates != null && privates.length == 1) {
            privateInstruction = privates[0];
        }

        Map<Integer, String> instructions = new HashMap<Integer, String>(2);
        instructions.put(InstructionsConverter.EXPORT_PACKAGE, exportInstruction);
        instructions.put(InstructionsConverter.PRIVATE_PACKAGE, privateInstruction);

        return InstructionsConverter.computeExportList(instructions, project);
    }

    public void write(Map<String, Boolean> selItems) {
        Map<Integer, String> exportIns = InstructionsConverter.computeExportInstructions(selItems);

        //Plugin felixPlugin = model.getFactory().createBuild().findPluginById(OSGIConstants.GROUPID_FELIX, OSGIConstants.ARTIFACTID_BUNDLE_PLUGIN);
        Plugin felixPlugin = handle.getPOMModel().getProject().getBuild().
                findPluginById(OSGIConstants.GROUPID_FELIX, OSGIConstants.ARTIFACTID_BUNDLE_PLUGIN);
        Configuration config = felixPlugin.getConfiguration();
        List<POMExtensibilityElement> confEls = config.getConfigurationElements();
        POMExtensibilityElement instructionsEl = null;
        for (POMExtensibilityElement el : confEls) {
            if (OSGIConstants.PARAM_INSTRUCTIONS.equals(el.getQName().getLocalPart())) {
                instructionsEl = el;
                break;
            }
        }

        POMExtensibilityElement exportEl = null;
        for (POMExtensibilityElement el : instructionsEl.getAnyElements()) {
            if (OSGIConstants.EXPORT_PACKAGE.equals(el.getQName().getLocalPart())) {
                exportEl = el;
                break;
            }
        }

        exportEl.setElementText(exportIns.get(InstructionsConverter.EXPORT_PACKAGE));

        handle.markAsModified(handle.getPOMModel());
    }

}
