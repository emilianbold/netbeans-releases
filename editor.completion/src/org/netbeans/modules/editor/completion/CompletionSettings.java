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

package org.netbeans.modules.editor.completion;

import java.awt.Dimension;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.openide.util.WeakListeners;

/**
 * Maintenance of the editor settings related to the code completion.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class CompletionSettings implements PreferenceChangeListener {
    
    // -----------------------------------------------------------------------
    // public implementation
    // -----------------------------------------------------------------------
    
    public static synchronized CompletionSettings getInstance() {
        CompletionSettings instance = ref == null ? null : ref.get();
        if (instance == null) {
            instance = new CompletionSettings();
            ref = new SoftReference<CompletionSettings>(instance);
        }
        return instance;
    }

    public boolean completionAutoPopup() {
        initialize();
        return completionAutoPopup;
    }
    
    public int completionAutoPopupDelay() {
        initialize();
        return completionAutoPopupDelay;
    }
    
    public boolean documentationAutoPopup() {
        initialize();
        return docsAutoPopup;
    }

    /**
     * Whether documentation popup should be displayed next to completion popup
     * @return true if yes
     */
    boolean documentationPopupNextToCC() {
        initialize();
        return docsNextCC;
    }
    
    public int documentationAutoPopupDelay() {
        initialize();
        return docsAutoPopupDelay;
    }
    
    public Dimension completionPaneMaximumSize() {
        initialize();
        return completionPaneMaxSize;
    }
    
    public Dimension documentationPopupPreferredSize() {
        initialize();
        return docsPreferredSize;
    }
    
    public boolean completionInstantSubstitution() {
        initialize();
        return completionInstantSubstitution;
    }
    
    public synchronized void notifyEditorComponentChange(JTextComponent newEditorComponent) {
        if (preferences != null) {
            assert weakListener != null;
            preferences.removePreferenceChangeListener(weakListener);
            preferences = null;
            weakListener = null;
        }
        
        if (newEditorComponent != null) {
            String mimeType = DocumentUtilities.getMimeType(newEditorComponent);
            Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
            read(prefs);

            preferences = prefs;
            weakListener = WeakListeners.create(PreferenceChangeListener.class, this, preferences);
            preferences.addPreferenceChangeListener(weakListener);
        }
    }
    
    // -----------------------------------------------------------------------
    // PreferenceChangeListener implementation
    // -----------------------------------------------------------------------
    
    public void preferenceChange(PreferenceChangeEvent evt) {
        String settingName = evt != null ? evt.getKey() : null;
        if (settingName == null || MANAGED_SETTINGS.contains(settingName)) {
            synchronized (this) {
                if (preferences != null) {
                    read(preferences);
                }
            }
        }
    }
    
    // -----------------------------------------------------------------------
    // private implementation
    // -----------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(CompletionSettings.class.getName());
    
    private static final Set<String> MANAGED_SETTINGS = new HashSet<String>(Arrays.asList(new String [] {
        SimpleValueNames.COMPLETION_AUTO_POPUP,
        SimpleValueNames.COMPLETION_AUTO_POPUP_DELAY,
        SimpleValueNames.COMPLETION_PANE_MAX_SIZE,
        SimpleValueNames.COMPLETION_INSTANT_SUBSTITUTION,
        SimpleValueNames.JAVADOC_AUTO_POPUP,
        SimpleValueNames.JAVADOC_POPUP_NEXT_TO_CC,
        SimpleValueNames.JAVADOC_AUTO_POPUP_DELAY,
        SimpleValueNames.JAVADOC_PREFERRED_SIZE,
    }));

    private static Reference<CompletionSettings> ref = null;
    
    private Preferences preferences = null;
    private PreferenceChangeListener weakListener = null;
    
    private boolean completionAutoPopup;
    private int completionAutoPopupDelay;
    private Dimension completionPaneMaxSize;
    private boolean completionInstantSubstitution;
    private boolean docsAutoPopup;
    private boolean docsNextCC;
    private int docsAutoPopupDelay;
    private Dimension docsPreferredSize;
    
    private CompletionSettings() {
    }

    private synchronized void initialize() {
        if (preferences == null) {
            Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
            read(prefs);
            
            preferences = prefs;
            weakListener = WeakListeners.create(PreferenceChangeListener.class, this, preferences);
            preferences.addPreferenceChangeListener(weakListener);
        }
    }
    
    private void read(Preferences prefs) {
        completionAutoPopup = prefs.getBoolean(SimpleValueNames.COMPLETION_AUTO_POPUP, true);
        completionAutoPopupDelay = prefs.getInt(SimpleValueNames.COMPLETION_AUTO_POPUP_DELAY, 250);
        completionPaneMaxSize = parseDimension(prefs.get(SimpleValueNames.COMPLETION_PANE_MAX_SIZE, null), new Dimension(400, 300));
        completionInstantSubstitution = prefs.getBoolean(SimpleValueNames.COMPLETION_INSTANT_SUBSTITUTION, true);
        docsAutoPopup = prefs.getBoolean(SimpleValueNames.JAVADOC_AUTO_POPUP, true);
        docsNextCC = prefs.getBoolean(SimpleValueNames.JAVADOC_POPUP_NEXT_TO_CC, false);
        docsAutoPopupDelay = prefs.getInt(SimpleValueNames.JAVADOC_AUTO_POPUP_DELAY, 200);
        docsPreferredSize = parseDimension(prefs.get(SimpleValueNames.JAVADOC_PREFERRED_SIZE, null), new Dimension(500, 300));
    }
    
    private static Dimension parseDimension(String s, Dimension d) {
        int arr[] = new int[2];
        int i = 0;
        
        if (s != null) {
            StringTokenizer st = new StringTokenizer(s, ","); // NOI18N

            while (st.hasMoreElements()) {
                if (i > 1) {
                    return d;
                }
                try {
                    arr[i] = Integer.parseInt(st.nextToken());
                } catch (NumberFormatException nfe) {
                    LOG.log(Level.WARNING, null, nfe);
                    return d;
                }
                i++;
            }
        }
        
        if (i != 2) {
            return d;
        } else {
            return new Dimension(arr[0], arr[1]);
        }
    }
}
