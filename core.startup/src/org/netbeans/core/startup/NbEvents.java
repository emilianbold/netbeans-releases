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

package org.netbeans.core.startup;

// May use core, GUI, ad nauseum.

import java.io.File;
import java.text.Collator;
import java.util.*;
import javax.swing.JOptionPane;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.netbeans.*;

/** Report events to the performance logger, status text/splash screen,
 * console, and so on.
 * @author Jesse Glick
 */
final class NbEvents extends Events {
    
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
            org.netbeans.core.startup.Main.addToSplashMaxSteps(1);
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
            List modules = (List)args[0];
            if (! modules.isEmpty()) {
                System.err.println(NbBundle.getMessage(NbEvents.class, "TEXT_finish_enable_modules"));
                dumpModulesList(modules);
            }
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_finish_enable_modules"));
            StartLog.logEnd("ModuleManager.enable"); // NOI18N
        } else if (message == START_DISABLE_MODULES) {
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_start_disable_modules"));
        } else if (message == FINISH_DISABLE_MODULES) {
            List modules = (List)args[0];
            if (! modules.isEmpty()) {
                System.err.println(NbBundle.getMessage(NbEvents.class, "TEXT_finish_disable_modules"));
                dumpModulesList(modules);
            }
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_finish_disable_modules"));
        } else if (message == START_DEPLOY_TEST_MODULE) {
            // No need to print anything. ModuleSystem.deployTestModule prints
            // its own stuff (it needs to be printed synchronously to console
            // in order to appear in the output window). But status text is OK.
            // Again no need for I18N as this is only for module developers.
            setStatusText(
                "Deploying test module in " + (File)args[0] + "..."); // NOI18N
        } else if (message == FINISH_DEPLOY_TEST_MODULE) {
            setStatusText(
                "Finished deploying test module."); // NOI18N
        } else if (message == FAILED_INSTALL_NEW) {
            SortedSet problemTexts = new TreeSet(Collator.getInstance()); // SortedSet<String>
            Iterator it = ((Set)args[0]).iterator();
            while (it.hasNext()) {
                Module m = (Module)it.next();
                Iterator pit = m.getProblems().iterator();
                if (pit.hasNext()) {
                    while (pit.hasNext()) {
                        problemTexts.add(m.getDisplayName() + " - " + // NOI18N
                                         NbProblemDisplayer.messageForProblem(m, pit.next()));
                    }
                } else {
                    throw new IllegalStateException("Module " + m + " could not be installed but had no problems"); // NOI18N
                }
            }
            StringBuffer buf = new StringBuffer(NbBundle.getMessage(NbEvents.class, "MSG_failed_install_new"));
            it = problemTexts.iterator();
            while (it.hasNext()) {
                buf.append("\n\t"); // NOI18N
                buf.append((String)it.next());
            }
            String msg = buf.toString();
            notify(msg, true);
            System.err.println(msg);
            setStatusText("");
        } else if (message == FAILED_INSTALL_NEW_UNEXPECTED) {
            Module m = (Module)args[0];
            // ignore args[1]: InvalidException
            StringBuffer buf = new StringBuffer(NbBundle.getMessage(NbEvents.class, "MSG_failed_install_new_unexpected", m.getDisplayName()));
            Iterator it = m.getProblems().iterator();
            if (it.hasNext()) {
                SortedSet problemTexts = new TreeSet(Collator.getInstance()); // SortedSet<String>
                while (it.hasNext()) {
                    problemTexts.add(NbProblemDisplayer.messageForProblem(m, it.next()));
                }
                it = problemTexts.iterator();
                while (it.hasNext()) {
                    buf.append(" - "); // NOI18N
                    buf.append((String)it.next());
                }
            } else {
                throw new IllegalStateException("Module " + m + " could not be installed but had no problems"); // NOI18N
            }
            notify(buf.toString(), true);
            System.err.println(buf.toString());
            setStatusText("");
        } else if (message == START_READ) {
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_start_read"));
            StartLog.logStart("ModuleList.readInitial"); // NOI18N
        } else if (message == MODULES_FILE_PROCESSED) {
            incrementSplashProgressBar();
            if (StartLog.willLog()) {
                StartLog.logProgress("file " + ((FileObject)args[0]).getNameExt() + " processed"); // NOI18N
            }
        } else if (message == FINISH_READ) {
            Set modules = (Set)args[0];
            org.netbeans.core.startup.Main.addToSplashMaxSteps(modules.size() + modules.size());
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_finish_read"));
            StartLog.logEnd("ModuleList.readInitial"); // NOI18N
        } else if (message == RESTORE) {
            // Don't look for display name. Just takes too long.
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_restore"/*, ((Module)args[0]).getDisplayName()*/));
            incrementSplashProgressBar();
        } else if (message == INSTALL) {
            // Nice to see the real title; not that common, after all.
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_install", ((Module)args[0]).getDisplayName()));
            System.err.println(NbBundle.getMessage(NbEvents.class, "TEXT_install", ((Module)args[0]).getDisplayName()));
            incrementSplashProgressBar();
        } else if (message == UPDATE) {
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_update", ((Module)args[0]).getDisplayName()));
            System.err.println(NbBundle.getMessage(NbEvents.class, "TEXT_update", ((Module)args[0]).getDisplayName()));
            incrementSplashProgressBar();
        } else if (message == UNINSTALL) {
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_uninstall", ((Module)args[0]).getDisplayName()));
        } else if (message == LOAD_SECTION) {
            // Again avoid finding display name now.
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_load_section"/*, ((Module)args[0]).getDisplayName()*/));
        } else if (message == LOAD_LAYERS) {
            setStatusText(
                NbBundle.getMessage(NbEvents.class, "MSG_load_layers"));
        } else if (message == WRONG_CLASS_LOADER) {
            if (! Boolean.getBoolean("netbeans.moduleitem.dontverifyclassloader") && Util.err.isLoggable(ErrorManager.WARNING)) { // NOI18N
                Class clazz = (Class)args[1];
                // Message for developers, no need for I18N.
                StringBuffer b = new StringBuffer();
                b.append("The module " + ((Module)args[0]).getDisplayName() + " loaded the class " + clazz.getName() + "\n"); // NOI18N
                b.append("from the wrong classloader. The expected classloader was " + args[2] + "\n"); // NOI18N
                b.append("whereas it was actually loaded from " + clazz.getClassLoader() + "\n"); // NOI18N
                b.append("Usually this means that some classes were in the startup classpath.\n"); // NOI18N
                b.append("To suppress this message, run with: -J-Dnetbeans.moduleitem.dontverifyclassloader=true"); // NOI18N
                Util.err.log(ErrorManager.WARNING, b.toString());
            }
        } else if (message == EXTENSION_MULTIPLY_LOADED) {
            // Developer-oriented message, no need for I18N.
            Util.err.log(ErrorManager.WARNING, "Warning: the extension " + (File)args[0] + " may be multiply loaded by modules: " + (Set/*<File>*/)args[1] + "; see: http://www.netbeans.org/download/dev/javadoc/OpenAPIs/org/openide/doc-files/classpath.html#class-path"); // NOI18N
        } else if (message == MISSING_JAR_FILE) {
            File jar = (File)args[0];
            System.err.println(NbBundle.getMessage(NbEvents.class, "TEXT_missing_jar_file", jar.getAbsolutePath()));
        } else if (message == CANT_DELETE_ENABLED_AUTOLOAD) {
            Module m = (Module)args[0];
            System.err.println(NbBundle.getMessage(NbEvents.class, "TEXT_cant_delete_enabled_autoload", m.getDisplayName()));
        } else if (message == MISC_PROP_MISMATCH) {
            // XXX does this really need to be logged to the user?
            // Or should it just be sent quietly to the log file?
            Module m = (Module)args[0];
            String prop = (String)args[1];
            Object onDisk = (Object)args[2];
            Object inMem = (Object)args[3];
            System.err.println(NbBundle.getMessage(NbEvents.class, "TEXT_misc_prop_mismatch", new Object[] {m.getDisplayName(), prop, onDisk, inMem}));
        } else if (message == PATCH) {
            File f = (File)args[0];
            System.err.println(NbBundle.getMessage(NbEvents.class, "TEXT_patch", f.getAbsolutePath()));
        }
        // XXX other messages?
    }

    /** Print a nonempty list of modules to console (= log file).
     * @param modules the modules
     */
    private void dumpModulesList(Collection modules) {
        Iterator it = modules.iterator();
        if (! it.hasNext()) throw new IllegalArgumentException();
        StringBuffer buf = new StringBuffer(modules.size() * 100 + 1);
        String lineSep = System.getProperty("line.separator");
        while (it.hasNext()) {
            Module m = (Module)it.next();
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
        System.err.print(buf.toString());
    }
    
    private void notify(String text, boolean warn) {
        if (Boolean.getBoolean("netbeans.full.hack")) { // NOI18N
            // #21773: interferes with automated GUI testing.
            System.err.println(text);
        } else {
            // Normal - display dialog.
            int type = warn ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
            javax.swing.JOptionPane p = new javax.swing.JOptionPane (text, type);
            RequestProcessor.getDefault().post(new Notifier(p));
        }
    }
    private static final class Notifier implements Runnable {
        private final javax.swing.JOptionPane desc;
        public Notifier(javax.swing.JOptionPane desc) {
            this.desc = desc;
        }
        public void run() {
             desc.setVisible (true);
        }
    }

    private static void incrementSplashProgressBar () {
        org.netbeans.core.startup.Main.incrementSplashProgressBar ();
    }
    
    private static void setStatusText (String msg) {
        org.netbeans.core.startup.Main.setStatusText (msg);
    }
}
