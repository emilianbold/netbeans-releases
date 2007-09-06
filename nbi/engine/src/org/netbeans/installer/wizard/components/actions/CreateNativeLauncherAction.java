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
import java.io.IOException;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.system.launchers.LauncherResource;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.launchers.LauncherProperties;
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
        } catch (IOException e) {
            ErrorManager.notifyError("Failed to create the launcher", e);
        } 
        LogManager.logExit("finished creating the native launcher");
    }
}
