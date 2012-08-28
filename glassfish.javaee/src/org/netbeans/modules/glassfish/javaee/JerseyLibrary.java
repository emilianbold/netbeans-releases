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
package org.netbeans.modules.glassfish.javaee;

import java.net.URL;
import java.util.List;
import org.glassfish.tools.ide.data.GlassFishLibrary;
import org.glassfish.tools.ide.data.GlassFishVersion;
import org.glassfish.tools.ide.server.config.LibraryBuilder;
import org.glassfish.tools.ide.server.config.LibraryConfig;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.ServerUtilities;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;

/**
 * GlassFish bundled Jersey library provider.
 * <p/>
 * Builds <code>LibraryImplementation3</code> instance containing Jersey
 * library from GlassFish modules.
 * Actually only GlassFish v3 and v4 are supported.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class JerseyLibrary {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Library builder default configuration file. */
    private static final URL LIBRARY_BUILDER_CONFIG_DEFAULT
            = JerseyLibrary.class.getResource("JerseyLibsDefault.xml");

    /** Library builder configuration since GlassFish 4. */
    private static final LibraryConfig.Next LIBRARY_BUILDER_CONFIG_V2
            = new LibraryConfig.Next(GlassFishVersion.GF_4,
            JerseyLibrary.class.getResource("JerseyLibs2.xml"));

    /** Library builder configuration for GlassFish cloud. */
    private static final LibraryConfig libraryConfig = new LibraryConfig(
            LIBRARY_BUILDER_CONFIG_DEFAULT, LIBRARY_BUILDER_CONFIG_V2);

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** Library builder associated with current platform.
      * This attribute should be accessed only using {@see #getBuilder()} even
      * internally. */
    private volatile LibraryBuilder builder;

    /** GlassFish server deployment manager. */
    private Hk2DeploymentManager dm;

    /** GlassFish server home directory. */
    private String serverHome;
    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Jersey library provider.
     * <p/>
     * @param url GlassFish server URL.
     */
    private JerseyLibrary(Hk2DeploymentManager dm) {
        if (dm == null) {
            throw new IllegalArgumentException(
                    "GlassFish server deployment manager shall not be null.");
        }
        this.dm = dm;
        this.serverHome = dm.getCommonServerSupport().getInstanceProperties()
                .get(GlassfishModule.GLASSFISH_FOLDER_ATTR);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Return Jersey libraries available in GlassFish v3.
     */
    public LibraryImplementation getLibrary() {
        LibraryBuilder lb = getBuilder();
        List<GlassFishLibrary> libs = lb.getLibraries(GlassFishVersion.GF_3);
        LibraryImplementation lis = CommonProjectUtils
//                = LibrariesSupport.createLibraryImplementation(type, volumeIds);
        return lis;
    }

    /**
     * Initialize library builder on demand.
     */
    private LibraryBuilder getBuilder() {
        if (builder != null) {
            return builder;
        }
        synchronized(this) {
            if (builder == null) {
                builder = new LibraryBuilder(libraryConfig,
                        serverHome, serverHome, serverHome);
            }
        }
        return builder;
    }

    /**
     * Get Jersey library
     */


}
