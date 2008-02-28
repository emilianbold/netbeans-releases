/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Sun
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.installer.products.sjsam;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import org.netbeans.installer.utils.applications.GlassFishUtils;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.helper.NbiThread;
import org.netbeans.installer.utils.helper.RemovalMode;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;

/**
 *
 * @author Kirill Sorokin
 */
public class ConfigurationLogic extends ProductConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String WIZARD_COMPONENTS_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX + // NOI18N
            "org/netbeans/installer/products/sjsam/wizard.xml"; // NOI18N
    
    private static final String GLASSFISH_UID =
            "glassfish"; // NOI18N
    private static final String APPSERVER_UID =
            "sjsas"; // NOI18N
    
    private static final String AM_INSTALLER =
            "addons/am_installer.jar"; // NOI18N
    private static final String AM_SUBDIR =
            "addons/accessmanager"; // NOI18N
    private static final String AM_CONFIGURATOR =
            "lib/addons/am-configurator.jar"; // NOI18N
    
    private static final String AMSERVER_DIR_INSIDE_AS =
            "domains/domain1/applications/j2ee-modules/amserver"; // NOI18N
    
    private static final String AM_ADDITIONAL_CP =
            "lib/appserv-ext.jar";
    
    private static final String ACCESS_MANAGER_UH =
            "AccessManager"; // NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private List<WizardComponent> wizardComponents;
    
    public ConfigurationLogic() throws InitializationException {
        wizardComponents = Wizard.loadWizardComponents(
                WIZARD_COMPONENTS_URI,
                getClass().getClassLoader());
    }
    
    public void install(Progress progress) throws InstallationException {
        final File installLocation = getProduct().getInstallationLocation();
        
        // get the list of suitable glassfish installations
        final List<Dependency> dependencies =
                getProduct().getDependencyByUid(APPSERVER_UID);
        final List<Product> sources =
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one and integrate with it
        final File asLocation = sources.get(0).getInstallationLocation();
        
        // resolve the dependency
        dependencies.get(0).setVersionResolved(sources.get(0).getVersion());
        
        final File amInstaller = new File(asLocation, AM_INSTALLER);
        final File javaExecutable;
        try {
            javaExecutable = JavaUtils.getExecutable(
                    GlassFishUtils.getJavaHome(asLocation));
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.java.home"), // NOI18N
                    e);
        }
        progress.setPercentage(Progress.START);
        final int DELTA_PROGRESS = 10;
        // stop the default domain //////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.stop.as")); // NOI18N
            
            GlassFishUtils.stopDefaultDomain(asLocation);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.stop.as"), // NOI18N
                    e);
        }
        
        // run the access manager installer /////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.am.installer")); // NOI18N
            File extCp = new File(asLocation, AM_ADDITIONAL_CP);
            String mainClass = new JarFile(amInstaller).getManifest().
                    getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
            
            ExecutionResults results = SystemUtils.executeCommand(
                    installLocation,
                    javaExecutable.getAbsolutePath(),
                    "-cp", // NOI18N
                    extCp.getAbsolutePath() +
                    SystemUtils.getPathSeparator() +
                    amInstaller.getAbsolutePath(),
                    mainClass,
                    asLocation.getAbsolutePath(),
                    "true", // NOI18N
                    "localhost"); // NOI18N
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.am.installer"), // NOI18N
                    e);
        }
        
        // install the jvm option ///////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.add.jvm.option")); // NOI18N
            
            GlassFishUtils.setJvmOption(
                    asLocation,
                    GlassFishUtils.DEFAULT_DOMAIN,
                    "-Dcom.sun.enterprise.server.ss.ASQuickStartup=false"); // NOI18N
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.add.jvm.option"), // NOI18N
                    e);
        } catch (XMLException e) {
            throw new InstallationException(
                    getString("CL.install.error.add.jvm.option"), // NOI18N
                    e);
        }
        progress.addPercentage(DELTA_PROGRESS);
        
        // start the default domain /////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.start.as")); // NOI18N
            
            GlassFishUtils.startDefaultDomain(asLocation);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.start.as"), // NOI18N
                    e);
        }
        progress.addPercentage(DELTA_PROGRESS);
        
        // stop the default domain //////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.stop.as")); // NOI18N
            
            GlassFishUtils.stopDefaultDomain(asLocation);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.stop.as"), // NOI18N
                    e);
        }
        progress.addPercentage(DELTA_PROGRESS * 7);
        
        // remove the jvm option ///////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.remove.jvm.option")); // NOI18N
            
            GlassFishUtils.removeJvmOption(
                    asLocation,
                    GlassFishUtils.DEFAULT_DOMAIN,
                    "-Dcom.sun.enterprise.server.ss.ASQuickStartup=false"); // NOI18N
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.remove.jvm.option"), // NOI18N
                    e);
        } catch (XMLException e) {
            throw new InstallationException(
                    getString("CL.install.error.remove.jvm.option"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public void uninstall(Progress progress) throws UninstallationException {
        // get the list of suitable glassfish installations
        final List<Dependency> dependencies =
                getProduct().getDependencyByUid(APPSERVER_UID);
        final List<Product> sources =
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one and integrate with it
        final File asLocation = sources.get(0).getInstallationLocation();
        
        final File amSubdir = new File(asLocation, AM_SUBDIR);
        final File amConfigurator = new File(asLocation, AM_CONFIGURATOR);
        
        final File amHomeFile = new File(
                new File(SystemUtils.getUserHomeDirectory(), ACCESS_MANAGER_UH),
                getAMServerLinkName(asLocation));
        
        // stop the default domain //////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.uninstall.stop.as")); // NOI18N
            
            GlassFishUtils.stopDefaultDomain(asLocation);
        } catch (IOException e) {
            throw new UninstallationException(
                    getString("CL.uninstall.error.stop.as"), // NOI18N
                    e);
        }
        
        // remove some extra files //////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.uninstall.extra.files")); // NOI18N
            
            FileUtils.deleteFile(amSubdir, true);
            FileUtils.deleteFile(amConfigurator);
            FileUtils.deleteWithEmptyParents(amHomeFile);
        } catch (IOException e) {
            throw new UninstallationException(
                    getString("CL.uninstall.error.extra.files"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public List<WizardComponent> getWizardComponents() {
        return wizardComponents;
    }
    
    @Override
    public boolean registerInSystem() {
        return false;
    }
    
    @Override
    public int getLogicPercentage() {
        return 90 ;
    }
    
    @Override
    public RemovalMode getRemovalMode() {
        return RemovalMode.LIST;
    }
    
    @Override
    public Text getLicense() {
       return null;
    }     
    
    // private //////////////////////////////////////////////////////////////////////
    private String getAMServerLinkName(final File asLocation) {
        final File file = new File(asLocation, AMSERVER_DIR_INSIDE_AS);
        final File root = FileUtils.getRoot(file);
        
        String result = file.
                getPath().
                substring(root.getPath().length()).
                replace(File.separatorChar, '_');
        
        return "AMConfig" + "_" + result + "_";
    }   
}
