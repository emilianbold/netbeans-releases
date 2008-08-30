/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.core.startup;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import org.netbeans.Events;
import org.netbeans.Module;
import org.netbeans.Util;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;

/** Report events to the performance logger, status text/splash screen,
 * console, and so on.
 * @author Jesse Glick
 */
final class NbEvents extends Events {
    private Logger logger = Logger.getLogger(NbEvents.class.getName());

    private int moduleCount;
	
    private int counter;

    /** Handle a logged event.
     * CAREFUL that this is called synchronously, usually within a write
     * mutex or other sensitive environment. So do not call anything
     * blocking (like TM.notify) directly. TM.setStatusText and printing
     * to console are fine, as well as performance logging.
     */
    protected void logged(final String message, Object[] args) {
        if (message == PERF_TICK) {
            StartLog.logProgress( (String)args[0]);
        } else if (message == PERF_START) {
            StartLog.logStart( (String)args[0]);
        } else if (message == PERF_END) {
            StartLog.logEnd( (String)args[0]);
        } else if (message == START_CREATE_BOOT_MODULE) {
            org.netbeans.core.startup.Splash.getInstance().increment(1);
        } else if (message == START_LOAD_BOOT_MODULES) {
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_start_load_boot_modules"));
            StartLog.logStart("ModuleSystem.loadBootModules"); // NOI18N
        } else if (message == START_LOAD) {
            StartLog.logStart("NbInstaller.load"); // NOI18N
        } else if (message == FINISH_LOAD_BOOT_MODULES) {
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_finish_load_boot_modules"));
            StartLog.logEnd( "ModuleSystem.loadBootModules" ); // NOI18N
        } else if (message == FINISH_LOAD) {
            StartLog.logEnd("NbInstaller.load"); // NOI18N
        } else if (message == START_AUTO_RESTORE) {
            Set modules = (Set)args[0];
            if (! modules.isEmpty()) {
                setStatusText(
                    NbBundle.getMessage(NbEvents.class, "MSG_start_auto_restore"));
            }
        } else if (message == FINISH_AUTO_RESTORE) {
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_finish_auto_restore"));
        } else if (message == START_ENABLE_MODULES) {
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_start_enable_modules"));
        } else if (message == FINISH_ENABLE_MODULES) {
            List<Module> modules = NbCollections.checkedListByCopy((List) args[0], Module.class, true);
            if (! modules.isEmpty()) {
                logger.log(Level.INFO, NbBundle.getMessage(NbEvents.class, "TEXT_finish_enable_modules"));
                dumpModulesList(modules);
            }
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_finish_enable_modules"));
            StartLog.logEnd("ModuleManager.enable"); // NOI18N
        } else if (message == START_DISABLE_MODULES) {
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_start_disable_modules"));
        } else if (message == FINISH_DISABLE_MODULES) {
            List<Module> modules = NbCollections.checkedListByCopy((List) args[0], Module.class, true);
            if (! modules.isEmpty()) {
                logger.log(Level.INFO, NbBundle.getMessage(NbEvents.class, "TEXT_finish_disable_modules"));
                dumpModulesList(modules);
            }
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_finish_disable_modules"));
        } else if (message == START_DEPLOY_TEST_MODULE) {
            // No need to print anything. ModuleSystem.deployTestModule prints
            // its own stuff (it needs to be printed synchronously to console
            // in order to appear in the output window). But status text is OK.
            // Fix for IZ#81566 - I18N: need to localize status messages for module dev
            String msg = MessageFormat.format(
                    NbBundle.getMessage(NbEvents.class, 
                            "TEXT_start_deploy_test_module" ), (File)args[0]); // NOI18N
            setStatusText( msg ); 
        } else if (message == FINISH_DEPLOY_TEST_MODULE) {
            // Fix for IZ#81566 - I18N: need to localize status messages for module dev
            setStatusText(
                    NbBundle.getMessage(NbEvents.class,  
                    		"TEXT_finish_deploy_test_module")); // NOI18N
        } else if (message == FAILED_INSTALL_NEW) {
            Set<Module> modules = NbCollections.checkedSetByCopy((Set) args[0], Module.class, true);
            {
                StringBuilder buf = new StringBuilder(NbBundle.getMessage(NbEvents.class, "MSG_failed_install_new"));
                NbProblemDisplayer.problemMessagesForModules(buf, modules, false);
                buf.append('\n'); // #123669
                logger.log(Level.INFO, buf.toString());
            }
            {
                StringBuilder buf = new StringBuilder(NbBundle.getMessage(NbEvents.class, "MSG_failed_install_new"));
                NbProblemDisplayer.problemMessagesForModules(buf, modules, true);
                String msg = buf.toString();
                notify(msg, true);
            }
            setStatusText("");
        } else if (message == FAILED_INSTALL_NEW_UNEXPECTED) {
            Module m = (Module)args[0];
            List<Module> modules = new ArrayList<Module> ();
            modules.add (m);
            modules.addAll (NbCollections.checkedSetByCopy((Set) args[1], Module.class, true));
            // ignore args[2]: InvalidException
            {
                StringBuilder buf = new StringBuilder(NbBundle.getMessage(NbEvents.class, "MSG_failed_install_new_unexpected", m.getDisplayName()));
                NbProblemDisplayer.problemMessagesForModules(buf, modules, false);
                buf.append('\n');
                logger.log(Level.INFO, buf.toString());
            }

            {
                notify(NbProblemDisplayer.messageForProblem (m, m.getProblems ().iterator ().next (), true), true);
            }
            setStatusText("");
        } else if (message == START_READ) {
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_start_read"));
            StartLog.logStart("ModuleList.readInitial"); // NOI18N
        } else if (message == MODULES_FILE_SCANNED) {
	    moduleCount = (Integer)args[0];
            Splash.getInstance().addToMaxSteps(Math.max(moduleCount + moduleCount/2 - 100, 0));
        } else if (message == MODULES_FILE_PROCESSED) {
            Splash.getInstance().increment(1);
            if (StartLog.willLog()) {
                StartLog.logProgress("module " + args[0] + " processed"); // NOI18N
            }
        } else if (message == FINISH_READ) {
	    if (moduleCount < 100) {
		Splash.getInstance().increment(moduleCount - 100);
	    }
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_finish_read"));
            StartLog.logEnd("ModuleList.readInitial"); // NOI18N
        } else if (message == RESTORE) {
            // Don't look for display name. Just takes too long.
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_restore"/*, ((Module)args[0]).getDisplayName()*/));
	    if (++counter < moduleCount / 2) {
		Splash.getInstance().increment(1);
	    }
        } else if (message == INSTALL) {
            // Nice to see the real title; not that common, after all.
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_install", ((Module)args[0]).getDisplayName()));
            logger.log(Level.INFO, NbBundle.getMessage(NbEvents.class, "TEXT_install", ((Module)args[0]).getDisplayName()));
        } else if (message == UPDATE) {
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_update", ((Module)args[0]).getDisplayName()));
            logger.log(Level.INFO, NbBundle.getMessage(NbEvents.class, "TEXT_update", ((Module)args[0]).getDisplayName()));
        } else if (message == UNINSTALL) {
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_uninstall", ((Module)args[0]).getDisplayName()));
        } else if (message == LOAD_SECTION) {
            // Again avoid finding display name now.
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_load_section"/*, ((Module)args[0]).getDisplayName()*/));
	    if (++counter < moduleCount / 4) {
		Splash.getInstance().increment(1);
	    }
        } else if (message == LOAD_LAYERS) {
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_load_layers"));
        } else if (message == WRONG_CLASS_LOADER) {
            if (! Boolean.getBoolean("netbeans.moduleitem.dontverifyclassloader") && Util.err.isLoggable(Level.WARNING)) { // NOI18N
                Class clazz = (Class)args[1];
                // Message for developers, no need for I18N.
                StringBuilder b = new StringBuilder();
                b.append("The module " + ((Module)args[0]).getDisplayName() + " loaded the class " + clazz.getName() + "\n"); // NOI18N
                b.append("from the wrong classloader. The expected classloader was " + args[2] + "\n"); // NOI18N
                b.append("whereas it was actually loaded from " + clazz.getClassLoader() + "\n"); // NOI18N
                b.append("Usually this means that some classes were in the startup classpath.\n"); // NOI18N
                b.append("To suppress this message, run with: -J-Dnetbeans.moduleitem.dontverifyclassloader=true"); // NOI18N
                Util.err.warning(b.toString());
            }
        } else if (message == EXTENSION_MULTIPLY_LOADED) {
            // Developer-oriented message, no need for I18N.
            logger.log(Level.WARNING, "The extension " + (File)args[0] + " may be multiply loaded by modules: " + (Set/*<File>*/)args[1] + "; see: http://www.netbeans.org/download/dev/javadoc/org-openide-modules/org/openide/modules/doc-files/classpath.html#class-path"); // NOI18N
        } else if (message == MISSING_JAR_FILE) {
            File jar = (File)args[0];
            logger.log(Level.INFO, NbBundle.getMessage(NbEvents.class, "TEXT_missing_jar_file", jar.getAbsolutePath()));
        } else if (message == CANT_DELETE_ENABLED_AUTOLOAD) {
            Module m = (Module)args[0];
            logger.log(Level.INFO, NbBundle.getMessage(NbEvents.class, "TEXT_cant_delete_enabled_autoload", m.getDisplayName()));
        } else if (message == MISC_PROP_MISMATCH) {
            // XXX does this really need to be logged to the user?
            // Or should it just be sent quietly to the log file?
            Module m = (Module)args[0];
            String prop = (String)args[1];
            Object onDisk = args[2];
            Object inMem = args[3];
            logger.log(Level.INFO, NbBundle.getMessage(NbEvents.class, "TEXT_misc_prop_mismatch", new Object[] {m.getDisplayName(), prop, onDisk, inMem}));
        } else if (message == PATCH) {
            File f = (File)args[0];
            logger.log(Level.INFO, NbBundle.getMessage(NbEvents.class, "TEXT_patch", f.getAbsolutePath()));
        }
        // XXX other messages?
    }

