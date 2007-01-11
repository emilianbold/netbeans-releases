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

package org.netbeans.core;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.TopSecurityManager;
import org.netbeans.core.startup.InstalledFileLocatorImpl;
import org.netbeans.core.startup.Main;
import org.netbeans.core.startup.ModuleSystem;
import org.netbeans.core.startup.Splash;
import org.netbeans.core.startup.StartLog;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

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
        // -----------------------------------------------------------------------------------------------------
        // 10. Loader pool loading
        try {
            LoaderPoolNode.load();
        } catch (IOException ioe) {
            Logger.getLogger(NonGui.class.getName()).log(Level.WARNING, null, ioe);
        }
        StartLog.logProgress ("LoaderPool loaded"); // NOI18N
        Splash.getInstance().increment(10);

        LoaderPoolNode.installationFinished ();
        StartLog.logProgress ("LoaderPool notified"); // NOI18N
        Splash.getInstance().increment(10);

        // install java.net.ProxySelector
        java.net.ProxySelector.setDefault (new NbProxySelector ());
        
        //---------------------------------------------------------------------------------------------------------
        // initialize main window AFTER the setup wizard is finished

        initializeMainWindow ();
        StartLog.logProgress ("Main window initialized"); // NOI18N
        Splash.getInstance().increment(1);

        // -----------------------------------------------------------------------------------------------------
        // 8. Advance Policy

        // set security manager
        SecurityManager secman = new TopSecurityManager();

        System.setSecurityManager(secman);
        TopSecurityManager.makeSwingUseSpecialClipboard(Lookup.getDefault().lookup(org.openide.util.datatransfer.ExClipboard.class));

        // install java.net.Authenticator
        java.net.Authenticator.setDefault (new NbAuthenticator ());
        
        StartLog.logProgress ("Security managers installed"); // NOI18N
        Splash.getInstance().increment(1);
        
        
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
        Splash.getInstance().increment(10);
        StartLog.logProgress ("Timer initialized"); // NOI18N

        // -----------------------------------------------------------------------------------------------------
        // 13. Initialize Shortcuts
        ShortcutsFolder.initShortcuts();
        Splash.getInstance().increment(1);
        StartLog.logProgress ("Shortcuts initialized"); // NOI18N


    // -----------------------------------------------------------------------------------------------------
    // 14. Open main window
        StatusDisplayer.getDefault().setStatusText (NbBundle.getMessage (NonGui.class, "MSG_WindowShowInit"));

        // Starts GUI components to be created and shown on screen.
        // I.e. main window + current workspace components.

        // Access winsys from AWT thread only. In this case main thread wouldn't harm, just to be kosher.
        SwingUtilities.invokeLater(new Runnable() {

                                       public void run() {
                                           StartLog.logProgress("Window system initialization");
                                           if (System.getProperty("netbeans.warmup.skip") ==
                                               null &&
                                               System.getProperty("netbeans.close") ==
                                               null) {
                                               final Frame mainWindow = WindowManager.getDefault().getMainWindow();

                                               mainWindow.addComponentListener(new ComponentAdapter() {

                                                                                   public void componentShown(ComponentEvent evt) {
                                                                                       mainWindow.removeComponentListener(this);
                                                                                       WarmUpSupport.warmUp();
                                                                                   }
                                                                               });
                                           }
                                           NbTopManager.WindowSystem windowSystem = (NbTopManager.WindowSystem) Lookup.getDefault().lookup(NbTopManager.WindowSystem.class);

                                           if (windowSystem != null) {
                                               windowSystem.load();
                                               StartLog.logProgress("Window system loaded");
                                               if (StartLog.willLog()) {
                                                   waitForMainWindowPaint();
                                               }
                                               windowSystem.show();
                                           } else {
                                               Logger.getLogger(NonGui.class.getName()).log(Level.WARNING,
                                                                 null,
                                                                 new NullPointerException("\n\n\nWindowSystem is not supplied!!!\\n\n"));
                                           }
                                           StartLog.logProgress("Window system shown");
                                           if (!StartLog.willLog()) {
                                               maybeDie(null);
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
                  Object o = m.invoke(null);
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

    /** Exits from the VM.
    */
    static void doExit (int code) {
        TopSecurityManager.exit(code);
    }

    /** Get the module subsystem.  */
    public ModuleSystem getModuleSystem() {
        return Main.getModuleSystem ();
    }

}
