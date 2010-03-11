/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.ruby.platform;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

public final class RubyPreferences {
    
    private static final String FIRST_TIME_KEY = "platform-manager-called-first-time"; // NOI18N
    private static final String FETCH_ALL_VERSIONS = "gem-manager-fetch-all-versions"; // NOI18N
    private static final String FETCH_GEM_DESCRIPTIONS = "gem-manager-fetch-descriptions"; // NOI18N
    private static final String GEM_VERBOSE_OUTPUT = "gem-manager-verbose-output"; // NOI18N
    private static final String INDEX_VENDOR_GEMS_ONLY = "rails-index-vendor-gems-only"; // NOI18N

    public static final String VENDOR_GEMS_PROPERTY = "vendor-gems"; //NOI18N

    private static final RubyPreferences INSTANCE = new RubyPreferences();
    private static final PropertyChangeSupport changeSupport = new PropertyChangeSupport(INSTANCE);

    private static Preferences preferences;
    
    private RubyPreferences() {
    }

    public static RubyPreferences getInstance() {
        return INSTANCE;
    }

    /** Returns {@link NbPreferences preferences} for this module. */
    public static synchronized Preferences getPreferences() {
        if (preferences == null) {
           preferences = NbPreferences.forModule(RubyPreferences.class);
        }
        return preferences;
    }

    public static void setFirstPlatformTouch(boolean b) {
        RubyPreferences.getPreferences().putBoolean(FIRST_TIME_KEY, b);
    }

    public static boolean isFirstPlatformTouch() {
        return RubyPreferences.getPreferences().getBoolean(FIRST_TIME_KEY, true);
    }

    /**
     * Retrieves stored setting whether to fetch all versions of Gems or not,
     * i.e. whether <em>-a</em> or <em>--all</em> respectively should be used
     * for operation like 'gem list'.
     */
    public static boolean shallFetchAllVersions() {
        return RubyPreferences.getPreferences().getBoolean(FETCH_ALL_VERSIONS, false);
    }

    /**
     * Stores setting whether to fetch all versions of Gems or not, i.e. whether
     * <em>-a</em> or <em>--all</em> respectively should be used for operation
     * like 'gem list'.
     */
    public static void setFetchAllVersions(boolean fetchAll) {
        RubyPreferences.getPreferences().putBoolean(FETCH_ALL_VERSIONS, fetchAll);
    }

    /**
     * Retrieves stored setting whether to fetch detailed descriptions of Gems
     * or not, i.e. whether <em>-d</em> or <em>--details</em> respectively should be
     * used for operation like 'gem list'.
     */
    public static boolean shallFetchGemDescriptions() {
        // false by default since fetching with -d often causes error -- see 
        // #22598 in the rubygems issue tracker 
        // (http://rubyforge.org/tracker/index.php?func=detail&aid=22598&group_id=126&atid=575)
        return RubyPreferences.getPreferences().getBoolean(FETCH_GEM_DESCRIPTIONS, false);
    }

    /**
     * Stores setting whether to fetch all detailed descriptions of Gems or not,
     * i.e. whether <em>-d</em> or <em>--details</em> respectively should be
     * used for operation like 'gem list'.
     */
    public static void setFetchGemDescriptions(boolean fetchDescriptions) {
        RubyPreferences.getPreferences().putBoolean(FETCH_GEM_DESCRIPTIONS, fetchDescriptions);
    }

    /**
     *@return true if the gem manager should use the verbose switch when updating/installing gems.
     */
    public static boolean isGemVerboseOutput() {
        return RubyPreferences.getPreferences().getBoolean(GEM_VERBOSE_OUTPUT, true);
    }

    /**
     * Sets whether the geme manager should use use the verbose switch when updating/installing gems.
     * @param verbose
     */
    public static void setGemVerboseOutput(boolean verbose) {
        RubyPreferences.getPreferences().putBoolean(GEM_VERBOSE_OUTPUT, verbose);
    }

    /**
     * @return whether only gems in vendor/gems should be indexed (for rails apps).
     */
    public static boolean isIndexVendorGemsOnly() {
        return RubyPreferences.getPreferences().getBoolean(INDEX_VENDOR_GEMS_ONLY, false);
    }

    /**
     * Sets whether only gems in vendor/gems should be indexed (for rails apps).
     */
    public static void setIndexVendorGemsOnly(boolean vendorGemsOnly) {
        boolean oldValue = isIndexVendorGemsOnly();
        getPreferences().putBoolean(INDEX_VENDOR_GEMS_ONLY, vendorGemsOnly);
        if (oldValue != vendorGemsOnly) {
            changeSupport.firePropertyChange(VENDOR_GEMS_PROPERTY, oldValue, vendorGemsOnly);
        }
    }

    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public static void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }
}
