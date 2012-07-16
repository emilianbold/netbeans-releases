/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.toolchain.compilerset;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsPanelSupport;
import org.netbeans.modules.cnd.toolchain.compilers.SPICompilerAccesor;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander Simon
 */
public final class ToolchainValidator {
    private static final boolean DISABLED = Boolean.getBoolean("cnd.toolchain.validator.disabled") || CompilerSetManagerImpl.DISABLED; // NOI18N
    private static final Logger LOG = Logger.getLogger(ToolchainValidator.class.getName());

    public static final ToolchainValidator INSTANCE = new ToolchainValidator();
    private static final RequestProcessor RP = new RequestProcessor("Tool collection validator", 1); // NOI18N

    private ToolchainValidator() {
    }

    public void validate(final ExecutionEnvironment env, final CompilerSetManagerImpl csm) {
        if (DISABLED) {
            return;
        }
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                validateImpl(env, csm);
            }
        };
        boolean postpone = env.isRemote() && ! ServerList.get(env).isOnline();
        if (postpone) {
            ServerList.get(env).addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (ServerRecord.PROP_STATE_CHANGED.equals(evt.getPropertyName())) {
                        if (ServerList.get(env).isOnline()) {
                            RP.post(runnable);
                            ServerList.get(env).removePropertyChangeListener(this);
                        }
                    }
                }
            });
        } else {
            RP.post(runnable);
        }
    }

    void applyChanges(Map<Tool, List<List<String>>> needReset, final CompilerSetManagerImpl csm) {
        for(Map.Entry<Tool, List<List<String>>> entry : needReset.entrySet()) {
            Tool tool = entry.getKey();
            List<List<String>> pair = entry.getValue();
            new SPICompilerAccesor(tool).applySystemIncludesAndDefines(pair);
        }
        CompilerSetPreferences.saveToDisk(csm);
        ToolsPanelSupport.fireCodeAssistanceChange(csm);
    }

    private void validateImpl(final ExecutionEnvironment env, final CompilerSetManagerImpl csm) {
        ProgressHandle createHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ToolchainValidator.class, "ToolCollectionValidation", env.getDisplayName())); // NOI18N
        createHandle.start();
        try {
            Map<Tool, List<List<String>>> needReset = new HashMap<Tool, List<List<String>>>();
            for (CompilerSet cs : csm.getCompilerSets()) {
                for (Tool tool : cs.getTools()) {
                    if (tool instanceof AbstractCompiler) {
                        if (tool.getKind() == PredefinedToolKind.CCompiler || tool.getKind() == PredefinedToolKind.CCCompiler) {
                            List<List<String>> systemIncludesAndDefines = new SPICompilerAccesor(tool).getSystemIncludesAndDefines();
                            if (!isEqualsSystemIncludesAndDefines(systemIncludesAndDefines, (AbstractCompiler) tool)) {
                                needReset.put(tool, systemIncludesAndDefines);
                            }
                        }
                    }
                }
            }
            if (needReset.size() > 0) {
                FixCodeAssistancePanel.showNotification(needReset, csm);
            }
        } finally {
            createHandle.finish();
        }
    }

    private boolean isEqualsSystemIncludesAndDefines(List<List<String>> systemIncludesAndDefines, AbstractCompiler tool) {
        if (systemIncludesAndDefines == null) {
            return true;
        }
        List<String> systemIncludeDirectories = tool.getSystemIncludeDirectories();
        List<String> systemPreprocessorSymbols = tool.getSystemPreprocessorSymbols();
        if (!comparePathsLists(systemIncludesAndDefines.get(0), systemIncludeDirectories, tool)) {
            return false;
        }
        if (!compareMacrosLists(systemIncludesAndDefines.get(1), systemPreprocessorSymbols, tool)) {
            return false;
        }
        return true;
    }

    private boolean comparePathsLists(List<String> newList, List<String> oldList, AbstractCompiler tool) {
        Set<String> oldSet = new HashSet<String>(oldList);
        boolean res = true;
        for(String s : newList) {
            if (!oldSet.contains(s)) {
                LOG.log(Level.FINE, "Tool {0} was changed. Added system include path {1}", new Object[]{tool.getDisplayName(), s});
                res = false;
            }
        }
        return res;
    }

    private boolean compareMacrosLists(List<String> newList, List<String> oldList, AbstractCompiler tool) {
        Map<String,String> oldMap = new HashMap<String,String>();
        for(String s : oldList) {
            int i = s.indexOf('=');
            if (i > 0) {
                oldMap.put(s.substring(0,i), s.substring(i+1));
            } else {
                oldMap.put(s, null);
            }
        }
        boolean res = true;
        for(String s : newList) {
            if (s.startsWith("__TIME__") || // NOI18N
                s.startsWith("__DATE__") || // NOI18N
                s.startsWith("__FILE__") || // NOI18N
                s.startsWith("__LINE__")) { // NOI18N
                continue;
            }
            String key;
            String value;
            int i = s.indexOf('=');
            if (i > 0) {
                key = s.substring(0,i);
                value = s.substring(i+1);
            } else {
                key = s;
                value = null;
            }
            if (!oldMap.containsKey(key)) {
                LOG.log(Level.FINE, "Tool {0} was changed. Added macro {1}", new Object[]{tool.getDisplayName(), s});
                res = false;
            }
            String oldValue = oldMap.get(key);
            if (value == null && oldValue == null) {
                // equals
                continue;
            }
            if (value == null && "1".equals(oldValue) ||
                "1".equals(value) && oldValue == null) {
                // equals
                continue;
            }
            if (value != null && oldValue != null && value.equals(oldValue)) {
                // equals
                continue;
            }
            LOG.log(Level.FINE, "Tool {0} was changed. Changed macro {1} from [{2}] to [{3}]", new Object[]{tool.getDisplayName(), key, oldValue, value});
            res = false;
        }
        return res;
    }
}
