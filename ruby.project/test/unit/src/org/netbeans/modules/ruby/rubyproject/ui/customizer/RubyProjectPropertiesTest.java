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
package org.netbeans.modules.ruby.rubyproject.ui.customizer;

import org.netbeans.modules.ruby.rubyproject.RubyProject;
import org.netbeans.modules.ruby.rubyproject.RubyProjectTestBase;
import org.netbeans.modules.ruby.spi.project.support.rake.GeneratedFilesHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;

import static org.netbeans.modules.ruby.rubyproject.SharedRubyProjectProperties.PLATFORM_ACTIVE;

public class RubyProjectPropertiesTest extends RubyProjectTestBase {

    public RubyProjectPropertiesTest(String testName) {
        super(testName);
    }

    public void testPlatformVsConfig() throws Exception {
        RubyProject project = createTestProject();
        FileObject prjDirFO = project.getProjectDirectory();
        String prjDirS = FileUtil.toFile(prjDirFO).getAbsolutePath();
        
        FileObject privateFO = touch(prjDirS, "nbproject/private/private.properties");
        EditableProperties privateProps = loadProperties(privateFO);
        privateProps.setProperty(PLATFORM_ACTIVE, "Ruby");
        storeProperties(privateFO, privateProps);
        
        FileObject configFO = touch(prjDirS, "nbproject/private/config.properties");
        EditableProperties configProps = loadProperties(configFO);
        configProps.setProperty("config", "jruby");
        storeProperties(configFO, configProps);
        
        FileObject jrubyConfigFO = touch(prjDirS, "nbproject/private/configs/jruby.properties");
        EditableProperties jrubyConfigProps = loadProperties(jrubyConfigFO);
        jrubyConfigProps.setProperty(PLATFORM_ACTIVE, "JRuby");
        storeProperties(jrubyConfigFO, jrubyConfigProps);
        
        RubyProjectProperties props = new RubyProjectProperties(
                project, project.getUpdateHelper(), project.evaluator(),
                project.getReferenceHelper(), project.getLookup().lookup(GeneratedFilesHelper.class));

        String platform = props.getRunConfigs().get(null).get(PLATFORM_ACTIVE);
        assertEquals("right platform", "Ruby", platform);
    }

}
