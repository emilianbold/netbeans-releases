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
 */

package org.netbeans.modules.apisupport.project.ui.wizard;

import org.openide.WizardDescriptor;

/**
 * Model for storing data gained from <em>NetBeans Plug-in Module</em> wizard
 * panels.
 *
 * @author Martin Krauskopf
 */
final class NewModuleProjectData {

    private WizardDescriptor settings;
    private final int wizardType;

    private boolean netBeansOrg;
    private boolean standalone = true; // standalone is default
    private String projectName;
    private String projectLocation;
    private String projectFolder;
    private String suiteRoot;
    private boolean mainProject;
    private String codeNameBase;
    private String platformID;
    private String bundle;
    private String layer;
    private String projectDisplayName;
    private int moduleCounter;
    private int suiteCounter;
    
    /**
     * @param wizardType one of NewNbModuleWizardIterator.TYPE_*
     */
    NewModuleProjectData(int wizardType) {
        this.wizardType = wizardType;
    }
    
    void setSettings(WizardDescriptor settings) {
        this.settings = settings;
    }
    
    WizardDescriptor getSettings() {
        assert settings != null;
        return settings;
    }
    
    void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }
    
    void setNetBeansOrg(boolean netBeansOrg) {
        this.netBeansOrg = netBeansOrg;
    }
    
    boolean isNetBeansOrg() {
        return netBeansOrg;
    }
    
    boolean isStandalone() {
        return standalone;
    }
    
    boolean isSuiteComponent() {
        return !isNetBeansOrg() && !isStandalone();
    }
    
    String getProjectName() {
        return projectName;
    }
    
    void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    String getProjectLocation() {
        return projectLocation;
    }
    
    void setProjectLocation(String projectLocation) {
        this.projectLocation = projectLocation;
    }
    
    String getProjectFolder() {
        return projectFolder;
    }
    
    void setProjectFolder(String projectFolder) {
        this.projectFolder = projectFolder;
    }
    
    String getSuiteRoot() {
        return suiteRoot;
    }
    
    void setSuiteRoot(String suiteRoot) {
        this.suiteRoot = suiteRoot;
    }
    
    protected boolean isMainProject() {
        return mainProject;
    }
    
    protected void setMainProject(boolean mainProject) {
        this.mainProject = mainProject;
    }
    
    String getCodeNameBase() {
        return codeNameBase;
    }
    
    void setCodeNameBase(String codeNameBase) {
        this.codeNameBase = codeNameBase;
    }
    
    String getPlatformID() {
        return platformID;
    }
    
    void setPlatformID(String platformID) {
        this.platformID = platformID;
    }
    
    String getBundle() {
        return bundle;
    }
    
    void setBundle(String bundle) {
        this.bundle = bundle;
    }
    
    String getLayer() {
        return layer;
    }
    
    void setLayer(String layer) {
        this.layer = layer;
    }
    
    String getProjectDisplayName() {
        return projectDisplayName;
    }
    
    void setProjectDisplayName(String projectDisplayName) {
        this.projectDisplayName = projectDisplayName;
    }
    
    int getModuleCounter() {
        return moduleCounter;
    }
    
    void setModuleCounter(int counter) {
        this.moduleCounter = counter;
    }
    
    int getSuiteCounter() {
        return suiteCounter;
    }
    
    void setSuiteCounter(int counter) {
        this.suiteCounter = counter;
    }
    
    int getWizardType() {
        return wizardType;
    }
    
}
