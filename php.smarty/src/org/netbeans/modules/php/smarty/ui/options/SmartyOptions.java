/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.smarty.ui.options;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.smarty.SmartyFramework;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

/**
 * @author Martin Fousek
 */
public final class SmartyOptions {
    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    protected static final String PREFERENCES_PATH = "smarty"; // NOI18N

    private static final SmartyOptions INSTANCE = new SmartyOptions();

    // default values for Smarty properties
    public static final int DEFAULT_TPL_SCANNING_DEPTH = 1;

    // preferences properties names
    private static final String OPEN_DELIMITER = "{"; // NOI18N
    private static final String CLOSE_DELIMITER = "}"; // NOI18N
    protected static final String PROP_TPL_SCANNING_DEPTH = "tpl-scanning-depth";

    // TODO - temporary property which should be removed release after NB71
    protected static final String PROP_TPL_SCANNING_DEPTH_OLD = "1";

    final ChangeSupport changeSupport = new ChangeSupport(this);

    private SmartyOptions() {
        getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                changeSupport.fireChange();
            }
        });
    }

    public static SmartyOptions getInstance() {
        return INSTANCE;
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public String getDefaultOpenDelimiter() {
        return getPreferences().get(OPEN_DELIMITER, "{"); // NOI18N
    }

    public void setDefaultOpenDelimiter(String delimiter) {
        getPreferences().put(OPEN_DELIMITER, delimiter);
        SmartyFramework.setDelimiterDefaultOpen(delimiter);
    }

    public String getDefaultCloseDelimiter() {
        return getPreferences().get(CLOSE_DELIMITER, "}"); // NOI18N
    }

    public void setDefaultCloseDelimiter(String delimiter) {
        getPreferences().put(CLOSE_DELIMITER, delimiter);
        SmartyFramework.setDelimiterDefaultClose(delimiter);
    }

    private static Preferences getPreferences() {
        return NbPreferences.forModule(SmartyOptions.class).node(PREFERENCES_PATH);
    }

    /**
     * Temporary method for updating Smarty php module preferences to use
     * {@link #PROP_TPL_SCANNING_DEPTH} property name instead of
     * {@link #PROP_TPL_SCANNING_DEPTH_OLD}.
     */
    public static void updateSmartyScanningDepthProperty() {
        Preferences preferences = getPreferences();

        int originalValue = preferences.getInt(PROP_TPL_SCANNING_DEPTH_OLD, -1);
        if (originalValue != -1) {
            preferences.remove(PROP_TPL_SCANNING_DEPTH_OLD);
            preferences.putInt(PROP_TPL_SCANNING_DEPTH, originalValue);
        }
    }

    public int getScanningDepth() {
        updateSmartyScanningDepthProperty();
        return getPreferences().getInt(PROP_TPL_SCANNING_DEPTH, DEFAULT_TPL_SCANNING_DEPTH);
    }

    public void setScanningDepth(int depth) {
        getPreferences().putInt(PROP_TPL_SCANNING_DEPTH, depth);
        SmartyFramework.setDepthOfScanningForTpl(depth);
    }
}