    /** Print a nonempty list of modules to console (= log file).
     * @param modules the modules
     */
    private void dumpModulesList(Collection<Module> modules) {
        if (modules.isEmpty()) {
            throw new IllegalArgumentException();
        }
        StringBuilder buf = new StringBuilder(modules.size() * 100 + 1);
        String lineSep = System.getProperty("line.separator");
        for (Module m : modules) {
            buf.append('\t'); // NOI18N
            buf.append(m.getCodeName());
            buf.append(" ["); // NOI18N
            SpecificationVersion sv = m.getSpecificationVersion();
            if (sv != null) {
                buf.append(sv);
            }
            String iv = m.getImplementationVersion();
            if (iv != null) {
                buf.append(' '); // NOI18N
                buf.append(iv);
            }
            String bv = m.getBuildVersion();
            if (bv != null && !bv.equals (iv)) {
                buf.append(' '); // NOI18N
                buf.append(bv);
            }
            buf.append(']'); // NOI18N
            // #32331: use platform-specific newlines
            buf.append(lineSep);
        }
        logger.log(Level.INFO, buf.toString());
    }
    
    private void notify(String text, boolean warn) {
        if (Boolean.getBoolean("netbeans.full.hack")) { // NOI18N
            // #21773: interferes with automated GUI testing.
            logger.log(Level.INFO, text + "\n");
        } else {
            // Normal - display dialog.
            new Notifier(text, warn);
        }
    }
    private static final class Notifier implements Runnable {
        private static boolean showDialog = true;
        
