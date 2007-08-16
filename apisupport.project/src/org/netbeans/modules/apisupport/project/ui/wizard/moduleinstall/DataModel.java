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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.moduleinstall;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;

/**
 * Data model used across the <em>New Module Installer</em>.
 */
final class DataModel extends BasicWizardIterator.BasicDataModel {

    static final String OPENIDE_MODULE_INSTALL = "OpenIDE-Module-Install"; // NOI18N
    private static final String INSTALLER_CLASS_NAME = "Installer"; // NOI18N
    
    private CreatedModifiedFiles cmf;
    
    DataModel(final WizardDescriptor wiz) {
        super(wiz);
    }
    
    CreatedModifiedFiles getCreatedModifiedFiles() {
        if (cmf == null) {
            regenerate();
        }
        return cmf;
    }
    
    private void regenerate() {
        cmf = new CreatedModifiedFiles(getProject());
        
        // obtain unique class name
        String className = INSTALLER_CLASS_NAME;
        String path = getDefaultPackagePath(className + ".java", false); // NOI18N
        int i = 0;
        while (alreadyExist(path)) {
            className = INSTALLER_CLASS_NAME + '_' + ++i;
            path = getDefaultPackagePath(className + ".java", false); // NOI18N
        }
        
        // generate .java file for ModuleInstall
        Map<String, String> basicTokens = new HashMap<String, String>();
        basicTokens.put("@@PACKAGE_NAME@@", getPackageName()); // NOI18N
        basicTokens.put("@@CLASS_NAME@@", className); // NOI18N
        // XXX use nbresloc URL protocol rather than
        // DataModel.class.getResource(...) and all such a cases below
        URL template = DataModel.class.getResource("moduleInstall.javx"); // NOI18N
        cmf.add(cmf.createFileWithSubstitutions(path, template, basicTokens));
        
        cmf.add(cmf.addModuleDependency("org.openide.modules")); // NOI18N
        cmf.add(cmf.addModuleDependency("org.openide.util")); // NOI18N
        
        // add manifest attribute
        Map<String, String> attribs = new HashMap<String, String>();
        attribs.put(OPENIDE_MODULE_INSTALL, getPackageName().replace('.','/') + '/' + className + ".class"); // NOI18N
        cmf.add(cmf.manifestModification(null, attribs));
    }
    
    private void reset() {
        cmf = null;
    }
    
    public @Override void setPackageName(String packageName) {
        super.setPackageName(packageName);
        reset();
    }
    
    private boolean alreadyExist(String relPath) {
        return getProject().getProjectDirectory().getFileObject(relPath) != null;
    }
    
}
