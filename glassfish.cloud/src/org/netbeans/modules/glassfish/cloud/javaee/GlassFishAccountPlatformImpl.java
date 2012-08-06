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
package org.netbeans.modules.glassfish.cloud.javaee;

import java.awt.Image;
import java.io.File;
import org.netbeans.modules.glassfish.cloud.data.GlassFishUrl;
import org.netbeans.spi.project.libraries.LibraryImplementation;

/**
 * Java EE platform SPI interface implementation for Java EE platform registered
 * with GlassFish cloud.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public class GlassFishAccountPlatformImpl extends GlassFishPlatformImpl {

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of Java EE platform registered with GlassFish cloud.
     * <p/>
     * @param url GlassFish cloud URL.
     */
    GlassFishAccountPlatformImpl(GlassFishUrl url) {
        super(url);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Interface Methods                                          //
    ////////////////////////////////////////////////////////////////////////////

    @Override
    public File getServerHome() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public File getDomainHome() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public File getMiddlewareHome() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LibraryImplementation[] getLibraries() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDisplayName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Image getIcon() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public File[] getPlatformRoots() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Return class path for the specified tool.
     * <p/>
     * Use the tool constants declared  in the
     * {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform}.
     * </p>
     * @param  toolName Tool name, for example
     *         {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform#TOOL_APP_CLIENT_RUNTIME}.
     * @return Class path for the specified tool.
     */
    @Override
    public File[] getToolClasspathEntries(String toolName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    /**
     * Specifies whether a tool of the given name is supported by GlassFish
     * cloud.
     * <p/>
     * @param toolName Tool name, for example
     *        {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform#TOOL_APP_CLIENT_RUNTIME}.
     * @return Always returns <code>false</code>. This method is not supported.
     * @deprecated
     */
    @Deprecated
    @Override    
    public boolean isToolSupported(String toolName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
