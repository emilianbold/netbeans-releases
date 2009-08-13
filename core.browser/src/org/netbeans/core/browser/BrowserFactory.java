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
package org.netbeans.core.browser;

import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.core.IDESettings;
import org.openide.awt.HtmlBrowser;
import org.openide.awt.HtmlBrowser.Impl;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;

/**
 * Creates internal browser which uses embedded native browser.
 *
 * @author S. Aubrecht
 */
public class BrowserFactory implements HtmlBrowser.Factory {
    
    static String PROP_EXTRA_BROWSER = "ExtraBrowser"; //NOI18N

    public Impl createHtmlBrowserImpl() {
        HtmlBrowser.Factory extraFactory = _getExtraBrowser();
        if( null != extraFactory ) {
            return extraFactory.createHtmlBrowserImpl();
        }
        return new HtmlBrowserImpl();
    }

    public static Boolean isHidden () {
        if( !BrowserManager.isSupportedPlatform() )
            return Boolean.TRUE;
        return Boolean.FALSE;
    }

    public HtmlBrowser.Factory getExtraBrowser() {
        return _getExtraBrowser();
    }

    public void setExtraBrowser( HtmlBrowser.Factory browser ) {
        _setExtraBrowser(browser);
    }

    static HtmlBrowser.Factory _getExtraBrowser() {
        String id = NbPreferences.forModule(BrowserFactory.class).get("extraBrowser", null); //NOI18N
        if( null == id || "".equals(id) ) {
            return null;
        }
        Lookup.Item<HtmlBrowser.Factory> item = Lookup.getDefault ().lookupItem (new Lookup.Template<HtmlBrowser.Factory> (HtmlBrowser.Factory.class, id, null));
        return item == null ? null : item.getInstance ();
    }

    static void _setExtraBrowser(HtmlBrowser.Factory browser) {
        String browserId = null;
        if( null != browser ) {
            Lookup.Item<HtmlBrowser.Factory> item = Lookup.getDefault ().lookupItem (new Lookup.Template<HtmlBrowser.Factory> (HtmlBrowser.Factory.class, null, browser));
            if (item != null) {
                browserId = item.getId ();
            } else {
                // strange
                Logger.getLogger (IDESettings.class.getName ()).warning ("Cannot find browser in lookup " + browser);// NOI18N
            }
        }
        if( null == browserId )
            browserId = ""; //NOI18N
        NbPreferences.forModule(BrowserFactory.class).put("extraBrowser", browserId); //NOI18N

        //force reset of URLDisplayer implementation in NbTopManager
        Preferences idePrefs = NbPreferences.forModule(IDESettings.class);
        String wwwBrowser = idePrefs.get(IDESettings.PROP_WWWBROWSER, null);
        idePrefs.put(IDESettings.PROP_WWWBROWSER, "");
        idePrefs.put(IDESettings.PROP_WWWBROWSER, wwwBrowser);
    }
}
