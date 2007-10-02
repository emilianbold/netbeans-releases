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

import org.openide.options.SystemOption;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Dataconnectivity settings.
 *
 * @author joel brown
 */
public class DataconnectivitySettings extends SystemOption {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 1L;

    /** create on page by default */
    public static final String PROP_MAKE_IN_SESSION = "1makeInSession"; // NOI18N
    public static final String PROP_CHECK_ROWSET = "2checkRowset"; // NOI18N
    public static final String PROP_PROMPT_FOR_NAME = "3checkRowset"; // NOI18N
    public static final String PROP_DATAPROVIDER = "4dataprovider"; // NOI18N
    public static final String PROP_ROWSET = "5rowset"; // NOI18N

    // Default instance of this system option, for the convenience of associated classes.
    public static DataconnectivitySettings getInstance() {
        return (DataconnectivitySettings)findObject(DataconnectivitySettings.class, true);
    }

    // do NOT use constructore for setting default values
    protected void initialize() {
        // Set default values of properties
        super.initialize();

        setMakeInSession( true ) ;  // EA default
        setCheckRowSetProp(true) ; // this checking is new in thresher FCS.
	setPromptForName(false) ;
        setRowSetSuffixProp("RowSet") ; //NOI18N
        setDataProviderSuffixProp("DataProvider") ; // NOI18N
    }

    public void setDataProviderSuffixProp(String dpSuffix) {
        putProperty(PROP_DATAPROVIDER, dpSuffix, true);
    }
    public String getDataProviderSuffixProp() {
        return (String)getProperty(PROP_DATAPROVIDER) ;
    }

    public void setRowSetSuffixProp(String rowsetSuffix) {
        putProperty(PROP_ROWSET, rowsetSuffix, true);
    }
    public String getRowSetSuffixProp() {
        return (String)getProperty(PROP_ROWSET) ;
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
        return ((Boolean)getProperty(PROP_MAKE_IN_SESSION)).booleanValue();
    }

    public void setMakeInSession(boolean makeInSession) {
        putProperty(PROP_MAKE_IN_SESSION, makeInSession ? Boolean.TRUE : Boolean.FALSE, true);
    }

    public boolean getCheckRowSetProp() {
        return ((Boolean)getProperty(PROP_CHECK_ROWSET)).booleanValue();
    }

    public void setCheckRowSetProp(boolean checkStuff) {
        putProperty(PROP_CHECK_ROWSET, checkStuff ? Boolean.TRUE : Boolean.FALSE, true);
    }

    public boolean getPromptForName() {
        return ((Boolean)getProperty(PROP_PROMPT_FOR_NAME)).booleanValue();
    }

    public void setPromptForName(boolean prompt) {
        putProperty(PROP_PROMPT_FOR_NAME, prompt ? Boolean.TRUE : Boolean.FALSE, true);
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

}
