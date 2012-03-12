/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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
package org.netbeans.modules.java.hints.spiimpl.options;

import java.util.Map;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.hints.spiimpl.RulesManager;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

/**
 *
 * @author Petr Hrebejk
 * @author Jan Lahoda
 */
public class HintsSettings {

//    // Only used for categories (disabled state in options dialog)
//    static final AbstractHint.HintSeverity SEVERITY_DEFAUT = AbstractHint.HintSeverity.WARNING;
//    static final boolean IN_TASK_LIST_DEFAULT = true;
//
//    public static HintsAccessor HINTS_ACCESSOR;
//
    static final String ENABLED_KEY = "enabled";         // NOI18N
    static final String OLD_SEVERITY_KEY = "severity";       // NOI18N
    static final String NEW_SEVERITY_KEY = "hintSeverity";       // NOI18N
    static final String IN_TASK_LIST_KEY = "inTaskList"; // NOI18N

    private static final String DEFAULT_PROFILE = "default"; // NOI18N

    private HintsSettings() {
    }

    public static String getCurrentProfileId() {
        return DEFAULT_PROFILE;
    }

    /** For current profile
     */
    public static boolean isEnabled( HintMetadata hint ) {
        Preferences p = getPreferences(hint.id, HintsSettings.getCurrentProfileId());
        return isEnabled(hint, p);
    }

    /** For current profile
     */
    public static boolean isShowInTaskList( HintMetadata hint ) {
        Preferences p = getPreferences(hint.id, HintsSettings.getCurrentProfileId());
        return isShowInTaskList(hint, p);
    }


    public static boolean isEnabled( HintMetadata metadata, Preferences preferences ) {
        return isEnabled(preferences, metadata.enabled);
    }

    public static boolean isEnabled(Preferences preferences, boolean enabledDefault) {
        boolean useDefault = preferences.parent()==null || DEFAULT_PROFILE.equals(preferences.parent().name());
        return preferences.getBoolean(ENABLED_KEY, useDefault && enabledDefault);
    }
//
//    public static boolean isEnabled( AbstractHint hint, Preferences preferences ) {
//        boolean useDefault = preferences.parent()==null || DEFAULT_PROFILE.equals(preferences.parent().name());
//        return preferences.getBoolean(ENABLED_KEY, useDefault && HintsSettings.HINTS_ACCESSOR.isEnabledDefault(hint));
//    }
//
    public static void setEnabled( HintMetadata metadata, boolean value ) {
	setEnabled(getPreferences(metadata.id, HintsSettings.getCurrentProfileId()), value);
	fireChangeEvent();
    }

    public static void setEnabled( Preferences p, boolean value ) {
        p.putBoolean(ENABLED_KEY, value);
    }

    public static boolean isShowInTaskList( HintMetadata hint, Preferences preferences ) {
        return preferences.getBoolean(IN_TASK_LIST_KEY, hint.showInTaskList);
    }

    public static void setShowInTaskList( Preferences p, boolean value ) {
        p.putBoolean(IN_TASK_LIST_KEY, value);
    }

    public static Severity getSeverity(@NullAllowed HintMetadata hint, @NonNull Preferences preferences ) {
        String s = preferences.get(NEW_SEVERITY_KEY, null);
        if (s != null) return Severity.valueOf(s);

        s = preferences.get(OLD_SEVERITY_KEY, null);

        if (s == null) return hint != null ? hint.severity : null;

        if ("ERROR".equals(s)) return Severity.ERROR;
        else if ("WARNING".equals(s)) return Severity.VERIFIER;
        else if ("CURRENT_LINE_WARNING".equals(s)) return Severity.HINT;
        
        return hint != null ? hint.severity : null;
    }
//
//    public static AbstractHint.HintSeverity getSeverity( AbstractHint hint, Preferences preferences ) {
//        String s = preferences.get(SEVERITY_KEY, null );
//        return s == null ? HINTS_ACCESSOR.severiryDefault(hint) : AbstractHint.HintSeverity.valueOf(s);
//    }
//
    public static void setSeverity( Preferences p, Severity severity ) {
        p.put(NEW_SEVERITY_KEY, severity.name());
    }
//
//    public static String[] getSuppressedBy(AbstractHint ah) {
//        return HINTS_ACCESSOR.getSuppressBy(ah);
//    }
//
    private static Map<String, Preferences> preferencesOverride;

    public static void setPreferencesOverride(Map<String, Preferences> preferencesOverride) {
        HintsSettings.preferencesOverride = preferencesOverride;
    }

    public static Map<String, Preferences> getPreferencesOverride() {
        return preferencesOverride;
    }

    private static final ChangeSupport cs = new ChangeSupport(HintsSettings.class);

    public static void addChangeListener(ChangeListener l) {
	cs.addChangeListener(l);
    }

    public static void removeChangeListener(ChangeListener l) {
	cs.removeChangeListener(l);
    }

    public static void fireChangeEvent() {
	cs.fireChange();
    }
    
    /** Gets preferences node, which stores the options for given hint.
     * The preferences node is created
     * by calling <code>NbPreferences.forModule(this.getClass()).node(profile).node(getId());</code>
     * @param hintId id of the hint
     * @param profile Profile to get the node for. May be null for current profile
     * @return Preferences node for given hint.
     */
    //XXX: move to HintsSettings
    public static Preferences getPreferences(String hintId, String profile) {
        Map<String, Preferences> override = getPreferencesOverride();

        if (override != null) {
            Preferences p = override.get(hintId);

            if (p != null) {
                return p;
            }
        }

        profile = profile == null ? HintsSettings.getCurrentProfileId() : profile;
        return NbPreferences.forModule(RulesManager.class).node(profile).node(hintId);
    }
}
