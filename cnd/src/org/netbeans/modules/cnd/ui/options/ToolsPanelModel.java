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


package org.netbeans.modules.cnd.ui.options;

import org.netbeans.modules.cnd.settings.CppSettings;

/** Manage the data for the ToolsPanel */
public class ToolsPanelModel {
    
    protected String getMakeName() {
        return CppSettings.getDefault().getMakeName();
    }
    
    protected String getGdbName() {
        return CppSettings.getDefault().getGdbName();
    }
    
    protected void setCompilerSetName(String name) {
        CppSettings.getDefault().setCompilerSetName(name);
    }
    
    protected String getCompilerSetName() {
        return CppSettings.getDefault().getCompilerSetName();
    }
    
    protected void setCompilerSetDirectories(String directories) {
        CppSettings.getDefault().setCompilerSetDirectories(directories);
    }
    
    protected String getCCompilerName() {
        String dbg = CppSettings.getDefault().getCCompilerName();
        return CppSettings.getDefault().getCCompilerName();
    }
    
    protected void setCCompilerName(String name) {
        CppSettings.getDefault().setCCompilerName(name);
    }
    
    protected String getCppCompilerName() {
        return CppSettings.getDefault().getCppCompilerName();
    }
    
    protected void setCppCompilerName(String name) {
        CppSettings.getDefault().setCppCompilerName(name);
    }
    
    protected String getFortranCompilerName() {
        return CppSettings.getDefault().getFortranCompilerName();
    }
    
    protected void setFortranCompilerName(String name) {
        CppSettings.getDefault().setFortranCompilerName(name);
    }
}
