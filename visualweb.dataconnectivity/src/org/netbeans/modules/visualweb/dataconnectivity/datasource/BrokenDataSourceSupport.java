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

package org.netbeans.modules.visualweb.dataconnectivity.datasource;

import java.util.Set;
import org.netbeans.api.project.Project;
import org.openide.util.NbBundle;

/**
 * Support for managing broken/missing servers.
 *
 * PLEASE NOTE! This is just a temporary solution. BrokenReferencesSupport from
 * the java project support currently does not allow to plug in a check for missing
 * servers. Once BrokenReferencesSupport will support it, this class should be
 * removed.
 */
public class BrokenDataSourceSupport {

    /** Last time in ms when the Broken References alert was shown. */
    private static long brokenAlertLastTime = 0;

    /** Is Broken References alert shown now? */
    private static boolean brokenAlertShown = false;

    /** Timeout within which request to show alert will be ignored. */
    private static int BROKEN_ALERT_TIMEOUT = 1000;

    private BrokenDataSourceSupport() {}

    /**
     * Checks whether the project has a broken/missing server problem.
     *
     * @param serverInstanceID server instance of which should be checked.
     *
     * @return true server instance of the specified id doesn't exist
     */
    public static boolean isBroken(Project project) {
        Set problemDatasources = DataSourceResolver.getInstance().getProblemDataSources(project);
        if (problemDatasources == null)
            return false;
        else
            return !problemDatasources.isEmpty();
    }

    /**
     * Shows UI customizer which gives users chance to fix encountered problems,
     * i.e. choose appropriate application server.
     *
     * @param j2eeSpec one of {@link J2eeModule#EJB}, {@link J2eeModule#EAR}
     * @param moduleType
     * @return selected application server. Might be <code>null</code>.
     */
    public static String selectServer(final Project project) {
        return NoSelectedServerWarning.selectServerDialog(
                NbBundle.getMessage(BrokenDataSourceSupport.class, "LBL_Resolve_Missing_DataSource_Title"),
                NbBundle.getMessage(BrokenDataSourceSupport.class, "ACSD_Resolve_Missing_DataSource"), project); //  NOI18N
    }


    public static boolean addDataSourceAndConnection(Project project, String item) {
        return DataSourceResolver.getInstance().addDataSource(project, item);
    }

    public static boolean dataSourceExistsCheck(Project project, String item) {
        return DataSourceResolver.getInstance().dataSourceExists(project, item);
    }
}
