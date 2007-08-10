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
/*
 * ImportDataSource.java
 *
 * Created on June 17, 2005, 2:49 PM
 * Modified on June 13, 2007
 *
 */

package org.netbeans.modules.visualweb.dataconnectivity.utils;

import java.awt.Dialog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Project;
import org.netbeans.modules.visualweb.dataconnectivity.datasource.CurrentProject;
import org.netbeans.modules.visualweb.dataconnectivity.naming.DatabaseSettingsImporter;
import org.netbeans.modules.visualweb.dataconnectivity.ui.DatasourceUISettings;
import org.netbeans.modules.visualweb.dataconnectivity.ui.MissingConnectionsAlertPanel;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;

/**
 * This is a utility class that has methods to migrate the old but critical
 * Creator 2u1 settings to NB 6.0 so that Creator projects can be opened in
 * NB 6.0 smoothly (without unresolved references).
 *
 * @author cnguyencasj, jbaker
 */

public class ImportDataSource {
    
    private static File curImportDir = null;
    private static String[] srcPaths = new String [] {".Creator/2_0/context.xml", ".Creator/2_0/jdbc-drivers", ".Creator/2_1/context.xml", ".Creator/2_1/jdbc-drivers", ".netbeans/5.5/context.xml", ".netbeans/5.5/jdbc-drivers", ".netbeans/5.5.1/context.xml", ".netbeans/5.5.1/jdbc-drivers"}; //NOI18N
    private static String[] destPaths = new String [] {"migrated/2_0/context.xml", "jdbc-drivers", "migrated/2_1/context.xml", "jdbc-drivers", "migrated/5_5/context.xml", "jdbc-drivers", "migrated/5_5_1/context.xml", "jdbc-drivers"}; //NOI18N
    private static File currentUserdir = getCurrentUserdir();
    private static final int CREATOR2 = 0;
    private static final int CREATOR2U1 = 2;
    private static final int VWP55 = 4;
    private static final int VWP551 = 6;
    private static final String config = "config"; //NOI18N
    
    /** Last time in ms when the Broken References alert was shown. */
    private static long brokenAlertLastTime = 0;
    
    /** Is Broken References alert shown now? */
    private static boolean brokenAlertShown = false;
    
    /** Timeout within which request to show alert will be ignored. */
    private static int BROKEN_ALERT_TIMEOUT = 1000;
    
    public ImportDataSource() {
        // Need to detect what version of the project and initialize curImportDir
    }
    
    private static File getCurrentUserdir() {
        if (currentUserdir == null) {
            currentUserdir =  InstalledFileLocator.getDefault().locate("config", null, false).getParentFile(); //NOI18N
        }
        
        return currentUserdir;
    }
    
    public static boolean isMigrated() {
        File migratedDir = new File(currentUserdir.getAbsolutePath() + File.separator + config + File.separator + "migrated"); //NOI18N
        if (migratedDir != null && migratedDir.exists() && migratedDir.isDirectory()) {
            return true;
        }
        return false;
    }
    
    public static boolean isMigrated(String path) {
        File file = new File(currentUserdir.getAbsolutePath() + File.separator + path);
        return (file.exists());
    }
    
