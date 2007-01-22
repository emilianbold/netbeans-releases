/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 * $Id$
 */

package org.netbeans.installer.wizard.components.actions;

import java.io.*;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.applications.JDKUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.helper.NativeLauncher;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;

/**
 *
 * @author Dmitry Lipin
 */
public class CreateNativeLauncherAction extends WizardAction {
    
    public static final String DEFAULT_TITLE = ResourceUtils.getString(
            CreateNativeLauncherAction.class,
            "CNLA.title"); // NOI18N
    
    public static final String DEFAULT_DESCRIPTION = ResourceUtils.getString(
            CreateNativeLauncherAction.class,
            "CNLA.description"); // NOI18N
    
    ///////////////////////////////////////////////////////////////////////////
    // Instance
    public CreateNativeLauncherAction() {
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
    }
    
    public void execute() {
        LogManager.log("Create native launcher...");
        LogManager.indent();
        final String targetPath = System.getProperty(Installer.CREATE_BUNDLE_PATH_PROPERTY);
        final File   targetFile = new File(targetPath);
        
        Progress progress = new Progress();        
        getWizardUi().setProgress(progress);
        try {
            NativeLauncher nl = new NativeLauncher();
            nl.setJavaVersionMin("1.5.0_01");            
            nl.setJar(new File(targetPath));
            nl.setJvmArguments(new String [] {"-Xmx256m", "-Xms64m"});
            Platform platform = Registry.getInstance().getTargetPlatform();
            File f = nl.createLauncher(platform, progress);
            if ( !targetFile.equals(f)) {
                FileUtils.deleteFile(targetFile);
                System.setProperty(Installer.CREATE_BUNDLE_PATH_PROPERTY, f.getPath());
            }            
        } catch (IOException e) {
            ErrorManager.notifyError("Failed to create the launcher", e);
        } 
        LogManager.unindent();
    }
}
