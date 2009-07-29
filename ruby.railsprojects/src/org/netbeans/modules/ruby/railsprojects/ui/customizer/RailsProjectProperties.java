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

package org.netbeans.modules.ruby.railsprojects.ui.customizer;

import java.io.IOException;
import java.util.logging.Logger;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatform.Info;
import org.netbeans.modules.ruby.railsprojects.RailsProject;
import org.netbeans.modules.ruby.railsprojects.server.ServerRegistry;
import org.netbeans.modules.ruby.railsprojects.server.spi.RubyInstance;
import org.netbeans.modules.ruby.rubyproject.RubyBaseProject;
import org.netbeans.modules.ruby.rubyproject.SharedRubyProjectProperties;
import org.netbeans.modules.ruby.rubyproject.UpdateHelper;
import org.netbeans.modules.ruby.rubyproject.Util;
import org.netbeans.modules.ruby.spi.project.support.rake.GeneratedFilesHelper;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.modules.ruby.spi.project.support.rake.ReferenceHelper;
import org.openide.util.EditableProperties;

public class RailsProjectProperties extends SharedRubyProjectProperties {
    
    private static final Logger LOGGER = Logger.getLogger(RailsProjectProperties.class.getName());

    public static final String RAILS_PORT = "rails.port"; // NOI18N
    public static final String RAILS_SERVERTYPE = "rails.servertype"; // NOI18N
    public static final String RAILS_ENV = "rails.env"; // NOI18N
    public static final String RAILS_URL = "rails.url"; // NOI18N
    
    /** All per-configuration properties to be stored. */
    private static final String[] CONFIG_PROPS = {
        RAILS_PORT, RAILS_SERVERTYPE, RAKE_ARGS, RAILS_ENV, RAILS_URL,
        MAIN_CLASS, APPLICATION_ARGS, RUBY_OPTIONS, RAILS_SERVERTYPE,
        PLATFORM_ACTIVE
    };
    
    /** Private per-configuration properties. */
    private static final String[] CONFIG_PRIVATE_PROPS = {
        RAILS_PORT, RAILS_ENV, RAKE_ARGS, APPLICATION_ARGS, PLATFORM_ACTIVE
    };

    private RubyInstance server;
    private String railsEnvironment;
    
    public RailsProjectProperties(
            final RubyBaseProject project,
            final UpdateHelper updateHelper,
            final PropertyEvaluator evaluator,
            final ReferenceHelper refHelper,
            final GeneratedFilesHelper genFileHelper) {
        super(project, evaluator, updateHelper, genFileHelper, refHelper);
    }

    RailsProject getRailsProject() {
        return getProject().getLookup().lookup(RailsProject.class);
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
        // nothing needed
    }

    @Override
    protected void storeProperties(EditableProperties projectProperties, EditableProperties privateProperties) throws IOException {
        if (server != null) {
            privateProperties.remove(RAILS_SERVERTYPE);
            projectProperties.setProperty(RAILS_SERVERTYPE, server.getServerUri());
        }
        if (getRailsEnvironment() != null) {
            privateProperties.setProperty(RAILS_ENV, getRailsEnvironment());
        }
        RubyPlatform platform = getPlatform();
        if (platform == null) {
            LOGGER.fine("Project has invalid platform (null).");
            return;
        }
        Info info = platform.getInfo();
        Util.logUsage(RailsProjectProperties.class, "USG_PROJECT_CONFIG_RAILS", // NOI18N
                info.getKind(),
                info.getPlatformVersion(),
                info.getGemVersion(),
                getServerIdForLogging(),
                "", // XXX database seems to not be configurable, so this attribute does not make sense here?
                ""); // XXX rails version - the same as above 'database' attribute?
    }
    
    // see #150975
    private String getServerIdForLogging() {
        if (server != null) {
            return server.getDisplayName();
        }
        String serverURI = evaluator.getProperty(RAILS_SERVERTYPE);
        RubyInstance serverInstance = ServerRegistry.getDefault().getServer(serverURI, getPlatform());
        return serverInstance != null ? serverInstance.getDisplayName() : "";
    }

    RubyInstance getServer() {
        return this.server;
    }

    void setServer(final RubyInstance server, final String config) {
        getRunConfigs().get(config).put(RailsProjectProperties.RAILS_SERVERTYPE, server.getServerUri());
        this.server = server;
    }

    String getRailsEnvironment() {
        return railsEnvironment;
    }

    void setRailsEnvironment(final String railsEnvironment, final String config) {
        if (railsEnvironment != null) {
            getRunConfigs().get(config).put(RailsProjectProperties.RAILS_ENV, railsEnvironment);
        }
        this.railsEnvironment = railsEnvironment;
    }

}
