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

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.settings.CppSettings;

/** Manage the data for the ToolsPanel */
public class GlobalToolsPanelModel extends ToolsPanelModel {
    
    public void setCompilerSetName(String name) {
        CppSettings.getDefault().setCompilerSetName(name);
    }
    
    public String getCompilerSetName() {
        return CppSettings.getDefault().getCompilerSetName();
    }
    
    protected void setCCompilerName(String name) {
        CppSettings.getDefault().setCCompilerName(name);
    }
    
    protected void setCppCompilerName(String name) {
        CppSettings.getDefault().setCppCompilerName(name);
    }
    
    protected void setFortranCompilerName(String name) {
        CppSettings.getDefault().setFortranCompilerName(name);
    }
    
    public boolean isGdbRequired() {
        return CppSettings.getDefault().isGdbRequired();
    }
    
    public void setGdbRequired(boolean value) {
        CppSettings.getDefault().setGdbRequired(value);
    }
    
    public void setGdbEnabled(boolean enabled) {
        // Do nothing
    }
    
    public boolean isCRequired() {
        return CppSettings.getDefault().isCRequired();
    }
    
    public void setCRequired(boolean value) {
        CppSettings.getDefault().setCRequired(value);
    }
    
    public boolean isCppRequired() {
        return CppSettings.getDefault().isCppRequired();
    }
    
    public void setCppRequired(boolean value) {
        CppSettings.getDefault().setCppRequired(value);
    }
    
    public boolean isFortranRequired() {
        return CppSettings.getDefault().isFortranRequired();
    }
    
    public void setFortranRequired(boolean value) {
        CppSettings.getDefault().setFortranRequired(value);
    }
}
