/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.core;

import java.awt.Frame;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.TopSecurityManager;
import org.netbeans.core.startup.CLIOptions;
import org.netbeans.core.startup.MainLookup;
import org.netbeans.core.startup.RunLevel;
import org.netbeans.core.startup.Splash;
import org.netbeans.core.startup.StartLog;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * GUI-oriented NetBeans startup logic.
 */
@ServiceProvider(service=RunLevel.class)
public class GuiRunLevel implements RunLevel {
    private static int count;
    
    public GuiRunLevel() {
        MainLookup.started();
        assert count++ == 0 : "Only one instance allowed"; // NOI18N
    }
    
    /** Initialization of the manager.
    */
    public @Override void run() {
        // -----------------------------------------------------------------------------------------------------
        // 10. Loader pool loading
        try {
            NbLoaderPool.load();
        } catch (IOException ioe) {
            Logger.getLogger(GuiRunLevel.class.getName()).log(Level.INFO, null, ioe);
        }
        StartLog.logProgress ("LoaderPool loaded"); // NOI18N
        Splash.getInstance().increment(10);

        NbLoaderPool.installationFinished ();
        StartLog.logProgress ("LoaderPool notified"); // NOI18N
        Splash.getInstance().increment(10);

        // install java.net.ProxySelector
        java.net.ProxySelector.setDefault (new NbProxySelector ());
        
        if (CLIOptions.isGui()) {
        //---------------------------------------------------------------------------------------------------------
        // initialize main window AFTER the setup wizard is finished

        initializeMainWindow ();
        StartLog.logProgress ("Main window initialized"); // NOI18N
        Splash.getInstance().increment(1);
        }

        // -----------------------------------------------------------------------------------------------------
        // 8. Advance Policy

        if (!Boolean.getBoolean("TopSecurityManager.disable")) {
            // set security manager
            TopSecurityManager.install();
            if (CLIOptions.isGui()) {
                TopSecurityManager.makeSwingUseSpecialClipboard(Lookup.getDefault().lookup(org.openide.util.datatransfer.ExClipboard.class));
            }
        }

        // install java.net.Authenticator
        java.net.Authenticator.setDefault (new NbAuthenticator ());
        
        StartLog.logProgress ("Security managers installed"); // NOI18N
        Splash.getInstance().increment(1);
    }


    /** Method to initialize the main window.
    */
    private void initializeMainWindow() {
        StartLog.logStart ("Main window initialization"); //NOI18N

        TimableEventQueue.initialize();
        
        // -----------------------------------------------------------------------------------------------------
        // 11. Initialization of main window
        StatusDisplayer.getDefault().setStatusText (NbBundle.getMessage (GuiRunLevel.class, "MSG_MainWindowInit"));

        // force to initialize timer
        // sometimes happened that the timer thread was initialized under
        // a TaskThreadGroup
        // such task never ends or, if killed, timer is over
        Timer timerInit = new Timer(0, new java.awt.event.ActionListener() {
              public @Override void actionPerformed(java.awt.event.ActionEvent ev) { }
        });
        timerInit.setRepeats(false);
        timerInit.start();
        Splash.getInstance().increment(10);
        StartLog.logProgress ("Timer initialized"); // NOI18N

    // -----------------------------------------------------------------------------------------------------
    // 14. Open main window
        StatusDisplayer.getDefault().setStatusText (NbBundle.getMessage (GuiRunLevel.class, "MSG_WindowShowInit"));

        // Starts GUI components to be created and shown on screen.
        // I.e. main window + current workspace components.



        // Access winsys from AWT thread only. In this case main thread wouldn't harm, just to be kosher.
        final WindowSystem windowSystem = Lookup.getDefault().lookup(WindowSystem.class);
        if (windowSystem != null) {
            windowSystem.init();
        }
        SwingUtilities.invokeLater(new Runnable() {
            public @Override void run() {
                StartLog.logProgress("Window system initialization");
                if (System.getProperty("netbeans.warmup.skip") == null && System.getProperty("netbeans.close") == null) {
                    final Frame mainWindow = WindowManager.getDefault().getMainWindow();
                    mainWindow.addComponentListener(new ComponentAdapter() {
                        public @Override void componentShown(ComponentEvent evt) {
                            mainWindow.removeComponentListener(this);
                            WarmUpSupport.warmUp();
                        }
                    });
                }
                if (windowSystem != null) {
                    windowSystem.load();
                    StartLog.logProgress("Window system loaded");
                    if (StartLog.willLog()) {
                        waitForMainWindowPaint();
                    }
                    windowSystem.show();
                } else {
                    Logger.getLogger(GuiRunLevel.class.getName()).log(Level.WARNING, "Module org.netbeans.core.windows missing, cannot start window system");
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
          public @Override void run() {
              try {
                  Class<?> clz = Class.forName("org.netbeans.performance.test.guitracker.LoggingRepaintManager"); // NOI18N
                  Method m = clz.getMethod("measureStartup"); // NOI18N
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
            if (o != null) {
                StartLog.logMeasuredStartupTime((Long) o);
            }
            org.openide.LifecycleManager.getDefault().exit();
        }
    }

}
