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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.common.ui;

import java.awt.Dialog;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Support for managing broken/missing Databases.
 *
 * PLEASE NOTE! This is just a temporary solution. BrokenReferencesSupport from
 * the java project support currently does not allow to plug in a check for missing
 * Databases. Once BrokenReferencesSupport will support it, this class should be
 * removed.
 */
public class BrokenDatasourceSupport {
    
    /** Last time in ms when the Broken References alert was shown. */
    private static long brokenAlertLastTime = 0;
    
    /** Is Broken References alert shown now? */
    private static boolean brokenAlertShown = false;
    
    /** Timeout within which request to show alert will be ignored. */
    private static int BROKEN_ALERT_TIMEOUT = 1000;
    
    private BrokenDatasourceSupport() {}
    
    /**
     * Shows UI customizer which gives users chance to fix encountered problems,
     * i.e. choose appropriate data source.
     *
     * @param project
     *
     */
    public static void fixDatasources(final Project project) {
        MissingDatabaseConnectionWarning.selectDatasources(
                NbBundle.getMessage(BrokenDatasourceSupport.class,"LBL_Resolve_Missing_Datasources_Title"),org.openide.util.NbBundle.getMessage(BrokenDatasourceSupport.class, "ACSD_Resolve_Missing_Datasources"), project); //  NOI18N
    }
    
    /**
     * Show alert message box informing user that a project has missing
     * database connections. This method can be safely called from any thread, e.g. during
     * the project opening, and it will take care about showing message box only
     * once for several subsequent calls during a timeout.
     * The alert box has also "show this warning again" check box.
     */
    public static synchronized void showAlert() {
        // Do not show alert if it is already shown or if it was shown
        // in last BROKEN_ALERT_TIMEOUT milliseconds or if user do not wish it.
        if (brokenAlertShown
                || brokenAlertLastTime+BROKEN_ALERT_TIMEOUT > System.currentTimeMillis()
                || !J2EEUISettings.getDefault().isShowAgainBrokenDatasourceAlert()) {
            return;
        }
        
        brokenAlertShown = true;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    BrokenDatasourceAlertPanel alert = new BrokenDatasourceAlertPanel();
                    JButton close = new JButton(
                            NbBundle.getMessage(BrokenDatasourceSupport.class, "LBL_BrokenDatasourcesCustomizer_Close"));
                    close.getAccessibleContext().setAccessibleDescription(
                            NbBundle.getMessage(BrokenDatasourceSupport.class, "ACSD_BrokenDatasourcesCustomizer_Close"));
                    DialogDescriptor dd = new DialogDescriptor(
                            alert,
                            NbBundle.getMessage(BrokenDatasourceAlertPanel.class, "MSG_Broken_Datasources_Title"),
                            true,
                            new Object[] {close},
                            close,
                            DialogDescriptor.DEFAULT_ALIGN,
                            null,
                            null);
                    dd.setMessageType(DialogDescriptor.WARNING_MESSAGE);
                    Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
                    dlg.setVisible(true);
                } finally {
                    synchronized (BrokenDatasourceSupport.class) {
                        brokenAlertLastTime = System.currentTimeMillis();
                        brokenAlertShown = false;
                    }
                }
            }
        });
    }
    
    /**
     * Returns set of broken datasources
     *
     * @param project
     * @return Set<Datasource>  returns a set of data sources that don't have corresponding database connections
     */
    public static Set<Datasource> getBrokenDatasources(Project project) {
        J2eeModuleProvider jmp =
                (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
        
        Set<Datasource> dss = null;
        try {
            dss = jmp.getModuleDatasources();
        } catch (ConfigurationException e) {
            dss = new HashSet<Datasource>();
        }
        
        Set<Datasource> brokenDatasources = new HashSet<Datasource>();
        Iterator it = dss.iterator();
        while (it.hasNext()) {
            Datasource ds = (Datasource)it.next();
            if(!isFound(ds)){
                brokenDatasources.add(ds);
            }
        }
        
        return brokenDatasources;
    }
    
    private static boolean isFound(Datasource ds) {
        boolean found = false;
        String url = ds.getUrl();
        String username = ds.getUsername();
        DatabaseConnection[] dbConns = ConnectionManager.getDefault().getConnections();
        for(int i=0; i<dbConns.length; i++ ){
            DatabaseConnection dbCon = dbConns[i];
            String url1 = dbCon.getDatabaseURL();
            String username1 = dbCon.getUser();
            if (matchURL(url, url1, true) && Utilities.compareObjects(username, username1)) {
                found = true;
            }
        }
        return found;
    }
    
    private static boolean matchURL(String jdbcResourceUrl, String dsInfoUrl, boolean ignoreCase) {
        if (ignoreCase){
            jdbcResourceUrl = jdbcResourceUrl.toLowerCase();
            dsInfoUrl = dsInfoUrl.toLowerCase();
        }
        if (jdbcResourceUrl.equals(dsInfoUrl)){
            return true;
        }
        
        if (jdbcResourceUrl.contains("derby")) {
            String newJdbcResourceUrl = jdbcResourceUrl.substring(0, jdbcResourceUrl.lastIndexOf(":")) + jdbcResourceUrl.substring(jdbcResourceUrl.lastIndexOf("/"));
            if (newJdbcResourceUrl.equals(dsInfoUrl)){
                return true;
            }
        }
        
        int nextIndex = 0;
        if ((jdbcResourceUrl != null) && (dsInfoUrl != null)){
            char[] jdbcResourceUrlChars = jdbcResourceUrl.toCharArray();
            char[] dsInfoUrlChars = dsInfoUrl.toCharArray();
            for(int i = 0; i < jdbcResourceUrlChars.length - 1; i++){
                if ((jdbcResourceUrlChars[i] != dsInfoUrlChars[i]) && jdbcResourceUrlChars[i] == ':'){
                    nextIndex = 1;
                } else if (jdbcResourceUrlChars[i + nextIndex] != dsInfoUrlChars[i]){
                    return false;
                }
            }
        }
        return true;
    }
}
