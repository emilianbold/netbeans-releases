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

package org.netbeans.modules.web.client.javascript.debugger.http.ui;

import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author joelle
 */
public class HttpMonitorPreferences {


    public static final String PROP_FILTER_HTML = "Filter_HTML";
    public static final String PROP_FILTER_JS = "Filter_JS";
    public static final String PROP_FILTER_IMAGES = "Filter_Images";
    public static final String PROP_FILTER_CSS = "Filter_CSS";
    public static final String PROP_FILTER_XHR = "Filter_XHR";
    public static final String PROP_FILTER_FLASH = "Filter_FLASH";

    public static final String PROP_METHOD_COLUMN_WIDTH = "Method_Column_Width";

    
    private static boolean SHOW_HTML_DEFAULT = true;
    private static boolean SHOW_JS_DEFAULT = true;
    private static boolean SHOW_IMAGES_DEFAULT = true;
    private static boolean SHOW_CSS_DEFAULT = true;
    private static boolean SHOW_XHR_DEFAULT = true;
    private static boolean SHOW_FLASH_DEFAULT = true;

    public static int DEFAULT_METHOD_COLUMN_WIDTH = 5;


    private static HttpMonitorPreferences INSTANCE = null;

    public static HttpMonitorPreferences getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HttpMonitorPreferences();
        }

        return INSTANCE;
    }

    public static boolean isPreference(String pref){
        if ( pref == null ){
            return false;
        }
        return ( PROP_FILTER_HTML.equals(pref)  ||
                 PROP_FILTER_JS.equals(pref) ||
                 PROP_FILTER_IMAGES.equals(pref) ||
                 PROP_FILTER_CSS.equals(pref) ||
                 PROP_FILTER_XHR.equals(pref) ||
                 PROP_FILTER_FLASH.equals(pref));
    }

//    public int getMethodColumnWidth() {
//        return getPreferences().getInt(PROP_METHOD_COLUMN_WIDTH, DEFAULT_METHOD_COLUMN_WIDTH);
//    }
//
//    public void setMethodColumnWidth(int value){
//        getPreferences().putInt(PROP_METHOD_COLUMN_WIDTH, value);
//    }

    /**
     * @return the SHOW_HTML
     */
    public boolean isShowHTML() {
        return getPreferences().getBoolean(PROP_FILTER_HTML, SHOW_HTML_DEFAULT);
    }

    /**
     * @param aSHOW_HTML the SHOW_HTML to set
     */
    public void setShowHTML(boolean aSHOW_HTML) {
        getPreferences().putBoolean(PROP_FILTER_HTML, aSHOW_HTML);
    }

    /**
     * @return the SHOW_JS
     */
    public boolean isShowJS() {
        return getPreferences().getBoolean(PROP_FILTER_JS, SHOW_JS_DEFAULT);
    }

    /**
     * @param aSHOW_JS the SHOW_JS to set
     */
    public void setShowJS(boolean aSHOW_JS) {
        getPreferences().putBoolean(PROP_FILTER_JS, aSHOW_JS);
    }

    /**
     * @return the SHOW_IMAGES
     */
    public boolean isShowImages() {
        return getPreferences().getBoolean(PROP_FILTER_IMAGES, SHOW_IMAGES_DEFAULT);
    }

    /**
     * @param aSHOW_IMAGES the SHOW_IMAGES to set
     */
    public void setShowImages(boolean aSHOW_IMAGES) {
        getPreferences().putBoolean(PROP_FILTER_IMAGES, aSHOW_IMAGES);
    }

    /**
     * @return the SHOW_CSS
     */
    public boolean isShowCSS() {
        return getPreferences().getBoolean(PROP_FILTER_CSS, SHOW_CSS_DEFAULT);
    }

    /**
     * @param aSHOW_CSS the SHOW_CSS to set
     */
    public void setShowCSS(boolean aSHOW_CSS) {
        getPreferences().putBoolean(PROP_FILTER_CSS, aSHOW_CSS);
    }

    /**
     * @return the SHOW_XHR
     */
    public boolean isShowXHR() {
        return getPreferences().getBoolean(PROP_FILTER_XHR, SHOW_XHR_DEFAULT);
    }

    /**
     * @param aSHOW_XHR the SHOW_XHR to set
     */
    public void setShowXHR(boolean aSHOW_XHR) {
        getPreferences().putBoolean(PROP_FILTER_XHR, aSHOW_XHR);
    }

    /**
     * @return the SHOW_FLASH
     */
    public  boolean isShowFlash() {
        return getPreferences().getBoolean(PROP_FILTER_FLASH, SHOW_FLASH_DEFAULT);
    }

    /**
     * @param aSHOW_FLASH the SHOW_FLASH to set
     */
    public void setShowFlash(boolean aSHOW_FLASH) {
        getPreferences().putBoolean(PROP_FILTER_FLASH, aSHOW_FLASH);
    }

    public boolean isShowAll() {
        return (isShowFlash() && isShowXHR() && isShowCSS() && isShowImages() && isShowJS() && isShowHTML());
    }

    public void setShowAll(boolean b_val) {
        setShowFlash(b_val);
        setShowXHR(b_val);
        setShowCSS(b_val);
        setShowImages(b_val);
        setShowJS(b_val);
        setShowHTML(b_val);
    }



   public void addPreferenceChangeListener(PreferenceChangeListener listener) {
        getPreferences().addPreferenceChangeListener(listener);
    }

    public void removePreferenceChangeListener(PreferenceChangeListener listener) {
        getPreferences().removePreferenceChangeListener(listener);
    }


    private Preferences getPreferences() {
        return NbPreferences.forModule(HttpMonitorPreferences.class);
    }
}
