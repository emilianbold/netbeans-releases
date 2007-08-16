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

package org.netbeans.modules.apisupport.project.ui.wizard.wizard;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.wizard.BasicWizardIterator;
import org.openide.WizardDescriptor;

/**
 * Data model used across the <em>New Wizard Wizard</em>.
 */
final class DataModel extends BasicWizardIterator.BasicDataModel {
    
    private CreatedModifiedFiles cmf;
    
    // first panel data (Wizard Type)
    private boolean branching;
    private boolean fileTemplateType;
    private int nOfSteps;
    
    // second panel data (Name, Icon and Location)
    private String prefix;
    private String displayName;
    private String category;
    private String origIconPath;
    
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
        
        Map<String, String> basicTokens = new HashMap<String, String>();
        basicTokens.put("@@PACKAGE_NAME@@", getPackageName()); // NOI18N
        basicTokens.put("@@WIZARD_PREFIX@@", prefix); // NOI18N
        
        StringBuffer panelsDefinitionBlock = new StringBuffer();
        String newLine = System.getProperty("line.separator") + "                "; // NOI18N
        
        // Create wizard and visual panels
        for (int stepNumber = 1; stepNumber < (nOfSteps + 1); stepNumber++) {
            String visualPanelClass = prefix + "VisualPanel" + stepNumber; // NOI18N
            String wizardPanelClass = prefix + "WizardPanel" + stepNumber; // NOI18N
            
            Map<String, String> replaceTokens = new HashMap<String, String>(basicTokens);
            replaceTokens.put("@@VISUAL_PANEL_CLASS@@", visualPanelClass); // NOI18N
            replaceTokens.put("@@WIZARD_PANEL_CLASS@@", wizardPanelClass); // NOI18N
            replaceTokens.put("@@STEP_NAME@@", "Step #" + stepNumber); // NOI18N
            
            // generate .java file for visual panel
            String path = getDefaultPackagePath(visualPanelClass + ".java", false); // NOI18N
            // XXX use nbresloc URL protocol rather than
            // DataModel.class.getResource(...) and all such a cases below
            URL template = DataModel.class.getResource("visualPanel.javx"); // NOI18N
            cmf.add(cmf.createFileWithSubstitutions(path, template, replaceTokens));
            
            // generate .form file for visual panel
            path = getDefaultPackagePath(visualPanelClass + ".form", false); // NOI18N
            template = DataModel.class.getResource("visualPanel.forx"); // NOI18N
            cmf.add(cmf.createFile(path, template));
            
            // generate .java file for wizard panel
            path = getDefaultPackagePath(wizardPanelClass + ".java", false); // NOI18N
            template = DataModel.class.getResource("wizardPanel.javx"); // NOI18N
            cmf.add(cmf.createFileWithSubstitutions(path, template, replaceTokens));
            
            panelsDefinitionBlock.append("new " + wizardPanelClass + "()"); // NOI18N
            if (stepNumber != nOfSteps) {
                panelsDefinitionBlock.append(',').append(newLine);
            }
        }
        
        cmf.add(cmf.addModuleDependency("org.openide.util")); // NOI18N
        cmf.add(cmf.addModuleDependency("org.openide.dialogs")); // NOI18N
        cmf.add(cmf.addModuleDependency("org.openide.awt")); // NOI18N
        
        // generate .java for wizard iterator
        if (fileTemplateType || branching) {
            String iteratorClass = prefix + "WizardIterator"; // NOI18N
            Map<String, String> replaceTokens = new HashMap<String, String>(basicTokens);
            replaceTokens.put("@@PANELS_DEFINITION_BLOCK@@", panelsDefinitionBlock.toString()); // NOI18N
            replaceTokens.put("@@ITERATOR_CLASS@@", iteratorClass); // NOI18N
            String path = getDefaultPackagePath(iteratorClass + ".java", false); // NOI18N
            URL template = DataModel.class.getResource(fileTemplateType
                    ? "instantiatingIterator.javx" : "wizardIterator.javx"); // NOI18N
            cmf.add(cmf.createFileWithSubstitutions(path, template, replaceTokens));
            
            if (fileTemplateType) {
                // generate .html description for the template
                String lowerCasedPrefix = prefix.substring(0, 1).toLowerCase(Locale.ENGLISH) + prefix.substring(1);
                template = DataModel.class.getResource("wizardDescription.html"); // NOI18N
                cmf.add(cmf.createFile(getDefaultPackagePath(lowerCasedPrefix, true) + ".html", template)); // NOI18N
                
                // add layer entry about a new file wizard
                String instanceFullPath = category + '/' + lowerCasedPrefix;
                cmf.add(cmf.createLayerEntry(instanceFullPath, null, null, displayName, null));
                cmf.add(cmf.createLayerAttribute(instanceFullPath, "template", Boolean.TRUE)); // NOI18N
                String fqIteratorClass = getPackageName() + '.' + iteratorClass;
                cmf.add(cmf.createLayerAttribute(instanceFullPath, "instantiatingIterator", // NOI18N
                        "newvalue:" + fqIteratorClass)); // NOI18N
                try {
                    URL url = new URL("nbresloc:/" + getPackageName().replace('.','/') + '/' // NOI18N
                            + lowerCasedPrefix + ".html"); // NOI18N
                    cmf.add(cmf.createLayerAttribute(instanceFullPath, "templateWizardURL", url)); // NOI18N
                } catch (MalformedURLException ex) {
                    Util.err.notify(ex);
                }
                
                // Copy wizard icon
                if (origIconPath != null && origIconPath.length() > 0) {
                    String relToSrcDir = addCreateIconOperation(cmf, origIconPath);
                    try {
                        URL url = new URL("nbresloc:/" + relToSrcDir); // NOI18N
                        cmf.add(cmf.createLayerAttribute(instanceFullPath, "SystemFileSystem.icon", url)); // NOI18N
                    } catch (MalformedURLException ex) {
                        Util.err.notify(ex);
                    }
                }
            }
        } else {
            Map<String, String> replaceTokens = new HashMap<String, String>(basicTokens);
            replaceTokens.put("@@PANELS_DEFINITION_BLOCK@@", panelsDefinitionBlock.toString()); // NOI18N
            String path = getDefaultPackagePath(prefix + "WizardAction.java", false); // NOI18N
            URL template = DataModel.class.getResource("sampleAction.javx"); // NOI18N
            cmf.add(cmf.createFileWithSubstitutions(path, template, replaceTokens));
        }
    }
    
    private void reset() {
        cmf = null;
    }
    
    void setBranching(boolean branching) {
        this.branching = branching;
    }
    
    boolean isBranching() {
        return branching;
    }
    
    void setFileTemplateType(boolean fileTemplateType) {
        this.fileTemplateType = fileTemplateType;
    }
    
    boolean isFileTemplateType() {
        return fileTemplateType;
    }
    
    void setNumberOfSteps(int nOfSteps) {
        this.nOfSteps = nOfSteps;
    }
    
    void setClassNamePrefix(String prefix) {
        reset();
        this.prefix = prefix;
    }
    
    void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    void setCategory(String category) {
        this.category = category;
    }
    
    void setIcon(String origIconPath) {
        reset();
        this.origIconPath = origIconPath;
    }
    
    public @Override void setPackageName(String packageName) {
        super.setPackageName(packageName);
        reset();
    }
    
}
