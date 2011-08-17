/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.apisupport.project.ui.wizard.wizard;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

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
        basicTokens.put("PACKAGE_NAME", getPackageName()); // NOI18N
        basicTokens.put("WIZARD_PREFIX", prefix); // NOI18N
        
        StringBuffer panelsDefinitionBlock = new StringBuffer();
        String newLine = System.getProperty("line.separator") + "                "; // NOI18N
        
        // Create wizard and visual panels
        for (int stepNumber = 1; stepNumber < (nOfSteps + 1); stepNumber++) {
            String visualPanelClass = prefix + "VisualPanel" + stepNumber; // NOI18N
            String wizardPanelClass = prefix + "WizardPanel" + stepNumber; // NOI18N
            
            Map<String, String> replaceTokens = new HashMap<String, String>(basicTokens);
            replaceTokens.put("VISUAL_PANEL_CLASS", visualPanelClass); // NOI18N
            replaceTokens.put("WIZARD_PANEL_CLASS", wizardPanelClass); // NOI18N
            replaceTokens.put("STEP_NAME", "Step #" + stepNumber); // NOI18N
            
            // generate .java file for visual panel
            String path = getDefaultPackagePath(visualPanelClass + ".java", false); // NOI18N
            FileObject template = CreatedModifiedFiles.getTemplate("visualPanel.java"); // NOI18N
            cmf.add(cmf.createFileWithSubstitutions(path, template, replaceTokens));
            
            // generate .form file for visual panel
            path = getDefaultPackagePath(visualPanelClass + ".form", false); // NOI18N
            template = CreatedModifiedFiles.getTemplate("visualPanel.form"); // NOI18N
            cmf.add(cmf.createFile(path, template));
            
            // generate .java file for wizard panel
            path = getDefaultPackagePath(wizardPanelClass + ".java", false); // NOI18N
            template = CreatedModifiedFiles.getTemplate("wizardPanel.java"); // NOI18N
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
            replaceTokens.put("PANELS_DEFINITION_BLOCK", panelsDefinitionBlock.toString()); // NOI18N
            replaceTokens.put("ITERATOR_CLASS", iteratorClass); // NOI18N
            String path = getDefaultPackagePath(iteratorClass + ".java", false); // NOI18N
            FileObject template = CreatedModifiedFiles.getTemplate(fileTemplateType
                    ? "instantiatingIterator.java" : "wizardIterator.java"); // NOI18N
            cmf.add(cmf.createFileWithSubstitutions(path, template, replaceTokens));
            
            if (fileTemplateType) {
                // generate .html description for the template
                String lowerCasedPrefix = prefix.substring(0, 1).toLowerCase(Locale.ENGLISH) + prefix.substring(1);
                template = CreatedModifiedFiles.getTemplate("wizardDescription.html"); // NOI18N
                cmf.add(cmf.createFileWithSubstitutions(getDefaultPackagePath(lowerCasedPrefix, true) + ".html", template, Collections.<String,String>emptyMap())); // NOI18N
                
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
            replaceTokens.put("PANELS_DEFINITION_BLOCK", panelsDefinitionBlock.toString()); // NOI18N
            String path = getDefaultPackagePath(prefix + "WizardAction.java", false); // NOI18N
            FileObject template = CreatedModifiedFiles.getTemplate("sampleAction.java"); // NOI18N
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
