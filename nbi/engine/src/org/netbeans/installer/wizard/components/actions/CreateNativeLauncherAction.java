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

package org.netbeans.installer.wizard.components.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.system.launchers.LauncherResource;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.launchers.LauncherProperties;
import org.netbeans.installer.utils.system.launchers.impl.CommandLauncher;
import org.netbeans.installer.wizard.components.WizardAction;

/**
 *
 * @author Dmitry Lipin
 */
public class CreateNativeLauncherAction extends WizardAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE = ResourceUtils.getString(
            CreateNativeLauncherAction.class,
            "CNLA.title"); // NOI18N
    
    public static final String DEFAULT_DESCRIPTION = ResourceUtils.getString(
            CreateNativeLauncherAction.class,
            "CNLA.description"); // NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public CreateNativeLauncherAction() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
    }
    
    public void execute() {
        LogManager.logEntry("creating the native launcher");
        
        final String targetPath =
                System.getProperty(Registry.CREATE_BUNDLE_PATH_PROPERTY);
        final File targetFile = new File(targetPath);
        
        final Progress progress = new Progress();
        
        getWizardUi().setProgress(progress);
        try {
            final Platform platform = Registry.getInstance().getTargetPlatform();
            final LauncherProperties properties = new LauncherProperties();
            
            if(platform.isCompatibleWith(Platform.MACOSX)) {
                final String appName = "NetBeans IDE Installer";
                final String testJDKName = ResourceUtils.getResourceFileName(JavaUtils.TEST_JDK_RESOURCE);
                properties.addJar(new LauncherResource(
                        LauncherResource.Type.RELATIVE_LAUNCHER_PARENT,
                        "../Resources/" +
                        appName +
                        "/" + new File(targetPath).getName()));
                properties.setTestJVM(new LauncherResource(
                        LauncherResource.Type.RELATIVE_LAUNCHER_PARENT,
                        "../Resources/" +
                        appName +
                        "/" +
                        testJDKName));
                
                properties.setJvmArguments(new String[]{
                    "-Xmx256m",
                    "-Xms64m"
                });
                properties.setMainClass(Installer.class.getName());
                properties.setTestJVMClass(JavaUtils.TEST_JDK_CLASSNAME);
                
                File tmpDirectory =
                        FileUtils.createTempFile(SystemUtils.getTempDirectory(), false, true);
                FileUtils.mkdirs(tmpDirectory);
                File appDirectory       = new File(tmpDirectory, appName + ".app");
                File contentsDirectory  = new File(appDirectory, "Contents");
                File resDirectory       = new File(contentsDirectory, "Resources");
                File macosDirectory     = new File(contentsDirectory, "MacOS");
                File appInsideDir       = new File(resDirectory, appName);
                File outputFile         = new File(macosDirectory, "executable");
                
                FileUtils.mkdirs(appDirectory);
                FileUtils.mkdirs(contentsDirectory);
                FileUtils.mkdirs(resDirectory);
                FileUtils.mkdirs(appInsideDir);
                FileUtils.mkdirs(macosDirectory);
                
                properties.getJvmArguments().add("-Xdock:icon=" +
                        LauncherResource.Type.RELATIVE_LAUNCHER_PARENT.
                        getPathString("../Resources/icon.icns"));
                properties.setOutput(outputFile, false);
                
                File file = SystemUtils.createLauncher(
                        properties, platform, progress).getOutputFile();
                String uri = System.getProperty(CommandLauncher.JAVA_APPLICATION_ICON_PROPERTY);
                if(uri == null) {
                    uri = CommandLauncher.JAVA_APPLICATION_ICON_DEFAULT_URI;
                }
                File iconFile = FileProxy.getInstance().getFile(uri);
                FileUtils.copyFile(iconFile,
                        new File(resDirectory, "icon.icns"));
                
                File testJDKFile = FileProxy.getInstance().getFile(JavaUtils.TEST_JDK_URI);
                
                FileUtils.copyFile(testJDKFile,
                        new File(appInsideDir, testJDKName));
                
                FileUtils.copyFile(targetFile,
                        new File(appInsideDir, targetFile.getName()));
                
                File infoplist = new File(contentsDirectory, "Info.plist");
                FileUtils.writeFile(infoplist, StringUtils.format(
                        FileUtils.INFO_PLIST_STUB, appName, 1.0, 0));
                
                String name = targetFile.getName();
                int index = name.lastIndexOf(".");
                String zipName = name.substring(0, (index==-1) ? name.length() : index) + ".zip";
                File zipFile = new File(targetFile.getParentFile(), zipName);
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
                FileUtils.zip(appDirectory, zos, appDirectory.getParentFile(), new ArrayList <File> ());
                zos.close();
                FileUtils.deleteFile(tmpDirectory, true);
                System.setProperty(
                        Registry.CREATE_BUNDLE_PATH_PROPERTY,
                        zipFile.getPath());
            } else {
                properties.addJar(new LauncherResource(new File(targetPath)));
                
                properties.setJvmArguments(new String[]{
                    "-Xmx256m",
                    "-Xms64m"
                });
                
                File file = SystemUtils.createLauncher(
                        properties, platform, progress).getOutputFile();
                
                if ( !targetFile.equals(file)) {
                    FileUtils.deleteFile(targetFile);
                    System.setProperty(
                            Registry.CREATE_BUNDLE_PATH_PROPERTY,
                            file.getPath());
                }
            }
        } catch (IOException e) {
            ErrorManager.notifyError("Failed to create the launcher", e);
        } catch (DownloadException e) {
            ErrorManager.notifyError("Failed to create the launcher", e);
        }
        
        LogManager.logExit("finished creating the native launcher");
    }
}
