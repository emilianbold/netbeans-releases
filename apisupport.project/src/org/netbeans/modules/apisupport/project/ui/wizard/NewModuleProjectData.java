/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.wizard;

/**
 * Model for storing data gained from Wizard panels.
 *
 * @author mkrauskopf
 */
final class NewModuleProjectData {
    
    static final String DATA_PROPERTY_NAME = "moduleProjectData"; // NOI18N
    
    private boolean standalone = true; // standalone is default
    private String projectName;
    private String projectLocation;
    private String projectFolder;
    private String suiteRoot;
    private boolean mainProject;
    private String codeNameBase;
    private String platform;
    private String bundle;
    private String layer;
    private String projectDisplayName;

    /** Creates a new instance of NewModuleProjectData */
    NewModuleProjectData() {/* empty constructor */}
    
    void setStandalone(boolean standalone) {
        this.standalone = standalone;
    }
    
    boolean isStandalone() {
        return standalone;
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
    
    String getPlatform() {
        return platform;
    }
    
    void setPlatform(String platform) {
        this.platform = platform;
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
}
