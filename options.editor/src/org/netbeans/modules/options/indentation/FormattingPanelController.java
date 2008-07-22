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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.options.indentation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.settings.storage.api.EditorSettings;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Dusan Balek
 */
public final class FormattingPanelController extends OptionsPanelController {

    static final String CODE_STYLE_PROFILE = "CodeStyle"; // NOI18N
    static final String DEFAULT_PROFILE = "default"; // NOI18N
    static final String PROJECT_PROFILE = "project"; // NOI18N
    static final String USED_PROFILE = "usedProfile"; // NOI18N
    private static final String FOLDER = "OptionsDialog/Editor/Formatting/"; //NOI18N

    private FormattingPanel panel;
    private Map<String, Collection<? extends OptionsPanelController>> mimeType2Controllers;
    private Preferences projectPreferences;
    private Map<String, Preferences> mimeTypePreferences;
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;
    
    public void update() {
        changed = false;
	panel.load();
    }
    
    public void applyChanges() {
	panel.store();
        try {
            if (projectPreferences != null) {
                projectPreferences.flush();
            } else {
                for (Preferences preferences : mimeTypePreferences.values())
                    preferences.flush();
            }
        } catch (BackingStoreException bse) {
        }
    }
    
    public void cancel() {
    }
    
    public boolean isValid() {
        return true; // XXXX
	// return getPanel().valid(); 
    }
    
    public boolean isChanged() {
	return changed;
    }
    
    public HelpCtx getHelpCtx() {
	return new HelpCtx("netbeans.optionsDialog.editor.formatting");
    }
    
    public synchronized JComponent getComponent(Lookup masterLookup) {
        if (panel == null) {
            mimeType2Controllers = getControllers();
            projectPreferences = getProjectPreferences(masterLookup.lookup(Project.class));
            if (projectPreferences == null)
                mimeTypePreferences = new HashMap<String, Preferences>();
            panel = new FormattingPanel(this);
        }
        return panel;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
	pcs.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
	pcs.removePropertyChangeListener(l);
    }
        
    void changed() {
	if (!changed) {
	    changed = true;
	    pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
	}
	pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
    
    Iterable<String> getMimeTypes() {
        return mimeType2Controllers.keySet();
    }
    
    Iterable<? extends OptionsPanelController> getControllers(String mimeType) {
        Iterable<? extends OptionsPanelController> ret = mimeType2Controllers.get(mimeType);
        return ret != null ? ret : Collections.<OptionsPanelController>emptySet();
    }
    
    Lookup getLookup(String mimeType, JEditorPane previewPane) {
        Preferences p = null;
        if (projectPreferences != null) {
            p = mimeType.length() > 0 ? projectPreferences.node(mimeType) : projectPreferences;
        } else {
            p = mimeTypePreferences.get(mimeType);
            if (p == null) {
                p = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
                mimeTypePreferences.put(mimeType, p);
            }
        }
        return p != null ? Lookups.fixed(p, previewPane) : Lookups.fixed(previewPane);
    }

    private Map<String, Collection<? extends OptionsPanelController>> getControllers() {
        Map<String, Collection<? extends OptionsPanelController>> ret = new LinkedHashMap<String, Collection<? extends OptionsPanelController>>();
        for (String mimeType : EditorSettings.getDefault().getAllMimeTypes()) {
            Lookup l = Lookups.forPath(FOLDER + mimeType);
            Collection<? extends OptionsPanelController> controllers = l.lookupAll(OptionsPanelController.class);
            if (!controllers.isEmpty())
                ret.put(mimeType, controllers);
        }
        return ret;
    }
    
    private Preferences getProjectPreferences(Project project) {
        if (project != null) {
            Preferences root = ProjectUtils.getPreferences(project, IndentUtils.class, true).node(CODE_STYLE_PROFILE);
            String profile = root.get(USED_PROFILE, DEFAULT_PROFILE);
            if (PROJECT_PROFILE.equals(profile))
                return root.node(PROJECT_PROFILE);
        }
        return null;
    }
}
