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
    
    private String projectName;
    private String projectLocation;
    private String projectFolder;
    private String suiteRoot;
    private boolean mainProject;
    private String codeNameBase;
    private String platform;
    private String bundle;
    private String projectDisplayName;
    
    /** Creates a new instance of NewModuleProjectData */
    NewModuleProjectData() {/* empty constructor */}
    
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
    
    String getPath() {
        return getProjectFolder().substring(getSuiteRoot().length() + 1);
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

    String getProjectDisplayName() {
        return projectDisplayName;
    }

    void setProjectDisplayName(String projectDisplayName) {
        this.projectDisplayName = projectDisplayName;
    }
}
