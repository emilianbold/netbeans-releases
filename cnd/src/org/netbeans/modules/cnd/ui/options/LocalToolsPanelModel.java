/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
    private String gdbPath;
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
        gdbPath = null;
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
    
    public String getGdbPath() {
        return gdbPath;
    }
    
    public void setGdbPath(String gdbPath) {
        this.gdbPath = gdbPath;
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
    
    public boolean showRequiredTools() {
        return true;
    }
}
