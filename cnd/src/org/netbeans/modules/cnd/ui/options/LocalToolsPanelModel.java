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
public class LocalToolsPanelModel extends ToolsPanelModel {
    
    private String compilerSetName;
    private String cCompilerName;
    private String cppCompilerName;
    private String fortranCompilerName;
    private boolean gdbEnabled;
    private boolean gdbRequired;
    private boolean cRequired;
    private boolean cppRequired;
    private boolean fortranRequired;
    
    public LocalToolsPanelModel() {
        compilerSetName = null;
        cCompilerName = null;
        cppCompilerName = null;
        fortranCompilerName = null;
        gdbEnabled = super.isGdbEnabled();
        gdbRequired = CppSettings.getDefault().isGdbRequired();
        cRequired = CppSettings.getDefault().isCRequired();
        cppRequired = CppSettings.getDefault().isCppRequired();
        fortranRequired = CppSettings.getDefault().isFortranRequired();
    }
    
    public boolean isGdbEnabled() {
        return gdbEnabled;
    }
    
    public void setGdbEnabled(boolean enabled) {
        gdbEnabled = enabled;
    }
    
    public void setCompilerSetName(String name) {
        compilerSetName = name;
    }
    
    public String getCompilerSetName() {
        if (compilerSetName == null) {
            compilerSetName = CppSettings.getDefault().getCompilerSetName();
        }
        return compilerSetName;
    }
    
    protected void setCompilerSetDirectories(String directories) {
        CppSettings.getDefault().setCompilerSetDirectories(directories);
    }
    
    protected String getCCompilerName() {
        if (cCompilerName == null) {
            cCompilerName = CppSettings.getDefault().getCCompilerName();
        }
        return cCompilerName;
    }
    
    protected void setCCompilerName(String name) {
        cCompilerName = name;
    }
    
    protected String getCppCompilerName() {
        if (cppCompilerName == null) {
            cppCompilerName = CppSettings.getDefault().getCppCompilerName();
        }
        return cppCompilerName;
    }
    
    protected void setCppCompilerName(String name) {
        cppCompilerName = name;
    }
    
    protected String getFortranCompilerName() {
        if (fortranCompilerName == null) {
            fortranCompilerName = CppSettings.getDefault().getFortranCompilerName();
        }
        return fortranCompilerName;
    }
    
    protected void setFortranCompilerName(String name) {
        fortranCompilerName = name;
    }
    
    public boolean isGdbRequired() {
        return gdbRequired;
    }
    
    public void setGdbRequired(boolean enabled) {
        gdbRequired = enabled;
    }
    
    public boolean isCRequired() {
        return cRequired;
    }
    
    public void setCRequired(boolean enabled) {
        cRequired = enabled;
    }
    
    public boolean isCppRequired() {
        return cppRequired;
    }
    
    public void setCppRequired(boolean enabled) {
        cppRequired = enabled;
    }
    
    public boolean isFortranRequired() {
        return fortranRequired;
    }
    
    public void setFortranRequired(boolean enabled) {
        fortranRequired = enabled;
    }
}