    public static boolean isMigrated(int release) {
        File file = new File(currentUserdir.getAbsolutePath() + File.separator + config + File.separator + destPaths[release]);
        return (file.exists());
    }
    
    
    public static void prepareCopy() throws IOException {
        // Check if already migrated
        if (isMigrated()) {
            System.out.println("nothing to migrated"); //NOI18N
            return;
        }
        
        // User Home Dir
        String userHome = System.getProperty("user.home"); //NOI18N
        if (userHome == null) {
            // cannot locate the old user dirs if this is null
            return;
        }
        
        // Get current NB user dir
        File nbUserDir = getCurrentUserdir();
        
        // For each element in the srcPaths array, perform the copy
        // to the corresponding destPaths element
        for (int i = 0; i < srcPaths.length; i++) {
            File src = new File(userHome + File.separator + srcPaths[i]);
            File dest;
            if (destPaths[i].equals("jdbc-drivers")) { //NOI18N
                dest = new File(nbUserDir.getAbsolutePath() + File.separator + destPaths[i]);
            } else {
                dest = new File(nbUserDir.getAbsolutePath() + File.separator + config + File.separator + destPaths[i]);
            }
            System.out.println("src ["+i+"]:" + src.getAbsolutePath()); //NOI18N
            System.out.println("dest ["+i+"]:" + dest.getAbsolutePath()); //NOI18N
            
            // For performance reason, if context.xml dest file already exists, skip
            // However, this should never happen now that we put under migrated dir
            if (dest.exists() && (i == CREATOR2 || i==CREATOR2U1 || i==VWP55 || i==VWP551)) {
                i++;
                System.out.println("this should never happen now"); //NOI18N
            } else if (src.exists()) {
                
                // Check parent directory of dest if not exist, create
                File parent = dest.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                if (src.isDirectory()) {
                    copyDirectory(src,dest);
                } else if (src.isFile()){
                    boolean append = false;
                    if (dest.getName().equals("build.properties")) { //NOI18N
                        append = true;
                    }
                    copy(src,dest, append);
                }
            }
        }
    }
    
    public static void copyDirectory(File srcDir, File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }
            String[] children = srcDir.list();
            for  (int i=0; i<children.length; i++) {
                copyDirectory(new File(srcDir, children[i]), new File(dstDir, children[i]));
            }
        } else {
            copy(srcDir, dstDir, false);
        }
    }
    
    public static void copy(File src, File dst, boolean append) throws IOException {
        InputStream in = new FileInputStream(src);
        
        OutputStream out = new FileOutputStream(dst, append);
        
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
    
    
    /**
     * Determine if project is a legacy (Creator 2, Creator 2, NB 5.5, NB 5.5.1 project) and
     * @param project 
     * @return return true if project is a legacy project
     */
    public static boolean isLegacyProject(Project project) {
        boolean legacyProject = false;
        
        if (project == null) {
            project = CurrentProject.getInstance().getProject();
        }
        // if project is a visualweb or creator project then find out whether it is a Creator 2 project
        if (JsfProjectUtils.getProjectVersion(project).equals("2.0") || JsfProjectUtils.getProjectVersion(project).equals("3.0")) { //NOI18N
            legacyProject = true;
        }
        
        return legacyProject;
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
                || !DatasourceUISettings.getDefault().isShowAgainBrokenDatasourceAlert()) {
            return;
        }
        
        if (brokenAlertShown
                || brokenAlertLastTime+BROKEN_ALERT_TIMEOUT > System.currentTimeMillis()) {
            return;
        }
        
        brokenAlertShown = true;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    MissingConnectionsAlertPanel alert = new MissingConnectionsAlertPanel();
                    JButton close = new JButton(
                            NbBundle.getMessage(MissingConnectionsAlertPanel.class, "LBL_UpdateDatasourcesCustomizer_Close")); //NOI18N
                    close.getAccessibleContext().setAccessibleDescription(
                            NbBundle.getMessage(MissingConnectionsAlertPanel.class, "ACSD_UpdateDatasourcesCustomizer_Close")); //NOI18N
                    DialogDescriptor dd = new DialogDescriptor(
                            alert,
                            NbBundle.getMessage(MissingConnectionsAlertPanel.class, "MSG_Update_Datasources_Title"), //NOI18N
                            true,
                            new Object[] {close},
                            close,
                            DialogDescriptor.DEFAULT_ALIGN,
                            null,
                            null);
                    dd.setMessageType(DialogDescriptor.WARNING_MESSAGE); //NOI18N
                    Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
                    dlg.setVisible(true);
                } finally {
                    synchronized (MissingConnectionsAlertPanel.class) {
                        brokenAlertLastTime = System.currentTimeMillis();
                        brokenAlertShown = false;
                    }
                }
            }
        });
    }                 
    
    public static void registerDatabaseSettings() {
        DatabaseSettingsImporter.getInstance().locateAndRegisterConnections(false);
    }
}
