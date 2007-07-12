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
import java.util.Iterator;
import java.util.StringTokenizer;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/** Manage the data for the ToolsPanel */
public abstract class ToolsPanelModel {
    
    protected ArrayList getPath() {
        ArrayList<String> path = new ArrayList();
        StringTokenizer tok = new StringTokenizer(CppSettings.getDefault().getPath(), File.pathSeparator);
        while (tok.hasMoreTokens()) {
            path.add(tok.nextToken());
        }
        return path;
    }
    
    protected void setPath(ArrayList<String> list) {
        StringBuffer path = new StringBuffer();
        for (String dir : list) {
            path.append(dir);
            path.append(File.pathSeparator);
        }
        CppSettings.getDefault().setPath(path.toString());
    }
    
    protected String getMakeName() {
        return CppSettings.getDefault().getMakeName();
    }
    
    protected void setMakeName(String name) {
        CppSettings.getDefault().setMakeName(name);
    }
    
    protected void setMakePath(String dir) {
        CppSettings.getDefault().setMakePath(dir);
    }
    
    public String getGdbName() {
        return CppSettings.getDefault().getGdbName();
    }
    
    public void setGdbName(String name) {
        CppSettings.getDefault().setGdbName(name);
    }
    
    public String getGdbPath() {
        return null;
    }
    
    public void setGdbPath(String dir) {
        CppSettings.getDefault().setGdbPath(dir);
    }
    
    /**
     * Check if the gdb module is enabled. Don't show the gdb line if it isn't.
     *
     * @return true if the gdb module is enabled, false if missing or disabled
     */
    protected boolean isGdbEnabled() {
        Iterator iter = Lookup.getDefault().lookup(new Lookup.Template(ModuleInfo.class)).allInstances().iterator();
        while (iter.hasNext()) {
            ModuleInfo info = (ModuleInfo) iter.next();
            if (info.getCodeNameBase().equals("org.netbeans.modules.cnd.debugger.gdb") && info.isEnabled()) { // NOI18N
                return true;
            }
        }
        return false;
    }
    
    public abstract void setGdbEnabled(boolean value);
    
    public abstract boolean isGdbRequired();
    
    public abstract void setGdbRequired(boolean value);
    
    public abstract boolean isCRequired();
    
    public abstract void setCRequired(boolean value);
    
    public abstract boolean isCppRequired();
    
    public abstract void setCppRequired(boolean value);
    
    public abstract boolean isFortranRequired();
    
    public abstract void setFortranRequired(boolean value);
    
    public abstract void setCompilerSetName(String name);
    
    public abstract String getCompilerSetName();
    
    protected abstract void setCCompilerName(String name);
    
    protected abstract void setCppCompilerName(String name);
    
    protected abstract void setFortranCompilerName(String name);
}
