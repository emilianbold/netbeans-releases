/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.utilities;

import java.io.IOException;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.*;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import org.openidex.util.Utilities2;

import org.netbeans.modules.url.*;

import org.netbeans.modules.openfile.*;

import org.netbeans.modules.group.CreateGroupAction;

import org.netbeans.modules.pdf.LinkProcessor;

/** ModuleInstall class for Utilities module
*
* @author Jesse Glick, Petr Kuzel, Martin Ryzl
*/
public class Installer extends ModuleInstall {

    private final static long serialVersionUID = 1;

    private final org.netbeans.modules.search.Installer searchInstaller;

    public Installer() {
        searchInstaller = new org.netbeans.modules.search.Installer();
    }

    /** Module installed for the first time. */
    public void installed () {
        //System.err.println("utilities.Installer.installed");
        // -----------------------------------------------------------------------------
        // 1. copy Templates
        copyURLTemplates ();
        copyGroupTemplates ();

        // -----------------------------------------------------------------------------
        // 2. copy default bookmarks under system/Bookmarks
        copyBookmarks ();

        // -----------------------------------------------------------------------------
        // 3. install Bookmarks action
        installActions ();

        searchInstaller.installed();

        // Don't ask:
        RequestProcessor.postRequest (new Runnable () {
                                          public void run () {
                                              Settings.DEFAULT.isRunning ();
                                          }
                                      }, 60000);
        
        LinkProcessor.init ();

    }

    public void uninstalled () {
        // -----------------------------------------------------------------------------
        // 1. uninstall Bookmarks action
        uninstallActions ();

        // OpenFile:
        Server.shutdown ();

        searchInstaller.uninstalled();
    }

    public boolean closing () {
        // OpenFile:
        Server.shutdown ();

        return true;
    }

    public void restored () {
        //System.err.println("utilities.Installer.restored");
        searchInstaller.restored();

        // Don't ask:
        RequestProcessor.postRequest (new Runnable () {
                                          public void run () {
                                              Settings.DEFAULT.isRunning ();
                                          }
                                      }, 60000);

        LinkProcessor.init ();
                                      
    }

    // -----------------------------------------------------------------------------
    // Private methods

    private void copyURLTemplates () {
        try {
            FileUtil.extractJar (
                TopManager.getDefault ().getPlaces ().folders().templates ().getPrimaryFile (),
                NbBundle.getLocalizedFile ("org.netbeans.modules.url.templates", "jar").openStream () // NOI18N
            );
        } catch (IOException e) {
            TopManager.getDefault ().notifyException (e);
        }
    }

    private void copyGroupTemplates () {
        try {
            FileUtil.extractJar (
                TopManager.getDefault ().getPlaces ().folders().templates ().getPrimaryFile (),
                NbBundle.getLocalizedFile ("org.netbeans.modules.group.toinstall.templates", "jar").openStream () // NOI18N
            );
        } catch (IOException e) {
            TopManager.getDefault ().notifyException (e);
        }
    }

    private void copyBookmarks () {
        try {
            FileUtil.extractJar (
                TopManager.getDefault ().getPlaces ().folders().bookmarks ().getPrimaryFile (),
                NbBundle.getLocalizedFile ("org.netbeans.modules.url.bookmarks", "jar").openStream () // NOI18N
            );
        } catch (IOException e) {
            TopManager.getDefault ().notifyException (e);
        }
    }

    private void installActions () {
        try {
            // install into actions pool
            Utilities2.createAction (BookmarksAction.class, DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().actions (), "Help")); // NOI18N
            Utilities2.createAction (OpenInNewWindowAction.class, DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().actions (), "System")); // NOI18N

            // install into menu
            Utilities2.createAction (BookmarksAction.class,
                                     DataFolder.create (TopManager.getDefault ().getPlaces ().folders().menus (), "Help"), // NOI18N
                                     "JavaIDEResources", true, false, false, false // NOI18N
                                    );

            // OpenFile:
            Utilities2.createAction (OpenFileAction.class,
                                     DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().menus (), "File"), // NOI18N
                                     "OpenExplorer", true, false, false, false); // NOI18N
            Utilities2.createAction (OpenFileAction.class,
                                     DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().toolbars (), "System"), // NOI18N
                                     "SaveAction", false, true, false, false); // NOI18N
            Utilities2.createAction (OpenFileAction.class,
                                     DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().actions (), "System")); // NOI18N

            // Group:
            Utilities2.createAction (CreateGroupAction.class, DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().actions (), "Tools")); // NOI18N

        } catch (Exception e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) { // NOI18N
                e.printStackTrace ();
            }
            // ignore failure to install
        }
    }

    private void uninstallActions () {
        try {
            // remove from actions pool and menu
            Utilities2.removeAction (BookmarksAction.class, DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().actions (), "Help")); // NOI18N
            Utilities2.removeAction (OpenInNewWindowAction.class, DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().actions (), "System")); // NOI18N
            Utilities2.removeAction (BookmarksAction.class, DataFolder.create (TopManager.getDefault ().getPlaces ().folders().menus (), "Help")); // NOI18N
            // OpenFile:
            Utilities2.removeAction (OpenFileAction.class, DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().menus (), "File")); // NOI18N
            Utilities2.removeAction (OpenFileAction.class, DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().toolbars (), "System")); // NOI18N
            Utilities2.removeAction (OpenFileAction.class, DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().actions (), "System")); // NOI18N
            // Group:
            Utilities2.removeAction (CreateGroupAction.class, DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().actions (), "Tools")); // NOI18N
        } catch (Exception e) {
            if (System.getProperty ("netbeans.debug.exceptions") != null) { // NOI18N
                e.printStackTrace ();
            }
            // ignore failure to uninstall
        }
    }
}
