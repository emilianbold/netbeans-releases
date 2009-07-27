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

import org.netbeans.modules.cnd.settings.CppSettings;

/** Manage the data for the ToolsPanel */
/*package-local*/ final class GlobalToolsPanelModel extends ToolsPanelModel {
    
    public void setCompilerSetName(String name) {
        CppSettings.getDefault().setCompilerSetName(name);
    }
    
    public String getCompilerSetName() {
        return CppSettings.getDefault().getCompilerSetName();
    }
//    
//    protected void setCCompilerName(String name) {
//        CppSettings.getDefault().setCCompilerName(name);
//    }
//    
//    protected void setCppCompilerName(String name) {
//        CppSettings.getDefault().setCppCompilerName(name);
//    }
//    
//    protected void setFortranCompilerName(String name) {
//        CppSettings.getDefault().setFortranCompilerName(name);
//    }
    public void setMakeRequired(boolean value) {
        
    }
    
    public boolean isMakeRequired() {
        return true;
    }
    
    public boolean isDebuggerRequired() {
        return true;
    }
    
    public void setDebuggerRequired(boolean value) {
//        CppSettings.getDefault().setGdbRequired(value);
    }
//    
//    public void setGdbEnabled(boolean enabled) {
//        // Do nothing
//    }
    
    public boolean isCRequired() {
        return true; //return CppSettings.getDefault().isCRequired();
    }
    
    public void setCRequired(boolean value) {
//        CppSettings.getDefault().setCRequired(value);
    }
    
    public boolean isCppRequired() {
        return true; //return CppSettings.getDefault().isCppRequired();
    }
    
    public void setCppRequired(boolean value) {
//        CppSettings.getDefault().setCppRequired(value);
    }
    
    public boolean isFortranRequired() {
        return true;
        //return CppSettings.getDefault().isFortranEnabled();
        //return CppSettings.getDefault().isFortranRequired();
    }
    
    public void setFortranRequired(boolean value) {
//        CppSettings.getDefault().setFortranRequired(value);
    }

    public boolean isAsRequired() {
        return true;
    }

    public void setAsRequired(boolean value) {
    }
    
    public boolean showRequiredTools() {
        return false;
    }
    
    public void setRequiredBuildTools(boolean enabled) {
 
    }
    
    public void setShowRequiredBuildTools(boolean enabled) {
        
    }
    
    public boolean showRequiredBuildTools() {
        return false;
    }
    
    public void setShowRequiredDebugTools(boolean enabled) {
        
    }
    
    public boolean showRequiredDebugTools() {
        return false;
    }

    @Override
    public void setEnableDevelopmentHostChange(boolean value) {
    }

    @Override
    public boolean getEnableDevelopmentHostChange() {
        return true;
    }
}
