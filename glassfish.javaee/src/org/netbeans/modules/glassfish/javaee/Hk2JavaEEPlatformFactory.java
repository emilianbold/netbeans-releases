/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.glassfish.javaee;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.deploy.spi.DeploymentManager;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.J2eePlatformImpl;
import org.openide.util.NbBundle;


/**
 *
 * @author Ludo
 * @author vince
 */
public class Hk2JavaEEPlatformFactory extends J2eePlatformFactory {

    // This had been a public static method that looked like createEe6(),
    // but the way the layer.xml entry is interpreted would not let that
    // work... so I had to conver it into this form.
    /**
     * @deprecated this is meant to be used by the layer.xml and NO ONE ELSE
     */
    public Hk2JavaEEPlatformFactory() {
    }

    public static Hk2JavaEEPlatformFactory createPrelude() {
        return new Hk2JavaEEPlatformFactory(
            NbBundle.getMessage(Hk2JavaEEPlatformFactory.class, "MSG_MyPreludeServerPlatform"),
            JavaPlatformManager.getDefault().getDefaultPlatform(),
            NbBundle.getMessage(Hk2JavaEEPlatformFactory.class, "LBL_PRELUDE_LIBRARY"),
            "J2EE/DeploymentPlugins/gfv3/Lookup",
            new HashSet(Arrays.asList(new String[] {"1.6","1.5"})),
            new HashSet<J2eeModule.Type>(Arrays.asList(new J2eeModule.Type[] { J2eeModule.Type.WAR })),
            new HashSet<Profile>(Arrays.asList(new Profile[] { Profile.J2EE_13, Profile.J2EE_14,
                Profile.JAVA_EE_5})));
    }

    public static Hk2JavaEEPlatformFactory createEe6() {
        String dn = NbBundle.getMessage(Hk2JavaEEPlatformFactory.class, "MSG_MyServerPlatform");
        JavaPlatform jp = null; // JavaPlatformManager.getDefault().getDefaultPlatform();
        String ln = NbBundle.getMessage(Hk2JavaEEPlatformFactory.class, "LBL_V3_LIBRARY");
        String lk = "J2EE/DeploymentPlugins/gfv3ee6/Lookup";
        Set sjp = new HashSet(Arrays.asList(new String[] {"1.6"}));
        Set<J2eeModule.Type> smt = new HashSet<J2eeModule.Type>(Arrays.asList(new J2eeModule.Type[] { J2eeModule.Type.WAR,
            J2eeModule.Type.CAR, J2eeModule.Type.EAR, J2eeModule.Type.EJB, J2eeModule.Type.RAR }));
        return new Hk2JavaEEPlatformFactory(dn,jp,ln,lk,sjp,smt,
                new HashSet<Profile>(Arrays.asList(new Profile[] { Profile.J2EE_13, Profile.J2EE_14,
                Profile.JAVA_EE_5, Profile.JAVA_EE_6_FULL, Profile.JAVA_EE_6_WEB })));

    }

    private String displayName;
    private JavaPlatform javaPlatform;
    private String libraryName;
    private String lookupKey;
    private Set supportedJavaPlatforms;
    private Set<J2eeModule.Type> supportedModuleTypes;
    private Set<Profile> supportedProfiles;

    protected Hk2JavaEEPlatformFactory(String displayName,
            JavaPlatform jp, String libraryName, String lookupKey, 
            Set supportedJavaPlatforms,
            Set<J2eeModule.Type> supportedModuleTypes,
            Set<Profile> supportedProfiles) {
        this.displayName = displayName;
        this.javaPlatform = jp;
        this.libraryName = libraryName;
        this.lookupKey = lookupKey;
        this.supportedJavaPlatforms = supportedJavaPlatforms;
        this.supportedModuleTypes = supportedModuleTypes;
        this.supportedProfiles = supportedProfiles;
    }
    
    public J2eePlatformImpl getJ2eePlatformImpl(DeploymentManager dm) {
        return new Hk2JavaEEPlatformImpl((Hk2DeploymentManager) dm, this);
    }

    String getDisplayName() {
        return displayName;
    }

    JavaPlatform getJavaPlatform() {
        return javaPlatform;
    }

    String getLibraryName() {
        return libraryName;
    }

    String getLookupKey() {
        return lookupKey;
    }

    Set getSupportedJavaPlatforms() {
        Set retVal = new HashSet();
        retVal.addAll(supportedJavaPlatforms);
        return retVal;
    }

    Set<J2eeModule.Type> getSupportedTypes() {
        Set<J2eeModule.Type> retVal = new HashSet<J2eeModule.Type>();
        retVal.addAll(supportedModuleTypes);
        return retVal;
    }

    Set<Profile> getSupportedProfiles() {
        Set<Profile> retVal = new HashSet<Profile>();
        retVal.addAll(supportedProfiles);
        return retVal;
    }
}