/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.html.editor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.openide.util.WeakListeners;

/**
 *
 * @author marekfukala
 */
public class HtmlPreferences {

    private static boolean autocompleQuotesAfterEQS;
    private static boolean autocompleQuotes;
    private static boolean completionOffersEndTagAfterLt;

    private static AtomicBoolean initialized = new AtomicBoolean(false);
    private static Preferences preferences;
    private static final PreferenceChangeListener preferencesTracker = new PreferenceChangeListener() {
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String settingName = evt == null ? null : evt.getKey();
            if (settingName == null || HtmlCompletionOptionsPanel.HTML_AUTOCOMPLETE_QUOTES_AFTER_EQS.equals(settingName)) {
                autocompleQuotesAfterEQS = preferences.getBoolean(HtmlCompletionOptionsPanel.HTML_AUTOCOMPLETE_QUOTES_AFTER_EQS, HtmlCompletionOptionsPanel.HTML_AUTOCOMPLETE_QUOTES_AFTER_EQS_DEFAULT);
            }
            if (settingName == null || HtmlCompletionOptionsPanel.HTML_AUTOCOMPLETE_QUOTES.equals(settingName)) {
                autocompleQuotes = preferences.getBoolean(HtmlCompletionOptionsPanel.HTML_AUTOCOMPLETE_QUOTES, HtmlCompletionOptionsPanel.HTML_AUTOCOMPLETE_QUOTES_DEFAULT);
            }
            if (settingName == null || HtmlCompletionOptionsPanel.HTML_COMPLETION_END_TAG_ADTER_LT.equals(settingName)) {
                completionOffersEndTagAfterLt = preferences.getBoolean(HtmlCompletionOptionsPanel.HTML_COMPLETION_END_TAG_ADTER_LT, HtmlCompletionOptionsPanel.HTML_COMPLETION_END_TAG_ADTER_LT_DEFAULT);
            }
        }
    };

    private static void lazyIntialize() {
        if(initialized.compareAndSet(false, true)) {
            preferences = MimeLookup.getLookup(HtmlKit.HTML_MIME_TYPE).lookup(Preferences.class);
            preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, preferencesTracker, preferences));
            preferencesTracker.preferenceChange(null);
        }
    }

    private HtmlPreferences() {
        //do not instantiate
    }

    /**
     * Autocomplete html quotations after equal sign typed in html tag?
     *
     * @return true if enabled
     */
    public static boolean autocompleteQuotesAfterEqualSign() {
        lazyIntialize();
        return autocompleQuotesAfterEQS;
    }

    /**
     * Autocomplete html quotations in tag attribute values
     *
     * @return true if enabled
     */
    public static boolean autocompleteQuotes() {
        lazyIntialize();
        return autocompleQuotes;
    }

    /**
     * Html code completion offers end tags after less than character
     *
     * @return true if enabled
     */
    public static boolean completionOffersEndTagAfterLt() {
        lazyIntialize();
        return completionOffersEndTagAfterLt;
    }
    
}
