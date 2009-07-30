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

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/** Manage the data for the ToolsPanel */
public abstract class ToolsPanelModel {
    
//    protected ArrayList getPath() {
//        ArrayList<String> path = new ArrayList();
//        StringTokenizer tok = new StringTokenizer(CppSettings.getDefault().getPath(), File.pathSeparator);
//        while (tok.hasMoreTokens()) {
//            path.add(tok.nextToken());
//        }
//        return path;
//    }
//    
//    protected void setPath(ArrayList<String> list) {
//        StringBuffer path = new StringBuffer();
//        for (String dir : list) {
//            path.append(dir);
//            path.append(File.pathSeparator);
//        }
//        CppSettings.getDefault().setPath(path.toString());
//    }
//    
//    protected String getMakeName() {
//        return CppSettings.getDefault().getMakeName();
//    }
//    
//    protected void setMakeName(String name) {
//        CppSettings.getDefault().setMakeName(name);
//    }
//    
//    protected void setMakePath(String dir) {
//        CppSettings.getDefault().setMakePath(dir);
//    }
//    
//    public String getGdbName() {
//        return CppSettings.getDefault().getGdbName();
//    }
//    
//    public void setGdbName(String name) {
//        CppSettings.getDefault().setGdbName(name);
//    }
//    
//    public String getGdbPath() {
//        return null;
//    }
//    
//    public void setGdbPath(String dir) {
//        CppSettings.getDefault().setGdbPath(dir);
//    }
//    
//    public abstract void setGdbEnabled(boolean value);
    
    public abstract void setMakeRequired(boolean value);
    
    public abstract boolean isMakeRequired();
    
    public abstract boolean isDebuggerRequired();
    
    public abstract void setDebuggerRequired(boolean value);
    
    public abstract boolean isCRequired();
    
    public abstract void setCRequired(boolean value);
    
    public abstract boolean isCppRequired();
    
    public abstract void setCppRequired(boolean value);
    
    public abstract boolean isFortranRequired();
    
    public abstract void setFortranRequired(boolean value);

    public abstract boolean isAsRequired();

    public abstract void setAsRequired(boolean value);
    
    public abstract void setCompilerSetName(String name);
    
    public abstract String getCompilerSetName();
    
    public void setSelectedCompilerSetName(String name) {};
    
    public String getSelectedCompilerSetName() {return null;}
    
//    protected abstract void setCCompilerName(String name);
//    
//    protected abstract void setCppCompilerName(String name);
//    
//    protected abstract void setFortranCompilerName(String name);
    
    public abstract boolean showRequiredTools();
    
    public abstract void setShowRequiredBuildTools(boolean value);
    
    public abstract boolean showRequiredBuildTools();
    
    public abstract void setShowRequiredDebugTools(boolean value);
    
    public abstract boolean showRequiredDebugTools();
    
    public void setEnableRequiredCompilerCB(boolean enabled) {}
    
    public boolean enableRequiredCompilerCB() {return true;}

    private ExecutionEnvironment selectedDevelopmentHost = null;

    public void setSelectedDevelopmentHost(ExecutionEnvironment env) {
        selectedDevelopmentHost = env;
    }

    public ExecutionEnvironment getSelectedDevelopmentHost() {
        return selectedDevelopmentHost;
    }

    public abstract void setEnableDevelopmentHostChange(boolean value);

    public abstract boolean getEnableDevelopmentHostChange();
}
