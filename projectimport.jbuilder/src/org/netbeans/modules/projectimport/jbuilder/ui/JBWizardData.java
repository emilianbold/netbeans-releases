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

package org.netbeans.modules.projectimport.jbuilder.ui;

import java.io.File;
import java.util.Collection;
import org.netbeans.modules.projectimport.j2seimport.ui.BasicPanel;

/**
 *
 * @author Radek Matous
 */
public class JBWizardData extends BasicPanel.WizardData {
    private File projectFile = null;
    private File destinationDir = null;
    private Collection projectDefinition = null;
    private boolean includeDependencies;
    
    
    /** Creates a new instance of JBWizardData */
    public JBWizardData() {
    }


    public File getDestinationDir() {
        return destinationDir;
    }

    void setDestinationDir(File destination) {
        this.destinationDir = destination;
    }

    public File getProjectFile() {
        return projectFile;
    }

    void setProjectFile(File projectFile) {
        this.projectFile = projectFile;
    }

    public Collection getProjectDefinition() {
        return projectDefinition;
    }

    void setProjectDefinition(Collection projectDefinition) {
        this.projectDefinition = projectDefinition;
    }

    public boolean isIncludeDependencies() {
        return includeDependencies;
    }

    void setIncludeDependencies(boolean includeDependencies) {
        this.includeDependencies = includeDependencies;
    }    
}
