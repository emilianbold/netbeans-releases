/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.InstanceDataObject;
import org.openide.modules.*;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.netbeans.modules.db.explorer.nodes.ConnectionNode;
import org.netbeans.modules.db.explorer.infos.ConnectionNodeInfo;

import org.openide.nodes.*;

//import org.openide.util.Mutex;

public class DatabaseModule extends ModuleInstall {
    private ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.db.resources.Bundle"); //NOI18N

    static final long serialVersionUID =5426465356344170725L;
    
    /* setting overtaken from PointBaseModule.java */
    /** name of pointbase directory name in shared installation 
     */
    private static final String POINTBASE_HOME_DIR = "pointbase"; //NOI18N
    
    /** Name of pointbase directory in user's home directory 
     */
    private static final String POINTBASE_USER_DIR = "pointbasedb"; //NOI18N

    /** Name of directory where are databases located
     */
    private static final String POINTBASE_DB_DIR = "databases"; //NOI18N
    
    /** The name of pointbase ini file
     */
    private static final String INI_FILE = "pointbase.ini"; //NOI18N

    /** File separator */
    private static final String SEPARATOR = java.io.File.separator;
    
    private static final String user = System.getProperty("netbeans.user"); //NOI18N
    private static final String home = System.getProperty("netbeans.home"); //NOI18N
        
    public void installed() {

        // test if the ppointbase module is installed
        // if not then ini file is set in the default place
        if(System.getProperty(INI_FILE)==null)
            System.setProperty(INI_FILE, home + java.io.File.separator + POINTBASE_HOME_DIR + java.io.File.separator + INI_FILE); //NOI18N

        TopManager tm = TopManager.getDefault();

        try {
            FileSystem rfs = tm.getRepository().getDefaultFileSystem();
            FileObject rootFolder = rfs.getRoot();
            FileObject databaseFileObject = rootFolder.getFileObject("Database"); //NOI18N
            if (databaseFileObject == null) {
                databaseFileObject = rootFolder.createFolder("Database"); //NOI18N
                FileObject adaptorsFileObject = databaseFileObject.createFolder("Adaptors"); //NOI18N
                InstanceDataObject.create(DataFolder.findFolder(adaptorsFileObject), "DefaultAdaptor", org.netbeans.lib.ddl.adaptors.DefaultAdaptor.class); //NOI18N
            }
        } catch (LinkageError ex) {
            String msg = MessageFormat.format(bundle.getString("FMT_CLASSNOTFOUND"), new String[] {ex.getMessage()}); //NOI18N
            if (tm != null)
                tm.notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
        } catch (Exception ex) {
            String msg = MessageFormat.format(bundle.getString("FMT_EXCEPTIONINSTALL"), new String[] {ex.getMessage()}); //NOI18N
            if (tm != null)
                tm.notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE));
        }
    }
    
    public void uninstalled() {
        // test if the ppointbase module is installed
        // if not then ini file is set in the default place
        if(System.getProperty(INI_FILE)==null)
            System.setProperty(INI_FILE, home + java.io.File.separator + POINTBASE_HOME_DIR + java.io.File.separator + INI_FILE); //NOI18N

        // closing all open connection
//        Node n[] = TopManager.getDefault().getPlaces().nodes().environment().getChildren().findChild(bundle.getString("Databases")).getChildren().getNodes(); //NOI18N
        Children.MUTEX.writeAccess (new Runnable () {
            public void run () {
                try {
                    Node n[] = TopManager.getDefault().getPlaces().nodes().environment().getChildren().findChild("Databases").getChildren().getNodes(); //NOI18N
                    for (int i = 0; i < n.length; i++)
                        if (n[i] instanceof ConnectionNode)
                            ((ConnectionNodeInfo)((ConnectionNode)n[i]).getInfo()).disconnect();
                } catch (Exception exc) {
                    //connection not closed
                }
            }
        });
    }
    
    public void restored() {
        // test if the ppointbase module is installed
        // if not then ini file is set in the default place
        if(System.getProperty(INI_FILE)==null)
            System.setProperty(INI_FILE, home + java.io.File.separator + POINTBASE_HOME_DIR + java.io.File.separator + INI_FILE); //NOI18N

    }

    public boolean closing () {
        // method is called because of closing connections
        uninstalled();
        return true;
    }

}
