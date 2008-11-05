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

package org.netbeans.modules.cnd.makeproject.api.compilers;

import java.io.IOException;
import java.util.List;
import org.netbeans.modules.cnd.api.compilers.CompilerSet.CompilerFlavor;
import org.netbeans.modules.cnd.api.compilers.ToolchainManager.CompilerDescriptor;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.api.utils.PlatformInfo;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

public abstract class SunCCCCompiler extends CCCCompiler {
    protected PersistentList<String> systemIncludeDirectoriesList = null;
    protected PersistentList<String> systemPreprocessorSymbolsList = null;
    
    public SunCCCCompiler(String hkey, CompilerFlavor flavor, int kind, String name, String displayName, String path) {
        super(hkey, flavor, kind, name, displayName, path);
    }
    
    
    @Override
    public boolean setSystemIncludeDirectories(List<String> values) {
        assert values != null;
        if (values.equals(systemIncludeDirectoriesList)) {
            return false;
        }
        systemIncludeDirectoriesList = new PersistentList<String>(values);
        normalizePaths(systemIncludeDirectoriesList);
        saveSystemIncludesAndDefines();
        return true;
    }
    
    @Override
    public boolean setSystemPreprocessorSymbols(List<String> values) {
        assert values != null;
        if (values.equals(systemPreprocessorSymbolsList)) {
            return false;
        }
        systemPreprocessorSymbolsList = new PersistentList<String>(values);
        saveSystemIncludesAndDefines();
        return true;
    }
    
    @Override
    public List<String> getSystemPreprocessorSymbols() {
        if (systemPreprocessorSymbolsList != null) {
            return systemPreprocessorSymbolsList;
        }
        
        getSystemIncludesAndDefines();
        return systemPreprocessorSymbolsList;
    }
    
    @Override
    public List<String> getSystemIncludeDirectories() {
        if (systemIncludeDirectoriesList != null) {
            return systemIncludeDirectoriesList;
        }
        
        getSystemIncludesAndDefines();
        return systemIncludeDirectoriesList;
    }
    
    
    @Override
    public void saveSystemIncludesAndDefines() {
        if (systemIncludeDirectoriesList != null) {
            systemIncludeDirectoriesList.saveList(getUniqueID() + "systemIncludeDirectoriesList"); // NOI18N
        }
        if (systemPreprocessorSymbolsList != null) {
            systemPreprocessorSymbolsList.saveList(getUniqueID() + "systemPreprocessorSymbolsList"); // NOI18N
        }
    }
    
    private void restoreSystemIncludesAndDefines() {
        systemIncludeDirectoriesList = PersistentList.restoreList(getUniqueID() + "systemIncludeDirectoriesList"); // NOI18N
        systemPreprocessorSymbolsList = PersistentList.restoreList(getUniqueID() + "systemPreprocessorSymbolsList"); // NOI18N
    }
    
    private void getSystemIncludesAndDefines() {
        restoreSystemIncludesAndDefines();
        if (systemIncludeDirectoriesList == null || systemPreprocessorSymbolsList == null) {
            getFreshSystemIncludesAndDefines();
        }
    }
    
    protected String getDefaultPath() {
        CompilerDescriptor compiler = getDescriptor();
        if (compiler != null && compiler.getNames().length > 0){
            return compiler.getNames()[0];
        }
        return ""; // NOI18N
    }

    protected abstract String getCompilerStderrCommand();
    protected abstract String getCompilerStderrCommand2();
    
    private void getFreshSystemIncludesAndDefines() {
        systemIncludeDirectoriesList = new PersistentList<String>();
        systemPreprocessorSymbolsList = new PersistentList<String>();
        String path = getPath();
        if (path == null || !PlatformInfo.getDefault(getHostKey()).fileExists(path)) {
            path = getDefaultPath();
        }
        try {
            getSystemIncludesAndDefines(IpeUtils.getDirName(path), path + getCompilerStderrCommand(), false);
            if (getCompilerStderrCommand2() != null) {
                getSystemIncludesAndDefines(IpeUtils.getDirName(path), path + getCompilerStderrCommand2(), false);
            }
            systemIncludeDirectoriesList.addUnique(applyPathPrefix("/usr/include")); // NOI18N

            saveSystemIncludesAndDefines();
        } catch (IOException ioe) {
            System.err.println("IOException " + ioe);
            String errormsg = NbBundle.getMessage(getClass(), "CANTFINDCOMPILER", path); // NOI18N
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
        }
    }
    
    @Override
    public void resetSystemIncludesAndDefines() {
        getFreshSystemIncludesAndDefines();
    }
}
