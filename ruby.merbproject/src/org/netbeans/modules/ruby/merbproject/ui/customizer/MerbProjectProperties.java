/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.ruby.merbproject.ui.customizer;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatform.Info;
import org.netbeans.modules.ruby.merbproject.MerbProject;
import org.netbeans.modules.ruby.merbproject.MerbSourceRoots;
import org.netbeans.modules.ruby.rubyproject.RubyProjectUtil;
import org.netbeans.modules.ruby.rubyproject.SharedRubyProjectProperties;
import org.netbeans.modules.ruby.rubyproject.UpdateHelper;
import org.netbeans.modules.ruby.rubyproject.Util;
import org.netbeans.modules.ruby.spi.project.support.rake.GeneratedFilesHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.modules.ruby.spi.project.support.rake.ReferenceHelper;
import org.openide.util.EditableProperties;

public class MerbProjectProperties extends SharedRubyProjectProperties {

    private static final Logger LOGGER = Logger.getLogger(MerbProjectProperties.class.getName());

    public static final String RUN_WORK_DIR = "work.dir"; // NOI18N

    public static final String RUN_SCRIPT = "run.script"; //NOI18N
    
    /** All per-configuration properties to be stored. */
    private static final String[] CONFIG_PROPS = {
        RUN_SCRIPT, APPLICATION_ARGS, RUBY_OPTIONS, RUN_WORK_DIR, RAKE_ARGS, JVM_ARGS, PLATFORM_ACTIVE
    };

    /** Private per-configuration properties. */
    private static final String[] CONFIG_PRIVATE_PROPS = { APPLICATION_ARGS, RUN_WORK_DIR, RAKE_ARGS, PLATFORM_ACTIVE };

    // CustomizerSources
    DefaultTableModel sourceRootsModel;
    DefaultTableModel testRootsModel;
     
    public MerbProjectProperties(
            final MerbProject project,
            final UpdateHelper updateHelper,
            final PropertyEvaluator evaluator,
            final ReferenceHelper refHelper,
            final GeneratedFilesHelper genFileHelper) {
        super(project, evaluator, updateHelper, genFileHelper, refHelper);
        
        sourceRootsModel = RubySourceRootsUi.createModel(getRubyProject().getSourceRoots());
        testRootsModel = RubySourceRootsUi.createModel(getRubyProject().getTestSourceRoots());
    }

    MerbProject getRubyProject() {
        return getProject().getLookup().lookup(MerbProject.class);
    }

    @Override
    protected String[] getConfigProperties() {
        return CONFIG_PROPS;
    }

    @Override
    protected String[] getConfigPrivateProperties() {
        return CONFIG_PRIVATE_PROPS;
    }

    @Override
    protected void prePropertiesStore() throws IOException {
//        storeRoots(getRubyProject().getSourceRoots(), sourceRootsModel);
//        storeRoots(getRubyProject().getTestSourceRoots(), testRootsModel);
    }

    @Override
    protected void storeProperties(EditableProperties projectProperties, EditableProperties privateProperties) throws IOException {
        RubyPlatform platform = getPlatform();
        if (platform == null) {
            LOGGER.fine("Project has invalid platform (null).");
            return;
        }
        Info info = platform.getInfo();
        Util.logUsage(MerbProjectProperties.class, "USG_PROJECT_CONFIG_MERB", // NOI18N
                info.getKind(),
                info.getPlatformVersion(),
                info.getGemVersion());
    }

    private void storeRoots(MerbSourceRoots roots, DefaultTableModel tableModel) throws MalformedURLException {
        Vector data = tableModel.getDataVector();
        URL[] rootURLs = new URL[data.size()];
        String[] rootLabels = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            File f = (File) ((Vector) data.elementAt(i)).elementAt(0);
            rootURLs[i] = RubyProjectUtil.getRootURL(f, null);
            rootLabels[i] = (String) ((Vector) data.elementAt(i)).elementAt(1);
        }
//        roots.putRoots(rootURLs, rootLabels);
    }
}
