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

package org.netbeans.installer.products.jdk;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.StringUtils;
import static org.netbeans.installer.utils.StringUtils.QUOTE;
import static org.netbeans.installer.utils.StringUtils.BACK_SLASH;
import static org.netbeans.installer.utils.StringUtils.EMPTY_STRING;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.EnvironmentScope;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.helper.RemovalMode;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;

/**
 *
 * @author Dmitry Lipin
 */
public class ConfigurationLogic extends ProductConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private List<WizardComponent> wizardComponents;
    
    public ConfigurationLogic() throws InitializationException {
        wizardComponents = Wizard.loadWizardComponents(
                WIZARD_COMPONENTS_URI,
                getClass().getClassLoader());
    }
    
    public void install(
            final Progress progress) throws InstallationException {
        final File location = getProduct().getInstallationLocation();
        final File installer = new File(location, JDK_INSTALLER_FILE_NAME);
        
        try {
            File logFile = LogManager.getLogFile();
            
            if(logFile!=null) {
                String name = logFile.getName();
                String ext;
                if(name.lastIndexOf(".")==-1) {
                    name += "_jdk.log";
                } else {
                    ext = name.substring(name.lastIndexOf("."));
                    name = name.substring(0, name.lastIndexOf(".")) +
                            "_jdk" + ext;
                }
                
                logFile = new File(LogManager.getLogFile().getParentFile(),name);
            }
            logFile = null;
            String [] commands = null;
            ExecutionResults results = null;
            progress.setDetail(PROGRESS_DETAIL_RUNNING_JDK_INSTALLER);
            
            if(SystemUtils.isWindows()) {
                results = runJDKInstallerWindows(location, installer, logFile);
            } else {
                results = runJDKInstallerUnix(location, installer, logFile);
            }
            
            
            if(results.getErrorCode()!=0) {
                throw new InstallationException(
                        ResourceUtils.getString(ConfigurationLogic.class,
                        ERROR_INSTALL_SCRIPT_RETURN_NONZERO_KEY,
                        StringUtils.EMPTY_STRING + results.getErrorCode()));
            }
        }  finally {
            try {
                FileUtils.deleteFile(installer);
            } catch (IOException e) {
                LogManager.log("Cannot delete installer file "+ installer, e);
                
            }
        }
        
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }
    
    private ExecutionResults runJDKInstallerWindows(File location, File installer, File logFile) throws InstallationException {
        try {
            SystemUtils.setEnvironmentVariable("TEMP",
                    SystemUtils.getTempDirectory().getAbsolutePath(),
                    EnvironmentScope.PROCESS,
                    false);
            SystemUtils.setEnvironmentVariable("TMP",
                    SystemUtils.getTempDirectory().getAbsolutePath(),
                    EnvironmentScope.PROCESS,
                    false);
            
            final String loggingOption = (logFile!=null) ?
                "/log " + BACK_SLASH + QUOTE  + logFile.getAbsolutePath()  + BACK_SLASH + QUOTE +" ":
                EMPTY_STRING;
            final String installLocationOption = "/qn INSTALLDIR=" + BACK_SLASH + QUOTE + location + BACK_SLASH + QUOTE;
            
            String [] commands = new String [] {
                installer.getAbsolutePath(),
                "/s",
                "/v" + loggingOption + installLocationOption};
            return SystemUtils.executeCommand(location, commands);
        } catch (NativeException e) {
            throw new InstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_INSTALL_ERROR_KEY),e);
        } catch (IOException e) {
            throw new InstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_INSTALL_ERROR_KEY),e);
        }
    }
    
    private ExecutionResults runJDKInstallerUnix(File location, File installer, File logFile) throws InstallationException {
        File yesFile = null;
        ExecutionResults results = null;
        try {
            yesFile = FileUtils.createTempFile();
            FileUtils.writeFile(yesFile, "yes" + SystemUtils.getLineSeparator());
            
            final String loggingOption = (logFile!=null) ?
                " > " + logFile.getAbsolutePath() + " 2>&1"  :
                EMPTY_STRING;
            
            String [] commands = new String [] {
                "/bin/sh", "-c",
                installer.getAbsolutePath() +
                        " < " + yesFile.getAbsolutePath() +
                        loggingOption
            };
            results = SystemUtils.executeCommand(location, commands);
            
            // unix JDK installers create extra level directory jdkxxx
            File [] jdkDirs = location.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return (pathname.isDirectory() &&
                            pathname.getName().startsWith("jdk"));
                }
            });
            
            try {
                for(File dir : jdkDirs) {
                    for(File f : dir.listFiles()) {
                        FileUtils.moveFile(f, location);
                    }
                }
            }  catch (IOException e) {
                throw new InstallationException(
                        ResourceUtils.getString(ConfigurationLogic.class,
                        ERROR_INSTALL_CANNOT_MOVE_DATA_KEY),e);
            }
        } catch (IOException e) {
            throw new InstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_INSTALL_ERROR_KEY),e);
        } finally {
            if(yesFile!=null) {
                try {
                    FileUtils.deleteFile(yesFile);
                } catch (IOException e) {
                    LogManager.log(e);
                }
            }
        }
        return results;
    }
    @Override
    public boolean registerInSystem() {
        return false;
    }
    
    public void uninstall(
            final Progress progress) throws UninstallationException {
        final File location = getProduct().getInstallationLocation();
        
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public List<WizardComponent> getWizardComponents() {
        return wizardComponents;
    }
    
    @Override
    public String getIcon() {
        if (SystemUtils.isWindows()) {
            return "bin/javaws.exe";
        } else {
            return null;
        }
    }
    @Override
    public int getLogicPercentage() {
        return 50;
    }
    
    @Override
    public boolean allowModifyMode() {
        return false;
    }
    public RemovalMode getRemovalMode() {
        return RemovalMode.ALL;
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String WIZARD_COMPONENTS_URI =
            "resource:" + // NOI18N
            "org/netbeans/installer/products/jdk/wizard.xml"; // NOI18N
    
    
    public static final String JDK_INSTALLER_FILE_NAME =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.jdk.installer.file");
    public static final String ERROR_INSTALL_SCRIPT_RETURN_NONZERO_KEY =
            "CL.error.installation.return.nonzero";//NOI18N
    public static final String ERROR_INSTALL_ERROR_KEY =
            "CL.error.install.exception";//NOI18N
    public static final String ERROR_INSTALL_CANNOT_MOVE_DATA_KEY =
            "CL.error.install.cannot.move.data";//NOI18N
    public static final String PROGRESS_DETAIL_RUNNING_JDK_INSTALLER =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.progress.detail.install.jdk");
}
