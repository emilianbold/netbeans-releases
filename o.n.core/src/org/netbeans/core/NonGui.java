/*
* Main.java -- synopsis.
*
*
* $Author$
* $Date$
* $Revision$
*
* This file is part of $Project: Corona$.
*
* Copyright � 1997-1999 Sun Microsystems, Inc. All rights reserved.
* Use is subject to license terms.
*/
package org.netbeans.core;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.border.*;

import org.openide.util.datatransfer.ExClipboard;
import org.openide.*;
import org.openide.cookies.ExecCookie;
import org.openide.loaders.*;
import org.openide.actions.*;
import org.openide.actions.PropertiesAction;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem;
import org.openide.options.ControlPanel;
import org.openide.windows.*;
import org.openide.explorer.*;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.UserCancelException;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.actions.SystemAction;
import org.openide.util.io.*;
import org.openide.nodes.*;
import org.openide.util.Utilities;
import org.netbeans.core.actions.*;
import org.netbeans.core.windows.WindowManagerImpl;


/** This class is a TopManager for Corona environment.
*
* @author Ales Novak, Jaroslav Tulach, Ian Formanek, Petr Hamernik, Jan Jancura
*/
public class NonGui extends NbTopManager implements Runnable {

  /** directory for modules */
  static final String DIR_MODULES = "modules"; // NOI18N

  /* The class of the UIManager to be used for netbeans - can be set by command-line argument -ui <class name> */
  protected static Class uiClass;

  /* The size of the fonts in the UI - 11pt by default, can be set by command-line argument -fontsize <size> */
  protected static int uiFontSize = 11;

  /** The netbeans home dir - acquired from property netbeans.home */
  private static String homeDir;
  /** The netbeans user dir - acquired from property netbeans.user / or copied from homeDir if such property does not exist */
  private static String userDir;
  /** The netbeans system dir - it is netbeans.home/system or a folder specified via -system option */
  private static String systemDir;
  
  /** system file system */
  private static FileSystem systemFileSystem;

  /** The flag whether to create the log - can be set via -nologging
  * command line option */
  private static boolean noLogging = false;

  /** Flag telling if the system clipboard should be used or not.
  * This helps avoid crashes and hangs on Unixes.
  */
  private static boolean noSysClipboard = false;

  /** The flag whether to show the Splash screen on the startup */
  protected  static boolean noSplash = false;

  /** The Class that logs the IDE events to a log file */
  private static TopLogging logger;


  /** Getter for home directory.
  */
  protected static String getHomeDir () {
      if (homeDir == null) {
          homeDir = System.getProperty ("netbeans.home");
          if (homeDir == null) {
            System.out.println(getString("CTL_Netbeanshome_property"));
            doExit (1);
          }
      }
      return homeDir;
  }

  /** Getter for user home directory.
  */
  protected static String getUserDir () {
      if (userDir == null) {
          userDir = System.getProperty ("netbeans.user");
          if (userDir == null) {
              userDir = getHomeDir ();
              System.getProperties ().put ("netbeans.user", homeDir); // NOI18N
          }
      }
      return userDir;
  }

  /** System directory getter.
  */
  protected static String getSystemDir () {
      if (systemDir == null) {
          systemDir = getUserDir () + File.separator + "system"; // NOI18N
      }
      return systemDir;
  }
  
  //
  // Protected methods that are provided for subclasses (Main) 
  // to plug-in better implementation
  //
  protected FileSystem createDefaultFileSystem () {
    if (systemFileSystem != null) {
      return systemFileSystem;
    }

// -----------------------------------------------------------------------------------------------------
// 1. Initialization and checking of netbeans.home and netbeans.user directories

    File homeDirFile = new File (getHomeDir ());
    File userDirFile = new File (getUserDir ());
    if (!homeDirFile.exists ()) {
      System.out.println (getString("CTL_Netbeanshome_notexists"));
      doExit (2);
    }
    if (!homeDirFile.isDirectory ()) {
      System.out.println (getString("CTL_Netbeanshome1"));
      doExit (3);
    }
    if (!userDirFile.exists ()) {
      System.out.println (getString("CTL_Netbeanshome2"));
      doExit (4);
    }
    if (!userDirFile.isDirectory ()) {
      System.out.println (getString("CTL_Netbeanshome3"));
      doExit (5);
    }

    File systemDirFile = new File (getSystemDir ());
    if (systemDirFile.exists ()) {
      if (!systemDirFile.isDirectory ()) {
        Object[] arg = new Object[] {userDir};
        System.out.println (new MessageFormat(getString("CTL_CannotCreate_text")).format(arg));
        doExit (6);
      }
    } else {
      // try to create it
      if (!systemDirFile.mkdir ()) {
        Object[] arg = new Object[] {userDir};
        System.out.println (new MessageFormat(getString("CTL_CannotCreateSysDir_text")).format(arg));
        doExit (7);
      }
    }

// -----------------------------------------------------------------------------------------------------
// 7. Initialize FileSystems

    setStatusText (getString("MSG_FSInit"));
    // system FS !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    {
      Exception exc = null;
      try {
        systemFileSystem = SystemFileSystem.create (userDir, homeDir);
      } catch (IOException ex) {
        exc = ex;
      } catch (java.beans.PropertyVetoException ex) {
        exc = ex;
      }

      if (exc != null) {
        Object[] arg = new Object[] {systemDir};
        System.out.println (new MessageFormat(getString("CTL_Cannot_mount_systemfs")).format(arg));
        doExit (3);
      }
    }

