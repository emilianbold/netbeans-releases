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

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.loaders.*;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbBundle;

import org.openidex.util.Utilities2;

import com.netbeans.developer.modules.loaders.url.*;

/** ModuleInstall class for Utilities module
*
* @author Jesse Glick, Petr Kuzel, Martin Ryzl
*/
public class Installer extends ModuleInstall {

  /** Module installed for the first time. */
  public void installed () {
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

  }
  
  public void uninstalled () {
  // -----------------------------------------------------------------------------
  // 1. uninstall Bookmarks action
    uninstallActions ();
  }

  public boolean closing () {
    return true;
  }

// -----------------------------------------------------------------------------
// Private methods
  
  private void copyURLTemplates () {
    try {
      org.openide.filesystems.FileUtil.extractJar (
        org.openide.TopManager.getDefault ().getPlaces ().folders().templates ().getPrimaryFile (),
        getClass ().getClassLoader ().getResourceAsStream ("com/netbeans/developer/modules/loaders/url/templates.jar") /* NO I18N */
      );
    } catch (java.io.IOException e) {
      org.openide.TopManager.getDefault ().notifyException (e);
    }
  }

  private void copyGroupTemplates () {
    try {
      org.openide.filesystems.FileUtil.extractJar (
        org.openide.TopManager.getDefault ().getPlaces ().folders().templates ().getPrimaryFile (),
        getClass ().getClassLoader ().getResourceAsStream ("com/netbeans/enterprise/modules/group/toinstall/templates.jar") /* NO I18N */
      );
    } catch (java.io.IOException e) {
      org.openide.TopManager.getDefault ().notifyException (e);
    }
  }

  private void copyBookmarks () {
    try {
      org.openide.filesystems.FileUtil.extractJar (
        org.openide.TopManager.getDefault ().getPlaces ().folders().bookmarks ().getPrimaryFile (),
        getClass ().getClassLoader ().getResourceAsStream ("com/netbeans/developer/modules/loaders/url/bookmarks.jar") /* NO I18N */
      );
    } catch (java.io.IOException e) {
      org.openide.TopManager.getDefault ().notifyException (e);
    }
  }

  private void installActions () {
    try {
      // install into actions pool
      Utilities2.createAction (BookmarksAction.class, DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "Help")); /* NO I18N */

      // install into menu
      Utilities2.createAction (BookmarksAction.class, 
        DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().menus (), "Help"), /* NO I18N */
        "TipOfTheDayAction", false, true, true, false /* NO I18N */
      );

    } catch (Exception e) {
      if (System.getProperty ("netbeans.debug.exceptions") != null) { /* NO I18N */
        e.printStackTrace ();
      }
      // ignore failure to install
    }
  }

  private void uninstallActions () {
    try {
      // remove from actions pool and menu
      Utilities2.removeAction (BookmarksAction.class, DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders ().actions (), "Help")); /* NO I18N */
      Utilities2.removeAction (BookmarksAction.class, DataFolder.create (org.openide.TopManager.getDefault ().getPlaces ().folders().menus (), "Help")); /* NO I18N */
    } catch (Exception e) {
      if (System.getProperty ("netbeans.debug.exceptions") != null) { /* NO I18N */
        e.printStackTrace ();
      }
      // ignore failure to uninstall
    }
  }
}

/*
 * Log
 *  3    Gandalf   1.2         1/4/00   Ian Formanek    Uses Utilities2 to 
 *       create/remove actions
 *  2    Gandalf   1.1         1/4/00   Ian Formanek    Group and URL 
 *       ModuleInstall code added
 *  1    Gandalf   1.0         1/4/00   Ian Formanek    
 * $
 */
