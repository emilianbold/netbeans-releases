/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.security.*;
import java.text.MessageFormat;
import javax.swing.*;
import javax.swing.border.*;
import java.lang.reflect.Method;
import org.netbeans.core.startup.Main;

import org.openide.*;
import org.openide.awt.StatusDisplayer;
import org.openide.loaders.*;
import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.windows.*;
import org.openide.explorer.*;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.openide.util.io.*;
import org.openide.nodes.*;

import org.netbeans.TopSecurityManager;

import org.netbeans.core.actions.*;
import org.netbeans.core.startup.InstalledFileLocatorImpl;
import org.netbeans.core.startup.StartLog;
import org.netbeans.core.startup.ModuleSystem;
import org.openide.modules.InstalledFileLocator;

/**
 * Most of the NetBeans startup logic that is not closely tied to the GUI.
 * The meat of the startup sequence is in {@link #run}.
 */
public class NonGui extends NbTopManager 
implements Runnable, org.netbeans.core.startup.RunLevel {
    private static int count;
    
    public NonGui () {
        assert count++ == 0 : "Only one instance allowed"; // NOI18N
    }
    
    /** Everything is interactive */
    public boolean isInteractive (int il) {
        return true;
    }

    /** Initialization of the manager.
    */
    public void run () {
        // autoload directories
        org.openide.util.Task automount = AutomountSupport.initialize ();
        StartLog.logProgress ("Automounter fired"); // NOI18N
        Main.incrementSplashProgressBar();
        
        // -----------------------------------------------------------------------------------------------------
        // 10. Loader pool loading
        try {
            LoaderPoolNode.load();
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        }
        StartLog.logProgress ("LoaderPool loaded"); // NOI18N
        Main.incrementSplashProgressBar(10);

        LoaderPoolNode.installationFinished ();
        StartLog.logProgress ("LoaderPool notified"); // NOI18N
        Main.incrementSplashProgressBar(10);

        Main.incrementSplashProgressBar(10);
        // wait until mounting really occurs
        automount.waitFinished ();
        StartLog.logProgress ("Automounter done"); // NOI18N
        Main.incrementSplashProgressBar(10);

        //---------------------------------------------------------------------------------------------------------
        // initialize main window AFTER the setup wizard is finished

        initializeMainWindow ();
        StartLog.logProgress ("Main window initialized"); // NOI18N
        Main.incrementSplashProgressBar();

        // -----------------------------------------------------------------------------------------------------
        // 8. Advance Policy

        // set security manager
        SecurityManager secman = new TopSecurityManager();

        /* Disabled until there is an IBM JDK 1.4 known to display the same bug:
        // XXX(-trung) workaround for IBM JDK 1.3 Linux bug in
        // java.net.URLClassLoader.findClass().  The IBM implementation of this
        // method is not reentrant. The problem happens when findClass()
        // indirectly calls methods of TopSecurityManager for the first time.
        // This may trigger other classes to be loaded, thus findClass() is
        // re-entered.
        //
        // We try to force dependent classes of TopSecurityManager to be loaded
        // before setting it as system's SecurityManager
        
        try {
            secman.checkRead("xxx"); // NOI18N
        }
        catch (RuntimeException ex) {
            // ignore
        }
        */
        
        System.setSecurityManager(secman);
        TopSecurityManager.makeSwingUseSpecialClipboard (
            (org.openide.util.datatransfer.ExClipboard)
            Lookup.getDefault ().lookup (org.openide.util.datatransfer.ExClipboard.class)
        );

        // install java.net.Authenticator
        java.net.Authenticator.setDefault (new NbAuthenticator ());
        StartLog.logProgress ("Security managers installed"); // NOI18N
        Main.incrementSplashProgressBar();
        
        org.netbeans.Main.finishInitialization();
        StartLog.logProgress("Ran any delayed command-line options"); // NOI18N
        
        InstalledFileLocatorImpl.discardCache();
    }


    /** Method to initialize the main window.
    */
    protected void initializeMainWindow () {
        if (!org.netbeans.core.startup.CLIOptions.isGui ()) {
            return;
        }
        
        StartLog.logStart ("Main window initialization"); //NOI18N

        // #28536: make sure a JRE bug does not prevent the event queue from having
        // the right context class loader
        // and #35470: do it early, before any module-loaded AWT code might run
        // and #36820: even that isn't always early enough, so we need to push
        // a new EQ to enforce the context loader
        // XXX this is a hack!
        try {
            org.openide.util.Mutex.EVENT.writeAccess (new Runnable() {
                public void run() {
                    Thread.currentThread().setContextClassLoader(Main.getModuleSystem().getManager().getClassLoader());
                    Toolkit.getDefaultToolkit().getSystemEventQueue().push(new EventQueue());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        // -----------------------------------------------------------------------------------------------------
        // 11. Initialization of main window
        StatusDisplayer.getDefault().setStatusText (NbBundle.getMessage (NonGui.class, "MSG_MainWindowInit"));

        // force to initialize timer
        // sometimes happened that the timer thread was initialized under
        // a TaskThreadGroup
        // such task never ends or, if killed, timer is over
        Timer timerInit = new Timer(0, new java.awt.event.ActionListener() {
              public void actionPerformed(java.awt.event.ActionEvent ev) { }
        });
        timerInit.setRepeats(false);
        timerInit.start();
        Main.incrementSplashProgressBar(10);
        StartLog.logProgress ("Timer initialized"); // NOI18N

        // -----------------------------------------------------------------------------------------------------
        // 13. Initialize Shortcuts
        ShortcutsFolder.initShortcuts();
        Main.incrementSplashProgressBar();
        StartLog.logProgress ("Shortcuts initialized"); // NOI18N


    // -----------------------------------------------------------------------------------------------------
    // 14. Open main window
        StatusDisplayer.getDefault().setStatusText (NbBundle.getMessage (NonGui.class, "MSG_WindowShowInit"));

        // Starts GUI components to be created and shown on screen.
        // I.e. main window + current workspace components.

        // Access winsys from AWT thread only. In this case main thread wouldn't harm, just to be kosher.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                StartLog.logProgress ("Window system initialization"); // NOI18N
                if (System.getProperty("netbeans.warmup.skip") == null // NOI18N
                        && System.getProperty("netbeans.close") == null) // NOI18N
                {
                    final Frame mainWindow = WindowManager.getDefault().getMainWindow();
                    mainWindow.addComponentListener(new ComponentAdapter() {
                        public void componentShown(ComponentEvent evt) {
                            mainWindow.removeComponentListener(this);
                            WarmUpSupport.warmUp();
                        }
                    });
                }

                NbTopManager.WindowSystem windowSystem = (NbTopManager.WindowSystem)
                        org.openide.util.Lookup.getDefault().lookup(NbTopManager.WindowSystem.class);
                if(windowSystem != null) {
                    windowSystem.load();
                    StartLog.logProgress ("Window system loaded"); // NOI18N
                    if (StartLog.willLog()) {
                        waitForMainWindowPaint();
                    }
                    windowSystem.show();
                } else {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, 
                        new NullPointerException("\n\n\nWindowSystem is not supplied!!!\\n\n")); // NOI18N
                }

                StatusDisplayer.getDefault().setStatusText(""); // NOI18N

                StartLog.logProgress ("Window system shown"); // NOI18N
                if (!StartLog.willLog()) {
                    maybeDie(null);
                    // note that if we ARE logging and measuring startup,
                    // the IDE is killed through wairForMainWindowPaint() called above
                }
            }
        });
        StartLog.logEnd ("Main window initialization"); //NOI18N
    }
  
    private static void waitForMainWindowPaint() {
        // Waits for notification about processed paint event for main window
        // require modified java.awt.EventQueue to run succesfully
        Runnable r = new Runnable() {
          public void run() {
              try {
                  Class clz = Class.forName("org.netbeans.performance.test.guitracker.LoggingRepaintManager"); // NOI18N
                  Method m = clz.getMethod("measureStartup", new Class[] {}); // NOI18N
                  Object o = m.invoke(null, null);
                  endOfStartupMeasuring(o);
              } catch (ClassNotFoundException e) {
                  StartLog.logProgress(e.toString());
              } catch (NoSuchMethodException e) {
                  StartLog.logProgress(e.toString());
        //              } catch (InterruptedException e) {
        //                  StartLog.logProgress(e.toString());
              } catch (IllegalAccessException e) {
                  StartLog.logProgress(e.toString());
              } catch (java.lang.reflect.InvocationTargetException e) {
                  StartLog.logProgress(e.toString());
              }
          }
        };
        new Thread(r).start();
    }
    private static void endOfStartupMeasuring(Object o) {
      StartLog.logProgress("Startup memory and time measured"); // NOI18N
      maybeDie(o);
    }

    private static void maybeDie(Object o) {
        // finish starting
        if (System.getProperty("netbeans.kill") != null) {
            org.netbeans.TopSecurityManager.exit(5);
        }

        // close IDE
        if (System.getProperty("netbeans.close") != null) {
            if (Boolean.getBoolean("netbeans.warm.close")) {
                // Do other stuff related to startup, to measure the effect.
                // Useful for performance testing.
                new WarmUpSupport().run(); // synchronous
            }
            if(o!=null) StartLog.logMeasuredStartupTime(((Long)o).longValue());
            org.openide.LifecycleManager.getDefault().exit();
        }
    }

    /** Getter for a text from resource.
    * @param resName resource name
    * @return string with resource
    */
    static String getString (String resName) {
        return NbBundle.getMessage(NonGui.class, resName);
    }

    /** Getter for a text from resource with one argument.
    * @param resName resource name
    * @return string with resource
    * @param arg1 the argument
    * @param arg2 the argument
    */
    static String getString (String resName, Object arg1, Object arg2) {
        return NbBundle.getMessage(NonGui.class, resName, arg1, arg2);
    }

    /** Exits from the VM.
    */
    static void doExit (int code) {
        TopSecurityManager.exit(code);
    }



    /** Get the module subsystem.  */
    public ModuleSystem getModuleSystem() {
        return Main.getModuleSystem ();
    }

    /** This is a notification about hiding wizards 
     * during startup (Import, Setup). It is used in subclass 
     * for showing the splash screen again, when wizard disappears.
     *
     * It does nothing in NonGui implementation.
     */
    protected void showSplash () {
    }

    /** Return splash screen if available.
     */
    protected org.netbeans.core.startup.Splash.SplashOutput getSplash() {
        return null;
    }
    
}
