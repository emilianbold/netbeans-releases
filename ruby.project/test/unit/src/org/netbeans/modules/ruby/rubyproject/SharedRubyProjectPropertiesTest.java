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
package org.netbeans.modules.ruby.rubyproject;

import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ruby.spi.project.support.rake.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.openide.util.Mutex;
import static org.netbeans.modules.ruby.rubyproject.SharedRubyProjectProperties.RUBY_OPTIONS;
import static org.netbeans.modules.ruby.rubyproject.SharedRubyProjectProperties.RUBY_OPTIONS_DEPRECATED;

public class SharedRubyProjectPropertiesTest extends RubyProjectTestBase {

    public SharedRubyProjectPropertiesTest(String testName) {
        super(testName);
    }

    public void testSupportForRunJvmArgs() throws Exception {
        final RubyProject project = createTestProject();
        FileObject prjDirFO = project.getProjectDirectory();
        String prjDirS = FileUtil.toFile(prjDirFO).getAbsolutePath();

        FileObject privateFO = touch(prjDirS, "nbproject/private/private.properties");
        EditableProperties privateProps = loadProperties(privateFO);
        privateProps.setProperty(RUBY_OPTIONS_DEPRECATED, "-v");
        storeProperties(privateFO, privateProps);

        assertRubyOptions(project, "run.jvmargs supported", "-v");
    }

    public void testGetRubyOptions() throws Exception {
        final RubyProject project = createTestProject();
        FileObject prjDirFO = project.getProjectDirectory();
        String prjDirS = FileUtil.toFile(prjDirFO).getAbsolutePath();

        FileObject privateFO = touch(prjDirS, "nbproject/private/private.properties");
        EditableProperties privateProps = loadProperties(privateFO);
        privateProps.setProperty(RUBY_OPTIONS, "-v");
        storeProperties(privateFO, privateProps);

        assertRubyOptions(project, "right options", "-v");
    }

    public void testGetRubyOptionsMixedWithDeprecatedRunJvmArgsPreference() throws Exception {
        final RubyProject project = createTestProject();
        FileObject prjDirFO = project.getProjectDirectory();
        String prjDirS = FileUtil.toFile(prjDirFO).getAbsolutePath();

        FileObject privateFO = touch(prjDirS, "nbproject/private/private.properties");
        EditableProperties privateProps = loadProperties(privateFO);
        privateProps.setProperty(RUBY_OPTIONS_DEPRECATED, "-d");
        privateProps.setProperty(RUBY_OPTIONS, "-v");
        storeProperties(privateFO, privateProps);

        assertRubyOptions(project, "right options", "-v");
    }

    private static void assertRubyOptions(final RubyBaseProject project, final String message, final String value) {
        String rubyOptions = ProjectManager.mutex().readAccess(new Mutex.Action<String>() {
            public String run() {
                return SharedRubyProjectProperties.getRubyOptions(project);
            }
        });
        assertEquals(message, "-v", rubyOptions);

    }
}
