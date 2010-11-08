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

package org.netbeans.modules.git;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.Preferences;
import org.netbeans.modules.git.FileInformation.Mode;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.util.NbPreferences;

/**
 *
 * @author ondra
 */
public final class GitModuleConfig {
    
    private static GitModuleConfig instance;
    private static final String AUTO_OPEN_OUTPUT_WINDOW = "autoOpenOutput";// NOI18N
    private static final String PROP_COMMIT_EXCLUSIONS  = "commitExclusions";                           // NOI18N
    private static final String PROP_LAST_USED_MODE     = "lastUsedMode"; //NOI18N
    
    private String lastCanceledCommitMessage;
    
    public static GitModuleConfig getDefault () {
        if (instance == null) {
            instance = new GitModuleConfig();
        }
        return instance;
    }
    private Set<String> exclusions;
    
    private GitModuleConfig() { }

    public boolean isExcludedFromCommit(String absolutePath) {
        return false;
    }

    public Color getColor(String colorName, Color defaultColor) {
         int colorRGB = getPreferences().getInt(colorName, defaultColor.getRGB());
         return new Color(colorRGB);
    }

    public void setColor(String colorName, Color value) {
         getPreferences().putInt(colorName, value.getRGB());
    }

    public Preferences getPreferences() {
        return NbPreferences.forModule(GitModuleConfig.class);
    }

    public boolean getExludeNewFiles() {
        // XXX
        return false;
    }
    
    public String getLastCanceledCommitMessage() {
        return lastCanceledCommitMessage == null ? "" : lastCanceledCommitMessage; //NOI18N
    }

    public void setLastCanceledCommitMessage(String message) {
        lastCanceledCommitMessage = message;
    }
    
    /**
     * @param paths collection of paths, of File.getAbsolutePath()
     */
    public void addExclusionPaths(Collection<String> paths) {
        Set<String> exclusions = getCommitExclusions();
        if (exclusions.addAll(paths)) {
            Utils.put(getPreferences(), PROP_COMMIT_EXCLUSIONS, new ArrayList<String>(exclusions));
        }
    }

    /**
     * @param paths collection of paths, File.getAbsolutePath()
     */
    public void removeExclusionPaths(Collection<String> paths) {
        Set<String> exclusions = getCommitExclusions();
        if (exclusions.removeAll(paths)) {
            Utils.put(getPreferences(), PROP_COMMIT_EXCLUSIONS, new ArrayList<String>(exclusions));
        }
    }   
    
    synchronized Set<String> getCommitExclusions() {
        if (exclusions == null) {
            exclusions = new HashSet<String>(Utils.getStringList(getPreferences(), PROP_COMMIT_EXCLUSIONS));
        }
        return exclusions;
    }  
    
    public Mode getLastUsedModificationContext () {
        Mode mode;
        try {
            mode = Mode.valueOf(getPreferences().get(PROP_LAST_USED_MODE, Mode.HEAD_VS_WORKING_TREE.name()));
        } catch (IllegalArgumentException ex) {
            mode = null;
        }
        return mode == null ? Mode.HEAD_VS_WORKING_TREE : mode;
    }

    public void setLastUsedModificationContext (Mode mode) {
        getPreferences().put(PROP_LAST_USED_MODE, mode.name());
    }    
    
    public boolean getAutoOpenOutput() {
        return getPreferences().getBoolean(AUTO_OPEN_OUTPUT_WINDOW, true);
    }

    public void setAutoOpenOutput(boolean value) {
        throw new UnsupportedOperationException("Not supported yet.");           
    }    
    
}
