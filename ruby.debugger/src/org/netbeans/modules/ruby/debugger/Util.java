/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.ruby.debugger;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.DebuggerPreferences;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor.Message;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author Martin Krauskopf
 */
public final class Util {
    
    public static final Logger LOGGER = Logger.getLogger(Util.class.getName());
    
    private static final String SPECIFICATIONS = "specifications"; // NOI18N
    
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

    public static String findRDebugExecutable(final RubyPlatform platform) {
        return platform.findExecutable(rdebugPattern());
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
    
    static boolean offerToInstallFastDebugger(final RubyPlatform platform) {
        return Util.ensureRubyDebuggerIsPresent(platform, false, "RubyDebugger.askMessage");
    }
    
    static boolean ensureRubyDebuggerIsPresent(final RubyPlatform platform,
            final boolean strict, final String messageKey) {
        if (!platform.hasRubyGemsInstalled()) {
            return false;
        }
        String problems = platform.getFastDebuggerProblemsInHTML();
        if (problems == null) {
            turnOnRubyDebugOptions(platform);
            return true;
        }
        if (!strict && DebuggerPreferences.getInstance().isDoNotAskAgain()) {
            return false;
        }
        String message = NbBundle.getMessage(RubyDebugger.class, messageKey, problems);
        RubyDebugInstallPanel rubyDebugPanel = new RubyDebugInstallPanel(strict, message);
        DialogDescriptor descriptor = new DialogDescriptor(rubyDebugPanel,
                NbBundle.getMessage(Util.class, "Util.installation.panel.title"));

        JButton installButton = new JButton();
        Mnemonics.setLocalizedText(installButton, getMessage("Util.installation.panel.installbutton")); // NOI18N
        JButton nonInstallButton = new JButton();
        if (strict) {
            Mnemonics.setLocalizedText(nonInstallButton, getMessage("Util.installation.panel.cancelbutton")); // NOI18N
        } else {
            Mnemonics.setLocalizedText(nonInstallButton, getMessage("Util.installation.panel.continuebutton")); // NOI18N
        }
        Object[] options = new Object[] { installButton, nonInstallButton };
        descriptor.setOptions(options);
        if (DialogDisplayer.getDefault().notify(descriptor) == installButton) {
            if (platform.installFastDebugger()) {
                turnOnRubyDebugOptions(platform);
            } else {
                Util.showWarning(getMessage("Util.fast.debugger.install.failed"));
            }
        }
        if (!strict) {
            DebuggerPreferences.getInstance().setDoNotAskAgain(rubyDebugPanel.isDoNotAskAgain());
        }
        return platform.hasFastDebuggerInstalled();
    }
    
    private static String getMessage(final String key) {
        return NbBundle.getMessage(Util.class, key);
    }
    
    private static void turnOnRubyDebugOptions(final RubyPlatform platform) {
        DebuggerPreferences prefs = DebuggerPreferences.getInstance();
        if (platform.hasFastDebuggerInstalled()) {
            prefs.setUseClassicDebugger(platform, false);
        }
    }
    
}
