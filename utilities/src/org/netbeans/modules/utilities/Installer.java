/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.utilities;

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

import com.netbeans.developer.modules.loaders.url.*;

import com.netbeans.developer.modules.openfile.*;

/** ModuleInstall class for Utilities module
*
* @author Jesse Glick, Petr Kuzel, Martin Ryzl
*/
public class Installer extends ModuleInstall {

  private final static long serialVersionUID = 1;
  
  private final com.netbeans.developer.modules.search.Installer searchInstaller;
  
  public Installer() {
    searchInstaller = new com.netbeans.developer.modules.search.Installer();
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
  }

// -----------------------------------------------------------------------------
// Private methods
  
  private void copyURLTemplates () {
    try {
      FileUtil.extractJar (
        TopManager.getDefault ().getPlaces ().folders().templates ().getPrimaryFile (),
        getClass ().getClassLoader ().getResourceAsStream ("com/netbeans/developer/modules/loaders/url/templates.jar") // NOI18N
      );
    } catch (IOException e) {
      TopManager.getDefault ().notifyException (e);
    }
  }

  private void copyGroupTemplates () {
    try {
      FileUtil.extractJar (
        TopManager.getDefault ().getPlaces ().folders().templates ().getPrimaryFile (),
        getClass ().getClassLoader ().getResourceAsStream ("com/netbeans/enterprise/modules/group/toinstall/templates.jar") // NOI18N
      );
    } catch (IOException e) {
      TopManager.getDefault ().notifyException (e);
    }
  }

  private void copyBookmarks () {
    try {
      FileUtil.extractJar (
        TopManager.getDefault ().getPlaces ().folders().bookmarks ().getPrimaryFile (),
        getClass ().getClassLoader ().getResourceAsStream ("com/netbeans/developer/modules/loaders/url/bookmarks.jar") // NOI18N
      );
    } catch (IOException e) {
      TopManager.getDefault ().notifyException (e);
    }
  }

  private void installActions () {
    try {
      // install into actions pool
      Utilities2.createAction (BookmarksAction.class, DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().actions (), "Help")); // NOI18N

      // install into menu
      Utilities2.createAction (BookmarksAction.class, 
        DataFolder.create (TopManager.getDefault ().getPlaces ().folders().menus (), "Help"), // NOI18N
        "TipOfTheDayAction", false, true, true, false // NOI18N
      );
      
      // OpenFile:
      Utilities2.createAction (OpenFileAction.class,
        DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().menus (), "File"), // NOI18N
        "SaveAction", false, true, false, false); // NOI18N
      Utilities2.createAction (OpenFileAction.class,
        DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().toolbars (), "System"), // NOI18N
        "SaveAction", false, true, false, false); // NOI18N
      Utilities2.createAction (OpenFileAction.class,
        DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().actions (), "System")); // NOI18N

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
      Utilities2.removeAction (BookmarksAction.class, DataFolder.create (TopManager.getDefault ().getPlaces ().folders().menus (), "Help")); // NOI18N
      // OpenFile:
      Utilities2.removeAction (OpenFileAction.class, DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().menus (), "File")); // NOI18N
      Utilities2.removeAction (OpenFileAction.class, DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().toolbars (), "System")); // NOI18N
      Utilities2.removeAction (OpenFileAction.class, DataFolder.create (TopManager.getDefault ().getPlaces ().folders ().actions (), "System")); // NOI18N
    } catch (Exception e) {
      if (System.getProperty ("netbeans.debug.exceptions") != null) { // NOI18N
        e.printStackTrace ();
      }
      // ignore failure to uninstall
    }
  }
}

/*
 * Log
 *  9    Gandalf   1.8         1/10/00  Jesse Glick     OpenFile server now 
 *       started differently.
 *  8    Gandalf   1.7         1/5/00   Ian Formanek    NOI18N
 *  7    Gandalf   1.6         1/5/00   Jesse Glick     Should be relative to 
 *       SaveAction, since OB is now installed after OpenFile due to module 
 *       dependencies.
 *  6    Gandalf   1.5         1/4/00   Ian Formanek    
 *  5    Gandalf   1.4         1/4/00   Petr Kuzel      Search module.
 *  4    Gandalf   1.3         1/4/00   Jesse Glick     OpenFile module 
 *       installation.
 *  3    Gandalf   1.2         1/4/00   Ian Formanek    Uses Utilities2 to 
 *       create/remove actions
 *  2    Gandalf   1.1         1/4/00   Ian Formanek    Group and URL 
 *       ModuleInstall code added
 *  1    Gandalf   1.0         1/4/00   Ian Formanek    
 * $
 */
