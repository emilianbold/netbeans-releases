/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */

package org.netbeans.installer.products.nb.javase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.NbClusterConfigurationLogic;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaFXUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.progress.Progress;

/**
 *
 * @author Kirill Sorokin
 */
public class ConfigurationLogic extends NbClusterConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String JAVA_CLUSTER = 
            "{java-cluster}"; // NOI18N
    private static final String APISUPPORT_CLUSTER = 
            "{apisupport-cluster}"; // NOI18N
    private static final String HARNESS_CLUSTER = 
            "{harness-cluster}"; // NOI18N
    private static final String PROFILER_CLUSTER =
            "{profiler-cluster}"; // NOI18N
    private static final String ID = 
            "JAVA"; // NOI18N
    private static final String JUNIT_ACCEPTED_PROPERTY =
            "junit.accepted"; // NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ConfigurationLogic() throws InitializationException {
        super(new String[]{
            JAVA_CLUSTER, 
            APISUPPORT_CLUSTER, 
            HARNESS_CLUSTER,
            PROFILER_CLUSTER}, ID);
    }

    @Override
    public void install(Progress progress) throws InstallationException {
        super.install(progress);
        String junitAccepted = getProduct().getProperty(JUNIT_ACCEPTED_PROPERTY);
        if(junitAccepted != null) {
            final List<Dependency> dependencies =
                    getProduct().getDependencyByUid(BASE_IDE_UID);
            final List<Product> sources =
                    Registry.getInstance().getProducts(dependencies.get(0));

            // pick the first one and integrate with it
            final File nbLocation = sources.get(0).getInstallationLocation();
            String licenseAcceptedText = junitAccepted.equals("true")? 
                getString("CL.junit.accepted.tag") :
                getString("CL.junit.denied.tag");
            LogManager.log("Adding " + licenseAcceptedText + " to license_accepted file");            
            try {
                NetBeansUtils.createLicenseAcceptedMarker(nbLocation, licenseAcceptedText);// NOI18N
            } catch (IOException e) {
                throw new InstallationException(
                        getString("CL.install.error.license.accepted"), // NOI18N
                        e);
            }
        }
        
        // register JavaFX if installed
        final Product product = getProduct();
        File location = getProduct().getInstallationLocation();        
        File sdkLocation = getInstalledFXSDKLocation(product);
        File runtimeLocation = getInstalledFXRuntimeLocation(product);
        
        try {
            if (location != null && sdkLocation != null && runtimeLocation != null) {
                registerJavaFX(location, sdkLocation, runtimeLocation);
            }
        } catch (IOException ex) {
            LogManager.log("... cannot execute commad to register JavaFX", ex);
        }
    }
    
    private boolean registerJavaFX(File nbLocation, File sdkLocation, File reLocation) throws IOException {
        File javaExe = JavaUtils.getExecutable(new File(System.getProperty("java.home")));
        String [] cp = {
            "platform/core/core.jar",
            "platform/lib/boot.jar",
            "platform/lib/org-openide-modules.jar",
            "platform/core/org-openide-filesystems.jar",
            "platform/lib/org-openide-util.jar",
            "platform/lib/org-openide-util-lookup.jar",
            "javafx/modules/org-netbeans-modules-javafx2-platform.jar"
        };
        for(String c : cp) {
            File f = new File(nbLocation, c);
            if(!FileUtils.exists(f)) {
                LogManager.log("... cannot find jar required for JavaFX integration: " + f);
                return false;
            }
        }
        String mainClass = "org.netbeans.modules.javafx2.platform.registration.AutomaticRegistration";
        List <String> commands = new ArrayList <String> ();
        File nbCluster = new File(nbLocation, "nb");
        commands.add(javaExe.getAbsolutePath());
        commands.add("-cp");
        commands.add(StringUtils.asString(cp, File.pathSeparator));
        commands.add(mainClass);        
        commands.add(nbCluster.getAbsolutePath());     
        commands.add(sdkLocation.getAbsolutePath());
        commands.add(reLocation.getAbsolutePath());
        
        return SystemUtils.executeCommand(nbLocation, commands.toArray(new String[]{})).getErrorCode() == 0;
    }
    
    private File getInstalledFXSDKLocation (Product product) {
        String sdkPath = JavaFXUtils.getJavaFXSDKInstallationPath(product.getPlatforms().get(0));
        File sdkLocation = null;
        if (sdkPath != null) {
            sdkLocation = new File(sdkPath);
        }
        return sdkLocation;
    }
    
    private File getInstalledFXRuntimeLocation (Product product) {
        String runtimePath = JavaFXUtils.getJavaFXRuntimeInstallationPath(product.getPlatforms().get(0));
        File runtimeLocation = null;
        if (runtimePath != null) {
            runtimeLocation = new File(runtimePath);
        }
        return runtimeLocation;
    }
}
