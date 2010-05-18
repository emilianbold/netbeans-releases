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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.visualweb.dataconnectivity;

import java.util.prefs.Preferences;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/** Dataconnectivity settings.
 *
 * @author joel brown
 */
public class DataconnectivitySettings {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 1L;

    /** create on page by default */
    public static final String PROP_MAKE_IN_SESSION = "1makeInSession"; // NOI18N
    public static final String PROP_CHECK_ROWSET = "2checkRowset"; // NOI18N
    public static final String PROP_PROMPT_FOR_NAME = "3checkRowset"; // NOI18N
    public static final String PROP_DATAPROVIDER = "4dataprovider"; // NOI18N
    public static final String PROP_ROWSET = "5rowset"; // NOI18N
    private static final String DEFAULT_ROWSET_SUFFIX = "RowSet"; // NOI18N
    private static final String DEFAULT_DATAPROVIDER_SUFFIX = "DataProvider";  // NOI18N

    // Default instance of this system option, for the convenience of associated classes.
    private static final DataconnectivitySettings INSTANCE = new DataconnectivitySettings();
    public static DataconnectivitySettings getInstance() {
        return INSTANCE;
    }

    public void setDataProviderSuffixProp(String dpSuffix) {        
        getPreferences().put(PROP_DATAPROVIDER, dpSuffix);
    }
    public String getDataProviderSuffixProp() {
        return getPreferences().get(PROP_DATAPROVIDER, DEFAULT_DATAPROVIDER_SUFFIX); 
    }

    public void setRowSetSuffixProp(String rowsetSuffix) {        
        getPreferences().put(PROP_ROWSET, rowsetSuffix);
    }
    public String getRowSetSuffixProp() {
        return getPreferences().get(PROP_ROWSET, DEFAULT_ROWSET_SUFFIX); 
    }

    public static String getRsSuffix() {
         return DataconnectivitySettings.getInstance().getRowSetSuffixProp() ;
    }
    public static String getDpSuffix() {
         return DataconnectivitySettings.getInstance().getDataProviderSuffixProp() ;
    }

    // This method must be overriden. It returns display name of this options.
    public String displayName() {
        return NbBundle.getBundle(DataconnectivitySettings.class).getString("CTL_DataconnectivitySettings");
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /****
     * whether of not all drags should create rowsets on the page.
     * false is the default - then rowsets are created in the session.
     */
    public boolean getMakeInSession() {
        // why the "!"(not)?  the option description wording changed from
        // "create in page" to "create in session".
        return getPreferences().getBoolean(PROP_MAKE_IN_SESSION, true);
    }

    public void setMakeInSession(boolean makeInSession) {
        getPreferences().putBoolean(PROP_MAKE_IN_SESSION, makeInSession ? Boolean.TRUE : Boolean.FALSE);
    }

    public boolean getCheckRowSetProp() {
        return getPreferences().getBoolean(PROP_CHECK_ROWSET, true);
    }

    public void setCheckRowSetProp(boolean checkStuff) {
        getPreferences().putBoolean(PROP_CHECK_ROWSET, checkStuff ? Boolean.TRUE : Boolean.FALSE);
    }

    public boolean getPromptForName() {
        return getPreferences().getBoolean(PROP_PROMPT_FOR_NAME, false);
    }

    public void setPromptForName(boolean prompt) {
        getPreferences().putBoolean(PROP_PROMPT_FOR_NAME, prompt ? Boolean.TRUE : Boolean.FALSE);
    }

    public static boolean canDropAnywhere() {
        return ! DataconnectivitySettings.getInstance().getMakeInSession() ;
    }

    public static boolean checkRowsets() {
        return DataconnectivitySettings.getInstance().getCheckRowSetProp() ;
    }

    public static boolean promptForName() {
        return DataconnectivitySettings.getInstance().getPromptForName() ;
    }
    
    private Preferences getPreferences() {
        return NbPreferences.forModule(DataconnectivitySettings.class);
    }
}
