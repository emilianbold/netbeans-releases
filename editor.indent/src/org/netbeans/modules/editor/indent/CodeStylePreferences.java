/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.editor.indent;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.WeakListeners;

/**
 *
 * @author vita
 */
public final class CodeStylePreferences implements PreferenceChangeListener {

    public static synchronized CodeStylePreferences get(Document doc) {
        CodeStylePreferences csp = (CodeStylePreferences) doc.getProperty(CodeStylePreferences.class);
        if (csp == null) {
            csp = new CodeStylePreferences(doc);
            doc.putProperty(CodeStylePreferences.class, csp);
        }
        return csp;
    }
    
    public Preferences getPreferences() {
        synchronized (this) {
            return useProject ? projectPrefs : globalPrefs;
        }
    }
    
    // ----------------------------------------------------------------------
    // PreferenceChangeListener implementation
    // ----------------------------------------------------------------------
    
    public void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey() == null || PROP_USED_PROFILE.equals(evt.getKey())) {
            synchronized (this) {
                useProject = PROJECT_PROFILE.equals(evt.getNewValue());
            }
        }
    }
    
    // ----------------------------------------------------------------------
    // private implementation
    // ----------------------------------------------------------------------
    
    private static final String NODE_CODE_STYLE = "CodeStyle"; //NOI18N
    private static final String PROP_USED_PROFILE = "usedProfile"; // NOI18N
    private static final String DEFAULT_PROFILE = "default"; // NOI18N
    private static final String PROJECT_PROFILE = "project"; // NOI18N
    
    private final Preferences projectRoot;
    private final Preferences projectPrefs;
    private final Preferences globalPrefs;
    private boolean useProject;
    
    private CodeStylePreferences(Document doc) {
        String mimeType = (String) doc.getProperty("mimeType"); //NOI18N
        
        this.projectRoot = findProjectPreferences(doc);
        if (projectRoot != null) {
            // determine if we are using code style preferences from the project
            String usedProfile = projectRoot.get(PROP_USED_PROFILE, DEFAULT_PROFILE);
            this.useProject = PROJECT_PROFILE.equals(usedProfile);
            this.projectPrefs = mimeType == null ? 
                projectRoot.node(PROJECT_PROFILE) : projectRoot.node(PROJECT_PROFILE).node(mimeType);
            
            // listen on changes
            projectRoot.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, this, this.projectRoot));
        } else {
            useProject = false;
            projectPrefs = null;
        }
        
        this.globalPrefs = MimeLookup.getLookup(mimeType == null ? MimePath.EMPTY : MimePath.parse(mimeType)).lookup(Preferences.class);
    }
    
    private static final Preferences findProjectPreferences(Document doc) {
        Object sdp = doc.getProperty(Document.StreamDescriptionProperty);
        if (sdp instanceof DataObject) {
            sdp = ((DataObject) sdp).getPrimaryFile();
        }
        
        Project p = sdp instanceof FileObject ? FileOwnerQuery.getOwner((FileObject) sdp) : null;
        if (p != null) {
            return ProjectUtils.getPreferences(p, IndentUtils.class, true).node(NODE_CODE_STYLE);
        } else {
            return null;
        }
    }

}
