/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 *
 *     "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 */
package org.netbeans.installer.products.nb.uml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.NbClusterConfigurationLogic;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.EnvironmentScope;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.WindowsNativeUtils;
import org.netbeans.installer.utils.system.windows.WindowsRegistry;

/**
 *
 * @author Kirill Sorokin
 */
public class ConfigurationLogic extends NbClusterConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String CLUSTER =
            "uml{uml-cluster-version}"; // NOI18N
    public static final String ID =
            "UML"; // NOI18N
    private static final long XMX_VALUE_REQUIRED = 512 * NetBeansUtils.M;
    
    private static final String MACOSX_QUARTZ_OPTION =
            "-Dapple.awt.graphics.UseQuartz"; //NOI18N
    
    private static final String PATH_ENV = "PATH"; //NOI18N
    
    private static final String CONFIG_DOORS_LOCATION =            
            "modules" + File.separator +  //NOI18N
            "DoorsIntegrationFiles" + File.separator +
            "modules" +  File.separator + "bin";//NOI18N
    
    private static final String CSCRIPT = "cscript"; //NOI18N
    
    private static final String CONFIG_DOORS_COMMAND =
            "modules" + File.separator +  //NOI18N
            "DoorsIntegrationFiles" + File.separator + //NOI18N
            "configDoors.vbs"; //NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ConfigurationLogic() throws InitializationException {
        super(CLUSTER, ID);
    }
    
    public void install(final Progress progress) throws InstallationException {
        // get the list of suitable netbeans ide installations
        LogManager.log("Configuring UML...");
        List<Dependency> dependencies =
                getProduct().getDependencyByUid(IDE_UID);
        List<Product> sources =
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one and integrate with it
        final File nbLocation = sources.get(0).getInstallationLocation();
        
        
        if (nbLocation != null) {
            progress.setDetail(getString("CL.install.netbeans.conf.uml")); // NOI18N
            try {
                // TODO
                // this option should be change back after UML uninstallation
                long xmx = NetBeansUtils.getJvmMemorySize(nbLocation, NetBeansUtils.MEMORY_XMX);
                if(xmx < XMX_VALUE_REQUIRED) {
                    NetBeansUtils.setJvmMemorySize(nbLocation,
                            NetBeansUtils.MEMORY_XMX,
                            XMX_VALUE_REQUIRED);
                }
                if(SystemUtils.isMacOS()) {
                    // TODO
                    // This option should not be set if JDK6 is used for NB
                    NetBeansUtils.setJvmOption(nbLocation, MACOSX_QUARTZ_OPTION, "false");
                }
            } catch (IOException ex) {
                throw new InstallationException(
                        getString("CL.install.error.netbeans.conf.uml"),
                        ex);
            }
            
            /////////////////////////////////////////////////////////////////////////////
            // Integrate with Telelogic Doors
            if(SystemUtils.isWindows()) {
                try {
                    LogManager.indent();
                    progress.setDetail(
                            getString("CL.install.telelogic.integration")); // NOI18N
                    configureTelelogicDoors(nbLocation, progress, true);
                } catch (IOException ex) {
                    throw new InstallationException(
                            getString("CL.install.error.telelogic.integration"),
                            ex);
                } finally {
                    LogManager.unindent();
                }
            }
            
        }
        /////////////////////////////////////////////////////////////////////////////
        super.install(progress);
    }
    
    public void uninstall(final Progress progress) throws UninstallationException {
        
        // get the list of suitable netbeans ide installations
        List<Dependency> dependencies =
                getProduct().getDependencyByUid(IDE_UID);
        List<Product> sources =
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one and integrate with it
        final File nbLocation = sources.get(0).getInstallationLocation();
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.uninstall.netbeans.conf.uml")); // NOI18N
            
            NetBeansUtils.removeJvmOption(nbLocation, MACOSX_QUARTZ_OPTION);
        } catch (IOException e) {
            throw new UninstallationException(
                    getString("CL.uninstall.error.netbeans.conf.uml"), // NOI18N
                    e);
        }
        /////////////////////////////////////////////////////////////////////////////
        // Cancel integration with Telelogic Doors
        if(SystemUtils.isWindows()) {            
            try {
                LogManager.indent();
                progress.setDetail(
                        getString("CL.uninstall.telelogic.integration.cancel")); // NOI18N
                configureTelelogicDoors(nbLocation, progress, false);
            } catch (IOException ex) {
                throw new UninstallationException(
                        getString("CL.uninstall.error.telelogic.integration.cancel"),
                        ex);
            } finally {
                LogManager.unindent();
            }
        }
        
        /////////////////////////////////////////////////////////////////////////////
        super.uninstall(progress);
    }
    
    private void configureTelelogicDoors(File nbLocation, Progress progress, boolean install) throws IOException {
        try {
            
            File    location = getProduct().getInstallationLocation();
            String  doorsBin = new File(location, CONFIG_DOORS_LOCATION).getPath();
            
            ////////////////////////////////////////////////////////////////////
            // First configuration step:
            // Integrate (if install) with telelogic itself
            // If uninstall then skip this step
            if(install) {
                LogManager.log(ErrorLevel.DEBUG,
                        "... running script that integrates Telelogic Doors with NetBeans UML");
                
                String processPathEnv = SystemUtils.getEnvironmentVariable(PATH_ENV);
                if(processPathEnv==null) {
                    processPathEnv = StringUtils.EMPTY_STRING;
                }
                
                if(!processPathEnv.contains(doorsBin)) {
                    processPathEnv += File.pathSeparator + doorsBin;
                    SystemUtils.setEnvironmentVariable(PATH_ENV,processPathEnv);
                }
                
                SystemUtils.executeCommand(nbLocation, new String [] {
                    CSCRIPT,
                    new File(location, CONFIG_DOORS_COMMAND).getPath(),
                    nbLocation.getAbsolutePath()
                });
            } else {
                LogManager.log(ErrorLevel.DEBUG,
                        "... cancel telelogic doors integration");
            }
            
            ////////////////////////////////////////////////////////////////////
            // Second configuration step:
            // Install   : modify system/user PATH variable so the necessary .dll would be in path
            // Uninstall : remove the necessary path from the the system envvar PATH
            LogManager.log(ErrorLevel.DEBUG,
                    "... modify PATH environent variable within windows registry");
            WindowsRegistry winReg =
                    ((WindowsNativeUtils) SystemUtils.getNativeUtils()).
                    getWindowsRegistry();
            
            // set appropriate environment scope
            EnvironmentScope scope = winReg.canModifyKey(
                    WindowsRegistry.HKEY_LOCAL_MACHINE,
                    WindowsNativeUtils.ALL_USERS_ENVIRONMENT_KEY) ?
                        EnvironmentScope.ALL_USERS :
                        EnvironmentScope.CURRENT_USER;
            
            LogManager.log(ErrorLevel.DEBUG,
                    "... environment access level is " + scope.toString());
            
            String pathValue = SystemUtils.getEnvironmentVariable(PATH_ENV, scope, false);
            LogManager.log(ErrorLevel.DEBUG,
                    "... old PATH : " + pathValue);
            if (pathValue==null && !install) {
                // no PATH env variable in the registry and is not an installation process
                return;
            }
            
            pathValue = (pathValue == null) ? StringUtils.EMPTY_STRING : pathValue;
            
            if (install) {
                if(!pathValue.contains(doorsBin)) {
                    pathValue += File.pathSeparator + doorsBin;
                }
            } else {
                // remove all occurences of the path to DLL directory
                pathValue = pathValue.
                        replace(File.pathSeparator + doorsBin, StringUtils.EMPTY_STRING). // remove ";%path%""
                        replace(doorsBin, StringUtils.EMPTY_STRING); //remove "%path%" if they still exist
                
            }
            
            // last parameters is true because PATH in most cases set to expandable
            LogManager.log(ErrorLevel.DEBUG,
                    "... new PATH : " + pathValue);
            SystemUtils.setEnvironmentVariable(PATH_ENV, pathValue, scope, true);            
            LogManager.log(ErrorLevel.DEBUG,
                    "... Telelogic Doors configuration finished");
            
        } catch (NativeException ex) {
            IOException e = new IOException();
            e.initCause(ex);
            throw e;
        }
    }
}
