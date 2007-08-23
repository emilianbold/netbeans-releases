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
import java.net.URL;
import java.net.URLDecoder;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StreamUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.EngineResources;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;

/**
 *
 * @author Dmitry Lipin
 */
public class CacheEngineAction extends WizardAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public CacheEngineAction() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
    }
    
    public void execute() {
        try {
            final Progress progress = new Progress();
            
            getWizardUi().setProgress(progress);
            Installer.cacheInstallerEngine(progress);            
        } catch (IOException e) {
            ErrorManager.notifyCritical("Cannot cache engine", e);
        } 
    }
    
    @Override
    public boolean isCancelable() {
        return false;
    }

    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE = ResourceUtils.getString(
            CacheEngineAction.class,
            "CEA.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION = ResourceUtils.getString(
            CacheEngineAction.class,
            "CEA.description"); // NOI18N
}
