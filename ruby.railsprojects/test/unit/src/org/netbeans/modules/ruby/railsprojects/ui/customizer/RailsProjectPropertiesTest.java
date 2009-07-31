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
package org.netbeans.modules.ruby.railsprojects.ui.customizer;

import java.io.File;
import java.util.concurrent.Future;
import javax.swing.event.ChangeListener;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.netbeans.modules.ruby.railsprojects.RailsProjectTestBase;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstance;
import org.netbeans.modules.ruby.rubyproject.RubyProjectTestBase;
import org.netbeans.modules.ruby.spi.project.support.rake.GeneratedFilesHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.EditableProperties;

import static org.netbeans.modules.ruby.rubyproject.SharedRubyProjectProperties.PLATFORM_ACTIVE;
import static org.netbeans.modules.ruby.railsprojects.ui.customizer.RailsProjectProperties.RAILS_ENV;
import static org.netbeans.modules.ruby.railsprojects.ui.customizer.RailsProjectProperties.RAILS_SERVERTYPE;

public class RailsProjectPropertiesTest extends RailsProjectTestBase {

    public RailsProjectPropertiesTest(String testName) {
        super(testName);
    }

    public void testPropertiesBasics() throws Exception {
        RailsProject project = createTestPlainProject();
        RailsProjectProperties props = getProperties(project);
        
        assertNull("initially null environment", props.getRailsEnvironment());
        assertNull("initially null environment property", project.evaluator().getProperty(RAILS_ENV));
        assertEquals("initially WEBRICK server", "WEBRICK", project.evaluator().getProperty(RAILS_SERVERTYPE));

        props.setServer(new WebMockerServer(), null);
        props.setRailsEnvironment("production", null);
        props.save();
        
        assertEquals("WEBMOCKER server", "WEBMOCKER", project.evaluator().getProperty(RAILS_SERVERTYPE));
        assertEquals("production environment", "production", project.evaluator().getProperty(RAILS_ENV));
    }

    public void testPlatformVsConfig() throws Exception {
        RailsProject project = createTestProject();
        FileObject prjDirFO = project.getProjectDirectory();
        String prjDirS = FileUtil.toFile(prjDirFO).getAbsolutePath();

        FileObject privateFO = touch(prjDirS, "nbproject/private/private.properties");
        EditableProperties privateProps = RubyProjectTestBase.loadProperties(privateFO);
        privateProps.setProperty(PLATFORM_ACTIVE, "Ruby");
        RubyProjectTestBase.storeProperties(privateFO, privateProps);

        FileObject configFO = touch(prjDirS, "nbproject/private/config.properties");
        EditableProperties configProps = RubyProjectTestBase.loadProperties(configFO);
        configProps.setProperty("config", "jruby");
        RubyProjectTestBase.storeProperties(configFO, configProps);

        FileObject jrubyConfigFO = touch(prjDirS, "nbproject/private/configs/jruby.properties");
        EditableProperties jrubyConfigProps = RubyProjectTestBase.loadProperties(jrubyConfigFO);
        jrubyConfigProps.setProperty(PLATFORM_ACTIVE, "JRuby");
        RubyProjectTestBase.storeProperties(jrubyConfigFO, jrubyConfigProps);

        RailsProjectProperties props = getProperties(project);
        String platform = props.getRunConfigs().get(null).get(PLATFORM_ACTIVE);
        assertEquals("right platform", "Ruby", platform);
    }
    
    public void testNPEIsNotThrownWhenPropertiesAreNotTouched() throws Exception {
        RailsProject project = createTestPlainProject();
        RailsProjectProperties props = getProperties(project);

        assertEquals("initially WEBRICK server", "WEBRICK", project.evaluator().getProperty(RAILS_SERVERTYPE));
        props.save();
        assertEquals("initially WEBRICK server", "WEBRICK", project.evaluator().getProperty(RAILS_SERVERTYPE));
    }

    private RailsProjectProperties getProperties(final RailsProject project) {
        return new RailsProjectProperties(
                project, project.getUpdateHelper(), project.evaluator(),
                project.getReferenceHelper(), project.getLookup().lookup(GeneratedFilesHelper.class));
    }

    private static class WebMockerServer implements RubyInstance {

        public String getServerUri() {
            return "WEBMOCKER";
        }

        public String getDisplayName() { return "WebMocker Server Ruby Edition 1.2.3 update 4"; }
        public ServerState getServerState() { throw uoe(); }
        public Future<OperationState> startServer(RubyPlatform platform) { throw uoe(); }
        public Future<OperationState> stopServer() { throw uoe(); }
        public Future<OperationState> deploy(String applicationName, File applicationDir) { throw uoe(); }
        public Future<OperationState> stop(String applicationName) { throw uoe(); }
        public Future<OperationState> runApplication(RubyPlatform platform, String applicationName, File applicationDir) { throw uoe(); }
        public boolean isPlatformSupported(RubyPlatform platform) { throw uoe(); }
        public void addChangeListener(ChangeListener listener) { throw uoe(); }
        public void removeChangeListener(ChangeListener listener) { throw uoe(); }
        public String getContextRoot(String applicationName) { throw uoe(); }
        public int getRailsPort() { throw uoe(); }
        public String getServerCommand(RubyPlatform platform, String classpath, File applicationDir, int httpPort, boolean debug) { throw uoe(); }
        private void notImpl() { throw uoe(); }

        private UnsupportedOperationException uoe() {
            return new UnsupportedOperationException("Not supported yet.");
        }

    }

}
