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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby.debugger;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.rubyproject.gems.Gem;
import org.netbeans.modules.ruby.rubyproject.gems.GemManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor.Message;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author Martin Krauskopf
 */
public final class Util {
    
    public static final Logger LOGGER = Logger.getLogger(Util.class.getName());
    
    /** Name of the Ruby Debug IDE gem. */
    static final String RUBY_DEBUG_IDE_NAME = "ruby-debug-ide";
    
    /** Required version of ruby-debug-ide gem. */
    static final String RDEBUG_IDE_VERSION = "0.1.7"; // NOI18N
    
    private static final String RUBY_MIME_TYPE = "text/x-ruby"; // NOI18N
    private static final String ERB_MIME_TYPE = "application/x-httpd-eruby"; // NOI18N
    
    private Util() { /* do not allow instances */ }
    
    public static void finest(String message) {
        LOGGER.finest(message);
    }
    
    public static void info(String message) {
        LOGGER.info(message);
    }
    
    public static void warning(String message) {
        LOGGER.warning(message);
    }
    
    public static void severe(String failure) {
        LOGGER.log(Level.SEVERE, failure);
    }
    
    public static void severe(Throwable t) {
        LOGGER.log(Level.SEVERE, t.getMessage(), t);
    }
    
    public static void severe(String message, Throwable t) {
        LOGGER.log(Level.SEVERE, message, t);
    }
    
    public static String rdebugPattern() {
        return "rdebug-ide"; // NOI18N
    }
    
    public static boolean isValidRDebugIDEGemInstalled() {
        return GemManager.isGemInstalled(RUBY_DEBUG_IDE_NAME, RDEBUG_IDE_VERSION);
    }
    
    public static String findRDebugExecutable() {
        return RubyInstallation.getInstance().findGemExecutable(rdebugPattern());
    }
    
    public static void showMessage(final String message) {
        DialogDisplayer.getDefault().notify(new Message(message));
    }
    
    public static void showWarning(final String warning) {
        DialogDisplayer.getDefault().notify(new Message(warning, Message.WARNING_MESSAGE));
    }
    
    /**
     * Supported mime-types:
     *
     * <ul>
     *   <li>text/x-ruby</li>
     *   <li>application/x-httpd-eruby</li>
     * </ul>
     */
    public static boolean isRubySource(final FileObject fo) {
        return RUBY_MIME_TYPE.equals(fo.getMIMEType()) || isERBSource(fo);
    }
    
    public static boolean isERBSource(final FileObject fo) {
        return ERB_MIME_TYPE.equals(fo.getMIMEType());
    }
    
    /**
     * Returns currently active RubySession. Might be <code>null</code>.
     */
    public static RubySession getCurrentSession() {
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        return (currentEngine == null) ? null :
            (RubySession) currentEngine.lookupFirst(null, RubySession.class);
    }
    
    static boolean ensureRubyDebuggerIsPresent(final boolean strict, final String message) {
        if (Util.isValidRDebugIDEGemInstalled()) {
            turnOnRubyDebugOptions();
            return true;
        }
        if (!strict && DebuggerPreferences.getInstance().isDoNotAskAgain()) {
            return false;
        }
        RubyDebugInstallPanel rubyDebugPanel = new RubyDebugInstallPanel(strict, message);
        DialogDescriptor descriptor = new DialogDescriptor(rubyDebugPanel,
                NbBundle.getMessage(Util.class, "Util.installation.panel.title", Util.RDEBUG_IDE_VERSION));
        JButton installButton = new JButton(getMessage("Util.installation.panel.installbutton"));
        JButton nonInstallButton;
        if (strict) {
            nonInstallButton = new JButton(getMessage("Util.installation.panel.cancelbutton"));
        } else {
            nonInstallButton = new JButton(getMessage("Util.installation.panel.continuebutton"));
        }
        Object[] options = new Object[] { installButton, nonInstallButton };
        descriptor.setOptions(options);
        if (DialogDisplayer.getDefault().notify(descriptor) == installButton) {
            installRubyDebugGem();
            if (Util.isValidRDebugIDEGemInstalled()) {
                turnOnRubyDebugOptions();
            }
        }
        if (!strict) {
            DebuggerPreferences.getInstance().setDoNotAskAgain(rubyDebugPanel.isDoNotAskAgain());
        }
        return Util.isValidRDebugIDEGemInstalled();
    }
    
    static void installRubyDebugGem() {
        final Gem[] gems = new Gem[] {
            new Gem(Util.RUBY_DEBUG_IDE_NAME, null, null)
        };
        // TODO is this really needed to force "Gem files reload"? If so should
        // not be it done automatically in install() and similar methods?
        Runnable installationComplete = new Runnable() {
            public void run() {
                RubyInstallation.getInstance().recomputeRoots();
            }
        };
        new GemManager().install(gems, null, null, false, false, null, true, true, installationComplete);
        if (!Util.isValidRDebugIDEGemInstalled()) {
            Util.showWarning(getMessage("Util.fast.debugger.install.failed"));
        }
    }
    
    private static String getMessage(final String key) {
        return NbBundle.getMessage(Util.class, key);
    }
    
    private static void turnOnRubyDebugOptions() {
        DebuggerPreferences prefs = DebuggerPreferences.getInstance();
        if (Util.isValidRDebugIDEGemInstalled()) {
            prefs.setUseClassicDebugger(false);
        }
    }
    
}