        private boolean warn;
        private String text;
        private static RequestProcessor RP = new RequestProcessor("Notify About Module System"); // NOI18N
        
        public Notifier(String text, boolean type) {
            this.warn = type;
            this.text = text;
            
            
            if (showDialog) {
                showDialog = false;
                RP.post(this, 0, Thread.MIN_PRIORITY).waitFinished ();
            }
        }
        
        public void run() {
            int type = warn ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
            String msg = NbBundle.getMessage(Notifier.class, warn ? "MSG_warning" : "MSG_info"); // NOI18N

            Splash out = Splash.getInstance();
            final Component c = out.getComponent() == null ? null : out.getComponent();
            try {
                UIManager.setLookAndFeel (UIManager.getSystemLookAndFeelClassName ());
            } catch (ClassNotFoundException ex) {
                Logger.getLogger (NbBundle.class.getName ()).log(Level.INFO, null, ex);
            } catch (InstantiationException ex) {
                Logger.getLogger (NbBundle.class.getName ()).log(Level.INFO, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger (NbBundle.class.getName ()).log(Level.INFO, null, ex);
            } catch (UnsupportedLookAndFeelException ex) {
                Logger.getLogger (NbBundle.class.getName ()).log(Level.INFO, null, ex);
            }
            JTextPane tp = new JTextPane ();
            tp.setContentType("text/html"); // NOI18N
            text = text.replace ("\n", "<br>"); // NOI18N

            tp.setEditable(false);
            tp.setOpaque (false);
            tp.setEnabled(true);
            tp.addHyperlinkListener(new HyperlinkListener() {
                public void hyperlinkUpdate(HyperlinkEvent hlevt) {
                    if (EventType.ACTIVATED == hlevt.getEventType()) {
                        assert hlevt.getURL() != null;
                        try {
                            showUrl (hlevt.getURL ().toURI (), c);
                        } catch (Exception ex) {
                            Logger.getLogger (NbBundle.class.getName ()).log(Level.INFO, null, ex);
                        }
                    }
                }
            });

            tp.setText (text);
            
            JComponent sp;
            if (tp.getPreferredSize ().width > 600 || tp.getPreferredSize ().height > 400) {
                tp.setPreferredSize (new Dimension (600, 400));
                sp = new JScrollPane (tp);
            } else {
                sp = tp;
            }
            final JOptionPane op = new JOptionPane (sp, type, JOptionPane.YES_NO_OPTION, null);

            JButton continueButton = new JButton (NbBundle.getMessage(Notifier.class, "MSG_continue")); // NOI18N
            continueButton.setDisplayedMnemonicIndex (0);
            continueButton.addActionListener (new ActionListener () {
                public void actionPerformed (ActionEvent e) {
                    op.setValue (0);
                }
            });
            JButton exitButton = new JButton (NbBundle.getMessage(Notifier.class, "MSG_exit")); // NOI18N
            exitButton.addActionListener (new ActionListener () {
                public void actionPerformed (ActionEvent e) {
                    op.setValue (1);
                }
            });

            Object [] options = new JButton [] {continueButton, exitButton};
            op.setOptions (options);
            op.setInitialValue (options [1]);
            JDialog d = op.createDialog (c, msg);
            d.setResizable (true);
            d.setVisible (true);
            
            Object res = op.getValue ();
            if (res instanceof Integer) {
                int ret = (Integer) res;
                if (ret == 1 || ret == -1) { // exit or close
                    TopLogging.exit(1);
                }
            }
        }
    }
        
    private static void setStatusText (String msg) {
        Main.setStatusText (msg);
    }
    
    private static void showUrl (URI uri, Component c) throws Exception {
        SpecificationVersion javaSpec = new SpecificationVersion (System.getProperty("java.specification.version")); // NOI18N
        if (javaSpec.compareTo (new SpecificationVersion ("1.6")) >= 0) {
            Class<?> desktopC = Class.forName ("java.awt.Desktop");
            Method getDesktopM = desktopC.getMethod ("getDesktop");
            Object desktopInstanceO = getDesktopM.invoke (null);
            Method browseM = desktopC.getMethod ("browse", URI.class);
            browseM.invoke (desktopInstanceO, uri);
        }
    }
}
