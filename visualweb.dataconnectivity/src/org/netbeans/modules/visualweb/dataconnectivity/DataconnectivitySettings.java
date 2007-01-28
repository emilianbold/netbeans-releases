/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
    public static final String PROP_DATAPROVIDER = "3dataprovider"; // NOI18N
    public static final String PROP_ROWSET = "4rowset"; // NOI18N

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


    public static boolean canDropAnywhere() {
        return ! DataconnectivitySettings.getInstance().getMakeInSession() ;
    }

    public static boolean checkRowsets() {
        return DataconnectivitySettings.getInstance().getCheckRowSetProp() ;
    }


}
