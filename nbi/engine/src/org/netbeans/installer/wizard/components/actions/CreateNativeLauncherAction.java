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

import java.io.File;
import java.io.IOException;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.helper.NativeLauncher;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.helper.launchers.JavaCompatibleProperties;
import org.netbeans.installer.utils.progress.Progress;
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
    
    public static final String MIN_JAVA_VERSION_DEFAULT       = "1.5";
    public static final String MIN_JAVA_VERSION_UNIX          = "1.5.0_01";
    public static final String MIN_JAVA_VERSION_WINDOWS       = "1.5.0_06";
    public static final String MIN_JAVA_VERSION_WINDOWS_VISTA = "1.5.0_11";
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public CreateNativeLauncherAction() {
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
    }
    
    public void execute() {
        LogManager.log("Create native launcher...");
        LogManager.indent();
        final String targetPath = System.getProperty(Registry.CREATE_BUNDLE_PATH_PROPERTY);
        final File   targetFile = new File(targetPath);
        
        Progress progress = new Progress();
        getWizardUi().setProgress(progress);
        try {
            
            Platform platform = Registry.getInstance().getTargetPlatform();
            
            NativeLauncher nl = new NativeLauncher();
            switch (platform) {
                // this code should be moved either to the .properties file or smth else outside here
                case WINDOWS :
                    nl.addCompatibleJava(
                            new JavaCompatibleProperties(MIN_JAVA_VERSION_WINDOWS_VISTA, null, null, "Vista", null));
                    nl.addCompatibleJava(
                            new JavaCompatibleProperties(MIN_JAVA_VERSION_WINDOWS, null, null, "XP", null));
                    nl.addCompatibleJava(
                            new JavaCompatibleProperties(MIN_JAVA_VERSION_WINDOWS, null, null, "2000", null));
                    nl.addCompatibleJava(
                            new JavaCompatibleProperties(MIN_JAVA_VERSION_WINDOWS, null, null, "2003", null));
                    break;
                    
                case LINUX :
                case SOLARIS_SPARC :
                case SOLARIS_X86 :
                case MACOS_X_PPC :
                case MACOS_X_X86 :
                    nl.addCompatibleJava(
                            new JavaCompatibleProperties(MIN_JAVA_VERSION_UNIX, null, null, null, null));
                    break;
                    
                default: // something else
                    nl.addCompatibleJava(
                            new JavaCompatibleProperties(MIN_JAVA_VERSION_DEFAULT, null, null, null, null));
                    break;
                    
            }
            
            nl.addBundledJar(new File(targetPath));
            
            nl.setJvmArguments(new String [] {"-Xmx256m", "-Xms64m"});
            
            File f = nl.create(platform, progress);
            
            if ( !targetFile.equals(f)) {
                FileUtils.deleteFile(targetFile);
                System.setProperty(Registry.CREATE_BUNDLE_PATH_PROPERTY, f.getPath());
            }
        } catch (IOException e) {
            ErrorManager.notifyError("Failed to create the launcher", e);
        }
        LogManager.unindent();
    }
}
