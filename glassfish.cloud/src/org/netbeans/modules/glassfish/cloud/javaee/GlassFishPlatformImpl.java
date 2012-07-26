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

import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.glassfish.cloud.data.GlassFishUrl;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl2;

/**
 * Common Java EE platform SPI interface implementation for Java EE platform
 * registered with GlassFish cloud.
 * <p/>
 * @author Tomas Kraus, Peter Benedikovic
 */
public abstract class GlassFishPlatformImpl extends J2eePlatformImpl2 {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    // Now there is only GlassFish 4 so we have single option to return.
    /** Set of Java platforms supported by GlassFish cloud. */
    static final Set<String> JAVA_PLATFORMS = new HashSet<String>();
    static {
        JAVA_PLATFORMS.add("1.6");
        JAVA_PLATFORMS.add("1.7");
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance attributes                                                    //
    ////////////////////////////////////////////////////////////////////////////

    /** GlassFish cloud URL. */
    final GlassFishUrl url;

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates an instance of common Java EE platform registered with GlassFish
     * cloud.
     * <p/>
     * Initializes common Java EE platform attributes.
     * <p/>
     * @param url GlassFish cloud URL.
     */
    GlassFishPlatformImpl(GlassFishUrl url) {
        this.url = url;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Implemented Interface Methods                                          //
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Return a set of J2SE platform versions GlassFish cloud can run with.
     * <p/>
     * This method should be updated once there will be more options to return.
     * <p/>
     * @return Set of J2SE platform versions GlassFish cloud can run with.
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Set getSupportedJavaPlatformVersions() {
        // Now there is only GlassFish 4 so we have single option to return.
        return JAVA_PLATFORMS;
    }

    /**
     * Return GlassFish cloud J2SE platform.
     * <p/>
     * Now this method returns default J2SE platform set in NEtBeans. In the
     * future it may return <code>AS_JAVA</code> value set in
     * <code>asenv.conf</code> or <code>asenv.bat</code> files.
     * <p/>
     * @return Default J2SE platform set in NetBeans.
     */
    @Override
    public org.netbeans.api.java.platform.JavaPlatform getJavaPlatform() {
        return JavaPlatformManager.getDefault().getDefaultPlatform();
    }

}