    return systemFileSystem;
  }

  /** Everything is interactive */
  public boolean isInteractive (int il) {
    return true;
  }

  
  protected static void showSystemInfo() {
    System.out.println("-- " + getString("CTL_System_info") + " ----------------------------------------------------------------");
    TopLogging.printSystemInfo(System.out);
    System.out.println("-------------------------------------------------------------------------------"); // NOI18N
  }

  protected static void showHelp() {
    System.out.println(getString("CTL_Cmd_options"));
    System.out.println(getString("CTL_System_option"));
    System.out.println("                      " + getString("CTL_System_option2"));
    System.out.println(getString("CTL_UI_option"));
    System.out.println(getString("CTL_FontSize_option"));
    System.out.println(getString("CTL_Locale_option"));
    System.out.println(getString("CTL_Noinfo_option"));
    System.out.println(getString("CTL_Nologging_option"));
    System.out.println(getString("CTL_Nosysclipboard_option"));
  }

  public static void parseCommandLine(String[] args) {
    boolean noinfo = false;

    // let's go through the command line
    for(int i = 0; i < args.length; i++)
    {
      if (args[i].equalsIgnoreCase("-nogui")) { // NOI18N
        System.getProperties().put (
          "org.openide.TopManager", // NOI18N
          "org.netbeans.core.NonGui" // NOI18N
        );
      } else if (args[i].equalsIgnoreCase("-nosplash")) // NOI18N
        noSplash = true;
      else if (args[i].equalsIgnoreCase("-noinfo")) // NOI18N
        noinfo = true;
      else if (args[i].equalsIgnoreCase("-nologging")) // NOI18N
        noLogging = true;
      else if (args[i].equalsIgnoreCase("-nosysclipboard")) // NOI18N
        noSysClipboard = true;
      else if (args[i].equalsIgnoreCase("-system")) { // NOI18N
        systemDir = args[++i];
      }
      else if (args[i].equalsIgnoreCase("-ui")) { // NOI18N
        try {
          uiClass = Class.forName(args[++i]);
        } catch(ArrayIndexOutOfBoundsException e) {
          System.out.println(getString("ERR_UIExpected"));
        } catch (ClassNotFoundException e2) {
          System.out.println(getString("ERR_UINotFound"));
        }
      } else if (args[i].equalsIgnoreCase("-fontsize")) { // NOI18N
        try {
          uiFontSize = Integer.parseInt (args[++i]);
        } catch(ArrayIndexOutOfBoundsException e) {
          System.out.println(getString("ERR_FontSizeExpected"));
        } catch (NumberFormatException e2) {
          System.out.println(getString("ERR_BadFontSize"));
        }

      } else if (args[i].equalsIgnoreCase("-locale")) { // NOI18N
        String localeParam = args[++i];
        String language;
        String country = ""; // NOI18N
        String variant = ""; // NOI18N
        int index1 = localeParam.indexOf(":"); // NOI18N
        if (index1 == -1)
          language = localeParam;
        else {
          language = localeParam.substring(0, index1);
          int index2 = localeParam.indexOf(":", index1+1); // NOI18N
          if (index2 != -1) {
            country = localeParam.substring(index1+1, index2);
            variant = localeParam.substring(index2+1);
          }
          else
            country = localeParam.substring(index1+1);
        }
        java.util.Locale.setDefault(new java.util.Locale(language, country, variant));
      }
      else if (args[i].equalsIgnoreCase("-?") || args[i].equalsIgnoreCase("-help")) { // NOI18N
        showHelp();
        doExit(0);
      }
      else {
        System.out.println(getString("ERR_UnknownOption")+": "+args[i]);
        showHelp();
        doExit(0);
      }
    }

    // ----------------------------
    // show System info
    if (!noinfo)
      showSystemInfo();

  }

  /** Initialization of the manager.
  */
  public void run () {
    System.getProperties ().put ("netbeans.design.time", "true"); // NOI18N

// Initialize beans - [PENDING - better place for this ?]
//                    [PENDING - can PropertyEditorManager garbage collect ?]
    java.beans.Introspector.setBeanInfoSearchPath (new String[] {
         "org.netbeans.beaninfo", // NOI18N
         "org.netbeans.beaninfo.awt", // NOI18N
         "org.netbeans.beaninfo.swing", // NOI18N
         "javax.swing.beaninfo", // NOI18N
         "sun.beans.infos" // NOI18N
       });
    java.beans.PropertyEditorManager.setEditorSearchPath (
        new String[] { "org.openide.explorer.propertysheet.editors", "org.netbeans.beaninfo.editors", "sun.beans.editors" }); // NOI18N
    java.beans.PropertyEditorManager.registerEditor (String[].class, org.openide.explorer.propertysheet.editors.StringArrayEditor.class);
    java.beans.PropertyEditorManager.registerEditor (org.openide.src.MethodParameter[].class, org.openide.explorer.propertysheet.editors.MethodParameterArrayEditor.class);
    java.beans.PropertyEditorManager.registerEditor (org.openide.src.Identifier[].class, org.openide.explorer.propertysheet.editors.IdentifierArrayEditor.class);

// -----------------------------------------------------------------------------------------------------
// 2. Parse command-line arguments

    // Set up module-versioning properties, which logger prints.
    // IMPORTANT: must use an unlocalized resource here.
    java.util.Properties versions = new java.util.Properties ();
    try {
      versions.load (Main.class.getClassLoader ().getResourceAsStream ("org/netbeans/core/Versioning.properties")); // NOI18N
    } catch (java.io.IOException ioe) {
      ioe.printStackTrace ();
    }
    System.setProperty ("org.openide.specification.version", versions.getProperty ("VERS_Specification_Version")); // NOI18N
    System.setProperty ("org.openide.version", versions.getProperty ("VERS_Implementation_Version")); // NOI18N
    System.setProperty ("org.openide.major.version", versions.getProperty ("VERS_Name")); // NOI18N
    // For TopLogging and MainWindow only:
    System.setProperty ("netbeans.buildnumber", versions.getProperty ("VERS_Build_Number")); // NOI18N


// -----------------------------------------------------------------------------------------------------
// 5. Start logging

    // do logging
    if (!noLogging) {
      try {
        logger = new TopLogging(systemDir);
      } catch (IOException e) {
//       System.err.println("Cannot create log file.  Logging disabled."); // [PENDING]
//        e.printStackTrace();
      }
    }

// -----------------------------------------------------------------------------------------------------
// 6. Initialize SecurityManager and ClassLoader

    setStatusText (getString("MSG_IDEInit"));


// -----------------------------------------------------------------------------------------------------
// 7. Initialize FileSystems
    getRepository ();

// -----------------------------------------------------------------------------------------------------
// 8. Advance Policy

    java.security.Policy.getPolicy().getPermissions(new java.security.CodeSource(null, null)).implies(new java.security.AllPermission());
    
    // set security manager
    System.setSecurityManager(new org.netbeans.core.execution.TopSecurityManager());

    // install java.net.Authenticator
    java.net.Authenticator.setDefault (new NbAuthenticator ());

// -----------------------------------------------------------------------------------------------------
// 9. Modules

    {
      File centralModuleDirectory = new File (
        homeDir + File.separator + DIR_MODULES
      );
      File userModuleDirectory = new File (
        userDir == null ? homeDir : userDir + File.separator + DIR_MODULES
      );
    
      // versions set in step 2 for logger
      ModuleInstaller.initialize (
        centralModuleDirectory,
        userModuleDirectory
      );
    }

    
// -----------------------------------------------------------------------------------------------------
// 10. Initialization of project (because it can change loader pool and it influences main window menu)

    try {
      NbProjectOperation.openOrCreateProject ();
    } catch (IOException e) {
      getErrorManager ().notify (ErrorManager.INFORMATIONAL, e);
    }

    LoaderPoolNode.installationFinished ();


    startFolder (getDefault ().getPlaces ().folders ().startup ());


    initializeMainWindow ();


// -----------------------------------------------------------------------------------------------------
// 15. Install new modules 

    ModuleInstaller.autoLoadModules ();


  }


  /** Method to initialize the main window.
  */
  protected void initializeMainWindow () {
  }

  /** Getter for a text from resource.
  * @param resName resource name
  * @return string with resource
  */
  static String getString (String resName) {
    return NbBundle.getBundle (Main.class).getString (resName);
  }

  /** Getter for a text from resource with one argument.
  * @param resName resource name
  * @return string with resource
  * @param arg the argument
  */
  static String getString (String resName, Object arg) {
    MessageFormat mf = new MessageFormat (getString (resName));
    return mf.format (new Object[] { arg });
  }
  
  /** Getter for a text from resource with one argument.
  * @param resName resource name
  * @return string with resource
  * @param arg1 the argument
  * @param arg2 the argument
  */
  static String getString (String resName, Object arg1, Object arg2) {
    MessageFormat mf = new MessageFormat (getString (resName));
    return mf.format (new Object[] { arg1, arg2 });
  }

  /** Exits from the VM.
  */
  static void doExit (int code) {
    Runtime.getRuntime ().exit (code);
  }



  /** Starts a folder by executing all of its executable children
  * @param f the folder
  */
  private static void startFolder (DataFolder f) {
    DataObject[] obj = f.getChildren ();
    org.openide.actions.ExecuteAction.execute(obj, true);
  }


}


/*
* $Log:
*/
