/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.toolchain.support;

import java.nio.charset.Charset;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetImpl;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerFlavorImpl;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.api.toolchain.CompilerFlavor;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.toolchain.ToolchainManager.ToolchainDescriptor;
import org.netbeans.modules.cnd.toolchain.compilerset.APIAccessor;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetManagerAccessorImpl;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetManagerImpl;
import org.netbeans.modules.cnd.toolchain.compilerset.CompilerSetPreferences;
import org.netbeans.modules.cnd.toolchain.compilerset.ToolUtils;
import org.netbeans.modules.cnd.toolchain.compilerset.ToolchainManagerImpl;
import org.netbeans.modules.cnd.toolchain.compilerset.ToolchainValidator;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Utilities;
import org.openide.util.WeakSet;

/**
 *
 * @author Alexander Simon
 */
public final class ToolchainUtilities {


    private ToolchainUtilities() {
    }
   
private static final Set<ChangeListener> codeAssistanceChanged = new WeakSet<ChangeListener>();

    public static void addCodeAssistanceChangeListener(ChangeListener l) {
        synchronized (codeAssistanceChanged) {
            codeAssistanceChanged.add(l);
        }
    }

    public static void removeCodeAssistanceChangeListener(ChangeListener l) {
        synchronized (codeAssistanceChanged) {
            codeAssistanceChanged.remove(l);
        }
    }

    public static void fireCodeAssistanceChange(CompilerSetManager csm) {
        ChangeEvent ev = new ChangeEvent(csm);
        synchronized (codeAssistanceChanged) {
            for (ChangeListener l : codeAssistanceChanged) {
                l.stateChanged(ev);
            }
        }
    }
        
    public static List<CompilerSet> findRemoteCompilerSets(CompilerSetManager csm, String path) {
        if (!csm.getClass().equals(CompilerSetManagerImpl.class)) {
            throw new UnsupportedOperationException("CompilerSetManager class can not be overriden by clients"); // NOI18N
        }
        return ((CompilerSetManagerImpl) csm).findRemoteCompilerSets(path);
    }

    public static CompilerSet createCopy(CompilerSet cs, ExecutionEnvironment env, CompilerFlavor flavor, String directory, String name, String displayName,
            boolean autoGenerated, boolean keepToolFlavor, String setBuildPath, String setRunPath) {
        if (!cs.getClass().equals(CompilerSetImpl.class)) {
            throw new UnsupportedOperationException("CompilerSet class can not be overriden by clients"); // NOI18N
        }
        return ((CompilerSetImpl) cs).createCopy(env, flavor, directory, name, displayName, autoGenerated, keepToolFlavor, setBuildPath, setRunPath);
    }

    public static CompilerSet initCompilerSet(CompilerSetManager csm, ExecutionEnvironment env, CompilerFlavor flavor, String directory) {
        if (!csm.getClass().equals(CompilerSetManagerImpl.class)) {
            throw new UnsupportedOperationException("CompilerSetManager class can not be overriden by clients"); // NOI18N
        }
        CompilerSetImpl cs = CompilerSetImpl.create(flavor, env, directory);
        cs.setAutoGenerated(false);
        ((CompilerSetManagerImpl) csm).initCompilerSet(cs);
        return cs;
    }

    public static void setCSName(CompilerSet cs, String compilerSetName) {
        if (!cs.getClass().equals(CompilerSetImpl.class)) {
            throw new UnsupportedOperationException("CompilerSet class can not be overriden by clients"); // NOI18N
        }
        ((CompilerSetImpl) cs).setName(compilerSetName);
    }

    public static CompilerSetManager create(ExecutionEnvironment execEnv) {
        return CompilerSetManagerAccessorImpl.create(execEnv);
    }

    public static CompilerSetManager getDeepCopy(ExecutionEnvironment execEnv, boolean initialize) {
        return CompilerSetManagerAccessorImpl.getDeepCopy(execEnv, initialize);
    }
    
    public static CompilerSetManager deepCopy(CompilerSetManager csm) {
        if (!csm.getClass().equals(CompilerSetManagerImpl.class)) {
            throw new UnsupportedOperationException("CompilerSetManager class can not be overriden by clients"); // NOI18N
        }
        return ((CompilerSetManagerImpl)csm).deepCopy();
    }    

    public static void saveCompileSetManagers(Collection<CompilerSetManager> allCSMs, List<ExecutionEnvironment> liveServers) {
        CompilerSetManagerAccessorImpl.setManagers(allCSMs, liveServers);
    }

    public static String getUniqueCompilerSetName(CompilerSetManager csm, String baseName) {
        if (!csm.getClass().equals(CompilerSetManagerImpl.class)) {
            throw new UnsupportedOperationException("CompilerSetManager class can not be overriden by clients"); // NOI18N
        }
        return ((CompilerSetManagerImpl)csm).getUniqueCompilerSetName(baseName);
    }
    
    public static void setModifyBuildPath(CompilerSet cs, String modifyBuildPath) {
        if (!cs.getClass().equals(CompilerSetImpl.class)) {
            throw new UnsupportedOperationException("CompilerSet class can not be overriden by clients"); // NOI18N
        }
        ((CompilerSetImpl) cs).setModifyBuildPath(modifyBuildPath);        
    }
    
    public static void setModifyRunPath(CompilerSet cs, String modifyRunPath) {
        if (!cs.getClass().equals(CompilerSetImpl.class)) {
            throw new UnsupportedOperationException("CompilerSet class can not be overriden by clients"); // NOI18N
        }
        ((CompilerSetImpl) cs).setModifyRunPath(modifyRunPath);        
    }    
    
    public static void setToolPath(Tool tool, String p) {
        APIAccessor.get().setToolPath(tool, p);
    }
    
    public static void setCharset(Charset charset, CompilerSet cs) {
        APIAccessor.get().setCharset(charset, cs);
    }    
    
    public static void fixCSM(final Map<Tool, List<List<String>>> needReset, CompilerSetManager csm) {
        ToolchainValidator.INSTANCE.applyChanges(needReset, csm);
    }

}
